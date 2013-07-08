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
package com.streetfiresound.clientlib.event;

// import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.clientlib.ContentId;


/**
 * PlayList created event
 */
public final class PlayListCreatedEvent extends StreetFireEvent
{
  private ContentId newPlaylistId;

  public PlayListCreatedEvent(int transactionId, ContentId playlistId)
  {
    super(transactionId);
    this.newPlaylistId = playlistId;
  }

  public ContentId getNewPlaylistId()
  {
    return newPlaylistId;
  }

  public String toString()
  {
    return "PlaylistCreatedEvent[id=" + newPlaylistId + "]";
  }
}
