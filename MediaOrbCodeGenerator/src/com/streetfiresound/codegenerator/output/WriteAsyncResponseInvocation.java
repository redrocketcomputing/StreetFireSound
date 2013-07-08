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
 * $Id: WriteAsyncResponseInvocation.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.File;
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
import com.streetfiresound.codegenerator.types.BaseDataType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.FunctionType;
import com.streetfiresound.codegenerator.types.HaviType;
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
public class WriteAsyncResponseInvocation implements ConstTypeDefinition
{

  FunctionType ft;

  	String system;
	String constant;
	String packageName;
	String type;
	String exception;
	String className;

	HashSet localExceptionList;

  /**
   * Constructor for WriteAsyncResponseInvocation.
   */
  public WriteAsyncResponseInvocation(FunctionType ft) throws IOException
  {
    super();
    this.ft = ft;
	localExceptionList = new HashSet();

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");


    String location = rootPath + "/" + packageName.replace('.','/') + "/" + system.replace('.', '/');
     File newfile = new File(location);
     if(!newfile.exists())
		throw new IOException("WriteAsyncResponseInvocation error - cannot found directory:"+ location);


	className = CodeGenerator.makeFileName(ft.getTypeName());


     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+className+"AsyncResponseInvocation.java");


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

    printDispatch(os);

    printCloseClass(os);


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
  		String packPath = packageName + "." + system;
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;
		Set set = CodeGenerator.projectList.entrySet();

		ArrayList pathList = new ArrayList();
		pathList.add(packPath);


	    os.write("import java.io.IOException;\n\n".getBytes());
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
      os.write("\n".getBytes());


  }



/**
 * Method printClass.
 * @param os
 * @throws IOException
 */
  private void printClass(OutputStream os) throws IOException
  {
      os.write("public class ".getBytes());
//      os.write(ft.getTypeName().getBytes());
      os.write(className.getBytes());

      os.write("AsyncResponseInvocation extends AsyncResponseInvocation\n".getBytes());

  }






