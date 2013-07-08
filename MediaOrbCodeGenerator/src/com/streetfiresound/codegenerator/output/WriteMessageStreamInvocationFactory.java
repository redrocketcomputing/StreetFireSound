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
 * $Id: WriteMessageStreamInvocationFactory.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.FunctionType;
import com.streetfiresound.codegenerator.types.InterfaceType;

/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteMessageStreamInvocationFactory
{

  /**
   * Constructor for WriteAsynResponseInvocationFactory.
   */

  InterfaceType it;
	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  /**
   * Constructor for WriteRemoteInvocationFactory.
   */
  public WriteMessageStreamInvocationFactory(InterfaceType it) throws IOException
  {

    super();
    this.it = it;

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

//     String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.packagePath.replace('.', '/');
     String location = rootPath + "/" + packageName.replace('.','/') + "/" + system.replace('.', '/');

     File newfile = new File(location);
     if(!newfile.exists())
        newfile.mkdir();

      FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+CodeGenerator.currentPackage+ "MessageStreamInvocationFactory.java");

       outputToFile(fos);
       fos.close();


  }

/**
 * Method outputToFile.
 * @param os
 * @throws IOException
 */
  private void outputToFile(OutputStream os) throws IOException
  {

    printPackage(os);

    printImport(os);

    printClass(os);

    printOpenClass(os);

    printPrivate(os);

    printStatic(os);

    printConstructor(os);

    printCloseClass(os);


  }



/**
 * Method printPackage.
 * @param os
 * @throws IOException
 */
  private void printPackage(OutputStream os) throws IOException
  {
  	String pack = packageName + "." + system;
      os.write("package ".getBytes());
      os.write(pack.getBytes());
      os.write(";\n\n\n\n".getBytes());
  }

  private void printImport(OutputStream os) throws IOException
  {
   		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;
		String packPath = packageName + "." + system;


		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

        os.write("import ".getBytes());
        os.write(CodeGenerator.REDROCKETBASEPATH.getBytes());
        os.write(".rmi.*;\n\n".getBytes());


		if(!pathList.contains(constantPath))
		{
			os.write("import ".getBytes());
			os.write(constantPath.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(constantPath);
		}

		if(!pathList.contains(typePath))
		{
			os.write("import ".getBytes());
			os.write(typePath.getBytes());
			os.write(".*;\n\n".getBytes());
			pathList.add(typePath);
		}


		if(!pathList.contains(exceptionPath))
		{
			os.write("import ".getBytes());
			os.write(exceptionPath.getBytes());
			os.write(".*;\n\n".getBytes());
			pathList.add(exceptionPath);
		}


  }

/**
 * Method printClass.
 * @param os
 * @throws IOException
 */
  private void printClass(OutputStream os) throws IOException
  {
    os.write("public class ".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("MessageStreamInvocationFactory extends MessageStreamInvocationFactory implements Const".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("MessageStreamOperationId\n".getBytes());
  }

/**
 * Method printOpenClass.
 * @param os
 * @throws IOException
 */
  private void printOpenClass(OutputStream os) throws IOException
  {
    os.write("{\n".getBytes());
  }

/**
 * Method printCloseClass.
 * @param os
 * @throws IOException
 */
  private void printCloseClass(OutputStream os) throws IOException
  {
      os.write("\n}\n\n".getBytes());
  }


/**
 * Method printConstructor.
 * @param os
 * @throws IOException
 */
  private void printConstructor(OutputStream os) throws IOException
  {
        os.write("\tpublic ".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("MessageStreamInvocationFactory()\n".getBytes());
        os.write("\t{\n".getBytes());
        os.write("\t\t// Construct super class\n".getBytes());
        os.write("\t\tsuper(".getBytes());

        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("Constant.API_CODE, invocationTable);\n".getBytes());
        os.write("\t}\n\n\n".getBytes());

  }
/**
 * Method printPrivate.
 * @param os
 * @throws IOException
 */
  private void printPrivate(OutputStream os) throws IOException
  {
      os.write("\tprivate final static Class[] invocationTable = new Class[".getBytes());
      os.write(Integer.toString(CodeGenerator.messageStreamOpCodeList.size()).getBytes());
      os.write("];\n\n\n".getBytes());
  }




/**
 * Method printStatic.
 * @param os
 * @throws IOException
 */
  private void printStatic(OutputStream os) throws IOException
  {
    os.write("\tstatic\n".getBytes());

    os.write("\t{\n".getBytes());
    os.write("\t\t// Build remote invocation table\n".getBytes());


    Iterator iter = it.iterator();
    int loopCount =  0;



    while(iter.hasNext())
    {


      FunctionType ft = (FunctionType) iter.next();

      String className = CodeGenerator.makeFileName(ft.getTypeName());

      os.write("\t\tinvocationTable[".getBytes());
      os.write(((String) CodeGenerator.messageStreamOpCodeList.get(loopCount++)).getBytes());
      os.write(".getOperationId()] = ".getBytes());

//      os.write(ft.getTypeName().getBytes());
      os.write(className.getBytes());

      os.write("MessageStreamInvocation.class;\n".getBytes());


      if(loopCount == CodeGenerator.opcodeList.size())
        break;

    }
    os.write("\n\t}\n\n".getBytes());

  }


}
