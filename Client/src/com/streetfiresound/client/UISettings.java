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
 * $Id: UISettings.java,v 1.7 2005/03/26 04:19:57 iain Exp $
 */

package com.streetfiresound.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.havi.fcm.types.TimeCode;


/**
 * Class for creating UI elements which conform to UI conventions for our products
 *
 *  @author iain huxley
 */
public class UISettings
{
  /** panel debugging switch */
  public static final boolean DEBUG_PANELS = false;

  /** initial app frame width */
  public static final int APP_FRAME_WIDTH = 1100;

  /** initial app frame height */
  public static final int APP_FRAME_HEIGHT = 900;

  /** standard divider size */
  public static final int DIVIDER_SIZE = 10;


  public static final Insets STANDARD_BUTTON_INSETS = new Insets(5, 5, 5, 5);

  /** medium width for standard button */
  public static final int STANDARD_BUTTON_XDIM_MEDIUM = 72;

  /** large width for standard button */
  public static final int STANDARD_BUTTON_XDIM_LARGE = 130;

  /** large width for standard button */
  public static final int STANDARD_BUTTON_YDIM = 27;

  /** standard spacer dim */
  public static final Dimension STANDARD_SPACER_DIM = new Dimension(5, 5);

  /** standard spacer dim */
  public static final int BASIC_OFFSET = 3;

  /** standard dim for medium button */
  public static final Dimension STANDARD_BUTTON_DIM_MEDIUM = new Dimension(STANDARD_BUTTON_XDIM_MEDIUM, STANDARD_BUTTON_YDIM);

  /** standard dim for large button */
  public static final Dimension STANDARD_BUTTON_DIM_LARGE  = new Dimension(STANDARD_BUTTON_XDIM_LARGE,  STANDARD_BUTTON_YDIM);

  /** size for buttons in right panel */
  public static final int LARGE_BUTTON_SIZE = 45; //XXX0:20040823iain: find better name

  /** background color for odd table lines */
  public static final Color TABLE_LINE_BACKGROUND_ODD = Util.getColorFromHexString(AppFramework.uiProperties.getProperty("ColorMap.alternateBackground"));

  /** background color for even table lines */
  public static final Color TABLE_LINE_BACKGROUND_EVEN = Util.getColorFromHexString(AppFramework.uiProperties.getProperty("ColorMap.windowBackground"));

  /** background color for selected table lines */
  public static final Color TABLE_LINE_BACKGROUND_SELECTED = Util.getColorFromHexString(AppFramework.uiProperties.getProperty("ColorMap.selectBackground"));

  /** foreground color for selected table lines */
  public static final Color TABLE_LINE_FOREGROUND_SELECTED= Util.getColorFromHexString(AppFramework.uiProperties.getProperty("ColorMap.selectForeground"));

  /** foreground color for selected table lines */
  public static final Color TABLE_LINE_FOREGROUND = Util.getColorFromHexString(AppFramework.uiProperties.getProperty("ColorMap.tableForeground"));

  /** foreground color for player display */
  public static final Color DISPLAY_FOREGROUND = Util.getColorFromHexString(AppFramework.uiProperties.getProperty("ColorMap.displayForeground"));

  /** foreground color for player display */
  public static final Color BACKGROUND = Util.getColorFromHexString(AppFramework.uiProperties.getProperty("ColorMap.background"));

  /** background texture image path */
  public static final String BACKGROUND_IMAGE_PATH = AppFramework.uiProperties.getProperty("Background.texture");

  /** color for status text in splashscreen */
  public static final Color STATUS_TEXT_COLOR = Color.black;

  /** color for the inset panels */
  public static final Color INSET_PANEL_BACKGROUND = new Color(212, 212, 212);

  /** borer for the inset panels */
  public static final int INSET_PANEL_BORDER_SIZE = 5;

