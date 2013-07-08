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
 * $Id: SubscriptionNotifierTask.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.fcm;

import org.havi.fcm.rmi.FcmNotificationMessageBackClient;
import org.havi.system.SoftwareElement;
import org.havi.system.types.HaviException;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class SubscriptionNotifierTask extends AbstractTask
{
  private SoftwareElement softwareElement;
  private AttributeSubscriptionTable table;
  private short indicator;
  private byte[] value;

  /**
   * @param softwareElement The SoftwareElement is used to send the notifications
   * @param table The AttributeSubscriptionTable to lookup all subscripts for this dispatch
   * @param indicator The FcmAttributeIndicator for this dispatch
   * @param value The new AttributeValue for this notification
   */
  public SubscriptionNotifierTask(SoftwareElement softwareElement, AttributeSubscriptionTable table, short indicator, byte[] value)
  {
    // Construct super class
    super();

    // Check the parameters
    if (softwareElement == null || table == null || value == null)
    {
      // Badness
      throw new IllegalArgumentException("SoftwareElement or AttributeSubscriptionTable or AttributeValue is null");
    }

    // Save the parameters
    this.softwareElement = softwareElement;
    this.table = table;
    this.indicator = indicator;
    this.value = value;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "SubscriptionNotifier on " + indicator;
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // Lookup subscriptions
    AttributeSubscription[] subscriptions = table.getByIndicator(indicator);
    if (subscriptions == null)
    {
      // Not subscriptions
      return;
    }

    // Loop through the subscript send the notifications
    for (int i = 0; i < subscriptions.length; i++)
    {
      // Check for match
      if (subscriptions[i].match(value))
      {
        try
        {
          // Create a corresponding client
          FcmNotificationMessageBackClient client = new FcmNotificationMessageBackClient(subscriptions[i].getOpCode(), softwareElement, subscriptions[i].getSeid());

          // Send the notification
          client.fcmNotificationSync(0, subscriptions[i].getNotficationId(), indicator, value);
        }
        catch (HaviException e)
        {
          // Log the error
          LoggerSingleton.logError(this.getClass(), "run", e.toString() + ", flushing subscription");

          // Flush out the subscription
          table.remove(subscriptions[i]);
        }
      }
    }
  }
}
