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
 * $Id: StructClassCreation.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

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
import com.streetfiresound.codegenerator.types.ConstructType;
import com.streetfiresound.codegenerator.types.DeclarationType;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.SequenceType;


/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class StructClassCreation implements ConstTypeDefinition
{

    ConstructType ht;


	String constant;
	String system;
	String type;
	String packageName;
	String exception;

  /**
   * Constructor for ClassCreation.
   */
  public StructClassCreation(ConstructType ct)
  {

    super();

    this.ht = ct;

	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);

	constant = (String) configInfo.get("CONSTANT");

	system = (String) configInfo.get("SYSTEM");

    type = (String) configInfo.get("TYPE");

	packageName = (String) configInfo.get("PACKAGE");

	exception = (String) configInfo.get("EXCEPTION");

  }



  /**
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
  public void outputToFile(OutputStream fos)throws java.io.IOException
  {
        printPackage(fos);
        printImport(fos);
        printClass(fos);
        printInterface(fos);
        printOpenClass(fos);
        printPrivate(fos);
        printConstructor1(fos);
        printConstructor2(fos);
        printConstructor3(fos);
        new WriteHaviObjectClass(ht, fos);
        printSetGet(fos);
        printCloseClass(fos);
        fos.close();

  }



  private void printDataType(HaviType dt, OutputStream os)  throws IOException
  {

            HaviType ht = dt;

            switch(ht.getConstantTypeDef())
            {
                case ENUM:
                  os.write("int".getBytes());
                  break;



				//if SequenceType, call getDataType() to get the real data type and call printDataType again
                case SEQUENCE:
                      HaviType h0  =  ((SequenceType) dt).getDataType();
                      printDataType(h0, os);
                      break;


                case STRUCT:
                case UNION:
                case UNIONSTRUCT:
                  os.write(ht.getTypeName().getBytes());
                  break;


                //if LiteralType, search for real data type from driver.dataTypeList and call printDataType again
                case LITERAL:
                    HaviType h = (HaviType) CodeGenerator.dataTypeList.get(ht.getTypeName());
                    if(h == null)
						os.write(ht.getTypeName().getBytes());
					else
					      printDataType(h, os);

                    break;


				//call base data type
                default:
                  dt.output(os);


            }


  }

  private void printPrivate(OutputStream ostream) throws IOException
  {
        //get the iterator
        Iterator iter = ht.iterator();

        //loop thru the content and the content must be a DeclarationType
        while(iter.hasNext())
        {


			  //cast the DeclarationType
               DeclarationType dt = (DeclarationType) iter.next();

			  //get the data type from DeclarationType
               HaviType datatype = dt.getDataType();

  			  //get the variable list from DeclarationType
               Iterator varIter = dt.iterator();


				//loop thru the variable list
               while( varIter.hasNext())
               {

			      //get the variable data type
                   HaviType vType = (HaviType)varIter.next();

                   ostream.write("\tprivate ".getBytes());
                   printDataType(datatype, ostream);


				  //print out [] if it is a array
                  if(datatype instanceof SequenceType || vType instanceof ArrayType)
                        ostream.write("[]".getBytes());

				 //space
                  ostream.write(" ".getBytes());

				  //printout the variable name
                  vType.output(ostream);

				//if more than one variable, print out ','
                 if(varIter.hasNext())
                    ostream.write(", ".getBytes());
               }

              ostream.write(";\n".getBytes());

        } //end while

        ostream.write("\n\n".getBytes());

  }




	/**
	 * Method constructor1Write.
	 * @param datatype
	 * @param vIter
	 * @param os
	 * @param array
	 * @throws IOException
	 */
	private void constructor1Write(HaviType datatype, Iterator vIter, OutputStream os, boolean array) throws IOException
	{
		HaviType type = datatype;


		//get the data type value
		switch(type.getConstantTypeDef())
		{

			//if this is SequenceType, get the real data type and call constructor1Write again
			case SEQUENCE:
				type = ((SequenceType) datatype).getDataType();
				constructor1Write(type, vIter, os, true);
				break;


			//if LiteralType, get the read data type from driver.dataTypeList and call constructo1Write again
			case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(datatype.getTypeName());
				if(newtype == null)
				{
						while(vIter.hasNext())
						{

							HaviType vType = (HaviType)vIter.next();


							os.write("\t\t".getBytes());

							//print the variable name
							os.write(vType.getTypeName().getBytes());

							os.write(" = new ".getBytes());
							os.write(type.getTypeName().getBytes());

							if(array == true || vType instanceof ArrayType)
								os.write("[0];\n".getBytes());
							else
								os.write("();\n".getBytes());
						}
				}

				else
					constructor1Write(newtype, vIter, os, array);
				break;

			case ENUM:

				//loop thru the variable list
				while(vIter.hasNext())
				{

					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());


					//print the variable name
					os.write(vType.getTypeName().getBytes());

					//if it is array, should initialize the list of array
					if(array == true || vType instanceof ArrayType)
					{
						os.write("= new int[0];\n".getBytes());
					}
					else
						os.write(" = 0;\n".getBytes());

				}
				break;


			case STRUCT:
			case UNIONSTRUCT:
			case UNION:
				//loop thru the variable list
				while(vIter.hasNext())
				{

					HaviType vType = (HaviType)vIter.next();


					os.write("\t\t".getBytes());

					//print the variable name
					os.write(vType.getTypeName().getBytes());

					os.write(" = new ".getBytes());
					os.write(type.getTypeName().getBytes());

					if(array == true || vType instanceof ArrayType)
						os.write("[0];\n".getBytes());
					else
						os.write("();\n".getBytes());
				}
				break;
/*
			case UNION:

				//loop thru the variable list
				while(vIter.hasNext())
				{

					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());

					//print out the variable name
					os.write(vType.getTypeName().getBytes());

					//if it is array, should initialize the list of array
					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new ".getBytes());
						os.write(type.getTypeName().getBytes());
						os.write("[0];\n".getBytes());
					}
					else
						os.write("= null;\n".getBytes());
				}
				break;
*/
			case BOOLEAN:
				//loop thru the variable list
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());

					//print out the variable name
					os.write(vType.getTypeName().getBytes());

					//if it is array, should initialize the list of array
					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new ".getBytes());
						type.output(os);
						os.write("[0];\n".getBytes());
					}
					else
						os.write(" = false;\n".getBytes());

				}
				break;
			case STRING:
			case WSTRING:
				//loop thru the variable list
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());

					//print out the variable name
					os.write(vType.getTypeName().getBytes());

					//if it is array, should initialize the list of array
					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new ".getBytes());
						type.output(os);
						os.write("[0];\n".getBytes());
					}
					else
						os.write(" = \"\";\n".getBytes());

				}
				break;


			default:
				//loop thru the variable list
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());

					//print out the variable name
					os.write(vType.getTypeName().getBytes());

					//if it is array, should initialize the list of array
					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new ".getBytes());
						type.output(os);
						os.write("[0];\n".getBytes());
					}
					else
						os.write(" = 0;\n".getBytes());

				}
				break;


		}	//end switch



	}





  /**
   * print out the default constructor
   * Method printConstructor1.
   * @param ostream
   * @throws IOException
   */
  private void printConstructor1(OutputStream ostream) throws IOException
  {
      ostream.write("\tpublic ".getBytes());
      ostream.write((""+ht.getTypeName()).getBytes());
      ostream.write("()\n\t{\n".getBytes());


      Iterator iter = ht.iterator();

	  //loop thru the constructtype list
      while(iter.hasNext())
      {
          //get the variables list from the DeclartionType
          DeclarationType dt = (DeclarationType ) iter.next();
		  constructor1Write(dt.getDataType(), dt.iterator(), ostream, false);
      }


      ostream.write("\t}\n\n\n".getBytes());
  }



