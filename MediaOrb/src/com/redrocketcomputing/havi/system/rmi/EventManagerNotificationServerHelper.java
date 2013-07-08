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
 * $Id: EventManagerNotificationServerHelper.java,v 1.2 2005/02/24 03:30:22 stephen Exp $
 */
package com.redrocketcomputing.havi.system.rmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstEventIdSchema;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.eventmanager.rmi.EventManagerClient;
import org.havi.system.types.AppEventId;
import org.havi.system.types.EventId;
import org.havi.system.types.HaviEventManagerUnidentifiedFailureException;
import org.havi.system.types.HaviException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;
import org.havi.system.types.VendorEventId;
import org.havi.system.types.VendorId;

import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class EventManagerNotificationServerHelper
{
  private final static OperationCode DEFAULT_EVENT_OPCODE = new OperationCode((short)ConstApiCode.EVENTMANAGER, (byte)0xff);

  private SoftwareElement softwareElement;
  private EventManagerClient eventClient;
  private Map eventServerMap = new ListMap();
  private OperationCode eventOpCode = DEFAULT_EVENT_OPCODE;

  public EventManagerNotificationServerHelper(SoftwareElement softwareElement, OperationCode eventOpCode) throws HaviException
  {
    // Check parameters
    if (softwareElement == null)
    {
      // Bad
      throw new IllegalArgumentException("SoftwareElement is null");
    }
    
    if (eventOpCode == null)
    {
      // Bad
      throw new IllegalArgumentException("OperationCode is null");
    }

    // Save the software element
    this.softwareElement = softwareElement;
    this.eventOpCode = eventOpCode;

    // Create event client
    eventClient = new EventManagerClient(softwareElement);

    // Subscript with the event manager
    eventClient.subscribeSync(0, new EventId[0], eventOpCode);
  }
  /**
   * Construct EventManagerNotificationServerHelper hack code
   */
  public EventManagerNotificationServerHelper(SoftwareElement softwareElement) throws HaviException
  {
    // Forward
    this(softwareElement, DEFAULT_EVENT_OPCODE);
  }

  /**
   * Release all resources
   */
  public synchronized void close()
  {
    try
    {
      // Unsubscribe
      eventClient.unsubscribeSync(0);
      
      // Flush the map
      eventServerMap.clear();
    }
    catch (HaviException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }

    // Loop through the server helper removing from the software element
    for (Iterator iterator = eventServerMap.values().iterator(); iterator.hasNext();)
    {
      // Extract the element
      EventManagerNotificationHelper element = (EventManagerNotificationHelper)iterator.next();

      // Remove from the software element
      softwareElement.removeHaviListener(element);
    }

    // Flush the map
    eventServerMap.clear();
  }

  /**
   * Add SystemEvent subscriptions
   * 
   * @param eventId The SystemEventId of the event to subscribe to
   * @param eventServerHelperClass The EventManagerNotificationHelper
   * @param listener The EventNotificationListener to receive the event
   * @throws HaviException The if a problem subscribing to the event is detected
   */
  public synchronized void addEventSubscription(EventId eventId, EventNotificationListener listener) throws HaviException
  {
    // Check EventId
    if (eventId == null)
    {
      // bad
      throw new IllegalArgumentException("bad EventId");
    }

    // Check Class
    if (listener == null)
    {
      // bad
      throw new IllegalArgumentException("bad listener");
    }

    try
    {
      // Try to get a matching event server entry
      EventManagerNotificationHelper eventServerHelper = (EventManagerNotificationHelper)eventServerMap.get(eventId);
      if (eventServerHelper == null)
      {
        // Extract helper classes and look for matching
        Class eventServerHelperClass = null;
        Field[] fields = listener.getClass().getFields();
        for (int i = 0; i < fields.length && eventServerHelperClass == null; i++)
        {
          // Look for server helper field
          if (fields[i].getName().equals("SERVER_HELPER_CLASS"))
          {
            // Extract helper class
            Class serverHelperClass = (Class)fields[i].get(null);

            // Extract event base field
            short serverHelperEventBase = serverHelperClass.getField("EVENT_BASE").getShort(null);
            switch (eventId.getDiscriminator())
            {
              case ConstEventIdSchema.SYSTEM:
              {
                // Match event bases
                eventServerHelperClass = ((SystemEventId)eventId).getBase() == serverHelperEventBase ? serverHelperClass : null;
                break;
              }
              case ConstEventIdSchema.VENDOR:
              {
                // Match event bases
                eventServerHelperClass = ((VendorEventId)eventId).getBase() == serverHelperEventBase ? serverHelperClass : null;
                break;
              }
              case ConstEventIdSchema.APP:
              {
                // Match event bases
                eventServerHelperClass = ((AppEventId)eventId).getBase() == serverHelperEventBase ? serverHelperClass : null;
                break;
              }
            }
          }
        }

        // Check for not found
        if (eventServerHelperClass == null)
        {
          // Bad arguments
          throw new IllegalArgumentException("EventId and EventManagerNotificationListener do not match for " + eventId);
        }

        // Build reflection parts
        Class[] parameterTypes = null;
        Object[] arguments = null;
        switch (eventId.getDiscriminator())
        {
          case ConstEventIdSchema.SYSTEM:
          {
            // Build constructor query
            parameterTypes = new Class[2];
            parameterTypes[0] = SoftwareElement.class;
            parameterTypes[1] = OperationCode.class;

            // Build arguments
            arguments = new Object[2];
            arguments[0] = softwareElement;
            arguments[1] = eventOpCode;

            break;
          }
          case ConstEventIdSchema.VENDOR:
          {
            // Build constructor query
            parameterTypes = new Class[3];
            parameterTypes[0] = SoftwareElement.class;
            parameterTypes[1] = OperationCode.class;
            parameterTypes[2] = VendorId.class;

            // Get the constructor
            Constructor constructor = eventServerHelperClass.getConstructor(parameterTypes);

            // Build arguments
            arguments = new Object[3];
            arguments[0] = softwareElement;
            arguments[1] = eventOpCode;
            arguments[2] = ((VendorEventId)eventId).getVendorId();

            break;
          }
          case ConstEventIdSchema.APP:
          {
            // Build constructor query
            parameterTypes = new Class[3];
            parameterTypes[0] = SoftwareElement.class;
            parameterTypes[1] = OperationCode.class;
            parameterTypes[2] = SEID.class;

            // Get the constructor
            Constructor constructor = eventServerHelperClass.getConstructor(parameterTypes);

            // Build arguments
            arguments = new Object[3];
            arguments[0] = softwareElement;
            arguments[1] = eventOpCode;
            arguments[2] = ((AppEventId)eventId).getSeid();

            break;
          }
        }

        // Get the constructor
        Constructor constructor = eventServerHelperClass.getConstructor(parameterTypes);

        // Create the server helper
        eventServerHelper = (EventManagerNotificationHelper)constructor.newInstance(arguments);

        // Add to the software element
        softwareElement.addHaviListener(eventServerHelper, softwareElement.getSystemSeid(ConstSoftwareElementType.EVENTMANAGER));

        // Subscribe to the event
        eventClient.addEventSync(0, eventId);

        // Add to the map
        eventServerMap.put(eventId, eventServerHelper);
      }

      // Add listener to the server helper
      eventServerHelper.addListener(listener);
    }
    catch (SecurityException e)
    {
      // Translate
      throw new HaviEventManagerUnidentifiedFailureException(e.toString());
    }
    catch (IllegalArgumentException e)
    {
      // Translate
      throw new HaviEventManagerUnidentifiedFailureException(e.toString());
    }
    catch (NoSuchMethodException e)
    {
      // Translate
      throw new HaviEventManagerUnidentifiedFailureException(e.toString());
    }
    catch (InstantiationException e)
    {
      // Translate
      throw new HaviEventManagerUnidentifiedFailureException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new HaviEventManagerUnidentifiedFailureException(e.toString());
    }
    catch (InvocationTargetException e)
    {
      // Translate
      throw new HaviEventManagerUnidentifiedFailureException(e.toString());
    }
    catch (NoSuchFieldException e)
    {
      // Translate
      throw new HaviEventManagerUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Remove the event subscriptions
   * 
   * @param eventId The EventId to remove
   * @param listener The matching listener;
   */
  public synchronized void removeEventSubscription(EventId eventId, EventNotificationListener listener)
  {
    try
    {
      // Try to get a matching event server entry
      EventManagerNotificationHelper eventServerHelper = (EventManagerNotificationHelper)eventServerMap.get(eventId);
      if (eventServerHelper != null)
      {
        // Remove listener
        eventServerHelper.removeListener(listener);

        // Check to see if this is the last subscription, if so remove event subscription
        if (eventServerHelper.size() == 0)
        {
          // Remove event
          eventClient.removeEventSync(0, eventId);

          // Remove form map
          eventServerMap.remove(eventId);
        }
      }
    }
    catch (HaviException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "removeEventSubscription", e.toString());
    }
  }
}