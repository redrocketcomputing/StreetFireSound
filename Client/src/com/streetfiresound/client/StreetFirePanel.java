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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;

import javax.swing.JPanel;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Panel with support for texture backgrounds, obeys opacity
 * @author iain huxley
 */
public class StreetFirePanel extends JPanel
{
  /** unit increment for size changes (to avoid minor changes)*/
  static final int SIZE_INCREMENT = 32;

  /** default opaque panel background texture (should be larger than monitor resolutions) */
  static final Image defaultBackgroundImage = ImageCache.getImageIcon(UISettings.BACKGROUND_IMAGE_PATH).getImage();

  /** image for resize corner */
  private static Image resizeImage = ImageCache.getImageIcon("resize.png").getImage();

  /** background image, with tint applied */
  private static Image tintedBackgroundImage = null;
  private boolean resizeCornerEnabled = false;

  /** custom panel background texture (optional use) */
  private Image backgroundImage = null;

  /**
   *  convenience constructor for non-opaque panels
   */
  public StreetFirePanel()
  {
    this(false);
  }

  /**
   *  @param opaque if true, panel will be opaque and textured
   */
  public StreetFirePanel(boolean opaque)
  {
    super(new BorderLayout());
    setOpaque(opaque);
  }

  /**
   *  @param opaque if true, panel will be opaque and textured
   */
  public StreetFirePanel(String backgroundImageFile)
  {
    this(true);
    backgroundImage = ImageCache.getImageIcon(backgroundImageFile).getImage();
    Util.waitForImage(backgroundImage, this);
  }


  /**
   *  Initialize the tinted background image
   *  External use optional, as will be called automatically, but can be used to prepare early e.g. during splash screen
   */
  public void initializeBackground()
  {
    // will start at app's default size rounded up to SIZE_INCREMENT
    int backgroundW = UISettings.APP_FRAME_WIDTH  | SIZE_INCREMENT;
    int backgroundH = UISettings.APP_FRAME_HEIGHT | SIZE_INCREMENT;

    prepareBackgroundImage(backgroundW, backgroundH);
  }

  /**
   *  Display a resize icon in the corner wich allows window resize
   */
  public void setResizeCornerEnabled(boolean enabled)
  {
    resizeCornerEnabled = enabled;
  }

  /**
   *  Called by awt to invalidate component (e.g. when resized)
   */
  public void invalidate()
  {
    super.invalidate();
  }


  /**
   * called by swing to paint this component
   */
  public void paintComponent(Graphics g)
  {
    if (isOpaque())
    {
      if (backgroundImage != null)
      {
        g.drawImage(backgroundImage, 0, 0, null);
      }
      else
      {
        if (tintedBackgroundImage == null)
        {
          // init, will start at app's default size rounded to SIZE_INCREMENT
          initializeBackground();
        }
        else
        {
          int panelW = getWidth();
          int panelH = getHeight();
          int backgroundW = tintedBackgroundImage.getWidth(null);
          int backgroundH = tintedBackgroundImage.getHeight(null);

          if (panelW > backgroundW || panelH > backgroundH)
          {
            // *increase* size of background image by SIZE_INCREMENT only
            backgroundW = Math.max(backgroundW, panelW | SIZE_INCREMENT);
            backgroundH = Math.max(backgroundH, panelH | SIZE_INCREMENT);

            prepareBackgroundImage(backgroundW, backgroundH);
          }
        }
        g.drawImage(tintedBackgroundImage, 0, 0, null);
      }
    }

    if (resizeCornerEnabled)
    {
      g.drawImage(resizeImage, getWidth() - resizeImage.getWidth(null) - 5, getHeight() - resizeImage.getHeight(null) - 5, null);
    }
  }

  /**
   *  prepare the background image for this panel
   */
  private void prepareBackgroundImage(int w, int h)
  {
    // use single large filtered image source for all StreetFirePanels

    // XXX:0:20050317iain:, write filtered image to disc to speed startup

    LoggerSingleton.logDebugCoarse(StreetFirePanel.class, "prepareBackgroundImage", "tinting new background image of size " + w + "x" + h);
    int startTime = Util.getMillisSinceInit();

    // make sure the bg image is here
    Util.waitForImage(defaultBackgroundImage, this);

    tintedBackgroundImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics g = tintedBackgroundImage.getGraphics();

    // draw image, clipping messy stuff out w/ negative draw location
    g.drawImage(defaultBackgroundImage, -20, -20, null);

    // colorize it
    Image colorizedSizedImage = createImage(new FilteredImageSource(tintedBackgroundImage.getSource(), new ColorizeImageFilter(UISettings.BACKGROUND, 189, 0.6F)));
    Util.waitForImage(colorizedSizedImage, this);
    g.drawImage(colorizedSizedImage, 0, 0, this);

    LoggerSingleton.logDebugCoarse(StreetFirePanel.class, "prepareBackgroundImage", "finished tinting new background image, took " + (Util.getMillisSinceInit() - startTime) + "ms");
  }
}

