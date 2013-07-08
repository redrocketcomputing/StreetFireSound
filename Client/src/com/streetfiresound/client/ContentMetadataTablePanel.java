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


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.havi.system.types.DateTime;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;

/**
 *  media summary table panel - used for media by type/artist/genre etc.
 *  @author iain huxley
 */
public class ContentMetadataTablePanel extends AbstractTablePanel implements ContentIdsSource
{
  private J2seClient client;

  private int categoryType; // the category of media summaries (by artist, genre etc.)
  private ContentMetadataTableModel contentMetadataTableModel;
  private ContentMetadataDoubleClickedListener contentMetadataDoubleClickedListener;

  private int albumArtSize; // album art thumbnail size (0 = no display)

  private CellRenderer albumArtRenderer;
  private CellRenderer leftRenderer;
  private CellRenderer centeredRenderer;

  // table columns
  private TableColumn artColumn;
  private TableColumn channelColumn;
  private TableColumn slotColumn;
  private TableColumn trackColumn;
  private TableColumn titleColumn;
  private TableColumn artistColumn;
  private TableColumn typeColumn;
  private TableColumn genreColumn;
  private TableColumn timeColumn;
  private TableColumn sizeColumn;

  private int originalRowHeight = -1; // row height before album art display changed it

  private String debugName; // name of this panel for debug output

  /**
   * @param allowSort if true, the table will support sorting by clicking on column headers
   */
  public ContentMetadataTablePanel(J2seClient client, ContentMetadataDoubleClickedListener contentMetadataDoubleClickedListener, boolean allowSort, boolean allowDrop, String debugName)
  {
    super(new ContentMetadataTableModel(), allowSort);

    this.client = client;
    this.debugName = debugName;

    // XXX:0:20041221iain:yuck, may as well get the cast over with here
    contentMetadataTableModel = (ContentMetadataTableModel)getTableModel();
    // init model field XXX:0:20050126iain:
    contentMetadataTableModel.setPanel(this);

    this.contentMetadataDoubleClickedListener = contentMetadataDoubleClickedListener;

    JTable table = getTable();

    table.setVisible(false);
    table.setTransferHandler(new ContentMetadataTransferHandler(this, allowDrop));
    table.setDragEnabled(true);

    // get the table columns
    TableColumnModel columnModel = table.getColumnModel();
    artColumn     = columnModel.getColumn(ContentMetadataTableModel.COLUMN_ART);
    channelColumn = columnModel.getColumn(ContentMetadataTableModel.COLUMN_CHANNEL);
    slotColumn    = columnModel.getColumn(ContentMetadataTableModel.COLUMN_SLOT);
    trackColumn   = columnModel.getColumn(ContentMetadataTableModel.COLUMN_TRACK);
    titleColumn   = columnModel.getColumn(ContentMetadataTableModel.COLUMN_TITLE);
    artistColumn  = columnModel.getColumn(ContentMetadataTableModel.COLUMN_ARTIST);
    typeColumn    = columnModel.getColumn(ContentMetadataTableModel.COLUMN_TYPE);
    genreColumn   = columnModel.getColumn(ContentMetadataTableModel.COLUMN_GENRE);
    timeColumn    = columnModel.getColumn(ContentMetadataTableModel.COLUMN_TIME);
    sizeColumn    = columnModel.getColumn(ContentMetadataTableModel.COLUMN_SIZE);

    // tweak column widths

    // channel column fixed to small
    channelColumn.setResizable(false);
    channelColumn.setPreferredWidth(34);
    channelColumn.setMaxWidth(34);

    // slot column fixed to fairlysmall
    slotColumn.setResizable(false);
    slotColumn.setPreferredWidth(40);
    slotColumn.setMaxWidth(40);

    // track column fixed to small
    trackColumn.setResizable(false);
    trackColumn.setPreferredWidth(30);
    trackColumn.setMaxWidth(30);

    // art column not resizable by the user, sized to the art size
    artColumn.setResizable(false);

    // set some decent defaults for the text columns
    titleColumn.setPreferredWidth(160);
    artistColumn.setPreferredWidth(100);
    typeColumn.setPreferredWidth(45);
    genreColumn.setPreferredWidth(60);

    // time column fixed to small
    timeColumn.setResizable(false);
    timeColumn.setPreferredWidth(50);
    timeColumn.setMaxWidth(50);

    // create the renderers
    centeredRenderer = new CellRenderer(SwingConstants.CENTER);
    leftRenderer     = new CellRenderer(SwingConstants.LEFT);

    // keep album art renderer so we can adjust the size
    albumArtRenderer = centeredRenderer;

    // set the renderers
    artColumn.setCellRenderer(centeredRenderer);
    channelColumn.setCellRenderer(centeredRenderer);
    slotColumn.setCellRenderer(centeredRenderer);
    trackColumn.setCellRenderer(centeredRenderer);
    titleColumn.setCellRenderer(leftRenderer);
    artistColumn.setCellRenderer(leftRenderer);
    typeColumn.setCellRenderer(leftRenderer);
    genreColumn.setCellRenderer(leftRenderer);
    sizeColumn.setCellRenderer(leftRenderer);
    timeColumn.setCellRenderer(leftRenderer);

    // init orig row height
    originalRowHeight = table.getRowHeight();

    // don't show art by default
    table.removeColumn(artColumn);

    // remove clutter
    table.removeColumn(typeColumn);
    table.removeColumn(sizeColumn);

    setAlbumArtSize(0);
    table.setVisible(true);
  }

