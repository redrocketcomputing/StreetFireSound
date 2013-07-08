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
 * $Id: GarpMessageFactory.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviUnmarshallingException;

/**
 * Factory for create GARP message from byte arrays
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
class GarpMessageFactory
{
  private List typeClasses;

  public GarpMessageFactory()
  {
    typeClasses = new ArrayList(4);
    typeClasses.add(GarpTimeoutMessage.MARSHAL_TYPE, GarpTimeoutMessage.class);
    typeClasses.add(GarpDeviceInfoMessage.MARSHAL_TYPE, GarpDeviceInfoMessage.class);
    typeClasses.add(GarpGoneDeviceMessage.MARSHAL_TYPE, GarpGoneDeviceMessage.class);
    typeClasses.add(GarpResetNetworkMessage.MARSHAL_TYPE, GarpResetNetworkMessage.class);
  }

  public GarpMessage create(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    try
    {
      // Mark the position
      hbais.mark(0);

      // Unmarshall the type
      int type = hbais.readInt();

      // Build constructor query
      Class[] parameterTypes = new Class[1];
      parameterTypes[0] = HaviByteArrayInputStream.class;

      // Get the constructor
      Constructor constructor = ((Class)typeClasses.get(type)).getConstructor(parameterTypes);

      // Build arguments
      Object[] arguments = new Object[1];
      arguments[0] = hbais;

      // Reset to the mark
      hbais.reset();

      // Unmarshall the object
      return (GarpMessage)constructor.newInstance(arguments);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
    catch (NoSuchMethodException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
    catch (InstantiationException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
    catch (InvocationTargetException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
  }

  public int getClassType(Class typeClass) throws IllegalAccessException, NoSuchFieldException
  {
    // Return the value of the type field
    return typeClass.getField("MARSHAL_TYPE").getInt(null);
  }
}
