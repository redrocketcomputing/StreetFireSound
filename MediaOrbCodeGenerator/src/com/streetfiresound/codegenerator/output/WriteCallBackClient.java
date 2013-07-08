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
 * $Id: WriteCallBackClient.java,v 1.2 2005/02/24 03:03:37 stephen Exp $
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
public class WriteCallBackClient implements ConstTypeDefinition
{
	private FunctionType ft;

	String system;
	String constant;
	String packageName;
	String type;
	String exception;

	String className;
	HashSet localExceptionList;


	public WriteCallBackClient(FunctionType ft) throws IOException
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

//		String location = "/" + driver.BASEOUTPUTPATH.replace('.', '/') + "/" + driver.packagePath.replace('.', '/');
		String location = rootPath + "/" + packageName.replace('.', '/') + "/" + system.replace('.', '/');

		File newfile = new File(location);
		if (!newfile.exists())
			newfile.mkdir();

		className = CodeGenerator.makeFileName(ft.getTypeName());


//		FileOutputStream fos = new FileOutputStream( location + "/" + ft.getTypeName() + "MessageBackClient.java");
		FileOutputStream fos = new GplHeaderFileOutputStream( location + "/" + className + "MessageBackClient.java");
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

		printFunction(os);

		printFunctionSync(os);

