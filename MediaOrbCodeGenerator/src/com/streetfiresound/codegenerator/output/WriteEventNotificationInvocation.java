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
 * $Id: WriteEventNotificationInvocation.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

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
import com.streetfiresound.codegenerator.types.HolderType;
import com.streetfiresound.codegenerator.types.SequenceType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteEventNotificationInvocation implements ConstTypeDefinition
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
   * Constructor for WriteEventNotificationInvocation.
   */
  public WriteEventNotificationInvocation(FunctionType ft) throws IOException
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

	className =  CodeGenerator.makeFileName(ft.getTypeName());



//      String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.packagePath.replace('.','/');
      String location = rootPath + "/" + packageName.replace('.','/') + "/" + system.replace('.','/');


//     FileOutputStream fos = new FileOutputStream(location+"/"+ft.getTypeName()+"EventNotificationInvocation.java");
     FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+className+"EventNotificationInvocation.java");

     outputToFile(fos);
     fos.close();
  }


  private void outputToFile(OutputStream os) throws IOException
  {
    printPackage(os);
    printImport(os);
    printClass(os);
    printOpenClass(os);
    printPrivate(os);
    printConstructor(os);
    printMarshal(os);
    printDispatch(os);
    printCloseClass(os);

  }


  private void printClass(OutputStream os) throws IOException
  {
    os.write("public class ".getBytes());

   // os.write(ft.getTypeName().getBytes());
    os.write(className.getBytes());

    os.write("EventNotificationInvocation extends EventNotificationInvocation\n".getBytes());
  }



   private void constructorWrite(HaviType dataType, String vName, OutputStream os, boolean array) throws IOException
   {
   		  	  HaviType type = dataType;

	          switch(type.getConstantTypeDef())
              {
    				case SEQUENCE:
    					type = ((SequenceType) dataType).getDataType();
    					constructorWrite(type, vName, os, true);
    					break;



                    case ENUM:
						localExceptionList.add("IOException");
						if(array == true)
						{
							os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" = new int[size];\n".getBytes());
							os.write("\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());


							os.write(vName.getBytes());
							os.write("[i] = hbais.readInt();\n".getBytes());

							os.write("\t\t\t}\n\n".getBytes());

						}
						else
						{
              			   os.write("\t\t\t".getBytes());
                           os.write(vName.getBytes());
                           os.write(" = hbais.readInt();\n".getBytes());
						}
						break;


                    case UNION:
						localExceptionList.add("HaviUnmarshallingException");
						if(array == true)
						{
							localExceptionList.add("IOException");
							os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());

                            os.write(vName.getBytes());
                            os.write("[i] = ".getBytes());
                            os.write(dataType.getTypeName().getBytes());
                            os.write(".create(hbais);\n".getBytes());

                            os.write("\t\t\t}\n\n".getBytes());

						}
						else
						{
                    		os.write("\t\t\t".getBytes());
                            os.write(vName.getBytes());
                            os.write(" = ".getBytes());
                            os.write(dataType.getTypeName().getBytes());
                            os.write(".create(hbais);\n".getBytes());
						}
                        break;


                    case STRUCT:
						localExceptionList.add("HaviUnmarshallingException");
						if(array == true)
						{
							localExceptionList.add("IOException");
							os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());

                            os.write(vName.getBytes());
                            os.write("[i] = new ".getBytes());
                            os.write(dataType.getTypeName().getBytes());
                            os.write("(hbais);\n".getBytes());

							os.write("\t\t\t}\n\n".getBytes());
						}
						else
						{
                    		os.write("\t\t\t".getBytes());
                            os.write(vName.getBytes());
                            os.write(" = new ".getBytes());
                            os.write(dataType.getTypeName().getBytes());
                            os.write("(hbais);\n".getBytes());
						}
                        break;


                    case UNIONSTRUCT:
						localExceptionList.add("HaviUnmarshallingException");
						if(array == true)
						{
							localExceptionList.add("IOException");
							os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
							os.write("\t\t\t\t{\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());

                    		os.write("hbais.readInt();\n\t\t\t\t\t".getBytes());
                            os.write(vName.getBytes());
                            os.write("[i] = new ".getBytes());
                            os.write(dataType.getTypeName().getBytes());
                            os.write("(hbais);\n".getBytes());

                            os.write("\t\t\t\t}\n".getBytes());

                            os.write("\t\t\t}\n\n".getBytes());

						}
						else
						{
                    		os.write("\t\t\t".getBytes());
                    		os.write("hbais.readInt();\n\t\t\t".getBytes());
                            os.write(vName.getBytes());
                            os.write(" = new ".getBytes());
                            os.write(dataType.getTypeName().getBytes());
                            os.write("(hbais);\n".getBytes());
						}
						break;



                        case LITERAL:
                       		HaviType newtype = (HaviType)CodeGenerator.dataTypeList.get(dataType.getTypeName());
                       		if(newtype == null)
                       		{
									localExceptionList.add("HaviUnmarshallingException");
									if(array == true)
									{
										localExceptionList.add("IOException");
										os.write("\t\t\t{\n".getBytes());

										os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
										os.write("\t\t\t\t".getBytes());
										os.write(vName.getBytes());
										os.write(" = new ".getBytes());
										os.write(type.getTypeName().getBytes());
										os.write("[size];\n".getBytes());

										os.write("\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
										os.write("\t\t\t\t\t".getBytes());

			                            os.write(vName.getBytes());
			                            os.write("[i] = new ".getBytes());
			                            os.write(dataType.getTypeName().getBytes());
			                            os.write("(hbais);\n".getBytes());

										os.write("\t\t\t}\n\n".getBytes());
									}
									else
									{
			                    		os.write("\t\t\t".getBytes());
			                            os.write(vName.getBytes());
			                            os.write(" = new ".getBytes());
			                            os.write(dataType.getTypeName().getBytes());
			                            os.write("(hbais);\n".getBytes());
									}

                       		}
							else
								constructorWrite(newtype, vName, os, array);
                    		break;



                    default:
						localExceptionList.add("IOException");
						if(array == true)
						{
							os.write("\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());
							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" = new ".getBytes());
							type.output(os);
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());

                            os.write(vName.getBytes());
                            os.write("[i] = hbais.read".getBytes());
                            os.write(((BaseDataType) dataType).getMarshalString().getBytes());
                            os.write("();\n".getBytes());

                            os.write("\t\t\t}\n\n".getBytes());

						}
						else
						{
                    	   os.write("\t\t\t".getBytes());
                           os.write(vName.getBytes());
                           os.write(" = hbais.read".getBytes());
                           os.write(((BaseDataType) dataType).getMarshalString().getBytes());
                           os.write("();\n".getBytes());
						}

						break;

              }

   }



  private void printConstructor(OutputStream os) throws IOException
  {

          os.write("\tpublic ".getBytes());

//          os.write(ft.getTypeName().getBytes());
          os.write(className.getBytes());

          os.write("EventNotificationInvocation(SEID posterSeid, HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());
          os.write("\t{\n".getBytes());
          os.write("\t\t// Construct super class\n".getBytes());


          os.write("\t\tsuper(posterSeid, ".getBytes());
          os.write(CodeGenerator.currentPackage.getBytes());
          os.write("Constant.SUCCESS);\n\n".getBytes());


      	//get the parameter list
	     Iterator iter = ft.iterator();


		 //print only if parameter list > 0
		 if(ft.getChildList().size() > 0)
		 {
	          os.write("\t\ttry\n".getBytes());
	          os.write("\t\t{\n".getBytes());
	          os.write("\t\t\t// Unmarshall\n".getBytes());
		 }

          while(iter.hasNext())
          {
                  HolderType ht = (HolderType) iter.next();
                  HaviType dataType = ht.getDataType();
			      constructorWrite(dataType, ht.getTypeName(),os, false);

         }  //end loop


		//print only if parameter list > 0
		 if(ft.getChildList().size() > 0)
	         os.write("\t\t}\n".getBytes());



		 Iterator exceptionIter = localExceptionList.iterator();
		 while(exceptionIter.hasNext())
		 {
		 	String exceptionName = (String)exceptionIter.next();
		 	os.write("\t\tcatch(".getBytes());
		 	os.write(exceptionName.getBytes());
		 	os.write(" e)\n".getBytes());
            os.write("\t\t{\n".getBytes());
            os.write("\t\t\tthrow new HaviUnmarshallingException(e.getMessage());\n".getBytes());
      		os.write("\t\t}\n\n".getBytes());
		 }

        os.write("\t}\n\n\n".getBytes());

        localExceptionList.clear();

  }


  private void privateWrite(HaviType dataType, OutputStream os) throws IOException
  {
		HaviType type = dataType;

        switch(type.getConstantTypeDef())
        {

           case SEQUENCE:
          	 	type = ((SequenceType) type).getDataType();
          		privateWrite(type, os);
          		break;


           case LITERAL:
           		HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(type.getTypeName());
           		if(newtype == null)
                    os.write(type.getTypeName().getBytes());
           		else
	           		privateWrite(newtype, os);
           		break;


            case ENUM:
              os.write("int".getBytes());
              break;

           case STRUCT:
           case UNION:
           case UNIONSTRUCT:
              os.write(type.getTypeName().getBytes());
              break;



           default:
            type.output(os);
            break;
        }




  }
  private void printPrivate(OutputStream os) throws IOException
  {
    Iterator iter = ft.iterator();
    while(iter.hasNext())
    {
        HolderType ht = (HolderType)iter.next();

        os.write("\t".getBytes());

		privateWrite(ht.getDataType(), os);


		if(ht.getDataType() instanceof SequenceType)
			os.write("[]".getBytes());

        os.write(" ".getBytes());
        os.write(ht.getTypeName().getBytes());
        os.write(";\n".getBytes());
    }

    os.write("\n\n".getBytes());

  }

  private void printMarshal(OutputStream os) throws IOException
  {

      os.write("\tpublic void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException\n".getBytes());
      os.write("\t{\n".getBytes());
      os.write("\t\t// Always fail\n".getBytes());
      os.write("\t\tthrow new HaviMarshallingException(\"nothing to marshall\");\n".getBytes());
      os.write("\t}\n\n\n".getBytes());
  }



  private void printDispatch(OutputStream os) throws IOException
  {

    os.write("\tpublic void dispatch(EventNotificationListener listener)\n".getBytes());
    os.write("\t{\n".getBytes());
    os.write("\t\t// Check type\n".getBytes());

    os.write("\t\tif (!(listener instanceof ".getBytes());


    os.write(className.getBytes());

    os.write("EventNotificationListener))\n".getBytes());

    os.write("\t\t{\n".getBytes());
    os.write("\t\t\t// Badness\n".getBytes());
    os.write("\t\t\tthrow new IllegalArgumentException(\"bad listener type\");\n".getBytes());
    os.write("\t\t}\n\n".getBytes());

    os.write("\t\t// Dispatch\n".getBytes());
    os.write("\t\t((".getBytes());

    String functionName = ft.getTypeName();


    os.write(className.getBytes());

    os.write("EventNotificationListener)listener).".getBytes());
    os.write(functionName.substring(0,1).toLowerCase().getBytes());
    os.write(functionName.substring(1).getBytes());
    os.write("EventNotification(posterSeid".getBytes());


    Iterator iter = ft.iterator();
    while(iter.hasNext())
    {
         os.write(", ".getBytes());

          HolderType ht = (HolderType)iter.next();
          os.write(ht.getTypeName().getBytes());


    }
    os.write(");\n\n".getBytes());
    os.write("\t}\n\n".getBytes());
  }



  private void printOpenClass(OutputStream os) throws IOException
  {
      os.write("{\n".getBytes());
  }

  private void printCloseClass(OutputStream os) throws IOException
  {
      os.write("}\n\n".getBytes());
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
		String packPath = packageName + "." + system;
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;


		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

  		Set set = CodeGenerator.projectList.entrySet();

        os.write("import java.io.*;\n\n".getBytes());

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




}
