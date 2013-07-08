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
 * $Id: WriteInterfaceMessageCallBack.java,v 1.2 2005/02/24 03:03:36 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;


import java.io.IOException;
import java.util.Iterator;

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
public class WriteInterfaceMessageCallBack {

  InterfaceType it;

  /**
   * Constructor for WriteInterfaceSystemEvent.
   */
  public WriteInterfaceMessageCallBack(InterfaceType it) throws IOException
  {
    super();
    this.it = it;

    outputToFile();
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

            new WriteCallBackListener(ft);	//ok

			new WriteCallBackClient(ft);

			new WriteCallBackHelper(ft);

			new WriteCallBackInvocation(ft);

        }


  }


}