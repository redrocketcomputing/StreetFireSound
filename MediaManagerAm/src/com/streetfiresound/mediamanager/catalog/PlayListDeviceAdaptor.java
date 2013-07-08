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
 * $Id: PlayListDeviceAdaptor.java,v 1.4 2005/03/20 00:20:27 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import org.havi.applicationmodule.rmi.ApplicationModuleClient;
import org.havi.dcm.types.HUID;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstVendorEventType;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;
import org.havi.system.types.VendorEventId;

import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaItemType;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogAdaptorFailureException;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogException;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogInvalidParameterException;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogNotImplementedException;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogUnidentifiedFailureException;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;
import com.streetfiresound.mediamanager.playlistcatalog.constants.PlayListCatalogConstant;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.GetMetaDataAsyncResponseListener;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogAsyncResponseHelper;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogChangedEventNotificationListener;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogClient;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogException;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogInvalidParameterException;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayList;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayListMetaData;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class PlayListDeviceAdaptor extends AbstractDeviceAdaptor implements PlayListCatalogChangedEventNotificationListener, GetMetaDataAsyncResponseListener 
{
  private ApplicationModule parent;
  private SoftwareElement softwareElement;
  private SEID remoteSeid;
  private ApplicationModuleClient applicationClient;
  private PlayListCatalogClient playListClient;
  private PlayListCatalogAsyncResponseHelper asyncHelper;
  private HUID huid;
  private volatile int retrieveTransactionId = -1;
  
  /**
   * @param softwareElement
   * @param remoteSeid
   */
  public PlayListDeviceAdaptor(ApplicationModule parent, SEID remoteSeid) throws HaviException
  {
    // Construct super class 
    super();
    
    // Check parameters
    if (parent == null || remoteSeid == null)
    {
      // Very bad
      throw new IllegalArgumentException("ApplicationModule or SEID is null");
    }
    
    // Save the parameters
    this.parent = parent;
    this.remoteSeid = remoteSeid;
    this.softwareElement = parent.getSoftwareElement();
    
    // Create clients
    applicationClient = new ApplicationModuleClient(softwareElement, remoteSeid);
    playListClient = new PlayListCatalogClient(softwareElement, remoteSeid);
    
    // Create async helper
    asyncHelper = new PlayListCatalogAsyncResponseHelper(softwareElement);
    softwareElement.addHaviListener(asyncHelper);
    
    // Get the HUID
    huid = parent.getHuid();
    
    // Register for event
    parent.addEventSubscription(new VendorEventId(ConstVendorEventType.PLAYLIST_CATALOG_CHANGED, ConstStreetFireVendorInformation.VENDOR_ID), this);
  }


  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#close()
   */
  public void close()
  {
    try
    {
      // Unregister for event
      parent.removeEventSubscription(new VendorEventId(ConstVendorEventType.PLAYLIST_CATALOG_CHANGED, ConstStreetFireVendorInformation.VENDOR_ID), this);
      
      // Release resources
      softwareElement.removeHaviListener(asyncHelper);
      parent = null;
      asyncHelper = null;
      applicationClient = null;
      playListClient = null;
      softwareElement = null;
      huid = null;
      remoteSeid = null;
      
      // Forward to super class
      super.close();
    }
    catch (HaviMsgListenerNotFoundException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
  }

  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#getHuid()
   */
  public HUID getHuid()
  {
    return huid;
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#getSummaries()
   */
  public MediaMetaData[] getSummaries() throws HaviMediaCatalogException
  {
    try
    {
      // Retrieve summaries
      return toMediaMetaData(playListClient.getMetaDataSync(0, new MLID(huid, (short)0, (short)0)));
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException("PlayListCatalogServer: " + huid);
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#retrieveSummaries()
   */
  public void retrieveSummaries() throws HaviMediaCatalogException
  {
    // Check for outstanding request
    if (retrieveTransactionId != -1)
    {
      // Drop request since a response is on its way
      return;
    }
    
    try
    {
      // Post an async request
      synchronized(softwareElement)
      {
        retrieveTransactionId = playListClient.getMetaData(new MLID(huid, (short)0, (short)0));
        asyncHelper.addAsyncResponseListener(retrieveTransactionId, this);
      }
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    catch (HaviPlayListCatalogException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#getMetaData(com.streetfiresound.mediamanager.catalog.types.MLID)
   */
  public MediaMetaData[] getMetaData(MLID mediaLocationId) throws HaviMediaCatalogException
  {
    try
    {
      // Match HUID
      if (!mediaLocationId.getHuid().equals(huid))
      {
        // Not for us
        throw new HaviMediaCatalogInvalidParameterException("bad " + mediaLocationId.getHuid());
      }
    
      // Get the play list meta data
      return toMediaMetaData(playListClient.getMetaDataSync(0, mediaLocationId));
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
    }
    catch (HaviPlayListCatalogInvalidParameterException e)
    {
      // Translate
      throw new HaviMediaCatalogInvalidParameterException("bad media ID: " + mediaLocationId.getList());
    }
    catch (HaviPlayListCatalogException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#putMetaData(com.streetfiresound.mediamanager.catalog.types.MLID, com.streetfiresound.mediamanager.catalog.types.MediaMetaData[])
   */
  public void putMetaData(MediaMetaData[] mediaMetaData) throws HaviMediaCatalogException
  {
    throw new HaviMediaCatalogNotImplementedException("putMetaData");
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#expand(com.streetfiresound.mediamanager.mediacatalog.types.MLID)
   */
  public MLID[] expand(MLID mediaLocationId) throws HaviMediaCatalogException
  {
    try
    {
      // Match HUID
      if (!mediaLocationId.getHuid().equals(huid))
      {
        // Not for us
        throw new HaviMediaCatalogInvalidParameterException("bad " + mediaLocationId.getHuid());
      }
    
      // Make sure this is a root entry
      MLID root = new MLID(mediaLocationId.getHuid(), mediaLocationId.getList(), (short)0);
      
      // Get the root playlist
      PlayList playList = playListClient.getPlayListSync(0, root);
      
      // Return the contents
      return playList.getContent();
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
    }
    catch (HaviPlayListCatalogException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogChangedEventNotificationListener#playListCatalogChangedEventNotification(org.havi.system.types.SEID, short)
   */
  public void playListCatalogChangedEventNotification(SEID posterSeid, MLID hint)
  {
    // Make sure this is for us
    if (!posterSeid.equals(remoteSeid))
    {
      // Not for us
      return;
    }
    
    // Dispatch event
    dispatchChangedMediaItem(hint);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.GetMediaMetaDataAsyncResponseListener#handleGetMediaMetaData(com.streetfiresound.mediamanager.catalog.types.MediaMetaData[], org.havi.system.types.Status)
   */
  public void handleGetMetaData(int transactionId, PlayListMetaData[] result, Status returnCode)
  {
    try
    {
      // Check the transaction ID
      if (retrieveTransactionId != transactionId)
      {
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "handleGetMediaMetaData", "transaction mismatch: " + retrieveTransactionId + "<>" + transactionId);
        
        // Drop it
        return;
      }

      // Check return code
      if (!returnCode.equals(PlayListCatalogConstant.SUCCESS))
      {
        // Log the error
        LoggerSingleton.logError(this.getClass(), "handleGetMediaMetaData", returnCode.toString());
        
        // Drop it
        return;
      }
      
      // Dispatch
      dispatchRetrievedMediaSummaries(toMediaMetaData(result));
    }
    finally
    {
      // Clear the transation ID
      retrieveTransactionId = -1;
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.AsyncResponseListener#timeout()
   */
  public void timeout(int transactionId)
  {
    try
    {
      if (retrieveTransactionId == transactionId)
      {
        // Clear the transation ID
        retrieveTransactionId = -1;
        
        // Log error
        LoggerSingleton.logWarning(this.getClass(), "timeout", "timeout retrieving playlist summaries, try again");
        
        // Retrieve summarys
        retrieveSummaries();
      }
      else
      {
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "timeout", "transaction mismatch: " + retrieveTransactionId + "<>" + transactionId);
      }
    }
    catch (HaviMediaCatalogException e)
    {
      LoggerSingleton.logError(this.getClass(), "timeout", e.toString());
    }
  }
  
  private final MediaMetaData toMediaMetaData(PlayListMetaData playListMetaData)
  {
    // Translate it
    MediaMetaData mediaMetaData = new MediaMetaData();
    mediaMetaData.setArtist(playListMetaData.getArtist());
    mediaMetaData.setGenre(playListMetaData.getGenre());
    mediaMetaData.setInitialTimeStamp(playListMetaData.getInitialTimeStamp());
    mediaMetaData.setLastUpdateTimeStamp(playListMetaData.getLastUpdateTimeStamp());
    mediaMetaData.setPlaybackTime(playListMetaData.getPlaybackTime());
    mediaMetaData.setMediaLocationId(playListMetaData.getMediaLocationId());
    mediaMetaData.setMediaType(ConstMediaItemType.PLAY_LIST);
    mediaMetaData.setTitle(playListMetaData.getTitle());
    
    // Return it
    return mediaMetaData;
  }
  
  private final MediaMetaData[] toMediaMetaData(PlayListMetaData[] playListMetaData)
  {
    // Translate it
    MediaMetaData[] mediaMetaData = new MediaMetaData[playListMetaData.length];
    for (int i = 0; i < mediaMetaData.length; i++)
    {
      mediaMetaData[i] = toMediaMetaData(playListMetaData[i]);
    }
    
    // Return it
    return mediaMetaData;
  }
}
