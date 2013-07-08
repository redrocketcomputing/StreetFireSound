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
 * $Id: TogglePowerStateCommand.java,v 1.1 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.dcm;

import org.havi.dcm.rmi.DcmClient;
import org.havi.system.types.HaviException;

import com.redrocketcomputing.havi.commandcontroller.AbstractCommand;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class TogglePowerStateCommand implements AbstractCommand
{
  private DcmClient client;
  
  /**
   * Construct a TogglePowerStateCommand
   */
  public TogglePowerStateCommand(DcmClient client)
  {
    // Save the client
    this.client = client;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.commandcontroller.AbstractCommand#execute()
   */
  public void execute() throws HaviException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "execute", "toggling power state");
    
    // Toggle the power state
    client.setPowerStateSync(0, !client.getPowerStateSync(0));
  }
}
