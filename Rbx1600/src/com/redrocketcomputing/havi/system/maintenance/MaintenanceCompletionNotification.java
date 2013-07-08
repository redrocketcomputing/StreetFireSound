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
 * $Id: MaintenanceCompletionNotification.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *  Maintenance completion notification message
 *
 *  Sent by the device after the file has been received and
 *  verified as being the correct type and version
 *
 *  Currently extremely simple, just a single byte response
 *  code, but may later be expanded to include additional
 *  information if necessary
 */
public class MaintenanceCompletionNotification
{
  private byte responseCode;

  /**
   * create a handshake request of the current version from a response code
   */
  public MaintenanceCompletionNotification(byte responseCode)
  {
    this.responseCode = responseCode;
  }

  /**
   * create a handshake request of the current version from a DataInputStream
   * @param dis The DataInputStream to read from
   */
  public MaintenanceCompletionNotification(DataInputStream dis) throws IOException
  {
    // Read the response code from the input stream
    responseCode = dis.readByte();

  }

  /**
   * Write the response to DataOutputStream
   * @param dos The DataOutputStream to write to
   * @throws IOException Thrown if an error is detected on the output stream
   */
  public void write(DataOutputStream dos) throws IOException
  {
    // Write the request to the output stream
    dos.writeByte(responseCode);
    dos.flush();
  }

  public byte getResponseCode()
  {
    return responseCode;
  }
}
