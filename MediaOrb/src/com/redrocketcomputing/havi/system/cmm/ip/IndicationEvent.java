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
 * $Id: IndicationEvent.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip;

import org.havi.system.types.GUID;

import com.redrocketcomputing.appframework.event.Event;
import com.redrocketcomputing.appframework.event.EventListener;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class IndicationEvent implements Event
{
  private GUID guid;
  private int indication;
  private int address;
  private Object data;

  /**
   * Constructor for IndicationEvent.
   */
  public IndicationEvent(GUID guid, int address, int indication, Object data)
  {
    // Construct super
    super();

    // Check parameters
    if (guid == null)
    {
      // Throw runtime exception
      throw new IllegalArgumentException("guid is null");
    }

    // Save the parameters
    this.guid = guid;
    this.indication = indication;
    this.address = address;
    this.data = data;
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#getEventListenerType()
   */
  public Class getEventListenerType()
  {
    return IndicationEventListener.class;
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#dispatch(EventListener)
   */
  public void dispatch(EventListener listener)
  {
    // Check type
    if (!(listener instanceof IndicationEventListener))
    {
      throw new IllegalArgumentException("bad interface type");
    }

    // Forward
    ((IndicationEventListener)listener).indicationEvent(guid, address, indication, data);
  }

}
