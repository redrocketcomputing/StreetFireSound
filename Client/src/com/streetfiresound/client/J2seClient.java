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
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.havi.system.types.HaviException;

import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.MediaOrbRuntimeException;
import com.streetfiresound.clientlib.StreetFireClient;
import com.streetfiresound.clientlib.event.EventDispatcher;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import org.havi.system.types.SEID;
import java.awt.Container;
import java.io.File;

/**
 *  Media catalog evaluation client
 *  @author iain huxley
 */
public class J2seClient extends StreetFireClient //implements StreetFireEventListener
{
  private JSplitPane       splitPane;
  private SplashScreen     splashScreen;


  private Container        uiContainer;
  private ActiveItemPanel  activeItemPanel;
  private CatalogBrowser   catalogBrowser;
  private Player           player;
  private StreetFirePanel  splitPanePanel;


  public J2seClient(SEID mediaManagerSeid, Container uiContainer) throws HaviException
  {
    super(mediaManagerSeid);

    this.uiContainer = uiContainer;

    SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          initUI();
        }
      });
  }

  /**
   *  initialize (but don't yet display) the UI
   */
  private void initUI()
  {
    StreetFirePanel mainPanel = new StreetFirePanel();

    // init catalog browser
    catalogBrowser = new CatalogBrowser(this);

    // init active item panel
    activeItemPanel = new ActiveItemPanel(this);

    // HOOK INTERFACES TOGETHER

    // add the add to playlist actions to the catalog browser
    catalogBrowser.addAddToPlayListAction(activeItemPanel.getPlayListEditorPanel().getAddCategoryToPlayListAction());
    catalogBrowser.addAddToPlayListAction(activeItemPanel.getPlayListEditorPanel().getAddToPlayListAction());

    // add the edit disc action in the catalog browser
    catalogBrowser.addEditDiscAction(activeItemPanel.getDiscEditPanel().getEditDiscAction());

    // add the edit playlist action to the catalogBrowser
    catalogBrowser.addEditPlayListAction(activeItemPanel.getPlayListEditorPanel().getEditPlayListAction());

    // add the 'add to play queue' action to the catalogBrowser
    catalogBrowser.addAddToPlayQueueAction(activeItemPanel.getPlayQueuePanel().getAddToPlayQueueAction());

    // place them in a split pane
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catalogBrowser, activeItemPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setContinuousLayout(true);
    splitPane.setOpaque(false);
    splitPane.setBorder(null);
    splitPane.setDividerSize(18);
    splitPanePanel = new StreetFirePanel(true);
    splitPanePanel.setBorder(UISettings.PANEL_BORDER_RAISED);
    splitPanePanel.add(splitPane, BorderLayout.CENTER);
    mainPanel.add(splitPanePanel, BorderLayout.CENTER);

    // add player
    //StreetFirePanel panel = new StreetFirePanel();
    //panel.setBorder(UISettings.INSET_PANEL_BORDER);
    player = new Player(this);
    //panel.add(player, BorderLayout.CENTER);
    mainPanel.add(player, BorderLayout.NORTH);
//    mainPanel.add(player, BorderLayout.SOUTH);

    // initially share extra space so that panels are stable on init
    splitPane.setResizeWeight(0.5);

    // XXX:0:20050318iain: uncomment following for event debugging, will unleash a flood of logs
//    AWTEventListener debugEventListener =
//  Toolkit.getDefaultToolkit().addAWTEventListener(debugEventListener, 0xFFFFFFFFFFFFFFFF);
//                                                          | AWTEvent.MOUSE_EVENT_MASK
//                                                          | AWTEvent.MOUSE_MOTION_EVENT_MASK
//                                                          | AWTEvent.ACTION_EVENT_MASK
//                                                          | AWTEvent.ADJUSTMENT_EVENT_MASK
//                                                          | AWTEvent.CONTAINER_EVENT_MASK
//                                                          | AWTEvent.FOCUS_EVENT_MASK
//                                                          | AWTEvent.ITEM_EVENT_MASK
//                                                          | AWTEvent.KEY_EVENT_MASK
//                                                          | AWTEvent.PAINT_EVENT_MASK
//                                                          | AWTEvent.INVOCATION_EVENT_MASK
//                                                          | AWTEvent.TEXT_EVENT_MASK
//                                                          | AWTEvent.WINDOW_EVENT_MASK
//                                                          | AWTEvent.INVOCATION_EVENT_MASK);
    uiContainer.removeAll();
    uiContainer.add(mainPanel, BorderLayout.CENTER);

    SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          finishUIInit();
        }
      });
  }

  public void setResizeCornerEnabled(boolean enabled)
  {
    // need to be able to resize
    splitPanePanel.setResizeCornerEnabled(enabled);
  }

  public void finishUIInit()
  {
    // when window is resized, give most of the extra space to the left panel
    splitPane.setResizeWeight(0.8);

    player.repaint(); //XXX:0:20050318iain: hack?

    // start anim
    player.playAnim(getMediaPlayer().getCurrentItem() == null);
  }


  public void saveCDList(File file)
  {
    try
    {
      catalogBrowser.saveCDList(file);
    }
    catch (IOException e)
    {
      showError("error saving file:\n" + e);
    }
  }