/**
 * Just print out the data type
 *
 * Method constructor2Write.
 * @param datatype
 * @param os
 * @throws IOException
 */
  private void constructor2Write(HaviType datatype, OutputStream os) throws IOException
  {

     HaviType dType = datatype;

      switch(  datatype.getConstantTypeDef())
      {
          case ENUM:
            os.write("int".getBytes());
            break;

          case STRUCT:
          case UNION:
          case UNIONSTRUCT:
            os.write(datatype.getTypeName().getBytes());
            break;

          case SEQUENCE:
                HaviType h0  =  ((SequenceType) datatype).getDataType();
                constructor2Write(h0, os);
                break;


          case LITERAL:
                HaviType h1 = (HaviType)CodeGenerator.dataTypeList.get(datatype.getTypeName());

                if(h1 == null)
		            os.write(datatype.getTypeName().getBytes());
      			else
	                constructor2Write(h1, os);
                break;


          default:  //base data type
            datatype.output(os);
            break;
      } //end case

  } //end function



  /**
   * print out the constructor with parameter list. The parameter list contains variables that match the
   * private variable.
   *
   * Method printConstructor2.
   * @param ostream
   * @throws IOException
   */
  private void printConstructor2(OutputStream ostream) throws IOException
  {

            ostream.write("\tpublic ".getBytes());
            ostream.write((""+ht.getTypeName()).getBytes());
            ostream.write("(".getBytes());


            //print the parameter list
            Iterator iter = ht.iterator();
            while(iter.hasNext())
            {
                      //get the variables list from the DeclartionType
                      DeclarationType dt = (DeclarationType ) iter.next();
                      Iterator vIter = dt.iterator();

                      //loop thru the variable list
                      while(vIter.hasNext())
                      {
                            HaviType varType = (HaviType)vIter.next();


                            constructor2Write(dt.getDataType(), ostream);

                            //first check if Declaration com.streetfiresound.codegenerator.types contains SequenceType
                            if(dt.getDataType() instanceof SequenceType || varType instanceof ArrayType)
                              ostream.write("[]".getBytes());

                            //write out a space
                            ostream.write(" ".getBytes());


							//print out the variable name
                             varType.output(ostream);


                            //if have more variable, print out ,
                            //for example int a,b,c
                            if(vIter.hasNext())
                              ostream.write(", ".getBytes());
                      }

                      if(iter.hasNext())
                          ostream.write(", ".getBytes());

            }
            ostream.write(" )\n\t{\n".getBytes());



            //loop thru the content again
            //this part create the content list in the function
            iter = ht.iterator();


			//make sure no variable name duplication
            int sizeCount = -1;

            while(iter.hasNext())
            {

					//setup a loop string in order to prevent variable name duplication
                    String loop = Integer.toBinaryString(sizeCount++);

                    DeclarationType dt = (DeclarationType ) iter.next();


				 	//get the data type
                    HaviType dType = dt.getDataType();

					//get the variable list
                    Iterator vIter = dt.iterator();

                    //loop thru the variable list
                    while(vIter.hasNext())
                    {

                           //get variable type
                           HaviType tempHt = (HaviType) vIter.next();


                           if(dt.getDataType() instanceof SequenceType || tempHt instanceof ArrayType)
                           {
                              ostream.write("\n\t\tint size".getBytes());
                              ostream.write(Integer.toString(sizeCount).getBytes());
                              ostream.write(" = ".getBytes());

                              //print out variable name
                              tempHt.output(ostream);
                              ostream.write(".length;\n\t\tthis.".getBytes());

                              //print out variable name, actually, it is same as tempHt.output(ostream);
                              ostream.write(tempHt.getTypeName().getBytes());;
                              ostream.write("= new ".getBytes());

                              //get the data type of this variable,so just call the decarlation data type
                              //dt.getDataType().output(ostream);
                               printDataType(dt.getDataType(), ostream);

                              ostream.write("[size".getBytes());

                              ostream.write(Integer.toString(sizeCount).getBytes());
                              ostream.write("];\n".getBytes());


                              ostream.write("\t\tfor(int i=0; i < size".getBytes());
                              ostream.write(Integer.toString(sizeCount).getBytes());
                              ostream.write("; i++)\n\t\t\t".getBytes());
                              ostream.write("this.".getBytes());

                              tempHt.output(ostream);
                              ostream.write("[i]".getBytes());
                              ostream.write(" = ".getBytes());
                              tempHt.output(ostream);
                              ostream.write("[i]".getBytes());
                              ostream.write(";\n".getBytes());
                           }
                           else
                           {
                                        ostream.write("\t\tthis.".getBytes());
                                        tempHt.output(ostream);
                                        ostream.write(" = ".getBytes());
                                        tempHt.output(ostream);
                                        ostream.write(";\n".getBytes());
                           }
                    }
            }
            ostream.write("\t}\n\n".getBytes());
  }



  /**
   * print out constructor with havibytearrayinputstream as parameter
   *
   * Method printConstructor3.
   * @param ostream
   * @throws IOException
   */
  private void printConstructor3(OutputStream ostream) throws IOException
  {
      ostream.write("\tpublic ".getBytes());
      ostream.write((""+ht.getTypeName()).getBytes());
      ostream.write("(HaviByteArrayInputStream hi) throws HaviUnmarshallingException\n\t{\n\n".getBytes());
      ostream.write("\t\tunmarshal(hi);\n".getBytes());

      ostream.write("\t}\n\n".getBytes());
  }



  /**
   * print out class header
   *
   * Method printClass.
   * @param ostream
   * @throws IOException
   */
  private void printClass(OutputStream ostream) throws IOException
  {
      ostream.write("public class ".getBytes());
      ostream.write(ht.getTypeName().getBytes());
      ostream.write(" extends HaviObject".getBytes());
  }



  /**
   * print out interface
   * loop thru the list, the list must contains only delcaration type
   * check to see if declarationType's datatype name is contains insides the inteface list
   * if found then print out the implements.
   *
   * Method printInterface.
   * @param ostream
   * @throws IOException
   */
  private void printInterface(OutputStream ostream) throws IOException
  {
     //get the list of declarationtype
     Iterator iter = ht.iterator();

      //To be used to determine print out implement or ,
     short flag = 0;



	//loop thru the list , if Enumtype found then output the interface file name
     while(iter.hasNext())
     {
          DeclarationType dt = (DeclarationType )iter.next();
          HaviType dataType = dt.getDataType();


          if(dataType.getConstantTypeDef() == ENUM)
          {

				//if this is the first implement
                if(flag == 0)
                {
                  flag = 1;
                  ostream.write(" implements Const".getBytes());
                }

				//more than one implement print this
                else
                  ostream.write(", Const".getBytes());


                //print out the interface name
                ostream.write(dt.getDataType().getTypeName().getBytes());
          }
     }


  }



  /**
   * print out "{" only
   *
   * Method printOpenClass.
   * @param ostream
   * @throws IOException
   */
  private void printOpenClass(OutputStream ostream) throws IOException
  {
    ostream.write("\n{\n\n".getBytes());
  }



  /**
   * print out "}" only
   *
   * Method printCloseClass.
   * @param ostream
   * @throws IOException
   */
  private void printCloseClass(OutputStream ostream) throws IOException
  {
    ostream.write("}\n\n".getBytes());
  }



  /**
   * print out the import
   * also print out every item from the modulelist except the same module name.
   *
   *
   * Method printImport.
   * @param ostream
   * @throws IOException
   */
  private void printImport(OutputStream ostream) throws IOException
  {

    ostream.write("import java.io.*;\n".getBytes());
    ostream.write("import java.util.*;\n\n".getBytes());


	String packPath = packageName + "." + system;
	String constantPath=packageName+"."+constant;
	String systemPath =packageName+"."+system;
	String typePath = packageName+"."+type;
	String exceptionPath = packageName + "." + exception;

	Set list = CodeGenerator.projectList.entrySet();
	ArrayList pathList = new ArrayList();
	pathList.add(packPath);


		//GENERAL CONSTANT PATH
		if(!pathList.contains(CodeGenerator.generalConstantPath))
		{
			ostream.write("import ".getBytes());
			ostream.write(CodeGenerator.generalConstantPath.getBytes());
			ostream.write(".*;\n".getBytes());

			pathList.add(CodeGenerator.generalConstantPath);
		}


		//GENERAL TYPE PATH
		if(!pathList.contains(CodeGenerator.generalTypePath))
		{
			ostream.write("import ".getBytes());
			ostream.write(CodeGenerator.generalTypePath.getBytes());
			ostream.write(".*;\n".getBytes());

			pathList.add(CodeGenerator.generalTypePath);
		}

		//GENERAL EXCEPTION PATH
		if(!pathList.contains(CodeGenerator.generalExceptionPath))
		{
			ostream.write("import ".getBytes());
			ostream.write(CodeGenerator.generalExceptionPath.getBytes());
			ostream.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.generalExceptionPath);
		}



		//dcm constants, com.streetfiresound.codegenerator.types  path
		if(!pathList.contains(CodeGenerator.DCM_PATH+".constants"))
		{
			ostream.write("import ".getBytes());
			ostream.write((CodeGenerator.DCM_PATH+".constants").getBytes());
			ostream.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.DCM_PATH+".constants");
		}
		if(!pathList.contains(CodeGenerator.DCM_PATH+".types"))
		{
			ostream.write("import ".getBytes());
			ostream.write((CodeGenerator.DCM_PATH+".types").getBytes());
			ostream.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.DCM_PATH+".types");
		}


		//fcm constants, type and system path
		if(!pathList.contains(CodeGenerator.FCM_PATH+".constants"))
		{
			ostream.write("import ".getBytes());
			ostream.write((CodeGenerator.FCM_PATH+".constants").getBytes());
			ostream.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.FCM_PATH+".constants");
		}
		if(!pathList.contains(CodeGenerator.FCM_PATH+".types"))
		{
			ostream.write("import ".getBytes());
			ostream.write((CodeGenerator.FCM_PATH+".types").getBytes());
			ostream.write(".*;\n".getBytes());
			pathList.add(CodeGenerator.FCM_PATH+".types");
		}



      //get the iterator from the modulelist
      Iterator iter = list.iterator();

      //loop the the list
      while(iter.hasNext())
      {

        //get the module name
		Map.Entry entry = (Map.Entry) iter.next();
		String output = (String)entry.getKey();
		HashMap map = (HashMap) entry.getValue();

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
				ostream.write("import ".getBytes());
				ostream.write(path.getBytes());
				ostream.write(";\n".getBytes());
			}
			ostream.write("\n".getBytes());
		}


		if(!pathList.contains(rmiPath))
		{
	        ostream.write("import ".getBytes());
	        ostream.write(rmiPath.getBytes());
	        ostream.write(".*;\n".getBytes());
	        pathList.add(rmiPath);
		}
		if(!pathList.contains(rmiCons))
		{
	        ostream.write("import ".getBytes());
	        ostream.write(rmiCons.getBytes());
	        ostream.write(".*;\n".getBytes());
	        pathList.add(rmiCons);
		}
		if(!pathList.contains(rmiType))
		{
	        ostream.write("import ".getBytes());
	        ostream.write(rmiType.getBytes());
	        ostream.write(".*;\n".getBytes());
	        pathList.add(rmiType);
		}

      }
    ostream.write("\n\n\n\n".getBytes());

  }


  /**
   * print out the package
   *
   * Method printPackage.
   * @param ostream
   * @throws IOException
   */
  private void printPackage(OutputStream ostream) throws IOException
  {

	String packPath = packageName + "." + type;
    ostream.write("package ".getBytes());

    ostream.write(packPath.getBytes());
    ostream.write(";\n\n".getBytes());

  }





