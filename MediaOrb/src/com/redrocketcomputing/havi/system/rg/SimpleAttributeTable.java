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
 * $Id: SimpleAttributeTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rg;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

import org.havi.dcm.types.HUID;
import org.havi.dcm.types.TargetId;
import org.havi.system.constants.ConstAttributeClassName;
import org.havi.system.constants.ConstAttributeName;
import org.havi.system.constants.ConstBoolOperation;
import org.havi.system.constants.ConstComparisonOperator;
import org.havi.system.types.Attribute;
import org.havi.system.types.AttributeClass;
import org.havi.system.types.ComplexQuery;
import org.havi.system.types.DeviceClassAttributeClass;
import org.havi.system.types.DeviceManufAttributeClass;
import org.havi.system.types.DeviceModelAttributeClass;
import org.havi.system.types.GuiReqAttributeClass;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviInvalidValueException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HuidAttributeClass;
import org.havi.system.types.InterfaceIdAttributeClass;
import org.havi.system.types.Query;
import org.havi.system.types.SeManufAttributeClass;
import org.havi.system.types.SeTypeAttributeClass;
import org.havi.system.types.SeVersAttributeClass;
import org.havi.system.types.SimpleQuery;
import org.havi.system.types.TargetIdAttributeClass;
import org.havi.system.types.UserPrefNameAttributeClass;
import org.havi.system.types.VendorId;
import org.havi.system.types.VendorIdAttributeClass;

/**
 * @author stephen
 *
 */
public class SimpleAttributeTable
{
	private AttributeClass[] table = new AttributeClass[15];  //XXX:0:20040908iain: consider using generated constant for array length

	/**
   * Convert an Attribute to an AttributeClass
	 * @param attribute The Attribute to convert
	 * @return The matching AttributeClass
	 */
	public static AttributeClass toAttributeClass(Attribute attribute)
	{
    try
    {
      // Write attribute to byte array
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(4 + attribute.getValue().length);
      hbaos.writeInt(attribute.getName());
      hbaos.write(attribute.getValue());

      // Construct a attribute class from the byte array
      return AttributeClass.create(hbaos.getBuffer());
    }
    catch (IOException e)
    {
    	return null;
    }
	}
  
  /**
   * Convert an Attribute array to a AttributeClass array
   * @param attributes The Attribute array to convert
   * @return The match AttributeClass array
   */
  public static AttributeClass[] toAttributeClass(Attribute[] attributes)
  {
    // Create array
    AttributeClass[] classArray = new AttributeClass[attributes.length];
    for (int i = 0; i < classArray.length; i++)
    {
      classArray[i] = toAttributeClass(attributes[i]);
    }
    
    // Return the array
    return classArray;
  }

  /**
   * Construct a simple attribute table which can be used to build
   * query, registration attribute arrays, etc..
   */
  public SimpleAttributeTable()
  {
  }
  
  /**
   * Construct an SimpleAttributeTable from a AttributeClass array, unknow attributes are dropped
   * @param attributes The Attribute array to build the table from
   */
  public SimpleAttributeTable(AttributeClass[] attributes)
  {
    // Loop through the attributes
    for (int i = 0; i < attributes.length; i++)
    {
      // Check range
      if (attributes[i].getDiscriminator() >= 0 && attributes[i].getDiscriminator() < table.length)
      {
        // Add entry
        setEntry(attributes[i]);
      }
    }
  }

  /**
   * Construct an SimpleAttributeTable from a Attribute array
   * @param attributes The Attribute array to build the table from
   */
  public SimpleAttributeTable(Attribute[] attributes)
  {
    // Forward
    this(toAttributeClass(attributes));
  }

  /**
   * Reset all internal component to null
   */
	public void reset()
	{
		// Empty the array
		Arrays.fill(table, null);
	}

  /**
   * Convert table to an array of AttributeClass with all unset elements removed
   * @return AttributeClass[] The array of set attributes
   */
	public AttributeClass[] toAttributeClassArray()
	{
    // Allocate array
    AttributeClass[] array = new AttributeClass[countEntries()];

    // Copy entries to array
    int position = 0;
    for (int i = 0; i < table.length; i++)
    {
    	// Check for non null
    	if (table[i] != null)
    	{
	    	array[position++] = table[i];
    	}
    }

    // Return the array
    return array;
	}

