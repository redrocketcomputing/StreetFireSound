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
 * $Id: TransferProtocol.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.transfer;

import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstMsgErrorCode;
import org.havi.system.constants.ConstMsgReliableAckCode;
import org.havi.system.constants.ConstTransferProtocolMessageTypes;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviMsgAckException;
import org.havi.system.types.HaviMsgDestSeidException;
import org.havi.system.types.HaviMsgDestUnreachableException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgFailException;
import org.havi.system.types.HaviMsgOverflowException;
import org.havi.system.types.HaviMsgSendException;
import org.havi.system.types.HaviMsgTargetRejectException;
import org.havi.system.types.Status;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.message.HaviMessage;
import com.redrocketcomputing.havi.system.ms.message.HaviMessageHandler;
import com.redrocketcomputing.havi.system.ms.message.HaviReliableAckMessage;
import com.redrocketcomputing.havi.system.ms.message.HaviReliableMessage;
import com.redrocketcomputing.havi.system.ms.message.HaviReliableNoackMessage;
import com.redrocketcomputing.havi.system.ms.message.HaviSimpleMessage;
import com.redrocketcomputing.havi.system.ms.tam.TransportAdaptationModule;
import com.redrocketcomputing.havi.system.ms.tam.TransportAdaptationModuleListener;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author Stephen Street
 */

public class TransferProtocol implements TransportAdaptationModuleListener, HaviMessageHandler
{
  private final static Status OK = new Status(ConstApiCode.ANY, ConstGeneralErrorCode.SUCCESS);
  private final static Status OVERFLOW = new Status(ConstApiCode.MSG, ConstMsgErrorCode.OVERFLOW);
  private final static Status REJECT = new Status(ConstApiCode.MSG, ConstMsgErrorCode.TARGET_REJECT);
  private final static Status DEST_SEID = new Status(ConstApiCode.MSG, ConstMsgErrorCode.DEST_SEID);

  private GUID localGuid;
  private EventDispatch eventDispatcher;
  private TransferProtocolEventListener listener;
  private SourceSeidTable sourceSeidTable;
  private DestinationSeidTable destinationSeidTable;
  private OutstandingMessageTable outstandingMessageTable;
  private ComponentConfiguration configuration;
  private TransportAdaptationModule tam;

  /**
   * Constructor
   * @param localGuid The local GUID for the device
   * @param listener The protocol listener, all message are deliveryed through this interface
   * @param configuration The configuration object for the transfer protocol
   * @throws HaviMsgException Thrown if there is a problem create the TransportAdaptationModule
   */
  public TransferProtocol(GUID localGuid, TransferProtocolEventListener listener, ComponentConfiguration configuration) throws HaviMsgException
  {
    // Check parameter
    if (localGuid == null || listener == null || configuration == null)
    {
      // Opps
      throw new IllegalArgumentException("parameter is null");
    }

    // Save the parameter
    this.listener = listener;
    this.configuration = configuration;
    this.localGuid = localGuid;

    // Try to get the event dispatcher service
    eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
    if (eventDispatcher == null)
    {
      // Again, very bad
      throw new IllegalStateException("can not find event dispatch service");
    }

    // Get the max outstanding
    int maxOutstanding = configuration.getIntProperty("transfer.protocol.max.outstanding", 25);

    // Allocate the tables
    sourceSeidTable = new SourceSeidTable(maxOutstanding);
    destinationSeidTable = new DestinationSeidTable();
    outstandingMessageTable = new OutstandingMessageTable();

    // Create the TAM
    tam = new com.redrocketcomputing.havi.system.ms.tam.cmm.ip.Tam(this);
  }

