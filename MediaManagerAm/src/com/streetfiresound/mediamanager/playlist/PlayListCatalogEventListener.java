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
 * $Id: PlayListCatalogEventListener.java,v 1.1 2005/02/22 03:41:17 stephen Exp $
 */
package com.streetfiresound.mediamanager.playlist;

import com.streetfiresound.mediamanager.mediacatalog.types.MLID;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
interface PlayListCatalogEventListener
{
  /**
   * Invocated when the PlayListCatalog contents have changed (i.e. new, 
   * remove, meta data updated).
   * @param hint The MLID of the play list which has changed or zero if the
   * catalog has changed
   */
  void playListCatalogChanged(MLID hint);
}
