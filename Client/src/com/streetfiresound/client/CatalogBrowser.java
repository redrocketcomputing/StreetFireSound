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
import java.awt.Toolkit;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.client.DiscEditPanel.EditDiscAction;
import com.streetfiresound.client.PlayListEditPanel.AddCategoryToPlayListAction;
import com.streetfiresound.client.PlayListEditPanel.AddToPlayListAction;
import com.streetfiresound.client.PlayListEditPanel.EditPlayListAction;
import com.streetfiresound.client.PlayQueuePanel.AddToPlayQueueAction;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.MediaCatalog;
import com.streetfiresound.clientlib.Util;
import com.streetfiresound.clientlib.event.CategorySummaryArrivedEvent;
import com.streetfiresound.clientlib.event.ContentIdsArrivedEvent;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.MediaCatalogChangedEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstCategoryType;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaItemType;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingConstants;
import java.awt.CardLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.ImageIcon;
import java.util.ArrayList;

/**
 *  Catalog Browser user interface
 *
 *  Responsibilities:
 *   - manages table requests and delivering responses to the appropriate table models
 *   - XXX:0000000000000:20050105iain: continue doc
 *  @author iain huxley
 */
public class CatalogBrowser extends StreetFirePanel implements StreetFireEventListener, ContentMetadataDoubleClickedListener
{
  // XXX:000:20050323iain: following based on order of ConstCategoryType
  public static final String[] CATEGORY_TYPE_NAME  = {"Type",     "Genre",     "Artist" };
  public static final String[] CATEGORY_TYPE_ICON  = {"save.gif", "genre.gif", "artist.gif" };

  // user selection tab result types (superset of ConstCategoryType)   XXX:000:20050323iain: kinda bogus, tightly tied to ConstCategoryType
  static final int USER_SELECTION_SUMMARIES_TYPE   = ConstCategoryType.TYPE;
  static final int USER_SELECTION_SUMMARIES_GENRE  = ConstCategoryType.GENRE;
  static final int USER_SELECTION_SUMMARIES_ARTIST = ConstCategoryType.ARTIST;
  static final int USER_SELECTION_EXPANDED_ITEM    = 3;
  static final int USER_SELECTION_SEARCH_RESULTS   = 4;

  public static final HashMap MEDIA_TYPE_ICONS = new HashMap();
  static
  {
    MEDIA_TYPE_ICONS.put(ConstMediaItemType.CDDA,        "disc.gif");
    MEDIA_TYPE_ICONS.put(ConstMediaItemType.PLAY_LIST,   "playlist.gif");
  }

  /** tabs or pulldown? */
  static final boolean USE_TABS = false;

  static final int CDS_VIEW            = 0;
  static final int ARTISTS_VIEW        = 1;
  static final int GENRES_VIEW         = 2;
  static final int TYPES_VIEW          = 3;
  static final int PLAYLISTS_VIEW      = 4;
  static final int USER_SELECTION_VIEW = 5;

  static final int NUM_VIEWS           = 6;

  private int          nextViewToAddIndex = 0; // used to make sure valid view indices are used

  private JTabbedPane  tabbedPane;            // primary UI element
  private StreetFirePanel catalogPanel;
  private CardLayout catalogPanelCardLayout;

  private JComboBox    viewByCombo;
  private J2seClient   client;                // client object
  private MediaCatalog mediaCatalog;          // media catalog, from client

  // table panels
  private CatalogBrowserCategoryPanel artistsPanel;
  private CatalogBrowserCategoryPanel genresPanel;
  private CatalogBrowserCategoryPanel typesPanel;
  private CatalogBrowserMediaPanel    cdsPanel;
  private CatalogBrowserMediaPanel    playListsPanel;
  private CatalogBrowserMediaPanel    userSelectionPanel;

  // search stuff
  private JTextField   searchTextField;
  private JComboBox    searchTypeCombo;
  private SearchAction searchAction;

  /** specifies the type of user selection visible in the user summary tab (USER_SELECTION_XXX), or -1 if not visible */
  private int userSelectionVariant = -1;

