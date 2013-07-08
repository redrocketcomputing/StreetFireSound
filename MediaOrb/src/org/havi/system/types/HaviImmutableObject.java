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
 * $Id: HaviImmutableObject.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package org.havi.system.types;

/**
 * <p>Title: HaviImmutableObject </p>
 * <p>Description: Base class for read-only HAVi object</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public abstract class HaviImmutableObject extends HaviObject
{
  /**
   *
   */
  public HaviImmutableObject()
  {
  }

  /**
   *
   * @param hbais
   * @throws HaviUnmarshallingException
   */
  public final void unmarshal(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    throw new HaviUnmarshallingException("Immutable Object");
  }

}
