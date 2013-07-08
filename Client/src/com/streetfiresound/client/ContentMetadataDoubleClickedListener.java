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
package com.streetfiresound.client;

import javax.swing.JComponent;

import com.streetfiresound.clientlib.ContentMetadata;


/**
 *  interface for those interested in double clicks on content metadata
 *  @author iain huxley
 */
public interface ContentMetadataDoubleClickedListener
{
  /**
   * called to indicate that a media summary was double clicked
   * @param index the index of the item clicked, or -1 if unknown or not applicable
   */
  public void contentMetadataDoubleClicked(JComponent source, ContentMetadata metadata, int index);
}