  /**
   * Convert the table to an array of Attributes which can be added to the registry.  All unset entries
   * are removed
   * @return Attribute[] The array of Attributes
   */
	public Attribute[] toAttributeArray()
	{
    try
    {
      // Allocate array
      Attribute[] array =  new Attribute[countEntries()];

      // Create output stream
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();

      // Loop through the table converting to attribute values
      int position = 0;
      for (int i = 0; i < table.length; i++)
      {
      	// Make sure the attribute class is set
      	if (table[i] != null)
      	{
	      	// Extract entry
	        AttributeClass entry = table[i];

	        // Build the output buffer
	        hbaos.reset();
	      	entry.marshal(hbaos);
	      	byte [] outputBuffer = hbaos.toByteArray();

	      	// Build attribute value
	      	byte[] valueBuffer = new byte[outputBuffer.length - 4];
	      	System.arraycopy(outputBuffer, 4, valueBuffer, 0, valueBuffer.length);

	      	// Save the attribute
	      	array[position++] = new Attribute(entry.getDiscriminator(), valueBuffer);
      	}
      }

      // All done
      return array;
    }
    catch (HaviMarshallingException e)
    {
    	// Umm
    	throw new HaviInvalidValueException(e.toString());
    }
	}

  /**
   * Convert the table to a registry query of the form. NEED BETTER DESCRIPTION OF THE QUERY
   * @return Query The query built
   */
	public Query toQuery()
	{
		// Convert table to attribute array
		Attribute[] attributeArray = toAttributeArray();

		// Check for empty array and query everything by matching software element type to anything
		if (attributeArray.length == 0)
		{
			return new SimpleQuery(ConstAttributeName.ATT_SE_TYPE, new byte[4], ConstComparisonOperator.ANY);
		}

		// Build simple querys for the attribute and push them on a stack
		Stack queryStack = new Stack();
		for (int i = 0; i < attributeArray.length; i++)
    {
    	// Create simple query
    	SimpleQuery query = new SimpleQuery(attributeArray[i].getName(), attributeArray[i].getValue(), ConstComparisonOperator.EQU);

    	// Push on to the stack
    	queryStack.push(query);
    }

		// Loop until there is only one query on the stack
		while (queryStack.size() != 1)
		{
			// Pop two querys
			Query query1 = (Query)queryStack.pop();
			Query query2 = (Query)queryStack.pop();

			// Create complex query
			Query query = new ComplexQuery(query1, query2, ConstBoolOperation.AND);

			// Push it back on the stack
			queryStack.push(query);
		}

    // Return the query
    return (Query)queryStack.pop();
	}

  /**
   * Return the table entry with the matching name
   * @param name The attribute name
   * @return AttributeClass The attribute class matching the name
   */
	public AttributeClass getEntry(int name)
	{
		// Check range
		if (name < 0 || name >= table.length)
		{
			// Badness
			throw new IllegalArgumentException("bad name: " + name);
		}

		// Return the entry
		return table[name];
	}

  /**
   * Clear the specified entry from the table
   * @param name The attribute name of the entry to clear
   */
	public void clearEntry(int name)
	{
		// Check range
		if (name < 0 || name >= table.length)
		{
			// Badness
			throw new IllegalArgumentException("bad name: " + name);
		}

		// Release the entry
		table[name] = null;
	}

  /**
   * Set a table entry to a new value
   * @param value The table entry value.
   */
	public void setEntry(AttributeClass value)
	{
		// Check range
		if (value.getDiscriminator() < 0 || value.getDiscriminator() >= table.length)
		{
			// Badness
			throw new IllegalArgumentException("bad name: " + value.getDiscriminator());
		}

		// Set the entry
		table[value.getDiscriminator()] = value;
	}

  /**
   * Check to see if an attribute is valid in the table
   * @param name The name of the attribute to check
   * @return boolean True if the attribute is valid
   */
	public boolean isValid(int name)
	{
		// Check range
		if (name < 0 || name >= table.length)
		{
			// Bad
			throw new IllegalArgumentException("bad name: " + name);
		}

		// Return true if the table entry is set
		return table[name] != null;
	}

  /**
   * Return the table's SoftwareElementType.
   * @return int The SoftwareElementType
   */
	public int getSoftwareElementType()
	{
		return ((SeTypeAttributeClass)table[ConstAttributeClassName.SE_TYPE]).getSoftwareElementType();
	}

