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
 * $Id: CddbResponse.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class CddbResponse
{
  protected int code = 0;
  protected String header = null;
  protected String additionalInfo = null;

  public CddbResponse()
  {
  }

  /**
   * Returns the response code.
   * 
   * @return
   */
  public int getCode()
  {
    return this.code;
  }

  /**
   * Returns first line of the response, not including the code.
   * 
   * @return
   */
  public String getHeader()
  {
    return this.header;
  }

  public String getAdditionInfo()
  {
    return this.additionalInfo;
  }

  /**
   * Responsible for parsing each response header and body. Usually there will be additional command specific data. This method is generally called in the constructor of subclasses.
   * 
   * @param is
   * @return
   */
  protected void unmarshal(InputStream is) throws IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    unmarshalHeader(br);
    unmarshalBody(br);
    br.close();
  }

  /**
   * Parses header into code and headerParams array.
   * 
   * @param isr
   * @throws IOException
   */
  protected void unmarshalHeader(BufferedReader br) throws IOException
  {
    String s = br.readLine();
    if (s == null)
    {
      throw new IOException("unexpected end of input");
    }

    this.code = Integer.parseInt(s.substring(0, 3));
    List params = new ArrayList();

    //if there is more header information...
    if (s.length() > 3)
    {
      header = s.substring(4);
    }
  }

  /**
   * Implement command specific parsing in subclasses.
   * 
   * @param isr
   * @return
   * @throws IOException
   */
  protected abstract void unmarshalBody(BufferedReader br) throws IOException;

  public abstract String getMessage();

  protected void unmarshalAdditionalInfo(BufferedReader br) throws IOException
  {
    String s = null;
    StringBuffer sb = new StringBuffer("");
    while ((s = br.readLine()) != null)
    {
      sb.append(s);
    }
    this.additionalInfo = sb.toString();
  }

  protected String getDefaultMessage(int code)
  {
    if (code > 199 && code < 300)
    {
      switch (code)
      {
        case 202:
          return ("No match found.");
        default:
          return ("Code[" + code + "] not recognized.");

      }
    }
    if (code > 299 && code < 400)
    {
      switch (code)
      {
        default:
          return ("Command OK so far.");

      }
    }
    else if (code > 399 && code < 500)
    {
      switch (code)
      {
        case 403:
          return ("Database entry corrupt.");
        case 409:
          return ("No handshake.");
        default:
          return ("Command correctly formatted but failed due to some other problem.");
      }
    }
    else if (code > 499 && code < 600)
    {
      switch (code)
      {
        default:
          return ("Command failed due to fact that it is either unimplemented or incorrectly formatted.");
      }
    }
    else
    {
      return ("Code[" + code + "] not recognized.");
    }
  }
}
