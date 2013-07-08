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
 * $Id: AppEventNotificationHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstEventIdSchema;
import org.havi.system.types.AppEventId;
import org.havi.system.types.EventId;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

/**
 * @author stephen
 *
 */
public abstract class AppEventNotificationHelper extends EventManagerNotificationHelper
{
	private SEID eventSeid;

  /**
   * Constructor for AppEventNotificationHelper.
   * @param softwareElement
   * @param opCode
   * @param eventBase
   * @param seid
   * @param listener
   * @throws HaviMsgException
   */
  public AppEventNotificationHelper(SoftwareElement softwareElement, OperationCode opCode, short eventBase, SEID eventSeid) throws HaviMsgException
  {
  	// Construct super class
    super(softwareElement, opCode, eventBase);

		// Check vendor id
		if (eventSeid == null)
		{
			// badness
			throw new IllegalArgumentException("seid is null");
		}

		// Save the vendor id
		this.eventSeid = eventSeid;
  }

  /**
   * @see com.redrocketcomputing.havi.system.rmi.EventManagerNotificationHelper#receiveEventNotification(SEID, EventId, HaviByteArrayInputStream)
   */
  public boolean receiveEventNotification(SEID posterSeid, EventId eventId, HaviByteArrayInputStream payload)
  {
  	// Make sure this is a system event id
  	if (eventId.getDiscriminator() != ConstEventIdSchema.APP)
  	{
  		// Not for us
  		return false;
  	}
  	AppEventId appEventId = (AppEventId)eventId;

  	// Check the base
  	if (appEventId.getBase() != eventBase)
  	{
  		// Not for us
  		return false;
  	}

		// Check the vendor ID
		if (!eventSeid.equals(appEventId.getSeid()))
		{
			// Not for us
			return false;
		}

		// Forward to subclass
		return receiveAppEventNotification(posterSeid, appEventId, payload);
  }

	public abstract boolean receiveAppEventNotification(SEID posterSeid, AppEventId eventId, HaviByteArrayInputStream payload);
}
