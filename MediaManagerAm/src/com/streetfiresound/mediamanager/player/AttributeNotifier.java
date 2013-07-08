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
 * $Id: AttributeNotifier.java,v 1.3 2005/03/16 04:23:42 stephen Exp $
 */

package com.streetfiresound.mediamanager.player;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.util.Util;
import com.redrocketcomputing.util.concurrent.SynchronizedShort;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerUnidentifiedFailureException;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AttributeNotifier implements MsgWatchOnNotificationListener
{
  private final static OperationCode WATCH_OPERATION_CODE = new OperationCode(ConstApiCode.ANY, (byte)0xf0);
  
  private ApplicationModule parent;
  private TaskPool taskPool;
  private SoftwareElement softwareElement;
  private AttributeSubscriptionTable attributeSubscriptionTable = new AttributeSubscriptionTable();
  private SynchronizedShort nextNotificationId = new SynchronizedShort((short)0);

  /**
   * Construct AttributeNotifier with the specified SoftwareElement
   */
  public AttributeNotifier(ApplicationModule parent)
  {
    // Check parameters
    if (parent == null)
    {
      throw new IllegalArgumentException("SoftwareElement is null");
    }

    // Lookup the task pool
    taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);

    // Save the parameters
    this.parent = parent;
    this.softwareElement = parent.getSoftwareElement();
  }

  /**
   * Release all resources
   */
  public void close()
  {
    try
    {
      // Remove all subscriptions
      AttributeSubscription[] subscriptions = attributeSubscriptionTable.removeAll();

      // Loop through the subscriptions turning watch off for these entries
      for (int i = 0; i < subscriptions.length; i++)
      {
        // Remove watch
        parent.removeMsgWatch(subscriptions[i].getSeid(), this);
      }
    }
    catch (HaviMsgListenerNotFoundException e)
    {
      LoggerSingleton.logError(this.getClass(), "close", Util.getStackTrace(e));
    }
  }

  /**
   * Add a new attribute subscription
   * @param seid The SEID of the subscriber
   * @param opCode The OperationCode for the subscriber
   * @param indicator The FcmAttributeIndicator key
   * @return The notification ID for the new subscription
   * @throws HaviException The is a problem is detected registering for SEID monitoring
   */
  public short addSubscription(SEID seid, OperationCode opCode, int indicator) throws HaviMediaPlayerException
  {
    // Allocate a ID
    short id = nextNotificationId.increment();

    try
    {
      // Create a new subscription and add the new subscription
      attributeSubscriptionTable.add(new AttributeSubscription(id, seid, opCode, indicator));

      // Turn on the message watch
      parent.addMsgWatch(seid, this);

      // Return the ID
      return id;
    }
    catch (HaviException e)
    {
      // Remove the subscription
      attributeSubscriptionTable.remove(id);
      
      // Translate exception 
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Remove an AttributeNotificationSubscription
   * @param notificationId The ID subscription to remove
   */
  public void removeSubscription(short notificationId)
  {
    // Remove the subscription
    AttributeSubscription subscription = attributeSubscriptionTable.remove(notificationId);
    if (subscription != null)
    {
      // Turn off watch
      parent.removeMsgWatch(subscription.getSeid(), this);
    }
  }

  /**
   * Dispatch all subscription matching the specified indicator and attribute value
   * @param indicator The FcmAttributeIndicator used to lookup the subscriptions
   * @param attributeValue The new FcmAttributeValue
   */
  public void updateAttribute(AttributeNotification attributeValue)
  {
    try
    {
      // Dispatch the notification task
      taskPool.execute(new SubscriptionNotifierTask(softwareElement, attributeSubscriptionTable, attributeValue));
    }
    catch (TaskAbortedException e)
    {
      // Convert exception to string
      LoggerSingleton.logError(this.getClass(), "updateAttribute", Util.getStackTrace(e));
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    // Remove all subscription by this SEID
    attributeSubscriptionTable.remove(targetSeid);
  }
}
