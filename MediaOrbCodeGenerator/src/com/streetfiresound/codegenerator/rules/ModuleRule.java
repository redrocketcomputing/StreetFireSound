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
 * $Id: ModuleRule.java,v 1.2 2005/02/24 03:03:35 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;


import java.util.HashMap;


import com.streetfiresound.codegenerator.output.WriteConstant;
import com.streetfiresound.codegenerator.output.WriteExceptionFactory;
import com.streetfiresound.codegenerator.output.WriteModuleException;
import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.ModuleType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ModuleRule extends RuleDefinition
{
  /**
   * Constructor for ModuleRule.
   */
  public ModuleRule(ModuleType it)
  {
    super(it);
  }

  /**
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
  public void outputToFile(java.io.OutputStream ostream) throws java.io.IOException
  {
    System.out.println("module name = '" + ht.getTypeName() + "'" );


    CodeGenerator.currentPackage = ((ModuleType) ht).getTypeName();

	CodeGenerator.printConstList = new HashMap();


	HashMap configurationInfo = (HashMap)CodeGenerator.projectList.get(ht.getTypeName());


    java.util.Iterator iter = ((ModuleType) ht).iterator();



    while(iter.hasNext())
    {
      HaviType ht2 = (HaviType) iter.next();
      ht2.output(ostream);
    }

   ConstRule.outToFile();			//need

   CodeGenerator.printConstList = null;


    new WriteModuleException();	//need

    new WriteConstant();			//need


//	new WriteEventManagerClient();
//    new WriteEventNotificationInvocationFactory();

    //create the exception factory
	if(!CodeGenerator.currentPackage.equalsIgnoreCase(CodeGenerator.COMPARETYPE))
    	 new WriteExceptionFactory();


//after each module should clean up eventlist and opcodelist.
//    CodeGenerator.eventList.clear();			// no need
    CodeGenerator.opcodeList.clear();			//need
	CodeGenerator.messageStreamOpCodeList.clear();//need
	CodeGenerator.messageStreamEventList.clear(); //need
	CodeGenerator.exceptionList.clear();
  }












}
