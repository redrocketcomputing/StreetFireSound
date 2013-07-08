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
 * $Id: WriteHaviObjectClass.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
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
 */
public class WriteHaviObjectClass implements ConstTypeDefinition
{

  ConstructType ht;

  HashSet localExceptionList;

  /**
   * Constructor for WriteHaviObjectClass.
   */
  public WriteHaviObjectClass(ConstructType ht, OutputStream os) throws IOException
  {
    super();

    localExceptionList = new HashSet();
    this.ht = ht;

    printMarshal(os);
    printUnMarshal(os);
    printEquals(os);
    printHashCode(os);
    printClone(os);
    printToString(os);
  }

  /**
   * 
   * Call by printMarshal() - output to file
   * 
   * Method printMarshalWrite.
   * 
   * @param loopCount
   * @param type
   * @param list
   * @param os
   * @param arrayFlag
   * @throws IOException
   */
  private void printMarshalWrite(int loopCount, HaviType type, ArrayList list, OutputStream os, boolean arrayFlag) throws IOException
  {

    //data type
    HaviType datatype = type;

    //variable list
    Iterator varIter = list.iterator();

    switch (datatype.getConstantTypeDef())
    {
      case SEQUENCE:
        datatype = ((SequenceType)type).getDataType();
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
          HaviType vType = (HaviType)varIter.next();
          if (arrayFlag == true || vType instanceof ArrayType)
          {
            os.write("\n\t\t\t{\n".getBytes());
            os.write("\n\t\t\t\tint size = ".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(".length;\n".getBytes());

            os.write("\t\t\t\tho.writeInt(size);\n".getBytes());

            os.write("\t\t\t\tfor(int i=0; i < size; i++)\n\t\t\t\t\t".getBytes());
            os.write("ho.writeInt(".getBytes());
            vType.output(os);
            os.write("[i]);\n".getBytes());
            os.write("\n\t\t\t}\n\n\n".getBytes());
          }
          else
          {

            os.write("\t\t\tho.writeInt(".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(");\n".getBytes());
          }

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
            os.write(((BaseDataType)datatype).getMarshalString().getBytes());
            os.write("(".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write("[i]);\n".getBytes());
          }
          else
          {

            os.write("\t\t\t".getBytes());
            os.write("ho.write".getBytes());
            os.write(((BaseDataType)datatype).getMarshalString().getBytes());
            os.write("(".getBytes());
            vType.output(os);
            os.write(");\n".getBytes());
          }

        }
        break;

    } //end switch

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

      HaviType type = dt.getDataType();
      ArrayList list = dt.getChildList();

      printMarshalWrite(loopCount, type, list, ostream, false);

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
   * call by printUnMarshal() - output to file
   * 
   * Method printUnMarshalWrite.
   * 
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
    String loop = Integer.toString(loopCount);

    switch (datatype.getConstantTypeDef())
    {
      case ENUM:
        localExceptionList.add("IOException");
        while (varIter.hasNext())
        {
          HaviType vType = (HaviType)varIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
          {

            os.write("\n\t\t\t{\n".getBytes());
            os.write("\n\t\t\t\tint size = hi.readInt();\n".getBytes());

            os.write("\t\t\t\t".getBytes());
            vType.output(os);
            os.write(" = new int[size];\n".getBytes());

            os.write("\t\t\t\tfor(int i=0; i < size; i++)\n\t\t\t\t\t".getBytes());
            vType.output(os);
            os.write("[i] = hi.readInt();\n".getBytes());
            os.write("\t\t\t}\n\n".getBytes());

          }
          else
          {
            os.write("\t\t\t".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write("= hi.readInt();\n".getBytes());
          }

        }
        break;

      case SEQUENCE:
        datatype = ((SequenceType)type).getDataType();
        printUnMarshalWrite(loopCount, datatype, list, os, true);
        break;

      case LITERAL:
        HaviType newtype = (HaviType)CodeGenerator.dataTypeList.get(type.getTypeName());
        if (newtype == null)
        {
          localExceptionList.add("HaviUnmarshallingException");
          while (varIter.hasNext())
          {
            HaviType vType = (HaviType)varIter.next();

            if (arrayFlag == true || vType instanceof ArrayType)
            {

              localExceptionList.add("IOException");
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

        while (varIter.hasNext())
        {
          HaviType vType = (HaviType)varIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
          {
            localExceptionList.add("IOException");

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

        while (varIter.hasNext())
        {
          HaviType vType = (HaviType)varIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
          {
            localExceptionList.add("IOException");

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
            os.write(".create(hi);\n\n".getBytes());

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

        while (varIter.hasNext())
        {
          HaviType vType = (HaviType)varIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
          {
            localExceptionList.add("IOException");

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

      default: //base data type
        localExceptionList.add("IOException");

        while (varIter.hasNext())
        {
          HaviType vType = (HaviType)varIter.next();

          if (arrayFlag == true || vType instanceof ArrayType)
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
            os.write(((BaseDataType)datatype).getMarshalString().getBytes());
            os.write("();\n\n".getBytes());
          }
          else
          {
            os.write("\t\t\t".getBytes());
            os.write(vType.getTypeName().getBytes());
            os.write(" = hi.read".getBytes());
            os.write(((BaseDataType)datatype).getMarshalString().getBytes());
            os.write("(".getBytes());
            os.write(");\n".getBytes());

          }

        }
        break;

    } //end switch

  } //end printWrite();

  /**
   * Method printUnMarshal.
   * 
   * @param ostream
   * @throws IOException
   */
  private void printUnMarshal(OutputStream ostream) throws IOException
  {
    ostream.write("\tpublic void unmarshal(HaviByteArrayInputStream hi) throws HaviUnmarshallingException\n".getBytes());
    ostream.write("\t{\n".getBytes());

    //get the iterator from the constructype, the list must be a declarationtype
    Iterator iter = ht.iterator();

    ostream.write("\t\ttry\n\t\t{\n".getBytes());

    int loopCount = 0;
    while (iter.hasNext())
    {

      //get the declarationtype its datatype contains only basedatatype, sequencetype and literaltype
      DeclarationType dt = (DeclarationType)iter.next();
      loopCount++;

      HaviType dtype = dt.getDataType();
      ArrayList list = dt.getChildList();

      printUnMarshalWrite(loopCount, dtype, list, ostream, false);

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
      ostream.write("\t\t\tthrow new HaviUnmarshallingException(e.getMessage());\n".getBytes());
      ostream.write("\t\t}\n\n".getBytes());
    }
    ostream.write("\t}\n\n".getBytes());
    localExceptionList.clear();

  }

  /**
   * call by printEqual
   * 
   * Method equalWrite.
   * 
   * @param datatype
   * @param list
   * @param os
   * @param arrayFlag
   * @throws IOException
   */
private void equalWrite(HaviType datatype, ArrayList list, OutputStream os, boolean arrayFlag)	throws IOException
	{
		Iterator vIter = list.iterator();

		switch (datatype.getConstantTypeDef())
		{
			case LITERAL :
				HaviType h = (HaviType) CodeGenerator.dataTypeList.get(datatype.getTypeName());
				if (h == null)
				{
						while (vIter.hasNext())
						{
							HaviType type2 = (HaviType) vIter.next();
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

			case SEQUENCE :
				HaviType type = ((SequenceType) datatype).getDataType();
				equalWrite(type, list, os, true);
				break;

			case ENUM :
				while (vIter.hasNext())
				{
					HaviType type2 = (HaviType) vIter.next();
					String vName = type2.getTypeName().substring(0, 1).toUpperCase() + type2.getTypeName().substring(1);

					if (arrayFlag == true || type2 instanceof ArrayType)
					{
						os.write("\t\t\t{\n".getBytes());
						os.write(
							"\t\t\t\tint[] otherInt = other.get".getBytes());
						os.write(vName.getBytes());
						os.write("();\n".getBytes());

						os.write("\t\t\t\tint size = ".getBytes());
						os.write(type2.getTypeName().getBytes());
						os.write(".length;\n".getBytes());

						os.write(
							"\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
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

			case STRUCT :
			case UNION :
			case UNIONSTRUCT :
				while (vIter.hasNext())
				{
					HaviType type2 = (HaviType) vIter.next();
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

						os.write(
							"\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
						os.write("\t\t\t\t\tif(".getBytes());
						os.write(type2.getTypeName().getBytes());
						os.write(
							"[i].equals(otherType[i]) ==  false)\n".getBytes());
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

      case STRING :
      case WSTRING :
        while (vIter.hasNext())
        {
          HaviType type2 = (HaviType) vIter.next();
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

			default : //base data type

				while (vIter.hasNext())
				{
					HaviType type2 = (HaviType) vIter.next();
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

						os.write(
							"\t\t\t\tfor(int i=0; i < size; i++)\n".getBytes());
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

		} //end switch

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
   * call by printhashcode()
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

  /**
   * Method printToString.
   * 
   * @param os
   * @throws IOException
   */
  private void printToString(OutputStream os) throws IOException
  {

    os.write("\tpublic String toString()\n".getBytes());
    os.write("\t{\n".getBytes());

    Iterator iter = ht.iterator();

    os.write("\t\treturn ".getBytes());

    if (iter.hasNext())
    {
      while (iter.hasNext())
      {
        DeclarationType dt = (DeclarationType)iter.next();

        toStringWrite(dt.getDataType(), dt.getChildList(), os, false);

        if (iter.hasNext())
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
   * 
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

    switch (type.getConstantTypeDef())
    {

      case SEQUENCE:
        type = ((SequenceType)datatype).getDataType();
        toStringWrite(type, vList, os, true);
        break;

      case STRUCT:
      case UNION:
      case UNIONSTRUCT:
        while (vIter.hasNext())
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
        while (vIter.hasNext())
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

}