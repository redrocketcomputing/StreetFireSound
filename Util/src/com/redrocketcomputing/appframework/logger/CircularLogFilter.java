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
 * $Id: CircularLogFilter.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;

import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;

public class CircularLogFilter extends AbstractLogFilter
{
  private final static int DEFAULT_BUFFER_SIZE = 512;

  private ComponentConfiguration configuration;
  private String buffer[];
  private int head;
  private int tail;
  private int size;

  /**
   * Constructor for CircularLogFilter.
   * @param instanceName The name of this filter. Used to execute commands against this filter
   * @param nextFilter Reference to the next filter in the filter stack
   */
  public CircularLogFilter(String instanceName, LogFilter nextFilter)
  {
    // Forward to the super class
    super(instanceName, nextFilter);

    // Create a configuration interface
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);

    // Create and initialize buffer
    size = configuration.getIntProperty("size", DEFAULT_BUFFER_SIZE);
    buffer = new String[size];
    head = 0;
    tail = 0;
  }

  /**
   * Write the current buffer to a PrintStream
   * @param ps The print stream to write the buffer to
   */
  public synchronized void dump(PrintStream ps)
  {
    // Loop through the buffer
    int current = tail;
    while (current != head)
    {
      // Print the current item
      ps.println(buffer[current & (size -1)]);

      // Move to next entry
      current++;
    }
  }

  /**
   * Clear the log buffer
   */
  public synchronized void clear()
  {
    head = tail;
  }

  /**
   * @see com.redrocketcomputing.util.log.Logger#log(String)
   */
  public synchronized void log(String message)
  {
    // Check for overflow
    if (head + 1 == tail)
    {
      // Remove on entry
      tail++;
    }

    // Add new entry
    buffer[head & (size - 1)] = message;
    head++;

    // Forward down the chain
    super.forward(message);
  }
}
