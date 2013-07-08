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
public class JukeboxContentLocation implements ContentLocation
{
  MLID mlid;

  JukeboxContentLocation(MlidContentId mlidId)
  {
    this.mlid = mlidId.getMlid();
  }

  public boolean equals(Object object)
  {
    return (object instanceof MlidContentId) && ((MlidContentId)object).mlid.equals(mlid);
  }

  public int hashCode()
  {
    return mlid.hashCode();
  }

  public int getPlayerSlot()
  {
    return mlid.getList();
  }

  public int getPlayerChannel()
  {
    return mlid.getHuid().getTargetId().getN2();
  }

  public String toString()
  {
    return "JukeboxContentLocation[chan=" + getPlayerChannel() + ", slot=" + getPlayerSlot() + ", MLID=" + mlid + "]";
  }
}
