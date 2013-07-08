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
 * $Id: RebootDeviceMaintenanceRequestHandler.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceException;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceIOException;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceRequestHandler;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.redrocketcomputing.util.simplefiletransfer.FileSender;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RebootDeviceMaintenanceRequestHandler implements MaintenanceRequestHandler
{
  private DataInputStream inputStream;
  private DataOutputStream outputStream;

  /**
   * Construct an BackupMaintenanceRequestHandler
   * @param inputStream The request input stream
   * @param outputStream The request output stream
   */
  public RebootDeviceMaintenanceRequestHandler(DataInputStream inputStream, DataOutputStream outputStream)
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
      LoggerSingleton.logInfo(this.getClass(), "executeRequest", "rebooting device");
      
      // Create output print stream
      PrintStream deviceStateFile = new PrintStream(new FileOutputStream("/tmp/state.txt"));
      
      // Dump service manager state
      deviceStateFile.println("<<<< ServiceManager >>>>");
      String[] emptyArgs = new String[0];
      ServiceManager.getInstance().info(deviceStateFile, emptyArgs);
      
      // Dump state to file
      Service[] services = ServiceManager.getInstance().getAll();
      for (int i = 0; i < services.length; i++)
      {
        // Add seperator
        deviceStateFile.println("<<<< " + services[i].getInstanceName() + " >>>>");
        
        // Dump service information
        services[i].info(deviceStateFile, emptyArgs);
      }
      deviceStateFile.close();

      // Launch log dump process
      String[] dumpCommand = {"/bin/sh", "-c", "/sbin/logread >> /tmp/state.txt"};
      Process dumpProcess = Runtime.getRuntime().exec(dumpCommand);

      // Wait for dump to complete
      dumpProcess.waitFor();

      // Check completion code
      if (dumpProcess.exitValue() != 0)
      {
        // We failed
        throw new MaintenanceException("dumping device log failed");
      }

      // Launch log ifconfig process
      String[] ifconfigCommand = {"/bin/sh", "-c", "/sbin/ifconfig >> /tmp/state.txt"};
      Process ifconfigProcess = Runtime.getRuntime().exec(ifconfigCommand);

      // Wait for dump to complete
      ifconfigProcess.waitFor();

      // Check completion code
      if (ifconfigProcess.exitValue() != 0)
      {
        // We failed
        throw new MaintenanceException("dumping ifconfig failed");
      }

      // Create file object for the backup file
      File dumpFile = new File("/tmp/state.txt");

      // Create file sender
      FileSender fileSender = new FileSender(dumpFile, outputStream);

      // Send the file
      fileSender.send();

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
      // Translate
      throw new MaintenanceIOException(e.toString());
    }
  }
}
