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
 * $Id: MsgWatchOnNotificationHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstSoftwareElementHandle;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgSuperExistsException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.ListSet;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author david
 *
 */
public final class MsgWatchOnNotificationHelper extends NotificationHelper
{
  private final static OperationCode DEFAULT_MSG_WATCH_OPCODE = new OperationCode((short)ConstApiCode.MSG, (byte)0xff);
  
  private final static Status UNIDENTIFIED_FAILURE = new Status(ConstApiCode.MSG , ConstGeneralErrorCode.UNIDENTIFIED_FAILURE);
  private final static byte[] EMPTY_RESPONSE = new byte[0];
	private final MsgWatchOnNotificationListener[] EMPTY_LISTENER_ARRAY = new MsgWatchOnNotificationListener[0];

  private Map listenerMap = new ListMap();

  /**
   * Constructor for MessageBackHelper.
   * @param softwareElement The SoftwareElement to use for this watcher
   * @param opCode The OperationCode to use
   */
  public MsgWatchOnNotificationHelper(SoftwareElement softwareElement, OperationCode opCode) throws HaviException
  {
    super(softwareElement, opCode);
    
    // Bind to the software element
    softwareElement.addHaviListener(this, softwareElement.getSystemSeid(ConstSoftwareElementType.MESSAGING_SYSTEM));
  }
  
  /**
   * Constructor for MessageBackHelper.
   * @param softwareElement The SoftwareElement to use for this watcher
   * @param softwareElement
   */
  public MsgWatchOnNotificationHelper(SoftwareElement softwareElement) throws HaviException
  {
    // Forward
    this(softwareElement, DEFAULT_MSG_WATCH_OPCODE);
  }
  
  /**
   * Release all resources
   */
  public void close()
  {
    synchronized(listenerMap)
    {
      for (Iterator iterator = listenerMap.keySet().iterator(); iterator.hasNext();)
      {
        try
        {
          // Extract target SEID
          SEID element = (SEID)iterator.next();
          
          // Unwatch
          softwareElement.msgWatchOff(element, opCode);
        }
        catch (HaviMsgException e)
        {
          // Log warning
          LoggerSingleton.logWarning(this.getClass(), "close", e.toString());
        }
      }
      
      // Flush the map
      listenerMap.clear();
    }
    
    // Unbind
    softwareElement.removeHaviListener(this);
  }

  /**
   * Add a listener for the specified event. The listener must be of the correct type for the event or a IllegalArgumentException
   * will be thrown
   * @param eventId The event ID to listener on
   * @param listener The event listener
   */
  public void addListener(SEID targetSeid, MsgWatchOnNotificationListener listener)
  {
    synchronized(listenerMap)
    {
      // Try to get the listener set
      Set listenerSet = (Set)listenerMap.get(targetSeid);
      if (listenerSet == null)
      {
        // Create new list
        listenerSet = new ListSet();

        // Add to the map
        listenerMap.put(targetSeid, listenerSet);
      }

      // Add the listener to the set
      listenerSet.add(listener);
    }
  }

  /**
   * Add a listener for the specified event and turn watch on. The listener must be of the correct type for the event or a IllegalArgumentException
   * will be thrown
   * @param eventId The event ID to listener on
   * @param listener The event listener
   */
  public void addListenerEx(SEID targetSeid, MsgWatchOnNotificationListener listener) throws HaviMsgException
  {
    try
    {
      // Check for debug
      if (softwareElement.isDebug())
      {
        LoggerSingleton.logDebugFine(this.getClass(), "addListenerEx", softwareElement.getSeid() + ": adding " + targetSeid);
      }
      
      // Forward
      addListener(targetSeid, listener);

      // Turn on watch
      softwareElement.msgWatchOn(targetSeid, opCode);
    }
    catch (HaviMsgSuperExistsException e)
    {
      // Ignore, someone has already registered a watch against the opcode and target SEID
      // This means we will see the watch event
    }
    catch (HaviMsgException e)
    {
    	// Remove listener
    	removeListener(targetSeid, listener);

    	// Re-throw
    	throw e;
    }
  }

