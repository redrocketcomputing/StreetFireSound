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
 * $Id: WriteModuleException.java,v 1.1 2005/02/22 03:46:08 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.streetfiresound.codegenerator.parser.CodeGenerator;




/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteModuleException
{

  /**
   * Constructor for writeModuleException.
   */

  String exceptionName;

	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  public WriteModuleException() throws IOException
  {
    super();


	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

    String location = rootPath + "/" + packageName.replace('.','/') + "/" + exception.replace('.','/');

    exceptionName = CodeGenerator.currentPackage;

    File newfile = new File(location);
    if(!newfile.exists())
        newfile.mkdir();


     if(exceptionName.equals("Havi"))
        exceptionName ="";



     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/Havi"+ exceptionName + "Exception.java");
     outputToFile(fos);
     fos.close();


  }


  private void outputToFile(OutputStream os) throws IOException
  {
    printPackage(os);
    printImport(os);
    printClass(os);
    printOpenClass(os);
    printConstructor1(os);
    printConstructor2(os);
    printGetApiCode(os);
    printGetErrorCode(os);


    if(exceptionName.length() == 0)
       printMakeStatus(os);
    printCloseClass(os);


  }

  private void printPackage(OutputStream os) throws IOException
  {
	String exceptionPath=packageName+"."+exception;

    os.write("package ".getBytes());
    os.write(exceptionPath.getBytes());
    os.write(";\n\n".getBytes());

  }

  private void printImport(OutputStream os) throws IOException
  {

		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String packPath = packageName + "." + exception;
		String exceptionPath=   packageName+"."+exception;

		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

		Set set = CodeGenerator.projectList.entrySet();


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


     os.write("\n\n\n\n".getBytes());
  }





  private void printClass(OutputStream os) throws IOException
  {
      os.write("public abstract class Havi".getBytes());

      os.write(exceptionName.getBytes());
      os.write("Exception ".getBytes());

      printExtend(os);

      if(exceptionName.length() > 0)
      {
          os.write("implements Const".getBytes());
          os.write(CodeGenerator.currentPackage.getBytes());
          os.write("ErrorCode\n".getBytes());
      }
      os.write("\n".getBytes());

  }


  private void printExtend(OutputStream os) throws IOException
  {

        os.write("extends ".getBytes());

        if(exceptionName.length() > 0)
          os.write("Havi".getBytes());

//        os.write(exceptionName.getBytes());
        os.write("Exception ".getBytes());

  }



  private void printOpenClass(OutputStream os) throws IOException
  {
      os.write("{\n".getBytes());

  }

  private void printConstructor1(OutputStream os) throws IOException
  {

      os.write("\tpublic Havi".getBytes());
      os.write(exceptionName.getBytes());
      os.write("Exception()\n".getBytes());
      os.write("\t{\n".getBytes());
      os.write("\t\tsuper();\n".getBytes());
      os.write("\t}\n\n".getBytes());

  }


    private void printConstructor2(OutputStream os) throws IOException
    {
        os.write("\tpublic Havi".getBytes());
        os.write(exceptionName.getBytes());
        os.write("Exception( String msg )\n".getBytes());
        os.write("\t{\n".getBytes());
        os.write("\t\tsuper(msg);\n".getBytes());
        os.write("\t}\n\n".getBytes());

    }


  private void printGetApiCode(OutputStream os) throws IOException
  {

      if(exceptionName.length() > 0)
      {
        os.write("\tpublic final short getApiCode()\n".getBytes());
        os.write("\t{\n".getBytes());
        os.write("\t\treturn ConstApiCode.".getBytes());
        os.write(CodeGenerator.currentPackage.toUpperCase().getBytes());
        os.write(";\n".getBytes());

        os.write("\t}\n\n".getBytes());
      }
      else
        os.write("\tpublic abstract short getApiCode();\n\n\n".getBytes());
  }


  private void printGetErrorCode(OutputStream os) throws IOException
  {

        os.write("\tpublic abstract short getErrorCode();\n\n\n".getBytes());
  }


  private void printCloseClass(OutputStream os) throws IOException
  {
      os.write("\n}\n\n".getBytes());
  }




  private void printMakeStatus(OutputStream os) throws IOException
  {

      os.write("\tpublic final Status makeStatus()\n".getBytes());
      os.write("\t{\n".getBytes());
      os.write("\t\treturn new Status(getApiCode(), getErrorCode());\n".getBytes());
      os.write("\t}\n\n\n".getBytes());

  }




}

