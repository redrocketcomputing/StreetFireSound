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
 * $Id: DigitalAudioController.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.hardware;

import java.io.IOException;

/**
 * @author stephen
 *
 */
public class DigitalAudioController
{
  public static int NUMBER_OF_CHANNELS = 4;
  public static int VOLUME_MAX = 0;
  public static int VOLUME_MIM = -90;
  public static int NORMAL_MODE = 0;
  public static int PLAYBACK_MODE = 1;
  public static int RECORD_MODE = 2;

  private static int RESET_COMMAND = 0x00000000;
  private static int ENABLE_COMMAND = 0x00010000;
  private static int MUTE_COMMAND = 0x00020000;
  private static int MODE_COMMAND = 0x00030000;
  private static int CHANNEL_COMMAND = 0x00040000;
  private static int VOLUME_COMMAND = 0x00050000;
	private static int CANCEL_COMMAND = 0xffff0000;

  private static int ENABLED_MASK = (1 << 31);
  private static int MUTE_MASK = (1 << 30);

  private static String DEVICE_CONTROL_PATH = "/dev/dac/control";

	private DeviceDriver control;

  /**
   * Construct a new digital audio controller
   */
  public DigitalAudioController() throws IOException
  {
    // Open the device driver files
    control = new DeviceDriver(DEVICE_CONTROL_PATH);
  }

  /**
   * Close the digital audio controller and release all resources
   */
  public synchronized void close()
  {
  	// Close the control and toc devices
  	control.close();
  }

  public synchronized void reset() throws IOException
  {
    // Send command
    control.writeInt(RESET_COMMAND);
  }

  public synchronized void setEnabled(boolean value) throws IOException
  {
    // Send command
    control.writeInt((value ? ENABLE_COMMAND | 0x0001 : ENABLE_COMMAND));
  }

  public synchronized boolean isEnabled() throws IOException
  {
    // Read current state
    return (control.readInt() & ENABLED_MASK) != 0;
  }

  public synchronized void setMuted(boolean value) throws IOException
  {
    // Send command
    control.writeInt((value ? MUTE_COMMAND | 0x0001 : MUTE_COMMAND));
  }

  public synchronized boolean isMuted() throws IOException
  {
    // Read current state
    return (control.readInt() & MUTE_MASK) != 0;
  }

  public synchronized void setMode(int value) throws IOException
  {
    // Check range
    if (value < NORMAL_MODE || value > RECORD_MODE)
    {
      throw new IllegalArgumentException("bad mode: " + value);
    }

    // Send command
    control.writeInt(MODE_COMMAND | value);
  }

  public synchronized int getMode() throws IOException
  {
    // Read current state
    return ((control.readInt() >> 16) & 0x07);
  }

  public synchronized void setChannel(int value) throws IOException
  {
    // Check range
    if (value < 0 || value > NUMBER_OF_CHANNELS)
    {
      throw new IllegalArgumentException("bad channel: " + value);
    }

    // Send command
    control.writeInt(CHANNEL_COMMAND | value);
  }

  public synchronized int getChannel() throws IOException
  {
    // Read current state
    return ((control.readInt() >> 8) & 0x07);
  }

  public synchronized void setVolume(int value) throws IOException
  {
    // Check range
    if (value < VOLUME_MIM || value > VOLUME_MAX)
    {
      throw new IllegalArgumentException("bad volume: " + value);
    }

    // Send command
    control.writeInt(VOLUME_COMMAND | value);
  }

  public synchronized int getVolume() throws IOException
  {
    // Read current state
    return control.readInt() & 0x7f;
  }
}
