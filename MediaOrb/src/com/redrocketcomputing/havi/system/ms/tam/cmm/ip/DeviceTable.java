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
 * $Id: DeviceTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.tam.cmm.ip;

import java.util.HashMap;
import java.util.Map;

import org.havi.system.types.GUID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener;

/**
 * @author stephen
 *
 */
class DeviceTable implements NetworkReadyEventListener
{
  private Map table = new HashMap();
  private EventDispatch eventDispatcher;

  /**
   * Constructor for DeviceTable.
   */
  public DeviceTable()
  {
    // Get the event dispatcher
    eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
    if (eventDispatcher == null)
    {
      // Badness
      throw new IllegalStateException("can not find event dispatch service");
    }

    // Register with the event dispatcher
    eventDispatcher.addListener(this);
  }

  /**
   * Search the table for the specified GUID, create one if it does not exist
   * @param guid The GUID to search for
   * @return Device The table entry
   */
  public synchronized Device get(GUID guid)
  {
    // Check the guid
    if (guid == null)
    {
      // Bad
      throw new IllegalArgumentException("guid can not be null");
    }

    // Check to see if the GUID is already in the table
    Device entry = (Device)table.get(guid);
    if (entry == null)
    {
      // Create one
      entry = new Device(guid);

      // Add to the table
      table.put(guid, entry);
    }

    // Return the local seid entry
    return entry;
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener#networkReadyEvent(GUID[], GUID[], GUID[], GUID[])
   */
  public synchronized void networkReadyEvent(GUID[] activeDevices, GUID[] nonactiveDevices, GUID[] newDevices, GUID[] goneDevices)
  {
    // Loop through all the gone devices
    for (int i = 0; i < goneDevices.length; i++)
    {
      // Try to remove from the table
      Device device = (Device)table.remove(goneDevices[i]);
      if (device != null)
      {
        // Interrupt all waiters
        device.interrupt();
      }
    }
  }
}
