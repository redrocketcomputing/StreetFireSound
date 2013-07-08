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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.streetfiresound.clientlib.event.CategorySummaryArrivedEvent;
import com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary;

/**
 * media summary table panel - used for media by type/artist/genre etc.
 *  @author iain huxley
 */
public class CategorySummaryTablePanel extends AbstractTablePanel implements CategorySummarySource
{
  private int categoryType; // the category of media summaries (by artist, genre etc.)
  private CategorySummaryTableModel categorySummaryTableModel;
  private CatalogBrowser catalogBrowser;

  /**
   *  construct a table panel for category summaries
   *  @param allowSort if true, the table will support sorting by clicking on column headers
   */
  public CategorySummaryTablePanel(CatalogBrowser catalogBrowser, int categoryType, boolean allowSort)
  {
    super(new CategorySummaryTableModel(), allowSort);

    // XXX:0:20041221iain: yuck, may as well get the cast over with here
    categorySummaryTableModel = (CategorySummaryTableModel)getTableModel();

    // keep catalogBrowser XXX:00:20041223iain: consider not directly using catalogBrowser
    this.catalogBrowser = catalogBrowser;
    this.categoryType = categoryType;

    // get the table columns
    JTable table = getTable();
    TableColumnModel columnModel = table.getColumnModel();
    TableColumn nameColumn  = columnModel.getColumn(CategorySummaryTableModel.COLUMN_NAME);
    TableColumn countColumn = columnModel.getColumn(CategorySummaryTableModel.COLUMN_COUNT);

    // put name count column first
    table.moveColumn(table.convertColumnIndexToView(countColumn.getModelIndex()), 0);

    // tweak count column
    countColumn.setResizable(false);
    countColumn.setPreferredWidth(50);
    countColumn.setMaxWidth(50);

    // set the renderers
    countColumn.setCellRenderer(new CellRenderer(SwingConstants.CENTER));
    nameColumn.setCellRenderer(new CellRenderer(SwingConstants.LEFT));
  }

  /**
   *  Abstract method implementation for updating the model
   */
  public void updateModel(CategorySummaryArrivedEvent e)
  {
    // update the model
    categorySummaryTableModel.setSummaries(e.getSummaries());
  }

  /**
   *  Abstract method implementation for handling a cell double click
   */
  public void handleDoubleClick(int row, int column)
  {
    // show the details for the selected item
    catalogBrowser.showUserSelectionSummaryTab(categoryType, categorySummaryTableModel.getCategorySummaries()[row].getValue());
  }

  public int getCategoryType()
  {
    return categoryType;
  }

  /**
   *  get the selected category summaries
   */
  public List getCategorySummaryItems()
  {
    CategorySummary[] categorySummaries = categorySummaryTableModel.getCategorySummaries();

    int[] selectedRows = getTable().getSelectedRows();
    ArrayList list = new ArrayList(selectedRows.length);
    for (int i=0; i<selectedRows.length; i++)
    {
      list.add(categorySummaries[resolveSortedRowIndex(selectedRows[i])]);
    }
    return list;
  }
}

/**
 * table model for summaries
 */
class CategorySummaryTableModel extends AbstractTableModel
{
  public static final int COLUMN_NAME  = 0;
  public static final int COLUMN_COUNT = 1;
  public static String COLUMN_NAMES[];

  static
  {
    COLUMN_NAMES = new String[2];
    COLUMN_NAMES[COLUMN_NAME]  = "Name";
    COLUMN_NAMES[COLUMN_COUNT] = "Count";
  }

  private CategorySummary[] summaries = new CategorySummary[0]; // table model data

  /**
   *  set the data for this model.  Will cause a full table refresh.
   */
  public void setSummaries(CategorySummary[] summaries)
  {
    // update member
    this.summaries = summaries;

    // notify listeners
    fireTableDataChanged();
  }

  public CategorySummary[] getCategorySummaries()
  {
    return summaries;
  }

  public int getColumnCount()
  {
    return COLUMN_NAMES.length;
  }

  public int getRowCount()
  {
    return summaries.length;
  }

  public Object getValueAt(int row, int column)
  {
    if (row >= summaries.length)
    {
      throw new IllegalArgumentException("invalid row " + row);
    }
    switch (column)
    {
      case COLUMN_NAME:
        return summaries[row].getValue();
      case COLUMN_COUNT:
        return new Integer(summaries[row].getCount());
      default:
        throw new IllegalArgumentException("invalid column " + column);
    }
  }

  public Class getColumnClass(int columnIndex)
  {
    switch (columnIndex)
    {
      case COLUMN_NAME:
        return String.class;
      case COLUMN_COUNT:
        return Integer.class;
      default:
        throw new IllegalArgumentException("invalid column " + columnIndex);
    }
  }

  public String getColumnName(int column)
  {
    return COLUMN_NAMES[column];
  }
}
