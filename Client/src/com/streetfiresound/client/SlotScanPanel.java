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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstAvDiscScanMode;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.SlotScanner;
import com.streetfiresound.clientlib.Util;
import com.streetfiresound.clientlib.SlotScanner.SonyJukeboxInfo;
import com.streetfiresound.clientlib.event.SlotScanProgressEvent;
import com.streetfiresound.clientlib.event.SlotScannerInitializedEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import com.streetfiresound.clientlib.event.ThumbnailsReadyEvent;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import javax.swing.border.CompoundBorder;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
// import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
// import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 *  Slot scan user interface
 *  @author iain huxley
 */
public class SlotScanPanel extends StreetFirePanel implements StreetFireEventListener
{
  private J2seClient  client;      // primary client object
  private SlotScanner slotScanner;

  private boolean initRequested = false;

  // following two arrays share common indices NOTE: not necessarily in channel order
  private SonyJukeboxInfo[] sonyJukeboxes = null;  // info for each player
  private PerPlayerSlotConfig[] playerSlotConfig;  // slot configuration panel for each playerx

  // ui elements
  private JLabel    lastThumbLabel;
  private JTextArea textArea;
  private JPanel    buttonPanel; // holds action buttons
  private JPanel    configPanel; // configuration panel - displays player models, capacities, and range to scan


