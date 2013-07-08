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

import java.awt.Component;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import org.havi.dcm.rmi.DcmClient;
import org.havi.dcm.types.HUID;
import org.havi.fcm.avdisc.constants.ConstAvDiscTransportMode;
import org.havi.fcm.avdisc.rmi.AvDiscClient;
import org.havi.fcm.avdisc.rmi.AvDiscItemListChangedEventNotificationListener;
import org.havi.fcm.avdisc.types.AvDiscCapabilities;
import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.AvDiscCurrentState;
import org.havi.fcm.avdisc.types.AvDiscTransportState;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.rmi.FcmClient;
import org.havi.fcm.rmi.FcmNotificationMessageBackHelper;
import org.havi.fcm.rmi.FcmNotificationMessageBackListener;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.SubscribeNotification;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstComparisonOperator;
import org.havi.system.constants.ConstDirection;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxClient;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;
import com.redrocketcomputing.util.concurrent.LinkedQueue;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.client.Util;
import com.streetfiresound.clientlib.LookupTask.LookupRequest;
import com.streetfiresound.clientlib.event.SlotScanProgressEvent;
import com.streetfiresound.clientlib.event.SlotScannerInitializedEvent;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;


//XXX:00:20050228iain: consider renaming RemoteSlotScanner
//XXX:00000000000000000:20050301iain: double check for concurrency issues
/**
 *  Coordinates a slot scan
 *  @author iain huxley
 */
public class SlotScanner implements AvDiscItemListChangedEventNotificationListener
{
  private final static OperationCode NOTIFICATION_OPCODE   = new OperationCode((short)0xffff, (byte)0xff);
  private final static int           MULTI_SLOT_CAPABILITY = org.havi.fcm.avdisc.constants.ConstAvDiscCapability.MULTI_SLOT;
  private final static int           SCAN_CAPABILITY       = com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstAvDiscCapability.SCAN;
  private final static short         POSITION_INDICATOR    = com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstFcmAttributeIndicator.CURRENT_POSITION;
  private final static short         STATE_INDICATOR       = org.havi.fcm.avdisc.constants.ConstFcmAttributeIndicator.CURRENT_STATE;

  // settings for current scan
  private int mode             = -1;               // scan mode (see ConstAvDiscScanMode) or -1 if not scanning
  private int slotsScanned     = -1;               // slots scanned so far, or -1 if not sxcanning
  private int totalSlotsToScan = -1;               // total slots to scan, or -1 if not scaxnning

  private StreetFireClient     client;             // the primary client object
  private Component            renderComponent;    // component used as rendering target
  private SoftwareElement      softwareElement;    // our SE

  private SonyJukeboxInfo[]    sonyJukeboxes;      // info for the jukeboxes on the RBX1600 we're talking to (non-null only after init)

  private LinkedQueue lookupQueue = new LinkedQueue();
  private LookupTask lookupTask;

  private EventManagerNotificationServerHelper eventHelper;

  /**
   * @param renderComponent the component on which found album art will be drawn
   */
  public SlotScanner(StreetFireClient client, Component renderComponent)
  {
    // keep params
    this.client          = client;
    this.renderComponent = renderComponent;

    // retreive our software element
    softwareElement = client.getSoftwareElement();
  }

  /**
   *  asynchronously trigger an init (loads player capacities, etc.)
   */
  public void init()
  {
    // decrement request tracker
    client.incrementAsyncRequests();

    // fire off the init task to find the sony jukeboxes and their capabilities, as well as subsribing to events etc.
    client.executeTask(new AbstractTask()
                       {
                         public void run()
                         {
                           initImpl();  // do it
                         };
                       }
                      );
  }

  /**
   *  return true if in the middle of scanning
   */
  public boolean isScanning()
  {
    return totalSlotsToScan != -1;
  }

  /**
   *  Initiate the slot scan.
   *
   *  NOTE: may only be called after the SlotScannerInitializedEvent has been delivered
   *
   *  @param mode slot scan mode, see ConstAvDiscScanMode
   */
  public void startSlotScan(int mode, int[] startSlots, int[] endSlots)
  {
    final int   finalMode       = mode;
    final int[] finalStartSlots = startSlots;
    final int[] finalEndSlots   = endSlots;
    // schedule the implementation to run on another thread
    client.executeTask(new AbstractTask()
                       {
                         public void run()
                         {
                           startSlotScanImpl(finalMode, finalStartSlots, finalEndSlots);  // do it
                         };
                       }
                      );
  }


