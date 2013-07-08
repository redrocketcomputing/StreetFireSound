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
 * $Id: GarpEntry.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

/**
 * Description:
 *
 * @author stephen Jul 25, 2003
 * @version 1.0
 *
 */
public class GarpEntry
{
  private final static int DEFAULT_TIME_TO_LIVE = 5;
  private byte[] address;
  private int port;
  private boolean active;
  private int timeToLive;

  /**
   * Constructor for GarpEntry.
   */
  public GarpEntry(byte[] address, int port, boolean active)
  {
    // Save the information
    this.address = address;
    this.port = port;
    this.active = active;
    this.timeToLive = DEFAULT_TIME_TO_LIVE;
  }

  /**
   * Returns the active.
   * @return boolean
   */
  public boolean isActive()
  {
    return active;
  }

  /**
   * Returns the ipAddress.
   * @return byte[] The IP address
   */
  public byte[] getAddress()
  {
    return address;
  }

  /**
   * Sets the active flag for this entry
   * @param active True for an active device and false otherwise
   */
  public void setActive(boolean active)
  {
    this.active = active;
  }

  /**
   * Returns the IP port.
   * @return int The IP port for the entry
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Reset the time to live counter
   */
  public void resetTimeToLive()
  {
    this.timeToLive = DEFAULT_TIME_TO_LIVE;
  }

  public boolean hasExpired()
  {
    return timeToLive == 0;
  }

  /**
   * Decrement the time to live counter
   */
  public void closerToDeath()
  {
    timeToLive--;
  }

  public String toString()
  {
    return "GarpEntry[active: " + active + " address: " + (address[0] & 0xff) + '.' + (address[1] & 0xff) + '.' + (address[2] & 0xff) + '.' + (address[3] & 0xff) + " port: " + port + ']';
  }
}
