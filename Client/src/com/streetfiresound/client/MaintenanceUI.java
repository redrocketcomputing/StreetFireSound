/*
2 * Copyright (C) 2004 by StreetFire Sound Labs
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
 * $Id: MaintenanceUI.java,v 1.1 2005/03/28 16:37:03 iain Exp $
 */

package com.streetfiresound.client;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

import org.havi.system.types.GUID;

import com.redrocketcomputing.havi.system.maintenance.MaintenanceClient;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceCompletionNotification;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceConstants;
import com.redrocketcomputing.havi.system.maintenance.MaintenanceException;
import com.redrocketcomputing.rbx1600.maintenance.BackupMaintenanceClient;
import com.redrocketcomputing.rbx1600.maintenance.RestoreMaintenanceClient;
import com.redrocketcomputing.rbx1600.maintenance.UpgradeMaintenanceClient;
import com.redrocketcomputing.rbx1600.maintenance.GetDeviceLogMaintenanceClient;
import com.redrocketcomputing.rbx1600.maintenance.RebootDeviceMaintenanceClient;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.redrocketcomputing.util.simplefiletransfer.FileTransferListener;

/**
 *  UI helper for device maintenance
 */
public class MaintenanceUI implements FileTransferListener
{
  private GUID      guid;                      // guid to perform maintenance on
  private String    userPreferredName;         // name of device
  private File      selectedFile = null;       // file for read or write depending on action
  private String    currentAction;             // string describing the current action for display (e.g. "backup", "restore", "upgrade")
  private boolean   showRebootWarning = false; // if true the user will be warned of a reboot

  private MaintenanceClient maintenanceClient = null; // maintenance client helper
  private ConfirmAndProgressDialog progressDialog = null;  // transfer progress display, only initialized once transfer has begun

  /**
   *  Create an object for managing the UI for device maintenance (backup, restore, upgrade)
   *
   *  @param featureCode the maintenance feature to be provided
   *  @see com.redrocketcomputing.rbx1600.maintenance.MaintenanceConstants
   */
  public MaintenanceUI(GUID guid, String userPreferredName)
  {
    // init members
    this.guid = guid;
    this.userPreferredName = userPreferredName;
  }

  //------------------------------------------------------------------------------------------
  // Backup
  //------------------------------------------------------------------------------------------

  /**
   *  Prompts the user to select a file for restore, then displays a confirmation dialog
   *  Prompt the user to select a file, then transfer/save the backup file while displaying
   *  a progress dialog
   *  Will return as soon as the file has been selected by the user
   */
  public void selectFileForBackup()
  {
    // set descriptive string
    currentAction = "backup";

    // display a file selection dialog, will block until selected
    selectedFile = displayFileSelect(true, "Select file to save backup of " + userPreferredName, new BackupFileFilter(), userPreferredName + "-backup-" + Util.getDateString() + ".sfb");

    // check for cancellation
    if (selectedFile == null)
    {
      return;
    }

    // init progress/confirmation dialog
    progressDialog = new ConfirmAndProgressDialog("Save Backup", "About to back up to file '" + selectedFile + "'", ""); //I18N
    progressDialog.setModal(true);
    Util.moveToCenter(progressDialog, AppFramework.instance.getAppFrame());

    // set initial button actions
    progressDialog.getOKButton().addActionListener(new PerformBackupAction());
    progressDialog.getCancelButton().addActionListener(new CloseDialogAction());

    // show the progress/confirmation dialog, when the user confirms the process will be continued (see
    progressDialog.setVisible(true);
  }

