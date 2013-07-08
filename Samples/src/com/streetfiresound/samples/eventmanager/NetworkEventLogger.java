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
 * $Id: NetworkEventLogger.java,v 1.1 2005/02/22 03:50:49 stephen Exp $
 */
package com.streetfiresound.samples.eventmanager;

import org.havi.system.SoftwareElement;
import org.havi.system.cmmip.rmi.GuidListReadyEventNotificationListener;
import org.havi.system.cmmip.rmi.NetworkResetEventNotificationListener;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.msg.rmi.MsgErrorEventNotificationListener;
import org.havi.system.msg.rmi.MsgLeaveEventNotificationListener;
import org.havi.system.msg.rmi.MsgTimeoutEventNotificationListener;
import org.havi.system.msg.rmi.SystemReadyEventNotificationListener;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;
import org.havi.system.types.SystemEventId;

import com.redrocketcomputing.havi.system.HaviApplication;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NetworkEventLogger extends HaviApplication implements NetworkResetEventNotificationListener, GuidListReadyEventNotificationListener, MsgLeaveEventNotificationListener, MsgTimeoutEventNotificationListener, MsgErrorEventNotificationListener, SystemReadyEventNotificationListener
{
  private SoftwareElement softwareElement;
  private EventManagerNotificationServerHelper serverHelper;
  
  /**
   * 
   */
  public NetworkEventLogger(String[] args)
  {
    super(args);
    
    try
    {
      // Construct software elemenet
      softwareElement = new SoftwareElement();
      
      // Construct server helper
      serverHelper = new EventManagerNotificationServerHelper(softwareElement);
      
      // Subscribe to event
      serverHelper.addEventSubscription(new SystemEventId(ConstSystemEventType.NETWORK_RESET), this);
      serverHelper.addEventSubscription(new SystemEventId(ConstSystemEventType.GUID_LIST_READY), this);
      serverHelper.addEventSubscription(new SystemEventId(ConstSystemEventType.MSG_LEAVE), this);
      serverHelper.addEventSubscription(new SystemEventId(ConstSystemEventType.MSG_ERROR), this);
      serverHelper.addEventSubscription(new SystemEventId(ConstSystemEventType.MSG_TIMEOUT), this);
      serverHelper.addEventSubscription(new SystemEventId(ConstSystemEventType.SYSTEM_READY), this);
    }
    catch (HaviException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      System.exit(1);
    }
  }

  /* (non-Javadoc)
   * @see org.havi.system.cmmip.rmi.NetworkResetEventNotificationListener#networkResetEventNotification(org.havi.system.types.SEID)
   */
  public void networkResetEventNotification(SEID posterSeid)
  {
    System.out.println("network reset");
  }

  /* (non-Javadoc)
   * @see org.havi.system.cmmip.rmi.GuidListReadyEventNotificationListener#guidListReadyEventNotification(org.havi.system.types.SEID, org.havi.system.types.GUID[], org.havi.system.types.GUID[], org.havi.system.types.GUID[], org.havi.system.types.GUID[])
   */
  public void guidListReadyEventNotification(SEID posterSeid, GUID[] activeGuidList, GUID[] nonactiveGuidList, GUID[] goneDevices, GUID[] newDevices)
  {
    System.out.println("GUID list ready");
  }

  /* (non-Javadoc)
   * @see org.havi.system.msg.rmi.MsgLeaveEventNotificationListener#msgLeaveEventNotification(org.havi.system.types.SEID, org.havi.system.types.SEID)
   */
  public void msgLeaveEventNotification(SEID posterSeid, SEID seid)
  {
    System.out.println("gone " + seid);
  }

  /* (non-Javadoc)
   * @see org.havi.system.msg.rmi.MsgTimeoutEventNotificationListener#msgTimeoutEventNotification(org.havi.system.types.SEID, org.havi.system.types.SEID)
   */
  public void msgTimeoutEventNotification(SEID posterSeid, SEID seid)
  {
    System.out.println("timeout " + seid);
  }

  /* (non-Javadoc)
   * @see org.havi.system.msg.rmi.MsgErrorEventNotificationListener#msgErrorEventNotification(org.havi.system.types.SEID, org.havi.system.types.SEID, short, org.havi.system.types.Status)
   */
  public void msgErrorEventNotification(SEID posterSeid, SEID seid, short attempts, Status error)
  {
    System.out.println("error on " + seid + " " + attempts + " attempts with " + error);
  }


  /* (non-Javadoc)
   * @see org.havi.system.msg.rmi.SystemReadyEventNotificationListener#systemReadyEventNotification(org.havi.system.types.SEID, org.havi.system.types.SEID)
   */
  public void systemReadyEventNotification(SEID posterSeid, SEID seid)
  {
    System.out.println("system ready from " + seid);
  }
  
  public static void main(String[] args)
  {
    // Create the application
    NetworkEventLogger application = new NetworkEventLogger(args);
    
    // Run the application
    application.run();
  }
}