//   /**
//    * Installs look and feel.
//    */
//   private void initLookAndFeel()
//   {
//     try
//     {
//       String lnf = uiProperties.getProperty("LnF");
//       if (lnf != null)
//       {
//         //System.out.println("XXX:000000000000000000:iain:>>>>lnf is '" + lnf + "'");
//         UIManager.setLookAndFeel(lnf);
//       }
//     }
//     catch (UnsupportedLookAndFeelException e)
//     {
//       e.printStackTrace();
//     }
//     catch (ClassNotFoundException e)
//     {
//       e.printStackTrace();
//     }
//     catch (InstantiationException e)
//     {
//       e.printStackTrace();
//     }
//     catch (IllegalAccessException e)
//     {
//       e.printStackTrace();
//     }

//     UIManager.put("Button.font",      UISettings.TABLE_FONT);
//     UIManager.put("Table.font",       UISettings.TABLE_FONT);
//     UIManager.put("TableHeader.font", UISettings.TABLE_FONT);
//     UIManager.put("TabbedPane.font",  UISettings.TABLE_FONT);
//     UIManager.put("TextField.font",   UISettings.TABLE_FONT);
//     UIManager.put("Label.font",       UISettings.TABLE_FONT);
//     UIManager.put("Button.insets",    UISettings.STANDARD_BUTTON_INSETS);

//     //XXX:0:20050125iain: couldn't figure out the best way to specify colors etc in a config file, went for this simplistic approach
//     for (Enumeration e = J2seClient.uiProperties.propertyNames(); e.hasMoreElements(); )
//     {
//       String prop = (String)e.nextElement();
//       String prefix = "Color.";
//       if (prop.startsWith(prefix))
//       {
//         String strippedProp = prop.substring(prefix.length());
//         String value = J2seClient.uiProperties.getProperty(prop);
//         Color color = Util.getColorFromHexString(value);

//         if (color != null)
//         {
//           UIManager.put(strippedProp, color);
//         }
//         else
//         {
//           LoggerSingleton.logError(this.getClass(), "initLnF", "invalid color for prop " + prop + ": '" +  value + "'");
//         }
//       }
//     }

//     //XXX:0:20050318iain: use UI settings/props
//     UIManager.put("ScrollPane.border", new EmptyBorder(0,0,0,0));//ThinBevelBorder(BevelBorder.LOWERED, new Color(85, 85, 85), new Color(35, 35, 35)));

// //     String themeProp = System.getProperty("theme");
// //     if (false);//.pthemeProp != null && themeProp.equals("dark"))
// //     {
// //       UIManager.put("TextField.foreground", Color.white);
// //       UIManager.put("Button.background", new Color(56, 150, 56));
// //       UIManager.put("ToggleButton.background", new Color(56, 150, 56));
// //       UIManager.put("TableHeader.foreground", new Color(230, 230, 230));
// //       UIManager.put("Viewport.background", new Color(60, 60, 60));
// //     }
//   }





  /**
   *  get the next disc to be edited
   */
  public ContentMetadata getNextDiscToBeEdited(ContentId currentDisc)
  {
    return catalogBrowser.getNextDiscToBeEdited(currentDisc);
  }



  public void showPlayListEditor()
  {
    activeItemPanel.showPlayListEditor();
  }

  public void showPlayQueue()
  {
    activeItemPanel.showPlayQueue();
  }

  public void showDiscEditor()
  {
    activeItemPanel.showDiscEditor();
  }

  //--------------------------------------------------------------------------------------------
  // SECTION: Global action handlers
  //
  // for actions that make sense at the global level (e.g. editing a playlist), it's the primary
  // client class that decides how it is handled (acts as mediator)
  //--------------------------------------------------------------------------------------------

  /**
   *
   */
  public void editPlayList(ContentMetadata metadata)
  {
    activeItemPanel.editPlayList(metadata);
  }

  //--------------------------------------------------------------------------------------------

  /**
   *  Factory method for creating the event dispatcher
   *  @return an event dispatcher which dispatches events on the system event queue
   */
  public EventDispatcher createEventDispatcher()
  {
    return new EventQueueEventDispatcher();
  }

  /**
   *  present an error message, then quit
   */
  public void fatalError(String message)
  {
    //XXX:000:20050314iain: consider doing this via events
  	JOptionPane.showMessageDialog(AppFramework.instance.getAppFrame(), message, "An error occurred", JOptionPane.ERROR_MESSAGE);
    System.exit(0);
  }

  /**
   *  present an error message in a dialog
   */
  public void showError(String message)
  {
    //XXX:000:20050314iain: consider doing this via events
  	JOptionPane.showMessageDialog(AppFramework.instance.getAppFrame(), message, "An error occurred", JOptionPane.ERROR_MESSAGE);
  }

  /**
   *  present an info message in a dialog
   */
  public void showInfo(String message)
  {
    //XXX:000:20050314iain: consider doing this via events
  	JOptionPane.showMessageDialog(AppFramework.instance.getAppFrame(), message);
  }

  /**
   *  Called to enable or disable the UI
   */
  public void setUIEnabled(boolean enabled)
  {
    // glass pane has hourglass cursor and noop mouse handler, making it visible will disable UI and show hourglass
    AppFramework.instance.getAppFrame().getGlassPane().setVisible(!enabled);
  }
}

class EventQueueEventDispatcher extends EventDispatcher
{
  EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();

  public void queueEventImpl(StreetFireEvent event)
  {
    systemEventQueue.invokeLater(event);
  }
}
