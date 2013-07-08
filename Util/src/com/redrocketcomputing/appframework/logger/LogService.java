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
 * $Id: LogService.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.util.Observable;
import java.util.Observer;
import java.lang.reflect.*;
import java.io.*;

import com.redrocketcomputing.util.configuration.*;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.redrocketcomputing.appframework.logger.commands.LoggerShell;
import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * Implements a configurable logging service within the Service framework
 *
 * @author stephen Jul 16, 2003
 * @version 1.0
 *
 */
public class LogService extends AbstractService implements Observer, Logger
{
  private LogFilter stack = null;
  private ComponentConfiguration configuration;
  private Logger oldLogger = null;

	/**
	 * Constructor for LogService
	 */
  public LogService(String instanceName)
  {
    // Construct superclass
    super(instanceName);

    // Create a component configuration interface
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);

    // Register configuration update observer
    configuration.addObserver(this);
  }

	/**
	 * @see com.redrocketcomputing.appframework.service.Service#initialize(ComponentConfiguration)
	 */
	public synchronized void start() throws ServiceException
	{
    // Check service state
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException(getInstanceName() + " is not IDLE");
    }

    // Install global log shell
    Shell.installGlobally(LoggerShell.class);

    // Build new filter stack
    String className = "unknown";
    String instanceName = "unknown";
		try
		{
      String property;
			int i = 0;
			LogFilter previousFilter = null;
			while ((property = configuration.getProperty(Integer.toString(i))) != null)
			{
        // Parse the property
        instanceName = property.substring(0, property.indexOf(',')).trim();
        className = property.substring(property.indexOf(',') + 1).trim();

			  // Get the Class for the type
			  Class filterClass = Class.forName(className);
			  if (!AbstractLogFilter.class.isAssignableFrom(filterClass))
			  {
			    // Bad type
			    throw new ServiceException("bad type: " + className);
			  }

			  // Constructor query
			  Class[] parameterTypes = new Class[2];
			  parameterTypes[0] = String.class;
			  parameterTypes[1] = LogFilter.class;

			  // Get the constructor
			  Constructor constructor = filterClass.getConstructor(parameterTypes);

			  // Build constructor arguments
			  Object[] arguments = new Object[2];
			  arguments[0] = instanceName;
			  arguments[1] = previousFilter;

			  // Create new filter
			  previousFilter = (LogFilter)constructor.newInstance(arguments);

			  // Move to next filter
			  i++;
			}

			// Save the stack
			stack = previousFilter;

      // Make sure aleast one filter was installed
      if (stack != null)
      {
        // Create initialize command for the stack
        StartFilterCommand initializeCommand = new StartFilterCommand(null);

        // Execute the command
        stack.executeFilterCommand(initializeCommand);

        // Bind singleton to the service
        LoggerSingleton.bindLogger(this);
      }

      // Replace stdout and stderr
      if (configuration.getBooleanProperty("std.override", true))
      {
        System.setOut(new PrintStream(new LoggerOutputStream("INFO")));
        System.setErr(new PrintStream(new LoggerOutputStream("ERROR")));
      }

      // Set the state to running
      setServiceState(Service.RUNNING);

      // Log service start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running");
		}
		catch (ClassNotFoundException e)
		{
      // Translate to service exception
      throw new ServiceException("ClassNotFoundException: " + className + ':' + instanceName);
		}
		catch (NoSuchMethodException e)
		{
      // Translate to service exception
      throw new ServiceException("NoSuchMethodException: " + className + ':' + instanceName);
		}
		catch (InstantiationException e)
		{
      // Translate to service exception
      throw new ServiceException("InstantiationException: " + className + ':' + instanceName);
		}
		catch (IllegalAccessException e)
		{
      // Translate to service exception
      throw new ServiceException("IllegalAccessException: " + className + ':' + instanceName);
		}
		catch (InvocationTargetException e)
		{
      // Translate to service exception
      throw new ServiceException("InvocationTargetException: " + className + ':' + instanceName);
		}
	}

	/**
	 * @see com.redrocketcomputing.appframework.service.Service#terminate()
	 */
	public synchronized void terminate() throws ServiceException
	{
    // Check for IDLE state
    if (getServiceState() == Service.IDLE)
    {
      // Opps
      throw new ServiceException(getInstanceName() + " is not running");
    }

    // Check to see if we should restore the old logger
    if (oldLogger != null)
    {
      // Restore old logger to the singleton
      LoggerSingleton.bindLogger(oldLogger);
    }

    // Build terminate command
    LogFilterCommand terminateCommand = new TerminateFilterCommand(null);

    // Send terminate command
    stack.executeFilterCommand(terminateCommand);

    // Clear the stack
    stack = null;

    // Set state to idle
    setServiceState(Service.IDLE);

    // Log termination of service
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
	}

	/**
	 * @see com.redrocketcomputing.appframework.service.Service#infor()
	 */
	public synchronized void info(PrintStream printStream, String[] arguments) throws ServiceException
	{
    // Print header
    printStream.println("Configuration for " + getInstanceName() + "[" + getServiceState() + "]");

    // Loop down the stack
    LogFilter current = stack;
    while (current != null)
    {
      // Print filter information
      printStream.println("LogFilter: " + current.getClass().getName() + "::" + current.getInstanceName());

      // Move to next filter
      current = current.getNextFilter();
    }
	}

	/**
	 * Log a message by pushing the message throught the LogFilter stack. This method throughs a illegal state exception
   * if the service is not running.
	 * @param message The message to log
	 */
  public void log(String message)
  {
    // Push down the stack
    stack.log(message);
  }

  /**
   * Execute a filter command on the stack
   * @param command The filter command to execute
   */
  public void execute(LogFilterCommand command)
  {
    // Forward to the stack
    stack.executeFilterCommand(command);
  }

	/**
	 * @see java.util.Observer#update(Observable, Object)
	 */
	public void update(Observable observable, Object data)
	{
		try
		{
			start();
		}
		catch (ServiceException e)
		{
      // Dump stack
      e.printStackTrace();

      // Big Problem, translate to runtime exception
      throw new RuntimeException("ServiceException during reconfiguration");
		}
	}

}