  /**
   *  Called to actually perform the backup after the user has selected a file and confirmed their selection
   */
  private void performBackupAction()
  {
    // update text
    progressDialog.setMessageLine1("Backing up now...");
    progressDialog.setMessageLine2("Please do not unplug your device or interrupt the transfer");

    // set progress bar indeterminate, don't yet know size
    progressDialog.getProgressBar().setIndeterminate(true);

    // show hourglass cursor
    progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    // can't cancel when in progress, disable actions
    progressDialog.getOKButton().setEnabled(false);
    progressDialog.getCancelButton().setEnabled(false);

    // create the client, will connect and handshake
    BackupMaintenanceClient backupClient;
    try
    {
      backupClient = new BackupMaintenanceClient(guid);
    }
    catch (MaintenanceException e)
    {
      // put up an error dialog
      handleError(e);
      return;
    }

    // keep the client for receiving the completion notification
    maintenanceClient = backupClient;

    // start the transfer, will be notified via TransferListener interface
    backupClient.start(selectedFile, this);
  }

  //------------------------------------------------------------------------------------------
  // Restore
  //------------------------------------------------------------------------------------------

  /**
   *  Prompts the user to select a file for restore, then displays a confirmation dialog
   */
  public void selectFileForRestore()
  {
    // set descriptive string
    currentAction = "restore";

    // will reboot, set flag so warning is shown
    showRebootWarning = true;

    // display a file selection dialog, will block until selected
    selectedFile = displayFileSelect(false, "Select file for restore of " + userPreferredName, new BackupFileFilter(), null);

    // check for cancellation
    if (selectedFile == null)
    {
      return;
    }

    // init progress/confirmation dialog
    progressDialog = new ConfirmAndProgressDialog("Restore From Backup",  "About to restore from file '" + selectedFile + "'", "This will cause a device reset - click OK to confirm."); //I18N
    Util.moveToCenter(progressDialog, AppFramework.instance.getAppFrame());

    // set initial button actions
    progressDialog.getOKButton().addActionListener(new PerformRestoreAction());
    progressDialog.getCancelButton().addActionListener(new CloseDialogAction());

    // show the progress/confirmation dialog, when the user confirms the process will be continued (see
    progressDialog.setVisible(true);
  }

  /**
   *  Called to actually perform the backup after the user has selected a file and confirmed their selection
   */
  private void performRestoreAction()
  {
    // update text
    progressDialog.setMessageLine1("Restoring now...");
    progressDialog.setMessageLine2("Please do not unplug your device or interrupt the transfer");

    // show hourglass cursor
    progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    // set progress bar indeterminate, don't yet know size
    progressDialog.getProgressBar().setIndeterminate(true);

    // can't cancel when in progress, disable actions
    progressDialog.getOKButton().setEnabled(false);
    progressDialog.getCancelButton().setEnabled(false);

    // create the client, will connect and handshake
    RestoreMaintenanceClient restoreClient;
    try
    {
      restoreClient = new RestoreMaintenanceClient(guid);
    }
    catch (MaintenanceException e)
    {
      handleError(e);
      return;
    }

    // keep the client for receiving the completion notification
    maintenanceClient = restoreClient;

    // start the transfer, will be notified via TransferListener interface
    restoreClient.start(selectedFile, this);
  }

  //------------------------------------------------------------------------------------------
  // Upgrade
  //------------------------------------------------------------------------------------------

  /**
   *  Prompts the user to select a file for update, then displays a confirmation dialog
   */
  public void selectFileForUpgrade()
  {
    // set descriptive string
    currentAction = "upgrade";

    // display a file selection dialog, will block until selected
    selectedFile = displayFileSelect(false, "Select file for upgrade of " + userPreferredName, new UpgradeFileFilter(), null);

    // will reboot, set flag so warning is shown
    showRebootWarning = true;

    // check for cancellation
    if (selectedFile == null)
    {
      return;
    }

    // init progress/confirmation dialog
    progressDialog = new ConfirmAndProgressDialog("Upgrade",  "About to upgrade using jar file '" + selectedFile + "'", "This will cause a device reset - click OK to confirm."); //I18N
    Util.moveToCenter(progressDialog, AppFramework.instance.getAppFrame());

    // set initial button actions
    progressDialog.getOKButton().addActionListener(new PerformUpgradeAction());
    progressDialog.getCancelButton().addActionListener(new CloseDialogAction());

    // show the progress/confirmation dialog, when the user confirms the process will be continued (see
    progressDialog.setVisible(true);
  }

