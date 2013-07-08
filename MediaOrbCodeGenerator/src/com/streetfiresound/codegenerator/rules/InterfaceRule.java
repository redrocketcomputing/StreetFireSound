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
 * $Id: InterfaceRule.java,v 1.2 2005/02/24 03:03:35 stephen Exp $
 */

package com.streetfiresound.codegenerator.rules;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import com.streetfiresound.codegenerator.output.WriteAsynResponseInvocationFactory;
import com.streetfiresound.codegenerator.output.WriteAsyncResponseHelper;
import com.streetfiresound.codegenerator.output.WriteClient;
import com.streetfiresound.codegenerator.output.WriteRemoteInvocationFactory;
import com.streetfiresound.codegenerator.output.WriteServerHelper;
import com.streetfiresound.codegenerator.output.WriteSkeleton;
import com.streetfiresound.codegenerator.types.HaviType;
import com.streetfiresound.codegenerator.types.InterfaceType;





/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class InterfaceRule extends RuleDefinition
{

  /**
   * Constructor for InterfaceRule.
   */
  public InterfaceRule(InterfaceType it)
  {
    super(it);
  }

  /**
   * @see com.streetfiresound.codegenerator.rules.RuleOutput#outputToFile()
   */
      public void outputToFile(java.io.OutputStream ostream)throws java.io.IOException
      {


		  // check for special case interface, it class not found then throw exception which perform normal interface rule
          String iName = ((InterfaceType) ht).getInterfaceHeaderType().getTypeName();

          try
          {

            String className = "com.streetfiresound.codegenerator.output.WriteInterface"+iName;

			//using reflection to active the appropriate class
            Class newClass = Class.forName(className);

            Class[] parameter = { ht.getClass() };

            Object[] parameterList = { ht };


             Constructor constructor = newClass.getConstructor(parameter);

           constructor.newInstance(parameterList);


          }
          catch(ClassNotFoundException e)
          {

              InterfaceType it = (InterfaceType) ht;

              Iterator iter  = it.getChildList().iterator();
              while(iter.hasNext())
              {
              	HaviType localHaviType = (HaviType) iter.next();

				localHaviType.output(ostream);
//                ((HaviType) iter.next()).output(ostream);

              }


			  //create skeleton
              new WriteSkeleton((InterfaceType) ht);

			 //create remoteinvocationfactory
              new WriteRemoteInvocationFactory((InterfaceType) ht);

			  //create asynresponseinvocationfactory
              new WriteAsynResponseInvocationFactory((InterfaceType) ht);

			  //create serverhelper
              new WriteServerHelper();

              //crate asyncresponsehelper
              new WriteAsyncResponseHelper();

			  //create client stub
              new WriteClient((InterfaceType) ht);


          }

          catch(Exception e)
          {
            System.out.println(e.getMessage());
          }
      }

}
