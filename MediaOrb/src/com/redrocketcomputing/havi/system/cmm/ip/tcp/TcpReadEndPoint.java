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
 * $Id: TcpReadEndPoint.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviCmmIpConfigurationException;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviCmmIpIoException;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.havi.constants.ConstCmmIpIndications;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.system.cmm.ip.IndicationEvent;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpEngine;
import com.redrocketcomputing.util.concurrent.BoundedBuffer;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Wrapper around a TCP socket designated as a message read socket.  Only one reader a time is allow to use the
 * socket.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class TcpReadEndPoint extends AbstractTask
{
  private final static int MAJIC_XOR = 0x39085729;
	private final static long FLOW_CONTROL_TIMEOUT = 1000;

  private EventDispatch eventDispatcher;
  private Socket socket = null;
  private DataInputStream dataInputStream = null;
  private GUID remoteGuid;
  private BoundedBuffer queue;
  private volatile boolean isOpen;
  private GarpEngine garp;
  private int queueBytes = 0;

  /**
   * Construct a new end point using the specified socket.  Restrict the maximum message size read to maxSize.
   * The construtor send maxSize to the remote end to indicate the max message the end point can receive.
   * @param socket The socket to receive on
   * @param queueSize The max number of message to buffer at this end point
   * @param garp The GARP engine
   * @throws HaviCmmIpException Thrown if there is a setting up the read end point
   */
  public TcpReadEndPoint(Socket socket, int queueSize, GarpEngine garp) throws HaviCmmIpException
  {
    try
    {
      // Check the parameters
      if (socket == null || queueSize <= 0)
      {
        throw new IllegalArgumentException();
      }

      // Save the parameters
      this.socket = socket;
      this.garp = garp;

      // Get the event dispatcher
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Bad configuration
        throw new HaviCmmIpConfigurationException("could not find EventDispatch service");
      }

      // Create data input stream
      dataInputStream = new DataInputStream(socket.getInputStream());

      // Read the remote guid
      byte[] rawRemoteGuid = new byte[GUID.SIZE];
      dataInputStream.readFully(rawRemoteGuid);

      // Create the remote GUID
      remoteGuid = new GUID(rawRemoteGuid);

      // Allocate the queue
      queue = new BoundedBuffer(queueSize);

      // Mark as open
      isOpen = true;
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
   * Receive a message from the connected remote end point.  First a message size integer is read.
   * A new buffer is allocated.  Last the entire message is read and returned.
   * @param length The maximum amount of data to read
   * @return byte[] The new message buffer, or null if there is no available message
   * @throws IOException If there is a problem reading from the socket or the message length exceeds maxSize
   */
  public byte[] receive() throws HaviCmmIpException
  {
    // Make sure the socket is opened
    if (!isOpen)
    {
      // Opps
      throw new HaviCmmIpIoException(toString() + " is closed");
    }

    try
    {
      // Try to get a message from the queue
      byte[] message = (byte[])queue.poll(0);
      if (message != null)
      {
        // Decrease queue bytes
        queueBytes -= message.length;
      }

      // Return the message
      return message;
    }
    catch (InterruptedException e)
    {
      // Clear the interrupted state
      Thread.currentThread().interrupted();

      // Translate
      throw new HaviCmmIpIoException(toString() + " - " + e.toString());
    }
  }

  /**
   * Receive a message from the connected remote end point.  First a message size integer is read.
   * The message size is checked against the available buffer size.  Lastly the entire message is read.
   * @param buffer The buffer to use in reading the message
   * @param offset The starting offset into the buffer
   * @param length The amount of available space in the buffer.
   * @throws IOException Thrown if there is a problem reading from the end point socket of the message length
   * exceeds the amount of buffer space available.
   */
  public int receive(byte[] buffer, int offset, int length) throws HaviCmmIpException
  {
    // Make sure the socket is opened
    if (!isOpen)
    {
      // Opps
      throw new HaviCmmIpIoException(toString() + " is closed");
    }

    try
    {
      // Try to get a message from the queue
      byte[] message = (byte[])queue.poll(0);
      if (message != null)
      {
        // Copy into the buffer
        System.arraycopy(message, 0, buffer, offset, message.length);

        // Decrease queue bytes
        queueBytes -= message.length;

        // Return the amount of data read
        return message.length;
      }
      else
      {
        // No message available
        return -1;
      }
    }
    catch (InterruptedException e)
    {
      // Clear the interrupted state
      Thread.currentThread().interrupted();

      // Translate
      throw new HaviCmmIpIoException(toString() + " - " + e.toString());
    }
  }

  /**
   * Close the end point and release all resources.
   */
  public synchronized void close()
  {
    try
    {
      // Clear all internal variables and close the socket
      this.isOpen = false;
      this.dataInputStream.close();
      this.socket.close();

      // Fire a close event
      eventDispatcher.dispatch(new IndicationEvent(remoteGuid, ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS, ConstCmmIpIndications.READ_CLOSE, null));
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
    return "TcpReadEndPoint(" + queueBytes + "): " + remoteGuid + '<' + socket.getInetAddress().getHostAddress() + '@' + socket.getPort() + '>';
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return toString();
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      while (isOpen)
      {
        // Loop trying to read message
        int size;
        int check;
        do
        {
          // Read majic check and message size
          check = dataInputStream.readInt();
          size = dataInputStream.readInt();
        }
        while (size != (check ^ MAJIC_XOR));

        // Check for ping, we drop the message and try again
        if (size != -1)
        {
          // Allocate a buffer
          byte[] message = new byte[size];

          // Read the buffer
          dataInputStream.readFully(message);

          // Add to the queue
          if (queue.offer(message, FLOW_CONTROL_TIMEOUT))
          {
            // Increase queue bytes
            queueBytes += message.length;

            // Fire indication FIX THIS!!!!!!!!!!!!!! Create multiple thread unneeded
          	eventDispatcher.dispatch(new IndicationEvent(remoteGuid, ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS, ConstCmmIpIndications.READ_AVAILABLE, new Integer(size)));
          }
          else
          {
            // Log dropped message
            LoggerSingleton.logError(this.getClass(), "run", "dropping message from " + remoteGuid);
          }
        }
      }
    }
    catch (InterruptedException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "run", toString() + " - " + e.toString());
    }
    catch (IOException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "run", toString() + " - " + e.toString());

      // Check to see if we are still open
      if (isOpen)
      {
        // Close the socket
        close();

        // Force a network reset
        garp.forceNetworkReset();
      }
    }
  }
}
