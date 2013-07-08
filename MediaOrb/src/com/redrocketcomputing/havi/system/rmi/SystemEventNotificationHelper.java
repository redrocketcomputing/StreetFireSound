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
 * $Id: SystemEventNotificationHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstEventIdSchema;
import org.havi.system.types.EventId;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;

/**
 * @author stephen
 *
 */
public abstract class SystemEventNotificationHelper extends EventManagerNotificationHelper
{
  /**
   * Constructor for SystemEventNotificationHelper.
   * @param softwareElement
   * @param opCode
   * @param listener
   * @throws HaviMsgException
   */
  public SystemEventNotificationHelper(SoftwareElement softwareElement, OperationCode opCode, short eventBase) throws HaviMsgException
  {
  	// Construct super class
    super(softwareElement, opCode, eventBase);
  }

  /**
   * @see com.redrocketcomputing.havi.system.rmi.EventManagerNotificationHelper#receiveEventNotification(SEID, EventId, HaviByteArrayInputStream)
   */
  public boolean receiveEventNotification(SEID posterSeid, EventId eventId, HaviByteArrayInputStream payload)
  {

  	// Make sure this is a system event id
  	if (eventId.getDiscriminator() != ConstEventIdSchema.SYSTEM)
  	{
  		// Not for us
  		return false;
  	}



  	SystemEventId systemEventId = (SystemEventId)eventId;

  	// Check the base
  	if (systemEventId.getBase() != eventBase)
  	{
  		// Not for us
  		return false;
  	}

		// Forward to subclass
		return receiveSystemEventNotification(posterSeid, systemEventId, payload);
  }

  /**
   * Method receiveSystemEventNotificaiton.
   * @param posterSeid
   * @param eventId
   * @param payload
   * @return boolean
   */
	public abstract boolean receiveSystemEventNotification(SEID posterSeid, SystemEventId eventId, HaviByteArrayInputStream payload);

}
