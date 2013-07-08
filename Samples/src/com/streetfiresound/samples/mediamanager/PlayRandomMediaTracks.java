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
 * $Id: PlayRandomMediaTracks.java,v 1.4 2005/03/16 04:25:34 stephen Exp $
 */
package com.streetfiresound.samples.mediamanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.havi.fcm.types.TimeCode;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.constants.ConstVendorEventType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.VendorEventId;

import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.streetfiresound.commands.HaviCommandLineApplication;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstCategoryType;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaItemType;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogClient;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstAttributeIndicator;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstPlayMode;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerClient;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerModeChangedEventNotificationListener;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackHelper;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackListener;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerQueueChangedEventNotificationListener;
import com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerStateChangedEventNotificationListener;
import com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.ModeAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;
import com.streetfiresound.mediamanager.mediaplayer.types.PositionAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.QueueAttributeNotification;
import com.streetfiresound.mediamanager.mediaplayer.types.StateAttributeNotification;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PlayRandomMediaTracks extends HaviCommandLineApplication implements MediaPlayerNotificationMessageBackListener, MediaPlayerStateChangedEventNotificationListener, MediaPlayerQueueChangedEventNotificationListener, MediaPlayerModeChangedEventNotificationListener
{
  private final static OperationCode NOTIFICATION_OPCODE = new OperationCode(ConstApiCode.ANY, (byte)0xff);
  
  private SoftwareElement softwareElement;
  private SEID mediaManagerSeid;
  private Object done = new Object();
  
  /**
   * 
   */
  public PlayRandomMediaTracks()
  {
    super();
  }
  
  public void run()
  {
    try
    {
      // Sleep for 5 seconds to let the network settle
      Thread.sleep(5000);

      // Look for match GUID
      GUID matchGuid = GuidUtil.fromString(ConfigurationProperties.getInstance().getProperties().getProperty("match.guid", "ff:ff:ff:ff:ff:ff:ff:ff"));

      // Create software element
      softwareElement = new SoftwareElement();
      EventManagerNotificationServerHelper eventServerHelper = new EventManagerNotificationServerHelper(softwareElement);
      
      // Rbx1600 DCMs
      System.out.println("finding MediaManager");
      SimpleAttributeTable attributes = new SimpleAttributeTable();
      attributes.setSoftwareElementType(ConstSoftwareElementType.APPLICATION_MODULE);
      attributes.setInterfaceId(ConstMediaManagerInterfaceId.MEDIA_MANAGER);
      RegistryClient registryClient = new RegistryClient(softwareElement);
      QueryResult result = registryClient.getElementSync(0, attributes.toQuery());
      
      // Look for matching GUID
      mediaManagerSeid = null;
      for (int i = 0; i < result.getSeidList().length; i++)
      {
        // Check for match GUID
        if (matchGuid.equals(GUID.BROADCAST) || result.getSeidList()[i].getGuid().equals(matchGuid))
        {
          // Found it
          mediaManagerSeid = result.getSeidList()[i];
          break;
        }
      }
      
      // Check not found
      if (mediaManagerSeid == null)
      {
        System.out.println(matchGuid.toString() + " not found");
        System.exit(1);
      }
      
      // Create clients and bind servers
      MediaPlayerNotificationMessageBackHelper notificationHelper = new MediaPlayerNotificationMessageBackHelper(softwareElement, NOTIFICATION_OPCODE, this);
      softwareElement.addHaviListener(notificationHelper, mediaManagerSeid);
      MediaPlayerClient mediaPlayerClient = new MediaPlayerClient(softwareElement, mediaManagerSeid);
      MediaCatalogClient mediaCatalogClient = new MediaCatalogClient(softwareElement, mediaManagerSeid);

      // Bind event listeners
      System.out.println("bind event listeners");
      eventServerHelper.addEventSubscription(new VendorEventId(ConstVendorEventType.MEDIA_PLAYER_STATE_CHANGED, ConstStreetFireVendorInformation.VENDOR_ID), this); 
      eventServerHelper.addEventSubscription(new VendorEventId(ConstVendorEventType.MEDIA_PLAYER_QUEUE_CHANGED, ConstStreetFireVendorInformation.VENDOR_ID), this); 
      eventServerHelper.addEventSubscription(new VendorEventId(ConstVendorEventType.MEDIA_PLAYER_MODE_CHANGED, ConstStreetFireVendorInformation.VENDOR_ID), this); 
      
      // Bind notifications
      System.out.println("bind notification listeners");
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.MODE);
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.POSITION);
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.QUEUE);
      mediaPlayerClient.subcribeNotificationSync(0, NOTIFICATION_OPCODE, ConstAttributeIndicator.STATE);
      
      // Build play queue
      System.out.print("Building list with ");
      MLID[] discMlids = mediaCatalogClient.getMediaSummarySync(0, ConstCategoryType.TYPE, ConstMediaItemType.CDDA);
      MediaMetaData[] summaryMetaData = mediaCatalogClient.getMultipleMetaDataSync(0, discMlids);
      List itemList = new ArrayList(discMlids.length * 10);
      for (int i = 0; i < summaryMetaData.length; i++)
      {
        // Check for not empty
        if (!summaryMetaData[i].getTitle().equals("EMPTY"))
        {
          // Retreive corresponding track data
          MediaMetaData[] discMetaData = mediaCatalogClient.getMetaDataSync(0, summaryMetaData[i].getMediaLocationId());
          
          // Add to item list
          for (int j = 1; j < discMetaData.length; j++)
          {
            itemList.add(discMetaData[j].getMediaLocationId());
          }
        }
      }
      
      // Shuffle list and convert to array
      Collections.shuffle(itemList);
      MLID[] playItems = (MLID[])itemList.toArray(new MLID[itemList.size()]);
      System.out.println(playItems.length + " items");
      
      // Change mode of media player
      mediaPlayerClient.setMode(ConstPlayMode.EXTERNAL);
      
      // Load list
      System.out.println("Loading item into queue");
      int version = mediaPlayerClient.cueSync(0, playItems);
      
      // Start playing
      System.out.println("Starting playing at 0");
      mediaPlayerClient.playSync(0, version, 0);
      
      // Wait for every
      synchronized(done)
      {
        done.wait();
      }
    }
    catch (HaviException e)
    {
      // Dump stack trace
      e.printStackTrace();
      System.exit(1);
    }
    catch (InterruptedException e)
    {
      // Dump stack trace
      e.printStackTrace();
      System.exit(1);
    }
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerNotificationMessageBackListener#mediaPlayerNotification(int, com.streetfiresound.mediamanager.mediaplayer.types.AttributeNotification)
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
        
        // Dump some information
        int queueIndex = positionNotification.getPosition().getIndex();
        TimeCode timeCode = positionNotification.getPosition().getPosition();
        short slot = positionNotification.getPosition().getMediaLocationId().getList();
        short track = positionNotification.getPosition().getMediaLocationId().getIndex();
        System.out.println("index: " + queueIndex + " slot: " + slot + " track: " + track + " time: " + timeCode.getHour() + ":" + timeCode.getMinute() + ":" + timeCode.getSec() + ":" + timeCode.getFrame());

        break;
      }
      
      case ConstAttributeIndicator.MODE:
      {
        // Cast it up
        ModeAttributeNotification modeNotification = (ModeAttributeNotification)value;
        
        // Dump some information
        System.out.println("player notified a mode changed to " + modeNotification.getMode());
        break;
      }

      case ConstAttributeIndicator.QUEUE:
      {
        // Cast it up
        QueueAttributeNotification queueNotification = (QueueAttributeNotification)value;
        
        // Dump some information, We could but will not dump the queue contents
        System.out.println("player notified queue has changed to " + queueNotification.getQueue().getVersion());
        break;
      }

      case ConstAttributeIndicator.STATE:
      {
        // Cast it up
        StateAttributeNotification stateNotification = (StateAttributeNotification)value;
        
        // Dump some information
        System.out.println("player notified state has changed to " + stateNotification.getState());
        break;
      }
    }
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerStateChangedEventNotificationListener#mediaPlayerStateChangedEventNotification(org.havi.system.types.SEID, int)
   */
  public void mediaPlayerStateChangedEventNotification(SEID posterSeid, int state)
  {
    // Match seid
    if (!mediaManagerSeid.equals(posterSeid))
    {
      // Drop
      return;
    }
    
    //System.out.println("player state changed to " + state);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerQueueChangedEventNotificationListener#mediaPlayerQueueChangedEventNotification(org.havi.system.types.SEID, int)
   */
  public void mediaPlayerQueueChangedEventNotification(SEID posterSeid, int version)
  {
    // Match seid
    if (!mediaManagerSeid.equals(posterSeid))
    {
      // Drop
      return;
    }
    
    //System.out.println("new queue version: " + version);
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.mediaplayer.rmi.MediaPlayerModeChangedEventNotificationListener#mediaPlayerModeChangedEventNotification(org.havi.system.types.SEID, int)
   */
  public void mediaPlayerModeChangedEventNotification(SEID posterSeid, int mode)
  {
    // Match seid
    if (!mediaManagerSeid.equals(posterSeid))
    {
      // Drop
      return;
    }
    
    //System.out.println("player mode changed to " + mode);
  }
  
  public static void main(String[] args)
  {
    // Create application
    PlayRandomMediaTracks application = new PlayRandomMediaTracks();
    
    // Run application
    application.run();
    
    // All done
    System.exit(0);
  }
}
