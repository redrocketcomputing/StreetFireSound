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
 * $Id: CddbCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Red Rocket Computing, Inc.
 * </p>
 * 
 * @author unascribed
 * @version 1.0
 */

public abstract class CddbCommand
{
  protected URL commandUrl = null;
  protected CddbResponse cddbResponse = null;
  protected Map params = null;
  protected CddbResponse response = null;

  public CddbCommand(Map params)
  {
    this.params = new HashMap(params);
  }

  protected void addCommandParameter(String commandString)
  {
    this.params.put(CddbConstant.COMMAND_PARAMETERS_KEY, commandString);
  }

  /**
   * Translates parameters into url string
   * 
   * @param params Map containing all command and communcation parameters.
   * @return
   */
  protected URL getCommandUrl() throws MalformedURLException
  {
    String cddbhost = (String)params.get(CddbConstant.CDDB_HOST_KEY);
    String userName = (String)params.get(CddbConstant.USER_NAME_KEY);
    String hostName = (String)params.get(CddbConstant.CLIENT_HOST_KEY);
    String clientName = (String)params.get(CddbConstant.CLIENT_NAME_KEY);
    String clientVersion = (String)params.get(CddbConstant.CLIENT_VERSION_KEY);
    String protocolVersion = (String)params.get(CddbConstant.PROTOCOL_VERSION);
    String commandParameters = (String)params.get(CddbConstant.COMMAND_PARAMETERS_KEY);

    StringBuffer sb = new StringBuffer("");
    sb.append("http://");
    sb.append(cddbhost);
    sb.append("/~cddb/cddb.cgi?");
    sb.append("cmd=");
    sb.append(commandParameters);
    sb.append("&hello=");
    sb.append(userName);
    sb.append("+");
    sb.append(hostName);
    sb.append("+");
    sb.append(clientName);
    sb.append("+");
    sb.append(clientVersion);
    sb.append("&proto=" + protocolVersion);

    return new URL(sb.toString());
  }

  /**
   * Executes command and returns response.
   * 
   * @return
   */
  public CddbResponse execute() throws CddbCommandException
  {
    DataInputStream dis = null;
    try
    {
      URL url = getCommandUrl();
      dis = new DataInputStream(url.openConnection().getInputStream());
      this.cddbResponse = createCddbResponse(dis);
    }
    catch (IOException io)
    {
      throw new CddbCommandException("Execution failed: " + io.toString());
    }
    catch (InvalidResponseException ire)
    {
      throw new CddbCommandException("Execution failed: Unable to parse response: " + ire.toString());
    }
    finally
    {
      try
      {
        if (dis != null)
        {
          dis.close();
        }
      }
      catch (IOException ioe)
      {
      }

      dis = null;
    }
    return this.cddbResponse;
  }

  /**
   * Constructs and returns the appropriate subclass of CddbResponse ( ie the response pertaining to the proper command ).
   * 
   * @param is
   * @return
   */
  protected CddbResponse createCddbResponse(DataInputStream is) throws InvalidResponseException
  {
    response = createCddbResponse();
    try
    {
      response.unmarshal(is);
    }
    catch (IOException io)
    {
      throw new InvalidResponseException("Unable to parse response due to unexpected data format: " + io.toString());
    }
    return response;
  }

  /**
   * Initializes a response object appropriate to the command using the null constructor.\ Subclasses should initialize the protected cddbResponse variable.
   */
  protected abstract CddbResponse createCddbResponse();

}
