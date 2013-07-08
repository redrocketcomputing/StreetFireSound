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
 * $Id: NetworkResetEvent.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.event;

import com.redrocketcomputing.appframework.event.Event;
import com.redrocketcomputing.appframework.event.EventListener;

/**
 * @author stephen
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class NetworkResetEvent implements Event
{

  /**
   * Constructor for NetworkResetEvent.
   */
  public NetworkResetEvent()
  {
    super();
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#getEventListenerType()
   */
  public Class getEventListenerType()
  {
    return NetworkResetEventListener.class;
  }

  /**
   * @see com.redrocketcomputing.appframework.event.Event#dispatch(EventListener)
   */
  public void dispatch(EventListener listener)
  {
    // Check type
    if (!(listener instanceof NetworkResetEventListener))
    {
      throw new IllegalArgumentException("bad interface type");
    }

    // Forward
    ((NetworkResetEventListener)listener).networkResetEvent();
  }

}
