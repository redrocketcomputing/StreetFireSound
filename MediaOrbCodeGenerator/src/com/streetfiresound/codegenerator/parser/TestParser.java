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
 * $Id: TestParser.java,v 1.2 2005/02/24 03:03:39 stephen Exp $
 */

package com.streetfiresound.codegenerator.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;

import com.streetfiresound.codegenerator.types.ContextType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestParser {


	ArrayList success;
	ArrayList failure;
	ArrayList error;

	IDLParser parser;

		public TestParser()
		{
			success = new ArrayList();
			failure = new ArrayList();
			error = new ArrayList();
			testOut();
			testInOut();
			testMissingComma();
			testMissingSemiColon();
			testInvalidDeclaration();
			testInvalidTypedef();
			out();
		}



		public void testOut()
		{
				String method = "out detected";
				boolean success = true;
					String idlFilePath = new String("/IdlFile/");
					String idlFileName = new String("testhavi1.idl");

				try
				{

					File idlFile = new File("");
					String filename = idlFile.getAbsolutePath()+idlFilePath+idlFileName;

					//create com.streetfiresound.codegenerator.parser object to parse the idl file.
		            IDLParser parser = new IDLParser(new java.io.FileInputStream(filename));
					ContextType ct = (ContextType)parser.specification();

				}
				catch(FileNotFoundException e) { System.err.println("file not found = "+ idlFileName); method="";}
				catch(Exception e) { success = false;}

				assertTrue(success, method);


		}




		public void testInOut()
		{

				String method = "inout detected";
					String idlFilePath = new String("/IdlFile/");
					String idlFileName = new String("testhavi2.idl");

				boolean success = true;
				try
				{

					File idlFile = new File("");
					String filename = idlFile.getAbsolutePath()+idlFilePath+idlFileName;

					//create com.streetfiresound.codegenerator.parser object to parse the idl file.
					parser.ReInit(new java.io.FileInputStream(filename));
					ContextType ct = (ContextType)parser.specification();

				}
				catch(FileNotFoundException e) { System.err.println("file not found = "+ idlFileName); method="";}
				catch(Exception e) { success = false;}
				assertTrue(success, method);

		}



		public void testMissingComma()
		{

				String method = "missing comma";
				boolean success = true;
					String idlFilePath = new String("/IdlFile/");
					String idlFileName = new String("testhavi3.idl");

				try
				{

					File idlFile = new File("");
					String filename = idlFile.getAbsolutePath()+idlFilePath+idlFileName;

					//create com.streetfiresound.codegenerator.parser object to parse the idl file.
					parser.ReInit(new java.io.FileInputStream(filename));
					ContextType ct = (ContextType)parser.specification();

				}
				catch(FileNotFoundException e) { System.err.println("file not found = "+ idlFileName); method="";}
				catch(Exception e) { success = false;}
				assertTrue(success, method);

		}



		public void testMissingSemiColon()
		{
				String method = "missing SemiColon";
				boolean success = true;
					String idlFilePath = new String("/IdlFile/");
					String idlFileName = new String("testhavi4.idl");

				try
				{

					File idlFile = new File("");
					String filename = idlFile.getAbsolutePath()+idlFilePath+idlFileName;

					//create com.streetfiresound.codegenerator.parser object to parse the idl file.
					parser.ReInit(new java.io.FileInputStream(filename));
					ContextType ct = (ContextType)parser.specification();

				}
				catch(FileNotFoundException e) { System.err.println("file not found = "+ idlFileName); method="";}
				catch(Exception e) { success = false;}
				assertTrue(success, method);

		}



		public void testInvalidDeclaration()
		{

				String method = "Invalid Declartion";
				String idlFilePath = new String("/IdlFile/");
				String idlFileName = new String("testhavi5.idl");

				boolean success = true;
				try
				{

					File idlFile = new File("");
					String filename = idlFile.getAbsolutePath()+idlFilePath+idlFileName;

					//create com.streetfiresound.codegenerator.parser object to parse the idl file.
					parser.ReInit(new java.io.FileInputStream(filename));
					ContextType ct = (ContextType)parser.specification();

				}
				catch(FileNotFoundException e) { System.err.println("file not found = "+ idlFileName); method="";}
				catch(Exception e) { success = false;}
				assertTrue(success, method);


		}


		public void testInvalidTypedef()
		{
				String idlFilePath = new String("/IdlFile/");
				String idlFileName = new String("testhavi6.idl");
				String method = "Invalid Typedef";

				boolean success = true;
				try
				{

					File idlFile = new File("");
					String filename = idlFile.getAbsolutePath()+idlFilePath+idlFileName;

					//create com.streetfiresound.codegenerator.parser object to parse the idl file.
					parser.ReInit(new java.io.FileInputStream(filename));
					ContextType ct = (ContextType)parser.specification();

				}
				catch(FileNotFoundException e) { System.err.println("file not found = "+ idlFileName); method="";}
				catch(Exception e) { success = false;}
				assertTrue(success, method);

		}


	public void assertTrue(boolean bool, String name)
	{
		if(name.length() == 0)
			error.add("error");
		else if(bool == true)
			success.add(name);
		else
			failure.add(name);


	}



	public void out()
	{
		System.out.println("Error detected = "+error.size());
		System.out.println();

		System.out.println("Success = "+success.size());
		for(Iterator iter = success.iterator(); iter.hasNext();)
			System.out.println(iter.next());

		System.out.println();
		System.out.println("Failure = "+ failure.size());
		for(Iterator iter = failure.iterator(); iter.hasNext(); )
			System.out.println(iter.next());
	}



	public static void main(String[] argv)
	{
		new TestParser();
	}

}


