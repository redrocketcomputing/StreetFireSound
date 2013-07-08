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
 * $Id: StopCommand.java,v 1.2 2005/02/23 19:58:38 stephen Exp $
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
class StopCommand extends ProtocolEventAdaptor
{
  private Protocol protocol;
  private Latch complete = new Latch();
  
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleStopped()
   */
  public void handleStopped()
  {
    // Release waiter
    complete.release();
  }
  
  
//  /* (non-Javadoc)
//   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleDuplicate()
//   */
//  public void handleDuplicate()
//  {
//    // Release waiter
//    complete.release();
//  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handlePlayerState(int, int, int, int)
   */
  public void handlePlayerState(int state, int mode, int disc, int track)
  {
    // Check state
    LoggerSingleton.logDebugCoarse(this.getClass(), "handlePlayerState", "checking " + Integer.toHexString(state & 0xff));
    if ((state & 0xf) == 0 || (state & 0xf) == 0xf || (state & 0x10) != 0)
    {
      complete.release();
    }
  }
  
  /**
   * 
   */
  public StopCommand(Protocol protocol)
  {
    // Save the protocol
    this.protocol = protocol;
  }
  
  /**
   * Execute the command
   * @throws ProtocolException
   */
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
        protocol.sendStop();
        
        // Wait for complete
        if (complete.attempt(5000))
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
}
