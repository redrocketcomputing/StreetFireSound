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
 * $Id: LedController.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.hardware;

import java.io.IOException;

/**
 * @author stephen
 *
 */
public class LedController extends DeviceDriver
{
  public static int CHANNEL_OFF = 0;
  public static int CHANNEL_ONE = 1;
  public static int CHANNEL_TWO = 2;
  public static int CHANNEL_THREE = 3;
  public static int CHANNEL_FOUR = 4;
  public static int CHANNEL_FLASH = 5;
  public static int CHANNEL_ROTATE = 6;
  public static int POWER_OFF = 7;
  public static int POWER_ON = 8;
  public static int POWER_TOGGLE = 9;
  public static int POWER_FLASH = 10;

  private static String DEVICE_PATH = "/proc/driver/led/control";

  public LedController() throws IOException
  {
    super(DEVICE_PATH);
  }

  public void setChannel(int value) throws IOException
  {
    // Check parameters
    if (value < CHANNEL_OFF || value > CHANNEL_ROTATE)
    {
      throw new IllegalArgumentException("bad led channel command: " + value);
    }

    // Send command
    writeInt(value);
  }

  public int getChannel() throws IOException
  {
    // Read state
    return (readInt() >> 16) & 0xffff;
  }

  public void setPower(int value) throws IOException
  {
    // Check parameters
    if (value < POWER_OFF || value > POWER_FLASH)
    {
      throw new IllegalArgumentException("bad led power command: " + value);
    }

    // Send command
    writeInt(value);
  }

  public int getPower() throws IOException
  {
    // Read state
    return readInt() & 0xffff;
  }
}
