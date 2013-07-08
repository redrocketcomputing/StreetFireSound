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
 * $Id: WriteEnumOperationId.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
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
import com.streetfiresound.codegenerator.types.EnumType;
import com.streetfiresound.codegenerator.types.HaviType;


/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteEnumOperationId
{


    int value;
    EnumType et;
    String prefix = "\tpublic final static ";

    String codeName;
    String dataType;


	String system;
	String constant;
	String packageName;
	String type;
	String exception;
	String constantValue;


  /**
   * Constructor for WriteOperationId.
   */
  public WriteEnumOperationId(EnumType et) throws IOException
  {
    super();
    this.et = et;


	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

     codeName = "OPCODE";
     dataType = "byte";
     value = 0;


     constantValue = (String)CodeGenerator.haviapi.get(CodeGenerator.currentPackage.toUpperCase());	//012303

     if(constantValue == null)
     	throw new IOException("ApiCode for "+ CodeGenerator.currentPackage + " cannot be found");



     String location = rootPath + "/" + packageName.replace('.','/') + "/" + constant.replace('.', '/');;

     File newfile = new File(location);
     if(!newfile.exists())
        newfile.mkdir();


      FileOutputStream fos = new GplHeaderFileOutputStream(location+"/Const"+CodeGenerator.currentPackage+et.getTypeName()+ ".java");

       outputToFile(fos);
       fos.close();


  }





  private void outputToFile(OutputStream os) throws IOException
  {

      printPackage(os);

      printImport(os);

      printInterface(os);

      printOpenInterface(os);

      printContent1(os);

      printContent2(os);

      printCloseInterface(os);

  }

  private void printPackage(OutputStream os) throws IOException
  {
	String constantPath=packageName+"."+constant;

    os.write("package ".getBytes());
    os.write(constantPath.getBytes());
    os.write(";\n\n\n\n".getBytes());

  }


  private void printInterface(OutputStream os) throws IOException
  {
        os.write("public interface Const".getBytes());
        os.write(CodeGenerator.currentPackage.getBytes());
        os.write(et.getTypeName().getBytes());
        os.write("\n".getBytes());

  }

  private void printContent1(OutputStream os) throws IOException
  {

        int currentValue = value;
        Iterator iter = et.iterator();
        String writeHex = "00";
        while(iter.hasNext())
        {

          String vName = ((HaviType) iter.next()).getTypeName();

          String hexValue = writeHex+Integer.toHexString(currentValue++);
          hexValue = hexValue.substring(  hexValue.length() -2 );


          String outputName = prefix + dataType +" " +vName + " = " +  "(" + dataType + ")" + "0x00" + hexValue +  ";\n";


          os.write(outputName.getBytes());


        }

  }




  private void printContent2(OutputStream os) throws IOException
  {

        os.write("\n\n".getBytes());
        int currentValue = value;
        Iterator iter = et.iterator();
        String writeHex = "00";



//        String constantValue = driver.getConstantValue(driver.currentPackage);
//		String constantValue = (String)CodeGenerator.haviapi.get(CodeGenerator.currentPackage.toUpperCase());	//012303

        while(iter.hasNext())
        {
          String vName = ((HaviType) iter.next()).getTypeName() + "_" + codeName;

          CodeGenerator.opcodeList.add(vName);

          String temp ="";
          String hexValue = writeHex+Integer.toHexString(currentValue++);
          hexValue = hexValue.substring(  hexValue.length() -2 );


          temp = prefix + "OperationCode " + vName +" = new OperationCode( (short)" + constantValue+ ", (byte)0x00" + hexValue + " );\n";

          os.write(temp.getBytes());

        }



  }



  private void printOpenInterface(OutputStream os) throws IOException
  {
    os.write("{\n".getBytes());

  }
  private void printCloseInterface(OutputStream os) throws IOException
  {
    os.write("\n}\n\n".getBytes());

  }

  private void printImport(OutputStream os) throws IOException
  {
	  	String packPath = packageName + "." + system;
		String typePath=packageName+"."+type;

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


