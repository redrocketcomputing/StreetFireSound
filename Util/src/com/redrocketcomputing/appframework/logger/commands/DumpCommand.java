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
 * $Id: DumpCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger.commands;

import com.redrocketcomputing.appframework.logger.DumpCircularFilterCommand;
import com.redrocketcomputing.appframework.logger.LogService;
import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

class DumpCommand extends InternalShellCommand
{
  private LogService logger;

  /**
   * Constructor for InfoCommand.
   * @param parent The parent shell for this command
   * @param logger The logger service to use
   */
  public DumpCommand(Shell parent, LogService logger)
  {
    // Construct parent
    super(parent);

    // Check parameters
    if (logger == null)
    {
      // Bad ness
      throw new IllegalArgumentException("LogService is null");
    }

    // Save the logger
    this.logger = logger;
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "dump";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "Dumps the buffered log contents";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "dump";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Execute the dump comamnd
    logger.execute(new DumpCircularFilterCommand(out));

    return 0;
  }

}