  public String getDebugName()
  {
    return debugName;
  }

  // column removal XXX:0:20050323iain: consider exposing column ids from table model to avoid multiple methods
  public void removeTrackColumn()  { getTable().removeColumn(trackColumn);  }
  public void removePlayerColumn() { getTable().removeColumn(channelColumn);}
  public void removeSlotColumn()   { getTable().removeColumn(slotColumn);   }
  public void removeArtistColumn() { getTable().removeColumn(artistColumn); }
  public void removeGenreColumn()  { getTable().removeColumn(genreColumn);  }

  public void setNowPlayingIndex(int index)
  {
    leftRenderer.setNowPlayingIndex(index);
    centeredRenderer.setNowPlayingIndex(index);
    albumArtRenderer.setNowPlayingIndex(index);
  }

  /**
   *  enables the display of album art
   */
  public void setAlbumArtSize(int size)
  {
    JTable table = getTable();
    TableColumnModel columnModel = table.getColumnModel();

    // if size has gone from zero to non-zero or vice versa, will have to deal with column
    boolean columnToggled = (albumArtSize == 0) ^ (size == 0);

    // keep size
    albumArtSize = size;
    int newRowHeight = -1;
    if (size > 0)
    {
      // see if the column needs re-adding
      if (columnToggled)
      {
        table.addColumn(artColumn);
        table.moveColumn(table.convertColumnIndexToView(artColumn.getModelIndex()), 0);
      }
      newRowHeight = size + UISettings.INSET_PANEL_BORDER_SIZE*2;

      // set column size
      // XXX:0:20050209iain: appears to be necessary for the column to be visible for this to work - a shame, as it appears to cause flicker
      int artCellDim = newRowHeight;
      artColumn.setPreferredWidth(artCellDim);
      artColumn.setMaxWidth(artCellDim);
      artColumn.setMinWidth(artCellDim);
    }
    else
    {
      // see if the column needs removal
      if (columnToggled)
      {
        //XXX:00000000000:20050128iain: hack - appears to be a bug in JTable (or perhaps our usage), the resize does not take
        //XXX:00000000000:20050128iain: effect if the column has just been added, so do it right before removal instead
        artColumn.setPreferredWidth(UISettings.ALBUM_THUMB_SIZE_SMALL);
        artColumn.setMaxWidth(UISettings.ALBUM_THUMB_SIZE_SMALL);
        artColumn.setMinWidth(UISettings.ALBUM_THUMB_SIZE_SMALL);
        table.removeColumn(artColumn);
      }
      newRowHeight = originalRowHeight;
    }

    // set the size in the cell renderer
    albumArtRenderer.setAlbumArtSize(size);

    // position the view port so that the same items are showing
    // XXX:0000:20050209iain: if visible items are selected, center on these instead
    JViewport viewport = getScrollPane().getViewport();
    int currentRowHeight = table.getRowHeight();
    int halfHeight = viewport.getHeight()/2;
    int vPos = viewport.getViewPosition().y;
    if (vPos == 0)
    {
      //XXX:0:20050128iain: hack - if view is right at the top, leave it that way instead of zooming on the center
      halfHeight = 0;
    }

    // figure ratio between old and new height ratio
    double heightRatio = (double)newRowHeight/(double)currentRowHeight;

    // figure new vertical position for keeping center items in place
    int newVPos = (int)((double)(vPos + halfHeight) * heightRatio) - halfHeight;

    // reset row height
    table.setRowHeight(newRowHeight);

    // viewport will flicker if it's doing it's standard blit scroll mode which appears to blit during setViewPosition,
    // so temporarily change the mode
    // NOTE: could just use BACKINGSTORE_SCROLL_MODE all the time, but uses a little extra ram and may not be as fast
    viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

    // set calculated position
    getScrollPane().getViewport().setViewPosition(new Point(0, newVPos));

    // set the viewport scroll mode back to the more efficent method
    viewport.setScrollMode(JViewport.BLIT_SCROLL_MODE);
  }

