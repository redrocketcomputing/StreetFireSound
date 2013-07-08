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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.streetfiresound.client.DiscEditPanel.EditDiscAction;
import com.streetfiresound.client.PlayListEditPanel.AddToPlayListAction;
import com.streetfiresound.client.PlayListEditPanel.EditPlayListAction;
import com.streetfiresound.client.PlayQueuePanel.AddToPlayQueueAction;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Dimension;

/**
 *  Catalog browser category summary table panel and button bar
 *  @author iain huxley
 */
public class CatalogBrowserMediaPanel extends StreetFirePanel implements ContentIdsSource
{
  static final int[] THUMB_SIZES = { 0,
                                     UISettings.ALBUM_THUMB_SIZE_SMALL,
                                     UISettings.ALBUM_THUMB_SIZE_MEDIUM,
                                     UISettings.ALBUM_THUMB_SIZE_LARGE,
                                     UISettings.ALBUM_THUMB_SIZE_HUGE   };
  private J2seClient client;

  private int sizeIndex = 0;
  private String debugName;

  private ContentMetadataTablePanel tablePanel; // table panel
  private JPanel borderedTablePanel;
  private JPanel buttonPanel;                 // holds action buttons

  /**
   *  Construct a category summary table panel
   *  @param debugName a name used only for debugging purposes (reported in toString())
   */
  public CatalogBrowserMediaPanel(CatalogBrowser catalogBrowser, String debugName)
  {
    client = catalogBrowser.getClient();
    this.debugName = debugName;

    // set up UI
    initUI(catalogBrowser);
  }

  public void initUI(CatalogBrowser catalogBrowser)
  {
    // set up table panel
    tablePanel = new ContentMetadataTablePanel(client, catalogBrowser, true, false, debugName); // allow sort, disallow drop
    tablePanel.setSorted(1); // sort by artist

    // set up the button panel
    buttonPanel = new StreetFirePanel();
    buttonPanel.setLayout(new FlowLayout());
    add(buttonPanel, BorderLayout.SOUTH);

    // need bordered table panel as want texture to show through border
    borderedTablePanel = new StreetFirePanel();
    borderedTablePanel.add(tablePanel, BorderLayout.CENTER);
    borderedTablePanel.setBorder(UISettings.PANEL_BORDER_LOWERED);
    add(borderedTablePanel, BorderLayout.CENTER);
  }

  /**
   *  get the next item
   */
  public ContentMetadata goToNextItem(ContentId currentItem)
  {
    return tablePanel.getNextItem(currentItem);
  }

  public void addToggleArtButton()
  {
    buttonPanel.add(new JButton(new ToggleArtAction()));
  }

  public void addAddToPlayQueueAction(AddToPlayQueueAction action)
  {
    action.setContentIdsSource(this);
    buttonPanel.add(new JButton(action));
  }

  public void addAddToPlayListAction(AddToPlayListAction action)
  {
    action.setContentIdsSource(this);
    buttonPanel.add(new JButton(action));
  }

  public void addEditPlayListAction(EditPlayListAction action)
  {
    action.setContentIdsSource(this);
    buttonPanel.add(new JButton(action));
  }

  public void addEditDiscAction(EditDiscAction action)
  {
    action.setContentIdsSource(this);
    buttonPanel.add(new JButton(action));
  }

  public List getContentIds()
  {
    return tablePanel.getContentIds();
  }

  public ContentMetadataTablePanel getTablePanel()
  {
    return tablePanel;
  }

  public String toString()
  {
    return "'" + debugName + "' [" + super.toString() + "]";
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class ToggleArtAction extends AbstractAction
  {
    public ToggleArtAction()
    {
      super("Art Size", ImageCache.getImageIcon("fullMode.gif"));
      putValue(SHORT_DESCRIPTION, "toggles album art display");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
    }

    public void actionPerformed(ActionEvent e)
    {
      if (++sizeIndex >= THUMB_SIZES.length)
      {
        sizeIndex = 0;
      }
      tablePanel.setAlbumArtSize(THUMB_SIZES[sizeIndex]);
    }
  }
}
