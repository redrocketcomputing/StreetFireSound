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
 * $Id: WriteStructTargetId.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
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
 * To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates. To enable and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */
public class WriteStructTargetId implements ConstTypeDefinition
{

  StructType ht;
  String system;
  String constant;
  String packageName;
  String type;
  String exception;

  /**
   * Constructor for WriteStructTargetId.
   */
  public WriteStructTargetId(StructType ct) throws IOException
  {
    super();
    this.ht = ct;

    HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
    constant = (String)configInfo.get("CONSTANT");
    system = (String)configInfo.get("SYSTEM");
    type = (String)configInfo.get("TYPE");
    packageName = (String)configInfo.get("PACKAGE");
    exception = (String)configInfo.get("EXCEPTION");
    String rootPath = (String)configInfo.get("ROOTPATH");

    String location = rootPath + "/" + packageName.replace('.', '/') + "/" + type.replace('.', '/');

    FileOutputStream fos = new GplHeaderFileOutputStream(location + "/" + ct.getTypeName() + ".java");
    outputToFile(fos);
  }

  /**
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
  public void outputToFile(OutputStream fos) throws java.io.IOException
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

  private void privateWrite(HaviType datatype, ArrayList list, OutputStream os) throws IOException
  {
    Iterator vIter = list.iterator();

    switch (datatype.getConstantTypeDef())
    {

      case ENUM:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();
          os.write("\tprivate int ".getBytes());
          os.write(vType.getTypeName().getBytes());
          os.write(" = 0;\n".getBytes());
        }
        os.write("\n".getBytes());
        break;

      case STRUCT:
      case UNION:
      case UNIONSTRUCT:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();

          os.write("\tprivate ".getBytes());
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
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();

          os.write("\tprivate boolean ".getBytes());
          os.write(vType.getTypeName().getBytes());
          os.write(" = false;\n".getBytes());
        }
        os.write("\n".getBytes());
        break;

      case STRING:
      case WSTRING:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();

          os.write("\tprivate ".getBytes());
          os.write(datatype.getTypeName().getBytes());
          os.write(" ".getBytes());
          os.write(vType.getTypeName().getBytes());
          os.write(" = new String();\n".getBytes());
        }
        os.write("\n".getBytes());
        break;
        
      case SHORT: 
      case LONG:           
      case LONGLONG:
      case ULONG:
      case ULONGLONG:        
      case USHORT:           
      case OCTET:            
      case CHAR:             
      case WCHAR:            
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();
          os.write("\tprivate ".getBytes());
          datatype.output(os);
          os.write(" ".getBytes());
          os.write(vType.getTypeName().getBytes());
          os.write(" = (".getBytes());
          datatype.output(os);
          os.write(") 0;\n".getBytes());
        }
        os.write("\n".getBytes());
        break;

      default:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();
          os.write("\tprivate ".getBytes());
          datatype.output(os);
          os.write(" ".getBytes());
          os.write(vType.getTypeName().getBytes());
          os.write(" = new ".getBytes());
          datatype.output(os);
          os.write("();\n".getBytes());
        }
        os.write("\n".getBytes());
        break;

    }//end switch

  }

  /**
   * Print private variable
   * 
   * Method printPrivate.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printPrivate(OutputStream ostream) throws IOException
  {

    ostream.write("\tpublic static final int SIZE = 18;\n".getBytes());

    ostream.write("\tpublic final static ".getBytes());
    ostream.write(ht.getTypeName().getBytes());
    ostream.write(" ZERO = new ".getBytes());
    ostream.write(ht.getTypeName().getBytes());
    ostream.write("();\n\n".getBytes());

    //get the iterator
    Iterator iter = ht.iterator();

    //loop thru the content and the content must be a DeclarationType
    while (iter.hasNext())
    {
      DeclarationType dt = (DeclarationType)iter.next();
      HaviType dataType = dt.getDataType();
      Iterator vIter = dt.iterator();

      privateWrite(dt.getDataType(), dt.getChildList(), ostream);

    }

    ostream.write("\n\n".getBytes());
    ostream.write("\n\n".getBytes());

  }

  /**
   * print out the default constructor Method printConstructor1.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printConstructor1(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic ".getBytes());
    ostream.write(("" + ht.getTypeName()).getBytes());
    ostream.write("(){}\n\n\n".getBytes());
  }

  private void printDataType(HaviType dataType, OutputStream os) throws IOException
  {
    HaviType thisDataType = dataType;

    if (dataType instanceof SequenceType)
      thisDataType = ((SequenceType)dataType).getDataType();

    switch (thisDataType.getConstantTypeDef())
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
   * print out the constructor with parameter list. The parameter list contains variables that match the private variable.
   * 
   * Method printConstructor2.
   * 
   * @param os
   * @throws IOException
   */
  private void printConstructor2(OutputStream os) throws IOException
  {

    os.write("\tpublic ".getBytes());
    os.write(("" + ht.getTypeName()).getBytes());
    os.write("(".getBytes());

    //loop thru the content and the content must be a DeclarationType
    //this part create the parameter list in the function
    Iterator iter = ht.iterator();
    while (iter.hasNext())
    {
      //get the variables list from the DeclartionType
      DeclarationType dt = (DeclarationType)iter.next();
      Iterator vIter = dt.iterator();

      //loop thru the variable list
      while (vIter.hasNext())
      {

        HaviType variable = (HaviType)vIter.next();
        printDataType(dt.getDataType(), os);

        if (dt.getDataType() instanceof SequenceType || variable instanceof ArrayType)
          os.write("[]".getBytes());

        //write out a space
        os.write(" ".getBytes());
        variable.output(os);

        //if have more variable, print out ,
        //for example int a,b,c
        if (vIter.hasNext())
          os.write(", ".getBytes());
      }

      if (iter.hasNext())
        os.write(", ".getBytes());

    }

    os.write(" )\n\t{\n".getBytes());

    //loop thru the content again
    //this part create the content list in the function

    iter = ht.iterator();
    while (iter.hasNext())
    {
      DeclarationType dt = (DeclarationType)iter.next();

      //get variable list
      Iterator vIter = dt.iterator();

      //in DeclarationType, the data type only has 3 kinds of type
      //1 BaseDatatype, 2 SequenceType and 3 LiteralType
      // BaseDataType suchas short, long, string etc
      // LiteralType usually is either and object name or interface value.
      // Since different type has different print out, so we need to take care all of the type

      while (vIter.hasNext())
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

    if (dataType instanceof SequenceType)
      thisDataType = ((SequenceType)dataType).getDataType();

    switch (thisDataType.getConstantTypeDef())
    {
      case STRUCT:
      case UNION:
      case UNIONSTRUCT:

        if (dataType instanceof SequenceType)
        {

          os.write("\n\t\tint size = ".getBytes());

          //print out variable name
          vType.output(os);
          os.write(".length;\n\t\tthis.".getBytes());

          //print out variable name, actually, it is same as tempHt.output(os);
          os.write(vType.getTypeName().getBytes());
          ;
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

  /**
   * print out constructor with havibytearrayinputstream as parameter
   * 
   * Method printConstructor3.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printConstructor3(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic ".getBytes());
    ostream.write(("" + ht.getTypeName()).getBytes());
    ostream.write("(HaviByteArrayInputStream hi) throws HaviUnmarshallingException\n\t{\n\n".getBytes());

    ostream.write("\t\ttry\n".getBytes());
    ostream.write("\t\t{\n".getBytes());
    ostream.write("\t\t\ttype = hi.readInt();\n".getBytes());
    ostream.write("\t\t\tguid = new GUID(hi);\n".getBytes());
    ostream.write("\t\t\tn1 = hi.readInt();\n".getBytes());
    ostream.write("\t\t\tn2 = hi.readShort();\n".getBytes());
    ostream.write("\t\t}\n".getBytes());
    ostream.write("\t\tcatch(IOException e)\n".getBytes());
    ostream.write("\t\t{\n".getBytes());
    ostream.write("\t\t\tthrow new HaviUnmarshallingException(e.getMessage());\n".getBytes());
    ostream.write("\t\t}\n\n\n".getBytes());
    ostream.write("\t}\n\n".getBytes());
  }

  /**
   * print out class header
   * 
   * Method printClass.
   * 
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
   * print out interface loop thru the list, the list must contains only delcaration type check to see if declarationType's datatype name is contains insides the inteface list if found then print out the implements.
   * 
   * Method printInterface.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printInterface(OutputStream ostream) throws IOException
  {

    //get the list of declarationtype
    Iterator iter = ht.iterator();

    //To be used to determine print out implement or ,
    short flag = 0;

    while (iter.hasNext())
    {
      DeclarationType dt = (DeclarationType)iter.next();

      HaviType dataType = dt.getDataType();

      if (dataType.getConstantTypeDef() == ENUM)
      {
        //if flag equals 0 means it is the first time, so we need to print out implements
        //if flag equals 1 only print out ","
        if (flag == 0)
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
   * 
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
   * 
   * @param ostream
   * @throws IOException
   */
  private void printCloseClass(OutputStream ostream) throws IOException
  {
    ostream.write("}\n\n".getBytes());
  }

  /**
   * print out the import also print out every item from the modulelist except the same module name.
   * 
   * 
   * Method printImport.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printImport(OutputStream ostream) throws IOException
  {
    String constantPath = packageName + "." + constant;
    String typePath = packageName + "." + type;
    String systemPath = packageName + "." + system;
    String packPath = packageName + "." + system;
    String exceptionPath = packageName + "." + exception;

    ArrayList pathList = new ArrayList();
    pathList.add(packPath);

    Set set = CodeGenerator.projectList.entrySet();

    ostream.write("import java.io.*;\n".getBytes());
    ostream.write("import java.util.*;\n\n".getBytes());

    //GENERAL CONSTANT PATH
    if (!pathList.contains(CodeGenerator.generalConstantPath))
    {
      ostream.write("import ".getBytes());
      ostream.write(CodeGenerator.generalConstantPath.getBytes());
      ostream.write(".*;\n".getBytes());

      pathList.add(CodeGenerator.generalConstantPath);
    }

    //GENERAL TYPE PATH
    if (!pathList.contains(CodeGenerator.generalTypePath))
    {
      ostream.write("import ".getBytes());
      ostream.write(CodeGenerator.generalTypePath.getBytes());
      ostream.write(".*;\n".getBytes());

      pathList.add(CodeGenerator.generalTypePath);
    }

    //GENERAL EXCEPTION PATH
    if (!pathList.contains(CodeGenerator.generalExceptionPath))
    {
      ostream.write("import ".getBytes());
      ostream.write(CodeGenerator.generalExceptionPath.getBytes());
      ostream.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.generalExceptionPath);
    }

    //get the iterator from the modulelist
    Iterator iter = set.iterator();

    //loop the the list
    while (iter.hasNext())
    {

      //get the module name
      Map.Entry entry = (Map.Entry)iter.next();
      String output = (String)entry.getKey();
      HashMap map = (HashMap)entry.getValue();

      //if module name is same as currentPackage name the skip
      // if(output.equalsIgnoreCase(driver.currentPackage))// || output.equalsIgnoreCase(driver.COMPARETYPE))
      //continue;

      String rmiPath = ((String)map.get("PACKAGE")) + "." + ((String)map.get("SYSTEM"));
      String rmiCons = ((String)map.get("PACKAGE")) + "." + ((String)map.get("CONSTANT"));
      String rmiType = ((String)map.get("PACKAGE")) + "." + ((String)map.get("TYPE"));
      ArrayList importList = (ArrayList)map.get("IMPORT");
      if (importList != null)
      {
        Iterator importIter = importList.iterator();
        while (importIter.hasNext())
        {
          String path = (String)importIter.next();
          ostream.write("import ".getBytes());
          ostream.write(path.getBytes());
          ostream.write(";\n".getBytes());
        }
        ostream.write("\n".getBytes());
      }

      if (!pathList.contains(rmiPath))
      {
        ostream.write("import ".getBytes());
        ostream.write(rmiPath.getBytes());
        ostream.write(".*;\n".getBytes());
        pathList.add(rmiPath);
      }
      if (!pathList.contains(rmiCons))
      {
        ostream.write("import ".getBytes());
        ostream.write(rmiCons.getBytes());
        ostream.write(".*;\n".getBytes());
        pathList.add(rmiCons);
      }
      if (!pathList.contains(rmiType))
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
   * 
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

  private void printSetGet(OutputStream os) throws IOException
  {
    Iterator iter = ht.iterator();

    while (iter.hasNext())
    {

      DeclarationType dt = (DeclarationType)iter.next();

      HaviType dataType = dt.getDataType();
      Iterator varIter = dt.iterator();
      while (varIter.hasNext())
      {
        HaviType vType = (HaviType)varIter.next();
        String variable = vType.getTypeName(); //variable name

        //set
        os.write("\tpublic void set".getBytes());
        os.write((variable.substring(0, 1).toUpperCase() + variable.substring(1)).getBytes());
        os.write("(".getBytes());

        printDataType(dataType, os);

        if (dataType instanceof SequenceType || vType instanceof ArrayType)
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
        printDataType(dataType, os); //return type

        if (dataType instanceof SequenceType || vType instanceof ArrayType)
          os.write("[]".getBytes());

        os.write(" get".getBytes());
        os.write((variable.substring(0, 1).toUpperCase() + variable.substring(1)).getBytes());
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