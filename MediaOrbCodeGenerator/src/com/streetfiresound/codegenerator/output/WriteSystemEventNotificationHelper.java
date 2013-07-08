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
 * $Id: WriteSystemEventNotificationHelper.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
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
import com.streetfiresound.codegenerator.types.ConstType;
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
public class WriteSystemEventNotificationHelper implements ConstTypeDefinition
{

  FunctionType ft;
	String constantName;

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
  public WriteSystemEventNotificationHelper(FunctionType ft, HashMap constantMap) throws IOException
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


	  constantName = getConstantName(constantMap);
		if(constantName.length() == 0)
			throw new IOException("WriteSystemEventNotificationHelper error: constantName not found");



    String location = rootPath + "/" + packageName.replace('.','/') + "/" + system.replace('.','/');

		if(checkLocation(location) == false)
			throw new IOException("WriteSystemEventNotificationHelper error: Directory not exist:" +location);


    FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+className+"EventNotificationHelper.java");

    outputToFile(fos);
    fos.close();
  }


	private boolean checkLocation(String location)
	{
		 File file = new File(location);
		 return  file.exists();
	}



  private void outputToFile(OutputStream os) throws IOException
  {
    printPackage(os);
    printImport(os);
    printClass(os);
    printOpenClass(os);
    printConstructor1(os);
    printConstructor2(os);
		printReceiveNotification(os);
    printAddListener(os);
    printRemoveListener(os);
    printCloseClass(os);

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

		//import softwarelement package
		os.write("import org.havi.system.SoftwareElement;\n".getBytes());

		//import loggersingleton package
		os.write("import com.redrocketcomputing.util.log.*;\n".getBytes());



			//get the list of all package in this project
			Set set = CodeGenerator.projectList.entrySet();
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
	private void printClass(OutputStream os) throws IOException
	{
		os.write("\tpublic class ".getBytes());
		os.write(className.getBytes());
		os.write("EventNotificationHelper extends SystemEventNotificationHelper\n".getBytes());

	}
	private void printOpenClass(OutputStream os) throws IOException
	{
		os.write("\t{\n".getBytes());
		os.write("\t\tpublic final static short EVENT_BASE = ConstSystemEventType.".getBytes());
    os.write(constantName.getBytes());
    os.write(";\n".getBytes());
	}


	private void printConstructor1(OutputStream os) throws IOException
	{
		os.write("\t\tpublic ".getBytes());
		os.write(className.getBytes());
		os.write("EventNotificationHelper(SoftwareElement softwareElement, OperationCode opCode) throws HaviMsgException\n".getBytes());
  	os.write("\t\t{\n".getBytes());
  	os.write("\t\t\tsuper(softwareElement, opCode, ConstSystemEventType.".getBytes());
  	os.write(constantName.getBytes());
  	os.write(");\n".getBytes());
  	os.write("\t\t}\n\n\n".getBytes());
	}


	private void printConstructor2(OutputStream os) throws IOException
	{
  	os.write("\t\tpublic ".getBytes());
  	os.write(className.getBytes());
  	os.write("EventNotificationHelper(SoftwareElement softwareElement, OperationCode opCode, ".getBytes());
  	os.write(className.getBytes());
  	os.write("EventNotificationListener listener) throws HaviMsgException\n".getBytes());
  	os.write("\t\t{\n".getBytes());
  	os.write("\t\t\tsuper(softwareElement, opCode, ConstSystemEventType.".getBytes());
  	os.write(constantName.getBytes());
		os.write(");\n".getBytes());
		os.write("\t\t\taddListener(listener);\n".getBytes());
		os.write("\t\t}\n\n\n".getBytes());
	}



	private void printReceiveNotification(OutputStream os) throws IOException
	{
		String listenerFunction = className.substring(0,1).toLowerCase() + className.substring(1);

		os.write("\t\tpublic boolean receiveSystemEventNotification(SEID posterSeid, SystemEventId eventId, HaviByteArrayInputStream payload)\n".getBytes());
		os.write("\t\t{\n".getBytes());

		printDeclareVariable(os);
		printUnmarshalling(os);


		os.write("\t\t\t\t// Get listeners\n".getBytes());
		os.write("\t\t\t\t".getBytes());
		os.write(className.getBytes());
		os.write("EventNotificationListener[] listeners = (".getBytes());
		os.write(className.getBytes());
		os.write("EventNotificationListener[])listenerSet.toArray(new ".getBytes());
		os.write(className.getBytes());
		os.write("EventNotificationListener[listenerSet.size()]);\n\n".getBytes());

		os.write("\t\t\t\t// Dispatch\n".getBytes());
		os.write("\t\t\t\tfor (int i = 0; i < listeners.length; i++)\n".getBytes());
		os.write("\t\t\t\t{\n".getBytes());
		os.write("\t\t\t\t\tlisteners[i].".getBytes());

		os.write(listenerFunction.getBytes());

		os.write("EventNotification(posterSeid".getBytes());
		printFunctionParameter(os);
		os.write(");\n".getBytes());


		os.write("\t\t\t\t}\n\n".getBytes());


		os.write("\t\t\t\t// Return handles based on the number of dispatched listeners\n".getBytes());
		os.write("\t\t\t\treturn listeners.length != 0;\n".getBytes());


		os.write("\t\t}\n\n\n".getBytes());
	}



	private void printAddListener(OutputStream os) throws IOException
	{
		os.write("\t\tpublic void addListener(".getBytes());
		os.write(className.getBytes());
		os.write("EventNotificationListener listener)\n".getBytes());
		os.write("\t\t{\n".getBytes());
		os.write("\t\t\t// Add the listener\n".getBytes());
		os.write("\t\t\tlistenerSet.add(listener);\n".getBytes());
		os.write("\t\t}\n\n\n".getBytes());
	}
	private void printRemoveListener(OutputStream os) throws IOException
	{
		os.write("\t\tpublic void removeListener(".getBytes());
		os.write(className.getBytes());
		os.write("EventNotificationListener listener)\n".getBytes());
		os.write("\t\t{\n".getBytes());
		os.write("\t\t\t// Add the listener\n".getBytes());
		os.write("\t\t\tlistenerSet.remove(listener);\n".getBytes());
		os.write("\t\t}\n\n\n".getBytes());

	}
	private void printCloseClass(OutputStream os) throws IOException
	{
		os.write("\n\t}\n\n".getBytes());
	}




	private String getConstantName(HashMap constantMap)
	{
				ConstType ct = (ConstType) constantMap.get(className.toUpperCase());


				String constantName ="";
				if(ct != null)
				{
						constantName = ct.getTypeName();
				}

				return constantName;
	}


	private void printFunctionParameter(OutputStream os) throws IOException
	{
		Iterator iter = ft.iterator();

		while(iter.hasNext())
		{
    	  os.write(", ".getBytes());
        HolderType ht = (HolderType) iter.next();

			  os.write(ht.getTypeName().getBytes());
		}

	}



  private void printDataType(HaviType type, OutputStream os, boolean array) throws IOException
  {
    switch(type.getConstantTypeDef())
    {

        case ENUM:
            os.write("\t\t\t\t\tint".getBytes());
            if(array == true)
            	os.write("[]".getBytes());

            break;

        case SEQUENCE:
        	//get the datatype and call printdatatype again
            HaviType h1 = ((SequenceType) type).getDataType();
            printDataType(h1, os, true);
            break;


        case LITERAL:
            HaviType h = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());

            if(h == null)
            {
							os.write("\t\t\t\t\t".getBytes());
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
							os.write("\t\t\t\t\t".getBytes());
            os.write(type.getTypeName().getBytes());
            if(array == true)
							os.write("[]".getBytes());
            break;

        default:  //base data type
						os.write("\t\t\t\t\t".getBytes());
            type.output(os);
            if(array == true)
            	os.write("[]".getBytes());

            break;

    }

  }


  private void printDeclareVariable(OutputStream os) throws IOException
  {
		Iterator iter = ft.iterator();


			while(iter.hasNext())
			{
          HolderType ht = (HolderType) iter.next();
          HaviType dataType = ht.getDataType();
					printDataType(dataType, os, false);
					os.write(" ".getBytes());
					os.write(ht.getTypeName().getBytes());
					os.write(";\n".getBytes());
			}

			os.write("\n\n".getBytes());

  }


	private void printUnmarshalling(OutputStream os) throws IOException
	{
	     Iterator iter = ft.iterator();

		 //print only if parameter list > 0
		 if(ft.getChildList().size() > 0)
		 {
	          os.write("\t\t\t\ttry\n".getBytes());
	          os.write("\t\t\t\t{\n".getBytes());
	          os.write("\t\t\t\t\t// Unmarshall\n".getBytes());


	          while(iter.hasNext())
  	        {
                HolderType ht = (HolderType) iter.next();
                HaviType dataType = ht.getDataType();

					      UnmarshallingWrite(dataType, ht.getTypeName(),os, false);

	         	}  //end loop

 		       os.write("\t\t\t\t}\n".getBytes());

					 Iterator exceptionIter = localExceptionList.iterator();
					 while(exceptionIter.hasNext())
					 {
						 	String exceptionName = (String)exceptionIter.next();
						 	os.write("\t\t\t\tcatch(".getBytes());
						 	os.write(exceptionName.getBytes());
						 	os.write(" e)\n".getBytes());
		          os.write("\t\t\t\t{\n".getBytes());

				    	os.write("\t\t\t\t\t// Log error\n".getBytes());
				    	os.write("\t\t\t\t\tLoggerSingleton.logError(this.getClass(), \"receiveSystemEventNotification\", e.toString());\n\n".getBytes());

							os.write("\t\t\t\t\t// Not handled\n".getBytes());
							os.write("\t\t\t\t\treturn false;\n".getBytes());

		      		os.write("\t\t\t\t}\n\n".getBytes());
					 }

		 }
     localExceptionList.clear();


	}



   private void UnmarshallingWrite(HaviType dataType, String vName, OutputStream os, boolean array) throws IOException
   {

   		  	  HaviType type = dataType;

	          switch(type.getConstantTypeDef())
            {

		    				case SEQUENCE:
			    					type = ((SequenceType) dataType).getDataType();
			    					UnmarshallingWrite(type, vName, os, true);
			    					break;



                case ENUM:
									localExceptionList.add("IOException");
									if(array == true)
									{
										os.write("\t\t\t\t\t{\n".getBytes());

										os.write("\t\t\t\t\t\tint size = payload.readInt();\n".getBytes());
										os.write("\t\t\t\t\t\t".getBytes());
										os.write(vName.getBytes());
										os.write(" = new int[size];\n".getBytes());
										os.write("\t\t\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
										os.write("\t\t\t\t\t\t\t".getBytes());


										os.write(vName.getBytes());
										os.write("[i] = payload.readInt();\n".getBytes());

										os.write("\t\t\t\t\t}\n\n".getBytes());

									}
									else
									{
		          			   os.write("\t\t\t\t\t".getBytes());
                       os.write(vName.getBytes());
                       os.write(" = payload.readInt();\n".getBytes());
									}
									break;


                case UNION:
										localExceptionList.add("HaviUnmarshallingException");
										if(array == true)
										{
											localExceptionList.add("IOException");
											os.write("\t\t\t\t\t{\n".getBytes());

											os.write("\t\t\t\t\t\tint size = payload.readInt();\n".getBytes());
											os.write("\t\t\t\t\t\t".getBytes());
											os.write(vName.getBytes());
											os.write(" = new ".getBytes());
											os.write(type.getTypeName().getBytes());
											os.write("[size];\n".getBytes());

											os.write("\t\t\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
											os.write("\t\t\t\t\t\t\t".getBytes());

                      os.write(vName.getBytes());
                      os.write("[i] = ".getBytes());
                      os.write(dataType.getTypeName().getBytes());
                      os.write(".create(payload);\n".getBytes());

                      os.write("\t\t\t\t\t}\n\n".getBytes());

										}
										else
										{
		                		os.write("\t\t\t\t".getBytes());
                        os.write(vName.getBytes());
                        os.write(" = ".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write(".create(payload);\n".getBytes());
										}

                    break;


               case STRUCT:
										localExceptionList.add("HaviUnmarshallingException");
										if(array == true)
										{
											localExceptionList.add("IOException");
											os.write("\t\t\t\t\t{\n".getBytes());

											os.write("\t\t\t\t\t\tint size = payload.readInt();\n".getBytes());
											os.write("\t\t\t\t\t\t".getBytes());
											os.write(vName.getBytes());
											os.write(" = new ".getBytes());
											os.write(type.getTypeName().getBytes());
											os.write("[size];\n".getBytes());

											os.write("\t\t\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
											os.write("\t\t\t\t\t\t\t".getBytes());

                      os.write(vName.getBytes());
                      os.write("[i] = new ".getBytes());
                      os.write(dataType.getTypeName().getBytes());
                      os.write("(payload);\n".getBytes());

											os.write("\t\t\t\t\t}\n\n".getBytes());
										}
										else
										{
		                		os.write("\t\t\t\t\t".getBytes());
                        os.write(vName.getBytes());
                        os.write(" = new ".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write("(payload);\n".getBytes());
										}

                    break;


                case UNIONSTRUCT:
										localExceptionList.add("HaviUnmarshallingException");
										if(array == true)
										{
											localExceptionList.add("IOException");
											os.write("\t\t\t\t\t{\n".getBytes());

											os.write("\t\t\t\t\t\tint size = payload.readInt();\n".getBytes());
											os.write("\t\t\t\t\t\t".getBytes());
											os.write(vName.getBytes());
											os.write(" = new ".getBytes());
											os.write(type.getTypeName().getBytes());
											os.write("[size];\n".getBytes());

											os.write("\t\t\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
											os.write("\t\t\t\t\t\t{\n".getBytes());
											os.write("\t\t\t\t\t\t\t".getBytes());

                  		os.write("payload.readInt();\n\t\t\t\t\t\t\t".getBytes());
                      os.write(vName.getBytes());
                      os.write("[i] = new ".getBytes());
                      os.write(dataType.getTypeName().getBytes());
                      os.write("(payload);\n".getBytes());

                      os.write("\t\t\t\t\t\t}\n".getBytes());

                      os.write("\t\t\t\t\t}\n\n".getBytes());

										}
										else
										{
                    		os.write("\t\t\t\t\t".getBytes());
//                    		os.write("payload.readInt();\n\t\t\t\t\t".getBytes());
                        os.write(vName.getBytes());
                        os.write(" = new ".getBytes());
                        os.write(dataType.getTypeName().getBytes());
                        os.write("(payload);\n".getBytes());
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
																os.write("\t\t\t\t\t{\n".getBytes());

																os.write("\t\t\t\t\t\tint size = payload.readInt();\n".getBytes());
																os.write("\t\t\t\t\t\t".getBytes());
																os.write(vName.getBytes());
																os.write(" = new ".getBytes());
																os.write(type.getTypeName().getBytes());
																os.write("[size];\n".getBytes());

																os.write("\t\t\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
																os.write("\t\t\t\t\t\t\t".getBytes());

		                            os.write(vName.getBytes());
		                            os.write("[i] = new ".getBytes());
		                            os.write(dataType.getTypeName().getBytes());
		                            os.write("(payload);\n".getBytes());

																os.write("\t\t\t\t\t}\n\n".getBytes());
															}
															else
															{
						                    		os.write("\t\t\t\t\t".getBytes());
				                            os.write(vName.getBytes());
				                            os.write(" = new ".getBytes());
				                            os.write(dataType.getTypeName().getBytes());
				                            os.write("(payload);\n".getBytes());
															}

                       		}
										else
													UnmarshallingWrite(newtype, vName, os, array);

			           		break;

               default:
											localExceptionList.add("IOException");
											if(array == true)
											{
												os.write("\t\t\t\t\t{\n".getBytes());

												os.write("\t\t\t\t\t\tint size = payload.readInt();\n".getBytes());
												os.write("\t\t\t\t\t\t".getBytes());
												os.write(vName.getBytes());
												os.write(" = new ".getBytes());
												type.output(os);
												os.write("[size];\n".getBytes());

												os.write("\t\t\t\t\t\tfor(int i=0; i< size; i++)\n".getBytes());
												os.write("\t\t\t\t\t\t\t".getBytes());

                        os.write(vName.getBytes());
                        os.write("[i] = payload.read".getBytes());
                        os.write(((BaseDataType) dataType).getMarshalString().getBytes());
                        os.write("();\n".getBytes());

                        os.write("\t\t\t\t\t}\n\n".getBytes());

											}
											else
											{
	                    	   os.write("\t\t\t\t\t".getBytes());
	                           os.write(vName.getBytes());
	                           os.write(" = payload.read".getBytes());
	                           os.write(((BaseDataType) dataType).getMarshalString().getBytes());
	                           os.write("();\n".getBytes());
											}

											break;

              }

   }



}
