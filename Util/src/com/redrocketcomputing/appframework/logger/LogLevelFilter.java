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
 * $Id: LogLevelFilter.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.util.Observable;
import java.util.Observer;

import com.redrocketcomputing.util.configuration.*;
import com.redrocketcomputing.util.concurrent.Gate;

/**
 * Description:
 *
 * @author stephen Jul 15, 2003
 * @version 1.0
 *
 */
public class LogLevelFilter extends AbstractLogFilter implements Observer
{
  private final static String[] LEVEL_NAMES = {"FATAL", "ERROR", "WARNING", "INFO", "DEBUGC", "DEBUGF"};

  private int level;
  private ComponentConfiguration configuration;

	/**
	 * Constructor for LogLevelFilter.
	 * @param instanceName
	 * @param nextFilter
	 */
	public LogLevelFilter(String instanceName, LogFilter nextFilter)
	{
    // Initialize super
		super(instanceName, nextFilter);

    // Create a configuration interface
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);

    // Bind to the configuration properties
    configuration.addObserver(this);
	}

	/**
	 * Initialize from the global configuration properties
	 */
  public void start()
  {
    // Get current level
    level = configuration.getIntProperty("report", 6);
  }

	/**
	 * @see com.redrocketcomputing.util.log.LogFilter#log(int, String, StringBuffer)
	 */
	public void log(String message)
	{
    // Extract text level
    String levelText = message.substring(0, message.indexOf(' '));

    // Convert to level
    int value;
    for (value = 0; value < LEVEL_NAMES.length; value++)
    {
      // Check for match
      if (levelText.equals(LEVEL_NAMES[value]))
      {
        break;
      }
    }

    // Check to see if specificed level is less than the current level
    if (value <= this.level)
    {
      // Yes log this message, forward it down the change
      super.forward(message);
    }
	}

	/**
	 * Set the current logging level
	 * @param level The new logging level
	 */
  public void setLevel(int level)
  {
    // Set the level
    this.level = level;
  }

	/**
	 * Return the current logging level
	 * @return int The current logging level
	 */
  public int getLevel()
  {
    return level;
  }

	/**
   * Invoked when the configuration properties change
	 * @see java.util.Observer#update(Observable, Object)
	 */
	public void update(Observable observable, Object data)
	{
    // Configuration properties have changed, re-initialize
    start();
	}
}
