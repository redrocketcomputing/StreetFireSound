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
 * $Id: EventManagerProtocolListener.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.em;

import java.io.IOException;

import org.havi.system.HaviListener;
import org.havi.system.constants.ConstEventManagerOperationId;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.constants.ConstSoftwareElementHandle;
import org.havi.system.types.EventId;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviEventManagerException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author george
 *
 */
class EventManagerProtocolListener extends HaviListener
{

  EventManager eventManager = null;

  public EventManagerProtocolListener(EventManager eventManager)
  {
    // Contruct super class
    super();
    this.eventManager = eventManager;
  }

  /**
   * @see org.havi.system.HaviListener#receiveMsg(boolean, byte, SEID, SEID, Status, HaviByteArrayInputStream)
   */
  public boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload)
  {
    // Make sure this is a event manage protocol message
    if (protocolType == ConstProtocolType.EVENT_MANAGER)
    {
      try
      {
        // Check source SEID
        if (sourceId.getHandle() != ConstSoftwareElementHandle.EVENT_MANAGER)
        {
          // Not for us
          return false;
        }

        // Umarshal the opcode
        OperationCode opCode = new OperationCode(payload);
        if (!opCode.equals(ConstEventManagerOperationId.FORWARD_EVENT_OPCODE))
        {
          // Not for us
          return false;
        }

        // Unmarshall the event
        SEID poster = new SEID(payload);
        EventId eventid = EventId.create(payload);
        byte[] info = new byte[payload.readInt()];
        payload.read(info);

        // Forward to the event manager
        eventManager.forwardEvent(poster, eventid, info);

				// All done
        return true;
      }
      catch (IOException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "receiveMsg", e.toString());
      }
      catch (HaviEventManagerException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "receiveMsg", e.toString());
      }
      catch (HaviUnmarshallingException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "receiveMsg", e.toString());
      }
    }

    // We did not handle this
    return false;
  }
}
