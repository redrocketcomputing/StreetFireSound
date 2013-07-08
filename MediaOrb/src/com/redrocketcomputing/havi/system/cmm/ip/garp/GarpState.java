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
 * $Id: GarpState.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import org.havi.system.types.GUID;

/**
 * Base class for all GarpState classes. This is the abstract state in the GOF State Pattern.
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
abstract class GarpState implements GarpMessageHandler
{
  protected GarpEngine context;

  /**
   * Constructor for GarpState. Let only subclass construct
   * @param context The GOF State context
   */
  protected GarpState(GarpEngine context)
  {
    this.context = context;
  }

  /**
   * Invoked when the subclass concrete state will become the current state
   */
  public abstract void enterState();

  /**
   * Invoked when the subclass concrete state is no longer the current state
   */
  public abstract void leaveState();

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleDeviceInfo(GUID, byte[], int)
   */
  public abstract void handleDeviceInfo(GUID guid, byte[] address, int port);

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleGoneDevice(GUID)
   */
  public abstract void handleGoneDevice(GUID guid);

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleTimeout()
   */
  public abstract void handleTimeout();

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleResetNetwork(GUID)
   */
  public abstract void handleResetNetwork(GUID guid);
}
