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
 * $Id: EventManagerNotificationHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.util.Collections;
import java.util.Set;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.constants.ConstTransferMode;
import org.havi.system.types.EventId;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.ListSet;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public abstract class EventManagerNotificationHelper extends NotificationHelper
{
  private final static Status OK = new Status(ConstApiCode.ANY, ConstGeneralErrorCode.SUCCESS);
  private final static Status UNIDENTIFIED_FAILURE = new Status(ConstApiCode.ANY, ConstGeneralErrorCode.UNIDENTIFIED_FAILURE);
  private final static byte[] EMPTY_RESPONSE = new byte[0];

  private SEID eventManagerSeid;
	protected Set listenerSet = Collections.synchronizedSet(new ListSet());
	protected int eventBase;

  /**
   * Constructor for EventManagerNotificationHelper.
   * @param softwareElement The software element the helper is bound to
   * @param opCode The OperationCode to listen for notification on
   * @throws HaviGeneralException Thrown if there is a problem get the EventManager SEID
   */
  public EventManagerNotificationHelper(SoftwareElement softwareElement, OperationCode opCode, short eventBase) throws HaviMsgException
  {

  	// Construct super class
  	super(softwareElement, opCode);

  	// Save the event base
  	this.eventBase = eventBase & 0xffff;

  	// Construct event manager seid
  	eventManagerSeid = softwareElement.getSystemSeid(ConstSoftwareElementType.EVENTMANAGER);
  }

  /**
   * Release all resources
   */
	public void close()
	{
    // Remove all listeners
    listenerSet.clear();
	}

  /**
   * Add the specified EventNotificationListener
   * @param listener The EventNotificationLister to add
   */
  public void addListener(EventNotificationListener listener)
  {
    // Add the listener
    listenerSet.add(listener);
  }

  /**
   * Remove the specified EventNotificationListener
   * @param listener The EventNotificationLister to remove
   */
  public void removeListener(EventNotificationListener listener)
  {
    // Add the listener
    listenerSet.remove(listener);
  }
  
  /**
   * Return the number of listeners attached to the helper
   * @return The number of listenerss
   */
  public final int size()
  {
    return listenerSet.size();
  }

  /**
   * @see com.redrocketcomputing.havi.system.rmi.NotificationHelper#receiveNotification(SEID, SEID, HaviRmiHeader, HaviByteArrayInputStream)
   */
  public boolean receiveNotification(SEID sourceId, HaviRmiHeader header, HaviByteArrayInputStream payload)
  {
    // Check to make sure this is from the event manager
    if (!eventManagerSeid.equals(sourceId))
    {
      // Not for us
      return false;
    }


    try
    {
      // Unmarshal the poster seid event id
      SEID posterSeid = new SEID(payload);
      EventId eventId = EventId.create(payload);

			// Skip the length field
			payload.skip(4);

      // Forward to subclass
      return receiveEventNotification(posterSeid, eventId, payload);
    }
    catch (HaviUnmarshallingException e)
    {
      // Problem
      sendResponse(sourceId, opCode, UNIDENTIFIED_FAILURE, header.getTransactionId());

      // We handled it
      return true;
    }
  }

	public abstract boolean receiveEventNotification(SEID posterSeid, EventId eventId, HaviByteArrayInputStream payload);

  /**
   * Send a response to the specified destination, while swallowing all exception, because we can not do
   * anything about them.
   * @param destinationId The destination software element
   * @param opCode The original OperationCode
   * @param returnCode The response status to send
   * @param transactionId The original transaction ID
   */
  private void sendResponse(SEID destinationId, OperationCode opCode, Status returnCode, int transactionId)
  {
    try
    {
      // Send ok response
      softwareElement.msgSendResponse(destinationId, opCode, ConstTransferMode.SIMPLE, returnCode, EMPTY_RESPONSE, transactionId);
    }
    catch (HaviException e)
    {
      // Just log error
      LoggerSingleton.logError(this.getClass(), "sendResponse", e.toString());
    }
  }
}
