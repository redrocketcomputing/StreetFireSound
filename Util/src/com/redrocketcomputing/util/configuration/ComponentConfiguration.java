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
 * $Id: ComponentConfiguration.java,v 1.2 2005/02/23 19:58:32 stephen Exp $
 */

package com.redrocketcomputing.util.configuration;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

/**
 * ConponentConfiguration provide a properties interface for a particular component.  The
 * properties items are prefixed with the class name and an instance name
 * (e.g. "com.redrocketcomputing.util.logger.consoleLogger.primary.queue.size=10")
 *
 * @author stephen Jul 14, 2003
 * @version 1.0
 *
 */
public class ComponentConfiguration extends Observable implements Observer
{
  private ObservableProperties properties;
  private String keyPrefix;

  /**
   * Constructor for ComponentConfiguration.
   * @param properties Global configuration properties
   */
  ComponentConfiguration(ObservableProperties properties)
  {
    // Check parameters
    if (properties == null)
    {
      throw new IllegalArgumentException();
    }

    // Save parameters
    this.properties = properties;
    this.keyPrefix = "";

    // Register observable
    properties.addObserver(this);
  }

	/**
	 * Constructor for ComponentConfiguration.
   * @param properties Global configuration properties
	 * @param keyPrefix Prefix to use with this component configuration
	 */
  ComponentConfiguration(ObservableProperties properties, String keyPrefix)
  {
    // Check parameters
    if (properties == null || keyPrefix == null)
    {
      throw new IllegalArgumentException();
    }

    // Save parameters
    this.properties = properties;
    this.keyPrefix = keyPrefix + '.';

    // Register observable
    properties.addObserver(this);
  }

	/**
	 * Constructor for ComponentConfiguration.
	 * @param properties Global configuration properties
	 * @param componentClass Class of component
	 * @param instanceName Name of the component
	 */
	ComponentConfiguration(ObservableProperties properties, Class componentClass, String instanceName)
	{
    // Check parameters
    if (properties == null || componentClass == null || instanceName == null || instanceName.equals(""))
    {
      throw new IllegalArgumentException();
    }

    // Save parameters
    this.properties = properties;

    // Build key prefix
    keyPrefix = componentClass.getName() + '.' + instanceName + '.';

    // Register observable
    properties.addObserver(this);
	}

  /**
   * Get a string property
   * @param propName The name of the property to get
   * @return The property string or null is the property is not found.
   */
  public String getProperty(String propName)
  {
    // Forward to the properties object
    return properties.getProperty(keyPrefix + propName);
  }

  /**
   * Get a string property and if it is not found return the default value
   * @param propName The name of the property to get
   * @param defVal The default value
   * @return The property value
   */
  public String getProperty(String propName, String defVal)
  {
    // Try to get the property
    String value = properties.getProperty(keyPrefix + propName);
    if (value == null)
    {
      value = defVal;
    }

    // Return the property
    return value;
  }

  /**
   * Set the property specified by the key to the specified value
   * @param key The name of the property to set
   * @param value The value to set the property to.
   */
  public void setProperty(String key, String value)
  {
    // Forward to the properties object
    properties.setProperty(keyPrefix + key, value);
  }

	/**
	 * Check to see if the specificied key is present
	 * @param key The key to check
	 * @return boolean True is property is present, otherwise false.
	 */
  public boolean isValid(String key)
  {
    return getProperty(key) != null;
  }

  /**
   * Get the specified property as a boolean value.  The property must have one of
   * the following values: true or false.  This method is case-insensitive
   * @param propName The name of the property
   * @return TRUE or FALSE
   * @throws BadPropertyException Thrown if the property is not found
   * or has a bad value
   */
  public boolean getBooleanProperty(String propName) throws BadPropertyException
  {
    // Try to get the properties string
    String stringValue = getProperty(propName);
    if (stringValue == null)
    {
      throw new BadPropertyException("property not found: " + propName);
    }

    // Convert value to lower case
    stringValue = stringValue.toLowerCase();

    // Check value
    if (stringValue.equals("true"))
    {
      return true;
    }
    else if (stringValue.equals("false"))
    {
      return false;
    }

    // Bad value
    throw new BadPropertyException("bad value");
  }

