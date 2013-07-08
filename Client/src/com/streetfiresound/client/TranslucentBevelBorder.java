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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 *  Transucent border suitable for use on any background color/texture
 *  @author iain huxley
 */
public class TranslucentBevelBorder implements Border
{

  public static final int BEVEL_TYPE_LOWERED = 1;
  public static final int BEVEL_TYPE_RAISED  = 2;
  public static final int BEVEL_TYPE_LOWERED_NO_SIDES  = 3;

  private static final float opacityFactor = 0.6F;

  private static final float[] raisedOpacities  = { 0.55F, 0.60F, 0.30F,   // top
                                                    0.40F, 0.40F, 0.30F,  // left
                                                    0.80F, 0.80F, 0.60F,  // bottom
                                                    0.70F, 0.70F, 0.60F}; // right

  private static final float[] loweredOpacities = { 0.55F, 0.60F, 0.50F,   // top
                                                    0.40F, 0.40F, 0.30F,  // left
                                                    0.50F, 0.60F, 0.40F,  // bottom
                                                    0.40F, 0.40F, 0.30F}; // right

  private int bevelType;
  private int pad;

  /**
   *  @param bevelType bevel type - BEVEL_TYPE_RAISED or BEVEL_TYPE_LOWERED
   */
  public TranslucentBevelBorder(int bevelType, int pad)
  {
    assert bevelType == BEVEL_TYPE_RAISED || bevelType == BEVEL_TYPE_LOWERED || bevelType == BEVEL_TYPE_LOWERED_NO_SIDES;
    assert pad >= 0;
    this.bevelType = bevelType;
    this.pad = pad;
  }

  /**
   *  @return the insets of the border.
   */
  public Insets getBorderInsets(Component c)
  {
    int xSize = pad + 3;
    int ySize = (bevelType == BEVEL_TYPE_LOWERED_NO_SIDES ? 0 : xSize);
    return new Insets(ySize, xSize, ySize, xSize);
  }

  /**
   *  @return false always - the border is not opaque.
   */
  public boolean isBorderOpaque()
  {
    // translucent, not opaque..
    return false;
  }

  /**
   *  Paints the border for the specified component with the specified position and size.
   */
  public void	paintBorder(Component c, Graphics g, int x, int y, int w, int h)
  {
    // make 0, 0 the top left of the border
    g.translate(x, y);

    // init bevel brightnesses
    float highlt = (bevelType == BEVEL_TYPE_RAISED) ? 1.0F : 0.0F;
    float shadow = (bevelType == BEVEL_TYPE_RAISED) ? 0.0F : 1.0F;

    float[] opacities = bevelType == BEVEL_TYPE_RAISED ? raisedOpacities : loweredOpacities ;

    // draw lines as per following pixel "diagram" (T=top L=left etc.)
    //
    // TTTTTTTTTTTTR
    // LTTTTTTTTTTRR
    // LLTTTTTTTTRRR
    // LLL       RRR
    // LLL       RRR
    // LLLBBBBBBBBRR
    // LLBBBBBBBBBBR
    // LBBBBBBBBBBBB

    if (false)
    {
      g.setColor(Color.red);
      g.drawLine(0, 0, w - 1, 0);
      g.drawLine(0, 1, 0, h - 1);
      g.drawLine(0, h - 1, w - 1, h - 1);
      g.drawLine(w - 1, 0, w - 1, h - 2);
      return;
    }

    int i = 0;

    // top lines
    drawLine(g, 0,     0,     w - 1, 0,     highlt, opacities[i++]); // top
    drawLine(g, 1,     1,     w - 2, 1,     highlt, opacities[i++]); // 2nd to top
    drawLine(g, 2,     2,     w - 3, 2,     highlt, opacities[i++]); // 3rd to top

//     g.setColor(Color.red);
//     g.drawLine(2,     2,     w - 3, 2);

    if (bevelType != BEVEL_TYPE_LOWERED_NO_SIDES)
    {
      // left lines
      drawLine(g, 0,     1,     0,     h - 1, highlt, opacities[i++]); // left
      drawLine(g, 1,     2,     1,     h - 2, highlt, opacities[i++]); // 2nd to left
      drawLine(g, 2,     3,     2,     h - 3, highlt, opacities[i++]); // ..
    }
    else
    {
      i += 3;
    }

    // bottom lines
    drawLine(g, 0,     h - 1, w - 1, h - 1, shadow, opacities[i++]); // bottom
    drawLine(g, 1,     h - 2, w - 2, h - 2, shadow, opacities[i++]); // ..
    drawLine(g, 2,     h - 3, w - 3, h - 3, shadow, opacities[i++]);

    if (bevelType != BEVEL_TYPE_LOWERED_NO_SIDES)
    {
      // right lines
      drawLine(g, w - 1, 0,     w - 1, h - 2, shadow, opacities[i++]); // right
      drawLine(g, w - 2, 1,     w - 2, h - 3, shadow, opacities[i++]); // ..
      drawLine(g, w - 3, 2,     w - 3, h - 4, shadow, opacities[i++]);
    }
    else
    {
      i += 3;
    }

    // restore the original offset
    g.translate(-x, -y);
  }

  private void drawLine(Graphics g, int x1, int y1, int x2, int y2, float brightness, float opacity)
  {
    g.setColor(new Color(brightness, brightness, brightness, opacity * opacityFactor)); // right
    g.drawLine(x1, y1, x2, y2);
  }
}
