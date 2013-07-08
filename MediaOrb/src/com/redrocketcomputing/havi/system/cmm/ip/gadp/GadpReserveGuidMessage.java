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
 * $Id: GadpReserveGuidMessage.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import java.io.IOException;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class GadpReserveGuidMessage extends GadpMessage
{
  public final static int MARSHAL_TYPE = 1;

  private GUID guid = GUID.ZERO;
  private int timestamp = -1;

  /**
   * Constructor for GadpReserveGuidMessage.
   */
  public GadpReserveGuidMessage()
  {
  }

  public GadpReserveGuidMessage(GUID guid, int timestamp)
  {
    // Check the parameters
    if (guid == null)
    {
      throw new IllegalArgumentException("guid can not be null");
    }

    // Save the parameters
    this.guid = guid;
    this.timestamp = timestamp;
  }

  public GadpReserveGuidMessage(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    // Forward
    unmarshal(hbais);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpMessage#dispatch(GadpMessageHandler)
   */
  public void dispatch(GadpMessageHandler handler)
  {
    // Forward
    handler.handleReserveGuid(guid, timestamp);
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    // Check type
    if (o instanceof GadpReserveGuidMessage)
    {
      // Cast it up
      GadpReserveGuidMessage other = (GadpReserveGuidMessage)o;

      // Check fields
      return guid.equals(other.guid) && timestamp == other.timestamp;
    }

    // Not equal
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return guid.hashCode() + timestamp + 820934073;
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    try
    {
      // Marshal the super class
      super.marshal(hbaos);

      // Marshal the guid and timestamp
      guid.marshal(hbaos);
      hbaos.writeInt(timestamp);
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
      // Unmarshall the super class
      super.unmarshal(hbais);

      // Unmarhsall the guid and timestamp
      guid = new GUID(hbais);
      timestamp = hbais.readInt();
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviUnmarshallingException(e.toString());
    }
  }

  /**
   * Returns the guid.
   * @return GUID
   */
  public GUID getGuid()
  {
    return guid;
  }

  /**
   * Returns the timestamp.
   * @return int
   */
  public int getTimestamp()
  {
    return timestamp;
  }

  /**
   * Sets the guid.
   * @param guid The guid to set
   */
  public void setGuid(GUID guid)
  {
    this.guid = guid;
  }

  /**
   * Sets the timestamp.
   * @param timestamp The timestamp to set
   */
  public void setTimestamp(int timestamp)
  {
    this.timestamp = timestamp;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "GadpReserveGuidMessage: " + guid + ' ' + timestamp;
  }
}
