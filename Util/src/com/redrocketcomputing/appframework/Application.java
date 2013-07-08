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
 * $Id: Application.java,v 1.1 2005/02/22 03:54:49 stephen Exp $
 */

package com.redrocketcomputing.appframework;

import jargs.gnu.CmdLineParser;
import java.lang.reflect.Method;

import com.redrocketcomputing.appframework.event.EventDispatchService;
import com.redrocketcomputing.appframework.logger.LogService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.shell.Shell;
import com.redrocketcomputing.appframework.taskpool.TaskPoolService;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;

/**
 * @author stephen
 *
 */
public class Application implements Runnable
{
  private String[] args;

  /**
   * Application class. Users should inherit from this class.
   * @param configurationFileName The configuration properties to use for the application
   */
  public Application(String configurationFileName)
  {
    // Initialize configuration
    ConfigurationProperties.initialize(configurationFileName);
    
    // Add base services to the service manager
    ServiceManager.getInstance().install(LogService.class, "logger");
    ServiceManager.getInstance().install(TaskPoolService.class, "taskpool");
    ServiceManager.getInstance().install(EventDispatchService.class, "eventdispatcher");
  }

  /**
   * Application class. Users should inherit from this class.
   * @param args
   */
  public Application(String[] args)
  {
    // Save the arguments
    this.args = args;

    // Create command line parser
    CmdLineParser cmdLineParser = new CmdLineParser();
    CmdLineParser.Option configurationFile = cmdLineParser.addStringOption('c', "config");

    try
    {
      // Parse the command line
      cmdLineParser.parse(args);

      // Extract the configuration file name
      String configurationFileValue = (String)cmdLineParser.getOptionValue(configurationFile);
      if (configurationFileValue == null)
      {
        // No option, use default
        configurationFileValue = "application.properties";
      }

      // Initialize configuration
      ConfigurationProperties.initialize(configurationFileValue);
      
      // Add base services to the service manager
      ServiceManager.getInstance().install(LogService.class, "logger");
      ServiceManager.getInstance().install(TaskPoolService.class, "taskpool");
      ServiceManager.getInstance().install(EventDispatchService.class, "eventdispatcher");
    }
    catch (CmdLineParser.OptionException e)
    {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }
  
  /**
   * Application Loop the default behavoir is to run a command shell on System.in, System.out, System.err.
   * User application can override this to change the behavoir.
   */
  public void run()
  {
    // Create a new command line shell
    Shell shell = new Shell(System.in, System.out, System.err, ConfigurationProperties.getInstance().getProperties());

    // Execute the shell
    shell.run(args);
  }
}
