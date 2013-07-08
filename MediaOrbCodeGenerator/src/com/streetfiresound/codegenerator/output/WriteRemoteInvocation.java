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
 * $Id: WriteRemoteInvocation.java,v 1.2 2005/02/24 03:03:37 stephen Exp $
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
import com.streetfiresound.codegenerator.types.VoidType;

/**
 * @author george
 *
 */

public class WriteRemoteInvocation implements ConstTypeDefinition {

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
	 * Constructor for WriteRemoteInvocation.
	 */
	public WriteRemoteInvocation(FunctionType ft) throws IOException {
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

		//constantName = "Const"+driver.currentPackage+"SystemEventConstant";
		constantName = CodeGenerator.currentPackage + "Constant";


//		String location = "/" + driver.BASEOUTPUTPATH.replace('.', '/') + "/" + driver.packagePath.replace('.', '/');
		String location = rootPath + "/" + packageName.replace('.', '/') + "/" + system.replace('.', '/');

		className = CodeGenerator.makeFileName(ft.getTypeName());

//		FileOutputStream fos = new FileOutputStream( location + "/" + ft.getTypeName() + "RemoteInvocation.java");
		FileOutputStream fos = new GplHeaderFileOutputStream( location + "/" + className + "RemoteInvocation.java");

		outputToFile(fos);
		fos.close();

	}

