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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.streetfiresound.client.generatedui.DiscEditHeaderPanel;
import com.streetfiresound.clientlib.ClientRuntimeException;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.MediaCatalog;
import com.streetfiresound.clientlib.RequestDiscArtTask;
import com.streetfiresound.clientlib.Util;
import com.streetfiresound.clientlib.event.ContentIdsArrivedEvent;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.OperationSuccessfulEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import com.streetfiresound.clientlib.event.ThumbnailsReadyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import java.io.File;
import java.awt.Cursor;
import java.awt.GridLayout;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.BorderFactory;

/**
 *  Presents a UI for editing metadata for a disc and constituent tracks
 *
 *  @author iain huxley
 */
public class SettingsPanel extends StreetFirePanel
{
  private J2seClient      client;          // client object

  private JPanel buttonPanel; // holds action buttons
  private JPanel optionsPanel; // holds action buttons


  public SettingsPanel(J2seClient client)
  {
    // init members
    this.client = client;

    // set up the user interface
    initUI();
  }

  private JButton getButton(AbstractAction a)
  {
    JButton button = new JButton(a);
    button.setMaximumSize(new Dimension(160, 40));
    button.setMinimumSize(new Dimension(160, 40));
    button.setPreferredSize(new Dimension(160, 20));
    return button;
  }

