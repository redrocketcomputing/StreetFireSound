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


import com.streetfiresound.clientlib.SlotScanner.SonyJukeboxInfo;


/**
 * slot scan initialization notification event
 */
public final class SlotScannerInitializedEvent extends StreetFireEvent
{
  private SonyJukeboxInfo[] sonyJukeboxes;

  public SlotScannerInitializedEvent(SonyJukeboxInfo[] sonyJukeboxes)
  {
    super(NOT_APPLICABLE);
    this.sonyJukeboxes = sonyJukeboxes;
  }

  public SonyJukeboxInfo[] getSonyJukeboxes()
  {
    return sonyJukeboxes;
  }

  public String toString()
  {
    return "SlotScannerInitializedEvent[SonyJukeboxes=" + sonyJukeboxes + "]";
  }
}
