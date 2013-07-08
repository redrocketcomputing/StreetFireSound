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
 * $Id: AvDiscDeviceAdaptor.java,v 1.5 2005/03/17 02:26:32 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import org.havi.dcm.types.HUID;
import org.havi.fcm.avdisc.constants.ConstAvDiscCapability;
import org.havi.fcm.avdisc.constants.ConstAvDiscPlayMode;
import org.havi.fcm.avdisc.constants.ConstAvDiscTransportMode;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.types.AvDiscCapabilities;
import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.AvDiscCurrentState;
import org.havi.fcm.avdisc.types.AvDiscTransportState;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.constants.ConstSkipMode;
import org.havi.fcm.rmi.FcmClient;
import org.havi.fcm.rmi.FcmNotificationMessageBackHelper;
import org.havi.fcm.rmi.FcmNotificationMessageBackListener;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.fcm.types.SubscribeNotification;
import org.havi.fcm.types.TimeCode;
import org.havi.system.constants.ConstComparisonOperator;
import org.havi.system.constants.ConstDirection;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxClient;
import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstSkipDirection;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstTransportState;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerAdaptorFailureException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AvDiscDeviceAdaptor extends DeviceAdaptor implements FcmNotificationMessageBackListener
{
  private final static short POSITION_INDICATOR = com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstFcmAttributeIndicator.CURRENT_POSITION;
  private final static short STATE_INDICATOR = org.havi.fcm.avdisc.constants.ConstFcmAttributeIndicator.CURRENT_STATE;
  
  private FcmClient fcmClient;
  private AvDiscClient avDiscClient;
  private SonyJukeboxClient sonyJukeboxClient;
  private boolean[] capabilities;
  private FcmNotificationMessageBackHelper notificationHelper;
  private HUID huid;
  private SubscribeNotification stateNotificationSubscription;
  private SubscribeNotification positionNotificationSubscription;
  private AvDiscCounterValue currentPosition = new AvDiscCounterValue();
  private AvDiscCurrentState currentState = new AvDiscCurrentState();
  private short targetList = -1;
  private short targetIndex = -1;
  private AvDiscTransportState targetState = new AvDiscTransportState(ConstAvDiscTransportMode.STOP, ConstAvDiscPlayMode.DIRECT);
  private boolean targetSynced = false;
  
  /**
   * @param softwareElement
   * @param eventNotificationHelper
   * @param remoteSeid
   */
  public AvDiscDeviceAdaptor(ApplicationModule parent, SEID remoteSeid) throws HaviException
  {
    // Construct super class
    super(parent, remoteSeid);
    
    // Create clients
    fcmClient = new FcmClient(parent.getSoftwareElement(), remoteSeid);
    avDiscClient = new AvDiscClient(parent.getSoftwareElement(), remoteSeid);
    
    // Get fcm HUID
    huid = fcmClient.getHuidSync(0);
    
    // Check for Sonyjukebox
    sonyJukeboxClient = null;
    if (huid.getInterfaceId() == ConstRbx1600DcmInterfaceId.RBX1600_JUKEBOX_FCM)
    {
      // Create sony jukebox client
      sonyJukeboxClient = new SonyJukeboxClient(parent.getSoftwareElement(), remoteSeid);
    }
    
    // Get capabilities
    AvDiscCapabilities result = avDiscClient.getCapabilitySync(0);
    capabilities = result.getCapabilityList();
    
    // Create and bind notification helper
    OperationCode opCode = new OperationCode(API_CODE, nextOperationId.decrement());
    notificationHelper = new FcmNotificationMessageBackHelper(parent.getSoftwareElement(), opCode, this);
    parent.getSoftwareElement().addHaviListener(notificationHelper, remoteSeid);
    
    // Subscribe to notifications
    stateNotificationSubscription = fcmClient.subscribeNotificationSync(0, STATE_INDICATOR, new byte[0], ConstComparisonOperator.ANY, opCode);
    positionNotificationSubscription = fcmClient.subscribeNotificationSync(0, POSITION_INDICATOR, new byte[0], ConstComparisonOperator.ANY, opCode);
    
    try
    {
      // Initialize state
      HaviByteArrayInputStream stateHbais = new HaviByteArrayInputStream(stateNotificationSubscription.getCurrentValue());
      currentState = new AvDiscCurrentState(stateHbais);
      
      // Initialize position
      HaviByteArrayInputStream positionHbais = new HaviByteArrayInputStream(positionNotificationSubscription.getCurrentValue());
      currentPosition = new AvDiscCounterValue(positionHbais);
    }
    catch (HaviUnmarshallingException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#close()
   */
  public void close()
  {
    try
    {
      // Unsubscribe to notification
      fcmClient.unsubscribeNotificationSync(0, stateNotificationSubscription.getNotificationId());
      fcmClient.unsubscribeNotificationSync(0, positionNotificationSubscription.getNotificationId());
    }
    catch (HaviException e)
    {
      // Just log exception
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
    
    // Unbind helper
    parent.getSoftwareElement().removeHaviListener(notificationHelper);
    
    // Forward to super class
    super.close();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#getHuid()
   */
  public HUID getHuid()
  {
    return huid;
  }


  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#setPowerState(boolean)
   */
  public boolean setPowerState(boolean newPowerState) throws HaviMediaPlayerException
  {
    try
    {
      // Forward
      return fcmClient.setPowerStateSync(0, newPowerState);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#play(com.streetfiresound.mediamanager.mediacatalog.types.MLID)
   */
  public void play(MLID mediaLocationId) throws HaviMediaPlayerException
  {
    // Verify HUID
    if (!huid.equals(mediaLocationId.getHuid()))
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException("HUID mismatch");
    }
    
    try
    {
      // Setup target state
      targetState = new AvDiscTransportState(ConstAvDiscTransportMode.PLAY,(mediaLocationId.getIndex() == 0 ? ConstAvDiscPlayMode.DIRECT : ConstAvDiscPlayMode.DIRECT_1)); 
      targetList = mediaLocationId.getList();
      targetIndex = mediaLocationId.getIndex();
      targetSynced = false;
      
      // Forward
      avDiscClient.playSync(0, targetState.getMode(), (short)0, mediaLocationId.getList(), mediaLocationId.getIndex());
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#cue(com.streetfiresound.mediamanager.mediacatalog.types.MLID)
   */
  public void cue(MLID mediaLocationId) throws HaviMediaPlayerException
  {
    // Verify HUID
    if (!huid.equals(mediaLocationId.getHuid()))
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException("HUID mismatch");
    }
    
    try
    {
      // Setup target state
      targetState = new AvDiscTransportState(ConstAvDiscTransportMode.PAUSE,(mediaLocationId.getIndex() == 0 ? ConstAvDiscPlayMode.DIRECT : ConstAvDiscPlayMode.DIRECT_1)); 
      targetList = mediaLocationId.getList();
      targetIndex = mediaLocationId.getIndex();
      targetSynced = false;
      
      // Forward if sony jukebox
      if (sonyJukeboxClient != null)
      {
        sonyJukeboxClient.cueSync(0, targetState.getMode(), (short)0, mediaLocationId.getList(), mediaLocationId.getIndex());
      }
      else
      {
        // Mark transport as paused
        currentState.getTransportState().setState(ConstAvDiscTransportMode.PAUSE);
        currentState.getTransportState().setMode(targetState.getMode());
        targetSynced = true;
        
        // Notify observers WATCH THIS, THREAD LOCKUP POSSBILE
        setChanged();
        notifyObservers(TRANSPORT_STATE_CHANGED);
      }
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#pause()
   */
  public void pause() throws HaviMediaPlayerException
  {
    try
    {
      // Forward
      avDiscClient.pauseSync(0, (short)0);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#resume()
   */
  public void resume() throws HaviMediaPlayerException
  {
    try
    {
      // Check for paused state with cue simulation
      if (sonyJukeboxClient == null)
      {
        avDiscClient.playSync(0, targetState.getMode(), (short)0, targetList, targetIndex);
        
        // All done
        return;
      }

      // Just issue resume
      avDiscClient.resumeSync(0, (short)0);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#stop()
   */
  public void stop() throws HaviMediaPlayerException
  {
    try
    {
      // Clear target
      targetList = -1;
      targetIndex = -1;
      targetState = new AvDiscTransportState(ConstAvDiscTransportMode.STOP, currentState.getTransportState().getMode());
      targetSynced = true;
      
      // Forward
      avDiscClient.stopSync(0, ConstDirection.OUT, (short)0);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#skip(int)
   */
  public void skip(int direction) throws HaviMediaPlayerException
  {
    // Check capabilities
    if (capabilities.length <= ConstAvDiscCapability.SKIP || capabilities[ConstAvDiscCapability.SKIP])
    {
      // Fail siently
      return;
    }
    
    try
    {
      // Forward
      avDiscClient.skipSync(0, direction, ConstSkipMode.TRACK, (direction == ConstSkipDirection.REVERSE ? 2 : 1), (short)0);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#getState()
   */
  public int getState() throws HaviMediaPlayerException
  {
    // Translate current state
    switch (currentState.getTransportState().getState())
    {
      case ConstAvDiscTransportMode.STOP:
      {
        return ConstTransportState.STOP;
      }
      
      case ConstAvDiscTransportMode.PAUSE:
      {
        return ConstTransportState.PAUSE;
      }
      
      case ConstAvDiscTransportMode.PLAY:
      {
        return ConstTransportState.PLAY;
      }
      
      default:
      {
        return ConstTransportState.NO_MEDIA;
      }
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#getPosition
   */
  public TimeCode getTimeCode() throws HaviMediaPlayerException
  {
    return currentPosition.getRelative();
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#getCurrentItem()
   */
  public MLID getCurrentItem()
  {
    // Check for paused state with cue simulation
    if (currentState.getTransportState().getState() == ConstAvDiscTransportMode.PAUSE && targetList != -1 && targetIndex != -1)
    {
      return new MLID(huid, targetList, targetIndex);
    }
    
    return new MLID(huid, currentPosition.getList(), currentPosition.getIndex());
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.DeviceAdaptor#getTrackCount()
   */
  public int getTrackCount() throws HaviMediaPlayerException
  {
    try
    {
      // Check transport state
      if (currentState.getTransportState().getState() != ConstAvDiscTransportMode.PLAY && currentState.getTransportState().getState() != ConstAvDiscTransportMode.PAUSE)
      {
        return 0;
      }
      
      // Check for track mode
      if (currentState.getTransportState().getMode() == ConstAvDiscPlayMode.DIRECT_1)
      {
        return 1;
      }
      
      // Retrieve the current item index
      ItemIndex[] itemIndex = avDiscClient.getItemListSync(0, targetList);
      
      // Return length of the item index as the track count
      return itemIndex.length - 1;
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviMediaPlayerAdaptorFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmNotificationMessageBackListener#fcmNotification(short, short, byte[])
   */
  public void fcmNotification(short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
  {
    try
    {
      // Handle based on indicator
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(value);
      switch (attributeIndicator)
      {
        case POSITION_INDICATOR:
        {
          // Unmarshall position
          currentPosition = new AvDiscCounterValue(hbais);
          
          // Fire notification
          setChanged();
          notifyObservers(POSITION_CHANGED);
          
          break;
        }
        
        case STATE_INDICATOR:
        {
          // Unmarshal state
          AvDiscCurrentState newState = new AvDiscCurrentState(hbais);
          
          // Check for match target state or target synced
          if (targetSynced || targetState.equals(newState.getTransportState()))
          {
            // Update state
            currentState = newState;
            
            // Alway mark as sync
            targetSynced = true;
            
            // Update observers
            setChanged();
            notifyObservers(TRANSPORT_STATE_CHANGED);
          }

          break;
        }
      }
    }
    catch (HaviUnmarshallingException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }
}
