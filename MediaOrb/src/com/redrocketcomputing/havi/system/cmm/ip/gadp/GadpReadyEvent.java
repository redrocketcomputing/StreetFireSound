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
 * $Id: GadpReadyEvent.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import org.havi.system.types.GUID;

import com.redrocketcomputing.appframework.event.Event;
import com.redrocketcomputing.appframework.event.EventListener;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class GadpReadyEvent implements Event
{
  private GUID localGuid;

  /**
   * Constructor for GadpReadyEvent.
   */
  public GadpReadyEvent(GUID localGuid)
  {
    // Construct super class
    super();

    // Save the local guid
    this.localGuid = localGuid;
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#getEventListenerType()
   */
  public Class getEventListenerType()
  {
    return GadpReadyEventListener.class;
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#dispatch(EventListener)
   */
  public void dispatch(EventListener listener)
  {
    // Check type
    if (!(listener instanceof GadpReadyEventListener))
    {
      throw new IllegalArgumentException("bad interface type");
    }

    // Forward
    ((GadpReadyEventListener)listener).readyEvent(localGuid);
  }

}
