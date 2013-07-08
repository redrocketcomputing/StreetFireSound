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
 * $Id: UnionChildClassCreation.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
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
import java.util.StringTokenizer;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ArrayType;
import com.streetfiresound.codegenerator.types.BaseDataType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
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
public class UnionChildClassCreation  implements ConstTypeDefinition
{

    ArrayList al;
    String parentName;
    String className;
    String fullClassName;
    int count=0;


	String constant;
	String type;
	String system;
	String packageName;
	String exception;


	HashSet localExceptionList;

/**
 * Method UnionChildClassCreation.
 * @param className
 * @param parentName
 * @param al
 * @param count
 * @throws IOException
 */
  public UnionChildClassCreation(String className, String parentName, ArrayList al, int count) throws IOException
  {


    this.al = al;
    this.parentName = parentName;
    this.className = className;
    this.count = count;
	localExceptionList = new HashSet();


	HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
	constant = (String) configInfo.get("CONSTANT");
	system = (String) configInfo.get("SYSTEM");
	type = (String) configInfo.get("TYPE");
	packageName = (String) configInfo.get("PACKAGE");
	exception = (String) configInfo.get("EXCEPTION");

	String rootPath = (String)configInfo.get("ROOTPATH");

    String location = rootPath+"/"+packageName.replace('.','/') + "/" + type.replace('.','/');


	String filename = makeFileName(className);
	this.fullClassName = filename+parentName;

     FileOutputStream fos = new GplHeaderFileOutputStream(location + "/" +filename+parentName+".java");
     outputToFile(fos);

     fos.close();


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
      printClass(os);
      printInheritance(os);
      printOpenClass(os);
      printPrivate(os);
      printConstructor1(os);
      printConstructor2(os);
      printConstructor3(os);
      printDiscriminator(os);
      printMarshal(os);
      printUnMarshal(os);
      printEquals(os);
      printHashCode(os);
      printClone(os);
	  printToString(os);

     printSetGet(os);

      printCloseClass(os);

  }


/**
 * Method printDiscriminator.
 * @param os
 * @throws IOException
 */
private void printDiscriminator(OutputStream os) throws IOException
{

    os.write("\tpublic int getDiscriminator()\n".getBytes());
    os.write("\t{\n".getBytes());
    os.write("\t\treturn ".getBytes());
    os.write(className.toUpperCase().getBytes());
    os.write(";\n\n".getBytes());
    os.write("\t}\n\n\n".getBytes());

}









  /**
   * print out the package
   *
   * Method printPackage.
   * @param os
   * @throws IOException
   */
  private void printPackage(OutputStream os) throws IOException
  {
  	String pack = packageName + "." + type;
    os.write("package ".getBytes());
	os.write(pack.getBytes());
    os.write(";\n\n".getBytes());
  }



  /**
   * print out the private variable
   *
   * declarationtype datatype can be basedatatype, sequentype or literaltype
   *
   * Method printPrivate.
   * @param os
   * @throws IOException
   */


 private void privateWrite(HaviType datatype, OutputStream os, boolean array, int size) throws IOException
 {
		  HaviType type = datatype;

          switch(type.getConstantTypeDef())
          {

                case SEQUENCE:
                    SequenceType st = (SequenceType) type;
					HaviType newtype = st.getDataType();
					privateWrite(newtype, os, true, Integer.parseInt(st.getValue()));
					break;

                case ENUM:
                      os.write("int".getBytes());

                      break;

                case STRUCT:
                case UNION:
                case UNIONSTRUCT:
                      os.write(type.getTypeName().getBytes());
                      break;

				case LITERAL:
					HaviType newtype2 = (HaviType) CodeGenerator.dataTypeList.get(type.getTypeName());

					if(newtype2 == null)
	                      os.write(type.getTypeName().getBytes());
	                else
	                	privateWrite(newtype2, os, array, size);
					break;

                default:
                  type.output(os);

          }

 }


  private void printPrivate(OutputStream os) throws IOException
  {

		  //get and loop thru the variable list
          Iterator iter = al.iterator();
          while(iter.hasNext())
          {

             DeclarationType dt = (DeclarationType) iter.next();


             //in union, delcarationtype variable list only contains one item
             HaviType var = (HaviType) dt.iterator().next();


             os.write("\tprivate ".getBytes());
      		 privateWrite(dt.getDataType(), os, false, 0);



			 if(dt.getDataType() instanceof SequenceType)
			 	os.write("[]".getBytes());


             os.write(" ".getBytes());
             var.output(os);
             if(var instanceof ArrayType)
				os.write("[]".getBytes());
             os.write(";\n".getBytes());

          }

          os.write("\n\n\n".getBytes());
  }


/**
 * Method printClass.
 * @param os
 * @throws IOException
 */
  private void printClass(OutputStream os) throws IOException
  {


      os.write("public class ".getBytes());
      os.write(fullClassName.getBytes());
  }


/**
 * Method printOpenClass.
 * @param os
 * @throws IOException
 */
  private void printOpenClass(OutputStream os) throws IOException
  {

      os.write("\n{\n".getBytes());

  }

/**
 * Method printInheritance.
 * @param os
 * @throws IOException
 */
  private void printInheritance(OutputStream os) throws IOException
  {
      os.write(" extends ".getBytes());
      os.write(parentName.getBytes());

  }


/**
 * A default constructor
 *
 * Method printConstructor1.
 * @param os
 * @throws IOException
 */
  private void printConstructor1(OutputStream os) throws IOException
  {

    os.write("\tpublic ".getBytes());
    os.write(fullClassName.getBytes());
    os.write("()\n".getBytes());
    os.write("\t{\n".getBytes());


      Iterator iter = al.iterator();
      while(iter.hasNext())
      {
          //get the variables list from the DeclartionType
          DeclarationType dt = (DeclarationType ) iter.next();
		  constructor1Write(dt.getDataType(), dt.iterator(), os, false, 0);
      }


    os.write("\t}\n\n".getBytes());


  }




