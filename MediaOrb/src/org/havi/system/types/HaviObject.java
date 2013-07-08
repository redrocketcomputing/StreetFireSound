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
 * $Id: HaviObject.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package org.havi.system.types;

/**
 * <p>Title: HaviObject</p>
 * <p>Description: Base HAVi object class</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public abstract class HaviObject extends Object implements Cloneable, Marshallable
{
  /**
   *
   */
  public HaviObject()
  {
  }

  /**
   *
   * @return
   */
  protected abstract Object clone() throws CloneNotSupportedException;

  /**
   *
   * @param o
   * @return
   */
  public abstract boolean equals(java.lang.Object o);

  /**
   *
   * @return
   */
  public abstract int hashCode();

  /**
   *
   * @param hbaos
   * @throws HaviMarshallingException
   */
  public abstract void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException;

  /**
   *
   * @param hbais
   * @throws HaviUnmarshallingException
   */
  public abstract void unmarshal(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException;
}
