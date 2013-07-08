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
 * $Id: Protocol.java,v 1.2 2005/03/16 04:25:03 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol;

import java.io.IOException;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.SonyJukeboxSlinkDevice;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public class Protocol extends ProtocolDispatcher
{
  private ProtocolByteArrayOutputStream pbaos = new ProtocolByteArrayOutputStream(75);

  public Protocol(SonyJukeboxSlinkDevice device) throws ProtocolException
  {
    // Construct super
    super(device);
    
    // Start running
    start();
  }

  public void close()
  {
    // Forward to super class
    super.close();
  }

  public synchronized void sendPlay() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x00);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendPlay", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendStop() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x01);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendStop", "IOException: " + ex.getMessage());
    }

  }

  public synchronized void sendPause() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x02);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendPause", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendNextTrack() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x08);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendNextTrack", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendPreviousTrack() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x09);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendPreviousTrack", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendQueryPlayerState() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x0f);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendQueryPlayState", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendFastForward() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x10);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendFastForward", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendFastRewind() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x11);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendFastRewind", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendForward() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x12);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendForward", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendRewind() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x13);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendRewind", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendResume() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x1f);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendForward", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendQueryPlayerType() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x22);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendQueryPlayerType", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendQueryPlayerModel() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x6a);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendQueryPlayerModel", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendTrackPositionReportOn() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x25);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendTrackPositionReportOn", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendTrackPositionReportOff() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x26);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendTrackPositionReportOff", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendSetPowerOn() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x2e);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendSetPowerOn", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendSetPowerOff() throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

      // Write command byte
      pbaos.writeByte(0x2f);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendSetPowerOn", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendQueryDiscInfo(int discId) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Build control byte
      int control = discId > 200 ? 0x91 : 0x90;

      // Write control byte
      pbaos.writeByte(control);

      // Write command byte
      pbaos.writeByte(0x44);

      // Write discid
      if (control == 0x90)
      {
        // Write low disc id
        pbaos.writeLowDiscId(discId);
      }
      else
      {
        // Write high disc id
        pbaos.writeHighDiscId(discId);
      }

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendQueryDiscInfo", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendQueryTrackInfo(int discId, int track) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Build control byte
      int control = discId > 200 ? 0x91 : 0x90;

      // Write control byte
      pbaos.writeByte(control);

      // Write command byte
      pbaos.writeByte(0x45);

      // Write discid
      if (control == 0x90)
      {
        // Write low disc id
        pbaos.writeLowDiscId(discId);
      }
      else
      {
        // Write high disc id
        pbaos.writeHighDiscId(discId);
      }

      // Write track
      pbaos.writeBcdByte(track);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendQueryTrackInfo", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendPlayAt(int discId, int track) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Build control byte
      int control = discId > 200 ? 0x91 : 0x90;

      // Write control byte
      pbaos.writeByte(control);

      // Write command byte
      pbaos.writeByte(0x50);

      // Write discid
      if (control == 0x90)
      {
        // Write low disc id
        pbaos.writeLowDiscId(discId);
      }
      else
      {
        // Write high disc id
        pbaos.writeHighDiscId(discId);
      }

      // Write track
      pbaos.writeBcdByte(track);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendPlayAt", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendCueAt(int discId, int track) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Build control byte
      int control = discId > 200 ? 0x91 : 0x90;

      // Write control byte
      pbaos.writeByte(control);

      // Write command byte
      pbaos.writeByte(0x51);

      // Write discid
      if (control == 0x90)
      {
        // Write low disc id
        pbaos.writeLowDiscId(discId);
      }
      else
      {
        // Write high disc id
        pbaos.writeHighDiscId(discId);
      }

      // Write track
      pbaos.writeBcdByte(track);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendPlayAt", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendIrCommand(int command) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Write ir control byte
      pbaos.writeByte(0xe0);

      // Write ir command byte
      pbaos.writeByte(command);

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendPlayAt", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendFadeOut(int seconds) throws ProtocolException
  {
  	try
  	{
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

			// Write command byte
			pbaos.writeByte(0x5e);

			// Write fade duration as bcd
			pbaos.writeBcdByte(seconds);

			// Send the message
			send(pbaos.getBuffer(), 0, pbaos.size());
  	}
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendSendFadeOut", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendFadeIn(int seconds) throws ProtocolException
  {
  	try
  	{
      // Reset the output stream
      pbaos.reset();

      // Write control byte
      pbaos.writeByte(0x90);

			// Write command byte
			pbaos.writeByte(0x5f);

			// Write fade duration as bcd
			pbaos.writeBcdByte(seconds);

			// Send the message
			send(pbaos.getBuffer(), 0, pbaos.size());
  	}
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendSendFadeIn", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendSetDiscMemo1(int discId, String memo) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Build control byte
      int control = discId > 200 ? 0x91 : 0x90;

      // Write control byte
      pbaos.writeByte(control);

      // Write command byte
      pbaos.writeByte(0x80);

      // Write discid
      if (control == 0x90)
      {
        // Write low disc id
        pbaos.writeLowDiscId(discId);
      }
      else
      {
        // Write high disc id
        pbaos.writeHighDiscId(discId);
      }

      // Send only the first 13 bytes of the string
      for (int i = 0; i < 13; i++)
      {
        if (i < memo.length())
        {
          // Write the character
          pbaos.writeByte((int)(memo.charAt(i) & 0x7f));
        }
        else
        {
          // Pad with space
          pbaos.writeByte(' ' & 0x7f);
        }
      }

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendSendFadeIn", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendSetDiscMemo2(int discId, String memo) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Build control byte
      int control = discId > 200 ? 0x91 : 0x90;

      // Write control byte
      pbaos.writeByte(control);

      // Write command byte
      pbaos.writeByte(0x83);

      // Write discid
      if (control == 0x90)
      {
        // Write low disc id
        pbaos.writeLowDiscId(discId);
      }
      else
      {
        // Write high disc id
        pbaos.writeHighDiscId(discId);
      }

      // Write part number
      pbaos.writeByte(0x02);

      // Send only the first 7 bytes of the string
      for (int i = 0; i < 7; i++)
      {
        if (i < memo.length())
        {
          // Write the character
          pbaos.writeByte((int)(memo.charAt(i) & 0x7f));
        }
        else
        {
          // Pad with space
          pbaos.writeByte(' ' & 0x7f);
        }
      }

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendSendFadeIn", "IOException: " + ex.getMessage());
    }
  }

  public synchronized void sendGetCdText(int discId) throws ProtocolException
  {
    try
    {
      // Reset the output stream
      pbaos.reset();

      // Build control byte
      int control = discId > 200 ? 0x91 : 0x90;

      // Write control byte
      pbaos.writeByte(control);

      // Write command byte
      pbaos.writeByte(0x98);

      // Write discid
      if (control == 0x90)
      {
        // Write low disc id
        pbaos.writeLowDiscId(discId);
      }
      else
      {
        // Write high disc id
        pbaos.writeHighDiscId(discId);
      }

      // Send the message
      send(pbaos.getBuffer(), 0, pbaos.size());
    }
    catch (IOException ex)
    {
      LoggerSingleton.logError(this.getClass(), "sendSendFadeIn", "IOException: " + ex.getMessage());
    }
  }
}
