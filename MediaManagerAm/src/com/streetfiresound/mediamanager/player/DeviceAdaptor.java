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
 * $Id: DeviceAdaptor.java,v 1.5 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import java.util.Observable;

import org.havi.dcm.types.HUID;
import org.havi.fcm.types.TimeCode;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.util.concurrent.SynchronizedByte;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
abstract class DeviceAdaptor extends Observable
{
  public final static Integer POSITION_CHANGED = new Integer(0);
  public final static Integer TRANSPORT_STATE_CHANGED = new Integer(1);
  
  protected static final short API_CODE = ConstApiCode.MEDIAPLAYER;
  
  protected static SynchronizedByte nextOperationId = new SynchronizedByte((byte)0xff);
  
  protected ApplicationModule parent;
  protected SEID remoteSeid;
  
  /**
   * Construct a DeviceAdaptor with a SoftwareElement and a EventManagerNotificationServerHelper
   * @param softwareElement The SoftwareElement to for communications 
   * @param eventNotificationHelper The EventManagerNotificationServerHelper to use for receiving events
   * @param remoteSeid The SEID of the remote device to communicate with
   */
  public DeviceAdaptor(ApplicationModule parent, SEID remoteSeid)
  {
    // Check parameters
    if (parent == null || remoteSeid == null)
    {
      throw new IllegalArgumentException("ApplicationModule or SEID is null");
    }
    
    // Save the parameters
    this.parent = parent;
    this.remoteSeid = remoteSeid;
  }
  
  /**
   * Release all resources used by the DeviceAdaptor
   */
  public void close()
  {
    parent = null;
    remoteSeid = null;
  }
  
  /**
   * Return the SEID of this DeviceAdaptor 
   */
  public final SEID getRemoteSeid()
  {
    return remoteSeid;
  }
  
  /**
   * Get the HUID of the DeviceAdaptor
   * @return
   */
  public abstract HUID getHuid();
  
  /**
   * Change the power state of the DeviceAdaptor
   * @param newPowerState True for power on and false for power off
   * @return The new PowerState
   * @throws HaviMediaPlayerException Thrown if a problem is detected changing the power state
   */
  public abstract boolean setPowerState(boolean newPowerState) throws HaviMediaPlayerException;
  
  /**
   * Start playing the specified location at starting at the specified index. If the 
   * index is 0, then play the entire media
   * @param mediaLocationId The MLID of the item to play
   * @param index The index or track offset to start playing at
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract void play(MLID mediaLocationId) throws HaviMediaPlayerException;
  
  /**
   * Cue the specified location at starting at the specified index. If the 
   * index is 0, then first index is cue.
   * @param mediaLocationId The MLID of the item to play
   * @param index The index or track offset to start playing at
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract void cue(MLID mediaLocationId) throws HaviMediaPlayerException;

  /**
   * Place the DeviceAdaptor in the paused state.  This command is ignored if the 
   * DeviceAdaptor is not playing
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract void pause() throws HaviMediaPlayerException;
  
  /**
   * Place the DeviceAdaptor in the playing state.  This command is ignored if the 
   * DeviceAdaptor is not paused
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract void resume() throws HaviMediaPlayerException;

  /**
   * Place the DeviceAdaptor in the stopped state.  This command is ignored if the 
   * DeviceAdaptor is not playing or paused
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract void stop() throws HaviMediaPlayerException;
  
  /**
   * Skip one index unit in the specified direction. This command is ignored if the 
   * currently playing item has not index units
   * @param direction 0 for skip forward and 1 for skip reverse
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract void skip(int direction) throws HaviMediaPlayerException;
  
  /**
   * Return the current transport state of this DeviceAdaptor. 
   * 0 = STOP, 1 = PLAY, 2 = PAUSE, 3 = SKIP_FORWARD, 4 = SKIP_REVERSE,
   * and 5 = NO_MEDIA
   * @return The DeviceAdaptor transport state
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract int getState() throws HaviMediaPlayerException;
  
  /**
   * Get the current relative TimeCode position of the DeviceAdaptor
   * @return The current relative TimeCode
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract TimeCode getTimeCode() throws HaviMediaPlayerException;
  
  /**
   * Get the current MLID of the DeviceAdaptor
   * @return The current MLID
   */
  public abstract MLID getCurrentItem() throws HaviMediaPlayerException;

  /**
   * Return the track count for the current playing item
   * @return The number of tracks in the currently playing item if playing a disc
   * or 1 if playing a track or zero is not playing.
   * @throws HaviMediaPlayerException Thrown if a problem is detected
   */
  public abstract int getTrackCount() throws HaviMediaPlayerException;
}