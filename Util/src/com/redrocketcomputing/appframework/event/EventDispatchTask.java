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
 * $Id: EventDispatchTask.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.event;

import java.util.List;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;

/**
 * Internal task for dispatching Application Framework Events
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class EventDispatchTask extends AbstractTask
{
  private Event event;
  private EventListener listener;

  /**
   * Constructor for EventDispatchTack.
   */
  public EventDispatchTask(Event event, EventListener listener)
  {
    // Save the event and listener
    this.event = event;
    this.listener = listener;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // Dispatch
    event.dispatch(listener);
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "EventDispatch::" + event.getClass().getName();
  }
}
