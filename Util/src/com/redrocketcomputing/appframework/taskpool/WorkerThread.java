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
 * $Id: WorkerThread.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.taskpool;

import com.redrocketcomputing.util.Util;
import com.redrocketcomputing.util.concurrent.SynchronizedInt;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Internal TaskPoolService worker thread
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class WorkerThread extends Thread
{
  private static SynchronizedInt nextWorkerThreadId = new SynchronizedInt(0);

  private int threadId = nextWorkerThreadId.increment();

  protected TaskPoolService parent;
  protected long startTime = System.currentTimeMillis();
  protected long executionTime = 0;
  protected int dispatchCounter = 0;
  protected Task firstTask;

  /**
   * Constructor for WorkerThread.
   * @param group The thread group for worker threads
   * @param runnable The task or command to execute
   */
  public WorkerThread(TaskPoolService parent, ThreadGroup group, Task task)
  {
    // Construct super class
    super(group, "IDLE");

    // Save the parent
    this.parent = parent;

    // Save the task
    this.firstTask = task;
  }

  /**
   * Loop dispatching tasks until interrupted or shutdown requested
   */
  public void run()
  {
    try
    {
      // Save the initialial priority
      int initialPriority = getPriority();

      // Initial task with the first task to run and enable GC on the first task
      Task task = firstTask;
      firstTask = null;

      // Try to run the first task
      if (task != null)
      {
        // Change the thread name
        setName(task.getTaskName());

        // Change the priority
        setPriority(task.getTaskPriority());

        // Update the dispatch count
        dispatchCounter++;

        // Mark task start time
        long taskStartMark = System.currentTimeMillis();

        // Run the task
        task.run();

        // Update execution time
        executionTime = executionTime + (System.currentTimeMillis() - taskStartMark);

        // Change to idle
        setName("IDLE");

        // Restore the initial priority
        setPriority(initialPriority);
      }

      // Loop until interrupted or the parent returns null task indicated we should exist
      while ((task = parent.getTask()) != null)
      {
        // Change the thread name
        setName(task.getTaskName());

        // Change the priority
        setPriority(task.getTaskPriority());

        // Update the dispatch count
        dispatchCounter++;

        // Mark task start time
        long taskStartMark = System.currentTimeMillis();

        // Run the task
        task.run();

        // Update execution time
        executionTime = executionTime + (System.currentTimeMillis() - taskStartMark);

        // Change to idle
        setName("IDLE");

        // Restore the initial priority
        setPriority(initialPriority);

        // Allow GC on the task
        task = null;
      }
    }
    catch (Exception e)
    {
      LoggerSingleton.logError(this.getClass(), "run", "uncaught exception: " + Util.getStackTrace(e));
    }
  }

  /**
   * Returns the dispatchCounter.
   * @return int
   */
  public int getDispatchCounter()
  {
    return dispatchCounter;
  }

  /**
   * Returns the executionTime.
   * @return long
   */
  public long getExecutionTime()
  {
    return executionTime;
  }

  /**
   * Returns the startTime.
   * @return long
   */
  public long getStartTime()
  {
    return startTime;
  }

  /**
   * Returns the threadId.
   * @return int
   */
  public int getThreadId()
  {
    return threadId;
  }
}
