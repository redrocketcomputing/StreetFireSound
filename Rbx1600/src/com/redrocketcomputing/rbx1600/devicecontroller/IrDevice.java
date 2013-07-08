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
 * $Id: IrDevice.java,v 1.1 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.devicecontroller;

import java.io.IOException;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.hardware.IrController;
import com.redrocketcomputing.hardware.IrEventListener;
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
public class IrDevice extends CommandManager implements IrEventListener
{
  private final static int DEFAULT_BUFFER_TIMEOUT = 7000;
  
  private IrController irController;
  private IrSignalProcessor irSignalProcessor;
  private int bufferTimeout;

  public IrDevice(String instanceName)
  {
    // Construct super class
    super(instanceName);
  }

  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Verify idle
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("service is already running");
    }
    
    try
    {
      // Get the buffer timeout
      bufferTimeout = getConfiguration().getIntProperty("buffer.timeout", DEFAULT_BUFFER_TIMEOUT);
      
      // Forward to super class
      super.start();
      
      // Create components
      irController = new IrController();
      irSignalProcessor = new IrSignalProcessor(this, bufferTimeout);
      irController.addListener(this);
      
      // Mark as running
      setServiceState(Service.RUNNING);
      
      // Log start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running");
    }
    catch(IOException e)
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
    // Verify running
    if (getServiceState() != Service.RUNNING)
    {
      // Bad
      throw new ServiceException("service is already idle");
    }
    
    // Close components
    irController.close();
    irSignalProcessor.close();
    
    // Forward to super class
    super.terminate();
    
    // Change state
    setServiceState(Service.IDLE);

    // Log start
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  /**
   * @see com.redrocketcomputing.hardware.IREventListener#IREvent(byte, byte)
   */
  public void IrEvent(byte deviceCode, byte signal)
  {
    if(deviceCode != IrSignalConstant.PLAYER_3)
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "IrEvent", "Signal not from player channel 3.  Ignore");
      return;
    }

//    LoggerSingleton.logDebugCoarse(this.getClass(), "messageRecieved", "deviceCode: " + Integer.toBinaryString(deviceCode).substring(24));
//    LoggerSingleton.logDebugCoarse(this.getClass(), "messageRecieved", "command: " + Integer.toBinaryString(signal));

    if(IrSignalProcessor.isMetaSignal(signal))
    {
      irSignalProcessor.processMetaSignal(signal);
    }
    else if(IrSignalProcessor.isNumericSignal(signal))
    {
      irSignalProcessor.processNumericSignal(signal);
    }
    else if(IrSignalProcessor.isExecutionSignal(signal))
    {
      irSignalProcessor.processExecutionSignal(signal);
    }
    else
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceived", "igoring unsupported signal");
    }
  }

}
