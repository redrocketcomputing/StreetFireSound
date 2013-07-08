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
 * $Id: LookupTask.java,v 1.4 2005/04/12 21:52:44 iain Exp $
 */
package com.streetfiresound.clientlib;

import java.util.List;

import org.havi.dcm.types.HUID;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.rmi.FcmClient;
import org.havi.system.SoftwareElement;
import org.havi.system.types.HaviException;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
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
import com.redrocketcomputing.util.concurrent.Channel;
import com.redrocketcomputing.util.concurrent.InterruptableChannel;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.event.LookupResultsArrivedEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import java.awt.Component;
import java.util.Iterator;
import java.util.Arrays;
import java.util.LinkedList;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 *  Lookup from FreeDb
 *
 *  @author stephen, iain
 */
public class LookupTask extends AbstractTask
{
  //XXX:0000:20050301iain: consider renaming, although this happens to be a task it should not be used as one externally, it
  //XXX:0000:20050301iain: adds itself to the task pool

  //XXX:0000:20050301iain: currently bound to the slot scanner, remove this dependency

  private static CddbClient           cddbClient;

  private SoftwareElement      softwareElement;
  private SlotScanner          slotScanner;
  private StreetFireClient     client;
  private LookupRequest        request;

  private List results;

  /**
   * Construct a FreeDB Lookup task
   */
  public LookupTask(StreetFireClient client, LookupRequest request, Component renderComponent)
  {
    this.client = client;
    slotScanner = client.getSlotScanner(renderComponent);
    softwareElement = client.getSoftwareElement();
    this.request = request;

    // Check parameters
    if (softwareElement == null)
    {
      // Bad
      throw new IllegalArgumentException("softwareElement or lookupQueue is null");
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "LookupTask";
  }

  public List getResults()
  {
    return results;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // lazy init
    //XXX:0:20050401iain: unsync, so chance of double init, don't care
    if (cddbClient == null)
    {
      cddbClient = connect();
    }

    if (cddbClient == null)
    {
      // Bad stuff
      LoggerSingleton.logError(this.getClass(), "run", "can not connect to FreeDB");
      return;
    }


    LoggerSingleton.logDebugCoarse(LookupTask.class, "run", "cddb lookup starting for request '" + request + "'");


    if (request.getItems() != null)
    {
      //already have item list
      List itemIndexResults = queryCddb(cddbClient, request.getItems());

      LinkedList list = new LinkedList();
      for (Iterator i=itemIndexResults.iterator(); i.hasNext(); )
      {
        ItemIndex index = (ItemIndex)i.next();
        MediaMetaData metadata = Util.itemIndexToMediaMetaData(new HUID(), index);
        assert metadata != null;
        list.add(new ContentMetadata(metadata));
      }

      client.getEventDispatcher().queueEvent(new LookupResultsArrivedEvent(StreetFireEvent.NOT_APPLICABLE, list));
    }
    else
    {
      try
      {
        // Get the item index
        AvDiscClient avDiscClient = new AvDiscClient(softwareElement, request.getRemoteSeid());
        FcmClient fcmClient = new FcmClient(softwareElement, request.getRemoteSeid());
        HUID huid = fcmClient.getHuidSync(0);

        // Only update if index is not empty and is unknown based on the title
        ItemIndex[] index = avDiscClient.getItemListSync(0, request.getList());
        if (true)//!index[0].getTitle().equals("EMPTY") && index[0].getTitle().equals(""))
        {
          // Try to query FreeDb
          ItemIndex[] decoratedIndex = queryCddb(cddbClient, index);

          // notify slot scanner
          if (slotScanner != null)
          {
            slotScanner.lookupComplete(huid, request.getList(), decoratedIndex);
          }

          if (decoratedIndex != null)
          {
            // update the index
            avDiscClient.putItemListSync(0, request.list, decoratedIndex);

            // queue event?
            //client.getEventDispatcher().queueEvent(new

            // Log update
            LoggerSingleton.logDebugCoarse(LookupTask.class, "run", "Location[" + huid.getTargetId().getN2() + ":" + request.getList() + ":0] found Title: " + decoratedIndex[0].getTitle());
          }
          else
          {
            // Log update
            LoggerSingleton.logDebugCoarse(LookupTask.class, "run", "Location[" + huid.getTargetId().getN2() + ":" + request.getList() + ":0] not found");
          }
        }
      }
      catch (HaviException e)
      {
        // Log the error
        LoggerSingleton.logError(this.getClass(), "run", e.toString());
      }
    }
    LoggerSingleton.logDebugCoarse(LookupTask.class, "run", "cddb lookup finished for request '" + request + "'");
  }

  /**
   *  Attempt to connect to free db
   */
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
        LoggerSingleton.logError(this.getClass(), "run", "not connected to FreeDB");
        return null;
      }
    }
    catch (CddbClientException e)
    {
      // Not connected
      LoggerSingleton.logError(this.getClass(), "run", "not connected to FreeDB");
      return null;
    }
  }


  /**
   *  attempt to perform a cddb lookup
   */
  private List queryCddb(CddbClient client, List contentMetadata)
  {
    ItemIndex[] indices = new ItemIndex[contentMetadata.size()];
    int count = 0;
    for (Iterator i=contentMetadata.iterator(); i.hasNext(); )
    {
      indices[count++] = Util.mediaMetaDataToItemIndex(Util.contentMetadataToMediaMetaData((ContentMetadata)i.next()));
    }
    ItemIndex[] result = queryCddb(cddbClient, indices);
    return Arrays.asList(result);
  }

  /**
   *  attempt to perform a cddb lookup
   */
  private ItemIndex[] queryCddb(CddbClient client, ItemIndex[] sourceItemIndex)
  {
    LoggerSingleton.logDebugCoarse(LookupTask.class, "queryCddb", "querying cddb - sourceItemIndex is '" + sourceItemIndex + "'");

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
        else
        {
          LoggerSingleton.logWarning(this.getClass(), "queryCddb", "server returned " + queryResponse.getCode() + " for query command");
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
      ItemIndex[] resultItemIndex = new ItemIndex[trackResults.size() + 1];

      // Check for track count match
      if (resultItemIndex.length != sourceItemIndex.length)
      {
        // Log error and return original
        LoggerSingleton.logError(this.getClass(), "queryCddb", "track count mismatch " + sourceItemIndex.length + "<>" + resultItemIndex.length);
        return null;
      }

      // Build new disc information
      resultItemIndex[0] = new ItemIndex();
      resultItemIndex[0].setList(sourceItemIndex[0].getList());
      resultItemIndex[0].setIndex((short)0);
      resultItemIndex[0].setTitle(discInformation.getTitle().trim());
      resultItemIndex[0].setArtist(discInformation.getArtist().trim());
      resultItemIndex[0].setGenre(discInformation.getCategory().trim());
      resultItemIndex[0].setPlaybackTime(sourceItemIndex[0].getPlaybackTime());
      resultItemIndex[0].setContentType(sourceItemIndex[0].getContentType());
      resultItemIndex[0].setContentSize(sourceItemIndex[0].getContentSize());
      resultItemIndex[0].setInitialTimeStamp(sourceItemIndex[0].getInitialTimeStamp());
      resultItemIndex[0].setLastUpdateTimeStamp(sourceItemIndex[0].getLastUpdateTimeStamp());

      // Build track information
      for (int i = 1; i < resultItemIndex.length; i++)
      {
        // Extract track result row
        TrackTextInfo text = (TrackTextInfo)trackResults.get(i - 1);

        // Build item index entry
        resultItemIndex[i] = new ItemIndex();
        resultItemIndex[i].setList(sourceItemIndex[i].getList());
        resultItemIndex[i].setIndex((short)i);
        resultItemIndex[i].setTitle(text.getTitle().trim());
        resultItemIndex[i].setArtist(text.getArtist().trim());
        resultItemIndex[i].setGenre(discInformation.getCategory().trim());
        resultItemIndex[i].setPlaybackTime(sourceItemIndex[i].getPlaybackTime());
        resultItemIndex[i].setContentType(sourceItemIndex[i].getContentType());
        resultItemIndex[i].setContentSize(sourceItemIndex[i].getContentSize());
        resultItemIndex[i].setInitialTimeStamp(sourceItemIndex[i].getInitialTimeStamp());
        resultItemIndex[i].setLastUpdateTimeStamp(sourceItemIndex[i].getLastUpdateTimeStamp());
      }

      // Return the resulting item index
      return resultItemIndex;
    }
    catch (CddbClientException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "queryCddb", e.toString());

      return null;
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS: represents a cddb lookup request
   *********************************************************************************/
  public static class LookupRequest
  {
    //XXX:00000:20050401iain: class EITHER uses remoteSeid and list # OR items (for query)
    private SEID remoteSeid; // avdisc seid
    private short list;
    private List items;

    /**
     * Construct a new Lookup Request
     *
     * @param remoteSeid The remote SEID for the AvDisc FCM to update the ItemIndex with FreeDB data
     * @param list The list number to update
     */
    public LookupRequest(SEID remoteSeid, short list)
    {
      this.remoteSeid = remoteSeid;
      this.list = list;
    }

    /**
     * Construct a new Lookup Request
     *
     * @param list the contentmetadata items for an entire disc
     */
    public LookupRequest(List items)
    {
      this.items = items;
    }


    public List getItems()
    {
      return items;
    }

    /**
     * @return Returns the list.
     */
    public final short getList()
    {
      return list;
    }

    /**
     * @return Returns the remoteSeid.
     */
    public final SEID getRemoteSeid()
    {
      return remoteSeid;
    }

    public String toString()
    {
      return "LookupTask.LookupRequest[remoteSeid=" + remoteSeid + " list=" + list;
    }
  }
}
