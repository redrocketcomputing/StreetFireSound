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
 * $Id: AbstractAmCodeUnit.java,v 1.3 2005/02/24 03:30:22 stephen Exp $
 */
package com.redrocketcomputing.havi.system.amm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.havi.dcm.types.TargetId;
import org.havi.system.AmCodeUnitInterface;
import org.havi.system.UninstallationListener;
import org.havi.system.types.ApplicationModuleProfile;

import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractAmCodeUnit implements AmCodeUnitInterface
{
  protected UninstallationListener listener = null;
  protected ApplicationModule applicationModule = null;
  protected Class applicationModuleClass;
  protected ApplicationModuleProfile profile;
  
  /**
   * 
   */
  public AbstractAmCodeUnit(ApplicationModuleProfile profile, Class applicationModuleClass)
  {
    this.applicationModuleClass = applicationModuleClass;
    this.profile = profile;
  }

  /* (non-Javadoc)
   * @see org.havi.system.AmCodeUnitInterface#install(org.havi.dcm.types.TargetId, boolean, org.havi.system.UninstallationListener)
   */
  public int install(TargetId targetId, boolean n1Uniqueness, UninstallationListener listener)
  {
    // Save the listener
    this.listener = listener;
    
    // Check to see is we are already setup
    if (applicationModule != null)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "install", "ApplicationModule: " + applicationModule.getClass().getName() + " already installed");
      
      // Return error
      return 1;
    }
    
    try
    {
      // Log installation
      LoggerSingleton.logDebugCoarse(this.getClass(), "install", "installing: " + profile.getName() + " description: " + profile.getDescription() + " from: " + profile.getManufacture());

      // Build constructor query
      Class[] parameterTypes = new Class[3];
      parameterTypes[0] = String.class;
      parameterTypes[1] = TargetId.class;
      parameterTypes[2] = boolean.class;

      // Get the constructor
      Constructor constructor = applicationModuleClass.getConstructor(parameterTypes);

      // Build arguments
      Object[] arguments = new Object[3];
      arguments[0] = profile.getName();
      arguments[1] = targetId;
      arguments[2] = new Boolean(n1Uniqueness);

      // Create the service class
      applicationModule = (ApplicationModule)constructor.newInstance(arguments);
      
      // Add to the service mananger
      ServiceManager.getInstance().add(applicationModule);
      
      // Start it up
      applicationModule.start();
      
      return 0;
    }
    catch (ServiceException e)
    {
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
    if (applicationModule == null)
    {
      // Just ignore
    }
    
    // Terminate application module
    applicationModule.terminate();
    
    // Invoke listener
    if (listener != null)
    {
      listener.uninstalled();
    }
  }
}
