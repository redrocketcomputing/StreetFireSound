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
 * $Id: TypedefRule.java,v 1.2 2005/02/24 03:03:34 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


import com.streetfiresound.codegenerator.output.GplHeaderFileOutputStream;
import com.streetfiresound.codegenerator.output.StructClassCreation;
import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ArrayType;
import com.streetfiresound.codegenerator.types.ConstructType;
import com.streetfiresound.codegenerator.types.DeclarationType;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.ListType;
import com.streetfiresound.codegenerator.types.LiteralType;
import com.streetfiresound.codegenerator.types.SequenceType;
import com.streetfiresound.codegenerator.types.TypedefType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TypedefRule extends RuleDefinition
{

  /**
   * Constructor for TypedefRule.
   */
  public TypedefRule(TypedefType it)
  {
    super(it);
  }

  /**
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */

  //Notice that SeqenceType or ArrayType will construct file, others store in scope table
  public void outputToFile(java.io.OutputStream ostream) throws java.io.IOException
  {
    DeclarationType dt = (DeclarationType) ((TypedefType) ht).iterator().next();

    Iterator iter = dt.iterator();

    //check for data type , if it is either sequecetype or arraytype then create class.
    if (dt.getDataType() instanceof SequenceType)
    {
      SequenceType st = (SequenceType) dt.getDataType();

      while (iter.hasNext())
      {
        String name = ((HaviType) iter.next()).getTypeName();
        makeSequence(st.getDataType(), st.getValue(), name);
      }

    }
    else
    {
      while (iter.hasNext())
      {
        HaviType hht = (HaviType) iter.next();

        if (hht instanceof ArrayType)
        {
          ArrayType at = (ArrayType) hht;
          makeSequence(dt.getDataType(), ((HaviType) at.iterator().next()).getTypeName(), hht.getTypeName());
        }
      }
    }
  }

  //spec-datatype, value=length, name=class name
  private void makeSequence(HaviType spec, String value, String name) throws IOException
  {
    HashMap configInfo = (HashMap) CodeGenerator.projectList.get(CodeGenerator.currentPackage);

    String packagePath = (String) configInfo.get("PACKAGE");
    String rootPath = (String) configInfo.get("ROOTPATH");

    String typePath = rootPath + "/" + packagePath.replace('.', '/') + "/" + ((String) configInfo.get("TYPE")).replace('.', '/');

    //because structclasscreate require a constructtype parameter and the constructype must contains
    //declarationtype, therefore, we use ListType which is a general constructtype and create
    //declarationtype for this purpose

    ArrayType at = new ArrayType();
    at.addChild(new LiteralType(value));

    DeclarationType dt = new DeclarationType();
    dt.setDataType(spec);
    dt.addChild(at);

    ListType lt = new ListType();
    lt.setTypeName(name);
    lt.addChild(dt);

    FileOutputStream fos = new GplHeaderFileOutputStream(typePath + "/" + name + ".java");

    StructClassCreation cc = new StructClassCreation((ConstructType) lt);
    cc.outputToFile(fos);

  }

}