	/**
	 * Method outputToFile.
	 * @param os
	 * @throws IOException
	 */
	private void outputToFile(OutputStream os) throws IOException {
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

	/**
	 * Method printPackage.
	 * @param os
	 * @throws IOException
	 */
	private void printPackage(OutputStream os) throws IOException {
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


		os.write("import java.io.IOException;\n\n".getBytes());
		os.write(("import " + CodeGenerator.REDROCKETBASEPATH + ".rmi.RemoteInvocation;\n").getBytes());
		os.write(("import " + CodeGenerator.REDROCKETBASEPATH + ".rmi.RemoteSkeleton;\n\n\n").getBytes());


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
		os.write("\n".getBytes());

	}

	/**
	 * Method printConstructorType.
	 * @param dataType
	 * @param vName
	 * @param os
	 * @param array
	 * @throws IOException
	 */
	private void printConstructorType(HaviType dataType, String vName, OutputStream os, boolean array) throws IOException {
		HaviType tempType = dataType;

		switch (dataType.getConstantTypeDef()) {

			case ENUM :
				localExceptionList.add("IOException");
				os.write(vName.getBytes());
				os.write(" = hbais.readInt".getBytes());
				os.write("();\n".getBytes());
				break;

			case STRUCT :

				localExceptionList.add("HaviUnmarshallingException");
				if (array == true)
				{
					localExceptionList.add("IOException");
					os.write("{\n".getBytes());
					os.write("\n\t\t\t\tint size = hbais.readInt();\n\t\t\t\t".getBytes());

					os.write(vName.getBytes());
					os.write(" = new ".getBytes());
					os.write(dataType.getTypeName().getBytes());

					os.write("[size];\n".getBytes());

					os.write(
						"\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
					os.write("\t\t\t\t\t".getBytes());

					os.write(vName.getBytes());
					os.write("[i] = new ".getBytes());
					os.write(dataType.getTypeName().getBytes());
					os.write("(hbais);\n\n".getBytes());

					os.write("\t\t\t}\n\n".getBytes());

				}
				else
				{
					os.write(vName.getBytes());
					os.write("= new ".getBytes());
					os.write(dataType.getTypeName().getBytes());
					os.write("(hbais);\n\n".getBytes());

				}
				break;

			case UNION :
				localExceptionList.add("HaviUnmarshallingException");

				if (array == true)
				{
					localExceptionList.add("IOException");

					os.write("{\n".getBytes());
					os.write("\n\t\t\t\tint size = hbais.readInt();\n\t\t\t\t".getBytes());

					os.write(vName.getBytes());
					os.write(" = new ".getBytes());
					os.write(dataType.getTypeName().getBytes());

					os.write("[size];\n".getBytes());

					os.write(
						"\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
					os.write("\t\t\t\t\t".getBytes());

					os.write(vName.getBytes());
					os.write("[i] = ".getBytes());
					os.write(dataType.getTypeName().getBytes());
					os.write(".create(hbais);\n\n".getBytes());
					os.write("\t\t\t}\n\n".getBytes());

				}
				else
				{
					os.write(vName.getBytes());
					os.write("= ".getBytes());
					os.write(dataType.getTypeName().getBytes());
					os.write(".create(hbais);\n\n".getBytes());
				}
				break;

			case UNIONSTRUCT :
				localExceptionList.add("HaviUnmarshallingException");
				localExceptionList.add("IOException");
				if (array == true)
				{
					os.write("{\n".getBytes());
					os.write("\n\t\t\t\tint size = hbais.readInt();\n\t\t\t\t".getBytes());

					os.write(vName.getBytes());
					os.write(" = new ".getBytes());
					os.write(dataType.getTypeName().getBytes());

					os.write("[size];\n".getBytes());

					os.write(
						"\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
					os.write("\t\t\t\t{\n".getBytes());
					os.write("\t\t\t\t\thbais.readInt();\n".getBytes());

					os.write("\t\t\t\t\t".getBytes());
					os.write(vName.getBytes());
					os.write("[i] = new ".getBytes());
					os.write(dataType.getTypeName().getBytes());
					os.write("(hbais);\n\n".getBytes());

					os.write("\t\t\t\t}\n".getBytes());

					os.write("\t\t\t}\n\n".getBytes());

				}
				else
				{
					os.write("hbais.readInt();\n\t\t\t".getBytes());
					os.write(vName.getBytes());
					os.write("= new ".getBytes());
					os.write(dataType.getTypeName().getBytes());
					os.write("(hbais);\n\n".getBytes());

				}
				break;

			case LITERAL :
				HaviType h = (HaviType) CodeGenerator.dataTypeList.get(dataType.getTypeName());
				if (h == null)
				{

						localExceptionList.add("HaviUnmarshallingException");
						if (array == true)
						{
							localExceptionList.add("IOException");
							os.write("{\n".getBytes());
							os.write("\n\t\t\t\tint size = hbais.readInt();\n\t\t\t\t".getBytes());

							os.write(vName.getBytes());
							os.write(" = new ".getBytes());
							os.write(dataType.getTypeName().getBytes());

							os.write("[size];\n".getBytes());

							os.write(
								"\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());

							os.write(vName.getBytes());
							os.write("[i] = new ".getBytes());
							os.write(dataType.getTypeName().getBytes());
							os.write("(hbais);\n\n".getBytes());

							os.write("\t\t\t}\n\n".getBytes());

						}
						else
						{
							os.write(vName.getBytes());
							os.write("= new ".getBytes());
							os.write(dataType.getTypeName().getBytes());
							os.write("(hbais);\n\n".getBytes());

						}

				}
				else
					printConstructorType(h, vName, os, array);

				break;

			case SEQUENCE :
				HaviType h2 = ((SequenceType) dataType).getDataType();
				printConstructorType(h2, vName, os, true);
				break;

			default :
				localExceptionList.add("IOException");
				if (array == true)
				{
					os.write("{\n".getBytes());
					os.write( "\n\t\t\t\tint size = hbais.readInt();\n\t\t\t\t".getBytes());

					os.write(vName.getBytes());
					os.write(" = new ".getBytes());
					dataType.output(os);
					os.write("[size];\n".getBytes());

					os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
					os.write("\t\t\t\t\t".getBytes());

					os.write(vName.getBytes());
					os.write("[i] = hbais.read".getBytes());
					os.write(((BaseDataType) dataType).getMarshalString().getBytes());
					os.write("();\n\n".getBytes());
					os.write("\t\t\t}\n\n".getBytes());
				}
				else
				{
					os.write(vName.getBytes());
					os.write(" = hbais.read".getBytes());
					os.write(
						((BaseDataType) dataType)
							.getMarshalString()
							.getBytes());
					os.write("();\n".getBytes());
				}
				break;

		}

	}

	/**
	 * Method printConstructor.
	 * @param os
	 * @throws IOException
	 */
	private void printConstructor(OutputStream os) throws IOException {
		os.write("\tpublic ".getBytes());

		os.write(className.getBytes());

		os.write("RemoteInvocation(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());


		os.write("\t{\n".getBytes());
		os.write("\t\t// Construct super class\n".getBytes());

		os.write("\t\tsuper(".getBytes());
		os.write(constantName.getBytes());

		os.write(".SUCCESS);\n\n".getBytes());

		Iterator iter = ft.iterator();


		//print if parameter list > 0
		if(ft.getChildList().size() > 0)
		{
			os.write("\t\ttry\n".getBytes());
			os.write("\t\t{\n".getBytes());
		}


		//loop thru parameter list
		while (iter.hasNext())
		{
			HolderType ht = (HolderType) iter.next();

			os.write("\t\t\t".getBytes());

			HaviType haviType = ht.getDataType();

			printConstructorType(haviType, ht.getTypeName(), os, false);

		}

		//print if parameter list > 0   (match the braces)
		if(ft.getChildList().size() > 0)
		{
			os.write("\t\t}\n".getBytes());
		}


		Iterator exceptionIter = localExceptionList.iterator();
		while(exceptionIter.hasNext())
		{
			String exceptionName = (String)exceptionIter.next();
			os.write("\t\tcatch(".getBytes());
			os.write(exceptionName.getBytes());
			os.write(" e)\n".getBytes());
			os.write("\t\t{\n".getBytes());
			os.write("\t\t\tthrow new HaviUnmarshallingException(e.getMessage());\n".getBytes());
			os.write("\t\t}\n".getBytes());
		}

		os.write("\t}\n\n".getBytes());
		localExceptionList.clear();

	}

	/**
	 * Method marshalWrite.
	 * @param dataType
	 * @param os
	 * @param array
	 * @throws IOException
	 */
	private void marshalWrite(HaviType dataType, OutputStream os, boolean array) throws IOException
	{
		switch (dataType.getConstantTypeDef())
		{
			case SEQUENCE :
				HaviType type = ((SequenceType) dataType).getDataType();
				marshalWrite(type, os, true);
				break;

			case ENUM :
				localExceptionList.add("IOException");
				if (array == true)
				{
					os.write("\t\t\t{\n".getBytes());

					os.write("\n\t\t\t\tint size = ".getBytes());
					os.write("result".getBytes());
					os.write(".length;\n".getBytes());

					os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

					os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
					os.write("\t\t\t\t\t".getBytes());

					os.write("\t\t\t\thbaos.writeInt( result[i] );\n".getBytes());
					os.write("\t\t\t}\n\n".getBytes());

				}
				else
				{
					os.write("\t\t\thbaos.writeInt( result );\n".getBytes());
				}
				break;

			case STRUCT :
			case UNION :
			case UNIONSTRUCT :
				localExceptionList.add("HaviMarshallingException");

				if (array == true)
				{
					localExceptionList.add("IOException");

					os.write("\t\t\t{\n".getBytes());

					os.write("\n\t\t\t\tint size = result.length;\n".getBytes());
					os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

					os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
					os.write("\t\t\t\t\t".getBytes());

					os.write("result[i].marshal(hbaos);\n\n".getBytes());
					os.write("\t\t\t}\n\n".getBytes());
				}
				else
				{
					os.write("\t\t\tresult.marshal(hbaos);\n\n".getBytes());
				}

				break;

			case LITERAL :
				HaviType h = (HaviType) CodeGenerator.dataTypeList.get(dataType.getTypeName());

				if (h == null)
				{

						localExceptionList.add("HaviMarshallingException");
						if (array == true)
						{
							localExceptionList.add("IOException");
							os.write("\t\t\t{\n".getBytes());

							os.write("\n\t\t\t\tint size = result.length;\n".getBytes());
							os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

							os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
							os.write("\t\t\t\t\t".getBytes());

							os.write("result[i].marshal(hbaos);\n\n".getBytes());
							os.write("\t\t\t}\n\n".getBytes());
						}
						else
						{
							os.write("\t\t\tresult.marshal(hbaos);\n\n".getBytes());
						}

				}
				else
					marshalWrite(h, os, array);
				break;

			default :
				localExceptionList.add("IOException");
				if (array == true)
				{

					os.write("\t\t\t{\n".getBytes());

					os.write("\n\t\t\t\tint size = result.length;\n".getBytes());
					os.write("\t\t\t\thbaos.writeInt(size);\n".getBytes());

					os.write("\t\t\t\tfor(int i =0; i < size; i++)\n".getBytes());
					os.write("\t\t\t\t\t".getBytes());

					os.write("hbaos.write".getBytes());
					os.write(((BaseDataType) dataType).getMarshalString().getBytes());
					os.write("( result[i] );\n\n".getBytes());
					os.write("\t\t\t}\n\n".getBytes());

				}
				else
				{
					os.write("\t\t\thbaos.write".getBytes());
					os.write(((BaseDataType) dataType).getMarshalString().getBytes());
					os.write("( result );\n".getBytes());
				}

				break;

		}

	}

	/**
	 * Method printMarshal.
	 * @param os
	 * @throws IOException
	 */
	private void printMarshal(OutputStream os) throws IOException
	{
		os.write("\tpublic void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException\n".getBytes());
		os.write("\t{\n".getBytes());

		if (ft.getReturnType().getConstantTypeDef() != VOID)
		{

			os.write("\t\ttry\n".getBytes());
			os.write("\t\t{\n".getBytes());
			marshalWrite(ft.getReturnType(), os, false);

			os.write("\t\t}\n".getBytes());


			Iterator exceptionIter = localExceptionList.iterator();
			while(exceptionIter.hasNext())
			{
				String exceptionName = (String )exceptionIter.next();
				os.write("\t\tcatch(".getBytes());
				os.write(exceptionName.getBytes());
				os.write(" e)\n".getBytes());
				os.write("\t\t{\n".getBytes());
				os.write("\t\t\tthrow new HaviMarshallingException(e.getMessage());\n".getBytes());
				os.write("\t\t}\n".getBytes());
			}
		}
		os.write("\t}\n\n".getBytes());
		localExceptionList.clear();
	}

	/**
	 * Method printDispatch.
	 * @param os
	 * @throws IOException
	 */
	private void printDispatch(OutputStream os) throws IOException {
		os.write("\tpublic void dispatch(RemoteSkeleton remoteSkeleton)\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\ttry\n\t\t{\n".getBytes());

		os.write("\t\t\tif (! (remoteSkeleton instanceof ".getBytes());
		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("Skeleton))\n\t\t\t{\n".getBytes());
		os.write("\t\t\t\t//Bad argument\n".getBytes());
		os.write("\t\t\t\tthrow new IllegalArgumentException(\"bad skeleton type\");\n".getBytes());
		os.write("\t\t\t}\n\n".getBytes());

		os.write("\t\t\t// Cast it up\n".getBytes());

		os.write("\t\t\t".getBytes());
		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("Skeleton skeleton = (".getBytes());
		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("Skeleton) remoteSkeleton;\n\n".getBytes());

		os.write("\t\t\t// Dispatch to the server\n".getBytes());

		os.write("\t\t\t".getBytes());
		if (!(ft.getReturnType() instanceof VoidType))
			os.write("result = ".getBytes());

		os.write("skeleton.".getBytes());
		os.write(ft.getTypeName().substring(0, 1).toLowerCase().getBytes());
		os.write(ft.getTypeName().substring(1).getBytes());

		os.write("(".getBytes());

		Iterator iter = ft.iterator();
		while (iter.hasNext())
		{
			HolderType ht = (HolderType) iter.next();
			os.write(ht.getTypeName().getBytes());

			if (iter.hasNext())
				os.write(", ".getBytes());
		}
		os.write(");\n\n".getBytes());

		os.write("\t\t}\n".getBytes());
		os.write("\t\tcatch (Havi".getBytes());

		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("Exception e)\n\t\t{\n".getBytes());
		os.write("\t\t\t// Save as return code\n".getBytes());
		os.write("\t\t\t returnCode = ((Havi".getBytes());
		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("Exception)e).makeStatus();\n".getBytes());
    os.write("errorMessage = e.toString();\t\t}\n".getBytes());
		os.write("\t}\n".getBytes());

	}

	/**
	 * Method printClass.
	 * @param os
	 * @throws IOException
	 */
	private void printClass(OutputStream os) throws IOException {
		os.write("public class ".getBytes());

		//os.write(ft.getTypeName().getBytes());
		os.write(className.getBytes());

		os.write("RemoteInvocation extends RemoteInvocation\n".getBytes());

	}

	/**
	 * Method printOpenClass.
	 * @param os
	 * @throws IOException
	 */
	private void printOpenClass(OutputStream os) throws IOException {
		os.write("{\n".getBytes());
	}

	/**
	 * Method printCloseClass.
	 * @param os
	 * @throws IOException
	 */
	private void printCloseClass(OutputStream os) throws IOException {
		os.write("}\n".getBytes());
	}

	/**
	 * Method printDataType.
	 * @param dataType
	 * @param os
	 * @throws IOException
	 */
	private void printDataType(HaviType dataType, OutputStream os)
		throws IOException {
		HaviType thisType = dataType;

		switch (thisType.getConstantTypeDef()) {
			case ENUM :
				os.write("int".getBytes());
				break;

			case STRUCT :
			case UNION :
			case UNIONSTRUCT :
				os.write(dataType.getTypeName().getBytes());
				break;

			case SEQUENCE :
				thisType = ((SequenceType) dataType).getDataType();
				printDataType(thisType, os);
				break;

			case LITERAL :
				thisType = (HaviType) CodeGenerator.dataTypeList.get(dataType.getTypeName());

				if (thisType == null) //1
					os.write(dataType.getTypeName().getBytes()); //1
				else //1
					printDataType(thisType, os);

				break;

			default :
				dataType.output(os);
				break;
		}
	}

	/**
	 * Method printPrivate.
	 * @param os
	 * @throws IOException
	 */
	private void printPrivate(OutputStream os) throws IOException {

		Iterator iter = ft.iterator();
		while (iter.hasNext()) {
			os.write("\tprivate ".getBytes());
			HolderType htype = (HolderType) iter.next();

			printDataType(htype.getDataType(), os);

			if (htype.getDataType() instanceof SequenceType)
				os.write("[]".getBytes());

			os.write(" ".getBytes());
			os.write(htype.getTypeName().getBytes());
			os.write(";\n".getBytes());

		}

		if (!(ft.getReturnType() instanceof VoidType)) {
			os.write("\n\tprivate ".getBytes());

			HaviType htype = ft.getReturnType();
			printDataType(htype, os);

			if (htype instanceof SequenceType)
				os.write("[]".getBytes());

			os.write(" result;\n".getBytes());
		}

		os.write("\n\n".getBytes());
	}

}
