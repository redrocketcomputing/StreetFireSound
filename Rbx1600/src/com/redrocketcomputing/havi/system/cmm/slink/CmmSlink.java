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
 * $Id: CmmSlink.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.slink;

import java.io.IOException;
import java.io.PrintStream;

import org.havi.system.constants.ConstSoftwareElementType;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.service.SystemService;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public class CmmSlink extends SystemService
{
  private SlinkMonitor monitor = null;
  private EventDispatch eventDispatcher = null;
  private int numberOfProbes = 0;

  /**
   * Construct a CmmSlink
   */
  public CmmSlink(String instanceName) throws SlinkIOException
  {
    // Construct super class
    super(instanceName, ConstSoftwareElementType.COMMUNICATION_MEDIA_MANAGER_SLINK);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Check to see if we are already running
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException("CmmSlinkService is already running");
    }

    try
    {
      // Start super class
      super.start();
      
      // Get the event dispatcher service
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Throw exception
        throw new IllegalStateException("can not find event dispatch service");
      }

      // Create the monitor
      monitor = new SlinkMonitor();

      // Build any configured probe strategies
      String className = "unknown";
      String property = null;
      int i = 0;
      while ((property = getConfiguration().getProperty("probe.strategy." + i)) != null)
      {
        // Update property position
        i++;

        try
        {
          // Create the strategy object
          ProbeStrategy strategy = (ProbeStrategy)Class.forName(property).newInstance();

          // Add to the CmmSlink
          addStrategy(strategy);
        }
        catch (IllegalAccessException e)
        {
          LoggerSingleton.logWarning(this.getClass(), "start", e.toString() + ": can not create strategy: " + property);
        }
        catch (InstantiationException e)
        {
          LoggerSingleton.logWarning(this.getClass(), "start", e.toString() + ": can not create strategy: " + property);
        }
        catch (ClassNotFoundException e)
        {
          LoggerSingleton.logWarning(this.getClass(), "start", e.toString() + ": can not create strategy: " + property);
        }
      }

      // Change state
      setServiceState(Service.RUNNING);

      // Log start of service
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running");
    }
    catch (SlinkIOException e)
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
    // Check to see if we are idle
    if (getServiceState() != Service.RUNNING)
    {
      throw new ServiceException("CmmSlink is not running");
    }

    // Forward to super class
    super.terminate();

    // Close the monitor
    monitor.close();
    monitor = null;

    // Flush all listeners
    eventDispatcher.flushListeners(SlinkMonitorEventListener.class);
    eventDispatcher = null;
    
    // Change state to IDLE
    setServiceState(Service.IDLE);

    // Log termination of service
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#info(java.io.PrintStream, java.lang.String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Forward to super class
    super.info(printStream, arguments);
    
    // Make sure the service is running
    if (getServiceState() != Service.RUNNING)
    {
      printStream.println("service not running");
      return;
    }

    // Display the counters
    printStream.println("Probe: " + getNumberOfProbes());
    printStream.println("Probing: " + isProbeRunning());

    // Display device information
    int[] deviceIds = getDeviceIds();
    printStream.println("Device Count: " + deviceIds.length);

    // Dump the devices
    for (int i = 0; i < deviceIds.length; i++)
    {
      // Get the device
      SlinkDevice device = getDevice(deviceIds[i]);

      // Display some device information
      printStream.println(device.getDescription());
    }
  }

  /**
   * Add a SLINK monitor event listener
   * @param listener The listener to add
   */
  public void addEventListener(SlinkMonitorEventListener listener)
  {
    // Forward to event dispatched
    eventDispatcher.addListener(listener);
  }

  /**
   * Remove a SLINK monitor event listener
   * @param listener The listener to remove
   */
  public void removeEventListener(SlinkMonitorEventListener listener)
  {
    // Forward to event dispatcher
    eventDispatcher.removeListener(listener);
  }

  /**
   * Add a new probe strategy the monitor.  This method may block if a probe is current running
   * @param strategy The strategy to add
   */
  public void addStrategy(ProbeStrategy strategy)
  {
    // Forward
    monitor.addStrategy(strategy);
  }

  /**
   * Remove an existing strategy from the monitor.  This method may block if a probe is current running.
   * @param strategy The strategy to remove.
   */
  public void removeStrategy(ProbeStrategy strategy)
  {
    // Forward
    monitor.removeStrategy(strategy);
  }

  /**
   * Lookup a SLINK device using the specified device ID
   * @param deviceId The device ID to lookup
   * @return SlinkDevice The device or null if not found.
   */
  public SlinkDevice getDevice(int deviceId)
  {
    // Forward to the monitor
    return monitor.getDevice(deviceId);
  }

  /**
   * Launch a new SLINK channel probe
   * @throws IOException Thrown if a problem launching the probe task is detected
   */
  public void probe() throws SlinkProbeAbortedException
  {
    // Forward
    numberOfProbes++;
    monitor.probe();
  }

  /**
   * Returns the state of the probe task
   * @return boolean True if the probe is running, false otherwise
   */
  public boolean isProbeRunning()
  {
    // Forward
    return monitor.isProbeRunning();
  }

  /**
   * Return an array of device IDs for the active devices
   * @return int[] The array of active devices
   */
  public int[] getDeviceIds()
  {
    // Forward the monitor
    return monitor.getDeviceIds();
  }

  /**
   * Set the timeout for the device. A -1 disables the timeout.
   * @param timeout Time to wait for a message.
   */
  public void setTimeout(int deviceId, long timeout) throws SlinkIOException
  {
    // Lookup the device
    SlinkDevice device = monitor.getDevice(deviceId);
    if (device == null)
    {
      // Badness
      throw new SlinkIOException("device 0x" + Integer.toHexString(deviceId) + " not found");
    }

    // Set the device timeout
    device.setTimeout(timeout);
  }

  /**
   * Read a SlinkMessage from the device.  This method will block until message is available.
   * @param deviceId The device to read from
   * @param data The buffer to place the data into
   * @param offset The offset within the buffer
   * @param length The max lenght of data to read
   * @return int The actucal number of byte read, -1 if a timeout is detected
   * @throws SlinkIOException Thrown is the underlying SlinkChannel detects an error.
   */
  public int read(int deviceId, byte[] data, int offset, int length) throws SlinkIOException
  {
    // Lookup the device
    SlinkDevice device = monitor.getDevice(deviceId);
    if (device == null)
    {
      // Badness
      throw new SlinkIOException("device 0x" + Integer.toHexString(deviceId) + " not found");
    }

    // Ask device to read
    return device.read(data, offset, length);
  }

  /**
   * Write a SlinkMessage to the device.  This method will block until space is available on the device
   * @param deviceId The device to read from
   * @param data The buffer send out the SLINK channel
   * @param offset The offset within the buffer
   * @param length The length of data to write
   * @throws SlinkIOException Thrown is the underlying SlinkChannel detects an error.
   */
  public void write(int deviceId, byte[] data, int offset, int length) throws SlinkIOException

  {
    // Lookup the device
    SlinkDevice device = monitor.getDevice(deviceId);
    if (device == null)
    {
      // Badness
      throw new SlinkIOException("device 0x" + Integer.toHexString(deviceId) + " not found");
    }

    // Write the message
    device.write(data, offset, length);
  }

  /**
   * Returns the number of probes executed
   * @return int The number probes
   */
  public int getNumberOfProbes()
  {
    return numberOfProbes;
  }
}