  /**
   * Get the specified property as a boolean value.  The property must have one of
   * the following values: true or false.  This method is case-insensitive. If the property
   * is not found or has a bad value the default value is returned
   * @param propName The name of the property
   * @return TRUE or FALSE
   */
  public boolean getBooleanProperty(String propName, boolean defVal)
  {
    // Try to get the value, return default if there is a problem
    try
    {
      return getBooleanProperty(propName);
    }
    catch (BadPropertyException ex)
    {
      return defVal;
    }
  }

  /**
   * Set the specified property with the specified value
   * @param key The name of the property to set
   * @param value The value to use
   */
  public void setBooleanProperty(String key, boolean value)
  {
    // Set the property
    if (value)
    {
    // Set property value to true
      setProperty(key, "true");
    }
    else
    {
    // Set property value to false
      setProperty(key, "false");
    }
  }


  /**
   * Get a property as a byte value and return it in an longeger which is not
   * sign extended.  The converted value will be trimed to fit in 1 byte.
   * The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x.
   * @param propName The property name
   * @return The byte value as an longereger
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a byte
   */
  public int getByteProperty(String propName) throws BadPropertyException
  {
    // Get the string value as all lower case
    String stringValue = getProperty(propName);
    if (stringValue == null)
    {
      throw new BadPropertyException("bad name: " + propName);
    }

    // Convert value to lower case
    stringValue = stringValue.toLowerCase();

    // Check base of the string
    int base = stringValue.indexOf("0x") == -1 ? 10 : 16;

    // Convert to byte
    try
    {
      return Byte.parseByte(stringValue, base) & 0xff;
    }
    catch (NumberFormatException ex)
    {
      throw new BadPropertyException("bad format: " + stringValue);
    }
  }

  /**
   * Get a property as a byte value and return it in an integer which is not
   * sign extended.  The converted value will be trimed to fit in 1 byte.
   * The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x. If property
   * does not exist or the number format is bad, the default value is returned
   * @param propName The property name
   * @param defVal The default value to use
   * @return The byte value as an intereger
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a byte
   */
  public int getByteProperty(String propName, byte defVal)
  {
    // Try to get the value, return default if there is a problem
    try
    {
      return getByteProperty(propName);
    }
    catch (BadPropertyException ex)
    {
      return defVal;
    }
  }

  /**
   * Set the specified property with the specified byte value.  The integer value
   * is masked to one byte.
   * @param key The property to set
   * @param value The new property value
   */
  public void setByteProperty(String key, int value)
  {
    setProperty(key, "0x" + Integer.toString(value & 0xff, 16));
  }

  /**
   * Get a property as a short value and return it in an integer which is not
   * sign extended.  The converted value will be trimed to fit in a short.
   * The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x.
   * @param propName The property name
   * @return The short value as an intereger
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a short
   */
  public int getShortProperty(String propName) throws BadPropertyException
  {
    // Get the string value as all lower case
    String stringValue = getProperty(propName);
    if (stringValue == null)
    {
      throw new BadPropertyException("bad name: " + propName);
    }

    // Convert value to lower case
    stringValue = stringValue.toLowerCase();

    // Check base of the string
    int base = stringValue.indexOf("0x") == -1 ? 10 : 16;

    // Convert to byte
    try
    {
      return Short.parseShort(stringValue, base) & 0xffff;
    }
    catch (NumberFormatException ex)
    {
      throw new BadPropertyException("bad format: " + stringValue);
    }
  }

  /**
   * Get a property as a short value and return it in an integer which is not
   * sign extended.  The converted value will be trimed to fit in 1 short.
   * The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x. If property
   * does not exist or the number format is bad, the default value is returned
   * @param propName The property name
   * @param defVal The default value to use
   * @return The short value as an intereger
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a short
   */
  public int getShortProperty(String propName, short defVal)
  {
    // Try to get the value, return default if there is a problem
    try
    {
      return getShortProperty(propName);
    }
    catch (BadPropertyException ex)
    {
      return defVal;
    }
  }

  /**
   * Set the specified property with the specified short value.  The integer value
   * is masked to short value.
   * @param key The property to set
   * @param value The new property value
   */
  public void setShortProperty(String key, int value)
  {
    setProperty(key, "0x" + Integer.toString(value & 0xffff, 16));
  }

