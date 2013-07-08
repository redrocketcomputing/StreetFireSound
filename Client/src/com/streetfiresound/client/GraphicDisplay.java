/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WIOUT ANY WARRANTY; without even the implied warranty of
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


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.streetfiresound.clientlib.ContentMetadata;
import com.streetfiresound.clientlib.RequestDiscArtTask;
import org.havi.fcm.types.TimeCode;

/**
 *  Graphic display panel, supports animation and pixellated style
 *  @author iain huxley
 */
public class GraphicDisplay extends JComponent
{
  public static final boolean DEBUG_CLIP = false;
  public static final boolean PIXELATED_ART = true;

  public static final int APPROX_FRAME_RATE = 20;
  public static final int APPROX_FRAME_TIME = 1000 / APPROX_FRAME_RATE;
  //  public static final int X_INCREMENT = 20;
  public static final int X_INCREMENT = 80;
  public static final int PAUSE_TIME = 1200; // ms to pause with logo in center

  public static final int PIXEL_SIZE = 3;

  public static final int TEXT_YPOS = 12;

  // insets for display area (scroll text clipped to this)
  public static final int DISPLAY_LEFT_INSET   = 3;
  public static final int DISPLAY_TOP_INSET    = 3;
  public static final int DISPLAY_RIGHT_INSET  = 3;
  public static final int DISPLAY_BOTTOM_INSET = 01;

  // font used for rendering
  private Font largeFont;
  private Font smallFont;

  // position
  private int  xPos;
  private int  pauseXPos = 0;

  // current item metadata
  private ContentMetadata currentItem = null;
  private TimeCode currentPosition = null;

  // times
  private int  timeRemainingBeforePaint = 0;
  private int  lastPaintTime;

  private boolean animating = false;


  boolean keepLogo = false;

  // images
  private Image backBuffer;
  private Image logoImage;
  private BufferedImage scrollImage = null;     // scrolling intro image (pre-pixelation)
  private BufferedImage trackInfoImage = null;  // track info image (pre-pixelation)
  private BufferedImage displayOverlay;         // image to overlay to give pixelated look
  private BufferedImage displayLargePixelImage; // display image after pixelation

  private Rectangle displayRect = null; // position/size of display area

  public GraphicDisplay()
  {
    // must hit zero exactly
    assert xPos % X_INCREMENT == 0;

    // we'll do our own back buffering XXX:0:20050201iain: may not be really necessary
    setDoubleBuffered(false);

    // init the fonts
    initFonts();

    // prerender text
    initScrollImage("STREETFIRE");
  }

  public synchronized void playAnim(boolean keepLogoUpAfterAnim)
  {
    keepLogo = keepLogoUpAfterAnim;

    if (animating == true)
    {
      throw new IllegalStateException("tried to play animation when animation still in progress");
    }

    // get a high priority animate thread
    Thread animateThread = new Thread(new AnimateRunnable());
    animateThread.setPriority(Thread.MAX_PRIORITY);
    animateThread.start();
  }

  public synchronized void setCurrentPosition(TimeCode position)
  {
    // keep position
    this.currentPosition = position;

    // don't attempt to render etc. if we're not yet visible or if we're animating
    if (displayRect != null && animating == false)
    {
      // make sure the trackInfoImage is not null
      if (trackInfoImage == null)
      {
        initTrackInfoImage();
      }

      //XXX:0000:20050322iain: check for same position, ignore?

      // render the position
      renderPosition();

      // force an update & repaint
      updateBackBuffer(null);
      repaint();
    }
  }

  public synchronized void setCurrentItem(ContentMetadata item)
  {
    assert item != null;
    currentItem = item;

    // don't attempt to render etc. if we're not yet visible or if we're animating
    if (displayRect != null && animating == false)
    {
      // make sure the trackInfoImage is not null
      if (trackInfoImage == null)
      {
        initTrackInfoImage();
      }

      //XXX:0000:20050322iain: check for same item as before, ignore?

      // render the info (and possibly the position if set)
      renderDisplay();

      // force an update & repaint
      updateBackBuffer(null);
      repaint();
    }
  }

