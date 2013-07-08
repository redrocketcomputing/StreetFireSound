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
 * $Id: ClearCircularFilterCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

/**
 * @author stephen
 *
 */
public class ClearCircularFilterCommand extends LogFilterCommand
{

  /**
   * Constructor for ClearCircularFilterCommand.
   */
  public ClearCircularFilterCommand()
  {
  }

  /**
   * Constructor for DumpBufferFilerCommand.
   * @param instanceName The name of the particular circular log to dump
   */
  public ClearCircularFilterCommand(String instanceName)
  {
    // Construct super class
    super(instanceName);
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
        blf.clear();

        // All done
        return true;
      }
      // Looking for particular one
      else if (filter.getInstanceName().equals(instanceName))
      {
        // Invoke dump
        blf.clear();

        // All done
        return true;
      }
    }

    // Not found yet
    return false;
  }
}
