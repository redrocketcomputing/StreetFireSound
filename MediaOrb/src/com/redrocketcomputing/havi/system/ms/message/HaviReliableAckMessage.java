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
 * $Id: HaviReliableAckMessage.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.message;

import org.havi.system.constants.ConstTransferProtocolMessageTypes;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.SEID;

/**
 * @author Stephen
 *
 */
public class HaviReliableAckMessage extends HaviMessage
{
  /**
   * Constructor for HaviReliableAckMessage.
   */
  public HaviReliableAckMessage()
  {
    // Construct super class
    super();

    // Initialize message type
    this.messageType = ConstTransferProtocolMessageTypes.RELIABLE_ACK;
  }

  /**
   * Constructor for HaviReliableAckMessage.
   * @param destination The destination SEID
   * @param source The source SEID
   * @param protocolType The protocol type for the message
   * @param messageNumber The message number of the message
   */
  public HaviReliableAckMessage(SEID destination, SEID source, int protocolType, int messageNumber)
  {
    // Contruct super class
    super();

    // Initialize base class
    this.destination = destination;
    this.source = source;
    this.protocolType = protocolType;
    this.messageType = ConstTransferProtocolMessageTypes.RELIABLE_ACK;
    this.messageNumber = messageNumber & 0xff;
  }

  /**
   * Constructor for HaviReliableAckMessage.
   * @param hbais
   * @throws HaviUnmarshallingException
   */
  public HaviReliableAckMessage(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    super(hbais);
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return new HaviReliableAckMessage(destination, source, protocolType, messageNumber);
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    // Check type
    if (o instanceof HaviReliableAckMessage)
    {
      // Ask super class
      return super.equals(o);
    }

    // Wrong type
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // Ask super type
    return super.hashCode() + 23823984;
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    super.marshal(hbaos);
  }

  /**
   * @see org.havi.system.types.Marshallable#unmarshal(HaviByteArrayInputStream)
   */
  public void unmarshal(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    super.unmarshal(hbais);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessage#dispatch(HaviMessageHandler)
   */
  public void dispatch(HaviMessageHandler handler)
  {
    handler.handleReliableAckMessage(this);
  }

}
