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
 * $Id: GadpTimeoutMessage.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class GadpTimeoutMessage extends GadpMessage
{
  public final static int MARSHAL_TYPE = 0;

  /**
   * Constructor for GadpTimeoutMessage.
   */
  public GadpTimeoutMessage()
  {
    super();
  }

  public GadpTimeoutMessage(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Forward
    unmarshal(hbais);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessage#dispatch(GadpMessageHandler)
   */
  public void dispatch(GadpMessageHandler handler)
  {
    // Forward
    handler.handleTimeout();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    return (o instanceof GadpTimeoutMessage);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return MARSHAL_TYPE + 480239;
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    // Super first
    super.marshal(hbaos);
  }

  /**
   * @see org.havi.system.types.Marshallable#unmarshal(HaviByteArrayInputStream)
   */
  public void unmarshal(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Super first
    super.unmarshal(hbais);
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "GadpTimeoutMessage";
  }
}