  /** if user selection is USER_SELECTION_SUMMARIES_XXX, the following var stores the match string (otherwise MUST be null) */
  private String userSelectionSummariesMatch = null;

  /** if user selection is USER_SELECTION_EXPANDED_ITEM, the following var stores the item's content id (otherwise MUST be null) */
  private ContentId userSelectionExpandedItem = null;

  /** if user selection is USER_SELECTION_SEARCH_RESULTS, the following var stores the containsstring (otherwise MUST be null) */
  private String userSelectionSearchContainsString = null;

  /**
   *  Construct a catalog browser
   */
  public CatalogBrowser(J2seClient client)
  {
    // init members
    this.client = client;
    mediaCatalog = client.getMediaCatalog();

    // initialize the user interface elements
    initUI();

    // listen for events
    client.getEventDispatcher().addListener(this);

    // disable UI until we've received the first summary
    // XXX:0000:20050104iain: slightly problematic as there are multiple panels that may want to change this state
    client.setUIEnabled(false);
  }

  /**
   *  Set up the user interface
   */
  private void initUI()
  {
    // must not init on a thread other than the AWT event dispatch thread
    assert Toolkit.getDefaultToolkit().getSystemEventQueue().isDispatchThread();

    if (UISettings.DEBUG_PANELS)
    {
      setBackground(Color.red);
    }

    // create search panel XXX:0:20050330iain: currently also includes view selection (non-tab mode), rename?
    StreetFirePanel searchPanel = new StreetFirePanel();
    searchPanel.setLayout(new FlowLayout());

    // create the view pane
    if (USE_TABS)
    {
      tabbedPane = new JTabbedPane();
      tabbedPane.setOpaque(false);
    }
    else
    {
      // create panel w/ cardlayout
      catalogPanel = new StreetFirePanel();
      catalogPanelCardLayout = new CardLayout();
      catalogPanel.setLayout(catalogPanelCardLayout);

      // create pulldown
      viewByCombo = new JComboBox();
      viewByCombo.setRenderer(new CellRenderer(SwingConstants.LEFT));
      viewByCombo.setMinimumSize(new Dimension(80, 37));
      viewByCombo.setPreferredSize(new Dimension(150, 37));
      viewByCombo.setMaximumSize(new Dimension(300, 37));

      // add
      searchPanel.add(new JLabel("View:"));
      searchPanel.add(viewByCombo);
    }

    searchPanel.add(new JLabel("Find:"));
    searchTextField  = new JTextField("");
    searchTextField.setPreferredSize(new Dimension(120, 20));
    searchTextField.setMinimumSize(new Dimension(60, 20));

    // "auto-go" on enter
    searchTextField.addKeyListener(new KeyAdapter()
      {
        public void keyTyped(KeyEvent e)
        {
          if (e.getKeyChar() == '\n')
          {
            searchAction.actionPerformed(new ActionEvent(searchTextField, -1, ""));
          }
        }
      }
                                   );

    searchPanel.add(searchTextField);
    searchAction = new SearchAction();

    JButton searchButton = new JButton(searchAction);
    searchPanel.add(searchButton);
    add(searchPanel, BorderLayout.NORTH);

    // create the table panels
    artistsPanel       = new CatalogBrowserCategoryPanel(this, ConstCategoryType.ARTIST, "artist panel");
    genresPanel        = new CatalogBrowserCategoryPanel(this, ConstCategoryType.GENRE,  "genre panel");
    typesPanel         = new CatalogBrowserCategoryPanel(this, ConstCategoryType.TYPE,   "type panel");
    cdsPanel           = new CatalogBrowserMediaPanel(this, "cds panel");
    playListsPanel     = new CatalogBrowserMediaPanel(this, "playlists panel");
    userSelectionPanel = new CatalogBrowserMediaPanel(this, "user selection panel");

    // remove some unnecessary columns
    cdsPanel.getTablePanel().removeTrackColumn();
    playListsPanel.getTablePanel().removeTrackColumn();
    playListsPanel.getTablePanel().removeSlotColumn();
    playListsPanel.getTablePanel().removePlayerColumn();
    playListsPanel.getTablePanel().removeArtistColumn();

    // cds panel allows art to be turned on
    cdsPanel.addToggleArtButton();

    // sort cds panel by slot then track
    cdsPanel.getTablePanel().setSorted(2); //XXX:0:20050324iain: don't use hardcoded column #s
    cdsPanel.getTablePanel().setSorted(1);

    // add the views
    addView(CDS_VIEW,       "CDs",       "discs.gif",    cdsPanel);
    addView(ARTISTS_VIEW,   "Artists",   "artist.gif",   artistsPanel);
    addView(GENRES_VIEW,    "Genres",    "genre.gif",    genresPanel);
    addView(TYPES_VIEW,     "Types",      null,          typesPanel);
    addView(PLAYLISTS_VIEW, "Playlists", "playlist.gif", playListsPanel);

    // add a change listener so that content can be requested when a view is selected
    // and add the tabbed view panel
    if (USE_TABS)
    {
      tabbedPane.addChangeListener(new TabChangeListener());
      tabbedPane.setOpaque(false);
      add(tabbedPane, BorderLayout.CENTER);
    }
    else
    {
      viewByCombo.addItemListener(new ViewByItemListener());
      add(catalogPanel);
    }

    requestContentForCurrentView();
  }

