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
 * $Id: SlinkDeviceProbe.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import com.redrocketcomputing.hardware.SlinkChannelController;
import com.redrocketcomputing.hardware.SlinkChannelEventListener;
import com.redrocketcomputing.havi.system.cmm.slink.ProbeStrategy;
import com.redrocketcomputing.havi.system.cmm.slink.SlinkDevice;
import com.redrocketcomputing.util.ListSet;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public class SlinkDeviceProbe implements ProbeStrategy, SlinkChannelEventListener
{
  private final static long PROBE_DELAY = 1000;

  private Set newDevices = new ListSet();
  private Set goneDevices = new ListSet();
  private volatile Set activeDevices = null;
  private volatile SlinkChannelController[] channels = null;
  private byte[] probeCommand = {(byte)0x10, (byte)0x0, (byte)0x0f};

  /**
   * Constructor for SlinkDeviceProbe.
   */
  public SlinkDeviceProbe()
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.slink.ProbeStrategy#probe(SlinkChannelController[], Set)
   */
  public void probe(SlinkChannelController[] channels, Set activeDevices)
  {
    // Check to channels
    if (channels == null)
    {
      throw new IllegalArgumentException("channels is null");
    }

    // Check active devices
    if (activeDevices == null)
    {
      throw new IllegalArgumentException("activeDevices is null");
    }

    // Save the channels
    this.channels = channels;

    // Set the device sets
    this.activeDevices = activeDevices;
    newDevices.clear();
    goneDevices.clear();
    goneDevices.addAll(activeDevices);

    // Attach the strategy to the channels
    addListener(channels);

    try
    {
      // Loop through all Sony Jukebox play IDs
      for (int id = 0x90; id < 0x93; id++)
      {
        // Loop through the
        for (int channel = 0; channel < (channels.length < 4 ? channels.length : 4); channel++)
        {
          // Insert the ID into the probeCommand
          probeCommand[1] = (byte)id;

          try
          {
            // Send the command three times
            channels[channel].send(probeCommand, 0, probeCommand.length);
            channels[channel].send(probeCommand, 0, probeCommand.length);
            channels[channel].send(probeCommand, 0, probeCommand.length);
          }
          catch (IOException e)
          {
            // Log error
            LoggerSingleton.logError(this.getClass(), "probe", e.toString() + " while probing channel " + channel);
          }
        }

        // Sleep some
        Thread.sleep(PROBE_DELAY);
      }
    }
    catch (InterruptedException e)
    {
      LoggerSingleton.logInfo(this.getClass(), "probe", "interrupted while probing");
    }
    finally
    {
      // Deattache the strategy from the channels
      removeListener(channels);

      // Release the channels and active devices
      channels = null;
      activeDevices = null;
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.slink.ProbeStrategy#getNewDevices()
   */
  public Set getNewDevices()
  {
    return newDevices;
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.slink.ProbeStrategy#getGoneDevices()
   */
  public Set getGoneDevices()
  {
    return goneDevices;
  }

  /**
   * @see com.redrocketcomputing.hardware.SlinkChannelEventListener#channelClosed()
   */
  public void channelClosed()
  {
  }

  /**
   * @see com.redrocketcomputing.hardware.SlinkChannelEventListener#messageReceived(byte[], int, int)
   */
  public void messageReceived(int channel, byte[] buffer, int offset, int length)
  {
    // Make thing copies of the critical objects
    Set activeDevices = this.activeDevices;
    SlinkChannelController[] channels = this.channels;

    // Make sure everythis is still hooked up
    if (channels == null || activeDevices == null)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "messageReceived", "received message while probe setup is bad, dropping message");

      // Drop
      return;
    }

    // Check for correct response type
    if (length != 8 || buffer[0] / 8 != 7)
    {
      // Not for us
      LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceive", "channel( " +  channel + "): failed length test: " + length + ':' + buffer[0]);
      return;
    }

    // Verify High nibble
    if ((buffer[1] & 0xf0) != 0x90)
    {
      // Not for us
      LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceive", "channel( " +  channel + "): failed high nibble test: 0x" + Integer.toHexString(buffer[1]));
      return;
    }

    // Check response type
    if (buffer[2] != 0x70)
    {
      // Not a deck status
      LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceive", "channel( " +  channel + "): failed response type test: " + Integer.toHexString(buffer[2]));
      return;
    }

    // Extract player ID
    int id = (buffer[1] & 0x7) < 3 ? (buffer[1] & 0x7) : (buffer[1] & 0x7) - 3;

    // Build device ID
    int deviceId = 0x90000000 |(channel << 16) | (id << 8) | (id + 8);

    // Check for device in the gone set
    SlinkDevice device = find(deviceId, goneDevices);
    if (device != null)
    {
      // Remove from the gone set
      goneDevices.remove(device);

      // All done
      return;
    }

    // Create a new device and add it the new devices set
    if (find(deviceId, newDevices) == null)
    {
      // Brand new device
      newDevices.add(new SonyJukeboxSlinkDevice(channels[channel], deviceId));
    }
  }

  /**
   * Add this channel listener to all channels.  This routes all message on the channel to
   * the probe strategy
   * @param channels The channels to add listener
   */
  private void addListener(SlinkChannelController[] channels)
  {
    // Attach the listeners
    for (int i = 0; i < channels.length; i++)
    {
      channels[i].addListener(this);
    }
  }

  /**
   * Remove this channel listener from all channels
   * @param channels The channels to remove listener
   */
  private void removeListener(SlinkChannelController[] channels)
  {
    // Attach the listeners
    for (int i = 0; i < channels.length; i++)
    {
      channels[i].removeListener(this);
    }
  }

  /**
   * Search a SLINK device set for a matching device ID
   * @param deviceId The device ID to search for
   * @param deviceSet The device set to search
   * @return SlinkDevice return the matching SlinkDevice, otherwise null
   */
  private SlinkDevice find(int deviceId, Set deviceSet)
  {
    for (Iterator iterator = deviceSet.iterator(); iterator.hasNext();)
    {
      // Extract device
      SlinkDevice element = (SlinkDevice) iterator.next();

      // Check for matching device id
      if (deviceId == element.getDeviceId())
      {
        return element;
      }
    }

    // Not found
    return null;
  }
}
