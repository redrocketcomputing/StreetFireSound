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
 * $Id: LoggerShell.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger.commands;

import java.util.Properties;

import com.redrocketcomputing.appframework.logger.LogService;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * @author stephen
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LoggerShell extends InternalShellCommand
{

  /**
   * Constructor for LoggerShell.
   * @param parent
   */
  public LoggerShell(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "logger";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "launchs a logger command shell";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "logger";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Get the logger service
    LogService logger = (LogService)ServiceManager.getInstance().find(LogService.class);

    // Create new shell
    Shell shell = new Shell(in, out, err, (Properties)getEnvironment().clone());
    shell.setVariable("shell.prompt", "logger> ");

    // Create local commands
    shell.installLocally(new InfoCommand(shell, logger));
    shell.installLocally(new DumpCommand(shell, logger));
    shell.installLocally(new ClearCommand(shell, logger));
    shell.installLocally(new LevelCommand(shell, logger));
    shell.installLocally(new WatchCommand(shell, logger));
    shell.installLocally(new RemoteCommand(shell, logger));

    // Run the shell
    shell.run(args);

    // All done
    return 0;
  }
}
