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
 * $Id: AbstractService.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.service;

import java.io.PrintStream;
import java.util.Observable;

import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public abstract class AbstractService extends Observable implements Service
{
  private String instanceName;
  private int serviceState;
  private int serviceId;
  private ComponentConfiguration configuration;

  /**
   * Constructor for AbstractService.
   */
  public AbstractService(String instanceName)
  {
    // Save the instance name
    this.instanceName = instanceName;

    // Get the next service ID
    serviceId = ServiceManager.getNewServiceId();
    
    // Create component configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);

    // Set the service state to idle
    setServiceState(IDLE);
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#getInstanceName()
   */
  public String getInstanceName()
  {
    return instanceName;
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public abstract void start();

  /**
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public abstract void terminate();

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public abstract void info(PrintStream printStream, String[] arguments);

  /**
   * @see com.redrocketcomputing.appframework.service.Service#getServiceState()
   */
  public int getServiceState()
  {
    return serviceState;
  }

  /**
   * Returns the serviceId.
   * @return int
   */
  public int getServiceId()
  {
    return serviceId;
  }

  /**
   * @return Returns the configuration.
   */
  public final ComponentConfiguration getConfiguration()
  {
    return configuration;
  }

  /**
   * Change the service state and notify all observes
   * @param serviceState The new service state
   */
  protected final void setServiceState(int serviceState)
  {
    // Check argument
    if (serviceState < Service.IDLE || serviceState > Service.RUNNING)
    {
      throw new IllegalArgumentException("bad service state");
    }

    // Change the state
    this.serviceState = serviceState;

    // Mark obserable as changed
    setChanged();

    // Notify observers
    this.notifyObservers();
  }
 
}
