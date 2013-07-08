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
 * $Id: ServiceManager.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.service;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.redrocketcomputing.appframework.service.commands.*;
import com.redrocketcomputing.appframework.shell.Shell;
import com.redrocketcomputing.util.concurrent.Mutex;
import com.redrocketcomputing.util.concurrent.Sync;
import com.redrocketcomputing.util.concurrent.SynchronizedInt;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Implement a global service manager based on the Component Configurator pattern. This is a singleton class.
 *
 * @author stephen Jul 16, 2003
 * @version 1.0
 *
 */
public class ServiceManager implements Observer
{
  private final static String SERVICE_MANAGER_PREFIX = "service.manager";
  private static ServiceManager instance = null;
  private static SynchronizedInt serviceId = new SynchronizedInt(0);
  private static Sync singletonLock = new Mutex();

  private List repository = new ArrayList();
  private ComponentConfiguration configuration;

  /**
   * Return the next unique service ID
   * @return int The next service ID
   */
  public final static int getNewServiceId()
  {
    return serviceId.increment();
  }

  public final static ServiceManager getInstance()
  {
    // Check to see if we need to create the manager using double checked locking
    if (instance == null)
    {
      try
      {
        // Lock the singleton instance
        singletonLock.acquire();

        if (instance == null)
        {
          // Create the instance
          instance = new ServiceManager();
        }
      }
      catch (InterruptedException e)
      {
        // Log fatal error
        LoggerSingleton.logFatal(ServiceManager.class, "getInstance", "interrupted while trying to create ServiceManager instance");

        // Exit the program
        System.exit(-1);
      }
      finally
      {
        // Release the singleton instance lock
        singletonLock.release();
      }
    }

    // Return the instance
    return instance;
  }

  /**
   * Private constructor for ServiceManager.
   */
  private ServiceManager()
  {
    // Create a configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(SERVICE_MANAGER_PREFIX);

    // Install shell command
    Shell.installGlobally(ServiceCommand.class);
  }

