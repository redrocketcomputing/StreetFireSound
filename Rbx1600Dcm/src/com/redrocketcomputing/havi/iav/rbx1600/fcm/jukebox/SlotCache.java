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
 * $Id: SlotCache.java,v 1.3 2005/03/16 04:25:03 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.io.File;
import java.io.IOException;
import java.util.Observable;

import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.system.types.DateTime;

import com.redrocketcomputing.havi.constants.ConstRbx1600DcmRelease;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.havi.util.TimeDateUtil;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Red Rocket Computing, LLC
 * </p>
 * 
 * @author Stephen Street
 * @version 1.0
 */

class SlotCache extends Observable
{
  private final static long REMOTE_LOOKUP_TASK_KEEP_ALIVE = 5 * 60 * 1000;
  
  private class MessageRouter extends ProtocolEventAdaptor
  {
      /*
       * (non-Javadoc)
       * 
       * @see com.redrocketcomputing.havi.lav.sony.jukebox.protocol.ProtocolEventListener#handleMissingDisc(int)
       */
    public void handleMissingDisc(int disc)
    {
      try
      {
        // Set to empty
        ItemIndex[] empty = { new ItemIndex((short)0x0, (short)0, "EMPTY", "EMPTY", "EMPTY", "EMPTY", TimeDateUtil.TIMECODE_ZERO, 0, TimeDateUtil.DATETIME_ZERO, TimeDateUtil.DATETIME_ZERO) };
        empty[0].setList((short)disc);
        putItemIndex(empty);
      }
      catch (IOException e)
      {
        // Log the error
        LoggerSingleton.logError(this.getClass(), "handleMissingDisc", e.toString());
      }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.redrocketcomputing.havi.lav.sony.jukebox.protocol.ProtocolEventListener#handlePowerOff()
     */
    public void handlePowerOff()
    {
      try
      {
        // Flush the cache file
        cacheFile.flush();
      }
      catch (IOException e)
      {
        // Log the error
        LoggerSingleton.logError(this.getClass(), "handlePowerOff", e.toString());
      }
    }
  }

  private Protocol protocol;
  private int capacity;
  private String cachePath;
  private ItemIndexFile cacheFile;
  private String cacheFilename;
  private MessageRouter messageRouter = new MessageRouter();
  
  public SlotCache(Protocol protocol, int capacity, String cachePath)
  {
    // Check parameter
    if (protocol == null || cachePath == null)
    {
      // Badness
      throw new IllegalArgumentException("Protocol or cachePath is null");
    }
    
    // Save the parameters
    this.protocol = protocol;
    this.capacity = capacity;
    this.cachePath = cachePath;
    
    try
    {
      // Check for cache
      cacheFilename = cachePath + File.separator + Integer.toHexString(protocol.getDeviceId()) + '-' + capacity + ".cache"; 
      if (!(new File(cacheFilename)).exists())
      {
        // Cache not found, create it
        create(cacheFilename, capacity);
      }
      
      // Open the slot cache file
      cacheFile = new ItemIndexFile(cacheFilename);
      
      // Bind to the protocol stack
      protocol.addEventListener(messageRouter);
      
      // Log some information
      LoggerSingleton.logInfo(this.getClass(), "SlotCache", cacheFilename + " loaded " + capacity + " slots");
    }
    catch (IOException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "SlotCache", e.toString());
      
      // Translate
      throw new IllegalStateException("Error opening SlotCache " + cacheFilename + ' ' + e.toString());
    }
  }

  public void close()
  {
    // Unbind from stuff
    protocol.removeEventListener(messageRouter);
    
    // Close the cache file
    cacheFile.close();
  }

  public ItemIndex[] getItemIndex(int slot) throws IOException
  {
    // Check for non-root index
    if (slot != 0)
    {
      // Just read the index
      return read(slot);
    }

    // Create root index
    ItemIndex[] root = new ItemIndex[capacity + 1];
    root[0] = new ItemIndex();
    for (int i = 1; i < root.length; i++)
    {
      root[i] = read(i)[0];
    }
    
    // Return array
    return root;
  }
  
