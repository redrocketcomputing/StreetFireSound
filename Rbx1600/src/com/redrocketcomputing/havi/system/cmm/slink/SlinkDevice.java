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
 * $Id: SlinkDevice.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.slink;

import java.io.IOException;
import java.io.InterruptedIOException;

import com.redrocketcomputing.hardware.SlinkChannelController;
import com.redrocketcomputing.hardware.SlinkChannelEventListener;
import com.redrocketcomputing.util.concurrent.BoundedBuffer;
import com.redrocketcomputing.util.concurrent.InterruptableChannel;

/**
 * @author stephen
 *
 */
public abstract class SlinkDevice implements SlinkChannelEventListener
{
  protected int deviceId = -1;
  protected long timeout = -1;
  protected String description = "";
  private SlinkChannelController channel;
  private volatile InterruptableChannel queue;

  /**
   * Construct a SlinkDevice base class and attach it to the channel
   * @param channel The SlinkChannelController to bind the device to
   */
  public SlinkDevice(SlinkChannelController channel, int queueSize)
  {
    // Check the channel
    if (channel == null)
    {
      throw new IllegalArgumentException("channel is null");
    }

    // Create the queue
    queue = new InterruptableChannel(new BoundedBuffer(queueSize));

    // Save the channel
    this.channel = channel;

    // Bind to the channel
    channel.addListener(this);
  }

  /**
   * Return the device ID for this device
   * @return int The device ID
   */
  public final int getDeviceId()
  {
    return deviceId;
  }

  /**
   * Return a description of this SLINK device
   * @return String The description
   */
  public final String getDescription()
  {
    return description;
  }

  /**
   * Cleanup the device, subclasses should overide this method to clean up and resources and ALWAYS
   * invoke the super class close method.
   */
  public void close()
  {
    // Interrupt any waiters
    InterruptableChannel temp = queue;
    queue = null;
    temp.interrupt();

    // Unbind from the channel
    channel.removeListener(this);
  }

  /**
   * Set the timeout for the device. A -1 disables the timeout.
   * @param timeout Time to wait for a message.
   */
  public void setTimeout(long timeout)
  {
    this.timeout = timeout;
  }

  /**
   * Read a SlinkMessage from the device.  This method will block until message is available.
   * @param data The buffer to place the data into
   * @param offset The offset within the buffer
   * @param length The max lenght of data to read
   * @return int The actucal number of byte read, -1 if a timeout is detected
   * @throws SlinkIOException Thrown is the underlying SlinkChannel detects an error.
   */
  public abstract int read(byte[] data, int offset, int length) throws SlinkIOException;

  /**
   * Write a SlinkMessage to the device.  This method will block until space is available on the device
   * @param data The buffer send out the SLINK channel
   * @param offset The offset within the buffer
   * @param length The length of data to write
   * @throws SlinkIOException Thrown is the underlying SlinkChannel detects an error.
   */
  public abstract void write(byte[] data, int offset, int length) throws SlinkIOException;

  /**
   * Subclasses will inspect the buffer to determine if the packet is this the device.  If the
   * subclass returns true the message will be added to the internal queue.
   * @param buffer The buffer to inspect
   * @param offset The offset of the start of the data
   * @param length The lenght of valid data
   * @return boolean True if the subclass is wants the buffer, false otherwise
   */
  protected abstract boolean isInterested(byte[] buffer, int offset, int length);

  /**
   * Get a message from the internal queue. Waits for a message for the current timeout method.
   * @return byte[] The message buffer
   * @throws IOException Thrown is an error is detected.
   * @throws InterruptedIOException Thrown if the device is close while there are threads waiting on the queue.
   */
  protected byte[] get() throws IOException
  {
    // Check state
    checkState();

    try
    {
      // Get the data buffer
      if (timeout == -1)
      {
        return (byte[])queue.take();
      }
      else
      {
        return (byte[])queue.poll(timeout);
      }
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new InterruptedIOException();
    }
  }

  /**
   * Write the specified buffer to the SlinkChannelController associated with this device.
   * @param buffer The data to write
   * @param offset The offset within the buffer
   * @param length The length of data to write
   * @throws IOException Thrown if an error is detected writing to the channel
   */
  protected void put(byte[] buffer, int offset, int length) throws IOException
  {
    // Check state
    checkState();

    // Forward to the channel
    channel.send(buffer, offset, length);
  }

  /**
   * @see com.redrocketcomputing.hardware.SlinkChannelEventListener#channelClosed()
   */
  public void channelClosed()
  {
    // Forward to device close
    close();
  }

  /**
   * @see com.redrocketcomputing.hardware.SlinkChannelEventListener#messageReceived(byte[], int, int)
   */
  public void messageReceived(int channel, byte[] buffer, int offset, int length)
  {
    try
    {
      // Check to see if this devices is interested in this message
      if (isInterested(buffer, offset, length))
      {
        // Create copy of the array
        byte[] data = new byte[length];
        System.arraycopy(buffer, offset, data, 0, length);

        // Add the data to the queue, removing old items until there is room
        while (!queue.offer(data, 0))
        {
          // Pop off head
          queue.poll(0);
        }
      }
    }
    catch (InterruptedException e)
    {
      // Clear interrupted state
      Thread.interrupted();
    }
  }

  /**
   * Check the state of the device.
   * @throws IOException Thrown if the device is closed
   */
  protected final void checkState() throws IOException
  {
    // Use queue to track state
    if (queue == null)
    {
      throw new IOException("device closed");
    }
  }

  /**
   * Dump byte to a hex string
   * @param data The data the dump
   * @param offset The starting offset within the data buffer
   * @param length The number of byte to dump
   * @return String The hex string
   */
  public String dumpBytes(byte[] data, int offset, int length)
  {
    // Build the buffer and header
    StringBuffer buffer = new StringBuffer("length: ");
    buffer.append(length);

    // Loop through all data bytes and add to the hex string
    for (int i = 0; i < length; i++)
    {
      buffer.append(":0x")     ;
      buffer.append(Integer.toHexString(data[i] & 0xff));
    }

    // All done
    return buffer.toString();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
  	// Check type
  	if (o instanceof SlinkDevice)
  	{
  		// Cast is up
  		SlinkDevice other = (SlinkDevice)o;

  		return deviceId == other.deviceId;
  	}

  	// Wrong type
  	return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
  	return deviceId;
  }

}
