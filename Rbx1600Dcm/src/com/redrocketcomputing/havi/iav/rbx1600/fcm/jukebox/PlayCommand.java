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
 * $Id: PlayCommand.java,v 1.3 2005/03/16 04:25:03 stephen Exp $
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
class PlayCommand extends ProtocolEventAdaptor
{
  private Protocol protocol;
  private int slot;
  private int track;
  private Latch complete = new Latch();
  private boolean error = false;
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handlePlaying()
   */
  public void handlePlaying()
  {
    complete.release();
  }

  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleMissingDisc(int)
   */
  public void handleMissingDisc(int disc)
  {
    error = true;
    complete.release();
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleNoDisc()
   */
  public void handleNoDisc()
  {
    error = true;
    complete.release();
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleLoadingDisc(int)
   */
  public void handleLoadingDisc(int disc)
  {
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
    if (this.slot == disc && this.track == track)
    {
      complete.release();
    }
  }
  
  /**
   * 
   */
  public PlayCommand(Protocol protocol, int slot, int track)
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

    try
    {
      // Loop forever
      while (true)
      {
        // Send command
        protocol.sendPlayAt(slot, track);
        
        // Wait for complete
        if (complete.attempt(10000))
        {
          // All done
          return;
        }

        LoggerSingleton.logDebugCoarse(this.getClass(), "execute", "retry");
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
