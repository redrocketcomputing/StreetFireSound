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
 * $Id: SwitchType.java,v 1.1 2005/02/24 03:03:38 stephen Exp $
 */

package com.streetfiresound.codegenerator.types;

/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SwitchType extends ConstructType
{

  /**
   * Constructor for switchType.
   * @param typeDef
   * @param typeName
   */

    String Label;
    DeclarationType declaration;


  public SwitchType()
  {
    super(SWITCH, "switch");
  }



    /**
     * Returns the declaration.
     * @return DeclarationType
     */
    public DeclarationType getDeclaration()
    {
      return declaration;
    }

  /**
   * Returns the label.
   * @return String
   */
  public String getLabel()
  {
    return Label;
  }

    /**
     * Sets the declaration.
     * @param declaration The declaration to set
     */
    public void setDeclaration(DeclarationType declaration)
    {
      this.declaration = declaration;
    }

  /**
   * Sets the label.
   * @param label The label to set
   */
  public void setLabel(String label)
  {
    Label = label;
  }

}
