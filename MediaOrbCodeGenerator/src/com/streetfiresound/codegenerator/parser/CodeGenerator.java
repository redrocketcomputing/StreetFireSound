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
 * $Id: CodeGenerator.java,v 1.2 2005/02/24 03:03:39 stephen Exp $
 */

package com.streetfiresound.codegenerator.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;


import com.streetfiresound.codegenerator.rules.RuleFactory;
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.ContextType;
import com.streetfiresound.codegenerator.types.ShortType;

/**
 * @author george
 *
 */
public class CodeGenerator implements PathConfiguration
{
  //keep track of current package (module scope);
  static public String currentPackage;

  //rule factory
  static public RuleFactory ruleFactory = new RuleFactory();

  //contains a list of constant variable
  static public HashMap constList = new HashMap();

  //contains a list of event
  //  static public HashMap eventList = new HashMap();

  static public ArrayList messageStreamEventList = new ArrayList();

  //contains a list of operation Code
  static public ArrayList opcodeList = new ArrayList();
  static public ArrayList messageStreamOpCodeList = new ArrayList();

  //contains a list of exception generated from errorcode
  //  static public HashMap exceptionList = new HashMap();
  static public ArrayList exceptionList = new ArrayList();

  //contain a list of module generated from com.streetfiresound.codegenerator.parser
  static public ArrayList moduleList = new ArrayList();

  //contain a list of data type generated from com.streetfiresound.codegenerator.parser. (structtype, unionstructtype, enum, union , typedef)
  static public HashMap dataTypeList = new HashMap();

  static public String packagePath = "";

  static public HashMap printConstList;

  //contain havi api code;
  static public HashMap haviapi = new HashMap();

  // root path for generated code
  public String genRootPath;

  // the directory in which to find type classes for the current idl file
  // currently always the havi gen dir, both for havi.conf and rbx1600.conf
  static public String typesRootPath; //XXX:0000:20041018iain: yuck, had to make static due to inappropriately static method

  static public String generalConstantPath;
  static public String generalTypePath;
  static public String generalExceptionPath;
  static public String generalHaviSystemPath;
  static public String generalSystemPath;

  static public HashMap projectList = new HashMap();
  private ArrayList outputList = new ArrayList();
  IDLParser parser;

