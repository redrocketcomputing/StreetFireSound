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
 * $Id: HaviByteArrayInputStream.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package org.havi.system.types;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * <p>Title: HaviByteArrayInputStream </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

/** @todo : verify format matches CDR spec */
public class HaviByteArrayInputStream extends ByteArrayInputStream
{
  private final static byte[] EMPTY = new byte[0];

  private DataInputStream dis = null;

  public HaviByteArrayInputStream()
  {
    super(EMPTY);
    dis = new DataInputStream(this);
  }

  /**
   *
   * @param buf
   */
  public HaviByteArrayInputStream(byte[] buf)
  {
    super(buf);
    dis = new DataInputStream(this);
  }


  /**
   *
   * @param buf
   * @param offset
   * @param length
   */
  public HaviByteArrayInputStream(byte[] buf, int offset, int length)
  {
    super(buf, offset, length);
    dis = new DataInputStream(this);
  }

  /**
   *
   * @param in
   */
  public void fromByteArray(byte[] in)
  {
    buf = in;
    count = in.length;
    pos = 0;
    mark = 0;
  }

  /**
   *
   * @param in
   * @param offset
   * @param length
   */
  public void fromByteArray(byte[] in, int offset, int length)
  {
    buf = in;
    count = Math.min(offset + length, buf.length);
    pos = offset;
    mark = offset;
  }



  /**
   *
   * @return
   * @throws IOException
   */
  public boolean readBoolean() throws IOException
  {
    return dis.readBoolean();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public byte readByte() throws IOException
  {
    return dis.readByte();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public char readChar() throws IOException
  {
    return dis.readChar();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public int readInt() throws IOException
  {
    return dis.readInt();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public long readLong() throws IOException
  {
    return dis.readLong();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public short readShort() throws IOException
  {
    return dis.readShort();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public int readUnsignedByte() throws IOException
  {
    return dis.readUnsignedByte();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public int readUnsignedShort() throws IOException
  {
    return dis.readUnsignedShort();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public String readUTF() throws IOException
  {
    return dis.readUTF();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  public String readHaviString() throws IOException
  {
    return readUTF();
  }


  /* george add 2 function write float and double start */

  public float readFloat() throws IOException
  {
    return dis.readFloat();

  }

  public double readDouble() throws IOException
  {
    return dis.readDouble();

  }



  /* george add 4 function read/write float and double end */


}
