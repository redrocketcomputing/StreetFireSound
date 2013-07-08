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
 * $Id: AbstractTask.java,v 1.2 2005/03/02 04:17:19 iain Exp $
 */

package com.redrocketcomputing.appframework.taskpool;

import com.redrocketcomputing.util.concurrent.Channel;
import com.redrocketcomputing.util.concurrent.SynchronizedInt;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class AbstractTask implements Task
{
  /**
   * Constructor for AbstractTask.
   */
  public AbstractTask()
  {
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#executionBlocked(Channel)
   */
  public void executionBlocked(Channel handoff) throws TaskAbortedException
  {
    throw new TaskAbortedException("aborted due to lack of resources");
  }

  /**
   * override to provide a descriptive name for the task
   * default implementation provides the toString() result (which is often sufficient)
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "Unspecified task '" + toString() + "'";
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskPriority()
   */
  public int getTaskPriority()
  {
    return Thread.NORM_PRIORITY;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
  }

}
