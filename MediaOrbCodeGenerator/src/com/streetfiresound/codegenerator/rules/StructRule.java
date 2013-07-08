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
 * $Id: StructRule.java,v 1.2 2005/02/24 03:03:34 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;


import com.streetfiresound.codegenerator.output.GplHeaderFileOutputStream;
import com.streetfiresound.codegenerator.output.StructClassCreation;
import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ConstructType;
import com.streetfiresound.codegenerator.types.StructType;

/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class StructRule extends RuleDefinition
{

  /**
   * Constructor for StructRule.
   */
  public StructRule(StructType it)
  {
        super(it);

  }

  /**
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
  public void outputToFile(OutputStream ostream)throws java.io.IOException
  {



	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);


	String packagePath = (String)configInfo.get("PACKAGE");
	String rootPath = (String)configInfo.get("ROOTPATH");
	String typePath = rootPath + "/" + packagePath.replace('.', '/')+ "/" + ((String)configInfo.get("TYPE")).replace('.','/');

      try
      {
		//check for special case of struct, if not found then throw classnotfoundexception which perfrom normal struct rule
        String className = "com.streetfiresound.codegenerator.output.WriteStruct"+ht.getTypeName();
        Class newClass = Class.forName(className);
        Class[] parameter = { ht.getClass() };
        Object[] parameterList = { ht };

        Constructor constructor = newClass.getConstructor(parameter);
        constructor.newInstance(parameterList);

      }
      catch(ClassNotFoundException e)
      {


//              String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.packagePath.replace('.','/');

//              File newfile = new File(location);
//              if(!newfile.exists())
//                  newfile.mkdir();


  //              FileOutputStream fos = new FileOutputStream(location+"/"+ht.getTypeName()+".java");

                FileOutputStream fos = new GplHeaderFileOutputStream(typePath+"/"+ht.getTypeName()+".java");

                StructClassCreation cc = new StructClassCreation((ConstructType) ht);

                cc.outputToFile(fos);

      }
      catch(Exception e)
      {
        e.printStackTrace();
        System.err.println("struct error="+e);
      }


  }





}
