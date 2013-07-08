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
 * $Id: ConfigurationProperties.java,v 1.2 2005/02/24 03:31:28 stephen Exp $
 */

package com.redrocketcomputing.util.configuration;

import java.io.*;
import java.util.*;

/**
 * Global configuration properties singleton.
 * @author Stephen Street
 * @version 1.0
 */
public class ConfigurationProperties
{
  private static ConfigurationProperties instance = null;
  private ObservableProperties properties = null;

	/**
	 * Initialize global configuration properties with specific set of properties
	 * @param properties Initial properties set
	 */
  public final static void initialize(String filename)
  {
    // Check to see if we already have initilized the properties
    if (instance != null)
    {
      throw new AlreadyInitializedException();
    }

    // Check paramters
    if (filename == null || filename.equals(""))
    {
      throw new IllegalArgumentException("bad filename: " + filename);
    }

    try
    {
      // Create file input stream
      FileInputStream inputStream = new FileInputStream(filename);
      
      // Create the instance
      instance = new ConfigurationProperties(inputStream);

      // Close the stream
      inputStream.close();
    }
    catch (FileNotFoundException e)
    {
      // Translate
      throw new LoadException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new LoadException(e.toString());
    }
  }

  /**
   * Initialize global configuration properties with specific set of properties
   * @param properties Initial properties set
   */
  public final static void initialize(InputStream is)
  {
    // Check to see if we already have initilized the properties
    if (instance != null)
    {
      throw new AlreadyInitializedException();
    }

    // Check paramters
    if (is == null)
    {
      throw new IllegalArgumentException("bad InputStream");
    }

    // Create the instance
    instance = new ConfigurationProperties(is);
  }

  /**
	 * Get the singleton instance of the ConfigurationProperties
	 * @return ConfigurationProperties
	 */
  public final static ConfigurationProperties getInstance()
  {
    // Check to make sure we are initialized
    if (instance == null)
    {
      // Opps! not initialize
      throw new NotInitializedException();
    }

    // Return the instance
    return instance;
  }

  public final Properties getProperties()
  {
    return properties.getProperties();
  }

  /**
   * Create a new component configurtion using a Class and and instance name
   * @return ComponentConfiguration The configuration object
   */
  public ComponentConfiguration getComponentConfiguration()
  {
    return new ComponentConfiguration(properties);
  }

	/**
	 * Create a new component configurtion using a Class and and instance name
	 * @param componentClass The compoenent class
	 * @param instanceName The component name
	 * @return ComponentConfiguration The configuration object
	 */
  public ComponentConfiguration getComponentConfiguration(Class componentClass, String instanceName)
  {
    return new ComponentConfiguration(properties, componentClass, instanceName);
  }

  /**
   * Create a new component configurtion using a key prefix for the root of the configuration
   * @param keyPrefix The prefix to use for all properties querys
   * @return ComponentConfiguration The configuration object
   */
  public ComponentConfiguration getComponentConfiguration(String keyPrefix)
  {
    return new ComponentConfiguration(properties, keyPrefix);
  }

  /**
   * Merge new Properties into the configuration properties by prepending the specified prefix.
   * PropetryName clashes are droped
   * @param prefix The String prepend
   * @param newProperties The Properties to add
   */
  public void merge(String prefix, Properties newProperties)
  {
    Enumeration enumeration = newProperties.propertyNames();
    while (enumeration.hasMoreElements())
    {
      // Extract property
      String newPropertyName = (String)enumeration.nextElement();
      
      // Add property
      properties.setProperty(prefix + '.' + newPropertyName, newProperties.getProperty(newPropertyName));
    }
  }
  
  /**
   * Prevent object creation.
   */
  private ConfigurationProperties(InputStream inputStream)
  {
     try
    {
      // Create properties
      properties = new ObservableProperties();

      // Load the properties file
      properties.load(inputStream);
    }
    catch (IOException e)
    {
      // Clear the properties
      properties = null;

      // Translate to ConfigurationPropertiesException
      throw new LoadException(e.toString());
    }
  }
}
