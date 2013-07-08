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
 * $Id: InstallCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell.commands;

import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

import jargs.gnu.CmdLineParser;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class InstallCommand extends InternalShellCommand
{

  /**
   * Constructor for InstallCommand.
   * @param parent
   */
  public InstallCommand(Shell parent)
  {
    super(parent);
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "install";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "installs a new shell command either locally or globally";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "install [-g] className";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    // Build the command line parser
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option globally = parser.addBooleanOption('g', "globally");

    try
    {
      // Parse the command line
      parser.parse(args);

      // Extract options
      Boolean globalFlag = (Boolean)parser.getOptionValue(globally);

      // Get the remain arguments
      String[] otherArgs = parser.getRemainingArgs();

      // Make sure we have a class path name
      if (otherArgs.length <= 0)
      {
        // Log err
        err.println("missing class name\n" + getCommandUsage());

        // Return error
        return -1;
      }

      // Check to see if we can find the class
      Class installClass = Class.forName(otherArgs[0]);

      // Check for globally flag
      if (globalFlag == Boolean.TRUE)
      {
        // Install globally first
        Shell.installGlobally(installClass);
      }

      // Always install locally
      parent.installLocally(installClass);

      // All done
      return 0;
    }
    catch (CmdLineParser.OptionException e)
    {
      // Log the error
      err.println("bad arguments\n" + getCommandUsage());

      // Return an error
      return -1;
    }
    catch (ClassNotFoundException e)
    {
      // Log the error
      err.println("can not find class: " + parser.getRemainingArgs()[0]);

      // All done
      return -1;
    }
  }
}
