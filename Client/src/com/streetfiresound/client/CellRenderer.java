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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.MediaTracker;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import com.streetfiresound.clientlib.RequestDiscArtTask;
import com.streetfiresound.clientlib.ContentMetadata;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import javax.swing.JComponent;
//import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 *  Cell renderer, optional images/multiline, no focus painting.
 *  @author iain huxley
 */
public class CellRenderer extends DefaultTableCellRenderer implements ListCellRenderer
{
  private int        nowPlayingRow = -1;
  private int        albumArtSize = 0;
  private JLabel     label;
  private JPanel     panel;
  private JTextArea  textArea;
  private FlowLayout layout;

  // borders
  private Border     oddBorder;
  private Border     evenBorder;
  private Border     nowPlayingBorder;


  private int        horizontalAlignment;

  private Border     cellBorderUnselected; // border to show line betw cells
  private Border     cellBorderSelected;   // border to show line betw cells

  private JLabel     stringRenderer;

  /**
   *  Create a cell renderer
   *  NOTE: cell renderers can be shared across columns
   */
  public CellRenderer(int horizontalAlignment)
  {
    stringRenderer = new JLabel();

    label = new JLabel();
    panel = new JPanel();
    panel.setOpaque(true);
    panel.add(label);
    panel.setBorder(null);

    layout = new FlowLayout(FlowLayout.LEFT, UISettings.INSET_PANEL_BORDER_SIZE - 1, UISettings.INSET_PANEL_BORDER_SIZE - 1);
    panel.setLayout(layout);

    this.horizontalAlignment = horizontalAlignment;
    label.setHorizontalAlignment(horizontalAlignment);
    textArea = new JTextArea();

    //XXX:000000000000:20050208iain: to ui.properties
    nowPlayingBorder  = new TranslucentBevelBorder(TranslucentBevelBorder.BEVEL_TYPE_LOWERED_NO_SIDES, 1);//new TranslucentBevelBorder(BevelBorder.RAISED), new Color(55, 55, 55), new Color(10, 10, 10))
    oddBorder  = new ThinBevelBorder(BevelBorder.LOWERED, new Color(55, 55, 55), new Color(10, 10, 10));
    evenBorder = new ThinBevelBorder(BevelBorder.LOWERED, new Color(80, 80, 80), new Color(30, 30, 30));
    cellBorderUnselected = BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(17, 17, 17));
    cellBorderSelected   = BorderFactory.createMatteBorder(0, 0, 1, 0, UISettings.TABLE_LINE_BACKGROUND_SELECTED.darker());
  }

  /**
   *  set the album art size to use for this cell renderer (used when MediaMetaData objects are rendered)
   */
  public void setAlbumArtSize(int size)
  {
    albumArtSize = size;
  }

  /**
   *  set the now playing index, will be given special border
   */
  public void setNowPlayingIndex(int index)
  {
    nowPlayingRow = index;
    System.out.println("XXX:000000000000000000:iain:" + this + ":>>>>nowPlayingRow is '" + nowPlayingRow + "'");
  }

  public Component getListCellRendererComponent(JList list, Object item, int index, boolean isSelected, boolean hasFocus)
  {
    return getTableCellRendererComponent(null, item, isSelected, hasFocus, index, 0);
  }

  /**
   *  Return the component used to render the cell
   */
  public Component getTableCellRendererComponent(JTable table, Object item, boolean isSelected, boolean hasFocus, int row, int column)
  {
    Component colorTarget = null;
    boolean noBorder = false;

    if (item instanceof ContentMetadata)
    {
      colorTarget = panel;
      layout.setAlignment(FlowLayout.CENTER);
      if (albumArtSize > 0)
      {
        // figure out filename for this content id
        String thumbFilePath = RequestDiscArtTask.getThumbFilePath(((ContentMetadata)item).getContentId(), albumArtSize);

        // attempt to retreive an image
        ImageIcon icon = ImageCache.getImageIcon(thumbFilePath, false);
        if (icon != ImageCache.getUnknownImageIcon() && icon.getImageLoadStatus() == MediaTracker.COMPLETE)
        {
          label.setIcon(icon);
          label.setBorder(row % 2 == 0 ? evenBorder : oddBorder);
        }
        else
        {
          // no image ready/exists, use stand-in default
          label.setIcon(ImageCache.getImageIcon("cd_" + albumArtSize + ".png", true));
          label.setBorder(null);
        }
      }
      else
      {
        label.setIcon(null);
      }
      label.setText(null);
    }
    else if (item instanceof JLabel)
    {
      JLabel itemLabel = (JLabel) item;
      label.setIcon(itemLabel.getIcon());
      label.setText(itemLabel.getText());
      noBorder = true;
    }
    else
    {
      // Unknown class (almost always String), use parent, but strip cell focus
      //XXX:000:20050209iain: should just use our own component instead of counting on swing to use a JLabel
      Component c = super.getTableCellRendererComponent(table, item, isSelected, false, row, column);
      if (c instanceof JLabel)
      {
        JLabel l = (JLabel)c;
        l.setHorizontalAlignment(horizontalAlignment);
        l.setBorder(isSelected ? cellBorderSelected : cellBorderUnselected);

      }
        //XXX:0000000000000000000000000000:20050330iain:
        if (row == nowPlayingRow)
        {
          if (c instanceof JComponent)
          {
            ((JComponent)c).setBorder(nowPlayingBorder);
          }
        }

      return c;
    }

    // deal with selection indication via backgrounds
    if (isSelected)
    {
      panel.setBackground(UISettings.TABLE_LINE_BACKGROUND_SELECTED);
      panel.setForeground(UISettings.TABLE_LINE_FOREGROUND_SELECTED);
    }
    else
    {
      panel.setBackground(row % 2 == 1 ? UISettings.TABLE_LINE_BACKGROUND_ODD : UISettings.TABLE_LINE_BACKGROUND_EVEN);
      panel.setForeground(UISettings.TABLE_LINE_FOREGROUND);
    }

    System.out.println("XXX:000000000000000000:iain:>>>>row is '" + row + "'");
    System.out.println("XXX:000000000000000000:iain:>>>>"+ this + "nowPlayingRow is '" + nowPlayingRow + "'");
    if (row == nowPlayingRow)
    {
      panel.setBorder(nowPlayingBorder);
    }
    else if (!noBorder)
    {
      panel.setBorder(isSelected ? cellBorderSelected : cellBorderUnselected);
    }

    return panel;
  }
}
