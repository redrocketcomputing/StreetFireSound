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
 * $Id: ServiceCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.service.commands;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class ServiceCommand extends InternalShellCommand
{

  /**
   * Constructor for ServiceCommand.
   * @param parent
   */
  public ServiceCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "service";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "Manager application services";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "service {-l,--list} | {-i,--info} <service_id> | {-s,--start} <service_id> | {-t,--terminate} <service_id> | {-a,--add} <instance_name> <class name>";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    try
    {
      // Create command line parser
      CmdLineParser parser = new CmdLineParser();
      CmdLineParser.Option list = parser.addBooleanOption('l',"list");
      CmdLineParser.Option info = parser.addIntegerOption('i', "info");
      CmdLineParser.Option start = parser.addIntegerOption('s', "start");
      CmdLineParser.Option terminate = parser.addIntegerOption('t', "terminate");
      CmdLineParser.Option add = parser.addBooleanOption('a', "add");

      // Parse the command line arguments
      parser.parse(args);

      // Extract the values entered for the various options -- if the options were not specified, the corresponding values will be null
      Boolean listValue = (Boolean)parser.getOptionValue(list);
      Integer infoValue = (Integer)parser.getOptionValue(info);
      Integer startValue = (Integer)parser.getOptionValue(start);
      Integer terminateValue = (Integer)parser.getOptionValue(terminate);
      Boolean addValue = (Boolean)parser.getOptionValue(add);

      // Check for list
      if (listValue != null)
      {
        // Info command on the service manager
        ServiceManager.getInstance().info(out, new String[0]);
      }

      // Check for add command
      else if (addValue != null)
      {
        // Check to make sure we have at least two remain arguments
        String[] remainingArgs = parser.getRemainingArgs();
        if (remainingArgs.length < 2)
        {
          // Report error
          err.println("missing arguments: " + getCommandUsage());

          // All done
          return -1;
        }

        // Add service
        ServiceManager.getInstance().install(Class.forName(remainingArgs[1]), remainingArgs[0]);
      }

      // Check for info command
      else if (infoValue != null)
      {
        // Lookup service
        Service service = ServiceManager.getInstance().get(infoValue.intValue());
        if (service == null)
        {
          // Report error
          err.println("service " + infoValue.intValue() + " not found");

          // All done
          return -1;
        }

        // Execute info command on service
        service.info(out, new String[0]);
      }

      // Check for start
      else if (startValue != null)
      {
        // Lookup service
        Service service = ServiceManager.getInstance().get(startValue.intValue());
        if (service == null)
        {
          // Report error
          err.println("service " + startValue.intValue() + " not found");

          // All done
          return -1;
        }

        // Execute info command on service
        service.start();
      }

      // Check for start
      else if (terminateValue != null)
      {
        // Lookup service
        Service service = ServiceManager.getInstance().get(terminateValue.intValue());
        if (service == null)
        {
          // Report error
          err.println("service " + terminateValue.intValue() + " not found");

          // All done
          return -1;
        }

        // Execute info command on service
        service.terminate();
      }

      // No option specified
      else
      {
        // Report error
        err.println(getCommandUsage());

        // All done
        return -1;
      }

      // All done
      return 0;
    }
    catch (IllegalOptionValueException e)
    {
      // Log error
      err.println("illegal option value\n" + getCommandUsage());

      // Return error
      return -1;
    }
    catch (UnknownOptionException e)
    {
      // Log error
      err.println("unknown option\n" + getCommandUsage());

      // Return error
      return -1;
    }
    catch (ServiceException e)
    {
      // Log error
      err.println("service exception: " + e.getMessage());

      // Return error
      return -1;
    }
    catch (ClassNotFoundException e)
    {
      // Log error
      err.println("ClassNotFoundException: " + e.getMessage());

      // Return error
      return -1;
    }
  }

}
