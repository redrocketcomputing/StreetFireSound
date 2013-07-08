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
 * $Id: DisabledPlayModeMediator.java,v 1.1 2005/03/09 04:11:38 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import java.util.Observable;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstPlayMode;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerException;
import com.streetfiresound.mediamanager.mediaplayer.types.HaviMediaPlayerNotSupportedException;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayQueue;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DisabledPlayModeMediator extends PlayModeMediator
{

  /**
   * @param deviceAdaptorFactory
   * @throws HaviMediaPlayerException
   */
  public DisabledPlayModeMediator(DeviceAdaptorFactory deviceAdaptorFactory) throws HaviMediaPlayerException
  {
    // Construct super class
    super(deviceAdaptorFactory);
    
    // Log start
    LoggerSingleton.logInfo(this.getClass(), "DisabledPlayModeMediator", "loaded");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#play(int, int)
   */
  public void play(int version, int playIndex) throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("play");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#pause()
   */
  public void pause() throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("pause");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#resume()
   */
  public void resume() throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("resume");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#stop()
   */
  public void stop() throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("stop");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#skip(int, int)
   */
  public void skip(int direction, int count) throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("skip");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#getMode()
   */
  public int getMode() throws HaviMediaPlayerException
  {
    return ConstPlayMode.DISABLED;
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#cue(com.streetfiresound.mediamanager.mediacatalog.types.MLID[])
   */
  public int cue(MLID[] items) throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("cue");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#remove(int, int, int)
   */
  public int remove(int version, int start, int size) throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("remove");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#move(int, int, int, int)
   */
  public int move(int version, int direction, int start, int size) throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("move");
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.player.PlayModeMediator#getQueue()
   */
  public PlayQueue getQueue() throws HaviMediaPlayerException
  {
    // Not supported
    throw new HaviMediaPlayerNotSupportedException("getQueue");
  }

  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
  }
}