  /**
   *  set up the track info image which shows title, artist, etc.
   */
  private void initTrackInfoImage()
  {
    if (displayRect != null) //XXX:0000000000000000000000:20050322iain: IAINFIX
    {
      trackInfoImage = new BufferedImage((displayRect.width/PIXEL_SIZE)+1, (displayRect.height/PIXEL_SIZE)+1, BufferedImage.TYPE_INT_ARGB_PRE);
    }
  }

  /**
   *  render the track position onto the track info image.  does not cause backbuffer update or repaint
   */
  private void renderPosition()
  {
    assert trackInfoImage != null;

    Graphics2D g2 = (Graphics2D)trackInfoImage.getGraphics();
    g2.setFont(largeFont);
    String timeText = Util.formatSeconds(currentPosition.getHour()*3600 + currentPosition.getMinute()*60 + currentPosition.getSec()); //XXX:0:20050304iain: hack, add util method which takes a timecode;

    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2.setColor(UISettings.DISPLAY_FOREGROUND);
    g2.setFont(largeFont);
    g2.clearRect(DISPLAY_LEFT_INSET, DISPLAY_TOP_INSET, 75, 50);      //XXX:0:20050322iain unhardcode offsets
    g2.drawString(timeText, DISPLAY_LEFT_INSET, 16);
    g2.dispose();
  }

  /**
   *  render the item info (and positio, if available) onto the track info image.  does not cause backbuffer update or repaint
   */
  private void renderDisplay()
  {
    Graphics2D g2 = (Graphics2D)trackInfoImage.getGraphics();
    g2.setFont(smallFont);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g2.clearRect(0, 0, (displayRect.width+1)/PIXEL_SIZE, (displayRect.height+5)/PIXEL_SIZE);
    g2.setColor(UISettings.DISPLAY_FOREGROUND);

    int labelXPos = 78;
    int valueXPos = 110;
    int ySpacing = 7;
    int yOffset = 6;

     g2.drawString("TITLE:",                                           labelXPos,  yOffset);                       //XXX:0:20050322iain: unhardcode offsets
     g2.drawString(currentItem == null ? "" : currentItem.getTitle(),  valueXPos,  yOffset);
     yOffset += ySpacing;

    g2.drawString("ARTIST:",                                          labelXPos,  yOffset);
    g2.drawString(currentItem == null ? "" : currentItem.getArtist(), valueXPos,  yOffset);
    yOffset += ySpacing;

    g2.drawString("ALBUM:",                                           labelXPos, yOffset);
    g2.drawString(currentItem == null ? "" : currentItem.getAlbumTitle() + " - " + currentItem.getAlbumArtist(),
                                                                      valueXPos,  yOffset);

    if (currentItem != null)
    {
      if (PIXELATED_ART)
      {
        // figure out filename for this MLID
        String thumbFilePath = RequestDiscArtTask.getThumbFilePath(currentItem.getContentId().getRootEntryContentId(), UISettings.ALBUM_THUMB_SIZE_MEDIUM);

        // attempt to retreive an image
        ImageIcon icon = ImageCache.getImageIcon(thumbFilePath, false);
        if (icon != ImageCache.getUnknownImageIcon() && icon.getImageLoadStatus() == MediaTracker.COMPLETE)
        {
          Util.waitForImage(icon.getImage(), this);
          int size = displayRect.height/PIXEL_SIZE;
          g2.clearRect(displayRect.width/PIXEL_SIZE - size - 1, 0, size + 5, size);
          g2.drawImage(icon.getImage(), displayRect.width/PIXEL_SIZE - size + 1, 0, size, size, null);
        }
      }
    }
    g2.dispose();

    if (currentPosition != null)
    {
      renderPosition();
    }
  }

  private void initFonts()
  {
    try
    {
      // load fonts XXX:000000:20050307iain: get font from UISettings, allow config in ui.properties
      largeFont = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(new FileInputStream("resources/fonts/bitmap1.ttf")));
      smallFont = Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(new FileInputStream("resources/fonts/bitmap3.ttf")));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (FontFormatException e)
    {
      e.printStackTrace();
    }

    // handle error case by using default font
    if (largeFont == null)
    {
      largeFont = UISettings.TABLE_FONT;
    }
    if (smallFont == null)
    {
      smallFont = UISettings.TABLE_FONT;
    }

