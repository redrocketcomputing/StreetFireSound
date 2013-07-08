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
 * $Id: ProtocolEventListener.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public interface ProtocolEventListener
{
	public void handleError();
  public void handleNoDisc();
  public void handleDuplicate();
  public void handleNotLoaded();
  public void handleNotAvailable();
  public void handleMissingDisc(int disc);
  public void handlePlaying();
  public void handleStopped();
  public void handlePaused();
  public void handleChangingDisc();
  public void handleReady();
  public void handleEotIn30();
  public void handleDoorOpened();
  public void handleDoorClosed();
  public void handlePowerOn();
  public void handlePowerOff();
  public void handleCdTextDetected(byte[] data);
  public void handlePlayingTrack(int disc, int track, int minutes, int seconds);
  public void handleTrackPosition(int track, int index, int minutes, int seconds);
  public void handleDisplayingDisc(int disc);
  public void handleLoadingDisc(int disc);
  public void handleLoadedDisc(int disc);
  public void handlePlayerType(int capacity);
  public void handlePlayerModel(String model);
  public void handlePlayerState(int state, int mode, int disc, int track);
  public void handleDiscInfo(int disc, int indexes, int tracks, int minutes, int seconds, int frames);
  public void handleTrackInfo(int disc, int track, int minutes, int seconds);
  public void handleMemoWritten();
  public void handleNoEnhancedMemo();
  public void handleGroupContent1(int group, byte flags[]);
  public void handleGroupContent2(int group, byte flags[]);
  public void handleGroupContent3(int group, byte flags[]);
  public void handleGroupContent4(int group, byte flags[]);
  public void handleEnhancedDiscMemo1(int discId, byte flags[], String text);
  public void handleEnhancedDiscMemo2(int part, String text);
  public void handleCdTextTrackTitle1(int track, byte flags[], String text);
  public void handleCdTextTrackTitle2(int part, String text);
};
