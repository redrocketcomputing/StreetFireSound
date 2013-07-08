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
 * $Id: TransportStateTracker.java,v 1.3 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.util.Observable;

import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.avdisc.types.HaviAvDiscUnidentifiedFailureException;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TransportStateTracker extends Observable implements org.havi.fcm.avdisc.constants.ConstAvDiscTransportMode, com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstAvDiscTransportMode
{
  private static class MessageRouter extends ProtocolEventAdaptor
  {
    private TransportStateTracker parent;
    
    public MessageRouter(TransportStateTracker parent)
    {
      this.parent = parent;
    }

    public void handleDoorOpened()
    {
      parent.setState(DOOR_OPENED);
    }

    public void handlePaused()
    {
      parent.setState(PAUSE);
    }

    public void handlePlaying()
    {
      parent.setState(PLAY);
    }

    public void handlePowerOff()
    {
      parent.setState(POWER_OFF);
    }

    public void handlePlayerState(int state, int mode, int disc, int track)
    {
      switch (state)
      {
        case 0x00:
        {
          parent.setState(STOP);
          break;
        }
        case 0x01:
        {
          parent.setState(PLAY);
          break;
        }
        case 0x02:
        {
          parent.setState(PAUSE);
          break;
        }
        case 0x03:
        {
          parent.setState(DOOR_OPENED);
          break;
        }
        case 0x10:
        case 0x3f:
        {
          parent.setState(POWER_OFF);
          break;
        }
        case 0x2f:
        {
          parent.setState(NO_MEDIA);
          break;
        }
      }
    }
  }

  private final static String[] STATE_STRINGS = { "PLAY", "RECORD", "VARIABLE_FORWARD", "VARIABLE_REVERSE", "STOP", "PAUSE", "SKIP", "NO_MEDIA", "POWER_OFF", "DOOR_OPENED"};

  private Protocol protocol;
  private MessageRouter messageRouter;
  private int state = -1;
  
  /**
   * 
   */
  public TransportStateTracker(Protocol protocol) throws HaviAvDiscException
  {
    try
    {
      // Save the protocol
      this.protocol = protocol;
      
      // Bind message router
      messageRouter = new MessageRouter(this);
      protocol.addEventListener(messageRouter);
      
      // Send state request
      while (state == -1)
      {
        protocol.sendQueryPlayerState();
        Thread.sleep(1000);
      }
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }
  
  public void close()
  {
    // Release resources
    protocol.removeEventListener(messageRouter);
  }
  
  public final int getState()
  {
    return state;
  }
 
  public final void reset()
  {
    setState(-1);
  }
  
  protected void setState(int newState)
  {
    // Ignore for matching states
    if (state == newState)
    {
      return;
    }
    
    // Update state
    state = newState;
    setChanged();
    notifyObservers(new Integer(state));
  }
  
  public String toString()
  {
    if (state == -1)
    {
      return "UNKNOWN";
    }
    
    if (state >= STATE_STRINGS.length)
    {
      return Integer.toString(state);
    }
    
    return STATE_STRINGS[state];
  }
}