  /**
   *  Called to actually perform the backup after the user has selected a file and confirmed their selection
   */
  private void performUpgradeAction()
  {
    // update text
    progressDialog.setMessageLine1("Upgrading now...");
    progressDialog.setMessageLine2("IMPORTANT: do NOT unplug your device or interrupt the transfer");

    // set progress bar indeterminate, don't yet know size
    progressDialog.getProgressBar().setIndeterminate(true);

    // show hourglass cursor
    progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    // can't cancel when in progress, disable actions
    progressDialog.getOKButton().setEnabled(false);
    progressDialog.getCancelButton().setEnabled(false);

    // create the client, will connect and handshake
    UpgradeMaintenanceClient upgradeClient;
    try
    {
      upgradeClient = new UpgradeMaintenanceClient(guid);
    }
    catch (MaintenanceException e)
    {
      handleError(e);
      return;
    }

    // keep the client for receiving the completion notification
    maintenanceClient = upgradeClient;

    // start the transfer, will be notified via TransferListener interface
    upgradeClient.start(selectedFile, this);
  }

  //------------------------------------------------------------------------------------------
  // Reboot and get Log (combined action)
  //------------------------------------------------------------------------------------------

  /**
   *  Prompts the user to select a file to save a log and then reboot and displays a confirmation dialog
   */
  public void selectFileForRebootLog()
  {
    // set descriptive string
    currentAction = "log transfer";

    // will reboot, set flag so warning is shown
    showRebootWarning = true;

    // display a file selection dialog, will block until selected
    selectedFile = displayFileSelect(true, "Select file to save pre-reboot log for " + userPreferredName, new LogFileFilter(), userPreferredName + "-" + Util.getDateString() + ".log");

    // check for cancellation
    if (selectedFile == null)
    {
      return;
    }

    // init progress/confirmation dialog
    progressDialog = new ConfirmAndProgressDialog("Reboot",  "About to reboot and save log to '" + selectedFile + "'", "Click OK to confirm."); //I18N
    Util.moveToCenter(progressDialog, AppFramework.instance.getAppFrame());

    // set initial button actions
    progressDialog.getOKButton().addActionListener(new PerformRebootAction());
    progressDialog.getCancelButton().addActionListener(new CloseDialogAction());

    // show the progress/confirmation dialog, when the user confirms the process will be continued (see
    progressDialog.setVisible(true);
  }

  /**
   *  Called to actually perform the backup after the user has selected a file and confirmed their selection
   */
  private void performRebootAction()
  {
    // update text
    progressDialog.setMessageLine1("Rebooting now...");
    progressDialog.setMessageLine2("");

    // show hourglass cursor
    progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    // set progress bar indeterminate, don't yet know size
    progressDialog.getProgressBar().setIndeterminate(true);

    // can't cancel when in progress, disable actions
    progressDialog.getOKButton().setEnabled(false);
    progressDialog.getCancelButton().setEnabled(false);

    // create the client, will connect and handshake
    RebootDeviceMaintenanceClient rebootClient;
    try
    {
      rebootClient = new RebootDeviceMaintenanceClient(guid);
    }
    catch (MaintenanceException e)
    {
      handleError(e);
      return;
    }

    // keep the client for receiving the completion notification
    maintenanceClient = rebootClient;

    // start the transfer, will be notified via TransferListener interface
    rebootClient.start(selectedFile, this);
  }

  //------------------------------------------------------------------------------------------
  // Get Log
  //------------------------------------------------------------------------------------------

