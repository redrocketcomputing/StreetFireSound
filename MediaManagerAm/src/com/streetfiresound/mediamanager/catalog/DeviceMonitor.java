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
 * $Id: DeviceMonitor.java,v 1.1 2005/02/27 22:57:22 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.MatchFoundMessageBackHelper;
import org.havi.system.registry.rmi.MatchFoundMessageBackListener;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.Attribute;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.Query;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.ListSet;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class DeviceMonitor implements MatchFoundMessageBackListener, MsgWatchOnNotificationListener
{
  private final static OperationCode MATCH_OPCODE = new OperationCode((short)0xff00, (byte)0xac);
  
  private List listenerList = new ArrayList();
  private ApplicationModule parent;
  private SoftwareElement softwareElement;
  private MatchFoundMessageBackHelper matchFoundHelper;
  private Map seidMap = new ListMap();
  private RegistryClient registryClient;
  private Set queryIdSet = new ListSet();
  private DeviceAdaptorFactory factory;
  private boolean localOnly;
  private GUID localGuid;
  private GUID targetGuid;
  
  public DeviceMonitor(ApplicationModule parent, DeviceAdaptorFactory factory) throws HaviException
  {
    // Check parameter
    if (parent == null || factory == null)
    {
      // Can't write code
      throw new IllegalArgumentException("ApplicationModule or DeviceAdaptorFactory is null");
    }
    
    // Save the software element
    this.parent = parent;
    this.factory = factory;
    this.softwareElement = parent.getSoftwareElement();
    
    // Get Local GUID
    Gadp gadp = (Gadp)ServiceManager.getInstance().get(Gadp.class);
    localGuid = gadp.getLocalGuid();
    
    // Check configuration for local guid only
    localOnly = parent.getConfiguration().getBooleanProperty("local.only", false);
    targetGuid = GuidUtil.fromString(parent.getConfiguration().getProperty("target.guid", "ff:ff:ff:ff:ff:ff:ff:ff"));
    
    // Create and attach a match found helper
    matchFoundHelper = new MatchFoundMessageBackHelper(softwareElement, MATCH_OPCODE, this);
    softwareElement.addHaviListener(matchFoundHelper, softwareElement.getSystemSeid(ConstSoftwareElementType.REGISTRY));
    
    // Create registry client
    registryClient = new RegistryClient(softwareElement);
    
    // Create device query
    Query[] queries = factory.getQueries();
    
    // Setup queries
    for (int i = 0; i < queries.length; i++)
    {
      // Register query
      int queryId = registryClient.subscribeSync(0, MATCH_OPCODE, queries[i]);
      
      // Add to set
      queryIdSet.add(new Integer(queryId));
      
      // Perform initial query and add to table
      QueryResult result = registryClient.getElementSync(0, queries[i]);
      addAdaptors(result.getSeidList());
    }
  }
  
  /**
   * Release all resource and unbind from the software element.
   */
  public void close()
  {
    try
    {
      synchronized(listenerList)
      {
        // Flush the listener list
        listenerList.clear();
      }
      
      // Unbind the helpers
      softwareElement.removeHaviListener(matchFoundHelper);
      
      // Unsubscribe to the query
      for (Iterator iterator = queryIdSet.iterator(); iterator.hasNext();)
      {
        // Extract query id
        Integer element = (Integer)iterator.next();
        
        // Unsubscibe
        registryClient.unsubscribeSync(0, element.intValue());
      }
      
      synchronized (seidMap)
      {
        for (Iterator iterator = seidMap.keySet().iterator(); iterator.hasNext();)
        {
          // Extract the SEID key
          SEID element = (SEID)iterator.next();
          
          // Turn off watch
          parent.addMsgWatch(element, this);
        }
        
        // Flush the map
        seidMap.clear();
      }
      
      // Clear the software piece
      softwareElement = null;
      listenerList = null;
      seidMap = null;
      registryClient = null;
      matchFoundHelper = null;
    }
    catch (HaviMsgListenerNotFoundException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
    catch (HaviException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
  }
  
  /**
   * Add a DeviceMonitorEventListener to the DeviceMonitor 
   * @param listener The listener to add
   */
  public void addListener(DeviceMonitorEventListener listener)
  {
    synchronized(listenerList)
    {
      // Add listener to list
      listenerList.add(listener);
    }
  }
  
  /**
   * Remove a DeviceMonitorEventListener from the DeviceMonitor
   * @param listener The listener to remove
   */
  public void removeListener(DeviceMonitorEventListener listener)
  {
    synchronized(listenerList)
    {
      // Remove all matching listeners
      while (listenerList.remove(listener));
    }
  }
  
  /**
   * Return the current set of DeviceAdaptors
   * @return The current set of DeviceAdaptors
   */
  public DeviceAdaptor[] getDeviceAdaptors()
  {
    synchronized(seidMap)
    {
      return (DeviceAdaptor[])seidMap.values().toArray(new DeviceAdaptor[seidMap.size()]);
    }
  }
  
  
  /* (non-Javadoc)
   * @see org.havi.system.registry.rmi.MatchFoundMessageBackListener#matchFound(int, org.havi.system.types.SEID[])
   */
  public void matchFound(int queryId, SEID[] seidList) throws HaviRegistryException
  {
    // Verify a match query id
    if (!queryIdSet.contains(new Integer(queryId)))
    {
      // Drop it
      return;
    }
    
    // Add adaptors
    addAdaptors(seidList);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "msgWatchOnNotification", targetSeid.toString());
    synchronized(seidMap)
    {
      // Find the SEID in the map
      DeviceAdaptor adaptor = (DeviceAdaptor)seidMap.remove(targetSeid);
      if (adaptor != null)
      {
        // Dispatch
        dispatchGoneDeviceAdaptor(adaptor);
        
        // Close the adaptor
        adaptor.close();
      }
    }
  }
  
  /**
   * Invoke listeners for the newDevice event
   * @param adaptor The new DeviceAdaptor found
   */
  private void dispatchNewDeviceAdaptor(DeviceAdaptor adaptor)
  {
    synchronized(listenerList)
    {
      for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
      {
        // Extract the listener
        DeviceMonitorEventListener element = (DeviceMonitorEventListener)iterator.next();
        
        // Dispatch
        element.newDevice(adaptor);
      }
    }
  }
  
  /**
   * Invoke listeners for the goneDevice event
   * @param adaptor The missing DeviceAdaptor
   */
  private void dispatchGoneDeviceAdaptor(DeviceAdaptor adaptor)
  {
    synchronized(listenerList)
    {
      for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
      {
        // Extract the listener
        DeviceMonitorEventListener element = (DeviceMonitorEventListener)iterator.next();
        
        // Dispatch
        element.goneDevice(adaptor);
      }
    }
  }
  
  /**
   * Add a array of SEID to the map of known devices, watch the SEID and create
   * DeviceAdaptor for the SEID using the SimpleDeviceAdaptorFactory
   * @param seids The array of SEID to add
   */
  private void addAdaptors(SEID[] seids)
  {
    synchronized(seidMap)
    {
      // Loop the the SEIDs
      for (int i = 0; i < seids.length; i++)
      {
        // Check to see if the adaptor is already in the map
        if (!seidMap.containsKey(seids[i]))
        {
          try
          {
            // Check for local only
            if ((!localOnly || localGuid.equals(seids[i].getGuid())) && (targetGuid.equals(GUID.BROADCAST) || targetGuid.equals(seids[i].getGuid())))
            {
              // Retrieve attributes for the seid
              Attribute[] attributes = registryClient.retrieveAttributesSync(0, seids[i]);
              
              // Create the adaptor
              DeviceAdaptor adaptor = factory.create(parent, seids[i], attributes);
              if (adaptor == null)
              {
                // Log error
                LoggerSingleton.logFatal(this.getClass(), "addAdaptor", "could not create adaptor for " + seids[i]);
                
                // Drop
                return;
              }
              
              // Add watch
              parent.addMsgWatch(seids[i], this);
              
              // Add it to the map
              seidMap.put(seids[i], adaptor);
              
              // Dispatch event
              dispatchNewDeviceAdaptor(adaptor);
            }
          }
          catch (HaviException e)
          {
            // Log the error
            LoggerSingleton.logError(this.getClass(), "addAdaptors", e.toString());
          }
        }
      }
    }
  }
}
