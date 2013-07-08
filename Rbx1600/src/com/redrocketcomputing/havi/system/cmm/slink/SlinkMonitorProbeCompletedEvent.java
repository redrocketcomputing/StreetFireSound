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
 * $Id: SlinkMonitorProbeCompletedEvent.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.slink;

import com.redrocketcomputing.appframework.event.Event;
import com.redrocketcomputing.appframework.event.EventListener;

/**
 * @author stephen
 *
 */
public class SlinkMonitorProbeCompletedEvent implements Event
{
  int[] newDevices;
  int[] goneDevices;
  int[] activeDevices;

  /**
   * Constructor for SlinkMonitorProbeCompletedEvent.
   */
  public SlinkMonitorProbeCompletedEvent(int[] newDevices, int[] goneDevices, int[] activeDevices)
  {
    // Construct super class
    super();

    // Save the parameters
    this.newDevices = newDevices;
    this.goneDevices = goneDevices;
    this.activeDevices = activeDevices;
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#getEventListenerType()
   */
  public Class getEventListenerType()
  {
    return SlinkMonitorProbeStartedEventListener.class;
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#dispatch(EventListener)
   */
  public void dispatch(EventListener listener)
  {
    // Check the type
    if (!(listener instanceof SlinkMonitorProbeCompletedEventListener))
    {
      // Badness
      throw new IllegalArgumentException("bad interface type");
    }

    // Dispatch
    ((SlinkMonitorProbeCompletedEventListener)listener).probeCompleted(newDevices, goneDevices, activeDevices);
  }
}
