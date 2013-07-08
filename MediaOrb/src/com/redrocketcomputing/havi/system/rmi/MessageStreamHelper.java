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
 * $Id: MessageStreamHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.havi.system.HaviListener;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.types.HaviByteArrayInputStream;
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
public abstract class MessageStreamHelper extends HaviListener
{
  private MessageStreamInvocationFactory invocationFactory;
  private Map listenerMap = new HashMap();

  /**
   * Constructor for MessageStreamHelper.
   */
  public MessageStreamHelper(MessageStreamInvocationFactory invocationFactory)
  {
    // Check the parameter
    if (invocationFactory == null)
    {
      // Bad
      throw new IllegalArgumentException("parameter is null");
    }

    this.invocationFactory = invocationFactory;
  }

  /**
   * @see org.havi.system.HaviListener#receiveMsg(boolean, byte, SEID, SEID, Status, HaviByteArrayInputStream)
   */
  public boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload)
  {
    //ignore if replied already
    if (haveReplied)
    {
      return false;
    }

    //ignore if not havi rmi
    if (protocolType != ConstProtocolType.MESSAGE_STREAM)
    {
      return false;
    }

    //try to handle.
    try
    {
      // Read opCode
      OperationCode opCode = new OperationCode(payload);

      // Try to create a remote invocation object
      MessageStreamInvocation invocation = invocationFactory.createInvocation(opCode, payload);
      if (invocation == null)
      {
        // Log error because the transactions match, but we could not find a invocation handler
        LoggerSingleton.logError(this.getClass(), "receiveMsg", "Could not find a invocation handler for " + opCode.toString());

        // Not for us
        return false;
      }

      // Dispatch to all listeners
      MessageStreamListener[] listenerArray = getListeners(opCode);

      if(listenerArray.length == 0)
      {
        return false;
      }

      for (int i = 0; i < listenerArray.length; i++)
      {
        // Dispatch
        invocation.dispatch(listenerArray[i]);
      }

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

  public void addListener(OperationCode opCode, MessageStreamListener listener)
  {
    synchronized(listenerMap)
    {
      // Try to get the listener set
      Set listenerSet = (Set)listenerMap.get(opCode);
      if (listenerSet == null)
      {
        // Create new list
        listenerSet = new HashSet();

        // Add to the map
        listenerMap.put(opCode, listenerSet);
      }

      // Add the listener to the set
      listenerSet.add(listener);
    }
  }

  /**
   * Remove the listener for the specified event.
   * @param eventId The event ID to which the listener is bound
   * @param listener The listener to remove
   */
  public void removeListener(OperationCode opCode, MessageStreamListener listener)
  {
    synchronized(listenerMap)
    {
      Set listenerSet = (Set)listenerMap.get(opCode);
      if (listenerSet != null)
      {
        // Remove the listener from the set
        listenerSet.remove(listener);
      }
    }
  }

  /**
   * Return an array of listener, possibly empty for the specified event ID
   * @param eventId The event ID to lookup the listeners for
   * @return EventNotificationListener[] The array of listeners, possible empty (length is 0)
   */
  private MessageStreamListener[] getListeners(OperationCode opCode)
  {
    synchronized(listenerMap)
    {
      // Try to get the listener set
      Set listenerSet = (Set)listenerMap.get(opCode);
      if (listenerSet == null)
      {
        // Create new list
        listenerSet = new HashSet();
      }

      // Convert to array
      return (MessageStreamListener[])listenerSet.toArray(new MessageStreamListener[listenerSet.size()]);
    }
  }

}
