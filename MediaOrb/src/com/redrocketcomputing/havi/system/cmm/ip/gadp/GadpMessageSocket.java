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
 * $Id: GadpMessageSocket.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

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
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class GadpMessageSocket
{
  private MulticastSocket socket;
  private int packetSize;
  private InetAddress inetAddress;
  private GadpTimeoutMessage timeoutMessage = new GadpTimeoutMessage();

  /**
   * Construct a GadpMessageSocket multicast socket on the specified port. Join the specified multicast
   * group.
   * @param address The multicast group
   * @param port The port number for the socket
   * @param packetSize The max packet size to send with this socket
   * @throws GadpException Thrown if there is a problem opening the socket or joining the multicast group
   */
  public GadpMessageSocket(String address, int port, int packetSize) throws GadpException
  {
    try
    {
      // Save the address and packet size
      this.packetSize = packetSize;

      // Create the socket
      socket = new MulticastSocket(port);

      // Create address
      inetAddress = InetAddress.getByName(address);
      
      // Join the multicast
      socket.joinGroup(inetAddress);
    }
    catch (UnknownHostException e)
    {
      // Translate
      throw new GadpHostException(e.getMessage());
    }
    catch (IOException e)
    {
      // Translate
      throw new GadpIOException(e.getMessage());
    }
  }

  /**
   * Wait for a specified amount of time for a GADP message to arrive at the socket.
   * @param timeout The time in milliseconds to wait for a GADP message
   * @return GadpMessage The GADP message received or a GadpTimeoutMessage object if a timeout was detected
   * @throws GadpException Thrown is there is a problem receiving on the socket
   */
  public GadpMessage receive(int timeout) throws GadpException
  {
    // Check the socket
    if (socket == null)
    {
      throw new GadpIOException("socket is closed");
    }

    try
    {
      // Allocate a new datagram
      DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);

      // Set the time
      socket.setSoTimeout(timeout);

      // Wait for a a packet
      socket.receive(packet);

      // Build havi byte array input stream
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(packet.getData(),packet.getOffset(), packet.getLength());

      // Unmarshall the message
      return GadpMessage.create(hbais);
    }
    catch (InterruptedIOException e)
    {
      // Timeout detected
      return timeoutMessage;
    }
    catch (SocketException e)
    {
      // Translate
      throw new GadpIOException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new GadpIOException(e.toString());
    }
    catch (HaviUnmarshallingException e)
    {
      // Translate
      throw new GadpIOException(e.toString());
    }
  }

  /**
   * Send the specified message over the multicast socket
   * @param message The GADP message to send
   * @throws GadpException Thrown if there is a problem sending the message
   */
  public void send(GadpMessage message) throws GadpException
  {
    // Check the socket
    if (socket == null)
    {
      throw new GadpIOException("socket is closed");
    }

    try
    {
      // Allocate a output stream with the maximum packet size
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(packetSize);

      // Marshal the message
      message.marshal(hbaos);

      // Allocate the packet
      DatagramPacket packet = new DatagramPacket(hbaos.getBuffer(), hbaos.size(), inetAddress, socket.getLocalPort());

      // Send the packet
      socket.send(packet);
    }
    catch (HaviMarshallingException e)
    {
      // Translate
      throw new GadpIOException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new GadpIOException(e.toString());
    }
  }

  /**
   * Close the socket and release the resources
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
