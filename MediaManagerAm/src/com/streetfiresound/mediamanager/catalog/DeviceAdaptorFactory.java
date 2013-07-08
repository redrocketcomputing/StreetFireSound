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
 * $Id: DeviceAdaptorFactory.java,v 1.1 2005/02/27 22:57:22 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import org.havi.system.types.Attribute;
import org.havi.system.types.Query;
import org.havi.system.types.SEID;

import com.redrocketcomputing.havi.system.am.ApplicationModule;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
interface DeviceAdaptorFactory
{
  /**
   * Create DeviceAdaptor using the specified Attributes on the provied SEID
   * @param parent The ApplicationModule providing base services
   * @param seid The remote SEID of the device 
   * @param attributes The HAVI registry Attribute for the device
   * @return The new device Adaptor
   */
  public DeviceAdaptor create(ApplicationModule parent, SEID seid, Attribute[] attributes);

  /**
   * Return a array of registry ComplexQuery for which the factory can create DeviceAdaptors
   * @return The array queryies
   */
  public Query[] getQueries();
}
