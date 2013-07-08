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
 * $Id: SourceSeid.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.transfer;

import gnu.trove.TIntArrayList;

import org.havi.system.types.HaviMsgBusyException;
import org.havi.system.types.SEID;

/**
 * @author stephen
 *
 */
class SourceSeid
{
  private SEID seid;
  private TIntArrayList allocated;
  private int currentMessageNumber = 0;
  private int maxOutstanding;

  /**
   * Constructor for SourceSeid.
   * @param seid The SEID for this local SEID
   * @param maxOutstanding The max number of outstanding message from this SEID
   */
  public SourceSeid(SEID seid, int maxOutstanding)
  {
    // Check the SEID
    if (seid == null)
    {
      // Bad
      throw new IllegalArgumentException("seid can not be null");
    }

    // Check range
    if (maxOutstanding < 1 || maxOutstanding > 255)
    {
      // Badness
      throw new IllegalArgumentException("maxOutstanding is out of range: " + maxOutstanding);
    }

    // Save the parameters
    this.seid = seid;
    this.maxOutstanding = maxOutstanding;

    // Allocate the allocated led
    allocated = new TIntArrayList(maxOutstanding);
  }

  /**
   * Allocate the next message number
   * @return int The allocated message number
   * @throws HaviMsgBusyException Thrown there are no available message numbers
   */
  public synchronized int allocateMessageNumber() throws HaviMsgBusyException
  {
    // Check current number of outstanding
    if (allocated.size() >= maxOutstanding)
    {
      // Umnm,
      throw new HaviMsgBusyException("too many outstanding messages from " + seid);
    }

    // Update the current message number
    currentMessageNumber = (currentMessageNumber + 1) % 255;

    // Check to see current message number is already allocated, this means that while there is room for the message, the next message is already in use
    if (allocated.contains(currentMessageNumber))
    {
      // Umnm,
      throw new HaviMsgBusyException("the next message number is not available from " + seid);
    }

    // Add to allocated list
    allocated.add(currentMessageNumber);

    // Return the message number
    return currentMessageNumber;
  }

  /**
   * Release a message number
   * @param messageNumber The message number to release
   */
  public synchronized void releaseMessageNumber(int messageNumber)
  {
    // Try to find the message number
    int offset = allocated.indexOf(messageNumber);
    if (offset == -1)
    {
      // Bad, we are very confused
      throw new IllegalStateException("tried to release a message number not allocated from " + seid);
    }

    // Remove the message number
    allocated.remove(offset);
  }

  /**
   * @see java.lang.Object#toString()
   */
  public synchronized String toString()
  {
    // Create header
    StringBuffer buffer = new StringBuffer(seid.toString());
    buffer.append(": allocated[");
    buffer.append(allocated.size());
    buffer.append(']');

    // Add allocated message numbers
    for (int i = 0; i < allocated.size(); i++)
    {
      // Append the message number
      buffer.append(allocated.get(i));

      // Check for end of the list
      if (i != allocated.size() - 1)
      {
        buffer.append(',');
      }
    }

    // Return the string
    return buffer.toString();
  }

  /**
   * Returns the seid.
   * @return SEID
   */
  public SEID getSeid()
  {
    return seid;
  }

}
