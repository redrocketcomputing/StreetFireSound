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
 * $Id: CddbClient.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

/**
 * @author Daniel Bernstein
 */

import java.util.HashMap;
import java.util.Map;

public class CddbClient
{

  public static final String DEFAULT_CDDB_HOST = "freedb.freedb.org";

  private Map connectionParams = null;

  public CddbClient()
  {
    this(DEFAULT_CDDB_HOST, "rbx1600", "streetfiresound.com", "rbx1600", "0.9.2", "4");
  }

  /**
   * Construct a client designating an alternate cddbhost - must be a valid host name such as "freedb.freedb.org".
   *
   * @param cddbhost
   */
  public CddbClient(String cddbhost, String userName, String hostName, String clientName, String clientVersion, String protocolVersion)
  {
    connectionParams = new HashMap();
    connectionParams.put(CddbConstant.CDDB_HOST_KEY, cddbhost);
    connectionParams.put(CddbConstant.USER_NAME_KEY, userName);
    connectionParams.put(CddbConstant.CLIENT_HOST_KEY, hostName);
    connectionParams.put(CddbConstant.CLIENT_NAME_KEY, clientName);
    connectionParams.put(CddbConstant.CLIENT_VERSION_KEY, clientVersion);
    connectionParams.put(CddbConstant.PROTOCOL_VERSION, protocolVersion);

  }

  public CddbClient(Map connectionParams)
  {
    this.connectionParams = connectionParams;
  }

  /*----------------------------------------------------------------------------
   *Public methods start here
   ----------------------------------------------------------------------------*/

  public LsCatResponse lsCat() throws CddbClientException
  {
    try
    {
      CddbCommand command = new LsCatCommand(connectionParams);
      return ((LsCatResponse)command.execute());
    }
    catch (CddbCommandException ex)
    {
      throw new CddbClientException("Command failed:" + ex.toString());
    }
  }

  public QueryResponse query(DiscInfo discInfo, TrackInfo[] trackInfo) throws CddbClientException
  {
    try
    {
      CddbCommand command = new QueryCommand(connectionParams, discInfo, trackInfo);
      return ((QueryResponse)command.execute());
    }
    catch (CddbCommandException ex)
    {
      throw new CddbClientException("Command failed:" + ex.toString());
    }
  }

  public ReadResponse read(String category, String discId) throws CddbClientException
  {
    try
    {
      CddbCommand command = new ReadCommand(connectionParams, category, discId);
      return ((ReadResponse)command.execute());
    }
    catch (CddbCommandException ex)
    {
      throw new CddbClientException("Command failed:" + ex.toString());
    }
  }

  public SitesResponse sites() throws CddbClientException
  {
    try
    {
      CddbCommand command = new SitesCommand(connectionParams);
      return ((SitesResponse)command.execute());
    }
    catch (CddbCommandException ex)
    {
      throw new CddbClientException("Command failed:" + ex.toString());
    }
  }

