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
 * $Id: ObservableProperties.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.util.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * Decorator class adding the observable pattern to changes in the configuration properties.
 *
 * @author stephen Jul 14, 2003
 * @version 1.0
 *
 */
class ObservableProperties extends Observable
{
  private Properties properties = null;

	/**
	 * Constructor for ObservableProperties.
	 */
	public ObservableProperties()
	{
    // Initialize super class
		super();

    // Create empty properties
    this.properties = new Properties();
	}


	/**
	 * @see java.util.Properties#getProperty(String, String)
	 */
	public final String getProperty(String name, String defaultValue)
	{
		return properties.getProperty(name, defaultValue);
	}

	/**
	 * @see java.util.Properties#getProperty(String)
	 */
	public final String getProperty(String name)
	{
		return properties.getProperty(name);
	}

	/**
	 * @see java.util.Properties#list(PrintStream)
	 */
	public final void list(PrintStream out)
	{
		properties.list(out);
	}

	/**
	 * @see java.util.Properties#list(PrintWriter)
	 */
	public final void list(PrintWriter writer)
	{
		properties.list(writer);
	}

	/**
	 * @see java.util.Properties#load(InputStream)
	 */
	public final void load(InputStream in) throws IOException
	{
		properties.load(in);
	}

	/**
	 * @see java.util.Properties#propertyNames()
	 */
	public final Enumeration propertyNames()
	{
		return properties.propertyNames();
	}

	/**
	 * @see java.util.Properties#setProperty(String, String)
	 */
	public final Object setProperty(String name, String value)
	{
    // Change the value
    Object oldValue = properties.setProperty(name, value);

    // Notfiy observers
    notifyObservers(name);

		return oldValue;
	}

	/**
	 * @see java.util.Properties#store(OutputStream, String)
	 */
	public final void store(OutputStream out, String comment) throws IOException
	{
		properties.store(out, comment);
	}

  /**
   * Returns the properties.
   * @return Properties
   */
  public final Properties getProperties()
  {
    return properties;
  }

}
