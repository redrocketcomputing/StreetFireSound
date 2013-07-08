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
 * $Id $
 */
package com.streetfiresound.clientlib;

import java.util.Collection;
import java.util.Iterator;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstVendorEventType;
import org.havi.system.types.HaviException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;
import org.havi.system.types.VendorEventId;

import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.client.Util;
import com.streetfiresound.clientlib.event.CategorySummaryArrivedEvent;
import com.streetfiresound.clientlib.event.ContentIdsArrivedEvent;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.MediaCatalogChangedEvent;
import com.streetfiresound.clientlib.event.OperationSuccessfulEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.mediamanager.mediacatalog.rmi.ChangedMediaCatalogEventNotificationListener;
import com.streetfiresound.mediamanager.mediacatalog.rmi.GetCategorySummaryAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.rmi.GetMediaSummaryAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.rmi.GetMetaDataAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.rmi.GetMultipleMetaDataAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogAsyncResponseHelper;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogClient;
import com.streetfiresound.mediamanager.mediacatalog.rmi.PutMetaDataAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;
import com.streetfiresound.mediamanager.mediacatalog.rmi.SearchMetaDataAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaItemType;
import java.util.List;
import java.util.ArrayList;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;



/**
 *  This class represents a media catalog provided on the network via HAVi
 *  It is a facade class which attempts to simplify access to a remote media catalog.
 *  In particular, it subscribes to necessary HAVi callbacks or events and delivers
 *  notification via StreetFireClient's event mechanism
 *  @author iain huxley
 */
