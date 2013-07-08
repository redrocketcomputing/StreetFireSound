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
 * $Id: MessageBackHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.havi.system.HaviListener;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.constants.ConstTransferMode;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author david
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class MessageBackHelper extends HaviListener
{
  protected final static Status OK = new Status(ConstApiCode.ANY, ConstGeneralErrorCode.SUCCESS);
  protected final static Status UNIDENTIFIED_FAILURE = new Status(ConstApiCode.ANY, ConstGeneralErrorCode.UNIDENTIFIED_FAILURE);
  protected final static byte[] EMPTY_RESPONSE = new byte[0];

  private final static Class[] PARAMETER_TYPES = { HaviByteArrayInputStream.class };
  private MessageBackListener listener;
  private Class invocationClass;
  private SoftwareElement softwareElement;
  private OperationCode opCode;


  /**
   * Constructor for MessageBackHelper.
   * @param softwareElement
   * @param opCode
   */
  public MessageBackHelper(SoftwareElement softwareElement, OperationCode opCode, MessageBackListener listener, Class invocationClass)
  {
    // Check the parameter
    if (softwareElement == null || opCode == null || listener == null || invocationClass == null)
    {
      // Bad
      throw new IllegalArgumentException("parameter is null");
    }

    this.softwareElement = softwareElement;
    this.opCode = opCode;
    this.listener = listener;
    this.invocationClass = invocationClass;
  }

  /**
   * @see org.havi.system.HaviListener#receiveMsg(boolean, byte, SEID, SEID, Status, HaviByteArrayInputStream)
   */
  public boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload)
  {
    // Check for debug
    if (softwareElement.isDebug())
    {
      // Log some debug
      LoggerSingleton.logDebugCoarse(this.getClass(), "receiveMsg", softwareElement.getSeid() + " haveReplied: " + haveReplied + " protocolType: " + protocolType + " sourceId: " + sourceId + " destinationId: " + destId + " state: " + state);
    }

    // Ignore if replied already
    //if (haveReplied || protocolType != ConstProtocolType.HAVI_RMI || state.getErrCode() != ConstGeneralErrorCode.SUCCESS)
    if (protocolType != ConstProtocolType.HAVI_RMI || state.getErrCode() != ConstGeneralErrorCode.SUCCESS)
    {
      return false;
    }

    try
    {
      // Read RMI header
      HaviRmiHeader header = new HaviRmiHeader(payload);

      // Check for debug
      if (softwareElement.isDebug())
      {
        // Log some debug
        LoggerSingleton.logDebugCoarse(this.getClass(), "receiveMsg", "sourceId: " + softwareElement.getSeid() + " destinationId: " + destId + " "+ header);
      }

      // Match the operation code and request
      if (!opCode.equals(header.getOpCode()) || (header.getControlFlags() & 0x01) != 0)
      {
        // Not for use
        return false;
      }

      // Create the invocation
      MessageBackInvocation invocation = createInvocation(payload);
      if(invocation == null)
      {
        LoggerSingleton.logError(this.getClass(), "recieveNotification", "Failed to invoke handler");
        return false;
      }
      
      // Dispatch to method
      invocation.dispatch(listener);

      // Marshall up a response
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
      invocation.marshal(hbaos);

      // Send ok response
      sendResponse(sourceId, opCode, invocation.getReturnCode(), hbaos.toByteArray(), header.getTransactionId());

      // We handle the message
      return true;
    }
    catch (HaviUnmarshallingException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "recieveMsg", e.toString());

      // We did not handle it
      return false;
    }
    catch (HaviMarshallingException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "recieveMsg", e.toString());

      // We did not handle it
      return false;
    }
  }

  public MessageBackInvocation createInvocation(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    try
    {
      // Get invocation class constructor
      Constructor constructor = invocationClass.getConstructor(PARAMETER_TYPES);

      // Build arguments
      Object[] arguments = new Object[1];
      arguments[0] = hbais;

      // Create the invocation object
      return (MessageBackInvocation)constructor.newInstance(arguments);
    }
    catch (NoSuchMethodException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (InstantiationException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    catch (InvocationTargetException e)
    {
      // Check the instance of the target exception
      if (e.getTargetException() instanceof HaviUnmarshallingException)
      {
        // Translate
        throw ((HaviUnmarshallingException)e.getTargetException());
      }

      // Unknow exception
      throw new IllegalStateException(e.getTargetException().toString());
    }
  }

  /**
   * Send response while swallowing all exceptions
   * @param returnCode The return status to send
   * @param buffer The response data
   */
  private void sendResponse(SEID destinationSeid, OperationCode opCode, Status returnCode, byte[] buffer, int transactionId)
  {
    try
    {
      // Send the reponse
      softwareElement.msgSendResponse(destinationSeid, opCode, ConstTransferMode.SIMPLE, returnCode, buffer, transactionId);
    }
    catch (HaviException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "sendResponse", e.toString());
    }
  }
}