  /**
   * Constructor for driver.
   */
  public CodeGenerator(String idlFilePath, String genRootPath, String typesRootPath)
  {
    super();

    // check file existance first
    File idlFile = new File(idlFilePath);
    if (!idlFile.exists())
    {
      throw new Error("Error: Idl File '" + idlFile.getAbsolutePath() + "' does not exist");
    }
    File genRoot = new File(genRootPath);
    if (!genRoot.exists())
    {
      throw new Error("Error: Idl File '" + genRoot.getAbsolutePath() + "' does not exist");
    }
    File typesRoot = new File(typesRootPath);
    if (!typesRoot.exists())
    {
      throw new Error("Error: Idl File '" + typesRoot.getAbsolutePath() + "' does not exist");
    }

    System.out.println("------------------------------------------------------------------------------");
    System.out.println("Idl File      = '" + idlFile.getAbsolutePath() + "'");
    System.out.println("Generate Root = '" + genRoot.getAbsolutePath() + "'");
    System.out.println("Types Root    = '" + typesRoot.getAbsolutePath() + "'");
    System.out.println("------------------------------------------------------------------------------");

    generalConstantPath = new String(GENERAL_PACKAGE + "." + GENERAL_CONSTANT_PATH);
    generalTypePath = new String(GENERAL_PACKAGE + "." + GENERAL_TYPE_PATH);
    generalExceptionPath = new String(GENERAL_PACKAGE + "." + GENERAL_EXCEPTION_PATH);
    generalHaviSystemPath = new String(GENERAL_PACKAGE + "." + GENERAL_TYPE_PATH);
    generalSystemPath = new String(GENERAL_PACKAGE + "." + GENERAL_SYSTEM_PATH);

    this.genRootPath = genRootPath;
    this.typesRootPath = typesRootPath;

    File file = new File(idlFilePath);

    if (file.isFile())
    {
      try
      {
        startParsing(file);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    else if (file.isDirectory())
    {
      //XXX:00000:20041018iain: disabled searching for all conf files, must specify on command line
      throw new Error("must specify a single idl file");

      //XXX:0000:20041018iain: remove following when we're sure it's dead
//       // basically, one configuration file for one project, so it need to clean up memory after each project
//       String[] configFile = file.list(new confFilter());
//       int i = 0;
//       for (i = 0; i < configFile.length; i++)
//       {
//         File cFile = new File(file.getAbsoluteFile() + "/" + configFile[i]);

//         try
//         {
//           startParsing(cFile);
//         }
//         catch (IOException e)
//         {
//           e.printStackTrace();
//           System.out.println(e + ":" + configFile[i]);
//         }
//         finally
//         {
//           outputList.clear();
//           projectList.clear();
//           constList.clear();
//           //					eventList.clear();
//           opcodeList.clear();
//           messageStreamOpCodeList.clear();
//           messageStreamEventList.clear();
//           exceptionList.clear();
//           moduleList.clear();
//           dataTypeList.clear();
//         }
//       }

    }
    else
    {
      System.err.println("Not a valid file");
      return;
    }

    System.out.println("done");
  }

  private class confFilter implements FilenameFilter
  {
    public boolean accept(File file, String ext)
    {
      if (ext.endsWith(".conf"))
        return true;
      else
        return false;

    }

  }

  private void startParsing(File configFile) throws IOException
  {

    String path = configFile.getParent();
    String rootPath = buildConfiguration(configFile);
    Set set = projectList.keySet();
    Iterator iter = set.iterator();

    //loop thru the module file inside the configuration file
    while (iter.hasNext())
    {
      String file = (String)iter.next();

      File idlFile = new File(path + "/" + file + ".idl");

      //if idlfile exists then start parsing
      if (idlFile.exists())
      {

        if (parser == null)
          parser = new IDLParser(new java.io.FileInputStream(idlFile));
        else
        {
          parser.ReInit(new java.io.FileInputStream(idlFile));
        }

        parseFile(parser, outputList, idlFile);
      }
      else
        System.err.println("IDL file not exists: " + idlFile.getName());
    }

    ArrayList tempList = (ArrayList)constList.get("ApiCode");

    // create ApiCode list
    if (tempList == null)
    {
      makeTypeIdList("ApiCode");
      tempList = (ArrayList)constList.get("ApiCode");

    }

    //build up a haviapi hash table for fast searching
    Iterator constIter = tempList.iterator();
    if (constIter != null)
    {
      while (constIter.hasNext())
      {
        ConstType temp = (ConstType)constIter.next();
        haviapi.put(temp.getTypeName(), temp.getValue());
      }
    }

    //after parsing all the idl files, start output
    iter = outputList.iterator();
    while (iter.hasNext())
    {
      ContextType ct = (ContextType)iter.next();

      try
      {
        ct.output(System.out);  //XXX:00000000:20041018iain passed outputstream ignored
      }
      catch (Exception e)
      {
        e.printStackTrace();
        //System.err.println("Error output");
      }

    }

  }

  private void parseFile(IDLParser parser, ArrayList outputList, File idlFile)
  {
    try
    {

      ContextType ct = (ContextType)parser.specification();
      //performing a lazy output
      //after parsing successful, put the contextype into the arraylist and output later
      //purpose is to make sure all the type such as typedef enum etc are available
      outputList.add(ct);
    }
    catch (ParseException e)
    {
              e.printStackTrace();
      System.err.println(e.getMessage(idlFile.getName()));
    }
    /*
     * catch(Exception e) { System.err.println("driver.parseFile() error=" + e + idlFile.getName() ); }
     */
  }

  private String buildConfiguration(File configFile) throws IOException
  {

    FileReader fr = new FileReader(configFile);
    BufferedReader br = new BufferedReader(fr);
    String packName;
    //			String projectName = configFile.getName().substring(0, configFile.getName().indexOf(".conf") );

    int commentoutcount = 0;

//     String defaultRootPath = br.readLine();
//     if (!defaultRootPath.startsWith("DEFAULTROOTPATH="))
//     {
//       throw new IOException("not a valid confgiruation file:" + configFile.getName());
//     }
//    defaultRootPath = getTokenizePath(defaultRootPath);

    String defaultRootPath = genRootPath;

    while ((packName = br.readLine()) != null)
    {
      if (packName.startsWith("/*"))
        commentoutcount++;

      if (packName.startsWith("*/"))
        commentoutcount--;

      if (commentoutcount == 0 && packName.endsWith(":"))
      {
        packName = packName.substring(0, packName.indexOf(":"));

        build(packName, br, defaultRootPath); //, projectName);

      }
    }

    return defaultRootPath;
  }

  private void build(String packageName, BufferedReader br, String defaultRootPath) throws IOException
  {

    String name = "";
    String path = "";

    HashMap list = new HashMap();

    list.put("ROOTPATH", defaultRootPath);

    while ((name = br.readLine()) != null)
    {
      if (name.trim().length() == 0)
        break;

      StringTokenizer st = new StringTokenizer(name, "=");
      name = st.nextToken();
      path = st.nextToken();

      if (name.equals("IMPORT"))
      {
        ArrayList importList = (ArrayList)list.get("IMPORT");

        if (importList == null)
        {
          importList = new ArrayList();
          list.put(name, importList);
        }

        importList.add(path);

      }
      else
        list.put(name.toUpperCase(), path);

    }

    projectList.put(packageName, list);

    try
    {
      String rootPath = (String)list.get("ROOTPATH");
      String typePath = (String)list.get("TYPE");
      String systemPath = (String)list.get("SYSTEM");
      String constantPath = (String)list.get("CONSTANT");
      String exceptionPath = (String)list.get("EXCEPTION");
      String packPath = (String)list.get("PACKAGE");

      createDirectory(rootPath + "/" + packPath.replace('.', '/') + "/" + typePath.replace('.', '/'));

      createDirectory(rootPath + "/" + packPath.replace('.', '/') + "/" + systemPath.replace('.', '/'));

      createDirectory(rootPath + "/" + packPath.replace('.', '/') + "/" + constantPath.replace('.', '/'));

      createDirectory(rootPath + "/" + packPath.replace('.', '/') + "/" + exceptionPath.replace('.', '/'));

    }
    catch (DirectoryCreationErrorException e)
    {
              e.printStackTrace();
      System.err.println("Driver.DirectoryCreateionError=" + e);
    }

  }

  /**
   * Create output directory, output directory is determine from BASEOUTPUTPATH after output to the directory, we need to import to the project
   *
   * Method createDirectory.
   *
   * @throws DirectoryCreationErrorException
   */
  public static void createDirectory(String path) throws DirectoryCreationErrorException
  {
    StringTokenizer pathToken = new StringTokenizer(path, "/");

    // initial location is absolute or relative depending on passed path
    String location = path.startsWith("/")  ? "/" : "";

    File directory;

    while (pathToken.hasMoreTokens())
    {
      location += pathToken.nextToken() + '/';
      directory = new File(location);

      if (!directory.exists())
      {
        directory.mkdir();
      }
    }
  }

  public static void main(String[] args)
  {
    if (args.length != 3)
    {
      System.err.println("Usage: CodeGenerator <conf file path> <generate root path> <com.streetfiresound.codegenerator.types root path>");
      System.exit(-1);
    }

    String confFilePath  = args[0];
    String genRootPath   = args[1];
    String typesRootPath = args[2];

    try
    {
      new CodeGenerator(confFilePath, genRootPath, typesRootPath);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

  }

  public static void makeTypeIdList(String type) throws IOException //SystemEventType, VendorEventType, SoftwareElementType
  {

    String filename = typesRootPath + "/" + GENERAL_PACKAGE.replace('.', '/') + "/" + GENERAL_CONSTANT_PATH + "/Const" + type + ".java";

    FileReader fr = new FileReader(filename);

    BufferedReader br = new BufferedReader(fr);

    String detect = "public static final ";

    String buffer;

    ArrayList newConstList = new ArrayList();
    while ((buffer = br.readLine()) != null)
    {
      buffer = buffer.trim();

      if (buffer.startsWith(detect))
      {
        ConstType ct = new ConstType();
        String newString = buffer.substring(detect.length() + 1);
        StringTokenizer st = new StringTokenizer(newString);

        st.nextToken();
        ct.setDataType(new ShortType());
        ct.setTypeName(st.nextToken());
        st.nextToken();
        st.nextToken();

        String value = st.nextToken();

        ct.setValue(value.substring(0, value.indexOf(";")));

        newConstList.add(ct);
      }
    }

    CodeGenerator.constList.put(type, newConstList);

  }

  private String getTokenizePath(String path)
  {
    StringTokenizer st = new StringTokenizer(path, "=");
    st.nextToken();
    return st.nextToken();
  }

  public static String makeFileName(String filename)
  {
    StringTokenizer st = new StringTokenizer(filename, "_");
    String newFileName = "";

    while (st.hasMoreTokens())
    {
      String tempName = st.nextToken();

      newFileName += tempName.substring(0, 1).toUpperCase() + tempName.substring(1);
    }

    return newFileName;

  }

}
