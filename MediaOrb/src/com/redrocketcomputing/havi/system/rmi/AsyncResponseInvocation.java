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
 * $Id: AsyncResponseInvocation.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.io.IOException;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviImmutableObject;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.Status;

/**
 * @author stephen
 *
 */
public abstract class AsyncResponseInvocation extends HaviImmutableObject
{
  protected Status returnCode;

  /**
   * Constructor for AsyncResponseInvocation.
   */
  protected AsyncResponseInvocation(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Unmarshall the return code
    returnCode = new Status(hbais);

    try
    {
      hbais.readInt();
    }
    catch(IOException e)
    {
      throw new HaviUnmarshallingException(e.toString());
    }

  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return this;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    // Use reference equality
    return o == this;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // Use identity hande
    return System.identityHashCode(this);
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    // Always throw a marshalling exception
    throw new HaviMarshallingException("can not marshal a AsyncResponseInvocation");
  }

  public abstract void dispatch(int transactionId, AsyncResponseListener listener);
}
