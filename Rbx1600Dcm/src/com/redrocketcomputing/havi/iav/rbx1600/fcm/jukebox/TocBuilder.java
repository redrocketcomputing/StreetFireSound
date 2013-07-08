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
 * $Id: TocBuilder.java,v 1.4 2005/02/25 19:54:40 iain Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.util.Observable;

import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.types.TimeCode;
import org.havi.system.types.HaviInvalidValueException;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public class TocBuilder extends Observable
{
  public final static int IDLE = 0;
  public final static int GETTING_DISC_INFO = 1;
  public final static int GETTING_TRACK_INFO = 2;
  public final static int GETTING_CD_TEXT = 3;
  public final static int READY = 4;
  public final static ItemIndex[] EMPTY_ITEM_INDEX = new ItemIndex[0];
  private final static int PUMP_COUNT = 1;

  private static class MessageRouter extends ProtocolEventAdaptor
  {
    private TocBuilder parent;
    private String trackTitle;
    private int track;
    private boolean startup = true;
    
    public MessageRouter(TocBuilder parent)
    {
      this.parent = parent;
    }
    
    public void handleDoorOpened()
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handlePowerOff()
    {
      // Reset the toc builder
      parent.reset();
      startup = true;
    }

    public void handleNoDisc()
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handleNotAvailable()
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handleNotLoaded()
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handleMissingDisc(int disc)
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handleChangingDisc()
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handleLoadingDisc(int disc)
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handleLoadedDisc(int disc)
    {
      // Reset the toc builder
      parent.reset();
    }

    public void handleDisplayingDisc(int disc)
    {
      // Start building the toc
      parent.startBuilding(disc);
    }

    public void handlePlayerState(int state, int mode, int disc, int track)
    {
      // Check for no media
      if (state == 0x2f)
      {
        // Reset the toc builder
        parent.reset();
      }

      // Check for power on toc building
      if ((state & 0x10) == 0 && startup)
      {
        // Force toc building
        parent.startBuilding(disc);
      }

      // Clear startup flag
      startup = false;

      // Forward to pumper
      parent.pump();
    }

    public void handleDiscInfo(int disc, int indexes, int tracks, int minutes, int seconds, int frames)
    {
      // Forward
      parent.gotDiscInfo(disc, tracks, minutes, seconds, frames);
    }

    public void handleTrackInfo(int disc, int track, int minutes, int seconds)
    {
      // Forward
      parent.gotTrackInfo(disc, track, minutes, seconds);
    }

    public void handleCdTextTrackTitle1(int track, byte[] flags, String text)
    {
      // Save parameters
      this.track = track;
      this.trackTitle = text;
      
      // Check for short length
      if (text.length() < 14)
      {
        parent.gotCdTextTrack(track, this.trackTitle);
      }
    }

    public void handleCdTextTrackTitle2(int part, String text)
    {
      // Forward
      parent.gotCdTextTrack(track, this.trackTitle + text);
    }

    public void handleEnhancedDiscMemo1(int discId, byte[] flags, String text)
    {
      // Forward
      parent.gotCdTextDisc(text);
    }

    public void handleEnhancedDiscMemo2(int part, String text)
    {
      // Forward
      parent.gotCdTextDisc(text);
    }
    
    public void handleNoEnhancedMemo()
    {
      parent.gotCdTextDisc("");
    }
    
    public void handleDuplicate()
    {
      // Forward
      parent.gotNoCdText();
    }
  }

  
  private Protocol protocol;
  private MessageRouter messageRouter;
  private ItemIndex[] currentItemIndex = EMPTY_ITEM_INDEX;
  private int currentSlot;
  private int currentTrack;
  private int pumpNeeded = PUMP_COUNT;
  private long buildMark = 0;
  private long buildDuration = 0;
  private int state = IDLE;

  public TocBuilder(Protocol protocol)
  {
    // Save the parent
    this.protocol = protocol;

    // Reset the TOC state machine
    reset();
    
    // Bind message router
    messageRouter = new MessageRouter(this);
    protocol.addEventListener(messageRouter);
  }
  
  public void close()
  {
    protocol.removeEventListener(messageRouter);
  }

  public final ItemIndex[] getCurrentItemIndex()
  {
    // Check for ready state
    if (state != READY)
    {
      return EMPTY_ITEM_INDEX;
    }

    // Index is ready
    return currentItemIndex;
  }

  public int getState()
  {
    return state;
  }
  
  public void reset()
  {
    // Reset the toc parameter
    currentSlot = -1;
    currentTrack = -1;
    currentItemIndex = EMPTY_ITEM_INDEX;

    // Reset the pump count
    pumpNeeded = PUMP_COUNT;

    // Set state to unknown
    setState(IDLE);
  }

  public void startBuilding(int slot)
  {
    try
    {
      // Check current state
      if (state != IDLE)
      {
        // Ignore
        return;
      }

      // Save the slot number
      currentSlot = slot;

      // Send disc information message
      protocol.sendQueryDiscInfo(currentSlot);

      // Reset the pump count
      pumpNeeded = PUMP_COUNT;

      // Mark build start
      buildMark = System.currentTimeMillis();

      // Change state
      setState(GETTING_DISC_INFO);
    }
    catch (ProtocolException ex)
    {
      LoggerSingleton.logError(this.getClass(), "startBuilding", "ProtocolException: " + ex.getMessage());
    }
  }

  private void setState(int newState)
  {
    // Check for state change
    if (state == newState)
    {
      // Ignore
      return;
    }
    
    // Change the state
    state = newState;

    // Change the state and notify observers
    setChanged();
    notifyObservers(new Integer(newState));
  }

  private void gotDiscInfo(int slot, int tracks, int minutes, int seconds, int frames)
  {
    try
    {
      // Verify the disc information
      if (state != GETTING_DISC_INFO)
      {
        // Drop it
        return;
      }
      
      //LoggerSingleton.logDebugCoarse(this.getClass(), "gotDiscInfo", "");

      // Allocate a new item idex list
      currentItemIndex = new ItemIndex[tracks + 1];

      // Patch the hours
      byte h = 0;
      byte m = (byte)minutes;
      if (minutes >= 60)
      {
        h = 1;
        m = (byte)(minutes % 60);
      }

      // Build disc summary
      currentItemIndex[0] = new ItemIndex();
      currentItemIndex[0].setList((short)slot);
      currentItemIndex[0].setIndex((short)0);
      currentItemIndex[0].setPlaybackTime(new TimeCode(h, m, (byte)seconds, (byte)frames));

      // Initialize current track
      currentTrack = 1;

      // Reset the pump count
      pumpNeeded = PUMP_COUNT;

      // Send track query
      protocol.sendQueryTrackInfo(currentSlot, currentTrack);

      // Changed state
      setState(GETTING_TRACK_INFO);
    }
    catch (HaviInvalidValueException ex)
    {
      // Reset
      reset();

      // Try again
      startBuilding(currentSlot);

      // Log error
      LoggerSingleton.logError(this.getClass(), "gotDiscInfo", "HaviInvalidValueException: " + ex.getMessage());
    }
    catch (ProtocolException ex)
    {
      // Reset
      reset();

      // Try again
      startBuilding(currentSlot);

      // Log error
      LoggerSingleton.logError(this.getClass(), "gotDiscInfo", "ProtocolException: " + ex.getMessage());
    }
  }

  private void gotTrackInfo(int slot, int track, int minutes, int seconds)
  {
    try
    {
      // Verify the disc information
      if (state != GETTING_TRACK_INFO)
      {
        // Drop it
        return;
      }
      
      // Check for duplicate message
      if (track != currentTrack)
      {
        // Ignore
        return;
      }

      //LoggerSingleton.logDebugCoarse(this.getClass(), "gotTrackInfo", "");

      // Patch the hours
      byte h = 0;
      byte m = (byte)minutes;
      if (minutes >= 60)
      {
        h = 1;
        m = (byte)(minutes % 60);
      }

      // Build track entry
      currentItemIndex[currentTrack] = new ItemIndex();
      currentItemIndex[currentTrack].setList((short)slot);
      currentItemIndex[currentTrack].setIndex((short)currentTrack);
      currentItemIndex[currentTrack].setPlaybackTime(new TimeCode(h, m, (byte)seconds, (byte)0));

      // Check for end of TOC
      if (currentTrack == currentItemIndex.length - 1)
      {
        // Check state to ready
        setState(GETTING_CD_TEXT);
        
        // Send CD-TEXT request
        protocol.sendGetCdText(currentSlot);

        // All done
        return;
      }

      // Update the track count
      currentTrack++;

      // Reset the pump count
      pumpNeeded = PUMP_COUNT;

      // Send next track query
      protocol.sendQueryTrackInfo(currentSlot, currentTrack);
    }
    catch (HaviInvalidValueException ex)
    {
      // Reset
      reset();

      // Try again
      startBuilding(currentSlot);

      // Log error
      LoggerSingleton.logError(this.getClass(), "gotTrackInfo", "HaviInvalidValueException: " + ex.getMessage());
    }
    catch (ProtocolException ex)
    {
      // Reset
      reset();

      // Try again
      startBuilding(currentSlot);

      // Log error
      LoggerSingleton.logError(this.getClass(), "gotTrackInfo", "ProtocolException: " + ex.getMessage());
    }
  }
  
  private void gotNoCdText()
  {
    // Check for correct state 
    if (state != GETTING_CD_TEXT)
    {
      // Drop this it is comming from somewhere else
      return;
    }
    
    //LoggerSingleton.logDebugCoarse(this.getClass(), "gotNoCdText", "");

    // Clear content type
    currentItemIndex[0].setContentType("");
    
    // Check state to ready
    setState(READY);

    // Calculate build duration
    buildDuration = System.currentTimeMillis() - buildMark;
  }
  
  private void gotCdTextDisc(String text)
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "gotCdTextDisc", "CD-TEXT found for slot " + currentSlot + " with title: " + text);

    // Clear content type
    currentItemIndex[0].setContentType("CD-TEXT");

    // Alway append
    currentItemIndex[0].setTitle(currentItemIndex[0].getTitle() + text);
  }
  
  private void gotCdTextTrack(int track, String text)
  {
    //LoggerSingleton.logDebugCoarse(this.getClass(), "gotCdTextTrack", "");

    // Initialize track title
    currentItemIndex[track].setTitle(text);
    
    // Check for end of TOC
    if (track == currentItemIndex.length - 1)
    {
      // Check state to ready
      setState(READY);

      // Calculate build duration
      buildDuration = System.currentTimeMillis() - buildMark;
    }
  }
  
  private void pump()
  {
    try
    {
      // Check to see if pump is needed
      if (--pumpNeeded == 0)
      {
        // Check for state
        int currentState = state;
        if (currentState == GETTING_DISC_INFO)
        {
          // Log warning
          LoggerSingleton.logWarning(this.getClass(), "pump", "pumping disc(" + currentSlot + ") info query on Device: " + Integer.toHexString(protocol.getDeviceId()));

          // Send disc information message
          protocol.sendQueryDiscInfo(currentSlot);
        }
        else if (currentState == GETTING_TRACK_INFO)
        {
          // Log warning
          LoggerSingleton.logWarning(this.getClass(), "pump", "pumping track(" + currentSlot + "." + currentTrack + ") info query on Device: " + Integer.toHexString(protocol.getDeviceId()));

          // Send disc information message
          protocol.sendQueryTrackInfo(currentSlot, currentTrack);
        }
        else if (currentState == GETTING_CD_TEXT)
        {
          // Log warning
          LoggerSingleton.logWarning(this.getClass(), "pump", "pumping track(" + currentSlot + "." + currentTrack + ") info query on Device: " + Integer.toHexString(protocol.getDeviceId()));
          
          // Reset disc title
          currentItemIndex[0].setTitle("");
          
          // Send CDText query 
          protocol.sendGetCdText(currentSlot);
        }

        // Reset the pump count
        pumpNeeded = PUMP_COUNT;
      }
    }
    catch (ProtocolException ex)
    {
      // Reset
      reset();

      // Try again
      startBuilding(currentSlot);

      // Log error
      LoggerSingleton.logError(this.getClass(), "pump", "ProtocolException: " + ex.getMessage());
    }
  }

  public String toString()
  {
    // Generate string based on state
    switch (state)
    {
      case IDLE:
      {
        return "Unknown TOC";
      }

      case GETTING_DISC_INFO:
      {
        return "Getting disc information for " + currentSlot;
      }

      case GETTING_TRACK_INFO:
      {
        return "Getting tracking information for " + currentSlot;
      }
      
      case GETTING_CD_TEXT :
      {
        return "Getting CD-TEXT for " + currentSlot;
      }

      case READY:
      {
        return "Built TOC for " + currentSlot + " in " + buildDuration + " milliseconds";
      }

      default:
      {
        return "Bad state";
      }
    }
  }

}
