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
 * $Id: UnsetCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell.commands;

import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class UnsetCommand extends InternalShellCommand
{

  /**
   * Constructor for UnsetCommand.
   * @param parent
   */
  public UnsetCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "unset";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "unset or deletes the specified environment variable";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "unset name";
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

    // Set the property value
    unsetVariable(args[0]);

    // All good
    return 0;
  }

}