  /**
   *  Prompts the user to select a file to save a log and then reboot and displays a confirmation dialog
   */
  public void selectFileForLog()
  {
    // set descriptive string
    currentAction = "log transfer";

    // display a file selection dialog, will block until selected
    selectedFile = displayFileSelect(true, "Save log for " + userPreferredName, new LogFileFilter(), userPreferredName + "-" + Util.getDateString() + ".log");

    // check for cancellation
    if (selectedFile == null)
    {
      return;
    }

    // init progress/confirmation dialog
    progressDialog = new ConfirmAndProgressDialog("Get log",  "About save log to '" + selectedFile + "'", "Click OK to confirm."); //I18N
    Util.moveToCenter(progressDialog, AppFramework.instance.getAppFrame());

    // set initial button actions
    progressDialog.getOKButton().addActionListener(new PerformGetLogAction());
    progressDialog.getCancelButton().addActionListener(new CloseDialogAction());

    // show the progress/confirmation dialog, when the user confirms the process will be continued (see
    progressDialog.setVisible(true);
  }

  /**
   *  Called to actually perform the backup after the user has selected a file and confirmed their selection
   */
  private void performGetLogAction()
  {
    // update text
    progressDialog.setMessageLine1("Fetching log...");
    progressDialog.setMessageLine2("");

    // show hourglass cursor
    progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    // set progress bar indeterminate, don't yet know size
    progressDialog.getProgressBar().setIndeterminate(true);

    // can't cancel when in progress, disable actions
    progressDialog.getOKButton().setEnabled(false);
    progressDialog.getCancelButton().setEnabled(false);

    // create the client, will connect and handshake
    GetDeviceLogMaintenanceClient getLogClient;
    try
    {
      getLogClient = new GetDeviceLogMaintenanceClient(guid);
    }
    catch (MaintenanceException e)
    {
      handleError(e);
      return;
    }

    // keep the client for receiving the completion notification
    maintenanceClient = getLogClient;

    // start the transfer, will be notified via TransferListener interface
    getLogClient.start(selectedFile, this);
  }


  //------------------------------------------------------------------------------------------
  // General
  //------------------------------------------------------------------------------------------

  /**
   * display a file selection dialog, wait for selection
   * @param forSave
   * @param defaultFilename default file name to use for save
   * @return the file, or null if cancelled
   */
  private File displayFileSelect(boolean forSave, String title, FileFilter filter, String defaultFilename)
  {
    // configure a file chooser
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(filter);
    chooser.setDialogTitle(title);
    if (forSave)
    {
      chooser.setSelectedFile(new java.io.File(defaultFilename));
    }

    // show the dialog (will block)
    int returnVal = chooser.showDialog(AppFramework.instance.getAppFrame(), "Select"); //I18N
    if (returnVal != JFileChooser.APPROVE_OPTION)
    {
      return null;
    }

    // return the file that the user selected
    return chooser.getSelectedFile();
  }

  /**
   * @param actionName the maintenance action taking place (backup/restore/upgrade) for use in the error string
   */
  private void handleError(Throwable error)
  {
    LoggerSingleton.logError(MaintenanceUI.class, "handleError", "error occurred:\n" + Util.getStackTrace(error));


    // reset the fields
    progressDialog.setMessageLine1("An error occurred, " + currentAction + " did not complete:"); //I18N
    progressDialog.setMessageLine2(error.toString());

    // change button state so only OK is active (closes)
    JButton ok = progressDialog.getOKButton();
    ok.removeActionListener(ok.getActionListeners()[0]); //XXX:0:20040921iain: only ever one listener
    ok.addActionListener(new CloseDialogAction());
    ok.setEnabled(true);
    progressDialog.getCancelButton().setEnabled(false);

    // back to regular cursor
    progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    // log
    LoggerSingleton.logError(this.getClass(), "handleError", error.toString());
  }