  public J2seClient getClient()
  {
    return client;
  }

  /**
   *  get the next disc to be edited
   */
  public ContentMetadata getNextDiscToBeEdited(ContentId currentDisc)
  {
    ContentMetadata nextDisc = cdsPanel.goToNextItem(currentDisc);
    //assert nextDisc.getMediaLocationId().getIndex() == 0; // make sure it's the right index for a disc (master index = 0)
    assert nextDisc.getMediaType().equals(ConstMediaItemType.CDDA); // make sure it's of the right media type
    return nextDisc;
  }

  /**
   *  Adds the action for adding media items to a playlist
   */
  public void addAddToPlayQueueAction(AddToPlayQueueAction action)
  {
    cdsPanel.addAddToPlayQueueAction(action);
    userSelectionPanel.addAddToPlayQueueAction(action.getNewInstance());
  }

  /**
   *  Adds the action for adding media items to a playlist
   */
  public void addAddToPlayListAction(AddToPlayListAction action)
  {
    cdsPanel.addAddToPlayListAction(action);
    userSelectionPanel.addAddToPlayListAction(action.getNewInstance());
  }

  /**
   *  Adds the action for adding categories to a playlist
   */
  public void addAddToPlayListAction(AddCategoryToPlayListAction action)
  {
    artistsPanel.addAddToPlayListAction(action);
    genresPanel.addAddToPlayListAction(action.getNewInstance());
    typesPanel.addAddToPlayListAction(action.getNewInstance());
  }

  /**
   *  Adds the action for editing disc information
   */
  public void addEditDiscAction(EditDiscAction action)
  {
    cdsPanel.addEditDiscAction(action);
    userSelectionPanel.addEditDiscAction(action.getNewInstance());
  }

  /**
   *  Adds the action for editing a playlist
   */
  public void addEditPlayListAction(EditPlayListAction action)
  {
    playListsPanel.addEditPlayListAction(action);
  }

  //XXX:00000000000000000000000000000:20050118iain: should not be here
  public void contentMetadataDoubleClicked(JComponent source, ContentMetadata metadata, int index)
  {
    // if it was a playlist, edit the playlist
    if (source == playListsPanel.getTablePanel().getTable())
    {
      client.editPlayList(metadata);
    }
    else // otherwise browse the media
    {
      //XXX:0000000000000000000:20050302iain:
      showUserSelectionTabExpandedItem(metadata);
    }
  }

  public void saveCDList(File file) throws IOException
  {
    client.saveCDList(file, cdsPanel.getTablePanel().getMetadata());
  }

