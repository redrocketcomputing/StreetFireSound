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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.RemoteMediaPlayer;
import com.streetfiresound.clientlib.ContentId;
import com.streetfiresound.clientlib.MlidContentId;
import com.streetfiresound.clientlib.event.ContentMetadataArrivedEvent;
import com.streetfiresound.clientlib.event.PlayPositionUpdateEvent;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.StreetFireEventListener;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstSkipDirection;
import com.streetfiresound.mediamanager.mediaplayer.constants.ConstTransportState;
import com.streetfiresound.mediamanager.mediaplayer.types.PlayPosition;
import java.util.List;

/**
 * Player controls
 * @ author iain
 */
public class Player extends StreetFirePanel implements StreetFireEventListener
{
  public static final int    CONTROLS_WIDTH  = 118;
  public static final Insets CONTROLS_INSETS = new Insets(6, 9, 5, 0); // top, left, bottom, right
  public static final Insets DISPLAY_INSETS  = new Insets(10, CONTROLS_WIDTH + CONTROLS_INSETS.left + CONTROLS_INSETS.right + 5, 10, 35); // top, left, bottom, right
  public static final int    HEIGHT = 96;

//   // images
//   private Image baseImage;
//   private Image endImage;

  // UI items
  private GraphicDisplay       graphicDisplay;        // graphic display (pixelated style)
  private StreetFirePanel      graphicDisplayPanel;
  private ControlsPanel        controlsPanel;         // play/pause/next etc.
  private WindowFunctionsPanel windowFunctionsPanel;  // close/maximize etc

  private ContentMetadata currentItemMetadata;

  private J2seClient client;
  private RemoteMediaPlayer mediaPlayer;

  public Player(J2seClient client)
  {
    super(true);

    this.client = client;
    this.mediaPlayer = client.getMediaPlayer();

    // initialize the user interface
    initUI();

    // listen for events
    client.getEventDispatcher().addListener(this);
  }

  public void initUI()
  {
    setLayout(null);
    setBorder(UISettings.PANEL_BORDER_RAISED);

    // fully load the player graphics
//     baseImage = ImageCache.getImageIcon("player.png").getImage();
//     endImage  = ImageCache.getImageIcon("player_end.png").getImage();
//     Util.waitForImage(baseImage, this);
//     Util.waitForImage(endImage,  this);

    graphicDisplay = new GraphicDisplay();
    graphicDisplayPanel = new StreetFirePanel();
    graphicDisplayPanel.add(graphicDisplay, BorderLayout.CENTER);
    graphicDisplayPanel.setBorder(UISettings.PANEL_BORDER_LOWERED);
    add(graphicDisplayPanel);
    // NOTE: graphicDisplayPanel and controlspanel bounds set in invalidate() as they need to change as the window size changes

    controlsPanel = new ControlsPanel();
    add(controlsPanel);

    windowFunctionsPanel = new WindowFunctionsPanel(true, 2);
    //windowFunctionsPanel.setBorder(BorderFactory.createLineBorder(Color.blue));
    add(windowFunctionsPanel);


    ContentId currentItem = mediaPlayer.getCurrentItem();
    if (currentItem != null)
    {
      graphicDisplay.setCurrentItem(client.getMetadataCache().getMetadata(currentItem, true));
    }
  }

  public Dimension getMinimumSize()   { return new Dimension(300, HEIGHT); }
  public Dimension getPreferredSize() { return new Dimension(500, HEIGHT); }

  public void playAnim(boolean keepLogoUpAfterAnim)
  {
    graphicDisplay.playAnim(keepLogoUpAfterAnim);
  }

  public void paint(Graphics g)
  {
    //    drawPlayerBase(g, g.getClipBounds());
    super.paint(g);
  }

  /**
   *  Called by the event dispatcher to notify of an event
   *  ---------------------------------------------------------------------------------------
   */
  public void eventNotification(StreetFireEvent event)
  {
    if (event instanceof PlayPositionUpdateEvent)
    {
      // handle it
      handlePositionUpdate((PlayPositionUpdateEvent) event);
    }
    else if (event instanceof ContentMetadataArrivedEvent)
    {
      // handle it
      handleMetadataArrived((ContentMetadataArrivedEvent)event);
    }
  }