  public StatusResponse status() throws CddbClientException
  {
    try
    {
      CddbCommand command = new StatusCommand(connectionParams);
      return ((StatusResponse)command.execute());
    }
    catch (CddbCommandException ex)
    {
      throw new CddbClientException("Command failed:" + ex.toString());
    }
  }

//  /**
//   * Unit testing.
//   */
//  public static void main(String[] args)
//  {
//    String method = "main";
//    LoggerSingleton.logDebugFine(CddbClient.class, method, "Unit testing...");
//    int testCount = 0;
//    int totalTests = 5;
//    try
//    {
//
//      CddbClient client = new CddbClient();
//
//      testCount++;
//      SitesResponse sitesResponse = client.sites();
//      if (sitesResponse.getCode() == 210)
//      {
//        List sites = sitesResponse.getSiteList();
//        for (int i = 0; i < sites.size(); i++)
//        {
//          LoggerSingleton.logInfo(CddbClient.class, "main", "sites[" + i + "]=" + sites.get(i));
//        }
//        LoggerSingleton.logInfo(CddbClient.class, method, "Test " + testCount + ": SUCCESS.");
//      }
//      else
//      {
//        LoggerSingleton.logWarning(CddbClient.class, method, "Test " + testCount + ": FAILURE.");
//      }
//
//      testCount++;
//      StatusResponse statusResponse = client.status();
//      if (statusResponse.getCode() == 210)
//      {
//        CddbStatus status = statusResponse.getCddbStatus();
//
//        LoggerSingleton.logInfo(CddbClient.class, "main", status.toString());
//        LoggerSingleton.logInfo(CddbClient.class, method, "Test " + testCount + ": SUCCESS.");
//      }
//      else
//      {
//        LoggerSingleton.logWarning(CddbClient.class, method, "Test " + testCount + ": FAILURE.");
//      }
//
//      testCount++;
//      LsCatResponse response = client.lsCat();
//      if (response.getCode() == 210)
//      {
//        List categories = response.getCategoryList();
//        for (int i = 0; i < categories.size(); i++)
//        {
//          LoggerSingleton.logInfo(CddbClient.class, "main", "category[" + i + "]=" + categories.get(i));
//        }
//        LoggerSingleton.logInfo(CddbClient.class, method, "Test " + testCount + ": SUCCESS.");
//      }
//      else
//      {
//        LoggerSingleton.logWarning(CddbClient.class, method, "Test " + testCount + ": FAILURE.");
//      }
//
//      //initialize test params.
//      testCount++;
//      DiscInfo discInfo = new DiscInfo(18, 59, 33);
//      TrackInfo[] trackInfo = { new TrackInfo(2, 26, 0), new TrackInfo(4, 17, 0), new TrackInfo(3, 48, 0), new TrackInfo(3, 29, 0), new TrackInfo(2, 38, 0), new TrackInfo(3, 44, 0), new TrackInfo(2, 39, 0), new TrackInfo(2, 56, 0), new TrackInfo(3, 2, 0), new TrackInfo(3, 27, 0), new TrackInfo(2, 52, 0), new TrackInfo(3, 48, 0), new TrackInfo(3, 13, 0), new TrackInfo(3, 10, 0), new TrackInfo(3, 55, 0), new TrackInfo(3, 21, 0), new TrackInfo(3, 21, 0), new TrackInfo(3, 27, 0) };
//
//      //test query
//      QueryResponse queryResponse = client.query(discInfo, trackInfo);
//      int code = queryResponse.getCode();
//      LoggerSingleton.logInfo(CddbClient.class, "main", "code=" + code);
//      if (code >= 200 && code <= 211)
//      {
//        List resultList = queryResponse.getQueryResultList();
//        for (int i = 0; i < resultList.size(); i++)
//        {
//          LoggerSingleton.logInfo(CddbClient.class, "main", resultList.get(i).toString());
//        }
//        LoggerSingleton.logInfo(CddbClient.class, method, "Test " + testCount + ": SUCCESS.");
//
//        //if query working, test read
//        testCount++;
//        QueryResultRow row = (QueryResultRow)resultList.get(0);
//        ReadResponse readResponse = client.read(row.getCategory(), row.getId());
//        int readCode = readResponse.getCode();
//        LoggerSingleton.logInfo(CddbClient.class, "main", "readCode=" + readCode);
//        if (readCode == 210)
//        {
//          List trackTextInfo = readResponse.getTrackTextInfoList();
//          for (int i = 0; i < trackTextInfo.size(); i++)
//          {
//            LoggerSingleton.logInfo(CddbClient.class, "main", trackTextInfo.get(i).toString());
//          }
//          LoggerSingleton.logInfo(CddbClient.class, method, "Test " + testCount + ": SUCCESS.");
//
//        }
//        else
//        {
//          LoggerSingleton.logWarning(CddbClient.class, method, "Test " + testCount + ": FAILURE.");
//        }
//      }
//      else
//      {
//        LoggerSingleton.logWarning(CddbClient.class, method, "Test " + testCount + ": FAILURE.");
//      }
//    }
//    catch (Exception ex)
//    {
//      ex.printStackTrace();
//    }
//
//    LoggerSingleton.logInfo(CddbClient.class, method, testCount + " of " + totalTests + " tests run.");
//
//  }
}