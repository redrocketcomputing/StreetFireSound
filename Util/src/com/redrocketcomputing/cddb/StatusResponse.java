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
 * $Id: StatusResponse.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.redrocketcomputing.util.log.LoggerSingleton;

public class StatusResponse extends CddbResponse
{
  CddbStatus cddbStatus = null;

  public StatusResponse()
  {
  }

  protected void unmarshalBody(BufferedReader br) throws IOException
  {
    switch (this.code)
    {
      case 210:
        Map serverStatus = new HashMap();
        int totalEntries = 0;
        Map entries = new HashMap();

        String line = null;

        while ((line = br.readLine()) != null)
        {
          if (line.length() > 17 && line.substring(0, 17).equals("Database entries:"))
          {
            break;
          }
          if (!line.equals(".") && !line.equals("Server status:"))
          {
            NameValuePair nv = processLine(line);
            serverStatus.put(nv.getName(), nv.getValue());
          }
        }

        NameValuePair nv = processLine(line);
        totalEntries = Integer.parseInt(nv.getValue());

        while ((line = br.readLine()) != null)
        {
          if (!line.equals(".") && !line.equals("Database entries by category:"))
          {
            NameValuePair nv2 = processLine(line);
            entries.put(nv2.getName(), new Integer(nv2.getValue()));
          }
        }

        cddbStatus = new CddbStatus(serverStatus, entries, totalEntries);

        break;
      default:
    }
  }

  public String getMessage()
  {
    switch (this.code)
    {
      case 210:
        return ("OK - status information follows.");
      default:
        return getDefaultMessage(this.code);
    }
  }

  public NameValuePair processLine(String line)
  {
    NameValuePair nv = null;
    StringTokenizer st = new StringTokenizer(line, ":");

    try
    {
      String name = (String)st.nextToken();
      String value = (String)st.nextToken();
      nv = new NameValuePair(name.trim(), value.trim());

    }
    catch (NoSuchElementException ex)
    {
      LoggerSingleton.logWarning(this.getClass(), "processLine", "Unable parse name value pair- unexpected data format-->" + line);
    }
    return nv;
  }

  public CddbStatus getCddbStatus()
  {
    return this.cddbStatus;
  }
}
