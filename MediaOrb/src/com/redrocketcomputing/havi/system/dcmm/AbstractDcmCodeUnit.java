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
 * $Id: AbstractDcmCodeUnit.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */
package com.redrocketcomputing.havi.system.dcmm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.havi.dcm.constants.ConstFCAssigner;
import org.havi.dcm.types.TargetId;
import org.havi.system.DcmCodeUnitInterface;
import org.havi.system.UninstallationListener;

import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.dcm.Dcm;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractDcmCodeUnit implements DcmCodeUnitInterface
{
  protected UninstallationListener listener = null;
  protected Dcm deviceControlModule = null;
  protected Class deviceControlModuleClass;
  
  /**
   * 
   */
  public AbstractDcmCodeUnit(Class deviceControlModuleClass)
  {
    this.deviceControlModuleClass = deviceControlModuleClass;
  }

  /* (non-Javadoc)
   * @see org.havi.system.DcmCodeUnitInterface#install(String, TargetId, boolean, UninstallationListener)
   */
  public int install(String instanceName, TargetId targetId, boolean n1Uniqueness, UninstallationListener listener)
  {
    // Save the listener
    this.listener = listener;
    
    // Check to see is we are already setup
    if (deviceControlModule != null)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", "DeviceControlModule: " + deviceControlModule.getClass().getName() + " already installed");
      
      // Return error
      return 1;
    }
    
    try
    {
      // Build constructor query
      Class[] parameterTypes = new Class[4];
      parameterTypes[0] = String.class;
      parameterTypes[1] = TargetId.class;
      parameterTypes[2] = Boolean.TYPE;
      parameterTypes[3] = Integer.TYPE;
      
      // Get the constructor
      Constructor constructor = deviceControlModuleClass.getConstructor(parameterTypes);

      // Build arguments
      Object[] arguments = new Object[4];
      arguments[0] = instanceName;
      arguments[1] = targetId;
      arguments[2] = new Boolean(n1Uniqueness);
      arguments[3] = new Integer(ConstFCAssigner.NONE);
      
      // Create the service class
      deviceControlModule = (Dcm)constructor.newInstance(arguments);
      
      // Add to service manager
      ServiceManager.getInstance().add(deviceControlModule);
      
      // Start it up
      deviceControlModule.start();
      
      return 0;
    }
    catch (ServiceException e)
    {
      // Remove it
      ServiceManager.getInstance().remove(deviceControlModule);
      
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", e.toString());
      
      // Return error
      return 1;
    }
    catch (SecurityException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", e.toString());
      
      // Return error
      return 1;
    }
    catch (IllegalArgumentException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", e.toString());
      
      // Return error
      return 1;
    }
    catch (NoSuchMethodException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", e.toString());
      
      // Return error
      return 1;
    }
    catch (InstantiationException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", e.toString());
      
      // Return error
      return 1;
    }
    catch (IllegalAccessException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", e.toString());
      
      // Return error
      return 1;
    }
    catch (InvocationTargetException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", e.toString());
      
      // Return error
      return 1;
    }
  }

  /* (non-Javadoc)
   * @see org.havi.system.AmCodeUnitInterface#uninstall()
   */
  public void uninstall()
  {
    // Check to see is we are not setup
    if (deviceControlModule == null)
    {
      // Just ignore
      return;
    }
    
    // Terminate application module
    deviceControlModule.terminate();
    
    // Remove from service manager
    ServiceManager.getInstance().remove(deviceControlModule);
    
    // Invoke listener
    if (listener != null)
    {
      listener.uninstalled();
    }
  }
}
