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
 * $Id: PrintStreamLogger.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;

public class PrintStreamLogger extends AbstractLogFilter
{
  private Set printStreamSet = new HashSet();
  private List openedStreamList = new ArrayList();
  private ComponentConfiguration configuration;

  /**
   * Constructor for PrintStreamLogger.
   * @param instanceName The name of this print stream logger
   * @param nextFilter The next filter in the stack
   */
  public PrintStreamLogger(String instanceName, LogFilter nextFilter)
  {
    super(instanceName, nextFilter);

    // Create a configuration interface
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
  }

  /**
   * Add a print stream to the logger
   * @param ps The print stream to add
   */
  public synchronized void addStream(PrintStream ps)
  {
    // Check the parameters
    if (ps == null)
    {
      // bad
      throw new IllegalArgumentException("PrintStream is null");
    }

    // Add it
    printStreamSet.add(ps);
  }

  /**
   * Remove a print stream from the logger
   * @param ps The print stream to remove
   */
  public synchronized void removeStream(PrintStream ps)
  {
    // Check the parameters
    if (ps == null)
    {
      // bad
      throw new IllegalArgumentException("PrintStream is null");
    }

    // Remove it
    printStreamSet.remove(ps);
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.Logger#log(String)
   */
  public synchronized void log(String message)
  {
    // Loop through the available stream and write the message to them
    for (Iterator iterator = printStreamSet.iterator(); iterator.hasNext();)
    {
      // Get the stream
      PrintStream element = (PrintStream) iterator.next();

      // Write the message
      element.println(message);
    }

    // Forward down the chain
    super.forward(message);
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.AbstractLogFilter#start()
   */
  public void start()
  {
    // Get any files from the configuration
    String fileName;
    int i = 0;
    while ((fileName = configuration.getProperty("file." + Integer.toString(i++))) != null)
    {
      try
      {
        // Check for standard out
        if (fileName.trim().equals("stdout"))
        {
          // Add it to the set
          addStream(System.out);
        }
        else
        {
          // Create file output stream
          FileOutputStream fos = new FileOutputStream(fileName.trim());

          // Wrap it in a print stream
          PrintStream ps = new PrintStream(fos);

          // Add it to the set and the open list
          addStream(ps);
          openedStreamList.add(ps);
        }
      }
      catch (FileNotFoundException e)
      {
        System.out.println("PrintStreamLogger.start: " + e.toString());
      }
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.AbstractLogFilter#terminate()
   */
  public void terminate()
  {
    // Loop through the opened stream list and remove and close
    for (Iterator iterator = openedStreamList.iterator(); iterator.hasNext();)
    {
      // Extract the print stream element
      PrintStream element = (PrintStream) iterator.next();

      // Remove it
      removeStream(element);

      // Close it
      element.close();
    }
  }

}
