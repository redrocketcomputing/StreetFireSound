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

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstVendorEventType;
import org.havi.system.types.HaviException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;
import org.havi.system.types.VendorEventId;

import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.event.OperationSuccessfulEvent;
import com.streetfiresound.clientlib.event.PlayListCatalogChangedEvent;
import com.streetfiresound.clientlib.event.PlayListCreatedEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.PlayListArrivedEvent;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.CreatePlayListAsyncResponseListener;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogAsyncResponseHelper;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogChangedEventNotificationListener;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogClient;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.UpdatePlayListAsyncResponseListener;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayList;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.RemovePlayListAsyncResponseListener;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.GetPlayListAsyncResponseListener;


/**
 *  This class represents a playlist catalog provided on the network via HAVi
 *  It is a facade class which attempts to simplify access to a remote playlist catalog.
 *  In particular, it subscribes to necessary HAVi callbacks or events and delivers
 *  notification via StreetFireClient's event mechanism
 *  @author iain huxley
 */
public class RemotePlayListCatalog implements PlayListCatalogChangedEventNotificationListener,  //XXX:0:20041217iain: breaking one line rule for clarity
                                              CreatePlayListAsyncResponseListener,
                                              GetPlayListAsyncResponseListener,
                                              RemovePlayListAsyncResponseListener,
                                              UpdatePlayListAsyncResponseListener
{
  private PlayListCatalogClient playListCatalogClient;
  private PlayListCatalogAsyncResponseHelper asyncHelper;

  private StreetFireClient client;
  private SoftwareElement softwareElement;
  private SEID mediaManagerSeid;

  /**
   * Constructor - connects to remote media catalog
   *
   * @param client the associated streetfireclient XXX:0:20041217iain: does not
   */
  public RemotePlayListCatalog(StreetFireClient client) throws HaviException
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
    playListCatalogClient = new PlayListCatalogClient(client.getSoftwareElement(), mediaManagerSeid);

    // Init async helper
    asyncHelper = new PlayListCatalogAsyncResponseHelper(softwareElement);
    softwareElement.addHaviListener(asyncHelper);

    // listen for playlist catalog changes
    client.getEventHelper().addEventSubscription(new VendorEventId(ConstVendorEventType.PLAYLIST_CATALOG_CHANGED, ConstStreetFireVendorInformation.VENDOR_ID), this);
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
      client.handleException(new RuntimeException("havi request returned error code " + returnCode.getErrCode()));
      return false;
    }
    return true;
  }

  //--------------------------------------------------------------------------------------------------------
  // SECTION: async request methods
  //--------------------------------------------------------------------------------------------------------

//   /**
//    * Asynchronously request the metadata for a playlist
//    * @return the transaction Id of the generated HAVi request or -1 if failed
//    */
//   public int requestMetaData(MLID mlid)
//   {
//     LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "requestMetaData", "requesting playlist metadata for playlist mlid " + mlid);
//     try
//     {
//       // sync on software element to prevent message delivery before listener added
//       synchronized (softwareElement)
//       {
//         // initiate the request
//         int transactionId = playListCatalogClient.getMediaMetaData(mlid);

//         // bump request tracker
//         client.incrementAsyncRequests();

//         // add response listener
//         asyncHelper.addAsyncResponseListener(60000, transactionId, this);

//         // return transaction ID
//         return transactionId;
//       }
//     }
//     catch (HaviException e)
//     {
//       // pass on exception
//       client.handleException(e);
//     }
//     return -1;
//   }

  /**
   * Asynchronously send a request for a playList to be created
   * @return the transaction Id of the generated HAVi request or -1 if failed
   */
  public int requestPlayList(ContentId id)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "requestPlayList", "requesting playlist of id " + id);
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = playListCatalogClient.getPlayList(((MlidContentId)id).getMlid());

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
   * Asynchronously send a request for a playList to be created
   * @return the transaction Id of the generated HAVi request or -1 if failed
   */
  public int requestCreatePlayList(PlayList playList)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "requestCreatePlayList", "requesting playlist creation for playlist " + playList);
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = playListCatalogClient.createPlayList(playList);

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
   * Asynchronously send a request for a playList to be created
   * @return the transaction Id of the generated HAVi request or -1 if failed
   */
  public int requestRemovePlayList(ContentId playListId)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "requestCreatePlayList", "requesting playlist removal for playlist id " + playListId);
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        // initiate the request
        int transactionId = playListCatalogClient.removePlayList(((MlidContentId)playListId).getMlid());

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
   * Asynchronously send a request for a playList to be saved
   * @return the transaction Id of the generated HAVi request or -1 if failed
   */
  public int requestSavePlayList(ContentId playListId, PlayList playList)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "requestSavePlayList", "requesting playlist save (idl='updatePlayList') containing " + playList.getContent().length + " items [ContentId=" + playListId + ", playlist=" + playList + "]");

    MLID playlistMlid = ((MlidContentId)playListId).getMlid();
    assert playlistMlid != null;
    try
    {
      // sync on software element to prevent message delivery before listener added
      synchronized (softwareElement)
      {
        assert playListId != null;

        // initiate the request
        int transactionId = playListCatalogClient.updatePlayList(playlistMlid, playList);

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

//   /**
//    * called to notify that requested metadata has arrived
//    */
// 	public void handleGetMediaMetaData(int transactionId, MediaMetaData[] result, Status returnCode)
//   {
//     LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "handleGetMediaMetaData", "received playlist metadata (" + result.length + " entries)");

//     // check the response
//     if (checkAsyncResponse(returnCode))
//     {
//       // queue the event for delivery
//       client.getEventDispatcher().queueEvent(new MediaMetaDataArrivedEvent(transactionId, result));
//     }
//   }

  /**
   * called to notify that playlist creation succeeded
   */
	public void handleCreatePlayList(int transactionId, MLID mlid, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "handleCreatePlayList", "playlist create successful, mlid is " + mlid);

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // queue the event for delivery
      client.getEventDispatcher().queueEvent(new PlayListCreatedEvent(transactionId, new MlidContentId(mlid)));
    }
  }

  /**
   * called to notify that playlist removal succeeded
   */
	public void handleRemovePlayList(int transactionId, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "handleRemovePlayList", "playlist removal successful");

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // queue the event for delivery
      client.getEventDispatcher().queueEvent(new OperationSuccessfulEvent(transactionId));
    }
  }

  /**
   * called to notify that a requested playlist has arrived
   */
  public void handleGetPlayList(int transactionId, PlayList playList, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "handleGetPlayList", "playlist arrived [" + playList + "]");

    // check the response
    if (checkAsyncResponse(returnCode))
    {
      // queue the event for delivery
      client.getEventDispatcher().queueEvent(new PlayListArrivedEvent(transactionId, playList));
    }
  }

  /**
   * called to notify that a playlist save succeeded
   */
  public void handleUpdatePlayList(int transactionId, Status returnCode)
  {
    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "handleUpdatePlayList", "playlist save [idl=update] successful");

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

	public synchronized void playListCatalogChangedEventNotification(SEID posterSeid, MLID hint)
  {
    // will receive events from all devices, narrow to our media manager
    if (!client.getMediaManagerSeid().equals(posterSeid))
    {
      return;
    }

    LoggerSingleton.logDebugCoarse(RemotePlayListCatalog.class, "handleUpdatePlayList", "playlist catalog changed, hint is " + hint);

    // fire event
    client.getEventDispatcher().queueEvent(new PlayListCatalogChangedEvent(StreetFireEvent.NOT_APPLICABLE, hint));
  }
}
