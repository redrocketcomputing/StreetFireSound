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
 * $Id: ProtocolDispatcher.java,v 1.2 2005/03/16 04:25:03 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol;

import java.io.IOException;

import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.SonyJukeboxSlinkDevice;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

class ProtocolDispatcher extends ProtocolBase
{
  public ProtocolDispatcher(SonyJukeboxSlinkDevice device) throws ProtocolException
  {
    // Construct super class
    super(device);

    // Add dispatcher
    addDispatchHandler(ErrorDispatch.TYPE, new ErrorDispatch());
    addDispatchHandler(NoDiscDispatch.TYPE, new NoDiscDispatch());
    addDispatchHandler(DuplicateDispatch.TYPE, new DuplicateDispatch());
    addDispatchHandler(NoDiscDispatch.TYPE, new NoDiscDispatch());
    addDispatchHandler(DuplicateDispatch.TYPE, new DuplicateDispatch());
    addDispatchHandler(NotLoadedDispatch.TYPE, new NotLoadedDispatch());
    addDispatchHandler(NotAvailableDispatch.TYPE, new NotAvailableDispatch());
    addDispatchHandler(MissingDiscDispatch.TYPE, new MissingDiscDispatch());
    addDispatchHandler(PlayingDispatch.TYPE, new PlayingDispatch());
    addDispatchHandler(StoppedDispatch.TYPE, new StoppedDispatch());
    addDispatchHandler(PausedDispatch.TYPE, new PausedDispatch());
    addDispatchHandler(ChangingDiscDispatch.TYPE, new ChangingDiscDispatch());
    addDispatchHandler(ReadyDispatch.TYPE, new ReadyDispatch());
    addDispatchHandler(EotIn30Dispatch.TYPE, new EotIn30Dispatch());
    addDispatchHandler(DoorOpenedDispatch.TYPE, new DoorOpenedDispatch());
    addDispatchHandler(DoorClosedDispatch.TYPE, new DoorClosedDispatch());
    addDispatchHandler(PowerOnDispatch.TYPE, new PowerOnDispatch());
    addDispatchHandler(PowerOffDispatch.TYPE, new PowerOffDispatch());
    addDispatchHandler(CdTextDetectedDispatch.TYPE, new CdTextDetectedDispatch());
    addDispatchHandler(PlayingTrackDispatch.TYPE, new PlayingTrackDispatch());
    addDispatchHandler(TrackPositionDispatch.TYPE, new TrackPositionDispatch());
    addDispatchHandler(DisplayingDiscDispatch.TYPE, new DisplayingDiscDispatch());
    addDispatchHandler(LoadingDiscDispatch.TYPE, new LoadingDiscDispatch());
    addDispatchHandler(LoadedDiscDispatch.TYPE, new LoadedDiscDispatch());
    addDispatchHandler(PlayerTypeDispatch.TYPE, new PlayerTypeDispatch());
    addDispatchHandler(PlayerModelDispatch.TYPE, new PlayerModelDispatch());
    addDispatchHandler(PlayerStateDispatch.TYPE, new PlayerStateDispatch());
    addDispatchHandler(DiscIndDispatch.TYPE, new DiscIndDispatch());
    addDispatchHandler(TrackInfoDispatch.TYPE, new TrackInfoDispatch());
    addDispatchHandler(MemoWrittenDispatch.TYPE, new MemoWrittenDispatch());
    addDispatchHandler(GroupContent1Dispatch.TYPE, new GroupContent1Dispatch());
    addDispatchHandler(GroupContent2Dispatch.TYPE, new GroupContent2Dispatch());
    addDispatchHandler(GroupContent3Dispatch.TYPE, new GroupContent3Dispatch());
    addDispatchHandler(GroupContent4Dispatch.TYPE, new GroupContent4Dispatch());
    addDispatchHandler(EnhancedDiscMemo1Dispatch.TYPE, new EnhancedDiscMemo1Dispatch());
    addDispatchHandler(EnhancedDiscMemo2Dispatch.TYPE, new EnhancedDiscMemo2Dispatch());
    addDispatchHandler(CdTextTrackTitle1Dispatch.TYPE, new CdTextTrackTitle1Dispatch());
    addDispatchHandler(CdTextTrackTitle2Dispatch.TYPE, new CdTextTrackTitle2Dispatch());
    addDispatchHandler(NoEnhancedMemoDispatch.TYPE, new NoEnhancedMemoDispatch());
  }

