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
 * $Id: RebootDeviceMaintenanceClient.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.maintenance;

import java.io.File;

import org.havi.system.types.GUID;

import com.redrocketcomputing.havi.system.maintenance.MaintenanceClient;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceException;
import com.redrocketcomputing.util.simplefiletransfer.FileTransferListener;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RebootDeviceMaintenanceClient extends MaintenanceClient
{

  /**
   * Create and negotiate a reboot maintenance request
   * @param guid The remote GUID to connect to
   * @throws MaintenanceException Thrown if a problem is detected during
   * connection setup or handshake negotiations
   */
  public RebootDeviceMaintenanceClient(GUID guid) throws MaintenanceException
  {
    super(guid, (byte)Rbx1600MaintenanceConstants.FEATURE_CODE_REBOOT);
  }

  /**
   * Create and negotiate a reboot maintenance request
   * @param ipAddress The IP address of the remote maintenance server
   * @param ipPort The IP address of the remote maintenance server
   * @throws MaintenanceException Thrown if a problem is detected during
   * connection setup or handshake negotiations
   */
  public RebootDeviceMaintenanceClient(String ipAddress, int ipPort) throws MaintenanceException
  {
    super(ipAddress, ipPort, (byte)Rbx1600MaintenanceConstants.FEATURE_CODE_REBOOT);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.maintenance.MaintenanceClient#start(java.io.File, com.redrocketcomputing.util.simplefiletransfer.FileTransferListener)
   */
  public void start(File file, FileTransferListener listener)
  {
    startAsynchronousReceive(file, listener);
  }

}
