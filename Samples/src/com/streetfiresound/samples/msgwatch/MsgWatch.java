/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 * 
 * Created on Oct 10, 2004 by stephen
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
 */
package com.streetfiresound.samples.msgwatch;

import java.util.Timer;
import java.util.TimerTask;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.Application;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MsgWatch extends Application implements MsgWatchOnNotificationListener
{
  private SEID notifier = null;

  private class SoftwareElementCloser extends TimerTask
  {
    private SoftwareElement softwareElement;
    
    public SoftwareElementCloser(SoftwareElement softwareElement)
    {
      // Save the software element
      this.softwareElement = softwareElement;
    }
    
    public void run()
    {
      try
      {
        softwareElement.close();
      }
      catch (HaviMsgException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * @param args
   */
  public MsgWatch(String[] args) throws Exception
  {
    // Initialize system
    super(args);
    
    // Hack wait for HAVI stack to settle down
    Thread.sleep(1000);
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Create software elements
      SoftwareElement localSe = new SoftwareElement();
      SoftwareElement remoteSe = new SoftwareElement();
      
      // Log the seid
      LoggerSingleton.logInfo(this.getClass(), "run", "local " + localSe.getSeid());
      LoggerSingleton.logInfo(this.getClass(), "run", "remote " + remoteSe.getSeid());
      
      // Enable debug on the software elements
      localSe.setDebug(true);
      remoteSe.setDebug(true);
      
      // Initialize notifier
      notifier = remoteSe.getSeid();
      
      // Create watcher for the remote software element
      MsgWatchOnNotificationHelper watchHelper = new MsgWatchOnNotificationHelper(localSe, new OperationCode((short)ConstApiCode.ANY, (byte)-1));
      
      // Add watch to remote software element
      watchHelper.addListenerEx(notifier, this);
      
      // Launch timed close
      Timer timer = new Timer();
      timer.schedule(new SoftwareElementCloser(remoteSe), 5000);
      
      // Wait for close notification
      synchronized (notifier)
      {
        notifier.wait();
      }
    }
    catch (HaviMsgException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (HaviMsgListenerExistsException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (HaviException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    // Log invokation
    LoggerSingleton.logInfo(this.getClass(), "run", "lost " + targetSeid);
    
    // Match target to notifier
    if (notifier.equals(targetSeid))
    {
      synchronized (notifier)
      {
        notifier.notify();
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    // Create application
    MsgWatch application = new MsgWatch(args);
    
    // Run the application
    application.run();
    
    // Force Exist
    System.exit(0);
  }
}
