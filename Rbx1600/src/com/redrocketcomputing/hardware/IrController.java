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
 * $Id: IrController.java,v 1.2 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.hardware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author david
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IrController extends SlinkChannelController
{
  private class IRSignalListener implements SlinkChannelEventListener
  {
    private static final long SIGNAL_TIME_GAP_LIMIT = 90;

    private long previousTimeStamp;
    private long currentTimeStamp;
    private long signalTimeGap;
    private List signalBufferList;

    public IRSignalListener()
    {
      signalBufferList = new ArrayList(3);
    }

    /**
     * @see com.redrocketcomputing.hardware.SlinkChannelEventListener#channelClosed()
     */
    public void channelClosed()
    {
      previousTimeStamp = 0;
      currentTimeStamp = 0;
      signalTimeGap = 0;
      signalBufferList.clear();
    }

    /**
     * @see com.redrocketcomputing.hardware.SlinkChannelEventListener#messageReceived(int, byte[], int, int)
     */
    public synchronized void messageReceived(int channel, byte[] buffer, int offset, int length)
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceived", "channel: " + channel + "; buffer: " + buffer + "; offset: " + offset + "; length: " + length);

      previousTimeStamp = currentTimeStamp;
      currentTimeStamp = System.currentTimeMillis();
      signalTimeGap = currentTimeStamp - previousTimeStamp;

      //LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceived", "Signal time gap: " + signalTimeGap + "; no. of buffers: " + signalBufferList.size());
      
      // Flash the power led
      try
      {
        ledController.setPower(LedController.POWER_TOGGLE);
        Thread.sleep(5);
        ledController.setPower(LedController.POWER_TOGGLE);
        Thread.sleep(5);
        ledController.setPower(LedController.POWER_TOGGLE);
        Thread.sleep(5);
        ledController.setPower(LedController.POWER_TOGGLE);
      }
      catch (InterruptedException e)
      {
        // Not that importand, ignore
      }
      catch (IOException e)
      {
        // Not that importand, ignore
      }
      
      byte[] tempBuffer = new byte[length];
      System.arraycopy(buffer, offset, tempBuffer, 0, length);

      if(signalTimeGap > SIGNAL_TIME_GAP_LIMIT)
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceived", "First of multiple signals");

        // Start of a series of signals from a key press.
        // Clear the buffer
        signalBufferList.clear();
      }
      else if(signalBufferList.size() == 0)
      {
        // signal gap shorter than limit and we don't have any signal in buffer.
        // trailing signals.  Ignore.
        //LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceived", "Ignoring trailing signals");
        return;
      }

      // Add signal to buffer.
      signalBufferList.add(tempBuffer);

      if(signalBufferList.size() == 3)
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "messageReceived", "We have gotten 3 signals to vote on");

        // Now we have enough signal to vote on.
        // Call a method to handle the previous signal first.
        ArrayList tempList = new ArrayList();
        tempList.addAll(signalBufferList);

        // Clear the buffer
        signalBufferList.clear();

        // The following method will spawn a thread and return immediately
        voteAndNotify(tempList);
      }
    }
  }

  private class SignalNotifier extends AbstractTask
  {
    private ArrayList signalBufferList;
    private ArrayList listenerList;

    public SignalNotifier(ArrayList signalBufferList, ArrayList listenerList)
    {
      this.signalBufferList = signalBufferList;
      this.listenerList = listenerList;
    }

    private byte getDeviceCode(byte[] signalBuffer)
    {
      byte deviceCode = 0;

      deviceCode = (byte)(deviceCode | ((signalBuffer[1] & 0x01) << 7));
      deviceCode = (byte)(deviceCode | ((signalBuffer[2] & 0xFE) >> 1));

      return (byte)deviceCode;
    }

    private byte getCommand(byte[] signalBuffer)
    {
      return (byte)((signalBuffer[1] >> 1) & 0x7F);
    }

    /* (non-Javadoc)
     * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
     */
    public String getTaskName()
    {
      return this.getClass().getName();
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
//      for(int i = 0; i < signalBufferList.size(); i++)
//      {
//        byte[] currentBuffer = (byte[])signalBufferList.get(i);
//
//        for(int j = 0; j < currentBuffer.length; j++)
//        {
//          LoggerSingleton.logDebugCoarse(this.getClass(), "messageRecieved", "buffer " + i + ": " + Integer.toBinaryString(currentBuffer[j]));
//        }
//      }

      if(signalBufferList.size() < 3)
      {
        // Not enough signals to vote on
        return;
      }

      //boolean notify = true;
      byte[] referenceBuffer = (byte[])signalBufferList.get(0);

      for(int i = 1; i < signalBufferList.size(); i++)
      {
        byte[] buffer = (byte[])signalBufferList.get(i);

        if(referenceBuffer.length != buffer.length)
        {
          return;
        }

        for(int j = 0; j < buffer.length; j++)
        {
          if(referenceBuffer[j] != buffer[j])
          {
            return;
          }
        }
      }

      //LoggerSingleton.logDebugCoarse(this.getClass(), "messageRecieved", "deviceCode: " + Integer.toBinaryString(getDeviceCode(referenceBuffer)).substring(24));
      //LoggerSingleton.logDebugCoarse(this.getClass(), "messageRecieved", "command: " + Integer.toBinaryString(getCommand(referenceBuffer)));

      //Notify all listeners
      synchronized(listenerList)
      {
        for(int i = 0; i < listenerList.size(); i++)
        {
          ((IrEventListener)listenerList.get(i)).IrEvent(getDeviceCode(referenceBuffer), getCommand(referenceBuffer));
        }
      }
    }
  }

  private final static int CHANNEL = 7;

  private boolean done = false;
  private LedController ledController;
  private ArrayList listenerList = new ArrayList();
  
  public IrController() throws IOException
  {
    super(CHANNEL);
    
    ledController = new LedController();
    
    super.addListener(new IRSignalListener());
  }

  public void addListener(IrEventListener listener)
  {
    synchronized (listenerList)
    {
      // Add the listener
      listenerList.add(listener);
    }
  }

  public void removeListener(IrEventListener listener)
  {
    synchronized (listenerList)
    {
      // Remove the listener
      while (listenerList.remove(listener));
    }
  }

  public void close()
  {
    super.close();
  }

  private void voteAndNotify(ArrayList signalBufferList)
  {
    try
    {
      // This method is called by the IRSignalListener.
      // Try not to tie up the listener.
      // Spawn a thread to vote and notify
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
      taskPool.execute(new SignalNotifier(signalBufferList, listenerList));
    }
    catch (ServiceException e)
    {
      // Not much we can do
      LoggerSingleton.logDebugCoarse(this.getClass(), "voteAndNotify", e.toString());
    }
    catch (TaskAbortedException e)
    {
      // Not much we can do
      LoggerSingleton.logDebugCoarse(this.getClass(), "voteAndNotify", e.toString());
    }
  }
}
