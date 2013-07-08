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
 * $Id: PlayListCatalog.java,v 1.3 2005/03/20 00:20:27 stephen Exp $
 */
package com.streetfiresound.mediamanager.playlist;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.havi.dcm.types.HUID;
import org.havi.system.types.DateTime;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;

import com.redrocketcomputing.havi.util.TimeDateUtil;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.concurrent.ReadWriteLock;
import com.redrocketcomputing.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import com.redrocketcomputing.util.concurrent.Sync;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogBadMlidException;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogException;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogInvalidParameterException;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogIoFailureException;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogUnidentifiedFailureException;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayList;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayListMetaData;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class PlayListCatalog
{
  private static class MediaMetaDataFilter implements FileFilter
  {
    public boolean accept(File pathname)
    {
      return pathname.getName().endsWith(".plm");
    }
  }
 
  private final static int BLOCK_SIZE = 4096;
  private final static int ENTRY_SIZE = 100;
  
  private ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();
  private Sync readLock = lock.readLock();
  private Sync writeLock = lock.writeLock();
  private List listenerList = new ArrayList();
  private String path;
  private Map catalog = new ListMap();
  private short lastPlayListId = 0;
  private HUID catalogHuid;
  
  /**
   * 
   */
  public PlayListCatalog(String path, HUID catalogHuid)
  {
    // Check parameter
    if (path == null || catalogHuid == null)
    {
      // Badness
      throw new IllegalArgumentException("path or HUID is null");
    }
    
    // Save the parameters
    this.path = path;
    this.catalogHuid = catalogHuid;
    
    // Load the catalog
    loadCatalog();
  }

  /**
   * Add a PlayListCatalogEventListener
   * @param listener The listener to add
   */
  public void addListener(PlayListCatalogEventListener listener)
  {
    synchronized(listenerList)
    {
      // Add the listener
      listenerList.add(listener);
    }
  }
  
  /**
   * Remove a PlayListCatalogEventListener
   * @param listener The listener to remove
   */
  public void removeListener(PlayListCatalogEventListener listener)
  {
    synchronized(listenerList)
    {
      // Remove all match listener
      while (listenerList.remove(listener));
    }
  }
  
  /**
   * Return summary of play lists
   * @return 
   * @throws HaviPlayListCatalogException
   */
  public PlayListMetaData[] getMetaData(MLID mediaLocationId) throws HaviPlayListCatalogException
  {
    try
    {
      // Lock the catalog for reading
      readLock.acquire();
      
      // Check for single item only
      if (mediaLocationId.getList() != 0)
      {
        // Try to get the requested item
        PlayListMetaData[] result = new PlayListMetaData[1];
        result[0] = (PlayListMetaData)catalog.get(mediaLocationId);
        if (result[0] == null)
        {
          // Bad MLID
          throw new HaviPlayListCatalogBadMlidException(mediaLocationId.toString());
        }
        
        // Return it
        return result;
      }
      
      // Return entire catalog
      return (PlayListMetaData[])catalog.values().toArray(new PlayListMetaData[catalog.size()]);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviPlayListCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Always release log
      readLock.release();
    }
    
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#createPlayList(com.streetfiresound.mediamanager.playlist.types.PlayListItem[])
   */
  public MLID createPlayList(PlayList playList) throws HaviPlayListCatalogException
  {
    try
    {
      // Lock the catalog for writing
      writeLock.acquire();
      
      // Create MLID
      lastPlayListId++;
      MLID mediaLocationId = new MLID(catalogHuid, lastPlayListId, (short)0);
      
      try
      {
        // Create PlayListMetadata 
        DateTime currentDateTime = TimeDateUtil.getCurrentDateTime();
        PlayListMetaData metaData = new PlayListMetaData();
        metaData.setIrcode(playList.getIrcode());
        metaData.setTitle(playList.getTitle());
        metaData.setArtist(playList.getArtist());
        metaData.setGenre(playList.getGenre());
        metaData.setPlaybackTime(playList.getPlaybackTime());
        metaData.setInitialTimeStamp(currentDateTime);
        metaData.setLastUpdateTimeStamp(currentDateTime);
        metaData.setMediaLocationId(mediaLocationId);
        
        // Write meta data
        writePlayListMetaData(mediaLocationId, metaData);
        
        // Write play list
        writePlayListItems(mediaLocationId, playList.getContent());
        
        // Add to catalog
        catalog.put(mediaLocationId, metaData);
      }
      catch (HaviPlayListCatalogIoFailureException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "createPlayList", e.toString());
        
        // Cleanup
        remove(mediaLocationId);
      }
      finally
      {
        // Release lock
        writeLock.release();
      }

      // File event
      dispatch(mediaLocationId);
      
      // Return the play list id
      return mediaLocationId;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviPlayListCatalogUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#getPlayList(short)
   */
  public PlayList getPlayList(MLID mediaLocationId) throws HaviPlayListCatalogException
  {
    try
    {
      // Lock for read
      readLock.acquire();
      
      // Check catalog for entry
      PlayListMetaData metaData = (PlayListMetaData)catalog.get(mediaLocationId);
      if (metaData == null)
      {
        // Opps
        throw new HaviPlayListCatalogInvalidParameterException("bad PlayListId: " + (mediaLocationId.getList() & 0xffff));
      }
      
      // Read play contents
      MLID[] contents = readPlayListItems(mediaLocationId);
      
      // Return the play list
      PlayList playList = new PlayList();
      playList.setTitle(metaData.getTitle());
      playList.setArtist(metaData.getArtist());
      playList.setGenre(metaData.getGenre());
      playList.setPlaybackTime(metaData.getPlaybackTime());
      playList.setInitialTimeStamp(metaData.getInitialTimeStamp());
      playList.setLastUpdateTimeStamp(metaData.getLastUpdateTimeStamp());
      playList.setContent(contents);
      return playList;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviPlayListCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      // Alway release lock
      readLock.release();
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#removePlayList(short)
   */
  public void removePlayList(MLID mediaLocationId) throws HaviPlayListCatalogException
  {
    try
    {
      // Lock for write
      writeLock.acquire();

      try
      {
        // Check catalog for entry
        if (!catalog.containsKey(mediaLocationId))
        {
          // Opps
          throw new HaviPlayListCatalogInvalidParameterException("bad PlayListId: " + (mediaLocationId.getList() & 0xffff));
        }

        // Remove the playlist
        remove(mediaLocationId);
      }
      finally
      {
        // Alway release lock
        writeLock.release();
      }

      // Fire event
      dispatch(mediaLocationId);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviPlayListCatalogUnidentifiedFailureException(e.toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#updatePlayList(short, com.streetfiresound.mediamanager.playlist.types.PlayListItem[])
   */
  public void updatePlayList(MLID mediaLocationId, PlayList playList) throws HaviPlayListCatalogException
  {
    try
    {
      // Lock for write
      writeLock.acquire();

      try
      {
        // Check catalog for entry
        if (!catalog.containsKey(mediaLocationId))
        {
          // Opps
          throw new HaviPlayListCatalogInvalidParameterException("bad PlayListId: " + (mediaLocationId.getList() & 0xffff));
        }

        // Create PlayListMetadata 
        PlayListMetaData metaData = new PlayListMetaData();
        metaData.setIrcode(playList.getIrcode());
        metaData.setTitle(playList.getTitle());
        metaData.setArtist(playList.getArtist());
        metaData.setGenre(playList.getGenre());
        metaData.setPlaybackTime(playList.getPlaybackTime());
        metaData.setInitialTimeStamp(playList.getInitialTimeStamp());
        metaData.setLastUpdateTimeStamp(TimeDateUtil.getCurrentDateTime());
        metaData.setMediaLocationId(mediaLocationId);
        
        // Write meta data
        writePlayListMetaData(mediaLocationId, metaData);
        
        // Write play list
        writePlayListItems(mediaLocationId, playList.getContent());
        
        // Add to catalog
        catalog.put(mediaLocationId, metaData);
      }
      finally
      {
        // Alway release lock
        writeLock.release();
      }

      // Fire event
      dispatch(mediaLocationId);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviPlayListCatalogUnidentifiedFailureException(e.toString());
    }
  }
  
  /**
   * Return the current IR Code Map
   * @return The array of 256 MLID to IR Codes
   * @throws HaviPlayListCatalogException
   */
  public MLID[] getIrCodeMap() throws HaviPlayListCatalogException
  {
    try
    {
      // Get read lock
      readLock.acquire();
      
      // Create ir code array
      MLID[] irCodeMap = new MLID[256];
      
      // Loop through catalog to build map
      for (Iterator iterator = catalog.entrySet().iterator(); iterator.hasNext();)
      {
        // Extract the entry
        Map.Entry entry = (Map.Entry)iterator.next();
        MLID mediaLocationId = (MLID)entry.getKey();
        PlayListMetaData metaData = (PlayListMetaData)entry.getValue();
        
        // Add to map incorrect position
        irCodeMap[metaData.getIrcode() & 0xff] = mediaLocationId; 
      }
      
      // Return the map
      return irCodeMap;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviPlayListCatalogUnidentifiedFailureException(e.toString());
    }
    finally
    {
      readLock.release();
    }
  }
  
  /**
   * Dispatch event
   * 
   * @param hint The PlayListId which has changed or zero if the catalog has changed
   */
  private final void dispatch(MLID hint)
  {
    synchronized(listenerList)
    {
      for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
      {
        // Extract the listener
        PlayListCatalogEventListener element = (PlayListCatalogEventListener)iterator.next();
        
        // dispatch
        element.playListCatalogChanged(hint);
      }
    }
  }
  
  /**
   * Read a PlayListItem file
   * @param playListId The PlayListId of the file to read
   * @return The contents of the PlayListItem file
   * @throws HaviPlayListCatalogIoFailureException Throw is a problem is detected read the file
   */
  private MLID[] readPlayListItems(MLID playListId) throws HaviPlayListCatalogIoFailureException
  {
    int i = 0;
    try
    {
      // Create input stream
      FileInputStream fis = new FileInputStream(path + File.separator + Short.toString(playListId.getList()) + ".pli");
      BufferedInputStream bis = new BufferedInputStream(fis, BLOCK_SIZE);
      GZIPInputStream gzis = new GZIPInputStream(bis);
      DataInputStream dis = new DataInputStream(gzis);
    
      // Read the entries
      int size = dis.readInt();
      MLID[] entries = new MLID[size];
      byte[] entryBuffer = new byte[ENTRY_SIZE];
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(entryBuffer);
      for (i = 0; i < entries.length; i++)
      {
        // Reset the HAVI input stream
        hbais.reset();
        
        // Read the entry
        int entrySize = dis.readInt();
        dis.readFully(entryBuffer, 0, entrySize);
        
        // Unmarshall the item
        entries[i] = new MLID(hbais);
      }
      
      // Alway close the file
      dis.close();

      // Return the array
      return entries;
    }
    catch (HaviUnmarshallingException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "readPlayListItems", "on " + i + "," + e.toString());

      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
    catch (IOException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "readPlayListItems", "on " + i + "," + e.toString());
      
      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
  }
  
  /**
   * Write the PlayListItem entries to a file
   * @param playListId The PlayListId of the entries to write
   * @param entries The MLID array to write
   * @throws HaviPlayListCatalogIoFailureException Thrown if an error writing to the file is detected
   */
  private void writePlayListItems(MLID playListId, MLID[] entries) throws HaviPlayListCatalogIoFailureException
  {
    try
    {
      // Create output stream
      FileOutputStream fos = new FileOutputStream(path + File.separator + Short.toString(playListId.getList()) + ".pli");
      BufferedOutputStream bos = new BufferedOutputStream(fos, BLOCK_SIZE);
      GZIPOutputStream gzos = new GZIPOutputStream(bos);
      DataOutputStream dos = new DataOutputStream(gzos);
    
      // Write the enteries
      LoggerSingleton.logDebugCoarse(this.getClass(), "writePlayListItems", entries.length + " items");
      dos.writeInt(entries.length);
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(ENTRY_SIZE);
      for (int i = 0; i < entries.length; i++)
      {
        // Reset the output stream
        hbaos.reset();
        
        // Marshall the entry
        entries[i].marshal(hbaos);
        
        // Write to file
        dos.writeInt(hbaos.size());
        dos.write(hbaos.toByteArray());
      }
      
      // Close the file
      dos.close();
    }
    catch (HaviMarshallingException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "writePlayListItems", e.toString());

      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
    catch (IOException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "writePlayListItems", e.toString());

      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
  }
  
  /**
   * Read a MediaMetaData associated with the playlist
   * @param playListId The play list to read
   * @return The PlayListMetaData associated with the playlist
   * @throws HaviPlayListCatalogIoFailureException Thrown if a problem read the meta data is detected
   */
  private PlayListMetaData readPlayListMetaData(MLID playListId) throws HaviPlayListCatalogIoFailureException
  {
    try
    {
      // Create input stream
      FileInputStream fis = new FileInputStream(path + File.separator + Short.toString(playListId.getList()) + ".plm");
      BufferedInputStream bis = new BufferedInputStream(fis, BLOCK_SIZE);
      DataInputStream dis = new DataInputStream(bis);
    
      // Read a meta data buffer
      int size = dis.readInt();
      byte[] buffer = new byte[size];
      dis.readFully(buffer);
    
      // Unmarshall
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(buffer);
      PlayListMetaData metaData = new PlayListMetaData(hbais);
      
      // Alway close the file
      dis.close();

      // Return the meta data
      return metaData;
    }
    catch (HaviUnmarshallingException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "readPlayListMetaData", e.toString());

      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
    catch (IOException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "readPlayListMetaData", e.toString());

      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
  }
  
  /**
   * Write the MediaMetaData associated with the playlist
   * @param playListId The play list to write the meta data for
   * @param metaData The PlayMetaData to write
   * @throws HaviPlayListCatalogIoFailureException Thrown if a problem is detected writing the meta data
   */
  private void writePlayListMetaData(MLID playListId, PlayListMetaData metaData) throws HaviPlayListCatalogIoFailureException
  {
    try
    {
      // Create output stream
      FileOutputStream fos = new FileOutputStream(path + File.separator + Short.toString(playListId.getList()) + ".plm");
      BufferedOutputStream bos = new BufferedOutputStream(fos, BLOCK_SIZE);
      DataOutputStream dos = new DataOutputStream(bos);
    
      // Convert to byte array
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(4096);
      metaData.marshal(hbaos);
      byte[] buffer = hbaos.toByteArray();
      
      // Write to the file
      dos.writeInt(buffer.length);
      dos.write(buffer);

      // Close the file
      dos.flush();
      dos.close();
    }
    catch (HaviMarshallingException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "writePlayListMetaData", e.toString());

      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
    catch (IOException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "writePlayListMetaData", e.toString());

      // Translate
      throw new HaviPlayListCatalogIoFailureException(e.toString());
    }
  }
  
  /**
   * Rebuild the playlist catalog map
   */
  private void loadCatalog()
  {
    // Clear the catalog
    catalog.clear();
    lastPlayListId = 0;
    
    // Build array of playlist metadata files
    File root = new File(path);
    File[] playListFiles = root.listFiles(new MediaMetaDataFilter());
    if (playListFiles == null)
    {
      // No play lists yet
      return;
    }
    
    // Build catalog
    for (int i = 0; i < playListFiles.length; i++)
    {
      try
      {
        // Get the file name
        String fileName = playListFiles[i].getName().substring(0, playListFiles[i].getName().indexOf('.'));
        LoggerSingleton.logDebugCoarse(this.getClass(), "loadCatalog", "load play list " + fileName);
        
        // Convert to MLID
        MLID playListId = new MLID(catalogHuid, Short.parseShort(fileName), (short)0);
        
        // Read the meta data
        PlayListMetaData metaData = readPlayListMetaData(playListId);
        
        // Add to the map
        catalog.put(playListId, metaData);
        
        // Check for bigger play list id
        lastPlayListId = (short)Math.max(lastPlayListId & 0xffff, playListId.getList() & 0xffff);
        LoggerSingleton.logDebugCoarse(this.getClass(), "loadCatalog", "last play is " + lastPlayListId);
      }
      catch (NumberFormatException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "loadCatalog", e.toString());
      }
      catch (HaviPlayListCatalogIoFailureException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "loadCatalog", e.toString());
      }
    }
  }
  
  /**
   * Remove all traces of the specified playlist 
   * @param playListId The play list to remove
   */
  private void remove(MLID playListId)
  {
    // Remove from map
    catalog.remove(playListId);
    
    // Remove meta data
    File metaDataFile = new File(path + File.separator + Short.toString(playListId.getList()) + ".plm");
    metaDataFile.delete();
    
    // Remove play list data
    File itemFile = new File(path + File.separator + Short.toString(playListId.getList()) + ".pli");
    itemFile.delete();
  }
  
}
