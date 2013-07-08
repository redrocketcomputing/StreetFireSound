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
 * $Id: WriteEventConstant.java,v 1.2 2005/02/24 03:03:37 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.FunctionType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteEventConstant
{

  /**
   * Constructor for WriteSystemEventType.
   */
  public WriteEventConstant()
  {
    super();
  }


    int value;

    String prefix = "\tpublic final static ";

    HashMap constMap;
	ArrayList aList;
	String eventType;
	String eventValue;


	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  /**
   * Constructor for writeErrorCode.
   */
  public WriteEventConstant(ArrayList aList, HashMap constantMap, String eType, String eValue) throws IOException
  {
    super();

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

	this.constMap = constantMap;
	this.aList = aList;
	this.eventType = eType;
	this.eventValue= eValue;

    String location = rootPath + "/" + packageName.replace('.','/') + "/" + constant.replace('.', '/');

     File newfile = new File(location);


//	System.out.println("write vendor eventconstant 1, package="+ CodeGenerator.currentPackage + "    location="+ location+"/"+ eventType);
     if(!newfile.exists())
		throw new IOException("Cannot create " + CodeGenerator.currentPackage+eType +"EventConstant due to directory not found:" + location);

//	System.out.println("write vendor eventconstant 2");



      FileOutputStream fos = new GplHeaderFileOutputStream(location+"/Const"+CodeGenerator.currentPackage+eventType+"EventConstant.java");

       outputToFile(fos);

       fos.close();


  }

  private void outputToFile(OutputStream os) throws IOException
  {

      printPackage(os);

      printImport(os);

      printInterface(os);

      printOpenInterface(os);


      printContent2(os);

//      printContent3(os);

      printCloseInterface(os);

  }

  private void printPackage(OutputStream os) throws IOException
  {
	String constantPath=packageName+"."+constant;
    os.write("package ".getBytes());
    os.write(constantPath.getBytes());
    os.write(";\n\n\n\n".getBytes());

  }


  private void printInterface(OutputStream os) throws IOException
  {
        os.write("public interface Const".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write(eventType.getBytes());
        os.write("EventConstant".getBytes());
        os.write("\n".getBytes());

  }




  private void printContent2(OutputStream os) throws IOException
  {

        Iterator iter = aList.iterator();



        while(iter.hasNext())
        {
			FunctionType ft = (FunctionType)iter.next();
			String searchName = this.makeSearchConstantName(ft.getTypeName());
			ConstType ct = (ConstType) constMap.get(searchName);


			if(ct != null)
			{
				String vName = ct.getTypeName();
				String value = ct.getValue();


	            String outputName = prefix + "byte[] " + vName + "_value = { 0,0,0," + eventValue +",0,0,0," +  Integer.decode(value) + "};\n";
	            os.write(outputName.getBytes());
			}
			else
				System.err.println("WriteSystemEventConstant - constant name not found:" + searchName);

        }


  }

  private void printContent3(OutputStream os) throws IOException
  {

        os.write("\n\n".getBytes());

		Iterator iter = aList.iterator();


		String constantValue = (String)CodeGenerator.haviapi.get(CodeGenerator.currentPackage.toUpperCase());	//012303



        while(iter.hasNext())
        {

			FunctionType ft = (FunctionType) iter.next();


			String searchName = this.makeSearchConstantName(ft.getTypeName());

			ConstType ct = (ConstType) constMap.get(searchName);
			if(ct != null)
			{
				String constantName= ct.getTypeName();

	            String temp ="";
    	        temp = prefix + "EventId " + constantName + "_EVENT_ID = EventId.create(" + constantName + "_value);\n";
                os.write(temp.getBytes());
			}
			else
				System.err.println("WriteSystemEventConstant- constant name not found: " + ct.getTypeName());

        }



  }



  private void printOpenInterface(OutputStream os) throws IOException
  {
    os.write("{\n".getBytes());

  }
  private void printCloseInterface(OutputStream os) throws IOException
  {
    os.write("\n}\n\n".getBytes());

  }


  private void printImport(OutputStream os ) throws IOException
  {

    	String packPath = packageName + "." + system;
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;


		String eventManagerpath =CodeGenerator.GENERAL_PACKAGE+".eventmanager.types";


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


      os.write("\n\n\n".getBytes());
  }


	  private String makeSearchConstantName(String iname)
	  {
	  	StringTokenizer st = new StringTokenizer(iname, "_");

	  	String newName ="";
	  	while(st.hasMoreTokens())
	  		newName += (String) st.nextToken();

		return newName.toUpperCase();
	  }


}

