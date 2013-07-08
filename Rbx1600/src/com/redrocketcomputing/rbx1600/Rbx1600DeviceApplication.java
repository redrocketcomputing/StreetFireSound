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
 * $Id: Rbx1600DeviceApplication.java,v 1.4 2005/03/16 04:24:32 stephen Exp $
 */

package com.redrocketcomputing.rbx1600;

import org.havi.system.types.HaviException;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.HaviApplication;
import com.redrocketcomputing.havi.system.amm.ApplicationModuleManager;
import com.redrocketcomputing.havi.system.cmm.slink.CmmSlink;
import com.redrocketcomputing.havi.system.dcmm.DcmManager;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceService;
import com.redrocketcomputing.rbx1600.devicecontroller.IrDevice;
import com.redrocketcomputing.rbx1600.devicecontroller.PowerSwitchDevice;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public class Rbx1600DeviceApplication extends HaviApplication
{
  /**
   * Constructor for Rbx1600DeviceApplication.
   * @param args
   */
  public Rbx1600DeviceApplication(String[] args)
  {
    // Construct Super class
    super(args);
    
    // Install base services
    ServiceManager.getInstance().install(CmmSlink.class, "cmmslink");
    ServiceManager.getInstance().install(MaintenanceService.class, "maintenance");
    ServiceManager.getInstance().install(PowerSwitchDevice.class, "powerswitch");
    ServiceManager.getInstance().install(IrDevice.class, "ir");
    
    // Log start up
    LoggerSingleton.logInfo(this.getClass(), "Rbx1600DeviceApplication", "initialized");
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Install Rbx1600Dcm
      DcmManager dcmm = (DcmManager)ServiceManager.getInstance().get(DcmManager.class);
      dcmm.install("file://opt/streetfire/rbx1600dcm.jar");
      
      // Install MediaManagerAm
      ApplicationModuleManager amm = (ApplicationModuleManager)ServiceManager.getInstance().get(ApplicationModuleManager.class);
      amm.install("file://opt/streetfire/mediamanageram.jar");
      
      // Wait forever
      synchronized(this)
      {
        wait();
      }
    }
    catch (InterruptedException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "run", e.toString());

      // Print stack trace
      e.printStackTrace();
    }
    catch (HaviException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "run", e.toString());

      // Print stack trace
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    // Create the Application
    Rbx1600DeviceApplication application = new Rbx1600DeviceApplication(args);

    // Execute run
    application.run();
  }
}
