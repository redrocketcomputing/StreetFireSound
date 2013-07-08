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
 * $Id: EventDispatch.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.event;

/**
 * Generic event dispatch service for the Application Framework.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public interface EventDispatch
{
  /**
   * Add a new listener to the internal listener dispatch map.  The listener class is inspected to determine
   * the types of event to handle.  This resolved by identifing the EventListener interfaces implemented.
   * @param listener The new event listener
   */
  public void addListener(EventListener listener);

  /**
   * Remove the specified listener from the interanl dispatch map.
   * @param listener The listener to remove
   */
  public void removeListener(EventListener listener);


  /**
   * Remove all listeners of the specified class type.
   * @param listenerClass The class of listeners to flush, normally this is a marker interface type
   */
  public void flushListeners(Class listenerClass);

  /**
   * Dispatch the listener based on the event type using the Executor provided at construction.
   * @param event The event to dispatch
   */
  public void dispatch(Event event);

}
