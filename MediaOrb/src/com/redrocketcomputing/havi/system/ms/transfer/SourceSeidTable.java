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
 * $Id: SourceSeidTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.transfer;

import java.util.HashMap;
import java.util.Map;

import org.havi.system.types.GUID;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener;

/**
 * @author stephen
 *
 */
class SourceSeidTable implements SeidLeaveEventListener, NetworkReadyEventListener
{
  private Map table = new HashMap();
  private int maxOutstanding;
  private EventDispatch eventDispatcher;

  /**
   * Constructor for SourceSeidTable.
   */
  public SourceSeidTable(int maxOutstanding)
  {
    // Range check the max outstanding
    if (maxOutstanding <0 || maxOutstanding > 255)
    {
      // Bad configuration
      throw new IllegalArgumentException("bad max outstanding: " + maxOutstanding);
    }

    // Save the parameters
    this.maxOutstanding = maxOutstanding;

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
   * Search the table for the specified SEID, create one if it does not exist
   * @param seid The SEID to search for
   * @return SourceSeid The table entry
   */
  public synchronized SourceSeid get(SEID seid)
  {
    // Check the seid
    if (seid == null)
    {
      // Bad
      throw new IllegalArgumentException("seid can not be null");
    }

    // Check to see if the SEID is already in the table
    SourceSeid entry = (SourceSeid)table.get(seid);
    if (entry == null)
    {
      // Create one
      entry = new SourceSeid(seid, maxOutstanding);

      // Add to the table
      table.put(seid, entry);
    }

    // Return the local seid entry
    return entry;
  }

  /**
   * Remove the specified SEID from the table
   * @param seid The SEID to remove
   * @return SourceSeid The table entry remove or null if not found
   */
  public synchronized SourceSeid remove(SEID seid)
  {
    // Check the seid
    if (seid == null)
    {
      // Bad
      throw new IllegalArgumentException("seid can not be null");
    }

    // Ask the map
    return (SourceSeid)table.remove(seid);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener#seidLeaveEvent(SEID)
   */
  public void seidLeaveEvent(SEID seid)
  {
    // Remove the seid
    remove(seid);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener#networkReadyEvent(GUID[], GUID[], GUID[], GUID[])
   */
  public synchronized void networkReadyEvent(GUID[] activeDevices, GUID[] nonactiveDevices, GUID[] newDevices, GUID[] goneDevices)
  {
    // Get an array of SEID in the table
    SEID[] seids = (SEID[])table.keySet().toArray(new SEID[table.size()]);

    // Loop through all the gone devices
    for (int i = 0; i < goneDevices.length; i++)
    {
      // Loop through the seids
      for (int j = 0; j < seids.length; j++)
      {
        // Check for match
        if (seids[j].getGuid().equals(goneDevices[i]))
        {
          // Remote the matching seid
          table.remove(seids[j]);
        }
      }
    }
  }

}
