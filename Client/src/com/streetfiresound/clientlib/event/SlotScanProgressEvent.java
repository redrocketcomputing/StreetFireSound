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

import com.streetfiresound.clientlib.ContentMetadata;

/**
 * slot scan progress notification event
 */
public final class SlotScanProgressEvent extends StreetFireEvent
{
  private int slotsScanned;
  private int totalSlotsToScan;
  private ContentMetadata[] metadata;

  public SlotScanProgressEvent(int slotsScanned, int totalSlotsToScan, ContentMetadata metadata[])
  {
    super(NOT_APPLICABLE);
    this.slotsScanned     = slotsScanned;
    this.totalSlotsToScan = totalSlotsToScan;
    this.metadata         = metadata;
  }

  public int getSlotsScanned()
  {
    return slotsScanned;
  }

  public int getTotalSlotsToScan()
  {
    return totalSlotsToScan;
  }

  public ContentMetadata[] getMetadata()
  {
    return metadata;
  }

  public String toString()
  {
    return "SlotScanProgressEvent[metadata=" + metadata + "]";
  }
}
