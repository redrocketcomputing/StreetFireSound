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
 * $Id: SetLevelCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

public class SetLevelCommand extends LogFilterCommand
{
  private boolean successful = false;
  private int level;

  /**
   * Constructor for SetLevelCommand.
   * @param The level to set
   */
  public SetLevelCommand(int level)
  {
    // Constuct super class
    super();

    // Save the level
    this.level = level;
  }

  /**
   * Constructor for SetLevelCommand.
   * @param instanceName The particular instance to set the level
   * @param level The level to set
   */
  public SetLevelCommand(String instanceName, int level)
  {
    // Constuct super class
    super(instanceName);

    // Save the level
    this.level = level;
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.LogFilterCommand#executeCommand(AbstractLogFilter)
   */
  public boolean executeCommand(AbstractLogFilter filter)
  {
    // Check type
    if (filter instanceof LogLevelFilter)
    {
      // Cast it up
      LogLevelFilter llf = (LogLevelFilter)filter;

      // If no instance name then stop at the first one
      if (instanceName == null)
      {
        // Mark as successfull and get the level
        successful = true;
        llf.setLevel(level);

        // All done
        return true;
      }
      // Looking for particular one
      else if (filter.getInstanceName().equals(instanceName))
      {
        // Mark as successfull and get the level
        successful = true;
        llf.setLevel(level);

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
