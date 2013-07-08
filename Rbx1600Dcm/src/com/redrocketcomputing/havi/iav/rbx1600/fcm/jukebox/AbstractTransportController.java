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
 * $Id: AbstractTransportController.java,v 1.3 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.util.Observable;
import java.util.Observer;

import org.havi.fcm.avdisc.types.HaviAvDiscAbortedException;
import org.havi.fcm.avdisc.types.HaviAvDiscException;

import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxException;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractTransportController implements Observer
{
  protected SonyJukeboxFcm parent;
  protected Protocol protocol;
  protected PositionTracker positionTracker;
  protected TocBuilder tocBuilder;
  protected TransportStateTracker transportStateTracker;
  protected SlotCache slotCache;
  protected volatile boolean open = true;

  
  /**
   * Construct an AbstractTransportController
   * @param parent The parent SonyJukeboxFcm
   */
  public AbstractTransportController(SonyJukeboxFcm parent, Protocol protocol, PositionTracker positionTracker, TocBuilder tocBuilder, TransportStateTracker transportStateTracker, SlotCache slotCache)
  {
    // Save the parameters
    this.parent = parent;
    this.protocol = protocol;
    this.positionTracker = positionTracker;
    this.tocBuilder = tocBuilder;
    this.transportStateTracker = transportStateTracker;
    this.slotCache = slotCache;
  }
  
  /**
   * Release all resources
   */
  public void close()
  {
    // Mark as closed
    open = false;
    
    try
    {
      // Try to force stop
      protocol.sendStop();
      protocol.sendStop();
      protocol.sendStop();
      protocol.sendStop();
    }
    catch (ProtocolException e)
    {
      // Just log error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#play(short, short, short)
   */
  public abstract void play(short plugNum, short listNumber, short indexNumber) throws HaviAvDiscException;

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton#cue(int, short, short)
   */
  public abstract void cue(short plugNum, short listNumber, short indexNumber) throws HaviSonyJukeboxException;
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton#scan(int, short, short)
   */
  public abstract void scan(short startList, short endList) throws HaviSonyJukeboxException;

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#stop(int, short)
   */
  public abstract void stop(int dir, short plugNum) throws HaviAvDiscException;

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#resume(short)
   */
  public abstract void resume(short plugNum) throws HaviAvDiscException;

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#pause(short)
   */
  public abstract void pause(short plugNum) throws HaviAvDiscException;
  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#skip(int, int, int, short)
   */
  public abstract void skip(int direction, int mode, int count, short plugNum) throws HaviAvDiscException;

  /**
   * Check to see if the current transport controller handle the specified ode
   * @param mode The mode to check
   * @return True if the transport controller handle the mode
   */
  public abstract boolean handles(int mode);

  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public abstract void update(Observable o, Object arg);
  
  /**
   * Check to see if the transport controller is close
   * @throws HaviAvDiscAbortedException
   */
  protected void ensureOpen() throws HaviAvDiscAbortedException
  {
    // Check for closed controller
    if (!open)
    {
      throw new HaviAvDiscAbortedException("TransportController closes");
    }
  }
}
