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
 * $Id: GadpState.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import org.havi.system.types.GUID;

/**
 * Base class for all GADP state.  This follows the GOF state pattern.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
abstract class GadpState implements GadpMessageHandler
{
  protected static GadpEngine context = null;

  /**
   * Set the context for fly weight states
   * @param context The state context
   */
  public final static void setContext(GadpEngine context)
  {
    // Save the context
    GadpState.context = context;
  }

  /**
   * Invoked when entering a new state.
   */
  public abstract void enterState();

  /**
   * Invoked when leave an existing state
   */
  public abstract void leaveState();


  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleNackGuid(GUID, int)
   */
  public abstract void handleNackGuid(GUID guid, int stamp);

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleReserveGuid(GUID, int)
   */
  public abstract void handleReserveGuid(GUID guid, int stamp);

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleTimeout()
   */
  public abstract void handleTimeout();

}