  /**
   *  send a request for the contents of the current tab
   */
  private void requestContentForCurrentView()
  {
    int index = USE_TABS ? tabbedPane.getSelectedIndex() : viewByCombo.getSelectedIndex();

    switch (index)
    {
      case ARTISTS_VIEW:
        // request the data for the view, setting the transaction ID in the panel
        artistsPanel.getTablePanel().setTransactionId(mediaCatalog.requestCategorySummaries(ConstCategoryType.ARTIST));
        break;

      case GENRES_VIEW:
        // request the data for the view, setting the transaction ID in the panel
        genresPanel.getTablePanel().setTransactionId(mediaCatalog.requestCategorySummaries(ConstCategoryType.GENRE));
        break;

      case TYPES_VIEW:
        // request the data for the view, setting the transaction ID in the panel
        typesPanel.getTablePanel().setTransactionId(mediaCatalog.requestCategorySummaries(ConstCategoryType.TYPE));
        break;

      case CDS_VIEW:
        // request the data for the initial view, setting the transaction ID in the panel
        cdsPanel.getTablePanel().setTransactionId(mediaCatalog.requestMediaSummaries(ConstCategoryType.TYPE, ConstMediaItemType.CDDA));
        break;

      case PLAYLISTS_VIEW:
        // request the data for the initial view, setting the transaction ID in the panel
        playListsPanel.getTablePanel().setTransactionId(mediaCatalog.requestMediaSummaries(ConstCategoryType.TYPE, ConstMediaItemType.PLAY_LIST));
        break;

      case USER_SELECTION_VIEW:
        // request the data for the initial view, setting the transaction ID in the panel
        requestContentForUserSelectionTab();
        break;

      default:
        LoggerSingleton.logError(this.getClass(), "requestContentForCurrentView", "bad tab index");
    }
  }

  /**
   *  Add a tab to the tabbed pane
   *  @param index must be 0 the first time called, 1 the second, 2 the third etc. etc. (used to validate order)
   */
  private void addView(int index, String title, String imageFile, JComponent component)
  {
    // check the index validity
    if (nextViewToAddIndex++ != index)
    {
      throw new IllegalArgumentException("invalid tab index order");
    };

    // add it
    if (USE_TABS)
    {
      tabbedPane.addTab(title, imageFile != null ? ImageCache.getImageIcon(imageFile) : null, component);
    }
    else
    {
      viewByCombo.addItem(new JLabel(title, imageFile != null ? ImageCache.getImageIcon(imageFile) : null, SwingConstants.LEFT));
      catalogPanel.add(component, String.valueOf(index));
    }
  }

  // ------------ SECTION: user selection --------------------------

  /**
   *  used for debugging only, checks to make sure that the user selection variables
   *  are in valid states for the userSelectionVariant selected
   */
  private boolean userSelectionMembersValid()
  {
    boolean valid;
    switch (userSelectionVariant)
    {
      case -1:
        valid = (userSelectionSummariesMatch == null && userSelectionExpandedItem == null && userSelectionSearchContainsString == null);
        break;

      case USER_SELECTION_SUMMARIES_GENRE:
      case USER_SELECTION_SUMMARIES_TYPE:
      case USER_SELECTION_SUMMARIES_ARTIST:
        valid = (userSelectionSummariesMatch != null && userSelectionExpandedItem == null && userSelectionSearchContainsString == null);
        break;

      case USER_SELECTION_EXPANDED_ITEM:
        valid = (userSelectionSummariesMatch == null && userSelectionExpandedItem != null && userSelectionSearchContainsString == null);
        break;

      case USER_SELECTION_SEARCH_RESULTS:
        valid = (userSelectionSummariesMatch == null && userSelectionExpandedItem == null && userSelectionSearchContainsString != null);
        break;

      default:
        return false; // bogus variant
    }
    if (!valid)
    {
      LoggerSingleton.logError(CatalogBrowser.class,  "userSelectionMembersValid", "bad user selection state! ["
                                                    + "variant=" + userSelectionVariant
                                                    + ",summMatch=" + userSelectionSummariesMatch
                                                    + ",expandedItem=" + userSelectionExpandedItem
                                                    + ",searchContains=" + userSelectionSearchContainsString + "]");

    }
    return valid;
  }

