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
 * $Id: QueryResponse.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class QueryResponse extends CddbResponse
{
  List queryResultRowList = new ArrayList();

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
      case 200:
        queryResultRowList.add(parseQueryResultRow(getHeader()));
        break;
      case 210:
      case 211:
        String line = null;
        while ((line = br.readLine()) != null && !line.equals("."))
        {
          queryResultRowList.add(parseQueryResultRow(line));
        }
        break;
      case 202:
      default:
    }
  }

  public String getMessage()
  {
    switch (this.code)
    {
      case 200:
        return ("OK - Found exact match.");
      case 211:
        return ("OK - Found inexact matches. See list.");
      case 202:
        return ("OK - No match found.");
      case 403:
        return ("ERROR - Database entry is corrupt.");
      default:
        return getDefaultMessage(this.code);
    }
  }

  private QueryResultRow parseQueryResultRow(String line)
  {
    StringTokenizer st = new StringTokenizer(line, " ");

    String id = null;
    String category = null;
    String artist = null;
    String title = null;

    if (st.hasMoreTokens())
    {
      category = (String)st.nextToken();
    }

    if (st.hasMoreTokens())
    {
      id = (String)st.nextToken();
    }

    StringBuffer sb = new StringBuffer("");
    while (st.hasMoreTokens())
    {
      String next = (String)st.nextToken();
      if (!next.equals("/"))
      {
        sb.append(next);
        sb.append(" ");

        //check if not more tokens - if so it
        //means that no album title accompanies artist name.
        //in this case assume album name and artist name are the same.
        if (!st.hasMoreTokens())
        {
          artist = sb.toString().trim();
          title = artist;
          break;
        }
      }
      else
      {
        artist = sb.toString().trim();
        break;
      }
    }

    if (title == null)
    {
      sb = new StringBuffer("");
      while (st.hasMoreTokens())
      {
        sb.append((String)st.nextToken());
        sb.append(" ");
      }

      title = sb.toString().trim();
    }

    return new QueryResultRow(id, category, artist, title);
  }

  public List getQueryResultList()
  {
    return this.queryResultRowList;
  }

}