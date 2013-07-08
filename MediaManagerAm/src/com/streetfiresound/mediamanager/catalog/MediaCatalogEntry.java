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
 * $Id: MediaCatalogEntry.java,v 1.6 2005/03/20 00:20:27 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;

import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogBadMlidIndexException;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogException;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogUnidentifiedFailureException;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
class MediaCatalogEntry
{
  private MLID mediaLocationId;
  private DeviceAdaptor adaptor;
  private byte[] compressedData = null;
  private MediaMetaData[] uncompressedData = null;

  /**
   * Compress the MediaMetaData
   * 
   * @param uncompressedData The MediaMetaData to compress
   * @return Byte array containing the compressed data
   * @throws HaviMediaCatalogException Thrown if the is a problem compressing the data
   */
  public static byte[] compress(MediaMetaData[] uncompressedData) throws HaviMediaCatalogException
  {
    try
    {
      // Convert to byte array
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
      hbaos.writeInt(uncompressedData.length);
      for (int i = 0; i < uncompressedData.length; i++)
      {
        uncompressedData[i].marshal(hbaos);
      }

      // Create output stream
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzos = new GZIPOutputStream(baos);
      DataOutputStream dos = new DataOutputStream(gzos);

      // Compress data
      dos.writeInt(hbaos.size());
      dos.write(hbaos.toByteArray());

      // Close the file and return data
      dos.close();
      return baos.toByteArray();
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    catch (HaviMarshallingException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Uncompress the MediaMetaData array
   * 
   * @param compressedData The compress byte array
   * @return The uncompressed MediaMetaData
   * @throws HaviMediaCatalogException Throw if a problem uncompressing the MediaMetaData is detected
   */
  public static MediaMetaData[] uncompress(byte[] compressedData) throws HaviMediaCatalogException
  {
    try
    {
      // Build input stream pipeline
      ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
      GZIPInputStream gzis = new GZIPInputStream(bais);
      DataInputStream dis = new DataInputStream(gzis);

      // Uncompress buffer
      int size = dis.readInt();
      byte[] buffer = new byte[size];
      dis.readFully(buffer, 0, size);

      // Unmarshall the metadata
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(buffer);
      MediaMetaData[] uncompressedData = new MediaMetaData[hbais.readInt()];
      for (int i = 0; i < uncompressedData.length; i++)
      {
        uncompressedData[i] = new MediaMetaData(hbais);
      }

      // All done
      return uncompressedData;
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
    catch (HaviUnmarshallingException e)
    {
      // Translate
      throw new HaviMediaCatalogUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Construct a MediaCatalogEntry with just the summary information
   * 
   * @param adaptor The adaptor which can retrieve this data
   * @param summary The summary MediaMetaData
   */
  public MediaCatalogEntry(DeviceAdaptor adaptor, MediaMetaData summary) throws HaviMediaCatalogException
  {
    // Save the parameters
    this.adaptor = adaptor;
    this.mediaLocationId = summary.getMediaLocationId();
    uncompressedData = new MediaMetaData[1];
    uncompressedData[0] = summary;

    // Compress the data
    compressedData = MediaCatalogEntry.compress(uncompressedData);
    uncompressedData = null;
  }

  /**
   * Construct a MediaCatalogEntry
   * 
   * @param adaptor The adaptor which can retrieve this data
   * @param mediaMetaData The complete MediaMetaData for this entry
   */
  public MediaCatalogEntry(DeviceAdaptor adaptor, MLID mediaLocationId) throws HaviMediaCatalogException
  {
    // Save the parameters
    this.adaptor = adaptor;
    this.mediaLocationId = mediaLocationId;
  }

  public MediaMetaData[] get() throws HaviMediaCatalogException
  {
    try
    {
      // Check to see if we should retreive the meta data
      if (compressedData == null)
      {
        // Get the data
        uncompressedData = adaptor.getMetaData(mediaLocationId);
        
        // Compress it
        compressedData = compress(uncompressedData);
      }
      else
      {
        // Uncompress the data we have
        uncompressedData = uncompress(compressedData);
      }

      // Check to see if we need to get the remain metadata
      if (uncompressedData.length == 1)
      {
        // Yep, get it
        uncompressedData = adaptor.getMetaData(uncompressedData[0].getMediaLocationId());

        // Compress it
        compressedData = compress(uncompressedData);
      }

      // Return it
      return uncompressedData;
    }
    finally
    {
      // Alway release the uncompress data
      uncompressedData = null;
    }
  }

  public MediaMetaData get(int index) throws HaviMediaCatalogException
  {
    try
    {
      // Check to see if we should retreive the meta data
      if (compressedData == null)
      {
        // Get the data
        uncompressedData = adaptor.getMetaData(mediaLocationId);
        
        // Compress it
        compressedData = compress(uncompressedData);
      }
      else
      {
        // Uncompress the data we have
        uncompressedData = uncompress(compressedData);
      }

      // Check to see if we need to get the remain metadata
      if (uncompressedData.length == 1 && index != 0)
      {
        // Yep, get it
        uncompressedData = adaptor.getMetaData(uncompressedData[0].getMediaLocationId());

        // Compress it
        compressedData = compress(uncompressedData);
      }

      // Check range
      if (index > uncompressedData.length)
      {
        // Bad range
        throw new HaviMediaCatalogBadMlidIndexException("index: " + index);
      }

      // Create result
      MediaMetaData result = uncompressedData[index];

      // Return it
      return uncompressedData[index];
    }
    finally
    {
      // Release uncompress data
      uncompressedData = null;
    }
  }
  
  /**
   * Update the MediaMetaData and forward to the corresponding DeviceAdaptor
   * @param mediaMetaData The new MediaMetaData
   * @throws HaviMediaCatalogException Throw if a problem is detected updating the MediaMetaData
   */
  public void put(MediaMetaData[] mediaMetaData) throws HaviMediaCatalogException
  {
    // Check to see if we should retreive the meta data
    if (compressedData == null)
    {
      // Get the data
      uncompressedData = adaptor.getMetaData(mediaLocationId);
      
      // Compress it
      compressedData = compress(uncompressedData);
    }
    else
    {
      // Uncompress the data we have
      uncompressedData = uncompress(compressedData);
    }
    
    // Check to see if we need to get the remain metadata
    if (uncompressedData.length == 1)
    {
      // Yep, get it
      uncompressedData = adaptor.getMetaData(uncompressedData[0].getMediaLocationId());
    }
    
    // Loop through the new data merging it
    for (int i = 0; i < mediaMetaData.length; i++)
    {
      // Range check 
      if (mediaMetaData[i].getMediaLocationId().getIndex() < uncompressedData.length)
      {
        // Replace
        uncompressedData[mediaMetaData[i].getMediaLocationId().getIndex()] = mediaMetaData[i];
      }
    }

    // Send to the adaptor
    adaptor.putMetaData(uncompressedData);
    
    // Compress the data
    compressedData = compress(uncompressedData);
    
    // Release the uncomressed data
    uncompressedData = null;
  }
  
  /**
   * Search the text fields of the entry for substring which match
   * @param value The String to search for
   * @return List of matching MLID
   * @throws HaviMediaCatalogException
   */
  public List search(String value) throws HaviMediaCatalogException
  {
    try
    {
      // Check to see if we should retreive the meta data
      if (compressedData == null)
      {
        // Get the data
        uncompressedData = adaptor.getMetaData(mediaLocationId);
        
        // Compress it
        compressedData = compress(uncompressedData);
      }
      else
      {
        // Uncompress the data we have
        uncompressedData = uncompress(compressedData);
      }
      
      // Check to see if we need to get the remain metadata
      if (uncompressedData.length == 1)
      {
        // Yep, get it
        uncompressedData = adaptor.getMetaData(uncompressedData[0].getMediaLocationId());

        // Compress it
        compressedData = compress(uncompressedData);
      }
      
      // Search for value
      List result = new ArrayList(uncompressedData.length);
      for (int i = 0; i < uncompressedData.length; i++)
      {
        // Extract entry
        MediaMetaData entry = uncompressedData[i];
        
        // Check title for match
        if (entry.getTitle().indexOf(value) != -1)
        {
          // Found it
          result.add(entry);
          continue;
        }
        
        // Check artist
        if (entry.getArtist().indexOf(value) != -1)
        {
          // Found it
          result.add(entry);
          continue;
        }
        
        // Check genre
        if (entry.getGenre().indexOf(value) != -1)
        {
          // Found it
          result.add(entry);
          continue;
        }
      }
      
      // Return it
      return result;
    }
    finally
    {
      // Release uncompress data
      uncompressedData = null;
    }
  }
  
  public final DeviceAdaptor getAdaptor()
  {
    return adaptor;
  }
  
}