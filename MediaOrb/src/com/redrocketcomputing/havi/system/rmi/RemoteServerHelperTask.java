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
 * $Id: RemoteServerHelperTask.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstTransferMode;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public class RemoteServerHelperTask extends AbstractTask
{
  private final static Status UNIDENTIFIED_FAILURE = new Status(ConstApiCode.ANY, ConstGeneralErrorCode.UNIDENTIFIED_FAILURE);
  private final static byte[] EMPTY_RESPONSE = new byte[0];
  
  private static ThreadLocal callerInformation = new ThreadLocal();
  
  private SoftwareElement softwareElement;
  private SEID destinationSeid;
  private HaviRmiHeader header;
  private RemoteInvocation invocation;
  private RemoteSkeleton remoteSkeleton;
  private boolean enableThreadLocal;
  
  public static RemoteInvocationInformation getInvocationInformation()
  {
    return (RemoteInvocationInformation)callerInformation.get();
  }
  
  /**
   * Constructor for RemoteServerHelperTask.
   */
  public RemoteServerHelperTask(SoftwareElement softwareElement, SEID destinationSeid, HaviRmiHeader header, RemoteInvocation invocation, RemoteSkeleton remoteSkeleton, boolean enableThreadLocal)
  {
    // Contruct super class
    super();

    // Check parameter
    if (softwareElement == null || header == null || invocation == null || remoteSkeleton == null || destinationSeid == null)
    {
      // Badness
      throw new IllegalArgumentException("parameter is null");
    }

    // Save the parameters
    this.softwareElement = softwareElement;
    this.destinationSeid = destinationSeid;
    this.header = header;
    this.invocation = invocation;
    this.remoteSkeleton = remoteSkeleton;
    this.enableThreadLocal = enableThreadLocal;
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return softwareElement.getSeid().toString() + "::RemoteServerHelperTask";
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Check to see if we remove invocation information is requested
      if (enableThreadLocal)
      {
        // Set the thread local caller information
        callerInformation.set(new RemoteInvocationInformation(destinationSeid, softwareElement.getSeid(), header));
      }

      // Dispatcher the invocation
      invocation.dispatch(remoteSkeleton);

      // Marshall up a response
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
			if (invocation.getReturnCode().getErrCode() == 0)
			{
				// Return return
	      invocation.marshal(hbaos);
			}
      else
      {
        // Logger error return
        LoggerSingleton.logError(this.getClass(), "run", "method: " + invocation.getClass().getName() + " message: " + invocation.getErrorMessage());
      }

      // Send the reponse
      sendResponse(invocation.getReturnCode(), hbaos.toByteArray());

    }
    catch (HaviMarshallingException e)
    {
      // Problem
      sendResponse(UNIDENTIFIED_FAILURE, EMPTY_RESPONSE);
    }
  }

  /**
   * Send response while swallowing all exceptions
   * @param returnCode The return status to send
   * @param buffer The response data
   */
  private void sendResponse(Status returnCode, byte[] buffer)
  {

    try
    {
      // Send the reponse
      softwareElement.msgSendResponse(destinationSeid, header.getOpCode(), ConstTransferMode.SIMPLE, returnCode, buffer, header.getTransactionId());
    }
    catch (HaviException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "sendResponse", e.toString());
    }
  }
}
