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
 * $Id: TcpWriteEndPoint.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.tcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviCmmIpConfigurationException;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviCmmIpIoException;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstCmmIpIndications;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.system.cmm.ip.IndicationEvent;


/**
 * Wrapper around a TCP socket designated as a message write socket.  Only one writer at a time is
 * allowed to use the socket.  The write protocol requires that a integer length field precend all messages.
 * The end point will close the socket if an message is received which is larger than the configured
 * maximum message size.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class TcpWriteEndPoint
{
  private final static int MAJIC_XOR = 0x39085729;

  private EventDispatch eventDispatcher;
  private GUID remoteGuid;
  private Socket socket = null;
  private DataOutputStream dataOutputStream = null;
  private int sentBytes = 0;

  /**
   * Construct a new end point using the specified socket.  Read the max message size from the remote end
   * point.
   * @param socket The socket to write on
   * @throws HaviCmmIpException Thrown is there is a problem read maxSize or if the maxSize is less than zero
   */
  public TcpWriteEndPoint(GUID localGuid, GUID remoteGuid, Socket socket) throws HaviCmmIpException
  {
    try
    {
      // Check the parameters
      if (socket == null)
      {
        throw new IllegalArgumentException();
      }

      // Save the parameters
      this.socket = socket;
      this.remoteGuid = remoteGuid;

      // Get the event dispatcher
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Bad configuration
        throw new HaviCmmIpConfigurationException("could not find EventDispatch service");
      }

      // Create data output stream
      dataOutputStream = new DataOutputStream(socket.getOutputStream());

      // Send the local guid
      dataOutputStream.write(localGuid.getValue());
      dataOutputStream.flush();
    }
    catch (IOException e)
    {
      // Close the end point
      close();

      // Rethrow
      throw new HaviCmmIpIoException(e.getMessage());
    }

  }

  /**
   * Send the entire buffer to the remote end point by first sending the message size and then the specified
   * data using the offset and length information
   * @param buffer The buffer containing the message to send
   * @param offset The starting position inside the buffer
   * @param length The amount of data to send
   * @throws HaviCmmIpException Thrown if the socket is closed or detects and error. Also thrown if the message
   * size is to large for the remote end point.
   **/
  public void send(byte[] buffer, int offset, int length) throws HaviCmmIpException
  {
    // Check the arguments
    if (buffer == null || offset < 0 || offset >= buffer.length || offset + length > buffer.length)
    {
      throw new IllegalArgumentException();
    }

    try
    {
      // Send the message length and it majic xor
      dataOutputStream.writeInt(length ^ MAJIC_XOR);
      dataOutputStream.writeInt(length);

      // Send the message
      dataOutputStream.write(buffer, offset, length);

      // Flush the buffer
      dataOutputStream.flush();

      // Update sent bytes
      sentBytes++;
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviCmmIpIoException(e.getMessage());
    }
  }

  /**
   * Close the end point and release all resources.
   */
  public void close()
  {
    // Ignore if the socket is already closed
    if (socket == null)
    {
      return;
    }

    try
    {
      // Clear all internal variables and close the socket
      this.dataOutputStream.close();
      this.socket.close();

      // Fire a close event
      eventDispatcher.dispatch(new IndicationEvent(remoteGuid, ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS, ConstCmmIpIndications.WRITE_CLOSE, null));
    }
    catch (IOException e)
    {
      // Ignore
    }
  }

  /**
   * Returns the remoteGuid.
   * @return GUID
   */
  public GUID getRemoteGuid()
  {
    return remoteGuid;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "TcpWriteEndPoint(" + sentBytes + "): " + remoteGuid + '<' + socket.getInetAddress().getHostAddress() + '@' + socket.getPort() +'>';
  }
}