  /** standard borders for panels of significance */
  public static final Border PANEL_BORDER_RAISED  = new TranslucentBevelBorder(TranslucentBevelBorder.BEVEL_TYPE_RAISED,  3);
  public static final Border PANEL_BORDER_LOWERED = new TranslucentBevelBorder(TranslucentBevelBorder.BEVEL_TYPE_LOWERED, 0);

  /** border for the inset panels XXX:0:20050209iain: deprecated, use PANEL_BORDER_XXX (above) */
  public static final Border INSET_PANEL_BORDER = BorderFactory.createEmptyBorder(INSET_PANEL_BORDER_SIZE, INSET_PANEL_BORDER_SIZE, INSET_PANEL_BORDER_SIZE, INSET_PANEL_BORDER_SIZE);
  //public static final Border INSET_PANEL_BORDER = new ThinBevelBorder(BevelBorder.LOWERED, BACKGROUND.brighter(), BACKGROUND.darker());
  //public static final Border INSET_PANEL_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);

  /** standard table font */
  public static final Font TABLE_FONT = new Font("Dialog", Font.PLAIN, 11);

  /** emphasis table font */
  public static final Font TABLE_FONT_EMPHASIS = new Font("Dialog", Font.BOLD, 11);

  /** emphasis table font */
  public static final Font SERVICE_BUTTON_FONT = new Font("Dialog", Font.PLAIN, 11);

  public static final int ALBUM_THUMB_SIZE_SMALL  = 28;
  public static final int ALBUM_THUMB_SIZE_MEDIUM = 64;
  public static final int ALBUM_THUMB_SIZE_LARGE  = 128;
  public static final int ALBUM_THUMB_SIZE_HUGE   = 200;

  /** private constructor to enforce singleton */
  private UISettings()
  {
  }

//   /**
//    * sets up a scrollable panel to display a table
//    */
//   public static JPanel createTablePanel(JTable table)
//   {
//     JPanel panel = new JPanel(new BorderLayout());

//     JScrollPane sp = new JScrollPane(table);
//     //sp.setBorder(BorderFactory.createLine(0, 0, 0, 0));
//     panel.add(sp, BorderLayout.CENTER);
//     //sp.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
//     table.getColumnModel().setColumnMargin(0);

//     //panel.setBackground(INSET_PANEL_BACKGROUND);
//     //panel.setBorder(new EmptyBorder(0, BASIC_OFFSET, 0, BASIC_OFFSET));

//     //sp.getViewport().setBackground(Color.white);
//     return panel;
//   }

//   /**
//    * Create a large labelled button
//    * our current style is square with a centered label underneath
//    *
//    * @param label
//    * @param actionListener
//    * @param buttonGroup    may be null
//    * @param mainIcon
//    * @param rolloverIcon
//    * @param selectedIcon
//    * @param pressedIcon
//    *
//    * @return a panel containing the laid out elements
//    */
//   public static JPanel createLargeLabelledButton(boolean toggle, JLabel label, ActionListener actionListener, ButtonGroup buttonGroup, Icon mainIcon, Icon rolloverIcon, Icon selectedIcon, Icon pressedIcon)
//   {
//     // create the panel which will hold the button and label
//     JPanel bPanel = new JPanel();
//     bPanel.setBackground(java.awt.Color.darkGray);
//     bPanel.setLayout(new BorderLayout());
//     bPanel.setPreferredSize(new Dimension(LARGE_BUTTON_SIZE + 10, LARGE_BUTTON_SIZE + 15));
//     label.setForeground(java.awt.Color.white);

//     AbstractButton button = null;

//     if (toggle)
//     {
//       button = new JToggleButton(mainIcon);
//     }
//     else
//     {
//       button = new JButton(mainIcon);
//     }

//     button.setPreferredSize(new Dimension(LARGE_BUTTON_SIZE, LARGE_BUTTON_SIZE));
//     button.setMaximumSize(new Dimension(LARGE_BUTTON_SIZE, LARGE_BUTTON_SIZE));
//     button.setMinimumSize(new Dimension(LARGE_BUTTON_SIZE, LARGE_BUTTON_SIZE));
//     button.setSelectedIcon(selectedIcon);
//     button.setRolloverSelectedIcon(rolloverIcon);
//     button.setRolloverIcon(rolloverIcon);
//     button.setPressedIcon(pressedIcon);
//     button.setFocusPainted(false);

//     button.setBorder(new javax.swing.border.EtchedBorder(java.awt.Color.gray, new java.awt.Color(30, 30, 30)));

//     JPanel buttonSubPanel = new JPanel();
//     buttonSubPanel.setLayout(new BoxLayout(buttonSubPanel, BoxLayout.X_AXIS));
//     buttonSubPanel.add(Box.createHorizontalGlue());
//     buttonSubPanel.add(button);
//     buttonSubPanel.add(Box.createHorizontalGlue());
//     buttonSubPanel.setOpaque(false);

//     // add the button to the buttongroup if applicable (for radiobutton-type behaviour)
//     if (buttonGroup != null)
//     {
//       buttonGroup.add(button);
//     }

//     // add the action listener so the button can do something
//     button.addActionListener(actionListener);

//     // add the button
//     bPanel.add(buttonSubPanel, BorderLayout.CENTER);

//     // add the label
//     bPanel.add(label, BorderLayout.SOUTH);

//     return bPanel;
//   }

