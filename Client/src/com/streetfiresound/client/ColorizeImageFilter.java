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


import java.awt.Color;
import java.awt.image.RGBImageFilter;

/**
 *  Colorizes an image
 *  @author iain huxley
 */
public class ColorizeImageFilter extends RGBImageFilter
{
  Color tintColor;
  int tintImageMidPoint;
  float contrast;

  /**
   *  @param tintColor the base color to tint
   *  @param tintImageMidPoint the midpoint of the luminosity of the tint image
   *  @param contrast how much to vary the output image
   *
   *  e.g. for use with a brushed aluminum grayscale image ranging from 0-255,
   *  a tintImageMidPoint of 128 would be used, and a contrast of, say 0.3 might be
   *  used to make the brushed aluminum effect more subtle
   */
  public ColorizeImageFilter(Color tintColor, int tintImageMidPoint, float contrast)
  {
    this.tintColor = tintColor;
    this.tintImageMidPoint = tintImageMidPoint;
    this.contrast = contrast;

    // this filter's operation does not depend on the pixel's location, so set
    // the canFilterIndexColorModel field so that IndexColorModels can be filtered directly
    canFilterIndexColorModel = true;
  }

  /**
   * perform the filter
   */
  public int filterRGB(int x, int y, int rgb)
  {
      int rIn = (rgb & 0x00FF0000) >> 16;
      int gIn = (rgb & 0x0000FF00) >> 8;
      int bIn = (rgb & 0x000000FF) >> 0;
      //System.out.println("XXX:000000000000000000:iain:>>>>rgb is " + r + "," + g + "," + b);

      int rOut = tintColor.getRed()   + (int)((rIn - tintImageMidPoint) * contrast);
      rOut = Math.max(0,   rOut);
      rOut = Math.min(255, rOut);

      int gOut = tintColor.getGreen() + (int)((gIn - tintImageMidPoint) * contrast);
      gOut = Math.max(0,   gOut);
      gOut = Math.min(255, gOut);

      int bOut = tintColor.getBlue()  + (int)((bIn - tintImageMidPoint) * contrast);
      bOut = Math.max(0,   bOut);
      bOut = Math.min(255, bOut);

      return   0xFF000000
             | rOut << 16
             | gOut << 8
             | bOut << 0;
  }
}
