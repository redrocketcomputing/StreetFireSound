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
 * $Id: WriteConstErrorCode.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
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
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.HaviType;




/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteConstErrorCode implements ConstTypeDefinition
{


	ArrayList constantList;
//    String constantName;
//    String dataType;
	String apiCode;


    String prefix = "\tpublic final static ";


	String system;
	String constant;
	String packageName;
	String type;
	String exception;

  /**
   * Constructor for ErrorCodeRule.
   */
  public WriteConstErrorCode(ArrayList cList) throws IOException
  {
    super();
    this.constantList = cList;

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");



    String location = rootPath + "/" + packageName.replace('.','/') + "/" + constant.replace('.', '/');

     File newfile = new File(location);

		if(!newfile.exists())
			throw new IOException("path not exists:"+ location);


      FileOutputStream fos = new GplHeaderFileOutputStream(location+"/Const"+CodeGenerator.currentPackage+"ErrorCode.java");

       outputToFile(fos);
       fos.close();

       makeException();

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
        os.write("ErrorCode".getBytes());
        os.write("\n".getBytes());

  }

  private void printContent1(OutputStream os) throws IOException
  {

        Iterator iter = constantList.iterator();

        while(iter.hasNext())
        {
			ConstType ct = (ConstType)iter.next();
  		    CodeGenerator.exceptionList.add(ct.getTypeName());	//mine

            os.write("\t\t".getBytes());
            os.write(prefix.getBytes());

			printDataType(ct.getDataType(), os);

			os.write(" ".getBytes());
            os.write(ct.getTypeName().getBytes());
            os.write(" = (".getBytes());

			printDataType(ct.getDataType(), os);

            os.write(") ".getBytes());
            os.write(ct.getValue().getBytes());
            os.write(";\n".getBytes());

        }

  }




  private void printContent2(OutputStream os) throws IOException
  {

        os.write("\n\n".getBytes());

        Iterator iter = constantList.iterator();

		 String constantValue = (String)CodeGenerator.haviapi.get(CodeGenerator.currentPackage.toUpperCase());	//012303


        while(iter.hasNext())
        {
			ConstType ct = (ConstType)iter.next();

			String vName = ct.getTypeName()+"_ERROR_CODE";
            os.write("\t\t".getBytes());
            os.write(prefix.getBytes());
            os.write(" Status ".getBytes());


            os.write(vName.getBytes());
            os.write(" = new Status ( (short)".getBytes());
            os.write(constantValue.getBytes());
			os.write(", (".getBytes());

			printDataType(ct.getDataType(), os);


			os.write(")".getBytes());
			os.write(ct.getValue().getBytes());
			os.write(" );\n".getBytes());

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


  private void makeException()
  {

	        Iterator iter = constantList.iterator();
	        while(iter.hasNext())
	        {
	          String vName = ((HaviType) iter.next()).getTypeName();

	          try
	          {
		          new WriteException(vName);
	          }
	          catch(IOException e)
	          {
	          	System.err.println(e + ":" + vName);
	          }

	        }

  }




    private void printDataType(HaviType dataType, OutputStream ostream) throws IOException
    {

              switch(dataType.getConstantTypeDef())
              {
                  case ENUM:

                      ostream.write("int".getBytes());
                      break;

                  case STRUCT:
                  case UNION:
                  case UNIONSTRUCT:

                      ostream.write(dataType.getTypeName().getBytes());
                      break;


                  case LITERAL:
                      HaviType h = (HaviType) CodeGenerator.dataTypeList.get(dataType.getTypeName());

                      printDataType(h, ostream);
                      break;

                  default:
                      dataType.output(ostream);

              }
    }




}