  public void setContentMetadataDoubleClickedListener(ContentMetadataDoubleClickedListener contentMetadataDoubleClickedListener)
  {
    this.contentMetadataDoubleClickedListener = contentMetadataDoubleClickedListener;
  }

  /**
   *  Abstract method implementation for updating the model
   */
  public void updateModel(ArrayList contentIds)
  {
    (new Throwable()).printStackTrace();

    // make sure these are really Content Ids
    assert Util.allEntriesAreAssignableTo(ContentId.class, contentIds);

    // keep selection, number of Items
    int[] selectedRows = getTable().getSelectedRows();
    int rowCount = getTableModel().getRowCount();

    // update the model
    contentMetadataTableModel.setContentMetadata(new ArrayList(client.getMetadataCache().getMetadata(contentIds, true))); //XXX:000:20050308iain: wasteful

    // restore selection XXX:0:20050330iain: only restore if number of rows the same.. may result in unintended selection,  could do better.
    if (getTableModel().getRowCount() == rowCount)
    {
      for (int i=0; i<selectedRows.length; i++)
      {
        getTable().addRowSelectionInterval(selectedRows[i], selectedRows[i]);
      }
    }
  }

  /**
   *  Abstract method implementation for updating metadata within the table model
   *
   *  Updates only rows with matching content ids, other rows are not modified.
   */
  public void updateMetadata(ContentMetadata[] contentMetadata)
  {
    // update the model
    contentMetadataTableModel.updateContentMetadata(contentMetadata);
  }

  /**
   *  Allow access to the actual data
   */
  public ArrayList getMetadata()
  {
    return contentMetadataTableModel.getContentMetadata();
  }

  /**
   *  ContentIdsSource implementation - called when an action needs to retreive source items, e.g. for adding to playlist
   */
  public List getContentIds()
  {
    int[] selectedRows = getTable().getSelectedRows();
    ArrayList list = new ArrayList(selectedRows.length);
    ArrayList metadata = contentMetadataTableModel.getContentMetadata();
    for (int i=0; i<selectedRows.length; i++)
    {
      ContentMetadata item = (ContentMetadata)metadata.get(resolveSortedRowIndex(selectedRows[i]));
      list.add(item.getContentId());
    }

    // make sure we're just returning content ids
    assert Util.allEntriesAreAssignableTo(ContentId.class, list);

    LoggerSingleton.logDebugCoarse(ContentMetadataTablePanel.class, "getContentIds", debugName + " providing " + selectedRows.length + " content ids as a ContentIdsSource");

    return list;
  }

