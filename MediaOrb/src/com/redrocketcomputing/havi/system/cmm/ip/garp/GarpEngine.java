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
 * $Id: GarpEngine.java,v 1.2 2005/03/17 02:27:32 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.havi.system.types.GUID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.Util;
import com.redrocketcomputing.util.concurrent.Gate;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * State machine for the GARP protocol.  This is the GOF State Pattern Context
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
public class GarpEngine extends AbstractTask
{
  public final static int DEFAULT_PACKET_SIZE = 128;

  public final static int IDLE = 0;
  public final static int RESET = 1;
  public final static int READY = 2;

  private GarpState[] garpStates = new GarpState[3];
  private GUID[] activeDevices;
  private GUID[] nonactiveDevices = new GUID[0];
  private EventDispatch eventDispatcher;
  private Set newGuids = new HashSet();
  private Set goneGuids = new HashSet();
  private GUID localGuid = null;
  private GarpEntry localGarpEntry = null;
  private byte[] localIpAddress = null;
  private boolean localActive = false;
  private GarpMessageSocket socket;
  private Map guidMap = Collections.synchronizedMap(new HashMap());
  private int currentTimeout = 0;
  private int readyTimeout;
  private int heartbeatTimeout;
  private int networkResetCounter = 0;
  private GarpState currentState;
  private Gate readyGate = new Gate();

  /**
   * Constructor for GarpEngine.
   */
  public GarpEngine(GUID localGuid, String multicastAddress, int multicastPort, int readyTimeout, int heartbeatTimeout) throws GarpException
  {
    try
    {
      // Save the parameters
      this.localGuid = localGuid;
      this.readyTimeout = readyTimeout;
      this.heartbeatTimeout = heartbeatTimeout;

      // Build state array
      garpStates[IDLE] = new GarpIdleState(this);
      garpStates[RESET] = new GarpResetState(this);
      garpStates[READY] = new GarpReadyState(this);

      // Initialize the state
      currentState = garpStates[IDLE];

      // Extract the raw guid array
      byte[] rawGuid = localGuid.getValue();

      // Extract local address from guid
      byte[] localAddress = new byte[4];
      System.arraycopy(rawGuid, 0, localAddress, 0, 4);

      // Extract local port from local guid
      int localPort = ((rawGuid[4] & 0xff) << 24) + ((rawGuid[5] & 0xff) << 16) + ((rawGuid[6] &0xff) << 8) + (rawGuid[7] & 0xff);

      // Create local GarpEntry
      localGarpEntry = new GarpEntry(localAddress, localPort, true);
      guidMap.put(localGuid, localGarpEntry);

      // Create the active devices array
      activeDevices = new GUID[1];
      activeDevices[0] = localGuid;

      // Try to get the event dispatch service
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        throw new GarpException("can't find EventDispatchService");
      }

      // Create the socket
      socket = new GarpMessageSocket(multicastAddress, multicastPort, DEFAULT_PACKET_SIZE);

      // Try to get the task pool service
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        throw new GarpException("can't find TaskPoolService");
      }

