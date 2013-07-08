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
 * $Id: DeviceAdaptorFactory.java,v 1.3 2005/03/03 01:08:21 stephen Exp $
 */
package com.streetfiresound.mediamanager.player;

import org.havi.dcm.types.HUID;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface DeviceAdaptorFactory
{
  /**
   * Create DeviceAdaptor using the specified Attributes on the provied SEID
   * @param huid The HUID of the DeviceAdaptor to create
   * @return The new device Adaptor
   */
  public DeviceAdaptor create(HUID huid);
  
  /**
   * Release all resources
   */
  public void close();

  /**
   * Flush the internal cache and close all adaptors
   */
  public void flush();
}