  /**
   * Get a property as a int value and return it in an integer which is not
   * sign extended.  The converted value will be trimed to fit in a int.
   * The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x.
   * @param propName The property name
   * @return The int value as an intereger
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a int
   */
  public int getIntProperty(String propName) throws BadPropertyException
  {
    // Get the string value as all lower case
    String stringValue = getProperty(propName);
    if (stringValue == null)
    {
      throw new BadPropertyException("bad name: " + propName);
    }

    // Convert value to lower case
    stringValue = stringValue.toLowerCase();

    // Check base of the string
    int base = stringValue.indexOf("0x") == -1 ? 10 : 16;

    // Convert to byte
    try
    {
      return Integer.parseInt(stringValue, base);
    }
    catch (NumberFormatException ex)
    {
      throw new BadPropertyException("bad format: " + stringValue);
    }
  }

  /**
   * Get a property as a int value and return it in an integer which is not
   * sign extended.  The converted value will be trimed to fit in 1 int.
   * The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x. If property
   * does not exist or the number format is bad, the default value is returned
   * @param propName The property name
   * @param defVal The default value to use
   * @return The int value as an intereger
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a int
   */
  public int getIntProperty(String propName, int defVal)
  {
    // Try to get the value, return default if there is a problem
    try
    {
      return getIntProperty(propName);
    }
    catch (BadPropertyException ex)
    {
      return defVal;
    }
  }

  /**
   * Set the specified property with the specified int value.  The integer value
   * is masked to int value.
   * @param key The property to set
   * @param value The new property value
   */
  public void setIntProperty(String key, int value)
  {
    setProperty(key, "0x" + Integer.toString(value, 16));
  }

  /**
   * Get a property as a long value and return it in an long which is not
   * sign extended.  The converted value will be trimed to fit in a long.
   * The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x.
   * @param propName The property name
   * @return The long value
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a long
   */
  public long getLongProperty(String propName) throws BadPropertyException
  {
    // Get the string value as all lower case
    String stringValue = getProperty(propName);
    if (stringValue == null)
    {
      throw new BadPropertyException("bad name: " + propName);
    }

    // Convert value to lower case
    stringValue = stringValue.toLowerCase();

    // Check base of the string
    int base = stringValue.indexOf("0x") == -1 ? 10 : 16;

    // Convert to byte
    try
    {
      return Long.parseLong(stringValue, base);
    }
    catch (NumberFormatException ex)
    {
      throw new BadPropertyException("bad format: " + stringValue);
    }
  }

  /**
   * Get a property as a long value and return it in an integer which is not
   * sign extended.  The value of the property can be either base 10 or base 16.
   * If the property is base 16, then it must be preceded with 0X or 0x. If property
   * does not exist or the number format is bad, the default value is returned
   * @param propName The property name
   * @param defVal The default value to use
   * @return The value as an long
   * @throws BadPropertyException Thrown is the property is not found
   * or the value can not be converted to a long
   */
  public long getLongProperty(String propName, long defVal)
  {
    // Try to get the value, return default if there is a problem
    try
    {
      return getLongProperty(propName);
    }
    catch (BadPropertyException ex)
    {
      return defVal;
    }
  }

  /**
   * Set the specified property with the specified long value.
   * @param key The property to set
   * @param value The new property value
   */
  public void setLongProperty(String key, long value)
  {
    setProperty(key, "0x" + Long.toString(value, 16));
  }

	/**
	 * @see java.util.Observer#update(Observable, Object)
	 */
	public void update(Observable observable, Object data)
	{
    // Check data type
    if (data instanceof String)
    {
      // Cast to string
      String key = (String)data;

      // Check for substring matching key prefix
      if (key.startsWith(keyPrefix))
      {
        // Mark observable as changed
        setChanged();

        // Notify all observers
        notifyObservers(data);

        // All done
        return;
      }
    }

    // Bad data not a string
    throw new IllegalArgumentException("bad observer argument: " + data.toString());
	}

  /**
   * @param out
   */
  public void list(PrintStream out)
  {
    out.println("key prefix: " + keyPrefix);
    properties.list(out);
  }
}
