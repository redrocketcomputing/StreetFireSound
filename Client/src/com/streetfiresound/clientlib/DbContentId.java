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
 *  @author iain huxley
 */
public class DbContentId implements ContentId
{
  Integer id;

  public DbContentId(int id)
  {
    this.id = new Integer(id);
  }

  public ContentId getRootEntryContentId()
  {
    // XXX:0:20050320iain: need to think about how this works for mp3s
    return new DbContentId(id.intValue());
  }

  public boolean equals(Object object)
  {
    return (object instanceof DbContentId) && ((DbContentId)object).id.equals(id);
  }

  public int hashCode()
  {
    return id.hashCode();
  }

  /**
   *  returns false if this item *may* be (not guaranteed) "expandable" e.g. if it corresponds to a disc
   */
  public boolean notExpandable()
  {
    System.out.println("XXX:000000:20050323: IAINFIX IAINFIX IAINFIX >>>> figure out what notexpandable means in the context of a db id");
    return false; // no idea
  }

  public int getPlayerChannel()
  {
    throw new ClientRuntimeException("attempting to get player channel from a non-MLID content item", null);
  }

  public int getPlayerSlot()
  {
    throw new ClientRuntimeException("attempting to get player slot from a non-MLID content item", null);
  }

  /**
   *  Get a compact, unique string representation of this ID that may be used in a filename
   */
  public String getCompactStringId()
  {
    return "DBID_" + id; // beat that
  }
}