/**
 * print out the set/get of each varible
 *
 * Method printSetGet.
 * @param os
 * @throws IOException
 */
  private void printSetGet(OutputStream os) throws IOException
  {

	  //get the list from the constructtype
      Iterator iter = ht.iterator();

      while(iter.hasNext())
      {

              DeclarationType dt = (DeclarationType) iter.next();


			  //get the variable list
              Iterator varIter = dt.iterator();


				//loop thru the variable list
                while(varIter.hasNext())
                {

					//get the data type of the variable
                      HaviType tempType = (HaviType) varIter.next();


                    //print set
                      String variable = tempType.getTypeName();

					  //construct the function name from the variable name so that the first character is upper case  and the rest of name remain the same case
                      String functionName = variable.substring(0,1).toUpperCase() + variable.substring(1);


                      os.write("\tpublic void set".getBytes());
                      os.write(functionName.getBytes());
                      os.write("(".getBytes());


                      HaviType printType = dt.getDataType();

						//print out the data type base on the variable type
                       printDataType(printType, os);

        				//print out [] if they are array
                      if(dt.getDataType() instanceof SequenceType || tempType instanceof ArrayType)
                        os.write("[]".getBytes());


						//prefix I with variable name in the function parameter
                        os.write((" I" + variable).getBytes());
                        os.write(")\n".getBytes());

                        os.write("\t{\n".getBytes());

                        os.write("\t\t".getBytes());
                        os.write(variable.getBytes());
                        os.write(" = I".getBytes());
                        os.write(variable.getBytes());
                        os.write(";\n".getBytes());
                        os.write("\t}\n\n".getBytes());



                    //print get
                        os.write("\tpublic ".getBytes());

                        printDataType(printType, os);

        				//print out [] if they are array
                        if(dt.getDataType() instanceof SequenceType || tempType instanceof ArrayType)
                          os.write("[]".getBytes());

                        os.write(" get".getBytes());
                        os.write(functionName.getBytes());
                        os.write("()\n".getBytes());
                        os.write("\t{\n".getBytes());

                        os.write("\t\treturn ".getBytes());
                        os.write(variable.getBytes());
                        os.write(";\n".getBytes());
                        os.write("\t}\n\n\n".getBytes());

                }

      }

  }





}