  private void requestContentForUserSelectionTab()
  {
    // make sure we're in a valid state
    assert userSelectionMembersValid();

    // clear existing stuff
    userSelectionPanel.getTablePanel().updateModel(new ArrayList(0));

    // perform the request
    switch (userSelectionVariant)
    {
      case USER_SELECTION_SUMMARIES_GENRE:
        userSelectionPanel.getTablePanel().setTransactionId(mediaCatalog.requestMediaSummaries(ConstCategoryType.GENRE, userSelectionSummariesMatch));
        break;

      case USER_SELECTION_SUMMARIES_TYPE:
        userSelectionPanel.getTablePanel().setTransactionId(mediaCatalog.requestMediaSummaries(ConstCategoryType.TYPE, userSelectionSummariesMatch));
        break;

      case USER_SELECTION_SUMMARIES_ARTIST:
        userSelectionPanel.getTablePanel().setTransactionId(mediaCatalog.requestMediaSummaries(ConstCategoryType.ARTIST, userSelectionSummariesMatch));
        break;

      case USER_SELECTION_EXPANDED_ITEM:
        userSelectionPanel.getTablePanel().setTransactionId(mediaCatalog.requestExpandItem(userSelectionExpandedItem));
        break;

      case USER_SELECTION_SEARCH_RESULTS:
        userSelectionPanel.getTablePanel().setTransactionId(mediaCatalog.requestSearch(userSelectionSearchContainsString));
        break;

      case -1:
      default:
        throw new IllegalStateException("tried to request contents for user selection with invalid variant " + userSelectionVariant);
    }
  }

  /**
   * Display a tab for the user selected item (creating if necessary) and request the contents
   */
  void showUserSelectionSummaryTab(int categoryType, String match)
  {
    // check argument validity
    assert categoryType > 0 && categoryType <= 2;

    // check validity of user selection state
    assert userSelectionMembersValid();

    // USER_SELECTION_XXX is superset of categorytype, can assign
    userSelectionVariant = categoryType;

    // keep match string (may need refresh) and reset other (now stale) user selection members
    userSelectionSummariesMatch       = match;
    userSelectionExpandedItem         = null;
    userSelectionSearchContainsString = null;

    // figure out icon, title
    String tabTitle = CATEGORY_TYPE_NAME[categoryType] + ":" + match;

    // activate the tab
    activateUserSelectionView(tabTitle, CATEGORY_TYPE_ICON[categoryType]);

    // check validity of user selection state
    assert userSelectionMembersValid();
  }

  /**
   * Display a tab for the user selected item (creating if necessary)
   * and request the contents
   */
  void showUserSelectionTabExpandedItem(ContentMetadata metadata)
  {
    // check validity of user selection state
    assert userSelectionMembersValid();

    // set variant
    userSelectionVariant = USER_SELECTION_EXPANDED_ITEM;

    // keep selection, may need refreshing, and reset other (now stale) user selection members
    userSelectionExpandedItem         = metadata.getContentId();
    userSelectionSummariesMatch       = null;
    userSelectionSearchContainsString = null;

    // figure out what icon to use
    String iconFile = (String)MEDIA_TYPE_ICONS.get(metadata.getMediaType());

    // activate the tab
    activateUserSelectionView(metadata.getTitle(), iconFile);

    // don't sort CD contents
    if (metadata.getMediaType().equals(ConstMediaItemType.CDDA))
    {
      userSelectionPanel.getTablePanel().setSorted(-1);
    }
    else
    {
      userSelectionPanel.getTablePanel().setSorted(1); //XXX:000000:20050324iain: hardcoded
    }

    // check validity of user selection state
    assert userSelectionMembersValid();
  }

