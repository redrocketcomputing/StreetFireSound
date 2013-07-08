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
 * $Id: SeidWatcherTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms;

import gnu.trove.TLinkedList;

import java.util.Iterator;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgSuperExistsException;
import org.havi.system.types.HaviMsgUnknownException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener;
import com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
class SeidWatcherTable implements SeidLeaveEventListener, SeidErrorEventListener, SeidTimeoutEventListener, SystemReadyEventListener, NetworkReadyEventListener
{
  private TLinkedList table = new TLinkedList();
  private EventDispatch eventDispatcher;
  private MessagingSystem parent;
  private SEID localSeid;

  /**
   * Constructor for SeidWatcherTable.
   */
  public SeidWatcherTable(MessagingSystem parent, SEID localSeid)
  {
    // Check parameters
    if (parent == null || localSeid == null)
    {
      // Bad stuff
      throw new IllegalArgumentException("parameter is null");
    }

    // Save parameters
    this.parent = parent;
    this.localSeid = localSeid;

    // Get the event dispatch service
    eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
    if (eventDispatcher == null)
    {
      // Very bad
      throw new IllegalStateException("can not find event dispatch service");
    }

    // Register for event
    eventDispatcher.addListener(this);
  }

  /**
   * Add a watcher to the internal table.
   * @param sourceSeid The source SEID of the watcher
   * @param destinationSeid The SEID to watch
   * @param opCode The operation code to use for notifications
   * @param callback The callback of the source SEID
   * @throws HaviMsgSuperExistsException Thrown if the SEID already has a supervision on the destination SEID
   */
  public synchronized void addWatcher(SEID sourceSeid, SEID destinationSeid, OperationCode opCode) throws HaviMsgSuperExistsException
  {
    // Check the parameters
    if (sourceSeid == null || destinationSeid == null || opCode == null)
    {
      // Bad stuff
      throw new IllegalArgumentException("parameter is null");
    }

    // Check to see if an entry already exists
    if (find(sourceSeid, destinationSeid, opCode) != null)
    {
      // Opps
      throw new HaviMsgSuperExistsException(destinationSeid.toString());
    }

    // Create and add a new one
    table.add(new SeidWatcher(sourceSeid, destinationSeid, opCode));
  }

  /**
   * Remove the specified watcher.
   * @param sourceSeid The source SEID of the watcher
   * @param destinationSeid The target of the watcher
   * @param opCode The operation code of the notifications
   * @param callback The callback of the source SEID
   * @throws HaviMsgUnknownException Thrown if the watcher is not found.
   */
  public synchronized void removeWatcher(SEID sourceSeid, SEID destinationSeid, OperationCode opCode) throws HaviMsgUnknownException
  {
    // Check the parameters
    if (sourceSeid == null || destinationSeid == null || opCode == null)
    {
      // Bad stuff
      throw new IllegalArgumentException("parameter is null");
    }

    // Lookup matching entry
    SeidWatcher entry = find(sourceSeid, destinationSeid, opCode);
    if (entry == null)
    {
      // Opps
      throw new HaviMsgUnknownException(destinationSeid.toString());
    }

    // Remove the entry
    table.remove(entry);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener#seidLeaveEvent(SEID)
   */
  public synchronized void seidLeaveEvent(SEID seid)
  {
    // Loop through the table
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract the element
      SeidWatcher element = (SeidWatcher) iterator.next();

      // Check match source to remove the entry
      if (element.getSourceSeid().equals(seid))
      {
        // Remove the element
        iterator.remove();
      }
      // Look for dispatch match
      else if (element.getDestinationSeid().equals(seid))
      {
        // Remove the element
        iterator.remove();

        // Dispatch this one
        dispatchWatcher(element);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener#seidErrorEvent(SEID)
   */
  public synchronized void seidErrorEvent(SEID seid)
  {
    // Loop through the table
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract the element
      SeidWatcher element = (SeidWatcher) iterator.next();

      // Check match source to remove the entry
      if (element.getSourceSeid().equals(seid))
      {
        // Remove the element
        iterator.remove();
      }
      // Look for dispatch match
      else if (element.getDestinationSeid().equals(seid))
      {
        // Remove the element
        iterator.remove();

        // Dispatch this one
        dispatchWatcher(element);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener#seidTimeoutEvent(SEID)
   */
  public synchronized void seidTimeoutEvent(SEID seid)
  {

    // Loop through the table
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {

      // Extract the element
      SeidWatcher element = (SeidWatcher) iterator.next();

      // Check match source to remove the entry
      if (element.getSourceSeid().equals(seid))
      {
        // Remove the element
        iterator.remove();
      }

      // Look for dispatch match
      else if (element.getDestinationSeid().equals(seid))
      {
        // Remove the element
        iterator.remove();

        // Dispatch this one
        dispatchWatcher(element);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener#systemReadyEvent(SEID)
   */
  public synchronized void systemReadyEvent(SEID seid)
  {
    // Loop through the table
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract the element
      SeidWatcher element = (SeidWatcher) iterator.next();

      // Look for dispatch match
      if (element.getDestinationSeid().equals(seid))
      {
        // Remove the element
        iterator.remove();

        // Dispatch this one
        dispatchWatcher(element);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener#networkReadyEvent(GUID[], GUID[], GUID[], GUID[])
   */
  public synchronized void networkReadyEvent(GUID[] activeDevices, GUID[] nonactiveDevices, GUID[] newDevices, GUID[] goneDevices)
  {
    // Loop through the gone devices
    for (int i = 0; i < goneDevices.length; i++)
    {
      // Loop through the table
      for (Iterator iterator = table.iterator(); iterator.hasNext();)
      {
        // Extract the element
        SeidWatcher element = (SeidWatcher) iterator.next();

        // Look for dispatch match
        if (element.getDestinationSeid().getGuid().equals(goneDevices[i]))
        {
	        // Remove the element
	        iterator.remove();

          // Dispatch this one
          dispatchWatcher(element);
        }
      }
    }
  }

  /**
   * Search the table for a matching entry
   * @param sourceSeid The source SEID to match
   * @param destinationSeid The destination SEID to match
   * @param opCode The operation code to match
   * @param callback The call back to match
   * @return SeidWatcher The matching entry or null if not found
   */
  private SeidWatcher find(SEID sourceSeid, SEID destinationSeid, OperationCode opCode)
  {
    // Loop through table looking for match
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract element
      SeidWatcher element = (SeidWatcher) iterator.next();

      // Check for match
      if (element.getSourceSeid().equals(sourceSeid) && element.getDestinationSeid().equals(destinationSeid) && element.getOpCode().equals(opCode))
      {
        // Matched
        return element;
      }
    }

    // Not found
    return null;
  }

  /**
   * Dispatch message to the callback
   * @param watcher The watcher to dispatch supervision to
   */
  private void dispatchWatcher(SeidWatcher watcher)
  {
    try
    {
      // Dispatch message
      // Use async call instead.  Couldn't care less about the response.
      // Besides, no response can be recieved from the same software element when sendRequestSync blocks.
      // Always sending the notification to itself.
      parent.sendRequest(localSeid, watcher.getSourceSeid(), watcher.getOpCode(), watcher.getDestinationSeid().getValue());
    }
    catch (HaviMsgException e)
    {
      // Logger error
      LoggerSingleton.logError(this.getClass(), "dispatchWatcher", e.toString());
    }
  }
}
