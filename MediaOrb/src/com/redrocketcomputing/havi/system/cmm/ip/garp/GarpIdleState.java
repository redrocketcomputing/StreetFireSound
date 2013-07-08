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
 * $Id: GarpIdleState.java,v 1.2 2005/03/17 02:27:32 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import org.havi.system.types.GUID;

/**
 * The Idle concrete GarpState.  All events are ignored in this state
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
public class GarpIdleState extends GarpState
{

  /**
   * Constructor for GarpIdleState.
   */
  public GarpIdleState(GarpEngine context)
  {
    super(context);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpState#enterState()
   */
  public void enterState()
  {
    // Wait forever
    context.enableReadyTimeout(false);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpState#leaveState()
   */
  public void leaveState()
  {
    // Ignore
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "IDLE";
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleDeviceInfo(GUID, byte[], int)
   */
  public void handleDeviceInfo(GUID guid, byte[] address, int port)
  {
    // Ignore
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleGoneDevice(GUID)
   */
  public void handleGoneDevice(GUID guid)
  {
    // Ignore
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleTimeout()
   */
  public void handleTimeout()
  {
    // Ignore
  }
  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleResetNetwork(GUID)
   */
  public void handleResetNetwork(GUID guid)
  {
    // Force to the reset state
    context.changeState(context.RESET);
  }

}
