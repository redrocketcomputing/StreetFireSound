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
 * $Id: SystemShellCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell.commands;

import java.util.Properties;

import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 */
public class SystemShellCommand extends InternalShellCommand
{

  /**
   * Constructor for SystemShellCommand.
   * @param parent The parent shell
   */
  public SystemShellCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "system";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "launches a system shell managing the VM";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "system";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Create new shell
    Shell shell = new Shell(in, out, err, (Properties)getEnvironment().clone());
    shell.setVariable("shell.prompt", "system> ");

    // Create local commands
    shell.installLocally(new SystemPropertiesCommand(shell));
    shell.installLocally(new GarbageCollectorCommand(shell));
    shell.installLocally(new FreeMemoryCommand(shell));
    shell.installLocally(new TotalMemoryCommand(shell));
    shell.installLocally(new RebootCommand(shell));

    // Run the shell
    shell.run(args);

    // All done
    return 0;
  }
}