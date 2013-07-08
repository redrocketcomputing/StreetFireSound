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
 * $Id: WriteCallBackListener.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


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
public class WriteCallBackListener implements ConstTypeDefinition {
	private FunctionType ft;


	String system;
	String constant;
	String packageName;
	String type;
	String exception;
	String className;


	/**
	 * Method WriteCallBackListener.
	 * @param ft
	 * @throws IOException
	 */
	public WriteCallBackListener(FunctionType ft) throws IOException {
		super();

		this.ft = ft;

		HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
		constant = (String) configInfo.get("CONSTANT");
		system = (String) configInfo.get("SYSTEM");
		type = (String) configInfo.get("TYPE");
		packageName = (String) configInfo.get("PACKAGE");
		exception = (String) configInfo.get("EXCEPTION");
		String rootPath = (String)configInfo.get("ROOTPATH");

		//String location = "/" + driver.BASEOUTPUTPATH.replace('.', '/') + "/" + driver.packagePath.replace('.', '/');
		String location = rootPath + "/" + packageName.replace('.', '/') + "/" + system.replace('.', '/');

		File newfile = new File(location);
		if (!newfile.exists())
			newfile.mkdir();

		className = CodeGenerator.makeFileName(ft.getTypeName());

//		FileOutputStream fos = new FileOutputStream(location + "/" + ft.getTypeName() + "MessageBackListener.java");
		FileOutputStream fos = new GplHeaderFileOutputStream(location + "/" + className + "MessageBackListener.java");

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
		printInterface(os);

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
	private void printImport(OutputStream os) throws IOException {
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		String exceptionPath=   packageName+"."+exception;
		String packPath = packageName + "." + system;


		ArrayList pathList = new ArrayList();
		pathList.add(packPath);



		//print out com.redrocketcomputing.system.rmi
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
	 * Method printInterface.
	 * @param os
	 * @throws IOException
	 */
	private void printInterface(OutputStream os) throws IOException {
		os.write("\n\n".getBytes());
		os.write("public interface ".getBytes());

//		os.write(ft.getTypeName().getBytes());
		os.write(className.getBytes());

		os.write("MessageBackListener extends MessageBackListener\n".getBytes());
		os.write("{\n".getBytes());
		os.write("\tpublic void ".getBytes());

		os.write(ft.getTypeName().substring(0, 1).toLowerCase().getBytes());
		os.write(ft.getTypeName().substring(1).getBytes());
		os.write("( ".getBytes());

		Iterator iter = ft.iterator();

		//loop thru the parameter list
		while (iter.hasNext()) {

			//functio parameter list must be a list of holdertype
			HolderType ht = (HolderType) iter.next();

			HaviType dataType = ht.getDataType();

			interfaceWrite(dataType, os);

			if (dataType instanceof SequenceType)
				os.write("[]".getBytes());

			os.write(" ".getBytes());

			//print out the variable name
			os.write(ht.getTypeName().getBytes());

			if (iter.hasNext())
				os.write(", ".getBytes());

		} //end while loop
		os.write(" ) throws Havi".getBytes());
	      os.write(CodeGenerator.currentPackage.getBytes());
	      os.write("Exception;\n".getBytes());

		os.write("}\n\n".getBytes());
	}

	/**
	 * Method interfaceWrite.
	 * @param hType
	 * @param os
	 * @throws IOException
	 */
	private void interfaceWrite(HaviType hType, OutputStream os)
		throws IOException {

		HaviType type = hType;

		//determine data type to print appropriate output
		switch (type.getConstantTypeDef()) {

			case SEQUENCE :
				type = ((SequenceType) type).getDataType();
				interfaceWrite(type, os);
				break;

			case ENUM :
				os.write("int ".getBytes());
				break;

			case STRUCT :
			case UNION :
			case UNIONSTRUCT :
				os.write(type.getTypeName().getBytes());
				break;

			case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(hType.getTypeName());
				if(newtype == null)
					os.write(type.getTypeName().getBytes());
				else
					interfaceWrite(newtype, os);

				break;




			default : //base data type
				type.output(os);
				break;
		} //end switch

	}

}
