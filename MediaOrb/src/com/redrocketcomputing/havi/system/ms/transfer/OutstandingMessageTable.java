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
 * $Id: OutstandingMessageTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.transfer;

import gnu.trove.TLinkedList;

import java.util.Iterator;

import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstMsgErrorCode;
import org.havi.system.types.GUID;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener;
import com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener;

/**
 * @author stephen
 *
 */
public class OutstandingMessageTable implements SeidLeaveEventListener, SeidTimeoutEventListener, SeidErrorEventListener, SystemReadyEventListener, NetworkReadyEventListener
{
  private final static Status ACK = new Status(ConstApiCode.MSG, ConstMsgErrorCode.ACK);
  private final static Status DEST_SEID = new Status(ConstApiCode.MSG, ConstMsgErrorCode.DEST_SEID);
  private final static Status DEST_UNREACHABLE = new Status(ConstApiCode.MSG, ConstMsgErrorCode.DEST_UNREACHABLE);
  private final static Status SEND = new Status(ConstApiCode.MSG, ConstMsgErrorCode.SEND);

  private TLinkedList table = new TLinkedList();
  private EventDispatch eventDispatcher;

  /**
   * Constructor for OutstandingMessageTable.
   */
  public OutstandingMessageTable()
  {
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

  public synchronized OutstandingMessage allocate(SEID sourceSeid, SEID destinationSeid, int messageNumber)
  {
    // Check paremeters
    if (sourceSeid == null || destinationSeid == null)
    {
      throw new IllegalArgumentException("one of the seids is null");
    }

    // Check message number
    if (messageNumber < 0 || messageNumber > 255)
    {
      throw new IllegalArgumentException("message number is out of range: " + messageNumber);
    }

    // Allocate the new entry
    OutstandingMessage entry = new OutstandingMessage(sourceSeid, destinationSeid, messageNumber);

    // Add to the list
    table.add(entry);

    // All done
    return entry;
  }

  public synchronized void release(OutstandingMessage entry)
  {
    // Check parametes
    if (entry == null)
    {
      throw new IllegalArgumentException("entry is null");
    }

    // Remove from the table
    table.remove(entry);
  }

  public synchronized OutstandingMessage get(SEID sourceSeid, SEID destinationSeid, int messageNumber)
  {
    // Check paremeters
    if (sourceSeid == null || destinationSeid == null)
    {
      throw new IllegalArgumentException("one of the seids is null");
    }

    // Check message number
    if (messageNumber < 0 || messageNumber > 255)
    {
      throw new IllegalArgumentException("message number is out of range: " + messageNumber);
    }

    // Loop through the table look for a matching entry
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract element
      OutstandingMessage element = (OutstandingMessage) iterator.next();

      // Check for match
      if (element.getSourceSeid().equals(sourceSeid) && element.getDestinationSeid().equals(destinationSeid) && element.getMessageNumber() == messageNumber)
      {
        // Found it
        return element;
      }
    }

    // Not found
    return null;
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener#seidLeaveEvent(SEID)
   */
  public synchronized void seidLeaveEvent(SEID seid)
  {
    // Check paremeters
    if (seid == null)
    {
      throw new IllegalArgumentException("seid is null");
    }

    // Loop through the table look for a matching entry
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract element
      OutstandingMessage element = (OutstandingMessage) iterator.next();

      // Check for match
      if (element.getSourceSeid().equals(seid))
      {
        // Post a SEND status
        element.postStatus(SEND);
      }
      else if (element.getDestinationSeid().equals(seid))
      {
        // Post a DEST_SEID status
        element.postStatus(DEST_SEID);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener#seidTimeoutEvent(SEID)
   */
  public synchronized void seidTimeoutEvent(SEID seid)
  {
    // Check paremeters
    if (seid == null)
    {
      throw new IllegalArgumentException("seid is null");
    }

    // Loop through the table look for a matching entry
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract element
      OutstandingMessage element = (OutstandingMessage) iterator.next();

      // Check for match
      if (element.getDestinationSeid().equals(seid))
      {
        // Post a DEST_SEID status
        element.postStatus(DEST_UNREACHABLE);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener#seidErrorEvent(SEID)
   */
  public synchronized void seidErrorEvent(SEID seid)
  {
    // Check paremeters
    if (seid == null)
    {
      throw new IllegalArgumentException("seid is null");
    }

    // Loop through the table look for a matching entry
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract element
      OutstandingMessage element = (OutstandingMessage) iterator.next();

      if (element.getDestinationSeid().equals(seid))
      {
        // Post a DEST_SEID status
        element.postStatus(DEST_UNREACHABLE);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener#systemReadyEvent(SEID)
   */
  public synchronized void systemReadyEvent(SEID seid)
  {
    // Check paremeters
    if (seid == null)
    {
      throw new IllegalArgumentException("seid is null");
    }

    // Loop through the table look for a matching entry
    for (Iterator iterator = table.iterator(); iterator.hasNext();)
    {
      // Extract element
      OutstandingMessage element = (OutstandingMessage) iterator.next();

      if (element.getDestinationSeid().equals(seid))
      {
        // Post a DEST_SEID status
        element.postStatus(ACK);
      }
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener#networkReadyEvent(GUID[], GUID[], GUID[], GUID[])
   */
  public synchronized void networkReadyEvent(GUID[] activeDevices, GUID[] nonactiveDevices, GUID[] newDevices, GUID[] goneDevices)
  {
    for (int i = 0; i < goneDevices.length; i++)
    {
      // Loop through the table look for a matching entry
      for (Iterator iterator = table.iterator(); iterator.hasNext();)
      {
        // Extract element
        OutstandingMessage element = (OutstandingMessage) iterator.next();

        if (element.getDestinationSeid().getGuid().equals(goneDevices[i]))
        {
          // Post a DEST_SEID status
          element.postStatus(DEST_UNREACHABLE);
        }
      }
    }
  }
}
