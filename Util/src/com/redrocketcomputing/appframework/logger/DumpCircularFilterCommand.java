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
 * $Id: DumpCircularFilterCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.PrintStream;

public class DumpCircularFilterCommand extends LogFilterCommand
{
  private PrintStream ps;

  /**
   * Constructor for DumpBufferFilerCommand.
   * @param ps The PrintStream to dump the buffer to
   */
  public DumpCircularFilterCommand(PrintStream ps)
  {
    // Check parameters
    if (ps == null)
    {
      throw new IllegalArgumentException("PrintStream is null");
    }

    // Save the parameter
    this.ps = ps;
  }

  /**
   * Constructor for DumpBufferFilerCommand.
   * @param instanceName The name of the particular circular log to dump
   * @param ps The PrintStream to dump the buffer to
   */
  public DumpCircularFilterCommand(String instanceName, PrintStream ps)
  {
    // Construct super class
    super(instanceName);

    // Check parameters
    if (ps == null)
    {
      throw new IllegalArgumentException("PrintStream is null");
    }

    // Save the parameter
    this.ps = ps;
  }


  /**
   * @see com.redrocketcomputing.util.log.LogFilterCommand#executeCommand(AbstractLogFilter)
   */
  public boolean executeCommand(AbstractLogFilter filter)
  {
    // Check type not instance name
    if (filter instanceof CircularLogFilter)
    {
      // Cast it up
      CircularLogFilter blf = (CircularLogFilter)filter;

      // If no instance name then stop at the first one
      if (instanceName == null)
      {
        // Invoke dump
        blf.dump(ps);

        // All done
        return true;
      }
      // Looking for particular one
      else if (filter.getInstanceName().equals(instanceName))
      {
        // Invoke dump
        blf.dump(ps);

        // All done
        return true;
      }
    }

    // Not found yet
    return false;

  }
}
