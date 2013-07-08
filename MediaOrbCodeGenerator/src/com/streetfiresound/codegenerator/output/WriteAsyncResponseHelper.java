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
 * $Id: WriteAsyncResponseHelper.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.streetfiresound.codegenerator.parser.CodeGenerator;

/**
 * @author george
 * 
 * To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates. To enable and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */
public class WriteAsyncResponseHelper
{
  String system;
  String constant;
  String packageName;
  String type;
  String exception;

  /**
   * Constructor for WriteAsyncResponseInvocationFactory.
   */
  public WriteAsyncResponseHelper() throws IOException
  {
    super();

    HashMap configInfo = (HashMap)CodeGenerator.projectList.get(CodeGenerator.currentPackage);
    constant = (String)configInfo.get("CONSTANT");
    system = (String)configInfo.get("SYSTEM");
    type = (String)configInfo.get("TYPE");
    packageName = (String)configInfo.get("PACKAGE");
    exception = (String)configInfo.get("EXCEPTION");

    String rootPath = (String)configInfo.get("ROOTPATH");

    //String location = "/" + driver.BASEOUTPUTPATH.replace('.','/') + "/" + driver.packagePath.replace('.', '/');
    String location = rootPath + "/" + packageName.replace('.', '/') + "/" + system.replace('.', '/');

    FileOutputStream fos = new GplHeaderFileOutputStream(location + "/" + CodeGenerator.currentPackage + "AsyncResponseHelper.java");

    outputToFile(fos);
    fos.close();

  }

  /**
   * Method outputToFile.
   * 
   * @param os
   * @throws IOException
   */
  private void outputToFile(OutputStream os) throws IOException
  {
    printPackage(os);
    printImport(os);
    printClass(os);
    printOpenClass(os);
    printPrivate(os);
    printConstructor(os);
    printCloseClass(os);

  }

  /**
   * Method printPackage.
   * 
   * @param os
   * @throws IOException
   */
  private void printPackage(OutputStream os) throws IOException
  {
    String pack = packageName + "." + system;
    os.write("package ".getBytes());
    os.write(pack.getBytes());
    os.write(";\n\n\n\n".getBytes());

  }

  /**
   * Method printImport.
   * 
   * @param os
   * @throws IOException
   */
  private void printImport(OutputStream os) throws IOException
  {
    String constantPath = packageName + "." + constant;
    String typePath = packageName + "." + type;
    String systemPath = packageName + "." + system;
    String exceptionPath = packageName + "." + exception;
    String packPath = packageName + "." + system;

    ArrayList pathList = new ArrayList();

    pathList.add(packPath);

    os.write("import org.havi.system.*;\n".getBytes());

    os.write("import ".getBytes());
    os.write(CodeGenerator.REDROCKETBASEPATH.getBytes());
    os.write(".rmi.*;\n\n".getBytes());
    
    //GENERAL CONSTANT PATH
    if (!pathList.contains(CodeGenerator.generalConstantPath))
    {
      os.write("import ".getBytes());
      os.write(CodeGenerator.generalConstantPath.getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.generalConstantPath.getBytes());
    }

    //GENERAL TYPE PATH
    if (!pathList.contains(CodeGenerator.generalTypePath))
    {
      os.write("import ".getBytes());
      os.write(CodeGenerator.generalTypePath.getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.generalTypePath.getBytes());
    }

    //GENERAL EXCEPTION PATH
    if (!pathList.contains(CodeGenerator.generalExceptionPath))
    {
      os.write("import ".getBytes());
      os.write(CodeGenerator.generalExceptionPath.getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.generalExceptionPath.getBytes());
    }

    if (!pathList.contains(constantPath))
    {
      os.write("import ".getBytes());
      os.write(constantPath.getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(constantPath);
    }

    if (!pathList.contains(typePath))
    {
      os.write("import ".getBytes());
      os.write(typePath.getBytes());
      os.write(".*;\n\n".getBytes());
      pathList.add(typePath);
    }

    if (!pathList.contains(exceptionPath))
    {
      os.write("import ".getBytes());
      os.write(exceptionPath.getBytes());
      os.write(".*;\n\n".getBytes());
      pathList.add(exceptionPath);
    }

    //dcm constants, com.streetfiresound.codegenerator.types and system path
    if (!pathList.contains(CodeGenerator.DCM_PATH + ".constants"))
    {
      os.write("import ".getBytes());
      os.write((CodeGenerator.DCM_PATH + ".constants").getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.DCM_PATH + ".constants");

    }
    if (!pathList.contains(CodeGenerator.DCM_PATH + ".types"))
    {
      os.write("import ".getBytes());
      os.write((CodeGenerator.DCM_PATH + ".types").getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.DCM_PATH + ".types");
    }
    if (!pathList.contains(CodeGenerator.DCM_PATH + ".rmi"))
    {
      os.write("import ".getBytes());
      os.write((CodeGenerator.DCM_PATH + ".rmi").getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.DCM_PATH + ".rmi");
    }

    //fcm constants, type and system path
    if (!pathList.contains(CodeGenerator.FCM_PATH + ".constants"))
    {
      os.write("import ".getBytes());
      os.write((CodeGenerator.FCM_PATH + ".constants").getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.FCM_PATH + ".constants");
    }
    if (!pathList.contains(CodeGenerator.FCM_PATH + ".types"))
    {
      os.write("import ".getBytes());
      os.write((CodeGenerator.FCM_PATH + ".types").getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.FCM_PATH + ".types");
    }

    if (!pathList.contains(CodeGenerator.FCM_PATH + ".rmi"))
    {
      os.write("import ".getBytes());
      os.write((CodeGenerator.FCM_PATH + ".rmi").getBytes());
      os.write(".*;\n".getBytes());
      pathList.add(CodeGenerator.FCM_PATH + ".rmi");
    }

  }

  /**
   * Method printClass.
   * 
   * @param os
   * @throws IOException
   */
  private void printClass(OutputStream os) throws IOException
  {
    os.write("public class ".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("AsyncResponseHelper extends AsyncResponseHelper\n".getBytes());

  }

  /**
   * Method printOpenClass.
   * 
   * @param os
   * @throws IOException
   */
  private void printOpenClass(OutputStream os) throws IOException
  {
    os.write("{\n".getBytes());

  }

  /**
   * Method printCloseClass.
   * 
   * @param os
   * @throws IOException
   */
  private void printCloseClass(OutputStream os) throws IOException
  {
    os.write("\n}\n\n".getBytes());

  }

  /**
   * Method printConstructor.
   * 
   * @param os
   * @throws IOException
   */
  private void printConstructor(OutputStream os) throws IOException
  {
    os.write("\tpublic ".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("AsyncResponseHelper(SoftwareElement softwareElement) throws HaviMsgListenerExistsException, Havi".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("Exception\n".getBytes());

    os.write("\t{\n".getBytes());

    os.write("\t\t// Construct super class\n".getBytes());
    os.write("\t\tsuper(softwareElement, invocationFactory);\n".getBytes());
    os.write("\t}\n\n\n".getBytes());

  }

  /**
   * Method printPrivate.
   * 
   * @param os
   * @throws IOException
   */
  private void printPrivate(OutputStream os) throws IOException
  {
    os.write("\tprivate final static ".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("AsyncResponseInvocationFactory invocationFactory = new ".getBytes());
    os.write(CodeGenerator.currentPackage.getBytes());
    os.write("AsyncResponseInvocationFactory();\n\n\n".getBytes());

  }

}