  /**
   * Send a message using the simple transfer protocol.  Messages for the local device are loop back.
   * @param message The message to send
   * @throws HaviMsgException Thrown when a problem is detected sending the message
   */
  public void sendSimple(HaviMessage message) throws HaviMsgException
  {
    // Check message
    if (message == null)
    {
      // Badness
      throw new IllegalArgumentException("message can not be null");
    }

    // Check parameters
    if (message.getMessageType() != ConstTransferProtocolMessageTypes.SIMPLE)
    {
      // Badness
      throw new IllegalArgumentException("bad message type: " + message.getMessageType());
    }

    // Look the source and destination table entry
    SourceSeid sourceEntry = sourceSeidTable.get(message.getSource());
    DestinationSeid destinationEntry = destinationSeidTable.get(message.getDestination());

    // Allocate a message number
    message.setMessageNumber(sourceEntry.allocateMessageNumber());

    try
    {
      // Check to see if the destination SEID is reachable
      if (!destinationEntry.isReachable())
      {
        // Well, not reachable
        throw new HaviMsgDestUnreachableException(destinationEntry.getSeid().toString());
      }

      // Check for loop back
      if (message.getDestination().getGuid().equals(localGuid))
      {
        // Loop it back
        listener.received(message);
      }
      else
      {

        // Send the message
        tam.send(message);
      }
    }
    catch (HaviMsgFailException e)
    {
      // Force the destination into a unreachable state
      destinationEntry.unreachable();

      // Forward on
      throw e;
    }
    finally
    {
      // Always release the message number
      sourceEntry.releaseMessageNumber(message.getMessageNumber());
    }
  }