  /**
   * Set the table's SoftwareElementType.
   * @param softwareElementType The SoftwareElementType
   */
	public void setSoftwareElementType(int softwareElementType)
	{
		table[ConstAttributeClassName.SE_TYPE] = new SeTypeAttributeClass(softwareElementType);
	}

	public VendorId getVendorId()
	{
		return ((VendorIdAttributeClass)table[ConstAttributeClassName.VENDOR_ID]).getVendorId();
	}

	public void setVendorId(VendorId vendorId)
	{
		table[ConstAttributeClassName.VENDOR_ID] = new VendorIdAttributeClass(vendorId);
	}

	public HUID getHuid()
	{
		return ((HuidAttributeClass)table[ConstAttributeClassName.HUID]).getHuid();
	}

	public void setHuid(HUID huid)
	{
		table[ConstAttributeClassName.HUID] = new HuidAttributeClass(huid);
	}

	public TargetId getTargetId()
	{
		return ((TargetIdAttributeClass)table[ConstAttributeClassName.TARGET_ID]).getTargetId();
	}

	public void setTargetId(TargetId targetId)
	{
		table[ConstAttributeClassName.TARGET_ID] = new TargetIdAttributeClass(targetId);
	}

	public short getInterfaceId()
	{
		return ((InterfaceIdAttributeClass)table[ConstAttributeClassName.INTERFACE_ID]).getInterfaceId();
	}

	public void setInterfaceId(short interfaceId)
	{
		table[ConstAttributeClassName.INTERFACE_ID] = new InterfaceIdAttributeClass(interfaceId);
	}

	public int getDeviceClass()
	{
		return ((DeviceClassAttributeClass)table[ConstAttributeClassName.DEVICE_CLASS]).getDeviceClass();
	}

	public void setDeviceClass(int deviceClass)
	{
		table[ConstAttributeClassName.DEVICE_CLASS] = new DeviceClassAttributeClass(deviceClass);
	}

	public String getDeviceManufacturer()
	{
		return ((DeviceManufAttributeClass)table[ConstAttributeClassName.DEVICE_MANUF]).getDeviceManufacture();
	}

	public void setDeviceManufacturer(String deviceManufacturer)
	{
		table[ConstAttributeClassName.DEVICE_MANUF] = new DeviceManufAttributeClass(deviceManufacturer);
	}

	public String getDeviceModel()
	{
		return ((DeviceModelAttributeClass)table[ConstAttributeClassName.DEVICE_MODEL]).getDeviceModel();
	}

	public void setDeviceModel(String deviceModel)
	{
		table[ConstAttributeClassName.DEVICE_MODEL] = new DeviceModelAttributeClass(deviceModel);
	}

	public String getSoftwareElementManufacturer()
	{
		return ((SeManufAttributeClass)table[ConstAttributeClassName.SE_MANUF]).getSoftwareElementManufacture();
	}

	public void setSoftwareElementManufacturer(String softwareElementManufacturer)
	{
		table[ConstAttributeClassName.SE_MANUF] = new SeManufAttributeClass(softwareElementManufacturer);
	}

	public String getSoftwareElementVersion()
	{
		return ((SeVersAttributeClass)table[ConstAttributeClassName.SE_VERS]).getSoftwareElementVersion();
	}

	public void setSoftwareElementVersion(String softwareElementVersion)
	{
		table[ConstAttributeClassName.SE_VERS] = new SeVersAttributeClass(softwareElementVersion);
	}

	public String getUserPreferredName()
	{
		return ((UserPrefNameAttributeClass)table[ConstAttributeClassName.USER_PREF_NAME]).getUserPreferredName();
	}

	public void setUserPreferredName(String userPreferredName)
	{
		table[ConstAttributeClassName.USER_PREF_NAME] = new UserPrefNameAttributeClass(userPreferredName);
	}
  
  public int getGuiReq()
  {
    return ((GuiReqAttributeClass)table[ConstAttributeClassName.GUI_REQ]).getGuiReq();
  }
  
  public void setGuiReq(int guiReq)
  {
    table[ConstAttributeClassName.GUI_REQ] = new GuiReqAttributeClass(guiReq);
  }

	private int countEntries()
	{
		// Count non null entries
		int count = 0;
		for (int i = 0; i < table.length; i++)
    {
    	// Check for non null entry
    	if (table[i] != null)
    	{
    		++count;
    	}
    }

		// Return the count
		return count;
	}
}