public class HaviRemoteMediaCatalog implements MediaCatalog,        //XXX:0:20041217iain: breaking one line rule for clarity
                                               ChangedMediaCatalogEventNotificationListener,
                                               GetMediaSummaryAsyncResponseListener,
                                               SearchMetaDataAsyncResponseListener,
                                               GetCategorySummaryAsyncResponseListener,
                                               GetMetaDataAsyncResponseListener,
                                               GetMultipleMetaDataAsyncResponseListener,
                                               PutMetaDataAsyncResponseListener
{
  private MediaCatalogClient mediaCatalogClient;
  private MediaCatalogAsyncResponseHelper asyncHelper;
  private SEID mediaManagerSeid; // media catalog is part of media manager software element

  private StreetFireClient client;
  private SoftwareElement softwareElement;

  /**
   * Constructor - connects to remote media catalog
   *
   * @param client the associated streetfireclient XXX:0:20041217iain: does not
   */
  public HaviRemoteMediaCatalog(StreetFireClient client) throws HaviException
  {
    // init members
    this.client = client;
    softwareElement = client.getSoftwareElement();
    mediaManagerSeid = client.getMediaManagerSeid();

    if (mediaManagerSeid == null)
    {
      client.fatalError("No media manager found");
      throw new MediaOrbRuntimeException("No media manager found", null);
    }

    // Create catalog client
    mediaCatalogClient = new MediaCatalogClient(client.getSoftwareElement(), mediaManagerSeid);

    // Init async helper
    asyncHelper = new MediaCatalogAsyncResponseHelper(softwareElement);
    softwareElement.addHaviListener(asyncHelper);

    // listen for media catalog changes
    client.getEventHelper().addEventSubscription(new VendorEventId(ConstVendorEventType.CHANGED_MEDIA_CATALOG, ConstStreetFireVendorInformation.VENDOR_ID), this);
  }

  /**
   * called when any async request times out
   */
  public void timeout(int transactionId)
  {
    // decrement request tracker
    client.decrementAsyncRequests();
    client.handleException(new RuntimeException("timeout"));
  }

  /**
   * Checks the return code from an async response and forwards error if necessary
   *
   * SIDE-EFFECT: decrements the client's async request count
   */
  private boolean checkAsyncResponse(Status returnCode)
  {
    // decrement request tracker
    client.decrementAsyncRequests();

    // check for errors
    if (returnCode.getErrCode() != 0)
    {
      // XXX:000000000000:20041213iain:
      throw new ClientRuntimeException("havi request returned error code " + returnCode.getErrCode(), null);
    }
    return true;
  }

  //--------------------------------------------------------------------------------------------------------
  // SECTION: async request methods
  //--------------------------------------------------------------------------------------------------------

  /**
   * Asynchronously request a category summary
   * @return the transaction Id of the generated HAVi request or -1 if failed
   *
   * EVENT GENERATED: CategorySummaryArrivedEvent
   */
  public int requestCategorySummaries(int categoryType)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "requestCategorySummaries", "requesting category summaries for category type " + categoryType);
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = mediaCatalogClient.getCategorySummary(categoryType);

        // bump request tracker
        client.incrementAsyncRequests();

        // add response listener
        asyncHelper.addAsyncResponseListener(transactionId, this); //XXX:0000:20050104iain: extended timeout

        // return transaction ID
        return transactionId;
      }
    }
    catch (HaviException e)
    {
      // pass on exception
      client.handleException(e);
    }
    return -1;
  }

  /**
   * Asynchronously request media summaries matching a string with a chosen category
   * @param categoryType the type of media category, ConstCategoryType.ARTIST etc.
   * @param match requests items where the selected category type matches this string
   * @return the transaction Id of the generated HAVi request. or -1 if failed
   *
   * EVENT GENERATED: ContentIdsArrivedEvent
   */
  public int requestMediaSummaries(int categoryType, String match)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "requestMediaSummaries", "requesting media summaries for category " + categoryType + ", match='" + match + "'");
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = mediaCatalogClient.getMediaSummary(categoryType, match);

        // bump request tracker
        client.incrementAsyncRequests();

        // add response listener
        asyncHelper.addAsyncResponseListener(transactionId, this);

        // return transaction ID
        return transactionId;
      }
    }
    catch (HaviException e)
    {
      // pass on exception
      client.handleException(e);
    }
    return -1;
  }


  /**
   * EVENT GENERATED: ContentIdsArrivedEvent
   */
  public int requestSearch(String contains)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "requestSearch", "searching for '" + contains + "'");
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = mediaCatalogClient.searchMetaData(contains);

        // bump request tracker
        client.incrementAsyncRequests();

        // add response listener
        asyncHelper.addAsyncResponseListener(transactionId, this);

        // return transaction ID
        return transactionId;
      }
    }
    catch (HaviException e)
    {
      // pass on exception
      client.handleException(e);
    }
    return -1;
  }

  /**
   * Asynchronously a content item expansion
   * (e.g. if passed an content item id of a CD, will return all tracks, if passed a content item id of
   * a playlist, will return all items in the playlist)
   *
   * EVENT GENERATED: ContentIdsArrivedEvent
   * (due to a mismatch with the back side's API actually also generates a ContentMetadataArrivedEvent, but this will have a transaction ID of NOT_APPLICABLE)
   *
   * @param categoryType the type of media category, ConstCategoryType.ARTIST etc.
   * @param match requests items where the selected category type matches this string
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  public int requestExpandItem(ContentId id)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "requestExpandedContentMetadata", "requesting expanded content meta data for " + id);

    //XXX:0000000000000000000000:20050320iain:
    MlidContentId mlidContentId = (MlidContentId)id;

    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = mediaCatalogClient.getMetaData(mlidContentId.getMlid());

        // bump request tracker
        client.incrementAsyncRequests();

        // add response listener
        asyncHelper.addAsyncResponseListener(transactionId, this);

        // return transaction ID
        return transactionId;
      }
    }
    catch (HaviException e)
    {
      // pass on exception
      client.handleException(e);
    }
    return -1;
  }

  /**
   * Asynchronously request media meta data for multiple content items.  Will not expand (i.e. it will only return one MediaMetaData object per MLID passed)
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   *
   * NOTE: accessible to package only - all metadata should be requested through the cache
   *
   * EVENT GENERATED: ContentMetadataArrivedEvent
   */
  public int requestMetadata(List contentIds)
  {
    // convert to array
    MLID[] mlidsArray = new MLID[contentIds.size()];
    int index = 0;
    for (Iterator i=contentIds.iterator(); i.hasNext();)
    {
      mlidsArray[index++] = ((MlidContentId)i.next()).getMlid();
    }

    // forward to method with mlid array signature
    return requestMetadata(mlidsArray);
  }

  /**
   * NOTE: PRIVATE!!! MLID APIs are not exposed outside of the clientlib
   * Asynchronously request media meta data for multiple MLIDs.  Will not expand (i.e. it will only return one MediaMetaData object per MLID passed)
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  private int requestMetadata(MLID[] mlids)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "requestMetaData", "requesting metadata for " + mlids.length  + " mlids");
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = mediaCatalogClient.getMultipleMetaData(mlids);

        // bump request tracker
        client.incrementAsyncRequests();

        // add response listener
        asyncHelper.addAsyncResponseListener(transactionId, this);

        // return transaction ID
        return transactionId;
      }
    }
    catch (HaviException e)
    {
      // pass on exception
      client.handleException(e);
    }
    return -1;
  }


/**
   * Asynchronously request save (update) of meta data
   *
   * EVENT GENERATED: OperationSuccessfulEvent
   *
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  public int requestPutMetadata(ContentMetadata discMetadata, List trackMetadata)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "requestPutMediaMetaData", "requesting metadat store for root content id " + discMetadata.getContentId());

    assert Util.allEntriesAreAssignableTo(ContentMetadata.class, trackMetadata);
    ContentMetadata[] trackMetadataArray = (ContentMetadata[])(new ArrayList(trackMetadata)).toArray(new ContentMetadata[0]);

    // create metadata array with disc metadata as first entry, track as rest (maps to HAVi's ItemIndex stuff)
    ContentMetadata[] combinedMetadata = new ContentMetadata[trackMetadataArray.length + 1];
    System.arraycopy(trackMetadataArray, 0, combinedMetadata, 1, trackMetadataArray.length);
    combinedMetadata[0] = discMetadata;

    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = mediaCatalogClient.putMetaData(Util.contentMetadataArrayToMediaMetaDataArray(combinedMetadata));

        // bump request tracker
        client.incrementAsyncRequests();

        // add response listener
        asyncHelper.addAsyncResponseListener(transactionId, this);

        // return transaction ID
        return transactionId;
      }
    }
    catch (HaviException e)
    {
      // pass on exception
      client.handleException(e);
    }
    return -1;
  }

  //--------------------------------------------------------------------------------------------------------
  // SECTION: havi async response handler methods
  //--------------------------------------------------------------------------------------------------------

  /**
   * called to notify that an category summary has arrived
   */
	public void handleGetCategorySummary(int transactionId, CategorySummary[] result, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "handleGetCategorySummary", "receiving " + result.length  + " category summaries");

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // queue the event for delivery
      client.getEventDispatcher().queueEvent(new CategorySummaryArrivedEvent(transactionId, result));
    }
  }

  /**
   *  Called to notify that a request for media summaries has arrived
   */
	public void handleGetMediaSummary(int transactionId, MLID[] result, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "handleGetMediaSummaries", "receiving " + result.length  + " mlids");

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // convert to abstracted type
      ContentId[] contentIds = Util.mlidArrayToContentIdArray(result);

      // queue the event for delivery
      client.getEventDispatcher().queueEvent(new ContentIdsArrivedEvent(transactionId, contentIds));
    }
  }

  /**
   *  Called to notify that a request for expanded metadata has arrived
   */
	public void handleGetMetaData(int transactionId, MediaMetaData[] result, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "handleGetMetaData", "receiving " + result.length  + " metadata entries (common root expanded)");

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // must be at least the root entry and one more for CDDA, otherwise at least 1 (possibly two for the other types, don't know yet)
      assert result.length >= 1 && (!(result[0].getMediaType().equals(ConstMediaItemType.CDDA)) || result.length >= 2);

      // convert to abstracted type
      // XXX:0:20050301iain: temporary hack, converts the special tokens such as "EMPTY" or "UNKNOWN"
      ContentMetadata[] metadata = Util.convertSpecialTokens(result);

      // XXX:0:20050321iain: since there's a mismatch with the server API (we really just want to expand to ids)
      // XXX:0:20050321iain: we will queue TWO events - first, one with the metadata, so that the cache is primed
      // XXX:0:20050321iain: and then secondly with the ids only (only this event will have the transaction Id, the other is considered a side-effect)

      // queue a metadata event, don't want to waste it
      client.getEventDispatcher().queueEvent(new ContentMetadataArrivedEvent(StreetFireEvent.NOT_APPLICABLE, metadata));

      // get the contentIds, dropping the summary item (second param = true means drop first)
      ContentId[] contentIds = Util.contentMetadataArrayToContentIdArray(metadata, true);

      // queue actual event promised
      client.getEventDispatcher().queueEvent(new ContentIdsArrivedEvent(transactionId, contentIds));
    }
  }

  /**
   *  Called to notify that search results have arrived
   */
	public void handleSearchMetaData(int transactionId, MediaMetaData[] result, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "handleSearchMetaData", "receiving " + result.length  + " metadata entries as search result");

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // convert to abstracted type
      // XXX:0:20050301iain: temporary hack, converts the special tokens such as "EMPTY" or "UNKNOWN"
      ContentMetadata[] metadata = Util.convertSpecialTokens(result);

      // queue the event for delivery
      client.getEventDispatcher().queueEvent(new ContentMetadataArrivedEvent(transactionId, metadata));
    }
  }


  /**
   *  Called to notify that a request for metadata has arrived
   */
	public void handleGetMultipleMetaData(int transactionId, MediaMetaData[] result, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "handleGetMultipleMetaData", "receiving " + result.length  + " metadata entries");

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      final MediaMetaData[] metadata = result;
      final int tid = transactionId;


      // request the metadata in another thread, can't do it here or will deadlock on SoftwareElement
      Task task = new AbstractTask()
        {
          public void run()
          {

            // convert to abstracted type
            // XXX:0:20050301iain: temporary hack, converts the special tokens such as "EMPTY" or "UNKNOWN"
            ContentMetadata[] contentMetadata = Util.convertSpecialTokens(metadata);

            // queue the event for delivery
            client.getEventDispatcher().queueEvent(new ContentMetadataArrivedEvent(tid, contentMetadata));
          }
        };
      client.executeTask(task);
    }
  }

  /**
   *  Called to notify that a request to save metadata has succeeded
   */
	public void handlePutMetaData(int transactionId, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "handlePutMetaData", "metadata put successful");
    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // queue the event for delivery
      client.getEventDispatcher().queueEvent(new OperationSuccessfulEvent(transactionId));
    }
  }

  //--------------------------------------------------------------------------------------------------------
  // SECTION: havi event handler methods
  //--------------------------------------------------------------------------------------------------------

	public synchronized void changedMediaCatalogEventNotification(SEID posterSeid, MLID hint)
  {
    LoggerSingleton.logDebugCoarse(HaviRemoteMediaCatalog.class, "changedMediaCatalogEventNotification", "revieved media catalog changed event with hint " + hint);
    // will receive events from all devices, narrow to our media manager
    if (!posterSeid.equals(mediaManagerSeid))
    {
      return;
    }

    if (hint != null)
    {
      MlidContentId id = new MlidContentId(hint);

      // remove the stale cache entry
      client.getMetadataCache().removeEntry(id);

      final ArrayList list = new ArrayList(1);
      list.add(id);

      // request the metadata in another thread, can't do it here or will deadlock on SoftwareElement
      Task task = new AbstractTask()
        {
          public void run()
          {
            requestMetadata(list);
          }
        };
      client.executeTask(task);
    }
    client.getEventDispatcher().queueEvent(new MediaCatalogChangedEvent(StreetFireEvent.NOT_APPLICABLE, hint));


  }
}
