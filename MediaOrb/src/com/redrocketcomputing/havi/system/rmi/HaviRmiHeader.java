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
 * $Id: HaviRmiHeader.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import java.io.IOException;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviImmutableObject;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;

/**
 * @author stephen
 *
 */
public class HaviRmiHeader extends HaviImmutableObject
{
  private OperationCode opCode;
  private byte controlFlags;
  private int transactionId;

  /**
   * Constructor for HaviRmiHeader.
   */
  public HaviRmiHeader(OperationCode opCode, byte controlFlags, int transactionId)
  {
    // Check the parameters
    if (opCode == null)
    {
      // Baddddd
      throw new IllegalArgumentException("opcode is null");
    }

    // Save the parameters
    this.opCode = opCode;
    this.controlFlags = controlFlags;
    this.transactionId = transactionId;
  }

  public HaviRmiHeader(HaviByteArrayInputStream hbais) throws HaviUnmarshallingException
  {
    try
    {
      // Unmarshall the operation code
      opCode = new OperationCode(hbais);

      // Unmarshall the control flag
      controlFlags = hbais.readByte();

      // Unmarshall the transaction id
      transactionId = hbais.readInt();
    }
    catch (IOException e)
    {
      // Transalte
      throw new HaviUnmarshallingException(e.toString());
    }
  }

  /**
   * @see java.lang.Object#clone()
   */
  protected Object clone() throws CloneNotSupportedException
  {
    return this;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
    // Check the type
    if (o instanceof HaviRmiHeader)
    {
      // Cast it up
      HaviRmiHeader other = (HaviRmiHeader)o;

      // Check stuff
      return opCode.equals(other.opCode) && controlFlags == other.controlFlags && transactionId == other.transactionId;
    }

    // Wrong type
    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return opCode.hashCode() + controlFlags + transactionId;
  }

  /**
   * @see org.havi.system.types.Marshallable#marshal(HaviByteArrayOutputStream)
   */
  public void marshal(HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    try
    {
      // Marshall the operation code
      opCode.marshal(hbaos);

      // Marshal the control flags
      hbaos.writeByte(controlFlags);

      // Marshall transaction id
      hbaos.writeInt(transactionId);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviMarshallingException(e.toString());
    }
  }

  /**
   * Returns the controlFlags.
   * @return byte
   */
  public final byte getControlFlags()
  {
    return controlFlags;
  }

  /**
   * Returns the opCode.
   * @return OperationCode
   */
  public final OperationCode getOpCode()
  {
    return opCode;
  }

  /**
   * Returns the transactionId.
   * @return int
   */
  public final int getTransactionId()
  {
    return transactionId;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "HaviRmiHeader[" + "OperationCode[apiCode: 0x" + Integer.toHexString(opCode.getApiCode() & 0xffff) + " operationId: 0x" + Integer.toHexString(opCode.getOperationId() & 0xff) + "] controlFlags: " + controlFlags + " transactionId: " + transactionId + ']';
  }
}
