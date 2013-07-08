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
 * $Id $
 */
package com.streetfiresound.clientlib;

import java.util.ArrayList;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.types.HaviException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.client.Util;
import com.streetfiresound.clientlib.event.ContentIdsArrivedEvent;
import com.streetfiresound.clientlib.event.VersionedContentIdListArrivedEvent;
import com.streetfiresound.clientlib.event.PlayModeChangedEvent;
import com.streetfiresound.clientlib.event.PlayPositionUpdateEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.TransportStateChangedEvent;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstAttributeIndicator;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstPlayMode;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerAsyncResponseHelper;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerClient;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackHelper;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackListener;
import com.streetfiresound.mediamanager.mediaplayer.rmi.RemoveAsyncResponseListener;
import com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.ModeAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayPosition;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;
import com.streetfiresound.mediamanager.mediaplayer.types.PositionAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.QueueAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.StateAttributeNotification;


//XXX:0000000:20050310iain: re-request queue when necessary

/**
 *  This class represents a media player provided on the network via HAVi
 *  It is a facade class which attempts to simplify access to a remote media player.
 *  In particular, it subscribes to necessary HAVi callbacks or events and delivers
 *  notification via StreetFireClient's event mechanism
 *
 *  @author iain huxley
 */
public class RemoteMediaPlayer implements MediaPlayerNotificationMessageBackListener, RemoveAsyncResponseListener
{
  private static final OperationCode NOTIFICATION_OPCODE = new OperationCode(ConstApiCode.ANY, (byte)0xff);

  private MediaPlayerClient mediaPlayerClient;
  private StreetFireClient  client;
  private SoftwareElement   softwareElement;
  private MediaPlayerAsyncResponseHelper asyncHelper;

  private SEID mediaManagerSeid; // media player is part of media manager software element

  // local state representation
  private int           transportState;       // current transport state
  private PlayQueue     queue;                // current play queue
  private int           playMode;             // current play mode
  private int           lastVersion = -1;     // most recent queue version id
  private ContentId     currentItem;
  private int           currentItemQueueIndex = -1;

  public RemoteMediaPlayer(StreetFireClient client)
  {
    // init members
    this.client = client;
    softwareElement = client.getSoftwareElement();

    try
    {
      asyncHelper = new MediaPlayerAsyncResponseHelper(softwareElement);
      softwareElement.addHaviListener(asyncHelper);

      // Create clients and bind servers
      mediaManagerSeid = client.getMediaManagerSeid();

      if (mediaManagerSeid == null)
      {
        client.fatalError("No media manager found");
        throw new MediaOrbRuntimeException("No media manager found", null);
      }

      MediaPlayerNotificationMessageBackHelper notificationHelper = new MediaPlayerNotificationMessageBackHelper(softwareElement, NOTIFICATION_OPCODE, this);
      softwareElement.addHaviListener(notificationHelper, mediaManagerSeid);
      mediaPlayerClient = new MediaPlayerClient(softwareElement, mediaManagerSeid);

      // Bind notifications
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.MODE);
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.POSITION);
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.QUEUE);
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.STATE);

      // Change mode of media player
      mediaPlayerClient.setMode(ConstPlayMode.EXTERNAL);

      // init state
      transportState        = mediaPlayerClient.getStateSync(0);
      playMode              = mediaPlayerClient.getModeSync(0);
      queue                 = mediaPlayerClient.getQueueSync(0);
      lastVersion           = queue.getVersion();
      currentItemQueueIndex = mediaPlayerClient.getPositionSync(0).getIndex(); //XXX:0:20050315iain: init other position stuff

      if (currentItemQueueIndex != -1 && queue.getQueue().length > 0)
      {
        currentItem         = new MlidContentId(queue.getQueue()[currentItemQueueIndex]);
      }

