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
 * $Id: ExternalPlayModeMediator.java,v 1.9 2005/03/26 22:36:29 iain Exp $
 */
package com.streetfiresound.mediamanager.player;

import java.util.Observable;

import org.havi.fcm.types.TimeCode;

import com.redrocketcomputing.havi.util.TimeDateUtil;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstPlayMode;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstSkipDirection;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstTransportState;
import com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerAdaptorFailureException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerBadVersionException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerTransitionNotAvailableException;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;
import com.streetfiresound.mediamanager.mediaplayer.types.QueueAttributeNotification;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExternalPlayModeMediator extends PlayModeMediator
{
  private DeviceAdaptor currentPlayer = null;
  private MLIDQueue playItemQueue = new MLIDQueue();

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#initialize(com.streetfiresound.mediamanager.player.MLIDQueue, com.streetfiresound.mediamanager.player.DeviceAdaptorFactory)
   */
  public ExternalPlayModeMediator(DeviceAdaptorFactory deviceAdaptorFactory) throws HaviMediaPlayerException
  {
    super(deviceAdaptorFactory);

    // Bind to the play queue
    playItemQueue.addObserver(this);

    // Log start
    LoggerSingleton.logInfo(this.getClass(), "ExternalPlayModeMediator", "loaded");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#close()
   */
  public void close()
  {
    // Release the current player
    if (currentPlayer != null)
    {
      currentPlayer.deleteObserver(this);
      currentPlayer = null;
    }

    // Unbind from the queue
    playItemQueue.deleteObserver(this);
    playItemQueue = null;

    // Forward to super class
    super.close();
  }


  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#cue(com.streetfiresound.mediamanager.mediacatalog.types.MLID[])
   */
  public int cue(MLID[] items) throws HaviMediaPlayerException
  {
    // Forward
    return playItemQueue.add(items);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#getQueue()
   */
  public PlayQueue getQueue() throws HaviMediaPlayerException
  {
    // forward
    return playItemQueue.getQueue();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#move(int, int, int, int)
   */
  public int move(int version, int direction, int start, int size) throws HaviMediaPlayerException
  {
    // Forward
    return playItemQueue.move(version, direction, start, size);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#remove(int, int, int)
   */
  public int remove(int version, int start, int size) throws HaviMediaPlayerException
  {
    // Forward
    return playItemQueue.remove(version, start, size);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#play(int, int)
   */
  public void play(int version, int playIndex) throws HaviMediaPlayerException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "play", "called with version:" + version + " index:" + playIndex);

    // First check version
    if (version != playItemQueue.getVersion())
    {
      // Bad version
      throw new HaviMediaPlayerBadVersionException("version mismatch, expecting " + playItemQueue.getVersion() + " got " + version);
    }

    // Change state
    changeState(ConstTransportState.PLAY);

    // Start playing at
    playAtItem(playIndex);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#pause()
   */
  public void pause() throws HaviMediaPlayerException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "pause", "call at " + getPosition().getIndex());

    // Verify playing state
    if (getState() != ConstTransportState.PLAY)
    {
      // Bad request
      throw new HaviMediaPlayerTransitionNotAvailableException("not playing");
    }

    // Change state
    changeState(ConstTransportState.PAUSE);

    // Forward to current player
    currentPlayer.pause();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#resume()
   */
  public void resume() throws HaviMediaPlayerException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "resume", "call at " + getPosition().getIndex());

    // Verify playing state
    if (getState() != ConstTransportState.PAUSE)
    {
      // Bad request
      throw new HaviMediaPlayerTransitionNotAvailableException("not playing");
    }

    // Change state
    changeState(ConstTransportState.PLAY);

    // Forward to current player
    currentPlayer.resume();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#stop()
   */
  public void stop() throws HaviMediaPlayerException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "stop", "call at " + getPosition().getIndex());

    // Never fail
    if (getState() != ConstTransportState.PLAY && getState() != ConstTransportState.PAUSE)
    {
      // Drop
      return;
    }

    // Check for current player
    if (currentPlayer != null)
    {
      // Forward to current player
      currentPlayer.stop();

      // Clear current player
      currentPlayer.deleteObserver(this);
      currentPlayer = null;

      // Change state
      changeState(ConstTransportState.STOP);

      // Change position
      changePosition(playItemQueue.getVersion(), getPosition().getIndex(), getPosition().getMediaLocationId(), TimeDateUtil.TIMECODE_ZERO);
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#skip(int, int)
   */
  public void skip(int direction, int count) throws HaviMediaPlayerException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "skip", "call at " + getPosition().getIndex() + " with " + direction + " " + count);

    // Verify playing state
    if (getState() != ConstTransportState.PAUSE && getState() != ConstTransportState.PLAY)
    {
      // Bad request
      throw new HaviMediaPlayerTransitionNotAvailableException("not playing");
    }

    // check for disc or track play
    if (playItemQueue.getAt(currentPosition.getPosition().getIndex()).getIndex() == 0)
    {
      // Calculate new queue index
      if (direction == ConstSkipDirection.FORWARD)
      {
        LoggerSingleton.logDebugCoarse(this.getClass(), "skip", "disc skip with " + getPosition().getMediaLocationId().getIndex() + ":" + currentPlayer.getTrackCount());
        if (getPosition().getMediaLocationId().getIndex() == currentPlayer.getTrackCount())
        {
          // Calculate new index
          int newIndex = getPosition().getIndex();
          newIndex = newIndex + 1 >= playItemQueue.size() ? 0 : newIndex + 1;

          LoggerSingleton.logDebugCoarse(this.getClass(), "skip", "end of disc, move to new queue index: " + newIndex);

          // Move to the new index
          playAtItem(newIndex);
        }
        else
        {
          LoggerSingleton.logDebugCoarse(this.getClass(), "skip", "middle of disc, forward to player");

          // Forward to the current player
          currentPlayer.skip(direction);
        }
      }
      else
      {
        // Check to see if we are at the begining or the end of the disc
        if (getPosition().getMediaLocationId().getIndex() == 1)
        {
          // Calculate new index
          int newIndex = getPosition().getIndex();
          newIndex = newIndex - 1 < 0 ? playItemQueue.size() - 1 : newIndex - 1;

          LoggerSingleton.logDebugCoarse(this.getClass(), "skip", "beginning of disc, move to new queue index: " + newIndex);

          // Move to the new index
          playAtItem(newIndex);
        }
        else
        {
          LoggerSingleton.logDebugCoarse(this.getClass(), "skip", "middle of disc, reverse to player");

          // Forward to the current player
          currentPlayer.skip(direction);
        }
      }
    }

    // Must be track
    else
    {
      // Calculate new index
      int newIndex = getPosition().getIndex();
      if (direction == ConstSkipDirection.FORWARD)
      {
        // Forward position
        newIndex = newIndex + 1 >= playItemQueue.size() ? 0 : newIndex + 1;
      }
      else
      {
        // Reverse position
        newIndex = newIndex - 1 < 0 ? playItemQueue.size() - 1 : newIndex - 1;
      }

      // Move to the new index
      playAtItem(newIndex);
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#getMode()
   */
  public int getMode() throws HaviMediaPlayerException
  {
    return ConstPlayMode.EXTERNAL;
  }

  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    try
    {
      // Check for queue events
      if (o == playItemQueue)
      {
        // Alway create notification
        AttributeNotification notification = new QueueAttributeNotification(playItemQueue.getQueue());

        // Notify observers of queue change first
        setChanged();
        notifyObservers(notification);

        // Check for empty queue
        if (playItemQueue.size() == 0 && getState() != ConstTransportState.NO_MEDIA)
        {
          // Check to see if we need to release the current player
          if (currentPlayer != null)
          {
            // Unbind from the player
            currentPlayer.deleteObserver(this);

            // Release the current player
            currentPlayer = null;
          }

          // Flush adaptor cache
          deviceAdaptorFactory.flush();

          // Change state to No Media
          changeState(ConstTransportState.NO_MEDIA);
        }

        // Check to see if our current state if no media and queue is not empty
        else if (getState() == ConstTransportState.NO_MEDIA && playItemQueue.size() != 0)
        {
          // Change state to stopped
          changeState(ConstTransportState.STOP);
        }

        // Check to is we are currently playing and index is outside the list
        else if (getState() == ConstTransportState.PLAY || getState() == ConstTransportState.PAUSE)
        {
          // Make sure we still point at the planned local
          if (getPosition().getIndex() > playItemQueue.size() || !getPosition().getMediaLocationId().equals(currentPlayer.getCurrentItem()))
          {
            LoggerSingleton.logDebugCoarse(this.getClass(), "update", "stopping current player: " + currentPlayer.getHuid());

            // Unbind from the player
            currentPlayer.deleteObserver(this);

            // Stop the current Player
            currentPlayer.stop();

            // Release the current player
            currentPlayer = null;

            // Change state
            changeState(ConstTransportState.STOP);

            // Change the current timecode to zero
            changePosition(playItemQueue.getVersion(), 0xffffffff, new MLID(), TimeDateUtil.TIMECODE_ZERO);
          }
        }

      }

      // Check for player update
      else if (o == currentPlayer)
      {
        // Check for position update
        if (arg == DeviceAdaptor.POSITION_CHANGED)
        {
          // Update position
          changePosition(playItemQueue.getVersion(), getPosition().getIndex(), currentPlayer.getCurrentItem(), currentPlayer.getTimeCode());

          MLID playerItem = currentPlayer.getCurrentItem();
          TimeCode playerPosition = currentPlayer.getTimeCode();

          LoggerSingleton.logDebugFine(this.getClass(), "update", playerItem.getList() + ":" + playerItem.getIndex() + ":" + playerPosition.getHour() + ":"+ playerPosition.getMinute() + ":" + playerPosition.getSec() + ":" + playerPosition.getFrame());
        }

        // Check transport state change
        else if (arg == DeviceAdaptor.TRANSPORT_STATE_CHANGED)
        {
          // Check for stop
          if (currentPlayer.getState() == ConstTransportState.STOP)
          {
            LoggerSingleton.logDebugCoarse(this.getClass(), "update", "stop from: " + currentPlayer.getHuid());

            // Play next item
            playNextItem();
          }
          else if (currentPlayer.getState() == ConstTransportState.PAUSE || currentPlayer.getState() == ConstTransportState.PLAY)
          {
            // Update state
            changeState(currentPlayer.getState());
          }
        }
      }
      else
      {
        // Log warning on unknow update
        LoggerSingleton.logWarning(this.getClass(), "update", "unknow update from " + o.getClass().getName());
      }
    }
    catch (HaviMediaPlayerException e)
    {
      // Not much we can do, just log error
      LoggerSingleton.logError(this.getClass(), "update", e.toString());
    }
  }

  private final void playNextItem() throws HaviMediaPlayerException
  {
    // Calculate next index
    int nextIndex = getPosition().getIndex() + 1 >= playItemQueue.size() ? 0 : getPosition().getIndex() + 1;

    // Forward
    playAtItem(nextIndex);
  }

  private final void playPreviousItem() throws HaviMediaPlayerException
  {
    // Calculate next index
    int nextIndex = getPosition().getIndex() - 1 <= 0 ? playItemQueue.size() - 1 : getPosition().getIndex() - 1;

    // Forward
    playAtItem(nextIndex);
  }

  private final void playAtItem(int index) throws HaviMediaPlayerException
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "playAtItem", "play at item " + index);

    // Check for current play disconnect
    if (currentPlayer != null)
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "playAtItem", "stopping current player " + currentPlayer.getHuid());

      // Disconnect
      currentPlayer.deleteObserver(this);

      // Always stop current player
      currentPlayer.stop();

      // Flush the current player
      currentPlayer = null;
    }

    // Check for paused player at this index
    MLID newItem = playItemQueue.getAt(index);
    DeviceAdaptor newPlayer = deviceAdaptorFactory.create(newItem.getHuid());
    if (newPlayer == null)
    {
      // Bad
      throw new HaviMediaPlayerAdaptorFailureException("can not find player for " + newItem.getHuid());
    }

    LoggerSingleton.logDebugCoarse(this.getClass(), "playAtItem", "new player " + newPlayer.getHuid());

    // Get going
    if (newPlayer.getState() == ConstTransportState.PAUSE && newItem.equals(newPlayer.getCurrentItem()))
    {
      // Just resume
      LoggerSingleton.logDebugCoarse(this.getClass(), "playAtItem", "resuming");
      newPlayer.resume();
    }
    else
    {
      // Start it playing
      LoggerSingleton.logDebugCoarse(this.getClass(), "playAtItem", "playing at " + newItem.getList() + ":" + newItem.getIndex());
      newPlayer.play(newItem);
    }

    // Calculate next index and get the next item
    int nextIndex = index + 1 >= playItemQueue.size() ? 0 : index + 1;
    MLID nextItem = playItemQueue.getAt(nextIndex);

    // Cue next item
    DeviceAdaptor nextPlayer = deviceAdaptorFactory.create(nextItem.getHuid());
    if (nextPlayer == null)
    {
      // Bad
      throw new HaviMediaPlayerAdaptorFailureException("can not find player for " + nextItem.getHuid());
    }

    // Only cue if nextPlayer is different from the currentPlayer
    if (newPlayer != nextPlayer)
    {
      LoggerSingleton.logDebugCoarse(this.getClass(), "playAtItem", "cueing player " + nextPlayer.getHuid() + " at " + nextIndex);
      nextPlayer.cue(nextItem);
    }

    // Initialize current player
    currentPlayer = newPlayer;
    currentPlayer.addObserver(this);
    changePosition(playItemQueue.getVersion(), index, newItem, TimeDateUtil.TIMECODE_ZERO);
    LoggerSingleton.logDebugCoarse(this.getClass(), "playAtItem", "done with " + newPlayer.getHuid() + "<->"  + nextPlayer.getHuid());
  }
}
