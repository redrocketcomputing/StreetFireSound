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
 * $Id: StartFilterCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

/**
 * Executes a initialize command on a log filter.  If the instance name is null, the all filter are intialize
 *
 * @author stephen Jul 16, 2003
 * @version 1.0
 *
 */
class StartFilterCommand extends LogFilterCommand
{
  StartFilterCommand(String instanceName)
  {
    // Initialize super
    super(instanceName);
  }

	/**
	 * @see com.redrocketcomputing.util.log.LogFilterCommand#executeCommand(AbstractLogFilter)
	 */
	public boolean executeCommand(AbstractLogFilter filter)
	{
    // Check to see if the instance name match
    if (instanceName == null || filter.getInstanceName().equals(instanceName))
    {
      // Invoke initialize
      filter.start();

      // Check instance to see if we should continue
      return (instanceName != null);
    }

    // Not right instance
    return false;
	}

}
