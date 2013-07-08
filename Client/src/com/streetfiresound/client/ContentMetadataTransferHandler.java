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


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import com.streetfiresound.clientlib.Util;


/**
 *  Handles transfer of media metadata during drag and drop or cut and paste
 *  @author iain huxley
 */
public class ContentMetadataTransferHandler extends TransferHandler
{
  private ContentMetadataTablePanel contentMetadataTablePanel;
  private int[] rows = null;
  private int addIndex = -1;   // Location where items were added

  private boolean dropEnabled; // is drop turned on for this component?

  /**
   *  @param dropEnabled if true, the component for this handler will accept dropped media items
   */
  public ContentMetadataTransferHandler(ContentMetadataTablePanel panel, boolean dropEnabled)
  {
    this.dropEnabled = dropEnabled;
    contentMetadataTablePanel = panel;
  }

  public ContentMetadataTransferHandler(ContentMetadataTablePanel panel)
  {
    this(panel, true);
  }

  protected Transferable createTransferable(JComponent component)
  {
    // keep the indices (needed for cleanup)
    rows = contentMetadataTablePanel.getTable().getSelectedRows();

    // return a new transferable
    return new MediaItemsTransferable(contentMetadataTablePanel.getContentIds());
  }

  public int getSourceActions(JComponent c)
  {
    // XXX:0:20050112iain: assume that if drop is not enabled, table is immutable
    return dropEnabled ? COPY_OR_MOVE : COPY;
  }

  /**
   *  Invoked as part of the UI system when items are dropped or pasted
   */
  public boolean importData(JComponent c, Transferable t)
  {
    if (canImport(c, t.getTransferDataFlavors()))
    {
      try
      {
        List mediaItemList = (List)t.getTransferData(MediaItemsTransferable.flavor);

        JTable target = (JTable)c;
        if (target != contentMetadataTablePanel.getTable())
        {
          throw new IllegalArgumentException("mismatched transfer handler");
        }
        addIndex = target.getSelectedRow();
        ArrayList items = Util.contentMetadataCollectionToContentIdArrayList(contentMetadataTablePanel.getMetadata());
        items.addAll(addIndex, mediaItemList);
        contentMetadataTablePanel.updateModel(items);//XXX:0:20050111iain:

        return true;
      }
      catch (UnsupportedFlavorException ufe)
      {
      }
      catch (IOException ioe)
      {
      }
    }

    return false;
  }

  protected void exportDone(JComponent component, Transferable data, int action)
  {
    if (action == MOVE)
    {
      assert addIndex != -1;
      JTable source = (JTable)component;
      assert source == contentMetadataTablePanel.getTable();
      ArrayList items = contentMetadataTablePanel.getMetadata();

      // If we are moving items around in the same table, we
      // need to adjust the rows accordingly, since those
      // after the insertion point have moved.
      if (rows.length > 0)
      {
        for (int i = 0; i < rows.length; i++)
        {
          if (rows[i] > addIndex)
          {
            rows[i] += rows.length;
          }
        }
      }

      for (int i = rows.length - 1; i >= 0; i--)
      {
        items.remove(rows[i]);
      }
      contentMetadataTablePanel.updateModel(items);//XXX:0:20050111iain:
      //contentMetadataTablePanel.getTable().setRowSelectionInterval
    }
    rows = null;
    addIndex = -1;
  }

  public boolean canImport(JComponent c, DataFlavor[] flavors)
  {
    // if drop is not enabled, always return false
    if (!dropEnabled)
    {
      return false;
    }

    // loop over the flavors list in an attempt to find a matching flavor
    for (int i = 0; i < flavors.length; i++)
    {
      if (MediaItemsTransferable.flavor.equals(flavors[i]))
      {
        return true;
      }
    }
    return false;
  }


  /*********************************************************************************
   * STATIC INNER CLASS: transferable for media items
   *********************************************************************************/
  public static class MediaItemsTransferable implements Transferable
  {
    private List items;
    public static final DataFlavor flavor = new DataFlavor(List.class, "media items");

    public MediaItemsTransferable(List items)
    {
      this.items = items;
    }

    public Object getTransferData(DataFlavor flavor)
    {
      assert flavor.equals(flavor);
      return items;
    }

    public DataFlavor[] getTransferDataFlavors()
    {
      DataFlavor[] flavors = new DataFlavor[1];
      flavors[0] = flavor;
      return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
      return this.flavor.equals(flavor);
    }
  }
}
