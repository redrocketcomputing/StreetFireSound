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
 * $Id: ScanDiscs.java,v 1.6 2005/03/16 04:25:46 stephen Exp $
 */
package com.streetfiresound.samples.rbx1600dcm;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.havi.dcm.rmi.DcmClient;
import org.havi.dcm.types.HUID;
import org.havi.dcm.types.HaviDcmException;
import org.havi.fcm.avdisc.constants.AvDiscConstant;
import org.havi.fcm.avdisc.constants.ConstAvDiscTransportMode;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.rmi.AvDiscItemListChangedEventNotificationListener;
import org.havi.fcm.avdisc.types.AvDiscCapabilities;
import org.havi.fcm.avdisc.types.AvDiscCurrentState;
import org.havi.fcm.avdisc.types.AvDiscTransportState;
import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.rmi.FcmClient;
import org.havi.fcm.rmi.FcmNotificationMessageBackHelper;
import org.havi.fcm.rmi.FcmNotificationMessageBackListener;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.SubscribeNotification;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstComparisonOperator;
import org.havi.system.constants.ConstDirection;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.eventmanager.rmi.EventManagerServerHelper;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstAvDiscScanMode;
import com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxClient;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxException;
import com.redrocketcomputing.havi.system.HaviApplication;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.concurrent.LinkedQueue;
import com.redrocketcomputing.util.concurrent.WaitableInt;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ScanDiscs extends HaviApplication implements FcmNotificationMessageBackListener, MsgWatchOnNotificationListener, AvDiscItemListChangedEventNotificationListener 
{
  private final static OperationCode NOTIFICATION_OPCODE = new OperationCode((short)0xffff, (byte)0xff);
  private final static int MULTI_SLOT_CAPABILITY = org.havi.fcm.avdisc.constants.ConstAvDiscCapability.MULTI_SLOT;
  private final static int SCAN_CAPABILITY = com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstAvDiscCapability.SCAN;
  private final static short POSITION_INDICATOR = com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstFcmAttributeIndicator.CURRENT_POSITION;
  private final static short STATE_INDICATOR = org.havi.fcm.avdisc.constants.ConstFcmAttributeIndicator.CURRENT_STATE;
  
  private final static int MODE = ConstAvDiscScanMode.SCAN_ALL;
  
  private int totalSlots = 0;
  private int slotsScanned = 0;
  private WaitableInt done = new WaitableInt(0);
  private Map notificationMap = new ListMap();
  private FcmClient[] fcmClients;
  private AvDiscClient[] avDiscClients;
  private SonyJukeboxClient[] sonyJukeboxClients;
  private LinkedQueue channel = new LinkedQueue();
  private LookupTask lookupTask;
  private EventManagerNotificationServerHelper eventHelper;
  private GUID matchGuid;
  
  /**
   * @param args
   */
  public ScanDiscs(String[] args)
  {
    super(args);
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscItemListChangedEventNotificationListener#avDiscItemListChangedEventNotification(org.havi.system.types.SEID, short)
   */
  public void avDiscItemListChangedEventNotification(SEID posterSeid, short listNumber)
  {
    try
    {
      if (matchGuid.equals(GUID.BROADCAST) || posterSeid.getGuid().equals(matchGuid))
      {
        // Post the lookup thread
        channel.put(lookupTask.new LookupRequest(posterSeid, listNumber));
      }
    }
    catch (InterruptedException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "avDiscItemListChangedEventNotification", e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    System.out.println("lost contact with " + targetSeid);
    System.exit(1);
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmNotificationMessageBackListener#fcmNotification(short, short, byte[])
   */
  public void fcmNotification(short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
  {
    // Verify that this notification is for us
    Integer id = new Integer(notificationId);
    if (!notificationMap.containsKey(id))
    {
      // Log error and drop
      LoggerSingleton.logError(this.getClass(), "fcmNotification", "bad notification Id: " + notificationId);
      return;
    }
    
    // Handle based on type
    switch (attributeIndicator)
    {
      case POSITION_INDICATOR:
      {
        // Update scanned slots
        slotsScanned++;
        
        // Log message
        double complete = (float)slotsScanned / (float)totalSlots * 100.0;
        System.out.println(slotsScanned + " scanned out of " + totalSlots + ", " + (int)complete + "% complete");
        break;
      }

      case STATE_INDICATOR:
      {
        // Decrement outstand player
        done.decrement();

        break;
      }
      default:
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "fcmNotification", "bad attribute indicator: " + attributeIndicator);
      }
    }
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
      matchGuid = GuidUtil.fromString(ConfigurationProperties.getInstance().getProperties().getProperty("match.guid", "ff:ff:ff:ff:ff:ff:ff"));

      // Create software element
      SoftwareElement softwareElement = new SoftwareElement();

      // Start lookup task
      lookupTask = new LookupTask(softwareElement, channel);
      
      // Create event helper and register to events
      eventHelper = new EventManagerNotificationServerHelper(softwareElement);
      eventHelper.addEventSubscription(new SystemEventId(ConstSystemEventType.AVDISC_ITEM_LIST_CHANGED), this);
      
      // Rbx1600 DCMs
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
      DcmClient dcmClient = new DcmClient(softwareElement, dcmSeid);
      if (!dcmClient.setPowerStateSync(0, true))
      {
        // Bad
        System.out.println("can not turn on power to DCM");
        System.exit(1);
      }
      
      // Build Sony Jukebox FCM SEID list
      System.out.println("enumerating sony jukeboxes");
      List fcmClientList = new ArrayList();
      List avDiscClientList = new ArrayList();
      List sonyJukeboxClientList = new ArrayList();
      SEID[] dcmSeidList = dcmClient.getFcmSeidListSync(0);
      MsgWatchOnNotificationHelper watchHelper = new MsgWatchOnNotificationHelper(softwareElement);
      for (int i = 0; i < dcmSeidList.length; i++)
      {
        // Create an fcm client
        FcmClient fcmClient = new FcmClient(softwareElement, dcmSeidList[i]);
        
        // Get the HUID which contains the interface id
        HUID huid = fcmClient.getHuidSync(0);
        
        // Check for match
        if (huid.getInterfaceId() == ConstRbx1600DcmInterfaceId.RBX1600_JUKEBOX_FCM)
        {
          // Add to the lists
          fcmClientList.add(fcmClient);
          avDiscClientList.add(new AvDiscClient(softwareElement, dcmSeidList[i]));
          sonyJukeboxClientList.add(new SonyJukeboxClient(softwareElement, dcmSeidList[i]));
          
          // Add watch
          watchHelper.addListenerEx(dcmSeidList[i], this);
        }
      }
      FcmClient[] fcmClients = (FcmClient[])fcmClientList.toArray(new FcmClient[fcmClientList.size()]);
      AvDiscClient[] avDiscClients = (AvDiscClient[])avDiscClientList.toArray(new AvDiscClient[avDiscClientList.size()]);
      SonyJukeboxClient[] sonyJukeboxClients = (SonyJukeboxClient[])sonyJukeboxClientList.toArray(new SonyJukeboxClient[sonyJukeboxClientList.size()]);
      System.out.println("found " + fcmClients.length + " sony jukeboxes");
      
      // Retrieve capacity and verify scan capabilities
      System.out.println("retrieving player capabilities");
      AvDiscCapabilities[] capabilities = new AvDiscCapabilities[avDiscClients.length];
      totalSlots = 0;
      for (int i = 0; i < capabilities.length; i++)
      {
        // Retrieve capabilities
        capabilities[i] = avDiscClients[i].getCapabilitySync(0);
        
        // Check for multi slot capabilities
        if (capabilities[i].getCapabilityList().length <= MULTI_SLOT_CAPABILITY || !capabilities[i].getCapabilityList()[MULTI_SLOT_CAPABILITY])
        {
          System.out.println("player " + i + " does not have MULTI_SLOT capability");
          System.exit(1);
        }
        
        // Check for scan capabilities and multi-slot
        if (capabilities[i].getCapabilityList().length <= SCAN_CAPABILITY || !capabilities[i].getCapabilityList()[SCAN_CAPABILITY])
        {
          System.out.println("player " + i + " does not have SCAN capability");
          System.exit(1);
        }
        
        // Update total slots
        totalSlots += capabilities[i].getCapacity();
      }
      
      // Stop all players
      System.out.println("stopping all players");
      for (int i = 0; i < avDiscClients.length; i++)
      {
        avDiscClients[i].stopSync(0, 0, (short)0);
      }
      
      // Build stop state indicator
      AvDiscCurrentState matchingState = new AvDiscCurrentState(new AvDiscTransportState(ConstAvDiscTransportMode.STOP, MODE), ConstDirection.OUT, (short)0);
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
      matchingState.marshal(hbaos);
      byte[] stateAttributeValue = hbaos.toByteArray();
      
      // Subscribe to transports state and position notifications
      System.out.println("subscribing to notifications");
      FcmNotificationMessageBackHelper notificationHelper = new FcmNotificationMessageBackHelper(softwareElement, NOTIFICATION_OPCODE, this);
      softwareElement.addHaviListener(notificationHelper);
      for (int i = 0; i < avDiscClients.length; i++)
      {
        // Subscibe to state notification
        SubscribeNotification notification = fcmClients[i].subscribeNotificationSync(0, STATE_INDICATOR, stateAttributeValue, ConstComparisonOperator.EQU, NOTIFICATION_OPCODE);
        notificationMap.put(new Integer(notification.getNotificationId()), new Integer(i));

        // Subscribe to position notification
        notification = fcmClients[i].subscribeNotificationSync(0, POSITION_INDICATOR, new byte[0], ConstComparisonOperator.ANY, NOTIFICATION_OPCODE);
        notificationMap.put(new Integer(notification.getNotificationId()), new Integer(i));
      }
      
      // Start scaning
      System.out.println("starting scanning");
      for (int i = 0; i < sonyJukeboxClients.length; i++)
      {
        sonyJukeboxClients[i].scanSync(0, MODE, (short)1, (short)capabilities[i].getCapacity());
      }
      
      // Wait until all player stop
      done.set(sonyJukeboxClients.length);
      done.whenEqual(0, null);
    }
    catch (HaviMsgListenerExistsException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (HaviMarshallingException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (HaviException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args)
  {
    // Create the application
    ScanDiscs application = new ScanDiscs(args);
    
    // Run the application
    application.run();
    
    // All done
    System.exit(0);
  }
}
