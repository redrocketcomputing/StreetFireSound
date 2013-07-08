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
 * $Id: LookupDiscs.java,v 1.3 2005/03/11 21:21:34 stephen Exp $
 */
package com.streetfiresound.samples.mediamanager;

import java.util.List;

import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviApplicationModuleManagerException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.cddb.CddbClient;
import com.redrocketcomputing.cddb.CddbClientException;
import com.redrocketcomputing.cddb.DiscInfo;
import com.redrocketcomputing.cddb.QueryResponse;
import com.redrocketcomputing.cddb.QueryResultRow;
import com.redrocketcomputing.cddb.ReadResponse;
import com.redrocketcomputing.cddb.StatusResponse;
import com.redrocketcomputing.cddb.TrackInfo;
import com.redrocketcomputing.cddb.TrackTextInfo;
import com.redrocketcomputing.havi.constants.ConstRbx1600Release;
import com.redrocketcomputing.havi.system.amm.ApplicationModuleManager;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.commands.HaviCommandLineApplication;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstCategoryType;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaItemType;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogClient;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogException;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LookupDiscs extends HaviCommandLineApplication
{

  /**
   * 
   */
  public LookupDiscs()
  {
    super();
  }

  private CddbClient connect()
  {
    try
    {
      // Try to connect to remote CDDB
      CddbClient cddbClient = new CddbClient(CddbClient.DEFAULT_CDDB_HOST, "rbx1600", "streetfiresound.com", "rbx1600", ConstRbx1600Release.getRelease(), "4");
      
      // Get status
      StatusResponse status = cddbClient.status();
      if (status.getCode() == 210)
      {
        // Log connection
        LoggerSingleton.logInfo(this.getClass(), "run", "connected to FreeDB");
        return cddbClient;
      }
      else
      {
        // Not connected 
        LoggerSingleton.logInfo(this.getClass(), "run", "not connected to FreeDB");
        return null;
      }
    }
    catch (CddbClientException e)
    {
      // Not connected 
      LoggerSingleton.logInfo(this.getClass(), "run", "not connected to FreeDB");
      return null;
    }
  }

  private MediaMetaData[] queryCddb(CddbClient client, MediaMetaData[] sourceItemIndex)
  {
    try
    {
      // Build DiscInfo
      int discMinutes = (sourceItemIndex[0].getPlaybackTime().getHour() * 60) + sourceItemIndex[0].getPlaybackTime().getMinute();
      int discSeconds = sourceItemIndex[0].getPlaybackTime().getSec();
      DiscInfo discInfo = new DiscInfo(sourceItemIndex.length - 1, discMinutes, discSeconds);
      
      // Build TrackInfo
      TrackInfo[] trackInfo = new TrackInfo[discInfo.getTracks()];
      for (int i = 1; i < sourceItemIndex.length; i++)
      {
        // Create new Track
        int trackMinutes = (sourceItemIndex[i].getPlaybackTime().getHour() * 60) + sourceItemIndex[i].getPlaybackTime().getMinute();
        int trackSeconds = sourceItemIndex[i].getPlaybackTime().getSec();
        trackInfo[i - 1] = new TrackInfo(trackMinutes, trackSeconds, 0);
      }
      
      // Query CDDB
      QueryResponse queryResponse = client.query(discInfo, trackInfo);
      if (queryResponse.getCode() != 200 && queryResponse.getCode() != 211)
      {
        // Log any error
        if (queryResponse.getCode() >= 400)
        {
          LoggerSingleton.logError(this.getClass(), "queryCddb", "server returned " + queryResponse.getCode() + " for query command");
        }
        
        // Not found
        return null;
      }

      // Log warning if inexact match and result contain more than one entry
      List discResults = queryResponse.getQueryResultList();
      if (queryResponse.getCode() == 211 && discResults.size() > 1)
      {
        LoggerSingleton.logWarning(this.getClass(), "queryCddb", "more than one matching disc has been found");
      }
      QueryResultRow discInformation = (QueryResultRow)discResults.get(0);
      
      // Read the first disc info
      ReadResponse trackInformation = client.read(discInformation.getCategory(), discInformation.getId());
      if (trackInformation.getCode() != 210)
      {
        // Log error and return original
        LoggerSingleton.logError(this.getClass(), "queryCddb", "server returned " + queryResponse.getCode() + " for read command");
        return null;
      }
      
      // Create new item index array
      List trackResults = trackInformation.getTrackTextInfoList();
      MediaMetaData[] resultItemIndex = new MediaMetaData[trackResults.size() + 1];
      
      // Check for track count match
      if (resultItemIndex.length != sourceItemIndex.length)
      {
        // Log error and return original
        LoggerSingleton.logError(this.getClass(), "queryCddb", "track count mismatch " + sourceItemIndex.length + "<>" + resultItemIndex.length);
        return null;
      }
      
      // Build new disc information
      resultItemIndex[0] = new MediaMetaData();
      resultItemIndex[0].setMediaLocationId(sourceItemIndex[0].getMediaLocationId());
      resultItemIndex[0].setTitle(discInformation.getTitle().trim());
      resultItemIndex[0].setArtist(discInformation.getArtist().trim());
      resultItemIndex[0].setGenre(discInformation.getCategory().trim());
      resultItemIndex[0].setPlaybackTime(sourceItemIndex[0].getPlaybackTime());

      // Build track information
      for (int i = 1; i < resultItemIndex.length; i++)
      {
        // Extract track result row
        TrackTextInfo text = (TrackTextInfo)trackResults.get(i - 1);
        
        // Build item index entry
        resultItemIndex[i] = new MediaMetaData();
        resultItemIndex[i].setMediaLocationId(sourceItemIndex[i].getMediaLocationId());
        resultItemIndex[i].setTitle(text.getTitle().trim());
        resultItemIndex[i].setArtist(text.getArtist().trim());
        resultItemIndex[i].setGenre(discInformation.getCategory().trim());
        resultItemIndex[i].setPlaybackTime(sourceItemIndex[i].getPlaybackTime());
      }
      
      // Return the resulting item index
      return resultItemIndex;
    }
    catch (CddbClientException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "queryCddb", e.toString());
      
      // Just return the source
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Sleep for 5 seconds to let the network settle
      Thread.sleep(5000);
      
      // Create software element
      SoftwareElement softwareElement = new SoftwareElement();
      
      // Find a MediaManager Application Module on the network
      SimpleAttributeTable attributes = new SimpleAttributeTable();
      attributes.setSoftwareElementType(ConstSoftwareElementType.APPLICATION_MODULE);
      attributes.setInterfaceId(ConstMediaManagerInterfaceId.MEDIA_MANAGER);
      RegistryClient registryClient = new RegistryClient(softwareElement);
      QueryResult result = registryClient.getElementSync(0, attributes.toQuery());
      
      // Look for matching GUID
      SEID remoteSeid = null;
      GUID matchGuid = GuidUtil.fromString(ConfigurationProperties.getInstance().getProperties().getProperty("match.guid", "ff:ff:ff:ff:ff:ff:ff"));
      for (int i = 0; i < result.getSeidList().length; i++)
      {
        // Check for match GUID
        if (matchGuid.equals(GUID.BROADCAST) || result.getSeidList()[i].getGuid().equals(matchGuid))
        {
          // Found it
          remoteSeid = result.getSeidList()[i];
          break;
        }
      }

      // Check not found
      if (remoteSeid == null)
      {
        System.out.println(matchGuid.toString() + " not found");
        System.exit(1);
      }

      // Create a media catalog client with the MediaManager found and get the Discs
      System.out.println("retrieving discs");
      MediaCatalogClient mediaManagerClient = new MediaCatalogClient(softwareElement, remoteSeid);
      MLID[] discMlids = mediaManagerClient.getMediaSummarySync(0, ConstCategoryType.TYPE, ConstMediaItemType.CDDA);
      MediaMetaData[] discMetaData = mediaManagerClient.getMultipleMetaDataSync(0, discMlids);
      
      // Create CDDB connection
      System.out.print("Connecting to FreeDB: ");
      CddbClient cddbClient = connect();
      if (cddbClient == null)
      {
        System.out.println("server not found");
        System.exit(1);
      }
      System.out.println(" done");
      
      // Loop through the disc look for CDDB entrys
      for (int i = 0; i < discMetaData.length; i++)
      {
        // Make sure a disc is loaded
        if (!discMetaData[i].getTitle().equals("EMPTY"))
        {
          // Extract player number
          String discLocation = "discLocation(" + discMetaData[i].getMediaLocationId().getHuid().getTargetId().getN2() + ":" + discMetaData[i].getMediaLocationId().getList() + ":" + discMetaData[i].getMediaLocationId().getIndex() + ")";  
          
          // Retreive complete metadata from the media manager
          MediaMetaData[] metaData = mediaManagerClient.getMetaDataSync(0, discMetaData[i].getMediaLocationId());
          if (metaData.length != 0)
          {
            // Try to look up from FreeDB
            MediaMetaData[] newMetaData = queryCddb(cddbClient, metaData);
            if (newMetaData != null)
            {
              // Log update
              String newDiscLocation = "newDiscLocation(" + newMetaData[0].getMediaLocationId().getHuid().getTargetId().getN2() + ":" + newMetaData[0].getMediaLocationId().getList() + ":" + newMetaData[0].getMediaLocationId().getIndex() + ")";  
              
              System.out.println(discLocation + ", " + newDiscLocation + ", title: " + newMetaData[0].getTitle());
              
              // Update the metadata
              mediaManagerClient.putMetaDataSync(0, newMetaData);
            }
            else
            {
              // Lookup did not find the disc
              System.out.println(discLocation + " not found");
            }
          }
          else
          {
            // Bad error
            System.out.println("bad MLID:" + discLocation);
            System.exit(1);
          }
        }
      }
      
      // All done clean up
      softwareElement.close();
    }
    catch (HaviException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public static void main(String[] args)
  {
    // Create the application
    LookupDiscs application = new LookupDiscs();
    
    // Run the application
    application.run();
    
    // All done
    System.exit(0);
  }
}
