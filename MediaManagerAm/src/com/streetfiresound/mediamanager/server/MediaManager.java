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
 * $Id: MediaManager.java,v 1.4 2005/03/16 04:23:43 stephen Exp $
 */
package com.streetfiresound.mediamanager.server;

import java.io.PrintStream;

import org.havi.applicationmodule.types.HaviApplicationModuleException;
import org.havi.applicationmodule.types.HaviApplicationModuleNotImplementedException;
import org.havi.applicationmodule.types.HavletCodeUnitProfile;
import org.havi.dcm.types.DeviceIcon;
import org.havi.dcm.types.TargetId;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviVersionException;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.catalog.MediaCatalogServer;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerRelease;
import com.streetfiresound.mediamanager.player.MediaPlayerServer;
import com.streetfiresound.mediamanager.playlist.PlayListCatalogServer;
import com.streetfiresound.mediamanager.power.PowerStateMonitor;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MediaManager extends ApplicationModule
{ 
  private MediaCatalogServer mediaCatalogServer = null;
  private PlayListCatalogServer playlistCatalogServer = null;
  private MediaPlayerServer mediaPlayerServer = null;
  private PowerStateMonitor powerStateMonitor = null;
  
  /**
   * Construct a MediaManager server
   * @param instanceName The ApplicationModule instance name
   * @param targetId The system assigned TargetId
   * @param n1Uniqueness True is the TargetId N1 field is unique.
   * @throws HaviException Thrown if an problem is detected launching the MediaManager
   */
  public MediaManager(String instanceName, TargetId targetId, boolean n1Uniqueness) throws HaviException
  {
    super(instanceName, targetId, n1Uniqueness, ConstMediaManagerInterfaceId.MEDIA_MANAGER, ConstStreetFireVendorInformation.VENDOR_ID);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Make sure we are not running
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("service is not idle");
    }
    
    try
    {
      // Forward to super class
      super.start();
      
      // Create media catalogs and players
      mediaCatalogServer = new MediaCatalogServer(this);
      playlistCatalogServer = new PlayListCatalogServer(this);
      mediaPlayerServer = new MediaPlayerServer(this);
      powerStateMonitor = new PowerStateMonitor(this);
      
      // Mark debug
      // getSoftwareElement().setDebug(true);
      
      // Register
      register();
      
      // Log start
      LoggerSingleton.logInfo(this.getClass(), "start", "start with version " + ConstMediaManagerRelease.getRelease() + " on " + getSoftwareElement().getSeid());
    }
    catch (HaviException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Bad
      throw new ServiceException("service is not running");
    }
    
    try
    {
      // Unregister
      unregister();
      
      // Close the servers
      powerStateMonitor.close();
      mediaPlayerServer.close();
      mediaCatalogServer.close();
      playlistCatalogServer.close();
      powerStateMonitor = null;
      mediaPlayerServer = null;
      mediaCatalogServer = null;
      playlistCatalogServer = null;
      
      // Forward to super class
      super.terminate();

      // Log terminate
      LoggerSingleton.logInfo(this.getClass(), "terminate", "stopped");
    }
    catch (HaviException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#info(java.io.PrintStream, java.lang.String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    super.info(printStream, arguments);
  }

  /* (non-Javadoc)
   * @see org.havi.applicationmodule.rmi.ApplicationModuleSkeleton#getHavletCodeUnit(int, int)
   */
  public byte[] getHavletCodeUnit(int firstByte, int lastByte) throws HaviApplicationModuleException
  {
    throw new HaviApplicationModuleNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.applicationmodule.rmi.ApplicationModuleSkeleton#getHavletCodeUnitProfile()
   */
  public HavletCodeUnitProfile getHavletCodeUnitProfile() throws HaviApplicationModuleException
  {
    throw new HaviApplicationModuleNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.applicationmodule.rmi.ApplicationModuleSkeleton#getIcon()
   */
  public DeviceIcon getIcon() throws HaviApplicationModuleException
  {
    throw new HaviApplicationModuleNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    return ConstMediaManagerRelease.getRelease();
  }
}
