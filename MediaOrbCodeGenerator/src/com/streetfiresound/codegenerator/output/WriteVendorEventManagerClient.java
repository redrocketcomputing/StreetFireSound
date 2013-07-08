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
 * $Id: WriteVendorEventManagerClient.java,v 1.2 2005/02/24 03:03:37 stephen Exp $
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
import java.util.StringTokenizer;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.BaseDataType;
import com.streetfiresound.codegenerator.types.ConstType;
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
public class WriteVendorEventManagerClient implements ConstTypeDefinition
{


    String timeout = "30000";

	String system;
	String constant;
	String packageName;
	String type;
	String exception;

	String eventType;
	HashMap constantMap;
	InterfaceType it;
	HashSet localExceptionList;

  /**
   * Constructor for WriteEventManagerClient.
   */
  public WriteVendorEventManagerClient(InterfaceType it, HashMap constantMap)  throws IOException
  {
    super();

	localExceptionList = new HashSet();
	this.it = it;
	this.constantMap = constantMap;



	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");



//	this.constantMap = constantMap;
//	this.eventType = eventType;
//    this.it = it;

//	if(CodeGenerator.eventList.size() > 0)
//	{
			String location = rootPath + "/" + packageName.replace('.','/') + "/" + system.replace('.', '/');

			File newfile = new File(location);

			if(!newfile.exists())
					throw new IOException("Directory not found:" + location);

			FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+CodeGenerator.currentPackage+ "VendorEventManagerClient.java");
			outputToFile(fos);
			fos.close();
//	}

  }



  private void outputToFile(OutputStream os) throws IOException
  {

    printPackage(os);

    printImport(os);

    printClass(os);

//    printImplement(os);

    printOpenClass(os);

    printPrivate(os);

    printConstructor(os);

//	printFireLoop(os);
	printFireFunction(os);

    printCloseClass(os);

  }

  private void printPackage(OutputStream os) throws IOException
  {
  	String pack = packageName + "." + system;
    os.write("package ".getBytes());
    os.write(pack.getBytes());
    os.write(";\n\n\n\n".getBytes());

  }
  private void printImport(OutputStream os) throws IOException
  {
		String packPath = packageName + "." + system;
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;

		Set set = CodeGenerator.projectList.entrySet();


		String eventManagerpath =CodeGenerator.GENERAL_PACKAGE+".eventmanager."+CodeGenerator.GENERAL_SYSTEM_PATH;
		ArrayList pathList = new ArrayList();
		pathList.add(packPath);



      os.write("import ".getBytes());
      os.write(CodeGenerator.JAVA_IO.getBytes());
      os.write(";\n\n".getBytes());


	  //softwareelement path
      os.write("import ".getBytes());
      os.write(CodeGenerator.SOFTWARE_ELEMENT_PATH.getBytes());
      os.write(".*;\n\n".getBytes());

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

		//EventManger PATH
		if(!pathList.contains(eventManagerpath))
		{
			os.write("import ".getBytes());
			os.write(eventManagerpath.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(eventManagerpath);
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

      os.write("\n\n\n".getBytes());

  }



  private void printClass(OutputStream os) throws IOException
  {

    os.write("public class ".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("VendorEventManagerClient ".getBytes());


  }

/*
  private void printImplement(OutputStream os) throws IOException
  {
		Set set = CodeGenerator.eventList.keySet();
		Iterator iter = set.iterator();

		os.write("implements ".getBytes());
		while(iter.hasNext())
		{
			String eventType = (String) iter.next();
			os.write("Const".getBytes());
	        os.write(CodeGenerator.currentPackage.getBytes());
			os.write(eventType.getBytes());
			os.write("Constant".getBytes());

			if(iter.hasNext())
				os.write(", ".getBytes());
		}
  }
*/

  private void printOpenClass(OutputStream os) throws IOException
  {
    os.write("\n{\n".getBytes());

  }

  private void printCloseClass(OutputStream os) throws IOException
  {
    os.write("\n}\n\n".getBytes());

  }

  private void printPrivate(OutputStream os) throws IOException
  {

      os.write("\n\tprivate SoftwareElement softwareElement;\n".getBytes());
      os.write("\tprivate EventManagerClient eventManagerClient;\n".getBytes());
      os.write("\tprivate VendorId vendorId;\n\n\n".getBytes());


  }

  private void printConstructor(OutputStream os) throws IOException
  {
      os.write("\tpublic ".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("VendorEventManagerClient(SoftwareElement softwareElement, VendorId vendorId) throws HaviMsgException, HaviEventManagerException, Havi".getBytes());
      os.write(CodeGenerator.currentPackage.getBytes());
      os.write("Exception\n".getBytes());



      os.write("\t{\n".getBytes());

      os.write("\t\t// Check the parameter\n".getBytes());
      os.write("\t\tif (softwareElement == null)\n".getBytes());
      os.write("\t\t{\n".getBytes());
      os.write("\t\t\t// Bad\n".getBytes());
      os.write("\t\t\tthrow new IllegalArgumentException(\"parameter is null\");\n".getBytes());
      os.write("\t\t}\n\n".getBytes());

      os.write("\t\t// Save parameters\n".getBytes());
      os.write("\t\tthis.softwareElement = softwareElement;\n\n".getBytes());

      os.write("\t\t// Create event manager client\n".getBytes());
      os.write("\t\teventManagerClient = new EventManagerClient(softwareElement);\n".getBytes());
	  os.write("\t\tthis.vendorId = vendorId;\n".getBytes());

      os.write("\n\t}\n\n".getBytes());
  }

/*
	private void printFireLoop(OutputStream os) throws IOException
	{
		Set set = CodeGenerator.eventList.entrySet();
		Iterator setIter = set.iterator();

		while(setIter.hasNext())
		{
			Map.Entry map = (Map.Entry) setIter.next();

			eventType = (String) map.getKey();
			ArrayList al = (ArrayList) map.getValue();

//			it = (InterfaceType)al.get(0);
//			constantMap = (HashMap)al.get(1);

			printFireFunction(os);
		}
	}
*/


      private void printFireFunction(OutputStream os) throws IOException
      {

            int loopCount =0;
            Iterator iter = it.iterator();

            //loop thru to get all the function

            while(iter.hasNext())
            {
                    FunctionType ft = (FunctionType)iter.next();
                    printFunctionSignature(ft, os);

                    //start print function body
                    os.write("\t{\n".getBytes());


                    printFunctionBody(ft, os, loopCount++);


                    os.write("\t}\n\n\n".getBytes());
                    //end print function body

            }

      }




	private void functionSignatureWrite(HaviType dataType, OutputStream os) throws IOException
	{
			HaviType type = dataType;

            switch(type.getConstantTypeDef())
            {

					case SEQUENCE:
						type = ((SequenceType) dataType).getDataType();
						functionSignatureWrite(type, os);
				  	break;

					case ENUM:
						os.write("int".getBytes());
						break;

					case STRUCT:
					case UNION:
					case UNIONSTRUCT:
				  		os.write(type.getTypeName().getBytes());
					    break;


  					case LITERAL:
						HaviType newtype = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());
						if(newtype == null)
							os.write(type.getTypeName().getBytes());
						else
							functionSignatureWrite(newtype, os);

						break;



					default:  //base data type
						dataType.output(os);
						break;
            }

	}



      private void printFunctionSignature(FunctionType ft, OutputStream os) throws IOException
      {

					String upperCaseClassName = CodeGenerator.makeFileName(ft.getTypeName());

                    os.write("\tpublic void fire".getBytes());

//                    os.write(ft.getTypeName().getBytes());
                    os.write(upperCaseClassName.getBytes());

                    os.write("Sync(".getBytes());
                    Iterator holderIter = ft.iterator();

                    //loop thru the parameter list
                    while(holderIter.hasNext())
                    {
                            HolderType hType = (HolderType) holderIter.next();

                            HaviType dataType = hType.getDataType();

							functionSignatureWrite(dataType, os);

      						if(dataType instanceof SequenceType)
      							os.write("[]".getBytes());

                            os.write(" ".getBytes());
                            os.write(hType.getTypeName().getBytes());

                            if(holderIter.hasNext())
                              os.write(", ".getBytes());


                    }
                    os.write(") throws HaviMsgException, HaviEventManagerException, Havi".getBytes());
				      os.write(CodeGenerator.currentPackage.getBytes());
				      os.write("Exception\n".getBytes());


      }



		private void functionBodyWrite(HaviType dataType, String vName, OutputStream os, boolean array) throws IOException
		{
					HaviType type = dataType;

                    switch(dataType.getConstantTypeDef())
                    {
						case SEQUENCE:
							type = ((SequenceType) dataType).getDataType();
							functionBodyWrite(type, vName, os, true);
							break;


                        case ENUM:

                        		localExceptionList.add("IOException");
                        		if(array == true)
                        		{
                        			os.write("\t\t\t{\n".getBytes());

									os.write("\t\t\t\tint size = ".getBytes());
									os.write(vName.getBytes());
									os.write(".length;\n".getBytes());


									os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());


									os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
	                                os.write("\t\t\t\t\thbaos.writeInt(".getBytes());
	                                os.write(vName.getBytes());
	                                os.write("[i]);\n".getBytes());

                        			os.write("\t\t\t}\n\n".getBytes());
                        		}
                        		else
                        		{
	                                os.write("\t\t\thbaos.writeInt(".getBytes());
	                                os.write(vName.getBytes());
	                                os.write(");\n".getBytes());
                        		}
                                break;

                        case STRUCT:
                        case UNION:
                        case UNIONSTRUCT:
								localExceptionList.add("HaviMarshallingException");

                        		if(array == true)
                        		{
									localExceptionList.add("IOException");
                        			os.write("\t\t\t{\n".getBytes());

									os.write("\t\t\t\tint size = ".getBytes());
									os.write(vName.getBytes());
									os.write(".length;\n".getBytes());

									os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

									os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
									os.write("\t\t\t\t\t".getBytes());
									os.write(vName.getBytes());
									os.write("[i].marshal(hbaos);\n".getBytes());

                        			os.write("\t\t\t}\n\n".getBytes());

                        		}
                        		else
                        		{
	                                os.write("\t\t\t".getBytes());
	                                os.write(vName.getBytes());
	                                os.write(".marshal(hbaos);\n".getBytes());
                        		}
                                break;



						case LITERAL:
							HaviType newtype = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());

    						if(newtype == null)
    						{
								localExceptionList.add("HaviMarshallingException");
                        		if(array == true)
                        		{
									localExceptionList.add("IOException");
                        			os.write("\t\t\t{\n".getBytes());

									os.write("\t\t\t\tint size = ".getBytes());
									os.write(vName.getBytes());
									os.write(".length;\n".getBytes());

									os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

									os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
									os.write("\t\t\t\t\t".getBytes());
									os.write(vName.getBytes());
									os.write("[i].marshal(hbaos);\n".getBytes());

                        			os.write("\t\t\t}\n\n".getBytes());

                        		}
                        		else
                        		{
	                                os.write("\t\t\t".getBytes());
	                                os.write(vName.getBytes());
	                                os.write(".marshal(hbaos);\n".getBytes());
                        		}
    						}
    						else
								functionBodyWrite(newtype, vName, os, array);

							break;


                        default:
								localExceptionList.add("IOException");
                        		if(array == true)
                        		{
                        			os.write("\t\t\t{\n".getBytes());

									os.write("\t\t\t\tint size = ".getBytes());
									os.write(vName.getBytes());
									os.write(".length;\n".getBytes());

									os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

									os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
	                                os.write("\t\t\t\t\thbaos.write".getBytes());
	                                os.write(((BaseDataType) type).getMarshalString().getBytes());
	                                os.write("(".getBytes());
	                                os.write(vName.getBytes());
	                                os.write("[i]);\n".getBytes());

                           			os.write("\t\t\t}\n\n".getBytes());


                        		}
                        		else
                        		{
	                                os.write("\t\t\thbaos.write".getBytes());
	                                os.write(((BaseDataType) type).getMarshalString().getBytes());
	                                os.write("(".getBytes());
	                                os.write(vName.getBytes());
	                                os.write(");\n".getBytes());
                        		}
                                break;
                    }//end switch




		}




      private void printFunctionBody(FunctionType ft, OutputStream os, int loopCount) throws IOException
      {

				String searchName = this.makeSearchConstantName(ft.getTypeName());

				ConstType ct = (ConstType) constantMap.get(searchName);

				if(ct != null)
				{
						String eventId = ct.getTypeName();

		                os.write("\t\ttry\n".getBytes());
		                os.write("\t\t{\n".getBytes());
		                os.write("\t\t\t// Marshall\n".getBytes());

		                os.write("\t\t\tHaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();\n".getBytes());

		                Iterator iter = ft.iterator();

		                while(iter.hasNext())
		                {

		                    HolderType ht = (HolderType) iter.next();

							functionBodyWrite(ht.getDataType(), ht.getTypeName(), os, false);


		                } //end loop

		                os.write("\n\n".getBytes());
		                os.write("\t\t\t// Post the event\n".getBytes());



						os.write("\t\t\tEventId eventId = new VendorEventId(ConstVendorEventType.".getBytes());
						os.write(eventId.getBytes());
						os.write(", vendorId);\n".getBytes());


		                os.write("\t\t\teventManagerClient.postEventSync(".getBytes());
		                os.write(timeout.getBytes());
		                os.write(", eventId, true, hbaos.toByteArray());\n\n".getBytes());

		                os.write("\t\t}\n".getBytes());

		                os.write("\t\tcatch (HaviMsgException e)\n".getBytes());
		                os.write("\t\t{\n".getBytes());
		                os.write("\t\t\t// Translate\n".getBytes());
		                os.write("\t\t\tthrow new Havi".getBytes());
		                os.write(CodeGenerator.currentPackage.getBytes());
					    os.write("UnidentifiedFailureException(e.toString());\n".getBytes());
		                os.write("\t\t}\n\n".getBytes());


						Iterator exceptionIter = localExceptionList.iterator();
						while(exceptionIter.hasNext())
						{
							String exceptionName = (String)exceptionIter.next();
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

				}
				else
					System.err.println("WriteEventMangerClient - printFunctionBody error due to constant name not found="+ searchName);


				localExceptionList.clear();
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