/**
 * Method printDataType.
 * @param ht
 * @param os
 * @throws IOException
 */

  private void printDataType(HaviType ht, OutputStream os, boolean array) throws IOException
  {
		HaviType type = ht;

        switch(type.getConstantTypeDef())
        {
            case ENUM:
                os.write("int".getBytes());
                if(array == true)
                	os.write("[]".getBytes());

                break;

            case SEQUENCE:
                type = ((SequenceType) ht).getDataType();
                printDataType(type, os, true);
                break;

            case STRUCT:
            case UNION:
            case UNIONSTRUCT:
                os.write(type.getTypeName().getBytes());
                if(array == true)
                	os.write("[]".getBytes());

                break;


            case LITERAL:
                HaviType newtype =  (HaviType) CodeGenerator.dataTypeList.get(ht.getTypeName());
                if(newtype == null)
                {
	                os.write(type.getTypeName().getBytes());
	                if(array == true)
	                	os.write("[]".getBytes());
                }
                else
	                printDataType(newtype, os, array);

                break;


            default:
                ht.output(os);
                if(array == true)
                	os.write("[]".getBytes());

                break;

        }



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

  private void printPrivate(OutputStream os) throws IOException
  {

      if( !(ft.getReturnType() instanceof VoidType))
      {
          HaviType ht = ft.getReturnType();

          os.write("\t".getBytes());

          printDataType(ht,os, false);

          os.write(" result; \n\n\n".getBytes());

      }

  }


/**
 * Method constructorWrite.
 * @param dataType
 * @param os
 * @throws IOException
 */
  private void constructorWrite(HaviType dataType, OutputStream os, boolean array) throws IOException
  {
    	HaviType type = dataType;


          switch( type.getConstantTypeDef())
          {
                case ENUM:
					localExceptionList.add("IOException");
                	if(array ==  true)
                	{
						os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\tresult = new int[size];\n".getBytes());
							os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());


							os.write("\t\t\t\t\tresult[i] = hbais.readInt();\n".getBytes());

						os.write("\t\t\t}\n\n".getBytes());
                	}
                	else
                	{
	                    os.write("\t\t\tresult = hbais.readInt();\n\n".getBytes());
                	}
                    break;

                case SEQUENCE:
                    type = ((SequenceType) dataType).getDataType();
                    constructorWrite(type, os, true);
                    break;


                case STRUCT:
					localExceptionList.add("HaviUnmarshallingException");
                	if(array ==  true)
                	{
						localExceptionList.add("IOException");
						os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\tresult = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());
							os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());

							os.write("\t\t\t\t\tresult[i] = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("(hbais);\n".getBytes());

						os.write("\t\t\t}\n\n".getBytes());
                	}
                	else
                	{

                      os.write("\n\t\t\t".getBytes());
                      os.write(" result = new ".getBytes());
                      os.write(dataType.getTypeName().getBytes());
                      os.write("(hbais);\n\n".getBytes());
                	}
                    break;


                case UNION:
					localExceptionList.add("HaviUnmarshallingException");
                	if(array ==  true)
                	{
						localExceptionList.add("IOException");
						os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\tresult = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());
							os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());


		                      os.write("\t\t\t\t\tresult[i] = ".getBytes());
		                      os.write(dataType.getTypeName().getBytes());
		                      os.write(".create(hbais);\n".getBytes());

						os.write("\t\t\t}\n\n".getBytes());
                	}
                	else
                	{
                      os.write("\n\t\t\tresult = ".getBytes());
                      os.write(dataType.getTypeName().getBytes());
                      os.write(".create(hbais);\n\n".getBytes());
                	}
                    break;

                case UNIONSTRUCT:
					localExceptionList.add("HaviUnmarshallingException");
					localExceptionList.add("IOException");
                	if(array ==  true)
                	{
						os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\tresult = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());
							os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
							os.write("\t\t\t\t{\n".getBytes());

		                      os.write("\n\t\t\t\t\thbais.readInt();\n".getBytes());
		                      os.write("\t\t\t\t\tresult[i] = new ".getBytes());
		                      os.write(dataType.getTypeName().getBytes());
		                      os.write("(hbais);\n\n".getBytes());

							os.write("\t\t\t\t}\n".getBytes());
						os.write("\t\t\t}\n\n".getBytes());
                	}
                	else
                	{
                      os.write("\n\t\t\thbais.readInt();\n".getBytes());
                      os.write("\t\t\tresult = new ".getBytes());
                      os.write(dataType.getTypeName().getBytes());
                      os.write("(hbais);\n\n".getBytes());
                	}
                    break;

                case LITERAL:
                    HaviType newtype = (HaviType)CodeGenerator.dataTypeList.get(dataType.getTypeName());
                    if(newtype == null)
                    {
							localExceptionList.add("HaviUnmarshallingException");
		                	if(array ==  true)
		                	{
								localExceptionList.add("IOException");
								os.write("\t\t\t{\n".getBytes());

									os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
									os.write("\t\t\t\tresult = new ".getBytes());
									os.write(type.getTypeName().getBytes());
									os.write("[size];\n".getBytes());
									os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());

									os.write("\t\t\t\t\tresult[i] = new ".getBytes());
									os.write(type.getTypeName().getBytes());
									os.write("(hbais);\n".getBytes());

								os.write("\t\t\t}\n\n".getBytes());
		                	}
		                	else
		                	{
		                      os.write("\n\t\t\t".getBytes());
		                      os.write(" result = new ".getBytes());
		                      os.write(dataType.getTypeName().getBytes());
		                      os.write("(hbais);\n\n".getBytes());
		                	}

                    }
					else
	                    constructorWrite(newtype, os, array);

                    break;


                default: //base data type
					localExceptionList.add("IOException");
                	if(array ==  true)
                	{
						os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\tresult = new ".getBytes());

							type.output(os);
							os.write("[size];\n".getBytes());
							os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());

							os.write("\t\t\t\t\tresult[i] = hbais.read".getBytes());
							os.write(((BaseDataType) type).getMarshalString().getBytes());
							os.write("();\n".getBytes());

						os.write("\t\t\t}\n\n".getBytes());
                	}
                	else
                	{
                      os.write("\t\t\tresult = hbais.read".getBytes());
                      os.write( ((BaseDataType) dataType).getMarshalString().getBytes());
                      os.write("();\n\n".getBytes());
                	}
                    break;
          } //end switch


  }


