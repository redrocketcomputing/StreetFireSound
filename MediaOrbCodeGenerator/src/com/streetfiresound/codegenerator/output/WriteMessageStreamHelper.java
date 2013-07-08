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
 * $Id: WriteMessageStreamHelper.java,v 1.1 2005/02/22 03:46:08 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class WriteMessageStreamHelper
{

	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  /**
   * Constructor for WriteAsyncResponseInvocationFactory.
   */
  public WriteMessageStreamHelper() throws IOException
  {
    super();


	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");

	String rootPath = (String)configInfo.get("ROOTPATH");

    //String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.packagePath.replace('.', '/');
    String location = rootPath + "/"+ packageName.replace('.','/') + "/" + system.replace('.', '/');


     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+CodeGenerator.currentPackage+ "MessageStreamHelper.java");

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

/**
 * Method printImport.
 * @param os
 * @throws IOException
 */
   private void printImport(OutputStream os) throws IOException
   {
		String packPath = packageName + "." + system;
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


		  	  Set set = CodeGenerator.projectList.entrySet();
		      Iterator iter = set.iterator();

		      //loop the the list
		      while(iter.hasNext())
		      {

		        //get the module name
				Map.Entry entry = (Map.Entry) iter.next();
				String output = (String)entry.getKey();
				HashMap map = (HashMap) entry.getValue();

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
 * Method printClass.
 * @param os
 * @throws IOException
 */
   private void printClass(OutputStream os) throws IOException
   {
        os.write("public class ".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("MessageStreamHelper extends MessageStreamHelper\n".getBytes());

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
      os.write("MessageStreamHelper() throws HaviMsgListenerExistsException, Havi".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("Exception\n".getBytes());
      os.write("\t{\n".getBytes());

      os.write("\t\t// Construct super class\n".getBytes());
      os.write("\t\tsuper(invocationFactory);\n".getBytes());
      os.write("\t}\n\n\n".getBytes());


   }



/**
 * Method printPrivate.
 * @param os
 * @throws IOException
 */
   private void printPrivate(OutputStream os) throws IOException
   {
      os.write("\tprivate final static ".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("MessageStreamInvocationFactory invocationFactory = new ".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("MessageStreamInvocationFactory();\n\n\n".getBytes());


   }





}
