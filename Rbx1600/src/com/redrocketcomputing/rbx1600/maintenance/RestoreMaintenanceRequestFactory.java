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
 * $Id: RestoreMaintenanceRequestFactory.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.redrocketcomputing.havi.system.maintenance.MaintenanceHandshakeRequest;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceRequestFactory;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceRequestHandler;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RestoreMaintenanceRequestFactory extends MaintenanceRequestFactory
{
  /**
   * Construct a new RestoreMaintenanceRequestFactory
   */
  public RestoreMaintenanceRequestFactory()
  {
    super();
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.maintenance.MaintenanceRequestFactory#create(com.redrocketcomputing.havi.system.maintenance.MaintenanceHandshakeRequest, java.net.Socket)
   */
  public MaintenanceRequestHandler create(MaintenanceHandshakeRequest request, DataInputStream inputStream, DataOutputStream outputStream)
  {
    // Check request type
    if (request.getFeatureCode() == Rbx1600MaintenanceConstants.FEATURE_CODE_RESTORE)
    {
      // Create the handler
      return new RestoreMaintenanceRequestHandler(inputStream, outputStream);
    }

    // Not for us
    return null;
  }
}