	/**
	 * Method constructor1Write.
	 * @param datatype
	 * @param vIter
	 * @param os
	 * @param array
	 * @throws IOException
	 */
	private void constructor1Write(HaviType datatype, Iterator vIter, OutputStream os, boolean array, int arraySize) throws IOException
	{
		HaviType type = datatype;


		switch(type.getConstantTypeDef())
		{
			case SEQUENCE:
				type = ((SequenceType) datatype).getDataType();
				String value = ((SequenceType) datatype).getValue();
				constructor1Write(type, vIter, os, true, Integer.parseInt(value));
				break;

			case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(datatype.getTypeName());
				if(newtype == null)
				{
						while(vIter.hasNext())
						{

							HaviType vType = (HaviType)vIter.next();

							os.write("\t\t".getBytes());
							os.write(vType.getTypeName().getBytes());

							os.write(" = new ".getBytes());
							os.write(type.getTypeName().getBytes());

							if(array == true || vType instanceof ArrayType)
							{

								if(array == true)
								{
									os.write("[".getBytes());

									os.write(Integer.toString(arraySize).getBytes());
									os.write("]".getBytes());
								}

								if(vType instanceof ArrayType)
								{
									ArrayType at = (ArrayType)vType;
									int size = Integer.parseInt(at.getValue());
									os.write("[".getBytes());

									os.write(at.getValue().getBytes());
									os.write("]".getBytes());
								}

								os.write(";\n".getBytes());
							}
							else
								os.write("();\n".getBytes());
						}

				}
				else
					constructor1Write(newtype, vIter, os, array, arraySize);

				break;

			case ENUM:
				while(vIter.hasNext())
				{

					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());
					os.write(vType.getTypeName().getBytes());


					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new int".getBytes());

						if(array ==  true)
						{
							os.write("[".getBytes());
							os.write(Integer.toString(arraySize).getBytes());
							os.write("]".getBytes());
						}

						if(vType instanceof ArrayType)
						{
							ArrayType at = (ArrayType) vType;

							os.write("[".getBytes());
							os.write(at.getValue().getBytes());
							os.write("]".getBytes());
						}
						os.write(";\n".getBytes());
					}
					else
						os.write(" = 0;\n".getBytes());

				}
				break;


			case STRUCT:
			case UNIONSTRUCT:
			case UNION:
				while(vIter.hasNext())
				{

					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());
					os.write(vType.getTypeName().getBytes());

					os.write(" = new ".getBytes());
					os.write(type.getTypeName().getBytes());

						if(array == true || vType instanceof ArrayType)
						{

							if(array == true)
							{
								os.write("[".getBytes());
								os.write(Integer.toString(arraySize).getBytes());
								os.write("]".getBytes());

							}
							if(vType instanceof ArrayType)
							{
								ArrayType at = (ArrayType)vType;
								int size = Integer.parseInt(at.getValue());
								os.write("[".getBytes());
								os.write(at.getValue().getBytes());
								os.write("]".getBytes());
							}

							os.write(";\n".getBytes());
						}
						else
							os.write("();\n".getBytes());


				}
				break;

