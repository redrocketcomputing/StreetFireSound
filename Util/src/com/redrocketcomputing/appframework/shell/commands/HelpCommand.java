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
 * $Id: HelpCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell.commands;

import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class HelpCommand extends InternalShellCommand
{

  /**
   * Constructor for HelpCommand.
   * @param parent
   */
  public HelpCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "help";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "help display summary information about installed shell commands";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "help [name]";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Check to see if we have a command specified
    if (args.length >= 1)
    {
      // Get the command
      InternalShellCommand command = parent.getInternalCommand(args[0]);
      if (command == null)
      {
        // Not found
        err.println(args[0] + " not found");

        // Return an error
        return -1;
      }

      // Display the summary
      out.println(command.getCommandName() + " - " + command.getCommandSummary());

      // All done
      return 0;
    }

    // Loop though all command and display the summarys
    InternalShellCommand[] commands = parent.getInternalCommands();
    for (int i = 0; i < commands.length; i++)
    {
      // Display summary on standard output
      out.println(commands[i].getCommandName() + " - " + commands[i].getCommandSummary());
    }

    // All done
    return 0;
  }

}
