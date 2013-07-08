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
 * $Id: SlinkMonitor.java,v 1.1 2005/02/22 03:47:24 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.slink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.hardware.SlinkChannelController;
import com.redrocketcomputing.util.ListSet;

/**
 * @author stephen
 *
 */
class SlinkMonitor extends AbstractTask
{
  private EventDispatch eventDispatcher;
  private TaskPool taskPool;
  private volatile Set activeDevices = new ListSet();
  private List strategies = new ArrayList();
  private SlinkChannelController[] channels = new SlinkChannelController[SlinkChannelController.NUMBER_OF_CHANNELS];
  private volatile Task probeTask = null;
  private boolean monitorState = false;

  /**
   * Construct a SLINK channel monitor
   */
  public SlinkMonitor() throws SlinkIOException
  {
    try
    {
      // Get the event dispatcher service
      eventDispatcher = (EventDispatch) ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Throw exception
        throw new IllegalStateException("can not find event dispatch service");
      }

      // Find the task pool service
      taskPool = (TaskPool) ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Throw exception
        throw new IllegalStateException("can not find task pool service");
      }

      // Create the channel device drivers
      for (int i = 0; i < channels.length; i++)
      {
        channels[i] = new SlinkChannelController(i);
      }

      // Change state to open
      monitorState = true;
    }
    catch (IOException e)
    {
      // Translate
      throw new SlinkIOException(e.toString());
    }
  }

  /**
   * Close the monitor and release all resources
   */
  public void close()
  {
    // Mark as close
    monitorState = false;

    // Wait for the probe to complete
    while (probeTask != null)
    {
      Thread.yield();
    }

    // Close all the devices
    for (Iterator iterator = activeDevices.iterator(); iterator.hasNext();)
    {
      // Extract the device
      SlinkDevice element = (SlinkDevice) iterator.next();

      // Close the devices
      element.close();
    }

    // Close all channels
    for (int i = 0; i < channels.length; i++)
    {
      channels[i].close();
    }
  }

  /**
   * Add a new probe strategy the monitor.  This method may block if a probe is current running
   * @param strategy The strategy to add
   */
  public void addStrategy(ProbeStrategy strategy)
  {
    synchronized (strategies)
    {
      // Add to the strategies list
      strategies.add(strategy);
    }
  }

  /**
   * Remove an existing strategy from the monitor.  This method may block if a probe is current running.
   * @param strategy The strategy to remove.
   */
  public void removeStrategy(ProbeStrategy strategy)
  {
    synchronized (strategies)
    {
      while (!strategies.remove(strategy));
    }
  }

  /**
   * Lookup a SLINK device using the specified device ID
   * @param deviceId The device ID to lookup
   * @return SlinkDevice The device or null if not found.
   */
  public SlinkDevice getDevice(int deviceId)
  {
    // Get a copy of the devices
    Set activeDevices = this.activeDevices;

    // Loop through the set looking for a match device id
    for (Iterator iterator = activeDevices.iterator(); iterator.hasNext();)
    {
      // Extract the device
      SlinkDevice element = (SlinkDevice) iterator.next();

      // Check the device id
      if (element.getDeviceId() == deviceId)
      {
        return element;
      }
    }

    // Not found
    return null;
  }

  /**
   * Return an array of device IDs for the active devices
   * @return int[] The array of active devices
   */
  public int[] getDeviceIds()
  {
    // Save local copy of the active devices
    Set activeDevices = this.activeDevices;

    // Return the device ID array
    return toDeviceIdArray(activeDevices);
  }

  /**
   * Launch a new SLINK channel probe
   * @throws IOException Thrown if a problem launching the probe task is detected
   */
  public void probe() throws SlinkProbeAbortedException
  {
    try
    {
      // Create the tasl
      probeTask = this;
      taskPool.execute(probeTask);
    }
    catch (TaskAbortedException e)
    {
      throw new SlinkProbeAbortedException(e.toString());
    }
  }

  /**
   * Returns the state of the probe task
   * @return boolean True if the probe is running, false otherwise
   */
  public boolean isProbeRunning()
  {
    return probeTask != null;
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "SlinkMonitor::Probe";
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    Set activeDevices = new ListSet(this.activeDevices);
    Set newDevices = new ListSet();
    Set goneDevices = new ListSet();

    // Fire probe started event
    eventDispatcher.dispatch(new SlinkMonitorProbeStartedEvent());

    synchronized (strategies)
    {
      // Loop through all the strategies probe
      for (Iterator iterator = strategies.iterator(); iterator.hasNext();)
      {
        // Extract stratey
        ProbeStrategy element = (ProbeStrategy) iterator.next();

        // Execute the probe
        element.probe(channels, activeDevices);

        // Update the new and gone devices set
        newDevices.addAll(element.getNewDevices());
        goneDevices.addAll(element.getGoneDevices());
      }
    }

    // Remove gone devices
    for (Iterator iterator = goneDevices.iterator(); iterator.hasNext();)
    {
      // Extract the gone device
      SlinkDevice element = (SlinkDevice) iterator.next();

      // Remove from the active device set
      activeDevices.remove(element);

      // Close the device
      element.close();
    }

    // Add the new devices to the active devices
    activeDevices.addAll(newDevices);

    // Fire the probe complete event
    eventDispatcher.dispatch(new SlinkMonitorProbeCompletedEvent(toDeviceIdArray(newDevices), toDeviceIdArray(goneDevices), toDeviceIdArray(activeDevices)));

    // All done
    this.activeDevices = activeDevices;
    probeTask = null;
  }

  /**
   * Extract an array of device ID from a set of SLINK devices
   * @param deviceSet The SLINK device set
   * @return int[] The array of device IDs
   */
  private int[] toDeviceIdArray(Set deviceSet)
  {
    // Allocate the array
    int[] deviceIds = new int[deviceSet.size()];
    int position = 0;

    // Loop through the set and extract the device ids
    for (Iterator iterator = deviceSet.iterator(); iterator.hasNext();)
    {
      // Extract the device
      SlinkDevice element = (SlinkDevice) iterator.next();

      // Add to the array
      deviceIds[position++] = element.getDeviceId();
    }

    // All done
    return deviceIds;
  }
}
