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
 * $Id: AudioPathManager.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.io.IOException;

import com.redrocketcomputing.hardware.DigitalAudioController;
import com.redrocketcomputing.hardware.LedController;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author unascribed
 * @version 1.0
 */

public class AudioPathManager extends ProtocolEventAdaptor
{
	private static DigitalAudioController dac = null;
	private static LedController led = null;
  private static int activeChannel = -1;

  private int channel;
	private Protocol protocol;
  private boolean positionTrackingEnabled = false;

  static
  {
    try
    {
    	// Create the common dac
      dac = new DigitalAudioController();

      // Create new common led
      led = new LedController();

      // Enable dac
      dac.setEnabled(true);
      led.setChannel(LedController.CHANNEL_OFF);
    }
    catch (IOException e)
    {
    	// Very bad
    	LoggerSingleton.logFatal(AudioPathManager.class, "static initializer", e.toString());
    }
  }

  public AudioPathManager(Protocol protocol)
  {
    // Save the parameters
    this.protocol = protocol;
    this.channel = (protocol.getDeviceId() >> 16) & 0x3;

    // Bind to the protocol
    protocol.addEventListener(this);
  }

  public void close()
  {
  	// Remove the listeners
  	protocol.removeEventListener(this);
  }

  public void handlePlaying()
  {
    try
    {
    	synchronized(dac)
    	{
	      // Switch audio channel
	      dac.setChannel(channel);
	      led.setChannel(channel + 1);
        activeChannel = channel;
    	}
    }
    catch (IOException e)
    {
    	// Log the error
    	LoggerSingleton.logError(this.getClass(), "handlePlaying", e.toString());
    }
  }

  public void handleStopped()
  {
    try
    {
    	synchronized(dac)
    	{
	      // Check to see if we are the active channel
	      if (channel == dac.getChannel())
	      {
	        // Turn mute it
		      led.setChannel(LedController.CHANNEL_OFF);
          activeChannel = -1;
	      }
    	}
    }
    catch (IOException e)
    {
    	// Log the error
    	LoggerSingleton.logError(this.getClass(), "handleStop", e.toString());
    }
  }

  public void handleDoorOpened()
  {
    try
    {
    	synchronized(dac)
    	{
	      // Check to see if we are the active channel
	      if (channel == dac.getChannel())
	      {
	        // Turn mute it
		      led.setChannel(LedController.CHANNEL_OFF);
          activeChannel = -1;
	      }
    	}
    }
    catch (IOException e)
    {
    	// Log the error
    	LoggerSingleton.logError(this.getClass(), "handleDoorOpened", e.toString());
    }
  }

  public void handlePowerOff()
  {
    try
    {
    	synchronized(dac)
    	{
	      // Check to see if we are the active channel
	      if (channel == dac.getChannel())
	      {
	        // Turn mute it
		      led.setChannel(LedController.CHANNEL_OFF);
          activeChannel = -1;
	      }
    	}
    }
    catch (IOException e)
    {
    	// Log the error
    	LoggerSingleton.logError(this.getClass(), "handlePowerOff", e.toString());
    }
  }
}
