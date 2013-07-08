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
 * $Id: HaviType.java,v 1.1 2005/02/24 03:03:37 stephen Exp $
 */

package com.streetfiresound.codegenerator.types;

import java.io.IOException;
import java.io.OutputStream;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.rules.RuleDefinition;

/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class HaviType implements ConstTypeDefinition
{
    private short constantTypeDef;
    private String typeName;

    private HaviType parent;

    public HaviType()
    {
    }


    public HaviType(short typeDef, String typeName)
    {
      this.constantTypeDef = typeDef;
      this.typeName = typeName;
    }

    public void output(OutputStream os) throws IOException
    {

         RuleDefinition rule = CodeGenerator.ruleFactory.createRuleObject(getConstantTypeDef(), this);
         rule.outputToFile(os);
    }






    /**
     * Returns the constantTypeDef.
     * @return short
     */
    public short getConstantTypeDef()
    {
      return constantTypeDef;
    }

    /**
     * Returns the typeName.
     * @return String
     */
    public String getTypeName()
    {
      return typeName;
    }

    public void setTypeName(String TypeName)
    {
      this.typeName =  TypeName;
    }



    /**
     * Returns the parent.
     * @return HaviType
     */
    public HaviType getParent()
    {
      return parent;
    }

    /**
     * Sets the parent.
     * @param parent The parent to set
     */
    public void setParent(HaviType parent)
    {
      this.parent = parent;
    }

}
