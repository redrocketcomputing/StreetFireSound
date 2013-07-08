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
 * $Id: WriteConstant.java,v 1.1 2005/02/22 03:46:08 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.streetfiresound.codegenerator.parser.CodeGenerator;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteConstant
{

    String prefix = "\tpublic final static ";
    String constantValue ="";


	String system;
	String constant;
	String packageName;
	String type;
	String exception;


  /**
   * Constructor for WriteConstant.
   */
  public WriteConstant() throws IOException
  {
    super();

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

	constantValue = (String)CodeGenerator.haviapi.get(CodeGenerator.currentPackage.toUpperCase());	//012303

    String location = rootPath + "/" + packageName.replace('.','/') + "/" + constant.replace('.', '/');


    if(constantValue != null)
    {

     File newfile = new File(location);

     if(!newfile.exists())
        newfile.mkdir();

       FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+CodeGenerator.currentPackage+"Constant.java");

       outputToFile(fos);
       fos.close();
    }

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

      printInterface(os);
      printOpenInterface(os);
      printContent1(os);
      printCloseInterface(os);
  }

/**
 * Method printPackage.
 * @param os
 * @throws IOException
 */
  private void printPackage(OutputStream os) throws IOException
  {
	String constantPath=packageName+"."+constant;

    os.write("package ".getBytes());

    os.write(constantPath.getBytes());


    os.write(";\n\n\n".getBytes());




  }


/**
 * Method printInterface.
 * @param os
 * @throws IOException
 */
  private void printInterface(OutputStream os) throws IOException
  {
        os.write("public interface ".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write("Constant\n".getBytes());

  }





/**
 * Method printContent1.
 * @param os
 * @throws IOException
 */
  private void printContent1(OutputStream os) throws IOException
  {



        String outputName = prefix + "short API_CODE =  (short) " + constantValue + ";\n\n";
        os.write(outputName.getBytes());


        os.write((prefix + "Status SUCCESS = new Status((short) " + constantValue + ", (short) 0x0000);\n\n").getBytes());

  }




/**
 * Method printOpenInterface.
 * @param os
 * @throws IOException
 */
  private void printOpenInterface(OutputStream os) throws IOException
  {
    os.write("{\n".getBytes());

  }
/**
 * Method printCloseInterface.
 * @param os
 * @throws IOException
 */
  private void printCloseInterface(OutputStream os) throws IOException
  {
    os.write("\n}\n\n".getBytes());

  }



  private void printImport(OutputStream os) throws IOException
  {

	    String typePath=packageName+"."+type;
		String packPath = packageName + "." + system;

		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

		//GENERAL TYPE PATH
		if(!pathList.contains(CodeGenerator.generalTypePath))
		{
			os.write("import ".getBytes());
			os.write(CodeGenerator.generalTypePath.getBytes());
			os.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.generalTypePath);
		}
		if(!pathList.contains(typePath))
		{
			os.write("import ".getBytes());
			os.write(typePath.getBytes());
			os.write(".*;\n\n".getBytes());
		}

  	 os.write("\n\n".getBytes());

  }

}