  /**
   *
   */
  public ContentMetadata getNextItem(ContentId currentItemId)
  {
    //XXX:0:20050318iain: switch to using content items internally
    int[] selectedRows = getTable().getSelectedRows();
    if (selectedRows.length == 1)
    {
      int nextRow = selectedRows[0] + 1;
      getTable().setRowSelectionInterval(nextRow, nextRow);
      ArrayList metadata = contentMetadataTableModel.getContentMetadata();
      return (ContentMetadata)metadata.get(resolveSortedRowIndex(nextRow));
    }
    return null;
  }

  public void handleDoubleClick(int row, int column)
  {
    // show the details for the selected item
    if (contentMetadataDoubleClickedListener != null)
    {
      contentMetadataDoubleClickedListener.contentMetadataDoubleClicked(getTable(), (ContentMetadata)contentMetadataTableModel.getContentMetadata().get(row), row);
    }
  }

  public String toString()
  {
    return "ContentMetadataTablePanel[name=" + debugName + ", super="+ super.toString() + "]";
  }
}
// /*********************************************************************************
//  * INNER CLASS: holds album art w/ metadata
//  *********************************************************************************/
//   public static class Album
//   {
//     public ImageIcon icon;
//     public ContentMetadata metadata;

//     public Album(ContentMetadata metadata, ImageIcon icon)
//     {
//       this.icon = icon;
//       this.metadata = metadata;
//     }
//   }


//XXX:0:20050318iain: move the following class to its own file

/*********************************************************************************
 * CLASS: media summary table model - used for media by type/artist/genre etc.
 *********************************************************************************/
class ContentMetadataTableModel extends AbstractTableModel
{
  //XXX:00000000000000000000000:20050318iain: should keep content ids, not metadata (just retreive it from the cache)

  public static final int COLUMN_ART     = 0;
  public static final int COLUMN_CHANNEL = 1;
  public static final int COLUMN_SLOT    = 2;
  public static final int COLUMN_TRACK   = 3;
  public static final int COLUMN_ARTIST  = 4;
  public static final int COLUMN_TITLE   = 5;
  public static final int COLUMN_GENRE   = 6;
  public static final int COLUMN_TYPE    = 7;
  public static final int COLUMN_TIME    = 8;
  public static final int COLUMN_SIZE    = 9;

  public static String COLUMN_NAMES[];

  static
  {
    COLUMN_NAMES = new String[10];
    COLUMN_NAMES[COLUMN_ART]     = "Art";
    COLUMN_NAMES[COLUMN_CHANNEL] = "Player";
    COLUMN_NAMES[COLUMN_SLOT]    = "Slot";
    COLUMN_NAMES[COLUMN_TRACK]   = "Track";
    COLUMN_NAMES[COLUMN_ARTIST]  = "Artist";
    COLUMN_NAMES[COLUMN_TITLE]   = "Title";
    COLUMN_NAMES[COLUMN_GENRE]   = "Genre";
    COLUMN_NAMES[COLUMN_TYPE]    = "Type";
    COLUMN_NAMES[COLUMN_TIME]    = "Time";
    COLUMN_NAMES[COLUMN_SIZE]    = "Size";
  }

  private ArrayList metadata = new ArrayList(); // the actual data
  private ContentMetadataTablePanel panel;
  private HashMap metadataIndexMap; // helps locate items based on content ids (key = content ID, value = Integer index into array list)

  public void setPanel(ContentMetadataTablePanel panel)
  {
    //XXX:0:20050308iain: why is this necessary? not a static inner class, so should already have association... ??
    this.panel = panel;
  }

  public void setContentMetadata(ArrayList metadataList)
  {
    // update member
    this.metadata = metadataList;

    // keep index  XXX:0:20050308iain: perhaps a bit heavyweight
    int index = 0;
    metadataIndexMap = new HashMap(64);
    for (Iterator i=metadata.iterator(); i.hasNext();)
    {
      ContentMetadata metadata = (ContentMetadata)i.next();
      metadataIndexMap.put(metadata.getContentId(), new Integer(index++));
    }

    // notify listeners
    fireTableDataChanged();
  }