  /**
   * Display a tab for the user selected item (creating if necessary) and request the contents
   */
  void showUserSelectionSearchTab(String contains)
  {
    // check validity of user selection state
    assert userSelectionMembersValid();

    // set variant
    userSelectionVariant = USER_SELECTION_SEARCH_RESULTS;

    // keep contains string (may need refresh) and reset other (now stale) user selection members
    userSelectionSearchContainsString = contains;
    userSelectionSummariesMatch       = null;
    userSelectionExpandedItem         = null;

    // activate tab
    activateUserSelectionView("Contains \"" + userSelectionSearchContainsString + "\"", "search.gif");

    // check validity of user selection state
    assert userSelectionMembersValid();
  }

  private int getNumViews()
  {
    return USE_TABS ? tabbedPane.getTabCount() : viewByCombo.getItemCount();
  }

  private void activateUserSelectionView(String tabTitle, String iconFile)
  {
    // XXX:0:20050104iain: kinda nasty ahead
    // see if a new tab is needed
    if (getNumViews() != NUM_VIEWS) //XXX:000000:20041220iain:
    {
      // need new tab
      addView(USER_SELECTION_VIEW, tabTitle, iconFile, userSelectionPanel);
    }
    else
    {
      // reuse existing tab
      setUserSelectionLabel(tabTitle, ImageCache.getImageIcon(iconFile));
    }

    if (getCurrentView() != USER_SELECTION_VIEW)
    {
      setCurrentView(USER_SELECTION_VIEW);
    }
    else
    {
      // otherwise, the tab is active, just request the data
      requestContentForUserSelectionTab();
    }
  }

  private void setCurrentView(int tabNumber)
  {

    if (USE_TABS)
    {
      // set the tab active (will generate a tab change event which requests data)
      tabbedPane.setSelectedIndex(tabNumber);
    }
    else
    {
      viewByCombo.setSelectedIndex(tabNumber);
      //      catalogPanelCardLayout.showItem(catalogPanel
    }
  }

  private int getCurrentView()
  {
    if (USE_TABS)
    {
      return tabbedPane.getSelectedIndex();
    }
    else
    {
      return viewByCombo.getSelectedIndex();
    }
  }


  private void setUserSelectionLabel(String text, ImageIcon icon)
  {
    if (USE_TABS)
    {
      // reuse existing tab
      tabbedPane.setIconAt(USER_SELECTION_VIEW, icon);
      tabbedPane.setTitleAt(USER_SELECTION_VIEW, text);
    }
    else
    {
      JLabel label = (JLabel)viewByCombo.getItemAt(USER_SELECTION_VIEW);
      label.setText(text);
      label.setIcon(icon);
    }
  }

  //----------------------------------------------------------------------------

  /**
   *  Called by the event dispatcher to notify of an event
   */
  public void eventNotification(StreetFireEvent event)
  {
    // XXX:0:20041223iain: consider using a hashmap to track transactions
    if (event instanceof CategorySummaryArrivedEvent)
    {
      handleCategorySummary((CategorySummaryArrivedEvent)event);
    }
    else if (event instanceof ContentIdsArrivedEvent)
    {
      handleContentIds((ContentIdsArrivedEvent)event);
    }
    else if (event instanceof ContentMetadataArrivedEvent)
    {
      handleMetadata((ContentMetadataArrivedEvent)event);
    }
    else if (event instanceof MediaCatalogChangedEvent)
    {
//       // media catalog change notification
//       MediaCatalogChangedEvent e = (MediaCatalogChangedEvent)event;

      // XXX:0:20050330iain: may not need to request content, need to figure out what "hint" specifies.

      // request an update
      requestContentForCurrentView();
    }
  }

