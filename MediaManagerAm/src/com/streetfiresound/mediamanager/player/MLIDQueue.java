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
 * $Id: MLIDQueue.java,v 1.6 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import com.redrocketcomputing.util.concurrent.NullReadWriteLock;
import com.redrocketcomputing.util.concurrent.ReadWriteLock;
import com.redrocketcomputing.util.concurrent.Sync;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstMoveDirection;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerBadQueueIndexException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerBadVersionException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerInvalidParameterException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerUnidentifiedFailureException;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class MLIDQueue extends Observable
{
  public final static Integer PLAYITEM_QUEUE_CHANGED = new Integer(0);
  
  private List queue = new ArrayList();
  private ReadWriteLock lock = new NullReadWriteLock(); 
  //private ReadWriteLock lock = new ReentrantWriterPreferenceReadWriteLock();
  private Sync readLock = lock.readLock();
  private Sync writeLock = lock.writeLock();
  private int version = 0;
  
  /**
   * Construct an empty MLIDQueue
   */
  public MLIDQueue()
  {
  }
  
  /**
   * Clear the queue
   * @throws HaviMediaPlayerException
   */
  public void clear() throws HaviMediaPlayerException
  {
    try
    {
      // Get write lock
      writeLock.acquire();
      
      // Clear the array
      queue.clear();
      
      // Mark as changed
      version++;
      setChanged();

      // Release
      writeLock.release();

      // Notify observers
      notifyObservers(PLAYITEM_QUEUE_CHANGED);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }
  
  /**
   * Return the current number of elements in the queue
   * @return The number of elements in the queue
   */
  public final int size() throws HaviMediaPlayerException
  {
    try
    {
      // Lock for read
      readLock.acquire();
      
      // Get size
      int size = queue.size();
      
      // Unlock
      readLock.release();
      
      // Return size
      return queue.size();
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }
  
  /**
   * Get the PlayItem at the specified index
   * @param index The index for the requested PlayItem
   * @return The PlayItem at the index
   * @throws HaviMediaPlayerException If there is a range problem or a thread interruption
   */
  public MLID getAt(int index) throws HaviMediaPlayerException
  {
    try
    {
      // Lock for read
      readLock.acquire();
      
      // Check range
      if (index < 0 || index >= queue.size())
      {
        // Unlock
        readLock.release();

        // Bad range we are confused
        throw new HaviMediaPlayerBadQueueIndexException("bad index: " + index);
      }
      
      // Unlock
      readLock.release();

      // Return entry
      return (MLID)queue.get(index);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Add an array of PlayItems to the end of the queue
   * @param items The array of PlayItems to add
   * @return The new queue version number
   * @throws HaviMediaPlayerException Thrown if a thread interruption is detected
   */
  public int add(MLID[] items) throws HaviMediaPlayerException
  {
    try
    {
      // Get write lock
      writeLock.acquire();
      
      // Append array to the list
      for (int i = 0; i < items.length; i++)
      {
        // Append element
        queue.add(items[i]);
        
        // Mark as changed
        setChanged();
      }

      // Update the version if changed
      if (hasChanged())
      {
        version++;
      }

      // Release
      writeLock.release();

      // Notify observers
      notifyObservers(PLAYITEM_QUEUE_CHANGED);
      
      // Return version
      LoggerSingleton.logDebugCoarse(this.getClass(), "add", "returning version: " + version);
      return version;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Remote the specified range to PlayItem from the queue.  The queue version numbers
   * must match in order for the removal to succeed.
   * @param verison The queue verision number
   * @param start The start index of the range to remove
   * @param size The size of the range to remove
   * @return The new queue version number or -1 on failure
   * @throws HaviMediaPlayerException Thrown if a range problem or thread interruption is detected
   */
  public int remove(int verison, int start, int size) throws HaviMediaPlayerException
  {
    try
    {
      // Get write lock
      writeLock.acquire();
      
      // Match versions
      if (verison != this.version)
      {
        // Release
        writeLock.release();
        
        // Bad version
        throw new HaviMediaPlayerBadVersionException("version mismatch");
      }
      
      // Check start range
      if (start < 0 || start > queue.size())
      {
        // Release
        writeLock.release();

        // Bad range
        throw new HaviMediaPlayerInvalidParameterException("bad start range: " + start);
      }
      
      // Check sublist range
      int end = start + size;
      if (end > queue.size() || start > end)
      {
        // Release
        writeLock.release();

        // Bad range
        throw new HaviMediaPlayerInvalidParameterException("bad size: " + start + "->" + size);
      }
      
      // Remove the element
      queue.subList(start, end).clear();
      
      // Update the version
      this.version++;
      
      // Release
      writeLock.release();

      // Mark as changed
      setChanged();

      // Notify observers
      notifyObservers(PLAYITEM_QUEUE_CHANGED);
      
      // Return true
      return this.version;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Move the specified range of PlayItem up or down 1 space in the queue. The queue 
   * version numbers must match in order for the move to succeed.
   * @param verison The queue verision number
   * @param direction The direction of the move 0 for up and 1 for down
   * @param start The start index of the range to remove
   * @param size The size of the range to remove
   * @return The new queue version number or -1 on failure
   * @throws HaviMediaPlayerException Thrown if a range problem or thread interruption is detected
   */
  public int move(int version, int direction, int start, int size) throws HaviMediaPlayerException
  {
    try
    {
      // Get write lock
      writeLock.acquire();
      
      // Match versions
      if (version != this.version)
      {
        // Release
        writeLock.release();

        // Bad version
        throw new HaviMediaPlayerBadVersionException("version mismatch");
      }
      
      // Range check the direction
      if (direction != ConstMoveDirection.UP && direction != ConstMoveDirection.DOWN)
      {
        // Bad range
        throw new HaviMediaPlayerInvalidParameterException("bad direction: " + direction);
      }
      
      // Check start range
      if (start < 0 || start > queue.size())
      {
        // Release
        writeLock.release();

        // Bad range
        throw new HaviMediaPlayerInvalidParameterException("bad start range: " + start);
      }
      
      // Check sublist range
      int end = start + size;
      if (end > queue.size() || start > end)
      {
        // Release
        writeLock.release();

        // Bad range
        throw new HaviMediaPlayerInvalidParameterException("bad size: " + start + "->" + size);
      }
      
      // Clone the subrange
      List rangeList = new ArrayList(queue.subList(start, end));
      
      // Remove the elements
      queue.subList(start, end).clear();
      
      // Calculate insert point
      int insertIndex;
      if (direction == ConstMoveDirection.UP)
      {
        // Limit move to start of list
        insertIndex = Math.max(0, start - 1);
      }
      else
      {
        // Limit move to end of list
        insertIndex = Math.max(queue.size(), start + 1); 
      }
      
      // Inset the range list
      queue.addAll(insertIndex, rangeList);
      
      // Update the version
      this.version++;
      
      // Release
      writeLock.release();

      // Mark as changed
      setChanged();

      // Notify observers
      notifyObservers(PLAYITEM_QUEUE_CHANGED);
      
      // Return true
      return this.version;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }

  
  /**
   * Return a PlayQueue object built from this queue
   * @return The matching PlayQueue
   * @throws HaviMediaPlayerException Thrown if a thread interruption is detected
   */
  public PlayQueue getQueue() throws HaviMediaPlayerException
  {
    try
    {
      // Lock for read
      readLock.acquire();
      
      // Create array
      MLID[] items = (MLID[])queue.toArray(new MLID[queue.size()]);
      
      // Unlock
      readLock.release();

      // Return queue
      return new PlayQueue(version, items);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Randomally re-order the queue
   * @throws HaviMediaPlayerException Thrown if a thread interruption is detected
   */
  public void shuffle() throws HaviMediaPlayerException
  {
    try
    {
      // Get write lock
      writeLock.acquire();
      
      // Shuffle the queue
      Collections.shuffle(queue);
      
      // Update the version
      version++;

      // Release
      writeLock.release();

      // Mark as changed
      setChanged();

      // Notify observers
      notifyObservers(PLAYITEM_QUEUE_CHANGED);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }
  
  /**
   * Return the current queue version 
   * @return The version of the current queue
   * @throws HaviMediaPlayerException Thrown if a thread interruption is detected
   */
  public int getVersion() throws HaviMediaPlayerException
  {
    try
    {
      // Lock for read
      readLock.acquire();
      
      LoggerSingleton.logDebugCoarse(this.getClass(), "getVersion", "return version: " + version);
      
      // Return the current version
      return version;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
    finally 
    {
      // Alway unlock
      readLock.release();
    }
  }
}
