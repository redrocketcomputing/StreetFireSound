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
 * $Id: PlayRandomTracks.java,v 1.1 2005/02/27 22:59:58 stephen Exp $
 */
package com.streetfiresound.samples.rbx1600dcm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.havi.dcm.rmi.DcmClient;
import org.havi.fcm.avdisc.constants.ConstAvDiscPlayMode;
import org.havi.fcm.avdisc.constants.ConstAvDiscTransportMode;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.types.AvDiscCapabilities;
import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.AvDiscCurrentState;
import org.havi.fcm.avdisc.types.AvDiscTransportState;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.rmi.FcmClient;
import org.havi.fcm.rmi.FcmNotificationMessageBackHelper;
import org.havi.fcm.rmi.FcmNotificationMessageBackListener;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.fcm.types.SubscribeNotification;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstComparisonOperator;
import org.havi.system.constants.ConstDirection;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;

import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxClient;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.commands.HaviCommandLineApplication;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PlayRandomTracks extends HaviCommandLineApplication
{
  private static class TrackEntry
  {
    public SEID remoteSeid;
    public short list;
    public short index;
    
    public TrackEntry(SEID remoteSeid, short list, short index)
    {
      this.remoteSeid = remoteSeid;
      this.list = list;
      this.index = index;
    }
  }
  private static class FcmNotificationDispatcher implements FcmNotificationMessageBackListener
  {
    private SEID remoteSeid;
    private PlayRandomTracks parent;

    public FcmNotificationDispatcher(PlayRandomTracks parent, SEID remoteSeid)
    {
      // Save the remote SEID
      this.parent = parent;
      this.remoteSeid = remoteSeid;
    }
    
    public void fcmNotification(short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
    {
      parent.fcmNotification(remoteSeid, notificationId, attributeIndicator, value);
    }
  }
  
  private final static short POSITION_INDICATOR = com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstFcmAttributeIndicator.CURRENT_POSITION;
  private final static short STATE_INDICATOR = org.havi.fcm.avdisc.constants.ConstFcmAttributeIndicator.CURRENT_STATE;
  
  private SoftwareElement softwareElement;
  private Stack playStack = new Stack();
  private TrackEntry currentTrack = null;
  private boolean running = false;
  
  private Object done = new Object();

  /**
   * 
   */
  public PlayRandomTracks()
  {
    super();
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Sleep for 5 seconds to let the network settle
      Thread.sleep(5000);

      // Look for match GUID
      GUID matchGuid = GuidUtil.fromString(ConfigurationProperties.getInstance().getProperties().getProperty("match.guid", "ff:ff:ff:ff:ff:ff:ff"));

      // Create software element
      softwareElement = new SoftwareElement();
      
      // Rbx1600 DCMs
      System.out.println("finding RBX1600 DCMs");
      SimpleAttributeTable attributes = new SimpleAttributeTable();
      attributes.setSoftwareElementType(ConstSoftwareElementType.DCM);
      attributes.setInterfaceId(ConstRbx1600DcmInterfaceId.RBX1600_DCM);
      RegistryClient registryClient = new RegistryClient(softwareElement);
      QueryResult result = registryClient.getElementSync(0, attributes.toQuery());
      
      // Look for matching GUID
      SEID dcmSeid = null;
      for (int i = 0; i < result.getSeidList().length; i++)
      {
        // Check for match GUID
        if (matchGuid.equals(GUID.BROADCAST) || result.getSeidList()[i].getGuid().equals(matchGuid))
        {
          // Found it
          dcmSeid = result.getSeidList()[i];
          break;
        }
      }
      
      // Check not found
      if (dcmSeid == null)
      {
        System.out.println(matchGuid.toString() + " not found");
        System.exit(1);
      }
      
      // Power on the DCM
      System.out.println("turning on power");
      DcmClient dcmClient = new DcmClient(softwareElement, dcmSeid);
      if (!dcmClient.setPowerStateSync(0, true))
      {
        // Bad
        System.out.println("can not turn on power to DCM");
        System.exit(1);
      }
      
      // Get the FCM SEIDs
      System.out.println("getting AVDISC Fcms");
      SEID[] fcmSeids = dcmClient.getFcmSeidListSync(0);
      List fcmSeidList = new ArrayList();
      List avDiscList = new ArrayList();
      byte notificationOperationId = (byte)0xff;
      for (int i = 0; i < fcmSeids.length; i++)
      {
        // Get the HUID of the FCM
        FcmClient fcmClient = new FcmClient(softwareElement, fcmSeids[i]);
        if (fcmClient.getFcmTypeSync(0) == ConstSoftwareElementType.AVDISC_FCM)
        {
          // Found one
          fcmSeidList.add(fcmSeids[i]);
          AvDiscClient avDiscClient = new AvDiscClient(softwareElement, fcmSeids[i]); 
          avDiscList.add(avDiscClient);
          
          // Stop the player
          avDiscClient.stopSync(0, ConstDirection.OUT, (short)0);
          
          // Subscribe to notifications saving the notificaiton id of the state notificaiton
          OperationCode notificationOpCode = new OperationCode(ConstApiCode.ANY, notificationOperationId);
          softwareElement.addHaviListener(new FcmNotificationMessageBackHelper(softwareElement, notificationOpCode, new FcmNotificationDispatcher(this, fcmSeids[i])));
          SubscribeNotification notification = fcmClient.subscribeNotificationSync(0, POSITION_INDICATOR, new byte[0], ConstComparisonOperator.ANY, notificationOpCode);
          notification = fcmClient.subscribeNotificationSync(0, STATE_INDICATOR, new byte[0], ConstComparisonOperator.ANY, notificationOpCode);
          notificationOperationId--;
        }
      }
      
      // Build player tracks
      System.out.println("building track entries");
      for (int i = 0; i < avDiscList.size(); i++)
      {
        // Extract client
        AvDiscClient avDiscClient = (AvDiscClient)avDiscList.get(i);
        SEID remoteSeid = (SEID)fcmSeidList.get(i);
        
        // Get capabilities
        AvDiscCapabilities capabilities = avDiscClient.getCapabilitySync(0);
        
        // Get root item index
        ItemIndex[] root = avDiscClient.getItemListSync(0, (short)0);
        
        // Add track entries for non empty slots and know title
        for (short j = 1; j < root.length; j++)
        {
          if (!root[j].getTitle().equals("EMPTY") && !root[j].getTitle().equals(""))
          {
            // Get full index
            ItemIndex[] entryIndex = avDiscClient.getItemListSync(0, j);
            
            // Add tracks
            for (short k = 1; k < entryIndex.length; k++)
            {
              // Push on to stack
              playStack.push(new TrackEntry(remoteSeid, j, k));
            }
          }
        }
      }
      
      // Shuffle array
      Collections.shuffle(playStack);
      System.out.println("add " + playStack.size() + " tracks");
      
      // Start running
      playNextTrack();
      
      // Mark as running
      running = true;

      // Wait until notification
      synchronized (done)
      {
        done.wait();
      }
    }
    catch (HaviException e)
    {
      // dump stack trace
      e.printStackTrace();
      System.exit(1);
    }
    catch (InterruptedException e)
    {
      // dump stack trace
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmNotificationMessageBackListener#fcmNotification(short, short, byte[])
   */
  public void fcmNotification(SEID remoteSeid, short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
  {
    // Drop if not running
    if (!running)
    {
      return;
    }
    
    try
    {
      // Handle based on indicator
      switch (attributeIndicator)
      {
        case POSITION_INDICATOR:
        {
          // Unmarshall
          AvDiscCounterValue position = new AvDiscCounterValue(new HaviByteArrayInputStream(value));
          
          // Log position
          String location = "Location[" + position.getList() + ":" + position.getIndex() + "]";
          String absoluteTime = position.getAbsolute().getHour() + ":" + position.getAbsolute().getMinute() + ":" + position.getAbsolute().getSec() + ":" + position.getAbsolute().getFrame(); 
          String relativeTime = position.getRelative().getHour() + ":" + position.getRelative().getMinute() + ":" + position.getRelative().getSec() + ":" + position.getRelative().getFrame(); 
          System.out.println(location + " " + absoluteTime + " " + relativeTime);
          
          break;
        }
        
        case STATE_INDICATOR:
        {
          // Unmarshall
          AvDiscCurrentState state = new AvDiscCurrentState(new HaviByteArrayInputStream(value));
          
          // Make sure Play mode match
          if (state.getTransportState().getMode() != ConstAvDiscPlayMode.DIRECT_1)
          {
            LoggerSingleton.logDebugCoarse(this.getClass(), "fcmNotification", "dropping because mode does not match: " + state.getTransportState().getState());
            
            // Drop
            return;
          }
          
          // Make sure the notification is for the current entry
          if (currentTrack == null || !remoteSeid.equals(currentTrack.remoteSeid))
          {
            LoggerSingleton.logDebugCoarse(this.getClass(), "fcmNotification", "dropping because SEID does not match: " +  notificationId);

            // Drop
            return;
          }
          
          // Only if current player is done
          if (currentTrack != null && state.getTransportState().getState() == ConstAvDiscTransportMode.STOP)
          {
            // Try to start next track
            if (!playNextTrack())
            {
              // All done
              synchronized (done)
              {
                done.notifyAll();
              }
            }
          }
          
          break;
        }
      }
    }
    catch (HaviUnmarshallingException e)
    {
      // Translate
      LoggerSingleton.logError(this.getClass(), "fcmNotification", e.toString());
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
    catch (HaviException e)
    {
      LoggerSingleton.logError(this.getClass(), "fcmNotification", e.toString());
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }
  
  private boolean playNextTrack() throws HaviException
  {
    // Check for empty
    if (playStack.isEmpty())
    {
      return false;
    }
    
    // Pop top of stack
    currentTrack = (TrackEntry)playStack.pop();
    LoggerSingleton.logDebugCoarse(this.getClass(), "playNextTrack", "popped " + currentTrack.list + ":" + currentTrack.index);
    
    // Check for paused
    AvDiscClient avDiscClient = new AvDiscClient(softwareElement, currentTrack.remoteSeid);
    AvDiscTransportState transportState = avDiscClient.getStateSync(0, ConstDirection.OUT, (short)0);
    AvDiscCounterValue currentPosition = avDiscClient.getPositionSync(0, ConstDirection.OUT, (short)0);
    if (transportState.getState() == ConstAvDiscTransportMode.PAUSE && currentPosition.getList() == currentTrack.list && currentPosition.getIndex() == currentTrack.index)
    {
      // Just resume
      LoggerSingleton.logDebugCoarse(this.getClass(), "playNextTrack", "resuming " + currentTrack.list + ":" + currentTrack.index);
      avDiscClient.resumeSync(0, (short)0);
    }
    else
    {
      // Need to play it
      LoggerSingleton.logDebugCoarse(this.getClass(), "playNextTrack", "playing " + currentTrack.list + ":" + currentTrack.index);
      avDiscClient.playSync(0, ConstAvDiscPlayMode.DIRECT_1, (short)0, currentTrack.list, currentTrack.index);
    }
    
    // Log meta data
    ItemIndex[] itemIndex = avDiscClient.getItemListSync(0, currentTrack.list);
    System.out.println("Playing track: " + itemIndex[currentTrack.index].getTitle() + " from disc: " + itemIndex[0].getTitle() + " by artist: " + itemIndex[currentTrack.index].getArtist());
    
    // Peek at top and cue if not the same SEID
    if (!playStack.isEmpty())
    {
      // Get top entry
      TrackEntry topEntry = (TrackEntry)playStack.peek();
      // Check to make sure we are not the current player
      if (!currentTrack.remoteSeid.equals(topEntry.remoteSeid))
      {
        // Create client and cue
        LoggerSingleton.logDebugCoarse(this.getClass(), "playNextTrack", "cue " + topEntry.list + ":" + topEntry.index);
        SonyJukeboxClient sonyJukeboxClient = new SonyJukeboxClient(softwareElement, topEntry.remoteSeid);
        sonyJukeboxClient.cueSync(0, ConstAvDiscPlayMode.DIRECT_1, (short)0, topEntry.list, topEntry.index);
      }
    }
    
    // All done
    LoggerSingleton.logDebugCoarse(this.getClass(), "playNextTrack", "done");
    return true;
  }
  
  public static void main(String[] args)
  {
    // Create application
    PlayRandomTracks application = new PlayRandomTracks();
    
    // Run application
    application.run();
    
    // All done
    System.exit(0);
    
  }
}
