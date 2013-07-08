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
 * $Id: ResumeCommand.java,v 1.2 2005/02/27 22:58:56 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.util.concurrent.Latch;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResumeCommand extends ProtocolEventAdaptor
{
  private Protocol protocol;
  private Latch complete = new Latch();
  
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventListener#handleStopped()
   */
  public void handlePlaying()
  {
    // Release waiter
    complete.release();
  }
  
  /**
   * 
   */
  public ResumeCommand(Protocol protocol)
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
        protocol.sendPlay();
        
        // Wait for complete
        if (complete.attempt(5000))
        {
          // All done
          return;
        }
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
