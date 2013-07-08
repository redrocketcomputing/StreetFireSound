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
 * $Id: WriteExceptionFactory.java,v 1.1 2005/02/22 03:46:08 stephen Exp $
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
public class WriteExceptionFactory
{

  /**
   * Constructor for WriteExceptionMaker.
   */

	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  public WriteExceptionFactory()  throws IOException
  {
    super();

  	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

//    String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.TYPEPATH.replace('.', '/');;
    String location = rootPath + "/" + packageName.replace('.','/') + "/" + system.replace('.', '/');;

    File newfile = new File(location);
    if(!newfile.exists())
		throw new IOException("Directory no found:"+ location);


     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+ CodeGenerator.currentPackage+"ExceptionFactory.java");
     outputToFile(fos);


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
//    printConstructor(os);
    printMakeFactory(os);
    printCloseClass(os);
  }










/**
 * Method printPackage.
 * @param os
 * @throws IOException
 */
  private void printPackage(OutputStream os) throws IOException
  {
	String systemPath=packageName+"."+system;
    os.write("package ".getBytes());

    os.write(systemPath.getBytes());
    os.write(";\n\n".getBytes());


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
		String exceptionPath = packageName+"."+exception;
		String packPath = packageName + "." + system;
		Set set = CodeGenerator.projectList.entrySet();


		ArrayList pathList = new ArrayList();
		pathList.add(packPath);


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
       // if(output.equalsIgnoreCase(driver.currentPackage))// || output.equalsIgnoreCase(driver.COMPARETYPE))
       //continue;

		String rmiPath = ((String) map.get("PACKAGE")) + "." + ((String) map.get("SYSTEM"));
		String rmiCons = ((String) map.get("PACKAGE")) + "." + ((String) map.get("CONSTANT"));
		String rmiType = ((String) map.get("PACKAGE")) + "." + ((String) map.get("TYPE"));
		String rmiException = ((String) map.get("PACKAGE")) + "." + ((String) map.get("EXCEPTION"));
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
		if(!pathList.contains(rmiException))
		{
			os.write("import ".getBytes());
			os.write(rmiException.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(rmiException);
		}

      }


    	os.write("\n\n\n".getBytes());
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
    os.write("ExceptionFactory extends ExceptionFactory\n".getBytes());

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
 * Method printConstructor.
 * @param os
 * @throws IOException
 */
  private void printConstructor(OutputStream os) throws IOException
  {
    os.write("\tpublic ExceptionFactory(){}\n\n\n".getBytes());

  }

/**
 * Method printMakeFactory.
 * @param os
 * @throws IOException
 */
  private void printMakeFactory(OutputStream os) throws IOException
  {
			//the content of excptionList will be remove at the end of loop in modulerule
            Iterator exceptionIter = CodeGenerator.exceptionList.iterator();

			String constantValue = (String)CodeGenerator.haviapi.get(CodeGenerator.currentPackage.toUpperCase());
	        String outputName = "\t\tshort APICODE = (short)" + constantValue + ";\n\n";

			String moduleName = CodeGenerator.currentPackage;


            os.write("\tpublic static Havi".getBytes());
            os.write(CodeGenerator.currentPackage.getBytes());
            os.write("Exception makeException(Status s)\n".getBytes());
            os.write("\t{\n".getBytes());
            os.write("\t\tHaviException exception = null;\n".getBytes());
						os.write(outputName.getBytes());

            os.write("\t\tString msg = \"API: \" + s.getApiCode() + \"; Error: \" + s.getErrCode();\n\n\n".getBytes());


//						os.write("\t\tif(s.getApiCode() != APICODE)\n".getBytes());
						os.write("\t\tif(s.getApiCode() != APICODE || (s.getApiCode() == ConstApiCode.ANY && s.getErrCode() > (short) 0x007f))\n".getBytes());
						os.write("\t\t{\n".getBytes());
						os.write("\t\t\tthrow new IllegalArgumentException(\"bad apicode: \" + s.getApiCode());\n".getBytes());
						os.write("\t\t}\n\n".getBytes());


                    os.write("\t\t\tswitch ( s.getErrCode())\n".getBytes());
                    os.write("\t\t\t{\n".getBytes());

                    while(exceptionIter.hasNext())
                    {
                          String codeName = (String) exceptionIter.next();

                          String fName = "";
                          StringTokenizer st = new StringTokenizer(codeName,"_");

                          while(st.hasMoreTokens())
                          {
                              String temp = st.nextToken();
                              fName +=  temp.substring(0,1).toUpperCase()+ temp.substring(1).toLowerCase();
                          }



                          os.write("\t\t\t\tcase Const".getBytes());
                          os.write(moduleName.getBytes());
                          os.write("ErrorCode.".getBytes());
                          os.write(codeName.getBytes());
                          os.write(":\n".getBytes());

												  os.write("\t\t\t\t\treturn new Havi".getBytes());
                          os.write(moduleName.getBytes());
                          os.write(fName.getBytes());
                          os.write("Exception(msg) ;\n\n".getBytes());
//                          os.write("\t\t\t\t\t\tbreak;\n".getBytes());
                    }


										os.write("\t\t\t\tdefault:\n".getBytes());
										os.write("\t\t\t\t\tthrow new IllegalArgumentException(\"bad error code:\" + s.getErrCode());\n\n".getBytes());


            os.write("\t\t\t}\t//end switch apidcode\n\n".getBytes());
          os.write("\t}\t//end function()\n\n".getBytes());

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


}
