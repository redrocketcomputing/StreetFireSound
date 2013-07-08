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
 * $Id: AbstractDeviceAdaptor.java,v 1.2 2005/02/27 22:57:22 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
abstract class AbstractDeviceAdaptor implements DeviceAdaptor
{
  private List listenerList = new ArrayList();
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#close()
   */
  public void close()
  {
    synchronized(listenerList)
    {
      // Flush the listener list
      listenerList.clear();
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o)
  {
    // Check type
    if (o instanceof AbstractDeviceAdaptor)
    {
      // Cast it up
      AbstractDeviceAdaptor other = (AbstractDeviceAdaptor)o;
      
      // Check for equal HUIDs
      return getHuid().equals(other.getHuid());
    }
   
    // Wrong tyope
    return false;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // Return hash of HUID
    return getHuid().hashCode();
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#addListener(com.streetfiresound.mediamanager.catalog.DeviceAdaptorEventListener)
   */
  public void addListener(DeviceAdaptorEventListener listener)
  {
    synchronized(listenerList)
    {
      listenerList.add(listener);
    }
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptor#removeListener(com.streetfiresound.mediamanager.catalog.DeviceAdaptorEventListener)
   */
  public void removeListener(DeviceAdaptorEventListener listener)
  {
    synchronized(listenerList)
    {
      while (listenerList.remove(listener));
    }
  }
  
  /**
   * Dispatch a changed media item event
   * @param mediaLocationId The MLID of the changed item
   */
  protected void dispatchChangedMediaItem(MLID mediaLocationId)
  {
    synchronized(listenerList)
    {
      // Loop through the list dispatching the event
      for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
      {
        // Extract the listener
        DeviceAdaptorEventListener element = (DeviceAdaptorEventListener)iterator.next();

        // Dispatch
        element.changedMediaItem(this, mediaLocationId);
      }
    }
  }
  
  /**
   * Dispatch a retrieved media summaries event
   * @param summaries The MediaSummary array retrieved
   */
  protected void dispatchRetrievedMediaSummaries(MediaMetaData[] summaries)
  {
    synchronized(listenerList)
    {
      // Loop through the list dispatching the event
      for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
      {
        // Extract the listener
        DeviceAdaptorEventListener element = (DeviceAdaptorEventListener)iterator.next();

        // Dispatch
        element.retrievedMediaSummaries(this, summaries);
      }
    }
  }
}
