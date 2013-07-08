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
 * $Id: GuidUtil.java,v 1.2 2005/02/23 19:58:47 stephen Exp $
 */

package com.redrocketcomputing.havi.util;

import org.havi.system.types.GUID;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GuidUtil
{
  /**
   * Extract the IP address of the GUID
   * @param guid The GUID to extract the IP address
   * @return The IP address string
   */
  public static String extractIpAddress(GUID guid)
  {
    // Get the GUID bytes
    byte[] rawGuid = guid.getValue();

    // Convert the first 4 bytes in an IP address string
    StringBuffer buffer = new StringBuffer();
    buffer.append(rawGuid[0] & 0xff);
    buffer.append('.');
    buffer.append(rawGuid[1] & 0xff);
    buffer.append('.');
    buffer.append(rawGuid[2] & 0xff);
    buffer.append('.');
    buffer.append(rawGuid[3] & 0xff);

    // Return the string
    return buffer.toString();
  }

  /**
   * Extract the IP port number from the GUID
   * @param guid The GUID to extract the IP port number from
   * @return The IP port number
   */
  public static int extractIpPort(GUID guid)
  {
    byte[] rawGuid = guid.getValue();
    return ((rawGuid[4] & 0xff) << 24) + ((rawGuid[5] & 0xff) << 16) + ((rawGuid[6] & 0xff) << 8) + (rawGuid[7] & 0xff);
  }
  
  public static GUID fromString(String guidString)
  {
    byte[] rawGuid = new byte[GUID.SIZE];
    String temp = new String(guidString);
    int position = 0;
    do
    {
      int index = temp.indexOf(":");
      rawGuid[position++] = (byte)(Integer.parseInt(temp.substring(0, index), 16) & 0xff);
      temp = temp.substring(index + 1);
    } 
    while (position < 7);
    rawGuid[position++] = (byte)(Integer.parseInt(temp, 16) & 0xff);
    return new GUID(rawGuid);
  }
}
