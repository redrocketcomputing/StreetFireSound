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
 * $Id: PlayerCommandFactory.java,v 1.1 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.devicecommand;

import org.havi.system.SoftwareElement;
import org.havi.system.types.HaviException;

import com.redrocketcomputing.havi.commandcontroller.AbstractCommand;
import com.redrocketcomputing.havi.commandcontroller.AbstractCommandFactory;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerClient;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PlayerCommandFactory implements AbstractCommandFactory
{
  private SoftwareElement softwareElement;

  /**
   * Construct a PlayCommandFactory with a specific MediaPlayerClient
   */
  public PlayerCommandFactory(SoftwareElement sofwareElement)
  {
    // Check parameters
    if (sofwareElement == null)
    {
      // Bad fish
      throw new IllegalArgumentException("SoftwareElement is null");
    }
    
    // Save the parameters
    this.softwareElement = sofwareElement;
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.commandcontroller.AbstractCommandFactory#createCommand(java.lang.String, java.lang.Object)
   */
  public AbstractCommand createCommand(String key, Object parameter)
  {
    try
    {
      // Create command based on key
      String commandKey = key.toUpperCase();
      if (commandKey.equals("STOP"))
      {
        // Create stop command
        return new StopCommand(new MediaPlayerClient(softwareElement, softwareElement.getSeid()));
      }
      else if (commandKey.equals("PLAY"))
      {
        // Create play command
        return new PlayCommand(new MediaPlayerClient(softwareElement, softwareElement.getSeid()));
      }
      else if (commandKey.equals("PAUSE"))
      {
        // Create pause command
        return new PauseCommand(new MediaPlayerClient(softwareElement, softwareElement.getSeid()));
      }
      else if (commandKey.equals("NEXT_TRACK"))
      {
        // Create skip forward command
        return new SkipForwardCommand(new MediaPlayerClient(softwareElement, softwareElement.getSeid()));
      }
      else if (commandKey.equals("PREVIOUS_TRACK"))
      {
        // Create skip reverse command
        return new SkipReverseCommand(new MediaPlayerClient(softwareElement, softwareElement.getSeid()));
      }
      
      // Not for us
      return null;
    }
    catch (HaviException e)
    {
      // Log the error and exit
      LoggerSingleton.logError(this.getClass(), "createCommand", e.toString());
      return null;
    }
  }
}