  /**
   *  Set up the user interface
   */
  private void initUI()
  {
    //setBorder(new CompoundBorder(UISettings.PANEL_BORDER_LOWERED, BorderFactory.createEmptyBorder(3, 6, 6, 6)));

    optionsPanel = new StreetFirePanel();
    optionsPanel.setLayout(new GridLayout(2,1));
    optionsPanel.setBorder(new CompoundBorder(new TitledBorder("Options"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    addCheckboxOption(new JCheckBox(), "Show device selection panel");

//     //MaintenanceUI maintenanceUI = new MaintenanceUI();
    StreetFirePanel maintenancePanel = new StreetFirePanel();
    maintenancePanel.setBorder(new CompoundBorder(new TitledBorder("Maintenance Actions"), BorderFactory.createEmptyBorder(10, 140, 10, 10)));
    //maintenancePanel.setLayout(new GridLayout(6,1));
    maintenancePanel.setLayout(new BoxLayout(maintenancePanel, BoxLayout.Y_AXIS));
    maintenancePanel.add(getButton(new BackupAction()));
    maintenancePanel.add(getButton(new RebootAction()));
    maintenancePanel.add(getButton(new GetLogAction()));
    maintenancePanel.add(getButton(new UpgradeAction()));
    maintenancePanel.add(getButton(new RestoreAction()));
    maintenancePanel.add(getButton(new ExportAction()));
    maintenancePanel.setPreferredSize(new Dimension(100, 300));
    maintenancePanel.setMinimumSize(new Dimension(100, 300));

    add(maintenancePanel, BorderLayout.NORTH);

    // create the button panel
    buttonPanel = new StreetFirePanel();
    buttonPanel.setLayout(new FlowLayout());


    // set up save action
    SaveAction saveAction = new SaveAction();
    buttonPanel.add(new JButton(saveAction));

    // add the button panel and scroll pane
    add(buttonPanel, BorderLayout.SOUTH);

    add(optionsPanel, BorderLayout.CENTER);
  }

  private void addCheckboxOption(JCheckBox checkBox, String title)
  {
    StreetFirePanel panel = new StreetFirePanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(new JLabel(title));
    panel.add(checkBox);
    optionsPanel.add(panel);
  }

  /**
t   *  save the settings
   */
  public void save()
  {
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class SaveAction extends AbstractAction
  {
    public SaveAction()
    {
      super("Save Settings", ImageCache.getImageIcon("save.gif"));
      putValue(SHORT_DESCRIPTION, "Save changes to this playlist");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      save();
    }
  }

  /**
   * Perform the backup, called by button action
   */
  private void performBackupAction()
  {
    // set up the maintenance user interface object
    MaintenanceUI mui = new MaintenanceUI(client.getMediaManagerSeid().getGuid(), "rbx1600");

    // select the file for restore then display confirmation dialog
    mui.selectFileForBackup();
  }

  /**
   * Perform the restore, called by button action
   */
  private void performRestoreAction()
  {
    // set up the maintenance user interface object
    MaintenanceUI mui = new MaintenanceUI(client.getMediaManagerSeid().getGuid(), "rbx1600");

    // select the file for restore then display confirmation dialog
    mui.selectFileForRestore();
  }

  /**
   * Perform the restore, called by button action
   */
  private void performUpgradeAction()
  {
    // set up the maintenance user interface object
    MaintenanceUI mui = new MaintenanceUI(client.getMediaManagerSeid().getGuid(), "rbx1600");

    // select the file for restore then display confirmation dialog
    mui.selectFileForUpgrade();
  }

  private void performRebootAction()
  {
    // set up the maintenance user interface object
    MaintenanceUI mui = new MaintenanceUI(client.getMediaManagerSeid().getGuid(), "rbx1600");

    // select the file for restore then display confirmation dialog
    mui.selectFileForRebootLog();
  }

  private void performGetLogAction()
  {
    // set up the maintenance user interface object
    MaintenanceUI mui = new MaintenanceUI(client.getMediaManagerSeid().getGuid(), "rbx1600");

    // select the file for restore then display confirmation dialog
    mui.selectFileForLog();
  }

  /**
   *  display a file selection then export an HTML disc list to the selected file
   */
  public void performExportCDList()
  {
    // pick the file
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save CD list as" );
    chooser.setSelectedFile(new java.io.File("RBX1600_CDs.html"));

    // set the cursor to hourglass
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    // show the dialog (will block)
    int returnVal = chooser.showDialog(AppFramework.instance.getAppFrame(), "Select"); //I18N
    if (returnVal != JFileChooser.APPROVE_OPTION)
    {
      return;
    }

    File file = chooser.getSelectedFile();

    // save it!
    client.saveCDList(file);

    // reset the cursor
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    // display a success result
    JPanel messagePanel = new JPanel(new GridLayout(5, 1));
    messagePanel.add(new JLabel("CD list export successful.", SwingConstants.CENTER));
    messagePanel.add(new JLabel());
    messagePanel.add(new JLabel("The HTML file generated may be loaded into a spreadsheet application", SwingConstants.CENTER));
    messagePanel.add(new JLabel("(e.g. MS Excel) for tasks such as sorting by alternative columns or", SwingConstants.CENTER));
    messagePanel.add(new JLabel("conversion into another file format", SwingConstants.CENTER));
    client.showInfo("CD list export successful");
  }


  /*************************************************************************************
   * PRIVATE INNER CLASS: action for backup button press
   *************************************************************************************/
  private class ExportAction extends AbstractAction
  {
    public ExportAction()
    {
      super("Export CD list");
    }

    public void actionPerformed(ActionEvent event)
    {
      // perform the backup
      performExportCDList();
    }
  }

  /*************************************************************************************
   * PRIVATE INNER CLASS: action for backup button press
   *************************************************************************************/
  private class BackupAction extends AbstractAction
  {
    public BackupAction()
    {
      super("Backup");
    }

    public void actionPerformed(ActionEvent event)
    {
      // perform the backup
      performBackupAction();
    }
  }

  /*************************************************************************************
   * PRIVATE INNER CLASS: action for restore button press
   *************************************************************************************/
  private class RestoreAction extends AbstractAction
  {
    public RestoreAction()
    {
      super("Restore");
    }

    public void actionPerformed(ActionEvent event)
    {
      // perform the restore
      performRestoreAction();
    }
  }


  /*************************************************************************************
   * PRIVATE INNER CLASS: action for upgrade event
   *************************************************************************************/
  private class UpgradeAction extends AbstractAction
  {
    public UpgradeAction()
    {
      super("Upgrade");
    }

    public void actionPerformed(ActionEvent event)
    {
      // perform the upgrade
      performUpgradeAction();
    }
  }

  /*************************************************************************************
   * PRIVATE INNER CLASS: action for reboot button press
   *************************************************************************************/
  private class RebootAction extends AbstractAction
  {
    public RebootAction()
    {
      super("Reboot");
    }

    public void actionPerformed(ActionEvent event)
    {
      // perform the backup
      performRebootAction();
    }
  }

  /*************************************************************************************
   * PRIVATE INNER CLASS: action for get log button press
   *************************************************************************************/
  private class GetLogAction extends AbstractAction
  {
    public GetLogAction()
    {
      super("Get log");
    }

    public void actionPerformed(ActionEvent event)
    {
      // perform the backup
      performGetLogAction();
    }
  }
}
