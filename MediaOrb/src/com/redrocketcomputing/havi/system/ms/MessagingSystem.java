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
 * $Id: MessagingSystem.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms;

import java.io.IOException;
import java.io.PrintStream;

import org.havi.system.MsgCallback;
import org.havi.system.cmmip.rmi.GuidListReadyEventNotificationHelper;
import org.havi.system.cmmip.rmi.GuidListReadyEventNotificationListener;
import org.havi.system.cmmip.rmi.NetworkResetEventNotificationHelper;
import org.havi.system.cmmip.rmi.NetworkResetEventNotificationListener;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstMsgErrorCode;
import org.havi.system.constants.ConstMsgOperationId;
import org.havi.system.constants.ConstMsgReliableAckCode;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.constants.ConstSoftwareElementHandle;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.constants.ConstTransferMode;
import org.havi.system.constants.ConstTransferProtocolMessageTypes;
import org.havi.system.msg.rmi.MsgErrorEventNotificationHelper;
import org.havi.system.msg.rmi.MsgErrorEventNotificationListener;
import org.havi.system.msg.rmi.MsgLeaveEventNotificationHelper;
import org.havi.system.msg.rmi.MsgLeaveEventNotificationListener;
import org.havi.system.msg.rmi.MsgServerHelper;
import org.havi.system.msg.rmi.MsgSkeleton;
import org.havi.system.msg.rmi.MsgTimeoutEventNotificationHelper;
import org.havi.system.msg.rmi.MsgTimeoutEventNotificationListener;
import org.havi.system.msg.rmi.SystemReadyEventNotificationHelper;
import org.havi.system.msg.rmi.SystemReadyEventNotificationListener;
import org.havi.system.types.EventId;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviEventManagerException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviMsgAllocException;
import org.havi.system.types.HaviMsgDestSeidException;
import org.havi.system.types.HaviMsgElementException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgFailException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviMsgOverflowException;
import org.havi.system.types.HaviMsgProtocolException;
import org.havi.system.types.HaviMsgRemoteApiException;
import org.havi.system.types.HaviMsgSendException;
import org.havi.system.types.HaviMsgTimeoutException;
import org.havi.system.types.HaviMsgUnidentifiedFailureException;
import org.havi.system.types.HaviMsgUnknownException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;
import org.havi.system.types.SystemEventId;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstMediaOrbRelease;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.em.EventManager;
import com.redrocketcomputing.havi.system.ms.event.NetworkReadyEvent;
import com.redrocketcomputing.havi.system.ms.event.NetworkResetEvent;
import com.redrocketcomputing.havi.system.ms.event.SeidErrorEvent;
import com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidLeaveEvent;
import com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener;
import com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEvent;
import com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener;
import com.redrocketcomputing.havi.system.ms.event.SystemReadyEvent;
import com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener;
import com.redrocketcomputing.havi.system.ms.message.HaviMessage;
import com.redrocketcomputing.havi.system.ms.message.HaviReliableMessage;
import com.redrocketcomputing.havi.system.ms.message.HaviSimpleMessage;
import com.redrocketcomputing.havi.system.ms.transfer.TransferProtocol;
import com.redrocketcomputing.havi.system.ms.transfer.TransferProtocolEventListener;
import com.redrocketcomputing.havi.system.service.SystemService;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 */
public class MessagingSystem extends SystemService implements MsgSkeleton, TransferProtocolEventListener, VersionSkeleton
{
  private final static int RMI_REQUEST = 0;
  private final static int RMI_RESPONSE = 1;
  private final static int RMI_REQUEST_HEADER_SIZE = 12;
  private final static int RMI_RESPONSE_HEADER_SIZE = 16;

  private final static OperationCode EVENT_OPCODE = new OperationCode(ConstApiCode.MSG, (byte) 0xff);
  private final static Status okStatus = new Status(ConstApiCode.MSG, ConstGeneralErrorCode.SUCCESS);
  private final static Status msgFailError = new Status(ConstApiCode.MSG, ConstMsgErrorCode.FAIL);
  private final static Status msgProtocolError = new Status(ConstApiCode.MSG, ConstMsgErrorCode.PROTOCOL);

  private MsgServerHelper serverHelper = null;
  private VersionServerHelper versionServerHelper = null;
  private SEID localSeid = null;
  private TransferProtocol transferProtocol = null;
  private EventManager eventManager = null;
  private EventDispatch eventDispatcher = null;
  private boolean systemElementsRegistered[] = { false, false, false, false };
  private int defaultTimeout = 30000;
  private GUID localGuid = null;
  private OpenSeidTable openSeidTable = null;
  private OutstandingTransactionTable outstandingTransactionTable = null;
  private SeidWatcherTable seidWatcherTable = null;

