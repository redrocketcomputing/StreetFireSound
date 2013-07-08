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
 * $Id: WriteStructSEID.java,v 1.2 2005/02/24 03:03:37 stephen Exp $
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
public class WriteStructSEID implements ConstTypeDefinition
{

  StructType ct;
	String system;
	String constant;
	String packageName;
	String type;
	String exception;

  /**
   * Constructor for WriteStructSeid.
   */
  public WriteStructSEID(StructType ct) throws IOException
  {
    super();
    this.ct = ct;

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");
	String rootPath = (String)configInfo.get("ROOTPATH");


    String location = rootPath + "/" + packageName.replace('.','/') + "/" + type.replace('.','/');

      FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+ct.getTypeName()+".java");
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
    printConstructor4(os);

    new WriteHaviImmutableObjectClass(ct, os);

    printSetGet(os);
    printGetValue(os);

	printToString(os);

    printCloseClass(os);

  }

  private void printPackage(OutputStream os) throws IOException
  {
  	String pack = packageName + "." + type;
    os.write("package ".getBytes());

    os.write(pack.getBytes());
    os.write(";\n\n".getBytes());


  }

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




private void privateWrite(HaviType datatype, ArrayList list, OutputStream os) throws IOException
{
    Iterator vIter = list.iterator();

    switch (datatype.getConstantTypeDef())
    {

        case ENUM:
            while(vIter.hasNext())
            {
                HaviType vType = (HaviType) vIter.next();
                os.write("\t\tprivate int ".getBytes());
                os.write(vType.getTypeName().getBytes());
                os.write(" = 0;\n".getBytes());
            }
            os.write("\n".getBytes());
            break;

        case STRUCT:
        case UNION:
        case UNIONSTRUCT:
            while(vIter.hasNext())
            {
                HaviType vType = (HaviType) vIter.next();

                os.write("\t\tprivate ".getBytes());
                os.write(datatype.getTypeName().getBytes());
                os.write(" ".getBytes());
                os.write(vType.getTypeName().getBytes());
                os.write(" = ".getBytes());
                os.write(datatype.getTypeName().getBytes());
                os.write(".ZERO;\n".getBytes());
            }
            os.write("\n".getBytes());
            break;


        case BOOLEAN:
            while(vIter.hasNext())
            {
                HaviType vType = (HaviType) vIter.next();

                os.write("\t\tprivate boolean ".getBytes());
                os.write(vType.getTypeName().getBytes());
                os.write(" = false;\n".getBytes());
            }
            os.write("\n".getBytes());
            break;


        case STRING:
        case WSTRING:
            while(vIter.hasNext())
            {
                HaviType vType = (HaviType) vIter.next();

                os.write("\t\tprivate ".getBytes());
                os.write(datatype.getTypeName().getBytes());
                os.write(" ".getBytes());
                os.write(vType.getTypeName().getBytes());
                os.write(" = new String();\n".getBytes());
            }
            os.write("\n".getBytes());
            break;


        default:
            while(vIter.hasNext())
            {
                HaviType vType = (HaviType) vIter.next();
                os.write("\t\tprivate ".getBytes());
                datatype.output(os);
                os.write(" ".getBytes());
                os.write(vType.getTypeName().getBytes());
                os.write(" = (".getBytes());
                datatype.output(os);
                os.write(") 0;\n".getBytes());
            }
            os.write("\n".getBytes());
            break;

    }//end switch

}





  private void printPrivate(OutputStream os) throws IOException
  {

    os.write("\tpublic final static int SIZE = 10;\n".getBytes());
    os.write("\tpublic final static SEID ZERO;\n\n".getBytes());


        //get the iterator
        Iterator iter = ct.iterator();

        //loop thru the content and the content must be a DeclarationType
        while(iter.hasNext())
        {
               DeclarationType dt = (DeclarationType) iter.next();
               HaviType dataType = dt.getDataType();
               Iterator vIter = dt.iterator();

               privateWrite(dt.getDataType(), dt.getChildList(), os);

        }

        os.write("\n\n".getBytes());




  }

  private void printStatic(OutputStream os) throws IOException
  {
    os.write("\tstatic\n".getBytes());
    os.write("\t{\n".getBytes());
    os.write("\t\t// Build zero value\n".getBytes());
    os.write("\t\tZERO = new SEID(GUID.ZERO, (short) 0);\n".getBytes());
    os.write("\t}\n\n".getBytes());
  }



  private void printConstructor(OutputStream os) throws IOException
  {

  }

  private void printCloseClass(OutputStream os) throws IOException
  {
      os.write("}\n\n".getBytes());
  }






  private void printConstructor1(OutputStream os) throws IOException
  {
      os.write("\tpublic ".getBytes());
      os.write((""+ct.getTypeName()).getBytes());
      os.write("(){}\n\n\n".getBytes());
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
                            os.write(" ".getBytes());
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

                if(dataType instanceof SequenceType)
                {

                        os.write("\n\t\tint size = ".getBytes());

                        //print out variable name
                        vType.output(os);
                        os.write(".length;\n\t\tthis.".getBytes());

                        //print out variable name, actually, it is same as tempHt.output(os);
                        os.write(vType.getTypeName().getBytes());;
                        os.write("= new ".getBytes());

                        //get the data type of this variable,so just call the decarlation data type
                        thisDataType.output(os);
                        os.write("[size];\n".getBytes());


                        os.write("\t\tfor(int i=0; i < size; i++)\n\t\t\t".getBytes());
                        os.write("this.".getBytes());

                        vType.output(os);
                        os.write("[i]".getBytes());
                        os.write(" = ".getBytes());
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
                os.write("\t\tthis.".getBytes());
                vType.output(os);
                os.write(" = ".getBytes());
                vType.output(os);
                os.write(";\n".getBytes());
                break;
      }//end switch

  }



  private void printConstructor3(OutputStream os) throws IOException
  {
      os.write("\tpublic SEID(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());
      os.write("\t{\n".getBytes());


      os.write("\t\ttry\n".getBytes());
      os.write("\t\t{\n".getBytes());
      os.write("\t\t\t// Unmarshal the data\n".getBytes());
      os.write("\t\t\tguid = new GUID(hbais);\n".getBytes());
      os.write("\t\t\thandle = hbais.readShort();\n".getBytes());
      os.write("\t\t}\n".getBytes());
      os.write("\t\tcatch (IOException e)\n".getBytes());
      os.write("\t\t{\n".getBytes());
      os.write("\t\t\t// Translate\n".getBytes());
      os.write("\t\t\tthrow new HaviUnmarshallingException(e.toString());\n".getBytes());
      os.write("\t\t}\n".getBytes());
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




  private void printGetValue(OutputStream os) throws IOException
  {
    os.write("\tpublic final byte[] getValue()\n".getBytes());
    os.write("\t{\n".getBytes());
    os.write("\t\t// Get raw guid\n".getBytes());
    os.write("\t\tbyte[] rawGuid = guid.getValue();\n\n".getBytes());

    os.write("\t\t// Allocate new array\n".getBytes());
    os.write("\t\tbyte[] rawSeid = new byte[SIZE];\n\n".getBytes());

    os.write("\t\t// Copy guid\n".getBytes());
    os.write("\t\tSystem.arraycopy(rawGuid, 0, rawSeid, 0, GUID.SIZE);\n\n".getBytes());


    os.write("\t\t// Add handle\n".getBytes());
    os.write("\t\trawSeid[8] = (byte)((handle >> 8) & 0xff);\n".getBytes());
    os.write("\t\trawSeid[9] = (byte)(handle & 0xff);\n\n".getBytes());


    os.write("\t\t// Return the raw seid\n".getBytes());
    os.write("\t\treturn rawSeid;\n\n".getBytes());
    os.write("\t}\n\n\n".getBytes());
  }




  private void printConstructor4(OutputStream os) throws IOException
  {

      os.write("\tpublic SEID(byte[] value) throws HaviInvalidValueException\n".getBytes());
      os.write("\t{\n".getBytes());
      os.write("\t\tif (value.length < SIZE)\n".getBytes());
      os.write("\t\t{\n".getBytes());
      os.write("\t\t\tthrow new HaviInvalidValueException(\"bad length\");\n".getBytes());
      os.write("\t\t}\n\n".getBytes());

      os.write("\t\t// Build guid\n".getBytes());
      os.write("\t\tguid = new GUID(value);\n\n".getBytes());

      os.write("\t\t// Extract handle\n".getBytes());
      os.write("\t\thandle = (short)(((value[8] & 0xff) << 8) + (value[9] & 0xff));\n".getBytes());
      os.write("\t}\n\n\n".getBytes());
  }




 private void printToString(OutputStream os) throws IOException
 {
   os.write("\tpublic String toString()\n".getBytes());
   os.write("\t{\n".getBytes());
   os.write("\t\treturn \"SEID:\" + guid + \"@0x\" + Integer.toHexString(handle & 0xffff);\n".getBytes());
   os.write("\t}\n\n\n".getBytes());
 }

}