  public void install()
  {
    // Create configured services
    String className = "unknown";
    String instanceName = "unknown";
    String property = null;
    int i = 0;
    while ((property = configuration.getProperty(Integer.toString(i))) != null)
    {

      try
      {
        // Update property position
        i++;

        // Parse the property
        instanceName = property.substring(0, property.indexOf(','));
        className = property.substring(property.indexOf(',') + 1);

        //LoggerSingleton.logInfo(this.getClass(), "install", "installing service " + i + ": " + instanceName + "' (" + className + ")");

        // Install the service
        install(Class.forName(className), instanceName);
      }
      catch (ClassNotFoundException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "ServiceManager", e.toString());
      }
      catch (ServiceException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "ServiceManager", e.toString());
      }
    }
  }

  /**
   * Configure or re-configure the service manager from the configuration properties
   */
  public Service install(Class serviceClass, String instanceName)
  {
    try
    {
      // Get the Class for the service and check for valid type
      if (Service.class.isAssignableFrom(serviceClass))
      {
        // Check to see if service is already active
        Service activeService = find(instanceName);
        if (activeService == null)
        {
          // Build constructor query
          Class[] parameterTypes = new Class[1];
          parameterTypes[0] = String.class;

          // Get the constructor
          Constructor constructor = serviceClass.getConstructor(parameterTypes);

          // Build arguments
          Object[] arguments = new Object[1];
          arguments[0] = instanceName;

          // Create the service class
          activeService = (Service) constructor.newInstance(arguments);

          // Add the service to the repository
          add(activeService);

          // Start the service
          activeService.start();
        }

        // Return the active service
        return activeService;
      }
      else
      {
        // Class does not support service interface
        LoggerSingleton.logError(this.getClass(), "install", serviceClass.getName() + " does not support Service interface");

        // Could not create the service
        return null;
      }
    }
    catch (NoSuchMethodException e)
    {
      throw new ServiceException("NoSuchMethodException: [" + serviceClass.getName() + ':' + instanceName + "] " + e.getMessage());
    }
    catch (InvocationTargetException e)
    {
      throw new ServiceException("InvocationTargetException: [" + serviceClass.getName() + ':' + instanceName + "] " + e.getTargetException());
    }
    catch (IllegalAccessException e)
    {
      throw new ServiceException("IllegalException: [" + serviceClass.getName() + ':' + instanceName + "] " + e.getMessage());
    }
    catch (InstantiationException e)
    {
      throw new ServiceException("InstantiationException: [" + serviceClass.getName() + ':' + instanceName + "] " + e.getMessage());
    }
  }

  /**
   * Display information on all services
   * @throws ServiceException
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    synchronized (repository)
    {
      printStream.println("Services: " + repository.size());

      // Loop through the services
      for (Iterator iterator = repository.iterator(); iterator.hasNext();)
      {
        // Extract the current service
        Service element = (Service) iterator.next();

        // Print service information
        printStream.println("   " + element.getServiceId() + " " + (element.getServiceState() == Service.IDLE ? "IDLE " : "RUNNING ") + element.getInstanceName() + " " + element.getClass().getName());
      }
    }
  }
  
  /**
   * Return an array of all installed services
   * @return The install services
   */
  public Service[] getAll()
  {
    synchronized(repository)
    {
      return (Service[])repository.toArray(new Service[repository.size()]);
    }
  }
  
  /**
   * Start the specificed service.
   * @param serviceId The service to start
   */
  public void start(int serviceId)
  {
    // Find the service
    Service service = get(serviceId);

    // Mark sure the service was found and the state is idle
    if (service != null && service.getServiceState() == Service.IDLE)
    {
      // Start the services
      service.start();
    }
  }

  /**
   * Terminate the specified service.
   * @param serviceId The service to terminate
   */
  public void terminate(int serviceId)
  {
    // Find the service
    Service service = get(serviceId);

    // Mark sure the service was found and the state is running
    if (service != null && service.getServiceState() == Service.RUNNING)
    {
      // Start the services
      service.terminate();
    }
  }

  /**
   * Terminate the specified service.
   * @param serviceId The service to terminate
   */
  public void terminateAll()
  {
    synchronized (repository)
    {
      // Loop through the repository
      for (Iterator iterator = repository.iterator(); iterator.hasNext();)
      {
        // Extract the current service
        Service service = (Service) iterator.next();

        // Make sure the service was found and the state is running
        if (service != null && service.getServiceState() == Service.RUNNING)
        {
          // Start the services
          service.terminate();
        }
      }
    }
  }

  /**
   * Get the service with the match service ID
   * @param serviceId The service ID to search for
   * @return Service The match service or -1 if not found
   */
  public final Service get(int serviceId)
  {
    synchronized (repository)
    {
      // Loop through the repository
      for (Iterator iterator = repository.iterator(); iterator.hasNext();)
      {
        // Extract the current service
        Service element = (Service) iterator.next();

        // Check for matching service id
        if (element.getServiceId() == serviceId)
        {
          return element;
        }
      }

      // Not found
      return null;
    }
  }
  
  /**
   * Get a matching Service for the specified Class.  Throw an exception if not found
   * @param serviceClass The Class to search for
   * @return The Service matching the Class
   * @throws ServiceException Thrown if a matching service is not found
   */
  public final Service get(Class serviceClass) throws ServiceException
  {
    synchronized (repository)
    {
      // Loop through the repository
      for (Iterator iterator = repository.iterator(); iterator.hasNext();)
      {
        // Extract the current service
        Service element = (Service) iterator.next();

        // Check for matching service id
        if (serviceClass.isAssignableFrom(element.getClass()))
        {
          return element;
        }
      }

      // Not found
      throw new ServiceException("can not find " + serviceClass.getName());
    }
  }

  /**
   * Find matching services which has the same signiture as the specified service class.
   * This method always returns the first match.
   * @param serviceClass The class to search for
   * @return Service The match service or null is not found.
   */
  public final Service find(Class serviceClass)
  {
    synchronized (repository)
    {
      // Loop through the repository
      for (Iterator iterator = repository.iterator(); iterator.hasNext();)
      {
        // Extract the current service
        Service element = (Service) iterator.next();

        // Check for matching service id
        if (serviceClass.isAssignableFrom(element.getClass()))
        {
          return element;
        }
      }

      // Not found
      return null;
    }
  }

  /**
   * Find matching services which has the same signiture as the specified service class.
   * This method return all matching services
   * @param serviceClass The class of service to search for
   * @return Service[] Array of match services or zero length array if no matches are found
   */
  public final Service[] findAll(Class serviceClass)
  {
    synchronized (repository)
    {
      // Create empty list
      List matches = new ArrayList(repository.size());

      // Loop through the repository
      for (Iterator iterator = repository.iterator(); iterator.hasNext();)
      {
        // Extract the current service
        Service element = (Service) iterator.next();

        // Check for matching service id
        if (serviceClass.isAssignableFrom(element.getClass()))
        {
          // Add to the matches list
          matches.add(element);
        }
      }

      // Create the array of matches
      return (Service[]) matches.toArray(new Service[matches.size()]);
    }
  }

  /**
   * Find service with the match instance name
   * @param instanceName The instance name to match
   * @return Service The matching service or null is not found
   */
  public final Service find(String instanceName)
  {
    synchronized (repository)
    {
      // Loop through the repository
      for (Iterator iterator = repository.iterator(); iterator.hasNext();)
      {
        // Extract the current service
        Service element = (Service) iterator.next();

        // Check for matching service id
        if (element.getInstanceName().equalsIgnoreCase(instanceName))
        {
          return element;
        }
      }

      // Not found
      return null;
    }
  }

  /**
   * Add a service to the respository
   * @param service The service to add
   */
  public void add(Service service)
  {
    synchronized (repository)
    {
      repository.add(service);
    }
  }

  /**
   * Removes a service with as matching class and instance name
   * @param service The service to remove
   */
  public void remove(Service service)
  {
    synchronized (repository)
    {
      repository.remove(service);
    }
  }

  /**
   * Invoke the initialize all service manager method
   * @see com.redrocketcomputing.appframework.service#initializeAll()
   * @see java.util.Observer#update(Observable, Object)
   */
  public void update(Observable observable, Object data)
  {
  }

}
