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
 * $Id: WriteInterfaceVendorEvent.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;


import com.streetfiresound.codegenerator.parser.CodeGenerator;
import com.streetfiresound.codegenerator.types.ConstType;
import com.streetfiresound.codegenerator.types.FunctionType;
import com.streetfiresound.codegenerator.types.InterfaceType;


/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WriteInterfaceVendorEvent
{

  InterfaceType it;
  HashMap constantMap;

  /**
   * Constructor for WriteInterfaceSystemEvent.
   */
  public WriteInterfaceVendorEvent(InterfaceType it) throws IOException
  {

    super();

    this.it = it;

	buildConstantMap();

    outputToFile();

	ArrayList al = new ArrayList();
	al.add(this.it);
	al.add(constantMap);
//	CodeGenerator.eventList.put("VendorEvent", al);


  }




/**
 * Method outputToFile.
 * @throws IOException
 */
  private void outputToFile() throws IOException
  {

        Iterator iter  = it.getChildList().iterator();


        while(iter.hasNext())
        {

            FunctionType ft = (FunctionType) iter.next();

			//output to EventNotificationLister
            new writeEventNotificationListener(ft);	//ok

			//output to EventNotificationInvocation
//            new WriteEventNotificationInvocation(ft); //ok

						new WriteVendorEventNotificationHelper(ft, constantMap);



        }


		//output to EventManagerNotificationHelper
//        new WriteEventManagerNotificationHelper();	//ok


		//output to EventManagerClient
        new WriteVendorEventManagerClient(it, constantMap);			//ok


		//output to EventNotificationInvocationFactory
//        new WriteEventNotificationInvocationFactory(it,"VendorEvent", constantMap);	//ok


//		new WriteEventConstant(it.getChildList(), constantMap, "Vendor", "1");

  }


	  private String makeSearchConstantName(String iname)
	  {
	  	StringTokenizer st = new StringTokenizer(iname, "_");

	  	String newName ="";
	  	while(st.hasMoreTokens())
	  		newName += (String) st.nextToken();

		return newName.toUpperCase();
	  }




	 private void buildConstantMap() throws IOException
	 {

			ArrayList constList = (ArrayList) CodeGenerator.constList.get("VendorEventType");//REFER TO HAVI.IDL

		    if(constList == null)
		    {
				try
				{
					CodeGenerator.makeTypeIdList("VendorEventType");

					constList = (ArrayList) CodeGenerator.constList.get("VendorEventType")	;

				}
				catch(FileNotFoundException e)
				{
					System.err.println("WriteInterfaceVendorEvent error - VendorEventType not found in constList");
					throw new IOException("WriteInterfaceVendorEvent error - VendorEventType not found in constList");
				}
		    }

			constantMap = new HashMap();

			Iterator constIter = constList.iterator();



			while(constIter.hasNext())
			{
				ConstType ct = (ConstType)constIter.next();
				constantMap.put(makeSearchConstantName(ct.getTypeName()), ct);

			}


	 }



}
