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
 * $Id: EnumRule.java,v 1.2 2005/02/24 03:03:34 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;


import com.streetfiresound.codegenerator.output.GplHeaderFileOutputStream;
import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.EnumType;
import com.streetfiresound.codegenerator.types.HaviType;





/**
 * @author george
 *
 */
public class EnumRule extends RuleDefinition
{


	String system;
	String constant;
	String packageName;
	String type;
	String exception;
	String rootPath;

  /**
   * Constructor for EnumRule.
   */
  public EnumRule(EnumType it)
  {

    super(it);

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);

	constant = (String) configInfo.get("CONSTANT");

	system = (String) configInfo.get("SYSTEM");

	type = (String) configInfo.get("TYPE");

	packageName = (String) configInfo.get("PACKAGE");

	exception = (String) configInfo.get("EXCEPTION");

	rootPath = (String)configInfo.get("ROOTPATH");
  }

  /**
   * Enumtype will create a interface with Const prefix and put intot the constant directory
   *
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
  public void outputToFile(java.io.OutputStream ostream) throws java.io.IOException
  {

      try
      {

		// check for special case enum, if class not found then will throw ClassNotFoundException mean perform normal enum rule
        String className = "com.streetfiresound.codegenerator.output.WriteEnum"+ht.getTypeName();

		//using reflection to create special case object
        Class newClass = Class.forName(className);

        Class[] parameter = { ht.getClass() };

        Object[] parameterList = { ht };



        Constructor constructor = newClass.getConstructor(parameter);


        constructor.newInstance(parameterList);


//		if(ht.getTypeName().equalsIgnoreCase("ErrorCode"))
//		    new WriteModuleException();

      }
      catch(ClassNotFoundException e)
      {

		//check for constantpath, if not exists then create the constant directory

        String location = rootPath + "/" + packageName.replace('.','/') +"/"+ constant.replace('.','/');

        //create fileoutputstream
        FileOutputStream fos = new GplHeaderFileOutputStream(location+"/Const"+ht.getTypeName()+".java");


        printPackage(fos);

        printOpening(fos);

        printContent(fos);

        printClosing(fos);

      }
      catch(NoSuchMethodException e)
      {
              e.printStackTrace();
        System.err.println("enum error="+e+":"+ ht.getTypeName());
      }


      catch(InstantiationException e)
      {
              e.printStackTrace();
        System.err.println("enum error="+e+":"+ ht.getTypeName());
      }

      catch(IllegalAccessException e)
      {
              e.printStackTrace();
        System.err.println("enum error="+e+":"+ ht.getTypeName());
      }
      catch(InvocationTargetException e)
      {
              e.printStackTrace();
        System.err.println("enum error="+e.getMessage()+":"+ ht.getTypeName());
      }







  }


/**
 * Method printPackage.
 * @param ostream
 * @throws IOException
 */
  private void printPackage(OutputStream ostream) throws IOException
  {
	String constantPath=packageName+"."+constant;
    ostream.write(( "package " + constantPath + ";\n\n\n").getBytes());

  }



/**
 * Method printOpening.
 * @param ostream
 * @throws IOException
 */
  private void printOpening(OutputStream ostream) throws IOException
  {
     ostream.write("public interface Const".getBytes());
     ostream.write(ht.getTypeName().getBytes());
     ostream.write("\n{\n".getBytes());


  }



/**
 * output the interface content
 *
 * Method printContent.
 * @param ostream
 * @throws IOException
 */
  private void printContent(OutputStream ostream) throws IOException
  {
    Iterator iter = ((EnumType)ht).iterator();
    int count = 0;
    while(iter.hasNext())
    {
        ostream.write("\tpublic static final int ".getBytes());
        ((HaviType) iter.next()).output(ostream);
        ostream.write(" = ".getBytes());
        ostream.write(Integer.toString(count++).getBytes());
        ostream.write(";\n".getBytes());
    }
  }


  private void printClosing(OutputStream ostream) throws IOException
  {
    ostream.write("}\n".getBytes());
  }


}
