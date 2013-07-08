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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.MediaCatalog;
import com.streetfiresound.clientlib.MlidContentId;
import com.streetfiresound.clientlib.RemotePlayListCatalog;
import com.streetfiresound.clientlib.Util;
import com.streetfiresound.clientlib.event.PlayListArrivedEvent;
import com.streetfiresound.clientlib.event.ContentIdsArrivedEvent;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.PlayListCreatedEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayList;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Dimension;
import javax.swing.border.CompoundBorder;
import javax.swing.BorderFactory;

// XXX:0:20050310iain: has quite a bit in common with PlayQueuePanel, consider using new common base

/**
 * Edits a playlist.  includes button bar for playlist context
 * @author iain huxley
 */
public class PlayListEditPanel extends StreetFirePanel implements StreetFireEventListener
{
  private J2seClient      client;          // client object
  private MediaCatalog    mediaCatalog;    // media catalog
  private RemotePlayListCatalog playListCatalog; // playlist catalog

  // transaction tracking
  private int outstandingTransactionId = StreetFireEvent.NOT_SET;

  private int remainingCategoriesInsertIndex = -1;
  private List remainingCategoriesToExplode = new LinkedList();  // categories to be exploded and added to the playlist

  // playlist info
  private ContentId playlistId = null;

  private ContentMetadataTablePanel playListTablePanel;   // playlist contents table panel

  private JPanel buttonPanel; // holds action buttons
  private JPanel borderedTablePanel;
  private JTextField playListNameField;
  private JTextField playListIrCodeField;


  public PlayListEditPanel(J2seClient client)
  {
    // init members
    this.client = client;
    playListCatalog = client.getPlayListCatalog();
    mediaCatalog = client.getMediaCatalog();

    // listen for events
    client.getEventDispatcher().addListener(this);

    // set up the user interface
    initUI();
  }

  /**
   *  Set up the user interface
   */
  private void initUI()
  {
    if (UISettings.DEBUG_PANELS)
    {
      setBackground(Color.orange);
    }

    //setBorder(new CompoundBorder(UISettings.PANEL_BORDER_LOWERED, BorderFactory.createEmptyBorder(3, 6, 3, 6))); // top, left, bottom, right

    StreetFirePanel playListInfoPanel = new StreetFirePanel();
    playListInfoPanel.setLayout(new FlowLayout());

    playListInfoPanel.add(new JLabel("IR Code:"));
    playListIrCodeField = new JTextField();
    playListIrCodeField.setPreferredSize(new Dimension(30, 20));
    playListInfoPanel.add(playListIrCodeField);

    playListInfoPanel.add(new JLabel("Name:"));
    playListNameField = new JTextField();
    playListNameField.setPreferredSize(new Dimension(130, 20));
    playListInfoPanel.add(playListNameField);

    add(playListInfoPanel, BorderLayout.NORTH);


    // create and add the table panel - no double click listener, disallow sort, allow drop
    playListTablePanel = new ContentMetadataTablePanel(client, null, false, true, "playlist editor");

    // create the button panel
    buttonPanel = new StreetFirePanel();
    buttonPanel.setLayout(new FlowLayout());

    // set up new action
    NewAction newAction = new NewAction();
    buttonPanel.add(new JButton(newAction));

    // set up save action
    SaveAction saveAction = new SaveAction();
    buttonPanel.add(new JButton(saveAction));

    // set up remove action
    RemoveAction removeAction = new RemoveAction();
    buttonPanel.add(new JButton(removeAction));

    // add the button panel
    add(buttonPanel, BorderLayout.SOUTH);

    // need bordered table panel as want texture to show through border
    borderedTablePanel = new StreetFirePanel();
    borderedTablePanel.add(playListTablePanel, BorderLayout.CENTER);
    borderedTablePanel.setBorder(UISettings.PANEL_BORDER_LOWERED);
    add(borderedTablePanel, BorderLayout.CENTER);
  }

  /**
   *  sets the playlist to be edited and triggers an async content request
   */
  public void editPlayList(ContentId contentId)
  {
    playlistId = contentId;
    requestContent();

    // make sure this tab is active
    client.showPlayListEditor();
  }

