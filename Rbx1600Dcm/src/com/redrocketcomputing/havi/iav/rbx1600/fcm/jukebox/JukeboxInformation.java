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
 * $Id: JukeboxInformation.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
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
 */
class JukeboxInformation extends ProtocolEventAdaptor
{
	private Protocol protocol;
  private Latch done = new Latch();
  private int capacity = 0;
  private String model = "";
  
  public JukeboxInformation(Protocol protocol) throws ProtocolException
  {
  	// Check the parameters
  	if (protocol == null)
  	{
  		// Badness
  		throw new IllegalArgumentException("Protocol is null");
  	}

    // Save the protocol and parent
    this.protocol = protocol;

    try
    {
      // Bind to the procotol
      protocol.addEventListener(this);

      // Send query for player type
      protocol.sendQueryPlayerType();
      
      // Wait for completion
      done.acquire();
      
      // Remove listener
      protocol.removeEventListener(this);
    }
    catch (ProtocolException e)
    {
      // Unbind the protocol on error
      protocol.removeEventListener(this);

      // Rethrow the exception
      throw e;
    }
    catch (InterruptedException e)
    {
      // Unbind the protocol on error
      protocol.removeEventListener(this);
      
      // Translate
      throw new ProtocolException("interrupted");
    }
  }

  /**
   * @see com.redrocketcomputing.havi.lav.sony.jukebox.protocol.ProtocolEventListener#handlePlayerType(int)
   */
  public void handlePlayerType(int capacity)
  {
		// Save the capacity
    this.capacity = capacity;

		// Check for model pump
		if (model.equals(""))
		{
      try
      {
        // Send the model query
        protocol.sendQueryPlayerModel();
      }
      catch (ProtocolException e)
      {
      	// Log the error
      	LoggerSingleton.logError(this.getClass(), "handlePlayerType", e.toString());
      }
		}
  }

  /**
   * @see com.redrocketcomputing.havi.lav.sony.jukebox.protocol.ProtocolEventListener#handlePlayerModel(String)
   */
  public void handlePlayerModel(String model)
  {
		// Save the model
    this.model = model;

		// Check for type pump
		if (capacity == 0)
		{
      try
      {
        // Send the model query
        protocol.sendQueryPlayerType();
      }
      catch (ProtocolException e)
      {
      	// Log the error
      	LoggerSingleton.logError(this.getClass(), "handlePlayerModel", e.toString());
      }
		}
  }

  /**
   * @see com.redrocketcomputing.havi.lav.sony.jukebox.protocol.ProtocolEventListener#handlePlayerState(int, int, int, int)
   */
  public void handlePlayerState(int state, int mode, int disc, int track)
  {
    try
    {
    	// Check to see if we are done
			if (!model.equals("") && capacity != 0)
			{
				// Unbind from the protocol, the will enable the probe to be garbage collect
				protocol.removeEventListener(this);

        // Release waiter
        done.release();

				// All done
				return;
			}

			// Check for model pump
			if (model.equals(""))
			{
		    // Log some debug information
		    LoggerSingleton.logDebugCoarse(this.getClass(), "handlePlayerState", "pumping player model");

		    // Send the model query
		    protocol.sendQueryPlayerModel();
			}

			// Check for type pump
			else if (capacity == 0)
			{
        // Log some debug information
        LoggerSingleton.logDebugCoarse(this.getClass(), "handlePlayerState", "pumping player type");

        // Send the model query
        protocol.sendQueryPlayerType();
			}
    }
    catch (ProtocolException e)
    {
      LoggerSingleton.logError(this.getClass(), "handlePlayerState", e.toString());
    }
  }
  
  
  /**
   * @return Returns the capacity.
   */
  public final int getCapacity()
  {
    return capacity;
  }
  
  /**
   * @return Returns the model.
   */
  public final String getModel()
  {
    return model;
  }
}
