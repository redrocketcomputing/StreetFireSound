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
 * $Id: AvDiscDeviceAdaptor.java,v 1.3 2005/03/20 00:20:27 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import org.havi.dcm.types.HUID;
import org.havi.fcm.avdisc.constants.AvDiscConstant;
import org.havi.fcm.avdisc.rmi.AvDiscAsyncResponseHelper;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.rmi.AvDiscItemListChangedEventNotificationListener;
import org.havi.fcm.avdisc.rmi.GetItemListAsyncResponseListener;
import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.rmi.FcmClient;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;
import org.havi.system.types.SystemEventId;

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

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AvDiscDeviceAdaptor extends AbstractDeviceAdaptor implements AvDiscItemListChangedEventNotificationListener, GetItemListAsyncResponseListener
{
  private ApplicationModule parent;
  private SoftwareElement softwareElement;
  private AvDiscClient avDiscClient;
  private AvDiscAsyncResponseHelper asyncHelper;
  private HUID huid;
  private SEID remoteSeid;
  private volatile int retrieveTransactionId = -1;
  
  /**
   * Construct a new AvDiscDeviceAdaptor
   * @param softwareElement The SoftwareElement to use for device communications
   * @param remoteSeid The remote SEID of the SonyJukeboxDcm 
   */
  public AvDiscDeviceAdaptor(ApplicationModule parent, SEID remoteSeid) throws HaviException
  {
    // Initialize super class
    super();
    
    // Check parameters
    if (parent == null || remoteSeid == null)
    {
      // Someone can't write code
      throw new IllegalArgumentException("SoftwareElement or SEID is null");
    }
    
    // Save the parameters
    this.parent = parent;
    this.remoteSeid = remoteSeid;
    this.softwareElement = parent.getSoftwareElement();
    
    // Create FCM clients
    FcmClient fcmClient = new FcmClient(softwareElement, remoteSeid);
    avDiscClient = new AvDiscClient(softwareElement, remoteSeid);

    // Create async response helper
    asyncHelper = new AvDiscAsyncResponseHelper(softwareElement);
    softwareElement.addHaviListener(asyncHelper);
    
    // Get the HUID of the FCM
    huid = fcmClient.getHuidSync(0);
    
    // Register for events
    parent.addEventSubscription(new SystemEventId(ConstSystemEventType.AVDISC_ITEM_LIST_CHANGED), this);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#close()
   */
  public void close()
  {
    try
    {
      // Unregister for events
      parent.removeEventSubscription(new SystemEventId(ConstSystemEventType.AVDISC_ITEM_LIST_CHANGED), this);
      
      // Release resources
      softwareElement.removeHaviListener(asyncHelper);
      parent = null;
      asyncHelper = null;
      avDiscClient = null;
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
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#getSummaries()
   */
  public MediaMetaData[] getSummaries() throws HaviMediaCatalogException
  {
    try
    {
      // Get the summaries, this return capacity + 1, with the 0 index being empty
      ItemIndex[] discSummaries = avDiscClient.getItemListSync(0, (short)0);
      
      // Allocate new translated media summaries
      MediaMetaData[] mediaSummaries = new MediaMetaData[discSummaries.length - 1];
      
      // Build the MediaSummary
      for (int i = 1; i < discSummaries.length; i++)
      {
//        //XXX:0000:stephen:20050113:Patch for problem with getDiscSummary index being set wrong
//        discSummaries[i].setIndex((short)0);
        
        // Create the meta data
        mediaSummaries[i - 1] = toMediaMetaData(discSummaries[i]);
      }
      
      // All done
      return mediaSummaries;
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    catch (HaviAvDiscException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
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
        retrieveTransactionId = avDiscClient.getItemList((short)0);
        asyncHelper.addAsyncResponseListener(60000, retrieveTransactionId, this);
      }
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    catch (HaviAvDiscException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#getMetaData(com.streetfiresound.mediamanager.catalog.types.MLID, int)
   */
  public MediaMetaData[] getMetaData(MLID mediaLocationId) throws HaviMediaCatalogException
  {
    try
    {
      // Validate HUID
      if (!huid.equals(mediaLocationId.getHuid()))
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "getMetaData", "unknow HUID, " + huid + " " +  mediaLocationId.getHuid());
        
        // Throw exception
        throw new HaviMediaCatalogInvalidParameterException("unknow HUID, " + huid + " " +  mediaLocationId.getHuid());
      }
      
      // Retrieve the item index
      ItemIndex[] index = avDiscClient.getItemListSync(0, mediaLocationId.getList());
      
//      // Patch up the item index because of bugs in the SonyJukeboxFcm
//      for (short i = 0; i < index.length; i++)
//      {
//        index[i].setList(mediaLocationId.getList());
//        index[i].setIndex(i);
//      }
      
      // Check index
      if (mediaLocationId.getIndex() >= index.length)
      {
        // Bad index
        return new MediaMetaData[0];
      }
      
      // Build new item index is just for a track
      if (mediaLocationId.getIndex() > 0)
      {
        ItemIndex[] temp = new ItemIndex[1];
        temp[0] = index[mediaLocationId.getIndex()];
        index = temp;
      }
      
      // Convert and return the meta data
      return toMediaMetaData(index);
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    catch (HaviAvDiscException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#putMetaData(com.streetfiresound.mediamanager.catalog.types.MLID, int, com.streetfiresound.mediamanager.catalog.types.MediaMetaData[])
   */
  public void putMetaData(MediaMetaData[] mediaMetaData) throws HaviMediaCatalogException
  {
    try
    {
      // Forward to fcm
      avDiscClient.putItemListSync(0, mediaMetaData[0].getMediaLocationId().getList(), toItemIndex(mediaMetaData));
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    catch (HaviAvDiscException e)
    {
      // Translate
      throw new HaviMediaCatalogAdaptorFailureException(e.toString());
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
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#expand(com.streetfiresound.mediamanager.mediacatalog.types.MLID)
   */
  public MLID[] expand(MLID mediaLocationId) throws HaviMediaCatalogException
  {
    throw new HaviMediaCatalogNotImplementedException("expand");
  }
  
  public void avDiscItemListChangedEventNotification(SEID posterSeid, short listNumber)
  {
    // Verify that this is for us
    if (!remoteSeid.equals(posterSeid))
    {
      // Not for us
      return;
    }

    // Create MLID
    MLID mlid = new MLID(huid, listNumber, (short)0);
    
    // Dispatch changed
    dispatchChangedMediaItem(mlid);
    
    // All done
    return;
  }
  
  /**
   * Translate an ItemIndex to to a MediaMetaData
   * @param itemIndex The ItemIndex to convert
   * @return The new MediaMetaData
   */
  private final MediaMetaData toMediaMetaData(ItemIndex itemIndex)
  {
    // Create the meta data
    MediaMetaData metaData = new MediaMetaData();
    
    // Move item index information
    metaData.setMediaLocationId(new MLID(huid, itemIndex.getList(), itemIndex.getIndex()));
    metaData.setMediaType(ConstMediaItemType.CDDA);
    metaData.setTitle(itemIndex.getTitle());
    metaData.setArtist(itemIndex.getArtist());
    metaData.setGenre(itemIndex.getGenre());
    metaData.setContentSize(itemIndex.getContentSize());
    metaData.setPlaybackTime(itemIndex.getPlaybackTime());
    metaData.setInitialTimeStamp(itemIndex.getInitialTimeStamp());
    metaData.setLastUpdateTimeStamp(itemIndex.getLastUpdateTimeStamp());
    
    // Return meta data
    return metaData;
  }
  
  /**
   * Translate an ItemIndex array to a MediaMetaData array
   * @param huid The HUID of this device
   * @param itemIndex The ItemIndex array to convert
   * @return The new MediaMetaData array
   */
  private final MediaMetaData[] toMediaMetaData(ItemIndex[] itemIndex)
  {
    // Create media meta data
    MediaMetaData[] metaData = new MediaMetaData[itemIndex.length];
    
    // Build the meta data
    for (int i = 0; i < metaData.length; i++)
    {
      // Translate index
      metaData[i] = toMediaMetaData(itemIndex[i]);
    }
    
    // Return meta data
    return metaData;
  }
  
  /**
   * Translate a MediaMetaData to an ItemIndex
   * @param metaData The MediaMetaData to conver
   * @return The converted ItemIndex
   */
  private final ItemIndex toItemIndex(MediaMetaData metaData)
  {
    // Create the ItemIndex
    ItemIndex index = new ItemIndex();
    
    // Move item index information
    index.setList(metaData.getMediaLocationId().getList());
    index.setIndex(metaData.getMediaLocationId().getIndex());
    index.setTitle(metaData.getTitle());
    index.setArtist(metaData.getArtist());
    index.setGenre(metaData.getGenre());
    index.setContentSize(metaData.getContentSize());
    index.setPlaybackTime(metaData.getPlaybackTime());
    index.setInitialTimeStamp(metaData.getInitialTimeStamp());
    index.setLastUpdateTimeStamp(metaData.getLastUpdateTimeStamp());
    index.setContentType("CDDA");
    
    // Return meta data
    return index;
  }
  
  /**
   * Translate a MediaMetaData array to an ItemIndex array
   * @param metaData The MediaMetaData to convert
   * @return The new ItemIndex array
   */
  private final ItemIndex[] toItemIndex(MediaMetaData[] metaData)
  {
    // Create the ItemIndex array
    ItemIndex[] index = new ItemIndex[metaData.length];
    
    // Build the ItemIndex Array
    for (int i = 0; i < index.length; i++)
    {
      index[i] = toItemIndex(metaData[i]);
    }
    
    // Return the new item index
    return index;
  }


  public void handleGetItemList(int transactionId, ItemIndex[] result, Status returnCode)
  {
    try
    {
      // Check the transaction ID
      if (retrieveTransactionId != transactionId)
      {
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "handleGetDiscSummaries", "transaction mismatch: " + retrieveTransactionId + "<>" + transactionId);
        
        // Drop it
        return;
      }
      
      // Check return code
      if (!returnCode.equals(AvDiscConstant.SUCCESS))
      {
        // Log the error
        LoggerSingleton.logError(this.getClass(), "handleGetDiscSummaries", returnCode.toString());
        
        // Drop response
        return;
      }
      
      // Allocate new translated media summaries, the result is capcity + 1, toss 0 index away
      MediaMetaData[] mediaSummaries = new MediaMetaData[result.length - 1];
      
      // Build the MediaSummary
      for (int i = 1; i < result.length; i++)
      {
//        //XXX:0000:stephen:20050113:Patch for problem with getDiscSummary index being set wrong
//        result[i].setIndex((short)0);
        
        // Create MLID
        MLID location = new MLID(huid, result[i].getList(), result[i].getIndex());
        
        // Create summary
        mediaSummaries[i - 1] = toMediaMetaData(result[i]);
      }
      
      // Post the event
      dispatchRetrievedMediaSummaries(mediaSummaries);
    }
    finally
    {
      // Clear the transaction ID
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
        LoggerSingleton.logWarning(this.getClass(), "timeout", "timeout retrieving disc summaries, try again");
        
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
}
