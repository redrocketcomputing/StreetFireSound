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
 * $Id: CommandManager.java,v 1.1 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.havi.commandcontroller;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.havi.system.types.HaviException;

import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.concurrent.BoundedBuffer;
import com.redrocketcomputing.util.concurrent.Channel;
import com.redrocketcomputing.util.concurrent.InterruptableChannel;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author david
 * 
 * To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates. To enable and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */
public class CommandManager extends AbstractService implements Task
{
  private final static int DEFAULT_QUEUE_SIZE = 5;

  private List factories = Collections.synchronizedList(new ArrayList());
  private InterruptableChannel commandQueue = null;

  /**
   * Constructor for CommandManager.
   */
  public CommandManager(String instanceName)
  {
    // Construct super class
    super(instanceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Check to see if we are already running
    if (getServiceState() == Service.RUNNING)
    {
      // Bad
      throw new ServiceException("service is already running");
    }

    try
    {
      // Get the configured queue size
      int queueSize = getConfiguration().getIntProperty("queue.size", DEFAULT_QUEUE_SIZE);

      // Create queue
      commandQueue = new InterruptableChannel(new BoundedBuffer(queueSize));

      // Launch dispath task
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Make sure we are not idle
    if (getServiceState() == Service.IDLE)
    {
      // Bad
      throw new ServiceException("service is already idle");
    }

    // Interrupt the task
    commandQueue.interrupt();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.appframework.service.Service#info(java.io.PrintStream, java.lang.String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    printStream.println("not implemented");
  }

  /**
   * Add an command factory to the list of registered command factories
   * 
   * @param factory The AbstractCommandFactory to add
   */
  public void addFactory(AbstractCommandFactory factory)
  {
    // Add the factory
    factories.add(factory);
  }

  /**
   * Remove the command factory from the list of registered command factories
   * 
   * @param factory The AbstractCommandFactory to remove
   */
  public void removeFactory(AbstractCommandFactory factory)
  {
    // Remove all match factories
    while (factories.remove(factory))
      ;
  }

  /**
   * Build an array of AbstractCommands for the specified key
   * 
   * @param key The AbstractCommandFactory key
   * @param parameter The opaque parameter to pass to the Command
   * @return An array of AbstractCommand to execute
   */
  public AbstractCommand[] createCommands(String key, Object parameter)
  {
    List commands = new ArrayList();

    synchronized (factories)
    {
      // Loop through registered factories and create commands
      for (Iterator iterator = factories.iterator(); iterator.hasNext();)
      {
        // Extract element
        AbstractCommandFactory element = (AbstractCommandFactory)iterator.next();

        // Try to create command
        AbstractCommand command = element.createCommand(key, parameter);
        if (command != null)
        {
          // Save the command
          commands.add(command);
        }
      }
    }

    // Return the array of commands
    return (AbstractCommand[])commands.toArray(new AbstractCommand[commands.size()]);
  }

  /**
   * Queue the command for threaded execution
   * 
   * @param command The AbstractCommand to queue
   * @return True if the command was successfully queue, false otherwise
   */
  public boolean queueCommand(AbstractCommand[] commands)
  {
    boolean success = true;
    try
    {
      // add command to the command queue for executtion
      success = commandQueue.offer(commands, 0);
    }
    catch (InterruptedException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "addToCommandQueue", "failed to add command into command queue due to InterruptedException");
    }

    return success;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Loop until interrupted
      while (true)
      {
        // Wait for a command
        AbstractCommand[] commands = (AbstractCommand[])commandQueue.take();
        if (commands != null)
        {
          // Loop through the command executing each in turn
          for (int i = 0; i < commands.length; i++)
          {
            try
            {
              // Execute the command
              commands[i].execute();
            }
            catch (HaviException e)
            {
              LoggerSingleton.logWarning(this.getClass(), "run", "error occured while executing command: " + commands[i].getClass().toString());
            }
          }
        }
      }
    }
    catch (InterruptedException e)
    {
      // Release the command queue
      commandQueue = null;
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#executionBlocked(Channel)
   */
  public void executionBlocked(Channel handoff) throws TaskAbortedException
  {
    throw new TaskAbortedException("aborted due to lack of resources");
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "CommandManager::" + getClass().getName();
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskPriority()
   */
  public int getTaskPriority()
  {
    return Thread.NORM_PRIORITY;
  }

}