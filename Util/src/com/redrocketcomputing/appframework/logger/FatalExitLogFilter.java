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
 * $Id: FatalExitLogFilter.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

/**
 * Halt the VM is a log message contains the string FATAL
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class FatalExitLogFilter extends AbstractLogFilter
{

  /**
   * Constructor for FatalExitLogFilter.
   * @param instanceName
   * @param nextFilter
   */
  public FatalExitLogFilter(String instanceName, LogFilter nextFilter)
  {
    super(instanceName, nextFilter);
  }

  /**
   * @see com.redrocketcomputing.util.log.Logger#log(String)
   */
  public void log(String message)
  {
    // Check for fatal error
    if (message.indexOf("FATAL") != -1)
    {
      // Make sure we log the message
      System.err.println(message);
      System.err.flush();

      // Force a thread dump
      //Runtime.getRuntime().halt(-1);
      System.exit(-1);
    }

    // Foward on
    super.forward(message);
  }
}
