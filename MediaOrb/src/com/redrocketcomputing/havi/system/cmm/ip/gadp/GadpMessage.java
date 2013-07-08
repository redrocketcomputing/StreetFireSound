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
 * $Id: GadpMessage.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import java.io.IOException;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviObject;
import org.havi.system.types.HaviUnmarshallingException;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
abstract class GadpMessage extends HaviObject
{
  private final static GadpMessageFactory factory = new GadpMessageFactory();

  public final static GadpMessage create(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Forward to the factory
    return factory.create(hbais);
  }

  /**
   * Constructor for GadpMessage.
   */
  public GadpMessage()
  {
    super();
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  public abstract void dispatch(GadpMessageHandler handler);

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    try
    {
      // Write the message type to stream
      hbaos.writeInt(factory.getClassType(this.getClass()));
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviMarshallingException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new HaviMarshallingException(e.toString());
    }
    catch (NoSuchFieldException e)
    {
      // Translate
      throw new HaviMarshallingException(e.toString());
    }
  }

  /**
   * @see org.havi.system.types.Marshallable#unmarshal(HaviByteArrayInputStream)
   */
  public void unmarshal(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    try
    {
      // Skip the type
      hbais.readInt();
    }
    catch (IOException e)
    {
      throw new HaviUnmarshallingException(e.toString());
    }
  }
}
