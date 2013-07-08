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
 * $Id: LevelCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger.commands;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import com.redrocketcomputing.appframework.logger.GetLevelCommand;
import com.redrocketcomputing.appframework.logger.LogService;
import com.redrocketcomputing.appframework.logger.SetLevelCommand;
import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

class LevelCommand extends InternalShellCommand
{
  private LogService logger;

  /**
   * Constructor for LevelCommand
   * @param parent The parent shell
   * @param logger The logger service to set the level for
   */
  public LevelCommand(Shell parent, LogService logger)
  {
    super(parent);

    // Check the parameters
    if (logger == null)
    {
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
    return "level";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "displays or set the current log level";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "level [0-6]";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Check for no agruments
    if (args.length == 0)
    {
      // Get the level value
      GetLevelCommand levelCommand = new GetLevelCommand();
      logger.execute(levelCommand);
      if (!levelCommand.isSuccessful())
      {
        // Report not found and exit
        out.println("level logger not found");
        return 1;
      }

      // Report the state
      out.println("log level is " +  levelCommand.getLevel());
    }
    else
    {
      try
      {
        // Build integer for log level
        int newLevel = Integer.parseInt(args[0]);

        // Set the level
        SetLevelCommand levelCommand = new SetLevelCommand(newLevel);
        logger.execute(levelCommand);
        if (!levelCommand.isSuccessful())
        {
          // Report not found and exit
          out.println("level logger not found");
          return 1;
        }
      }
      catch (NumberFormatException e)
      {
        // Report and exit
        out.println("bad option value");
        return 1;
      }
    }

    // All good
    return 0;
  }
}