  /**
   *  FileTransferListener implementation - Called to notify a listener of progress during file transfer
   */
  public void progressNotification(int bytesTransferred, int totalBytes)
  {
    // progress bar will initially be indeterminate, now we know the size, set maximum
    JProgressBar progress = progressDialog.getProgressBar();
    if (progress.isIndeterminate())
    {
      progress.setIndeterminate(false);
      progress.setMaximum(totalBytes);
    }

    // set the progress
    progress.setValue(bytesTransferred);

    // if bytes transferred == total bytes we're done
    if (bytesTransferred == totalBytes)
    {
      // receive the completion notification
      MaintenanceCompletionNotification notification;
      try
      {
        notification = maintenanceClient.receiveCompletionNotification();
      }
      catch (IOException e)
      {
        handleError(e);
        return;
      }

      // check the response, perhaps the file was no good.
      if (notification.getResponseCode() != MaintenanceConstants.COMPLETION_RESPONSE_SUCCESS)
      {
        handleError(new RuntimeException("Unknown error")); //XXX:0000:20040922iain: improve message if detail available
        return;
      }

      // display warning about device restart
      progressDialog.setMessageLine1("Your " + currentAction + " has completed successfully");
      progressDialog.setMessageLine2(showRebootWarning ? "The RBX1600 will now restart, its icon will reappear momentarily" : ""); //I18N

      // reenable OK button with close action
      JButton ok = progressDialog.getOKButton();
      ok.setEnabled(true);
      ok.removeActionListener(ok.getActionListeners()[0]); //XXX:0:20040921iain: only ever one listener
      ok.addActionListener(new CloseDialogAction());

      // remove progress bar
      progressDialog.setProgressVisible(false);
      progressDialog.invalidate();

      // back to regular cursor
      progressDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  /**
   *  FileTransferListener implementation - called to notify a listener of an error during file transfer
   */
  public void errorNotification(IOException e)
  {
    handleError(e);
  }

  /************************************************************************************
   * PRIVATE INNER CLASS: File selection mask for backup files
   ************************************************************************************/
  private class BackupFileFilter extends FileFilter
  {
    public String getDescription()
    {
      return "Street Fire Backup File (.sfb)"; //I18N
    }

    public boolean accept(java.io.File f)
    {
      // show directories or files with our extension
      return f.getName().toLowerCase().endsWith(".sfb") || f.isDirectory();
    }
  };

  /************************************************************************************
   * PRIVATE INNER CLASS: File selection mask for backup files
   ************************************************************************************/
  private class LogFileFilter extends FileFilter
  {
    public String getDescription()
    {
      return "Log File (.log)"; //I18N
    }

    public boolean accept(java.io.File f)
    {
      // show directories or files with our extension
      return f.getName().toLowerCase().endsWith(".log") || f.isDirectory();
    }
  };

  /************************************************************************************
   * PRIVATE INNER CLASS: File selection mask for backup files
   ************************************************************************************/
  private class UpgradeFileFilter extends FileFilter
  {
    public String getDescription()
    {
      return "Street Fire RBX1600 software update File (.rbx1600)"; //I18N
    }

    public boolean accept(java.io.File f)
    {
      // show directories or files with our extension
      return f.getName().toLowerCase().endsWith(".rbx1600") || f.isDirectory();
    }
  };

  /************************************************************************************
   * PRIVATE INNER CLASS: action to perform the backup after confirmation
   ************************************************************************************/
  private class PerformBackupAction implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      performBackupAction();
    }
  }

  /************************************************************************************
   * PRIVATE INNER CLASS: action to perform the restore after confirmation
   ************************************************************************************/
  private class PerformRestoreAction implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      performRestoreAction();
    }
  }

  /************************************************************************************
   * PRIVATE INNER CLASS: action to perform the upgrade after confirmation
   ************************************************************************************/
  private class PerformUpgradeAction implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      performUpgradeAction();
    }
  }


  /************************************************************************************
   * PRIVATE INNER CLASS: action to perform the upgrade after confirmation
   ************************************************************************************/
  private class PerformRebootAction implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      performRebootAction();
    }
  }

  /************************************************************************************
   * PRIVATE INNER CLASS: action to perform the upgrade after confirmation
   ************************************************************************************/
  private class PerformGetLogAction implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      performGetLogAction();
    }
  }

  /************************************************************************************
   * PRIVATE INNER CLASS: action to close the dialog on cancel or completion confirm
   ************************************************************************************/
  private class CloseDialogAction implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      progressDialog.setVisible(false);
    }
  }
}

