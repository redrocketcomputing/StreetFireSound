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
 * $Id: ServiceHandler.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip;

import java.io.PrintStream;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviCmmIpException;

/**
 * Base interface for all CMM IP services.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public interface ServiceHandler
{
  /**
   * Return the service handle ID for this service
   * @return int The service handler ID
   */
  public int getServiceId();

  /**
   * Close the services handler and terminate all connections.  The service handler can not be restarted
   */
  public void close();

  /**
   * Send a message to the specified GUID.  The entire buffer is send to the remote GUID.
   * @param guid The target device
   * @param buffer The message buffer to send
   * @throws HaviCmmIpException Thrown if a error is detected sending the message.
   */
  public void send(GUID guid, byte[] buffer) throws HaviCmmIpException;

  /**
   * Send a message to the specified GUID. The message data sent is extracted from the buffer starting at
   * the specified offset using the provided length
   * @param guid The target device
   * @param buffer The buffer containing the message data
   * @param offset The start of the message data
   * @param length The lenght of the message data
   * @throws HaviCmmIpException Thrown if an error detected send the message.
   */
  public void send(GUID guid, byte[] buffer, int offset, int length) throws HaviCmmIpException;

  /**
   * Wait for a message from the specified device.  A new byte array is allocated to hold the message data
   * @param guid The target device
   * @return byte[] The new message buffer
   * @throws HaviCmmIpException Thrown is a error is detected while receiving the message
   */
  public byte[] receive(GUID  guid) throws HaviCmmIpException;

  /**
   * Dump statical and configuration information about the service to the specified printstring
   * @param printStream The print stream to use
   * @param arguments Additional configuration arguments
   */
  public void dump(PrintStream printStream, String[] arguments);
}