//       // XXX:00000000000000000000000000000000000000000000000:20050320iain: test hacks
//       transportState        = ConstTransportState.STOP;
//       playMode              = ConstPlayMode.EXTERNAL;
//       queue                 = new PlayQueue();
//       currentItemQueueIndex = 0;
//       lastVersion           = 0;
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("error during media player init", e);
    }
  }


  /**
   *  Get the content id of the item now playing/cued/paused
   */
  public ContentId getCurrentItem()
  {
    return currentItem;
  }


  /**
   *  get the play queue index of the current item playing/cued/paused
   *  @return immutable versioned index
   */
  public VersionedIndex getCurrentItemQueueIndex()
  {
    return new VersionedIndex(currentItemQueueIndex, lastVersion);
  }

  /**
   *  get the play queue (versioned, immutable)
   */
  public VersionedContentIdList getPlayQueue()
  {
    return new HaviPlayQueueContentIdList(queue);
  }

  /**
   *  get the current play mode, ConstPlayMode.XXX
   */
  public int getPlayMode()
  {
    return playMode;
  }

  /**
   *  get the current transport state
   */
  public int getTransportState()
  {
    return transportState;
  }

  /**
   *  async request: add items to the end of the play queue
   */
  public void addItems(ContentId[] items)
  {
    try
    {
      assert lastVersion != -1;
      lastVersion = mediaPlayerClient.cue(Util.mlidContentIdArrayToMlidArray(items));
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("cue error", e);
    }
  }

  /**
   *  async request: play a given index in the play queue
   *
   */
  public void play(VersionedIndex versionedIndex)
  {
    try
    {
      mediaPlayerClient.play(versionedIndex.version, versionedIndex.index);
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("resume error", e);
    }
  }

  /**
   *  async request: resume play from pause state
   */
  public void resume()
  {
    try
    {
      mediaPlayerClient.resume();
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("resume error", e);
    }
  }

  /**
   *  async request: skip forward/backwards
   *  @param direction ConstSkipDirection.XXX
   */
  public void skip(int direction, int count)
  {
    try
    {
      mediaPlayerClient.skip(direction, count);
     }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("skip error", e);
    }
  }

  /**
   *  async request: stop play
   */
  public void stop()
  {
    try
    {
      mediaPlayerClient.stop();
     }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("stop error", e);
    }
  }

  /**
   *  async request: pause
   */
  public void pause()
  {
    try
    {
      mediaPlayerClient.pause();
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("pause/resume error", e);
    }
  }

  /**
   *  Remove items from the play queue
   *
   *  @param indices list of indices to remove
   */
  public void removeItemsByIndex(VersionedIndex[] indices)
  {
    assert indices.length > 0;
    int version = indices[0].version;

    // disallow non-contiguous selections
    int startIndex = indices[0].index;
    for (int i=1; i<indices.length; i++)
    {
      assert indices[i].version == version;
      if (indices[i].index-1 != indices[i-1].index)
      {
        throw new IllegalStateException("non contiguous selection not yet implemented");
      }
    }

    try
    {
      // send the remove request, the current version sent to ensure indices are correct
      int transactionId = mediaPlayerClient.remove(version, startIndex, indices.length);

      // add response listener
      asyncHelper.addAsyncResponseListener(60000, transactionId, this);
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("pause error during play queue remove", e);
    }
  }

  //--------------------------------------------------------------------------------------------------------
  // SECTION: havi event notification
  //--------------------------------------------------------------------------------------------------------

  /**
   *  event notification
   *  @see com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackListener#mediaPlayerNotification(int, com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification)
   */
  public void mediaPlayerNotification(int notificationId, AttributeNotification value) throws HaviMediaPlayerException
  {
    // Handle based on type of notification
    switch (value.getDiscriminator())
    {
      case ConstAttributeIndicator.POSITION:
      {
        // Cast it up
        PositionAttributeNotification positionNotification = (PositionAttributeNotification)value;
        PlayPosition playPosition = positionNotification.getPosition();

        // update queue index stored here
        currentItemQueueIndex    = playPosition.getIndex();

        // dispatch event
        client.getEventDispatcher().queueEvent(new PlayPositionUpdateEvent(StreetFireEvent.NOT_APPLICABLE, positionNotification.getPosition()));

        // Dump some information
        //XXX:00000000000000000000000000000000000000000000:20050322iain: suppressing playposition events LoggerSingleton.logDebugFine(RemoteMediaPlayer.class, "mediaPlayerNotification", "notified of position change, new position=" + playPosition);

        break;
      }

      case ConstAttributeIndicator.MODE:
      {
        // Cast it up
        ModeAttributeNotification modeNotification = (ModeAttributeNotification)value;

        // log info
        LoggerSingleton.logDebugCoarse(RemoteMediaPlayer.class, "mediaPlayerNotification", "notified of a mode change to " + modeNotification.getMode());

        // keep it
        playMode = modeNotification.getMode();

        // notify
        //XXX:000000000:20050307iain: queue event
        client.getEventDispatcher().queueEvent(new PlayModeChangedEvent(StreetFireEvent.NOT_APPLICABLE, playMode));

        break;
      }

      case ConstAttributeIndicator.QUEUE:
      {
        // Cast it up
        QueueAttributeNotification queueNotification = (QueueAttributeNotification)value;

        // keep version
        lastVersion = queueNotification.getQueue().getVersion();

        // keep updated queue
        queue = queueNotification.getQueue();

        // log info
        LoggerSingleton.logDebugCoarse(RemoteMediaPlayer.class, "mediaPlayerNotification", "notified of a queue change, new queue has " + queue.getQueue().length + " entries");

        // notify
        client.getEventDispatcher().queueEvent(new VersionedContentIdListArrivedEvent(StreetFireEvent.NOT_APPLICABLE, new HaviPlayQueueContentIdList(queue)));

        break;
      }

      case ConstAttributeIndicator.STATE:
      {
        // Cast it up
        StateAttributeNotification stateNotification = (StateAttributeNotification)value;

        // keep it
        transportState = stateNotification.getState();

        // notify
        client.getEventDispatcher().queueEvent(new TransportStateChangedEvent(StreetFireEvent.NOT_APPLICABLE, transportState));

        // Dump some information
        LoggerSingleton.logDebugCoarse(RemoteMediaPlayer.class, "mediaPlayerNotification", "player notified state has changed to " + stateNotification.getState());
        break;
      }
    }
  }

  //--------------------------------------------------------------------------------------------------------
  // SECTION: havi async response handler methods
  //--------------------------------------------------------------------------------------------------------

  /**
   * called to notify that an category summary has arrived
   */
	public void handleRemove(int transactionId, int result, Status returnCode)
  {
    // check for errors
    if (returnCode.getErrCode() != 0)
    {
      // XXX:000000000000:20041213iain: not quite sure how to handle this
      throw new ClientRuntimeException("havi request returned error code " + returnCode.getErrCode(), null);
    }
  }

  public void timeout(int transactionId)
  {
    throw new MediaOrbRuntimeException("transaction id " + transactionId + " timed out", null);
  }
}