  /**
   *  Update metadata for content ids in the table that match the content ids of the passed metadata
   */
  public void updateContentMetadata(ContentMetadata[] newMetadata)
  {
    // see if there's anything to update
    if (metadata.size() == 0)
    {
      return;
    }

    // find matching content ids and update them
    int numUpdated = 0;
    for (int i=0; i<newMetadata.length; i++)
    {
      assert newMetadata[i] != null;
      assert metadataIndexMap != null;
      assert newMetadata[i].getContentId() != null;

      // see if this item is in our table
      Integer metadataIndex = (Integer)metadataIndexMap.get(newMetadata[i].getContentId());

      if (metadataIndex != null)
      {
        // skip unchanged metadata
        ContentMetadata oldMetadata = (ContentMetadata)metadata.get(metadataIndex.intValue());

        if (oldMetadata != newMetadata[i]) // assume that if it's the same object, we should update (album info may have arrived and been set)
        {
          DateTime oldTS = oldMetadata.getLastUpdateTimeStamp();
          DateTime newTS = newMetadata[i].getLastUpdateTimeStamp();

          // XXX:0:20050311iain: hack - DataTime.equals appears busted
          if (   oldTS.getHour()   == newTS.getHour()
                 && oldTS.getMinute() == newTS.getMinute()
                 && oldTS.getSec()    == newTS.getSec()
                 && oldTS.getMonth()  == newTS.getMonth()
                 && oldTS.getYear()   == newTS.getYear())
          {
            continue;
          }
        }
        metadata.set(metadataIndex.intValue(), newMetadata[i]);
        numUpdated++;
      }
    }
    if (numUpdated > 0)
    {
      // notify table XXX:0:20050330iain: could reduce range if performance is an issue
      fireTableRowsUpdated(0, getRowCount());
    }
  }

  /**
   * @return a list of ContentMetadata objects
   */
  public ArrayList getContentMetadata()
  {
    return metadata;
  }

  public int getColumnCount()
  {
    return COLUMN_NAMES.length;
  }

  public int getRowCount()
  {
    return metadata.size();
  }

  public Object getValueAt(int row, int column)
  {
    if (row >= metadata.size())
    {
      throw new IllegalArgumentException("invalid row " + row);
    }

    Object object = metadata.get(row);
    ContentMetadata rowItem = (ContentMetadata) object;

    switch (column)
    {
      case COLUMN_ART:
        return rowItem;
      case COLUMN_TRACK:
        return new Integer(rowItem.getTrackNumber()); // XXX:00000000000000000000:20050321iain: use locationmanager
      case COLUMN_CHANNEL:
        return new Integer(rowItem.getContentId().getPlayerChannel()); // XXX:00000000000000000000:20050321iain: use locationmanager
      case COLUMN_SLOT:
        return new Integer(rowItem.getContentId().getPlayerSlot());  //XXX:00000000000000000000:20050321iain: use locationmanager
      case COLUMN_TITLE:
        return rowItem.getTitle();
      case COLUMN_ARTIST:
        return rowItem.getArtist();
      case COLUMN_GENRE:
        return rowItem.getGenre();
      case COLUMN_TYPE:
        return rowItem.getMediaType();
      case COLUMN_TIME:
        return UISettings.formatTimeCode(rowItem.getPlaybackTime());
      case COLUMN_SIZE:
        return new Integer(rowItem.getContentSize());
      default:
        throw new IllegalArgumentException("invalid column " + column);
    }
  }

  public Class getColumnClass(int column)
  {
    switch (column)
    {
      case COLUMN_ART:
        return ContentMetadata.class;
      case COLUMN_TITLE:
      case COLUMN_ARTIST:
      case COLUMN_GENRE:
      case COLUMN_TYPE:
      case COLUMN_TIME:
        return String.class;
      case COLUMN_CHANNEL:
      case COLUMN_SLOT:
      case COLUMN_SIZE:
      case COLUMN_TRACK:
        return Integer.class;
      default:
        throw new IllegalArgumentException("invalid column " + column);
    }
  }

  public String getColumnName(int column)
  {
    return COLUMN_NAMES[column];
  }
}

