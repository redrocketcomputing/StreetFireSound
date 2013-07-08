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
 * $Id: SimpleDeviceAdaptorFactory.java,v 1.4 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import java.util.Iterator;
import java.util.Map;

import org.havi.dcm.types.HUID;
import org.havi.system.constants.ConstAttributeClassName;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.Attribute;
import org.havi.system.types.HaviException;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;

import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SimpleDeviceAdaptorFactory implements DeviceAdaptorFactory, MsgWatchOnNotificationListener
{
  private ApplicationModule parent;
  private Map deviceMap = new ListMap();
  
  /**
   * Construct a SimpleDeviceAdaptorFactory
   */
  public SimpleDeviceAdaptorFactory(ApplicationModule parent)
  {
    // Check application module
    if (parent == null)
    {
      // Badness
      throw new IllegalArgumentException("ApplicationModule is null");
    }
    
    // Save the parent
    this.parent = parent;
  }


  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptorFactory#close()
   */
  public synchronized void close()
  {
    // Loop through the map close all adaptors
    for (Iterator iterator = deviceMap.keySet().iterator(); iterator.hasNext();)
    {
      // Extract element
      DeviceAdaptor element = (DeviceAdaptor)iterator.next();
      
      // Close the DeviceAdaptor
      element.close();
    }
    
    // Flush the map
    deviceMap.clear();
    parent = null;
    deviceMap = null;
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptorFactory#flush()
   */
  public synchronized void flush()
  {
    // Loop through the map close all adaptors
    for (Iterator iterator = deviceMap.values().iterator(); iterator.hasNext();)
    {
      // Extract element
      DeviceAdaptor element = (DeviceAdaptor)iterator.next();
      
      // Close the DeviceAdaptor
      element.close();
    }
    
    // Flush the map
    deviceMap.clear();
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptorFactory#create(org.havi.dcm.types.HUID)
   */
  public synchronized DeviceAdaptor create(HUID huid)
  {
    // Check parameter
    if (huid == null)
    {
      // badness
      throw new IllegalArgumentException("HUID is null");
    }
    
    try
    {
      // See if the HUID is in the map
      DeviceAdaptor adaptor = (DeviceAdaptor)deviceMap.get(huid);
      if (adaptor != null)
      {
        // Found it
        return adaptor;
      }
      
      // Query registry for SEID matching the HUID
      SimpleAttributeTable simpleAttributeTable = new SimpleAttributeTable();
      simpleAttributeTable.setHuid(huid);
      RegistryClient registryClient = new RegistryClient(parent.getSoftwareElement());
      QueryResult result = registryClient.getElementSync(0, simpleAttributeTable.toQuery());
      if (result.getSeidList().length != 1)
      {
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "create", "confused by registry result: " + result.getSeidList().length);
        
        // Abandon
        return null;
      }
      
      // Check for known interfaces
      if (huid.getInterfaceId() == ConstRbx1600DcmInterfaceId.RBX1600_JUKEBOX_FCM)
      {
        // Create it, add it and return it
        adaptor = new AvDiscDeviceAdaptor(parent, result.getSeidList()[0]);
        parent.addMsgWatch(result.getSeidList()[0], this);
        deviceMap.put(huid, adaptor);
        return adaptor;
      }
      
      // Do this the hardway
      Attribute[] seidAttributes = registryClient.retrieveAttributesSync(0, result.getSeidList()[0]);
      SimpleAttributeTable seidAttributeTable = new SimpleAttributeTable(seidAttributes);
      if (!seidAttributeTable.isValid(ConstAttributeClassName.SE_TYPE) || seidAttributeTable.getSoftwareElementType() != ConstSoftwareElementType.AVDISC_FCM)
      {
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "create", "device is not an AVDISC");
        
        // Abandon
        return null;
      }
      
      // Create it, add it and return it
      adaptor = new AvDiscDeviceAdaptor(parent, result.getSeidList()[0]);
      parent.addMsgWatch(result.getSeidList()[0], this);
      deviceMap.put(huid, adaptor);
      return adaptor;
    }
    catch (HaviException e)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "create", e.toString());
      return null;
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
   */
  public synchronized void msgWatchOnNotification(SEID targetSeid)
  {
    // Lookup matching SEID
    for (Iterator iterator = deviceMap.entrySet().iterator(); iterator.hasNext();)
    {
      // Extract element
      Map.Entry element = (Map.Entry)iterator.next();
      HUID elementHuid = (HUID)element.getKey();
      DeviceAdaptor elementAdaptor = (DeviceAdaptor)element.getValue();
      
      // Check for match
      if (targetSeid.equals(elementAdaptor.getRemoteSeid()))
      {
        // Remove it
        iterator.remove();
        
        // All done
        return;
      }
    }
  }
}
