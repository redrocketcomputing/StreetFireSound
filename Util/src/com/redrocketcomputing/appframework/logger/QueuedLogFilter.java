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
 * $Id: QueuedLogFilter.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.util.Observable;
import java.util.Observer;

import com.redrocketcomputing.util.concurrent.Channel;
import com.redrocketcomputing.util.concurrent.LinkedQueue;
import com.redrocketcomputing.util.concurrent.WaitableInt;

import com.redrocketcomputing.util.configuration.*;

/**
 * Description:
 *
 * @author stephen Jul 15, 2003
 * @version 1.0
 *
 */
public class QueuedLogFilter extends AbstractLogFilter implements Runnable, Observer
{
  private final static int RUNNING = 0;
  private final static int IDLE = 1;
  private final static int SUSPENDED = 2;

  private Channel queue = new LinkedQueue();
  private volatile Thread thread = null;
  private WaitableInt state = new WaitableInt(IDLE);
  private ComponentConfiguration configuration;
  private int priority;

	/**
	 * Constructor for QueuedLogFilter.
	 * @param instanceName
	 * @param nextFilter
	 */
	public QueuedLogFilter(String instanceName, LogFilter nextFilter)
	{
    // Initialize super class
		super(instanceName, nextFilter);

    // Create component configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
	}

	/**
	 * @see com.redrocketcomputing.util.log.AbstractLogFilter#initialize()
	 */
	public synchronized void start()
	{
    // Check state and terminate is not idle
    if (state.get() != IDLE)
    {
      // Terminate any running logger
      terminate();
    }

    // Get thread priority
    priority = configuration.getIntProperty("priority", Thread.currentThread().getPriority());

    // Create new thread
    thread = new Thread(this, getInstanceName());

    // Set the thread priority
    thread.setPriority(priority);

    // Mark the state as running
    state.set(RUNNING);

    // Start the thread running
    thread.start();
	}

	/**
	 * @see com.redrocketcomputing.util.log.AbstractActiveLogFilter#supend()
	 */
	public synchronized void suspend()
	{
    // Set state to suspend
    state.set(SUSPENDED);

    // Notify the thread
    thread.interrupt();
	}

	/**
	 * @see com.redrocketcomputing.util.log.AbstractActiveLogFilter#resume()
	 */
	public synchronized void resume()
	{
    // Set state to running
    state.set(RUNNING);

    // Notify the thread
    thread.interrupt();
	}

	/**
	 * @see com.redrocketcomputing.util.log.AbstractActiveLogFilter#terminate()
	 */
	public synchronized void terminate()
	{
    // Set state to idle
    state.set(IDLE);

    // Interrupt the thread
    Thread temp = thread;
    thread = null;
    temp.interrupt();
	}

  /**
   * @see com.redrocketcomputing.util.log.LogFilter#log(int, StringBuffer, StringBuffer)
   */
  public void log(String message)
  {
		try
		{
			// Wait for state to goto running
			state.whenEqual(RUNNING, null);

			// Add the message to the queue
			queue.put(message);
		}
		catch (InterruptedException e)
		{
      // Ignore, maybe we should clear the interrupt flag for the thread
		}
  }

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
    // Loop thread is null
    while (thread != null)
    {
			try
			{
				// Wait on queue
				String message = (String)queue.take();

        // Forward on
        forward(message);
			}
			catch (InterruptedException e)
			{
        // Clear the interrupted state, we maybe stopped
        Thread.currentThread().interrupted();
			}
    }
	}

	/**
	 * @see java.util.Observer#update(Observable, Object)
	 */
	public void update(Observable observable, Object data)
	{
    // Invoke initialize
    start();
	}
}
