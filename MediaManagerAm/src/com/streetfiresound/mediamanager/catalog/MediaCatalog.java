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
 * $Id: MediaCatalog.java,v 1.9 2005/03/21 22:26:03 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.havi.dcm.types.HUID;

import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.concurrent.ReadWriteLock;
import com.redrocketcomputing.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import com.redrocketcomputing.util.concurrent.Sync;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstCategoryType;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaItemType;
import com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary;
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
/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class MediaCatalog implements DeviceAdaptorEventListener
{
  private List listenerList = new ArrayList();
  private ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();
  private Sync readLock = lock.readLock();
  private Sync writeLock = lock.writeLock();
  private Map mediaMetaDataCache = new HashMap();
  private CategoryMap[] categoryMaps = {new CategoryMap(ConstCategoryType.TYPE), new CategoryMap(ConstCategoryType.GENRE), new CategoryMap(ConstCategoryType.ARTIST)};
  private Map adaptorMap = new ListMap();
  private HUID localHuid;

  public MediaCatalog(HUID localHuid)
  {
    // Check parameters
    if (localHuid == null)
    {
      throw new IllegalArgumentException("HUID is null");
    }
    
    // Save the parameters
    this.localHuid = localHuid;
  }
  
  /**
   * Add a MediaCatalogEventListener
   * @param listener The listener to add
   */
  public void addListener(MediaCatalogEventListener listener)
  {
    synchronized(listenerList)
    {
      // Add the listener
      listenerList.add(listener);
    }
  }
  
  /**
   * Remove a MediaCatalogEventListener
   * @param listener The listener to remove
   */
  public void removeListener(MediaCatalogEventListener listener)
  {
    synchronized(listenerList)
    {
      // Remove all match listener
      while (listenerList.remove(listener));
    }
  }
  
  /**
   * @param adaptor
   */
  public void addAdaptor(DeviceAdaptor adaptor)
  {
    try
    {
      writeLock.acquire();
      
      // Check for duplicate
      if (adaptorMap.containsKey(adaptor))
      {
        // Log warning and drop
        LoggerSingleton.logWarning(this.getClass(), "addAdaptor", "already have: " + adaptor.getHuid());
        
        // Drop
        return;
      }
      
      // Bind to adaptor
      adaptor.addListener(this);
      
      // Add to the map
      adaptorMap.put(adaptor.getHuid(), adaptor);
      
      // Retreive summaries
      adaptor.retrieveSummaries();
    }
    catch (InterruptedException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "addAdaptor", e.toString());
    }
    catch (HaviMediaCatalogException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "addAdaptor", e.toString());
    }
    finally
    {
      // Alway release the lock
      writeLock.release();
    }
    
    // Dispatch event
    dispatch(new MLID(adaptor.getHuid(), (short)0, (short)0));
  }
  
  /**
   * @param adaptor
   */
  public void removeAdaptor(DeviceAdaptor adaptor)
  {
    try
    {
      // Lock the catalog
      writeLock.acquire();
      
      // Try to remove adaptor
      adaptorMap.remove(adaptor.getHuid());
      
      // Loop through meta data removing from the map
      for (Iterator iterator = mediaMetaDataCache.values().iterator(); iterator.hasNext();)
      {
        // Extract the entry
        MediaCatalogEntry element = (MediaCatalogEntry)iterator.next();
        
        // Check for match HUID
        if (adaptor.getHuid().equals(element.getAdaptor().getHuid()))
        {
          iterator.remove();
        }
      }
      
      // Flush from the type maps
      categoryMaps[ConstCategoryType.TYPE].flush(adaptor.getHuid());
      categoryMaps[ConstCategoryType.ARTIST].flush(adaptor.getHuid());
      categoryMaps[ConstCategoryType.GENRE].flush(adaptor.getHuid());
    }
    catch (InterruptedException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "addAdaptor", e.toString());
    }
    finally
    {
      // Alway release the lock
      writeLock.release();
    }
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptorEventListener#changedMediaItem(com.streetfiresound.mediamanager.catalog.DeviceAdaptor, com.streetfiresound.mediamanager.mediacatalog.types.MLID)
   */
  public void changedMediaItem(DeviceAdaptor adaptor, MLID mediaLocationId)
  {
    try
    {
      // Write lock the catalog
      writeLock.acquire();
      
      // Remove the old MLID
      mediaMetaDataCache.remove(mediaLocationId);
      categoryMaps[ConstCategoryType.TYPE].remove(mediaLocationId);
      categoryMaps[ConstCategoryType.ARTIST].remove(mediaLocationId);
      categoryMaps[ConstCategoryType.GENRE].remove(mediaLocationId);
      
      // Check for local HUID and retreive summaring instead of adding
      if (adaptor.getHuid().equals(localHuid))
      {
        // Retreive summaries
        adaptor.retrieveSummaries();
        
        // All done
        return;
      }
      
      // Create root MLID
      MLID root = new MLID(mediaLocationId.getHuid(), mediaLocationId.getList(), (short)0);
      
      // Create new entry
      MediaCatalogEntry entry = new MediaCatalogEntry(adaptor, root);

      // Add the entry
      mediaMetaDataCache.put(root, entry);
      categoryMaps[ConstCategoryType.TYPE].put(entry.get(0).getMediaType(), root);
      categoryMaps[ConstCategoryType.ARTIST].put(entry.get(0).getArtist(), root);
      categoryMaps[ConstCategoryType.GENRE].put(entry.get(0).getGenre(), root);
    }
    catch (HaviMediaCatalogException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "changedMediaItem", e.toString());
    }
    catch (InterruptedException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "changedMediaItem", e.toString());
    }
    finally
    {
      // unlock
      writeLock.release();
    }
    
    // dispatch
    dispatch(mediaLocationId);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptorEventListener#retrievedMediaSummaries(com.streetfiresound.mediamanager.catalog.DeviceAdaptor, com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData[])
   */
  public void retrievedMediaSummaries(DeviceAdaptor adaptor, MediaMetaData[] summaries)
  {
    try
    {
      // Lock the catalog
      writeLock.acquire();
      
      // Loop through the summaries adding them
      for (int i = 0; i < summaries.length; i++)
      {
        try
        {
          // Create the new entry
          MediaCatalogEntry entry = new MediaCatalogEntry(adaptor, summaries[i]);
          
          // Add it
          mediaMetaDataCache.put(summaries[i].getMediaLocationId(), entry);
          categoryMaps[ConstCategoryType.TYPE].put(summaries[i].getMediaType(), summaries[i].getMediaLocationId());
          categoryMaps[ConstCategoryType.ARTIST].put(summaries[i].getArtist(), summaries[i].getMediaLocationId());
          categoryMaps[ConstCategoryType.GENRE].put(summaries[i].getGenre(), summaries[i].getMediaLocationId());
        }
        catch (HaviMediaCatalogException e)
        {
          // Just log the error and continue
          LoggerSingleton.logError(this.getClass(), "retrievedMediaSummaries", e.toString());
        }
      }
    }
    catch (InterruptedException e)
    {
      // Just log the error and continue
      LoggerSingleton.logError(this.getClass(), "retrievedMediaSummaries", e.toString());
    }
    finally
    {
      // Alway unlock
      writeLock.release();
    }

    // Dispatch event
    dispatch(new MLID(adaptor.getHuid(), (short)0, (short)0));
  }

  /**
   * Retrieve the MediaMetaData associated with the specified MLID
   * @param mediaLocationId The MLID of the MediaMetaData to retrieve
   * @return The MediaMetaData associated with the MLID
   * @throws HaviMediaCatalogException Throw is a locking error is detected
   */
  public MediaMetaData[] getMetaData(MLID mediaLocationId) throws HaviMediaCatalogException
  {
    try
    {
      // Lock for read
      readLock.acquire();
      
      // Get the entry
      MediaCatalogEntry entry = (MediaCatalogEntry)mediaMetaDataCache.get(new MLID(mediaLocationId.getHuid(), mediaLocationId.getList(), (short)0));
      if (entry == null)
      {
        return new MediaMetaData[0];
      }
      
      // Check for playlist
      if (entry.get(0).getMediaType().equals(ConstMediaItemType.PLAY_LIST))
      {
        // Expand content
        MLID[] content = entry.getAdaptor().expand(mediaLocationId);
        
        // Return result
        return getMultipleMetaData(entry.getAdaptor().expand(mediaLocationId));
      }
      
      // Check for non root request
      if (mediaLocationId.getIndex() != 0)
      {
        // Return single entry
        MediaMetaData[] result = new MediaMetaData[1];
        result[0] = entry.get(mediaLocationId.getIndex()); 
        return result;
      }
      
      // Return complete list
      return entry.get();
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Alway unlock
      readLock.release();
    }
  }
  
  /**
   * @param mediaLocationIdList
   * @return
   * @throws HaviMediaCatalogException
   */
  public MediaMetaData[] getMultipleMetaData(MLID[] mediaLocationIdList) throws HaviMediaCatalogException
  {
    try
    {
      // Lock the catalog
      readLock.acquire();
      
      // Loop though the MLIDs and create meta data list
      List list = new ArrayList(mediaLocationIdList.length);
      MLID root = new MLID();
      for (int i = 0; i < mediaLocationIdList.length; i++)
      {
        // Lookup root entry
        root.setHuid(mediaLocationIdList[i].getHuid());
        root.setList(mediaLocationIdList[i].getList());
        MediaCatalogEntry entry = (MediaCatalogEntry)mediaMetaDataCache.get(root);
        if (entry != null)
        {
          // Found good one, add it
          list.add(entry.get(mediaLocationIdList[i].getIndex()));
        }
        else
        {
          // Add missing
          MediaMetaData missing = new MediaMetaData();
          missing.setMediaLocationId(mediaLocationIdList[i]);
          missing.setTitle("MISSING");
          missing.setGenre("MISSING");
          missing.setArtist("MISSING");
          missing.setMediaType("MISSING");
          list.add(missing);
        }
      }
      
      // Return the array
      return (MediaMetaData[])list.toArray(new MediaMetaData[list.size()]);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Alway unlock
      readLock.release();
    }
  }
  
  /**
   * @param mediaMetaData
   * @throws HaviMediaCatalogException
   */
  public void putMetaData(MediaMetaData[] mediaMetaData) throws HaviMediaCatalogException
  {
    try
    {
      // Lock
      writeLock.acquire();
      
      // Find entry
      MediaCatalogEntry entry = (MediaCatalogEntry)mediaMetaDataCache.get(mediaMetaData[0].getMediaLocationId());
      
      // Ask entry to update
      entry.put(mediaMetaData);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Release the load
      writeLock.release();
    }
  }

  /**
   * @param type
   * @return
   * @throws HaviMediaCatalogException
   */
  public CategorySummary[] getCategorySummary(int type) throws HaviMediaCatalogException
  {
    // Range check the type
    if (type < 0 || type > ConstCategoryType.ARTIST)
    {
      // Bad
      throw new HaviMediaCatalogInvalidParameterException("bad type: " + type);
    }
    
    try
    {
      // Lock
      readLock.acquire();
      
      // Return summary
      return categoryMaps[type].getSummaries();
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }

  /**
   * @param type
   * @param value
   * @return
   * @throws HaviMediaCatalogException
   */
  public MLID[] getMediaSummary(int type, String value) throws HaviMediaCatalogException
  {
    // Range check the type
    if (type < 0 || type > ConstCategoryType.ARTIST)
    {
      // Bad
      throw new HaviMediaCatalogInvalidParameterException("bad type: " + type);
    }
    
    try
    {
      // Lock
      readLock.acquire();
      
      // Return summary
      return categoryMaps[type].get(value);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }

  /**
   * @param contains
   * @return
   * @throws HaviMediaCatalogException
   */
  public MediaMetaData[] searchMetaData(String contains) throws HaviMediaCatalogException
  {
    try
    {
      // Lock
      readLock.acquire();
      
      // Loop through the cache looking for matches
      List result = new ArrayList(mediaMetaDataCache.size() * 15);
      for (Iterator iterator = mediaMetaDataCache.values().iterator(); iterator.hasNext();)
      {
        // Extract element
        MediaCatalogEntry element = (MediaCatalogEntry)iterator.next();
        
        // Add search results
        result.addAll(element.search(contains));
      }
      
      // Return results
      return (MediaMetaData[])result.toArray(new MediaMetaData[result.size()]);
      
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }
  
  /**
   * Dispatch event
   * @param hint The MLID which caused the event
   */
  private final void dispatch(MLID hint)
  {
    synchronized(listenerList)
    {
      for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
      {
        // Extract the listener
        MediaCatalogEventListener element = (MediaCatalogEventListener)iterator.next();
        
        // dispatch
        element.changedMediaCatalog(hint);
      }
    }
  }
}
