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
 * $Id: HaviReliableNoackMessage.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.message;

import java.io.IOException;

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
public class HaviReliableNoackMessage extends HaviMessage
{
  private int code = -1;

  /**
   * Constructor for HaviReliableNoackMessage.
   */
  public HaviReliableNoackMessage()
  {
    // Construct super class
    super();

    // Set the message type
    this.messageType = ConstTransferProtocolMessageTypes.RELIABLE_NOACK;
  }

  /**
   * Constructor for HaviReliableNoackMessage.
   * @param destination
   * @param source
   * @param protocolType
   * @param messageNumber
   * @param code
   */
  public HaviReliableNoackMessage(SEID destination, SEID source, int protocolType, int messageNumber, int code)
  {
    // Contruct super
    super();

    // Initialize base class
    this.destination = destination;
    this.source = source;
    this.protocolType = protocolType;
    this.messageType = ConstTransferProtocolMessageTypes.RELIABLE_NOACK;
    this.messageNumber = messageNumber & 0xff;

    // Save the noack code
    this.code = code;
  }

  /**
   * Constructor for HaviReliableNoackMessage.
   * @param hbais
   * @throws HaviUnmarshallingException
   */
  public HaviReliableNoackMessage(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Unmarshall
    unmarshal(hbais);
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return new HaviReliableNoackMessage(destination, source, protocolType, messageNumber, code);
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    // Check type
    if (o instanceof HaviReliableNoackMessage)
    {
      // Cast it up
      HaviReliableNoackMessage other = (HaviReliableNoackMessage)o;
      // Ask super class
      return super.equals(o) && code == other.code;
    }

    // Wrong type
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return super.hashCode() + code + 55700;
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    try
    {
      super.marshal(hbaos);

      // Write message length
      hbaos.writeInt(4);

      // Write the code
      hbaos.writeByte(code);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviMarshallingException(e.toString());
    }
  }

  /**
   * @see org.havi.system.types.Marshallable#unmarshal(HaviByteArrayInputStream)
   */
  public void unmarshal(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    try
    {
      super.unmarshal(hbais);

      // Skip the length
      hbais.skip(4);

      // Read the code
      code = hbais.readByte();
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
  }

  /**
   * Returns the code.
   * @return int
   */
  public int getCode()
  {
    return code;
  }

  /**
   * Sets the code.
   * @param code The code to set
   */
  public void setCode(int code)
  {
    this.code = code;
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessage#dispatch(HaviMessageHandler)
   */
  public void dispatch(HaviMessageHandler handler)
  {
    handler.handleReliableNoackMessage(this);
  }

}
