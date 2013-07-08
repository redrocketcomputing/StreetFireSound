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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.RemoteMediaPlayer;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.MlidContentId;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.PlayPositionUpdateEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstSkipDirection;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstTransportState;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayPosition;
import java.util.List;
import javax.swing.BorderFactory;
import java.awt.Color;



/**
 *  window controls (minimize, maximize, quitx
 *
 *  @author iain huxley
 */
public class WindowFunctionsPanel extends StreetFirePanel
{
  public static int SIZE     = 26;

  private int spacing = 0;

  /**
   * @param verticalLayout if true, buttons will be laid out in a column, otherwise in a row
   */
  public WindowFunctionsPanel(boolean verticalLayout, int spacing)
  {
    this.spacing = spacing;

    // manual layout
    setLayout(null);

    // create and add the buttons
    JButton quitButton     = new JButton(new QuitAction());
    JButton minimizeButton = new JButton(new MinimizeAction());
    JButton maximizeButton = new JButton(new MaximizeAction());
    add(quitButton);
    add(minimizeButton);
    add(maximizeButton);

    // figure out increments
    int xPosIncr = verticalLayout ? 0 : SIZE + spacing;
    int yPosIncr = verticalLayout ? SIZE + spacing : 0;
    int xPos = 0;
    int yPos = 0;

    // set our min/pref size
    Dimension size = new Dimension(SIZE + spacing*2 + xPosIncr * 2, SIZE + spacing*2 + yPosIncr * 2);
    setMinimumSize(size);
    setPreferredSize(size);

    JButton[] orderedButtons = new JButton[3];

    orderedButtons[verticalLayout ? 0 : 2] = quitButton;
    orderedButtons[1] = maximizeButton;
    orderedButtons[verticalLayout ? 2 : 0] = minimizeButton;

    // lay out
    for (int i=0; i<orderedButtons.length; i++)
    {
      orderedButtons[i].setBounds(xPos, yPos, SIZE, SIZE);
      xPos += xPosIncr;
      yPos += yPosIncr;
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class QuitAction extends AbstractAction
  {
    public QuitAction()
    {
      super("", ImageCache.getImageIcon("quit.png"));
      putValue(SHORT_DESCRIPTION, "Quit the application");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      AppFramework.instance.quit();
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class MinimizeAction extends AbstractAction
  {
    public MinimizeAction()
    {
      super("", ImageCache.getImageIcon("minimize.png"));
      putValue(SHORT_DESCRIPTION, "Minimize the application");
    }

    public void actionPerformed(ActionEvent e)
    {
      AppFramework.instance.minimize();
    }
  }


  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class MaximizeAction extends AbstractAction
  {
    public MaximizeAction()
    {
      super("", ImageCache.getImageIcon("maximize.png"));
      putValue(SHORT_DESCRIPTION, "Maximize the application");
    }

    public void actionPerformed(ActionEvent e)
    {
      AppFramework.instance.maximize();
    }
  }
}
