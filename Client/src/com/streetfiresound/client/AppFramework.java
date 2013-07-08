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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.system.HaviApplication;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.ClientRuntimeException;
import com.streetfiresound.clientlib.MediaOrbRuntimeException;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.MatchFoundMessageBackHelper;
import org.havi.system.registry.rmi.MatchFoundMessageBackListener;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.Query;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.SimpleQuery;

/**
 *  Application framework - container for software modules that will later be dynamically loaded from the network
 *
 *  XXX:0:20050325iain: think about better names
 *
 *  @author iain huxley
 */
public class AppFramework extends HaviApplication implements MatchFoundMessageBackListener, MsgWatchOnNotificationListener
{
  public static AppFramework instance; // singleton

  public static Properties uiProperties = new Properties();
  static
  {//XXX:00000000000000:20050125iain:yuk
    try
    {
      String uiPropsFile = System.getProperty("UIPropsFile");
      uiProperties.load(new FileInputStream(uiPropsFile != null ? uiPropsFile : "configuration/ui.properties"));
    }
    catch (IOException e)
    {
    }
  }

  // UI elements
  private UndecoratedFrame appFrame;
  private StreetFirePanel  mainPanel;
  private StreetFirePanel  orbletSelectionPanel;  // where the 'orblets' get displayed
  private StreetFirePanel  orbletSelectionButtonPanel;  // where the 'orblets' get displayed
  private JButton          hideButton;
  private J2seClient       activeOrblet;

  private StreetFirePanel  blankPanel;

  // havi related stuff
  private MsgWatchOnNotificationHelper watchHelper;
  private SoftwareElement softwareElement;
  private HashMap buttonMap = new HashMap(); // resolves seids to buttons

  public AppFramework(String[] args)
  {
    super(args);

    try
    {
      // Create local software element
      softwareElement = new SoftwareElement();
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("error initializing software element", e);
    }

    // set instance, this class is a singleton
    assert instance == null;
    instance = this;
  }

  public UndecoratedFrame getAppFrame()
  {
    return appFrame;
  }

  private void addBlankPanel()
  {
    StreetFirePanel fullAreaBlankPanel = new StreetFirePanel();
    blankPanel = new StreetFirePanel();
    blankPanel.setBorder(UISettings.PANEL_BORDER_RAISED);
    blankPanel.setLayout(new BoxLayout(blankPanel, BoxLayout.Y_AXIS));
    JLabel blankPanelLabel = new JLabel(ImageCache.getImageIcon("chromelogolarge.png"));
    blankPanelLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
    blankPanelLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    StreetFirePanel labelPanel = new StreetFirePanel();
    labelPanel.add(blankPanelLabel);
    blankPanel.add(labelPanel);
    mainPanel.add(blankPanel, BorderLayout.CENTER);
  }

  /**
   *  initialize (but don't yet display) the UI
   */
  private void initUI()
  {
    // init main application frame
    appFrame = new UndecoratedFrame();
    appFrame.setResizable(true);

    // set up the main panel
    mainPanel = new StreetFirePanel();
    addBlankPanel();

    // contentPane.add(mainPanel, BorderLayout.CENTER);
    appFrame.getContentPane().setLayout(new BorderLayout());
    appFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);

    // set up the glass pane for global UI disable (when set visible)
    appFrame.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    appFrame.getGlassPane().addMouseListener(new MouseAdapter() {});

