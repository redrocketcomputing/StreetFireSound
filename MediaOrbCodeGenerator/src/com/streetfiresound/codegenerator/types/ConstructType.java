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
 * $Id: ConstructType.java,v 1.1 2005/02/24 03:03:38 stephen Exp $
 */

package com.streetfiresound.codegenerator.types;

import java.io.IOException;
import java.util.ArrayList;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.rules.RuleDefinition;


/**
 * @author george
 *
 *  Basically, constructtype contains 2 type of container. 1 is complex and another is simple.
 *
 * Complex = contains arraylist e.g. StructType, EnumType, UnionType, ContextType, ScopeType, InterfaceType, FunctionType
 * Simple = no ArrayList = DeclarationType, HolderType,
 *
 *
 */
abstract public class ConstructType extends HaviType
{


  private ArrayList childList;

  /**
   * Constructor for ConstructType.
   * @param typeDef
   * @param typeName
   */
  public ConstructType(short typeDef, String typeName)
  {
    super(typeDef, typeName);
    childList = new ArrayList();

  }

  public void addChild(HaviType child)
  {

    child.setParent(this);
    childList.add(child);

  }

  public HaviType getChild(int index)
  {
    return (HaviType)childList.get(index);
  }

  public void removeChild(HaviType child)
  {
    if(this == child.getParent())
      child.setParent(null);

    childList.remove(child);

  }


  /**
   * Returns the childList.
   * @return ArrayList
   */
  public ArrayList getChildList()
  {
    return childList;
  }


  /**
   * Sets the childList.
   * @param childList The childList to set
   */
  public void setChildList(ArrayList childList)
  {
    this.childList = childList;
  }


  public void output(java.io.OutputStream ostream) throws IOException
  {

          RuleDefinition rule = CodeGenerator.ruleFactory.createRuleObject(this.getConstantTypeDef(), this);

          rule.outputToFile(ostream);

/*
          java.util.Iterator iter = childList.iterator();
          while(iter.hasNext())
          {

              HaviType hObject = (HaviType) iter.next();
              RuleDefinition rule = driver.ruleFactory.createRuleObject(hObject.getConstantTypeDef(), hObject);
              rule.outputToFile(ostream);

          }
*/
  }


  public java.util.Iterator iterator()
  {

    return childList.iterator();

  }

}
