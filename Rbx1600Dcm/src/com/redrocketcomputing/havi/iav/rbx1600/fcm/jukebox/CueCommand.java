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
 * $Id: CueCommand.java,v 1.4 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.util.concurrent.Latch;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CueCommand extends ProtocolEventAdaptor
{
  private Protocol protocol;
  private int slot;
  private int track;
  private Latch complete = new Latch();
  private boolean error = false;
  
//  /* (non-Javadoc)
//   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handlePlaying()
//   */
//  public void handlePaused()
//  {
//    complete.release();
//  }

  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleMissingDisc(int)
   */
  public void handleMissingDisc(int disc)
  {
    LoggerSingleton.logDebugFine(this.getClass(), "handleMissingDisc", "Device: " + Integer.toHexString(protocol.getDeviceId()) + " disc: " + disc);
    error = true;
    complete.release();
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleNoDisc()
   */
  public void handleNoDisc()
  {
    LoggerSingleton.logDebugFine(this.getClass(), "handleNoDisc", "Device: " + Integer.toHexString(protocol.getDeviceId()));
    error = true;
    complete.release();
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleLoadingDisc(int)
   */
  public void handleLoadingDisc(int disc)
  {
    LoggerSingleton.logDebugFine(this.getClass(),  "Device: " + Integer.toHexString(protocol.getDeviceId()) + " handleLoadingDisc", "disc: " + disc);
    if (slot == disc)
    {
      complete.release();
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleLoadedDisc(int)
   */
  public void handleLoadedDisc(int disc)
  {
    LoggerSingleton.logDebugFine(this.getClass(),  "Device: " + Integer.toHexString(protocol.getDeviceId()) + " handleLoadedDisc", "disc: " + disc);
    if (slot == disc)
    {
      complete.release();
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleDisplayingDisc(int)
   */
  public void handleDisplayingDisc(int disc)
  {
    LoggerSingleton.logDebugFine(this.getClass(), "Device: " + Integer.toHexString(protocol.getDeviceId()) + " handleDisplayingDisc", "disc: " + disc);
    if (slot == disc)
    {
      complete.release();
    }
  }
  
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handlePlayingTrack(int, int, int, int)
   */
  public void handlePlayingTrack(int disc, int track, int minutes, int seconds)
  {
    LoggerSingleton.logDebugFine(this.getClass(), "Device: " + Integer.toHexString(protocol.getDeviceId()) + " handlePlayingTrack", "disc: " + disc + " track: " + track);
    if (this.slot == disc && this.track == track)
    {
      complete.release();
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handlePlayerState(int, int, int, int)
   */
  public void handlePlayerState(int state, int mode, int disc, int track)
  {
    LoggerSingleton.logDebugFine(this.getClass(), "handlePlayerState", "Device: " + Integer.toHexString(protocol.getDeviceId()) + " mode: " + Integer.toHexString(mode) + " disc: " + disc + " track:" + track);
    if (this.slot == disc && this.track == track)
    {
      complete.release();
    }
  }
  
  /**
   * 
   */
  public CueCommand(Protocol protocol, int slot, int track)
  {
    // Save parameters
    this.protocol = protocol;
    this.slot = slot;
    this.track = track;
  }
  
  public void execute() throws ProtocolException
  {
    // Bind to the protocol
    protocol.addEventListener(this);
    LoggerSingleton.logDebugFine(this.getClass(), "execute", "Device: " + Integer.toHexString(protocol.getDeviceId()) + " cue at " + slot + ":" + track);
    try
    {
      // Loop forever
      while (true)
      {
        // Send command
        protocol.sendCueAt(slot, track);
        
        // Wait for complete
        if (complete.attempt(10000))
        {
          // All done
          return;
        }

        LoggerSingleton.logDebugCoarse(this.getClass(), "Device: " + Integer.toHexString(protocol.getDeviceId()) + " execute", "retry");
      }
    }
    catch (InterruptedException e)
    {
      // Clear interruption and translate
      Thread.currentThread().interrupt();
      throw new ProtocolException(e.toString());
    }
    finally
    {
      // Alway bind the protocol
      protocol.removeEventListener(this);
    }
  }
  
  public boolean hasError()
  {
    return error;
  }
}