  /**
   *  sets the playlist to be edited and triggers an async content request
   */
  public void savePlayList()
  {
    // send the request
    outstandingTransactionId = playListCatalog.requestSavePlayList(playlistId, preparePlayList());
  }

  /**
   * @param the name of the new playlist, or if null a default name will be used (e.g. "untitled")
   */
  public void editNewPlayList(String name)
  {
    // set up the metadata
    PlayList playList = new PlayList();
    playList.setTitle(name == null ? "untitled" : name);

    // send the request
    outstandingTransactionId = playListCatalog.requestCreatePlayList(playList);

    // make sure this tab is active
    client.showPlayListEditor();
  }

  /**
   *  Remove the currently selected items
   */
  public void removeSelectedItems()
  {
    int[] selectedRows = playListTablePanel.getTable().getSelectedRows();
    ArrayList mediaItems = playListTablePanel.getMetadata();

    for (int i=selectedRows.length - 1; i>=0; i--)
    {
      mediaItems.remove(selectedRows[i]);
    }

    playListTablePanel.updateModel(mediaItems);//XXX:0:20050111iain:
  }

  /**
   *  convert current playlist collection to havi type
   */
  private PlayList preparePlayList()
  {
    ArrayList playListItems = playListTablePanel.getMetadata();

    ContentId[] contentIds = new MlidContentId[playListItems.size()];
    int index = 0;
    for (Iterator iter = playListItems.iterator(); iter.hasNext(); index++)
    {
      ContentId contentId = ((ContentMetadata)iter.next()).getContentId();

      contentIds[index] = contentId;
    }

    MLID[] mlidList = Util.mlidContentIdArrayToMlidArray(contentIds);

    //XXX:00000:20050321iain: get rid of this, uses a non-clientlib type
    PlayList playList = new PlayList();
    playList.setTitle(playListNameField.getText().trim());

    byte irCode = 0;
    irCode = Byte.parseByte(playListIrCodeField.getText());

    playList.setIrcode(irCode);
    playList.setContent(mlidList);

    return playList;
  }

  /**
   *  insert items into the playlist
   *  @param items a list of ContentId objects
   */
  public void insertItems(int insertIndex, Collection items)
  {
    // convert the playlist back to a list of content ids
    //XXX:0:20050318iain: should not have to resolve back to ContentIds, table model should only keep mlids
    ArrayList playListItems = Util.contentMetadataCollectionToContentIdArrayList(playListTablePanel.getMetadata());

    // add the new items
    playListItems.addAll(insertIndex, items);

    // force an update
    playListTablePanel.updateModel(playListItems);

    // ensure we're visible
    client.showPlayListEditor();
  }

  /**
   *  insert all items from a given category summary
   */
  public void insertItemsFromCategories(int insertIndex, Collection items, int categoryType)
  {
    // should be no preexisting categories to explode
    assert remainingCategoriesToExplode.isEmpty();
    assert remainingCategoriesInsertIndex == -1;

    // keep items, index, type
    remainingCategoriesInsertIndex = insertIndex;
    remainingCategoriesToExplode.addAll(items);

    // set off the request chain
    requestRemainingCategoriesToExplode();
  }

  /**
   *
   */
  private void requestRemainingCategoriesToExplode()
  {
    // pop off a category
    CategorySummary summary = (CategorySummary)remainingCategoriesToExplode.remove(0);

    // make sure we're in the expected state
    assert remainingCategoriesInsertIndex != -1;
    assert outstandingTransactionId == StreetFireEvent.NOT_SET;

    // make the request
    outstandingTransactionId = mediaCatalog.requestMediaSummaries(summary.getType(), summary.getValue());
  }

  /**
   *
   */
  private void handleExplodeCategoryArrived(ContentIdsArrivedEvent e)
  {
    // deal with transaction id
    assert outstandingTransactionId == e.getTransactionId();
    outstandingTransactionId = StreetFireEvent.NOT_SET;

    // attempt to find metadata in the cache (cache will request metadata not found)
    List items = e.getContentIdsAsList();

    // insert the items,
    insertItems(remainingCategoriesInsertIndex, items);

    // bump insert index
    remainingCategoriesInsertIndex += items.size();

    if (remainingCategoriesToExplode.isEmpty())
    {
      // if done, reset vars
      remainingCategoriesInsertIndex = -1;
    }
    else
    {
      // otherwise, trigger the next request
      requestRemainingCategoriesToExplode();
    }
  }

