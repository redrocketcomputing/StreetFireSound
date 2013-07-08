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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;


/**
 *  Abstract base for a panel containing a sorted table
 *  @author iain huxley
 */
public abstract class AbstractTablePanel extends JPanel implements MouseListener
{
  private JTable       table;        // UI element shown
  private TableSorter  tableSorter;  // sorter (implements tablemodel, remaps table)
  private TableModel   tableModel;   // table model with unsorted data
  private JScrollPane  scrollPane;   // table's scroll pane

  private boolean dirty;
  private int transactionId = -1;

  /**
   * @param tableModel the tablemodel which will contain the data to be displayed
   * @param allowSort if true, the table will support sorting by clicking on column headers
   */
  public AbstractTablePanel(TableModel tableModel, boolean allowSort)
  {
    super(new BorderLayout());

    this.tableModel = tableModel;

    // create table and sorter XXX:0:20050112iain: omit sorter when sort is disabled
    tableSorter = new TableSorter(tableModel);
    table = new JTable(tableSorter);
    table.setDragEnabled(true);

    //table.getTableHeader().setReorderingAllowed(false);
    //table.setIntercellSpacing(new Dimension(10,10));
    //     table.setShowHorizontalLines(true);
    //     table.setShowGrid(true);

    // provide the sorter access to the table header if sorting allowed
    if (allowSort)
    {
      tableSorter.setTableHeader(table.getTableHeader());
    }

    // place the UI
    JPanel panel = new JPanel(new BorderLayout());

    scrollPane = new JScrollPane(table);
    //table.getColumnModel().setColumnMargin(0);

    add(scrollPane, BorderLayout.CENTER);

    // listen for mouse events
    table.addMouseListener(this);
  }

  /**
   *  Called when a cell is double clicked
   */
  abstract public void handleDoubleClick(int row, int column);

  /**
   *  Set the table model to sort based on a particular column (ascending order)
   *  @param byColumn index of the column to sort by, or -1 to clear sort
   */
  public void setSorted(int byColumn)
  {
    // handle -1 case
    if (byColumn == -1)
    {
      tableSorter.setSortingStatus(0, TableSorter.NOT_SORTED);
    }
    else
    {
      tableSorter.setSortingStatus(byColumn, TableSorter.ASCENDING);
    }
  }

  /**
   *  resolves the mapping of a sorted table row index to the original data,
   *  required when locating selected items etc. in the base table model
   */
  public int resolveSortedRowIndex(int sortedIndex)
  {
    return tableSorter.modelIndex(sortedIndex);
  }

  /**
   * @return the table model which supplies the [unsorted] data for this table
   */
  public TableModel getTableModel()
  {
    return tableModel;
  }

  /**
   * @return the table UI element
   */
  public JTable getTable()
  {
    return table;
  }

  /**
   * @return the scroll pane element
   */
  public JScrollPane getScrollPane()
  {
    return scrollPane;
  }

  /**
   * utility method for storing transaction ID (commonly used when implementations request table contents
   */
  public void setTransactionId(int transactionId)
  {
    this.transactionId = transactionId;
  }

  /**
   * utility method for retreiving a previously set transaction ID (commonly used when implementations request table contents
   */
 public int getTransactionId()
  {
    return transactionId;
  }

  /**
   *  MouseListener implementation for determining when a cell is double clicked
   */
  public void mouseClicked(MouseEvent e)
  {
    if (e.getClickCount() == 2)
    {
      Point p = e.getPoint();
      int row = tableSorter.modelIndex(table.rowAtPoint(p));
      handleDoubleClick(row, table.columnAtPoint(p));
    }
  }

  // non-required mouselistener interface
  public void mouseEntered(MouseEvent e)  {}
  public void mouseExited(MouseEvent e)   {}
  public void mousePressed(MouseEvent e)  {}
  public void mouseReleased(MouseEvent e) {}
}
