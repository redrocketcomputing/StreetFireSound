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
 * $Id: WriteCallBackInvocation.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

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
public class WriteCallBackInvocation implements ConstTypeDefinition
{
		private FunctionType ft;

		String system;
		String constant;
		String packageName;
		String type;
		String exception;
		String className;

		HashSet localExceptionList;


		public WriteCallBackInvocation(FunctionType ft) throws IOException
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

			String location = rootPath + "/" + packageName.replace('.', '/') + "/" + system.replace('.', '/');


			className = CodeGenerator.makeFileName(ft.getTypeName());

//			FileOutputStream fos = new FileOutputStream( location + "/" + ft.getTypeName() + "MessageBackInvocation.java");
			FileOutputStream fos = new GplHeaderFileOutputStream( location + "/" + className + "MessageBackInvocation.java");

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

			os.write(className.getBytes());

			os.write("MessageBackInvocation extends MessageBackInvocation\n".getBytes());

		}


		private void printOpenClass(OutputStream os) throws IOException
		{
			os.write("{\n".getBytes());
		}

		private void printCloseClass(OutputStream os) throws IOException
		{
			os.write("\n}\n\n".getBytes());
		}

		private void printPrivate(OutputStream os) throws IOException
		{
			Iterator iter = ft.iterator();

			while(iter.hasNext())
			{
				HolderType ht = (HolderType) iter.next();


				os.write("\tprivate ".getBytes());
				printDataType(ht.getDataType(), os, false);

				os.write(" ".getBytes());
				os.write(ht.getTypeName().getBytes());
				os.write(";\n".getBytes());
			}

			os.write("\n\n".getBytes());
		}


		private void printDataType(HaviType dataType, OutputStream os, boolean array) throws IOException
		{
			HaviType type = dataType;

			switch(type.getConstantTypeDef())
			{
				case SEQUENCE:
					type = ((SequenceType) dataType).getDataType();
					printDataType(type, os, true);
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
						printDataType(newtype, os, array);
					break;


				default:
					type.output(os);
					if(array == true)
						os.write("[]".getBytes());
					break;
			}//end switch
		}



		private void printConstructor(OutputStream os) throws IOException
		{

			os.write("\tpublic ".getBytes());

//			os.write(ft.getTypeName().getBytes());
			os.write(className.getBytes());

			os.write("MessageBackInvocation(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());
			os.write("\t{\n".getBytes());

			os.write("\t\t// Construct super class\n".getBytes());
			os.write("\t\tsuper(".getBytes());
			os.write(CodeGenerator.currentPackage.getBytes());
			os.write("Constant.SUCCESS);\n\n".getBytes());


			if(ft.getChildList().size() > 0)
			{
					os.write("\t\ttry\n".getBytes());
					os.write("\t\t{\n".getBytes());


					Iterator iter = ft.iterator();
					while(iter.hasNext())
					{
						HolderType hType = (HolderType)iter.next();
						constructorWrite(hType.getDataType(), hType.getTypeName(), os, false);

					}

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
						os.write("\t\t}\n".getBytes());
					}
			}

			os.write("\t}\n\n\n".getBytes());
			localExceptionList.clear();
		}



		private void constructorWrite(HaviType dataType, String vName, OutputStream os, boolean array) throws IOException
		{
			HaviType type = dataType;

			switch(type.getConstantTypeDef())
			{

				case SEQUENCE:
					type = ((SequenceType)dataType).getDataType();
					constructorWrite(type, vName, os, true);
					break;


				case LITERAL:
					type = (HaviType)CodeGenerator.dataTypeList.get(dataType.getTypeName());
					if(type == null)
					{
						localExceptionList.add("HaviUnmarshallingException");
						if(array == true)
						{
							localExceptionList.add("IOException");

							os.write("\n\t\t\t{\n".getBytes());

								os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());

								os.write("\t\t\t\t".getBytes());
								os.write(vName.getBytes());
								os.write(" =  new ".getBytes());
								os.write(type.getTypeName().getBytes());
								os.write("[size];\n".getBytes());

								os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
								os.write("\t\t\t\t\t".getBytes());
								os.write(vName.getBytes());
								os.write("[i] = new ".getBytes());
								os.write(type.getTypeName().getBytes());
								os.write("(hbais);\n".getBytes());

							os.write("\t\t\t}\n\n".getBytes());
						}
						else
						{
							os.write("\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("(hbais);\n".getBytes());
						}

					}
					else
						constructorWrite(type, vName, os, array);
					break;



				case ENUM:
					localExceptionList.add("IOException");
					if(array == true)
					{
						os.write("\n\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());

							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" =  new int[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
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


				case STRUCT:
					localExceptionList.add("HaviUnmarshallingException");
					if(array == true)
					{
						localExceptionList.add("IOException");
						os.write("\n\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());

							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" =  new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write("[i] = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("(hbais);\n".getBytes());

						os.write("\t\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\t\t".getBytes());
						os.write(vName.getBytes());
						os.write(" = new ".getBytes());
						os.write(type.getTypeName().getBytes());
						os.write("(hbais);\n".getBytes());
					}
					break;

				case UNION:
					localExceptionList.add("HaviUnmarshallingException");
					if(array == true)
					{
						localExceptionList.add("IOException");
						os.write("\n\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());

							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" =  new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write("[i] = ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write(".create(hbais);\n".getBytes());

						os.write("\t\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\t\t".getBytes());
						os.write(vName.getBytes());
						os.write(" = ".getBytes());
						os.write(type.getTypeName().getBytes());
						os.write(".create(hbais);\n".getBytes());
					}
					break;

				case UNIONSTRUCT:
					localExceptionList.add("HaviUnmarshallingException");
					localExceptionList.add("IOException");
					if(array == true)
					{
						os.write("\n\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());

							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" =  new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
							os.write("\t\t\t\t{\n".getBytes());

							os.write("\t\t\t\t\thbais.readInt();\n\t\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write("[i] = new ".getBytes());
							os.write(type.getTypeName().getBytes());
							os.write("(hbais);\n".getBytes());

							os.write("\t\t\t\t}\n".getBytes());
						os.write("\t\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\t\thbais.readInt();\n\t\t\t".getBytes());
						os.write(vName.getBytes());
						os.write(" = new ".getBytes());
						os.write(type.getTypeName().getBytes());
						os.write("(hbais);\n".getBytes());
					}
					break;


				default:
					localExceptionList.add("IOException");
					if(array == true)
					{
						os.write("\n\t\t\t{\n".getBytes());

							os.write("\t\t\t\tint size = hbais.readInt();\n".getBytes());

							os.write("\t\t\t\t".getBytes());
							os.write(vName.getBytes());
							os.write(" =  new ".getBytes());
							type.output(os);
							os.write("[size];\n".getBytes());

							os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());

							os.write(vName.getBytes());
							os.write("[i] = hbais.read".getBytes());
							os.write(((BaseDataType) type).getMarshalString().getBytes());
							os.write("();\n".getBytes());

						os.write("\t\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\t\t".getBytes());
						os.write(vName.getBytes());
						os.write(" = hbais.read".getBytes());
						os.write(((BaseDataType) type).getMarshalString().getBytes());
						os.write("();\n".getBytes());
					}
					break;

			}	//end switch
		}




		private void printDispatch(OutputStream os) throws IOException
		{
			String vName = ft.getTypeName().substring(0,1).toLowerCase() +  ft.getTypeName().substring(1);
			String className = CodeGenerator.makeFileName(ft.getTypeName());


			os.write("\tpublic void dispatch(MessageBackListener listener)\n".getBytes());
			os.write("\t{\n".getBytes());
			os.write("\t\ttry\n".getBytes());
			os.write("\t\t{\n".getBytes());

			os.write("\t\t\tif (!(listener instanceof ".getBytes());


//			os.write(ft.getTypeName().getBytes());
			os.write(className.getBytes());

			os.write("MessageBackListener))\n".getBytes());
			os.write("\t\t\t{\n".getBytes());
			os.write("\t\t\t\t//Bad argument\n".getBytes());
			os.write("\t\t\t\tthrow new IllegalArgumentException(\"bad listener type\");\n".getBytes());
			os.write("\t\t\t}\n\n".getBytes());

			os.write("\t\t\t// Cast it up\n".getBytes());
			os.write("\t\t\t".getBytes());

//			os.write(ft.getTypeName().getBytes());
			os.write(className.getBytes());


			os.write("MessageBackListener ".getBytes());

			os.write(vName.getBytes());
			os.write("Listener = (".getBytes());

//			os.write(ft.getTypeName().getBytes());
			os.write(className.getBytes());

			os.write("MessageBackListener)listener;\n\n".getBytes());

			os.write("\t\t\t// Dispatch\n".getBytes());
			os.write("\t\t\t".getBytes());
			os.write(vName.getBytes());
			os.write("Listener.".getBytes());
			os.write(vName.getBytes());
			os.write("(".getBytes());


			Iterator iter = ft.iterator();
			while(iter.hasNext())
			{
				HolderType hType = (HolderType)iter.next();

				os.write(hType.getTypeName().getBytes());

				if(iter.hasNext())
					os.write(", ".getBytes());

			}


			os.write(");\n".getBytes());
			os.write("\t\t}\n".getBytes());
			os.write("\t\tcatch (HaviException e)\n".getBytes());
			os.write("\t\t{\n".getBytes());
			os.write("\t\t\t// Save as return code\n".getBytes());

			os.write("\t\t\treturnCode = e.makeStatus();\n".getBytes());

			os.write("\t\t}\n\n".getBytes());
			os.write("\t}\n\n\n".getBytes());

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
			String exceptionPath=   packageName+"."+exception;
			String packPath = packageName + "." + system;


			ArrayList pathList = new ArrayList();
			pathList.add(packPath);


			//import java.io
			os.write("import ".getBytes());
			os.write(CodeGenerator.JAVA_IO.getBytes());
			os.write(";\n\n".getBytes());

			//import com.redrocketcomputer.system
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


		private void printMarshal(OutputStream os) throws IOException
		{
			os.write("\tpublic void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException\n".getBytes());
			os.write("\t{\n".getBytes());
/*
			os.write("\t\ttry\n".getBytes());
			os.write("\t\t{\n".getBytes());
			os.write("\t\t\t// marshal nothing\n\n".getBytes());
			os.write("\t\t}\n".getBytes());
			os.write("\t\t catch(HaviException e)\n".getBytes());
			os.write("\t\t{\n".getBytes());
			os.write("\t\t\tthrow new HaviMarshallingException(e.getMessage());\n".getBytes());
			os.write("\t\t}\n\n".getBytes());
*/
			os.write("\t}\n\n\n".getBytes());
		}


}
