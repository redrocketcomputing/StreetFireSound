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
 * $Id: PositionTracker.java,v 1.3 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.util.Observable;

import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.types.TimeCode;

import com.redrocketcomputing.hardware.DigitalAudioQSubcodeEventListener;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolEventAdaptor;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PositionTracker extends Observable implements DigitalAudioQSubcodeEventListener
{
  private static class MessageRouter extends ProtocolEventAdaptor
  {
    private PositionTracker parent;
    
    public MessageRouter(PositionTracker parent)
    {
      this.parent = parent;
    }

    public void handleDisplayingDisc(int disc)
    {
      parent.reset((short)disc);
    }

    public void handleDoorOpened()
    {
      parent.reset();
    }

    public void handleLoadedDisc(int disc)
    {
      parent.reset((short)disc);
    }

    public void handleLoadingDisc(int disc)
    {
      parent.reset((short)disc);
    }

    public void handlePlayerState(int state, int mode, int disc, int track)
    {
      // Reset is slot position does not match
      if (parent.position.getList() != (short)disc)
      {
        parent.reset((short)disc, (short)track);
      }
    }

    public void handlePowerOff()
    {
      parent.reset();
    }

    public void handlePowerOn()
    {
      parent.reset();
    }

    public void handleStopped()
    {
      parent.reset(parent.position.getList());
    }
    
    public void handlePlayingTrack(int disc, int track, int minutes, int seconds)
    {
    }
  }
  
  private final static int CONTROL = 0;
  private final static int TNO = 1;
  private final static int POINT = 2;
  private final static int RMIN = 3;
  private final static int RSEC = 4;
  private final static int RFRAME = 5;
  private final static int AMIN = 7;
  private final static int ASEC = 8;
  private final static int AFRAME = 9;
  
  private Protocol protocol;
  private MessageRouter messageRouter;
  private AvDiscCounterValue position = new AvDiscCounterValue((short)0, (short)0, new TimeCode((byte)0, (byte)0, (byte)0, (byte)0), new TimeCode((byte)0, (byte)0, (byte)0, (byte)0));

  /**
   * 
   */
  public PositionTracker(Protocol protocol)
  {
    // Change the procotol
    if (protocol == null)
    {
      // Bad
      throw new IllegalArgumentException("protocol is null");
    }
    
    // Save protocol
    this.protocol = protocol;
    
    // Bind protocol to internal message router
    messageRouter = new MessageRouter(this);
    protocol.addEventListener(messageRouter);
  }
  
  /**
   * Release all resources
   */
  public void close()
  {
    protocol.removeEventListener(messageRouter);
  }
  
  /**
   * Return the current position list number
   * @return The current list number
   */
  public final short getCurrentList()
  {
    return position.getList();
  }
  
  /**
   * Return the current position index number
   * @return The current index number
   */
  public final short getCurrentIndex()
  {
    return position.getIndex();
  }

  /**
   * Reset the position tracker
   */
  public void reset()
  {
    // Clear the position
    position.setList((short)0);
    position.setIndex((short)0);
    position.getAbsolute().setHour((byte)0);
    position.getAbsolute().setMinute((byte)0);
    position.getAbsolute().setSec((byte)0);
    position.getAbsolute().setFrame((byte)0);
    position.getRelative().setHour((byte)0);
    position.getRelative().setMinute((byte)0);
    position.getRelative().setSec((byte)0);
    position.getRelative().setFrame((byte)0);
    setChanged();
    notifyObservers(position);
  }
  
  /**
   * Reset the position tracker
   */
  public void reset(short list)
  {
    // Clear the position
    position.setList((short)list);
    position.setIndex((short)0);
    position.getAbsolute().setHour((byte)0);
    position.getAbsolute().setMinute((byte)0);
    position.getAbsolute().setSec((byte)0);
    position.getAbsolute().setFrame((byte)0);
    position.getRelative().setHour((byte)0);
    position.getRelative().setMinute((byte)0);
    position.getRelative().setSec((byte)0);
    position.getRelative().setFrame((byte)0);
    setChanged();
    notifyObservers(position);
  }

  /**
   * Reset the position tracker
   */
  public void reset(short list, short index)
  {
    // Clear the position
    position.setList((short)list);
    position.setIndex((short)index);
    position.getAbsolute().setHour((byte)0);
    position.getAbsolute().setMinute((byte)0);
    position.getAbsolute().setSec((byte)0);
    position.getAbsolute().setFrame((byte)0);
    position.getRelative().setHour((byte)0);
    position.getRelative().setMinute((byte)0);
    position.getRelative().setSec((byte)0);
    position.getRelative().setFrame((byte)0);
    setChanged();
    notifyObservers(position);
  }

  public final String toString()
  {
    String absolute = position.getAbsolute().getHour() + ":" + position.getAbsolute().getMinute() + ":" + position.getAbsolute().getSec() + ":" + position.getAbsolute().getFrame();
    String relative = position.getRelative().getHour() + ":" + position.getRelative().getMinute() + ":" + position.getRelative().getSec() + ":" + position.getRelative().getFrame();
    return position.getList() + "," + position.getIndex() + "," + absolute + "," + relative;
  }
  
  public final String toString(AvDiscCounterValue position)
  {
    String absolute = position.getAbsolute().getHour() + ":" + position.getAbsolute().getMinute() + ":" + position.getAbsolute().getSec() + ":" + position.getAbsolute().getFrame();
    String relative = position.getRelative().getHour() + ":" + position.getRelative().getMinute() + ":" + position.getRelative().getSec() + ":" + position.getRelative().getFrame();
    return position.getList() + "," + position.getIndex() + "," + absolute + "," + relative;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.hardware.DigitalAudioQSubcodeEventListener#newQSubcode(byte[])
   */
  public void newQSubcode(byte[] subcode)
  {
    // Check address 
    if ((subcode[CONTROL] & 0x0f) != 0x01)
    {
      return;
    }
    
    // Check for lead in or lead out
    if (subcode[TNO] == 0 || subcode[TNO] == 0xaa)
    {
      position.setIndex((short)0);
      position.getAbsolute().setHour((byte)0);
      position.getAbsolute().setMinute((byte)0);
      position.getAbsolute().setSec((byte)0);
      position.getAbsolute().setFrame((byte)0);
      position.getRelative().setHour((byte)0);
      position.getRelative().setMinute((byte)0);
      position.getRelative().setSec((byte)0);
      position.getRelative().setFrame((byte)0);
    }
    else
    {
      //LoggerSingleton.logDebugCoarse(this.getClass(), "newSubcode", "POINT is nonzero: " + dumpSubcode(subcode));
      position.setIndex(bcd2Int(subcode[TNO]));
      position.getRelative().setHour((byte)0);
      position.getRelative().setMinute(bcd2Int(subcode[RMIN]));
      position.getRelative().setSec(bcd2Int(subcode[RSEC]));
      position.getRelative().setFrame(bcd2Int(subcode[RFRAME]));
      position.getAbsolute().setHour((byte)0);
      position.getAbsolute().setMinute(bcd2Int(subcode[AMIN]));
      position.getAbsolute().setSec(bcd2Int(subcode[ASEC]));
      position.getAbsolute().setFrame(bcd2Int(subcode[AFRAME]));
      
      // Patch up relative hours
      if (position.getRelative().getMinute() >= 60)
      {
        position.getRelative().setHour((byte)(position.getRelative().getMinute() / 60));
        position.getRelative().setMinute((byte)(position.getRelative().getMinute() - (position.getRelative().getHour() * 60)));
      }
  
      // Patch up absoute hours
      if (position.getAbsolute().getMinute() >= 60)
      {
        position.getAbsolute().setHour((byte)(position.getAbsolute().getMinute() / 60));
        position.getAbsolute().setMinute((byte)(position.getAbsolute().getMinute() - (position.getAbsolute().getHour() * 60)));
      }
    }
    
    // Notify observers
    setChanged();
    notifyObservers(position);
    //LoggerSingleton.logDebugCoarse(this.getClass(), "newQSubcode", "Device: " + Integer.toHexString(protocol.getDeviceId()) + ": " + toString() + " " + dumpSubcode(subcode));
  }
  
  /**
   * Convert BCD byte to numeric byte
   * @param b The byte to convert
   * @return The numeric byte
   */
  private final static byte bcd2Int(byte b)
  {
    int bcd_high_nibble = (b >> 4) & 0x0f;
    int bcd_low_nibble = b & 0x0f;

    return (byte)(bcd_high_nibble * 10 + bcd_low_nibble); 
  }

  /**
   * Dump the subcode to string
   * @param subcode The subcode to dump
   * @return The subcode as a String
   */
  private final String dumpSubcode(byte[] subcode)
  {
    // Build string buffer
    StringBuffer bufferString = new StringBuffer("subcode");
    for (int i = 0; i < subcode.length; i++)
    {
      bufferString.append(':');
      bufferString.append(Integer.toHexString(subcode[i] & 0xff));
    }
    
    // All done
    return bufferString.toString();
  }
}
