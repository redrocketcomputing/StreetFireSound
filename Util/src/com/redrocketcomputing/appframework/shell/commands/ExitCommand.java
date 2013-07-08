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
 * $Id: ExitCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell.commands;

import jargs.gnu.CmdLineParser;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class ExitCommand extends InternalShellCommand
{

  /**
   * Constructor for ExitCommand.
   * @param parent The parent shell of this internal command
   */
  public ExitCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "exit";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "exit the current shell with the provided exit value";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "usage: exit {exitValue}";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#execute(String[])
   */
  public int run(String[] args)
  {
    try
    {
      int exitValue = 0;

      if (args.length >= 1)
      {
        // Try to parse the value into an integer
        exitValue = Integer.parseInt(args[0]);
      }

      // Tell the shell
      parent.exit(exitValue);

      // All done
      return 0;
    }
    catch (NumberFormatException e)
    {
      // Log error
      err.println("bad argument " + args[0] + '\n' + getCommandUsage());
      return -1;
    }
  }

}
