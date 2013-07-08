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
 * $Id: ConfirmAndProgressDialog.java,v 1.1 2005/03/28 11:22:06 iain Exp $
 */

package com.streetfiresound.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;



/**
 * Simple progress dialog which can be used to provide confirmation
 * or other messages before and after an action
 */
public class ConfirmAndProgressDialog extends JDialog
{
  private JLabel       messageLine1;
  private JLabel       messageLine2;
  private JProgressBar progress;
  private JButton      okButton;
  private JButton      cancelButton;

  /**
   * Create a progress/confirmation dialog with an initial title/messages
   * after construction use getOKButton().setActionListener(...) etc. to hook
   * it up.
   */
  public ConfirmAndProgressDialog(String title, String initialMessageLine1, String initialMessageLine2)
  {
    super(AppFramework.instance.getAppFrame(), title);

    // set up a main panel filling the dialog
    Container main = getContentPane();
    main.setLayout(new BorderLayout());
    JPanel mainPanel = new JPanel();
    main.add(mainPanel, BorderLayout.CENTER);

    // layout the main panel
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    // add first message line
    messageLine1 = new JLabel(initialMessageLine1);
    messageLine1.setAlignmentX(0.5F);
    mainPanel.add(messageLine1);
    mainPanel.add(Box.createRigidArea(UISettings.STANDARD_SPACER_DIM));

    // add second message line
    messageLine2 = new JLabel(initialMessageLine2);
    messageLine2.setAlignmentX(0.5F);
    mainPanel.add(messageLine2);
    mainPanel.add(Box.createRigidArea(UISettings.STANDARD_SPACER_DIM));

    // add buttons
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setAlignmentX(0.5F);
    buttonPanel.setPreferredSize(new Dimension(360, 28));
    okButton = new JButton("OK"); //I18N
    okButton.setPreferredSize(UISettings.STANDARD_BUTTON_DIM_MEDIUM);
    okButton.setMaximumSize(UISettings.STANDARD_BUTTON_DIM_MEDIUM);
    buttonPanel.add(okButton);
    buttonPanel.add(Box.createRigidArea(UISettings.STANDARD_SPACER_DIM));
    cancelButton = new JButton("Cancel"); //I18N
    cancelButton.setPreferredSize(UISettings.STANDARD_BUTTON_DIM_MEDIUM);
    cancelButton.setMaximumSize(UISettings.STANDARD_BUTTON_DIM_MEDIUM);
    buttonPanel.add(cancelButton);
    mainPanel.add(buttonPanel);
    mainPanel.add(Box.createRigidArea(UISettings.STANDARD_SPACER_DIM));

    // add progress bar
    progress = new JProgressBar();
    mainPanel.add(progress);

    // fit the dialog to size
    pack();
  }

  public void setMessageLine1(String message)
  {
    messageLine1.setText(message);
  }

  public void setMessageLine2(String message)
  {
    messageLine2.setText(message);
  }

  public JButton getCancelButton()
  {
    return cancelButton;
  }

  public JButton getOKButton()
  {
    return okButton;
  }

  public JProgressBar getProgressBar()
  {
    return progress;
  }

  public void setProgressVisible(boolean visible)
  {
    progress.setVisible(visible);
  }
}
