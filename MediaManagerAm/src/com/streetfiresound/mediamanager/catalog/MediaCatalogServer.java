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
 * $Id: MediaCatalogServer.java,v 1.7 2005/03/21 22:26:03 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviMsgListenerNotFoundException;

import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogServerHelper;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogSkeleton;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogVendorEventManagerClient;
import com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogException;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MediaCatalogServer implements MediaCatalogSkeleton, DeviceMonitorEventListener, MediaCatalogEventListener
{
  private ApplicationModule parent;
  private MediaCatalog catalog = null;
  private DeviceMonitor monitor = null;
  private MediaCatalogServerHelper serverHelper = null;
  private MediaCatalogVendorEventManagerClient eventClient = null;

  public MediaCatalogServer(ApplicationModule parent) throws HaviException
  {
    // Check the parameter
    if (parent == null)
    {
      // Very bad
      throw new IllegalArgumentException("ApplicationModule is null");
    }

    try
    {
      // Create event manager client
      eventClient = new MediaCatalogVendorEventManagerClient(parent.getSoftwareElement(), ConstStreetFireVendorInformation.VENDOR_ID);
      
      // Create and bind server helper
      serverHelper = new MediaCatalogServerHelper(parent.getSoftwareElement(), this);
      serverHelper.setMultiThread(true);
      parent.getSoftwareElement().addHaviListener(serverHelper);
      
      // Create bind to media catalog
      catalog = new MediaCatalog(parent.getHuid());
      catalog.addListener(this);
      
      // Create bind to device monitor
      monitor = new DeviceMonitor(parent, new SimpleDeviceAdaptorFactory());
      monitor.addListener(this);
      
      // Get and add to catalog current adaptors
      DeviceAdaptor[] adaptors = monitor.getDeviceAdaptors();
      for (int i = 0; i < adaptors.length; i++)
      {
        // Add to catalog
        catalog.addAdaptor((DeviceAdaptor)adaptors[i]);
      }
    }
    catch (HaviMsgListenerExistsException e)
    {
      // Translate to service exception
      throw new ServiceException(e.toString());
    }
    catch (HaviException e)
    {
      // Translate to service exception
      throw new ServiceException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void close()
  {
    try
    {
      // Remove and close the server helper
      parent.getSoftwareElement().removeHaviListener(serverHelper);
      serverHelper.close();
      serverHelper = null;
      
      // Unbind from the monitor and close it
      monitor.removeListener(this);
      monitor.close();
      monitor = null;
      
      // Unbind from the catalog
      catalog.removeListener(this);
      catalog = null;
      
      // Release the event client
      eventClient = null;
    }
    catch (HaviMsgListenerNotFoundException e)
    {
      // Translate to service exception
      throw new ServiceException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.rmi.MediaCatalogSkeleton#getMetaData(com.streetfiresound.mediamanager.catalog.types.MLID, int)
   */
  public MediaMetaData[] getMetaData(MLID mediaLocationId) throws HaviMediaCatalogException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "getMetaData", "with " + mediaLocationId);

    // Forward to catalog
    return catalog.getMetaData(mediaLocationId);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.rmi.MediaCatalogSkeleton#getMetaData(com.streetfiresound.mediamanager.catalog.types.MLID, int)
   */
  public MediaMetaData[] getMultipleMetaData(MLID[] mediaLocationIdList) throws HaviMediaCatalogException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "getMultipleMetaData", "with " + mediaLocationIdList.length + " items");

    // Forward to catalog
    return catalog.getMultipleMetaData(mediaLocationIdList);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.rmi.MediaCatalogSkeleton#putMetaData(com.streetfiresound.mediamanager.catalog.types.MLID, com.streetfiresound.mediamanager.catalog.types.MediaMetaData[], int)
   */
  public void putMetaData(MediaMetaData[] mediaMetaData) throws HaviMediaCatalogException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "putMetaData", "with " + mediaMetaData.length + " items");

    // Forward to catalog
    catalog.putMetaData(mediaMetaData);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.rmi.MediaCatalogSkeleton#getCategorySummary(int)
   */
  public CategorySummary[] getCategorySummary(int type) throws HaviMediaCatalogException
  {
    //LoggerSingleton.logDebugCoarse(this.getClass(), "getCategorySummary", "with type " + type);

    CategorySummary[] result = catalog.getCategorySummary(type); 
    
    //LoggerSingleton.logDebugCoarse(this.getClass(), "getCategorySummary", "returning " + result.length + " items");

    // Forward
    return result;
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.rmi.MediaCatalogSkeleton#getMediaSummary(int, java.lang.String)
   */
  public MLID[] getMediaSummary(int type, String value) throws HaviMediaCatalogException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "getMediaSummary", "with type " + type + " and value '" + value + "'");
    
    MLID[] result = catalog.getMediaSummary(type, value);
    
    LoggerSingleton.logDebugCoarse(this.getClass(), "getMediaSummary", "returning " + result.length + " items");

    // Forward
    return result;
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.rmi.MediaCatalogSkeleton#searchMetaData(java.lang.String)
   */
  public MediaMetaData[] searchMetaData(String contains) throws HaviMediaCatalogException
  {
    // Forward
    return catalog.searchMetaData(contains);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceMonitorEventListener#goneDevice(com.streetfiresound.mediamanager.catalog.DeviceAdaptor)
   */
  public void goneDevice(DeviceAdaptor adaptor)
  {
    // forward to the catalog
    catalog.removeAdaptor((DeviceAdaptor)adaptor);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceMonitorEventListener#newDevice(com.streetfiresound.mediamanager.catalog.DeviceAdaptor)
   */
  public void newDevice(DeviceAdaptor adaptor)
  {
    // Forward to the catalog
    catalog.addAdaptor((DeviceAdaptor)adaptor);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.MediaCatalogEventListener#changedMediaCatalog(com.streetfiresound.mediamanager.catalog.types.MLID)
   */
  public void changedMediaCatalog(MLID hint)
  {
    try
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "changedMediaCatalog", hint.toString());
      
      // Fire event
      eventClient.fireChangedMediaCatalogSync(hint);
    }
    catch (HaviException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "changedMediaCatalog", e.toString());
    }
  }
}
