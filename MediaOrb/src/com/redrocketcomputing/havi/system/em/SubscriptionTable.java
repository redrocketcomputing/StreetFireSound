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
 * $Id: SubscriptionTable.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */
package com.redrocketcomputing.havi.system.em;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.havi.system.types.EventId;
import org.havi.system.types.HaviEventManagerExistException;
import org.havi.system.types.HaviEventManagerNotFoundException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.ListSet;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class SubscriptionTable
{
  private Map eventMap = new ListMap(LinkedList.class);
  private Map seidMap = new ListMap(LinkedList.class);
  
  /**
   * Create empty SubscriptionTable
   */
  public SubscriptionTable()
  {
  }
  
  /**
   * Add a new subscription
   * @param seid The SEID of the subscribing SoftwareElement
   * @param opCode The OperationCode of the subscibing SoftwareElement
   * @throws HaviEventManagerExistException Thrown if the SEID already exists
   */
  public synchronized void add(SEID seid, OperationCode opCode) throws HaviEventManagerExistException
  {
    // Check for existing subscription
    if (seidMap.containsKey(seid))
    {
      // Badness
      throw new HaviEventManagerExistException(seid + ":" + opCode);
    }
    
    // Create a new entry
    SubscriptionEntry entry = new SubscriptionEntry(seid, opCode);
    
    // Add to the seid map
    seidMap.put(seid, entry);
  }
  
  /**
   * Add event subscriptions
   * @param seid The SEID of the subscribing SoftwareElement
   * @param eventId The EventId of the subscription
   * @throws HaviEventManagerExistException Thrown if the event is already subscribed
   * @throws HaviEventManagerNotFoundException Thrown if the SEID has not register for events
   */
  public synchronized void add(SEID seid, EventId eventId) throws HaviEventManagerExistException, HaviEventManagerNotFoundException
  {
    // Lookup subscription
    SubscriptionEntry entry = (SubscriptionEntry)seidMap.get(seid);
    if (entry == null)
    {
      // Bad
      throw new HaviEventManagerNotFoundException(seid.toString());
    }
    
    // Lookup the event subscription set
    Set eventSet = (Set)eventMap.get(eventId);
    if (eventSet == null)
    {
      // Create new event set
      eventSet = new ListSet(LinkedList.class);
      eventMap.put(eventId, eventSet);
    }
    
    // Make sure the event entry does not exist
    if (eventSet.contains(entry))
    {
      // Bad
      throw new HaviEventManagerExistException(seid + ":" + eventId);
    }
    
    // Add entry
    eventSet.add(entry);
  }
  
  /**
   * Add event subscriptions
   * @param seid The SEID of the subscribing SoftwareElement
   * @param eventId The EventId of the subscription
   * @throws HaviEventManagerExistException Thrown if the event is already subscribed
   * @throws HaviEventManagerNotFoundException Thrown if the SEID has not register for events
   */
  public synchronized void add(SEID seid, EventId eventId[]) throws HaviEventManagerExistException, HaviEventManagerNotFoundException
  {
    // Lookup subscription
    SubscriptionEntry entry = (SubscriptionEntry)seidMap.get(seid);
    if (entry == null)
    {
      // Bad
      throw new HaviEventManagerNotFoundException(seid.toString());
    }
    
    // Loop through the array
    for (int i = 0; i < eventId.length; i++)
    {
      // Lookup the event subscription set
      Set eventSet = (Set)eventMap.get(eventId[i]);
      if (eventSet == null)
      {
        // Create new event set
        eventSet = new ListSet(LinkedList.class);
        eventMap.put(eventId[i], eventSet);
      }
      
      // Make sure the event entry does not exist
      if (eventSet.contains(entry))
      {
        // Bad
        throw new HaviEventManagerExistException(seid + ":" + eventId[i]);
      }
      
      // Add entry
      eventSet.add(entry);
    }
  }
  
  /**
   * Remove all subscriptions for the specified SEID
   * @param seid The SEID to remove
   * @throws HaviEventManagerNotFoundException Thrown if the SEID has not already registered
   */
  public synchronized void remove(SEID seid) throws HaviEventManagerNotFoundException
  {
    // Lookup subscription
    SubscriptionEntry entry = (SubscriptionEntry)seidMap.remove(seid);
    if (entry == null)
    {
      // Bad
      throw new HaviEventManagerNotFoundException(seid.toString());
    }
    
    // Remove from all event sets
    for (Iterator iterator = eventMap.values().iterator(); iterator.hasNext();)
    {
      // Extract event set
      Set element = (Set)iterator.next();
      
      // Remove matching entries
      element.remove(entry);
    }
  }
  
  /**
   * Remove a event subscriptions
   * @param seid The SEID of the subscribing SoftwareElement
   * @param eventId The EventId to remove
   * @throws HaviEventManagerNotFoundException Thrown if the SEID is not registered
   */
  public synchronized void remove(SEID seid, EventId eventId) throws HaviEventManagerNotFoundException
  {
    // Lookup subscription
    SubscriptionEntry entry = (SubscriptionEntry)seidMap.remove(seid);
    if (entry == null)
    {
      // Bad
      throw new HaviEventManagerNotFoundException(seid.toString());
    }

    // Lookup the event subscription set
    Set eventSet = (Set)eventMap.get(eventId);
    if (eventSet == null)
    {
      // Create new event set
      eventSet = new ListSet(LinkedList.class);
      eventMap.put(eventId, eventSet);
    }
    
    // Remove from the list
    eventSet.remove(entry);
  }
  
  /**
   * Replace an existing subscription
   * @param seid The SEID of subscriptions to replace
   * @param opCode The new OperationCode for the subscriptions
   * @param eventId The array of new EventId to subscribe to
   * @throws HaviEventManagerExistException Thrown if the event is already subscribed
   * @throws HaviEventManagerNotFoundException Thrown if the SEID has not register for events
   */
  public synchronized void replace(SEID seid, OperationCode opCode, EventId eventId[]) throws HaviEventManagerNotFoundException, HaviEventManagerExistException
  {
    // Remove
    remove(seid);
    
    // Add
    add(seid, opCode);
    add(seid, eventId);
  }
  
  /**
   * Return an array of SubscriptionEntry for the specified EventId
   * @param eventId The EventId to retrieve the matching subscriptions
   * @return The SubscriptionEntry array matching the specified EventId
   */
  public synchronized SubscriptionEntry[] getEntries(EventId eventId)
  {
    // Lookup the event subscription set
    Set eventSet = (Set)eventMap.get(eventId);
    if (eventSet == null)
    {
      // Create new event set
      eventSet = new ListSet(LinkedList.class);
      eventMap.put(eventId, eventSet);
    }
    
    // Return array
    return (SubscriptionEntry[])eventSet.toArray(new SubscriptionEntry[eventSet.size()]);
  }
  
  /**
   * Get the array of subscribe EventIds
   * @return The array of subscribed EventId
   */
  public synchronized EventId[] getEntries()
  {
    // Return array
    return (EventId[])eventMap.keySet().toArray(new EventId[eventMap.size()]);
  }
}