  public EditPlayListAction getEditPlayListAction()
  {
    return new EditPlayListAction();
  }

  public AddToPlayListAction getAddToPlayListAction()
  {
    return new AddToPlayListAction();
  }

  public AddCategoryToPlayListAction getAddCategoryToPlayListAction()
  {
    return new AddCategoryToPlayListAction();
  }

  /**
   *  Request the playlist entries/metadata
   */
  private void requestContent()
  {
    // request the content ids of the playlist content and keep the the transaction ID
    playListTablePanel.setTransactionId(playListCatalog.requestPlayList(playlistId));
  }

  //-----------------------------------------------------------------------------------------------------

  /**
   *  Called by the event dispatcher to notify of an event
   */
  public void eventNotification(StreetFireEvent event)
  {
    if (event instanceof PlayListArrivedEvent)
    {
      handlePlaylist((PlayListArrivedEvent)event);
    }
    else if (event instanceof ContentIdsArrivedEvent)
    {
      handleContentIds((ContentIdsArrivedEvent)event);
    }
    else if (event instanceof ContentMetadataArrivedEvent)
    {
      handleMetadata((ContentMetadataArrivedEvent)event);
    }
    else if (event instanceof PlayListCreatedEvent)
    {
      handlePlaylistCreated((PlayListCreatedEvent)event);
    }
  }

  /**
   *  playlist has arrived
   */
  public void handlePlaylist(PlayListArrivedEvent e)
  {
    // determine the destination
    int transactionId = e.getTransactionId();
    if (transactionId == playListTablePanel.getTransactionId()) // the actual playlist (content ids)
    {
      PlayList pl = e.getPlayList();

      // keep title, IR code
      playListIrCodeField.setText(String.valueOf(pl.getIrcode()));
      playListNameField.setText(String.valueOf(pl.getTitle()));

      // create a new collections arraylist from the passed data
      playListTablePanel.updateModel(Util.mlidArrayToContentIdArrayList(pl.getContent()));

      // clear the transactionId
      playListTablePanel.setTransactionId(StreetFireEvent.NOT_SET);
    }
  }

  public void handleContentIds(ContentIdsArrivedEvent e)
  {
    if (e.getTransactionId() == outstandingTransactionId) // data for category explode
    {
      handleExplodeCategoryArrived(e);
    }
  }

  /**
   *  Metadata has arrived
   */
  public void handleMetadata(ContentMetadataArrivedEvent e)
  {
    ContentMetadata[] metadata = e.getMetadata();

    // see if it's the playlist metadata
    // XXX:0:20050318iain: funky to only check if there's only one entry
    if (metadata.length == 1 && metadata[0].getContentId().equals(playlistId))
    {
      //System.out.println("XXX:000000000000000000:iain:>>>>playList meta data is '" + e.getMetaData() + "'");
    }
    else
    {
      playListTablePanel.updateMetadata(metadata);
    }
  }