    // set up the orblet selection panel
    orbletSelectionPanel = new StreetFirePanel("orblet_selector_bg.png");
    orbletSelectionPanel.setResizeCornerEnabled(true);
    orbletSelectionButtonPanel = new StreetFirePanel();
    orbletSelectionPanel.add(orbletSelectionButtonPanel, BorderLayout.CENTER);
    orbletSelectionButtonPanel.setLayout(new FlowLayout());
    orbletSelectionButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 13)); // top, left, bottom, right
    StreetFirePanel windowFunctionsPanel = new StreetFirePanel();
    windowFunctionsPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 2)); // top, left, bottom, right
    windowFunctionsPanel.add(new WindowFunctionsPanel(false, 1));
    orbletSelectionPanel.add(windowFunctionsPanel, BorderLayout.NORTH);
    orbletSelectionButtonPanel.setMinimumSize(new Dimension(93, 0));
    orbletSelectionButtonPanel.setPreferredSize(new Dimension(93, 0));
    StreetFirePanel hideButtonPanel = new StreetFirePanel();
    hideButton = new JButton(new HideOrbletSelectionPanelAction());
    hideButton.setEnabled(false);
    hideButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
    hideButtonPanel.add(hideButton, BorderLayout.CENTER);
    orbletSelectionPanel.add(hideButtonPanel, BorderLayout.SOUTH);
    appFrame.getContentPane().add(orbletSelectionPanel, BorderLayout.EAST);
  }

  private JButton getButton(AbstractAction action)
  {
    JButton button = new JButton(action);
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setPreferredSize(new Dimension(90, 90));
    return button;
  }


  /*********************************************************************************
   * PUBLIC INNER CLASS: Action with shows the UI for an RBX1600
   *********************************************************************************/
  public class ShowRbx1600Action extends AbstractAction
  {
    private SEID mediaManagerSeid;

    public ShowRbx1600Action(String name, SEID mediaManagerSeid)
    {
      super(name, ImageCache.getImageIcon("chromelogo.png"));
      putValue(SHORT_DESCRIPTION, "Open " + name);
      this.mediaManagerSeid = mediaManagerSeid;
    }

    public void actionPerformed(ActionEvent e)
    {
      showRbx1600Client(mediaManagerSeid);
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS: ion which hides the orblet selection panel
   *********************************************************************************/
  public class HideOrbletSelectionPanelAction extends AbstractAction
  {
    public HideOrbletSelectionPanelAction()
    {
      super("Hide", ImageCache.getImageIcon("left.png"));
      putValue(SHORT_DESCRIPTION, "Hide the orblet selection panel");
    }

    public void actionPerformed(ActionEvent e)
    {
      appFrame.getContentPane().remove(orbletSelectionPanel);
      appFrame.getContentPane().doLayout();
      appFrame.getContentPane().validate();
      activeOrblet.setResizeCornerEnabled(true);
      activeOrblet.showInfo("The orblet selection panel may be reenabled by clicking the checkbox in the settings tab");
    }
  }

  public void showUI()
  {
    appFrame.setVisible(true);
  }


  public void setupListeners()
  {
    OperationCode MATCH_OPCODE = new OperationCode((short)0xff00, (byte)0xac);

    try
    {
      // Create watcher for the remote software element
      watchHelper = new MsgWatchOnNotificationHelper(softwareElement, new OperationCode((short)ConstApiCode.ANY, (byte)-1));

      // Create and attach a match found helper
      MatchFoundMessageBackHelper matchFoundHelper = new MatchFoundMessageBackHelper(softwareElement, MATCH_OPCODE, this);
      softwareElement.addHaviListener(matchFoundHelper, softwareElement.getSystemSeid(ConstSoftwareElementType.REGISTRY));
      LoggerSingleton.logDebugCoarse(AppFramework.class, "initUI", "attached match found helper");

      // Create registry client
      RegistryClient registryClient = new RegistryClient(softwareElement);

      // Build MediaManaager query
      SimpleAttributeTable mmAttributeTable = new SimpleAttributeTable();
      mmAttributeTable.setSoftwareElementType(ConstSoftwareElementType.APPLICATION_MODULE);
      mmAttributeTable.setInterfaceId(ConstMediaManagerInterfaceId.MEDIA_MANAGER);
      Query query = mmAttributeTable.toQuery();

      // subscribe for future matches
      registryClient.subscribeSync(0, MATCH_OPCODE, query);

      // Perform initial query and add to table
      QueryResult result = registryClient.getElementSync(0, query);
      addMediaManagerButtons(result.getSeidList());
    }
    catch (HaviException e)
    {
      e.printStackTrace();
    }
  }


  /* (non-Javadoc)
   * @see org.havi.system.registry.rmi.MatchFoundMessageBackListener#matchFound(int, org.havi.system.types.SEID[])
   */
  public void matchFound(int queryId, SEID[] seidList) throws HaviRegistryException
  {
    LoggerSingleton.logDebugCoarse(AppFramework.class, "matchFound", "match found, seids is " + seidList);
    for (int i=0; i<seidList.length; i++)
    {
      // add button(s)
      addMediaManagerButtons(seidList);
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "msgWatchOnNotification", targetSeid.toString());

    synchronized(buttonMap)
    {
      JButton button = (JButton)buttonMap.get(targetSeid);
      if (button != null)
      {
        orbletSelectionButtonPanel.remove(button);
        orbletSelectionButtonPanel.invalidate();
        orbletSelectionButtonPanel.repaint();
      }
    }

    if (activeOrblet != null && activeOrblet.getMediaManagerSeid().equals(targetSeid))
    {
      activeOrblet.showError("lost connection to device");
      mainPanel.removeAll();
      addBlankPanel();
      mainPanel.invalidate();
      mainPanel.doLayout();
      mainPanel.repaint();
      mainPanel.validate();
      activeOrblet = null;
   }
  }

  /**
   *  quit the application
   *  @return true if successful
   */
  public boolean quit()
  {
    System.exit(0);
    return true;
  }

  /**
   *  minimize the application
   *  @return true if successful
   */
  public boolean minimize()
  {
    appFrame.setState(JFrame.ICONIFIED);
    return true;
  }

  /**
   *  minimize the application
   *  @return true if successful
   */
  public boolean maximize()
  {
    //XXX:000000000000000000000:20050319iain: following appears broken, set size to screen width/height instead
    appFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    return true;
  }

  private void addMediaManagerButtons(SEID[] seids)
  {
    for (int i=0; i<seids.length; i++)
    {
      SEID mmSeid = seids[i];

      try
      {
        // watch for gone
        watchHelper.addListenerEx(mmSeid, this);
      }
      catch (HaviException e)
      {
        throw new MediaOrbRuntimeException("error adding watch on listener", e);
      }

      // get name
      byte[] guidValue = mmSeid.getGuid().getValue();
      String name = "" + guidValue[0] + "." + guidValue[1] + "." + guidValue[2] + "." + guidValue[3];

      // create button
      JButton button = getButton(new ShowRbx1600Action(name, mmSeid));

      synchronized(buttonMap)
      {
        // add to button map to allow cleanup
        buttonMap.put(mmSeid, button);
      }

      // add button to panel
      orbletSelectionButtonPanel.add(button);
      orbletSelectionButtonPanel.doLayout();
    }
  }

  public void mediaManagerPresenceChange(SEID mediaManagerSeid, boolean available)
  {

  }

  public void showRbx1600Client(SEID mediaManagerSeid)
  {
    assert appFrame != null;

    // ignore if we're already connected
    if (activeOrblet != null && activeOrblet.getMediaManagerSeid().equals(mediaManagerSeid))
    {
      return;
    }

    final SEID mmSeid = mediaManagerSeid;

    JLabel waitLabel = new JLabel("connecting to RBX1600, please wait....", ImageCache.getImageIcon("logo_wait.gif"), SwingConstants.CENTER);
    JPanel labelPanel = new JPanel();
    labelPanel.add(waitLabel);
    blankPanel.add(labelPanel);
    blankPanel.add(Box.createVerticalStrut(200));


    // glass pane has hourglass cursor and noop mouse handler, making it visible will disable UI and show hourglass
    appFrame.getGlassPane().setVisible(true);

    // client init [may] involve some blocking calls, do it in another thread
    Task createClientTask = new AbstractTask()
      {
        public void run()
        {
          try
          {
            activeOrblet = new J2seClient(mmSeid, mainPanel);
          }
          catch (HaviException e)
          {
            LoggerSingleton.logError(J2seClient.class, "main", "init failed: " + Util.getStackTrace(e));
          }
          catch (MediaOrbRuntimeException e)
          {
            LoggerSingleton.logError(J2seClient.class, "main", "init failed: " + Util.getStackTrace(e));
          }
        }

        public String getName()
        {
          return "CreateJ2seClient";
        }
      };

    // Try to get the task pool
    TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
    if (taskPool == null)
    {
      throw new IllegalStateException("Can't find task pool");
    }

    try
    {
      // fire off the task
      taskPool.execute(createClientTask);
    }
    catch (TaskAbortedException e)
    {
      throw new ClientRuntimeException("task '" + createClientTask.getTaskName() + "' aborted", e);
    }

    hideButton.setEnabled(true);

  }

  /**
   * Installs look and feel.
   */
  private void initLookAndFeel()
  {
    try
    {
      String lnf = uiProperties.getProperty("LnF");
      if (lnf != null)
      {
        //System.out.println("XXX:000000000000000000:iain:>>>>lnf is '" + lnf + "'");
        UIManager.setLookAndFeel(lnf);
      }
    }
    catch (UnsupportedLookAndFeelException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (InstantiationException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }

    UIManager.put("Button.font",      UISettings.TABLE_FONT);
    UIManager.put("Table.font",       UISettings.TABLE_FONT);
    UIManager.put("TableHeader.font", UISettings.TABLE_FONT);
    UIManager.put("TabbedPane.font",  UISettings.TABLE_FONT);
    UIManager.put("TextField.font",   UISettings.TABLE_FONT);
    UIManager.put("TextArea.font",   UISettings.TABLE_FONT);
    UIManager.put("Label.font",       UISettings.TABLE_FONT);
    UIManager.put("Button.insets",    UISettings.STANDARD_BUTTON_INSETS);

    UIManager.put("ComboBox.foreground", Color.white);

    //XXX:0:20050318iain: use UI settings/props
    UIManager.put("ScrollPane.border", new EmptyBorder(0,0,0,0));//ThinBevelBorder(BevelBorder.LOWERED, new Color(85, 85, 85), new Color(35, 35, 35)));

    //XXX:0:20050125iain: couldn't figure out the best way to specify colors etc in a config file, went for this simplistic approach
    for (Enumeration e = AppFramework.uiProperties.propertyNames(); e.hasMoreElements(); )
    {
      String prop = (String)e.nextElement();
      String prefix = "Color.";
      if (prop.startsWith(prefix))
      {
        String strippedProp = prop.substring(prefix.length());
        String value = AppFramework.uiProperties.getProperty(prop);
        Color color = Util.getColorFromHexString(value);

        if (color != null)
        {
          UIManager.put(strippedProp, color);
        }
        else
        {
          LoggerSingleton.logError(this.getClass(), "initLnF", "invalid color for prop " + prop + ": '" +  value + "'");
        }
      }
    }
  }

  public static void main(String args[])
  {
    SplashScreen splashScreen = new SplashScreen(ImageCache.getImageIcon("splash.png").getImage());
    splashScreen.setStatus("initializing...");
    Util.moveToCenter(splashScreen, null);
    splashScreen.setVisible(true);

    // get the tint going, it takes a while...
    final SplashScreen finalSplashScreen = splashScreen;
    Thread tintThread = new Thread("tint")
      {
        public void run()
        {
          // preload background before paint
          (new StreetFirePanel()).initializeBackground();
        }
      };
    tintThread.setPriority(Thread.MAX_PRIORITY);
    tintThread.start();

    // XXX:0:20050318iain: hack, give the tint thread a little time to start.
    Util.sleep(100);

    AppFramework appFramework = new AppFramework(args);

    // set up the look and feel
    appFramework.initLookAndFeel();

    appFramework.initUI();


    appFramework.setupListeners();


    //appFramework.showRbx1600Client(appFramework.getMediaManagerSeid());
    appFramework.showUI();

    splashScreen.setVisible(false);
  }
}
