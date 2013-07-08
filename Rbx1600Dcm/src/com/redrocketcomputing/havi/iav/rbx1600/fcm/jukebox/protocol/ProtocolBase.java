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
 * $Id: ProtocolBase.java,v 1.2 2005/03/16 04:25:03 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol;

import gnu.trove.TIntObjectHashMap;

import java.util.ArrayList;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.SonyJukeboxSlinkDevice;
import com.redrocketcomputing.havi.system.cmm.slink.SlinkIOException;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

class ProtocolBase extends AbstractTask
{
  private final static int DEFAULT_READ_TIMEOUT = 2500;
  private final static int DEFAULT_SEND_GATE_DELAY = 100;
  private final static byte[] PLAYER_STATE_COMMAND = {(byte)0x90, (byte)0x0f};

  private static TIntObjectHashMap dispatchMap = new TIntObjectHashMap(50);

  private SonyJukeboxSlinkDevice device;
  private boolean opened = false;
  private ArrayList listenerList = new ArrayList();
  private volatile Object[] listenerArray = new Object[0];
  private volatile long lastSendTimeStamp = System.currentTimeMillis();
  private ComponentConfiguration configuration;
  private int readTimeout;
  private int sendGateDelay;

  public ProtocolBase(SonyJukeboxSlinkDevice device) throws ProtocolException
  {
    // Check parameters
    if (device == null)
    {
      // Bad
      throw new IllegalArgumentException("device is null");
    }

    // Save the device
    this.device = device;

    // Get the configuration properties
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration("slink.protocol");
    readTimeout = configuration.getIntProperty("read.timeout", DEFAULT_READ_TIMEOUT);
    sendGateDelay = configuration.getIntProperty("send.gate.delay", DEFAULT_SEND_GATE_DELAY);

    // All ready
    opened = true;

    // Log some debug infro
    LoggerSingleton.logDebugCoarse(this.getClass(), "ProtocolBase", "started on device " + Integer.toHexString(device.getDeviceId()) + " with read timeout " +  readTimeout + " and send gate delay of " + sendGateDelay);
  }
  
  protected void start()
  {
    try
    {
      // Get the task manager
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Very bad
        throw new IllegalStateException("can not find task pool service");
      }
  
      // Launch reader task
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      // Log error
      LoggerSingleton.logFatal(this.getClass(), "start", e.toString());
    }
  }

  public void close()
  {
    // Mark as closed
    opened = false;

    // Flush the dispatch map
    dispatchMap.clear();
  }
  
  public int getDeviceId()
  {
    return device.getDeviceId();
  }

  public final synchronized void send(byte[] data, int offset, int length) throws ProtocolException
  {
    try
    {
    	// Check send gate
    	while (System.currentTimeMillis() - lastSendTimeStamp < sendGateDelay)
    	{
        try
        {
          Thread.sleep(System.currentTimeMillis() - lastSendTimeStamp);
        }
        catch (InterruptedException e)
        {
        	// Clear interrupted state
        	Thread.currentThread().interrupted();
        }
    	}
    	lastSendTimeStamp = System.currentTimeMillis();

      // Forward to the device
      device.write(data, offset, length);
    }
    catch (SlinkIOException e)
    {
      // Translate
      throw new ProtocolException(e.toString());
    }
  }

  public void addEventListener(ProtocolEventListener listener)
  {
    synchronized (listenerList)
    {
      // Add the listener
      listenerList.add(listener);

      // Build new listener array
      listenerArray = listenerList.toArray();
    }
  }

  public void removeEventListener(ProtocolEventListener listener)
  {
    synchronized (listenerList)
    {
      // Remove all matching listeners
      while (listenerList.remove(listener));

      // Build new listener array
      listenerArray = listenerList.toArray();
    }
  }

  protected void addDispatchHandler(int type, ProtocolDispatchHandler handler)
  {
    dispatchMap.put(type, handler);
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "ProtocolBase::0x" + Integer.toHexString(device.getDeviceId());
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    byte[] data = new byte[33];
    int size;

    try
    {
      // Set timeout for the device
      device.setTimeout(readTimeout);

      // Loop until close
      while (opened)
      {
        // Read some data
        size = device.read(data, 0, data.length);
        if (size == -1)
        {
          try
          {
            // Timeout, send a device probe
            send(PLAYER_STATE_COMMAND, 0, PLAYER_STATE_COMMAND.length);
          }
          catch (ProtocolException e)
          {
          	// Log warning
          	LoggerSingleton.logWarning(this.getClass(), "run", e.toString());
          }
        }
        else
        {
          // Make copy of listener array
          Object[] listeners = listenerArray;

          // Get the dispatch handler
          ProtocolDispatchHandler handler = (ProtocolDispatchHandler)dispatchMap.get(data[1] & 0xff);
          if (handler != null)
          {
            // Dispatch
            for (int i = 0; i < listeners.length; i++)
            {
              handler.dispatch((ProtocolEventListener)listeners[i], data, 0, size);
            }
          }
          else
          {
          	// Log a warning
          	LoggerSingleton.logWarning(this.getClass(), "run", "unknown message type 0x" + Integer.toHexString(data[1]&0xff));
          }
        }
      }

      LoggerSingleton.logDebugCoarse(this.getClass(), "run", "read task on device 0x" + Integer.toHexString(device.getDeviceId()) + " exiting");
    }
    catch (SlinkIOException e)
    {
    	// Log error and exit
    	LoggerSingleton.logError(this.getClass(), "run", e.toString());
    }
  }
}
