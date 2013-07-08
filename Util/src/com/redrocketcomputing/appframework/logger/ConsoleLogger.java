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
 * $Id: ConsoleLogger.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.PrintStream;

/**
 * Log Filter which send all message to the console
 *
 * @author stephen Jul 17, 2003
 * @version 1.0
 *
 */
public class ConsoleLogger extends AbstractLogFilter
{
  /**
   * Constructor for ConsoleLogger.
   * @param instanceName
   * @param nextFilter
   */
  public ConsoleLogger(String instanceName, LogFilter nextFilter)
  {
    super(instanceName, nextFilter);
  }

  /**
   * @see com.redrocketcomputing.util.log.Logger#log(String)
   */
  public void log(String message)
  {
    // Check for ERROR, WARNING OR FATAL
    PrintStream printStream = System.out;
    if (message.indexOf("FATAL") != -1 || message.indexOf("ERROR") != -1 || message.indexOf("WARNING") != -1)
    {
      printStream = System.err;
    }

    // Send message to the console
    printStream.println(message);

    // Forward down the chain
    super.forward(message);
  }
}
