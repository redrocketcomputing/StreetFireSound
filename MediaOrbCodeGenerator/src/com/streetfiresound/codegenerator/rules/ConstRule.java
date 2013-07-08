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
 * $Id: ConstRule.java,v 1.2 2005/02/24 03:03:34 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.streetfiresound.codegenerator.output.GplHeaderFileOutputStream;
import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.ConstTypeDefinition;
import com.streetfiresound.codegenerator.types.HaviType;

/**
 * @author george
 *
 */
public class ConstRule extends RuleDefinition implements ConstTypeDefinition
{

  /**
   * Constructor for ConstRule.
   */



  public ConstRule(ConstType it)
  {
    super(it);
  }

  /**
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
  public void outputToFile(java.io.OutputStream ostream)throws java.io.IOException
  {

//	System.out.println("consttyep="+((ConstType)ht).getDataType().getTypeName());


	String typeName=((ConstType)ht).getDataType().getTypeName();
	ArrayList aList = (ArrayList)CodeGenerator.printConstList.get(typeName);

    if(aList == null)
    {
        aList = new ArrayList();
        CodeGenerator.printConstList.put(typeName, aList);
    }
    aList.add((ConstType) ht);

  }


    public static void outToFile() throws IOException
    {

            Set set = CodeGenerator.printConstList.entrySet();
            Iterator iter = set.iterator();

            while(iter.hasNext())
            {
				Map.Entry map = (Map.Entry) iter.next();

				String name = (String)map.getKey();
				ArrayList constList = (ArrayList) map.getValue();

 		        try
				{

					// check for special case enum, if class not found then will throw ClassNotFoundException mean perform normal enum rule
				     String className = "com.streetfiresound.codegenerator.output.WriteConst"+name;

					//using reflection to create special case object
			        Class newClass = Class.forName(className);

			        Class[] parameter = { constList.getClass() };

			        Object[] parameterList = { constList };

			        Constructor constructor = newClass.getConstructor(parameter);

			        constructor.newInstance(parameterList);


		        }
				catch(ClassNotFoundException e)
				{
					HashMap  configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);

					String system =  (String) configInfo.get("SYSTEM");
					String type = (String) configInfo.get("TYPE");
					String constant = (String) configInfo.get("CONSTANT");
					String packageName = (String) configInfo.get("PACKAGE");
					String rootPath = (String)configInfo.get("ROOTPATH");

					String constantPath = packageName+"."+constant;
					String pack = packageName + "." + constant;
					String systemPath = packageName + "."+ system;
					String typePath = packageName + "." + type;


		            String location = rootPath + "/" + constantPath.replace('.','/');


					  HaviType checktype = (HaviType)CodeGenerator.dataTypeList.get(name);
					  if(checktype != null)
					  {
		                  FileOutputStream fos = new GplHeaderFileOutputStream(location+"/Const"+name+".java");

		                  printPackage(fos, pack);

		                  printInterface(fos, name);

		                  printOpen(fos);

		                  printContent(fos, constList);

		                  printClose(fos);
					  }
					  else
					  	System.err.println("constant type cannot be found:"+  name);

				  }
			      catch(NoSuchMethodException e)
			      {
			        System.err.println("Const error="+e+":"+ name);
			      }


			      catch(InstantiationException e)
			      {
			        System.err.println("Const error="+e+":"+ name);
			      }

			      catch(IllegalAccessException e)
			      {
			        System.err.println("Const error="+e+":"+ name);
			      }
			      catch(InvocationTargetException e)
			      {
			        System.err.println("Const error="+e+":"+ name);
			      }

            }
			CodeGenerator.printConstList.clear();

    }




    private static void printPackage(OutputStream ostream, String packPath) throws IOException
    {
        ostream.write(( "package " + packPath + ";\n\n\n").getBytes());

    }


    private static void printInterface(OutputStream ostream, String name) throws IOException
    {
        ostream.write("public interface Const".getBytes());
        ostream.write(name.getBytes());
    }






    private static void printContent(OutputStream ostream, ArrayList al) throws IOException
    {

        Iterator iter = al.iterator();

        while(iter.hasNext())
        {

              ConstType ct = (ConstType)iter.next();

              HaviType dataType = ct.getDataType();

              ostream.write("\t\tpublic static final ".getBytes());

              printDataType(dataType, ostream);

              ostream.write(" ".getBytes());

              ostream.write(ct.getTypeName().getBytes());

              ostream.write(" = (".getBytes());


              printDataType(dataType, ostream);

              ostream.write(") ".getBytes());

              ostream.write(ct.getValue().getBytes());

              ostream.write(";\n".getBytes());

        }

    }


    private static void printDataType(HaviType dataType, OutputStream ostream) throws IOException
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












    private static  void printOpen(OutputStream ostream) throws IOException
    {
      ostream.write("\n{\n".getBytes());

    }

    private static void  printClose(OutputStream ostream) throws IOException
    {
      ostream.write("\n}\n".getBytes());
    }



}
