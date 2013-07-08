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
 * $Id: AsyncResponseHelper.java,v 1.2 2005/02/24 03:30:22 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import gnu.trove.TLinkedList;

import java.util.Iterator;

import org.havi.system.HaviListener;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public abstract class AsyncResponseHelper extends HaviListener
{
  private SoftwareElement softwareElement;
  private AsyncResponseInvocationFactory invocationFactory;
  private TLinkedList outstandTransactions = new TLinkedList();

  /**
   * Constructor for RemoteServerHelper.
   */
  public AsyncResponseHelper(SoftwareElement softwareElement, AsyncResponseInvocationFactory invocationFactory)
  {
    // Check the parameter
    if (softwareElement == null || invocationFactory == null)
    {
      // Opps
      throw new IllegalArgumentException("softwareElement or factory is null");
    }

    // Save the parameters
    this.invocationFactory = invocationFactory;
    this.softwareElement = softwareElement;
  }
  
  public void addAsyncResponseListener(int timeout, int transactionId, AsyncResponseListener listener)
  {
    // Create new entry
    AsyncResponseHandler handler = new AsyncResponseHandler(this, transactionId, listener, timeout);

    // Add entry to the list
    add(handler);
  }
  
  public void addAsyncResponseListener(int transactionId, AsyncResponseListener listener)
  {
    // Create new entry
    AsyncResponseHandler handler = new AsyncResponseHandler(this, transactionId, listener, 30000);

    // Add entry to the list
    add(handler);
  }

  public void removeAsyncResponseListener(int transactionId)
  {
    // Remove the entry
    AsyncResponseHandler handler = remove(transactionId);
    if (handler != null)
    {
      // Cancel the timer
      handler.cancel();
    }
  }

  /**
   * @see org.havi.system.HaviListener#receiveMsg(boolean, byte, SEID, SEID, Status, HaviByteArrayInputStream)
   */
  public boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload)
  {
    // Check for software element debug
    if (softwareElement.isDebug())
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "receiveMsg", softwareElement.getSeid().toString() + "- looking at " + protocolType + " from " + sourceId + " to " + destId + " with state " + state + " and haveReplied " + haveReplied);
    }
    
    //ignore if replied already
    if (haveReplied)
    {
      return false;
    }

    //ignore if not havi rmi
    if (protocolType != ConstProtocolType.HAVI_RMI)
    {
      return false;
    }

    //try to handle.
    try
    {
      // Read RMI header
      HaviRmiHeader header = new HaviRmiHeader(payload);

      // Check for software element debug
      if (softwareElement.isDebug())
      {
        LoggerSingleton.logDebugCoarse(this.getClass(), "receiveMsg", softwareElement.getSeid().toString() + "- " + header.toString());
      }
      
      // Make sure this is a response before go any farther
      if ((header.getControlFlags() & 0x01) != 1)
      {
        // Not a response, can not possbily be for us
        return false;
      }
      
      // Try to lookup the transaction
      AsyncResponseHandler handler = remove(header.getTransactionId());
      if (handler == null)
      {
        // Not for us
        return false;
      }

      // Check for timeout
      if (state.getErrCode() == ConstGeneralErrorCode.LOCAL)
      {
        // Yes timeout
        // XXX:000:stephen:20041219:This should really be dispatched through the invocation, but we do not have the OpCode so we can not
        // build an accurate message buffer.
        handler.getListener().timeout(handler.getTransactionId());
        
        // All done
        return true;
      }

      // Cancel the timer
      handler.cancel();
      
      // Try to create a remote invocation object
      AsyncResponseInvocation invocation = invocationFactory.createInvocation(header, payload);
      if (invocation == null)
      {
        // Log error because the transactions match, but we could not find a invocation handler
        LoggerSingleton.logError(this.getClass(), "receiveMsg", "found matching transaction but could not find a invocation handler for " + header.getOpCode());

        // Not for us
        return false;
      }

      // Dispatcher the invocation
      invocation.dispatch(handler.getTransactionId(), handler.getListener());

      // All gone
      return true;
    }
    catch(HaviUnmarshallingException e)
    {
      LoggerSingleton.logError(this.getClass(), "receiveMsg", e.toString());
    }

    // We did not handle it
    return false;
  }

  void timeout(AsyncResponseHandler handler)
  {
    try
    {
      if (contains(handler))
      {
        // Build dummy HaviRmiHeader
        //softwareElement.setDebug(true);
        HaviRmiHeader header = new HaviRmiHeader(new OperationCode(invocationFactory.getApiCode(), invocationFactory.getLastOperationId()), (byte)0x01, handler.getTransactionId());
        Status timeoutStatus = new Status(invocationFactory.getApiCode(), ConstGeneralErrorCode.LOCAL);
        
        // Make byte stream
        HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
        header.marshal(hbaos);
        
        // Push timeout onto software element
        Status result = softwareElement.callback(ConstProtocolType.HAVI_RMI, softwareElement.getSeid(), softwareElement.getSeid(), timeoutStatus, hbaos.toByteArray());
      }
    }
    catch (HaviMarshallingException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "timeout", e.toString());
    }
  }
  
  private boolean contains(AsyncResponseHandler handler)
  {
    synchronized(outstandTransactions)
    {
      return outstandTransactions.contains(handler);
    }
  }

  private void add(AsyncResponseHandler handler)
  {
    synchronized(outstandTransactions)
    {
      // Add to the outstanding list
      outstandTransactions.add(handler);
    }
  }

  private void remove(AsyncResponseHandler handler)
  {
    synchronized(outstandTransactions)
    {
      outstandTransactions.remove(handler);
    }
  }

  private AsyncResponseHandler remove(int transactionId)
  {
    synchronized(outstandTransactions)
    {
      // Loop through the list looking for a matching transaction id
      for (Iterator iterator = outstandTransactions.iterator(); iterator.hasNext();)
      {
        // Extract the element
        AsyncResponseHandler element = (AsyncResponseHandler) iterator.next();

        // Check for matching transaction
        if (element.getTransactionId() == transactionId)
        {
          // Remove from the list
          iterator.remove();

          // Return it
          return element;
        }
      }

      // Not found
      return null;
    }
  }
}
