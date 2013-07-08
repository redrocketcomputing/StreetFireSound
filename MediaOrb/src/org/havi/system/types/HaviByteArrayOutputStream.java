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
 * $Id: HaviByteArrayOutputStream.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package org.havi.system.types;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * <p>Title: HaviByteArrayOutputStream</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

/** @todo : verify format matches CDR spec */
public class HaviByteArrayOutputStream extends ByteArrayOutputStream
{
  protected DataOutputStream dos = null;

  /**
   *
   */
  public HaviByteArrayOutputStream()
  {
    super();
    dos = new DataOutputStream(this);
  }

  /**
   *
   */
  public HaviByteArrayOutputStream(int size)
  {
    super(size);
    dos = new DataOutputStream(this);
  }

  public byte[] getBuffer()
  {
    return buf;
  }

  /**
   *
   * @param v
   * @throws IOException
   */
  public void writeBoolean(boolean v) throws IOException
  {
    dos.writeBoolean(v);
    dos.flush();
  }


  /**
   *
   * @param v
   * @throws IOException
   */
  public void writeByte(int v) throws IOException
  {
    dos.writeByte(v);
    dos.flush();
  }

  /**
   *
   * @param v
   * @throws IOException
   */
  public void writeChar(int v) throws IOException
  {
    dos.writeChar(v);
    dos.flush();
  }

  /**
   *
   * @param s
   * @throws IOException
   */
  public void writeChars(String s) throws IOException
  {
    dos.writeChars(s);
    dos.flush();
  }

  /**
   *
   * @param v
   * @throws IOException
   */
  public void writeInt(int v) throws IOException
  {
    dos.writeInt(v);
    dos.flush();
  }

  /**
   *
   * @param v
   * @throws IOException
   */
  public void writeLong(long v) throws IOException
  {
    dos.writeLong(v);
    dos.flush();
  }

  /**
   *
   * @param v
   * @throws IOException
   */
  public void writeShort(int v) throws IOException
  {
    dos.writeShort(v);
    dos.flush();
  }

  /**
   *
   * @param str
   * @throws IOException
   */
  public void writeUTF(String str) throws IOException
  {
    dos.writeUTF(str);
    dos.flush();
  }


  /* george add 2 function write float and double start */

  public void writeFloat(float v) throws IOException
  {
    dos.writeFloat(v);
    dos.flush();
  }

  public void writeDouble(double v) throws IOException
  {
    dos.writeDouble(v);
    dos.flush();
  }



  /* george add 4 function read/write float and double end */
}
