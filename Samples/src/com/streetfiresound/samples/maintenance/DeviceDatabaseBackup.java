/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 * 
 * Created on Sep 19, 2004 by stephen
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
 */
package com.streetfiresound.samples.maintenance;

import java.io.File;
import java.io.IOException;

import com.redrocketcomputing.rbx1600.maintenance.BackupMaintenanceClient;
import com.redrocketcomputing.rbx1600.maintenance.Rbx1600MaintenanceConstants;
import com.redrocketcomputing.util.simplefiletransfer.FileTransferListener;


/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DeviceDatabaseBackup
{
  private static void usage(String message)
  {
    // Display message is provided
    if (message != null)
    {
      System.out.println(message);
    }
    
    // Display usage
    System.out.println("DeviceDatabaseBackup <host address> <file>");
  }
  
  public static void main(String[] args) throws Exception
  {
    // Check argument count
    if (args.length < 2)
    {
      usage("bad arguments");
      System.exit(1);
    }
    
    // Create File object for path
    File backupFile = new File(args[1]);
    
    // Create backup client
    BackupMaintenanceClient client = new BackupMaintenanceClient(args[0], Rbx1600MaintenanceConstants.PORT + 46000);
    
    // Start the backup
    client.start(backupFile, new ProgressListener());
  }

  private static class ProgressListener implements FileTransferListener
  {
    public void progressNotification(int bytesTransferred, int totalBytes)
    {
      System.out.println("transferrered " + bytesTransferred + " of " + totalBytes + " bytes");
    }
  
    public void errorNotification(IOException e)
    {
      System.err.println("error: " + e);
    }
  }
}
