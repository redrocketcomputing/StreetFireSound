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
 * $Id: GarpMessageSocket.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;

/**
 * Multicast socket for sending and receiving GARP messages
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
class GarpMessageSocket
{
  private InetAddress inetAddress;
  private int port;
  private MulticastSocket socket;
  private DatagramPacket receivePacket;
  private DatagramPacket sendPacket;
  private HaviByteArrayInputStream hbais;

  /**
   * Constructor for GarpMessageSocket.
   */
  public GarpMessageSocket(String address, int port, int packetSize) throws GarpException
  {
    try
    {
      // Save the port number
      this.port = port;

      // Build the multicast address
      inetAddress = InetAddress.getByName(address);

      // Create the socket
      socket = new MulticastSocket(port);

      // Join the multicast group
      socket.joinGroup(inetAddress);

      // Allocate the datagram packets
      receivePacket = new DatagramPacket(new byte[packetSize], 0, packetSize);
      sendPacket = new DatagramPacket(new byte[0], 0, inetAddress, port);

      // Allocate input byte array stream
      hbais = new HaviByteArrayInputStream();
    }
    catch (UnknownHostException e)
    {
      // Translate
      throw new GarpHostException(e.getMessage());
    }
    catch (IOException e)
    {
      // Translate
      throw new GarpIOException(e.getMessage());
    }
  }

  /**
   * Wait for a GARP message to arrive or generate a timeout message
   * @param timeout The amount of time to wait for a GARP message
   * @return GarpMessage The message received. This could be a timeout message
   * @throws GarpException Thrown when the socket is closed or some other error is detected
   */
  public GarpMessage receive(int timeout) throws GarpException
  {
    // ensure the socket is open
    if (socket == null)
    {
      throw new GarpIOException("socket closed");
    }

    // Lock the receiver
    synchronized (receivePacket)
    {
      try
      {
        // Set the timeout
        socket.setSoTimeout(timeout);

        // Reset the packet
        receivePacket.setLength(receivePacket.getData().length);

        // Wait for packet
        socket.receive(receivePacket);

        // Bind packet to the input stream
        hbais.fromByteArray(receivePacket.getData(), 0, receivePacket.getLength());

        // User factory to create GARP message
        return GarpMessage.create(hbais);
      }
      catch (InterruptedIOException e)
      {
        // Return the timeout message
        return new GarpTimeoutMessage();
      }
      catch (SocketException e)
      {
        // Translate
        throw new GarpIOException(e.toString());
      }
      catch (IOException e)
      {
        // Translate
        throw new GarpIOException(e.getMessage());
      }
      catch (HaviUnmarshallingException e)
      {
        // Translate
        throw new GarpIOException(e.toString());
      }
    }
  }

  /**
   * Wait for a GARP message to arrive or generate a timeout message
   * @return GarpMessage The message received.
   * @throws GarpException Thrown when the socket is closed or some other error is detected
   */
  public GarpMessage receive() throws GarpException
  {
    // Forward with no timeout
    return receive(0);
  }

  /**
   * Send the specified message
   * @param message The message to send
   * @throws GarpException Thrown is the socket is close or some other error is detected
   */
  public void send(GarpMessage message) throws GarpException
  {
    // ensure the socket is open
    if (socket == null)
    {
      throw new GarpIOException("socket closed");
    }

    // Lock the sender
    synchronized (sendPacket)
    {
      try
      {
        // Create the byte array
        HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();

        // Marshal into byte array
        message.marshal(hbaos);

        // Bind to the packet
        sendPacket.setAddress(inetAddress);
        sendPacket.setPort(port);
        sendPacket.setData(hbaos.getBuffer(), 0, hbaos.size());

        // Send the packet
        socket.send(sendPacket);
      }
      catch (HaviMarshallingException e)
      {
        // Translate
        throw new GarpIOException(e.toString());
      }
      catch (IOException e)
      {
        // Translate
        throw new GarpIOException(e.toString());
      }
    }
  }

  /**
   * Close the socket and release any resources
   */
  public void close()
  {
    // Check for already close
    if (socket != null)
    {
      try
      {
        // Leave the group
        socket.leaveGroup(inetAddress);
      }
      catch (IOException e)
      {
        // Ignore
      }

      // Close the socket
      socket.close();

      // Release the socket
      socket = null;
    }
  }

}
