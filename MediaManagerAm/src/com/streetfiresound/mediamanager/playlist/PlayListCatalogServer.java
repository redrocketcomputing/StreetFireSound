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
 * $Id: PlayListCatalogServer.java,v 1.3 2005/03/20 00:20:27 stephen Exp $
 */
package com.streetfiresound.mediamanager.playlist;

import java.io.File;
import java.util.Map;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.QueryResult;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogServerHelper;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogSkeleton;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogVendorEventManagerClient;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogException;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayList;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayListMetaData;
import com.streetfiresound.mediamanager.server.MediaManager;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PlayListCatalogServer implements PlayListCatalogSkeleton, PlayListCatalogEventListener
{
  private final static String DEFAULT_CATALOG_PATH = File.separator + "opt" + File.separator + "streetfire";
  
  private ApplicationModule parent;
  private SoftwareElement softwareElement;
  private PlayListCatalogServerHelper serverHelper;
  private PlayListCatalog catalog;
  private boolean localOnly;
  private GUID localGuid;
  private GUID targetGuid;
  
  /**
   * Construct an new media catalog application module
   * @param instanceName The service manage instance name for this application module
   * @param targetId The HAVI TargetId for the application module
   * @param n1Uniqueness True is the TargetId is uniquie and presistent
   * @param interfaceId The HAVI InterfaceId for this application module, comes from the jar manifest 
   * @param vendorId The HAVI VendorId for this application module, come from the JAR manifest
   * @throws HaviException The is a problem is found creating the application module
   */
  public PlayListCatalogServer(ApplicationModule parent) throws HaviException
  {
    // The parent
    if (parent == null)
    {
      throw new IllegalArgumentException("ApplicationModule is null");
    }
    
    // Save the parent
    this.parent = parent;
    this.softwareElement = parent.getSoftwareElement();

    // Get Local GUID
    Gadp gadp = (Gadp)ServiceManager.getInstance().get(Gadp.class);
    localGuid = gadp.getLocalGuid();
    
    // Check configuration for local guid only
    localOnly = parent.getConfiguration().getBooleanProperty("local.only", false);
    targetGuid = GuidUtil.fromString(parent.getConfiguration().getProperty("target.guid", "ff:ff:ff:ff:ff:ff:ff:ff"));

    // Create and bind server helper
    serverHelper = new PlayListCatalogServerHelper(softwareElement, this);
    serverHelper.setMultiThread(true);
    softwareElement.addHaviListener(serverHelper);
    
    // Create and bind to playlist catalog
    String path = parent.getConfiguration().getProperty("path", DEFAULT_CATALOG_PATH);
    catalog = new PlayListCatalog(path, parent.getHuid());
    catalog.addListener(this);
  }
  
  /**
   * Release all resources
   */
  public void close()
  {
    // Unbind from software element
    softwareElement.removeHaviListener(serverHelper);
    
    // Release components
    catalog.removeListener(this);
    catalog = null;
    parent = null;
    softwareElement = null;
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogSkeleton#getPlayListSummaries()
   */
  public PlayListMetaData[] getMetaData(MLID mediaLocationId) throws HaviPlayListCatalogException
  {
    // Forward
    return catalog.getMetaData(mediaLocationId);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#createPlayList(com.streetfiresound.mediamanager.playlist.types.PlayListItem[])
   */
  public MLID createPlayList(PlayList playList) throws HaviPlayListCatalogException
  {
    // Forward
    return catalog.createPlayList(playList);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#getPlayList(short)
   */
  public PlayList getPlayList(MLID mediaLocationId) throws HaviPlayListCatalogException
  {
    // Forward
    return catalog.getPlayList(mediaLocationId);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#removePlayList(short)
   */
  public void removePlayList(MLID mediaLocationId) throws HaviPlayListCatalogException
  {
    // Forward
    catalog.removePlayList(mediaLocationId);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#updatePlayList(short, com.streetfiresound.mediamanager.playlist.types.PlayListItem[])
   */
  public void updatePlayList(MLID mediaLocationId, PlayList playList) throws HaviPlayListCatalogException
  {
    // Forward
    catalog.updatePlayList(mediaLocationId, playList);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.PlayListCatalogEventListener#playListCatalogChanged(int)
   */
  public void playListCatalogChanged(MLID hint)
  {
    try
    {
      // Create event manager client
      PlayListCatalogVendorEventManagerClient eventClient = new PlayListCatalogVendorEventManagerClient(parent.getSoftwareElement(), ConstStreetFireVendorInformation.VENDOR_ID);
      
      // Fire the event
      eventClient.firePlayListCatalogChangedSync(hint);
    }
    catch (HaviException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "playListCatalogChanged", e.toString());
    }
  }
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.playlist.rmi.PlayListCatalogSkeleton#getIrCodeMap()
   */
  public MLID[] getIrCodeMap() throws HaviPlayListCatalogException
  {
    // Forward
    return catalog.getIrCodeMap();
  }
}
