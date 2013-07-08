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
 * $Id: GadpIdleState.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import org.havi.system.types.GUID;

/**
 * The GUID Address Discovery Protocol idle state.  On entry to this state, the local GUID is set to zero.
 * All other events are ignored. This a GOF State Pattern concrete state
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class GadpIdleState extends GadpState
{
  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpState#enterState()
   */
  public void enterState()
  {
    // Initialize the local guid
    context.setLocalGuid(GUID.ZERO);

    // Disable timeout
    context.enableTimeout(false);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpState#leaveState()
   */
  public void leaveState()
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpState#timeout()
   */
  public void timeout()
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleNackGuid(GUID, int)
   */
  public void handleNackGuid(GUID guid, int stamp)
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleReserveGuid(GUID, int)
   */
  public void handleReserveGuid(GUID guid, int stamp)
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleTimeout()
   */
  public void handleTimeout()
  {
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "IDLE";
  }

}
