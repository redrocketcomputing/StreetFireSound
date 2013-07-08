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
 * $Id: HaviListener.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package org.havi.system;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

/**
 * <p>Title: HaviListener</p>
 * <p>Description: Base class for all software element listeners.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public abstract class HaviListener
{
  /**
   * Empyt constructor
   */
  public HaviListener()
  {
  }

  /**
   * This abstract method should be implemented by all listeners. This could be
   * a good place to act as a filter for incoming messages and provide
   * additional functionality. This method will be called by an
   * implementation-dependent dispatcher after it ACKs the incoming message to
   * the messaging system. "payload" corresponds to the "MessageBody" (the
   * fields after the "MessageLength" field) in the message data which is
   * mapped using the "General Message Representation". For a given message
   * received by a SoftwareElement object, the haveReplied argument of the
   * listeners' receiveMsg method shallbe set to false until a listener has
   * returned true. After a listener has returned true, all following listeners'
   * receiveMsg shall have haveReplied set to true. The parameters sourceId,
   * destId, state and payload correspond to the sourceId, destId, state and buffer
   * parameters used in the MsgCallback API of the Messaging System. The implementer
   * should be aware that during the time that this methods blocks, the related SoftwareElement
   * may not be able to process other incoming messages.
   * @param haveReplied
   * @param protocolType
   * @param sourceId
   * @param destId
   * @param state
   * @param payload
   * @return
   */
  public abstract boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload);
}
