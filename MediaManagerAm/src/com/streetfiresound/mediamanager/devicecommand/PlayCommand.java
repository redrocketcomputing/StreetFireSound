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
 * $Id: PlayCommand.java,v 1.1 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.devicecommand;

import org.havi.system.types.HaviException;

import com.redrocketcomputing.havi.commandcontroller.AbstractCommand;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstTransportState;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerClient;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class PlayCommand implements AbstractCommand
{
  private MediaPlayerClient client;

  /**
   * Construct PlayCommand
   * @param client The MediaPlayerClient to issue the Play command
   */
  public PlayCommand(MediaPlayerClient client)
  {
    // Save the client
    this.client = client;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.commandcontroller.AbstractCommand#execute()
   */
  public void execute() throws HaviException
  {
    // Get the player state and queue
    int state = client.getStateSync(0);
    
    // Check state
    if (state == ConstTransportState.STOP)
    {
      // Retreive qeue
      PlayQueue playQueue = client.getQueueSync(0);
      
      // Play at zero index
      client.playSync(0, playQueue.getVersion(), 0);
    }
    else if (state == ConstTransportState.PAUSE)
    {
      // Just resume
      client.resumeSync(0);
    }
  }
}
