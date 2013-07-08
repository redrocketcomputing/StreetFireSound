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
 * $Id: NativePlayModeTransportController.java,v 1.5 2005/03/17 02:28:22 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.io.IOException;
import java.util.Observable;

import org.havi.fcm.avdisc.constants.ConstAvDiscPlayMode;
import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.avdisc.types.HaviAvDiscNotSupportedException;
import org.havi.fcm.avdisc.types.HaviAvDiscTransitionNotAvailableException;
import org.havi.fcm.avdisc.types.HaviAvDiscUnidentifiedFailureException;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.constants.ConstSkipDirection;
import org.havi.fcm.constants.ConstSkipMode;

import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxException;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxTransitionNotAvailableException;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxUnidentifiedFailureException;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.havi.util.TimeDateUtil;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class NativePlayModeTransportController extends AbstractTransportController
{
  private final static ItemIndex[] EMPTY_ITEM_INDEX = new ItemIndex[0];
  
  private int mode;
  private ItemIndex[] currentItemIndex = EMPTY_ITEM_INDEX; 
  /**
   * Construct UnsupportPlayModeTransportController
   * 
   * @param parent The parent SonyJukeboxFcm
   */

  public NativePlayModeTransportController(int mode, SonyJukeboxFcm parent, Protocol protocol, PositionTracker positionTracker, TocBuilder tocBuilder, TransportStateTracker transportStateTracker, SlotCache slotCache)
  {
    // Construct super class
    super(parent, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);

    // Save the mode
    this.mode = mode;

    // Bind to components
    positionTracker.addObserver(this);
    transportStateTracker.addObserver(this);

    // Initialize current state
    parent.changeTransportState(transportStateTracker.getState(), ConstAvDiscPlayMode.DIRECT_1);

    // Log some information
    LoggerSingleton.logDebugCoarse(this.getClass(), "NativePlayModeTransportController", "started for mode " + mode);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#close()
   */
  public void close()
  {
    // Unbind
    positionTracker.deleteObserver(this);
    transportStateTracker.deleteObserver(this);

    // Forward to super class
    super.close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#play(short, short, short)
   */
  public void play(short plugNum, short listNumber, short indexNumber) throws HaviAvDiscException
  {
    try
    {
      // Patch up the index number
      indexNumber = indexNumber == 0 ? 1 : indexNumber;
      
      // Retrieve current item index
      currentItemIndex = retreiveCurrentItemIndex(listNumber);
      
      // Execute command
      PlayCommand command = new PlayCommand(protocol, listNumber & 0xffff, indexNumber & 0xffff);
      command.execute();
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#cue(short, short)
   */
  public void cue(short plugNum, short listNumber, short indexNumber) throws HaviSonyJukeboxException
  {
    try
    {
      // Patch up the index number
      indexNumber = indexNumber == 0 ? 1 : indexNumber;

      // Retrieve current item index
      currentItemIndex = retreiveCurrentItemIndex(listNumber);
      
      // Execute command
      CueCommand command = new CueCommand(protocol, listNumber & 0xffff, indexNumber & 0xffff);
      command.execute();
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException("communications error");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#scan(short, short)
   */
  public void scan(short startList, short endList) throws HaviSonyJukeboxException
  {
    // Not support
    throw new HaviSonyJukeboxTransitionNotAvailableException("scan");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#stop(int, short)
   */
  public void stop(int dir, short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Make sure we are not DOOR_OPEN or POWER_OFF
      if (transportStateTracker.getState() == ConstSonyJukeboxTransportMode.DOOR_OPENED || transportStateTracker.getState() == ConstSonyJukeboxTransportMode.POWER_OFF)
      {
        // Bad transition
        throw new HaviAvDiscTransitionNotAvailableException("current state: " + transportStateTracker.getState());
      }

      // Issue stop
      StopCommand command = new StopCommand(protocol);
      command.execute();
      
      // Clear the current item index
      currentItemIndex = EMPTY_ITEM_INDEX;
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException("communications error");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#resume(short)
   */
  public void resume(short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Make sure we are not DOOR_OPEN or POWER_OFF
      if (transportStateTracker.getState() == ConstSonyJukeboxTransportMode.DOOR_OPENED || transportStateTracker.getState() == ConstSonyJukeboxTransportMode.POWER_OFF)
      {
        // Bad transition
        throw new HaviAvDiscTransitionNotAvailableException("current state: " + transportStateTracker.getState());
      }

      // Issue resume
      ResumeCommand command = new ResumeCommand(protocol);
      command.execute();
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException("communications error");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#pause(short)
   */
  public void pause(short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Make sure we are not DOOR_OPEN or POWER_OFF
      if (transportStateTracker.getState() == ConstSonyJukeboxTransportMode.DOOR_OPENED || transportStateTracker.getState() == ConstSonyJukeboxTransportMode.POWER_OFF)
      {
        // Bad transition
        throw new HaviAvDiscTransitionNotAvailableException("current state: " + transportStateTracker.getState());
      }

      // Issue resume
      PauseCommand command = new PauseCommand(protocol);
      command.execute();
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException("communications error");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#skip(int, int, int, short)
   */
  public void skip(int direction, int mode, int count, short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Check mode
      if (mode != ConstSkipMode.TRACK)
      {
        throw new HaviAvDiscNotSupportedException("unsupported mode " + mode);
      }
      
      // Check state
      if (transportStateTracker.getState() != ConstSonyJukeboxTransportMode.PLAY && transportStateTracker.getState() != ConstSonyJukeboxTransportMode.PLAY)
      {
        // Can not do this
        throw new HaviAvDiscTransitionNotAvailableException("skip in bad mode");
      }
      
      // Skip
      for (int i = 0; i < count; i++)
      {
        // Sleep
        Thread.sleep(500);

        // Check direction
        if (direction == ConstSkipDirection.FORWARD)
        {
          // Send next track
          protocol.sendNextTrack();
        }
        else
        {
          // Send previous track
          protocol.sendPreviousTrack();
        }
      }
    }
    catch (ProtocolException e)
    {
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
    catch (InterruptedException e)
    {
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.AbstractTransportController#handles(int)
   */
  public boolean handles(int mode)
  {
    return mode == this.mode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    // Check for position update
    if (o == positionTracker)
    {
      // Only if not DOOR_OPEN or POWER_OFF
      if (transportStateTracker.getState() != ConstSonyJukeboxTransportMode.DOOR_OPENED && transportStateTracker.getState() != ConstSonyJukeboxTransportMode.POWER_OFF && transportStateTracker.getState() != ConstSonyJukeboxTransportMode.PAUSE)
      {
        // Cast it up
        AvDiscCounterValue value = (AvDiscCounterValue)arg;
        
        // Check for valid item index
        if (value.getList() != 0 && (currentItemIndex == EMPTY_ITEM_INDEX || currentItemIndex[0].getList() != value.getList()))
        {
          // Retreive current item index
          currentItemIndex = retreiveCurrentItemIndex(value.getList());
        }
        
        // Change position if range is good
        if (checkRange(value))
        {
          parent.changePosition((AvDiscCounterValue)arg);
        }
      }
      else
      {
        currentItemIndex = EMPTY_ITEM_INDEX;
      }
    }
    // Check for transport state update
    else if (o == transportStateTracker)
    {
      // Update transport state
      parent.changeTransportState(((Integer)arg).intValue(), mode);

      LoggerSingleton.logDebugCoarse(this.getClass(), "update", "transport state is " + transportStateTracker.toString());
    }
  }
  
  private final boolean checkRange(AvDiscCounterValue position)
  {
    // Range check the track index
    if (position.getIndex() < 1 || position.getIndex() >= currentItemIndex.length)
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "checkRange", "bad index: " + position.getIndex() + "<>" + currentItemIndex.length);
      // Bad track index
      return false;
    }
    
    // Convert timecode to long
    long longPosition = TimeDateUtil.toLong(position.getRelative());
    long trackDuration = TimeDateUtil.toLong(currentItemIndex[position.getIndex()].getPlaybackTime());
    if (longPosition == 0 || longPosition > trackDuration)
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "checkRange", "relative position: " + Long.toHexString(longPosition) + "<>" + Long.toHexString(trackDuration));
      // Bad relative position
      return false;
    }
    
    // All good
    return true;
  }
  
  private final ItemIndex[] retreiveCurrentItemIndex(short list)
  {
    try
    {
      return slotCache.getItemIndex(list);
    }
    catch (IOException e)
    {
      return EMPTY_ITEM_INDEX;
    }
  }
}