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
 * $Id: PowerStateController.java,v 1.2 2005/03/16 04:25:03 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.util.Observable;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.util.concurrent.Gate;
import com.redrocketcomputing.util.log.LoggerSingleton;


/**
 * @author stephen
 *
 */
class PowerStateController extends Observable
{
  private static class ProtocolRouter extends ProtocolEventAdaptor
  {
    private PowerStateController parent;
    
    public ProtocolRouter(PowerStateController parent)
    {
      // Save the parent
      this.parent = parent;
    }

    public void handlePowerOff()
    {
      // forward
      parent.powerIsOff();
    }
    
    public void handlePowerOn()
    {
      // forward
      parent.powerIsOn();
    }

    public void handlePlayerState(int state, int mode, int disc, int track)
    {
      // Forward
      parent.setCurrentPlayerState((state & 0x10) == 0);
    }
  }
  
  private Protocol protocol;
  private ProtocolRouter router;
  private boolean targetPowerState = false;
  private boolean currentPowerState = true;
  private Gate powerStateMatches = new Gate();
  
  /**
   * Contruct a command to change the sony jukebox power state
   * @param protocol The sony jukebox protocol to use.
   */
  public PowerStateController(Protocol protocol) throws ProtocolException
  {
    // Check the parameters
    if (protocol == null)
    {
      // Badness
      throw new IllegalArgumentException("Protocol is null");
    }

    // Save the protocol and new power state
    this.protocol = protocol;
    
    // Bind to the protocol
    router = new ProtocolRouter(this);
    protocol.addEventListener(router);
    
    // Set power state off
    setPowerState(false);
  }

  /**
   * Release resources
   */
  public void close()
  {
    // Unbind
    protocol.removeEventListener(router);
  }
  
  public void setPowerState(boolean newPowerState) throws ProtocolException
  {
    // Check for change in power state
    if (currentPowerState == newPowerState)
    {
      return;
    }
    
    try
    {
      // Reset gate
      powerStateMatches.reset();
      
      // Set the target power state
      targetPowerState = newPowerState;
      
      // Initial power state change
      if (targetPowerState)
      {
        protocol.sendSetPowerOn();
      }
      else
      {
        protocol.sendSetPowerOff();
      }
      
      // Wait for power state to match
      powerStateMatches.acquire();
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new ProtocolException(e.toString());
    }
  }
  
  public boolean getPowerState()
  {
    return currentPowerState;
  }
  
  private void powerIsOff()
  {
    // Set current power state
    currentPowerState = false;
    
    // Check for match power state
    if (targetPowerState == currentPowerState)
    {
      // Notify any observers
      setChanged();
      notifyObservers(new Boolean(currentPowerState));

      // Release waiter
      powerStateMatches.release();
    }
  }

  private void powerIsOn()
  {
    // Set current power state
    currentPowerState = true;

    // Check for match power state
    if (targetPowerState == currentPowerState)
    {
      // Notify any observers
      setChanged();
      notifyObservers(new Boolean(currentPowerState));

      // Release waiter
      powerStateMatches.release();
    }
  }
  
  private void setCurrentPlayerState(boolean playerPowerState)
  {
    try
    {
      // Update current state
      currentPowerState = playerPowerState;
      
      // Check to see if  the target and current state
      if (targetPowerState == currentPowerState)
      {
        // Make sure to release gate
        powerStateMatches.release();
        
        // All done
        return;
      }
      
      // Pump the port state
      LoggerSingleton.logDebugCoarse(this.getClass(), "setCurrentPlayerState", "pump power state to " + targetPowerState);
      if (targetPowerState)
      {
        protocol.sendSetPowerOn();
      }
      else
      {
        protocol.sendSetPowerOff();
      }
    }
    catch (ProtocolException e)
    {
      // Log error because we can not do anything about it
      LoggerSingleton.logError(this.getClass(), "pump", e.toString());
    }
  }
}
