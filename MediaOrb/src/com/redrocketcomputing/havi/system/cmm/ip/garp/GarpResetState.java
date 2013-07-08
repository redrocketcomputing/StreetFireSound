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
 * $Id: GarpResetState.java,v 1.2 2005/03/17 02:27:32 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import java.util.Iterator;
import java.util.Map;

import org.havi.system.types.GUID;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Reset GARP protocol state. This is a concrete state in the GOF State pattern
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
public class GarpResetState extends GarpState
{
  private GarpDeviceInfoMessage deviceInfoMessage;

  /**
   * Constructor for GarpResetState.
   * @param context
   */
  public GarpResetState(GarpEngine context)
  {
    super(context);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpState#enterState()
   */
  public void enterState()
  {
    try
    {
      // Update the reset counter
      context.incrementResetCounter();

      // Create the initial gone GUIDs list from all active GUIDS
      for (Iterator iterator = context.getGuidMap().entrySet().iterator(); iterator.hasNext();)
      {
        // Extract the element
        Map.Entry element = (Map.Entry)iterator.next();
        GUID elementGuid = (GUID)element.getKey();
        GarpEntry elementGarpEntry = (GarpEntry)element.getValue();

        // Add to the gone set if the GUID is marked as active
        if (elementGarpEntry.isActive())
        {
          // Add active element to the GUID set
          context.getGoneGuids().add(elementGuid);
        }
      }

      // Get the local GUID GARP Entry
      GarpEntry localGarpEntry = (GarpEntry)context.getGuidMap().get(context.getLocalGuid());

      // Build device info message
      deviceInfoMessage = new GarpDeviceInfoMessage(context.getLocalGuid(), localGarpEntry.getAddress(), localGarpEntry.getPort());

      // Set timeout THE ORDER OF THE NEXT TWO AND THE SEND ARE IMPORTANT!!!!!
      context.enableReadyTimeout(true);

      // Send the message
      context.sendGarpMessage(deviceInfoMessage);

      // Fire the network reset envent
      context.fireNetworkResetEvent();
    }
    catch (GarpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "enterState", e.toString());

      // Change state to idle
      context.changeState(context.IDLE);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpState#leaveState()
   */
  public void leaveState()
  {
    // Loop throught the gone list and set active static to false
    for (Iterator iter = context.getGoneGuids().iterator(); iter.hasNext();)
    {
      // Extract the guid
      GUID element = (GUID) iter.next();

      // Change to active state to false
      GarpEntry entry = (GarpEntry)context.getGuidMap().get(element);
      entry.setActive(false);
    }

    // Release the device info message
    deviceInfoMessage = null;
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#deviceInfo(GUID, long, boolean, byte[])
   */
  public void handleDeviceInfo(GUID guid, byte[] address, int port)
  {
    try
    {
      // Try to get the GARP entry
      GarpEntry garpEntry = (GarpEntry)context.getGuidMap().get(guid);
      if (garpEntry == null)
      {
        // Add to the new devices set
        context.getNewGuids().add(guid);

        // Send the device information message again because this is an new entry
        context.sendGarpMessage(deviceInfoMessage);
      }
      else
      {
        // Remove the the gone set because this is an existing GUID
        context.getGoneGuids().remove(guid);

        // If this is not an active device, than add it to the new devices list except where the guid is the local
        if (!garpEntry.isActive() && !context.getLocalGuid().equals(guid))
        {
          // Add to the new device list
          context.getNewGuids().add(guid);

          // Send the device information message again because this is an inactive entry
          context.sendGarpMessage(deviceInfoMessage);
        }
      }

      // Update the device map
      context.getGuidMap().put(guid, new GarpEntry(address, port, true));
    }
    catch (GarpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "handleDeviceInfo", e.toString());

      // Change state to idle
      context.changeState(context.IDLE);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#goneDevice(GUID)
   */
  public void handleGoneDevice(GUID guid)
  {
    // Check for local guid
    if (context.getLocalGuid().equals(guid))
    {
      // We must be quiting
      context.changeState(context.IDLE);

      // All done
      return;
    }

    // Add to the gone set
    context.getGoneGuids().add(guid);

    // Remove from the new set THIS SHOULD NOT HAPPEN
    context.getNewGuids().remove(guid);

    // Set the active state to false
    GarpEntry entry = (GarpEntry)context.getGuidMap().get(guid);
    entry.setActive(false);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#timeout()
   */
  public void handleTimeout()
  {
    // Change state to ready
    context.changeState(context.READY);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#handleResetNetwork(GUID)
   */
  public void handleResetNetwork(GUID guid)
  {
    // Change to reset state again
    context.changeState(context.RESET);
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "RESETTING";
  }
}
