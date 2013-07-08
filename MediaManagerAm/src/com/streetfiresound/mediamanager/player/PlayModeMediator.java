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
 * $Id: PlayModeMediator.java,v 1.5 2005/03/22 20:41:54 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import java.util.Observable;
import java.util.Observer;

import org.havi.fcm.types.TimeCode;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstPlayMode;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstTransportState;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerInvalidParameterException;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayPosition;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;
import com.streetfiresound.mediamanager.mediaplayer.types.PositionAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.StateAttributeNotification;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
abstract class PlayModeMediator extends Observable implements Observer
{
  protected DeviceAdaptorFactory deviceAdaptorFactory;
  protected StateAttributeNotification currentState = new StateAttributeNotification(ConstTransportState.NO_MEDIA);
  protected PositionAttributeNotification currentPosition = new PositionAttributeNotification(new PlayPosition(0xffffffff, 0xffffffff, new MLID(), new TimeCode((byte)0, (byte)0, (byte)0, (byte)0)));
  
  /**
   * Factory method for create PlayModeMediator subclasses
   * @param mode The type of PlayModeMediator subsclass to create
   * @return The created subclass
   * @throws HaviMediaPlayerException Thrown if the mode is not valid
   */
  public static PlayModeMediator create(int mode, DeviceAdaptorFactory deviceAdaptorFactory) throws HaviMediaPlayerException
  {
    // Create subclass based on the mode
    switch (mode)
    {
      case ConstPlayMode.EXTERNAL:
      {
        // Not implemented
        return new ExternalPlayModeMediator(deviceAdaptorFactory);
      }
      
      case ConstPlayMode.DISABLED:
      {
        return new DisabledPlayModeMediator(deviceAdaptorFactory);
      }
      
      default:
      {
        // Bad
        throw new HaviMediaPlayerInvalidParameterException("bad mode: " + mode);
      }
    }
  }

  /**
   * Hide all subclass contructors 
   */
  protected PlayModeMediator(DeviceAdaptorFactory deviceAdaptorFactory) throws HaviMediaPlayerException
  {
    // Check parameters
    if (deviceAdaptorFactory == null)
    {
      // Bad ness
      throw new IllegalArgumentException("DeviceAdaptorFactory is null");
    }
    
    // Save the parameters
    this.deviceAdaptorFactory = deviceAdaptorFactory;
  }
  
  /**
   * Release all resources
   */
  public void close()
  {
    // Close the device factory
    deviceAdaptorFactory.close();
  }
  
  /**
   * Get the current mediator state
   * @return The current State
   * @throws HaviMediaPlayerException
   */
  public int getState() throws HaviMediaPlayerException
  {
    return currentState.getState();
  }
  
  /**
   * Return the current mediator PlayPosition
   * @return The current PlayPosition
   * @throws HaviMediaPlayerException
   */
  public PlayPosition getPosition() throws HaviMediaPlayerException
  {
    return currentPosition.getPosition();
  }

  public abstract void play(int version, int playIndex) throws HaviMediaPlayerException;
  public abstract void pause() throws HaviMediaPlayerException;
  public abstract void resume() throws HaviMediaPlayerException;
  public abstract void stop() throws HaviMediaPlayerException;
  public abstract void skip(int direction, int count) throws HaviMediaPlayerException;
  public abstract int getMode() throws HaviMediaPlayerException;
  public abstract int cue(MLID[] items) throws HaviMediaPlayerException;
  public abstract int remove(int version, int start, int size) throws HaviMediaPlayerException;
  public abstract int move(int version, int direction, int start, int size) throws HaviMediaPlayerException;
  public abstract PlayQueue getQueue() throws HaviMediaPlayerException;
  
  
  /**
   * Change the current state and fire update
   * @param newState The new state
   */
  protected void changeState(int newState)
  {
    // Check for existing state
    if (currentState.getState() == newState)
    {
      // drop
      return;
    }
    
    // Update state and notif observers
    LoggerSingleton.logDebugCoarse(this.getClass(), "changeState", "old state " + currentState.getState() + " new state " + newState);
    currentState.setState(newState);
    setChanged();
    notifyObservers(currentState);
  }
  
  /**
   * Change current position and fire update
   * @param index The new PlayQueue index
   * @param mlid The MLID of the current play item
   * @param timeCode The current relative TimeCode
   */
  protected void changePosition(int version, int index, MLID mlid, TimeCode timeCode)
  {
    // Check for match position
    if (currentPosition.getPosition().getIndex() == index && currentPosition.getPosition().getMediaLocationId().equals(mlid) && currentPosition.getPosition().getPosition().equals(timeCode))
    {
      // Drop it
      return;
    }
    
    // Update postiion
    currentPosition.getPosition().setVersion(version);
    currentPosition.getPosition().setIndex(index);
    currentPosition.getPosition().setMediaLocationId(mlid);
    currentPosition.getPosition().setPosition(timeCode);
    
    // Mark as changed and notify
    setChanged();
    notifyObservers(currentPosition);
  }
}
