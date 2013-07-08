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
 * $Id: WatchCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger.commands;

import java.io.IOException;

import com.redrocketcomputing.appframework.logger.AddPrintStreamFilterCommand;
import com.redrocketcomputing.appframework.logger.LogService;
import com.redrocketcomputing.appframework.logger.RemovePrintStreamFilterCommand;
import com.redrocketcomputing.appframework.shell.InternalShellCommand;
import com.redrocketcomputing.appframework.shell.Shell;

class WatchCommand extends InternalShellCommand
{
  private LogService logger;

  /**
   * Constructor for WatchCommand.
   * @param parent
   */
  public WatchCommand(Shell parent, LogService logger)
  {
    super(parent);

    // Check the parameter
    if (logger == null)
    {
      // bad
      throw new IllegalArgumentException("LogService is null");
    }

    // Save the parameter
    this.logger = logger;
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandName()
   */
  public String getCommandName()
  {
    return "watch";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandSummary()
   */
  public String getCommandSummary()
  {
    return "Watches the log service until a the return key is pressed";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.InternalShellCommand#getCommandUsage()
   */
  public String getCommandUsage()
  {
    return "watch";
  }

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#run(String[])
   */
  public int run(String[] args)
  {
    try
    {
      // Add our print stream to the print stream logger
      AddPrintStreamFilterCommand addCommand = new AddPrintStreamFilterCommand(out);
      logger.execute(addCommand);
      if (!addCommand.isSuccessful())
      {
        out.println("could not find print stream logger");
        return 1;
      }

      // Wait for a keystroke
      in.read();

      // All done
      return 0;
    }
    catch (IOException e)
    {
      // Print exception and exit
      out.println(e.toString());
      return 1;
    }
    finally
    {
      // Remove the print stream
      logger.execute(new RemovePrintStreamFilterCommand(out));
    }
  }

}
