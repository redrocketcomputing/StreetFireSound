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
 * $Id: ThinBevelBorder.java,v 1.1 2005/02/24 01:52:25 iain Exp $
 */

package com.streetfiresound.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

/**
 * A thin (1 pixel) bevel border
 */
public class ThinBevelBorder extends LineBorder
{
  public Color highlightColor = Color.WHITE;
  public Color shadowColor    = Color.GRAY;

  private int type; // BevelBorder.RAISED or BevelBorder.LOWERED

  /**
   * Create a thin (1 pixel) bevel border
   * @param type BevelBorder.RAISED or BevelBorder.LOWERED
   */
  public ThinBevelBorder(int type, Color highlightColor, Color shadowColor)
  {
    super(Color.BLUE); // arbitrary, not used

    if (highlightColor != null)
    {
      this.highlightColor = highlightColor;
    }
    if (shadowColor != null)
    {
      this.shadowColor = shadowColor;
    }

    // check type
    if (!(type == BevelBorder.LOWERED || type == BevelBorder.RAISED))
    {
      throw new IllegalArgumentException("invalid bevel border type");
    }

    // keep type
    this.type = type;
  }

  /**
   * Create a thin (1 pixel) bevel border
   * @param type BevelBorder.RAISED or BevelBorder.LOWERED
   */
  public ThinBevelBorder(int type)
  {
    this(type, null, null);
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
  {
    // calc width/height to be added to x, y
    int w = width  - 1;
    int h = height - 1;

    // figure out the two colors
    Color topLeftColor     = null;
    Color bottomRightColor = null;
    switch (type)
    {
    case BevelBorder.LOWERED:
      topLeftColor     = shadowColor;
      bottomRightColor = highlightColor;
      break;

    case BevelBorder.RAISED:
      topLeftColor     = highlightColor;
      bottomRightColor = shadowColor;
      break;
    }

    // draw the top and left
    g.setColor(topLeftColor);
    g.drawLine(x + 0, y + 0, x + w, y + 0);
    g.drawLine(x + 0, y + 0, x + 0, y + h);

    // draw the shadow bottom and right
    g.setColor(bottomRightColor);
    g.drawLine(x + 1, y + h, x + w, y + h);
    g.drawLine(x + w, y + 0, x + w, y + h);
  }
}
