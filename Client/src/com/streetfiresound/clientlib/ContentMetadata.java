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
 * $Id $
 */
package com.streetfiresound.clientlib;

import org.havi.fcm.types.TimeCode;
import org.havi.system.types.DateTime;

import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;


/**
 *  @author iain huxley
 */
public class ContentMetadata
{
  /** string format for HTML (can be used as header row) */
  public static final String TO_HTML_STRING_FORMAT = "<tr class=\"columntitles\"><td>Player</td><td>Slot</td><td>Title</td><td>Artist</td><td>Genre</td><td>Time</td></tr>";

  private ContentId contentId;  // consider not storing this here, and having a Content class which pairs ContentId with ContentMetadata

  //XXX:0:20050320iain: later use hashmap and store arbitrary metadata, with provisions for finding out types (e.g. String or DateTime), id codes and descriptive names
	private String   title;
	private String   artist;
	private String   albumTitle;
	private String   albumArtist;
	private String   genre;
	private String   mediaType;  //XXX:0:20050321iain: move, this is not not a content metadata item
	private TimeCode playbackTime;
	private int      contentSize;
	private DateTime initialTimeStamp;
	private DateTime lastUpdateTimeStamp;
	private int      trackNumber;

	public ContentMetadata()
	{
    contentId           = null;
		title               = "";
		artist              = "";
		albumTitle          = "";
		albumArtist         = "";
		genre               = "";
		mediaType           = "";
		playbackTime        = new TimeCode();
		contentSize         = 0;
		initialTimeStamp    = new DateTime();
		lastUpdateTimeStamp = new DateTime();
    trackNumber         = 0;
	}

	public ContentMetadata(ContentId contentId, String title, String artist, String albumTitle, String albumArtist, String genre, String mediaType, TimeCode playbackTime, int contentSize, DateTime initialTimeStamp, DateTime lastUpdateTimeStamp, int trackNumber)
	{
		this.contentId           = contentId;
		this.title               = title;
		this.artist              = artist;
		this.albumTitle          = albumTitle;
		this.albumArtist         = albumArtist;
		this.genre               = genre;
		this.mediaType           = mediaType;
		this.playbackTime        = playbackTime;
		this.contentSize         = contentSize;
		this.initialTimeStamp    = initialTimeStamp;
		this.lastUpdateTimeStamp = lastUpdateTimeStamp;
		this.trackNumber         = trackNumber;
	}

	public ContentMetadata(MediaMetaData metaData)
	{
    this(metaData.getMediaLocationId() == null ? null : new MlidContentId(metaData.getMediaLocationId()), // new content id from mlid
         metaData.getTitle(),
         metaData.getArtist(),
         "",
         "",
         metaData.getGenre(),
         metaData.getMediaType(),
         metaData.getPlaybackTime(),
         metaData.getContentSize(),
         metaData.getInitialTimeStamp(),
         metaData.getLastUpdateTimeStamp(),
         metaData.getMediaLocationId().getIndex());

	}

  //XXX:0000000000000000000000:20050321iain: change formatting of accessors ahead for SS

  public String   getArtist()
  {
    if (artist == null || artist.equals("") || artist == Util.TOKEN_UNKNOWN && albumArtist != Util.TOKEN_UNKNOWN)
    {
      return albumArtist;
    }
    return artist;
  }

  public String   getTitle()                {return title;}

  public String   getGenre()                {return genre;}
  public String   getAlbumTitle()           {return albumTitle;}
  public String   getAlbumArtist()          {return albumArtist;}
  public String   getMediaType()            {return mediaType;}
  public TimeCode getPlaybackTime()         {return playbackTime;}
  public int      getContentSize()          {return contentSize;}
  public DateTime getInitialTimeStamp()     {return initialTimeStamp;}
  public DateTime getLastUpdateTimeStamp()  {return lastUpdateTimeStamp;}
  public int      getTrackNumber()          {return trackNumber;}

  public void setGenre(String                 genre)                 {this.genre               = genre;}
  public void setArtist(String                artist)                {this.artist              = artist;}
  public void setTitle(String                 title)                 {this.title               = title;}
  public void setAlbumTitle(String            title)                 {this.albumTitle          = title;}
  public void setAlbumArtist(String           artist)                {this.albumArtist         = artist;}
  public void setMediaType(String             mediaType)             {this.mediaType           = mediaType;}
  public void setPlaybackTime(TimeCode        playbackTime)          {this.playbackTime        = playbackTime;}
  public void setContentSize(int              contentSize)           {this.contentSize         = contentSize;}
  public void setInitialTimeStamp(DateTime    initialTimeStamp)      {this.initialTimeStamp    = initialTimeStamp;}
  public void setLastUpdateTimeStamp(DateTime lastUpdateTimeStamp)   {this.lastUpdateTimeStamp = lastUpdateTimeStamp;}
  public void setTrackNumber(int              trackNumber)           {this.trackNumber         = trackNumber;}


  public ContentId getContentId()
  {
    return contentId;
  }

  public void setContentId(ContentId contentId)
  {
    this.contentId = contentId;
  }

  /**
   * get disc info as HTML table row string
   * @param trClass the class attribute value to be used for the tr element
   */
  public String toHtmlString(String trClass)
  {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("<tr " + (trClass == null ? "" : "class=\"" + trClass + "\"") + ">");
    stringBuffer.append("<td x:num>" + getContentId().getPlayerChannel()   + "</td>");
    stringBuffer.append("<td x:num>" + getContentId().getPlayerSlot()    + "</td>");
    stringBuffer.append("<td>" + title    + "</td>");
    stringBuffer.append("<td>" + artist + "</td>");
    stringBuffer.append("<td>" + genre  + "</td>");
    stringBuffer.append("<td>" + Util.padStringWithZeros(String.valueOf(playbackTime.getHour()), 2, false) + ":" + Util.padStringWithZeros(String.valueOf(playbackTime.getMinute()), 2, false) + ":" + Util.padStringWithZeros(String.valueOf(playbackTime.getSec()), 2, false) + "</td>");
    return stringBuffer.toString();
  }

  public String toString()
  {
    return "ContentMetadata[contentId=" + contentId + ",type=" + mediaType + ",title=" + title + ",pbTime=" + playbackTime + ",lastUpdate=" + lastUpdateTimeStamp + "]";
  }
}
