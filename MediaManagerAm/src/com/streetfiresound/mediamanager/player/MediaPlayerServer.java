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
 * $Id: MediaPlayerServer.java,v 1.6 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import org.havi.applicationmodule.types.HaviApplicationModuleException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.OperationCode;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.havi.system.rmi.RemoteInvocationInformation;
import com.redrocketcomputing.havi.system.rmi.RemoteServerHelperTask;
import com.redrocketcomputing.rbx1600.devicecontroller.IrDevice;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.devicecommand.PlayerCommandFactory;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstAttributeIndicator;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstPlayMode;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerServerHelper;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerSkeleton;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerVendorEventManagerClient;
import com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerInvalidParameterException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerUnidentifiedFailureException;
import com.streetfiresound.mediamanager.mediaplayer.types.ModeAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayPosition;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;
import com.streetfiresound.mediamanager.mediaplayer.types.PositionAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.QueueAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.StateAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.SubscriptionResult;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogClient;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogException;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayList;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MediaPlayerServer implements MediaPlayerSkeleton, Observer
{
  private ApplicationModule parent;
  private MediaPlayerServerHelper serverHelper = null;
  private AttributeNotifier attributeNotifier = null;
  private PlayModeMediator playModeMediator = null;
  private PlayerCommandFactory commandFactory = null;
  
  /**
   * Construct an new media player application module
   * @param parent The parent ApplicationModule
   * @throws HaviException The is a problem is found creating the application module
   */
  public MediaPlayerServer(ApplicationModule parent) throws HaviException
  {
    // Check that parenet
    if (parent == null)
    {
      // Bad
      throw new IllegalArgumentException("ApplicationModule is null");
    }
    
    // Save the parent
    this.parent = parent;
    
    // Create command factory and bind to the IrDevice
    commandFactory = new PlayerCommandFactory(parent.getSoftwareElement());
    IrDevice ir = (IrDevice)ServiceManager.getInstance().get(IrDevice.class);
    ir.addFactory(commandFactory);

    // Create and bind server helper and enable caller info tracking
    serverHelper = new MediaPlayerServerHelper(parent.getSoftwareElement(), this);
    serverHelper.setThreadLocal(true);
    parent.getSoftwareElement().addHaviListener(serverHelper);
    
    // Create attribute notifier
    attributeNotifier = new AttributeNotifier(parent);
    
    // Create initial play mode mediator
    playModeMediator = PlayModeMediator.create(ConstPlayMode.DISABLED, new SimpleDeviceAdaptorFactory(parent));
    playModeMediator.addObserver(this);
  }

  /**
   * Release all resource 
   */
  public void close()
  {
    // Remove the command factory
    IrDevice ir = (IrDevice)ServiceManager.getInstance().get(IrDevice.class);
    ir.removeFactory(commandFactory);
    
    // Close the server helper
    serverHelper.close();
    
    // Close attribute notifier
    attributeNotifier.close();
    attributeNotifier = null;
      
    // Close the current mediator
    playModeMediator.close();
    playModeMediator = null;
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#subcribeNotification(org.havi.system.types.OperationCode, int)
   */
  public SubscriptionResult subcribeNotification(OperationCode opCode, int indicator) throws HaviMediaPlayerException
  {
    // Get caller information
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Register subscription
    short notificationId = attributeNotifier.addSubscription(invocationInformation.getSourceSeid(), opCode, indicator);
    
    // Return current state of attribute
    switch (indicator)
    {
      case ConstAttributeIndicator.MODE:
      {
        // Mode attribute
        return new SubscriptionResult(notificationId, new ModeAttributeNotification());
      }
      
      case ConstAttributeIndicator.POSITION:
      {
        // Position attribute
        return new SubscriptionResult(notificationId, new PositionAttributeNotification());
      }
      
      case ConstAttributeIndicator.STATE:
      {
        // State attribute
        return new SubscriptionResult(notificationId, new StateAttributeNotification());
      }
      
      case ConstAttributeIndicator.QUEUE:
      {
        // Queue attribute
        return new SubscriptionResult(notificationId, new QueueAttributeNotification());
      }
      
      // Bad ness
      default:
      {
        // Invalid parameter
        throw new HaviMediaPlayerInvalidParameterException("bad indicator: " + indicator);
      }
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#unsubscribeNotification(int)
   */
  public void unsubscribeNotification(short notificationId) throws HaviMediaPlayerException
  {
    // Forward to the notifier
    attributeNotifier.removeSubscription(notificationId);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#play(int)
   */
  public void play(int version, int playIndex) throws HaviMediaPlayerException
  {
    // Forward
    playModeMediator.play(version, playIndex);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#pause()
   */
  public void pause() throws HaviMediaPlayerException
  {
    // Forward
    playModeMediator.pause();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#resume()
   */
  public void resume() throws HaviMediaPlayerException
  {
    // Forward
    playModeMediator.resume();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#stop()
   */
  public void stop() throws HaviMediaPlayerException
  {
    // Forward
    playModeMediator.stop();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#skip(int)
   */
  public void skip(int direction, int count) throws HaviMediaPlayerException
  {
    // Forward
    playModeMediator.skip(direction, count);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#cue(com.streetfiresound.sample.mediaplayer.types.PlayItem[])
   */
  public int cue(MLID[] items) throws HaviMediaPlayerException
  {
    try
    {
      // Look for play list MLIDS
      boolean expand = false;
      for (int i = 0; i < items.length; i++)
      {
        // Check for local huid
        if (parent.getHuid().equals(items[i].getHuid()))
        {
          // Mark for expansion
          expand = true;
        }
      }
      
      // Do we need to expand
      if (expand)
      {
        // Setup for expansion
        PlayListCatalogClient client = new PlayListCatalogClient(parent.getSoftwareElement(), parent.getSoftwareElement().getSeid());
        ArrayList expandedItems = new ArrayList(items.length);
        
        // Loop through the items
        for (int i = 0; i < items.length; i++)
        {
          // Check again for play list MLID
          if (parent.getHuid().equals(items[i].getHuid()))
          {
            // Get the playlist
            PlayList list = client.getPlayListSync(0, items[i]);
            
            // Ensure the correct capacity
            expandedItems.ensureCapacity(items.length + list.getContent().length);
            
            // Add new items
            expandedItems.addAll(Arrays.asList(list.getContent()));
          }
          else
          {
            // Just add the item
            expandedItems.add(items[i]);
          }
        }
        
        // Convert to array
        items = (MLID[])expandedItems.toArray(new MLID[expandedItems.size()]);
      }
      
      // Forward
      return playModeMediator.cue(items);
    }
    catch (HaviApplicationModuleException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
    catch (HaviMsgException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
    catch (HaviPlayListCatalogException e)
    {
      // Translate
      throw new HaviMediaPlayerUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#remove(int, int, int)
   */
  public int remove(int version, int start, int size) throws HaviMediaPlayerException
  {
    // Forward
    return playModeMediator.remove(version, start, size);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#move(int, int, int, int, int)
   */
  public int move(int version, int direction, int start, int size) throws HaviMediaPlayerException
  {
    // Forward
    return playModeMediator.move(version, direction, start, size);
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#getState()
   */
  public int getState() throws HaviMediaPlayerException
  {
    // Forward
    return playModeMediator.getState();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#getPosition()
   */
  public PlayPosition getPosition() throws HaviMediaPlayerException
  {
    // Forward
    return playModeMediator.getPosition();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#getQueue()
   */
  public PlayQueue getQueue() throws HaviMediaPlayerException
  {
    // Forward
    return playModeMediator.getQueue();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#getMode()
   */
  public int getMode() throws HaviMediaPlayerException
  {
    // Forward
    return playModeMediator.getMode();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.sample.mediaplayer.rmi.MediaPlayerSkeleton#setMode(int)
   */
  public void setMode(int playMode) throws HaviMediaPlayerException
  {
    // Check for existing mode
    if (playMode == playModeMediator.getMode())
    {
      // Do nothing
      return;
    }
    
    // Create a new mediator
    PlayModeMediator newMediator = PlayModeMediator.create(playMode, new SimpleDeviceAdaptorFactory(parent));
    
    // Close the existing mediator
    playModeMediator.deleteObserver(this);
    playModeMediator.close();
    
    // Initialize the new one
    playModeMediator = newMediator;
    playModeMediator.addObserver(this);
  }

  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    try
    {
      // Check the observable
      if (o != playModeMediator)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "update", "unknown Observable in update");
        
        // Drop it
        return;
      }
      
      // Check argument
      if (!(arg instanceof AttributeNotification))
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "update", "bad argument type: " + arg.getClass().getName());
        
        // Drop it
        return;
      }
      
      // Cast it up
      AttributeNotification notification = (AttributeNotification)arg;
      
      // Post notifications
      attributeNotifier.updateAttribute(notification);
      
      // Post event
      MediaPlayerVendorEventManagerClient client = new MediaPlayerVendorEventManagerClient(parent.getSoftwareElement(), ConstStreetFireVendorInformation.VENDOR_ID);
      
      // Post event depending on type
      switch (notification.getDiscriminator())
      {
        case ConstAttributeIndicator.MODE:
        {
          // Cast attribute notification
          ModeAttributeNotification modeAttributeNotification = (ModeAttributeNotification)notification;
          
          // Fire event
          client.fireMediaPlayerModeChangedSync(modeAttributeNotification.getMode());
          
          // All done
          break;
        }
        
        case ConstAttributeIndicator.QUEUE:
        {
          // Cast attribute notification
          QueueAttributeNotification queueAttributeNotification = (QueueAttributeNotification)notification;
          
          // Fire event
          client.fireMediaPlayerQueueChangedSync(queueAttributeNotification.getQueue().getVersion());
          
          // All done
          break;
        }
        
        case ConstAttributeIndicator.STATE:
        {
          // Cast attribute notification
          StateAttributeNotification stateAttributeNotification = (StateAttributeNotification)notification;
          
          // Fire event
          client.fireMediaPlayerStateChangedSync(stateAttributeNotification.getState());
          
          // All done
          break;
        }
      }
    }
    catch (HaviException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "update", e.toString());
    }
  }
}
