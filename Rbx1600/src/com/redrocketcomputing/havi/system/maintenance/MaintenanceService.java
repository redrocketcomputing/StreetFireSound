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
 * $Id: MaintenanceService.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

import java.io.PrintStream;
import java.net.ServerSocket;

import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MaintenanceService extends AbstractService
{
  private ComponentConfiguration configuration;
  private MaintenanceAcceptor acceptor = null;
  private ServerSocket serverSocket;

  /**
   * Construct a new MaintenanceService
   * @param instanceName The name of this service
   */
  public MaintenanceService(String instanceName)
  {
    // Construct superclass
    super(instanceName);

    // Construct a configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Check to see if we are already started
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException("MaintenanceService already running");
    }

    try
    {
      // Get the local GUID
      Gadp gadp = (Gadp)ServiceManager.getInstance().find(Gadp.class);
      if (gadp == null)
      {
        // Badness
        throw new ServiceException("Gadp Service not found");
      }

      // Create a new Acceptor
      acceptor = new MaintenanceAcceptor(GuidUtil.extractIpPort(gadp.getLocalGuid()) + MaintenanceConstants.PORT);

      // Create all request handler factories
      String property;
      int i = 0;
      while ((property = configuration.getProperty("request.factory." + Integer.toString(i++))) != null)
      {
        try
        {
          // Try to create the factory
          MaintenanceRequestFactory factory = (MaintenanceRequestFactory)Class.forName(property).newInstance();

          // Add it to the acceptor
          acceptor.addRequestFactory(factory);
        }
        catch (InstantiationException e)
        {
          // Log the error
          LoggerSingleton.logError(this.getClass(), "start", e.toString());
        }
        catch (IllegalAccessException e)
        {
          // Log the error
          LoggerSingleton.logError(this.getClass(), "start", e.toString());
        }
        catch (ClassNotFoundException e)
        {
          // Log the error
          LoggerSingleton.logError(this.getClass(), "start", e.toString());
        }
      }

      // Change state to running
      setServiceState(Service.RUNNING);

      // Log the start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running on port " + GuidUtil.extractIpPort(gadp.getLocalGuid()));
    }
    catch (MaintenanceException e)
    {
      e.printStackTrace();

      // Translate to a service exception
      throw new ServiceException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Check to see if we are already started
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException("MaintenanceService is already idle");
    }

    // Close the acceptor
    acceptor.close();
    acceptor = null;

    // Change state to running
    setServiceState(Service.IDLE);

    // Log the start
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#info(java.io.PrintStream, java.lang.String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Check for running
    if (getServiceState() == Service.RUNNING)
    {
      // Print header
      printStream.println("Maintenance factories:");

      // Dump the factories
      acceptor.dumpFactories(printStream);
    }
    else
    {
      printStream.println("MaintenanceService is IDLE");
    }
  }
}