  /**
   * Remove the listener for the specified event.
   * @param eventId The event ID to which the listener is bound
   * @param listener The listener to remove
   */
  public void removeListener(SEID targetSeid, MsgWatchOnNotificationListener listener)
  {
    synchronized(listenerMap)
    {
      Set listenerSet = (Set)listenerMap.get(targetSeid);
      if (listenerSet != null)
      {
        // Remove the listener from the set
        listenerSet.remove(listener);

        // Check for empty set
        if (listenerSet.size() == 0)
        {
        	// Remove map entry
        	listenerMap.remove(targetSeid);
        }
      }
    }
  }

  /**
   * Remove the listener for the specified event and turn watch off
   * @param eventId The event ID to which the listener is bound
   * @param listener The listener to remove
   */
  public void removeListenerEx(SEID targetSeid, MsgWatchOnNotificationListener listener)
  {
    // Check for debug
    if (softwareElement.isDebug())
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "removeListenerEx", softwareElement.getSeid() + ": adding " + targetSeid);
    }
    
  	// Forward
  	removeListener(targetSeid, listener);

    try
    {
      // Try turn watch off
      softwareElement.msgWatchOff(targetSeid, opCode);
    }
    catch (HaviMsgException e)
    {
    	// Just log the error
    	LoggerSingleton.logError(this.getClass(), "removeListenerEx", e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.rmi.NotificationHelper#receiveNotification(SEID, HaviRmiHeader, HaviByteArrayInputStream)
   */
  public boolean receiveNotification(SEID sourceId, HaviRmiHeader header, HaviByteArrayInputStream payload)
  {
    try
    {
      // The source of this message should always be from the local messaging system.
      if (sourceId.getHandle() != ConstSoftwareElementHandle.MESSAGING_SYSTEM)
      {
        // Not for us
        return false;
      }

			// Unmarshall the payload
			SEID targetSeid = new SEID(payload);

      // Check for debug
      if (softwareElement.isDebug())
      {
        LoggerSingleton.logDebugCoarse(this.getClass(), "receiveNotification", softwareElement.getSeid() + ": target " + targetSeid + " " + listenerMap.size());
      }
      
      // Get the listeners, this is a one short
      MsgWatchOnNotificationListener[] listenerArray = getListeners(targetSeid);
      if(listenerArray.length == 0)
      {
        // Check for debug
        if (softwareElement.isDebug())
        {
          LoggerSingleton.logDebugFine(this.getClass(), "receiveNotification", softwareElement.getSeid() + ": no listeners");
        }

        return false;
      }

      // Dispatch to the listeners
      for (int i = 0; i < listenerArray.length; i++)
      {
        // Dispatch
        listenerArray[i].msgWatchOnNotification(targetSeid);
      }

      // We handle the message
      return true;
    }
    catch(HaviUnmarshallingException e)
    {
    	// Log the error
    	LoggerSingleton.logError(this.getClass(), "receiveNotification", e.toString());

      // Unable to unmarshal.  Did not handle
      return false;
    }
  }

  /**
   * Return an array of listener, possibly empty for the specified event ID, this is a one shot
   * and remove all matching entries
   * @param eventId The event ID to lookup the listeners for
   * @return EventNotificationListener[] The array of listeners, possible empty (length is 0)
   */
  private MsgWatchOnNotificationListener[] getListeners(SEID destSeid)
  {
    synchronized(listenerMap)
    {
      // Try to get the listener set
      Set listenerSet = (Set)listenerMap.remove(destSeid);
      if (listenerSet == null)
      {
      	return EMPTY_LISTENER_ARRAY;
      }

      // Convert to array
      return (MsgWatchOnNotificationListener[])listenerSet.toArray(new MsgWatchOnNotificationListener[listenerSet.size()]);
    }
  }
}
