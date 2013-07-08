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
 * $Id: SequenceType.java,v 1.1 2005/02/24 03:03:38 stephen Exp $
 */

package com.streetfiresound.codegenerator.types;


/**
 * @author george
 * contains data type and variable name
 * eg. sequence<String> xxxxx
 *
 */
public class SequenceType extends ConstructType
{

  /**
   * Constructor for SequenceType.
   * @param typeDef
   * @param typeName
   */

    //data type
    private HaviType dataType;


    //bound value
    private String value;


    public SequenceType()
    {
      super(SEQUENCE, "sequence");
      dataType = null;
      value = "";
    }



   public void setDataType(HaviType hType)
   {
      dataType = hType;
   }

   public HaviType getDataType()
   {
      return dataType;
   }



   public void setValue(String iValue)
   {
      value = iValue;
   }

   public String getValue()
   {
      return value;
   }




}
