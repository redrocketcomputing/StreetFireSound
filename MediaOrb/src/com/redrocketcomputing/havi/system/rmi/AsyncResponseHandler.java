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
 * $Id: AsyncResponseHandler.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import gnu.trove.TLinkable;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.concurrent.Latch;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
class AsyncResponseHandler extends AbstractTask implements TLinkable
{
  private AsyncResponseHelper parent;
  private TLinkable previous;
  private TLinkable next;
  private String taskName;
  private AsyncResponseListener listener;
  private int duration;
  private int transactionId;
  private Latch cancel = new Latch();
  private AsyncResponseInvocation invocation = null;

  /**
   * Constructor for AsyncResponseHandler.
   * @param listener
   * @param duration
   * @param transaction
   */
  public AsyncResponseHandler(AsyncResponseHelper parent, int transactionId, AsyncResponseListener listener, int duration)
  {
    // Construct super class
    super();

    // Check parametes
    if (parent == null || listener == null || duration < 0)
    {
      // Bad
      throw new IllegalArgumentException("bad parameter");
    }

    try
    {
      // Save parameters
      this.parent = parent;
      this.listener = listener;
      this.duration = duration;
      this.transactionId = transactionId;

      // Build task name
      taskName = "AsyncResponseHandler[" + transactionId + ']';

      // Get the task pool
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Very bad
        throw new IllegalStateException("can not find task pool service");
      }

      // Launch the task
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      // Just log the error, this may cause a memory leak if the user does not get a response message
      LoggerSingleton.logError(this.getClass(), "AsyncResponseHandler", e.toString());
    }
  }

  public void cancel()
  {
    // Release the latch to that the timer thread exits
    cancel.release();
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return taskName;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Wait on the latch
      if (cancel.attempt(duration))
      {
        // All done
        return;
      }

      // Time occurred dispatch the listener
      parent.timeout(this);
    }
    catch (InterruptedException e)
    {
      // Invoke timeout due to interruption
      parent.timeout(this);
    }
  }

  /**
   * Returns the listener.
   * @return AsyncResponseListener
   */
  public AsyncResponseListener getListener()
  {
    return listener;
  }

  /**
   * Returns the transactionId.
   * @return int
   */
  public int getTransactionId()
  {
    return transactionId;
  }

  /**
   * @see gnu.trove.TLinkable#getNext()
   */
  public TLinkable getNext()
  {
    return next;
  }

  /**
   * @see gnu.trove.TLinkable#getPrevious()
   */
  public TLinkable getPrevious()
  {
    return previous;
  }

  /**
   * @see gnu.trove.TLinkable#setNext(TLinkable)
   */
  public void setNext(TLinkable linkable)
  {
    next = linkable;
  }

  /**
   * @see gnu.trove.TLinkable#setPrevious(TLinkable)
   */
  public void setPrevious(TLinkable linkable)
  {
    previous = linkable;
  }
}
