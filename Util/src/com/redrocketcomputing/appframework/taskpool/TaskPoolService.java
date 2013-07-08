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
 * $Id: TaskPoolService.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.taskpool;

import java.io.PrintStream;

import com.redrocketcomputing.util.concurrent.Channel;
import com.redrocketcomputing.util.concurrent.PooledExecutor;
import com.redrocketcomputing.util.concurrent.SynchronizedInt;
import com.redrocketcomputing.util.concurrent.SynchronousChannel;
import com.redrocketcomputing.util.concurrent.ThreadFactory;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;

/**
 * Implements a shared task pool service for the application framework.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class TaskPoolService extends AbstractService implements TaskPool
{
  /**
   * The default maximum pool size, this is Integer.MAX_VALUE
   **/
  public static final int DEFAULT_MAXIMUM_POOL_SIZE = Integer.MAX_VALUE;

  /**
   * The default minimum pool size, This is set to 1
   **/
  public static final int DEFAULT_MINIMUM_POOL_SIZE = 1;

  /**
   * The default worker thread keep alive time.
   **/
  public static final long DEFAULT_KEEP_ALIVE = 60 * 1000;

  private SynchronizedInt idleThreads = new SynchronizedInt(0);
  private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
  private int minimumPoolSize = DEFAULT_MINIMUM_POOL_SIZE;
  private long keepAlive = DEFAULT_KEEP_ALIVE;
  private Channel handoff = new SynchronousChannel();
  private boolean shutdown = true;

  private ComponentConfiguration configuration;
  private ThreadGroup threadGroup = null;

  /**
   * Constructor for TaskPoolService with the specified service name
   * @param instanceName The instance name of the TaskPoolService
   */
  public TaskPoolService(String instanceName)
  {
    // Construct super class
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
      throw new ServiceException("TaskPoolService is already running");
    }

    // Get Thread and Group parameters
    String threadGroupName = configuration.getProperty("thread.group.name", getInstanceName());
    int threadPriority = configuration.getIntProperty("thread.priority", Thread.NORM_PRIORITY);

    // Create new thread group
    threadGroup = new ThreadGroup(threadGroupName);
    threadGroup.setDaemon(true);

    // Get PoolExecutor parameters
    keepAlive = configuration.getLongProperty("keep.alive", DEFAULT_KEEP_ALIVE);
    minimumPoolSize = configuration.getIntProperty("minimum.pool.size", DEFAULT_MINIMUM_POOL_SIZE);
    maximumPoolSize = configuration.getIntProperty("maximum.pool.size", DEFAULT_MAXIMUM_POOL_SIZE);

    // Mark as not shutdown
    shutdown = false;

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
      throw new ServiceException("TaskPoolService is not running");
    }

    // Mark as shuting down
    shutdown = true;

    // Make pool size zero to stop thread creation
    maximumPoolSize = 0;
    minimumPoolSize = 0;

    // Interrupt all threads
    threadGroup.interrupt();

    // Change state to IDLE
    setServiceState(Service.IDLE);

    // Enable GC on the thread group
    threadGroup = null;

    // Log termination of event dispatcher
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Display the counters
    printStream.println("Pool Size: " + threadGroup.activeCount());
    printStream.println("Idle Threads: " + idleThreads.get());

    // Display the thread pool
    printStream.println("Pool Threads:");
    Thread[] poolThreads = new Thread[threadGroup.activeCount()];
    int count = threadGroup.enumerate(poolThreads);
    for (int i = 0; i < count; i++)
    {
      // Cast to worker thread
      if (poolThreads[i] instanceof WorkerThread)
      {
        // Cast it up
        WorkerThread workerThread = (WorkerThread)poolThreads[i];

        // Display the thread name
        printStream.println("   " + workerThread.getThreadId() + ' ' + workerThread.getPriority() + ' ' + workerThread.getDispatchCounter() + ' ' + workerThread.getName());
      }
      else
      {
        // Display the thread name
        printStream.println("   " + -1 + ' ' + poolThreads[i].getPriority() + ' ' + -1 + ' ' + poolThreads[i].getName());
      }
    }
  }

  /**
   * Try to execute the specified task.
   * @param task The task to execute
   * @throws TaskAbortedException Throw is the execution of the task is blocked due to shutdown or
   * lack of resources
   */
  public void execute(Task task) throws TaskAbortedException
  {
    try
    {
      // Check for shutdown
      if (shutdown)
      {
        // Throw task aborted exception
        throw new TaskAbortedException("pool is shutdown");
      }

      // Insure the minumim number of worker threads
      if (threadGroup.activeCount() < minimumPoolSize)
      {
        // Create the new thread
        WorkerThread thread = new WorkerThread(this, threadGroup, task);

        // Start it up
        thread.setDaemon(true);
        thread.start();

        // All done
        return;
      }

      // Try to handoff to an existing thread
      if (handoff.offer(task, 0))
      {
        // All done, we handed off the task
        return;
      }

      // Blocked, can we add a new thread
      if (threadGroup.activeCount() < maximumPoolSize)
      {
        // Create the new thread
        WorkerThread thread = new WorkerThread(this, threadGroup, task);

        // Start it up
        thread.setDaemon(true);
        thread.start();

        // All done
        return;
      }

      // Ask the task what we should do
      task.executionBlocked(handoff);
    }
    catch (InterruptedException e)
    {
      // Clear the task state and throw a task aborted exception
      Thread.currentThread().interrupted();
      throw new TaskAbortedException("interrupted during handoff");
    }
  }

  /**
   * Returns a task to run.  This method is used by the worker threads to get some work.
   * @return Task The task to execute or null if we should terminate
   * @throws InterruptedException
   */
  Task getTask()
  {
    try
    {
      // Increment idle count
      idleThreads.increment();

      // Check to see if the pool is too big
      if (threadGroup.activeCount() > maximumPoolSize)
      {
        // Cause current thread to exit by return null task
        return null;
      }

      // Check for shutdown when determining the time to wait on the queue
      long waitTime = shutdown ? 0 : keepAlive;

      try
      {
        // Get a task, if keep alive time is less than zero, wait until interrupted
        if (waitTime >= 0)
        {
          return (Task)handoff.poll(waitTime);
        }
        else
        {
          return (Task)handoff.take();
        }
      }
      catch (InterruptedException e)
      {
        // Interrupted, we must be existing, return null to the worker thread
        return null;
      }
    }
    finally
    {
      // Decrement idle threads
      idleThreads.decrement();
    }
  }
}
