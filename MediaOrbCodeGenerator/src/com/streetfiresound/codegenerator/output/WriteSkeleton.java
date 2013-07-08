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
 * $Id: WriteSkeleton.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
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
import com.streetfiresound.codegenerator.types.HolderType;
import com.streetfiresound.codegenerator.types.InterfaceType;
import com.streetfiresound.codegenerator.types.SequenceType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteSkeleton  implements ConstTypeDefinition
{

    InterfaceType it;
	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  /**
   * Constructor for WriteSkeleton.
   */
  public WriteSkeleton(InterfaceType it) throws IOException
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

//    String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.packagePath.replace('.','/');
    String location = rootPath + "/" + packageName.replace('.','/') + "/" + system.replace('.','/');

       File newfile = new File(location);
       if(!newfile.exists())
          newfile.mkdir();


     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+it.getInterfaceHeaderType().getTypeName()+"Skeleton.java");
     outputToFile(fos);
     fos.close();



  }


  private void outputToFile(OutputStream os) throws IOException
  {

      printPackage(os);

      printImport(os);

      printInterface(os);

      printInheritance(os);

      printOpenInterface(os);

      printContent(os);

      printCloseInterface(os);


  }

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
		String systemPath=   packageName+"."+system;
		String packPath = packageName + "." + system;
		String exceptionPath = packageName + "." + exception;

		ArrayList pathList = new ArrayList();
		pathList.add(packPath);



		Set set = CodeGenerator.projectList.entrySet();


        os.write("import ".getBytes());
        os.write(CodeGenerator.REDROCKETBASEPATH.getBytes());
        os.write(".rmi.*;\n".getBytes());

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


		//dcm constants and com.streetfiresound.codegenerator.types path
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


		//fcm constants and type path
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

      os.write("\n\n".getBytes());


  }



  private void printInterface(OutputStream os) throws IOException
  {
    os.write("public interface ".getBytes());
    os.write(it.getInterfaceHeaderType().getTypeName().getBytes());

    os.write("Skeleton".getBytes());


  }

  private void printInheritance(OutputStream os) throws IOException
  {
      os.write(" extends RemoteSkeleton\n".getBytes());

  }

  private void printOpenInterface(OutputStream os) throws IOException
  {
    os.write("{\n".getBytes());

  }

  private void printCloseInterface(OutputStream os) throws IOException
  {
    os.write("\n}\n".getBytes());

  }



  private void printDataType(HaviType dataType, OutputStream os) throws IOException
  {

      HaviType type = dataType;


      switch( type.getConstantTypeDef())
      {
          case ENUM:
            os.write("int".getBytes());
            break;


          case STRUCT:
          case UNION:
          case UNIONSTRUCT:
            os.write(type.getTypeName().getBytes());
            break;


          case SEQUENCE:
            type = ((SequenceType) type).getDataType();
            printDataType(type, os);
            break;


          case LITERAL:
            HaviType h = (HaviType) CodeGenerator.dataTypeList.get(type.getTypeName());

			if(h == null)
              os.write(type.getTypeName().getBytes());
            else
            	printDataType(h, os);
            break;

          default:
            type.output(os);
            break;

      }//end switch


  }


/**
 * Print the interface content
 *
 * Method printContent.
 * @param os
 * @throws IOException
 */
  private void printContent(OutputStream os) throws IOException
  {
            Iterator iter = it.iterator();

            while(iter.hasNext())
            {
                    HaviType fType = (HaviType) iter.next();

                    if(fType instanceof FunctionType)
                    {
                            os.write("\tpublic ".getBytes());

                           //((FunctionType) fType).getReturnType().output(os);
                             HaviType returnType = ((FunctionType) fType).getReturnType();
                             printDataType(returnType, os);


                             if(returnType instanceof SequenceType)
                                os.write("[]".getBytes());

                           os.write(" ".getBytes());


                           os.write(fType.getTypeName().substring(0,1).toLowerCase().getBytes());
                           os.write(fType.getTypeName().substring(1).getBytes());
                           os.write("(".getBytes());

                           Iterator holdList = ((FunctionType) fType).iterator();
                           while(holdList.hasNext())
                           {
                               HolderType hType = (HolderType) holdList.next();
                               HaviType dataType = hType.getDataType();

                               printDataType(dataType, os);

                               if(dataType instanceof SequenceType)
                                  os.write("[]".getBytes());

                              os.write(" ".getBytes());
                              os.write(hType.getTypeName().getBytes());

                              if(holdList.hasNext())
                                  os.write(", ".getBytes());
                          }


                          os.write(") throws Havi".getBytes());
                          os.write(it.getInterfaceHeaderType().getTypeName().getBytes());
                          os.write("".getBytes());
                          os.write("Exception;\n".getBytes());


                      }//end if
            }//end while

  }//end function




}
