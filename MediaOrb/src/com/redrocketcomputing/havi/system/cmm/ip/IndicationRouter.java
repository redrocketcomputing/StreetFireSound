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
 * $Id: IndicationRouter.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviCmmIpConfigurationException;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviCmmIpNotFoundException;
import org.havi.system.types.HaviCmmIpUnknownGuidException;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpEngine;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpGoneDevicesEventListener;

/**
 * Class to handle indication event registrations, and dispatches.  The class monitors the GARP for
 * gone devices and flush any listeners.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class IndicationRouter implements IndicationEventListener, GarpGoneDevicesEventListener
{
  private GarpEngine garp;
  private EventDispatch eventDispatcher;
  private Map indicationMap = new HashMap();
  private IndicationKey queryKey = new IndicationKey();

  /**
   * Constructor for IndicationRouter.
   */
  public IndicationRouter(GarpEngine garp) throws HaviCmmIpConfigurationException
  {
    // Check the parameters
    if (garp == null)
    {
      throw new IllegalArgumentException("GARP is null");
    }

    // Save the parameters
    this.garp = garp;

    // Get the event dispatcher
    eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
    if (eventDispatcher == null)
    {
      // This is very bad
      throw new HaviCmmIpConfigurationException("can not find event dispatch service");
    }

    // Register for indication events
    eventDispatcher.addListener(this);
  }

  /**
   * Add a listener interested in the specified indications. The internal table is checked to see someone is
   * already registered with the handler and the result of method indicates there is a conflict
   * @param listener The listener to add
   * @param guid The GUID to listen for indications
   * @param address The address (service handler id) to listen for indications
   * @return boolean True is the is a conflict in enrollment, false otherwise
   * @throws HaviCmmIpException
   */
  public synchronized boolean enrollIndication(IndicationEventListener listener, GUID guid, int address) throws HaviCmmIpException
  {
    // Check to make sure the GUID is valid
    if (!guid.equals(GUID.BROADCAST) && !garp.isActive(guid))
    {
      // Unknow guid
      throw new HaviCmmIpUnknownGuidException(guid.toString());
    }

    // Create a key
    IndicationKey indicationKey = new IndicationKey(guid, address);

    // Lookup the entry
    List listenerList = (List)indicationMap.get(indicationKey);
    if (listenerList == null)
    {
      // Create empty listener list
      listenerList = new ArrayList();

      // Create and add a empty list to the indication map
      indicationMap.put(indicationKey, listenerList);
    }

    // Setup the query key for a boardcast check
    queryKey.setGuid(GUID.BROADCAST);
    queryKey.setAddress(address);

    // Check for conflict
    boolean conflict = listenerList.size() > 0 || indicationMap.containsKey(queryKey);

    // Add this listener to the list
    listenerList.add(listener);

    // All done
    return conflict;
  }

  /**
   * Remove a indication listener from the internal table.
   * @param listener The listener to remove
   * @param guid The GUID the listener is bound to
   * @param address The address the listener is bound to
   * @throws HaviCmmIpException Thrown if the listener can not be found.
   */
  public synchronized void dropIndication(IndicationEventListener listener, GUID guid, int address) throws HaviCmmIpException
  {
    // Setup the query key
    queryKey.setGuid(guid);
    queryKey.setAddress(address);

    // Lookup the listener list
    List listenerList = (List)indicationMap.get(queryKey);
    if (listenerList == null || !listenerList.contains(listener))
    {
      // Not listener found
      throw new HaviCmmIpNotFoundException(queryKey.toString());
    }

    // Remove the listener
    listenerList.remove(listener);
  }

  /**
   * Release all indication handle resources
   */
  public synchronized void close()
  {
    // Unregister
    eventDispatcher.removeListener(this);
    indicationMap = null;
    queryKey = null;
    garp = null;
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.IndicationEventListener#indicationEvent(GUID, int, int, Object)
   */
  public synchronized void indicationEvent(GUID guid, int address, int indication, Object data)
  {
    // Set up broadcast query key
    queryKey.setGuid(GUID.BROADCAST);
    queryKey.setAddress(address);

    // Get the boardcast list
    List listenerList = (List)indicationMap.get(queryKey);
    if (listenerList != null)
    {
      // Dispatch
      dispatchListeners(listenerList, guid, address, indication, data);
    }

    // Setup specific query
    queryKey.setGuid(guid);
    queryKey.setAddress(address);

    // Get specific list
    listenerList = (List)indicationMap.get(queryKey);
    if (listenerList != null)
    {
      // Dispatch
      dispatchListeners(listenerList, guid, address, indication, data);
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpGoneDevicesEventListener#goneDevicesEvent(GUID[])
   */
  public synchronized void goneDevicesEvent(GUID[] guids)
  {
    // Extract a key set array
    IndicationKey[] keys = (IndicationKey[])indicationMap.keySet().toArray(new IndicationKey[indicationMap.size()]);

    // Loop throught gone guid
    for (int g = 0; g < guids.length; g++)
    {
      // Loop through the keys
      for (int k = 0; k < keys.length; k++)
      {
        // Check for match
        if (guids[g].equals(keys[k].getGuid()))
        {
          // Hooray, we have a match
          indicationMap.remove(keys[k]);
        }
      }
    }
  }

  /**
   * Dispatch an indication event to the specified list of listeners
   * @param listenerList The list of listener to invoke
   * @param guid The indication event GUID
   * @param address The indication event address
   * @param indication The indication type
   * @param data The opaque indication data
   */
  private void dispatchListeners(List listenerList, GUID guid, int address, int indication, Object data)
  {
    // Loop through the listener dispatching
    for (Iterator iterator = listenerList.iterator(); iterator.hasNext();)
    {
      // Extract the listener
      IndicationEventListener element = (IndicationEventListener) iterator.next();

      // Dispatch
      element.indicationEvent(guid, address, indication, data);
    }
  }

  /**
   * Dump the current indication table to the specified print stream.
   * @param printStream The print stream to use
   * @param arguments Additional arguments
   */
  public synchronized void dump(PrintStream printStream, String[] arguments)
  {
    // Print header
    printStream.println("Indication Table");

    // Loop through the map
    for (Iterator interator = indicationMap.entrySet().iterator(); interator.hasNext();)
    {
      // Extract the entry
      Map.Entry element = (Map.Entry) interator.next();
      IndicationKey key = (IndicationKey)element.getKey();
      List listenerList = (List)element.getValue();

      // Print key
      printStream.println("  " + key.getGuid() + '@' + key.getAddress());

      // Print the listener list
      for (Iterator listIterator = listenerList.iterator(); listIterator.hasNext();)
      {
        // Extract the listener
        IndicationEventListener listener = (IndicationEventListener) listIterator.next();

        // Display the listener
        printStream.println("    " + listener.toString());
      }
    }
  }
}
