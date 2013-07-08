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
 * $Id: writeEventNotificationListener.java,v 1.2 2005/02/24 03:03:37 stephen Exp $
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
import com.streetfiresound.codegenerator.types.SequenceType;


/**
 * @author george
 *
 */
public class writeEventNotificationListener implements ConstTypeDefinition
{

  FunctionType ft;

	String system;
	String constant;
	String packageName;
	String type;
	String exception;
	String className;


  /**
   * Constructor for writeEventNotificationListener.
   */
  public writeEventNotificationListener(FunctionType ft) throws IOException
  {
    super();
    this.ft = ft;

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

	className = CodeGenerator.makeFileName(ft.getTypeName());


      String location = rootPath +  "/" + packageName.replace('.','/') + "/" + system.replace('.','/');

       File newfile = new File(location);
       if(!newfile.exists())
          throw new IOException("Cannot create EventNotificationListener for "+className +" due to directory not found");


//     FileOutputStream fos = new FileOutputStream(location+"/"+ft.getTypeName()+"EventNotificationListener.java");
     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+className+"EventNotificationListener.java");

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


  }



/**
 * Method printPackage.
 * @param os
 * @throws IOException
 */
  private void printPackage(OutputStream os) throws IOException
  {
   		String pack = packageName +"." + system;
        os.write("package ".getBytes());
        os.write(pack.getBytes());
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
		String packPath = packageName + "." + system;
		String exceptionPath=   packageName+"."+exception;

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
      os.write("\n\n\n\n".getBytes());


  }



/**
 * Method interfaceWrite.
 * @param hType
 * @param os
 * @throws IOException
 */
  private void interfaceWrite(HaviType hType, OutputStream os) throws IOException
  {

            HaviType type = hType;

            switch(type.getConstantTypeDef())
            {

                case SEQUENCE:
                	type = ((SequenceType) type).getDataType();
                	interfaceWrite(type, os);
                	break;


                case ENUM:
                    os.write("int ".getBytes());
                    break;


                case STRUCT:
                case UNION:
                case UNIONSTRUCT:
                    os.write(type.getTypeName().getBytes());
                    break;


				case LITERAL:
               		HaviType newtype = (HaviType)CodeGenerator.dataTypeList.get(hType.getTypeName());
					if(newtype == null)
						os.write(type.getTypeName().getBytes());
					else
	                	interfaceWrite(newtype, os);

					break;


                default:  //base data type
                    type.output(os);
                    break;
            }//end switch

  }




/**
 * Method printInterface.
 * @param os
 * @throws IOException
 */
  private void printInterface(OutputStream os) throws IOException
  {
    os.write("\n\n".getBytes());
    os.write("public interface ".getBytes());

   // os.write(ft.getTypeName().getBytes());
    os.write(className.getBytes());

    os.write("EventNotificationListener extends EventNotificationListener\n".getBytes());
    os.write("{\n".getBytes());

    os.write("\tpublic final static Class SERVER_HELPER_CLASS = ".getBytes());
    os.write(className.getBytes());
    os.write("EventNotificationHelper.class;\n\n".getBytes());
/*
    os.write("\tpublic static Class INVOCATION_CLASS = ".getBytes());
    os.write(className.getBytes());
    os.write("EventNotificationInvocation.class;\n\n".getBytes());
*/

    os.write("\tpublic void ".getBytes());

    os.write(ft.getTypeName().substring(0,1).toLowerCase().getBytes());
    os.write(ft.getTypeName().substring(1).getBytes());
    os.write("EventNotification( SEID posterSeid".getBytes());

    Iterator iter = ft.iterator();
    while(iter.hasNext())
    {
            os.write(", ".getBytes());

            HolderType ht = (HolderType)iter.next();
            HaviType dataType = ht.getDataType();

            interfaceWrite(dataType, os);

            if(dataType instanceof SequenceType)
            	os.write("[]".getBytes());

          os.write(" ".getBytes());
          os.write(ht.getTypeName().getBytes());



    } //end while loop
    os.write(" );\n".getBytes());

    os.write("}\n\n".getBytes());
  }

}


