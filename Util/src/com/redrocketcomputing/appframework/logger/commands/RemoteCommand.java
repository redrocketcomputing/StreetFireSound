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
 * $Id: RemoteCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger.commands;

import com.redrocketcomputing.appframework.logger.GetRemoteStateCommand;
import com.redrocketcomputing.appframework.logger.LogService;
import com.redrocketcomputing.appframework.logger.SetEnableRemoteFilterCommand;
import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * @author stephen
 *
 */
public class RemoteCommand extends InternalShellCommand
{
  private LogService logger;

  /**
   * Constructor for RemoteCommand.
   * @param parent The parent command shell
   * @param logger The logger service to use
   */
  public RemoteCommand(Shell parent,  LogService logger)
  {
    // Construct super class
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
    return "remote";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "display, enable or display remote logging";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "remote [on | off]";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Check for at least one argument
    if (args.length == 0)
    {
      // Get the remote state
      GetRemoteStateCommand remoteState = new GetRemoteStateCommand();
      logger.execute(remoteState);
      if (!remoteState.isSuccessful())
      {
        // Remote not found and exit
        out.println("remote logger not found");
        return 1;
      }

      // Report the state
      out.println("remote logging " + (remoteState.isEnabled() ? "enabled" : "disabled") + " to remote address " + remoteState.getRemoteAddress());
    }
    else if (args[0].trim().equals("on"))
    {
      // Set enabled
      SetEnableRemoteFilterCommand enable = new SetEnableRemoteFilterCommand(true);
      logger.execute(enable);
    }
    else if (args[0].trim().equals("off"))
    {
      // Set disabled
      SetEnableRemoteFilterCommand enable = new SetEnableRemoteFilterCommand(false);
      logger.execute(enable);
    }
    else
    {
      // Badness
      out.println("unknown options");
      return 1;
    }

    // All good
    return 0;
  }

}
