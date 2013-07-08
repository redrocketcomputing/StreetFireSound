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
 * $Id: ArrayFileCreation.java,v 1.1 2005/02/22 03:46:07 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


import com.streetfiresound.codegenerator.output.GplHeaderFileOutputStream;
import com.streetfiresound.codegenerator.parser.CodeGenerator;


/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ArrayFileCreation
{

  String type;
  String name;
  String value;

  /**
   * Constructor for ArrayFileCreation.
   */
  public ArrayFileCreation()
  {
    super();
    type = new String();
    name = new String();
    value = new String();
  }


  public ArrayFileCreation(String type, String name, String value)
  {
    this.type = type;
    this.name = name;
    this.value = value;
  }



    private void createFile(String type, String name, String value)
    {
      try
      {

        String location = "/" + CodeGenerator.packagePath.replace('.', '/') + "/" + CodeGenerator.currentPackage;

        File newfile = new File(location);
        if(!newfile.exists())
          newfile.mkdir();
         FileOutputStream fos = new GplHeaderFileOutputStream(location+"/"+name+".java");

      }
      catch(IOException e)
      {
        System.err.println("Typedef create file error");
        e.printStackTrace();
      }

    }


    private void printPackage(OutputStream os) throws IOException
    {
      os.write("package ".getBytes());
      os.write(CodeGenerator.packagePath.getBytes());
      os.write(";\n\n".getBytes());
    }

    private void printOpening(OutputStream os) throws IOException
    {
      os.write("public final class ".getBytes());
      os.write(name.getBytes());
      os.write(" extends HaviObject\n".getBytes());
      os.write("{ \n".getBytes());


    }







}
