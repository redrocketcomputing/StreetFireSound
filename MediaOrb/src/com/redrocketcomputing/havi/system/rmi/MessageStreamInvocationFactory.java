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
 * $Id: MessageStreamInvocationFactory.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;

/**
 * @author david
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class MessageStreamInvocationFactory
{
  private short apiCode;
  private Class table[];

  private final static Class[] PARAMETER_TYPES = { HaviByteArrayInputStream.class };

  /**
   * Constructor for MessageStreamInvocationFactory.
   */
  public MessageStreamInvocationFactory(short apiCode, Class[] table)
  {
    // Check to parameters
    if (table == null)
    {
      // Badness
      throw new IllegalArgumentException("table is null");
    }

    // Save the parameters
    this.apiCode = apiCode;
    this.table = table;
  }

  public MessageStreamInvocation createInvocation(OperationCode opCode, HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Check API code
    if (opCode.getApiCode() != apiCode)
    {
      // Do not know about this
      return null;
    }

    // Range and value
    int index = opCode.getOperationId() & 0xff;

    if (index >= table.length || table[index] == null)
    {
      // Not for us
      return null;
    }

    try
    {

      // Get invocation class constructor
      Constructor constructor = table[index].getConstructor(PARAMETER_TYPES);

      // Build arguments
      Object[] arguments = new Object[1];
      arguments[0] = hbais;

      // Create the invocation object
      return (MessageStreamInvocation) constructor.newInstance(arguments);
    }
    catch (NoSuchMethodException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (InstantiationException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (InvocationTargetException e)
    {
      // Check the instance of the target exception
      if (e.getTargetException() instanceof HaviUnmarshallingException)
      {
        // Translate
        throw ((HaviUnmarshallingException) e.getTargetException());
      }

      // Unknow exception
      throw new IllegalStateException(e.getTargetException().toString());
    }
  }
}