  public SlotScanPanel(J2seClient client)
  {
    // init members
    this.client = client;
    //mediaCatalog = client.getMediaCatalog();
    slotScanner = client.getSlotScanner(this); //XXX:000000000000000000:20050224iain:  use client.getSlotScanner()

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
    //    setBorder(new CompoundBorder(UISettings.PANEL_BORDER_LOWERED, BorderFactory.createEmptyBorder(3, 6, 3, 6))); // top, left, bottom, right

    // init config area (partial, don't yet know players, capacities)
    configPanel = new StreetFirePanel();
    configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
    configPanel.add(lastThumbLabel = new JLabel());

    textArea = new JTextArea();
    textArea.setOpaque(false);
    textArea.setForeground(UISettings.TABLE_LINE_FOREGROUND);
    textArea.setBorder(UISettings.PANEL_BORDER_LOWERED);
    textArea.setEditable(false);

    // create the button panel
    buttonPanel = new StreetFirePanel();
    buttonPanel.setLayout(new FlowLayout());

    // set up save action
    StartAction startAction = new StartAction();
    buttonPanel.add(new JButton(startAction));

    // add the panels
    add(configPanel, BorderLayout.NORTH);
    add(new JScrollPane(textArea),    BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  public void paint(Graphics g)
  {
    if (!initRequested)
    {
      initRequested = true;
      initSlotScanner();
    }
    super.paint(g);
  }

  /**
   *  (async) trigger the slot scanner to discover player capabilities, etc.
   */
  public void initSlotScanner()
  {
    // trigger a slot scanner init (async)
    slotScanner.init();
  }

  /**
   *  Checks to see if the user has entered valid information
   *  @return null if valid, otherwise a user readable error string
   */
  private String validateUserEntries()
  {
    // validate all players, accumulating the error message
    String errorMessage = null;
    for (int i=0; i<playerSlotConfig.length; i++)
    {
      String[] errors = playerSlotConfig[i].validateUserEntries();
      for (int j=0; j<errors.length; j++)
      {
        if (errorMessage == null)
        {
          errorMessage = "Please correct the following errors:\nPlayer " + (i+1) + ": " + errors[j] + "\n";
        }
        else
        {
          errorMessage += "Player " + (i+1) + ": " + errors[j] + "\n";
        }
      }
    }
    return errorMessage;
  }

  /**
   *  Perform the slot scan using the user specified parameters
   */
  private void performSlotScan()
  {
    setConfigEnabled(false);

    int[] startSlots = new int[sonyJukeboxes.length];
    int[] endSlots   = new int[sonyJukeboxes.length];

    // iterate over the player slot configs
    for (int i=0; i<playerSlotConfig.length; i++)
    {
      startSlots[i] = playerSlotConfig[i].getStartSlot();
      endSlots[i]   = playerSlotConfig[i].getEndSlot();
    }
    int mode = ConstAvDiscScanMode.SCAN_ALL;

    // start the slot scan
    slotScanner.startSlotScan(mode, startSlots, endSlots);
    textArea.append("Initiating Scan.... please wait\n");
  }

  public void setConfigEnabled(boolean enabled)
  {
    for (int i=0; i<playerSlotConfig.length; i++)
    {
      playerSlotConfig[i].setEnabled(enabled);
    }
  }

  /**
   *  Called by the event dispatcher to notify of an event
   *  ---------------------------------------------------------------------------------------
   */
  public void eventNotification(StreetFireEvent event)
  {
    if (event instanceof SlotScannerInitializedEvent)
    {
      // handle it
      handleSlotScannerInitialized((SlotScannerInitializedEvent)event);
    }
    else if (event instanceof SlotScanProgressEvent)
    {
      // handle it
      handleSlotScanProgress((SlotScanProgressEvent)event);
    }
    else if (event instanceof ThumbnailsReadyEvent)
    {
      // handle it
      handleThumbnailsReady((ThumbnailsReadyEvent)event);
    }
    else if (event instanceof ContentMetadataArrivedEvent)
    {
      handleMetadataArrived((ContentMetadataArrivedEvent)event);
    }
    else

    {
      //LoggerSingleton.logDebugFine(this.getClass(), "eventNotification", "unknown transaction id: " + event.getTransactionId());
    }
  }

  public void handleMetadataArrived(ContentMetadataArrivedEvent e)
  {
    if (!slotScanner.isScanning())
    {
      return;
    }
    ContentMetadata[] metadata = e.getMetadata();
    for (int i=0; i<metadata.length; i++)
    {
      if (metadata[0].getContentId().notExpandable())
      {
        continue;
      }
      textArea.append("       " + metadata[0].getArtist() + " - " + metadata[0].getTitle() + "\n");
    }
  }

  /**
   *  Handle a notification that the slot scanner has been initialized
   */
  public void handleSlotScannerInitialized(SlotScannerInitializedEvent e)
  {
    // keep info for jukeboxes
    sonyJukeboxes = e.getSonyJukeboxes();
    playerSlotConfig = new PerPlayerSlotConfig[sonyJukeboxes.length];

    // iterate over the players
    for (int i=0; i<sonyJukeboxes.length; i++)
    {
      playerSlotConfig[i] = new PerPlayerSlotConfig(i+1, "Player " + (sonyJukeboxes[i].getChannel() + 1), sonyJukeboxes[i].getCapabilities().getCapacity());
      configPanel.add(playerSlotConfig[i]);
    }
    textArea.append("\n");
  }

  /**
   *  Handle a notification of slot scan progress
   */
  public void handleSlotScanProgress(SlotScanProgressEvent event)
  {
    // extract the necessary info
    ContentMetadata metadata = event.getMetadata()[0];
    ContentId contentId = metadata.getContentId();
    String title = metadata.getTitle();

    // append info to text area
    textArea.append("Scanned player " + contentId.getPlayerChannel() + " slot " + contentId.getPlayerSlot() + " - ");
    if (title.length() == 0 || title.equals(Util.TOKEN_UNKNOWN))
    {
      textArea.append("found unknown disc, looking up\n");
    }
    else if (title.equals(Util.TOKEN_EMPTY))
    {
      textArea.append("empty\n");
    }
    else
    {
      textArea.append("found " + metadata.getTitle() + " by " + metadata.getArtist() + "\n");
    }
  }

  /**
   *  Handle a notification of slot scan progress
   */
  public void handleThumbnailsReady(ThumbnailsReadyEvent event)
  {
    //lastThumbLabel.setText("last cover art");
    lastThumbLabel.setIcon(new ImageIcon(event.getLargestThumb()));

    if (lastThumbLabel.getBorder() != UISettings.PANEL_BORDER_LOWERED)
    {
      lastThumbLabel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 0, 5, 100), UISettings.PANEL_BORDER_LOWERED));
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class StartAction extends AbstractAction
  {
    public StartAction()
    {
      super("Start", ImageCache.getImageIcon("scanslots.gif"));
      putValue(SHORT_DESCRIPTION, "Start Slot Scanning");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      String errors = validateUserEntries();
      if (errors == null)
      {
        performSlotScan();
      }
      else
      {
        JOptionPane.showMessageDialog(SlotScanPanel.this, errors, "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /*********************************************************************************
   * STATIC INNER CLASS: configuration for a player
   *********************************************************************************/
  static class PerPlayerSlotConfig extends StreetFirePanel
  {
    JTextField startSlot;
    JTextField endSlot;
    JLabel label;

    int numSlots;

    public PerPlayerSlotConfig(int channel, String model, int numSlots)
    {
      setLayout(new FlowLayout());

      // keep number of slots
      this.numSlots = numSlots;

      label     = new JLabel(model + " (" + numSlots + " slots)      start:");
      startSlot = new JTextField("1");
      startSlot.setPreferredSize(new Dimension(36, 18));
      endSlot   = new JTextField(String.valueOf(numSlots));
      endSlot.setPreferredSize(new Dimension(36, 18));

      add(label);
      add(startSlot);
      add(new JLabel(" end:"));
      add(endSlot);
    }

    public void setEnabled(boolean enabled)
    {
      startSlot.setEnabled(enabled);
      endSlot.setEnabled(enabled);
    }

    public int getStartSlot()
    {
      return getIntFromTextField(startSlot);
    }

    public int getEndSlot()
    {
      return getIntFromTextField(endSlot);
    }

    /**
     *  Checks to see if the user has entered valid information
     *  @return null if valid, otherwise a user readable error string
     */
    public String[] validateUserEntries()
    {
      // create an arraylist to hold the result
      ArrayList errors = new ArrayList();

      // get the values
      int start = getIntFromTextField(startSlot);
      int end = getIntFromTextField(endSlot);

      if (start == Integer.MIN_VALUE || start < 1 || start > numSlots)
      {
        errors.add("Start slot must be a number between 1 and " + numSlots);
      }
      if (end == Integer.MIN_VALUE || end < 1 || end > numSlots)
      {
        errors.add("End slot must be a number between 1 and " + numSlots);
      }
      if (start != Integer.MIN_VALUE && end != Integer.MIN_VALUE && end < start)
      {
        errors.add("Start slot must be less than or equal to the end slot");
      }
      return (String[])errors.toArray(new String[0]);
    }

    private int getIntFromTextField(JTextField textField)
    {
      int result = Integer.MIN_VALUE;
      try
      {
        result = Integer.parseInt(textField.getText());
      }
      catch (NumberFormatException e) {}
      return result;
    }
  }
}