  private class NoDiscDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x05;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forware
      listener.handleNoDisc();
    }
  }

  private class DuplicateDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x0e;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleDuplicate();
    }
  }

  private class ErrorDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x0f;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleError();
    }
  }

  private class NotLoadedDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x14;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleNotLoaded();
    }
  }

  private class NotAvailableDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x15;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleNotAvailable();
    }
  }

  private class MissingDiscDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x53;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Forward
      listener.handleMissingDisc(discId);
    }
  }

  private class PlayingDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x00;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handlePlaying();
    }
  }

  private class StoppedDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x01;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleStopped();
    }
  }

  private class PausedDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x02;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handlePaused();
    }
  }

  private class ChangingDiscDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x06;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleChangingDisc();
    }
  }

  private class ReadyDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x08;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleReady();
    }
  }

  private class EotIn30Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0xc;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleEotIn30();
    }
  }

  private class DoorOpenedDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x18;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleDoorOpened();
    }
  }

  private class NoEnhancedMemoDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x1c;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleNoEnhancedMemo();
    }
  }
  
  private class MemoWrittenDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x1f;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleMemoWritten();
    }
  }

  private class PowerOnDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x2e;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handlePowerOn();
    }
  }

  private class PowerOffDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x2f;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handlePowerOff();
    }
  }

  private class GroupContent1Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x43;

    private byte[] data = new byte[13];

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip two bytes
      pbais.skip(2);

      // Read the group
      int group = pbais.readByte();

      // Read the bit map
      pbais.read(data);

      // Forward
      listener.handleGroupContent1(group, data);
    }
  }

  private class GroupContent2Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x44;

    private byte[] data = new byte[13];

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip two bytes
      pbais.skip(2);

      // Read the group
      int group = pbais.readByte();

      // Read the bit map
      pbais.read(data);

      // Forward
      listener.handleGroupContent2(group, data);
    }
  }

  private class GroupContent3Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x45;

    private byte[] data = new byte[13];

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip two bytes
      pbais.skip(2);

      // Read the group
      int group = pbais.readByte();

      // Read the bit map
      pbais.read(data);

      // Forward
      listener.handleGroupContent3(group, data);
    }
  }

  private class GroupContent4Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x78;

    private byte[] data = new byte[13];

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip two bytes
      pbais.skip(2);

      // Read the group
      int group = pbais.readByte();

      // Read the bit map
      pbais.read(data);

      // Forward
      listener.handleGroupContent4(group, data);
    }
  }

  private class CdTextDetectedDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x47;

    private byte[] data = new byte[8];

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip two bytes
      pbais.skip(2);

      // Read the byte array
      pbais.read(data);

      // Forward
      listener.handleCdTextDetected(data);
    }
  }

  private class EnhancedDiscMemo1Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x48;
    
    private StringBuffer text = new StringBuffer();
    private byte flags[] = new byte[2];
    
    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      /* Read the remaining data */
      flags[0] = (byte)pbais.readByte();
      flags[1] = (byte)pbais.readByte();
      text.setLength(0); 
      for (int i = 0; i < 14; i++)
      {
        byte data = pbais.readByte();
        if (data != 0)
        {
          text.append((char)data);
        }
      }
      
      /* Dispatch */
      listener.handleEnhancedDiscMemo1(discId, flags, text.toString());
    }
  }
  
  private class EnhancedDiscMemo2Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x49;
    
    private StringBuffer text = new StringBuffer();
    
    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip control bytes and command byte
      pbais.skip(2);

      /* Read the remaining data */
      int part = (byte)pbais.readUnsignedByte();
      text.setLength(0); 
      for (int i = 0; i < 16; i++)
      {
        byte data = pbais.readByte();
        if (data != 0)
        {
          text.append((char)data);
        }
      }
      
      /* Dispatch */
      listener.handleEnhancedDiscMemo2(part, text.toString());
    }
  }

  private class CdTextTrackTitle1Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x4a;
    
    private StringBuffer text = new StringBuffer();
    private byte flags[] = new byte[2];
    
    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip Control Byte and Command Byte
      pbais.skip(2);

      /* Read the remaining data */
      int track = pbais.readBcdByte();
      flags[0] = (byte)pbais.readByte();
      flags[1] = (byte)pbais.readByte();
      text.setLength(0); 
      for (int i = 0; i < 14; i++)
      {
        byte data = pbais.readByte();
        if (data != 0)
        {
          text.append((char)data);
        }
      }
      
      /* Dispatch */
      listener.handleCdTextTrackTitle1(track, flags, text.toString());
    }
  }

  private class CdTextTrackTitle2Dispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x4b;
    
    private StringBuffer text = new StringBuffer();
    
    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip control bytes and command byte
      pbais.skip(2);

      /* Read the remaining data */
      int part = (byte)pbais.readUnsignedByte();
      text.setLength(0); 
      for (int i = 0; i < 16; i++)
      {
        byte data = pbais.readByte();
        if (data != 0)
        {
          text.append((char)data);
        }
      }
      
      /* Dispatch */
      listener.handleCdTextTrackTitle2(part, text.toString());
    }
  }

  private class PlayingTrackDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x50;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Read the track, minutes and seconds
      int track = pbais.readBcdByte();
      int minutes = pbais.readBcdByte();
      int seconds = pbais.readBcdByte();

      // Forward
      listener.handlePlayingTrack(discId, track, minutes, seconds);
    }
  }

  private class TrackPositionDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x51;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Skip some bytes
      pbais.skip(2);

      // Read the track, minutes and seconds
      int track = pbais.readBcdByte();
      int index = pbais.readBcdByte();
      int minutes = pbais.readBcdByte();
      int seconds = pbais.readBcdByte();

      // Forward
      listener.handleTrackPosition(track, index, minutes, seconds);
    }
  }

  private class DisplayingDiscDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x52;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Forward
      listener.handleDisplayingDisc(discId);
    }
  }

  private class LoadingDiscDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x54;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Forward
      listener.handleLoadingDisc(discId);
    }
  }

  private class LoadedDiscDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x58;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Forward
      listener.handleLoadedDisc(discId);
    }
  }

  private class PlayerTypeDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x61;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int capacity;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        capacity = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        capacity = pbais.readLowDiskId();
      }

      // Forward
      listener.handlePlayerType(capacity);
    }
  }

  private class PlayerModelDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x6a;

    private StringBuffer modelBuffer = new StringBuffer();

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the model string
      modelBuffer.setLength(0);
      for (int i = 0; i < 13; i++)
      {
        byte data = pbais.readByte();
        if (data != 0)
        {
          modelBuffer.append((char)data);
        }
      }

      // Forward
      listener.handlePlayerModel(modelBuffer.toString());
    }
  }

  private class PlayerStateDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x70;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read state byte
      int state = pbais.readUnsignedByte();

      // Read mode byte
      int mode = pbais.readUnsignedByte();

      // Skip a byte
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Read track
      int track = pbais.readBcdByte();

      // Forward
      listener.handlePlayerState(state, mode, discId, track);
    }
  }

  private class DiscIndDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x60;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Read indexes, tracks, minutes, seconds and frames
      int indexes = pbais.readBcdByte();
      int tracks = pbais.readBcdByte();
      int minutes = pbais.readBcdByte();
      int seconds = pbais.readBcdByte();
      int frames = pbais.readBcdByte();

      // Forward
      listener.handleDiscInfo(discId, indexes, tracks, minutes, seconds, frames);
    }
  }

  private class TrackInfoDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x62;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Read control byte
      int control = pbais.readUnsignedByte();
      pbais.skip(1);

      // Read the disc id based on the control flag
      int discId;
      if ((control & 0x01) == 1)
      {
        // Read high disc id
        discId = pbais.readHighDiskId();
      }
      else
      {
        // Read low disc id
        discId = pbais.readLowDiskId();
      }

      // Read indexes, tracks, minutes, seconds and frames
      int track = pbais.readBcdByte();
      int minutes = pbais.readBcdByte();
      int seconds = pbais.readBcdByte();

      // Forward
      listener.handleTrackInfo(discId, track, minutes, seconds);
    }
  }

  private class DoorClosedDispatch extends ProtocolDispatchHandlerAdaptor
  {
    public final static int TYPE = 0x83;

    public void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream pbais) throws IOException
    {
      // Forward
      listener.handleDoorClosed();
    }
  }

}
