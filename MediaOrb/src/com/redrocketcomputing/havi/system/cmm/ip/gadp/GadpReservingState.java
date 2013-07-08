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
 * $Id: GadpReservingState.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import org.havi.system.types.GUID;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * The GUID Address Discovery Protocol reserving state.  On entry a reserve GUID message is send of the GADP
 * socket.  The is a GOF State Pattern concrete state.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class GadpReservingState extends GadpState
{
  private GUID canidateGuid = GUID.ZERO;

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpState#enterState()
   */
  public void enterState()
  {
    try
    {
      // Enable the timeout ORDER IS IMPORTANT
      context.enableTimeout(true);

      // Get the first canidate guid
      canidateGuid = context.getNextCanidateGuid();

      // Send a reserve guid message
      context.sendGadpMessage(new GadpReserveGuidMessage(canidateGuid, context.getLocalTimeStamp()));
    }
    catch (GadpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "enterState", e.toString());

      // Change state to idle
      context.changeState(context.IDLE);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpState#leaveState()
   */
  public void leaveState()
  {
    // Set the local guid
    context.setLocalGuid(canidateGuid);

    // Release the canidate guid
    canidateGuid = null;
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleNackGuid(GUID, int)
   */
  public void handleNackGuid(GUID guid, int stamp)
  {
    try
    {
      // Check to see if this message is from ourselves
      if (canidateGuid.equals(guid) && context.getLocalTimeStamp() != stamp)
      {
        // Nope. somebody has claimed this GUID, lets try another one
        canidateGuid = context.getNextCanidateGuid();

        // Send a new reserve guid message
        context.sendGadpMessage(new GadpReserveGuidMessage(canidateGuid, context.getLocalTimeStamp()));
      }
      else
      {
        // Send our reserve again
        context.sendGadpMessage(new GadpReserveGuidMessage(canidateGuid, context.getLocalTimeStamp()));
      }
    }
    catch (GadpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "handleNackGuid", e.toString());

      // Change state to idle
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
      // Check to see if this guid match our canidate guid
      if (canidateGuid.equals(guid))
      {
        // Deteminate if we sent this message by matching the timestamp
        if (context.getLocalTimeStamp() == stamp)
        {
          // Ignore this message because we sent it
          return;
        }

        // This message is not from us, check to see if we should claim this GUID by comparing the stamps
        if (context.getLocalTimeStamp() <= stamp)
        {
          // We have the lowest timestamp, let NACK this message
          context.sendGadpMessage(new GadpNackGuidMessage(guid, context.getLocalTimeStamp()));

          // All done
          return;
        }

        // Uhmm, somebody has claim this GUID, lets try another one
        canidateGuid = context.getNextCanidateGuid();

        // Send a new reserve guid message
        context.sendGadpMessage(new GadpReserveGuidMessage(canidateGuid, context.getLocalTimeStamp()));
      }
    }
    catch (GadpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "handleReserveGuid", e.toString());

      // Change state to idle
      context.changeState(context.IDLE);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessageHandler#handleTimeout()
   */
  public void handleTimeout()
  {
    // All done
    context.changeState(context.READY);
  }
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "RESERVING";
  }

}
