/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *	Liquid Look and Feel                                                   *
 *                                                                              *
 *  Author, Miroslav Lazarevic                                                  *
 *                                                                              *
 *   For licensing information and credits, please refer to the                 *
 *   comment in file com.birosoft.liquid.LiquidLookAndFeel                      *
 *                                                                              *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.birosoft.liquid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.birosoft.liquid.skin.Skin;
import com.streetfiresound.client.ImageCache;
import javax.swing.ImageIcon;
import java.awt.Image;

/**
 * Liquid's split pane divider
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @version 1.17 12/03/01
 * @author Steve Wilson
 * @author Ralph kar
 */
class LiquidSplitPaneDivider extends BasicSplitPaneDivider
{
    private int inset = 0;

  private Image grabImage;

    public LiquidSplitPaneDivider(BasicSplitPaneUI ui)
    {
        super(ui);
        //setLayout(new LiquidDividerLayout());

        grabImage = ImageCache.getImageIcon("grab.png").getImage();
    }

    public void paint(Graphics g)
    {
      int height = getHeight();
      int width  = getWidth();
      g.drawImage(grabImage, width/2 - grabImage.getWidth(null)/2, height/2 - grabImage.getHeight(null)/2, null);
    }

    /**
     * Used to layout a XPSplitPaneDivider. Layout for the divider
     * involves appropriately moving the left/right buttons around.
     * <p>
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of XPSplitPaneDivider.
     */
    public class LiquidDividerLayout implements LayoutManager
    {
        public void layoutContainer(Container c)
        {
            JButton leftButton = getLeftButtonFromSuper();
            JButton rightButton = getRightButtonFromSuper();
            JSplitPane splitPane = getSplitPaneFromSuper();
            int orientation = getOrientationFromSuper();
            int oneTouchSize = getOneTouchSizeFromSuper();
            int oneTouchOffset = getOneTouchOffsetFromSuper();
            Insets insets = getInsets();

            // This layout differs from the one used in BasicSplitPaneDivider.
            // It does not center justify the oneTouchExpadable buttons.
            // This was necessary in order to meet the spec of the Liquid
            // splitpane divider.
            if (leftButton != null && rightButton != null && c == LiquidSplitPaneDivider.this)
            {
                if (splitPane.isOneTouchExpandable())
                {
                    if (orientation == JSplitPane.VERTICAL_SPLIT)
                    {
                        int extraY = (insets != null) ? insets.top : 0;
                        int blockSize = getDividerSize();

                        if (insets != null)
                        {
                            blockSize -= (insets.top + insets.bottom);
                        }
                        blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(oneTouchOffset, extraY, blockSize * 2, blockSize);
                        rightButton.setBounds(oneTouchOffset + oneTouchSize * 2, extraY, blockSize * 2, blockSize);
                    } else
                    {
                        int blockSize = getDividerSize();
                        int extraX = (insets != null) ? insets.left : 0;

                        if (insets != null)
                        {
                            blockSize -= (insets.left + insets.right);
                        }
                        blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(extraX, oneTouchOffset, blockSize, blockSize * 2);
                        rightButton.setBounds(extraX, oneTouchOffset + oneTouchSize * 2, blockSize, blockSize * 2);
                    }
                } else
                {
                    leftButton.setBounds(-5, -5, 1, 1);
                    rightButton.setBounds(-5, -5, 1, 1);
                }
            }
        }

        public Dimension minimumLayoutSize(Container c)
        {
            return new Dimension(0, 0);
        }

        public Dimension preferredLayoutSize(Container c)
        {
            return new Dimension(0, 0);
        }

        public void removeLayoutComponent(Component c)
        {
        }

        public void addLayoutComponent(String string, Component c)
        {
        }
    }

        /*
         * The following methods only exist in order to be able to access protected
         * members in the superclass, because these are otherwise not available
         * in any inner class.
         */

    int getOneTouchSizeFromSuper()
    {
        return super.ONE_TOUCH_SIZE;
    }

    int getOneTouchOffsetFromSuper()
    {
        return super.ONE_TOUCH_OFFSET;
    }

    int getOrientationFromSuper()
    {
        return super.orientation;
    }

    JSplitPane getSplitPaneFromSuper()
    {
        return super.splitPane;
    }

    JButton getLeftButtonFromSuper()
    {
        return super.leftButton;
    }

    JButton getRightButtonFromSuper()
    {
        return super.rightButton;
    }
}
