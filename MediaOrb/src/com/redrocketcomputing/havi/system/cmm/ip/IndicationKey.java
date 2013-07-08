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
 * $Id: IndicationKey.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip;

import org.havi.system.types.GUID;

/**
 * Key for the IndicationRouter internal dispatch table
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class IndicationKey
{
  private GUID guid = GUID.ZERO;
  private int address = -1;

  /**
   * @see java.lang.Object#Object()
   */
  public IndicationKey()
  {
  }

  /**
   * Method IndicationKey.
   * @param guid
   * @param address
   */
  public IndicationKey(GUID guid, int address)
  {
    // Save the parameters
    this.guid = guid;
    this.address = address;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return guid.hashCode() + address + 729867235;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    if (o instanceof IndicationKey)
    {
      IndicationKey other = (IndicationKey)o;
      return guid.equals(other.guid) && address == other.address;
    }
    return false;
  }

  /**
   * @see java.lang.Object#toString()
   */
  final public String toString()
  {
    return guid.toString() + '@' + address;
  }
  /**
   * Returns the address.
   * @return int
   */
  final public int getAddress()
  {
    return address;
  }

  /**
   * Returns the guid.
   * @return GUID
   */
  final public GUID getGuid()
  {
    return guid;
  }

  /**
   * Sets the address.
   * @param address The address to set
   */
  final public void setAddress(int address)
  {
    this.address = address;
  }

  /**
   * Sets the guid.
   * @param guid The guid to set
   */
  final public void setGuid(GUID guid)
  {
    this.guid = guid;
  }
}
