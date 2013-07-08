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
 * $Id: WriteException.java,v 1.1 2005/02/22 03:46:08 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.streetfiresound.codegenerator.parser.CodeGenerator;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteException
{

    String exceptionName;
    String printName = "";


	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  /**
   * Constructor for WriteException.
   */
  public WriteException(String eName) throws IOException
  {
    super();

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");


    StringTokenizer st = new StringTokenizer(eName, "_");

    while(st.hasMoreTokens())
    {
        String temp = st.nextToken();
        printName +=  temp.substring(0,1)+ temp.substring(1).toLowerCase();
    }

    exceptionName = eName;



    String location = rootPath + "/" + packageName.replace('.','/') + "/" + exception.replace('.','/');
    FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+"Havi" + CodeGenerator.currentPackage+printName+"Exception.java");
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
      printInheritance(os);
      printOpenClass(os);
	  printPrivate(os);

      printConstructor1(os);
      printConstructor2(os);
      printConstructor3(os);
      printGetErrorCode(os);
      printGetStatus(os);
      printCloseClass(os);
  }


/**
 * Method printPackage.
 * @param os
 * @throws IOException
 */
  private void printPackage(OutputStream os) throws IOException
  {
	String exceptionPath=packageName+"."+exception;
    os.write("package ".getBytes());
    os.write(exceptionPath.getBytes());
    os.write(";\n\n".getBytes());


  }






/**
 * Method printPrivate.
 * @param os
 * @throws IOException
 */
  private void printPrivate(OutputStream os) throws IOException
  {
  	String pName = printName.substring(0,1).toLowerCase() + printName.substring(1);

  	os.write("\tprivate Status ".getBytes());
	os.write(pName.getBytes());
	os.write("Status;\n\n\n".getBytes());
  }


/**
 * Method printImport.
 * @param os
 * @throws IOException
 */
  private void printImport(OutputStream os) throws IOException
  {
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String packPath = packageName + "." + system;
		String exceptionPath=   packageName+"."+exception;
		ArrayList pathList = new ArrayList();
		pathList.add(packPath);



		//GENERAL CONSTANT PATH
		if(!pathList.contains(CodeGenerator.generalConstantPath))
		{
			os.write("import ".getBytes());
			os.write(CodeGenerator.generalConstantPath.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.generalConstantPath);
		}


		//GENERAL TYPE PATH
		if(!pathList.contains(CodeGenerator.generalTypePath))
		{
			os.write("import ".getBytes());
			os.write(CodeGenerator.generalTypePath.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.generalTypePath);
		}

		//GENERAL EXCEPTION PATH
		if(!pathList.contains(CodeGenerator.generalExceptionPath))
		{
			os.write("import ".getBytes());
			os.write(CodeGenerator.generalExceptionPath.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.generalExceptionPath);
		}


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

		os.write("\n\n".getBytes());


  }

/**
 * Method printClass.
 * @param os
 * @throws IOException
 */
  private void printClass(OutputStream os) throws IOException
  {

      os.write("public class Havi".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write(printName.getBytes());
      os.write("Exception ".getBytes());

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
 * Method printInheritance.
 * @param os
 * @throws IOException
 */
  private void printInheritance(OutputStream os) throws IOException
  {
    os.write("extends Havi".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("Exception\n".getBytes());

  }


/**
 * Method printConstructor1.
 * @param os
 * @throws IOException
 */
  private void printConstructor1(OutputStream os) throws IOException
  {
    os.write("\tpublic Havi".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write(printName.getBytes());
    os.write("Exception()\n".getBytes());
    os.write("\t{\n".getBytes());

    os.write("\t\tsuper(\"Havi".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write(printName.getBytes());
    os.write("Exception: unspecified message. \");\n".getBytes());
    os.write("\t}\n\n\n".getBytes());
  }

/**
 * Method printConstructor2.
 * @param os
 * @throws IOException
 */
  private void printConstructor2(OutputStream os) throws IOException
  {
    os.write("\tpublic Havi".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write(printName.getBytes());
    os.write("Exception( String msg )\n".getBytes());
    os.write("\t{\n".getBytes());

    os.write("\t\tsuper( msg );\n".getBytes());
    os.write("\t}\n\n\n".getBytes());

  }


	/**
	 * Method printConstructor3.
	 * @param os
	 * @throws IOException
	 */
	private void printConstructor3(OutputStream os) throws IOException
	{

		  	String pName = printName.substring(0,1).toLowerCase() + printName.substring(1);

		    os.write("\tpublic Havi".getBytes());
		    os.write(CodeGenerator.currentPackage.getBytes());
		    os.write(printName.getBytes());
		    os.write("Exception( Status status )\n".getBytes());
		    os.write("\t{\n".getBytes());

		    os.write("\t\tsuper();\n".getBytes());

			os.write("\t\tthis.".getBytes());
			os.write(pName.getBytes());
			os.write("Status = status;\n\n".getBytes());
		    os.write("\t}\n\n\n".getBytes());
	}

/**
 * Method printGetErrorCode.
 * @param os
 * @throws IOException
 */
  private void printGetErrorCode(OutputStream os) throws IOException
  {
      os.write("\tpublic final short getErrorCode()\n".getBytes());
      os.write("\t{\n".getBytes());
      os.write("\t\treturn ".getBytes());
      os.write(exceptionName.getBytes());
      os.write(";\n".getBytes());
      os.write("\t}\n\n\n".getBytes());

  }


/**
 * Method printGetStatus.
 * @param os
 * @throws IOException
 */
  private void printGetStatus(OutputStream os) throws IOException
  {
      String pName = printName.substring(0,1).toLowerCase() + printName.substring(1);
      os.write("\tpublic final Status getStatus()\n".getBytes());
      os.write("\t{\n".getBytes());
      os.write("\t\treturn ".getBytes());

  	  os.write(pName.getBytes());
  	  os.write("Status;\n".getBytes());
      os.write("\t}\n\n\n".getBytes());

  }





}