  //XXX:0:20050301iain: consider breaking up the following method, it's pretty long
  /**
   *  Performs the initialization.  Uses numerous synchronous calls, so is invoked from the InitTask task.
   */
  private void initImpl()
  {
    // should not be called on AWT event dispatch thread, will block
    assert !Toolkit.getDefaultToolkit().getSystemEventQueue().isDispatchThread();

    // should not yet be initialized
    assert sonyJukeboxes == null;

    try // big try block, lots of HAVi Horsesh^H^H^H^H^H^H^Hstuff ahead
    {
      // register to avdisc item list changed event
      client.getEventHelper().addEventSubscription(new SystemEventId(ConstSystemEventType.AVDISC_ITEM_LIST_CHANGED), SlotScanner.this);

      // retreive the dcm client
      DcmClient dcmClient = new DcmClient(softwareElement, client.getRbx1600DcmSied());

      //       // Power on the DCM
      //       if (!dcmClient.setPowerStateSync(0, true))
      //       {
      //         throw new IllegalStateException("can not turn on power to DCM");
      //      }

      // Build Sony Jukebox FCM SEID list
      LoggerSingleton.logDebugCoarse(SlotScanner.class, "initImpl", "enumerating sony jukeboxes");

      List fcmClientList         = new ArrayList();
      List avDiscClientList      = new ArrayList();
      List sonyJukeboxClientList = new ArrayList();
      List playerHuidList        = new ArrayList();

      SEID[] dcmSeidList         = dcmClient.getFcmSeidListSync(0);

      //MsgWatchOnNotificationHelper watchHelper = new MsgWatchOnNotificationHelper(softwareElement);
      for (int i = 0; i < dcmSeidList.length; i++)
      {
        // Create an fcm client
        FcmClient fcmClient = new FcmClient(softwareElement, dcmSeidList[i]);

        // Get the HUID which contains the interface id
        HUID huid = fcmClient.getHuidSync(0);

        // Check for match
        if (huid.getInterfaceId() == ConstRbx1600DcmInterfaceId.RBX1600_JUKEBOX_FCM)
        {
          // Add to the lists
          fcmClientList.add(fcmClient);
          avDiscClientList.add(new AvDiscClient(softwareElement, dcmSeidList[i]));
          sonyJukeboxClientList.add(new SonyJukeboxClient(softwareElement, dcmSeidList[i]));
          playerHuidList.add(huid);

          // Add watch
          //watchHelper.addListenerEx(dcmSeidList[i], SlotScanner.this);
        }
      }

      assert    fcmClientList.size() == avDiscClientList.size()
             && fcmClientList.size() == sonyJukeboxClientList.size()
             && fcmClientList.size() == playerHuidList.size();

      // populate UNSORTED (potentially sparse) jukeboxes array
      SonyJukeboxInfo[] sonyJukeboxUnsortedArray = new SonyJukeboxInfo[StreetFireClient.MAX_PLAYERS];
      for (int i=0; i<playerHuidList.size(); i++)
      {
        // get the huid and figure out the channel
        HUID playerHuid = (HUID)playerHuidList.get(i);
        int chan = playerHuid.getTargetId().getN2();

        // populate the array index correspoding to the player channel
        sonyJukeboxUnsortedArray[chan] = new SonyJukeboxInfo();
        sonyJukeboxUnsortedArray[chan].fcmClient         = (FcmClient)fcmClientList.get(i);
        sonyJukeboxUnsortedArray[chan].avDiscClient      = (AvDiscClient)avDiscClientList.get(i);
        sonyJukeboxUnsortedArray[chan].sonyJukeboxClient = (SonyJukeboxClient)sonyJukeboxClientList.get(i);
        sonyJukeboxUnsortedArray[chan].huid              = playerHuid;

        // Retrieve capabilities
        sonyJukeboxUnsortedArray[chan].capabilities      = sonyJukeboxUnsortedArray[chan].avDiscClient.getCapabilitySync(0);

        // Check for multi slot capabilities
        if (sonyJukeboxUnsortedArray[chan].capabilities.getCapabilityList().length <= MULTI_SLOT_CAPABILITY || !sonyJukeboxUnsortedArray[chan].capabilities.getCapabilityList()[MULTI_SLOT_CAPABILITY])
        {
          throw new IllegalStateException("player " + i + " does not have MULTI_SLOT capability");
        }

        // Check for scan capabilities and multi-slot
        if (sonyJukeboxUnsortedArray[chan].capabilities.getCapabilityList().length <= SCAN_CAPABILITY || !sonyJukeboxUnsortedArray[chan].capabilities.getCapabilityList()[SCAN_CAPABILITY])
        {
          throw new IllegalStateException("player " + i + " does not have SCAN capability");
        }
      }

      // create the compacted, ordered array
      sonyJukeboxes = new SonyJukeboxInfo[playerHuidList.size()];
      for (int i=0, count=0; i<sonyJukeboxUnsortedArray.length; i++)
      {
        if (sonyJukeboxUnsortedArray[i] != null)
        {
          sonyJukeboxes[count++] = sonyJukeboxUnsortedArray[i];
        }
      }

      LoggerSingleton.logDebugCoarse(getClass(), "run", "found " + sonyJukeboxes.length + " sony jukeboxes");

      // Stop all players
      LoggerSingleton.logDebugCoarse(getClass(), "run", "stopping all players");
      for (int i = 0; i < sonyJukeboxes.length; i++)
      {
        sonyJukeboxes[i].avDiscClient.stopSync(0, 0, (short)0);
      }

      // Build stop state indicator
      AvDiscCurrentState matchingState = new AvDiscCurrentState(new AvDiscTransportState(ConstAvDiscTransportMode.STOP, mode), ConstDirection.OUT, (short)0);
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
      matchingState.marshal(hbaos);
      byte[] stateAttributeValue = hbaos.toByteArray();

      // Subscribe to transports state and position notifications
      LoggerSingleton.logDebugCoarse(getClass(), "run", "subscribing to notifications");

      // Subscribe to notifications saving the notificaiton id of the state notification
      byte notificationOperationId = (byte)0xff;
      for (int i = 0; i < sonyJukeboxes.length; i++)
      {
        FcmClient fcmClient = sonyJukeboxes[i].fcmClient;
        OperationCode notificationOpCode = new OperationCode(ConstApiCode.ANY, notificationOperationId);
        softwareElement.addHaviListener(new FcmNotificationMessageBackHelper(softwareElement, notificationOpCode, new FcmNotificationDispatcher(fcmClient.getDestSeid())));
        SubscribeNotification notification = fcmClient.subscribeNotificationSync(0, POSITION_INDICATOR, new byte[0], ConstComparisonOperator.ANY, notificationOpCode);
        notification = fcmClient.subscribeNotificationSync(0, STATE_INDICATOR, new byte[0], ConstComparisonOperator.ANY, notificationOpCode);
        notificationOperationId--; // need to use unique id to differentiate notifications
      }
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("Error initializing slot scanner", e);
    }
    catch (HaviMarshallingException e)
    {
      throw new MediaOrbRuntimeException("Error initializing slot scanner", e);
    }

    // decrement request tracker
    client.decrementAsyncRequests();

    // queue a notification event for delivery
    client.getEventDispatcher().queueEvent(new SlotScannerInitializedEvent(sonyJukeboxes));
  }

