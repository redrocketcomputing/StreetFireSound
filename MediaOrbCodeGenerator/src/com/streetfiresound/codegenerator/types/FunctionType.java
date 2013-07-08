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
 * $Id: FunctionType.java,v 1.1 2005/02/24 03:03:38 stephen Exp $
 */

package com.streetfiresound.codegenerator.types;


/**
 * @author george
 *
 * Contains Return type  function name  HolderType list
 * e.g  String xxx (in String aaa, out int bbb, inout short ccc);
 *
 *
 */
public class FunctionType extends ConstructType
{

  private HaviType  returnType = null;


  private RaiseListType raiseListType = null;
  private ContextListType contextListType = null;


  /**
   * Constructor for FunctionType.
   * @param typeDef
   * @param typeName
   */
  public FunctionType()
  {
    super(FUNCTION, "function");
  }

  public FunctionType(String functionName)
  {
    super(FUNCTION, functionName);
  }




  /**
   * Returns the contextListType.
   * @return ContextListType
   */
  public ContextListType getContextListType()
  {
    return contextListType;
  }


  /**
   * Returns the raiseListType.
   * @return RaiseListType
   */
  public RaiseListType getRaiseListType()
  {
    return raiseListType;
  }

  /**
   * Sets the contextListType.
   * @param contextListType The contextListType to set
   */
  public void setContextListType(ContextListType contextListType)
  {
    this.contextListType = contextListType;
  }


  /**
   * Sets the raiseListType.
   * @param raiseListType The raiseListType to set
   */
  public void setRaiseListType(RaiseListType raiseListType)
  {
    this.raiseListType = raiseListType;
  }

  /**
   * Returns the returnType.
   * @return HaviType
   */
  public HaviType getReturnType()
  {
    return returnType;
  }

  /**
   * Sets the returnType.
   * @param returnType The returnType to set
   */
  public void setReturnType(HaviType returnType)
  {
    this.returnType = returnType;
  }

}
