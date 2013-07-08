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
 * $Id: SystemService.java,v 1.2 2005/02/24 03:30:22 stephen Exp $
 */
package com.redrocketcomputing.havi.system.service;

import java.io.PrintStream;

import org.havi.system.SoftwareElement;
import org.havi.system.types.HaviMsgException;

import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.system.SystemSoftwareElement;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SystemService extends AbstractService
{
  private SoftwareElement softwareElement;
  private int type;
  
  /**
   * 
   */
  public SystemService(String instanceName, int type)
  {
    // Construct super class
    super(instanceName);
    
    // Range check the handle
    if (type < 0)
    {
      // Bad
      throw new IllegalArgumentException("bad SoftwareElementHandle");
    }
    
    // Save the handle
    this.type = type;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Check for running
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("already started");
    }
    
    try
    {
      // Create the system software element
      softwareElement = new SystemSoftwareElement(type);
    }
    catch (HaviMsgException e)
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
    // Check for running
    if (getServiceState() != Service.RUNNING)
    {
      // Bad
      throw new ServiceException("not started");
    }
    
    try
    {
      // Close the software element
      softwareElement.close();
      softwareElement = null;
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#info(java.io.PrintStream, java.lang.String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Just print some default
    printStream.println("not implemented");
  }
  
  
  /**
   * @return Returns the softwareElement.
   */
  protected final SoftwareElement getSoftwareElement()
  {
    return softwareElement;
  }
  
  /**
   * @return Returns the type.
   */
  protected final int getType()
  {
    return type;
  }
}