  /**
   *  Perform the slot scan.
   *
   *  The slot scan occurs asynchronously, but this method does contain a synchronous call,
   *  so should NOT be called directly from the AWT event thread etc.
   *
   *  NOTE: may only be called after the SlotScannerInitializedEvent has been delivered
   *
   */
  private synchronized void startSlotScanImpl(int mode, int[] startSlots, int[] endSlots)
  {
    assert !Toolkit.getDefaultToolkit().getSystemEventQueue().isDispatchThread();
    assert startSlots.length <= sonyJukeboxes.length;
    assert startSlots.length == endSlots.length;
    assert sonyJukeboxes  != null; // must be properly initialized
    assert totalSlotsToScan == -1; // must be in non-scanning state
    assert slotsScanned     == -1; // must be in non-scanning state
    assert this.mode        == -1; // must be in non-scanning state

    // init counts
    slotsScanned     = 0;
    totalSlotsToScan = 0;

    // keep mode
    this.mode = mode;

    // Change mode of media player
    //XXX:000000000000000000000000000:20050315iain:mediaPlayerClient.setMode(ConstPlayMode.DISABLED);

    try
    {
      // Start each jukebox scanning
      for (int i = 0; i < sonyJukeboxes.length; i++)
      {
        assert endSlots[i]   >= startSlots[i];
        assert startSlots[i] >  0;
        assert endSlots[i]   <= sonyJukeboxes[i].capabilities.getCapacity();

        // accumulate total
        totalSlotsToScan += endSlots[i] - startSlots[i] + 1; // +1 since it's inclusive

        // start scanning
        sonyJukeboxes[i].sonyJukeboxClient.scanSync(0, mode, (short)startSlots[i], (short)endSlots[i]);
      }
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("Error starting slot scan", e);
    }
  }