/**
 * Method printConstructor.
 * @param os
 * @throws IOException
 */
  private void printConstructor(OutputStream os) throws IOException
  {

          os.write("\tpublic ".getBytes());

          os.write(className.getBytes());
          //os.write(ft.getTypeName().getBytes());

          os.write("AsyncResponseInvocation(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());
          os.write("\t{\n".getBytes());


           os.write("\t\tsuper(hbais);\n".getBytes());
           
           // Check status and only unmarshal if sucess
           os.write("\t\tif (returnCode.getErrCode() != 0)\n".getBytes());
           os.write("\t\t{\n".getBytes());
           os.write("\t\t\treturn;\n".getBytes());
           os.write("\t\t}\n\n".getBytes());

            //HaviType hType = ((FunctionType) ft).getReturnType();
            HaviType hType = ft.getReturnType();

            if(!(hType instanceof VoidType))
            {

                      os.write("\t\ttry\n".getBytes());
                      os.write("\t\t{\n".getBytes());

                      constructorWrite(hType, os, false);

                        os.write("\t\t}\n".getBytes());

						Iterator exceptionIter = localExceptionList.iterator();
						while(exceptionIter.hasNext())
						{
							String exceptionName = (String) exceptionIter.next();

							os.write("\t\tcatch(".getBytes());
							os.write(exceptionName.getBytes());
							os.write(" e)\n".getBytes());
	                        os.write("\t\t{\n".getBytes());
	                        os.write("\t\t\tthrow new HaviUnmarshallingException(e.getMessage());\n".getBytes());
	                        os.write("\t\t}\n\n".getBytes());
						}
              }

            os.write("\t}\n".getBytes());
			localExceptionList.clear();

  }

/**
 * Method printDispatch.
 * @param os
 * @throws IOException
 */
  private void printDispatch(OutputStream os) throws IOException
  {
//		String fName =  ft.getTypeName().substring(0,1).toUpperCase()+ ft.getTypeName().substring(1);

	  String className = CodeGenerator.makeFileName(ft.getTypeName());



      os.write("\tpublic void dispatch(int transactionId, AsyncResponseListener listener)\n".getBytes());
      os.write("\t{\n".getBytes());

      os.write("\t\t// Check interface\n".getBytes());
      os.write("\t\tif (!(listener instanceof ".getBytes());


//      os.write(ft.getTypeName().getBytes());
      os.write(className.getBytes());


      os.write("AsyncResponseListener))\n".getBytes());
      os.write("\t\t{\n".getBytes());
      os.write("\t\t\t//Bad argument\n".getBytes());
      os.write("\t\t\tthrow new IllegalArgumentException(\"bad listener type\");\n".getBytes());
      os.write("\t\t}\n\n".getBytes());

      os.write("\t\t// Cast it up\n\t\t".getBytes());

//      os.write(ft.getTypeName().getBytes());
      os.write(className.getBytes());

      os.write("AsyncResponseListener handler = (".getBytes());



//      os.write(ft.getTypeName().getBytes());
      os.write(className.getBytes());

      os.write("AsyncResponseListener) listener;\n\n".getBytes());

      os.write("\t\t// Dispatch\n".getBytes());


      os.write("\t\t".getBytes());

      os.write("handler.handle".getBytes());

//      os.write(fName.getBytes());
      os.write(className.getBytes());

      os.write("(transactionId, ".getBytes());

      if(!(((FunctionType) ft).getReturnType() instanceof VoidType))
         os.write("result, ".getBytes());

      os.write("returnCode);\n".getBytes());


      os.write("\t}\n\n".getBytes());
  }


  private void printCloseClass(OutputStream os) throws IOException
  {
    os.write("}\n\n".getBytes());
  }

}
