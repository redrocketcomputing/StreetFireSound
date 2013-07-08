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
 * $Id: DigitalAudioQSubcodeInputStream.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */
package com.redrocketcomputing.hardware;

import java.io.IOException;
import java.util.ArrayList;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DigitalAudioQSubcodeInputStream extends AbstractTask
{
  private final static String PATH = "/dev/dac/qsubcode/";
  private static int CANCEL_COMMAND = 0xffff0000;
  
  private int channel;
  private DeviceDriver device;
  private ArrayList listenerList = new ArrayList();
  private volatile DigitalAudioQSubcodeEventListener[] activeListeners = new DigitalAudioQSubcodeEventListener[0];
  private volatile Task running = null;

  /**
   * @param path
   * @throws IOException
   */
  public DigitalAudioQSubcodeInputStream(int channel) throws IOException
  { 
    // Range check the channel
    if (channel < 0 || channel >= 4)
    {
      // Bad
      throw new IllegalArgumentException("bad channel: " + channel);
    }
    
    try
    {
      // Save the channel
      this.channel = channel;
      
      // Open device
      device = new DeviceDriver(PATH + channel);
      
      // Start the task
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
      running = this;
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
  }
  
  /**
   * Close and release all resources
   */
  public void close()
  {
    try
    {
      running = null;
      device.writeInt(CANCEL_COMMAND);
    }
    catch (IOException e)
    {
      // Just log error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
    
    // Forward to super class
    device.close();
  }
  
  /**
   * Add a Q-Channel subcode listener
   * @param listener The listener to add
   */
  public void addListener(DigitalAudioQSubcodeEventListener listener)
  {
    synchronized(listenerList)
    {
      // Add the listener
      listenerList.add(listener);
      
      // Rebuild the array
      activeListeners = (DigitalAudioQSubcodeEventListener[])listenerList.toArray(new DigitalAudioQSubcodeEventListener[listenerList.size()]);
    }
  }
  
  /**
   * Remove a Q-Channel subcode listener
   * @param listener The listener to remove
   */
  public void removeListener(DigitalAudioQSubcodeEventListener listener)
  {
    synchronized(listenerList)
    {
      // Add the listener
      while (listenerList.remove(listener));
      
      // Rebuild the array
      activeListeners = (DigitalAudioQSubcodeEventListener[])listenerList.toArray(new DigitalAudioQSubcodeEventListener[listenerList.size()]);
    }
  }
  
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "DigitalAudioQsubCodeInputStream::" + channel;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    byte[] subcode = new byte[10];
    
    try
    {
      while (running != null)
      {
        // Read the subcode
        int count = device.read(subcode);
        if (count != 10)
        {
          LoggerSingleton.logDebugCoarse(this.getClass(), "run", "only read " + count);
        }
        
        // Dispatch
        DigitalAudioQSubcodeEventListener[] dispatchListeners = activeListeners;
        for (int i = 0; i < dispatchListeners.length; i++)
        {
          dispatchListeners[i].newQSubcode(subcode);
        }
      }
    }
    catch (IOException e)
    {
      // Log error
      LoggerSingleton.logWarning(this.getClass(), "run", "task is existing due to: " + e.toString());
    }
  }
}
