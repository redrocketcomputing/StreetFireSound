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
 * $Id: MaintenanceHandshakeException.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

/**
 *  Handshake Exception, indicates error during handshake
 */
public class MaintenanceHandshakeException extends MaintenanceException
{
  byte responseCode;

  /**
   * Create an exception for a given response code
   * @param respondeCode The handshake response code causing the exception
   */
  public MaintenanceHandshakeException(byte responseCode)
  {
    // Construct super class
    super();

    // Save the response code
    this.responseCode = responseCode;
  }

  /**
   * Create an exception for a given response code
   * @param s The exception message
   * @param respondeCode The handshake response code causing the exception
   */
  public MaintenanceHandshakeException(String s, byte responseCode)
  {
    // Construct super class
    super(s);

    // Save the response code
    this.responseCode = responseCode;
  }

  /**
   *  Get the response code sent/received in the handshake response
   */
  public byte getResponseCode()
  {
    return responseCode;
  }
}
