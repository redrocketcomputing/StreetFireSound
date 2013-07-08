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
 * $Id: OpenSeidTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms;

import java.util.HashMap;
import java.util.Map;

import org.havi.system.MsgCallback;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviMsgAllocException;
import org.havi.system.types.HaviMsgSeidException;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.event.SeidLeaveEvent;

/**
 * @author stephen
 *
 */
class OpenSeidTable
{
  private GUID localGuid;
  private Map table = new HashMap();
  private int nextHandle = 256;
  private EventDispatch eventDispatcher;

  /**
   * Constructor for OpenSeidTable.
   * @param localGuid The GUID of this device
   */
  public OpenSeidTable(GUID localGuid)
  {
    // Check the guid
    if (localGuid == null)
    {
      // Badness
      throw new IllegalArgumentException("local guid can not be null");
    }

    // Save the parameter
    this.localGuid = localGuid;

    // Get the event dispatcher
    eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
    if (eventDispatcher == null)
    {
      // Really bad
      throw new IllegalStateException("can not find event dispatch service");
    }
  }

  /**
   * Open a new SEID using the specified callback
   * @param callback The callback for the new SEID
   * @return SEID The new SEID
   * @throws HaviMsgAllocException Thrown if no handles remain
   */
  public synchronized SEID open(MsgCallback callback) throws HaviMsgAllocException
  {
    // Check parameters
    if (callback == null)
    {
      // More badness
      throw new IllegalArgumentException("callback can not be null");
    }

    // Check to see if there is an available handle
    if (nextHandle > 655535)
    {
      // Nope
      throw new HaviMsgAllocException("no more handles available");
    }

    // Create the SEID
    SEID seid = new SEID(localGuid, (short)(nextHandle & 0xffff));

    // Update the handle
    nextHandle++;

    // Add to the table
    table.put(seid, callback);

    // Return the new SEID
    return seid;
  }

  /**
   * Open a new system SEID using the specified callback and type
   * @param callback The callback for the new SEID
   * @param type The new system SEID type
   * @return SEID The new SEID
   * @throws HaviMsgAllocException Thrown if the system SEID is already registered
   */
  public synchronized SEID sysOpen(MsgCallback callback, int type) throws HaviMsgAllocException
  {
    // Check parameters
    if (callback == null)
    {
      // More badness
      throw new IllegalArgumentException("callback can not be null");
    }

    // Check type range
    if (type < 0 || type > 255)
    {
      // More and more badness
      throw new IllegalArgumentException("type is out of range: " + type);
    }

    // Build the SEID
    SEID seid = new SEID(localGuid, (short)(type & 0xff));

    // Make sure we have not already registered this callback
    if (table.containsKey(seid))
    {
      // Opps
      throw new HaviMsgAllocException(seid.toString() + " already exists");
    }

    // Add to the table
    table.put(seid, callback);

    // All done, return the new SEID
    return seid;
  }

  /**
   * Release the SEID and remove from the table of open SEID
   * @param seid The SEID to close
   * @throws HaviMsgSeidException Thrown if the SEID is not in the internal table
   */
  public synchronized void close(SEID seid) throws HaviMsgSeidException
  {
    // Check the parameter
    if (seid == null)
    {
      // More badness
      throw new IllegalArgumentException("seid can not be null");
    }

    // Remove it
    if (table.remove(seid) == null)
    {
      // Not found
      throw new HaviMsgSeidException(seid.toString() + " not found");
    }

    // Fire leave event
    eventDispatcher.dispatch(new SeidLeaveEvent(seid));
  }

  /**
   * Lookup the callback for the specified SEID
   * @param seid The SEID of the callback to lookup
   * @return MsgCallback The callback or null if not found
   */
  public synchronized MsgCallback get(SEID seid)
  {
    // Check the parameter
    if (seid == null)
    {
      // More badness
      throw new IllegalArgumentException("seid can not be null");
    }

    // Lookup the seid
    return (MsgCallback)table.get(seid);
  }
}
