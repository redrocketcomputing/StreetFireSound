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
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.MediaCatalog;
import com.streetfiresound.clientlib.RemoteMediaPlayer;
import com.streetfiresound.clientlib.VersionedIndex;
import com.streetfiresound.clientlib.VersionedContentIdList;
import com.streetfiresound.clientlib.event.VersionedContentIdListArrivedEvent;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.PlayPositionUpdateEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;

// XXX:0:20050310iain: has quite a bit in common with PlayListEditPanel, consider using new common base

/**
 * Displays and provides actions for the now playing queue
 * @author iain huxley
 */
public class PlayQueuePanel extends StreetFirePanel implements StreetFireEventListener, ContentMetadataDoubleClickedListener
{
  private J2seClient          client;       // client object
  private MediaCatalog  mediaCatalog; // media catalog
  private RemoteMediaPlayer   mediaPlayer;  // media player

  // transaction tracking
  private int outstandingTransactionId = StreetFireEvent.NOT_SET;

  private int remainingCategoriesInsertIndex = -1;
  private List remainingCategoriesToExplode = new LinkedList();  // categories to be exploded and added to the playlist

  private VersionedIndex currentPlayQueueIndex = new VersionedIndex(-1, -1);
  private int lastVersion = -1; //XXX:0000000:20050322iain: ideally use versioned arraylists everywhere (including ContentMetadataTablePanel)

  private ContentId playListContentId = null;

  private ContentMetadataTablePanel playQueueTablePanel;   // playlist contents table panel

  private JPanel buttonPanel; // holds action buttons
  private JPanel borderedTablePanel;

  public PlayQueuePanel(J2seClient client)
  {
    // init members
    this.client  = client;
    mediaPlayer  = client.getMediaPlayer();
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

    // create and add the table panel - no double click listener, disallow sort, allow drop
    playQueueTablePanel = new ContentMetadataTablePanel(client, null, false, true, "play queue");

    // remove the track column and the genre column
    playQueueTablePanel.removeTrackColumn();
    playQueueTablePanel.removeGenreColumn();

    // create the button panel
    buttonPanel = new StreetFirePanel();
    buttonPanel.setLayout(new FlowLayout());

    // set up remove action
    RemoveAction removeAction = new RemoveAction();
    buttonPanel.add(new JButton(removeAction));

    // add the button panel
    add(buttonPanel, BorderLayout.SOUTH);

    // need bordered table panel as want texture to show through border
    borderedTablePanel = new StreetFirePanel();
    borderedTablePanel.add(playQueueTablePanel, BorderLayout.CENTER);
    borderedTablePanel.setBorder(UISettings.PANEL_BORDER_LOWERED);
    add(borderedTablePanel, BorderLayout.CENTER);

    // set double click listener
    playQueueTablePanel.setContentMetadataDoubleClickedListener(this);

    // set initial contents XXX:0:20050322iain: keep actual VersionedContentIdList so we have the version
    VersionedContentIdList playQueue = mediaPlayer.getPlayQueue();
    List queueContents = playQueue.getContentIds();
    assert queueContents != null;
    lastVersion = playQueue.getVersion();

    assert playQueueTablePanel != null;
    playQueueTablePanel.updateModel(new ArrayList(queueContents)); //XXX:0000000000000000000:20050322iain: wasteful copy (shallow) because of versioned queue immutability
  }

  /**
   *  Remove the currently selected items
   */
  public void removeSelectedItems()
  {
    // send a request to remove the items.  we'll be notified of the change, no need to update the table
    // XXX:000:20050310iain: assumes JTable always returns the selected rows in order
    mediaPlayer.removeItemsByIndex(Util.indexArrayToVersionedIndexArray(lastVersion, playQueueTablePanel.getTable().getSelectedRows()));
  }

//   /**
//    *  insert items into the playlist
//    *  @param items a list of ContentMetadata objects
//    */
//   public void insertItems(int insertIndex, Collection items)
//   {
//     // add the items to the list backing the table
//     ArrayList playListItems = playQueueTablePanel.getContentMetadata();
//     playListItems.addAll(insertIndex, items);

//     // force an update
//     playQueueTablePanel.updateModel(playListItems);

//     // ensure we're visible
//     client.showPlayListEditor();
//   }

//   /**
//    *  insert all items from a given category summary
//    */
//   public void insertItemsFromCategories(int insertIndex, Collection items, int categoryType)
//   {
//     // should be no preexisting categories to explode
//     assert remainingCategoriesToExplode.isEmpty();
//     assert remainingCategoriesInsertIndex == -1;

//     // keep items, index, type
//     remainingCategoriesInsertIndex = insertIndex;
//     remainingCategoriesToExplode.addAll(items);

//     // set off the request chain
//     requestRemainingCategoriesToExplode();
//   }

//   /**
//    *
//    */
//   private void requestRemainingCategoriesToExplode()
//   {
//     // pop off a category
//     CategorySummary summary = (CategorySummary)remainingCategoriesToExplode.remove(0);

//     // make sure we're in the expected state
//     assert remainingCategoriesInsertIndex != -1;
//     assert outstandingTransactionId == StreetFireEvent.NOT_SET;

//     // make the request
//     outstandingTransactionId = mediaCatalog.requestMediaSummaries(summary.getType(), summary.getValue());
//   }

//   /**
//    *
//    */
//   private void handleExplodeCategoryArrived(ContentMetadataArrivedEvent e)
//   {
//     // deal with transaction id
//     assert outstandingTransactionId == e.getTransactionId();
//     outstandingTransactionId = StreetFireEvent.NOT_SET;

//     // insert the items.
//     ContentMetadata[] metadata = e.getMetaData();
//     insertItems(remainingCategoriesInsertIndex, Arrays.asList(metadata));

//     // bump insert index
//     remainingCategoriesInsertIndex += metadata.length;

//     if (remainingCategoriesToExplode.isEmpty())
//     {
//       // if done, reset vars
//       remainingCategoriesInsertIndex = -1;
//     }
//     else
//     {
//       // otherwise, trigger the next request
//       requestRemainingCategoriesToExplode();
//     }
//   }

//   /**
//    *  Request the playlist entries/metadata
//    */
//   private void requestContent()
//   {
//     // request the content and keep the the transaction ID
//     playQueueTablePanel.setTransactionId(mediaCatalog.requestExpandedContentMetadata(playListMLID));
//   }