    // adjust sizes
    largeFont = largeFont.deriveFont(12.0F);
    smallFont = smallFont.deriveFont(8.0F);
  }

  /**
   *  set up a bitmap for the image to scroll
   */
  private void initScrollImage(String text)
  {
    /// set up buffered image so we're not font rendering during anim

    // need temp image for font metrics Graphics object
    scrollImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics2D g2 = (Graphics2D)scrollImage.getGraphics();
    g2.setFont(largeFont);

    FontMetrics metrics = g2.getFontMetrics();
    int width = metrics.stringWidth(text); //XXX:0:20050201iain: pad it a bit, not super accurate for slanted text
    int height = metrics.getHeight();

    logoImage = ImageCache.getImageIcon("16x16x32.png").getImage();
    Util.waitForImage(logoImage, this);

    //Image colorizedLogoImage = XXX:0000000000:20050207iain:
    // logoImage = createImage(new FilteredImageSource(logoImage.getSource(), new ColorizeImageFilter(UISettings.DISPLAY_FOREGROUND, 189, 1.0F)));
    //Util.waitForImage(logoImage, this);

    // create a buffered image of the required size
    scrollImage = new BufferedImage(width + logoImage.getWidth(null) + 3, Math.max(height, logoImage.getHeight(null)), BufferedImage.TYPE_INT_ARGB_PRE);

    g2 = (Graphics2D)scrollImage.getGraphics();
    g2.drawImage(logoImage, 0, 0, null);

    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    g2.setFont(largeFont);
    g2.setColor(UISettings.DISPLAY_FOREGROUND);
    g2.drawString(text, logoImage.getWidth(null)+3, height);
    g2.dispose();
  }

  /**
   *  AWT update routine, overridden so it does not fill the background for us
   */
  public void update(Graphics g)
  {
    paint(g);
  }

  /**
   * AWT paint routine
   */
  public synchronized void paint(Graphics g)
  {
    super.paint(g);
    //System.out.println("XXX:000000000000000000:iain:>>>>Painting, clipRect is " + g.getClipBounds() + "");
    //try{Thread.sleep(1200);}catch(InterruptedException e){}

    if (animating == false && displayRect != null && trackInfoImage == null) //XXX:00000000000000:20050322iain: IAINFIX
    {
      initTrackInfoImage();
      renderDisplay();
    }

    // see if backbuffer needs an update
    if (backBuffer == null)
    {
      updateBackBuffer(null);
    }

    // wait for just the right moment if we're ahead of schedule (which we try to be)
    while (animating && timeRemainingBeforePaint > 0)
    {
      if (false)
      {
        try
        {
          Thread.sleep(Math.max(0, timeRemainingBeforePaint - 10));
        }
        catch (InterruptedException e) {}
      }
      timeRemainingBeforePaint = APPROX_FRAME_TIME - (Util.getMillisSinceInit() - lastPaintTime);
    }

    // paint back buffer
    //g.drawImage(backBuffer, -800, 0, PIXEL_SIZE*backBuffer.getWidth(null), PIXEL_SIZE*backBuffer.getHeight(null), null);
    g.drawImage(backBuffer, 0, 0, null);

    // time reporting
//     int actualFrameTime = Util.getMillisSinceInit() - lastPaintTime;
//     if (Math.abs(APPROX_FRAME_TIME - actualFrameTime) > 5 )
//     {
//       LoggerSingleton.logWarning(this.getClass(), "paint", "Possible frame slippage, time was " + actualFrameTime + "(" + APPROX_FRAME_TIME + " desired)");
//    }

    // keep the time we actually painted
    lastPaintTime = Util.getMillisSinceInit();
  }

  /**
   *  Called by awt to invalidate component (e.g. when resized)
   */
  public void invalidate()
  {
    super.invalidate();

    // cause the bb to be reallocated.
    backBuffer  = null;
    displayRect = null;
    displayOverlay = null;
    trackInfoImage = null;
  }

  /**
   *  trace clipRects for debugging purposes
   * XXX:0:20050201iain: move to util
   * @param color the color to outline, or null for a randomly selected color
   */
  private boolean debugClip(Graphics g, Color color)
  {
    if (DEBUG_CLIP)
    {
      Rectangle clip = g.getClipBounds();
      if (clip == null)
      {
        clip = getBounds();
      }
      g.setColor(color != null ? color : new Color((int)(Math.random() * Integer.MAX_VALUE)));
      g.drawRect(clip.x, clip.y, clip.width-1, clip.height-1);
    }
    return true;
  }

  private Rectangle calcDisplayRect()
  {
    // calc display dimensions via insets, actual width
    int displayWidth  = getWidth()  - DISPLAY_LEFT_INSET - DISPLAY_RIGHT_INSET;
    int displayHeight = getHeight() - DISPLAY_TOP_INSET  - DISPLAY_BOTTOM_INSET;
    return new Rectangle(DISPLAY_LEFT_INSET, DISPLAY_TOP_INSET, displayWidth, displayHeight);
  }

  /**
   *  update the backbuffer according to the current test position, resizing if necessary
   */
  private synchronized void updateBackBuffer(Rectangle clip)
  {
    // [re]init displayRect if necessary
    if (displayRect == null)
    {
      displayRect = calcDisplayRect();

      // if displayRect has neg. dimensions, we're not yet visible, wait
      if (displayRect.width <= 0 || displayRect.height <= 0)
      {
        displayRect = null;
        return;
      }

      // set the pauseXPos to new center
      pauseXPos = displayRect.x + (displayRect.width - scrollImage.getWidth(null)*PIXEL_SIZE)/2 - 75; // XXX:0:20050329iain: hack addl. offset, figure out why calc wrong
    }

    // [re]init displayOverlay (for pixelation effect) if necessary
    if (displayOverlay == null)
    {
      // prepare texture
      BufferedImage tile = new BufferedImage(PIXEL_SIZE, PIXEL_SIZE, BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics gTile = tile.getGraphics();
      gTile.setColor(Color.white);
      gTile.fillRect(0, 0, PIXEL_SIZE - 1, PIXEL_SIZE - 1);
      gTile.dispose();

      // fill displayOverlay with texture
      displayOverlay = new BufferedImage(displayRect.width, displayRect.height, BufferedImage.TYPE_INT_ARGB_PRE);
      TexturePaint paint = new TexturePaint(tile, new Rectangle(PIXEL_SIZE, PIXEL_SIZE));
      Graphics2D gOverlay = (Graphics2D)displayOverlay.getGraphics();
      gOverlay.setPaint(paint);
      gOverlay.fillRect(0, 0, displayRect.width, displayRect.height);
      gOverlay.dispose();
    }

    // [re]init BB if necessary
    if (backBuffer == null)
    {
      // create backbuffer with this component's default pixel format
      backBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);//createImage(getWidth(), getHeight());

      // createImage will return null only when the UI is not yet displayed, so don't care
      if (backBuffer == null)
      {
        return;
      }

      // kill clip, need to repaint all
      clip = null;
    }

    Graphics g = backBuffer.getGraphics();

    g.clearRect(0, 0, getWidth(), getHeight());

    // draw the screen content
    drawDisplayContent(g, clip);
  }

  /**
   *  Overlay the content on the "LED display"
   */
  private void drawDisplayContent(Graphics g, Rectangle clip)
  {
    assert g != null;

    g.setClip(clip);

    int width  = getWidth();
    int height = getHeight();

    // narrow clip Rect to display area
    g.clipRect(displayRect.x, displayRect.y, displayRect.width+2, displayRect.height);

    assert debugClip(g, null);

    if (displayLargePixelImage == null)
    {
      displayLargePixelImage = new BufferedImage(displayRect.width, displayRect.height, BufferedImage.TYPE_INT_ARGB_PRE);
    }

    Graphics2D gg = (Graphics2D)displayLargePixelImage.getGraphics();
    gg.clearRect(0, 0, displayRect.width, displayRect.height);
    gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0F));

    if (!animating && trackInfoImage == null)
    {
      return;
    }

    Image imageToDraw = animating ? scrollImage : trackInfoImage;

    // draw the image (either track info or scroll depending on current mode) with pixelation
    gg.drawImage(imageToDraw, 0, 0, imageToDraw.getWidth(null)*PIXEL_SIZE, imageToDraw.getHeight(null)*PIXEL_SIZE, null);

    boolean usePixelatingOverlay = true;
    if (usePixelatingOverlay)
    {
      gg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F));
      gg.drawImage(displayOverlay, 0, 0, null);
    }

    gg.dispose();

    if (animating)
    {
      // blit the large pixelated scroll image
      g.drawImage(displayLargePixelImage, xPos, TEXT_YPOS, null);
    }
    else
    {
      // blit the large pixelated track info image
      g.drawImage(displayLargePixelImage, 0, 0, null);
    }


