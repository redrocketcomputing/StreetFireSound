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
 * $Id: WriteAsyncResponseListener.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.FunctionType;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.SequenceType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteAsyncResponseListener implements ConstTypeDefinition
{

  FunctionType ft;

	String constant;
	String system;
	String type;
	String packageName;
	String exception;

	String className;

  /**
   * Constructor for WriteAsyncResponseListener.
   */
  public WriteAsyncResponseListener(FunctionType ft) throws IOException
  {
    super();
    this.ft = ft;

	HashMap configInfo = (HashMap) CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");


	String location = rootPath+"/"+packageName.replace('.','/')+"/"+system.replace('.','/');
     File newfile = new File(location);
     if(!newfile.exists())
        newfile.mkdir();


    className = CodeGenerator.makeFileName(ft.getTypeName());


//     FileOutputStream fos = new FileOutputStream(location+"/"+ft.getTypeName()+"AsyncResponseListener.java");
     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+className+"AsyncResponseListener.java");
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

    printInterface(os);

    printOpenInterface(os);

    printHandle(os);

    printCloseInterface(os);

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
        os.write(";\n\n".getBytes());

  }


  private void printImport(OutputStream os) throws IOException
  {
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=packageName+"."+system;
		String exceptionPath=packageName+"."+exception;
		String packPath = packageName + "." + system;

		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

		Set set = CodeGenerator.projectList.entrySet();

        os.write("import ".getBytes());
        os.write(CodeGenerator.REDROCKETBASEPATH.getBytes());
        os.write(".rmi.*;\n\n".getBytes());

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

		//dcm constants, com.streetfiresound.codegenerator.types and system path
		if(!pathList.contains(CodeGenerator.DCM_PATH+".constants"))
		{
			os.write("import ".getBytes());
			os.write((CodeGenerator.DCM_PATH+".constants").getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.DCM_PATH+".constants");
		}
		if(!pathList.contains(CodeGenerator.DCM_PATH+".types"))
		{
			os.write("import ".getBytes());
			os.write((CodeGenerator.DCM_PATH+".types").getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.DCM_PATH+".types");
		}


		//fcm constants, type and system path
		if(!pathList.contains(CodeGenerator.FCM_PATH+".constants"))
		{
			os.write("import ".getBytes());
			os.write((CodeGenerator.FCM_PATH+".constants").getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.FCM_PATH+".constants");
		}
		if(!pathList.contains(CodeGenerator.FCM_PATH+".types"))
		{
			os.write("import ".getBytes());
			os.write((CodeGenerator.FCM_PATH+".types").getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.FCM_PATH+".types");
		}


      //get the iterator from the modulelist
      Iterator iter = set.iterator();

      //loop the the list
      while(iter.hasNext())
      {

        //get the module name
		Map.Entry entry = (Map.Entry) iter.next();
		String output = (String)entry.getKey();
		HashMap map = (HashMap) entry.getValue();

        //if module name is same as currentPackage name the skip
//        if(output.equalsIgnoreCase(CodeGenerator.currentPackage))// || output.equalsIgnoreCase(driver.COMPARETYPE))
//          continue;

		String rmiPath = ((String) map.get("PACKAGE")) + "." + ((String) map.get("SYSTEM"));
		String rmiCons = ((String) map.get("PACKAGE")) + "." + ((String) map.get("CONSTANT"));
		String rmiType = ((String) map.get("PACKAGE")) + "." + ((String) map.get("TYPE"));
		ArrayList importList = (ArrayList) map.get("IMPORT");
		if(importList != null)
		{
			Iterator importIter = importList.iterator();
			while(importIter.hasNext())
			{
				String path= (String)importIter.next();
				os.write("import ".getBytes());
				os.write(path.getBytes());
				os.write(";\n".getBytes());
			}
			os.write("\n".getBytes());
		}

		if(!pathList.contains(rmiPath))
		{
	        os.write("import ".getBytes());
	        os.write(rmiPath.getBytes());
	        os.write(".*;\n".getBytes());
	        pathList.add(rmiPath);
		}
		if(!pathList.contains(rmiCons))
		{
	        os.write("import ".getBytes());
	        os.write(rmiCons.getBytes());
	        os.write(".*;\n".getBytes());
	        pathList.add(rmiCons);
		}
		if(!pathList.contains(rmiType))
		{
	        os.write("import ".getBytes());
	        os.write(rmiType.getBytes());
	        os.write(".*;\n".getBytes());
	        pathList.add(rmiType);
		}

      }
      os.write("\n\n".getBytes());

  }

/**
 * Method printInterface.
 * @param os
 * @throws IOException
 */
  private void printInterface(OutputStream os) throws IOException
  {
      os.write("public interface ".getBytes());

//      os.write(ft.getTypeName().getBytes());
      os.write(className.getBytes());

      os.write("AsyncResponseListener extends AsyncResponseListener\n".getBytes());
  }

  private void printOpenInterface(OutputStream os) throws IOException
  {
      os.write("{\n".getBytes());
  }




/**
 * Method printDataType.
 * @param type
 * @param os
 * @throws IOException
 */
  private void printDataType(HaviType type, OutputStream os, boolean array) throws IOException
  {

          switch( type.getConstantTypeDef())
          {
			  case SEQUENCE:
					type = ((SequenceType) type).getDataType();
					printDataType(type, os, true);
					break;

              case ENUM:
                os.write("int".getBytes());
                if(array == true)
                	os.write("[]".getBytes());
                break;

              case LITERAL:
                  HaviType h = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());
                  if(h == null)
                  {
		                os.write(type.getTypeName().getBytes());
		                if(array == true)
		                	os.write("[]".getBytes());
                  }
				  else
	                  printDataType(h, os, array);

                  break;


              case STRUCT:
              case UNION:
              case UNIONSTRUCT:
                os.write(type.getTypeName().getBytes());
                if(array == true)
                	os.write("[]".getBytes());
                break;


              default:  //base data tyep
                type.output(os);
                if(array == true)
                	os.write("[]".getBytes());
                break;

          }

  }




/**
 * Method printHandle.
 * @param os
 * @throws IOException
 */
  private void printHandle(OutputStream os) throws IOException
  {

	String fName = ft.getTypeName().substring(0,1).toUpperCase()+ft.getTypeName().substring(1);

      os.write("\tpublic void".getBytes());

      os.write(" handle".getBytes());

      os.write(fName.getBytes());

      os.write("(int TransactionId, ".getBytes());


      HaviType hType =  ((FunctionType) ft).getReturnType();


     if(hType.getConstantTypeDef() != VOID)
     {
	      printDataType(hType, os, false);
	      os.write(" result, ".getBytes());
     }

	 os.write("Status returnCode);\n".getBytes());



  }

/**
 * Method printCloseInterface.
 * @param os
 * @throws IOException
 */
  private void printCloseInterface(OutputStream os) throws IOException
  {

      os.write("}\n".getBytes());
  }




}
