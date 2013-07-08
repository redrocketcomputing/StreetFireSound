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
 * $Id: UnionType.java,v 1.1 2005/02/24 03:03:38 stephen Exp $
 */

package com.streetfiresound.codegenerator.types;


/**
 * @author george
 * Contains list of HaviDatatype and list of CaseType
 * e.g. union unionName switch(HaviDatatype)
 * {
 *  case type
 *    ....
 * }
 */
public class UnionType extends ConstructType
{

  /**
   * Constructor for UnionType.
   * @param typeDef
   * @param typeName
   */

    HaviType dataType;


  public UnionType()
  {
    super(UNION, "union");
  }


  /**
   * Returns the dataType.
   * @return HaviType
   */
  public HaviType getDataType()
  {
    return dataType;
  }

  /**
   * Sets the dataType.
   * @param dataType The dataType to set
   */
  public void setDataType(HaviType dataType)
  {
    this.dataType = dataType;
  }

}