//       // draw scroll image from prerendered buffer
//       //      g.fillRect(xPos, TEXT_YPOS, scrollImage.getWidth(null)*PIXEL_SIZE, scrollImage.getHeight(null)*PIXEL_SIZE);
//       //       g.setColor(Color.white);
//       //       g.drawRect(xPos, TEXT_YPOS, scrollImage.getWidth(null)*PIXEL_SIZE-1, scrollImage.getHeight(null)*PIXEL_SIZE-1);


//       //Graphics2D g2 = (Graphics2D)g;
// //       g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
// //       g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
// //       g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//       //((Graphics2D)g).drawImage(displayLargePixelImage, new AffineTransform(0.8, 0.2, 0.2, 0.8, xPos + 0.5, TEXT_YPOS + 0.5), null);
//       //    ((Graphics2D)g).drawImage(displayLargePixelImage, new AffineTransform(1, 0, 0, 1, xPos + 0.5, TEXT_YPOS + 0.5), null);
//       //g.drawImage(displayLargePixelImage, xPos, TEXT_YPOS, null);

//       //g.drawImage(displayOverlay, xPos, TEXT_YPOS, null);
//     }
//     else
//     {
// //       Graphics2D g2 = (Graphics2D)g;
// //       g2.setFont(font.deriveFont(25.0F));
// //       g2.setColor(UISettings.DISPLAY_FOREGROUND);

