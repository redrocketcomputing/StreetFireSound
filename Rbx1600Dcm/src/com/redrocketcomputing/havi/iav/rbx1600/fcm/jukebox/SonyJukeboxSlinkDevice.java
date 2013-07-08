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
 * $Id: SonyJukeboxSlinkDevice.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.io.IOException;

import com.redrocketcomputing.hardware.SlinkChannelController;
import com.redrocketcomputing.havi.system.cmm.slink.SlinkDevice;
import com.redrocketcomputing.havi.system.cmm.slink.SlinkIOException;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public class SonyJukeboxSlinkDevice extends SlinkDevice
{
  public final static int DEFAULT_QUEUE_SIZE = 10;

  private byte[] writeBuffer = new byte[33];

  /**
   * Constructor for SonyJukeboxSlinkDevice.
   * @param channel
   */
  public SonyJukeboxSlinkDevice(SlinkChannelController channel, int deviceId)
  {
    // Construct super class
    super(channel, DEFAULT_QUEUE_SIZE);

    // Save the device id
    this.deviceId = deviceId;
    this.description = "SonyJukeboxSlinkDevice 0x" + Integer.toHexString(deviceId);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.slink.SlinkDevice#close()
   */
  public void close()
  {
    super.close();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.slink.SlinkDevice#read(byte[], int, int)
   */
  public int read(byte[] data, int offset, int length) throws SlinkIOException
  {
    try
    {
      // Wait for message
      byte[] buffer = get();
      if (buffer == null)
      {
        // Timeout
        return -1;
      }

      int size = -1;
      try
      {
        // Build message size and check buffer length
        size = ((buffer[0] & 0xff) / 8);
        if (length <= size)
        {
          // Bad buffer
          throw new SlinkIOException("bad buffer length, need " + size + " have " + length);
        }

        // Build first message byte, which contains the control flag
        if ((buffer[1] & 0x0f) == (deviceId & 0x0f))
        {
          // Low device
          data[0] = (byte) 0x90;
        }
        else
        {
          // High device
          data[0] = (byte) 0x91;
        }

        // Copy into the
        System.arraycopy(buffer, 2, data, 1, size - 1);
      }
      catch (IndexOutOfBoundsException e)
      {
        StringBuffer bufferString = new StringBuffer(" buffer:");
        for (int i = 0; i < buffer.length; i++)
        {
          bufferString.append(':');
          bufferString.append(Integer.toHexString(buffer[0] & 0xff));
        }
        LoggerSingleton.logError(this.getClass(), "read", "buffer length: " + buffer.length + " data length: " + data.length + " size " + size + bufferString.toString());
        return -1;
      }

      // Return count
      return size;
    }
    catch (IOException e)
    {
      // Translate
      throw new SlinkIOException(e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.slink.SlinkDevice#write(byte[], int, int)
   */
  public void write(byte[] data, int offset, int length) throws SlinkIOException
  {
    try
    {
      // Determine message type based on the first byte of the buffer
      if ((data[offset] & 0xff) == 0xe0)
      {
        // Build base IR command, prepends 0xE to the IR command
        int ir = 0x700 + (data[offset + 1] & 0xff);

        // Check player ID
        switch ((deviceId >> 8) & 0xff)
        {
          case 0x00 :
          {
            // Add CD1 player id
            ir = (ir << 5) | 0x11;

            // Build write buffer
            writeBuffer[0] = 16;
            writeBuffer[1] = (byte) ((ir >> 8) & 0xff);
            writeBuffer[2] = (byte) (ir & 0xff);
            length = 3;
            LoggerSingleton.logDebugCoarse(this.getClass(), "write", "sending IR to 0x90: " + Integer.toHexString(writeBuffer[0] & 0xff) + Integer.toHexString(writeBuffer[1] & 0xff) + Integer.toHexString(writeBuffer[2] & 0xff));
            break;
          }
          case 0x01 :
          {
            // Add CD2 player id
            ir = ((ir << 9) | 0x138);

            // Build write buffer
            writeBuffer[0] = 19;
            writeBuffer[1] = (byte) ((ir >> 12) & 0xff);
            writeBuffer[2] = (byte) ((ir >> 4) & 0xff);
            writeBuffer[3] = (byte) ((ir << 4) & 0xff);
            length = 4;
            LoggerSingleton.logDebugCoarse(this.getClass(), "write", "sending IR to 0x91: " + Integer.toHexString(writeBuffer[0] & 0xff) + Integer.toHexString(writeBuffer[1] & 0xff) + Integer.toHexString(writeBuffer[2] & 0xff) + Integer.toHexString(writeBuffer[3] & 0xff));
            break;
          }
          case 0x02 :
          {
            // Add CD3 player id
            ir = ((ir << 9) | 0x114);

            // Build write buffer
            writeBuffer[0] = 19;
            writeBuffer[1] = (byte) ((ir >> 12) & 0xff);
            writeBuffer[2] = (byte) ((ir >> 4) & 0xff);
            writeBuffer[3] = (byte) ((ir << 4) & 0xff);
            length = 4;
            LoggerSingleton.logDebugCoarse(this.getClass(), "write", "sending IR to 0x92: " + Integer.toHexString(writeBuffer[0] & 0xff) + Integer.toHexString(writeBuffer[1] & 0xff) + Integer.toHexString(writeBuffer[2] & 0xff) + Integer.toHexString(writeBuffer[3] & 0xff));
            break;
          }

          default:
          {
          	// Bad ness
          	throw new IllegalStateException("bad deviceId: 0x" + Integer.toHexString(deviceId));
          }
        }
      }
      else
      {
        // Check low device
        if ((data[offset] & 0xff) == 0x90)
        {
          // Build outbound device id
          writeBuffer[1] = (byte) (0x90 | ((deviceId >> 8) & 0xff));
        }
        else
        {
          // Build outbound device id
          writeBuffer[1] = (byte) (0x90 | (((deviceId >> 8) & 0xff) + 3));
        }

        // Copy remaining data
        System.arraycopy(data, offset + 1, writeBuffer, 2, length - 1);

        // Calculate the number of bits
        writeBuffer[0] = (byte) (length * 8);
      }

      // Send the message to the channel
      put(writeBuffer, 0, length + 1);
    }
    catch (IOException e)
    {
      throw new SlinkIOException(e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.slink.SlinkDevice#isInterested(byte[], int, int)
   */
  protected boolean isInterested(byte[] buffer, int offset, int length)
  {
    // MAY NEED TO HANDLE INBOUND IR COMMANDS
    int delta = (buffer[1] & 0x07) - (deviceId & 0x07);
    return delta == 0 || delta == 3;
  }
}
