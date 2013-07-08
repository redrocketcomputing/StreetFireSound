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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import java.util.ArrayList;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;


/**
 *  Cache for media meta data
 *
 *  Thread safe
 *  @author iain huxley
 */
public final class ContentMetadataCache implements StreetFireEventListener
{
  static ContentMetadataCache instance = null;  //XXX:000000000000000000000:20050322iain: temp hack, remove

  private HashMap cache;
  private int maxEntries;
  private MediaCatalog mediaCatalog;
  private StreetFireClient client;

  /**
   *  @param mediaCatalog the media catalog from which to recieve the metadata
   *  @param maxEntries CURRENTLY UNUSED the maximum number of entries to store in the cache
   */
  public ContentMetadataCache(StreetFireClient client, int maxEntries)
  {
    // init members
    this.client = client;
    this.maxEntries = maxEntries;
    mediaCatalog = client.getMediaCatalog();

    // init cache
    cache = new HashMap(64); // largish capacity, don't care about iteration time

    // listen for fresh metadata
    client.getEventDispatcher().addListener(this);

    //XXX:000000000000000000000:20050322iain: temp hack, remove
    instance = this;
  }

  /**
   *  Retreive metadata from the cache, requesting it if not found
   *
   *  @param supplyDummy if true, the list returned will contain dummy items for where
   *                     the metadata is not yet available
   */
  public synchronized ContentMetadata getMetadata(ContentId contentId, boolean supplyDummy)
  {
    return getMetadata(contentId, supplyDummy, true);
  }

  /**
   *  Retreive metadata from the cache, requesting it if not found
   *
   *  @param supplyDummy if true, the list returned will contain dummy items for where
   *                     the metadata is not yet available
   */
  public synchronized ContentMetadata getMetadata(ContentId contentId, boolean supplyDummy, boolean requestNeededMetadata)
  {
    assert contentId != null;

    ContentMetadata metadata = (ContentMetadata)cache.get(contentId);

    // if found, return
    if (metadata != null)
    {
      return metadata;
    }

    if (requestNeededMetadata)
    {
      // request metadata
      ArrayList id = new ArrayList(1);
      id.add(contentId);
      mediaCatalog.requestMetadata(id);
    }

    // otherwise return dummy or null
    return supplyDummy ? getDummy(contentId) : null;
  }



  /**
   *  Retreive metadata from the cache, requesting it if not found
   *
   *  @param supplyDummies if true, the list returned will contain dummy items for where
   *                       the metadata is not yet available
   *
   *  @return a list of metadata found (and, optionally, dummies when not found)
   */
  public synchronized List getMetadata(List contentIds, boolean supplyDummies)
  {
    return getMetadata(contentIds, supplyDummies, true);
  }

  /**
   *  Retreive metadata from the cache, optionally requesting it if not found
   *
   *  @param supplyDummies if true, the list returned will contain dummy items for where
   *                       the metadata is not yet available
   *
   *  @return a list of metadata found (and, optionally, dummies when not found)
   */
  public synchronized List getMetadata(List contentIds, boolean supplyDummies, boolean requestNeededMetadata)
  {
    // XXX:0:20050322iain: this should internally call the above method, need a way to figure out if a ContentMetadata is a dummy or not

    assert Util.allEntriesAreAssignableTo(ContentId.class, contentIds);

    LinkedList result                = new LinkedList(); // list of ContentMetadata
    final LinkedList itemsNotInCache = new LinkedList(); // list of ContentIds

    int count = 0;
    for (Iterator i=contentIds.iterator(); i.hasNext(); )
    {
      ContentId contentId = (ContentId)i.next();
      count++;
      if (contentId == null)
      {
        throw new IllegalArgumentException("contentId was null for " + count + " index");
      }

      ContentMetadata metadata = (ContentMetadata)cache.get(contentId);
      if (metadata != null)
      {
        result.add(metadata);
      }
      else
      {
        itemsNotInCache.add(contentId);

        // if requested, supply dummy items where metadata is not available
        if (supplyDummies)
        {
          result.add(getDummy(contentId));
        }
      }
    }
    LoggerSingleton.logDebugCoarse(ContentMetadataCache.class, "getMetadata", String.valueOf(itemsNotInCache.size()) + " items not found in cache out of " + contentIds.size() + " total requested");

    if (requestNeededMetadata)
    {
      // use a task to perform the request as this may be called during a havi notification, which would deadlock on the SoftwareElement lock.
      Task requestTask = new AbstractTask()
        {
          public void run()
          {

            // request the metadata that was not found
            if (itemsNotInCache.size() > 0)
            {
              mediaCatalog.requestMetadata(itemsNotInCache);
            }
          }
        };
      client.executeTask(requestTask);
    }

    return result;
  }

