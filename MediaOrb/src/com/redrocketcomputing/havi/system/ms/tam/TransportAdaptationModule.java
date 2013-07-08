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
 * $Id: TransportAdaptationModule.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.tam;

import org.havi.system.types.HaviMsgException;

import com.redrocketcomputing.havi.system.ms.message.HaviMessage;

/**
 * @author Stephen Street
 */

public interface TransportAdaptationModule
{
  /**
   * Send a message via the installed CMM
   * @param message The message to send
   * @throws HaviMsgException Thrown is an error send the message to the remote device is detected
   */
  public void send(HaviMessage message) throws HaviMsgException;
}
