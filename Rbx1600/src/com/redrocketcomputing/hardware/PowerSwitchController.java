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
 * $Id: PowerSwitchController.java,v 1.2 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.hardware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;

/**
 * @author stephen
 *
 */
public class PowerSwitchController extends AbstractTask
{
  private static String EVENT_DEVICE_PATH = "/dev/power/event";
  private static String STATE_DEVICE_PATH = "/dev/power/state";
  private static int CANCEL = -1;

  private DeviceDriver eventDevice;
  private DeviceDriver stateDevice;
  private List listenerList = new ArrayList();
  private volatile boolean done = false;

  /**
   * Constructor for PowerSwitchController.
   */
  public PowerSwitchController() throws IOException
  {
    try
    {
      // Open the state device
      stateDevice = new DeviceDriver(STATE_DEVICE_PATH);

      // Open the event device
      eventDevice = new DeviceDriver(EVENT_DEVICE_PATH);

      // Get the task pool
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Badness
        throw new IllegalStateException("TaskPool service not found");
      }

      // Launch task
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new IOException(e.toString());
    }
  }

  /**
   * Close the power switch controller and release all resources
   */
  public void close()
  {
    try
    {
      // Mark as complete
      done = true;

      // Write cancel to device
      eventDevice.writeInt(CANCEL);
    }
    catch (IOException e)
    {
      // Ignore
    }
  }

  /**
   * Get the current power switch state.
   * @return boolean True is power is on, false otherwise
   * @throws IOException Thrown if the is a problem talking with the device driver
   */
  public boolean getPowerState() throws IOException
  {
    return stateDevice.readInt() == 1;
  }

  /**
   * Add a event listener to the power switch controller.  The listener is invoke whenever the power
   * state changes
   * @param listener The listener to notify of power state changes
   */
  public void addListener(PowerSwitchEventListener listener)
  {
    // Check the parameters
    if (listener == null)
    {
      throw new IllegalArgumentException("listener is null");
    }

    synchronized (listenerList)
    {
      // Add the listener
      listenerList.add(listener);
    }
  }

  /**
   * Remove an event listener from the power switch controller.
   * @param listener The listener to remove
   */
  public void removeListener(PowerSwitchEventListener listener)
  {
    // Check the parameters
    if (listener == null)
    {
      throw new IllegalArgumentException("listener is null");
    }

    synchronized (listenerList)
    {
      // Remove the listener
      while (listenerList.remove(listener));
    }
  }

  /**
   * Thread body to monitor the power switch device driver for power state changes.  This method
   * dispatches power state event listeners
   */
  public void run()
  {
    // Loop until interrupted, done or error
    while (!done)
    {
      try
      {
        // Read from the device
        int event = eventDevice.readInt();

        // Check for cancel
        if (event == CANCEL)
        {
          break;
        }

        // Dispatch events
        synchronized (listenerList)
        {
          for (int i = 0; i < listenerList.size(); i++)
          {
            ((PowerSwitchEventListener) listenerList.get(i)).pushedEvent();
          }
        }
      }
      catch (IOException e)
      {
        // Mark as done
        done = true;
      }
    }

    // Close the event devices
    eventDevice.close();
    stateDevice.close();
  }
  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "PowerSwitchController::EventDispatcher";
  }

}
