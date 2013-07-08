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
 * $Id: DcmCommandFactory.java,v 1.1 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.dcm;

import org.havi.dcm.rmi.DcmClient;

import com.redrocketcomputing.havi.commandcontroller.AbstractCommand;
import com.redrocketcomputing.havi.commandcontroller.AbstractCommandFactory;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class DcmCommandFactory implements AbstractCommandFactory
{
  private DcmClient client;
  
  /**
   * 
   */
  public DcmCommandFactory(DcmClient client)
  {
    // Check parameter
    if (client == null)
    {
      // Very bad
      throw new IllegalArgumentException("DcmClient is null");
    }
    
    // Save the parameter
    this.client = client;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.commandcontroller.AbstractCommandFactory#createCommand(java.lang.String, java.lang.Object)
   */
  public AbstractCommand createCommand(String key, Object parameter)
  {
    // Check key
    if (key.toUpperCase().equals("POWER"))
    {
      return new TogglePowerStateCommand(client);
    }
    
    // Not found
    return null;
  }

}
