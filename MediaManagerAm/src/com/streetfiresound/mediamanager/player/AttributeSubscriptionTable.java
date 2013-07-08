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
 * $Id: AttributeSubscriptionTable.java,v 1.1 2005/02/22 03:41:17 stephen Exp $
 */

package com.streetfiresound.mediamanager.player;

import java.util.Map;
import java.util.Set;

import org.havi.system.types.SEID;

import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.ListSet;


/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AttributeSubscriptionTable
{
  private Map notificationIdMap = new ListMap();
  private Map seidMap = new ListMap();
  private Map indicatorMap = new ListMap();

  /**
   * Construct an empty table
   */
  public AttributeSubscriptionTable()
  {
  }

  /**
   * Add a new AttributeSubscription to the table
   * @param subscription The subscription to add
   */
  public synchronized void add(AttributeSubscription subscription)
  {
    // Add to notification ID map
    notificationIdMap.put(new Short(subscription.getNotficationId()), subscription);

    // Lookup SEID
    Set subscriptionSet = (Set)seidMap.get(subscription.getSeid());
    if (subscriptionSet == null)
    {
      // Add it
      subscriptionSet = new ListSet();
      seidMap.put(subscription.getSeid(), subscriptionSet);
    }

    // Add the subscription
    subscriptionSet.add(subscription);

    // Lookup FcmAttributeIndicator
    Set indicatorSet = (Set)indicatorMap.get(new Integer(subscription.getIndicator()));
    if (indicatorSet == null)
    {
      // Add it
      indicatorSet = new ListSet();
      indicatorMap.put(new Integer(subscription.getIndicator()), indicatorSet);
    }

    // Add subscription
    indicatorSet.add(subscription);
  }

  /**
   * Remove the specified AttributeSubscription
   * @param subscription The AttributeSubscription to remove
   * @return True if the AttributeSubscrition was found, false otherwise
   */
  public synchronized boolean remove(AttributeSubscription subscription)
  {
    // Try to remove from the notification set
    if (notificationIdMap.remove(new Short(subscription.getNotficationId())) != null)
    {
      // Look up the seid subscription set and check state
      Set subscriptionSet = (Set)seidMap.get(subscription.getSeid());
      if (subscriptionSet == null)
      {
        throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing subscription set");
      }

      // Remove subscription from the set
      subscriptionSet.remove(subscription);

      // Lookup the indicator subscription set
      Set indicatorSet = (Set)indicatorMap.get(new Integer(subscription.getIndicator()));
      if (subscriptionSet == null)
      {
        throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing indicator set");
      }

      // Remove subscription
      indicatorSet.remove(subscription);

      // All done
      return true;
    }

    // Not found
    return false;
  }

  /**
   * Remove a AttributeSubscription using the specified notification ID
   * @param notificationId The notification ID of the AttributeSubscription to remove
   * @return The AttributeSubsciption remove or null if not found
   */
  public synchronized AttributeSubscription remove(short notificationId)
  {
    // Lookup the subscription by the notification ID
    AttributeSubscription subscription = (AttributeSubscription)notificationIdMap.get(new Short(notificationId));
    if (subscription != null)
    {
      // Look up the seid subscription set and check state
      Set subscriptionSet = (Set)seidMap.get(subscription.getSeid());
      if (subscriptionSet == null)
      {
        throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing subscription set");
      }

      // Remove subscription from the set
      subscriptionSet.remove(subscription);

      // Lookup the indicator subscription set
      Set indicatorSet = (Set)indicatorMap.get(new Integer(subscription.getIndicator()));
      if (subscriptionSet == null)
      {
        throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing indicator set");
      }

      // Remove subscription
      if (!indicatorSet.remove(subscription))
      {
        throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing indicator subscription");
      }

      // All done
      return subscription;
    }

    // Not found
    return null;
  }

  /**
   * Remove all AttributeSubscription entries using the specified SEID
   * @param seid The SEID of the AttributeSubscription to remove
   * @return Array of AttributeSubscription entries remove, null otherwise
   */
  public synchronized AttributeSubscription[] remove(SEID seid)
  {
    // Look up the seid subscription set and check state
    Set subscriptionSet = (Set)seidMap.remove(seid);
    if (subscriptionSet != null)
    {
      // Convert to array
      AttributeSubscription[] subscriptions = (AttributeSubscription[])subscriptionSet.toArray(new AttributeSubscription[subscriptionSet.size()]);

      // Loop through the array removing all notification map entries
      for (int i = 0; i < subscriptions.length; i++)
      {
        // Remove from notification map
        if (notificationIdMap.remove(new Short(subscriptions[i].getNotficationId())) == null)
        {
          throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing notification entry");
        }

        // Lookup subscription Set in indicator map
        Set indicatorSet = (Set)indicatorMap.get(new Integer(subscriptions[i].getIndicator()));
        if (indicatorSet == null)
        {
          throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing indicator set");
        }

        // Remove subscription from indicator set
        if (!indicatorSet.remove(subscriptions[i]))
        {
          throw new IllegalStateException("AttributeSubscriptionTable is corrupted, missing indicator entry");
        }
      }

      // Return the removed subscriptions
      return subscriptions;
    }

    // Not found
    return null;
  }

  /**
   * Remove all AttributeSubscription entries from the table
   * @return An array of AttributeSubscription entries removed
   */
  public synchronized AttributeSubscription[] removeAll()
  {
    // Convert notification map to subscription array
    AttributeSubscription[] subscriptions = (AttributeSubscription[])notificationIdMap.values().toArray(new AttributeSubscription[notificationIdMap.size()]);

    // Flush the maps
    notificationIdMap.clear();
    seidMap.clear();
    indicatorMap.clear();

    // Return the subscriptions
    return subscriptions;
  }

  /**
   * Lookup and return a AttributeSubscription using the specified notification ID
   * @param notificationId The notification ID to use for the lookup
   * @return The matching AttributeSubscription or null if not found
   */
  public synchronized AttributeSubscription getByNotificationId(short notificationId)
  {
    return (AttributeSubscription)notificationIdMap.get(new Short(notificationId));
  }

  /**
   * Lookup and return a array AttributeSubscription entries using the specified SEID
   * @param seid The SEDI to use for the lookup
   * @return The match AttributeSubscription array or null if not found
   */
  public synchronized AttributeSubscription[] getBySeid(SEID seid)
  {
    // Look up the seid subscription set and check state
    Set subscriptionSet = (Set)seidMap.get(seid);
    if (subscriptionSet != null)
    {
      // Convert to array and return
      return (AttributeSubscription[])subscriptionSet.toArray(new AttributeSubscription[subscriptionSet.size()]);
    }

    // Not found
    return null;
  }

  /**
   * Lookup and return a array AttributeSubscription entries using the specified FcmAttributeIndicator
   * @param indicator The FcmAttributeIndicator to use for the lookup
   * @return The matching AttributeSubscription array or null if not found
   */
  public synchronized AttributeSubscription[] getByIndicator(int indicator)
  {
    // Look up the seid subscription set and check state
    Set indicatorSet = (Set)indicatorMap.get(new Integer(indicator));
    if (indicatorSet != null)
    {
      // Convert to array and return
      return (AttributeSubscription[])indicatorSet.toArray(new AttributeSubscription[indicatorSet.size()]);
    }

    // Not found
    return null;
  }
}
