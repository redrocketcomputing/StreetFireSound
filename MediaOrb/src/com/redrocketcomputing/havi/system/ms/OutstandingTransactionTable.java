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
 * $Id: OutstandingTransactionTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms;

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
class OutstandingTransactionTable implements SeidLeaveEventListener, SeidTimeoutEventListener, SeidErrorEventListener, SystemReadyEventListener, NetworkReadyEventListener
{
  private final static Status ACK = new Status(ConstApiCode.MSG, ConstMsgErrorCode.ACK);
  private final static Status DEST_SEID = new Status(ConstApiCode.MSG, ConstMsgErrorCode.DEST_SEID);
  private final static Status DEST_UNREACHABLE = new Status(ConstApiCode.MSG, ConstMsgErrorCode.DEST_UNREACHABLE);
  private final static Status SEND = new Status(ConstApiCode.MSG, ConstMsgErrorCode.SEND);

  private long nextTransactionId = 0;
  private TLinkedList table = new TLinkedList();
  private EventDispatch eventDispatcher;

  /**
   * Constructor for OutstandingTransactionTable.
   */
  public OutstandingTransactionTable()
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

  /**
   * Create new outstanding transaction and add to outstanding table
   * @param sourceSeid The source SEID for the transaction
   * @param destinationSeid The destination SEID for the transaction
   * @return OutstandingTransaction The new outstand transaction
   */
  public synchronized OutstandingTransaction allocateSync(SEID sourceSeid, SEID destinationSeid)
  {
    // Check the parameters
    if (sourceSeid == null || destinationSeid == null)
    {
      // BAD
      throw new IllegalArgumentException("seids can not be null");
    }

    // Create new transaction
    OutstandingTransaction outstanding = new OutstandingTransaction(sourceSeid, destinationSeid, (int)(nextTransactionId & 0xffffffff));

    // Update the next transaction id
    nextTransactionId++;

    // Add to the table
    table.add(outstanding);

    // Return the new transaction
    return outstanding;
  }

  /**
   * Create new outstand transaction but do not add to the table
   * @param sourceSeid The source SEID for the transaction
   * @param destinationSeid The destination SEID for the transaction
   * @return OutstandingTransaction The new outstand transaction
   */
  public synchronized OutstandingTransaction allocate(SEID sourceSeid, SEID destinationSeid)
  {
    // Check the parameters
    if (sourceSeid == null || destinationSeid == null)
    {
      // BAD
      throw new IllegalArgumentException("seids can not be null");
    }

    // Create new transaction
    OutstandingTransaction outstanding = new OutstandingTransaction(sourceSeid, destinationSeid, (int)(nextTransactionId & 0xffffffff));

    // Update the next transaction id
    nextTransactionId++;

    // Return the new transaction
    return outstanding;
  }

  /**
   * Release the specified transaction
   * @param outstanding The transaction to release
   */
  public synchronized void releaseSync(OutstandingTransaction outstanding)
  {
    // Check parameters
    if (outstanding == null)
    {
      // MORE BAD
      throw new IllegalArgumentException("outstanding can not be null");
    }

    // Remove the transaction
    table.remove(outstanding);
  }

  /**
   * Release the specified transaction
   * @param outstanding The transaction to release
   */
  public synchronized void release(OutstandingTransaction outstanding)
  {
    // Do nothing.
    // Just make the API looks symmetric
  }

  /**
   * Lookup the transaction using the specified transaction ID
   * @param transactionId The transaction ID to use for searching the internal table
   * @return OutstandingTransaction The matched outstanding transaction or null if not found
   */
  public synchronized OutstandingTransaction get(int transactionId)
  {
    // Loop through the table and return it
    for (Iterator iteration = table.iterator(); iteration.hasNext();)
    {
      // Extract the type
      OutstandingTransaction element = (OutstandingTransaction)iteration.next();

      // Check for match
      if (element.getTransactionId() == transactionId)
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
      OutstandingTransaction element = (OutstandingTransaction)iterator.next();

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
      OutstandingTransaction element = (OutstandingTransaction) iterator.next();

      // Check for match
      if (element.getDestinationSeid().equals(seid))
      {
        // Post a destination unreachable
        element.postStatus(DEST_UNREACHABLE);
      }
    }
  }

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
      OutstandingTransaction element = (OutstandingTransaction) iterator.next();

      if (element.getDestinationSeid().equals(seid))
      {
        // Post a destination unreachable
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
      OutstandingTransaction element = (OutstandingTransaction) iterator.next();

      if (element.getDestinationSeid().equals(seid))
      {
        // Post a ACK status
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
        OutstandingTransaction element = (OutstandingTransaction) iterator.next();

        if (element.getDestinationSeid().getGuid().equals(goneDevices[i]))
        {
        // Post a destination unreachable
          element.postStatus(DEST_UNREACHABLE);
        }
      }
    }
  }
}
