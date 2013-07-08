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
 * $Id: WriteClient.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ArrayType;
import com.streetfiresound.codegenerator.types.BaseDataType;
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.EnumType;
import com.streetfiresound.codegenerator.types.FunctionType;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.HolderType;
import com.streetfiresound.codegenerator.types.InterfaceType;
import com.streetfiresound.codegenerator.types.SequenceType;
import com.streetfiresound.codegenerator.types.VoidType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteClient  implements ConstTypeDefinition
{

  InterfaceType it;
//  String timeout = driver.MSGSENDSYNC_TIMEOUT;

	String system;
	String constant;
	String packageName;
	String type;
	String exception;


	HashSet localExceptionList;

  /**
   * Constructor for WriteClient.
   */
  public WriteClient( InterfaceType it) throws IOException
  {
    super();

	localExceptionList = new HashSet();

    this.it = it;
	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

//    String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.packagePath.replace('.', '/');
    String location = rootPath +  "/" + packageName.replace('.','/') + "/" + system.replace('.', '/');

     File newfile = new File(location);
     if(!newfile.exists())
        newfile.mkdir();

      FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+CodeGenerator.currentPackage+ "Client.java");

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

      printConstructor1(os);

      printConstructor2(os);

      printFunction(os);

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
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;
		String packPath = packageName + "." + system;



		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

		Set set = CodeGenerator.projectList.entrySet();


      os.write("import ".getBytes());
      os.write(CodeGenerator.JAVA_IO.getBytes());
      os.write(";\n".getBytes());

      os.write("import ".getBytes());
      os.write(CodeGenerator.JAVA_UTIL.getBytes());
      os.write(";\n".getBytes());

        os.write("\n".getBytes());
        os.write("import ".getBytes());
        os.write(CodeGenerator.REDROCKETBASEPATH.getBytes());
        os.write(".rmi.*;\n\n".getBytes());

	    os.write("import ".getBytes());
	    os.write(CodeGenerator.SOFTWARE_ELEMENT_PATH.getBytes());
	    os.write(".*;\n".getBytes());


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
      os.write("\n\n\n\n".getBytes());

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
      os.write("Client extends HaviClient implements Const".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("OperationId\n".getBytes());


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
        os.write("\n}\n\n\n".getBytes());
   }

/**
 * if this module name contains in the SoftwareElementType constant list then print out this constructor
 *
 * Method printConstructor1.
 * @param os
 * @throws IOException
 */
   private void printConstructor1(OutputStream os) throws IOException
   {
      ArrayList al = (ArrayList)CodeGenerator.constList.get("SoftwareElementType");

    if(al == null)
    {
		try
		{
			CodeGenerator.makeTypeIdList("SoftwareElementType");
			al = (ArrayList) CodeGenerator.constList.get("SoftwareElementType")	;

		}
		catch(FileNotFoundException e)
		{
			throw new IOException("WriteEnumSystemEventConstant - SoftwareElementType  not found in constList");
		}
    }




      boolean found = false;
      Iterator iter = al.iterator();
      while(iter.hasNext())
      {
      	ConstType ct = (ConstType) iter.next();


		if(ct.getTypeName().equalsIgnoreCase(CodeGenerator.currentPackage) && Long.decode(ct.getValue()).longValue() <= 6)
        {
           found = true;
           break;
        }
      }




      if(found == true)
      {
          os.write("\tpublic ".getBytes());
          os.write(CodeGenerator.currentPackage.getBytes());
          os.write("Client(SoftwareElement se) throws HaviMsgException, Havi".getBytes());
	      os.write(CodeGenerator.currentPackage.getBytes());
	      os.write("Exception\n".getBytes());

          os.write("\t{\n".getBytes());
          os.write("\t\t// Construct super class\n".getBytes());
          os.write("\t\tsuper(se, se.getSystemSeid(se.getSeid(), ConstSoftwareElementType.".getBytes());
          os.write(CodeGenerator.currentPackage.toUpperCase().getBytes());
          os.write("));\n".getBytes());
          os.write("\t}\n".getBytes());
      }

      os.write("\n\n".getBytes());

   }

/**
 * Method printConstructor2.
 * @param os
 * @throws IOException
 */
   private void printConstructor2(OutputStream os) throws IOException
   {
      os.write("\tpublic ".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("Client(SoftwareElement se, SEID destSeid) throws HaviMsgException, Havi".getBytes());

      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("Exception\n".getBytes());

      os.write("\t{\n".getBytes());
      os.write("\t\tsuper(se, destSeid);\n".getBytes());
      os.write("\t}\n\n\n".getBytes());


   }


/**
 * print out the function from the interface of Idl file.  Each funtion contains sync and async part
 *
 * Method printFunction.
 * @param os
 * @throws IOException
 */
   private void printFunction(OutputStream os) throws IOException
   {

      Iterator iter = it.iterator();
      int loopCount = 0;
      while(iter.hasNext())
      {
        FunctionType ft = (FunctionType) iter.next();

        makeFunction(ft, os, loopCount,"");
        makeFunction(ft, os, loopCount++,"Sync");

      }

      os.write("\n\n".getBytes());
      os.write("\tprivate void throwReturnCode(Status returnCode) throws Havi".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("Exception\n".getBytes());


      os.write("\t{\n".getBytes());
      os.write("\t\tthrow new Havi".getBytes());

      os.write(CodeGenerator.currentPackage.getBytes());
	  os.write("UnidentifiedFailureException();\n".getBytes());


      os.write("\t}\n\n".getBytes());
  }



  private void makeFunction(FunctionType ft, OutputStream os, int loopCount, String sync) throws IOException
  {

      printFunctionSignature(os,sync, ft);
       os.write("\t{\n".getBytes());
      printFunctionBody(os, sync, ft, loopCount);

      os.write("\n\t}\n\n\n".getBytes());

  }




/**
 * Method printDataType.
 * @param type
 * @param os
 * @throws IOException
 */
  private void printDataType(HaviType type, OutputStream os) throws IOException
  {
    switch(type.getConstantTypeDef())
    {

        case ENUM:
            os.write("int".getBytes());
            break;

        case SEQUENCE:
        	//get the datatype and call printdatatype again
            HaviType h1 = ((SequenceType) type).getDataType();
            printDataType(h1, os);
            break;


        case LITERAL:

            HaviType h = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());

            if(h == null)
	            os.write(type.getTypeName().getBytes());

			else if(h instanceof SequenceType)
               os.write(type.getTypeName().getBytes());
            else
                printDataType(h, os);

            break;


        case STRUCT:
        case UNION:
        case UNIONSTRUCT:
            os.write(type.getTypeName().getBytes());
            break;

        default:  //base data type
            type.output(os);
            break;

    }

  }







/**
 * Method printFunctionSignature.
 * @param os
 * @param sync
 * @param ft
 * @throws IOException
 */
  private void printFunctionSignature(OutputStream os, String sync, FunctionType ft) throws IOException
  {
      String functionName = ft.getTypeName().substring(0,1).toLowerCase() + ft.getTypeName().substring(1);

      os.write("\tpublic ".getBytes());

      HaviType returnType = ft.getReturnType();

      //print return type
      if(sync.length() > 0)
      {
                printDataType(returnType, os);

                if(returnType instanceof SequenceType)
                  os.write("[]".getBytes());
      }
      else
        os.write("int".getBytes());





      os.write(" ".getBytes());
      os.write(functionName.getBytes());

      os.write(sync.getBytes());
      os.write("(".getBytes());

      Iterator iter = ft.iterator();

	//if this is a sync function than print out the timeout
      if(sync.length() > 0)
      {
        os.write("int timeout".getBytes());
        if(iter.hasNext())
          os.write(", ".getBytes());
      }


    //print parameter list
      while(iter.hasNext())
      {
          HolderType ht = (HolderType) iter.next();

          HaviType dataType = ht.getDataType();
          printDataType(dataType, os);

          if(ht.getDataType() instanceof SequenceType)
            os.write("[]".getBytes());


          os.write(" ".getBytes());
          os.write(ht.getTypeName().getBytes());    //variable name

          if(iter.hasNext())
            os.write(", ".getBytes());
      }

      os.write(") ".getBytes());
      printFunctionThrows(os, sync);


  }


/**
 * Method printFunctionThrows.
 * @param os
 * @param sync
 * @throws IOException
 */
  private void printFunctionThrows(OutputStream os, String sync) throws IOException
  {

      os.write("throws HaviMsgException, Havi".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("Exception\n".getBytes());

  }







	/**
	 * print out the marhsalling method
	 *
	 * Method FunctionBodyWrite1.
	 * @param dataType
	 * @param vName
	 * @param os
	 * @param array
	 * @throws IOException
	 */
     private void FunctionBodyWrite1(HaviType dataType, String vName, OutputStream os, boolean array) throws IOException
     {


          switch( dataType.getConstantTypeDef())
          {
              case ENUM:
              	localExceptionList.add("IOException");
              	  if(array ==  true || dataType instanceof ArrayType)
              	  {
						os.write("\n\t\t\thbaos.writeInt(".getBytes());
						os.write(vName.getBytes());
						os.write(".length);\n".getBytes());

                        os.write("\t\t\tfor(int i = 0; i < ".getBytes());
                        os.write(vName.getBytes());
                        os.write(".length; i++)\n".getBytes());
                        os.write("\t\t\t{\n".getBytes());

 	                  	os.write("\t\t\t\thbaos.writeInt(".getBytes());
		                os.write(vName.getBytes());
		                os.write("[i]);\n".getBytes());
                        os.write("\t\t\t}\n\n\n".getBytes());

              	  }
              	  else
              	  {
	                  os.write("\t\t\thbaos.writeInt".getBytes());
	                  os.write("(".getBytes());
	                  os.write(vName.getBytes());
	                  os.write(");\n".getBytes());
              	  }
                  break;


              case STRUCT:
              case UNION:
              case UNIONSTRUCT:
				  localExceptionList.add("HaviMarshallingException");

                  if(array == true || dataType instanceof ArrayType)
                  {
	 				  	localExceptionList.add("IOException");
						os.write("\n\t\t\thbaos.writeInt(".getBytes());
						os.write(vName.getBytes());
						os.write(".length);\n".getBytes());

                        os.write("\t\t\tfor(int i = 0; i < ".getBytes());
                        os.write(vName.getBytes());
                        os.write(".length; i++)\n".getBytes());
                        os.write("\t\t\t{\n".getBytes());

                        os.write("\t\t\t\t".getBytes());
                        os.write(vName.getBytes());
                        os.write("[i].marshal(hbaos);\n".getBytes());
                        os.write("\t\t\t}\n\n\n".getBytes());

                  }
                  else
                  {
	                  	os.write("\t\t\t".getBytes());
                        os.write(vName.getBytes());
                        os.write(".marshal(hbaos);\n".getBytes());
                  }
                  break;


			case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(dataType.getTypeName());
				if(newtype == null)
				{
					  localExceptionList.add("HaviMarshallingException");
	                  if(array == true || dataType instanceof ArrayType)
	                  {
  						  	localExceptionList.add("IOException");

							os.write("\n\t\t\thbaos.writeInt(".getBytes());
							os.write(vName.getBytes());
							os.write(".length);\n".getBytes());

	                        os.write("\t\t\tfor(int i = 0; i < ".getBytes());
	                        os.write(vName.getBytes());
	                        os.write(".length; i++)\n".getBytes());
	                        os.write("\t\t\t{\n".getBytes());

	                        os.write("\t\t\t\t".getBytes());
	                        os.write(vName.getBytes());
	                        os.write("[i].marshal(hbaos);\n".getBytes());
	                        os.write("\t\t\t}\n\n\n".getBytes());

	                  }
	                  else
	                  {
		                  	os.write("\t\t\t".getBytes());
	                        os.write(vName.getBytes());
	                        os.write(".marshal(hbaos);\n".getBytes());
	                  }

				}
				else
					FunctionBodyWrite1(newtype, vName,os, array);


				break;




              default:
				  localExceptionList.add("IOException");
                  if(array == true || dataType instanceof ArrayType)
                  {
						os.write("\n\t\t\thbaos.writeInt(".getBytes());
						os.write(vName.getBytes());
						os.write(".length);\n".getBytes());

                        os.write("\t\t\tfor(int i = 0; i < ".getBytes());
                        os.write(vName.getBytes());
                        os.write(".length; i++)\n".getBytes());
                        os.write("\t\t\t{\n".getBytes());

                        os.write("\t\t\t\t".getBytes());
                        os.write("hbaos.write".getBytes());
                        os.write(((BaseDataType) dataType).getMarshalString().getBytes());
                        os.write("(".getBytes());
                        os.write(vName.getBytes());
                        os.write("[i]);\n".getBytes());
                        os.write("\t\t\t}\n\n\n".getBytes());
                  }
                  else
                  {
                          os.write("\t\t\thbaos.write".getBytes());
                          os.write(((BaseDataType) dataType).getMarshalString().getBytes());
                          os.write("(".getBytes());
                          os.write(vName.getBytes());
                          os.write(");\n".getBytes());

                  }

          }


     }  //end function




	/**
	 * print out the unmarshalling method
	 *
	 *
	 * Method FunctionBodyWrite2.
	 * @param type
	 * @param os
	 * @param array
	 * @throws IOException
	 */
     private void FunctionBodyWrite2(HaviType type, OutputStream os, boolean array) throws IOException
     {
          HaviType dataType = type;


          switch( dataType.getConstantTypeDef())
          {


              case ENUM:
					localExceptionList.add("IOException");
					os.write("\t\t\t".getBytes());
                    if(array == true)
                    {

                        os.write("int[] table;\n".getBytes());
                        os.write("\n\t\t\t{\n".getBytes());

                          os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
                          os.write("\t\t\t\ttable = new int[size];\n".getBytes());
                          os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
                          os.write("\t\t\t\t\ttable[i] =  hbais.readInt();\n".getBytes());
                        os.write("\t\t\t}\n\n".getBytes());

                    }
                    else
                          os.write("int table =  hbais.readInt();\n".getBytes());
                  break;

              case SEQUENCE:
                  dataType =   ((SequenceType) dataType).getDataType();
                  FunctionBodyWrite2(dataType, os, true);
                  break;


              case STRUCT:
					localExceptionList.add("HaviUnmarshallingException");
					os.write("\t\t\t".getBytes());
                    if(array == true)
                    {
						localExceptionList.add("IOException");
                        os.write(dataType.getTypeName().getBytes());
                        os.write("[] table;\n".getBytes());
                        os.write("\n\t\t\t{\n".getBytes());

                          os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
                          os.write("\t\t\t\ttable = new ".getBytes());
                          os.write(dataType.getTypeName().getBytes());
                          os.write("[size];\n".getBytes());
                          os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
                          os.write("\t\t\t\t\ttable[i] = new ".getBytes());
                          os.write(dataType.getTypeName().getBytes());
                          os.write("(hbais);\n".getBytes());
                        os.write("\t\t\t}\n\n".getBytes());

                    }
                    else
                    {
                        os.write(dataType.getTypeName().getBytes());
                        os.write(" table = ".getBytes());
                        os.write("new ".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write("(hbais);\n".getBytes());
                    }
                    break;


              case LITERAL:
                    HaviType h = (HaviType)CodeGenerator.dataTypeList.get(dataType.getTypeName());
					os.write("\t\t\t".getBytes());
					if(h == null)
                    {
						localExceptionList.add("HaviUnmarshallingException");
                        if(array == true)
                        {

	    					  localExceptionList.add("IOException");

	                          os.write(dataType.getTypeName().getBytes());
	                          os.write("[] table;\n".getBytes());
	                          os.write("\n\t\t\t{\n".getBytes());

                              os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
                              os.write("\t\t\t\ttable = new ".getBytes());
                              os.write(dataType.getTypeName().getBytes());
                              os.write("[size];\n".getBytes());
                              os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
                              os.write("\t\t\t\t\ttable[i] = new ".getBytes());
                              os.write(dataType.getTypeName().getBytes());
	                          os.write("(hbais);\n".getBytes());
                            os.write("\t\t\t}\n\n".getBytes());

                        }
                        else
                        {
                            os.write(dataType.getTypeName().getBytes());
                            os.write(" table = ".getBytes());
                            os.write("new ".getBytes());
                            os.write(dataType.getTypeName().getBytes());
                            os.write("(hbais);\n".getBytes());
                        }
                    }

                    else
                    	FunctionBodyWrite2(h, os, array);

                    break;


              case UNION:
				  localExceptionList.add("HaviUnmarshallingException");
				  os.write("\t\t\t".getBytes());
                  if(array == true)
                  {
						localExceptionList.add("IOException");

                        os.write(dataType.getTypeName().getBytes());
                        os.write("[] table;\n".getBytes());

                        os.write("\n\t\t\t{\n".getBytes());
                        os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
                        os.write("\t\t\t\ttable = new ".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write("[size];\n".getBytes());

                        os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
                        os.write("\t\t\t\t\ttable[i] = ".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write(".create(hbais);\n".getBytes());
                        os.write("\t\t\t}\n\n".getBytes());

                  }
                  else
                  {
                      os.write(dataType.getTypeName().getBytes());
                      os.write(" table = ".getBytes());
                      os.write(dataType.getTypeName().getBytes());
                      os.write(".create(hbais);\n".getBytes());
                  }
                  break;


              case UNIONSTRUCT:
					localExceptionList.add("HaviUnmarshallingException");
					os.write("\t\t\t".getBytes());
                    if(array == true)
                    {
						localExceptionList.add("IOException");
                        os.write(dataType.getTypeName().getBytes());
                        os.write("[] table;\n".getBytes());
                        os.write("\n\t\t\t{\n".getBytes());

                          os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
                          os.write("\t\t\t\ttable = new ".getBytes());
                          os.write(dataType.getTypeName().getBytes());
                          os.write("[size];\n".getBytes());
                          os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
                          os.write("\t\t\t\t{\n".getBytes());
						  os.write("\t\t\t\t\thbais.readInt();\n".getBytes());
                          os.write("\t\t\t\t\ttable[i] = new ".getBytes());
                          os.write(dataType.getTypeName().getBytes());
                          os.write("(hbais);\n\n".getBytes());
                          os.write("\t\t\t\t}\n".getBytes());
                        os.write("\t\t\t}\n\n".getBytes());
                    }
                    else
                    {
                    	os.write("hbais.readInt();\n\t\t\t".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write(" table = ".getBytes());
                        os.write("new ".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write("(hbais);\n".getBytes());
                    }
                    break;



              default:
				  localExceptionList.add("IOException");
				  os.write("\t\t\t".getBytes());
                  if(array == true)
                  {
                        dataType.output(os);
                        os.write("[] table;\n".getBytes());

                        os.write("\n\t\t\t{\n".getBytes());
                          os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
                          os.write("\t\t\t\ttable = new ".getBytes());
                          dataType.output(os);
                          os.write("[size];\n".getBytes());
                          os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
                          os.write("\t\t\t\t\ttable[i] = hbais.read".getBytes());
                          os.write(((BaseDataType) dataType).getMarshalString().getBytes());
                          os.write("();\n".getBytes());
                        os.write("\t\t\t}\n\n".getBytes());
                  }
                  else
                  {
                      dataType.output(os);
                      os.write(" table = hbais.read".getBytes());
                      os.write(((BaseDataType) dataType).getMarshalString().getBytes());
                      os.write("();\n".getBytes());
                  }
                  break;

          }


     }  // end function


	/**
	 * Method printReturn.
	 * @param type
	 * @param os
	 * @throws IOException
	 */
    private void printReturn(HaviType type, OutputStream os) throws IOException
    {

             switch(type.getConstantTypeDef())
            {
                case ENUM:
                    os.write("\t\t\treturn 0;\n".getBytes());
                    break;

                case STRUCT:
                case UNION:
                case STRING:
                case WSTRING:
                case SEQUENCE:
                    os.write("\t\t\treturn null;\n".getBytes());
                    break;

                case BOOLEAN:
                    os.write("\t\t\treturn false;\n".getBytes());
                    break;

                case LITERAL:
                  HaviType h = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());
                  if(h instanceof EnumType)
                        os.write("\t\t\treturn 0;\n".getBytes());
                  else
                        os.write("\t\t\treturn null;\n".getBytes());

                   break;



                default:  // base data type
                    os.write("\t\t\treturn 0;\n".getBytes());
            }//end switch
    }







	/**
	 * Each function contains at least one part. That is marshalling.So we must call FunctionBodyWrite1. If the function
	 * contains return type then we must FunctionBodyWrite2 for unmarshalling
	 *
	 * Method printFunctionBody.
	 * @param os
	 * @param sync
	 * @param ft
	 * @param loopCount
	 * @throws IOException
	 */
    private void printFunctionBody(OutputStream os, String sync, FunctionType ft, int loopCount) throws IOException
    {
//            boolean marshal = false;

            os.write("\t\ttry\n".getBytes());
            os.write("\t\t{\n".getBytes());

            os.write("\t\t\t// Create output stream\n".getBytes());
            os.write("\t\t\tHaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();\n\n".getBytes());

            os.write("\t\t\t// Marshall\n".getBytes());


            Iterator iter = ft.iterator();

//            if(iter.hasNext())
//              marshal = true;


      		//send request
            while(iter.hasNext())
            {
                    HolderType ht = (HolderType) iter.next();

                    HaviType dataType = ht.getDataType();
                    boolean array = false;

                    if(dataType instanceof SequenceType)
                    {
                        dataType = ((SequenceType) dataType).getDataType();
                        array = true;
                    }

                    FunctionBodyWrite1(dataType, ht.getTypeName(), os, array);
            }



            String returnString="return ";

            if(sync.length() > 0)
                returnString = "byte[] result = ";

            os.write("\n\t\t\t// Send the request\n".getBytes());
            os.write("\t\t\t".getBytes());
            os.write(returnString.getBytes());
            os.write("se.msgSendRequest".getBytes());
            os.write(sync.getBytes());
            os.write("(destSeid, ".getBytes());

            os.write(((String) CodeGenerator.opcodeList.get(loopCount)).getBytes());


            if(sync.length() > 0)
                os.write(", timeout".getBytes());

            os.write(", hbaos.toByteArray());\n\n".getBytes());


			//if contains return type then call FunctionBodyWrite2 for unmarshalling
            if(!(ft.getReturnType() instanceof VoidType) && sync.length() > 0)
            {
                os.write("\t\t\t//Unmarshal\n".getBytes());
                os.write("\t\t\tHaviByteArrayInputStream hbais = new HaviByteArrayInputStream(result);\n".getBytes());

                HaviType returnType  = ft.getReturnType();

                 FunctionBodyWrite2(returnType, os, false);

                 os.write("\t\t\treturn table;\n\n".getBytes());
                 os.write("\t\t}\n\n".getBytes());

            }
            else
                 os.write("\t\t}\n\n".getBytes());

/*
            if(sync.length() > 0)
            {
                  os.write("\t\tcatch (HaviMsgRemoteApiException e)\n".getBytes());
                  os.write("\t\t{\n".getBytes());
                  os.write("\t\t\t// Forward\n".getBytes());
                  os.write("\t\t\tthrowReturnCode(e.getStatus());\n\n".getBytes());


                  if(!(ft.getReturnType() instanceof VoidType) && sync.length() > 0)
                  {
                        os.write("\t\t\t// Will never reach here\n".getBytes());

                        HaviType returnType = ft.getReturnType();

                        printReturn(returnType, os);

                  }
                  os.write("\t\t}\n".getBytes());

            }
*/
            if(sync.length() > 0)
            {
	            os.write("\t\tcatch (HaviMsgRemoteApiException e)\n".getBytes());
	            os.write("\t\t{\n".getBytes());
	            os.write("\t\t\t// Translate\n".getBytes());
				os.write("\t\t\tthrow ".getBytes());
	            os.write(CodeGenerator.currentPackage.getBytes());
	            os.write("ExceptionFactory.makeException(e.getStatus());\n".getBytes());
	            os.write("\t\t}\n\n".getBytes());

            }


            os.write("\t\tcatch (HaviMsgException e)\n".getBytes());
            os.write("\t\t{\n".getBytes());
            os.write("\t\t\tthrow e;\n".getBytes());
            os.write("\t\t}\n\n".getBytes());


			Iterator exceptionIter = localExceptionList.iterator();
			while(exceptionIter.hasNext())
			{
				String exceptionName = (String) exceptionIter.next();

				os.write("\t\tcatch (".getBytes());
				os.write(exceptionName.getBytes());
				os.write(" e)\n".getBytes());
				os.write("\t\t{\n".getBytes());
	            os.write("\t\t\t// Translate\n".getBytes());
	            os.write("\t\t\tthrow new Havi".getBytes());
	            os.write(CodeGenerator.currentPackage.getBytes());
				os.write("UnidentifiedFailureException(e.toString());\n".getBytes());
	            os.write("\t\t}\n\n".getBytes());
			}

			localExceptionList.clear();

     }




}
