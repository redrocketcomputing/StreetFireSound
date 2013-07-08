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
 * $Id: Task.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.taskpool;

import com.redrocketcomputing.util.concurrent.Channel;

/**
 * Required interface for all tasks (threads) with use the TaskPoolService
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public interface Task extends Runnable
{
  /**
   * Return the name of the task.  This is used to customize the task pool worker thread
   * @return String The name of the task
   */
  public String getTaskName();

  /**
   * Return the desired priority to the task.  This maybe modified by the TaskPoolService to meet pool policies
   * @return int The desired task priority
   */
  public int getTaskPriority();

  /**
   * Invoked by the TaskPoolServices when the thread executing the task would block waiting for an available thread
   * to handle the task. If this method returns without an exception the current thread will block waiting for
   * an available thread to execute the task.
   * @param Channel Synchronization point for the task to wait on if it chooses.
   * @throws TaskAbortedException Thrown if the task does not want to block.
   */
  public void executionBlocked(Channel handoff) throws TaskAbortedException;

}
