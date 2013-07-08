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
 * $Id: SplashScreen.java,v 1.4 2005/03/22 08:41:03 iain Exp $
 */

package com.streetfiresound.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

//import com.redrocketcomputing.rbx1600.gui.ConstHadesRelease;

/**
 * @author iain huxley
 */
public class SplashScreen extends JWindow
{
  int height, width;
  private ImageIcon splashImage;
  private JLabel status;

  public SplashScreen(Image image)
  {
    // create an image icon
    this.splashImage = new ImageIcon(image);

    // set the window to the size of the splash image
    setSize(this.splashImage.getIconWidth(), this.splashImage.getIconHeight());

    // wrap in label and add to the content pane
    JLabel backgroundLabel = new JLabel(this.splashImage);
    backgroundLabel.setOpaque(false);
    getContentPane().add(backgroundLabel, BorderLayout.CENTER);

    // put a border on the window
    backgroundLabel.setBorder(UISettings.PANEL_BORDER_RAISED);

    // create and add the status label
    status = new JLabel();//TextField("");
    status.setBounds(25, this.splashImage.getIconHeight()-25, this.splashImage.getIconWidth()-30, 20);
    status.setFont(new Font("Dialog", Font.PLAIN, 11));
    status.setForeground(Color.white);

    //XXX:00000000000000000:20050223iain: disable wait animated icon, now in splash image
    //    status.setIcon(new ImageIcon(ImageCache.getImageIcon("logo_wait.gif").getImage()));
    getLayeredPane().add(status, new Integer(1));

    // create and add the version label
    JLabel version = new JLabel("Media Catalog Test User Interface v0.1.0"); // + ConstHadesRelease.getRelease());
    version.setHorizontalAlignment(SwingConstants.CENTER);
    version.setFont(new Font("Dialog", Font.PLAIN, 11));
    version.setBounds(5, 332, this.splashImage.getIconWidth()-10, 25);
    version.setForeground(Color.white);
    getLayeredPane().add(version, new Integer(1));
  }

  /**
   * Modifies the status string
   * @param msg
   */
  public void setStatus(String msg)
  {
    this.status.setText(msg);
  }

  /**
   * sets the status text color
   * @param color
   */
  public void setStatusTextColor(Color color)
  {
    this.status.setForeground(color);
  }
}
