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
 * $Id $
 */

package com.streetfiresound.clientlib.event;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.redrocketcomputing.util.log.LoggerSingleton;

import com.streetfiresound.clientlib.Util;


/**
 * Simple event dispatcher
 * @author iain huxley
 */
public abstract class EventDispatcher
{
  private List listeners = new LinkedList();

  public EventDispatcher()
  {
  }

  public final void queueEvent(StreetFireEvent event)
  {
    event.dispatcher = this;
    queueEventImpl(event);

    //XXX:00000000000000000000000000000000000:20050322iain: suppressing playpos events
    if (!(event instanceof PlayPositionUpdateEvent))
    {
      LoggerSingleton.logDebugCoarse(EventDispatcher.class, "queueEvent",    ">>>>>>>> Queued event: " + event);
    }
  }

  public abstract void queueEventImpl(StreetFireEvent event);

  /**
   *  dispatches an event to all registered listeners in an unspecified order
   */
  public synchronized void dispatchEvent(StreetFireEvent event)
  {
    //XXX:00000000000000000000000000000000000:20050322iain: suppressing playpos events
    if (!(event instanceof PlayPositionUpdateEvent))
    {
      LoggerSingleton.logDebugCoarse(EventDispatcher.class, "dispatchEvent", ">>>> Dispatching event: " + event);
    }

    // iterate through listeners
    for (Iterator i=listeners.iterator(); i.hasNext();)
    {
      StreetFireEventListener listener = (StreetFireEventListener)i.next();

      // dispatch to listener
      long startTime = System.currentTimeMillis();
      listener.eventNotification(event);

      // check for excessive dispatch times
      long dispatchTime = System.currentTimeMillis() - startTime;
      if (dispatchTime > 200)
      {
        LoggerSingleton.logWarning(EventDispatcher.class, "dispatchEvent", Util.padStringWithSpaces(String.valueOf(dispatchTime), 4, false) + "ms taken by '" + listener + "' to process event notification");
      }
    }
  }

  public synchronized void addListener(StreetFireEventListener listener)
  {
    listeners.add(listener);
  }

  public synchronized void removeListener(StreetFireEventListener listener)
  {
    listeners.remove(listener);
  }
}
