/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: PowerStateMonitor.java,v 1.2 2005/03/16 04:23:43 stephen Exp $
 */
package com.streetfiresound.mediamanager.power;

import java.util.Iterator;
import java.util.Set;

import org.havi.applicationmodule.types.HaviApplicationModuleUnidentifiedFailureException;
import org.havi.dcm.rmi.DcmClient;
import org.havi.dcm.rmi.PowerStateChangedEventNotificationListener;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.registry.rmi.MatchFoundMessageBackHelper;
import org.havi.system.registry.rmi.MatchFoundMessageBackListener;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.ListSet;
import com.redrocketcomputing.util.concurrent.Gate;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstAttributeIndicator;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstTransportState;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerClient;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackHelper;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackListener;
import com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.StateAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.SubscriptionResult;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class PowerStateMonitor extends AbstractTask implements MediaPlayerNotificationMessageBackListener, MsgWatchOnNotificationListener, MatchFoundMessageBackListener, PowerStateChangedEventNotificationListener
{
  private final int DEFAULT_POWER_OFF_DELAY = 60000 * 15;
  private final static OperationCode MATCH_OPCODE = new OperationCode((short)0xff00, (byte)0xab);
  private final static OperationCode NOTIFICATION_OPCODE = new OperationCode((short)0xff00, (byte)0xaa);

  private ApplicationModule parent;
  private Set dcmSet = new ListSet();
  private MatchFoundMessageBackHelper matchFoundHelper;
  private MediaPlayerNotificationMessageBackHelper playerNotificationHelper;
  private GUID localGuid;
  private GUID targetGuid;
  private boolean localOnly;
  private RegistryClient registryClient;
  private MediaPlayerClient mediaPlayerClient;
  private int queryId;
  private short notificationId;
  private int powerOffDelay;
  private Gate timer = new Gate();
  private volatile Thread running = null;
  private int currentState;
  
  /**
   * Construct a PowerStateMonitor
   * 
   * @param parent The parent ApplicationModule
   */
  public PowerStateMonitor(ApplicationModule parent) throws HaviException
  {
    // Check the parameter
    if (parent == null)
    {
      // Bad
      throw new IllegalArgumentException("ApplicationModule is null");
    }

    // Save the parameters
    this.parent = parent;

    try
    {
      // Get Local GUID
      Gadp gadp = (Gadp)ServiceManager.getInstance().get(Gadp.class);
      localGuid = gadp.getLocalGuid();

      // Check configuration for local guid only
      localOnly = parent.getConfiguration().getBooleanProperty("local.only", false);
      targetGuid = GuidUtil.fromString(parent.getConfiguration().getProperty("target.guid", "ff:ff:ff:ff:ff:ff:ff:ff"));
      
      // Subscribe to events
      parent.addEventSubscription(new SystemEventId(ConstSystemEventType.POWER_STATE_CHANGED), this);

      // Create and attach a match found helper
      matchFoundHelper = new MatchFoundMessageBackHelper(parent.getSoftwareElement(), MATCH_OPCODE, this);
      parent.getSoftwareElement().addHaviListener(matchFoundHelper, parent.getSoftwareElement().getSystemSeid(ConstSoftwareElementType.REGISTRY));

      // Create registry client
      registryClient = new RegistryClient(parent.getSoftwareElement());

      // Create DCM query
      SimpleAttributeTable dcmQueryTable = new SimpleAttributeTable();
      dcmQueryTable.setSoftwareElementType(ConstSoftwareElementType.DCM);
      dcmQueryTable.setInterfaceId(ConstRbx1600DcmInterfaceId.RBX1600_DCM);

      // Subscribe to query
      int queryId = registryClient.subscribeSync(0, MATCH_OPCODE, dcmQueryTable.toQuery());

      // Perform initial query and add to table
      QueryResult result = registryClient.getElementSync(0, dcmQueryTable.toQuery());
      addDcm(result.getSeidList());
      
      // Create and bind media player notification helper
      playerNotificationHelper = new MediaPlayerNotificationMessageBackHelper(parent.getSoftwareElement(), NOTIFICATION_OPCODE, this);
      parent.getSoftwareElement().addHaviListener(playerNotificationHelper, parent.getSoftwareElement().getSeid());
      
      // Subscribe to transport state notifications
      mediaPlayerClient = new MediaPlayerClient(parent.getSoftwareElement(), parent.getSoftwareElement().getSeid());
      SubscriptionResult subscriptionResult = mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.STATE);
      notificationId = subscriptionResult.getNotificationId();
      
      // Launch timer if current state is NO_MEDIA or STOPPED
      powerOffDelay = parent.getConfiguration().getIntProperty("power.off.delay", DEFAULT_POWER_OFF_DELAY);
      currentState = ((StateAttributeNotification)subscriptionResult.getValue()).getState();
      if (currentState == ConstTransportState.NO_MEDIA || currentState == ConstTransportState.STOP)
      {
        // Launch the power off timer
        timer.reset();
        TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
        taskPool.execute(this);
      }
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new HaviApplicationModuleUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Release all resources
   */
  public void close()
  {
    synchronized(dcmSet)
    {
      // Unbind helpers
      parent.getSoftwareElement().removeHaviListener(playerNotificationHelper);
      parent.getSoftwareElement().removeHaviListener(matchFoundHelper);

      try
      {
        // Unsubscribe from notifications, events and querys
        mediaPlayerClient.unsubscribeNotificationSync(0, notificationId);
        registryClient.unsubscribeSync(0, queryId);
        parent.removeEventSubscription(new SystemEventId(ConstSystemEventType.POWER_STATE_CHANGED), this);
      }
      catch (HaviException e)
      {
        // Just log the error
        LoggerSingleton.logError(this.getClass(), "close", e.toString());
      }
      
      // Cancel any timer
      timer.release();
      
      // Remove watch from SEIDs
      for (Iterator iterator = dcmSet.iterator(); iterator.hasNext();)
      {
        // Extract element
        SEID element = (SEID)iterator.next();
        
        // Turn off watch
        parent.removeMsgWatch(element, this);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "MediaManagerAm::PowerStateMonitor";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "run", "power timer started");
    // Mark us as running
    running = Thread.currentThread();
    
    try
    {
      // Wait for timer gate to release
      if (!timer.attempt(powerOffDelay))
      {
        synchronized(dcmSet)
        {
          // Turn on power on all DCM
          for (Iterator iterator = dcmSet.iterator(); iterator.hasNext();)
          {
            // Extract SEID
            SEID element = (SEID)iterator.next();
            
            try
            {
              LoggerSingleton.logDebugCoarse(this.getClass(), "run", "turning power off");
              // Turn off power
              DcmClient dcmClient = new DcmClient(parent.getSoftwareElement(), element);
              dcmClient.setPowerStateSync(0, false);
            }
            catch (HaviException e)
            {
              // Just log the error
              LoggerSingleton.logError(this.getClass(), "run", e.toString());
            }
          }
        }
      }
    }
    catch (InterruptedException e)
    {
      // Ignore
    }
    finally
    {
      // Mark as not running
      running = null;
    }

    LoggerSingleton.logDebugCoarse(this.getClass(), "run", "power timer done");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackListener#mediaPlayerNotification(int, com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification)
   */
  public void mediaPlayerNotification(int notificationId, AttributeNotification value) throws HaviMediaPlayerException
  {
    try
    {
      synchronized (dcmSet)
      {
        // Cast it up and extract new state
        currentState = ((StateAttributeNotification)value).getState();
        
        // Check to see if we should state the timer task running
        if (running == null && (currentState == ConstTransportState.NO_MEDIA || currentState == ConstTransportState.STOP))
        {
          // Yes, reset the gate and start the timer task
          timer.reset();
          TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
          taskPool.execute(this);
        }
        
        // Check to see if we should terminate the timer
        else if (running != null && currentState != ConstTransportState.NO_MEDIA && currentState != ConstTransportState.STOP)
        {
          // Yes, release the timer thread to allow exit
          timer.release();
        }
      }
    }
    catch (TaskAbortedException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "mediaPlayerNotification", e.toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    synchronized(dcmSet)
    {
      // Remove from the set
      dcmSet.remove(targetSeid);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.havi.system.registry.rmi.MatchFoundMessageBackListener#matchFound(int, org.havi.system.types.SEID[])
   */
  public void matchFound(int queryId, SEID[] seidList) throws HaviRegistryException
  {
    // Verify matching query id
    if (this.queryId == queryId)
    {
      // Forward
      addDcm(seidList);
    }
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.PowerStateChangedEventNotificationListener#powerStateChangedEventNotification(org.havi.system.types.SEID, boolean)
   */
  public void powerStateChangedEventNotification(SEID posterSeid, boolean powerState)
  {
    try
    {
      synchronized (dcmSet)
      {
        // Verify this is a DCM we are managing
        if (!dcmSet.contains(posterSeid))
        {
          // Nope drop it
          return;
        }
        
        LoggerSingleton.logDebugCoarse(this.getClass(), "powerStateChangedEventNotification", "running: " + (running != null) + " currentState: " + currentState);
        
        // Release existing timer
        timer.release();
        
        // Check for power on
        if (powerState && (currentState == ConstTransportState.NO_MEDIA || currentState == ConstTransportState.STOP))
        {
          // Yes, reset the gate and start the timer task
          timer.reset();
          TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
          taskPool.execute(this);
        }
      }
    }
    catch (TaskAbortedException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "mediaPlayerNotification", e.toString());
    }
    
  }
  
  /**
   * Add Dcms to the internal SEID set if GUID configuration matches
   * 
   * @param seids The DCM SEID array to add
   */
  private void addDcm(SEID[] seids)
  {
    synchronized (dcmSet)
    {
      // Loop through the seid look for GUID matches
      for (int i = 0; i < seids.length; i++)
      {
        // Check to see if the adaptor is already in the map
        if (!dcmSet.contains(seids[i]))
        {
          try
          {
            // Check for local only
            if ((!localOnly || localGuid.equals(seids[i].getGuid())) && (targetGuid.equals(GUID.BROADCAST) || targetGuid.equals(seids[i].getGuid())))
            {
              // Add watch
              parent.addMsgWatch(seids[i], this);

              // Add it to the map
              dcmSet.add(seids[i]);
            }
          }
          catch (HaviException e)
          {
            // Log the error
            LoggerSingleton.logError(this.getClass(), "addAdaptors", e.toString());
          }
        }
      }
    }
  }
}