  /**
   * category summaries have arrived
   */
  private void handleCategorySummary(CategorySummaryArrivedEvent e)
  {
    // determine the destination
    int transactionId = e.getTransactionId();
    if (transactionId == artistsPanel.getTablePanel().getTransactionId())
    {
      artistsPanel.getTablePanel().updateModel(e);
      artistsPanel.getTablePanel().setTransactionId(StreetFireEvent.NOT_SET);
    }
    else if (transactionId == genresPanel.getTablePanel().getTransactionId())
    {
      genresPanel.getTablePanel().updateModel(e);
      genresPanel.getTablePanel().setTransactionId(StreetFireEvent.NOT_SET);
    }
    else if (transactionId == typesPanel.getTablePanel().getTransactionId())
    {
      typesPanel.getTablePanel().updateModel(e);
      typesPanel.getTablePanel().setTransactionId(StreetFireEvent.NOT_SET);
    }
    else
    {
      //LoggerSingleton.logError(this.getClass(), "eventNotification", "unknown transaction id: " + transactionId);
    }
  }

  /**
   *  Handle incoming media location ids
   */
  private void handleContentIds(ContentIdsArrivedEvent e)
  {
    // determine the destination
    int transactionId = e.getTransactionId();

    // check for a destination
    ContentMetadataTablePanel metadataTablePanel = null;
    if (transactionId == cdsPanel.getTablePanel().getTransactionId())
    {
      metadataTablePanel = cdsPanel.getTablePanel();
    }
    else if (transactionId == playListsPanel.getTablePanel().getTransactionId())
    {
      metadataTablePanel = playListsPanel.getTablePanel();
    }
    else if (transactionId == userSelectionPanel.getTablePanel().getTransactionId())
    {
      metadataTablePanel = userSelectionPanel.getTablePanel();
    }

    // check if we fell through with no hit
    if (metadataTablePanel == null)
    {
      //LoggerSingleton.logDebugFine(this.getClass(), "eventNotification", "unknown transaction id: " + transactionId);
    }
    else
    {
      // update the model
      metadataTablePanel.updateModel(e.getContentIdsAsList()); //XXX:0000000000:20050106iain:

      // clear the transaction id
      metadataTablePanel.setTransactionId(StreetFireEvent.NOT_SET);
    }
  }

  /**
   *  handle incoming metadata arrived
   */
  private void handleMetadata(ContentMetadataArrivedEvent e)
  {
    // currently track request returns metadata not ContentIds, yuck
    if (userSelectionPanel.getTablePanel().getTransactionId() == e.getTransactionId())
    {
      userSelectionPanel.getTablePanel().updateModel(Util.contentMetadataArrayToContentIdArrayList(e.getMetadata()));
    }

    // all panels may be interested
    // XXX:00+0:20050308iain: make panel a listener directly?
    cdsPanel.getTablePanel().updateMetadata(e.getMetadata());
    playListsPanel.getTablePanel().updateMetadata(e.getMetadata());
    userSelectionPanel.getTablePanel().updateMetadata(e.getMetadata());
  }

  /*********************************************************************************
   * NON-PUBLIC INNER CLASS: listens for tab changes so that content may be requested etc.
   *********************************************************************************/
  class TabChangeListener implements ChangeListener
  {
    // different tab was selected
    public void stateChanged(ChangeEvent e)
    {
      // request content for the currently selected tab
      requestContentForCurrentView();
    }
  }

  /*********************************************************************************
   * NON-PUBLIC INNER CLASS: listens for tab changes so that content may be requested etc.
   *********************************************************************************/
  class ViewByItemListener implements ItemListener
  {
    // different tab was selected
    public void itemStateChanged(ItemEvent e)
    {
      // don't care about des
      if (e.getStateChange() == ItemEvent.DESELECTED)
      {
        return;
      }

      catalogPanelCardLayout.show(catalogPanel, String.valueOf(viewByCombo.getSelectedIndex()));

      // request content for the currently selected tab
      requestContentForCurrentView();
    }
  }

  /*********************************************************************************
   * NON-PUBLIC INNER CLASS: search
   *********************************************************************************/
  class SearchAction extends AbstractAction
  {
    public SearchAction()
    {
      super("Go");//, ImageCache.getImageIcon("fullMode.gif"));
      putValue(SHORT_DESCRIPTION, "Search for any item with metadata containing the specified string");
    }

    public void actionPerformed(ActionEvent e)
    {
      showUserSelectionSearchTab(searchTextField.getText().trim());
    }
  }
}