// //       g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
// //       g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
// //       g2.drawString("00:32", DISPLAY_LEFT_INSET, g2.getFontMetrics().getHeight() + TEXT_YPOS);
//     }
  }

  /*********************************************************************************
   * PUBLIC INNER CLASS:  Thread to run the animation
   *********************************************************************************/
  public class AnimateRunnable implements Runnable
  {
    public void run()
    {
      xPos = getWidth() + 200; // start to right
      animating = true;

      //XXX:0:20050331iain: hack, wait for image
      while (displayLargePixelImage == null)
      {
        updateBackBuffer(null);
        Util.sleep(300);
      }

      while (xPos > -1 * displayLargePixelImage.getWidth(null)) //XXX:0000000000000000000000:20050202iain: sync??
      {
        //XXX:0000000:20050329iain: HACK
        while (GraphicDisplay.this == null)
        {
          Util.sleep(100);
        }

        synchronized(GraphicDisplay.this)
        {
          // adjust positon
          xPos -= X_INCREMENT;
          if (Math.abs(xPos - pauseXPos) < X_INCREMENT/2)
          {
            // stop dead center even if overshot
            xPos = pauseXPos;
            timeRemainingBeforePaint = PAUSE_TIME;
          }
          else
          {
            timeRemainingBeforePaint = APPROX_FRAME_TIME - (Util.getMillisSinceInit() - lastPaintTime);
          }

          // pos is updated, trigger a repaint
          repaint(xPos, TEXT_YPOS, displayLargePixelImage.getWidth(null) + X_INCREMENT + 50, displayLargePixelImage.getHeight(null)); //XXX:0:20050201iain: hack, should not need more than INC + 1 extra
        }

        // if we've been asked to keep the logo up (no tracks), we're done.
        if (xPos == pauseXPos)
        {
          if (keepLogo)
          {
            break;
          }
        }

        try
        {
          // XXX:0:20050202iain: same rect as above, share
          //updateBackBuffer(new Rectangle(xPos, 20, scrollImage.getWidth(null)*PIXEL_SIZE + 25, scrollImage.getHeight(null)*PIXEL_SIZE));
          updateBackBuffer(new Rectangle(xPos, TEXT_YPOS, displayLargePixelImage.getWidth(null) + X_INCREMENT + 50, displayLargePixelImage.getHeight(null)));
          Thread.sleep(Math.max(0, timeRemainingBeforePaint - 20));
        }
        catch (InterruptedException e) {}
      }
      animating = false;
      if (!keepLogo)
      {
        updateBackBuffer(null);
        repaint();
      }
      //try{Thread.sleep(10000);}catch (InterruptedException e){}
      //playAnim();
    }
    //System.out.println("XXX:000000000000000000:iain:>>>>Thread finished");
  }
}
