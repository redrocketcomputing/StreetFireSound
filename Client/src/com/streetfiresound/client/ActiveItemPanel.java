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
import java.awt.Color;

import javax.swing.JTabbedPane;

import com.streetfiresound.clientlib.ContentMetadata;


/**
 *  Provides access to the "active item":
 *  - a playlist being edited
 *  - a disc being edited
 *
 *  @author iain huxley
 */
public class ActiveItemPanel extends StreetFirePanel //implements  MediaSummaryDoubleClickedListener
{
  static final int PLAY_QUEUE_TAB  = 0;
  static final int PLAYLIST_TAB    = 1;
  static final int DISC_EDIT_TAB   = 2;
  static final int SLOT_SCAN_TAB   = 3;
  static final int SETTINGS_TAB    = 4;

  private J2seClient        client;              // client object

  private JTabbedPane       tabbedPane;          // primary UI element

  private PlayQueuePanel    playQueuePanel;      // tab 0 UI - play queue
  private PlayListEditPanel playListEditorPanel; // tab 1 UI - playlist editor
  private DiscEditPanel     discEditPanel;       // tab 2 UI - disc editor
  private SlotScanPanel     slotScanPanel;       // tab 3 UI - slot scanner
  private SettingsPanel     settingsPanel;       // tab 4 UI - settings panel

  public ActiveItemPanel(J2seClient client)
  {
    // init members
    this.client = client;

    //XXX:0000000000000:20050105iain:  listen for events
    //client.getEventDispatcher().addListener(this);

    // set up the user interface
    initUI();
  }

  /**
   * @return the playlist editor
   */
  public PlayListEditPanel getPlayListEditorPanel()
  {
    return playListEditorPanel;
  }

  /**
   * @return the disc editor
   */
  public DiscEditPanel getDiscEditPanel()
  {
    return discEditPanel;
  }

  /**
   * @return the play queue planel
   */
  public PlayQueuePanel getPlayQueuePanel()
  {
    return playQueuePanel;
  }

//   public int getCurrentItemPlayQueueIndex()
//   {
//     return playQueuePanel.getCurrentItemPlayQueueIndex();
//   }

  /**
   *  Set up the user interface
   */
  private void initUI()
  {
     //    setBorder(UISettings.INSET_PANEL_BORDER);
    //setBorder(new ThinBevelBorder(BevelBorder.LOWERED, UISettings.BACKGROUND, UISettings.BACKGROUND.darker().darker()));
    //setBorder(new LineBorder(Color.black, 1, true)); //UISettings.INSET_PANEL_BORDER_SIZE, true));

    if (UISettings.DEBUG_PANELS)
    {
      setBackground(Color.cyan);
    }

    // create the tabbed pane
    tabbedPane = new JTabbedPane();

    // create the editor panels
    playQueuePanel      = new PlayQueuePanel(client);
    playListEditorPanel = new PlayListEditPanel(client);
    discEditPanel       = new DiscEditPanel(client);
    slotScanPanel       = new SlotScanPanel(client);
    settingsPanel       = new SettingsPanel(client);

    // add the tabs
    tabbedPane.addTab("Now Playing",     ImageCache.getImageIcon("queue.png"),       playQueuePanel);
    tabbedPane.addTab("Playlist Editor", ImageCache.getImageIcon("playlist.gif"),    playListEditorPanel);
    tabbedPane.addTab("Disc Editor",     ImageCache.getImageIcon("discEdit.gif"),    discEditPanel);
    tabbedPane.addTab("Slot Scanner",    ImageCache.getImageIcon("scanslots.gif"),   slotScanPanel);
    tabbedPane.addTab("Settings",        ImageCache.getImageIcon("preferences.gif"), settingsPanel);

//     // add a change listener so that content can be requested when a tab is selected
//     tabbedPane.addChangeListener(
//       new ChangeListener()
//       {
//         // different tab was selected
//         public void stateChanged(ChangeEvent e)
//         {
//           // request content for the currently selected tab
//           requestContentForCurrentTab();
//         }
//       }
//     );

    // add the tabbed pane
    add(tabbedPane, BorderLayout.CENTER);
  }

  //XXX:00000000000000000:20050104iain:
  public void editPlayList(ContentMetadata playListMetadata)
  {
    playListEditorPanel.editPlayList(playListMetadata.getContentId());
    showPlayListEditor();
  }

  //XXX:00000000000000000:20050104iain:
  public void editDisc(ContentMetadata discMetadata)
  {
    discEditPanel.editDisc(discMetadata.getContentId());
    showDiscEditor();
  }

  /**
   *  ensures that the playlist editor tab is active
   */
  public void showPlayListEditor()
  {
    tabbedPane.setSelectedIndex(PLAYLIST_TAB);
  }

  /**
   *  ensures that the disc editor tab is active
   */
  public void showDiscEditor()
  {
    tabbedPane.setSelectedIndex(DISC_EDIT_TAB);
  }

  /**
   * ensures that the play queue tab is active
   */
  public void showPlayQueue()
  {
    tabbedPane.setSelectedIndex(PLAY_QUEUE_TAB);
  }
}
