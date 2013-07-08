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
 * $Id: DeviceDriver.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.hardware;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author stephen
 *
 */
public class DeviceDriver
{
  private RandomAccessFile device = null;

  protected DeviceDriver(String path) throws IOException
  {
    // Open input stream
    device = new RandomAccessFile(path, "rw");
  }

  public void close()
  {
    try
    {
      // Close output stream and release memory
      device.close();
      device = null;
    }
    catch (IOException e)
    {
      // Ignore
    }
  }

  protected final int read() throws IOException
  {
    // Check file state
    checkState();

    // Forward
    return device.read();
  }

  protected final int read(byte[] buffer, int offset, int length) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    return device.read(buffer, offset, length);
  }

  protected final int read(byte[] buffer) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    return device.read(buffer);
  }

  protected final void readFully(byte[] buffer) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    device.readFully(buffer);
  }

  protected final void readFully(byte[] buffer, int offset, int length) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    device.readFully(buffer, offset, length);
  }

  protected final byte readByte() throws IOException
  {
    // Check file state
    checkState();

    // Forward
    return device.readByte();
  }

  protected final int readInt() throws IOException
  {
    // Check file state
    checkState();

    // Extract byte
    int value = device.readInt();
    int b0 = (value >>> 24) & 0xff;
    int b1 = (value >>> 16) & 0xff;
    int b2 = (value >>> 8) & 0xff;
    int b3 = value & 0xff;

    // Reorder and return
    return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
  }

  protected final void write(int value) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    device.write(value);
  }

  protected final void write(byte[] buffer) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    device.write(buffer);
  }

  protected final void write(byte[] buffer, int offset, int length) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    device.write(buffer, offset, length);
  }

  protected final void writeByte(int value) throws IOException
  {
    // Check file state
    checkState();

    // Forward
    device.writeByte(value);
  }

  protected final void writeInt(int value) throws IOException
  {
    // Check file state
    checkState();

    // Extract byte
    int b0 = (value >>> 24) & 0xff;
    int b1 = (value >>> 16) & 0xff;
    int b2 = (value >>> 8) & 0xff;
    int b3 = value & 0xff;

    // Reorder
    value = (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;

    // Forward
    device.writeInt(value);
  }

  private final void checkState() throws IOException
  {
    if (device == null)
    {
      throw new IOException("bad device file state");
    }
  }
}
