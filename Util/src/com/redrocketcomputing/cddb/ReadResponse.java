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
 * $Id: ReadResponse.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadResponse extends CddbResponse
{
  private List trackTextInfo = null;
  private Map discProperties = null;

  /**
   * Unmarshals information specific this response.
   * 
   * @param br
   * @throws IOException
   */
  protected void unmarshalBody(BufferedReader br) throws IOException
  {
    switch (this.code)
    {
      case 210:
        discProperties = new HashMap();
        String line = null;

        while ((line = br.readLine()) != null)
        {
          processLine(line);
        }

        break;
      case 417:
        unmarshalAdditionalInfo(br);
      default:
    }
  }

  private void processLine(String line)
  {
    String name = null;
    String value = null;

    //skip lines starting with #
    if (!line.substring(0, 1).equals("#") && !line.substring(0, 1).equals("."))
    {
      int eqPos = line.indexOf("=");
      if (eqPos > 0)
      {
        name = line.substring(0, eqPos);
        //check that there's a value otherwise ignore.
        if (line.length() > eqPos)
        {
          value = line.substring(eqPos + 1);
        }
        //check that neither name nor value is null
        if (name != null && value != null)
        {
          //concatenate values if key already exists.
          if (discProperties.containsKey(name))
          {
            value = (String)discProperties.get(name) + value;
          }
          discProperties.put(name, value);
        }
      }
    }

  }

  public String getMessage()
  {
    switch (this.code)
    {
      case 210:
        return ("OK - CDDB database entry follows.");
      case 401:
        return ("ERROR - Specified CDDB entry not found.");
      case 402:
        return ("ERROR - Server error.");
      case 417:
        return ("ERROR - Access limit exceeded, see additional info for explanation.");
      default:
        return getDefaultMessage(this.code);
    }
  }

  public List getTrackTextInfoList()
  {
    if (this.trackTextInfo == null)
    {
      loadTrackTextInfo();
    }
    return this.trackTextInfo;
  }

  private void loadTrackTextInfo()
  {
    int count = 0;
    trackTextInfo = new ArrayList();

    //if props are not null, get all track in sequence until null value is returned.
    while (this.discProperties != null)
    {
      String titleKey = "TTITLE" + count;
      String title = (String)discProperties.get(titleKey);
      
      String extKey = "EXTT" + count;
      String ext = (String)discProperties.get(extKey);

      if (title != null)
      {
        // Check for track artist
        String artist = "";
        if (title.indexOf(" / ") != -1)
        {
          artist = title.substring(0, title.indexOf(" / "));
          title = title.substring(title.indexOf(" / ") + 3);
        }
        else if (title.indexOf(" - ") != -1)
        {
          artist = title.substring(0, title.indexOf(" - "));
          title = title.substring(title.indexOf(" - ") + 3);
        }
        else if (title.indexOf(" \\ ") != -1)
        {
          artist = title.substring(0, title.indexOf(" \\ "));
          title = title.substring(title.indexOf(" \\ ") + 3);
        }

        trackTextInfo.add(new TrackTextInfo(title, artist, ext));
      }
      else
      {
        break;
      }
      count++;
    }
  }

}