/*
			case UNION:
				while(vIter.hasNext())
				{

					HaviType vType = (HaviType)vIter.next();


					os.write("\t\t".getBytes());
					os.write(vType.getTypeName().getBytes());

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
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());
					os.write(vType.getTypeName().getBytes());

					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new ".getBytes());
						type.output(os);

						if(array == true)
						{
							os.write("[".getBytes());
							os.write(Integer.toString(arraySize).getBytes());
							os.write("]".getBytes());

						}
						if(vType instanceof ArrayType)
						{
							ArrayType at = (ArrayType)vType;
							int size = Integer.parseInt(at.getValue());
							os.write("[".getBytes());
							os.write(at.getValue().getBytes());
							os.write("]".getBytes());
						}

						os.write(";\n".getBytes());

					}
					else
						os.write(" = false;\n".getBytes());

				}
				break;
			case STRING:
			case WSTRING:
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());

					os.write(vType.getTypeName().getBytes());
					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new ".getBytes());
						type.output(os);

						if(array == true)
						{
							os.write("[".getBytes());
							os.write(Integer.toString(arraySize).getBytes());
							os.write("]".getBytes());

						}
						if(vType instanceof ArrayType)
						{
							ArrayType at = (ArrayType)vType;
							int size = Integer.parseInt(at.getValue());
							os.write("[".getBytes());
							os.write(at.getValue().getBytes());
							os.write("]".getBytes());
						}
						os.write(";\n".getBytes());
					}
					else
						os.write(" = \"\";\n".getBytes());

				}
				break;


			default:
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\t\t".getBytes());
					os.write(vType.getTypeName().getBytes());

					if(array == true || vType instanceof ArrayType)
					{
						os.write(" = new ".getBytes());
						type.output(os);

						if(array == true)
						{
							os.write("[".getBytes());
							os.write(Integer.toString(arraySize).getBytes());
							os.write("]".getBytes());

						}
						if(vType instanceof ArrayType)
						{
							ArrayType at = (ArrayType)vType;
							int size = Integer.parseInt(at.getValue());
							os.write("[".getBytes());
							os.write(at.getValue().getBytes());
							os.write("]".getBytes());
						}

						os.write(";\n".getBytes());
					}
					else
						os.write(" = 0;\n".getBytes());

				}
				break;


		}	//end switch



	}











/**
 * Method printConstructor2.
 * @param os
 * @throws IOException
 */
  private void printConstructor2(OutputStream os) throws IOException
  {

    os.write("\tpublic ".getBytes());
    os.write(fullClassName.getBytes());
    os.write("(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());

    os.write("\t{\n".getBytes());


        os.write("\t\tunmarshal(hbais);\n".getBytes());
    os.write("\t}\n\n".getBytes());
  }







 private void Constructor3Write(HaviType datatype, OutputStream os, boolean array) throws IOException
 {
	 	HaviType type = datatype;

        switch(type.getConstantTypeDef())
        {

            case SEQUENCE:
                SequenceType st = (SequenceType) type;
				Constructor3Write(st.getDataType(), os, true);
                break;
           case ENUM:
                os.write("int".getBytes());
                break;

 		   case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(type.getTypeName());
				if(newtype == null)
	                os.write(type.getTypeName().getBytes());
	            else
	            	Constructor3Write(newtype, os, array);

	            break;



          case STRUCT:
          case UNION:
          case UNIONSTRUCT:
                os.write(type.getTypeName().getBytes());
                break;

          default:
            type.output(os);


        }

 }






/**
 * Method printConstructor3.
 * @param os
 * @throws IOException
 */
  private void printConstructor3(OutputStream os) throws IOException
  {
    os.write("\tpublic ".getBytes());
    os.write(fullClassName.getBytes());
    os.write("(".getBytes());   //start print parameter list


    //print function parameter list
    Iterator iter = al.iterator();
    while(iter.hasNext())
    {
            DeclarationType dt = (DeclarationType) iter.next();
			Constructor3Write(dt.getDataType(), os, false);



 			if(dt.getDataType() instanceof SequenceType)
 				os.write("[]".getBytes());


            os.write(" ".getBytes());
           //in union, delcarationtype variable list only contains one item
           HaviType var = (HaviType) dt.iterator().next();
           var.output(os);
 			if(var instanceof ArrayType)
 				os.write("[]".getBytes());

          if(iter.hasNext())
             os.write(", ".getBytes());
    }
    os.write(")\n".getBytes());
    //end print parameter list



    //start print content
    os.write("\t{\n".getBytes());
    iter = al.iterator();
    while(iter.hasNext())
    {
        DeclarationType dt = (DeclarationType) iter.next();

        String vName = ((HaviType) dt.iterator().next()).getTypeName();
        //print variable
        os.write("\t\tthis.".getBytes());
        os.write(vName.getBytes());
        os.write(" = ".getBytes());
        os.write(vName.getBytes());
        os.write(";\n".getBytes());
    }
    os.write("\t}\n\n\n".getBytes());//end print content

  }





/**
 * Method printCloseClass.
 * @param os
 * @throws IOException
 */
  private void printCloseClass(OutputStream os) throws IOException
  {
    os.write("\n}\n\n".getBytes());
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
		String exceptionPath = packageName+"."+exception;
		String packPath = packageName + "." + system;


		Set set = CodeGenerator.projectList.entrySet();

		ArrayList pathList = new ArrayList();
		pathList.add(packPath);

		os.write("import ".getBytes());
		os.write(CodeGenerator.JAVA_IO.getBytes());
		os.write(";\n".getBytes());



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
//        if(output.equalsIgnoreCase(driver.currentPackage))// || output.equalsIgnoreCase(driver.COMPARETYPE))
//          continue;

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

      os.write("\n\n\n".getBytes());

  }



/**
 * Method printDataType.
 * @param dataType
 * @param os
 * @throws IOException
 */
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


		  case LITERAL:
				HaviType newtype = (HaviType) CodeGenerator.dataTypeList.get(thisDataType.getTypeName());
				if(newtype == null)
					os.write(dataType.getTypeName().getBytes());
				else
					printDataType(newtype, os);

				break;


          default:
            dataType.output(os);
            break;

      }//end switch


  }



