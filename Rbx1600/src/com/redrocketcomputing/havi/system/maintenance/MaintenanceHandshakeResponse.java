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
 * $Id: MaintenanceHandshakeResponse.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *  Handshake message response
 */
public class MaintenanceHandshakeResponse
{
  private byte majorVersion = MaintenanceConstants.VERSION_MAJOR;
  private byte minorVersion = MaintenanceConstants.VERSION_MINOR;
  private byte responseCode;
  private byte reserved;

  /**
   * create a handshake request of the current version from a response code
   */
  public MaintenanceHandshakeResponse(byte responseCode)
  {
    this.responseCode = responseCode;
  }

  /**
   * create a handshake request of the current version from a DataInputStream
   * @param dis The DataInputStream to read from
   */
  public MaintenanceHandshakeResponse(DataInputStream dis) throws IOException, MaintenanceHandshakeException
  {
    // Read the bytes from the input stream
    majorVersion = dis.readByte();
    minorVersion = dis.readByte();

    // Check version information
    if (majorVersion != MaintenanceConstants.VERSION_MINOR || minorVersion != MaintenanceConstants.VERSION_MINOR)
    {
      // Umm, bad version
      throw new MaintenanceHandshakeException("bad version number", (byte)-1);
    }

    // Read remaining information
    responseCode = dis.readByte();
    reserved = dis.readByte();
  }
  /**
   * create a handshake response from a binary message
   */
  public MaintenanceHandshakeResponse(byte[] message)
  {
    // check that it's the right length
    if (message.length != 4)
    {
      throw new IllegalArgumentException();
    }

    // unmarshall
    majorVersion = message[0];
    minorVersion = message[1];
    responseCode = message[2];
  }

  /**
   * Write the response to DataOutputStream
   * @param dos The DataOutputStream to write to
   * @throws IOException Thrown if an error is detected on the output stream
   */
  public void write(DataOutputStream dos) throws IOException
  {
    // Write the request to the output stream
    dos.writeByte(majorVersion);
    dos.writeByte(minorVersion);
    dos.writeByte(responseCode);
    dos.writeByte(reserved);
    dos.flush();
  }

  /**
   * marshall the response to a byte array
   */
  public byte[] toByteArray()
  {
    // stuff the array
    byte[] array = new byte[4];
    array[0] = majorVersion;
    array[1] = minorVersion;
    array[2] = responseCode;
    array[3] = 0; // reserved

    // done
    return array;
  }

  public byte getResponseCode()
  {
    return responseCode;
  }
}