		printThrowReturnCode(os);

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
		os.write(";\n\n".getBytes());

	}

	/**
	 * Method printImport.
	 * @param os
	 * @throws IOException
	 */
	private void printImport(OutputStream os) throws IOException {

		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;
		String packPath = packageName + "." + system;


		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

		os.write("import ".getBytes());
		os.write(CodeGenerator.JAVA_IO.getBytes());
		os.write(";\n".getBytes());

		os.write("import ".getBytes());
		os.write(CodeGenerator.HAVI_CLIENT_PATH.getBytes());
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


		if(!pathList.contains(constantPath))
		{
			os.write("import ".getBytes());
			os.write(constantPath.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(constantPath);
		}

		if(!pathList.contains(typePath))
		{
			os.write("import ".getBytes());
			os.write(typePath.getBytes());
			os.write(".*;\n\n".getBytes());
			pathList.add(typePath);
		}


		if(!pathList.contains(exceptionPath))
		{
			os.write("import ".getBytes());
			os.write(exceptionPath.getBytes());
			os.write(".*;\n\n".getBytes());
			pathList.add(exceptionPath);
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


	}


	/**
	 * Method printClass.
	 * @param os
	 * @throws IOException
	 */
	private void printClass(OutputStream os) throws IOException
	{
		os.write("public class ".getBytes());

//		os.write(ft.getTypeName().getBytes());
		os.write(className.getBytes());

		os.write("MessageBackClient extends HaviClient\n".getBytes());

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
	 * Method printConstructor.
	 * @param os
	 * @throws IOException
	 */
	private void printConstructor(OutputStream os) throws IOException
	{
		os.write("\tpublic ".getBytes());
		os.write(className.getBytes());
//		os.write(ft.getTypeName().getBytes());

		os.write("MessageBackClient(OperationCode opCode, SoftwareElement se, SEID destSeid) throws HaviMsgException, Havi".getBytes());
	      os.write(CodeGenerator.currentPackage.getBytes());
	      os.write("Exception\n".getBytes());

		os.write("\t{\n".getBytes());

		os.write("\t\tsuper(se, destSeid);\n".getBytes());
		os.write("\t\tthis.opCode = opCode;\n\n".getBytes());
		os.write("\t}\n\n\n".getBytes());

	}

	private void printPrivate(OutputStream os) throws IOException
	{
		os.write("\tprivate OperationCode opCode;\n\n\n".getBytes());
	}




	/**
	 * Method functionSignatureWrite.
	 * @param dataType
	 * @param vName
	 * @param os
	 * @param array
	 * @throws IOException
	 */
	private void functionSignatureWrite(HaviType dataType, OutputStream os, boolean array) throws IOException
	{
		HaviType type = dataType;

		switch (type.getConstantTypeDef())
		{
			case SEQUENCE:
				type = ((SequenceType) dataType).getDataType();
				functionSignatureWrite(type, os, true);
				break;

			case ENUM:
				os.write("int".getBytes());
				if(array == true)
					os.write("[]".getBytes());
				break;

			case STRUCT:
			case UNION:
			case UNIONSTRUCT:
				os.write(type.getTypeName().getBytes());
				if(array == true)
					os.write("[]".getBytes());

				break;

			case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(dataType.getTypeName());
				if(newtype == null)
				{
					os.write(type.getTypeName().getBytes());
					if(array == true)
						os.write("[]".getBytes());
				}
				else
					functionSignatureWrite(newtype,os, array);

				break;


			default:
				type.output(os);
				if(array == true)
					os.write("[]".getBytes());
				break;

		}


	}

	/**
	 * Method functionBodyWrite.
	 * @param datatype
	 * @param vName
	 * @param os
	 * @param array
	 * @throws IOException
	 */
	private void functionBodyWrite(HaviType datatype, String vName, OutputStream os, boolean array) throws IOException
	{
		HaviType type = datatype;

		switch(type.getConstantTypeDef())
		{

			case SEQUENCE:
				type = ((SequenceType) datatype).getDataType();
				functionBodyWrite(type, vName, os, true);
				break;


			case ENUM:
				localExceptionList.add("IOException");
				if(array == true)
				{
					os.write("\n\t\t\t{\n".getBytes());
					os.write("\t\t\t\tint size = ".getBytes());
					os.write(vName.getBytes());
					os.write(".length;\n".getBytes());

					os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

					os.write("\t\t\t\tfor(int i = 0; i < size; i++)\n".getBytes());
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

			case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(type.getTypeName());
				if(newtype == null)
				{
						localExceptionList.add("HaviMarshallingException");
						if(array == true)
						{
							localExceptionList.add("IOException");
							os.write("\n\t\t\t{\n".getBytes());
							os.write("\t\t\t\tint size = ".getBytes());
							os.write(vName.getBytes());
							os.write(".length;\n".getBytes());

							os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

							os.write("\t\t\t\tfor(int i = 0; i < size; i++)\n".getBytes());
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





			case STRUCT:
			case UNION:
			case UNIONSTRUCT:
				localExceptionList.add("HaviMarshallingException");
				if(array == true)
				{
					localExceptionList.add("IOException");
					os.write("\n\t\t\t{\n".getBytes());
					os.write("\t\t\t\tint size = ".getBytes());
					os.write(vName.getBytes());
					os.write(".length;\n".getBytes());

					os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

					os.write("\t\t\t\tfor(int i = 0; i < size; i++)\n".getBytes());
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

			default:
				localExceptionList.add("IOException");
				if(array == true)
				{
					os.write("\n\t\t\t{\n".getBytes());
					os.write("\t\t\t\tint size = ".getBytes());
					os.write(vName.getBytes());
					os.write(".length;\n".getBytes());

					os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());
					os.write("\t\t\t\tfor(int i = 0; i < size; i++)\n".getBytes());
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



	/**
	 * Method printFunction.
	 * @param os
	 * @throws IOException
	 */
	private void printFunction(OutputStream os) throws IOException
	{
		String functionName = ft.getTypeName().substring(0,1).toLowerCase() + ft.getTypeName().substring(1);

		//start  write function signature
		os.write("\tpublic int ".getBytes());
		os.write(functionName.getBytes());
		os.write("(".getBytes());

		Iterator fIter = ft.iterator();

		while(fIter.hasNext())
		{
			HolderType htype = (HolderType) fIter.next();

			//print datatype
			functionSignatureWrite(htype.getDataType(),  os, false);

			//print space
			os.write(" ".getBytes());

			//print variable name
			os.write(htype.getTypeName().getBytes());

			if(fIter.hasNext())
				os.write(", ".getBytes());
		}
		os.write(")  throws HaviMsgException, Havi".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("Exception\n".getBytes());
		//end write function signature

		//start writing function body write open function braces
		os.write("\t{\n".getBytes());



			if(ft.getChildList().size() > 0)
			{

				//start try catch
				os.write("\t\ttry\n".getBytes());
				os.write("\t\t{\n".getBytes());

				//start function content
				os.write("\t\t\t// Create output stream;\n".getBytes());
				os.write("\t\t\tHaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();\n\n".getBytes());


				os.write("\t\t\t// Marshall\n".getBytes());
				fIter = ft.iterator();
				while(fIter.hasNext())
				{
					HolderType htype = (HolderType) fIter.next();
					functionBodyWrite(htype.getDataType(), htype.getTypeName(), os, false);
				}

				os.write("\n\t\t\t// Send the request\n".getBytes());
		      	os.write("\t\t\treturn se.msgSendRequest(destSeid, opCode, hbaos.toByteArray());\n\n".getBytes());


				//close brace of try catch
				os.write("\t\t}\n".getBytes());

				//start catch Exception

				Iterator exceptionIter = localExceptionList.iterator();

				while(exceptionIter.hasNext())
				{
					String exceptionName = (String) exceptionIter.next();
					os.write("\t\tcatch(".getBytes());
					os.write(exceptionName.getBytes());
					os.write(" e)\n".getBytes());
					os.write("\t\t{\n".getBytes());
					os.write("\t\t\t// Translate\n".getBytes());
					os.write("\t\t\tthrow new Havi".getBytes());
			        os.write(CodeGenerator.currentPackage.getBytes());
			        os.write("UnidentifiedFailureException(e.toString());\n".getBytes());
					os.write("\t\t}\n".getBytes());
					//finish catch exception
				}
			}
			else
				os.write("\t\t\treturn 0;\n".getBytes());


		//end write function body. Write close function brace
		os.write("\n\t}\n\n\n".getBytes());

		localExceptionList.clear();

	}

	/**
	 * Method printThrowReturnCode.
	 * @param os
	 * @throws IOException
	 */
	private void printThrowReturnCode(OutputStream os) throws IOException
	{
		os.write("\tprivate void throwReturnCode(Status returnCode) throws Havi".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("Exception\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\tthrow new Havi".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("UnidentifiedFailureException();\n".getBytes());
		os.write("\t}\n\n".getBytes());

	}



	/**
	 * Method printFunctionSync.
	 * @param os
	 * @throws IOException
	 */
	private void printFunctionSync(OutputStream os) throws IOException
	{
		String functionName = ft.getTypeName().substring(0,1).toLowerCase() + ft.getTypeName().substring(1);

		//start  write function signature
		os.write("\tpublic void ".getBytes());
		os.write(functionName.getBytes());
		os.write("Sync(int timeout".getBytes());

		Iterator fIter = ft.iterator();

		//check if there any parameter, if no parameter then we don't need to print ","
		if(fIter.hasNext())
			os.write(", ".getBytes());

		while(fIter.hasNext())
		{
			HolderType htype = (HolderType) fIter.next();
			functionSignatureWrite(htype.getDataType(), os, false);

			//print space
			os.write(" ".getBytes());

			//print variable name
			os.write(htype.getTypeName().getBytes());

			if(fIter.hasNext())
				os.write(", ".getBytes());
		}
		os.write(")  throws HaviMsgException, Havi".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("Exception\n".getBytes());
		//end write function signature




		//start writing function body write open function braces
		os.write("\t{\n".getBytes());


			if(ft.getChildList().size() > 0)
			{
				//start try catch
				os.write("\t\ttry\n".getBytes());
				os.write("\t\t{\n".getBytes());

				//start function content
				os.write("\t\t\t// Create output stream;\n".getBytes());
				os.write("\t\t\tHaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();\n\n".getBytes());


				os.write("\t\t\t// Marshall\n".getBytes());
				fIter = ft.iterator();
				while(fIter.hasNext())
				{
					HolderType htype = (HolderType) fIter.next();

					functionBodyWrite(htype.getDataType(), htype.getTypeName(), os, false);
				}
				os.write("\t\t\t// Send the request\n".getBytes());
		      	os.write("\t\t\tse.msgSendRequestSync(destSeid, opCode, timeout, hbaos.toByteArray());\n\n".getBytes());



				//close brace of try catch
				os.write("\t\t}\n".getBytes());



				//start write out Exception
				Iterator exceptionIter = localExceptionList.iterator();
				while(exceptionIter.hasNext())
				{
					String exceptionName = (String)exceptionIter.next();

					os.write("\t\tcatch(".getBytes());
					os.write(exceptionName.getBytes());
					os.write(" e)\n".getBytes());
					os.write("\t\t{\n".getBytes());
					os.write("\t\t\t// Translate\n".getBytes());
					os.write("\t\t\tthrow new Havi".getBytes());
			        os.write(CodeGenerator.currentPackage.getBytes());
			         os.write("UnidentifiedFailureException(e.toString());\n".getBytes());
					os.write("\t\t}\n".getBytes());

				}
				//finish cat excpetion
			}

		//end write function body. Write close function brace
		os.write("\n\t}\n\n\n".getBytes());

		localExceptionList.clear();

	}



}


