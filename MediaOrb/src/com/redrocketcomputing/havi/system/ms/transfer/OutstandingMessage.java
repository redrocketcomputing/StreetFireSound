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
 * $Id: OutstandingMessage.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.transfer;

import gnu.trove.TLinkableAdaptor;

import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstMsgErrorCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.concurrent.Slot;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
class OutstandingMessage extends TLinkableAdaptor
{
  private final static Status ACK = new Status(ConstApiCode.MSG, ConstMsgErrorCode.ACK);

  private SEID sourceSeid;
  private SEID destinationSeid;
  private int messageNumber;
  private Slot slot = new Slot();

  /**
   * Constructor
   * @param sourceSeid The local SEID for this entry
   * @param destinationSeid The remote SEID for this entry
   * @param messageNumber The message number for the entry
   */
  public OutstandingMessage(SEID sourceSeid, SEID destinationSeid, int messageNumber)
  {
    // Construct super class
    super();

    // Check the parameters
    if (sourceSeid == null || destinationSeid == null)
    {
      // Bad thing are happening
      throw new IllegalArgumentException("seids can not be null");
    }

    // Check the message number
    if (messageNumber < 0 || messageNumber > 255)
    {
      throw new IllegalArgumentException("message number is out of range: " + messageNumber);
    }

    // Save the parameters
    this.sourceSeid = sourceSeid;
    this.destinationSeid = destinationSeid;
    this.messageNumber = messageNumber;
  }

  /**
   * Wait for status from the remote end
   * @param timeout The amount of time in millseconds to wait
   * @return Status The wait result
   */
  public Status waitForStatus(long timeout)
  {
    try
    {
      // Wait for the status
      Status status = (Status)slot.poll(timeout);

      // Check for timeout
      if (status == null)
      {
        // Log error 
        LoggerSingleton.logError(this.getClass(), "waitForStatus", timeout + "ms timer expired, returning ACK");
        
        // Use ACK status
        status = ACK;
      }

      // Return the status
      return status;
    }
    catch (InterruptedException e)
    {
      // Clear thread interrupted status
      Thread.currentThread().interrupted();

      // Return ACK status
      return ACK;
    }
  }

  /**
   * Post a status to the waiter
   * @param status The status to post
   */
  public void postStatus(Status status)
  {
    try
    {
      // Try to post the status
      if (!slot.offer(status, 0))
      {
        // Log error
        LoggerSingleton.logWarning(this.getClass(), "postStatus", "problem returning " + status + " to " + sourceSeid + " from " + destinationSeid);
      }
    }
    catch (InterruptedException e)
    {
      // Clear thread interrupted status
      Thread.currentThread().interrupted();

      // Log error
      LoggerSingleton.logError(this.getClass(), "postStatus", "interrupted returning " + status + " to " + sourceSeid + " from " + destinationSeid);
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    // Return to stuff
    return "OutstandingMessage(" + messageNumber + ": " + sourceSeid + "->" + destinationSeid;
  }

  /**
   * Returns the sourceSeid.
   * @return SEID
   */
  public SEID getSourceSeid()
  {
    return sourceSeid;
  }

  /**
   * Returns the messageNumber.
   * @return int
   */
  public int getMessageNumber()
  {
    return messageNumber;
  }

  /**
   * Returns the destinationSeid.
   * @return SEID
   */
  public SEID getDestinationSeid()
  {
    return destinationSeid;
  }

}