/**
 * Method printSetGet.
 * @param os
 * @throws IOException
 */
  private void printSetGet(OutputStream os) throws IOException
  {
      Iterator iter = al.iterator();

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




/**
 * Method printMarshalWrite.
 * @param loopCount
 * @param type
 * @param list
 * @param os
 * @param arrayFlag
 * @throws IOException
 */
  private void printMarshalWrite(int loopCount, HaviType type, ArrayList list, OutputStream os, boolean arrayFlag) throws IOException
  {

          HaviType datatype = type;
          Iterator varIter = list.iterator();


            switch(datatype.getConstantTypeDef())
            {

                  case SEQUENCE:
                        datatype = ((SequenceType) type).getDataType();
                        printMarshalWrite(loopCount, datatype, list, os, true);
                        break;


                  case LITERAL:
                      HaviType h = (HaviType)CodeGenerator.dataTypeList.get(datatype.getTypeName());
                      if(h == null)
                      {
							  localExceptionList.add("HaviMarshallingException");
		                      while(varIter.hasNext())
		                      {
		                            HaviType vType = (HaviType)varIter.next();
		                            if(arrayFlag == true || vType instanceof ArrayType)
		                            {
		                                     os.write("\n\t\t\tint size".getBytes());
		                                     os.write(Integer.toString(loopCount).getBytes());
		                                     os.write(" = ".getBytes());
		                                     os.write(vType.getTypeName().getBytes());
		                                     os.write(".length;\n".getBytes());

		                                     os.write("\t\t\tho.writeInt(size".getBytes());
		                                     os.write(Integer.toString(loopCount).getBytes());
		                                     os.write(");\n".getBytes());

		                                     os.write("\t\t\tfor(int i=0; i < size".getBytes());
		                                     os.write(Integer.toString(loopCount).getBytes());
		                                     os.write("; i++)\n\t\t\t\t".getBytes());
		                                     os.write(vType.getTypeName().getBytes());
		                                     os.write("[i].marshal(ho);\n\n".getBytes());
		                            }
		                            else
		                            {
		                                      os.write("\t\t\t".getBytes());
		                                      vType.output(os);
		                                      os.write(".marshal(ho);\n".getBytes());


		                            }

		                      }
                      }
					  else
	                      printMarshalWrite(loopCount, h, list, os, arrayFlag);

                      break;



                    case ENUM:
                        while(varIter.hasNext())
                        {
                              HaviType thisHt = (HaviType) varIter.next();
                              os.write("\t\t\tho.writeInt(".getBytes());
                              thisHt.output(os);
                              os.write(");\n".getBytes());
                        }
                        break;


                    case STRUCT:
                    case UNION:
                    case UNIONSTRUCT:
					  localExceptionList.add("HaviMarshallingException");
                      while(varIter.hasNext())
                      {
                            HaviType vType = (HaviType)varIter.next();


                            if(arrayFlag == true || vType instanceof ArrayType)
                            {
                                     os.write("\n\t\t\tint size".getBytes());
                                     os.write(Integer.toString(loopCount).getBytes());
                                     os.write(" = ".getBytes());
                                     os.write(vType.getTypeName().getBytes());
                                     os.write(".length;\n".getBytes());

                                     os.write("\t\t\tho.writeInt(size".getBytes());
                                     os.write(Integer.toString(loopCount).getBytes());
                                     os.write(");\n".getBytes());

                                     os.write("\t\t\tfor(int i=0; i < size".getBytes());
                                     os.write(Integer.toString(loopCount).getBytes());
                                     os.write("; i++)\n\t\t\t\t".getBytes());
                                     os.write(vType.getTypeName().getBytes());
                                     os.write("[i].marshal(ho);\n\n".getBytes());
                            }
                            else
                            {
                                      os.write("\t\t\t".getBytes());
                                      vType.output(os);
                                      os.write(".marshal(ho);\n".getBytes());


                            }

                      }
                      break;



                      default:  //base data type
                      while(varIter.hasNext())
                      {
                                HaviType vType = (HaviType)varIter.next();


                                if(arrayFlag == true || vType instanceof ArrayType)
                                {
                                               os.write("\n\t\t\tint size".getBytes());
                                               os.write(Integer.toString(loopCount).getBytes());
                                               os.write(" = ".getBytes());
                                               os.write(vType.getTypeName().getBytes());
                                               os.write(".length;\n".getBytes());

                                               os.write("\t\t\tho.writeInt(size".getBytes());
                                               os.write(Integer.toString(loopCount).getBytes());
                                               os.write(");\n".getBytes());

                                               os.write("\t\t\tfor(int i=0; i < size".getBytes());
                                               os.write(Integer.toString(loopCount).getBytes());
                                               os.write("; i++)\n".getBytes());


                                               os.write("\t\t\t\tho.write".getBytes());
                                               os.write(((BaseDataType) datatype).getMarshalString().getBytes());
                                               os.write("(".getBytes());
                                               os.write(vType.getTypeName().getBytes());
                                               os.write("[i]);\n".getBytes());
                                 }


                                 else
                                 {

                                                 os.write("\t\t\t".getBytes());
                                                 os.write("ho.write".getBytes());
                                                 os.write(((BaseDataType) datatype).getMarshalString().getBytes());
                                                 os.write("(".getBytes());
                                                 vType.output(os) ;
                                                 os.write(");\n".getBytes());
                                  }


                      }
                      break;



            }//end switch

    } //end printWrite();




  /**
   *
   * print out the marshal function
   *
   * Method printMarshal.
   * @param ostream
   * @throws IOException
   */
  private void printMarshal(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic void marshal(HaviByteArrayOutputStream ho) throws HaviMarshallingException\n".getBytes());
    ostream.write("\t{\n".getBytes());


    ostream.write("\t\ttry\n\t\t{\n".getBytes());
    //loop thru the list


    //get the list from constructype. the constructype must contains a list of declarationtype
    Iterator iter = al.iterator();


    ostream.write("\t\t\tho.writeInt(".getBytes());
    ostream.write(Integer.toString(count).getBytes());
    ostream.write(");\n".getBytes());

    int loopCount = 0;
    while(iter.hasNext())
    {

          //get the declarationtype its datatype contains only basedatatype, sequencetype and literaltype
          DeclarationType dt  = (DeclarationType)iter.next();
          loopCount++;

          HaviType type = dt.getDataType();
          ArrayList list = dt.getChildList();

          printMarshalWrite(loopCount, type, list , ostream, false);



    }
    ostream.write("\t\t}\n".getBytes());
    ostream.write("\t\tcatch(IOException e)\n".getBytes());
    ostream.write("\t\t{\n".getBytes());
    ostream.write("\t\t\tthrow new HaviMarshallingException(e.getMessage());\n".getBytes());
    ostream.write("\t\t}\n\n".getBytes());


	if(localExceptionList.size() > 0)
	{
		Iterator exceptionIter = localExceptionList.iterator();
		while(exceptionIter.hasNext())
		{
			String exceptionName= (String) exceptionIter.next();
			ostream.write("\t\tcatch(".getBytes());
			ostream.write(exceptionName.getBytes());
			ostream.write(" e)\n".getBytes());
		    ostream.write("\t\t{\n".getBytes());
		    ostream.write("\t\t\tthrow new HaviMarshallingException(e.getMessage());\n".getBytes());
		    ostream.write("\t\t}\n\n".getBytes());
		}
	}

    ostream.write("\t}\n\n".getBytes());
    localExceptionList.clear();
  }




/**
 * Method printUnMarshalWrite.
 * @param loopCount
 * @param type
 * @param list
 * @param os
 * @param arrayFlag
 * @throws IOException
 */
  private void printUnMarshalWrite(int loopCount, HaviType type, ArrayList list, OutputStream os, boolean arrayFlag) throws IOException
  {

          HaviType datatype = type;
          Iterator varIter = list.iterator();

		  //avoid variable name duplication
          String loop = Integer.toString(loopCount);

            switch(datatype.getConstantTypeDef())
            {
                    case ENUM:
						localExceptionList.add("IOException");
                        while(varIter.hasNext())
                        {
                              HaviType thisHt = (HaviType) varIter.next();
                              os.write("\t\t\t".getBytes());
                              os.write(thisHt.getTypeName().getBytes());
                              os.write("= hi.readInt();\n".getBytes());
                        }
                        break;

                    case SEQUENCE:
                        datatype = ((SequenceType) type).getDataType();
                        printUnMarshalWrite(loopCount, datatype, list, os, true);
                        break;


                    case LITERAL:

                        HaviType newtype = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());
                        if(newtype == null)
						{
		  					  localExceptionList.add("HaviUnmarshallingException");
		                      while(varIter.hasNext())
		                      {
		                            HaviType vType = (HaviType)varIter.next();

		                            if(arrayFlag == true || vType instanceof ArrayType)
		                            {
		                                  os.write("\n\t\t\tint loopSize".getBytes());
		                                  os.write(loop.getBytes());
		                                  os.write(" = hi.readInt();\n\t\t\t".getBytes());


		                                  os.write(vType.getTypeName().getBytes());
		                                  os.write("= new ".getBytes());
		                                  os.write(datatype.getTypeName().getBytes());
		                                  os.write("[loopSize".getBytes());
		                                  os.write(loop.getBytes());
		                                  os.write("];\n".getBytes());

		                                  os.write("\t\t\tfor(int i = 0; i < loopSize".getBytes());
		                                  os.write(loop.getBytes());
		                                  os.write("; i++)\n\t\t\t\t".getBytes());

		                                  os.write(vType.getTypeName().getBytes());
		                                  os.write("[i] = new ".getBytes());
		                                  os.write(datatype.getTypeName().getBytes());
		                                  os.write("(hi);\n\n".getBytes());
		                            }
		                            else
		                            {
		                                      os.write("\t\t\t".getBytes());
		                                      os.write(vType.getTypeName().getBytes());
		                                      os.write(" = new ".getBytes());
		                                      os.write(datatype.getTypeName().getBytes());
		                                      os.write("(hi);\n".getBytes());

		                            }

		                      }
						}
						else
	                        printUnMarshalWrite(loopCount, newtype, list, os, arrayFlag);

                        break;


                    case STRUCT:
					  localExceptionList.add("HaviUnmarshallingException");
                      while(varIter.hasNext())
                      {
                            HaviType vType = (HaviType)varIter.next();

                            if(arrayFlag == true || vType instanceof ArrayType)
                            {
                                  os.write("\n\t\t\tint loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write(" = hi.readInt();\n\t\t\t".getBytes());


                                  os.write(vType.getTypeName().getBytes());
                                  os.write("= new ".getBytes());
                                  os.write(datatype.getTypeName().getBytes());
                                  os.write("[loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write("];\n".getBytes());

                                  os.write("\t\t\tfor(int i = 0; i < loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write("; i++)\n\t\t\t\t".getBytes());

                                  os.write(vType.getTypeName().getBytes());
                                  os.write("[i] = new ".getBytes());
                                  os.write(datatype.getTypeName().getBytes());
                                  os.write("(hi);\n\n".getBytes());
                            }
                            else
                            {
                                      os.write("\t\t\t".getBytes());
                                      os.write(vType.getTypeName().getBytes());
                                      os.write(" = new ".getBytes());
                                      os.write(datatype.getTypeName().getBytes());
                                      os.write("(hi);\n".getBytes());

                            }

                      }
                      break;


                    case UNION:
						localExceptionList.add("HaviUnmarshallingException");
						while(varIter.hasNext())
						{
							HaviType vType = (HaviType)varIter.next();
							if(arrayFlag == true || vType instanceof ArrayType)
							{
                                  os.write("\n\t\t\tint loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write(" = hi.readInt();\n\t\t\t".getBytes());


                                  os.write(vType.getTypeName().getBytes());
                                  os.write("= new ".getBytes());
                                  os.write(datatype.getTypeName().getBytes());
                                  os.write("[loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write("];\n".getBytes());

                                  os.write("\t\t\tfor(int i = 0; i < loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write("; i++)\n\t\t\t\t".getBytes());

                                  os.write(vType.getTypeName().getBytes());
                                  os.write("[i] = ".getBytes());
                                  os.write(datatype.getTypeName().getBytes());
                                  os.write(".create(hi);\n".getBytes());



							}
							else
							{
								os.write("\t\t\t".getBytes());
								os.write(vType.getTypeName().getBytes());
								os.write(" = ".getBytes());
								os.write(datatype.getTypeName().getBytes());
								os.write(".create(hi);\n".getBytes());

							}
						}
                    	break;


                    case UNIONSTRUCT:
					  localExceptionList.add("HaviUnmarshallingException");
                      while(varIter.hasNext())
                      {
                            HaviType vType = (HaviType)varIter.next();

                            if(arrayFlag == true || vType instanceof ArrayType)
                            {
                                  os.write("\n\t\t\tint loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write(" = hi.readInt();\n\t\t\t".getBytes());


                                  os.write(vType.getTypeName().getBytes());
                                  os.write("= new ".getBytes());
                                  os.write(datatype.getTypeName().getBytes());
                                  os.write("[loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write("];\n".getBytes());

                                  os.write("\t\t\tfor(int i = 0; i < loopSize".getBytes());
                                  os.write(loop.getBytes());
                                  os.write("; i++)\n\t\t\t\t".getBytes());

								  os.write("hi.readInt();\n\t\t\t\t".getBytes());
                                  os.write(vType.getTypeName().getBytes());
                                  os.write("[i] = new ".getBytes());
                                  os.write(datatype.getTypeName().getBytes());
                                  os.write("(hi);\n\n".getBytes());


                            }
                            else
                            {
									  os.write("\t\t\thi.readInt();\n".getBytes());
                                      os.write("\t\t\t".getBytes());
                                      os.write(vType.getTypeName().getBytes());
                                      os.write(" = new ".getBytes());
                                      os.write(datatype.getTypeName().getBytes());
                                      os.write("(hi);\n".getBytes());
                            }

                      }
                      break;


                      default:  //base data type
					  localExceptionList.add("IOException");
                      while(varIter.hasNext())
                      {
                                HaviType vType = (HaviType)varIter.next();


                                if(arrayFlag == true || vType instanceof ArrayType)
                                {
                                        os.write("\n\t\t\tint loopSize".getBytes());
                                        os.write(loop.getBytes());
                                        os.write(" = hi.readInt();\n\t\t\t".getBytes());

                                        vType.output(os);
                                        os.write("= new ".getBytes());
                                        datatype.output(os);
                                        os.write("[loopSize".getBytes());
                                        os.write(loop.getBytes());
                                        os.write("];\n".getBytes());

                                        os.write("\t\t\tfor(int i = 0; i < loopSize".getBytes());
                                        os.write(loop.getBytes());
                                        os.write("; i++)\n\t\t\t\t".getBytes());

                                        vType.output(os);
                                        os.write("[i] = hi.read".getBytes());
                                        os.write(((BaseDataType) datatype).getMarshalString().getBytes());
                                        os.write("();\n\n".getBytes());
                                 }
                                 else
                                 {
                                         os.write("\t\t\t".getBytes());
                                         os.write(vType.getTypeName().getBytes());
                                         os.write(" = hi.read".getBytes());
                                         os.write(((BaseDataType) datatype).getMarshalString().getBytes());
                                         os.write("(".getBytes());
                                         os.write(");\n".getBytes());

                                  }

                      }
                      break;



            }//end switch

    } //end printWrite();




/**
 * Method printUnMarshal.
 * @param ostream
 * @throws IOException
 */
  private void printUnMarshal(OutputStream ostream) throws IOException
  {
          ostream.write("\tpublic void unmarshal(HaviByteArrayInputStream hi) throws HaviUnmarshallingException\n".getBytes());
          ostream.write("\t{\n".getBytes());


          //get the iterator from the constructype, the list must be a declarationtype
          Iterator iter = al.iterator();

         ostream.write("\t\ttry\n\t\t{\n".getBytes());

          int loopCount = 0;
          while(iter.hasNext())
          {

                //get the declarationtype its datatype contains only basedatatype, sequencetype and literaltype
                DeclarationType dt  = (DeclarationType)iter.next();
                loopCount++;

                HaviType dtype = dt.getDataType();
                ArrayList list = dt.getChildList();

                printUnMarshalWrite(loopCount, dtype, list, ostream, false);

          }

          ostream.write("\t\t}\n".getBytes());


		  Iterator exceptionIter = localExceptionList.iterator();
		  while(exceptionIter.hasNext())
		  {
		  	String exceptionName = (String) exceptionIter.next();
		  	ostream.write("\t\tcatch(".getBytes());
		  	ostream.write(exceptionName.getBytes());
			ostream.write(" e)\n".getBytes());
            ostream.write("\t\t{\n".getBytes());
            ostream.write("\t\t\tthrow new HaviUnmarshallingException(e.getMessage());\n".getBytes());
            ostream.write("\t\t}\n\n".getBytes());
		  }

          ostream.write("\t}\n\n".getBytes());

          localExceptionList.clear();

  }



/**
 * Method equalWrite.
 * @param datatype
 * @param vType
 * @param os
 * @param array
 */
//3 tabs
private void equalWrite(HaviType datatype, HaviType vType, OutputStream os, boolean array) throws IOException
{

			String vName = vType.getTypeName();
            switch(datatype.getConstantTypeDef())
            {
                case SEQUENCE:
					equalWrite(	((SequenceType) datatype).getDataType(), vType, os, true);
					break;

               case ENUM:
		               	if(array == true)
		               	{
								os.write("\t\t\t".getBytes());

								os.write("for(int i=0; i< ".getBytes());
								os.write(vName.getBytes());
								os.write(".length; i++)\n".getBytes());
								os.write("\t\t\t{\n".getBytes());


								os.write("\t\t\t\t".getBytes());
								os.write("int[] x = ((".getBytes());
								os.write(fullClassName.getBytes());
								os.write(")o).get".getBytes());
								os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
								os.write("();\n".getBytes());


								os.write("\t\t\t\tif(".getBytes());
								os.write(vName.getBytes());
								os.write("[i] != x[i])\n".getBytes());
								os.write("\t\t\t\t\treturn false;\n".getBytes());


								os.write("\t\t\t}\n\n".getBytes());
		               	}
		               	else
		               	{
							os.write("\t\t\t".getBytes());
		               		os.write("if ( ".getBytes());
		               		os.write(vType.getTypeName().getBytes());
		               		os.write(" != ((".getBytes());
		               		os.write(fullClassName.getBytes());
		               		os.write(") o).get".getBytes());
							os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
		               		os.write("())\n".getBytes());
							os.write("\t\t\t\treturn false;\n\n".getBytes());

		               	}
		                break;


              case LITERAL:
              		HaviType newType = (HaviType)CodeGenerator.dataTypeList.get(datatype.getTypeName());

					//Because type cannot be found, therefore assume it is a havi object
              		if(newType == null)
              		{
		               	if(array == true)
		               	{
								os.write("\t\t\t".getBytes());
								os.write(datatype.getTypeName().getBytes());
								os.write("[] x = ((".getBytes());
								os.write(fullClassName.getBytes());
								os.write(")o).get".getBytes());
								os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
								os.write("();\n".getBytes());



								os.write("\t\t\t".getBytes());
								os.write("for(int i=0; i< ".getBytes());
								os.write(vName.getBytes());
								os.write(".length; i++)\n".getBytes());
								os.write("\t\t\t{\n".getBytes());

								os.write("\t\t\t\tif( !(".getBytes());
								os.write(vName.getBytes());
								os.write("[i].equals(x[i])))\n".getBytes());
								os.write("\t\t\t\t\treturn false;\n\n".getBytes());

								os.write("\t\t\t}\n\n".getBytes());

		               	}
		               	else
		               	{
							os.write("\t\t\t".getBytes());
		               		os.write("if (!".getBytes());
		               		os.write(vType.getTypeName().getBytes());
		               		os.write(".equals(((".getBytes());
		               		os.write(fullClassName.getBytes());
		               		os.write(") o).get".getBytes());
							os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
		               		os.write("()))\n".getBytes());
							os.write("\t\t\t\treturn false;\n\n".getBytes());

		               	}

              		}
              		else
              			equalWrite(newType, vType, os, array);

              		break;



              case STRUCT:
              case UNION:
              case UNIONSTRUCT:
              case STRING:
              case WSTRING:
		               	if(array == true)
		               	{
								os.write("\t\t\t".getBytes());
								os.write(datatype.getTypeName().getBytes());
								os.write("[] x = ((".getBytes());
								os.write(fullClassName.getBytes());
								os.write(")o).get".getBytes());
								os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
								os.write("();\n".getBytes());


								os.write("\t\t\t".getBytes());
								os.write("for(int i=0; i< ".getBytes());
								os.write(vName.getBytes());
								os.write(".length; i++)\n".getBytes());
								os.write("\t\t\t{\n".getBytes());


								os.write("\t\t\t\tif( !(".getBytes());
								os.write(vName.getBytes());
								os.write("[i].equals(x[i])))\n".getBytes());
								os.write("\t\t\t\t\treturn false;\n\n".getBytes());

								os.write("\t\t\t}\n\n".getBytes());

		               	}
		               	else
		               	{
//							os.write("\t\t\t".getBytes());
//		               		os.write("if ( !(".getBytes());
//		               		os.write(vType.getTypeName().getBytes());
//		               		os.write(".equals(o)))\n".getBytes());
//							os.write("\t\t\t\treturn false;\n\n".getBytes());


							os.write("\t\t\t".getBytes());
		               		os.write("if (!".getBytes());
		               		os.write(vType.getTypeName().getBytes());
		               		os.write(".equals(((".getBytes());
		               		os.write(fullClassName.getBytes());
		               		os.write(") o).get".getBytes());
							os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
		               		os.write("()))\n".getBytes());
							os.write("\t\t\t\treturn false;\n\n".getBytes());

		               	}
		                break;
              default:
		               	if(array == true)
		               	{
								os.write("\t\t\t".getBytes());
								os.write("for(int i=0; i< ".getBytes());
								os.write(vName.getBytes());
								os.write(".length; i++)\n".getBytes());
								os.write("\t\t\t{\n".getBytes());

								os.write("\t\t\t\t".getBytes());
								datatype.output(os);
								os.write("[] x = ((".getBytes());
								os.write(fullClassName.getBytes());
								os.write(")o).get".getBytes());
								os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
								os.write("();\n".getBytes());


								os.write("\t\t\t\tif(".getBytes());
								os.write(vName.getBytes());
								os.write("[i] != x[i])\n".getBytes());
								os.write("\t\t\t\t\treturn false;\n".getBytes());


								os.write("\t\t\t}\n\n".getBytes());
		               	}
		               	else
		               	{
							os.write("\t\t\t".getBytes());
		               		os.write("if ( ".getBytes());
		               		os.write(vType.getTypeName().getBytes());
		               		os.write(" != ((".getBytes());
		               		os.write(fullClassName.getBytes());
		               		os.write(") o).get".getBytes());
							os.write((vName.substring(0,1).toUpperCase() + vName.substring(1)).getBytes());
		               		os.write("())\n".getBytes());
							os.write("\t\t\t\treturn false;\n\n".getBytes());

		               	}
						break;

            }//end switch

}




/**
 * Method printEquals.
 * @param ostream
 * @throws IOException
 */
  private void printEquals(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic boolean equals(Object o)\n".getBytes());
    ostream.write("\t{\n".getBytes());


	ostream.write("\t\tif( o instanceof ".getBytes());
	ostream.write(fullClassName.getBytes());
	ostream.write(")\n".getBytes());
	ostream.write("\t\t{\n".getBytes());

    Iterator iter = al.iterator();
    while(iter.hasNext())
    {
            DeclarationType dt = (DeclarationType) iter.next();

			equalWrite(dt.getDataType(), (HaviType) dt.iterator().next(), ostream, false);
    }


   ostream.write("\t\t\treturn true;\n".getBytes());
	ostream.write("\t\t}\n".getBytes());
    ostream.write("\n\t\treturn false;\n".getBytes());
	ostream.write("\t}\n\n".getBytes());

  }










/**
 * Method printHashCode.
 * @param ostream
 * @throws IOException
 */
  private void printHashCode(OutputStream ostream) throws IOException
  {
/*
    ostream.write("\tpublic int hashCode()\n".getBytes());
    ostream.write("\t{\n".getBytes());
    ostream.write("\t\treturn 0;\n".getBytes());
    ostream.write("\t}\n\n".getBytes());
*/
		ostream.write("\tpublic int hashCode()\n".getBytes());
		ostream.write("\t{\n".getBytes());

		ostream.write("\t\tint hash = 0;\n".getBytes());

  	    Iterator iter = al.iterator();

		while (iter.hasNext())
		{
			DeclarationType dt = (DeclarationType) iter.next();

			HaviType type = dt.getDataType();

			ArrayList list = dt.getChildList();

			hashCodeWrite(type, list, ostream, false);

		}

		ostream.write("\t\treturn hash;\n\n".getBytes());
		ostream.write("\t}\n\n".getBytes());


  }


/**
 * Method printClone.
 * @param ostream
 * @throws IOException
 */
  private void printClone(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic Object clone()\n".getBytes());
    ostream.write("\t{\n".getBytes());
    ostream.write("\t\treturn this;\n".getBytes());
    ostream.write("\t}\n\n".getBytes());
  }


	/**
	 * Method printToString.
	 * @param os
	 * @throws IOException
	 */
	private void printToString(OutputStream os) throws IOException
	{

		 os.write("\tpublic String toString()\n".getBytes());
		 os.write("\t{\n".getBytes());


  		 Iterator iter = al.iterator();

		 os.write("\t\treturn ".getBytes());

		 if(iter.hasNext())
		 {
				 while (iter.hasNext())
				 {
					DeclarationType dt = (DeclarationType) iter.next();

					toStringWrite(dt.getDataType(), dt.getChildList(), os, false);

					if(iter.hasNext())
						os.write("+ \" \" + ".getBytes());
				 }
		 }
		 else
		 	os.write("\"\"".getBytes());

		 os.write(";\n".getBytes());
		os.write("\t}\n\n\n".getBytes());

	}



	/**
	 * Method toStringWrite.
	 * @param datatype
	 * @param vList
	 * @param os
	 * @param array
	 * @throws IOException
	 */
	private void toStringWrite(HaviType datatype, ArrayList vList, OutputStream os, boolean array) throws IOException
	{
		HaviType type = datatype;

		Iterator vIter = vList.iterator();

		switch(type.getConstantTypeDef())
		{

			case SEQUENCE:
				type = ((SequenceType) datatype).getDataType();
				toStringWrite(type, vList, os, true);
				break;

			case STRUCT:
			case UNION:
			case UNIONSTRUCT:
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\"".getBytes());
					os.write(vType.getTypeName().getBytes());
					os.write("=\"+".getBytes());
					os.write(vType.getTypeName().getBytes());
					os.write(".toString()".getBytes());
				}
				break;


			default:
				while(vIter.hasNext())
				{
					HaviType vType = (HaviType)vIter.next();

					os.write("\"".getBytes());
					os.write(vType.getTypeName().getBytes());
					os.write("=\"+".getBytes());
					os.write(vType.getTypeName().getBytes());

				}
				break;

		} // end case

	}



  private String makeFileName(String childName)
  {
	StringTokenizer st = new StringTokenizer(childName, "_");

	String newChildName="";
	while(st.hasMoreTokens())
	{
		String tempName = st.nextToken();
		newChildName += tempName.substring(0,1).toUpperCase()+tempName.substring(1).toLowerCase();

	}

	return newChildName;

  }


	/**
	 * call by printhashcode()
	 *
	 * Method hashCodeWrite.
	 * @param datatype
	 * @param list
	 * @param os
	 * @param arrayFlag
	 * @throws IOException
	 */
	private void hashCodeWrite(HaviType datatype, ArrayList list, OutputStream os, boolean arrayFlag) throws IOException {

		Iterator vIter = list.iterator();

		switch (datatype.getConstantTypeDef())
		{

			case SEQUENCE :
				HaviType type = ((SequenceType) datatype).getDataType();
				hashCodeWrite(type, list, os, true);
				break;

			case LITERAL :
				HaviType h =(HaviType) CodeGenerator.dataTypeList.get(datatype.getTypeName());
				if (h == null)
				{
						while (vIter.hasNext())
						{
							HaviType vType = (HaviType) vIter.next();

							if (arrayFlag == true || vType instanceof ArrayType)
							{

								os.write("\t\t{\n".getBytes());
								os.write("\t\t\tint size = ".getBytes());
								os.write(vType.getTypeName().getBytes());
								os.write(".length;\n".getBytes());

								os.write(
									"\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
								os.write("\t\t\t\thash += ".getBytes());
								os.write(vType.getTypeName().getBytes());
								os.write("[i].hashCode();\n\n".getBytes());
								os.write("\t\t}\n\n".getBytes());
							}
							else
							{
								os.write("\t\thash += ".getBytes());
								os.write(vType.getTypeName().getBytes());
								os.write(".hashCode();\n".getBytes());
							}

						}
				}
				else
					hashCodeWrite(h, list, os, arrayFlag);
				break;

			case ENUM :
				while (vIter.hasNext()) {
					HaviType vType = (HaviType) vIter.next();
					if (arrayFlag == true || vType instanceof ArrayType) {

						os.write("\t\t{\n".getBytes());
						os.write("\t\t\tint size = ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write(".length;\n".getBytes());

						os.write(
							"\t\t\tfor(int i= 0; i < size; i++)\n".getBytes());
						os.write("\t\t\t\thash += ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write("[i];\n\n".getBytes());
						os.write("\t\t}\n\n".getBytes());

					} else {
						os.write("\t\thash += ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write(";\n".getBytes());
					}

				}
				break;

			case STRUCT :
			case UNION :
			case UNIONSTRUCT :
				while (vIter.hasNext())
				{
					HaviType vType = (HaviType) vIter.next();

					if (arrayFlag == true || vType instanceof ArrayType)
					{

						os.write("\t\t{\n".getBytes());
						os.write("\t\t\tint size = ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write(".length;\n".getBytes());

						os.write(
							"\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
						os.write("\t\t\t\thash += ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write("[i].hashCode();\n\n".getBytes());
						os.write("\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\thash += ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write(".hashCode();\n".getBytes());
					}

				}
				break;

			case BOOLEAN :
				while (vIter.hasNext())
				{
					HaviType vType = (HaviType) vIter.next();
					if (arrayFlag == true || vType instanceof ArrayType)
					{
						os.write("\t\t{\n".getBytes());
						os.write("\t\t\tint size = ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write(".length;\n".getBytes());

						os.write(
							"\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
						os.write("\t\t\t\thash += (".getBytes());
						vType.output(os);
						os.write("[i] ? 1 : 0);\n\n".getBytes());
						os.write("\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\thash += (".getBytes());
						vType.output(os);
						os.write(" ? 1 : 0);\n".getBytes());
					}
				}
				break;

			case STRING :
			case WSTRING :
				while (vIter.hasNext())
				{
					HaviType vType = (HaviType) vIter.next();
					if (arrayFlag == true || vType instanceof ArrayType)
					{
						os.write("\t\t{\n".getBytes());
						os.write("\t\t\tint size = ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write(".length;\n".getBytes());

						os.write(
							"\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
						os.write("\t\t\t\thash += ".getBytes());
						vType.output(os);
						os.write("[i].hashCode();\n\n".getBytes());
						os.write("\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\thash += ".getBytes());
						vType.output(os);
						os.write(".hashCode();\n".getBytes());
					}

				}
				break;

			default : //base data type
				while (vIter.hasNext())
				{
					HaviType vType = (HaviType) vIter.next();
					if (arrayFlag == true || vType instanceof ArrayType)
					{
						os.write("\t\t{\n".getBytes());
						os.write("\t\t\tint size = ".getBytes());
						os.write(vType.getTypeName().getBytes());
						os.write(".length;\n".getBytes());

						os.write(
							"\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
						os.write("\t\t\t\thash += ".getBytes());
						vType.output(os);
						os.write("[i];\n\n".getBytes());
						os.write("\t\t}\n\n".getBytes());
					}
					else
					{
						os.write("\t\thash += ".getBytes());
						vType.output(os);
						os.write(";\n".getBytes());
					}
				}
				break;

		} //end switch

	}



}

