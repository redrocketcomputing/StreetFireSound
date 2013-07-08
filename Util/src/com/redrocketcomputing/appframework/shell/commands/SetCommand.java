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
 * $Id: SetCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell.commands;

import java.util.StringTokenizer;

import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class SetCommand extends InternalShellCommand
{

  /**
   * Constructor for SetCommand.
   * @param parent
   */
  public SetCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "set";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "set the specificied enviroment variable to the provided value";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "set name=value";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Display usage if we have no arguments
    if (args.length <= 0)
    {
      // Display usage
      err.println("missing argument\n" + getCommandUsage());

      // Return error
      return -1;
    }

    // Try to extract name and value
    int seperatorPosition = args[0].indexOf('=');
    if (seperatorPosition == -1)
    {
      // Display usage
      out.println("bad format: " + args[0] + '\n' + getCommandUsage());

      // Return error
      return -1;
    }

    // Extract the name and value
    String name = args[0].substring(0, seperatorPosition);
    String value = args[0].substring(seperatorPosition + 1);

    // Set the property value
    setVariable(name, value);

    // All good
    return 0;
  }

}