      // Use the first service to run GARP reader task
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      throw new GarpException("TaskAbortedException: " + e.getMessage());
    }
  }

  /**
   * Terminate the protocol engine
   */
  public synchronized void close()
  {
    try
    {
      // Get a gone guid for the local device
      GarpGoneDeviceMessage goneDevice = new GarpGoneDeviceMessage(localGuid);

      // Send the message out
      socket.send(goneDevice);

      // Change state to IDLE
      changeState(IDLE);
    }
    catch (GarpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
    finally
    {
      // Close the socket
      socket.close();
    }
  }

  /**
   * Force a newwork reset
   */
  public void forceNetworkReset()
  {
    try
    {
      // Send the network reset message
      sendGarpMessage(new GarpResetNetworkMessage(localGuid));
    }
    catch (GarpException e)
    {
      LoggerSingleton.logWarning(this.getClass(), "forceNetworkReset", e.toString());
    }
  }

  public final boolean isActive(GUID guid)
  {
    // Look up the entry
    GarpEntry entry = resolve(guid);
    if (entry != null)
    {
      return entry.isActive();
    }

    // Not, must be false
    return false;
  }

  /**
   * Lookup the matching garp entry using the specified GUID
   * @param guid The guid to lookup
   * @return GarpEntry the
   */
  public final GarpEntry resolve(GUID guid)
  {
    //LoggerSingleton.logDebugCoarse(this.getClass(), "resolve", Util.getStackTrace(new Exception()));
    
    // Check for local resolve, we never block for local resolve
    if (localGuid.equals(guid))
    {
      return localGarpEntry;
    }
    
    try
    {
      // Pass through ready gate
      readyGate.acquire();
    }
    catch (InterruptedException e)
    {
      // Umm,
      return null;
    }
    
    // Try to get the entry
    return (GarpEntry)guidMap.get(guid);
  }

  /**
   * Return an array of all known GUIDs
   * @return GUID[] The array of known GUIDS
   * @throws GarpException Thrown if the GARP state is not RESETTING or READY
   */
  public final GUID[] getAllGuids() throws GarpException
  {
    // Return the array
    return (GUID[])guidMap.keySet().toArray(new GUID[guidMap.size()]);
  }

  /**
   * Returns the activeDevices.
   * @return GUID[] The array of active devices. This will never be null
   */
  public final GUID[] getActiveDevices()
  {
    return activeDevices;
  }

  /**
   * Returns the nonactiveDevices.
   * @return GUID[] The array of nonactive devices. This will never be null
   */
  public final GUID[] getNonactiveDevices()
  {
    return nonactiveDevices;
  }

  /**
   * Returns the networkResetCounter.
   * @return int The current network reset counter
   */
  public final int getNetworkResetCounter()
  {
    return networkResetCounter;
  }

  /**
   * Check the current state of the protocol
   * @return boolean True is the protocol is ready, false otherwise
   */
  public final boolean isReady()
  {
    return currentState == garpStates[READY];
  }

  /**
   * Change the current protocol state.  This should only be called by the protocol states.
   * @param newState The new state to enter
   */
  final void changeState(int newState)
  {
    // Leave current state if current is available
    if (currentState != null)
    {
      currentState.leaveState();
    }

    // Save the new state
    currentState = garpStates[newState];

    // Enter the new state
    currentState.enterState();
    
    // Change ready gate status
    if (currentState == garpStates[READY])
    {
      // Open gate
      readyGate.release();
    }
    else
    {
      // Close the gate
      readyGate.reset();
    }

    // Log some debug
    LoggerSingleton.logDebugCoarse(this.getClass(), "changeState", currentState.toString());
  }

  /**
   * Returns the goneGuids.
   * @return Set
   */
  Set getGoneGuids()
  {
    return goneGuids;
  }

  /**
   * Add or Update the GUID map with the specified GUID and entry
   * @param guid The GUID to update or add
   * @param entry The new entry
   */
  void update(GUID guid, GarpEntry entry)
  {
    // Add to map again
    guidMap.put(guid, entry);
  }

  /**
   * Enable the ready time out on the message reader
   * @param enable True to enable the ready timeout, false to disable it.
   */
  void enableReadyTimeout(boolean enable)
  {
    // Check the false
    if (enable)
    {
      // Set the current timeout to the configured time
      currentTimeout = readyTimeout;
    }
    else
    {
      // Wait for a message or heartbeat timeout
      currentTimeout = heartbeatTimeout;
    }
  }

  /**
   * Returns the guidMap.
   * @return ConcurrentHashMap
   */
  Map getGuidMap()
  {
    return guidMap;
  }

  /**
   * Returns the newGuids.
   * @return Set
   */
  Set getNewGuids()
  {
    return newGuids;
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "GARP";
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Loop until an error or the socket is null
      while (socket != null)
      {
        // Read from socket
        GarpMessage message = socket.receive(currentTimeout);

        // Dispatch the message
        message.dispatch(currentState);
      }
    }
    catch (GarpIOException e)
    {
      // Change state to IDLE
      changeState(IDLE);
    }
    catch (GarpException e)
    {
      // Log an error
      LoggerSingleton.logError(this.getClass(), "run", "exiting due to " + e.toString());
    }
  }

  /**
   * Returns the localGuid has retreive from the GADP engine
   * @return GUID The local guid
   */
  GUID getLocalGuid()
  {
    return localGuid;
  }

  /**
   * Return a string representing the current GARP state
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    // Forward to the current state
    return currentState.toString();
  }

  /**
   * Send a message using the internal GarpMessageSocket
   * @param message The message to send
   * @throws GarpException Thrown by the socket if an error is detected
   */
  void sendGarpMessage(GarpMessage message) throws GarpException
  {
    // Forward to the socket
    socket.send(message);
  }

  /**
   * Create and send a network reset event using the event dispatch service
   */
  void fireNetworkResetEvent()
  {
    // Forward the event dispatcher
    eventDispatcher.dispatch(new GarpNetworkResetEvent());
  }

  /**
   * Create and send a new devices event using the event dispatch service
   * @param guids The new device on the network
   */
  void fireNewDevicesEvent(GUID[] guids)
  {
    // Forward to the event dispatcher
    eventDispatcher.dispatch(new GarpNewDevicesEvent(guids));
  }

  /**
   * Create and send a gone devices event using the event dispatch service
   * @param guids The gone device on the network
   */
  void fireGoneDevicesEvent(GUID[] guids)
  {
    // Forward to the event dispatcher
    eventDispatcher.dispatch(new GarpGoneDevicesEvent(guids));
  }

  /**
   * Create and send a network event using the event dispatch service
   * @param newDevices The new devices on the network
   * @param goneDevices The gone devices on the network
   * @param activeDevices The active devices on the network
   * @param nonactiveDevices The non active devices on the network
   */
  void fireEventNetworkReadyEvent(GUID[] newDevices, GUID[] goneDevices, GUID[] activeDevices, GUID[] nonactiveDevices)
  {
    // Forward to the event dispatcher
    eventDispatcher.dispatch(new GarpNetworkReadyEvent(newDevices, goneDevices, activeDevices, nonactiveDevices));
  }

  /**
   * Set the active and nonactive device arrays
   * @param activeDevices The active devices on the network
   * @param nonActiveDevices The non active devices on the network
   */
  void setDeviceArrays(GUID[] activeDevices, GUID[] nonActiveDevices)
  {
    // Save the device arrays
    this.activeDevices = activeDevices;
    this.nonactiveDevices = nonActiveDevices;
  }

  /**
   * Increment the reset counter
   */
  void incrementResetCounter()
  {
    networkResetCounter++;
  }
}
