/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 * 
 * Created on Oct 9, 2004 by stephen
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
 */
package com.streetfiresound.samples.amplifier;

import java.io.IOException;

import org.havi.fcm.amplifier.constants.ConstFcmAttributeIndicator;
import org.havi.fcm.amplifier.rmi.AmplifierClient;
import org.havi.fcm.rmi.FcmClient;
import org.havi.fcm.rmi.FcmNotificationMessageBackHelper;
import org.havi.fcm.rmi.FcmNotificationMessageBackListener;
import org.havi.fcm.types.HaviFcmException;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstComparisonOperator;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.Application;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.util.concurrent.Latch;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ToggleMute extends Application implements FcmNotificationMessageBackListener
{
  private Latch notifier = new Latch();
  private byte[] notifierValue = null;
  private short notificationId = -1;
  
  /**
   * @param args
   */
  public ToggleMute(String[] args)
  {
    super(args);
  }
  
  public void run()
  {
    try
    {
      // Create software element
      SoftwareElement softwareElement = new SoftwareElement();
      Thread.sleep(5000);
      
      // Create a registry client to find all Amplifier FCM
      RegistryClient registryClient = new RegistryClient(softwareElement);
      SimpleAttributeTable queryTable = new SimpleAttributeTable();
      queryTable.setSoftwareElementType(ConstSoftwareElementType.AMPLIFIER_FCM);
      queryTable.setInterfaceId(ConstRbx1600DcmInterfaceId.RBX1600_AMPLIFIER_FCM);
      
      // Run the query
      SEID[] seids = registryClient.getElementSync(0, queryTable.toQuery()).getSeidList();
      if (seids.length == 0)
      {
        LoggerSingleton.logInfo(this.getClass(), "run", "no AmplifierFcm found");
        return;
      }
      
      // Register for mute state notifications
      FcmClient fcmClient = new FcmClient(softwareElement, seids[0]);
      byte[] value = new byte[0];
      OperationCode opCode = new OperationCode((short)ConstApiCode.ANY, (byte)0xff);
      softwareElement.addHaviListener(new FcmNotificationMessageBackHelper(softwareElement, opCode, this));
      notificationId = fcmClient.subscribeNotificationSync(0, ConstFcmAttributeIndicator.AMPLIFIER_MUTE, value, ConstComparisonOperator.ANY, opCode).getNotificationId();
      
      // Get mute state for the first one only
      softwareElement.setDebug(true);
      AmplifierClient amplifierClient = new AmplifierClient(softwareElement, seids[0]);
      boolean muteState = amplifierClient.getMuteSync(0);
      LoggerSingleton.logInfo(this.getClass(), "run", seids[0].toString() + " mute state is " + muteState);
      
      // Toggle state
      amplifierClient.setMuteSync(0, !muteState);

      // Wait for notification
      notifier.acquire();
      
      // All good
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(notifierValue);
      boolean newMuteState = hbais.readBoolean();
      
      LoggerSingleton.logInfo(this.getClass(), "run", "mute state changed to " + newMuteState);
    }
    catch (HaviException e)
    {
      System.out.println(e.toString());
    }
    catch (InterruptedException e) 
    {
      System.out.println(e.toString());
    }
    catch (HaviMsgListenerExistsException e)
    {
      System.out.println(e.toString());
    }
    catch (IOException e)
    {
      System.out.println(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmNotificationMessageBackListener#fcmNotification(short, short, org.havi.fcm.types.FcmAttributeValue)
   */
  public void fcmNotification(short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
  {
    // Match notification id
    if (notificationId != this.notificationId)
    {
      LoggerSingleton.logError(this.getClass(), "fcmNotification", "mismatched notificationId: " + this.notificationId + ' ' + notificationId);
      return;
    }
    
    // Check attribute indicator
    if (attributeIndicator != ConstFcmAttributeIndicator.AMPLIFIER_MUTE)
    {
      LoggerSingleton.logError(this.getClass(), "fcmNotification", "wrong indicator: " + attributeIndicator);
      return;
    }

    // Update notifier and release waiter
    notifierValue = value;
    notifier.release();
  }

  public static void main(String[] args) throws Exception
  {
    // Create application
    ToggleMute application = new ToggleMute(args);
    
    // Execute the application
    application.run();

    // Force exit
    LoggerSingleton.logInfo(ToggleMute.class, "main", "EXITING");
    System.exit(0);
  }
}