  /**
   * configure a standard tool button
   */
  public static AbstractButton configureToolButton(AbstractButton button)
  {
    //XXX000000:20050320iah: todo
    //    button.setInsets();
    return button;
  }

//     // XXX:0:20040928iain: pull into config?
//     boolean showText = true;

//     // clear button
//     if (!showText)
//     {
//       button.setText("");
//     }
//     button.setFocusPainted(false);
//     button.setMargin(new Insets(0, 1, 0, 1));
//     button.setVerticalTextPosition(SwingConstants.BOTTOM);
//     button.setHorizontalTextPosition(SwingConstants.CENTER);

//     // set the size
//     if (!showText)
//     {
//       button.setPreferredSize(new Dimension(STANDARD_BUTTON_YDIM, STANDARD_BUTTON_YDIM));
//     }
//     else
//     {
//       button.setIconTextGap(0);
//       button.setPreferredSize(new Dimension(56, 40));
//     }

//     // get the button's action, it stores the images and tool tip text
//     Action action = button.getAction();
//     if (action != null)
//     {
//       // set the tooltip text
//       button.setToolTipText((String)action.getValue("tooltiptext"));

//       // set rollover images etc. if available
//       Icon selectedIcon  = (Icon)action.getValue("selected");
//       if (selectedIcon != null)
//       {
//         button.setSelectedIcon(selectedIcon);
//       }

//       Icon pressedIcon   = (Icon)action.getValue("pressed");
//       if (pressedIcon != null)
//       {
//         button.setPressedIcon(pressedIcon);
//       }

//       Icon mouseoverIcon = (Icon)action.getValue("mouseover");
//       if (mouseoverIcon != null)
//       {
//         button.setRolloverIcon(mouseoverIcon);
//       }
//     }
//     return button;
//   }


  /**
   *  @param timeCode the time to format
   *  @return a string representation of timeCode in the format H:MM
   */
  public static String formatTimeCode(TimeCode timeCode)
  {
    //XXX:0:20041220iain: move to util class

    // pad mins if necessary
    String mins = String.valueOf(timeCode.getMinute());
    if (mins.length() < 2)
    {
      mins = "0" + mins;
    }

    // pad seconds if necessary
    String seconds = String.valueOf(timeCode.getSec());
    if (seconds.length() < 2)
    {
      seconds = "0" + seconds;
    }

    return timeCode.getHour() + ":" + mins + ":" + seconds;
  }
}
