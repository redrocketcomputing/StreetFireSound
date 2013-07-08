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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.streetfiresound.client.generatedui.DiscEditHeaderPanel;
import com.streetfiresound.clientlib.ClientRuntimeException;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.MediaCatalog;
import com.streetfiresound.clientlib.RequestDiscArtTask;
import com.streetfiresound.clientlib.Util;
import com.streetfiresound.clientlib.MlidContentId;
import com.streetfiresound.clientlib.LookupTask;
import com.streetfiresound.clientlib.LookupTask.LookupRequest;
import com.streetfiresound.clientlib.event.LookupResultsArrivedEvent;
import com.streetfiresound.clientlib.event.ContentIdsArrivedEvent;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.OperationSuccessfulEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import com.streetfiresound.clientlib.event.ThumbnailsReadyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import org.havi.dcm.types.HUID;

/**
 *  Presents a UI for editing metadata for a disc and constituent tracks
 *  @author iain huxley
 */
public class DiscEditPanel extends StreetFirePanel implements StreetFireEventListener
{
  private J2seClient      client;          // client object
  private MediaCatalog    mediaCatalog;    // media catalog

  private int outstandingTransactionId = StreetFireEvent.NOT_SET;

  private ContentId       discContentId = null;
  private ContentMetadata discMetadata  = null; // metadata for the entire disc
  private List            trackMetadata = null; // metadata for each track, derived from received ids and used when sending back edits; will not be up to date if there are unsaved edits

  private DiscEditHeaderPanel headerPanel;
  private JPanel tracksPanel;
  private JPanel buttonPanel; // holds action buttons

  private JPanel discArtPanel;
  private JComboBox discArtCombo;
  private JLabel lookupLabel = new JLabel("looking up...");
  public static final int COL1_WIDTH = 55;

