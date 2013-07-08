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
 * $Id: RemoteCommand.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.remoteshell.commands;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;
import com.redrocketcomputing.havi.system.remoteshell.RemoteShellService;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class RemoteCommand extends InternalShellCommand
{

  /**
   * Constructor for RemoteCommand.
   * @param parent
   */
  public RemoteCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "remote";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "manages the remote shell server";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "remote [-n server_name] -l | -k id_number | -t";
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
      CmdLineParser.Option info = parser.addBooleanOption('i', "info");
      CmdLineParser.Option list = parser.addBooleanOption('l', "list");
      CmdLineParser.Option kill = parser.addIntegerOption('k', "kill");
      CmdLineParser.Option name = parser.addStringOption('n', "name");
      CmdLineParser.Option terminate = parser.addBooleanOption('t', "terminate");

      parser.parse(args);

      // Extract the values entered for the various options -- if the options were not specified, the corresponding values will be null
      Boolean infoValue = (Boolean)parser.getOptionValue(info);
      Boolean listValue = (Boolean)parser.getOptionValue(list);
      Integer killValue = (Integer)parser.getOptionValue(kill);
      String nameValue = (String)parser.getOptionValue(name);
      Boolean terminateValue = (Boolean)parser.getOptionValue(terminate);

      // Try to lookup the service name in the properties
      String serverName = getVariable("remote.shell.server.name");

      // Check to see if a name option is present
      if (nameValue != null)
      {
        // Save as the server name
        serverName = nameValue;
      }

      // Make sure we have a valid name
      if (serverName == null)
      {
        // Log error
        err.println("no remote shell server specified\n" + getCommandUsage());

        // Return error
        return -1;
      }

      // Try to lookup the services
      Service server = ServiceManager.getInstance().find(serverName);
      if (server == null || !(server instanceof RemoteShellService))
      {
        // Log error
        err.println("remote shell server not found or wrong type was found for name: " + serverName);

        // Return error
        return -1;
      }

      // Cast to the correct type
      RemoteShellService remoteShellServer = (RemoteShellService)server;

      // Invoke the correct command
      if (infoValue != null)
      {
        // Display information
        remoteShellServer.info(out, new String[0]);
      }
      else if (listValue != null)
      {
        // List open connections
        remoteShellServer.list(out);
      }
      else if (killValue != null)
      {
        // Kill the specified connection
        remoteShellServer.kill(killValue.intValue());
      }
      else if (terminateValue != null)
      {
        // Terminate all open connections
        remoteShellServer.killAll();
      }
      else
      {
        // Display usage
        out.println(getCommandUsage());
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
  }
}
