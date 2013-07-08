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
 * $Id: UdpLogger.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;

/**
 * @author stephen
 *
 */
public class UdpLogger extends AbstractLogFilter
{
  private final static int DEFAULT_MESSAGE_SIZE = 1400;
  private final static int DEFAULT_PORT = 47000;
  private final static String DEFAULT_TAG = "00:50:c2:ff:ff:ff";

  private int messageSize;
  private DatagramSocket socket = null;
  private ByteArrayOutputStream bos = null;
  private DataOutputStream dos = null;
  private String tag = null;
  private String remoteAddress = null;
  private boolean enabled = false;
  private InetAddress remoteInetAddress = null;
  private int remoteInetPort = -1;
  private ComponentConfiguration configuration;

  /**
   * Constructor for UdpLogger.
   * @param instanceName The configuration name for this filter
   * @param nextFilter The next filter in the chain
   */
  public UdpLogger(String instanceName, LogFilter nextFilter)
  {
    // Construct super class
    super(instanceName, nextFilter);

    // Create component configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.AbstractLogFilter#start()
   */
  public synchronized void start()
  {
    try
    {
      // Get configuration information
      enabled = configuration.getBooleanProperty("enabled", false);
      tag = System.getProperty("mac.address", DEFAULT_TAG);
      messageSize = configuration.getIntProperty("message.size", DEFAULT_MESSAGE_SIZE);
      remoteAddress = configuration.getProperty("remote.address");
      if (remoteAddress == null)
      {
        // All done
        return;
      }

      // Create the data output stream for the messages
      bos = new ByteArrayOutputStream(messageSize);
      dos = new DataOutputStream(bos);

      // Try to extract the parts
      String inetAddressString = remoteAddress.trim();
      remoteInetPort = DEFAULT_PORT;
      int portIndex = inetAddressString.indexOf('@');
      if (portIndex != -1)
      {
        // Extract the address string
        inetAddressString = inetAddressString.substring(0, portIndex);

        // Extract the port and convert the port to a integer, we are purposely letting the NumberFormatException escape
        remoteInetPort = Integer.parseInt(remoteAddress.trim().substring(portIndex + 1));
      }

      // Build InetAddress
      remoteInetAddress = InetAddress.getByName(inetAddressString);

      // Create the socket
      socket = new DatagramSocket();
    }
    catch (UnknownHostException e)
    {
      System.out.println(e.toString());
    }
    catch (SocketException e)
    {
      System.out.println(e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.AbstractLogFilter#terminate()
   */
  public synchronized void terminate()
  {
    try
    {
      // Mark as disabled
      enabled = false;

      // Close the socket
      socket.close();

      // Close the output streams
      dos.close();
      bos.close();
    }
    catch (IOException e)
    {
      // Ignore
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.Logger#log(String)
   */
  public synchronized void log(String message)
  {
    try
    {
      // Check for enable
      if (isEnabled())
      {
        // Flush the output stream
        bos.reset();

        // Write message the the outputstream
        dos.writeUTF(tag);
        dos.writeUTF(message.trim());
        dos.flush();
        int length = bos.size() < messageSize ? bos.size() : messageSize;

        // Build outbound packet
        DatagramPacket packet = new DatagramPacket(bos.toByteArray(), length, remoteInetAddress, remoteInetPort);

        // Send the packet
        socket.send(packet);
      }
    }
    catch (IOException e)
    {
      // Error down stream
      forward("ERROR  - " + e.toString());
    }

    // Forward the message
    forward(message);
  }

  /**
   * Returns the enabled.
   * @return boolean
   */
  public boolean isEnabled()
  {
    return enabled;
  }

  /**
   * Returns the remoteAddress.
   * @return String
   */
  public String getRemoteAddress()
  {
    return remoteAddress;
  }

  /**
   * Sets the enabled.
   * @param enabled The enabled to set
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }
  /**
   * Returns the tag.
   * @return String
   */
  public String getTag()
  {
    return tag;
  }
}