  //XXX:00000000000000000000000000000:20050118iain: should not be here
  public void contentMetadataDoubleClicked(JComponent source, ContentMetadata metadata, int index)
  {
    assert source == playQueueTablePanel.getTable();
    mediaPlayer.play(new VersionedIndex(index, lastVersion));
  }

  //-----------------------------------------------------------------------------------------------------

  /**
   *  Called by the event dispatcher to notify of an event
   */
  public void eventNotification(StreetFireEvent event)
  {
    if (event instanceof VersionedContentIdListArrivedEvent)
    {
      handleVersionedContentIdList((VersionedContentIdListArrivedEvent)event);
    }
    else if (event instanceof ContentMetadataArrivedEvent)
    {
      handleContentMetadata((ContentMetadataArrivedEvent)event);
    }
    else if (event instanceof PlayPositionUpdateEvent)
    {
      handlePlayPositionUpdate((PlayPositionUpdateEvent)event);
    }
  }

  /**
   *  playlist contents (as media ids) has arrived
   */
  public void handleVersionedContentIdList(VersionedContentIdListArrivedEvent e)
  {
    // determine the destination
    int transactionId = e.getTransactionId();
    if (transactionId == playQueueTablePanel.getTransactionId() || transactionId == StreetFireEvent.NOT_APPLICABLE)
    {
      VersionedContentIdList idList = e.getContentIdList();

      // keep version
      lastVersion = idList.getVersion();

      // create a new collections arraylist from the passed data
      playQueueTablePanel.updateModel(new ArrayList(idList.getContentIds())); //XXX:0:20050322iain: wasteful, array copy (shallow) because of versioned list immutability

      // clear the transactionId
      playQueueTablePanel.setTransactionId(StreetFireEvent.NOT_SET);
    }
    else if (transactionId == outstandingTransactionId) // data for category explode
    {
      //handleExplodeCategoryArrived(e);
    }
  }

  /**
   *  Metadata has arrived
   */
  public void handleContentMetadata(ContentMetadataArrivedEvent e)
  {
    playQueueTablePanel.updateMetadata(e.getMetadata());
  }

  /**
   *  Play queue index may have changed
   */
  public void handlePlayPositionUpdate(PlayPositionUpdateEvent e)
  {
    int newIndex = e.getPlayPosition().getIndex();

    //XXX:00000000000:20050322iain: confirm play position index version
    if (newIndex != currentPlayQueueIndex.index)
    {
      currentPlayQueueIndex = new VersionedIndex(newIndex, lastVersion);
      //      playQueueTablePanel.getTable().setRowSelectionInterval(newIndex, newIndex);
      playQueueTablePanel.setNowPlayingIndex(newIndex);
      playQueueTablePanel.getTable().repaint();
    }
  }

  public AddToPlayQueueAction getAddToPlayQueueAction()
  {
    return new AddToPlayQueueAction();
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class AddToPlayQueueAction extends AbstractAction
  {
    private ContentIdsSource itemSource;

    public AddToPlayQueueAction()
    {
      super("Queue", ImageCache.getImageIcon("queue.png"));
      putValue(SHORT_DESCRIPTION, "Add the selected items to the now playing queue");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    /**
     *  Return a copy of the action.  Not using clone(), see Effective Java (Joshua Bloch), item 10
     */
    public AddToPlayQueueAction getNewInstance()
    {
      AddToPlayQueueAction action = new AddToPlayQueueAction();
      action.itemSource = itemSource;
      return action;
    }

    public void setContentIdsSource(ContentIdsSource itemSource)
    {
      this.itemSource = itemSource;
    }

    public void actionPerformed(ActionEvent e)
    {
      // add the items
      mediaPlayer.addItems((ContentId[])(new ArrayList(itemSource.getContentIds()).toArray(new ContentId[0])));

      // make sure the play queue is showing
      client.showPlayQueue();
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
}