  public MessagingSystem(String instanceName) throws HaviMsgListenerExistsException, HaviMsgException
  {
    // Construct superclass
    super(instanceName, ConstSoftwareElementType.MESSAGING_SYSTEM);
    
    // Build configuration
    ComponentConfiguration configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);

    // Try to get GADP service
    Gadp gadp = (Gadp) ServiceManager.getInstance().find(Gadp.class);
    if (gadp == null)
    {
      // On, nooo, bad configuration
      throw new ServiceException("can not find GADP service");
    }

    // Get the local GUID
    localGuid = gadp.getLocalGuid();

    // Get default timeout
    defaultTimeout = configuration.getIntProperty("default.timeout", 30000);

    // Create local SEID
    localSeid = new SEID(localGuid, ConstSoftwareElementHandle.MESSAGING_SYSTEM);

    // Create tables
    openSeidTable = new OpenSeidTable(localGuid);
    outstandingTransactionTable = new OutstandingTransactionTable();
    seidWatcherTable = new SeidWatcherTable(this, localSeid);

    // Create transfer protocol
    transferProtocol = new TransferProtocol(localGuid, this, configuration);
  }

  public void start() throws ServiceException
  {
    // Check service state
    if (getServiceState() != Service.IDLE)
    {
      // Opps
      throw new ServiceException("service is not idle");
    }

    try
    {
      // Forward to super class
      super.start();
      
      // Create the server helper
      serverHelper = new MsgServerHelper(getSoftwareElement(), this);
      versionServerHelper = new VersionServerHelper(getSoftwareElement(), this);
      getSoftwareElement().addHaviListener(serverHelper);
      getSoftwareElement().addHaviListener(versionServerHelper);

      // Mark service as started
      setServiceState(Service.RUNNING);

      // Log initialization
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running with version " + ConstMediaOrbRelease.getRelease());

    }
    catch (HaviMsgListenerExistsException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "start", e.toString());

      // Translate exception
      throw new ServiceException("detected " + e.toString());
    }
    catch (HaviException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "start", e.toString());

      // Translate exception
      throw new ServiceException("detected " + e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Report not implemented
    printStream.println("not implemented");
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Check service state
    if (getServiceState() != Service.RUNNING)
    {
      // Opps
      throw new ServiceException("service is not running");
    }

    // Forward to super class
    super.terminate();
    
    serverHelper = null;
    versionServerHelper = null;

    // Mark as idle
    setServiceState(Service.IDLE);

    // Log start
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  public SEID open(MsgCallback callback) throws HaviMsgException
  {
    // Create a new allocator, default to trusted
    return openSeidTable.open(callback);
  }

  public void close(SEID seid) throws HaviMsgException
  {
  	// Check parameter
  	if (seid == null)
  	{
  		// bad, bad
  		throw new IllegalArgumentException("seid is null");
  	}

    // Remove the seid
    openSeidTable.close(seid);
  }

  public boolean isTrusted(SEID seid) throws HaviMsgException
  {
    // Check the handle
    if (seid.getHandle() > 0 && seid.getHandle() < 0x7fff)
    {
      // it is trusted
      return true;
    }

    // Not trusted
    return false;
  }

  public SEID getSystemSeid(SEID seid, int type) throws HaviMsgException
  {
    // Check the type
    short handle = (short) (type & 0xffff);
    if (handle < ConstSoftwareElementHandle.MESSAGING_SYSTEM || handle > ConstSoftwareElementHandle.RESOURCE_MANAGER)
    {
      throw new HaviMsgElementException("bad type " + type);
    }

    // Return system seid
    return new SEID(seid.getGuid(), handle);
  }

  public void watchOn(SEID sourceId, SEID destinationId, OperationCode opCode) throws HaviMsgException
  {
    // Check arguments
    if (sourceId == null || destinationId == null || opCode == null)
    {
      // Bad, bad thing
      throw new IllegalArgumentException("source or destination is null");
    }

    try
    {
      // Send request synchronous
      byte[] result = sendRequestSync(getSoftwareElement().getSeid(), getSystemSeid(destinationId, ConstSoftwareElementType.MESSAGING_SYSTEM), ConstMsgOperationId.PING_OPCODE, 30000, destinationId.getValue());

      // Unmarshall the response
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(result);
      boolean valid = hbais.readBoolean();

      // Check the response
      if (!valid)
      {
        // No good
        throw new HaviMsgDestSeidException(destinationId.toString());
      }

      // Add supervision to the watcher table
      seidWatcherTable.addWatcher(sourceId, destinationId, opCode);
    }
    catch (IOException ex)
    {
      // Opps
      throw new HaviMsgUnidentifiedFailureException(ex.toString());
    }
  }

  public void watchOff(SEID sourceId, SEID destinationId, OperationCode opCode) throws HaviMsgException
  {
    // Check arguments
    if (sourceId == null || destinationId == null || opCode == null)
    {
      // Bad, bad thing
      throw new IllegalArgumentException("source or destination is null");
    }

    // Remove supervision
    seidWatcherTable.removeWatcher(sourceId, destinationId, opCode);
  }

  public boolean ping(SEID seid) throws HaviMsgException
  {
    // Check arguments
    if (seid == null)
    {
      // Bad, bad thing
      throw new IllegalArgumentException("seid is null");
    }

    // Check for call back
    return openSeidTable.get(seid) != null;
  }

  public void sendSimple(int protocol, SEID sourceSeid, SEID[] destSeidList, byte[] buffer) throws HaviMsgException
  {
    // Check parameters
    if (sourceSeid == null || destSeidList == null || destSeidList.length == 0 || buffer == null)
    {
      // Bad arguments
      throw new IllegalArgumentException("bad arguments");
    }

    // Check the protocol type
    if (protocol == ConstProtocolType.HAVI_RMI)
    {
      throw new HaviMsgProtocolException("bad protocol type: " + protocol);
    }

    // Build the message, this relies on the fact that the buffer is the exact size
    HaviSimpleMessage message = new HaviSimpleMessage();
    message.setSource(sourceSeid);
    message.setProtocolType(protocol);
    message.setBody(buffer);

    try
    {
      // Send the message to each destination
      for (int i = 0; i < destSeidList.length; i++)
      {
        // Bind the destination
        message.setDestination(destSeidList[i]);

        // Send the message
        transferProtocol.sendSimple(message);
      }
    }
    catch (HaviMsgUnknownException ex)
    {
      // Translate exception
      throw new HaviMsgSendException("unknown: " + sourceSeid + " " + message.getDestination());
    }
    catch (HaviMsgTimeoutException ex)
    {
      // Translate exception
      throw new HaviMsgSendException("timeout: " + sourceSeid + " " + message.getDestination());
    }
  }

  public void sendReliable(int protocol, SEID sourceSeid, SEID destSeid, byte[] buffer) throws HaviMsgException
  {
    // Check parameters
    if (sourceSeid == null || destSeid == null || buffer == null)
    {
      // Bad arguments
      throw new IllegalArgumentException("bad arguments");
    }

    // Check the protocol type
    if (protocol == ConstProtocolType.HAVI_RMI)
    {
      throw new HaviMsgProtocolException();
    }

    // Build the message, this relies on the fact that the buffer is the exact size
    HaviReliableMessage message = new HaviReliableMessage();
    message.setSource(sourceSeid);
    message.setDestination(destSeid);
    message.setProtocolType(protocol);
    message.setBody(buffer);

    // Send the message to each destination
    transferProtocol.sendReliable(message, defaultTimeout);
  }

  public int sendRequest(SEID sourceSeid, SEID destSeid, OperationCode opCode, byte[] buffer) throws HaviMsgException
  {
    // Check parameters
    if (sourceSeid == null || destSeid == null || opCode == null || buffer == null)
    {
      // Bad arguments
      throw new IllegalArgumentException("bad arguments");
    }

    // Get a transaction id
    OutstandingTransaction transaction = outstandingTransactionTable.allocate(sourceSeid, destSeid);

    try
    {
      // Check for large message and force gc
      if (buffer.length > (1024*100))
      {
        // Log some information
        LoggerSingleton.logInfo(this.getClass(), "sendRequest", "large message(" + buffer.length + " bytes) forcing GC");
        System.gc();
      }
      
      // Marshal request
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(RMI_REQUEST_HEADER_SIZE + buffer.length);
      opCode.marshal(hbaos);
      hbaos.writeByte(RMI_REQUEST);
      hbaos.writeInt(transaction.getTransactionId());
      hbaos.write(buffer);

      // Build the message, this relies on the fact that the buffer is the exact size
      HaviReliableMessage message = new HaviReliableMessage();
      message.setSource(sourceSeid);
      message.setDestination(destSeid);
      message.setProtocolType(ConstProtocolType.HAVI_RMI);
      message.setBody(hbaos.toByteArray()); // BAD BAD, Must fix to prevent allocations

      // Send request
      transferProtocol.sendReliable(message, defaultTimeout);

      // return the transaction id
      return transaction.getTransactionId();
    }
    catch (HaviMarshallingException ex)
    {
      // Translate
      throw new HaviMsgSendException(ex.toString());
    }
    catch (IOException ex)
    {
      // Translate
      throw new HaviMsgSendException(ex.toString());
    }
    finally
    {
      // Release the transaction, THIS COULD BE A PROBLEM BECAUSE THE TRANSACTION ID COULD BE RESUSE, THIS IS HIGHLY UNLIKELY
      outstandingTransactionTable.release(transaction);
    }
  }

  public byte[] sendRequestSync(SEID sourceSeid, SEID destSeid, OperationCode opCode, long timeout, byte buffer[]) throws HaviMsgException
  {
    // Get a transaction id
    OutstandingTransaction transaction = outstandingTransactionTable.allocateSync(sourceSeid, destSeid);

    try
    {
      // Check for large message and force gc
      if (buffer.length > (1024*100))
      {
        // Log some information
        LoggerSingleton.logInfo(this.getClass(), "sendRequestSync", "large message(" + buffer.length + " bytes) forcing GC");
        System.gc();
      }
      
      // Marshal request
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(RMI_REQUEST_HEADER_SIZE + buffer.length);
      opCode.marshal(hbaos);
      hbaos.writeByte(RMI_REQUEST);
      hbaos.writeInt(transaction.getTransactionId());
      hbaos.write(buffer);

      // Build the message, this relies on the fact that the buffer is the exact size
      HaviReliableMessage message = new HaviReliableMessage();
      message.setSource(sourceSeid);
      message.setDestination(destSeid);
      message.setProtocolType(ConstProtocolType.HAVI_RMI);
      message.setBody(hbaos.toByteArray()); // BAD BAD, Must fix to prevent allocations

      // Adjust timeout
      if (timeout == 0)
      {
        timeout = defaultTimeout;
      }

      // Send request
      transferProtocol.sendReliable(message, timeout);

      // wait on the transaction monitor
      HaviMessage result = transaction.waitForMessage(timeout);

      // Check protocol type
      if (result.getProtocolType() != ConstProtocolType.HAVI_RMI)
      {
        // Throw send exception
        throw new HaviMsgSendException("response protocol type is not HAVI_RMI");
      }

      // Check message type to build input stream
      HaviByteArrayInputStream hbais;

      switch (result.getMessageType())
      {
        case ConstTransferProtocolMessageTypes.SIMPLE :
          {
            // Build input stream from simple message
            hbais = new HaviByteArrayInputStream(((HaviSimpleMessage) result).getBody());

            // Found good type
            break;
          }

        case ConstTransferProtocolMessageTypes.RELIABLE :
          {
            // Build input stream from reliable message
            hbais = new HaviByteArrayInputStream(((HaviReliableMessage) result).getBody());

            // Found good type
            break;
          }

        default :
          {

            // Bad problem
            throw new HaviMsgSendException("bad message type: " + result.getMessageType());
          }
      }

      // Read opCode and check the opcode
      OperationCode resultOpCode = new OperationCode(hbais);
      if (!opCode.equals(resultOpCode))
      {
        // Bad problem
        throw new HaviMsgSendException("Opcode mismatch: " + opCode + "<->" + resultOpCode);
      }

      // Make sure this is a response
      int controlFlags = hbais.readByte();
      if (controlFlags != RMI_RESPONSE)
      {
        // Throw a send exception
        throw new HaviMsgSendException("not a response on " + transaction.getTransactionId());
      }

      // Make sure the transaction id match
      int resultTransactionId = hbais.readInt();
      if (transaction.getTransactionId() != resultTransactionId)
      {
        // Throw a send exception
        throw new HaviMsgSendException("transaction ID does not match expecting " + transaction.getTransactionId() + "<->" + resultTransactionId);
      }

      // Check for remote api error
      Status resultStatus = new Status(hbais);
      if (resultStatus.getErrCode() != ConstGeneralErrorCode.SUCCESS)
      {
        // Throw a remote api exception
        throw new HaviMsgRemoteApiException(resultStatus);
      }

      // Skip reserved work
      hbais.skip(4);

      // Allocate new buffer to hold result
      byte[] resultBuffer = new byte[hbais.available()];

      // Read the result buffer
      hbais.read(resultBuffer);

      // Return the result buffer
      return resultBuffer;
    }
    catch (IOException ex)
    {
      // Translate
      throw new HaviMsgSendException(ex.toString());
    }
    catch (HaviMarshallingException ex)
    {
      // Translate
      throw new HaviMsgSendException(ex.toString());
    }
    catch (HaviUnmarshallingException ex)
    {
      // Translate
      throw new HaviMsgSendException(ex.toString());
    }
    finally
    {
      // Remove transaction
      outstandingTransactionTable.releaseSync(transaction);
    }
  }

  public void sendResponse(SEID sourceSeid, SEID destSeid, OperationCode opCode, int transferMode, Status returnCode, byte[] buffer, int transactionId) throws HaviMsgException
  {

    // Check parameters
    if (sourceSeid == null || destSeid == null || opCode == null || buffer == null)
    {
      // Bad arguments
      throw new IllegalArgumentException("bad arguments");
    }

    // Build the message
    try
    {
      // Check for large message and force gc
      if (buffer.length > (1024*100))
      {
        // Log some information
        LoggerSingleton.logInfo(this.getClass(), "sendResponse", "large message(" + buffer.length + " bytes) forcing GC");
        System.gc();
      }
      
      // Marshal request
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(RMI_RESPONSE_HEADER_SIZE + buffer.length);
      opCode.marshal(hbaos);
      hbaos.writeByte(RMI_RESPONSE);
      hbaos.writeInt(transactionId);
      returnCode.marshal(hbaos);
      hbaos.writeInt(0);
      hbaos.write(buffer);

      // Send request based on transfer mode
      if (transferMode == ConstTransferMode.RELIABLE)
      {
        // Build the message, this relies on the fact that the buffer is the exact size
        HaviReliableMessage message = new HaviReliableMessage();
        message.setSource(sourceSeid);
        message.setDestination(destSeid);
        message.setProtocolType(ConstProtocolType.HAVI_RMI);
        message.setBody(hbaos.toByteArray()); // BAD BAD, Must fix to prevent allocations

        // Send the message
        transferProtocol.sendReliable(message, defaultTimeout);
      }
      else
      {
        // Build the message, this relies on the fact that the buffer is the exact size
        HaviSimpleMessage message = new HaviSimpleMessage();
        message.setSource(sourceSeid);
        message.setDestination(destSeid);
        message.setProtocolType(ConstProtocolType.HAVI_RMI);
        message.setBody(hbaos.toByteArray()); // BAD BAD, Must fix to prevent allocations

        // Send the message
        transferProtocol.sendSimple(message);
      }
    }
    catch (HaviMarshallingException ex)
    {
      // Translate
      throw new HaviMsgSendException(ex.toString());
    }
    catch (IOException ex)
    {
      // Translate
      throw new HaviMsgSendException(ex.toString());
    }
  }

  /**
   * This is a system only API
   * @param callback
   * @param type
   * @return
   * @throws HaviMsgAllocException
   */
  public SEID sysOpen(MsgCallback callback, int type) throws HaviMsgException
  {
    // Create a new system seid
    SEID seid = openSeidTable.sysOpen(callback, (short) type);

    // Mark element as registered
    if (type < systemElementsRegistered.length)
    {
      systemElementsRegistered[type] = true;
    }

    // Check to see if this is the event manager starting
    if (type == ConstSoftwareElementType.EVENTMANAGER)
    {
      try
      {
        // Get internal event dispatcher
        eventDispatcher = (EventDispatch) ServiceManager.getInstance().find(EventDispatch.class);
        if (eventDispatcher == null)
        {
          // Very bad
          throw new IllegalStateException("can not find event dispatch service");
        }

        // Get the event manager
        eventManager = (EventManager) ServiceManager.getInstance().find(EventManager.class);
        if (eventManager == null)
        {
          // Throw an illegal state message
          throw new IllegalStateException("can not find event manager");
        }

        // Bind internal event handler
        eventDispatcher.addListener(new MsgInternalEventHandler());

				// Create event listeners
				MsgTimeoutEventNotificationHelper timeoutEventNotificationHelper = new MsgTimeoutEventNotificationHelper(getSoftwareElement(), EVENT_OPCODE, new MsgTimeoutEventNotificationHandler());
				MsgErrorEventNotificationHelper errorEventNotificationHelper = new MsgErrorEventNotificationHelper(getSoftwareElement(), EVENT_OPCODE, new MsgErrorEventNotificationHandler());
				MsgLeaveEventNotificationHelper leaveEventNotificationHelper = new MsgLeaveEventNotificationHelper(getSoftwareElement(), EVENT_OPCODE, new MsgLeaveEventNotificationHandler());
				SystemReadyEventNotificationHelper systemReadyEventNotificationHelper = new SystemReadyEventNotificationHelper(getSoftwareElement(), EVENT_OPCODE,  new SystemReadyEventNotificationHandler());
				NetworkResetEventNotificationHelper networkResetEventNotificationHelper = new NetworkResetEventNotificationHelper(getSoftwareElement(), EVENT_OPCODE, new NetworkResetEventNotificationHandler());
				GuidListReadyEventNotificationHelper guidListReadyEventNotificationHelper = new GuidListReadyEventNotificationHelper(getSoftwareElement(), EVENT_OPCODE, new GuidListReadyEventNotificationHandler());

				// Bind helpers to the software element
				getSoftwareElement().addHaviListener(timeoutEventNotificationHelper, getSystemSeid(getSoftwareElement().getSeid(), ConstSoftwareElementType.EVENTMANAGER));
				getSoftwareElement().addHaviListener(errorEventNotificationHelper, getSystemSeid(getSoftwareElement().getSeid(), ConstSoftwareElementType.EVENTMANAGER));
				getSoftwareElement().addHaviListener(leaveEventNotificationHelper, getSystemSeid(getSoftwareElement().getSeid(), ConstSoftwareElementType.EVENTMANAGER));
				getSoftwareElement().addHaviListener(systemReadyEventNotificationHelper, getSystemSeid(getSoftwareElement().getSeid(), ConstSoftwareElementType.EVENTMANAGER));
				getSoftwareElement().addHaviListener(networkResetEventNotificationHelper, getSystemSeid(getSoftwareElement().getSeid(), ConstSoftwareElementType.EVENTMANAGER));
				getSoftwareElement().addHaviListener(guidListReadyEventNotificationHelper, getSystemSeid(getSoftwareElement().getSeid(), ConstSoftwareElementType.EVENTMANAGER));

        // Subscribe to events
        EventId[] events = new EventId[6];
        events[0] = new SystemEventId(ConstSystemEventType.SYSTEM_READY);
        events[1] = new SystemEventId(ConstSystemEventType.MSG_LEAVE);
        events[2] = new SystemEventId(ConstSystemEventType.MSG_TIMEOUT);
        events[3] = new SystemEventId(ConstSystemEventType.MSG_ERROR);
        events[4] = new SystemEventId(ConstSystemEventType.NETWORK_RESET);
        events[5] = new SystemEventId(ConstSystemEventType.GUID_LIST_READY);

        // Subcribe to events
        eventManager.internalSubscribe(getSoftwareElement().getSeid(), events, EVENT_OPCODE);
      }
      catch (HaviMsgListenerExistsException e)
      {
        // Translate
        throw new HaviMsgFailException(e.toString());
      }
      catch (HaviException e)
      {
        // Translate
        throw new HaviMsgFailException(e.toString());
      }
    }

    // Return it
    return seid;
  }

  /**
   * This is a system only API
   * @param guid
   * @param type
   * @return
   */
  public SEID getRemoteSystemSeid(GUID guid, int type) throws HaviMsgException
  {
    // Check the type
    short handle = (short) (type & 0xffff);
    if (handle < ConstSoftwareElementHandle.MESSAGING_SYSTEM || handle > ConstSoftwareElementHandle.COMMUNICATION_MEDIA_MANAGER_SLINK)
    {
      throw new HaviMsgElementException("bad type " + type);
    }

    // Build seid
    byte[] rawSeid = new byte[SEID.SIZE];
    System.arraycopy(guid.getValue(), 0, rawSeid, 0, GUID.SIZE);
    rawSeid[8] = (byte) ((type >> 8) & 0xff);
    rawSeid[9] = (byte) (type & 0xff);

    // Return system seid
    return new SEID(rawSeid);
  }

  public int received(HaviMessage message)
  {
    // Try to find the callback
    MsgCallback destinationCallback = openSeidTable.get(message.getDestination());

    if (destinationCallback == null)
    {
      return ConstMsgReliableAckCode.UNKNOWN_TARGET_OBJECT;
    }

    // Peek at the message an check to see if it is a response
    if (message.getProtocolType() == ConstProtocolType.HAVI_RMI && message.peek(3) == RMI_RESPONSE)
    {
      try
      {
        // Assemble transaction ID
        int transactionId = (message.peek(4) << 24) + (message.peek(5) << 16) + (message.peek(6) << 8) + message.peek(7);

        // Check to see if we are waiting on the response
        OutstandingTransaction transaction = outstandingTransactionTable.get(transactionId);
        if (transaction != null)
        {
          // Hand to the transaction monitor
          transaction.postMessage(message);

          // All done
          return ConstMsgReliableAckCode.SUCCESS;
        }
      }
      catch (HaviMsgOverflowException ex)
      {

        // We must have found one and failed due to an overflow of the transaction slot, we are done
        return ConstMsgReliableAckCode.SYSTEM_OVERFLOW;
      }
    }

    // Extract message body
    byte buffer[];
    switch (message.getMessageType())
    {
      case ConstTransferProtocolMessageTypes.SIMPLE :
        {
          // Extract buffer
          buffer = ((HaviSimpleMessage) message).getBody();

          // Found good type
          break;
        }

      case ConstTransferProtocolMessageTypes.RELIABLE :
        {
          // Extract buffer
          buffer = ((HaviReliableMessage) message).getBody();

          // Found good type
          break;
        }

      default :
        {
          // Umm, message system problem, log error
          LoggerSingleton.logError(this.getClass(), "receiveMessage", "bad message type: " + message.getMessageType());

          // Return problem
          return ConstMsgReliableAckCode.TARGET_REJECT;
        }
    }

    // Invoke callback based on callback type
    Status callbackResult = destinationCallback.callback(message.getProtocolType(), message.getSource(), message.getDestination(), okStatus, buffer);

    // Check for large message and force gc
    if (buffer.length > (1024*100))
    {
      // Log some information
      LoggerSingleton.logInfo(this.getClass(), "received", "large message(" + buffer.length + " bytes) forcing GC");
      System.gc();
    }
    
    // Check return code
    if (callbackResult.getErrCode() != ConstGeneralErrorCode.SUCCESS)
    {
      // Some kind of problem
      return ConstMsgReliableAckCode.TARGET_REJECT;
    }

    // All good
    return ConstMsgReliableAckCode.SUCCESS;
  }

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    return ConstMediaOrbRelease.getRelease();
  }

  // Private inner classes for handling event notification
  private class MsgInternalEventHandler implements SeidErrorEventListener, SeidLeaveEventListener, SeidTimeoutEventListener, SystemReadyEventListener
  {
    /**
     * @see com.redrocketcomputing.havi.system.ms.event.SeidLeaveEventListener#seidLeaveEvent(SEID)
     */
    public void seidLeaveEvent(SEID seid)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "seidLeaveEvent", "internal event recieved");
      try
      {
        // Post event manager event only if the SEID is local
        if (seid.getGuid().equals(localGuid))
        {
          //LoggerSingleton.logDebugCoarse(this.getClass(), "seidLeaveEvent", "forward local event to remote device(s)");

          // Post the event
          eventManager.internalPostEvent(localSeid, new SystemEventId(ConstSystemEventType.MSG_LEAVE), true, seid.getValue());
        }
      }
      catch (HaviEventManagerException e)
      {
        // Just log error
        LoggerSingleton.logError(this.getClass(), "seidLeaveEvent", e.toString());
      }
    }

    /**
     * @see com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEventListener#seidTimeoutEvent(SEID)
     */
    public void seidTimeoutEvent(SEID seid)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "seidTimeoutEvent", "internal event recieved");
      try
      {
        // Post event manager event only if the SEID is local
        if (seid.getGuid().equals(localGuid))
        {
          //LoggerSingleton.logDebugCoarse(this.getClass(), "seidTimeoutEvent", "forward local event to remote device(s)");

          // Post the event
          eventManager.internalPostEvent(localSeid, new SystemEventId(ConstSystemEventType.MSG_TIMEOUT), true, seid.getValue());
        }
      }
      catch (HaviEventManagerException e)
      {
        // Just log error
        LoggerSingleton.logError(this.getClass(), "seidTimeoutEvent", e.toString());
      }
    }

    /**
     * @see com.redrocketcomputing.havi.system.ms.event.SeidErrorEventListener#seidErrorEvent(SEID)
     */
    public void seidErrorEvent(SEID seid)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "seidErrorEvent", "internal event recieved");
      try
      {
        // Post event manager event only if the SEID is local
        if (seid.getGuid().equals(localGuid))
        {
          //LoggerSingleton.logDebugCoarse(this.getClass(), "seidErrorEvent", "forward local event to remote device(s)");

          // Post the event
          eventManager.internalPostEvent(localSeid, new SystemEventId(ConstSystemEventType.MSG_ERROR), true, seid.getValue());
        }
      }
      catch (HaviEventManagerException e)
      {
        // Just log error
        LoggerSingleton.logError(this.getClass(), "seidErrorEvent", e.toString());
      }
    }

    /**
     * @see com.redrocketcomputing.havi.system.ms.event.SystemReadyEventListener#systemReadyEvent(SEID)
     */
    public void systemReadyEvent(SEID seid)
    {
      try
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "systemReadyEvent", "internal event recieved. recieved guid: " + seid.getGuid().toString() + " localGuid: " + localGuid.toString());
        // Post event manager event only if the SEID is local
        if (seid.getGuid().equals(localGuid))
        {
          //LoggerSingleton.logDebugCoarse(this.getClass(), "systemReadyEvent", "forward local event to remote device(s)");

          // Post the event
          eventManager.internalPostEvent(localSeid, new SystemEventId(ConstSystemEventType.SYSTEM_READY), true, seid.getValue());
        }
      }
      catch (HaviEventManagerException e)
      {
        // Just log error
        LoggerSingleton.logError(this.getClass(), "systemReadyEvent", e.toString());
      }
    }
  }

  private class MsgErrorEventNotificationHandler implements MsgErrorEventNotificationListener
  {
    /**
     * @see org.havi.system.rmi.msg.MsgErrorEventNotificationListener#msgErrorEventNotification(SEID, short, Status)
     */
    public void msgErrorEventNotification(SEID posterSeid, SEID seid, short attempts, Status error)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "msgErrorEventNotification", "event recieved");
      // Do not forward local events
      if (!localGuid.equals(seid.getGuid()))
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "msgErrorEventNotification", "Forward external event to internal components");

        // Launch internal event
        eventDispatcher.dispatch(new SeidErrorEvent(seid));
      }
    }
  }

  private class MsgTimeoutEventNotificationHandler implements MsgTimeoutEventNotificationListener
  {
    /**
     * @see org.havi.system.rmi.msg.MsgTimeoutEventNotificationListener#msgTimeoutEventNotification(SEID)
     */
    public void msgTimeoutEventNotification(SEID posterSeid, SEID seid)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "msgTimeoutEventNotification", "event recieved");
      // Do not forward local events
      if (!localGuid.equals(seid.getGuid()))
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "msgTimeoutEventNotification", "Forward external event to internal components");

        // Launch internal event
        eventDispatcher.dispatch(new SeidTimeoutEvent(seid));
      }
    }
  }

  private class MsgLeaveEventNotificationHandler implements MsgLeaveEventNotificationListener
  {
    /**
     * @see org.havi.system.rmi.msg.MsgLeaveEventNotificationListener#msgLeaveEventNotification(SEID)
     */
    public void msgLeaveEventNotification(SEID posterSeid, SEID seid)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "msgLeaveEventNotification", "event recieved");
      // Do not forward local events
      if (!localGuid.equals(seid.getGuid()))
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "msgLeaveEventNotification", "Forward external event to internal components");

        // Launch internal event
        eventDispatcher.dispatch(new SeidLeaveEvent(seid));
      }
    }
  }

  private class SystemReadyEventNotificationHandler implements SystemReadyEventNotificationListener
  {
    /**
     * @see org.havi.system.rmi.msg.SystemReadyEventNotificationListener#systemReadyEventNotification(SEID)
     */
    public void systemReadyEventNotification(SEID posterSeid, SEID seid)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "systemReadyEventNotification", "event recieved. recieved guid: " + seid.getGuid().toString() + " localGuid: " + localGuid.toString());
      // Do not forward local events
      if (!localGuid.equals(seid.getGuid()))
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "systemReadyEventNotification", "Forward external event to internal components");

        // Launch internal event
        eventDispatcher.dispatch(new SystemReadyEvent(seid));
      }
    }
  }

  private class GuidListReadyEventNotificationHandler implements GuidListReadyEventNotificationListener
  {
    /**
     * @see org.havi.system.rmi.cmmip.GuidListReadyEventNotificationListener#guidListReadyEventNotification(GUID[], GUID[], GUID[], GUID[])
     */
    public void guidListReadyEventNotification(SEID posterSeid, GUID[] activeGuidList, GUID[] nonactiveGuidList, GUID[] goneDevices, GUID[] newDevices)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "guidListReadyEventNotification", "event recieved.  Forward to internal components");

      // Launch internal event
      eventDispatcher.dispatch(new NetworkReadyEvent(activeGuidList, nonactiveGuidList, newDevices, goneDevices));

	    // Check to see if we should fire ready message
	    if (systemElementsRegistered[0] && systemElementsRegistered[1] && systemElementsRegistered[2] && systemElementsRegistered[3])
	    {
	      // Launch a system ready event
	      eventDispatcher.dispatch(new SystemReadyEvent(localSeid));
	    }
    }
  }

  private class NetworkResetEventNotificationHandler implements NetworkResetEventNotificationListener
  {
    /**
     * @see org.havi.system.rmi.cmmip.NetworkResetEventNotificationListener#networkResetEventNotification()
     */
    public void networkResetEventNotification(SEID posterSeid)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "networkResetEventNotification", "event recieved. Forward to internal components");

      // Launch internal event
      eventDispatcher.dispatch(new NetworkResetEvent());
    }
  }
}
