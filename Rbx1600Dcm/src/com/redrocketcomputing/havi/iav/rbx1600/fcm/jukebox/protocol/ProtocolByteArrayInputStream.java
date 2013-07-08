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
 * $Id: ProtocolByteArrayInputStream.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol;

import java.io.IOException;

import org.havi.system.types.HaviByteArrayInputStream;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public class ProtocolByteArrayInputStream extends HaviByteArrayInputStream
{

  public ProtocolByteArrayInputStream()
  {
    super();
  }

  public ProtocolByteArrayInputStream(byte[] buf)
  {
    super(buf);
  }

  public ProtocolByteArrayInputStream(byte[] buf, int offset, int length)
  {
    super(buf, offset, length);
  }

  public int readBcdByte() throws IOException
  {
    int value = readUnsignedByte();
    int highNibble = (value >> 4) & 0x0f;
    int lowNibble = value & 0x0f;
    return highNibble * 10 + lowNibble;
  }

  public int readHighDiskId() throws IOException
  {
    return readUnsignedByte() + 200;
  }

  public int readLowDiskId() throws IOException
  {
    int value = readUnsignedByte();

    // Check for above 100
    if (value > 153)
    {
      return value - 54;
    }

    // Below 100
    int highNibble = (value >> 4) & 0x0f;
    int lowNibble = value & 0x0f;
    return highNibble * 10 + lowNibble;
  }
}
