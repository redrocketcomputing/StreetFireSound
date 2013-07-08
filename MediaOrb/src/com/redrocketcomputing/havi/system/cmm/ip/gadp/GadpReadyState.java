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
 * $Id: GadpReadyState.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import org.havi.system.types.GUID;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class GadpReadyState extends GadpState
{

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpState#enterState()
   */
  public void enterState()
  {
    // Disable timeout
    context.enableTimeout(false);

    // Fire ready event
    context.fireReadyEvent();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpState#leaveState()
   */
  public void leaveState()
  {
    // Nothing to do
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleNackGuid(GUID, int)
   */
  public void handleNackGuid(GUID guid, int stamp)
  {
    // Check to see if this message is from ourselves
    if (guid.equals(context.getLocalGuid()) && context.getLocalTimeStamp() != stamp)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "handleNackGuid", "found our own GUID in the ready state");

      // Change state to IDLE
      context.changeState(context.IDLE);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleReserveGuid(GUID, int)
   */
  public void handleReserveGuid(GUID guid, int stamp)
  {
    try
    {
      // Check to see if this GUID match ours
      if (guid.equals(context.getLocalGuid()))
      {
        // Check for match timestamp, this should never happen
        if (context.getLocalTimeStamp() == stamp)
        {
          // Log error
          LoggerSingleton.logError(this.getClass(), "handleReserveGuid", "found our own GUID in the ready state");

          // Change state to IDLE
          context.changeState(context.IDLE);
        }

        // Nack the message
        context.sendGadpMessage(new GadpNackGuidMessage(context.getLocalGuid(), context.getLocalTimeStamp()));
      }
    }
    catch (GadpException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "handleReserveGuid", e.toString());

      // Change state to IDLE
      context.changeState(context.IDLE);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleTimeout()
   */
  public void handleTimeout()
  {
    // Ignore
  }


  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "READY";
  }

}
