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
 * $Id: UpgradeMaintenanceRequestHandler.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import com.redrocketcomputing.havi.system.maintenance.MaintenanceException;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceIOException;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceInvalidFileException;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceRequestHandler;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.redrocketcomputing.util.simplefiletransfer.FileReceiver;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UpgradeMaintenanceRequestHandler implements MaintenanceRequestHandler
{
  private DataInputStream inputStream;
  private DataOutputStream outputStream;

  /**
   * Construct an BackupMaintenanceRequestHandler
   * @param inputStream The request input stream
   * @param outputStream The request output stream
   */
  public UpgradeMaintenanceRequestHandler(DataInputStream inputStream, DataOutputStream outputStream)
  {
    // Check the parameters
    if (inputStream == null || outputStream == null)
    {
      // Badness
      throw new IllegalArgumentException("inputStream or outputStream is null");
    }

    // Save the parameters
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.maintenance.MaintenanceRequestHandler#executeRequest()
   */
  public void executeRequest() throws MaintenanceException
  {
    try
    {
      // Log start of maintenance request
      LoggerSingleton.logInfo(this.getClass(), "executeRequest", "upgrading BSP");

      // Create file object for the restore file
      File restoreFile = new File("/upgrade.rbx1600");

      // Create file receiver
      FileReceiver fileReceiver = new FileReceiver(restoreFile, inputStream);

      // Receive the file
      fileReceiver.receive();

      // Check the upgrade file
      Process checkProcess = Runtime.getRuntime().exec("/sbin/check_upgrade /upgrade.rbx1600");
      checkProcess.waitFor();

      // Check completion code
      if (checkProcess.exitValue() != 0)
      {
        // Remove the file
        Runtime.getRuntime().exec("rm -f /upgrade.rbx1600");

        // We failed
        throw new MaintenanceInvalidFileException((byte)Rbx1600MaintenanceConstants.COMPLETION_RESPONSE_FAILED_UNKNOWN_REASON);
      }

      // Force a system reboot
      Runtime.getRuntime().exec("/sbin/restart");
    }
    catch (IOException e)
    {
      // Translate
      throw new MaintenanceIOException(e.toString());
    }
    catch (InterruptedException e)
    {
      throw new MaintenanceException(e.toString());
    }
  }
}
