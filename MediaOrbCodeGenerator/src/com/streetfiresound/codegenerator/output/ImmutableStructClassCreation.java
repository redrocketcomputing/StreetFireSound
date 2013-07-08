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
 * $Id: ImmutableStructClassCreation.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.IOException;
import java.io.OutputStream;
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
 */
public class ImmutableStructClassCreation implements ConstTypeDefinition
{

    ConstructType ht;
	String system;
	String constant;
	String packageName;
	String type;


  /**
   * Constructor for ClassCreation.
   */
  public ImmutableStructClassCreation(ConstructType ct)
  {
    super();
    this.ht = ct;
	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");


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

        new WriteHaviImmutableObjectClass(ht, fos);

        printSetGet(fos);

        printCloseClass(fos);
        fos.close();


  }

/**
 * Method printDataType.
 * @param dt
 * @param os
 * @throws IOException
 */
  private void printDataType(HaviType dt, OutputStream os)  throws IOException
  {
           HaviType dataType = dt;

           if(dt instanceof SequenceType)
                dataType = ((SequenceType) dt).getDataType();


            switch(dataType.getConstantTypeDef())
            {
                case ENUM:
                  os.write("int".getBytes());
                  break;


                case STRUCT:
                case UNION:
                case UNIONSTRUCT:
                  os.write(dt.getTypeName().getBytes());
                  break;


                default:
                  dt.output(os);
            }


  }

/**
 * Method printPrivate.
 * @param ostream
 * @throws IOException
 */
  private void printPrivate(OutputStream ostream) throws IOException
  {


        //get the iterator
        Iterator iter = ht.iterator();

        //loop thru the content and the content must be a DeclarationType
        while(iter.hasNext())
        {
               DeclarationType dt = (DeclarationType) iter.next();

                ostream.write("\tprivate ".getBytes());

               HaviType datatype = dt.getDataType();
               printDataType(datatype, ostream);


              if(dt.getDataType() instanceof SequenceType)
                  ostream.write("[]".getBytes());

              ostream.write(" ".getBytes());

              ostream.write(" ".getBytes());
              Iterator varIter = dt.iterator();
              while(varIter.hasNext())
              {
                  HaviType hType = (HaviType) varIter.next();

                  ostream.write(hType.getTypeName().getBytes());

                  if(hType instanceof ArrayType)
                    ostream.write("[]".getBytes());


                   if(varIter.hasNext())
                    ostream.write(", ".getBytes());
              }

              ostream.write(";\n".getBytes());

        } //end while

        ostream.write("\n\n".getBytes());

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
      ostream.write("(){}\n\n\n".getBytes());
  }






/**
 * Method constructor2Write.
 * @param datatype
 * @param os
 * @throws IOException
 */
  private void constructor2Write(HaviType datatype, OutputStream os) throws IOException
  {

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
            int sizeCount = -1;

            while(iter.hasNext())
            {

                     String loop = Integer.toBinaryString(sizeCount++);

                    DeclarationType dt = (DeclarationType ) iter.next();

                    //get variable list
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
                              dt.getDataType().output(ostream);
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
      ostream.write(" extends HaviImmutableObject".getBytes());



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


     while(iter.hasNext())
     {
              DeclarationType dt = (DeclarationType )iter.next();

              HaviType dataType = dt.getDataType();

              if(dataType.getConstantTypeDef()  == ENUM)
              {
                    //if flag equals 0 means it is the first time, so we need to print out implements
                    //if flag equals 1 only print out ","
                    if(flag == 0)
                    {
                      flag = 1;
                      ostream.write(" implements Const".getBytes());
                    }
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
		String constantPath=packageName+"."+constant;
		String typePath=packageName+"."+type;
		String systemPath=   packageName+"."+system;
		Set set = CodeGenerator.projectList.entrySet();

	    ostream.write("import java.io.*;\n".getBytes());
	    ostream.write("import java.util.*;\n\n".getBytes());


        //import constant
        ostream.write("import ".getBytes());
        ostream.write(constantPath.getBytes());
        ostream.write(".*;\n".getBytes());


        //import type
        if(!(typePath.equals(CodeGenerator.packagePath)))
        {
          ostream.write("import ".getBytes());
          ostream.write(typePath.getBytes());
          ostream.write(".*;\n\n".getBytes());
        }

      //get the iterator from the modulelist
//      Iterator iter = driver.moduleList.iterator();
      Iterator iter = set.iterator();

      //loop the the list
      while(iter.hasNext())
      {

		Map.Entry entry = (Map.Entry)iter.next();

        //get the module name
        String output = (String) entry.getKey();
        HashMap map =  (HashMap) entry.getValue();


        //if module name is same as currentPackage name the skip
        if(output.equalsIgnoreCase(CodeGenerator.currentPackage))//  || output.equalsIgnoreCase(driver.COMPARETYPE))
          continue;

		String rmiPath = ((String) map.get("PACKAGE")) + "." + ((String) map.get("SYSTEM"));
        ostream.write("import ".getBytes());
        ostream.write(rmiPath.getBytes());
        ostream.write(".".getBytes());
        ostream.write(output.getBytes());
        ostream.write(".*;\n".getBytes());
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

    ostream.write("package ".getBytes());

    ostream.write(CodeGenerator.packagePath.getBytes());
    ostream.write(";\n\n".getBytes());

  }



  private void printSetGet(OutputStream os) throws IOException
  {
      Iterator iter = ht.iterator();

      while(iter.hasNext())
      {

            DeclarationType dt = (DeclarationType) iter.next();


            HaviType  dataType = dt.getDataType();


            Iterator vIter = dt.iterator();
            while(vIter.hasNext())
            {
                        HaviType vType = (HaviType) vIter.next();
                        String variable = vType.getTypeName();

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
                          printDataType(dataType, os);

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
            } //end while loop

      }//end while loop

  } //end function

}
