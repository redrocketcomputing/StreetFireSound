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
 * $Id: LogFilter.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;

/**
 * Interface descibing the mimimum functionality a logger filter component. The interface is used as part of
 * a decorator and filter pattern.
 *
 * @author Stephen Street Jul 15, 2003
 * @version 1.0
 *
 */
public interface LogFilter extends Logger
{
	/**
	 * Return the configured instance name of the filter
	 * @return String The instance name
	 */
  String getInstanceName();

	/**
	 * Return the next log filter in the chain otherwise null
	 * @return LogFilter The next filter or null
	 */
  LogFilter getNextFilter();

	/**
	 * Execute a filter command using the Chain of Responsibly and Command patterns.
   * @param instanceName The target instanceName for the command
	 * @param command The command to execute
	 */
  void executeFilterCommand(LogFilterCommand command);

}
