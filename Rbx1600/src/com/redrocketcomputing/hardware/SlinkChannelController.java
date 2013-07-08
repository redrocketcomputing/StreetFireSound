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
 * $Id: SlinkChannelController.java,v 1.2 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.hardware;

import java.io.IOException;
import java.util.ArrayList;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;

/**
 * @author stephen
 *
 */
public class SlinkChannelController extends AbstractTask
{
  public final static int NUMBER_OF_CHANNELS = 6;

  private final static String DEVICE_PATH = "/proc/driver/slink/slc";
  private final static byte CANCEL = (byte) 0xff;
  private final static int MAX_PACKET_SIZE = 33;

  private int channel;
  private ArrayList listenerList = new ArrayList();
  private volatile boolean done = false;
  private DeviceDriver deviceDriver;

  public SlinkChannelController(int channel) throws IOException
  {
    try
    {
      // Save the channel number
      this.channel = channel;

      // driver object
      deviceDriver = new DeviceDriver(DEVICE_PATH + channel);

      //Get the task pool
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);

      // Launch task
      taskPool.execute(this);
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new IOException(e.toString());
    }
  }

  public void close()
  {
    try
    {
      // Mark as complete
      done = true;

      // Write cancel to device
      deviceDriver.writeByte(CANCEL);
    }
    catch (IOException e)
    {
      // Ignore
    }
  }

  public void addListener(SlinkChannelEventListener listener)
  {
    synchronized (listenerList)
    {
      // Add the listener
      listenerList.add(listener);
    }
  }

  public void removeListener(SlinkChannelEventListener listener)
  {
    synchronized (listenerList)
    {
      // Remove the listener
      while (listenerList.remove(listener));
    }
  }

  public void send(byte[] buffer, int offset, int length) throws IOException
  {
    /* Forward to driver */
    deviceDriver.write(buffer, offset, length);
  }

  /**
   * Returns the channel number of this controller
   * @return int The channel number
   */
  public int getChannel()
  {
    return channel;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    byte[] packet = new byte[MAX_PACKET_SIZE];

    // Loop until interrupted, done or error
    while (!done)
    {
      try
      {
        // Read from the device
        int size = deviceDriver.read(packet);

        // Check for cancel
        if (size <= 0)
        {
          // Dispatch events
          synchronized (listenerList)
          {
            for (int i = 0; i < listenerList.size(); i++)
            {
              ((SlinkChannelEventListener) listenerList.get(i)).channelClosed();
            }
          }

          // Mark as done
          done = true;
        }
        else
        {
          // Dispatch events
          synchronized (listenerList)
          {
            for (int i = 0; i < listenerList.size(); i++)
            {
              ((SlinkChannelEventListener) listenerList.get(i)).messageReceived(channel, packet, 0, size);
            }
          }
        }
      }
      catch (IOException e)
      {
        // Mark as done
        done = true;
      }
    }

    // Close the driver
    deviceDriver.close();
  }
  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "SlinkChannelController(" + channel + ')';
  }
}