  public void sendReliable(HaviMessage message, long timeout) throws HaviMsgException
  {
    // Check message
    if (message == null)
    {
      // Badness
      throw new IllegalArgumentException("message can not be null");
    }

    // Check parameters
    if (message.getMessageType() != ConstTransferProtocolMessageTypes.RELIABLE)
    {
      // Badness
      throw new IllegalArgumentException("bad message type: " + message.getMessageType());
    }

    // Look the source and destination table entry
    SourceSeid sourceEntry = sourceSeidTable.get(message.getSource());
    DestinationSeid destinationEntry = destinationSeidTable.get(message.getDestination());

    // Allocate a message number
    message.setMessageNumber(sourceEntry.allocateMessageNumber());

    // Allocate an outstanding message entry
    OutstandingMessage outstanding = outstandingMessageTable.allocate(message.getSource(), message.getDestination(), message.getMessageNumber());

    try
    {
      // Check to see if the destination SEID is reachable
      if (!destinationEntry.isReachable())
      {
        // Well, not reachable
        throw new HaviMsgDestUnreachableException(destinationEntry.getSeid().toString());
      }

      // Check for loop back
      Status result;
      if (message.getDestination().getGuid().equals(localGuid))
      {
        // Loop it back
        result = toStatus(listener.received(message));
      }
      else
      {
        // Send the message
        tam.send(message);

        // Wait for response
        result = outstanding.waitForStatus(timeout);
      }

      // Throw any exceptions
      throwStatus(result);
    }
    catch (HaviMsgException e)
    {
    	// Log the error
    	LoggerSingleton.logError(this.getClass(), "sendReliable", message.getSource().toString() + "->" + message.getDestination().toString() + ' ' + e.toString());

      // Update error
      destinationEntry.error();

      // Throw again
      throw e;
    }
    finally
    {
      // Always release the message number
      sourceEntry.releaseMessageNumber(message.getMessageNumber());

      // Always release the outstanding message
      outstandingMessageTable.release(outstanding);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleReliableAckMessage(HaviReliableAckMessage)
   */
  public void handleReliableAckMessage(HaviReliableAckMessage message)
  {
    // Lookup the outstanding message
    OutstandingMessage outstanding = outstandingMessageTable.get(message.getDestination(), message.getSource(), message.getMessageNumber());
    if (outstanding == null)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "handleReliableAckMessage", "missing OutstandingMessage entry for " + message.getSource() + "->" + message.getDestination() + " with message number " + message.getMessageNumber());

      // All done
      return;
    }

    // Post OK status
    outstanding.postStatus(OK);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleReliableMessage(HaviReliableMessage)
   */
  public void handleReliableMessage(HaviReliableMessage message)
  {

    try
    {
      // Forward
      int result = listener.received(message);

      // Create a response message
      HaviMessage responseMessage;
      if (result == ConstMsgReliableAckCode.SUCCESS)
      {
        // Allocate reliable ack message
        responseMessage = new HaviReliableAckMessage(message.getSource(), message.getDestination(), message.getProtocolType(), message.getMessageNumber());
      }
      else
      {
        // Allocate reliable no ack message
        responseMessage = new HaviReliableNoackMessage(message.getSource(), message.getDestination(), message.getProtocolType(), message.getMessageNumber(), result);
      }

      // Send the response
      tam.send(responseMessage);



    }

    catch (HaviMsgException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "handleReliableMessage", e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleReliableNoackMessage(HaviReliableNoackMessage)
   */
  public void handleReliableNoackMessage(HaviReliableNoackMessage message)
  {
    // Lookup the outstanding message
    OutstandingMessage outstanding = outstandingMessageTable.get(message.getDestination(), message.getSource(), message.getMessageNumber());
    if (outstanding == null)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "handleReliablenoackMessage", "missing OutstandingMessage entry for " + message.getSource() + "->" + message.getDestination() + " with message number " + message.getMessageNumber());

      // All done
      return;
    }

    // Post return code
    outstanding.postStatus(toStatus(message.getCode()));
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleSimpleMessage(HaviSimpleMessage)
   */
  public void handleSimpleMessage(HaviSimpleMessage message)
  {
    // Just forward
    listener.received(message);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.tam.TransportAdaptationModuleListener#received(HaviMessage)
   */
  public void received(HaviMessage message)
  {
    // Dispatch through the message
    message.dispatch(this);
  }

  /**
   * Converts a status into an exception and throws it
   * @param status The Status to convert
   * @throws HaviMsgException Thrown always
   */
  private void throwStatus(Status status) throws HaviMsgException
  {

    // Check for OK
    if (status.getErrCode() == ConstGeneralErrorCode.SUCCESS)
    {
      // Ok, just return
      return;
    }

    // Well, translate to exception and throw it
    switch (status.getErrCode())
    {
      case ConstMsgErrorCode.ACK:
      {
        throw new HaviMsgAckException("from remote end or timeout");
      }
      case ConstMsgErrorCode.OVERFLOW:
      {
        throw new HaviMsgOverflowException("from remote end");
      }
      case ConstMsgErrorCode.TARGET_REJECT:
      {
        throw new HaviMsgTargetRejectException("from remote end");
      }
      case ConstMsgErrorCode.DEST_SEID:
      {
        throw new HaviMsgDestSeidException("from remote end");
      }
      case ConstMsgErrorCode.DEST_UNREACHABLE:
      {
        throw new HaviMsgDestUnreachableException("from remote end");
      }
      default:
      {
        LoggerSingleton.logError(this.getClass(), "throwStatus", "bad error code: " + status.toString());
        throw new HaviMsgSendException("bad error code: " + status.toString());
      }
    }
  }

  /**
   * Convert a noack or return code to a status
   * @param The code to convert
   * @return Status The status for the corresponding code
   */
  private Status toStatus(int code)
  {
     switch (code)
    {
      case ConstMsgReliableAckCode.SUCCESS:
      {
        return OK;
      }

      case ConstMsgReliableAckCode.SYSTEM_OVERFLOW:
      {
        return OVERFLOW;
      }

      case ConstMsgReliableAckCode.TARGET_REJECT:
      {
        return REJECT;
      }

      case ConstMsgReliableAckCode.UNKNOWN_TARGET_OBJECT:
      {
        return DEST_SEID;
      }

      default:
      {
        // Some big badness
        LoggerSingleton.logError(this.getClass(), "toStatus", "bad error code: " + code);
        return DEST_SEID;
      }
    }
  }
}
