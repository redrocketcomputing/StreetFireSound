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
 * $Id: DestinationSeidTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.transfer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.havi.system.types.GUID;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener;
import com.redrocketcomputing.havi.system.ms.event.NetworkResetEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener;
import com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener;

/**
 * @author stephen
 *
 */
public class DestinationSeidTable implements SeidLeaveEventListener, SeidTimeoutEventListener, SeidErrorEventListener, SystemReadyEventListener, NetworkResetEventListener, NetworkReadyEventListener
{
  private Map table = new HashMap();
  private EventDispatch eventDispatcher;

  /**
   * Constructor for DestinationSeidTable.
   */
  public DestinationSeidTable()
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
   * Search the table for the specified SEID, create one if it does not exist
   * @param seid The SEID to search for
   * @return DestinationSeid The table entry
   */
  public synchronized DestinationSeid get(SEID seid)
  {
    // Check the seid
    if (seid == null)
    {
      // Bad
      throw new IllegalArgumentException("seid can not be null");
    }

    // Check to see if the SEID is already in the table
    DestinationSeid entry = (DestinationSeid)table.get(seid);
    if (entry == null)
    {
      // Create one
      entry = new DestinationSeid(seid);

      // Add to the table
      table.put(seid, entry);
    }

    // Ask the map
    return (DestinationSeid)table.get(seid);
  }

  /**
   * Remove the specified SEID from the table
   * @param seid The SEID to remove
   * @return DestinationSeid The table entry remove or null if not found
   */
  public synchronized DestinationSeid remove(SEID seid)
  {
    // Check the seid
    if (seid == null)
    {
      // Bad
      throw new IllegalArgumentException("seid can not be null");
    }

    // Ask the map
    return (DestinationSeid)table.remove(seid);
  }
  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener#seidLeaveEvent(SEID)
   */
  public void seidLeaveEvent(SEID seid)
  {
    // Flush the seid
    remove(seid);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener#seidTimeoutEvent(SEID)
   */
  public void seidTimeoutEvent(SEID seid)
  {
    // Lookup the remote SEID
    DestinationSeid remoteSeid = get(seid);
    if (remoteSeid != null)
    {
      // Force unreachable state
      remoteSeid.unreachable();
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener#seidErrorEvent(SEID)
   */
  public void seidErrorEvent(SEID seid)
  {
    // Lookup the remote SEID
    DestinationSeid remoteSeid = get(seid);
    if (remoteSeid != null)
    {
      // Force unreachable state
      remoteSeid.unreachable();
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener#systemReadyEvent(SEID)
   */
  public void systemReadyEvent(SEID seid)
  {
    // Lookup the remote SEID
    DestinationSeid remoteSeid = get(seid);
    if (remoteSeid != null)
    {
      // Reset the error state
      remoteSeid.reset();
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.NetworkResetEventListener#networkResetEvent()
   */
  public synchronized void networkResetEvent()
  {
    // Loop through the map and reset all entries
    for (Iterator iterator = table.values().iterator(); iterator.hasNext();)
    {
      // Extract the element
      DestinationSeid element = (DestinationSeid) iterator.next();

      // Reset the element
      element.reset();
    }
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

//    // Loop through the map and reset all remaining entries
//    for (Iterator iterator = table.values().iterator(); iterator.hasNext();)
//    {
//      // Extract the element
//      DestinationSeid element = (DestinationSeid) iterator.next();
//
//      // Reset the element
//      element.reset();
//    }
  }
}