  public void putItemIndex(ItemIndex[] itemIndex) throws IOException
  {
    // Get current date and time
    DateTime currentDateTime = TimeDateUtil.getCurrentDateTime();
    DateTime initialDateTime = currentDateTime;
    DateTime updateDateTime = currentDateTime;

    // Retrieve existing information
    ItemIndex[] existing = read(itemIndex[0].getList());
    if (!existing[0].getContentType().equals("EMPTY"))
    {
      // Used existing initialial date and time
      initialDateTime = existing[0].getInitialTimeStamp();
    }
    
    // Update the timestamps
    for (int i = 0; i < itemIndex.length; i++)
    {
      itemIndex[i].setInitialTimeStamp(initialDateTime);
      itemIndex[i].setLastUpdateTimeStamp(updateDateTime);
    }

    // Write
    write(itemIndex[0].getList(), itemIndex);
    
    // Notify
    setChanged();
    notifyObservers(new Short(itemIndex[0].getList()));
    
    // All done
    return;
  }
  
  public boolean itemIndexEqual(ItemIndex[] lhs, ItemIndex[] rhs)
  {
    // True is disc id match
    return makeKey(lhs) == makeKey(rhs);
  }
  
  public void update(ItemIndex[] itemIndex)
  {
    try
    {
      // Read existing information
      ItemIndex[] existing = getItemIndex(itemIndex[0].getList());
      
      // Write new index if indexes do not match
      if (!itemIndexEqual(itemIndex, existing))
      {
        putItemIndex(itemIndex);
      }
    }
    catch (IOException e)
    {
      // Log error and drop
      LoggerSingleton.logError(this.getClass(), "update", e.toString());
    }
  }
  
  private int makeKey(ItemIndex[] itemIndex)
  {
    if (itemIndex.length == 0)
    {
      return 0;
    }
    
    int t = (itemIndex[0].getPlaybackTime().getHour() * 3600) + (itemIndex[0].getPlaybackTime().getMinute() * 60) + itemIndex[0].getPlaybackTime().getSec();

    int offset = 2;
    int n = 0;
    for (int i = 1; i < itemIndex.length; i++)
    {
      offset = offset + (itemIndex[i].getPlaybackTime().getHour() * 3600) + (itemIndex[i].getPlaybackTime().getMinute() * 60) + itemIndex[i].getPlaybackTime().getSec();
      n = n + sum(offset);
    }

    int nShift = (n % 0xff) << 24;
    int tShift = t << 8;
    return (n % 0xff) << 24 | t << 8 | itemIndex.length - 1;
  }

  private final int sum(int n)
  {
    int ret = 0;
    while (n > 0)
    {
      ret = ret + (n % 10);
      n = n / 10;
    }
    return ret;
  }

  private ItemIndex[] read(int blockNumber) throws IOException
  {
    synchronized(cacheFile)
    {
      try
      {
        // Seek to block
        cacheFile.seekBlock(blockNumber);
        
        ItemIndex[] itemIndex = cacheFile.read();
        return itemIndex;
      }
      catch (IOException e)
      {
        // Decorate the exception
        throw new IOException("read blockNumber " + blockNumber + ": " + e.getMessage());
      }
    }
  }

  private void write(int blockNumber, ItemIndex[] itemIndex) throws IOException
  {
    synchronized(cacheFile)
    {
      try
      {
        // Seek to block
        cacheFile.seekBlock(blockNumber);
        
        // Write
        cacheFile.write(itemIndex);
      }
      catch (IOException e)
      {
        // Decorate the exception
        throw new IOException("write blockNumber " + blockNumber + ": " + e.getMessage());
      }
    }
  }

  private void create(String name, int capacity) throws IOException
  {
    // Create empty item index
    DateTime current = TimeDateUtil.getCurrentDateTime();
    ItemIndex[] empty = { new ItemIndex((short)0, (short)0, "EMPTY", "EMPTY", "EMPTY", "EMPTY", TimeDateUtil.TIMECODE_ZERO, 0, current, current) };
    
    // Create the file
    ItemIndexFile.create(name, 4096, ConstRbx1600DcmRelease.getRelease(), ItemIndexFile.GZIP_COMPRESSION);
    
    // Open it up
    ItemIndexFile itemIndexFile = new ItemIndexFile(name);
    for (int i = 1; i <= capacity; i++)
    {
      // Update list
      empty[0].setList((short)i);
      
      // Write empty block
      itemIndexFile.write(empty);
    }
  }
}