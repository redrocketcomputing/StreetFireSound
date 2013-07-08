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
 * $Id: AllScanModeTransportController.java,v 1.8 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.system.types.DateTime;

import com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstAvDiscScanMode;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.havi.util.TimeDateUtil;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AllScanModeTransportController extends AbstractScanModeTransportController
{

  public AllScanModeTransportController(SonyJukeboxFcm parent, Protocol protocol, PositionTracker positionTracker, TocBuilder tocBuilder, TransportStateTracker transportStateTracker, SlotCache slotCache)
  {
    super(ConstAvDiscScanMode.SCAN_ALL, parent, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "AllScanModeTransportController";
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      int currentSlot = startSlot;
      AvDiscCounterValue position = new AvDiscCounterValue((short)0, (short)0, TimeDateUtil.TIMECODE_ZERO, TimeDateUtil.TIMECODE_ZERO);

      // Loop through the slots
      for (currentSlot = startSlot; currentSlot <= endSlot; currentSlot++)
      {
        // Log some debug
        LoggerSingleton.logDebugCoarse(this.getClass(), "run", "scaning slot: " + currentSlot);
        
        // Wait for state to be running
        while (true)
        {
          // Check for stopped state
          if (scanState.get() == ABORT)
          {
            // Exit
            LoggerSingleton.logDebugCoarse(this.getClass(), "run", "scan task stopping");
            return;
          }
          
          // Wait awhile
          if (scanState.waitEqual(RUNNING, 1000))
          {
            // Let go baby
            break;
          }
        }
        
        // Stop the Player
        StopCommand stopCommand = new StopCommand(protocol);
        stopCommand.execute();

        // Cue up the disc
        LoggerSingleton.logDebugCoarse(this.getClass(), "run", "cueing slot: " + currentSlot);
        CueCommand cueCommand = new CueCommand(protocol, currentSlot, 1);
        cueCommand.execute();
        
        // Check for empty slot
        if (cueCommand.hasError())
        {
          // Create empty index
          LoggerSingleton.logDebugCoarse(this.getClass(), "run", "error on slot: " + currentSlot);
          DateTime current = TimeDateUtil.getCurrentDateTime();
          ItemIndex[] empty = { new ItemIndex((short)currentSlot, (short)0, "EMPTY", "EMPTY", "EMPTY", "EMPTY", TimeDateUtil.TIMECODE_ZERO, 0, current, current) };
          
          // Write to cache
          slotCache.update(empty);
        }
        else
        {
          // Wait for TOC to update
          do
          {
            // Force the tocBuilder to run
            LoggerSingleton.logDebugCoarse(this.getClass(), "run", "waiting for TOC to update on slot: " + currentSlot);
            tocBuilder.startBuilding(currentSlot);
          }
          while (!tocState.waitEqual(TocBuilder.READY, 30000));
          
          // Update the slot cache
          slotCache.update(tocBuilder.getCurrentItemIndex());
        }

        // Update the current position
        position.setList((short)(currentSlot & 0xffff));
        parent.changePosition(position);
      }
    }
    catch (ProtocolException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "run", e.toString());
    }
    catch (InterruptedException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "run", e.toString());
    }
    finally
    {
      // Change state
      scanState.set(STOPPED);

      // Update date parent
      parent.changeTransportState(STOPPED, ConstSonyJukeboxPlayMode.SCAN_ALL);
    }
  }
}