  public void handlePlaylistCreated(PlayListCreatedEvent e)
  {
    // confirm the destination
    int transactionId = e.getTransactionId();
    if (transactionId == outstandingTransactionId)
    {
      // update the playlistId
      playlistId = e.getNewPlaylistId();

      // clear the contents
      playListTablePanel.updateModel(new ArrayList());
      playListTablePanel.setTransactionId(StreetFireEvent.NOT_SET);
    }
  }


  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class NewAction extends AbstractAction
  {
    public NewAction()
    {
      super("New Playlist", ImageCache.getImageIcon("new.gif"));
      putValue(SHORT_DESCRIPTION, "Create a new playlist");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      editNewPlayList(null);
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class SaveAction extends AbstractAction
  {
    public SaveAction()
    {
      super("Save Playlist", ImageCache.getImageIcon("save.gif"));
      putValue(SHORT_DESCRIPTION, "Save changes to this playlist");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      savePlayList();
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class RemoveAction extends AbstractAction
  {
    public RemoveAction()
    {
      super("Remove", ImageCache.getImageIcon("icon_activeQueueClear.gif"));
      putValue(SHORT_DESCRIPTION, "Remove Selected Items");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    public void actionPerformed(ActionEvent e)
    {
      removeSelectedItems();
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:  sets the playlist to be edited and triggers an async content request
   *********************************************************************************/
  public class EditPlayListAction extends AbstractAction
  {
    private ContentIdsSource itemSource;

    public EditPlayListAction()
    {
      super("Edit Playlist", ImageCache.getImageIcon("playlistEditor.gif"));
      putValue(SHORT_DESCRIPTION, "Edits the currently selected playlist");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
    }

    public void setContentIdsSource(ContentIdsSource itemSource)
    {
      this.itemSource = itemSource;
    }

    /**
     *  Return a copy of the action.  Not using clone(), see Effective Java (Joshua Bloch), item 10
     */
    public EditPlayListAction getNewInstance()
    {
      EditPlayListAction action = new EditPlayListAction();
      action.itemSource = itemSource;
      return action;
    }

    public void actionPerformed(ActionEvent e)
    {
      List contentIds = itemSource.getContentIds();
      assert contentIds.size() == 1; //XXX:0000000000000:20050118iain: need button disabling

      // get the item
      ContentId playlistId = (ContentId)contentIds.get(0);
      //assert playList.getMediaType().equals(ConstMediaItemType.PLAY_LIST); // must be a playlist XXX:0:20050317iain: but can't check from ContentId...

      // edit the selected playlist
      editPlayList(playlistId);
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class AddToPlayListAction extends AbstractAction
  {
    private ContentIdsSource itemSource;

    public AddToPlayListAction()
    {
      super("Add To Playlist", ImageCache.getImageIcon("addToPlaylist.gif"));
      putValue(SHORT_DESCRIPTION, "Adds the currently selected items to the playlist");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
    }

    /**
     *  Return a copy of the action.  Not using clone(), see Effective Java (Joshua Bloch), item 10
     */
    public AddToPlayListAction getNewInstance()
    {
      AddToPlayListAction action = new AddToPlayListAction();
      action.itemSource = itemSource;
      return action;
    }

    public void setContentIdsSource(ContentIdsSource itemSource)
    {
      this.itemSource = itemSource;
    }

    public void actionPerformed(ActionEvent e)
    {
      // get the items from the item source associated with this action
      List items = itemSource.getContentIds();

      // make sure we got items of the right type
      assert Util.allEntriesAreAssignableTo(ContentId.class, items);

      // log
      LoggerSingleton.logDebugCoarse(AddToPlayListAction.class, "actionPerformed", "inserting " + items.size() + " items from source " + itemSource + " to playlist " + playlistId);

      // insert 'em
      insertItems(playListTablePanel.getMetadata().size(), items);
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class AddCategoryToPlayListAction extends AbstractAction
  {
    private CategorySummarySource categorySummarySource;

    public AddCategoryToPlayListAction()
    {
      super("Add To Playlist", ImageCache.getImageIcon("addToPlaylist.gif"));
      putValue(SHORT_DESCRIPTION, "Adds the currently selected items to the playlist");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
    }

    /**
     *  Return a copy of the action.  Not using clone(), see Effective Java (Joshua Bloch), item 10
     */
    public AddCategoryToPlayListAction getNewInstance()
    {
      AddCategoryToPlayListAction action = new AddCategoryToPlayListAction();
      action.categorySummarySource = categorySummarySource;
      return action;
    }

    public void setCategorySummarySource(CategorySummarySource categorySummarySource)
    {
      this.categorySummarySource = categorySummarySource;
    }

    public void actionPerformed(ActionEvent e)
    {
      // insert the items (does an async explode)
      insertItemsFromCategories(playListTablePanel.getMetadata().size(), categorySummarySource.getCategorySummaryItems(), categorySummarySource.getCategoryType());
    }
  }
}
