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
 * $Id: EventDispatchService.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.event;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Generic event dispatch service for the Application Framework
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class EventDispatchService extends AbstractService implements EventDispatch
{
  private ComponentConfiguration configuration;

  private Map listenerMap = new HashMap();

  private TaskPool taskPool = null;
  private int eventsDispatchedCounter = 0;
  private int eventsDroppedCounter = 0;
  private int registeredListenersCounter = 0;

  /**
   * Constructor for EventDispatchService.
   */
  public EventDispatchService(String instanceName)
  {
    // Construct superclass
    super(instanceName);

    // Create component configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public synchronized void start()
  {
    // Check to see if we are already running
    if (getServiceState() == Service.RUNNING)
    {
      throw new ServiceException("EventDispatcherService is already running");
    }

    // Use the first task pool service found
    taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
    if (taskPool == null)
    {
      // Can not find required component
      throw new ServiceException("task pool service not found");
    }

    // Change state
    setServiceState(Service.RUNNING);

    // Log start of event dispatcher
    LoggerSingleton.logInfo(this.getClass(), "start", "service is running");
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public synchronized void terminate()
  {
    // Check to see if we are idle
    if (getServiceState() == Service.IDLE)
    {
      throw new ServiceException("EventDispatcherService is not running");
    }

    // Change state to IDLE
   setServiceState(Service.IDLE);

    // Log termination of event dispatcher
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public synchronized void info(PrintStream printStream, String[] arguments)
  {
    // Display the counters
    printStream.println("Events Dispatched: " + eventsDispatchedCounter);
    printStream.println("Events Dropped: " + eventsDroppedCounter);
    printStream.println("Registered Listeners: " + registeredListenersCounter + '\n');

    // Dump the listener map
    synchronized (listenerMap)
    {
      for (Iterator mapIterator = listenerMap.entrySet().iterator(); mapIterator.hasNext();)
      {
        // Get the current listener interface
        Map.Entry entry = (Map.Entry)mapIterator.next();
        Class listenerClass = (Class)entry.getKey();
        List listenerList = (List)entry.getValue();

        // Display the class name and list count
        printStream.println("Listener: " + listenerClass.getName() + '[' + listenerList.size() + "]: ");

        // Loop through the list and display all the objects
        for (Iterator listIterator = listenerList.iterator(); listIterator.hasNext();)
        {
          // Extract the listener
          EventListener element = (EventListener) listIterator.next();

          // Display the object to string
          printStream.println("          " + element.toString());
        }
      }
    }
  }

  /**
   * Add a new listener to the internal listener dispatch map.  The listener class is inspected to determine
   * the types of event to handle.  This resolved by identifing the EventListener interfaces implemented.
   * @param listener The new event listener
   */
  public void addListener(EventListener listener)
  {
    // Get the listener interfaces
    Class[] listenerInterfaces = listener.getClass().getInterfaces();

    // Loop through the interface looking for the GarpEventListener interface markers
    for (int i = 0; i < listenerInterfaces.length; i++)
    {
      // Check for marker interface
      if (EventListener.class.isAssignableFrom(listenerInterfaces[i]))
      {
        // Found the marker
        synchronized (listenerMap)
        {
          // Try to get the register listener list
          List listenerList = (List)listenerMap.get(listenerInterfaces[i]);

          // Check to if we need to create entry for this listener interface type
          if (listenerList == null)
          {
            // Create new registered listeners
            listenerList = new ArrayList();

            // Add new interface key
            listenerMap.put(listenerInterfaces[i], listenerList);
          }

          // Add the listener to the list
          listenerList.add(listener);

          // Update the number of registered listeners
          registeredListenersCounter++;
        }
      }
    }
  }

  /**
   * Remove the specified listener from the interanl dispatch map.
   * @param listener The listener to remove
   */
  public void removeListener(EventListener listener)
  {
    // Get the listener interfaces
    Class[] listenerInterfaces = listener.getClass().getInterfaces();

    // Loop through the interface looking for the GarpEventListener interface markers
    for (int i = 0; i < listenerInterfaces.length; i++)
    {
      // Check for marker interface
      if (EventListener.class.isAssignableFrom(listenerInterfaces[i]))
      {
        // Found the marker
        synchronized (listenerMap)
        {
          // Try to get the register listener list
          List listenerList = (List)listenerMap.get(listenerInterfaces[i]);

          // Check to if listener list entry is valid for this listener interface type
          if (listenerList != null)
          {
            // Remove the listener
            listenerList.remove(listener);

            // Check to see if we should remove the map entry, IS THIS BROKEN????
            if (listenerList.size() == 0)
            {
              listenerMap.remove(listener);
            }

            // Update the number of registered listeners
            registeredListenersCounter--;
          }
        }
      }
    }
  }

  /**
   * Remove all listeners with the specified class type
   * @param listenerClass The class of listeners to flush
   */
  public void flushListeners(Class listenerClass)
  {
    // Get the listener interfaces
    Class[] listenerInterfaces = listenerClass.getInterfaces();

    // Loop through the interface looking for the GarpEventListener interface markers
    for (int i = 0; i < listenerInterfaces.length; i++)
    {
      // Check for marker interface
      if (EventListener.class.isAssignableFrom(listenerInterfaces[i]))
      {
        // Found the marker
        synchronized (listenerMap)
        {
          // Try to get the register listener list
          List listenerList = (List)listenerMap.get(listenerInterfaces[i]);

          // Check to if listener list entry is valid for this listener interface type
          if (listenerList != null)
          {
            // Update the number of registered listeners
            registeredListenersCounter -= listenerList.size();

            // Remove the listener
            listenerList.clear();
            listenerMap.remove(listenerInterfaces[i]);
          }
        }
      }
    }
  }

  /**
   * Dispatch the listener based on the event type using the Executor provided at construction.
   * @param event The event to dispatch
   */
  public void dispatch(Event event)
  {
    try
    {
      synchronized (listenerMap)
      {
        // Lookup the listener list based on the event's interface type
        List listenerList = (List)listenerMap.get(event.getEventListenerType());

        // If there are registered listenes create a dispatch task
        if (listenerList != null)
        {
          for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
          {
            // Extract the listener
            EventListener element = (EventListener) iterator.next();

            // Create the dispatch task
            EventDispatchTask dispatchTask = new EventDispatchTask(event, element);

            // Execute the dispatch task
            taskPool.execute(dispatchTask);
          }

          // Update dispatch counter
          eventsDispatchedCounter++;
        }
      }
    }
    catch (TaskAbortedException e)
    {
      // Log a debug message
      LoggerSingleton.logError(this.getClass(), "dispatch", "Task aborted dispatching: " + event.toString());
      eventsDroppedCounter++;
    }
  }
}
