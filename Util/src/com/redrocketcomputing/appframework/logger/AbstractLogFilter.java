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
 * $Id: AbstractLogFilter.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.appframework.service.ServiceException;

/**
 * Base class of all log filter.  Provide support for maintaining the filter chain and requires the implementation of the
 * LogFilter interface
 *
 * @author stephen Jul 15, 2003
 * @version 1.0
 *
 */
abstract class AbstractLogFilter implements LogFilter
{
  private String instanceName;
  private LogFilter nextFilter;

	/**
	 * Constructor for AbstractLogFilter.
	 */
	public AbstractLogFilter(String instanceName, LogFilter nextFilter)
	{
    // Ensure the instance name is valid
    if (instanceName == null || instanceName.equalsIgnoreCase(""))
    {
      // Opps runtime exception
      throw new IllegalArgumentException("bad instance name");
    }

    // Save the parameters
    this.instanceName = instanceName;
    this.nextFilter = nextFilter;
	}

	/**
	 * @see com.redrocketcomputing.util.log.LogFilter#getInstanceName()
	 */
	public final String getInstanceName()
	{
    // Return this objects instance name
		return instanceName;
	}

  /**
   * @see com.redrocketcomputing.util.log.LogFilter#getNextFilter()
   */
  public final LogFilter getNextFilter()
  {
    // Return the next filter in the chain
    return nextFilter;
  }

	/**
	 * Initialize the log filter
	 */
  public void start()
  {
  }

  /**
   * Terminate the active log filter
   */
  public void terminate()
  {
  }

  /**
   * Execute a filter command using the Chain of Responsibly and Command patterns.
   * @param instanceName The target instanceName for the command
   * @param command The command to execute
   */
  public final void executeFilterCommand(LogFilterCommand command)
  {
    // Check for valid command
    if (command == null)
    {
      // Opps!, runtime exception
      throw new IllegalArgumentException("FilterCommand can not be null");
    }

    // Let the command have a look at us by dispatching using modified vistor pattern, if result is true we are all done
    if (!command.executeCommand(this))
    {
      // Check see if there is another filter in the chain
      if (nextFilter != null)
      {
        // Forward
        nextFilter.executeFilterCommand(command);
      }
    }
  }


	/**
	 * @see com.redrocketcomputing.util.log.LogFilter#log(int, String, StringBuffer)
	 */
	public abstract void log(String message);

  /**
   * Forward the log entry down the the chain.
   * @param message The message to log
   */
  protected final void forward(String message)
  {
    // Check for more filter chain
    if (nextFilter == null)
    {
      // All done
      return;
    }

    // Decend to next filter
    nextFilter.log(message);
  }
}