  public DiscEditPanel(J2seClient client)
  {
    // init members
    this.client = client;
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
    StreetFirePanel scrollingPanel = new StreetFirePanel();
    JScrollPane scrollPane = new JScrollPane(scrollingPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setOpaque(false);
    scrollPane.setBorder(UISettings.PANEL_BORDER_LOWERED);

    JViewport viewport = scrollPane.getViewport();
    viewport.setOpaque(false);
    //viewport.setLayout(new BorderLayout());
    //viewport.add(scrollingPanel, BorderLayout.CENTER);

    // set up header panel
    headerPanel = new DiscEditHeaderPanel();
    headerPanel.setOpaque(false);
    scrollingPanel.add(headerPanel, BorderLayout.NORTH);

    // set up disc art panel
    discArtPanel = new StreetFirePanel();
    discArtPanel.setLayout(new FlowLayout());
    //    discArtPanel.setPreferredSize(new Dimension(
    discArtCombo = new JComboBox();
    discArtPanel.add(discArtCombo);
    //discArtCombo.addActionListener(new SaveSelectedArtListener());

    // set up track panel
    tracksPanel = new StreetFirePanel();
    tracksPanel.setLayout(new BoxLayout(tracksPanel, BoxLayout.Y_AXIS));
    tracksPanel.add(discArtPanel);
    scrollingPanel.add(tracksPanel, BorderLayout.CENTER);
    //tracksPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
    //tracksPanel.setPreferredSize(new Dimension(400, 0));


    // create the button panel
    buttonPanel = new StreetFirePanel();
    buttonPanel.setLayout(new FlowLayout());

    // set up actions

    SaveAction saveAction = new SaveAction();
    buttonPanel.add(new JButton(saveAction));

    NextAction nextAction = new NextAction();
    buttonPanel.add(new JButton(nextAction));

    LookupAction lookupAction = new LookupAction();
    buttonPanel.add(new JButton(lookupAction));

//     ScanAction scanAction = new ScanAction();
//     buttonPanel.add(new JButton(scanAction));

    // add the button panel and scroll pane
    add(buttonPanel, BorderLayout.SOUTH);
    add(scrollPane, BorderLayout.CENTER);
  }

  /**
   *  save the disc currently being edited
   */
  public void save()
  {
    assert outstandingTransactionId == StreetFireEvent.NOT_SET;

    // first array entry is common (summary) info
    discMetadata.setTitle (headerPanel.getTitle().getText());
    discMetadata.setGenre (headerPanel.getGenre().getText());
    discMetadata.setArtist(headerPanel.getArtist().getText());

    // get the track panel
    Component[] components = tracksPanel.getComponents();

    // now set the track titles
    int componentIndex = 0;
    for (Iterator i = trackMetadata.iterator(); i.hasNext(); )
    {
      Component component = components[componentIndex++];
      if (!(component instanceof TrackPanel))
      {
        continue;
      }
      TrackPanel trackPanel = (TrackPanel)component;
      ((ContentMetadata)i.next()).setTitle(trackPanel.textField.getText());
    }

    // send the request
    outstandingTransactionId = mediaCatalog.requestPutMetadata(discMetadata, trackMetadata);

    // save the cover art
    ImageIcon icon = (ImageIcon)discArtCombo.getSelectedItem();
    if (icon != null)
    {
      RequestDiscArtTask.saveThumbs(icon.getImage(), discMetadata, client, this);
    }
  }

  /**
   *  Request the playlist entries/metadata
   */
  private void requestContent()
  {
    assert outstandingTransactionId == StreetFireEvent.NOT_SET;

    // request the content and keep the the transaction ID
    outstandingTransactionId = mediaCatalog.requestExpandItem(discContentId);
  }

  /**
   *  Called by the event dispatcher to notify of an event
   *  ---------------------------------------------------------------------------------------
   */
  public void eventNotification(StreetFireEvent event)
  {
    if (event instanceof ContentIdsArrivedEvent)
    {
      // handle it
      handleContentIdsArrived((ContentIdsArrivedEvent) event);
    }
    else if (event instanceof ContentMetadataArrivedEvent)
    {
      // handle it
      handleContentMetadataArrived((ContentMetadataArrivedEvent) event);
    }
    else if (event instanceof LookupResultsArrivedEvent)
    {
      // handle it
      setDiscMetadata(((LookupResultsArrivedEvent)event).getMetadata());
      tracksPanel.remove(lookupLabel);
    }
    else if (event instanceof ThumbnailsReadyEvent)
    {
      // handle it
      handleThumbnailsReady((ThumbnailsReadyEvent)event);
    }
    else if (event instanceof OperationSuccessfulEvent)
    {
      // means save was successful
      if (event.getTransactionId() == outstandingTransactionId)
      {
        // clear transaction id only
        outstandingTransactionId = StreetFireEvent.NOT_SET;
      }
    }
    else
    {
      //LoggerSingleton.logDebugFine(this.getClass(), "eventNotification", "unknown transaction id: " + event.getTransactionId());
    }
  }

  /**
   *  metadata for this disc has arrived
   */
  private void handleContentIdsArrived(ContentIdsArrivedEvent e)
  {
    // determine the destination
    int tid = e.getTransactionId();
    if (outstandingTransactionId == tid)
    {
      ContentId[] contentIds = e.getContentIds();

      // make trackContentIds list
      ArrayList trackContentIds = Util.contentIdArrayToContentIdArrayList(contentIds);

      // get the metadata from the cache, using dummies
      trackMetadata = client.getMetadataCache().getMetadata(trackContentIds, true);

      // update UI
      updateFields();

      // reset transaction ID
      outstandingTransactionId = StreetFireEvent.NOT_SET;
    }
  }

  private void setDiscMetadata(List discItems)
  {
    LinkedList list = new LinkedList(discItems);

    //XXX:0:20050401iain: hack - patch up HUIDs, will be null
    HUID huid = ((MlidContentId)discMetadata.getContentId()).getMlid().getHuid();

    for (Iterator i=list.iterator(); i.hasNext(); )
    {
      ContentMetadata metadata = (ContentMetadata)i.next();
      ((MlidContentId)metadata.getContentId()).getMlid().setHuid(huid);
    }

    discMetadata = (ContentMetadata)list.remove(0);

    trackMetadata = list;

    updateFields();
  }

  /**
   *  metadata for this disc has arrived
   */
  private void handleContentMetadataArrived(ContentMetadataArrivedEvent e)
  {

  }

  public void handleThumbnailsReady(ThumbnailsReadyEvent e)
  {
    if (discMetadata != null && !e.getDiscMetadata().getContentId().equals(discMetadata.getContentId()))
    {
      // not for us
      return;
    }
    discArtCombo.addItem(new ImageIcon(e.getLargestThumb()));
  }

  private void updateFields()
  {
    // init text fields
    headerPanel.getTitle() .setText(discMetadata.getTitle());
    headerPanel.getArtist().setText(discMetadata.getArtist());
    headerPanel.getGenre() .setText(discMetadata.getGenre());

//     if (
//XXX:000000000000000000000000000000000000000000000000000000:20050324iain:
tracksPanel.removeAll();

    // add the tracks

    discArtPanel.removeAll();
    discArtCombo.removeAllItems();
    discArtPanel.add(discArtCombo);

    // re-add disc art panel XXX:0000:20050119iain:hack
    tracksPanel.add(discArtPanel);
    tracksPanel.validate();
    discArtPanel.validate();
    validate();

    // look for some cover art
    if (   !discMetadata.getTitle().equalsIgnoreCase(Util.TOKEN_UNKNOWN)
        && !discMetadata.getTitle().equalsIgnoreCase(Util.TOKEN_EMPTY  ))
    {
      try
      {
        client.getTaskPool().execute(new RequestDiscArtTask(client, discMetadata, this));
      }
      catch (TaskAbortedException ex)
      {
        throw new ClientRuntimeException("Disc art search task aborted", ex);
      }
    }

    for (Iterator i=trackMetadata.iterator(); i.hasNext(); )
    {
      ContentMetadata iteratorTrackMetadata = (ContentMetadata)i.next();
      tracksPanel.add(new TrackPanel("Track " + iteratorTrackMetadata.getTrackNumber(), iteratorTrackMetadata.getTitle()));
    }

    tracksPanel.add(new Box.Filler(new Dimension(5,5), new Dimension(5,5), new Dimension(5, Short.MAX_VALUE)));
    tracksPanel.setMaximumSize(new Dimension(0, (trackMetadata.size()) * 22));
  }


  /**
   *  Sets the disc to be edited and triggers an async content request
   */
  public void editDisc(ContentId contentId)
  {
    // set the disc ContentId
    discContentId = contentId;

    // request the root metadata, we'll almost always have it in the cache
    discMetadata = client.getMetadataCache().getMetadata(contentId, true);

    // get the content
    requestContent();

    // make sure this tab is active
    client.showDiscEditor();
  }

  public EditDiscAction getEditDiscAction()
  {
    return new EditDiscAction();
  }

  private void editNextDisc()
  {
    editDisc(client.getNextDiscToBeEdited(discContentId).getContentId());
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class SaveAction extends AbstractAction
  {
    public SaveAction()
    {
      super("Save", ImageCache.getImageIcon("save.gif"));
      putValue(SHORT_DESCRIPTION, "Save changes to this playlist");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      save();
    }
  }


  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class NextAction extends AbstractAction
  {
    public NextAction()
    {
      super("Next Disc", ImageCache.getImageIcon("nextarrow.gif"));
      putValue(SHORT_DESCRIPTION, "Got to the next disc");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      editNextDisc();
    }
  }


  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class EditDiscAction extends AbstractAction
  {
    private ContentIdsSource itemSource;

    public EditDiscAction()
    {
      super("Edit Disc", ImageCache.getImageIcon("discEdit.gif"));
      putValue(SHORT_DESCRIPTION, "Edit disc/track info for this disc");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    }

    public void setContentIdsSource(ContentIdsSource itemSource)
    {
      this.itemSource = itemSource;
    }

    /**
     *  Return a copy of the action.  Not using clone(), see Effective Java (Joshua Bloch), item 10
     */
    public EditDiscAction getNewInstance()
    {
      EditDiscAction action = new EditDiscAction();
      action.itemSource = itemSource;
      return action;
    }

    public void actionPerformed(ActionEvent e)
    {
      // get items
      List items = itemSource.getContentIds();

      // should only be one item selected
      assert items.size() == 1;
      ContentId itemContentId = (ContentId)items.get(0);

      // must be either a track or a disc
      //XXX:0:20050321iain: use cache to find out from contentid
      //assert metadata.getMediaType().equals(ConstMediaItemType.CDDA);

      // find the root entry (the disc)
      discContentId = itemContentId.getRootEntryContentId();

      // edit it
      editDisc(discContentId);
    }
  }

  /*********************************************************************************
   * STATIC INNER CLASS:
   *********************************************************************************/
  static class TrackPanel extends StreetFirePanel
  {
    static final Dimension maxDim = new Dimension(1000, 30);

    JLabel label;
    JTextField textField;

    public TrackPanel(String labelText, String initalText)
    {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setBorder(UISettings.INSET_PANEL_BORDER);
      label = new JLabel(labelText + " ", SwingConstants.RIGHT);
      add(label, null);
      textField = new JTextField(initalText);
      add(textField, null);

      label.setPreferredSize(new Dimension(COL1_WIDTH, 10));
      setMaximumSize(maxDim);
    }
  }

//   //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! EXPERIMENTAL STUFF AHEAD !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


//   public void initSaveList()
//   {
//     Thread saveThread = new Thread()
//       {
//         public void run()
//         {
//           while (true)
//           {
//             synchronized (saveList)
//             {
//               while (saveList.isEmpty())
//               {
//                 try
//                 {
//                   saveList.wait();
//                 }
//                 catch (InterruptedException e)
//                 {
//                 }
//               }
//               LoggerSingleton.logDebugFine(this.getClass(), "run", "invoking runnable, "  + saveList.size() + " remain in queue");
//             }
//             Runnable runnable = (Runnable)saveList.removeFirst();
//             runnable.run();
//           }
//         }
//       };
//     saveThread.start();
//   }

//   class SaveSelectedArtListener implements ActionListener
//   {
//     public void actionPerformed(ActionEvent e)
//     {
//       ImageIcon icon = (ImageIcon)discArtCombo.getSelectedItem();
//       if (icon != null)
//       {
//         saveThumbs(icon.getImage());
//       }
//     }
//   }


  /*********************************************************************************
   * STATIC INNER CLASS:
   *********************************************************************************/
  class LookupAction extends AbstractAction
  {
    public LookupAction()
    {
      super("Lookup", ImageCache.getImageIcon("updateWithCddb.gif"));
      putValue(SHORT_DESCRIPTION, "Look up disc in free db");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      LinkedList list = new LinkedList(trackMetadata);
      list.add(0, discMetadata);

      LookupRequest request = new LookupRequest(list);

      // Post the lookup thread
      client.executeTask(new LookupTask(client, request, DiscEditPanel.this));

      tracksPanel.add(lookupLabel);
    }
  }

//   /*********************************************************************************
//    * STATIC INNER CLASS:
//    *********************************************************************************/
//   class ScanAction extends AbstractAction implements Runnable
//   {
//     public ScanAction()
//     {
//       super("Scan", ImageCache.getImageIcon("updateWithCddb.gif"));
//       putValue(SHORT_DESCRIPTION, "HACKScan (TM)");
//       putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
//     }

//     public void actionPerformed(ActionEvent e)
//     {
//       new Thread(this).start();
//     }

//     public void run()
//     {

//       for (int i=0; i<1600; i++)
//       {
//         long startTime = System.currentTimeMillis();
//         editNextDisc();
//         try
//         {
//           while (System.currentTimeMillis() - startTime < 8000)
//           {
//             Thread.sleep(500);
//           }
//         }
//         catch (InterruptedException e)
//         {
//         }
//       }
//     }
//   }
}