  /**
   *  @return a dummy metadata, with all fields at their default except for contentId
   */
  private ContentMetadata getDummy(ContentId contentId)
  {
    ContentMetadata dummy = new ContentMetadata();
    dummy.setContentId(contentId);
    return dummy;
  }

  /**
   *  update/add an item in the cache
   */
  public void putMetadata(ContentMetadata[] metadata)
  {
    for (int i=0; i<metadata.length; i++)
    {
      cache.put(metadata[i].getContentId(), metadata[i]);
    }
  }

  /**
   *  remove an item from the cache
   */
  public void removeEntry(ContentId contentId)
  {
    cache.remove(contentId);
  }

  public void eventNotification(StreetFireEvent event)
  {
    if (event instanceof ContentMetadataArrivedEvent)
    {
      handleContentMetadata((ContentMetadataArrivedEvent) event);
    }
  }

  public synchronized void handleContentMetadata(ContentMetadataArrivedEvent event)
  {
    //    System.out.println("XXX:000000000000000000:iain:>>>>cache is '" + cache + "'");



    // just bung it in the cache, don't need to fire another event as listeners will get this event
    ContentMetadata[] metadata = event.getMetadata();
    ArrayList newTrackMetadata = new ArrayList();
    for (int i=0; i< metadata.length; i++)
    {
      cache.put(metadata[i].getContentId(), metadata[i]);

      //      patchUpTrackMetadata(metadata[i]);

      // rbx1600 hackery due to havi index crap
      if (metadata[i].getContentId() instanceof MlidContentId && metadata[i].getTrackNumber() <= 0) //getContentId().notExpandable()))
      {
        //XXX:0000:20050329iain: hack... look for track entries for this album and if found update their albumTitle/albumArtist fields
        int track = 0;
        //        System.out.println("XXX:000000000000000000:iain:>>>>looking for  '" + ((MlidContentId)metadata[i].getContentId()).getTrackContentId(track) +  "'");


        //ContentMetadata trackMetadata = (ContentMetadata)cache.get(((MlidContentId)metadata[i].getContentId()).getTrackContentId(track));
        while (++track < 30) //trackMetadata != null)
        {
          // attempt to get next
          ContentMetadata trackMetadata = (ContentMetadata)cache.get(((MlidContentId)metadata[i].getContentId()).getTrackContentId(track));
          if (trackMetadata == null)
          {
            continue;
          }

          // found one, update
          trackMetadata.setAlbumTitle(metadata[i].getTitle());
          trackMetadata.setAlbumArtist(metadata[i].getArtist());

          LoggerSingleton.logDebugCoarse(ContentMetadataCache.class, "handleContentMetadata", "setting album info for track " + trackMetadata.getTrackNumber() + ", album is " + metadata[i].getTitle());

          // add it to the list so we can notify
          newTrackMetadata.add(trackMetadata);

        }
      }
    }



    // notify of any hack updated track metadata
    if (newTrackMetadata.size() > 0)
    {
      client.getEventDispatcher().queueEvent(new ContentMetadataArrivedEvent(StreetFireEvent.NOT_APPLICABLE, (ContentMetadata[])newTrackMetadata.toArray(new ContentMetadata[0])));
    }

  }

  public String toString()
  {
    return super.toString(); //XXX:0:20050308iain:
  }
}
