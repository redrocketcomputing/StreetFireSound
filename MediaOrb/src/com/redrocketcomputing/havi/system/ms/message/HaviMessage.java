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
 * $Id: HaviMessage.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.message;

import java.io.IOException;

import org.havi.system.constants.ConstTransferProtocolMessageTypes;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviObject;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.SEID;


/**
 * @author Stephen
 *
 */
public abstract class HaviMessage extends HaviObject
{
  protected final static byte[] EMPTY = new byte[0];

  protected SEID destination = SEID.ZERO;
  protected SEID source = SEID.ZERO;
  protected int protocolType = -1;
  protected int messageType = -1;
  protected int messageNumber = -1;

  /**
   * Factory method for reading HAVI messages.  This should be redesigned to be extensible but I'm not
   * sure what we would do the a sub-type in the MessagingSystemService and the interface pass out byte arrays
   * anyways.
   * @param hbais The input stream to unmarshall the message from
   * @return HaviMessage The correct sub-type created by the factory
   * @throws HaviUnmarshallingException Thrown if there is a error decoding the message
   */
  public static HaviMessage create(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    try
    {
      // Mark current position
      hbais.mark(0);

      // Skip to the message type
      hbais.skip(25);

      // Unmarshall the message type information
      int messageType = hbais.readByte();

      // Reset the input stream
      hbais.reset();

      // Create correct message type
      switch (messageType)
      {
        case ConstTransferProtocolMessageTypes.RELIABLE_ACK:
        {
          // Build and return the message
          return new HaviReliableAckMessage(hbais);
        }

        case ConstTransferProtocolMessageTypes.SIMPLE:
        {
          // Ask simple factory to create the message
          return new HaviSimpleMessage(hbais);
        }

        case ConstTransferProtocolMessageTypes.RELIABLE:
        {
          // Ask the relialble factory to create the message
          return new HaviReliableMessage(hbais);
        }

        case ConstTransferProtocolMessageTypes.RELIABLE_NOACK:
        {
          // Build and return the message
          return new HaviReliableNoackMessage(hbais);
        }

        default:
        {
          // Unknown message type
          throw new HaviUnmarshallingException("unknown message type: " + messageType);
        }
      }
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
  }

  /**
   * Constructor for HaviMessage.
   */
  protected HaviMessage()
  {
    super();
  }

  protected HaviMessage(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Unmarshall the message header
    unmarshal(hbais);
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    // throw unsupported
    throw new CloneNotSupportedException();
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    // Check type
    if (o instanceof HaviMessage)
    {
      // Cast it up
      HaviMessage other = (HaviMessage)o;

      // Check members
      return destination.equals(other.destination) && source.equals(other.source) && protocolType == other.protocolType && messageType == other.messageType && messageNumber == other.messageNumber;
    }

    // Wrong type
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // Use members
    return destination.hashCode() + source.hashCode() + (protocolType << 16) + (messageType << 8) + messageNumber + 925307;
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    try
    {
      // Marhsall the header
      hbaos.writeInt(0);
      destination.marshal(hbaos);
      source.marshal(hbaos);
      hbaos.writeByte(protocolType);
      hbaos.writeByte(messageType);
      hbaos.writeByte(messageNumber);

      // Write reserved byte
      hbaos.writeByte(0);
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
      // Unmarshall the header
      hbais.skip(4);
      destination = new SEID(hbais);
      source = new SEID(hbais);
      protocolType = hbais.readByte();
      messageType = hbais.readByte();
      messageNumber = hbais.readUnsignedByte();

      // Skip the reserved byte
      hbais.skip(1);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
  }

  /**
   * Returns the destination.
   * @return SEID
   */
  public SEID getDestination()
  {
    return destination;
  }

  /**
   * Returns the messageNumber.
   * @return int
   */
  public int getMessageNumber()
  {
    return messageNumber;
  }

  /**
   * Returns the messageType.
   * @return int
   */
  public int getMessageType()
  {
    return messageType;
  }

  /**
   * Returns the protocolType.
   * @return int
   */
  public int getProtocolType()
  {
    return protocolType;
  }

  /**
   * Returns the source.
   * @return SEID
   */
  public SEID getSource()
  {
    return source;
  }

  /**
   * Sets the destination.
   * @param destination The destination to set
   */
  public void setDestination(SEID destination)
  {
    this.destination = destination;
  }

  /**
   * Sets the messageNumber.
   * @param messageNumber The messageNumber to set
   */
  public void setMessageNumber(int messageNumber)
  {
    this.messageNumber = messageNumber;
  }

  /**
   * Sets the protocolType.
   * @param protocolType The protocolType to set
   */
  public void setProtocolType(int protocolType)
  {
    this.protocolType = protocolType;
  }

  /**
   * Sets the source.
   * @param source The source to set
   */
  public void setSource(SEID source)
  {
    this.source = source;
  }

  /**
   * Method the peek at a message body.
   * @param offset The byte to peek at
   * @return int Return the value at the specified offset if the actual type has a message byte, otherwise -1
   */
  public int peek(int offset)
  {
    return -1;
  }

  /**
   * Dispatch a handler using a modified visitor pattern
   * @param handler The handler for the message
   */
  public abstract void dispatch(HaviMessageHandler handler);
}
