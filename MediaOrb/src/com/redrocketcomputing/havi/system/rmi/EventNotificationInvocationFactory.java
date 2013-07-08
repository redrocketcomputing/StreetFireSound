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
 * $Id: EventNotificationInvocationFactory.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.havi.system.types.EventId;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.SEID;

/**
 * @author stephen
 *
 */
public class EventNotificationInvocationFactory
{
  private Map table = Collections.synchronizedMap(new HashMap());

  private final static Class[] PARAMETER_TYPES = { SEID.class, HaviByteArrayInputStream.class };

  /**
   * Constructor for RemoteInvocationFactory.
   */
	public EventNotificationInvocationFactory()
	{
	}

  /**
   * Constructor for RemoteInvocationFactory.
   * @param table A table of invocation to add this factory
   */
  public EventNotificationInvocationFactory(Map table)
  {
    // Check to parameters
    if (table == null)
    {
      // Badness
      throw new IllegalArgumentException("table is null");
    }

    // Save the parameters
    this.table.putAll(table);
  }

  /**
   * Add a new event notification invocation class to the factory
   * @param eventId The event id of the new invocation class
   * @param invocation The new invocation class
   */
	public void addInvocation(EventId eventId, Class invocation)
	{
		// Check the class
		if (!EventNotificationInvocation.class.isAssignableFrom(invocation))
		{
			// bad
			throw new IllegalArgumentException("bad type: " + invocation.getName());
		}

		// Add to the table
		table.put(eventId, invocation);
	}

  /**
   * Checks the internal invocation table to see if class is already registered for this event ID
   * @param eventId The event ID to check
   * @return boolean True if a class is already registered, false otherwise
   */
	public boolean contains(EventId eventId)
	{
		return table.containsKey(eventId);
	}

  /**
   * Contruct an matching invocation from the specified event id
   * @param posterSeid The poster SEID of the event
   * @param eventId The event ID
   * @param hbais The event payload
   * @return EventNotificationInvocation The new invocation or null if not match invocation found
   * @throws HaviUnmarshallingException The if the invocation can not unmarshall the payload
   */
  public EventNotificationInvocation createInvocation(SEID posterSeid, EventId eventId, HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    try
    {
      // Try to get class based on the event ID
      Class eventClass = (Class)table.get(eventId);
      if (eventClass == null)
      {
        // Could not find it
        return null;
      }

      // Get invocation class constructor
      Constructor constructor = eventClass.getConstructor(PARAMETER_TYPES);

      // Build arguments
      Object[] arguments = new Object[2];
      arguments[0] = posterSeid;
      arguments[1] = hbais;

      // Create the invocation object and bind the poster seid
      EventNotificationInvocation invocation = (EventNotificationInvocation)constructor.newInstance(arguments);

      return invocation;
    }
    catch (NoSuchMethodException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (InstantiationException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (InvocationTargetException e)
    {
      // Check the instance of the target exception
      if (e.getTargetException() instanceof HaviUnmarshallingException)
      {
        // Translate
        throw ((HaviUnmarshallingException)e.getTargetException());
      }

      // Unknow exception
      throw new IllegalStateException(e.getTargetException().toString());
    }
  }
}