  /**
   *  Deal with a position update event
   */
  public void handlePositionUpdate(PlayPositionUpdateEvent event)
  {
    PlayPosition playPosition = event.getPlayPosition();
    graphicDisplay.setCurrentPosition(playPosition.getPosition());

    ContentId updatedCurrentItemId = new MlidContentId(event.getPlayPosition().getMediaLocationId()); //XXX:00000000000000000000:20050322iain: MLIDs should not be used here

    if (currentItemMetadata == null || !currentItemMetadata.getContentId().equals(updatedCurrentItemId))
    {
      currentItemMetadata = client.getMetadataCache().getMetadata(updatedCurrentItemId, true);
      setTrackInfo(currentItemMetadata);
    }
  }

  /**
   *  Deal with a metadata arrived event
   */
  public void handleMetadataArrived(ContentMetadataArrivedEvent event)
  {
    // get first metadata (always guaranteed to have at least one
    ContentMetadata firstMetadata = event.getMetadata()[0];

    if (currentItemMetadata != null && firstMetadata.getContentId().equals(currentItemMetadata.getContentId()))
    {
      setTrackInfo(firstMetadata);
    }
  }

  private void setTrackInfo(ContentMetadata metadata)
  {
    graphicDisplay.setCurrentItem(metadata);
  }

//   /**
//    *  draws the base player images
//    */
//   private void drawPlayerBase(Graphics g, Rectangle clip)
//   {
//     // set clip
//     g.setClip(clip);

//     int width  = getWidth();
//     int height = getHeight();

//     // clear any possible crud
//     //g.setColor(Color.black);//getBackground());
//     //g.fillRect(0, 0, width, height);

//     // intersect clip for base Image
//     g.clipRect(0, 0, width - .getWidth(null), height);

//     // draw base image
//     g.drawImage(baseImage, 0, 0, null);

//     // back to passed clip
//     if (clip == null)
//     {
//       g.setClip(0, 0, width, height);
//     }
//     else
//     {
//       g.setClip(clip.x, clip.y, clip.width, clip.height);
//     }

//     // draw end image
//     int endPos = width - endImage.getWidth(null);

//     // image has grid, tweak position it so that it aligns
//     g.drawImage(endImage, endPos - (endPos % 2 == 1 ? 0 : 1), 0, null);
//   }

  public void invalidate()
  {
    super.invalidate();
    int w = getWidth();
    int h = getHeight();
    Rectangle bounds = new Rectangle(DISPLAY_INSETS.left,
                                     DISPLAY_INSETS.top,
                                     w - DISPLAY_INSETS.left - DISPLAY_INSETS.right,
                                     h - DISPLAY_INSETS.top  - DISPLAY_INSETS.bottom);

    graphicDisplayPanel.setBounds(bounds);
    controlsPanel.setBounds(CONTROLS_INSETS.left, CONTROLS_INSETS.top, CONTROLS_WIDTH, h - CONTROLS_INSETS.top - CONTROLS_INSETS.bottom);
    windowFunctionsPanel.setBounds(w - 35, 5, 30, h);
  }

