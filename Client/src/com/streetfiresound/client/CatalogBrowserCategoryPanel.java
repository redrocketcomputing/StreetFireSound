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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.streetfiresound.client.PlayListEditPanel.AddCategoryToPlayListAction;

/**
 *  Catalog browser category summary table panel and button bar
 *  @author iain huxley
 */
public class CatalogBrowserCategoryPanel extends StreetFirePanel implements CategorySummarySource
{
  private CategorySummaryTablePanel tablePanel; // table panel
  private JPanel borderedTablePanel;
  private JPanel buttonPanel;                   // holds action buttons
  private String debugName;

  /**
   *  Construct a category summary table panel
   */
  public CatalogBrowserCategoryPanel(CatalogBrowser catalogBrowser, int categoryType, String debugName)
  {
    // set up UI
    initUI(catalogBrowser, categoryType);

    this.debugName = debugName;
  }

  public void initUI(CatalogBrowser catalogBrowser, int categoryType)
  {
    //    setBorder(UISettings.PANEL_BORDER_LOWERED);

    // set up table panel
    tablePanel = new CategorySummaryTablePanel(catalogBrowser, categoryType, true);
    tablePanel.setSorted(0);

    // set up the button panel
    buttonPanel = new StreetFirePanel();
    buttonPanel.setLayout(new FlowLayout());

    //buttonPanel.setBorder(BorderFactory.createLineBorder(Color.red));
    add(buttonPanel, BorderLayout.SOUTH);

    // need bordered table panel as want texture to show through border
    borderedTablePanel = new StreetFirePanel();
    borderedTablePanel.add(tablePanel, BorderLayout.CENTER);
    borderedTablePanel.setBorder(UISettings.PANEL_BORDER_LOWERED);
    add(borderedTablePanel, BorderLayout.CENTER);
  }

  public void addAddToPlayListAction(AddCategoryToPlayListAction action)
  {
    action.setCategorySummarySource(this);
    buttonPanel.add(new JButton(action));
  }

  public List getCategorySummaryItems()
  {
    return tablePanel.getCategorySummaryItems();
  }

  public int getCategoryType()
  {
    return tablePanel.getCategoryType();
  }

  public CategorySummaryTablePanel getTablePanel()
  {
    return tablePanel;
  }

  public String toString()
  {
    return "'" + debugName + "' [" + super.toString() + "]";
  }
}
