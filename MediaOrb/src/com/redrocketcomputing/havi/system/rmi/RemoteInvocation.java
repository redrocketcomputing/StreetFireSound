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
 * $Id: RemoteInvocation.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import org.havi.system.types.HaviImmutableObject;
import org.havi.system.types.Status;

/**
 * @author stephen
 *
 */
public abstract class RemoteInvocation extends HaviImmutableObject
{
  protected Status returnCode;
  protected String errorMessage = "";

  /**
   * Construct RemoteInvocation
   */
  protected RemoteInvocation(Status returnCode)
  {
    // Check return code
    if (returnCode == null)
    {
      // Badd
      throw new IllegalArgumentException("return code is null");
    }

    // Save the return code
    this.returnCode = returnCode;
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
  public boolean equals(Object arg0)
  {
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return 0;
  }

  /**
   * Returns the returnCode.
   * @return Status
   */
  public Status getReturnCode()
  {
    return returnCode;
  }
  
  public String getErrorMessage()
  {
    return errorMessage;
  }
  
  public abstract void dispatch(RemoteSkeleton skeleton);

}
