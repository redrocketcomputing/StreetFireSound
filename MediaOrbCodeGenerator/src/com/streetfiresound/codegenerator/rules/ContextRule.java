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
 * $Id: ContextRule.java,v 1.2 2005/02/24 03:03:35 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;


import java.io.IOException;
import java.util.Iterator;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ContextType;
import com.streetfiresound.codegenerator.types.HaviType;



/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ContextRule extends RuleDefinition
{

  /**
   * Constructor for ContextRule.
   */
  public ContextRule(ContextType it)
  {
    super(it);
  }

  /**
   * Loop the the context list and call the appropriate rule to output the class file
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
  public void outputToFile(java.io.OutputStream ostream) throws IOException
  {

	  //get the list
      Iterator iter = ((ContextType)ht).iterator();

      //iterate the list
      while(iter.hasNext())
      {
      	  //get the havitype object
          HaviType hObject = (HaviType) iter.next();


		  //get rule that associate with the havitype object

          RuleDefinition rule = CodeGenerator.ruleFactory.createRuleObject(hObject.getConstantTypeDef(), hObject);

          //call the object rule
          rule.outputToFile(ostream);

      }


    //create the exception factory
//     new WriteExceptionFactory();





  }

}
