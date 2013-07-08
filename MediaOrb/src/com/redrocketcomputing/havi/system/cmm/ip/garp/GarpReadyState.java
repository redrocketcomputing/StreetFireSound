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
 * $Id: GarpReadyState.java,v 1.2 2005/03/17 02:27:32 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.havi.system.types.GUID;

/**
 * Ready GARP protocol state. This is a concrete state in the GOF State pattern
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
public class GarpReadyState extends GarpState
{
  /**
   * Constructor for GarpReadyState.
   */
  public GarpReadyState(GarpEngine context)
  {
    super(context);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpState#enterState()
   */
  public void enterState()
  {
    // Change timeout to forever
    context.enableReadyTimeout(false);

    // Create gone devices array
    GUID[] goneDevices = (GUID[])context.getGoneGuids().toArray(new GUID[context.getGoneGuids().size()]);

    // Send the gone devices event
    context.fireGoneDevicesEvent(goneDevices);

    // Get the new devices array
    GUID[] newDevices = (GUID[])context.getNewGuids().toArray(new GUID[context.getNewGuids().size()]);

    // Send the new devices event
    context.fireNewDevicesEvent(newDevices);

    // Create the active and nonactive device lists
    List activeDevicesList = new ArrayList(context.getGuidMap().size());
    List nonactiveDevicesList = new ArrayList(context.getGuidMap().size());
    for (Iterator iterator = context.getGuidMap().entrySet().iterator(); iterator.hasNext();)
    {
      // Extract the element
      Map.Entry element = (Map.Entry) iterator.next();
      GUID key = (GUID)element.getKey();
      GarpEntry entry = (GarpEntry)element.getValue();

      // Add element to correct list
      if (entry.isActive())
      {
        activeDevicesList.add(key);
      }
      else
      {
        nonactiveDevicesList.add(key);
      }
    }

    // Create active devices array
    GUID[] activeDevices = (GUID[])activeDevicesList.toArray(new GUID[activeDevicesList.size()]);

    // Create nonactive devices array
    GUID[] nonactiveDevices = (GUID[])nonactiveDevicesList.toArray(new GUID[nonactiveDevicesList.size()]);

    // Save the active and non active device arrays
    context.setDeviceArrays(activeDevices, nonactiveDevices);

    // Send network ready event
    context.fireEventNetworkReadyEvent(newDevices, goneDevices, activeDevices, nonactiveDevices);

    // Flush the new and gone devices set
    context.getNewGuids().clear();
    context.getGoneGuids().clear();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpState#leaveState()
   */
  public void leaveState()
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#deviceInfo(GUID, long, boolean, byte[])
   */
  public void handleDeviceInfo(GUID guid, byte[] address, int port)
  {
    // Change state to reset
    context.changeState(context.RESET);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpMessageHandler#goneDevice(GUID)
   */
  public void handleGoneDevice(GUID guid)
  {
    // Change state to reset
    context.changeState(context.RESET);
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
    // Change state to reset
    context.changeState(context.RESET);
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "READY";
  }

}
