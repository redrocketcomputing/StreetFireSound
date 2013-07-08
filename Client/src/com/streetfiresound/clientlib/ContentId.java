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

/**
 *  Interface for an id representing an item of content (e.g. a disc, disc track, mp3 file, etc.)
 *
 *  @author iain huxley
 */
public interface ContentId
{
  public ContentId getRootEntryContentId();

  public boolean equals(Object object);

  public int hashCode();

  /**
   *  returns false if this item *may* be (not guaranteed) "expandable" e.g. if it corresponds to a disc
   */
  public boolean notExpandable();

  //XXX:000000000000:20050321iain: HACK locations should be obtained via the location manager
  public int getPlayerSlot();
  public int getPlayerChannel();

  /**
   *  Get a compact, unique string representation of this ID that does not contain special characters
   *  (e.g. for use in a filename)
   */
  public String getCompactStringId();
}
