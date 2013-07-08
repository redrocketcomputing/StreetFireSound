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
 * $Id: PlayModeController.java,v 1.2 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.util.Observable;

import org.havi.fcm.avdisc.constants.ConstAvDiscPlayMode;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.util.concurrent.Latch;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class PlayModeController extends Observable implements ConstSonyJukeboxPlayMode
{
  private static class TransitionEntry
  {
    public TIntArrayList resetList = new TIntArrayList();
    public TIntArrayList toList = new TIntArrayList();
  }

  private static class MessageRouter extends ProtocolEventAdaptor
  {
    private PlayModeController parent;

    public MessageRouter(PlayModeController parent)
    {
      this.parent = parent;
    }

    public void handlePlayerState(int state, int mode, int disc, int track)
    {
      if (parent.currentPlayMode != parent.mapPlayMode(mode))
      {
        parent.currentPlayMode = parent.mapPlayMode(mode);
        parent.playModeSync.release();
        parent.setChanged();
        parent.notifyObservers(new Integer(parent.currentPlayMode));
      }
    }
  }

  private final static String[] MODE_STRINGS = { "NORMAL", "DIRECT_1", "DIRECT", "REPEAT_1", "REPEAT", "SHUFFLE", "RANDOM", "DIRECT_ALL", "RANDOM_ALL", "SHUFFLE_ALL", "REPEAT_ALL" };

  private static TIntObjectHashMap transitionMap = new TIntObjectHashMap();
  private final static int REPEAT_COMMAND = 0x1a;
  private final static int SHUFFLE_COMMAND = 0x56;
  private final static int CONTINUE_COMMAND = 0x5c;

  private Protocol protocol;
  private MessageRouter messageRounter;
  private int currentPlayMode = UNKNOWN;
  private Latch playModeSync = new Latch();
  
  static
  {
    // Build UNKNOWN
    transitionMap.put(UNKNOWN, new TransitionEntry());

    // Build NORMAL
    transitionMap.put(NORMAL, new TransitionEntry());

    // Build DIRECT_1
    transitionMap.put(DIRECT_1, new TransitionEntry());

    // Build DIRECT
    transitionMap.put(DIRECT, new TransitionEntry());

    // Build DIRECT_ALL
    TransitionEntry directAll = new TransitionEntry();
    directAll.resetList.add(CONTINUE_COMMAND);
    directAll.toList.add(CONTINUE_COMMAND);
    transitionMap.put(DIRECT_ALL, directAll);

    // Build REPEAT_1
    TransitionEntry repeat1 = new TransitionEntry();
    repeat1.resetList.add(REPEAT_COMMAND);
    repeat1.toList.add(REPEAT_COMMAND);
    repeat1.toList.add(REPEAT_COMMAND);
    transitionMap.put(REPEAT_1, repeat1);

    // Build REPEAT
    TransitionEntry repeat = new TransitionEntry();
    repeat.resetList.add(REPEAT_COMMAND);
    repeat.resetList.add(REPEAT_COMMAND);
    repeat.toList.add(REPEAT_COMMAND);
    transitionMap.put(REPEAT, repeat);

    // Build REPEAT_ALL_DISCS
    TransitionEntry repeatAll = new TransitionEntry();
    repeatAll.resetList.add(CONTINUE_COMMAND);
    repeatAll.resetList.add(REPEAT_COMMAND);
    repeatAll.resetList.add(REPEAT_COMMAND);
    repeatAll.toList.add(CONTINUE_COMMAND);
    repeatAll.toList.add(REPEAT_COMMAND);
    transitionMap.put(REPEAT_ALL, repeatAll);

    // Build SHUFFLE
    TransitionEntry shuffle = new TransitionEntry();
    shuffle.resetList.add(CONTINUE_COMMAND);
    shuffle.toList.add(SHUFFLE_COMMAND);
    transitionMap.put(SHUFFLE, shuffle);

    // Build SHUFFLE_ALL
    TransitionEntry shuffleAll = new TransitionEntry();
    shuffleAll.resetList.add(CONTINUE_COMMAND);
    shuffleAll.resetList.add(CONTINUE_COMMAND);
    shuffleAll.toList.add(CONTINUE_COMMAND);
    shuffleAll.toList.add(SHUFFLE_COMMAND);
    transitionMap.put(SHUFFLE_ALL, shuffleAll);

    // Build RANDOM
    TransitionEntry random = new TransitionEntry();
    random.resetList.add(CONTINUE_COMMAND);
    random.resetList.add(REPEAT_COMMAND);
    random.resetList.add(REPEAT_COMMAND);
    random.toList.add(SHUFFLE_COMMAND);
    random.toList.add(REPEAT_COMMAND);
    transitionMap.put(RANDOM, random);

    // Build RANDOM_ALL
    TransitionEntry randomAll = new TransitionEntry();
    randomAll.resetList.add(CONTINUE_COMMAND);
    randomAll.resetList.add(CONTINUE_COMMAND);
    randomAll.resetList.add(REPEAT_COMMAND);
    randomAll.resetList.add(REPEAT_COMMAND);
    randomAll.toList.add(CONTINUE_COMMAND);
    randomAll.toList.add(SHUFFLE_COMMAND);
    randomAll.toList.add(REPEAT_COMMAND);
    transitionMap.put(RANDOM_ALL, randomAll);
  }

  /**
   *  
   */
  public PlayModeController(Protocol protocol) throws ProtocolException
  {
    try
    {
      // Check parameter
      if (protocol == null)
      {
        // Bad
        throw new IllegalArgumentException("Protocol is null");
      }

      // Save the procotol
      this.protocol = protocol;

      // Bind message router
      messageRounter = new MessageRouter(this);
      protocol.addEventListener(messageRounter);
      
      // Send status request to force mode update
      protocol.sendQueryPlayerState();
      
      // Wait for sync
      playModeSync.acquire();
    }
    catch (InterruptedException e)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "PlayModeController", e.toString());
    }
  }

  public void close()
  {
    protocol.removeEventListener(messageRounter);
  }

  public void setPlayMode(int newMode) throws ProtocolException
  {
    // Check for change
    if (currentPlayMode == newMode)
    {
      // Ignore
      return;
    }

    // Reset the player to the default state
    TIntArrayList resetList = ((TransitionEntry)transitionMap.get(currentPlayMode)).resetList;
    for (int i = 0; i < resetList.size(); i++)
    {
      // Send the transition command
      protocol.sendIrCommand(resetList.get(i));
    }

    // Set to player new state
    TIntArrayList toList = ((TransitionEntry)transitionMap.get(newMode)).toList;
    for (int i = 0; i < toList.size(); i++)
    {
      // Send the transition command
      protocol.sendIrCommand(toList.get(i));
    }

    // Save the play mode as the state value
    currentPlayMode = newMode;
    setChanged();
    notifyObservers(new Integer(currentPlayMode));
  }

  public final int getPlayMode()
  {
    return currentPlayMode;
  }

  private final int mapPlayMode(int jukeboxMode)
  {
    switch (jukeboxMode & 0x7f)
    {
      case 0x00:
      {
        return DIRECT;
      }
      case 0x01:
      {
        return SHUFFLE;
      }
      case 0x10:
      {
        return REPEAT;
      }
      case 0x11:
      {
        return RANDOM;
      }
      case 0x20:
      case 0x21:
      case 0x60:
      case 0x61:
      {
        return ConstAvDiscPlayMode.REPEAT_1;
      }
      case 0x40:
      {
        return DIRECT_ALL;
      }
      case 0x41:
      {
        return SHUFFLE_ALL;
      }
      case 0x50:
      {
        return REPEAT_ALL;
      }
      case 0x51:
      {
        return RANDOM_ALL;
      }
      default:
      {
        LoggerSingleton.logWarning(this.getClass(), "mapPlayMode", "Unknown play mode:  0x" + Integer.toHexString(jukeboxMode));
        return UNKNOWN;
      }
    }
  }
  
  public String toString()
  {
    // Check for unknow
    if (currentPlayMode == UNKNOWN)
    {
      return "UNKNOWN";
    }
    
    // Translate
    return MODE_STRINGS[currentPlayMode]; 
  }
}