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
 * $Id: UnionClassCreation.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.SwitchType;
import com.streetfiresound.codegenerator.types.UnionType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class UnionClassCreation implements ConstTypeDefinition
{

  UnionType ht;

  String system;
  String type;
  String constant;
  String packageName;
  String exception;

  /**
   * Constructor for UnionClassCreation.
   */
  public UnionClassCreation(UnionType ht)
  {
    super();

    this.ht = ht;

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
        printGetDiscriminator(fos);
        printCreate1(fos);
        printCreate2(fos);



		printClone(fos);
		printHashCode(fos);
		printEqual(fos);
		printMarshall(fos);
		printUnmarshall(fos);

        printCloseClass(fos);
        fos.close();

        makeChild();




  }



/**
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
 * Method printInterface.
 * @param ostream
 * @throws IOException
 */
  private void printInterface(OutputStream ostream) throws IOException
  {

        if(ht.getDataType().getConstantTypeDef() == ENUM)
        {
                ostream.write(" implements Const".getBytes());
                ostream.write(ht.getDataType().getTypeName().getBytes());
        }


  }


/**
 * Method printOpenClass.
 * @param ostream
 * @throws IOException
 */
  private void printOpenClass(OutputStream ostream) throws IOException
  {
    ostream.write("\n{\n\n".getBytes());
  }


/**
 * Method printCloseClass.
 * @param ostream
 * @throws IOException
 */
  private void printCloseClass(OutputStream ostream) throws IOException
  {
    ostream.write("}\n\n".getBytes());
  }



/**
 * Method printImport.
 * @param ostream
 * @throws IOException
 */
  private void printImport(OutputStream ostream) throws IOException
  {
	String packPath = packageName + "." + system;
	String constantPath=packageName+"."+constant;
	String typePath=packageName+"."+type;
	String systemPath=   packageName+"."+system;
	String exceptionPath = packageName+"." + exception;

	Set set = CodeGenerator.projectList.entrySet();

    ostream.write("import java.io.*;\n".getBytes());
    ostream.write("import java.util.*;\n\n".getBytes());


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
 * Method printPackage.
 * @param ostream
 * @throws IOException
 */
  private void printPackage(OutputStream ostream) throws IOException
  {
	String pack = packageName + "." + type;
    ostream.write("package ".getBytes());

    ostream.write(pack.getBytes());
    ostream.write(";\n\n".getBytes());


  }

/**
 * Method printGetDiscriminator.
 * @param ostream
 * @throws IOException
 */
  private void printGetDiscriminator(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic int getDiscriminator()\n".getBytes());
    ostream.write("\t{\n".getBytes());
    ostream.write("\t\tthrow new IllegalStateException(\"bad state\");\n".getBytes());
    ostream.write("\t}\n\n\n".getBytes());
  }




/**
 * Method printCreate1.
 * @param ostream
 * @throws IOException
 */
  private void printCreate1(OutputStream ostream) throws IOException
  {
    Iterator iter = ht.iterator();


    ostream.write("\tstatic public ".getBytes());
    ostream.write((""+ht.getTypeName()).getBytes());
    ostream.write(" create(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException\n".getBytes());
    ostream.write("\t{\n".getBytes());

    ostream.write("\t\ttry\n".getBytes());
    ostream.write("\t\t{\n".getBytes());

        ostream.write("\t\t\tint type = hbais.readInt();\n\n".getBytes());
        ostream.write("\t\t\tswitch (type)\n".getBytes());
        ostream.write("\t\t\t{\n".getBytes());


        while(iter.hasNext())
        {

              SwitchType st = (SwitchType )iter.next();
              String label = st.getLabel();
    		  String childUnionName = makeUnionChildName(label);

              ostream.write("\t\t\t\t case ".getBytes());
              ostream.write(label.getBytes());
              ostream.write(" :\treturn new ".getBytes());

//              ostream.write((label.substring(0,1) + label.substring(1).toLowerCase()+ht.getTypeName()).getBytes());
              ostream.write((childUnionName+ht.getTypeName()).getBytes());

              ostream.write("(hbais);\n\n".getBytes());

              if(st.getLabel().equals("default"))
              {
                break;
              }
        }
        ostream.write("\t\t\t\tdefault: return null;\n\n".getBytes());
        ostream.write("\t\t\t}\n\n".getBytes());

    ostream.write("\t\t}\n".getBytes());
    ostream.write("\t\tcatch(IOException e)\n".getBytes());
    ostream.write("\t\t{\n".getBytes());
    ostream.write("\t\t\tthrow new HaviUnmarshallingException(e.getMessage());\n".getBytes());
    ostream.write("\t\t}\n".getBytes());



    ostream.write("\t}\n\n\n".getBytes());

  }










/**
 * Method makeChild.
 * @throws IOException
 */
  private void makeChild() throws IOException
  {
    Iterator iter = ht.iterator();
    int count =0;
    while(iter.hasNext())
    {

          SwitchType st = (SwitchType )iter.next();
          String label = st.getLabel();
          ArrayList al = st.getChildList();


//         new UnionChildClassCreation(label.substring(0,1) + label.substring(1).toLowerCase(), ht.getTypeName(), al, count++);

         new UnionChildClassCreation(label, ht.getTypeName(), al, count++);

    }


  }




  private void printCreate2(OutputStream ostream) throws IOException
  {
    Iterator iter = ht.iterator();


    ostream.write("\tstatic public ".getBytes());
    ostream.write((""+ht.getTypeName()).getBytes());
    ostream.write(" create(byte[] byteStream)\n".getBytes());
    ostream.write("\t{\n".getBytes());

    ostream.write("\t\ttry\n".getBytes());
    ostream.write("\t\t{\n".getBytes());


      ostream.write("\t\t\tHaviByteArrayInputStream  hbais = new HaviByteArrayInputStream(byteStream);\n\n".getBytes());

        ostream.write("\t\t\tint type = hbais.readInt();\n\n".getBytes());
        ostream.write("\t\t\tswitch (type)\n".getBytes());
        ostream.write("\t\t\t{\n".getBytes());


        while(iter.hasNext())
        {

              SwitchType st = (SwitchType )iter.next();
              String label = st.getLabel();
    		  String childUnionName = makeUnionChildName(label);


              ostream.write("\t\t\t\t case ".getBytes());
              ostream.write(label.getBytes());
              ostream.write(" :\treturn new ".getBytes());

//              ostream.write((label.substring(0,1) + label.substring(1).toLowerCase()+ht.getTypeName()).getBytes());
              ostream.write((childUnionName+ht.getTypeName()).getBytes());

              ostream.write("(hbais);\n\n".getBytes());

              if(st.getLabel().equals("default"))
              {
                break;
              }
        }
        ostream.write("\t\t\t\tdefault: return null;\n\n".getBytes());
        ostream.write("\t\t\t}\n\n".getBytes());

    ostream.write("\t\t}\n".getBytes());
    ostream.write("\t\tcatch(Exception e)\n".getBytes());
    ostream.write("\t\t{\n".getBytes());
    ostream.write("\t\t\treturn null;\n".getBytes());
    ostream.write("\t\t}\n".getBytes());



    ostream.write("\t}\n\n\n".getBytes());

  }




  private String makeUnionChildName(String childName)
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





	private void printClone(OutputStream os) throws IOException
	{
		os.write("\tprotected Object clone() throws CloneNotSupportedException\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\tthrow new CloneNotSupportedException(\"bad state\");\n".getBytes());
		os.write("\t}\n\n\n".getBytes());
	}


	private void printHashCode(OutputStream os) throws IOException
	{
		os.write("\tpublic int hashCode()\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\tthrow new IllegalStateException(\"bad state\");\n".getBytes());
		os.write("\t}\n\n\n".getBytes());

	}


	private void printEqual(OutputStream os) throws IOException
	{
		os.write("\tpublic boolean equals(Object arg0)\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\tthrow new IllegalStateException(\"bad state\");\n".getBytes());
		os.write("\t}\n\n\n".getBytes());

	}

	private void printMarshall(OutputStream os) throws IOException
	{
		os.write("\tpublic void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\tthrow new HaviMarshallingException();\n".getBytes());
		os.write("\t}\n\n\n".getBytes());

	}

	private void printUnmarshall(OutputStream os) throws IOException
	{
		os.write("\tpublic void unmarshal(HaviByteArrayInputStream hbais)throws HaviUnmarshallingException\n".getBytes());
		os.write("\t{\n".getBytes());
		os.write("\t\tthrow new HaviUnmarshallingException();\n".getBytes());
		os.write("\t}\n\n\n".getBytes());

	}










}
