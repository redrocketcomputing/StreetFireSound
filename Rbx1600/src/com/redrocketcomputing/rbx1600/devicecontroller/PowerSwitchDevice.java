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
 * $Id: PowerSwitchDevice.java,v 1.1 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.devicecontroller;

import java.io.IOException;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.hardware.PowerSwitchController;
import com.redrocketcomputing.hardware.PowerSwitchEventListener;
import com.redrocketcomputing.havi.commandcontroller.CommandManager;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author david
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PowerSwitchDevice extends CommandManager implements PowerSwitchEventListener
{
  private PowerSwitchController powerSwitchController = null;

  /**
   * Constructor for PowerSwitchDevice.
   */
  public PowerSwitchDevice(String instanceName)
  {
    // Construct super class
    super(instanceName);
  }

  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Verify we are not running
    if (getServiceState() == Service.RUNNING)
    {
      // Bad
      throw new ServiceException("already running");
    }
    
    try
    {
      // Forward to super class
      super.start();
      
      // Create components
      this.powerSwitchController = new PowerSwitchController();
      this.powerSwitchController.addListener(this);

      // Mark as running
      setServiceState(Service.RUNNING);
      
      // Log start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running");
    }
    catch (IOException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Make sure we are not idle
    if (getServiceState() == Service.IDLE)
    {
      // Bad
      throw new ServiceException("service is already idle");
    }
    
    // Remove listeners and close components
    powerSwitchController.removeListener(this);
    powerSwitchController.close();
    powerSwitchController = null;
    
    // Forward to super class
    super.terminate();

    // Mark as running
    setServiceState(Service.IDLE);
    
    // Log start
    LoggerSingleton.logInfo(this.getClass(), "start", "service is idle");
  }
  
  /**
   * @see com.redrocketcomputing.hardware.PowerSwitchEventListener#powerStateChanged(boolean)
   */
	public void pushedEvent()
  {
    // Execute power commands
    LoggerSingleton.logDebugCoarse(this.getClass(), "powerStateChanged", "pushed");
    queueCommand(createCommands("POWER", null));
  }
}
