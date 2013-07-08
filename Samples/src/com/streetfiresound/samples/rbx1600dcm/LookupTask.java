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
 * $Id: LookupTask.java,v 1.2 2005/03/11 21:28:58 iain Exp $
 */
package com.streetfiresound.samples.rbx1600dcm;

import java.util.List;

import org.havi.dcm.types.HUID;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.rmi.FcmClient;
import org.havi.system.SoftwareElement;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.service.ServiceException;
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
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class LookupTask extends AbstractTask
{
  /*********************************************************************************
   * PUBLIC INNER CLASS: represents a cddb lookup request
   *********************************************************************************/
  public class LookupRequest
  {
    private SEID remoteSeid;
    private short list;

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
  }

  private SoftwareElement softwareElement;
  private InterruptableChannel channel;

  /**
   * Construct a FreeDB Lookup task
   */
  public LookupTask(SoftwareElement softwareElement, Channel channel)
  {
    // Check parameters
    if (softwareElement == null || channel == null)
    {
      // Bad
      throw new IllegalArgumentException("SoftwareElement or InterruptableChannel is null");
    }

    try
    {
      // Save parameters
      this.softwareElement = softwareElement;
      this.channel = new InterruptableChannel(channel);

      // Launch task
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      // Bad stuff
      e.printStackTrace();
      throw new IllegalStateException();
    }
  }

  /**
   * Release all resources and terminate thread
   */
  public void close()
  {
    // Interrupt channel
    channel.interrupt();
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

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    CddbClient cddbClient = connect();
    if (cddbClient == null)
    {
      // Bad stuff
      LoggerSingleton.logError(this.getClass(), "run", "can not connect to FreeDB");
      return;
    }

    try
    {
      // Loop until interrupted
      while (true)
      {
        // Extract request
        LookupRequest request = (LookupRequest)channel.take();

        try
        {
          // Get the item index
          AvDiscClient avDiscClient = new AvDiscClient(softwareElement, request.getRemoteSeid());
          FcmClient fcmClient = new FcmClient(softwareElement, request.getRemoteSeid());
          HUID huid = fcmClient.getHuidSync(0);

          // Only update if index is not empty and is unknown based on the title
          ItemIndex[] index = avDiscClient.getItemListSync(0, request.getList());
          if (!index[0].getTitle().equals("EMPTY") && index[0].getTitle().equals(""))
          {
            // Try to query FreeDb
            ItemIndex[] decoratedIndex = queryCddb(cddbClient, index);
            if (decoratedIndex != null)
            {
              // update the index
              avDiscClient.putItemListSync(0, request.list, decoratedIndex);

              // Log update
              System.out.println("Location[" + huid.getTargetId().getN2() + ":" + request.getList() + ":0] found Title: " + decoratedIndex[0].getTitle());
            }
            else
            {
              // Log update
              System.out.println("Location[" + huid.getTargetId().getN2() + ":" + request.getList() + ":0] not found");
            }
          }
        }
        catch (HaviException e)
        {
          // Log the error
          LoggerSingleton.logError(this.getClass(), "run", e.toString());
        }
      }
    }
    catch (InterruptedException e)
    {
      // All done ignore
    }
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

  private ItemIndex[] queryCddb(CddbClient client, ItemIndex[] sourceItemIndex)
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

      // Just return the source
      return null;
    }
  }

}
