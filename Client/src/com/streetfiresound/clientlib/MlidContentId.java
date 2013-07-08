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


import com.streetfiresound.mediamanager.mediacatalog.types.MLID;


/**
 *  @author iain huxley
 */
public class MlidContentId implements ContentId
{
  MLID mlid;

  public MlidContentId(MLID mlid)
  {
    assert mlid != null;
    this.mlid = mlid;
  }

  public ContentId getRootEntryContentId()
  {
    // return a content item id where the mlid has had index set to 0
    return new MlidContentId(new MLID(mlid.getHuid(), mlid.getList(), (short)0));
  }

  /**
   *  get the content id for a track.  may only be called on root (disc) ids
   */
  public ContentId getTrackContentId(int track)
  {
    assert mlid.getIndex() == 0;

    // return a content item id where the mlid has had index set to 0
    return new MlidContentId(new MLID(mlid.getHuid(), mlid.getList(), (short)track));
  }

  public boolean equals(Object object)
  {
    return (object instanceof MlidContentId) && ((MlidContentId)object).mlid.equals(mlid);
  }

  public int hashCode()
  {
    return mlid.hashCode();
  }

  /**
   *  returns false if this item *may* be (not guaranteed) "expandable" e.g. if it corresponds to a disc
   */
  public boolean notExpandable()
  {
    // if the index is non-zero it must [currently] be a track, so is not expandable
    return mlid.getIndex() != 0;
  }


  //XXX:000000000000:20050321iain: HACK locations should be obtained via the location manager
  public int getPlayerSlot()
  {
    return mlid.getList();
  }

  //XXX:000000000000:20050321iain: HACK locations should be obtained via the location manager
  public int getPlayerChannel()
  {
    return mlid.getHuid().getTargetId().getN2();
  }

  // XXX:0:20050320iain: only for use in dire emergencies
  public MLID getMlid()
  {
    return mlid;
  }

  public String toString()
  {
    return "MlidContentId[index=" + mlid.getIndex() + ",list=" + mlid.getList() + ",chan/N2=" + getPlayerChannel() + ",huid=" + mlid.getHuid() + "]";
  }

  /**
   *  Get a compact, unique string representation of this ID that may be used in a filename
   */
  public String getCompactStringId()
  {
    return "MLID_H" + mlid.getHuid().toString().substring(24) + "_L" + mlid.getList()  + "_I" + mlid.getIndex();
  }
}
