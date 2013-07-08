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
 * $Id: ApplicationModule.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */
package com.redrocketcomputing.havi.system.am;

import java.io.PrintStream;

import org.havi.applicationmodule.rmi.ApplicationModuleServerHelper;
import org.havi.applicationmodule.rmi.ApplicationModuleSkeleton;
import org.havi.applicationmodule.types.HaviApplicationModuleException;
import org.havi.applicationmodule.types.HavletCodeUnitProfile;
import org.havi.dcm.constants.ConstFCAssigner;
import org.havi.dcm.types.DeviceIcon;
import org.havi.dcm.types.HUID;
import org.havi.dcm.types.TargetId;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.EventId;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.SEID;
import org.havi.system.types.VendorId;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;
import com.redrocketcomputing.havi.system.rmi.EventNotificationListener;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ApplicationModule extends AbstractService implements ApplicationModuleSkeleton, VersionSkeleton
{
  private SoftwareElement softwareElement = null;
  private HUID huid;
  private SimpleAttributeTable attributeTable;
  private ApplicationModuleServerHelper serverHelper = null;
  private VersionServerHelper versionServerHelper = null;
  private EventManagerNotificationServerHelper eventServer = null;
  private MsgWatchOnNotificationHelper watchHelper = null;
  private boolean isRegistered = false;
  
  public ApplicationModule(String instanceName, TargetId targetId, boolean n1Uniqueness, short interfaceId, VendorId vendorId) throws HaviException
  {
    // Construct super class
    super(instanceName);
    
    // Construct HUID
    huid = new HUID(targetId, interfaceId, vendorId, n1Uniqueness, ConstFCAssigner.NONE);
    
    // Bind the attribute table
    attributeTable = new SimpleAttributeTable();
    attributeTable.setHuid(huid);
    attributeTable.setTargetId(huid.getTargetId());
    attributeTable.setSoftwareElementType(ConstSoftwareElementType.APPLICATION_MODULE);
    attributeTable.setInterfaceId(interfaceId);
    attributeTable.setVendorId(vendorId);
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    try
    {
      // Create the software element
      softwareElement = new SoftwareElement();
      
      // Bind interface to the software element
      serverHelper = new ApplicationModuleServerHelper(softwareElement, this);
      versionServerHelper = new VersionServerHelper(softwareElement, this);
      softwareElement.addHaviListener(serverHelper);
      softwareElement.addHaviListener(versionServerHelper);
      eventServer = new EventManagerNotificationServerHelper(softwareElement);
      watchHelper = new MsgWatchOnNotificationHelper(softwareElement);

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
  public void terminate()
  {
    try
    {
      // Check to see if we are registered
      if (isRegistered)
      {
        try
        {
          // Unregister first
          unregister();
        }
        catch (HaviException e)
        {
          // Just log the error
          LoggerSingleton.logError(this.getClass(), "terminate", e.toString());
        }
      }
      
      // Close the server helper and software element
      eventServer.close();
      watchHelper.close();
      versionServerHelper.close();
      serverHelper.close();
      softwareElement.close();
    }
    catch (HaviMsgListenerNotFoundException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "terminate", e.toString());
    }
    catch (HaviMsgException e)
    {
      // Just log the error
      LoggerSingleton.logError(this.getClass(), "terminate", e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#info(java.io.PrintStream, java.lang.String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Dump the instance name and HUID
    printStream.println("InstanceName: " + getInstanceName());
    printStream.print("HUID: " + huid.toString());
  }

  /**
   * @return Returns the softwareElement.
   */
  public final SoftwareElement getSoftwareElement()
  {
    return softwareElement;
  }

  /**
   * Add SystemEvent subscriptions
   * @param eventId The SystemEventId of the event to subscribe to
   * @param listener The EventNotificationListener to receive the event
   * @throws HaviException The if a problem subscribing to the event is detected
   */
  public final void addEventSubscription(EventId eventId, EventNotificationListener listener) throws HaviException
  {
    // Forward
    eventServer.addEventSubscription(eventId, listener);
  }

  /**
   * Remove the event subscriptions
   * @param eventId The EventId to remove
   * @param listener The matching listener;
   */
  public final void removeEventSubscription(EventId eventId, EventNotificationListener listener)
  {
    // Forward
    eventServer.removeEventSubscription(eventId, listener);
  }

  /**
   * Add a listener for the specified event. The listener must be of the correct type for the event or a IllegalArgumentException
   * will be thrown
   * @param eventId The event ID to listener on
   * @param listener The event listener
   */
  public final void addMsgWatch(SEID targetSeid, MsgWatchOnNotificationListener listener) throws HaviException
  {
    // Forward
    watchHelper.addListenerEx(targetSeid, listener);
  }

  /**
   * Remove the listener for the specified event.
   * @param eventId The event ID to which the listener is bound
   * @param listener The listener to remove
   */
  public final void removeMsgWatch(SEID targetSeid, MsgWatchOnNotificationListener listener)
  {
    // Forward
    watchHelper.removeListenerEx(targetSeid, listener);
  }

  /* (non-Javadoc)
   * @see org.havi.applicationmodule.applicationmodule.rmi.ApplicationModuleSkeleton#getHuid()
   */
  public HUID getHuid() throws HaviApplicationModuleException
  {
    return huid;
  }

  /* (non-Javadoc)
   * @see org.havi.applicationmodule.applicationmodule.rmi.ApplicationModuleSkeleton#getHavletCodeUnit(int, int)
   */
  public abstract byte[] getHavletCodeUnit(int firstByte, int lastByte) throws HaviApplicationModuleException;
  
  /* (non-Javadoc)
   * @see org.havi.applicationmodule.applicationmodule.rmi.ApplicationModuleSkeleton#getHavletCodeUnitProfile()
   */
  public abstract HavletCodeUnitProfile getHavletCodeUnitProfile() throws HaviApplicationModuleException;
  
  /* (non-Javadoc)
   * @see org.havi.applicationmodule.applicationmodule.rmi.ApplicationModuleSkeleton#getIcon()
   */
  public abstract DeviceIcon getIcon() throws HaviApplicationModuleException;
  
  /**
   * Publish the application module by registering with the network
   * @throws HaviException
   */
  protected void register() throws HaviException
  {
    // Create registry client
    RegistryClient client = new RegistryClient(softwareElement);
    
    // Register
    client.registerElementSync(0, softwareElement.getSeid(), attributeTable.toAttributeArray());
    
    // Mark as registered
    isRegistered = true;
  }
  
  /**
   * Unpublish the application module by unregistering with the network 
   * @throws HaviException
   */
  protected void unregister() throws HaviException
  {
    // Create registry client
    RegistryClient client = new RegistryClient(softwareElement);
    
    // Unregister
    client.unregisterElement(softwareElement.getSeid());
  }
  
  /**
   * @return Returns the attributeTable.
   */
  protected SimpleAttributeTable getAttributeTable()
  {
    return attributeTable;
  }

}
