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
 * $Id: ApplicationLogFilter.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;

public class ApplicationLogFilter extends AbstractLogFilter
{
  private final static String DEFAULT_APP_LOG_FILE = "/tmp/applog";
  private final static String DEFAULT_APP_LOG_FACILITY = "user";
  private final static String DEFAULT_APP_LOG_TAG = "rbx1600";

  private ComponentConfiguration configuration;
  private String tag;
  private String facility;
  private String fileName;
  private Map priorityMap = new TreeMap();
  private PrintStream ps;

  /**
   * Constructor for ApplicationLogFilter.
   * @param instanceName The instance name for the filter
   * @param nextFilter The next log filter in the stack
   */
  public ApplicationLogFilter(String instanceName, LogFilter nextFilter)
  {
    super(instanceName, nextFilter);

    // Create the configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.AbstractLogFilter#start()
   */
  public void start()
  {
    try
    {
      // Get the configuration parameters
      fileName = configuration.getProperty("app.log.file", DEFAULT_APP_LOG_FILE);
      facility = configuration.getProperty("app.log.facility", DEFAULT_APP_LOG_FACILITY);
      tag = configuration.getProperty("app.log.tag", DEFAULT_APP_LOG_TAG);

      // Build facility map
      priorityMap.put("fatal", tag + ' ' + facility + ".panic - ");
      priorityMap.put("error", tag + ' ' + facility + ".error - ");
      priorityMap.put("warning", tag + ' ' + facility + ".warning - ");
      priorityMap.put("info", tag + ' ' + facility + ".info - ");
      priorityMap.put("debugc", tag + ' ' + facility + ".debug - ");
      priorityMap.put("debugf", tag + ' ' + facility + ".debug - ");

      // Open the app log file and wrap with a print stream
      FileOutputStream fos = new FileOutputStream(fileName);
      ps = new PrintStream(fos);
    }
    catch (IOException e)
    {
      // Umm,
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.AbstractLogFilter#terminate()
   */
  public void terminate()
  {
    // Clear map
    priorityMap.clear();

    // Close the print stream
    ps.close();
  }

  /**
   * @see com.redrocketcomputing.appframework.logger.Logger#log(String)
   */
  public void log(String message)
  {
    // Extract level
    String levelText = message.substring(0, message.indexOf(' ')).trim().toLowerCase();

    // Lookup logger
    String header = (String)priorityMap.get(levelText);
    if (header != null)
    {
      // Log the message
      ps.println(header + message.substring(message.indexOf('-') + 2));
    }

    // Forward down the chain
    super.forward(message);
  }
}
