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
 * $Id: WriteRemoteInvocationFactory.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.FunctionType;
import com.streetfiresound.codegenerator.types.InterfaceType;

/**
 * @author george
 *
 */
public class WriteRemoteInvocationFactory {

	InterfaceType it;

	String system;
	String constant;
	String packageName;
	String type;
	String exception;

	/**
	 * Constructor for WriteRemoteInvocationFactory.
	 */
	public WriteRemoteInvocationFactory(InterfaceType it) throws IOException
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

		String location = rootPath + "/" + packageName.replace('.', '/') + "/" + system.replace('.', '/');


		FileOutputStream fos = new GplHeaderFileOutputStream( location + "/" + CodeGenerator.currentPackage + "RemoteInvocationFactory.java");

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
		printStatic(os);
		printConstructor(os);
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
		String packPath = packageName + "." + system;
		String exceptionPath = packageName + "." + exception;
		ArrayList pathList = new ArrayList();
		pathList.add(packPath);


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


	}

	/**
	 * Method printClass.
	 * @param os
	 * @throws IOException
	 */
	private void printClass(OutputStream os) throws IOException {
		os.write("class ".getBytes());
		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("RemoteInvocationFactory extends RemoteInvocationFactory implements Const".getBytes());
		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("OperationId\n".getBytes());
	}

	/**
	 * Method printOpenClass.
	 * @param os
	 * @throws IOException
	 */
	private void printOpenClass(OutputStream os) throws IOException {
		os.write("{\n".getBytes());
	}

	private void printCloseClass(OutputStream os) throws IOException {
		os.write("\n}\n\n".getBytes());
	}

	/**
	 * Method printConstructor.
	 * @param os
	 * @throws IOException
	 */
	private void printConstructor(OutputStream os) throws IOException {
		os.write("\tpublic ".getBytes());
		os.write(CodeGenerator.currentPackage.getBytes());
		os.write("RemoteInvocationFactory()\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\t// Construct super class\n".getBytes());
		os.write("\t\tsuper(".getBytes());

		os.write(CodeGenerator.currentPackage.getBytes());

		os.write("Constant.API_CODE, invocationTable);\n".getBytes());
		os.write("\t}\n\n\n".getBytes());

	}
	/**
	 * Method printPrivate.
	 * @param os
	 * @throws IOException
	 */
	private void printPrivate(OutputStream os) throws IOException {
		os.write("\tprivate final static Class[] invocationTable = new Class[".getBytes());
		os.write(Integer.toString(CodeGenerator.opcodeList.size()).getBytes());
		os.write("];\n\n\n".getBytes());
	}

	/**
	 * Method printStatic.
	 * @param os
	 * @throws IOException
	 */
	private void printStatic(OutputStream os) throws IOException {
		os.write("\tstatic\n".getBytes());

		os.write("\t{\n".getBytes());
		os.write("\t\t// Build remote invocation table\n".getBytes());

		Iterator iter = it.iterator();
		int loopCount = 0;

		//loop thru the list of interface
		while (iter.hasNext()) {

			//interface must contains only functiontype
			FunctionType ft = (FunctionType) iter.next();

			String className = CodeGenerator.makeFileName(ft.getTypeName());


			os.write("\t\tinvocationTable[".getBytes());
			os.write(((String) CodeGenerator.opcodeList.get(loopCount++)).getBytes());
			os.write(".getOperationId()] = ".getBytes());

//			os.write(ft.getTypeName().getBytes());
			os.write(className.getBytes());

			os.write("RemoteInvocation.class;\n".getBytes());

			if (loopCount == CodeGenerator.opcodeList.size())
				break;

		}
		os.write("\n\t}\n\n".getBytes());

	}

}
