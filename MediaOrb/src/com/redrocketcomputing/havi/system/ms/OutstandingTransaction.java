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
 * $Id: OutstandingTransaction.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms;

import gnu.trove.TLinkableAdaptor;

import org.havi.system.constants.ConstMsgErrorCode;
import org.havi.system.types.HaviMsgAckException;
import org.havi.system.types.HaviMsgDestSeidException;
import org.havi.system.types.HaviMsgDestUnreachableException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgOverflowException;
import org.havi.system.types.HaviMsgSendException;
import org.havi.system.types.HaviMsgTimeoutException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.havi.system.ms.message.HaviMessage;
import com.redrocketcomputing.util.concurrent.Slot;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
class OutstandingTransaction extends TLinkableAdaptor
{
  private int transactionId;
  private SEID sourceSeid;
  private SEID destinationSeid;
  private Slot slot = new Slot();

  /**
   * Constructor for OutstandingTransaction.
   * @param sourceSeid The source SEID for the transaction
   * @param destinationSeid The destination SEID for the transaction
   * @param transactionId The ID of the transaction
   */
  public OutstandingTransaction(SEID sourceSeid, SEID destinationSeid, int transactionId)
  {
    // Construct super class
    super();

        // Check the parameters
    if (sourceSeid == null || destinationSeid == null)
    {
      // Bad thing are happening
      throw new IllegalArgumentException("seids can not be null");
    }

    // Save the parameters
    this.sourceSeid = sourceSeid;
    this.destinationSeid = destinationSeid;
    this.transactionId = transactionId;
  }

  /**
   * Wait for a message or exception
   * @param timeout Time in milliseconds to wait for a result
   * @return HaviMessage The message received
   * @throws HaviMsgException Thrown is a problem has been detected or the timeout was exceeded
   */
  public HaviMessage waitForMessage(long timeout) throws HaviMsgException
  {
    try
    {
      // Wait for result

      Object result = slot.poll(timeout);

      if (result == null)
      {
        // Timed out
        throw new HaviMsgTimeoutException(sourceSeid + "->" + destinationSeid);
      }

      // Check for status return, UGLY
      if (result instanceof Status)
      {
        // Let get out of here, this will cause an exception
        throwStatus((Status)result);
      }

      // Return the message
      return (HaviMessage)result;
    }
    catch (InterruptedException e)
    {
      // Clear thread interrupted status
      Thread.currentThread().interrupted();

      // Timed out
      throw new HaviMsgTimeoutException("interupted: " + sourceSeid + "->" + destinationSeid);
    }
  }

  /**
   * Post a status to the waiter.  This will cause an exception to be generated
   * @param status The status to post
   */
  public void postStatus(Status status)
  {
    try
    {
      // Offer to the slot
      if (!slot.offer(status, 0))
      {
        // Opp, log an error
        LoggerSingleton.logError(this.getClass(), "postStatus", "problem posting " + status + " to transaction " + transactionId);
      }
    }
    catch (InterruptedException e)
    {
      // Opp, log an error
      LoggerSingleton.logError(this.getClass(), "postStatus", "interrupted posting " + status + " to transaction " + transactionId);
    }
  }

  /**
   * Post a message response to the waiter. The wait will return this message
   * @param message The message to post
   */
  public void postMessage(HaviMessage message) throws HaviMsgOverflowException
  {
    try
    {
      // Offer to the slot
      if (!slot.offer(message, 0))
      {
        // Opp, overflow
        throw new HaviMsgOverflowException("problem posting message to transaction " + transactionId);
      }
    }
    catch (InterruptedException e)
    {
      // Opp, overflow
      throw new HaviMsgOverflowException("problem posting message to transaction " + transactionId);
    }
  }

  /**
   * Returns the destinationSeid.
   * @return SEID
   */
  public SEID getDestinationSeid()
  {
    return destinationSeid;
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
   * Returns the transactionId.
   * @return int
   */
  public int getTransactionId()
  {
    return transactionId;
  }

  /**
   * Convert a status into an exception and throw it
   * @param status The status to convert
   * @throws HaviMsgException Thrown if the status error code match
   */
  private void throwStatus(Status status) throws HaviMsgException
  {
    // Check to error code
    switch (status.getErrCode())
    {
      case ConstMsgErrorCode.ACK:
      {
        throw new HaviMsgAckException(sourceSeid + "->" + destinationSeid);
      }
      case ConstMsgErrorCode.DEST_SEID:
      {
        throw new HaviMsgDestSeidException(sourceSeid + "->" + destinationSeid);
      }
      case ConstMsgErrorCode.DEST_UNREACHABLE:
      {
        throw new HaviMsgDestUnreachableException(sourceSeid + "->" + destinationSeid);
      }
      case ConstMsgErrorCode.SEND:
      {
        throw new HaviMsgSendException(sourceSeid + "->" + destinationSeid);
      }
      default:
      {
        // This very bad
        throw new IllegalArgumentException("unknow " + status);
      }
    }
  }
}
