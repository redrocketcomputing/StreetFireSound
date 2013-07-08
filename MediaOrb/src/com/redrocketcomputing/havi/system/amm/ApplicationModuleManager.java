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
 * $Id: ApplicationModuleManager.java,v 1.2 2005/02/24 03:30:22 stephen Exp $
 */
package com.redrocketcomputing.havi.system.amm;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.havi.dcm.constants.ConstTargetType;
import org.havi.dcm.types.TargetId;
import org.havi.system.AmCodeUnitInterface;
import org.havi.system.applicationmodulemanager.rmi.ApplicationModuleManagerServerHelper;
import org.havi.system.applicationmodulemanager.rmi.ApplicationModuleManagerSkeleton;
import org.havi.system.applicationmodulemanager.rmi.ApplicationModuleManagerVendorEventManagerClient;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.types.ApplicationModuleGuestId;
import org.havi.system.types.ApplicationModuleProfile;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviApplicationModuleManagerBadLocationException;
import org.havi.system.types.HaviApplicationModuleManagerException;
import org.havi.system.types.HaviApplicationModuleManagerInvalidParameterException;
import org.havi.system.types.HaviApplicationModuleManagerUnidentifiedFailureException;
import org.havi.system.types.HaviEventManagerException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstMediaOrbRelease;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.service.SystemService;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ApplicationModuleManager extends SystemService implements ApplicationModuleManagerSkeleton, VersionSkeleton, UninstallListener
{
  private ApplicationModuleManagerServerHelper serverHelper;
  private VersionServerHelper versionServerHelper;
  private ApplicationModuleManagerVendorEventManagerClient eventClient;
  private Map installedCodeUnits = null;
  private GUID localGuid = null;
  private int nextN1;

  /**
   * 
   */
  public ApplicationModuleManager(String instanceName)
  {
    // Construct superclass
    super(instanceName, ConstSoftwareElementType.AM_MANAGER);
  }


  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public synchronized void start()
  {
    // Check for running
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("already started");
    }
    
    try
    {
      // First start the super class
      super.start();
      
      // Get the Local GUID from the GADP
      Gadp gadp = (Gadp)ServiceManager.getInstance().find(Gadp.class);
      if (gadp == null)
      {
        // Very bad
        throw new ServiceException("can not find GADP service");
      }
      localGuid = gadp.getLocalGuid();
      
      // Create the server helpers
      serverHelper = new ApplicationModuleManagerServerHelper(getSoftwareElement(), this);
      versionServerHelper = new VersionServerHelper(getSoftwareElement(), this);
      getSoftwareElement().addHaviListener(serverHelper);
      getSoftwareElement().addHaviListener(versionServerHelper);
      
      // Create event client
      eventClient = new ApplicationModuleManagerVendorEventManagerClient(getSoftwareElement(), ConstStreetFireVendorInformation.VENDOR_ID);
      
      // Create map and class loader
      installedCodeUnits = new ListMap();
      
      // Load any application in the configuration properties
      ComponentConfiguration configuration = getConfiguration();
      int i = 0;
      String property;
      while ((property = configuration.getProperty("load." + Integer.toString(i++))) != null)
      {
        try
        {
          // Forward to installer
          install(property);
        }
        catch (HaviApplicationModuleManagerException e)
        {
          // Log error and continure
          LoggerSingleton.logError(this.getClass(), "start", "error installing " + property + ": " + e.toString());
        }
      }
      
      // Mark as idle
      setServiceState(Service.RUNNING);

      // Log the start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running");
    }
    catch (HaviMsgListenerExistsException e)
    {
      // Translate
      throw new ServiceException(e.toString());
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
  public synchronized void terminate()
  {
    // Check for running
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("not started");
    }
    
    try
    {
      // Close the server helper
      getSoftwareElement().removeHaviListener(serverHelper);
      getSoftwareElement().removeHaviListener(versionServerHelper);
      serverHelper.close();
      versionServerHelper.close();
      serverHelper = null;
      versionServerHelper = null;
      
      // Get installed guest id and uninstall
      try
      {
        ApplicationModuleGuestId[] guests = getInstalledGuests();
        for (int i = 0; i < guests.length; i++)
        {
          // Uninstall
          uninstall(guests[i]);
        }
      }
      catch (HaviApplicationModuleManagerException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "terminate", e.toString());
      }
      
      // Release the client
      eventClient = null;
      
      // Last terminate the super class
      super.terminate();
      
      // Mark as terminated
      setServiceState(Service.IDLE);
      
      // Log the start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is idle");
    }
    catch (HaviMsgListenerNotFoundException e)
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
    // TODO Auto-generated method stub
    super.info(printStream, arguments);
  }

  /* (non-Javadoc)
   * @see org.havi.system.applicationmodulemanager.rmi.ApplicationModuleManagerSkeleton#install(java.lang.String)
   */
  public void install(String amUrl) throws HaviApplicationModuleManagerException
  {
    try
    {
      // Create URL
      URL url = new URL(amUrl);
      
      // Create the class loader
      ApplicationModuleCodeUnitClassLoader classLoader = new ApplicationModuleCodeUnitClassLoader(url);
      
      // Get the profile
      ApplicationModuleProfile profile = classLoader.getProfile();
      
      // Create the code unit
      AmCodeUnitInterface codeUnit = classLoader.createCodeUnit();
      
      // Create a new target Id
      TargetId targetId = new TargetId(ConstTargetType.AM, localGuid, nextN1++, (short)0xffff);
      
      // Create the guest id
      ApplicationModuleGuestId guestId = new ApplicationModuleGuestId(targetId, amUrl);
      
      // Create the code unit entry
      CodeUnitEntry entry = new CodeUnitEntry(guestId, codeUnit, classLoader, this);
      
      // Merge the code unit properties
      ConfigurationProperties.getInstance().merge(profile.getName(), classLoader.getConfiguration());
      
      // Install the code unit
      codeUnit.install(targetId, false, entry);
      
      // Add to the map
      installedCodeUnits.put(guestId, entry);
      
      // Post event
      eventClient.fireApplicationModuleInstalledSync(guestId);
    }
    catch (MalformedURLException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerBadLocationException(e.toString());
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerUnidentifiedFailureException(e.toString());
    }
    catch (HaviEventManagerException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.system.applicationmodulemanager.rmi.ApplicationModuleManagerSkeleton#uninstall(org.havi.system.types.ApplicationModuleGuestId)
   */
  public void uninstall(ApplicationModuleGuestId guestId) throws HaviApplicationModuleManagerException
  {
    // Check guest id
    CodeUnitEntry entry = (CodeUnitEntry)installedCodeUnits.get(guestId);
    if (entry == null)
    {
      // Bad
      throw new HaviApplicationModuleManagerInvalidParameterException("bad guest: " + guestId);
    }
    
    // Start uninstall
    entry.getCodeUnit().uninstall();
  }

  /* (non-Javadoc)
   * @see org.havi.system.applicationmodulemanager.rmi.ApplicationModuleManagerSkeleton#getInstalledGuests()
   */
  public ApplicationModuleGuestId[] getInstalledGuests() throws HaviApplicationModuleManagerException
  {
    return (ApplicationModuleGuestId[])installedCodeUnits.keySet().toArray(new ApplicationModuleGuestId[installedCodeUnits.size()]);
  }

  /* (non-Javadoc)
   * @see org.havi.system.applicationmodulemanager.rmi.ApplicationModuleManagerSkeleton#getInstalledGuestProfile(org.havi.system.types.ApplicationModuleGuestId)
   */
  public ApplicationModuleProfile getInstalledGuestProfile(ApplicationModuleGuestId guestId) throws HaviApplicationModuleManagerException
  {
    // Check guest id
    CodeUnitEntry entry = (CodeUnitEntry)installedCodeUnits.get(guestId);
    if (entry == null)
    {
      // Bad
      throw new HaviApplicationModuleManagerInvalidParameterException("bad guest: " + guestId);
    }
    
    // Return the profile
    return entry.getClassLoader().getProfile();
  }

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    return ConstMediaOrbRelease.getRelease();
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.amm.UninstallListener#uninstalled(org.havi.system.types.ApplicationModuleGuestId)
   */
  public void uninstalled(ApplicationModuleGuestId guestId)
  {
    try
    {
      // Remove from map
      installedCodeUnits.remove(guestId);
      
      // Fire event
      eventClient.fireApplicationModuleUninstalledSync(guestId);
    }
    catch (HaviException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "uninstalled", e.toString());
    }
  }
}
