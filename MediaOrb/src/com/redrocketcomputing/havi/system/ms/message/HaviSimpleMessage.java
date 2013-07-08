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
 * $Id: HaviSimpleMessage.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
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
public class HaviSimpleMessage extends HaviMessage
{
  private int messageLength = -1;
  private byte[] body = EMPTY;

  /**
   * Constructor for HaviSimpleMessage.
   */
  public HaviSimpleMessage()
  {
    // Construct super
    super();

    // Set the message type
    this.messageType = ConstTransferProtocolMessageTypes.SIMPLE;
  }

  /**
   * Constructor for HaviSimpleMessage.
   * @param destination
   * @param source
   * @param protocolType
   * @param messageNumber
   * @param body
   */
  public HaviSimpleMessage(SEID destination, SEID source, int protocolType, int messageNumber, byte[] body)
  {
    // Construct super class
    super();

    // Initialize base class
    this.destination = destination;
    this.source = source;
    this.protocolType = protocolType;
    this.messageType = ConstTransferProtocolMessageTypes.SIMPLE;
    this.messageNumber = messageNumber & 0xff;
    this.messageLength = body.length;

    // Create copy of the body
    this.body = new byte[body.length];
    System.arraycopy(body, 0, this.body, 0, body.length);
  }

  /**
   * Constructor for HaviSimpleMessage.
   * @param hbais
   * @throws HaviUnmarshallingException
   */
  public HaviSimpleMessage(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Forward
    unmarshal(hbais);
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return new HaviSimpleMessage(destination, source, protocolType, messageNumber, body);
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    // Check type
    if (o instanceof HaviSimpleMessage)
    {
      // Cast it up
      HaviSimpleMessage other = (HaviSimpleMessage)o;

      // Check the length
      if (messageLength != other.messageLength)
      {
        return false;
      }

      // Loop through the body
      for (int i = 0; i < body.length; i++)
      {
        if (body[i] != other.body[i])
        {
          return false;
        }
      }

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
    // Sum the bytes
    int sum = 0;
    for (int i = 0; i < body.length; i++)
    {
      sum += body[i];
    }

    // Ask super and add message length an sum
    return super.hashCode() + messageLength + sum + 70392;
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    try
    {
      // Marshall super class
      super.marshal(hbaos);

      // Marshal the data
      hbaos.writeInt(messageLength);
      hbaos.write(body);
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
      // Unmarhsall super class
      super.unmarshal(hbais);

      // Unmarshal data
      messageLength = hbais.readInt();
      body = new byte[messageLength];
      hbais.read(body);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
  }

  /**
   * Returns the body.
   * @return byte[]
   */
  public byte[] getBody()
  {
    return body;
  }

  /**
   * Returns the messageLength.
   * @return int
   */
  public int getMessageLength()
  {
    return messageLength;
  }

  /**
   * Sets the body.
   * @param body The body to set
   */
  public void setBody(byte[] body)
  {
    messageLength = body.length;

    // Create copy of the body
    this.body = new byte[body.length];
    System.arraycopy(body, 0, this.body, 0, body.length);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessage#peek(int)
   */
  public int peek(int offset)
  {
    return body[offset] & 0xff;
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessage#dispatch(HaviMessageHandler)
   */
  public void dispatch(HaviMessageHandler handler)
  {
    handler.handleSimpleMessage(this);
  }

}
