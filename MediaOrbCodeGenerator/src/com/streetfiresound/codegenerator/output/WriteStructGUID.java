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
 * $Id: WriteStructGUID.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ArrayType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.DeclarationType;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.SequenceType;
import com.streetfiresound.codegenerator.types.StructType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteStructGUID  implements ConstTypeDefinition
{
  /**
   * Constructor for WriteStructGUID.
   */

  StructType ct;

  String constant;
  String type;
  String system;
  String packageName;
  String exception;

  /**
   * Constructor for WriteStructGUID.
   */
  public WriteStructGUID(StructType ct) throws IOException
  {
    super();

    HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
    system = (String)configInfo.get("SYSTEM");
    type = (String)configInfo.get("TYPE");
    constant = (String)configInfo.get("CONSTANT");
	packageName =  (String)configInfo.get("PACKAGE");
	exception =  (String)configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");

    this.ct = ct;

	 String systemPath = rootPath+"/"+ packageName.replace('.','/')+"/"+ type.replace('.','/');
      FileOutputStream fos = new GplHeaderFileOutputStream(systemPath+"/"+ct.getTypeName()+".java");
      outputToFile(fos);



  }


  private void outputToFile(OutputStream os) throws IOException
  {

    printPackage(os);
    printImport(os);

    printClass(os);
    printOpenClass(os);
    printPrivate(os);
    printStatic(os);

    printConstructor1(os);
    printConstructor2(os);
    printConstructor3(os);

    new WriteHaviImmutableObjectClass(ct, os);

    printSetGet(os);

    printToString(os);
    printCloseClass(os);


  }

  private void printPackage(OutputStream os) throws IOException
  {
  	String pack = packageName+ "."+ type;

    os.write("package ".getBytes());
    os.write(pack.getBytes());

    os.write(";\n\n".getBytes());


  }

  private void printImport(OutputStream os) throws IOException
  {

	String constantPath = packageName+"."+ constant;
	String typePath = packageName + "." + type;
	String packPath = packageName + "." + system;
	String exceptionPath = packageName + "." + exception;

	ArrayList pathList = new ArrayList();
	pathList.add(packPath);

	Set set = CodeGenerator.projectList.entrySet();


    os.write("import java.io.*;\n".getBytes());
    os.write("import java.util.*;\n\n".getBytes());

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
    os.write("\n\n\n\n".getBytes());

  }

  private void printClass(OutputStream os) throws IOException
  {
      os.write("public class ".getBytes());
      os.write(ct.getTypeName().getBytes());
      os.write(" extends HaviImmutableObject".getBytes());

  }

  private void printOpenClass(OutputStream os) throws IOException
  {
    os.write("\n{\n\n".getBytes());
  }





  private void printPrivate(OutputStream os) throws IOException
  {

      os.write("\tpublic final static int SIZE = 8;\n".getBytes());
      os.write("\tpublic final static GUID BROADCAST;\n".getBytes());
      os.write("\tpublic final static GUID ZERO;\n\n".getBytes());

      os.write("\tprivate byte value[];\n\n\n".getBytes());



  }

  private void printStatic(OutputStream os) throws IOException
  {
    os.write("\tstatic\n".getBytes());
    os.write("\t{\n".getBytes());
    os.write("\t\t// Initialize broadcast GUID\n".getBytes());

    os.write("\t\tBROADCAST = new GUID();\n".getBytes());
    os.write("\t\tZERO = new GUID();\n".getBytes());
    os.write("\t\tfor (int i = 0; i < SIZE; i++)\n".getBytes());
    os.write("\t\t{\n".getBytes());
      os.write("\t\t\tBROADCAST.value[i] = -1;\n".getBytes());
      os.write("\t\t\tZERO.value[i] = 0;\n".getBytes());
    os.write("\t\t}\n".getBytes());


    os.write("\t}\n\n".getBytes());
  }


  private void printCloseClass(OutputStream os) throws IOException
  {
      os.write("}\n\n".getBytes());
  }




  private void printConstructor1(OutputStream os) throws IOException
  {
      os.write("\tpublic ".getBytes());
      os.write((""+ct.getTypeName()).getBytes());
      os.write("()\n".getBytes());
      os.write("\t{\n".getBytes());
      os.write("\t\tvalue = new byte[SIZE];\n".getBytes());
      os.write("\t}\n\n\n".getBytes());

  }









  private void printDataType(HaviType dataType, OutputStream os) throws IOException
  {
      HaviType thisDataType = dataType;

      if(dataType instanceof SequenceType)
        thisDataType = ((SequenceType) dataType).getDataType();

      switch( thisDataType.getConstantTypeDef())
      {
          case ENUM:
            os.write("int".getBytes());
            break;


          case STRUCT:
          case UNION:
          case UNIONSTRUCT:
            os.write(dataType.getTypeName().getBytes());
            break;


          default:
            dataType.output(os);
            break;

      }//end switch


  }






  /**
   * print out the constructor with parameter list. The parameter list contains variables that match the
   * private variable.
   *
   * Method printConstructor2.
   * @param os
   * @throws IOException
   */
  private void printConstructor2(OutputStream os) throws IOException
  {


            os.write("\tpublic ".getBytes());
            os.write((""+ct.getTypeName()).getBytes());
            os.write("(".getBytes());


            //loop thru the content and the content must be a DeclarationType
            //this part create the parameter list in the function
            Iterator iter = ct.iterator();
            while(iter.hasNext())
            {
                      //get the variables list from the DeclartionType
                      DeclarationType dt = (DeclarationType ) iter.next();
                      Iterator vIter = dt.iterator();

                      //loop thru the variable list
                      while(vIter.hasNext())
                      {

                           HaviType variable = (HaviType)vIter.next();
                           printDataType(dt.getDataType(), os);


                            if(dt.getDataType() instanceof SequenceType || variable instanceof ArrayType)
                              os.write("[]".getBytes());

                            //write out a space
                            os.write(" i".getBytes());
                            variable.output(os);

                            //if have more variable, print out ,
                            //for example int a,b,c
                            if(vIter.hasNext())
                              os.write(", ".getBytes());
                      }

                      if(iter.hasNext())
                          os.write(", ".getBytes());

            }

             os.write(" )\n\t{\n".getBytes());


            //loop thru the content again
            //this part create the content list in the function

            iter = ct.iterator();
            while(iter.hasNext())
            {
                    DeclarationType dt = (DeclarationType ) iter.next();

                    //get variable list
                    Iterator vIter = dt.iterator();


                    //in DeclarationType, the data type only has 3 kinds of type
                    //1 BaseDatatype, 2 SequenceType and 3  LiteralType
                    // BaseDataType suchas short, long, string etc
                    // LiteralType usually is either and object name or interface value.
                    // Since different type has different print out, so we need to take care all of the type


                  while(vIter.hasNext())
                  {
                        HaviType vType = (HaviType)vIter.next();
                        constructor2ContentWrite(dt.getDataType(), vType, os);
                  }
            }

            os.write("\t}\n\n".getBytes());

  }



  private void constructor2ContentWrite(HaviType dataType, HaviType vType, OutputStream os) throws IOException
  {

      HaviType thisDataType = dataType;

      if(dataType instanceof SequenceType)
          thisDataType = ((SequenceType) dataType).getDataType();


      switch(thisDataType.getConstantTypeDef())
      {
          case STRUCT:
          case UNION:
          case UNIONSTRUCT:

                if(dataType instanceof SequenceType || vType instanceof ArrayType)
                {

//                        os.write("\n\t\tint size = ".getBytes());

                        //print out variable name
//                        vType.output(os);
//                        os.write(".length;\n\t\tthis.".getBytes());

                        //print out variable name, actually, it is same as tempHt.output(os);
                        os.write(vType.getTypeName().getBytes());;
                        os.write("= new ".getBytes());

                        //get the data type of this variable,so just call the decarlation data type
                        thisDataType.output(os);
                        os.write("[SIZE];\n".getBytes());


                        os.write("\t\tfor(int i=0; i < SIZE; i++)\n\t\t\t".getBytes());
                        os.write("this.".getBytes());

                        vType.output(os);
                        os.write("[i]".getBytes());
                        os.write(" = i".getBytes());
                        vType.output(os);
                        os.write("[i]".getBytes());
                        os.write(";\n".getBytes());
                }
                else
                {
                  os.write("\t\tthis.".getBytes());
                  vType.output(os);
                  os.write(" = ".getBytes());
                  vType.output(os);
                  os.write(";\n".getBytes());
                }
                break;

          default:
                if(dataType instanceof SequenceType || vType instanceof ArrayType)
                {
						os.write("\t\t".getBytes());
                        os.write(vType.getTypeName().getBytes());;
                        os.write("= new ".getBytes());
						thisDataType.output(os);
						os.write("[SIZE];\n".getBytes());

						os.write("\t\tfor(int i=0; i < SIZE; i++)\n".getBytes());
						os.write("\t\t\tthis.".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write("[i] = i".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write("[i];\n\n".getBytes());
                }
                else
                {
	                os.write("\t\tthis.".getBytes());
	                vType.output(os);
	                os.write(" = ".getBytes());
	                vType.output(os);
	                os.write(";\n".getBytes());
                }
                break;
      }//end switch

  }




  private void printConstructor3(OutputStream os) throws IOException
  {
      os.write("\tpublic GUID(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());
      os.write("\t{\n".getBytes());


//      os.write("\t\ttry\n".getBytes());
//      os.write("\t\t{\n".getBytes());
      os.write("\t\t\t// Unmarshal the data\n".getBytes());
      os.write("\t\t\tvalue = new byte[SIZE];\n".getBytes());
      os.write("\t\t\thbais.read(value, 0, SIZE);\n".getBytes());
//      os.write("\t\t}\n".getBytes());
//      os.write("\t\tcatch (IOException e)\n".getBytes());
//      os.write("\t\t{\n".getBytes());
//      os.write("\t\t\t// Translate\n".getBytes());
//      os.write("\t\t\tthrow new HaviUnmarshallingException(e.toString());\n".getBytes());
//      os.write("\t\t}\n".getBytes());
      os.write("\t}\n\n".getBytes());

  }




  private void printSetGet(OutputStream os) throws IOException
  {
      Iterator iter = ct.iterator();

      while(iter.hasNext())
      {

            DeclarationType dt = (DeclarationType) iter.next();


            HaviType dataType = dt.getDataType();
            Iterator varIter = dt.iterator();
            while(varIter.hasNext())
            {
                  HaviType vType = (HaviType)varIter.next();
                  String variable = vType.getTypeName();    //variable name


                  //set
                    os.write("\tpublic void set".getBytes());
                    os.write((variable.substring(0,1).toUpperCase() + variable.substring(1)).getBytes());
                    os.write("(".getBytes());

                    printDataType(dataType, os);


                    if(dataType instanceof SequenceType || vType instanceof ArrayType)
                        os.write("[]".getBytes());

                    os.write((" I" + variable).getBytes());
                    os.write(")\n".getBytes());

                    os.write("\t{\n".getBytes());

                    os.write("\t\t".getBytes());
                    os.write(variable.getBytes());
                    os.write(" = I".getBytes());
                    os.write(variable.getBytes());
                    os.write(";\n".getBytes());
                    os.write("\t}\n\n".getBytes());



                //get
                    os.write("\tpublic ".getBytes());
                    printDataType(dataType, os);  //return type

                    if(dataType instanceof SequenceType || vType instanceof ArrayType)
                        os.write("[]".getBytes());

                    os.write(" get".getBytes());
                    os.write((variable.substring(0,1).toUpperCase() + variable.substring(1)).getBytes());
                    os.write("()\n".getBytes());
                    os.write("\t{\n".getBytes());

                    os.write("\t\treturn ".getBytes());
                    os.write(variable.getBytes());
                    os.write(";\n".getBytes());
                    os.write("\t}\n\n\n".getBytes());

            }


      }

  }



	private void printToString(OutputStream os ) throws IOException
	{
		os.write("".getBytes());

	 os.write("\tpublic String toString()\n".getBytes());
	 os.write("\t{\n".getBytes());
     os.write("\t\tString string = \"GUID\";\n".getBytes());
     os.write("\t\tfor (int i = 0; i < SIZE; i++)\n".getBytes());
     os.write("\t\t{\n".getBytes());
     os.write("\t\t\tstring = string + ':' + Integer.toHexString(value[i] & 0xff);\n".getBytes());
     os.write("\t\t}\n".getBytes());
     os.write("\t\treturn string;\n".getBytes());
	 os.write("\t}\n\n".getBytes());


	}

}
