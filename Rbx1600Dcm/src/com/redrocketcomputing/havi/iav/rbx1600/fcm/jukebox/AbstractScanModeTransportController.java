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
 * $Id: AbstractScanModeTransportController.java,v 1.2 2005/03/08 03:21:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.util.Observable;

import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.avdisc.types.HaviAvDiscNotSupportedException;
import org.havi.fcm.avdisc.types.HaviAvDiscTransitionNotAvailableException;
import org.havi.fcm.avdisc.types.HaviAvDiscUnidentifiedFailureException;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxException;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxTransitionNotAvailableException;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxUnidentifiedFailureException;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.util.concurrent.Channel;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractScanModeTransportController extends AbstractTransportController implements Task
{
  protected final static int STOPPED = ConstSonyJukeboxTransportMode.STOP;
  protected final static int RUNNING = ConstSonyJukeboxTransportMode.PLAY;
  protected final static int PAUSED = ConstSonyJukeboxTransportMode.PAUSE;
  protected final static int ABORT = -1;

  protected WaitableInteger tocState;
  protected WaitableInteger scanState;
  protected int startSlot = -1;
  protected int endSlot = -1;
  protected int mode = -1;
  
  /**
   * @param parent
   * @param protocol
   * @param positionTracker
   * @param tocBuilder
   * @param transportStateTracker
   * @param slotCache
   */
  public AbstractScanModeTransportController(int mode, SonyJukeboxFcm parent, Protocol protocol, PositionTracker positionTracker, TocBuilder tocBuilder, TransportStateTracker transportStateTracker, SlotCache slotCache)
  {
    // Contruct super class
    super(parent, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
    
    // Save the mode
    this.mode = mode;
    
    // Bind to the TocBuilder and TransportStateTracker
    tocBuilder.addObserver(this);
    transportStateTracker.addObserver(this);
    
    // Initialize thread communication support
    tocState = new WaitableInteger(tocBuilder.getState());
    scanState = new WaitableInteger(STOPPED);
    
    // Change mode
    parent.changeTransportState(STOPPED, mode);
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#close()
   */
  public void close()
  {
    // Unbind
    tocBuilder.deleteObserver(this);
    transportStateTracker.deleteObserver(this);
    
    // Forward to super class
    super.close();
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#play(short, short, short)
   */
  public void play(short plugNum, short listNumber, short indexNumber) throws HaviAvDiscException
  {
    // Not support
    throw new HaviAvDiscTransitionNotAvailableException("play");
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#cue(short, short, short)
   */
  public void cue(short plugNum, short listNumber, short indexNumber) throws HaviSonyJukeboxException
  {
    // Not support
    throw new HaviSonyJukeboxTransitionNotAvailableException("play");
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#stop(int, short)
   */
  public void stop(int dir, short plugNum) throws HaviAvDiscException
  {
    // Check current state
    if (scanState.get() == STOPPED)
    {
      // Drop
      return;
    }
    
    try
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "stop", "stopping scanner");
      
      // Change state
      scanState.set(ABORT);
      
      // Wait for stopped state
      scanState.waitEqual(STOPPED);
      
      // Update date parent
      parent.changeTransportState(STOPPED, mode);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#scan(short, short)
   */
  public void scan(short startList, short endList) throws HaviSonyJukeboxException
  {
    // Check to see if we are already running
    if (scanState.get() != STOPPED)
    {
      // Already running
      throw new HaviSonyJukeboxTransitionNotAvailableException("play");
    }
    
    try
    {
      // Launch the scanner
      startSlot = startList & 0xffff;
      endSlot = endList & 0xffff;
      scanState.set(RUNNING);
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().get(TaskPool.class);
      taskPool.execute(this);
      
      // Change state
      scanState.set(RUNNING);
      parent.changeTransportState(RUNNING, mode);
    }
    catch (TaskAbortedException e)
    {
      // Problem
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#resume(short)
   */
  public void resume(short plugNum) throws HaviAvDiscException
  {
    // Check current state
    if (scanState.get() != PAUSED)
    {
      // Transition not support
      throw new HaviAvDiscTransitionNotAvailableException("not paused");
    }
    
    // Change state
    scanState.set(RUNNING);
    
    // Update date parent
    parent.changeTransportState(RUNNING, mode);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#pause(short)
   */
  public void pause(short plugNum) throws HaviAvDiscException
  {
    // Check current state
    if (scanState.get() != RUNNING)
    {
      // Transition not support
      throw new HaviAvDiscTransitionNotAvailableException("not running");
    }
    
    // Change state
    scanState.set(PAUSED);
    
    // Update date parent
    parent.changeTransportState(PAUSED, mode);
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#skip(int, int, int, short)
   */
  public void skip(int direction, int mode, int count, short plugNum) throws HaviAvDiscException
  {
    throw new HaviAvDiscNotSupportedException("skip");
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#handles(int)
   */
  public boolean handles(int mode)
  {
    return this.mode == mode;
  }

  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    // Check for transport state changed
    if (o == transportStateTracker)
    {
      // Check for POWER_OFF, DOOR_OPENED or NO_MEDIA
      Integer newState = (Integer)arg;
      if (newState.intValue() == ConstSonyJukeboxTransportMode.POWER_OFF || newState.intValue() == ConstSonyJukeboxTransportMode.DOOR_OPENED || newState.intValue() == ConstSonyJukeboxTransportMode.NO_MEDIA)
      {
        // Force thread to exist
        scanState.set(ABORT);
        
        // Update parent
        parent.changeTransportState(newState.intValue(), mode);
      }
      
      LoggerSingleton.logDebugCoarse(this.getClass(), "update", "transport state changed: " + newState.intValue());
      
    }
    else if (o == tocBuilder)
    {
      // Update Toc state
      tocState.set(((Integer)arg).intValue());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.taskpool.Task#executionBlocked(com.redrocketcomputing.util.concurrent.Channel)
   */
  public void executionBlocked(Channel handoff) throws TaskAbortedException
  {
    throw new TaskAbortedException("aborted due to lack of resources");
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskPriority()
   */
  public int getTaskPriority()
  {
    return Thread.NORM_PRIORITY;
  }

}
