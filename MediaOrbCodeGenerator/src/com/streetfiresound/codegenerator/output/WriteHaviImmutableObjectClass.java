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
 * $Id: WriteHaviImmutableObjectClass.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ArrayType;
import com.streetfiresound.codegenerator.types.BaseDataType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.ConstructType;
import com.streetfiresound.codegenerator.types.DeclarationType;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.SequenceType;

/**
 * @author george
 * 
 * This class is only used by 5 special class: GUID, HUID, SEID, TargetId, VendorId. In the printMarhal() It seems it is not following a pattern that write in the size of array. But this is an exception case for this class
 *  
 */
public class WriteHaviImmutableObjectClass implements ConstTypeDefinition
{

  ConstructType ht;
  HashSet localExceptionList;

  /**
   * Constructor for WriteHaviImmutableObjectClass.
   */
  public WriteHaviImmutableObjectClass(ConstructType ht, OutputStream os) throws IOException
  {
    super();

    this.ht = ht;
    localExceptionList = new HashSet();

    printMarshal(os);
    printEquals(os);
    printHashCode(os);
    printClone(os);

  }

  /**
   * Method printMarshalWrite.
   * 
   * @param loopCount
   * @param dt
   * @param list
   * @param os
   * @param arrayFlag
   * @throws IOException
   */
  private void printMarshalWrite(int loopCount, HaviType dt, ArrayList list, OutputStream os, boolean arrayFlag) throws IOException
  {

    HaviType datatype = dt;
    Iterator varIter = list.iterator();

    switch (datatype.getConstantTypeDef())
    {

      case SEQUENCE:
        datatype = ((SequenceType)datatype).getDataType();
        printMarshalWrite(loopCount, datatype, list, os, true);
        break;

      case LITERAL:
        HaviType h = (HaviType)CodeGenerator.dataTypeList.get(datatype.getTypeName());
        if (h == null)
        {
          localExceptionList.add("HaviMarshallingException");

          while (varIter.hasNext())
          {
            HaviType vType = (HaviType)varIter.next();

            if (arrayFlag == true || vType instanceof ArrayType)
            {
              localExceptionList.add("IOException");
              os.write("\t\t\tho.write(".getBytes());
              os.write(vType.getTypeName().getBytes());
              os.write(")\n\n".getBytes());
            }
            else
            {
              os.write("\t\t\t".getBytes());
              os.write(vType.getTypeName().getBytes());
              os.write(".marshal(ho);\n".getBytes());
            }

          }

        }
        else
          printMarshalWrite(loopCount, h, list, os, arrayFlag);

        break;

      case ENUM:
        localExceptionList.add("IOException");
        while (varIter.hasNext())
        {
          HaviType thisHt = (HaviType)varIter.next();
          os.write("\t\t\tho.writeInt(".getBytes());
          os.write(thisHt.getTypeName().getBytes());
          os.write(");\n".getBytes());
        }
        break;

      case STRUCT:
      case UNION:
      case UNIONSTRUCT:
        localExceptionList.add("HaviMarshallingException");
        while (varIter.hasNext())
        {
          HaviType vType = (HaviType)varIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
          {
            localExceptionList.add("IOException");
            os.write("\t\t\tho.write(".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(")\n\n".getBytes());

          }
          else
          {
            os.write("\t\t\t".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(".marshal(ho);\n".getBytes());
          }

        }
        break;

      default: //base data type
        localExceptionList.add("IOException");
        while (varIter.hasNext())
        {
          HaviType vType = (HaviType)varIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
          {
            os.write("\t\t\tho.write(".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(");\n\n".getBytes());

          }

          else
          {
            os.write("\t\t\t".getBytes());
            os.write("ho.write".getBytes());
            os.write(((BaseDataType)datatype).getMarshalString().getBytes());
            os.write("(".getBytes());
            os.write(vType.getTypeName().getBytes());
            //vType.output(os) ;
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
   * 
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
    Iterator iter = ht.iterator();

    int loopCount = 0;
    while (iter.hasNext())
    {
      //get the declarationtype its datatype contains only basedatatype, sequencetype and literaltype
      DeclarationType dt = (DeclarationType)iter.next();
      loopCount++;

      HaviType dtype = dt.getDataType();
      ArrayList al = dt.getChildList();
      printMarshalWrite(loopCount, dtype, al, ostream, false);

    }
    ostream.write("\t\t}\n".getBytes());

    Iterator exceptionIter = localExceptionList.iterator();
    while (exceptionIter.hasNext())
    {
      String exceptionName = (String)exceptionIter.next();

      ostream.write("\t\tcatch(".getBytes());
      ostream.write(exceptionName.getBytes());
      ostream.write(" e)\n".getBytes());
      ostream.write("\t\t{\n".getBytes());
      ostream.write("\t\t\tthrow new HaviMarshallingException(e.getMessage());\n".getBytes());
      ostream.write("\t\t}\n\n".getBytes());
    }

    ostream.write("\t}\n\n".getBytes());
    localExceptionList.clear();
  }

  /**
   * Method equalWrite.
   * 
   * @param datatype
   * @param list
   * @param os
   * @param arrayFlag
   * @throws IOException
   */
  private void equalWrite(HaviType datatype, ArrayList list, OutputStream os, boolean arrayFlag) throws IOException
  {
    Iterator vIter = list.iterator();

    switch (datatype.getConstantTypeDef())
    {
      case LITERAL:
        HaviType h = (HaviType)CodeGenerator.dataTypeList.get(datatype.getTypeName());
        if (h == null)
        {
          while (vIter.hasNext())
          {
            HaviType type2 = (HaviType)vIter.next();
            String vName = type2.getTypeName().substring(0, 1).toUpperCase() + type2.getTypeName().substring(1);

            if (arrayFlag == true || type2 instanceof ArrayType)
            {
              os.write("\t\t\t{\n".getBytes());
              os.write("\t\t\t\t".getBytes());
              os.write(datatype.getTypeName().getBytes());
              os.write("[] otherType = other.get".getBytes());
              os.write(vName.getBytes());
              os.write("();\n".getBytes());

              os.write("\t\t\t\tint size = ".getBytes());
              os.write(type2.getTypeName().getBytes());
              os.write(".length;\n".getBytes());

              os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
              os.write("\t\t\t\t\tif(".getBytes());
              os.write(type2.getTypeName().getBytes());
              os.write("[i].equals(otherType[i]) ==  false)\n".getBytes());
              os.write("\t\t\t\t\t\treturn false;\n\n".getBytes());
              os.write("\t\t\t}\n\n".getBytes());

            }
            else
            {
              os.write("\t\t\tif( this.".getBytes());
              os.write(type2.getTypeName().getBytes());
              os.write(".equals(other.get".getBytes());
              os.write(vName.getBytes());
              os.write("()) == false)\n".getBytes());
              os.write("\t\t\t\treturn false;\n\n".getBytes());

            }
          }
        }
        else
          equalWrite(h, list, os, arrayFlag);

        break;

      case SEQUENCE:
        HaviType type = ((SequenceType)datatype).getDataType();
        equalWrite(type, list, os, true);
        break;

      case ENUM:
        while (vIter.hasNext())
        {
          HaviType type2 = (HaviType)vIter.next();
          String vName = type2.getTypeName().substring(0, 1).toUpperCase() + type2.getTypeName().substring(1);

          if (arrayFlag == true || type2 instanceof ArrayType)
          {
            os.write("\t\t\t{\n".getBytes());
            os.write("\t\t\t\tint[] otherInt = other.get".getBytes());
            os.write(vName.getBytes());
            os.write("();\n".getBytes());

            os.write("\t\t\t\tint size = ".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
            os.write("\t\t\t\t\tif(".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write("[i] != otherInt[i])\n".getBytes());
            os.write("\t\t\t\t\t\treturn false;\n\n".getBytes());

            os.write("\t\t\t}\n\n".getBytes());

          }
          else
          {

            os.write("\t\t\tif( this.get".getBytes());
            os.write(vName.getBytes());
            os.write("() != other.get".getBytes());
            os.write(vName.getBytes());
            os.write("())\n".getBytes());
            os.write("\t\t\t\treturn false;\n".getBytes());

          }

        }
        break;

      case STRUCT:
      case UNION:
      case UNIONSTRUCT:
        while (vIter.hasNext())
        {
          HaviType type2 = (HaviType)vIter.next();
          String vName = type2.getTypeName().substring(0, 1).toUpperCase() + type2.getTypeName().substring(1);

          if (arrayFlag == true || type2 instanceof ArrayType)
          {

            os.write("\t\t\t{\n".getBytes());
            os.write("\t\t\t\t".getBytes());
            os.write(datatype.getTypeName().getBytes());
            os.write("[] otherType = other.get".getBytes());
            os.write(vName.getBytes());
            os.write("();\n".getBytes());

            os.write("\t\t\t\tint size = ".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
            os.write("\t\t\t\t\tif(".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write("[i].equals(otherType[i]) ==  false)\n".getBytes());
            os.write("\t\t\t\t\t\treturn false;\n\n".getBytes());
            os.write("\t\t\t}\n\n".getBytes());

          }
          else
          {

            os.write("\t\t\tif( this.".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write(".equals(other.get".getBytes());
            os.write(vName.getBytes());
            os.write("()) == false)\n".getBytes());
            os.write("\t\t\t\treturn false;\n\n".getBytes());

          }

        }
        break;

      case STRING:
      case WSTRING:
        while (vIter.hasNext())
        {
          HaviType type2 = (HaviType)vIter.next();
          String vName = type2.getTypeName().substring(0, 1).toUpperCase() + type2.getTypeName().substring(1);

          if (arrayFlag == true || type2 instanceof ArrayType)
          {
            os.write("\t\t\t{\n".getBytes());

            os.write("\t\t\t\t".getBytes());
            datatype.output(os);
            os.write("[] otherType = other.get".getBytes());
            os.write(vName.getBytes());
            os.write("();\n".getBytes());

            os.write("\t\t\t\tint size = ".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
            os.write("\t\t\t\t\tif(".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write("[i].equals(otherType[i]))\n".getBytes());
            os.write("\t\t\t\t\t\treturn false;\n\n".getBytes());

            os.write("\t\t\t}\n\n".getBytes());

          }
          else
          {
            os.write("\t\t\tif(this.get".getBytes());
            os.write(vName.getBytes());
            os.write("().equals(other.get".getBytes());
            os.write(vName.getBytes());
            os.write("()) == false)\n".getBytes());
            os.write("\t\t\t\treturn false;\n".getBytes());
          }
        }
        break;

      default: //base data type

        while (vIter.hasNext())
        {
          HaviType type2 = (HaviType)vIter.next();
          String vName = type2.getTypeName().substring(0, 1).toUpperCase() + type2.getTypeName().substring(1);

          if (arrayFlag == true || type2 instanceof ArrayType)
          {
            os.write("\t\t\t{\n".getBytes());

            os.write("\t\t\t\t".getBytes());
            datatype.output(os);
            os.write("[] otherType = other.get".getBytes());
            os.write(vName.getBytes());
            os.write("();\n".getBytes());

            os.write("\t\t\t\tint size = ".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
            os.write("\t\t\t\t\tif(".getBytes());
            os.write(type2.getTypeName().getBytes());
            os.write("[i] != otherType[i])\n".getBytes());
            os.write("\t\t\t\t\t\treturn false;\n\n".getBytes());

            os.write("\t\t\t}\n\n".getBytes());

          }
          else
          {
            os.write("\t\t\tif( this.get".getBytes());
            os.write(vName.getBytes());
            os.write("() != other.get".getBytes());
            os.write(vName.getBytes());
            os.write("())\n".getBytes());
            os.write("\t\t\t\treturn false;\n".getBytes());
          }

        } //end while
        break;

    }//end switch

  }

  /**
   * Method printEquals.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printEquals(OutputStream ostream) throws IOException
  {

    ostream.write("\tpublic boolean equals(Object o)\n".getBytes());
    ostream.write("\t{\n".getBytes());

    ostream.write("\t\tif (o instanceof ".getBytes());
    ostream.write(ht.getTypeName().getBytes());
    ostream.write(")\n".getBytes());
    ostream.write("\t\t{\n".getBytes());

    ostream.write("\t\t\t".getBytes());
    ostream.write(ht.getTypeName().getBytes());
    ostream.write(" other = (".getBytes());
    ostream.write(ht.getTypeName().getBytes());
    ostream.write(")o;\n".getBytes());

    Iterator iter = ht.iterator();

    while (iter.hasNext())
    {
      DeclarationType type = (DeclarationType)iter.next();
      equalWrite(type.getDataType(), type.getChildList(), ostream, false);
    }

    ostream.write("\t\t\treturn true;\n\n".getBytes());

    ostream.write("\t\t}\n\n".getBytes());
    ostream.write("\t\treturn false;\n".getBytes());
    ostream.write("\t}\n\n".getBytes());
  }

  /**
   * call by printhashcode() only
   * 
   * Method hashCodeWrite.
   * 
   * @param datatype
   * @param list
   * @param os
   * @param arrayFlag
   * @throws IOException
   */
  private void hashCodeWrite(HaviType datatype, ArrayList list, OutputStream os, boolean arrayFlag) throws IOException
  {

    Iterator vIter = list.iterator();

    switch (datatype.getConstantTypeDef())
    {

      case SEQUENCE:
        HaviType type = ((SequenceType)datatype).getDataType();
        hashCodeWrite(type, list, os, true);
        break;

      case LITERAL:
        HaviType h = (HaviType)CodeGenerator.dataTypeList.get(datatype.getTypeName());
        if (h == null)
        {
          while (vIter.hasNext())
          {
            HaviType vType = (HaviType)vIter.next();

            if (arrayFlag == true || vType instanceof ArrayType)
            {

              os.write("\t\t{\n".getBytes());
              os.write("\t\t\tint size = ".getBytes());
              os.write(vType.getTypeName().getBytes());
              os.write(".length;\n".getBytes());

              os.write("\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
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

      case ENUM:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();
          if (arrayFlag == true || vType instanceof ArrayType)
          {

            os.write("\t\t{\n".getBytes());
            os.write("\t\t\tint size = ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\tfor(int i= 0; i < size; i++)\n".getBytes());
            os.write("\t\t\t\thash += ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write("[i];\n\n".getBytes());
            os.write("\t\t}\n\n".getBytes());

          }
          else
          {
            os.write("\t\thash += ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(";\n".getBytes());
          }

        }
        break;

      case STRUCT:
      case UNION:
      case UNIONSTRUCT:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
          {

            os.write("\t\t{\n".getBytes());
            os.write("\t\t\tint size = ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
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

      case BOOLEAN:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();
          if (arrayFlag == true || vType instanceof ArrayType)
          {
            os.write("\t\t{\n".getBytes());
            os.write("\t\t\tint size = ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
            os.write("\t\t\t\thash += (".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write("[i] ? 1 : 0);\n\n".getBytes());
            os.write("\t\t}\n\n".getBytes());
          }
          else
          {
            os.write("\t\thash += (".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(" ? 1 : 0);\n".getBytes());
          }
        }
        break;

      case STRING:
      case WSTRING:
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();
          if (arrayFlag == true || vType instanceof ArrayType)
          {
            os.write("\t\t{\n".getBytes());
            os.write("\t\t\tint size = ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
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

      default: //base data type
        while (vIter.hasNext())
        {
          HaviType vType = (HaviType)vIter.next();
          if (arrayFlag == true || vType instanceof ArrayType)
          {
            os.write("\t\t{\n".getBytes());
            os.write("\t\t\tint size = ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
            os.write("\t\t\t\thash += ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write("[i];\n\n".getBytes());
            os.write("\t\t}\n\n".getBytes());
          }
          else
          {
            os.write("\t\thash += ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(";\n".getBytes());
          }

        }
        break;

    }//end switch

  }

  /**
   * Method printHashCode.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printHashCode(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic int hashCode()\n".getBytes());
    ostream.write("\t{\n".getBytes());

    ostream.write("\t\tint hash = 0;\n".getBytes());
    Iterator iter = ht.iterator();

    while (iter.hasNext())
    {
      DeclarationType dt = (DeclarationType)iter.next();

      HaviType type = dt.getDataType();
      ArrayList list = dt.getChildList();

      hashCodeWrite(type, list, ostream, false);
    }

    ostream.write("\t\treturn hash;\n\n".getBytes());
    ostream.write("\t}\n\n".getBytes());

  }

  /**
   * Method printClone.
   * 
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

}