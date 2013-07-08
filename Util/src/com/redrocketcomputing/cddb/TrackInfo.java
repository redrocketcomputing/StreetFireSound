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
 * $Id: TrackInfo.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

public class TrackInfo
{
  int minutes = 0;
  int seconds = 0;
  int frames = 0;

  public TrackInfo()
  {
  }

  public TrackInfo(int minutes, int seconds, int frames)
  {
    this.minutes = minutes;
    this.seconds = seconds;
  }

  public int getMinutes()
  {
    return minutes;
  }

  public void setMinutes(int minutes)
  {
    this.minutes = minutes;
  }

  public int getSeconds()
  {
    return seconds;
  }

  public void setSeconds(int seconds)
  {
    this.seconds = seconds;
  }

  public int getFrames()
  {
    return frames;
  }

  public void setFrames(int frames)
  {
    this.frames = frames;
  }
}
