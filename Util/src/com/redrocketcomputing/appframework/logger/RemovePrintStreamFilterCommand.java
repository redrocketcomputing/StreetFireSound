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
 * $Id: RemovePrintStreamFilterCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.PrintStream;

public class RemovePrintStreamFilterCommand extends LogFilterCommand
{

  private PrintStream ps;
  private boolean successful = false;

  /**
   * Constructor for RemovePrintStreamFilterCommand.
   * @param The PrintStream to remove from the filter
   */
  public RemovePrintStreamFilterCommand(PrintStream ps)
  {
    super();

    // Check the parameter
    if (ps == null)
    {
      // Bad
      throw new IllegalArgumentException("PrintStream is null");
    }

    // Save the parameter
    this.ps = ps;
  }

  /**
   * Constructor for removePrintStreamFilterCommand.
   * @param instanceName The name of the filter to remove the PrintStream
   * @param The PrintStream to remove from the filter
   */
  public RemovePrintStreamFilterCommand(String instanceName)
  {
    super(instanceName);
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.LogFilterCommand#executeCommand(AbstractLogFilter)
   */
  public boolean executeCommand(AbstractLogFilter filter)
  {
    // Check type not instance name
    if (filter instanceof PrintStreamLogger)
    {
      // Cast it up
      PrintStreamLogger psl = (PrintStreamLogger)filter;

      // If no instance name then stop at the first one
      if (instanceName == null)
      {
        // Invoke remove
        psl.removeStream(ps);

        // Mark as successful
        successful = true;

        // All done
        return true;
      }
      // Looking for particular one
      else if (filter.getInstanceName().equals(instanceName))
      {
        // Invoke remove
        psl.removeStream(ps);

        // Mark as successful
        successful = true;

        // All done
        return true;
      }
    }

    // Not found yet
    return false;
  }

  /**
   * Returns the successful.
   * @return boolean
   */
  public boolean isSuccessful()
  {
    return successful;
  }
}
