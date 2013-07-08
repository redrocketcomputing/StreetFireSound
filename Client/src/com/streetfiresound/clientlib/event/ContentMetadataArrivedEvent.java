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
import com.streetfiresound.clientlib.Util;

/**
 *  Content Metadata arrived
 *  @author iain huxley
 */
public final class ContentMetadataArrivedEvent extends StreetFireEvent
{
  private int categoryType;
  private ContentMetadata[] metadata;

  public ContentMetadataArrivedEvent(int transactionId, ContentMetadata metadata[])
  {
    super(transactionId);

//     // check we've got at least one
//     assert metadata.length > 0;

    // keep it
    this.metadata = metadata;
  }

  public ContentMetadata[] getMetadata()
  {
    return metadata;
  }

  public String toString()
  {
    return "ContentMetadataArrivedEvent[tid=" + getTransactionId() + ", num=" + metadata.length + "]";
  }
}