  /**
   * @see org.havi.fcm.avdisc.rmi.AvDiscItemListChangedEventNotificationListener#avDiscItemListChangedEventNotification(org.havi.system.types.SEID, short)
   */
  public void avDiscItemListChangedEventNotification(SEID posterSeid, short listNumber)
  {
    LoggerSingleton.logDebugCoarse(SlotScanner.class, "avDiscItemListChangedEventNotification", " posterSeid is '" + posterSeid + "', listNumber is '" + listNumber + "' " + (isScanning() ? "" : " [will be discard3ed, not scanning]"));

    // ignore if we're not scannning
    if (!isScanning())
    {
      return;
    }

    try
    {
      if (posterSeid.getGuid().equals(client.getMediaManagerSeid()))
      {
        // Post the lookup thread
        //XXX:000000000000000000000000000000:20050301iain: disabled, for now force lookup always
        if (false)lookupQueue.put(new LookupRequest(posterSeid, listNumber));
      }
    }
    catch (InterruptedException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "avDiscItemListChangedEventNotification", e.toString());
    }
  }

  /**
   *  Progress notification
   *  @see org.havi.fcm.rmi.FcmNotificationMessageBackListener#fcmNotification(short, short, byte[])
   */
  public synchronized void fcmNotification(SEID remoteSeid, short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
  {
    // don't care if not slotscanning
    if (!isScanning())
    {
      return;
    }

    // figure out which player sent this
    int playerIndex = -1;
    for (int i=0; i<sonyJukeboxes.length; i++) //XXX:0000:20050301iain: switch jukebox array to jukebox map?
    {
      if (sonyJukeboxes[i].fcmClient.getDestSeid().equals(remoteSeid))
      {
        playerIndex = i;
        break;
      }
    }
    assert playerIndex != -1; // must have a match

    // Handle based on type
    switch (attributeIndicator)
    {
      case POSITION_INDICATOR:
      {
        AvDiscCounterValue position = null;
        try
        {
          // Unmarshall
          position = new AvDiscCounterValue(new HaviByteArrayInputStream(value));

          // get the metadata itemindex objects (not really indices, they're metadata)
          ItemIndex[] discInfo = sonyJukeboxes[playerIndex].avDiscClient.getItemListSync(0, position.getList());

          // perform conversions
          MediaMetaData[] mediaMetadata = Util.itemIndexArrayToMediaMetaDataArray(sonyJukeboxes[playerIndex].huid, discInfo);
          ContentMetadata[] contentMetadata = Util.mediaMetaDataArrayToContentMetadataArray(mediaMetadata);

          // queue the event for delivery, bumping the slotsScanned count
          client.getEventDispatcher().queueEvent(new SlotScanProgressEvent(++slotsScanned, totalSlotsToScan, contentMetadata));

          // see if we're done
          if (slotsScanned == totalSlotsToScan)
          {
            // done, reset
            totalSlotsToScan = -1;
            slotsScanned = -1;
            mode = -1;
          }
        }
        catch (HaviUnmarshallingException e)
        {
          throw new MediaOrbRuntimeException("error unmarshalling position info", e);
        }
        catch (HaviException e)
        {
          throw new MediaOrbRuntimeException("error unmarshalling position info", e);
        }

        //XXX:00000000000000000000000000000000:20050301iain: currently forcing lookup
        LookupRequest request = new LookupRequest(sonyJukeboxes[playerIndex].avDiscClient.getDestSeid(), position.getList());

        // Post the lookup thread
        client.executeTask(new LookupTask(client, request, renderComponent));

        break;
      }

//       case STATE_INDICATOR:
//       {
//         System.out.println("XXX:000000000000000000:iain:>>>>fcm notify, value is '" + value + "'");
//         break;
//       }
//       default:
//       {
//         // Log error
//         LoggerSingleton.logError(this.getClass(), "fcmNotification", "bad attribute indicator: " + attributeIndicator);
//       }
    }
  }

  /**
   *  called by the cddb lookup object to notify that a lookup is complete
   */
  public void lookupComplete(HUID playerHuid, int slot, ItemIndex[] result)
  {
    // NOTE currently this method does not require synchronization, it does not access any slot scanner state info
    LoggerSingleton.logDebugCoarse(SlotScanner.class, "lookupComplete", "cddb lookup complete for slot " + slot);

    if (result != null)
    {
      MediaMetaData[]   mediaMetadata   = Util.itemIndexArrayToMediaMetaDataArray(playerHuid, result);
      ContentMetadata[] contentMetadata = Util.mediaMetaDataArrayToContentMetadataArray(mediaMetadata);

      // fire off a cover art search
      if (   !contentMetadata[0].getTitle().equalsIgnoreCase(Util.TOKEN_UNKNOWN)
          && !contentMetadata[0].getTitle().equalsIgnoreCase(Util.TOKEN_EMPTY  ))
      {
        try
        {
          client.getTaskPool().execute(new RequestDiscArtTask(client, contentMetadata[0], renderComponent));
        }
        catch (TaskAbortedException ex)
        {
          throw new ClientRuntimeException("Disc art search task aborted", ex); //XXX:0000:20050224iain:
        }
      }

      // queue the event for delivery
      //client.getEventDispatcher().queueEvent(new SlotScanProgressEvent(metadata));
    }
  }

  /**********************************************************************************
   * STATIC INNER CLASS - jukebox info, havi clients (for local use only)
   *********************************************************************************/
  public static class SonyJukeboxInfo
  {
    private HUID               huid;              // HUID this sony jukebox
    private FcmClient          fcmClient;         // fcm havi client
    private AvDiscClient       avDiscClient;      // avdisc havi client
    private SonyJukeboxClient  sonyJukeboxClient; // sonyjukebox havi client
    private AvDiscCapabilities capabilities;      // capabilities (includes capacity)

    public HUID getHuid()
    {
      return huid;
    }

    /**
     *  return the jukebox channel, starting at zero (must add one for displaying to the user)
     */
    public int getChannel()
    {
      return huid.getTargetId().getN2();
    }

    public AvDiscCapabilities getCapabilities()
    {
      return capabilities;
    }

    public String toString()
    {
      return "SonyJukeboxInfo[huid=" + huid + ", capabilities=" + capabilities + "]";
    }
  }

  /**********************************************************************************
   * PRIVATE INNER CLASS - Class that resolves fcmNotifications from different FCMs, providing an seid for resolution
   *********************************************************************************/
  private class FcmNotificationDispatcher implements FcmNotificationMessageBackListener
  {
    private SEID remoteSeid;

    public FcmNotificationDispatcher(SEID remoteSeid)
    {
      this.remoteSeid = remoteSeid;
    }

    /**
     *  adds the stored SEID to the fcmNotification API
     */
    public void fcmNotification(short notificationId, short attributeIndicator, byte[] value) throws HaviFcmException
    {
      // pass it back to the slot scanner with the seid
      SlotScanner.this.fcmNotification(remoteSeid, notificationId, attributeIndicator, value);
    }
  }
}
