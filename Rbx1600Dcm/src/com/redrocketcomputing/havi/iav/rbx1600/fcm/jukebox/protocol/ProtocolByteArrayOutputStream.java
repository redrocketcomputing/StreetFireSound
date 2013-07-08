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
 * $Id: ProtocolByteArrayOutputStream.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol;

import java.io.IOException;

import org.havi.system.types.HaviByteArrayOutputStream;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public class ProtocolByteArrayOutputStream extends HaviByteArrayOutputStream
{
  public ProtocolByteArrayOutputStream()
  {
    super();
  }

  public ProtocolByteArrayOutputStream(int size)
  {
    super(size);
  }

  public void writeBcdByte(int value) throws IOException
  {
    // Check range
    if (value < 0 || value > 99)
    {
      throw new IOException("bad bcd range: " + value);
    }

    // Build bcd byte
    byte bcdValue = (byte)(((value / 10) << 4) + (value % 10));

    // Write the byte
    writeByte(bcdValue);
  }

  public void writeHighDiscId(int value) throws IOException
  {
    // Above 200
    writeByte(value - 200);
  }

  public void writeLowDiscId(int value) throws IOException
  {
    // Check for bcd disc id
    if (value <= 99)
    {
      // Write s bcd disc id
      writeBcdByte(value);
      return;
    }

    // Below 200
    writeByte(value + 54);
  }
}
