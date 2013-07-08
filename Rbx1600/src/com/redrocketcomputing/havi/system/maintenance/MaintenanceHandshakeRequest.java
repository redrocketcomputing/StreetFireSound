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
 * $Id: MaintenanceHandshakeRequest.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *  Handshake message, sent when a client initiates a connection for maintenance
 */
public class MaintenanceHandshakeRequest
{
  private byte majorVersion = MaintenanceConstants.VERSION_MAJOR;
  private byte minorVersion = MaintenanceConstants.VERSION_MINOR;
  private byte featureCode;
  private byte reserved = 0;

  /**
   * create a handshake requestof the current version from a feature code
   */
  public MaintenanceHandshakeRequest(byte featureCode)
  {
    this.featureCode = featureCode;
  }

  /**
   * create a handshake request of the current version from a DataInputStream
   * @param dis The DataInputStream to read from
   */
  public MaintenanceHandshakeRequest(DataInputStream dis) throws IOException
  {
    // Read the bytes from the input stream
    majorVersion = dis.readByte();
    minorVersion = dis.readByte();

    // Check version information
    if (majorVersion != MaintenanceConstants.VERSION_MINOR || minorVersion != MaintenanceConstants.VERSION_MINOR)
    {
      // Umm, bad version
      throw new IOException("bad version number");
    }

    featureCode = dis.readByte();
    reserved = dis.readByte();
  }

  /**
   * create a handshake request from a binary message
   */
  public MaintenanceHandshakeRequest(byte[] message)
  {
    // check that it's the right length
    if (message.length != 4)
    {
      throw new IllegalArgumentException();
    }

    // unmarshall
    majorVersion = message[0];
    minorVersion = message[1];
    featureCode  = message[2];
  }

  /**
   * Write the request to DataOutputStream
   * @param dos The DataOutputStream to write to
   * @throws IOException Thrown if an error is detected on the output stream
   */
  public void write(DataOutputStream dos) throws IOException
  {
    // Write the request to the output stream
    dos.writeByte(majorVersion);
    dos.writeByte(minorVersion);
    dos.writeByte(featureCode);
    dos.writeByte(reserved);
    dos.flush();
  }

  /**
   * marshall the request to a byte array
   */
  public byte[] toByteArray()
  {
    // stuff the array
    byte[] array = new byte[4];
    array[0] = majorVersion;
    array[1] = minorVersion;
    array[2] = featureCode;
    array[3] = 0; // reserved

    // done
    return array;
  }

  public byte getFeatureCode()
  {
    return featureCode;
  }

  public byte getMajorVersion()
  {
    return majorVersion;
  }

  public byte getMinorVersion()
  {
    return minorVersion;
  }
}
