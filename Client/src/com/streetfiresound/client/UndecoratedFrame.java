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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

/**
 *  Undecorated frame that supports move and resize
 *  @author iain huxley
 */
public class UndecoratedFrame extends JFrame
{
  private boolean moveEnabled = true;

  public UndecoratedFrame()
  {
    initUI();
  }

  public void setMoveEnabled(boolean enabled)
  {
    moveEnabled = enabled;
  }

  private void initUI()
  {
    // init main application frame
    Util.moveToCenter(this, null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setIconImage(ImageCache.getImageIcon("sfsl.gif").getImage());
    setTitle("Media Catalog");

    // get screen dim
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

    // choose initial size based on desired size, but make sure it's a bit below screen dim
    setBounds(10, 10, Math.min(screenDim.width  - 20, UISettings.APP_FRAME_WIDTH),
                      Math.min(screenDim.height - 50, UISettings.APP_FRAME_HEIGHT));

    setUndecorated(true);

    final Point origin = new Point();
    addMouseListener(
                       new MouseAdapter()
                       {
                         public void mousePressed(MouseEvent e)
                         {
                           if (moveEnabled)
                           {
                             origin.x = e.getX();
                             origin.y = e.getY();
                           }
                         }
                       });

    addMouseMotionListener(
                             new MouseMotionAdapter()
                             {
                               public void mouseDragged(MouseEvent e)
                               {
                                 if (moveEnabled)
                                 {
                                   Point p = getLocation();
                                   setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
                                 }
                               }
                             });
    // listen for resize mouse events
    Toolkit.getDefaultToolkit().addAWTEventListener(new ResizeListener(), AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
  }


  class ResizeListener implements AWTEventListener
  {
    private Point     startPoint  = null;
    private Rectangle startBounds = null;

    private Cursor oldCursor = null;

    public void eventDispatched(AWTEvent event)
    {
      if (event instanceof MouseEvent)
      {
        MouseEvent e = (MouseEvent)event;

        // adjust offset if target is not the frame
        if (e.getSource() != this)
        {
          e = SwingUtilities.convertMouseEvent((Component)e.getSource(), e, UndecoratedFrame.this);
        }

        Point currentPoint = e.getPoint();

        if (e.getID() == MouseEvent.MOUSE_PRESSED)
        {
          if (isInCorner(e))
          {
            // begin resize - keep start point, bounds
            startPoint = e.getPoint();
            startBounds = UndecoratedFrame.this.getBounds();

            // if we don't disable move in the Undecorated frame, strange behavior will result from attempting to move and resize based on the
            UndecoratedFrame.this.setMoveEnabled(false);
          }
        }
        else if (startPoint != null && e.getID() == MouseEvent.MOUSE_DRAGGED)
        {
          assert startBounds != null;
          Rectangle newRect = new Rectangle(startBounds.x, startBounds.y, startBounds.width  + currentPoint.x - startPoint.x,
                                            startBounds.height + currentPoint.y - startPoint.y);
          UndecoratedFrame.this.setBounds(newRect);
          UndecoratedFrame.this.validate();
        }
        else if (e.getID() == MouseEvent.MOUSE_RELEASED)
        {
          if (startPoint != null)
          {
            assert startBounds != null;

            // reset tracking vars
            startPoint  = null;
            startBounds = null;

            // allow move now
            UndecoratedFrame.this.setMoveEnabled(true);

            // repaint
            //this.validate();
          }
        }
        else if (e.getID() == MouseEvent.MOUSE_MOVED)
        {
          if (oldCursor == null && isInCorner(e))
          {
            oldCursor = UndecoratedFrame.this.getCursor();
            UndecoratedFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
          }
          else if (oldCursor != null && !isInCorner(e))
          {
            UndecoratedFrame.this.setCursor(oldCursor);
            oldCursor = null;
          }
        }
      }
    }

    private boolean isInCorner(MouseEvent e)
    {
      return e.getX() > UndecoratedFrame.this.getWidth()  - 18 && e.getY() > UndecoratedFrame.this.getHeight() - 18;
    }
  }
}
