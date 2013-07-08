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
 * $Id: HaviApplication.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */
package com.redrocketcomputing.havi.system;

import com.redrocketcomputing.appframework.Application;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.amm.ApplicationModuleManager;
import com.redrocketcomputing.havi.system.cmm.ip.CmmIp;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.dcmm.DcmManager;
import com.redrocketcomputing.havi.system.em.EventManager;
import com.redrocketcomputing.havi.system.ms.MessagingSystem;
import com.redrocketcomputing.havi.system.remoteshell.RemoteShellService;
import com.redrocketcomputing.havi.system.rg.Registry;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HaviApplication extends Application
{
  
  public HaviApplication(String configurationFileName)
  {
    // Construct Super class
    super(configurationFileName);

    // Install base HAVI services
    ServiceManager.getInstance().install(Gadp.class, "gadp");
    ServiceManager.getInstance().install(RemoteShellService.class, "rs");
    ServiceManager.getInstance().install(MessagingSystem.class, "ms");
    ServiceManager.getInstance().install(EventManager.class, "em");
    ServiceManager.getInstance().install(Registry.class, "rg");
    ServiceManager.getInstance().install(CmmIp.class, "cmm");
    ServiceManager.getInstance().install(DcmManager.class, "dcmm");
    ServiceManager.getInstance().install(ApplicationModuleManager.class, "amm");
    
    // Log start up
    LoggerSingleton.logInfo(this.getClass(), "HaviApplication", "initialized");
  }
  
  /**
   * @param args
   */
  public HaviApplication(String[] args)
  {
    // Construct Super class
    super(args);
    
    // Install base HAVI services
    ServiceManager.getInstance().install(Gadp.class, "gadp");
    ServiceManager.getInstance().install(RemoteShellService.class, "rs");
    ServiceManager.getInstance().install(MessagingSystem.class, "ms");
    ServiceManager.getInstance().install(EventManager.class, "em");
    ServiceManager.getInstance().install(Registry.class, "rg");
    ServiceManager.getInstance().install(CmmIp.class, "cmm");
    ServiceManager.getInstance().install(DcmManager.class, "dcmm");
    ServiceManager.getInstance().install(ApplicationModuleManager.class, "amm");
    
    // Log start up
    LoggerSingleton.logInfo(this.getClass(), "HaviApplication", "initialized");
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
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
  }

  public static void main(String[] args)
  {
    // Create the Application
    HaviApplication application = new HaviApplication(args);

    // Execute run
    application.run();
  }
}
