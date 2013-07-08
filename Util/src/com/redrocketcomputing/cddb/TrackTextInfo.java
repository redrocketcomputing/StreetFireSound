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
 * $Id: TrackTextInfo.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

public class TrackTextInfo
{
  private String title = "";
  private String ext = "";
  private String artist = "";

  public TrackTextInfo(String title, String ext)
  {
    this.title = title;
    this.ext = ext;

    // Check for track artist
    if (title.indexOf(" / ") != -1)
    {
      artist = title.substring(0, title.indexOf(" / "));
      title = title.substring(title.indexOf(" / ") + 3);
    }
  }

  public TrackTextInfo(String title, String artist, String ext)
  {
    this.title = title;
    this.ext = ext;
    this.artist = artist;
  }

  public TrackTextInfo()
  {
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTitle()
  {
    return this.title;
  }
  
  public void setArtist(String artist)
  {
    this.artist = artist;
  }
  
  public String getArtist()
  {
    return this.artist;
  }

  public void setExt(String ext)
  {
    this.ext = ext;
  }

  public String getExt()
  {
    return this.ext;
  }

  public String toString()
  {
    return ("TrackTextInfo[title=" + this.title + ";ext=" + this.ext + "]");
  }
}
