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
 * $Id: Util.java,v 1.7 2005/03/22 08:59:06 iain Exp $
 */

package com.streetfiresound.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;

/**
 * A singleton utility library that provides useful miscellaneous functions
 * throughout the GUI.
 *
 * Extends non-gui util class for scoping convenience
 */
public final class Util extends com.streetfiresound.clientlib.Util
{
    /**	
   * Private constructor to enforce singleton/zeroton structure
   */
  private Util() {}

  /**
   * Centers componentToMove over componentToCenterOver. If component to centerOver is not visible or
   * null, the componentToMove is center on screen.
   * @param componentToMove
   * @param componentToCenterOver
   */
  public static void moveToCenter(Component componentToMove, Component componentToCenterOver)
  {
    try
    {
      int centerX, centerY;

      // component is null
      if (componentToCenterOver == null)
      {
        // center on screen
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        centerX = p.x;
        centerY = p.y;
      }
      else
      {
        // try to get location on screen
        try
        {
          Point point = componentToCenterOver.getLocationOnScreen();
          centerX = (int)point.x + componentToCenterOver.getWidth()/2;
          centerY = (int)point.y + componentToCenterOver.getHeight()/2;
        }
        catch(IllegalComponentStateException ex)
        {
          // component not visible on screen
          Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
          centerX= dim.width/2;
          centerY= dim.height/2;
        }
      }

      // determine new local for component
      int moveX = centerX-(componentToMove.getWidth()/2);
      int moveY = centerY-(componentToMove.getHeight()/2);

      // move it.
      componentToMove.setLocation(moveX, moveY);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * convert a color string to a Color object
   * @param string color in hex in the form #XXXXXX
   */
  public static Color getColorFromHexString(String string)
  {
    try
    {
      return new Color(Integer.parseInt(string.substring(1), 16));// convert #XXXXXX format
    }
    catch (NumberFormatException ex)
    {
    }
    return null;
  }

  /**
   *  Wait for an image to be ready
   *  @return boolean false if interrupted
   */
  public static boolean waitForImage(Image image, Component component)
  {
    MediaTracker mediaTracker = new MediaTracker(component);
    mediaTracker.addImage(image, 0);
    try
    {
      mediaTracker.waitForID(0);
    }
    catch (InterruptedException e)
    {
      return false;
    }
    return true;
  }
}