  /**
   *  For Testing
   */
  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(
      new Runnable()
      {
        public void run()
        {
          final JFrame f = new JFrame();
          f.setUndecorated(true);

          // allow it to be moved around
          // XXX:0000000000000000000000000:20040927iain: get rid of anonymous inner class usage if we keep this feature
          final Point origin = new Point();
          f.addMouseListener(
            new MouseAdapter()
            {
              public void mousePressed(MouseEvent e)
              {
                origin.x = e.getX();
                origin.y = e.getY();
              }
            });
          f.addMouseMotionListener(
            new MouseMotionAdapter()
            {
              public void mouseDragged(MouseEvent e)
              {
                Point p = f.getLocation();
                f.setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
              }
            });

          f.getContentPane().setLayout(new BorderLayout());
          Player player = new Player(null);
          f.getContentPane().add(player,  BorderLayout.CENTER);
          f.setVisible(true);
          f.setSize(new Dimension(800, 120));
          f.validate();
          player.playAnim(true);
        }
      }
    );
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class ControlsPanel extends StreetFirePanel
  {
    public static final int BUTTON_WIDTH      = 45;
    public static final int BUTTON_HEIGHT     = 43;
    public static final int BUTTON_GAP        = -8;
    public static final int BUTTON_X_OFFSET   = 0;
    public static final int BUTTON_Y_OFFSET   = 0; //XXX:0:20050319iain: get rid of, have insets
    public static final int PLAY_BUTTON_WIDTH = 70;

    private Image controlsImage;

    public ControlsPanel()
    {
      setLayout(null);

      JButton prevButton  = new JButton(new SkipBackwardAction());
      JButton playButton  = new JButton(new PlayAction());
      JButton pauseButton = new JButton(new PauseAction());
      JButton stopButton  = new JButton(new StopAction());
      JButton nextButton  = new JButton(new SkipForwardAction());

      add(prevButton);
      add(playButton,  null);
      add(pauseButton, null);
      add(stopButton);
      add(nextButton);

      int xOff = BUTTON_X_OFFSET;
      int pauseButtonWidth = (BUTTON_WIDTH * 3 + BUTTON_GAP) - PLAY_BUTTON_WIDTH;
      playButton.setBounds(  xOff, BUTTON_Y_OFFSET, PLAY_BUTTON_WIDTH , BUTTON_HEIGHT);
      xOff += PLAY_BUTTON_WIDTH + BUTTON_GAP;
      pauseButton.setBounds( xOff, BUTTON_Y_OFFSET, pauseButtonWidth, BUTTON_HEIGHT);

      xOff = BUTTON_X_OFFSET;
      prevButton.setBounds(  xOff,                              BUTTON_Y_OFFSET + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
      stopButton.setBounds(  xOff += BUTTON_WIDTH + BUTTON_GAP, BUTTON_Y_OFFSET + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
      nextButton.setBounds(  xOff += BUTTON_WIDTH + BUTTON_GAP, BUTTON_Y_OFFSET + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);

      controlsImage = ImageCache.getImageIcon("chromelogo.png").getImage();
      //      Util.waitForImage(baseImage,  this);
    }

    public int getImageWidth()
    {
      return controlsImage.getWidth(null);
    }

    public int getImageHeight()
    {
      return controlsImage.getHeight(null);
    }

    public void paintComponent(Graphics g)
    {
      //g.drawImage(controlsImage, 5, 5, null);
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class PlayAction extends AbstractAction
  {
    public PlayAction()
    {
      super(null, ImageCache.getImageIcon("play.png"));
      putValue(SHORT_DESCRIPTION, "Play the currently queued item");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      // resume if paused, play current if not
      if (mediaPlayer.getTransportState() == ConstTransportState.PAUSE)
      {
        mediaPlayer.resume();
      }
      else if (mediaPlayer.getTransportState() != ConstTransportState.PLAY)
      {
        mediaPlayer.play(mediaPlayer.getCurrentItemQueueIndex());
      }
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class PauseAction extends AbstractAction
  {
    public PauseAction()
    {
      super(null, ImageCache.getImageIcon("pause.png"));
      putValue(SHORT_DESCRIPTION, "Pause the currently playing item");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      // resume if paused, otherwise pause.
      if (mediaPlayer.getTransportState() == ConstTransportState.PAUSE)
      {
        mediaPlayer.resume();
      }
      else
      {
        mediaPlayer.pause();
      }
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class StopAction extends AbstractAction
  {
    public StopAction()
    {
      super(null, ImageCache.getImageIcon("stop.png"));
      putValue(SHORT_DESCRIPTION, "Stop the currently playing item");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      mediaPlayer.stop();
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class SkipForwardAction extends AbstractAction
  {
    public SkipForwardAction()
    {
      super("", ImageCache.getImageIcon("next.png"));
      putValue(SHORT_DESCRIPTION, "Skip to the next queued item");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      mediaPlayer.skip(ConstSkipDirection.FORWARD, 1);
    }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:
   *********************************************************************************/
  public class SkipBackwardAction extends AbstractAction
  {
    public SkipBackwardAction()
    {
      super("", ImageCache.getImageIcon("prev.png"));
      putValue(SHORT_DESCRIPTION, "Skip to the next queued item");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
      mediaPlayer.skip(ConstSkipDirection.REVERSE, 1);
    }
  }
}


