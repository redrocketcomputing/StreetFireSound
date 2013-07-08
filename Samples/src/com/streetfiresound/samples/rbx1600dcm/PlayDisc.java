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
 * $Id: PlayDisc.java,v 1.1 2005/02/27 22:59:58 stephen Exp $
 */
package com.streetfiresound.samples.rbx1600dcm;

import org.havi.dcm.rmi.DcmClient;
import org.havi.dcm.types.HUID;
import org.havi.fcm.avdisc.constants.ConstAvDiscPlayMode;
import org.havi.fcm.avdisc.constants.ConstAvDiscTransportMode;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.AvDiscCurrentState;
import org.havi.fcm.rmi.FcmClient;
import org.havi.fcm.rmi.FcmNotificationMessageBackHelper;
import org.havi.fcm.rmi.FcmNotificationMessageBackListener;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.fcm.types.SubscribeNotification;
import org.havi.system.SoftwareElement;
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
import com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstPositionReportMode;
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
public class PlayDisc extends HaviCommandLineApplication implements FcmNotificationMessageBackListener
{
  private final static OperationCode NOTIFICATION_OPCODE = new OperationCode((short)0xffff, (byte)0xff);
  private final static short POSITION_INDICATOR = com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstFcmAttributeIndicator.CURRENT_POSITION;
  private final static short STATE_INDICATOR = org.havi.fcm.avdisc.constants.ConstFcmAttributeIndicator.CURRENT_STATE;

  private Object done = new Object();
  
  /**
   * 
   */
  public PlayDisc()
  {
    super();
  }

  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Look for match GUID
      GUID matchGuid = GuidUtil.fromString(ConfigurationProperties.getInstance().getProperties().getProperty("match.guid", "ff:ff:ff:ff:ff:ff:ff"));

      // Create software element
      SoftwareElement softwareElement = new SoftwareElement();
      
      // Attach a notification event helper
      FcmNotificationMessageBackHelper fcmNotificationHelper = new FcmNotificationMessageBackHelper(softwareElement, NOTIFICATION_OPCODE, this);
      softwareElement.addHaviListener(fcmNotificationHelper);
      
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
      System.out.println("getting first AVDISC FCM");
      SEID[] fcmSeids = dcmClient.getFcmSeidListSync(0);
      AvDiscClient avDiscClient = null;
      FcmClient fcmClient = null;
      SonyJukeboxClient sonyClient = null;
      for (int i = 0; i < fcmSeids.length; i++)
      {
        // Get the HUID of the FCM
        fcmClient = new FcmClient(softwareElement, fcmSeids[i]);
        HUID huid = fcmClient.getHuidSync(0);
        
        // Check interface id
        if (huid.getInterfaceId() == ConstRbx1600DcmInterfaceId.RBX1600_JUKEBOX_FCM)
        {
          // Found one
          avDiscClient = new AvDiscClient(softwareElement, fcmSeids[i]);
          sonyClient = new SonyJukeboxClient(softwareElement, fcmSeids[i]);
          break;
        }
      }
      
      // Ensure we found a fcm
      if (avDiscClient == null)
      {
        System.out.println("no AVDISC FCM found");
        System.exit(1);
      }
      
      // Subscribe to transports state and position notifications
      System.out.println("subscribing to notifications");
      sonyClient.setPositionReportModeSync(0, ConstPositionReportMode.SECOND);
      
      // Stop player
      avDiscClient.stopSync(0, ConstDirection.OUT, (short)0);
      
      // Subscibe to state notification
      SubscribeNotification notification = fcmClient.subscribeNotificationSync(0, STATE_INDICATOR, new byte[0], ConstComparisonOperator.ANY, NOTIFICATION_OPCODE);
      notification = fcmClient.subscribeNotificationSync(0, POSITION_INDICATOR, new byte[0], ConstComparisonOperator.ANY, NOTIFICATION_OPCODE);

      // Play disc
      avDiscClient.playSync(0, ConstAvDiscPlayMode.DIRECT, (short)0, (short)4, (short)0);
      
      // Wait until done
      synchronized(done)
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
  public void fcmNotification(short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
  {
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
          if (state.getTransportState().getMode() != ConstAvDiscPlayMode.DIRECT)
          {
            // Drop
            return;
          }
          
          // Log the new state
          System.out.println("new state: " + state.getTransportState().getState());
          
          if (state.getTransportState().getState() == ConstAvDiscTransportMode.STOP)
          {
            // All done
            synchronized(done)
            {
              done.notify();
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
  }
  
  public static void main(String[] args)
  {
    // Create application
    PlayDisc application = new PlayDisc();
    
    // Run application
    application.run();
    
    // All done
    System.exit(0);
    
  }
}
