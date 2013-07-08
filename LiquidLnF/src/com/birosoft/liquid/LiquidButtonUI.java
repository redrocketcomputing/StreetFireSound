/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *        Liquid Look and Feel                                                  *
 *                                                                              *
 *  Author, Miroslav Lazarevic                                                  *
 *                                                                              *
 *   For licensing information and credits, please refer to the                 *
 *   comment in file com.birosoft.liquid.LiquidLookAndFeel                      *
 *                                                                              *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package com.birosoft.liquid;

import com.birosoft.liquid.skin.Skin;
import com.birosoft.liquid.skin.SkinToggleButtonIndexModel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Dimension;


/**
 * This class represents the UI delegate for the JButton component.
 *
 * @author Taoufik Romdhane
 */
public class LiquidButtonUI extends BasicButtonUI {
    public static final boolean HINT_DO_NOT_PAINT_TOOLBARBUTTON_IF_NO_MOUSE_OVER =
        true;

    /**
     * The Cached UI delegate.
     */
    private static final LiquidButtonUI buttonUI = new LiquidButtonUI();

    /** the skin  for buttons */
    static Skin skinButton;
    private static Skin skinToolbar;
    private static Skin skinSmallButton;

    /** the stroke for the fcouse */
    static BasicStroke focusStroke = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f,
            new float[] { 1.0f, 1.0f }, 1.0f);

    /** the index model. Allow default buttons */
    SkinToggleButtonIndexModel buttonIndexModel = new SkinToggleButtonIndexModel(true);

    /** the index model. Forbid default buttons */
    SkinToggleButtonIndexModel toolbarIndexModel = new SkinToggleButtonIndexModel();

    // ********************************
    //          Install
    // ********************************
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
    }

    public void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
    }

    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect,
        Rectangle textRect, Rectangle iconRect) {
        Rectangle bounds = b.getBounds();
        int offset = (bounds.height / 2) - 5;

        if (buttonIndexModel.getIndexForState() == 2) {
            return;
        }

        if ((b.getClientProperty("JToolBar.isToolbarButton") != Boolean.TRUE) &&
                b.isFocusPainted()) {
            paintFocus(g, offset);
        }
    }

    public void paintFocus(Graphics g, int offset) {
//         Graphics2D g2d = (Graphics2D) g;
//         g.setColor(new Color(196, 195, 194));
//         g2d.drawLine(6, offset, 11, offset + 5);
//         g.setColor(new Color(175, 174, 174));
//         g2d.drawLine(6, offset + 1, 6, offset + 11);
//         g2d.drawLine(6, offset + 11, 11, offset + 6);
    }

    /**
     * Creates the UI delegate for the given component.
     *
     * @param c The component to create its UI delegate.
     * @return The UI delegate for the given component.
     */
    public static ComponentUI createUI(final JComponent c) {
        JButton b = (JButton) c;
        b.setRolloverEnabled(true);

        // If we used an transparent toolbutton skin we would have to add:
        c.setOpaque(false);
        c.addPropertyChangeListener("opaque",
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    c.setOpaque(false);
                }
            });

        return buttonUI;
    }

  public Dimension getPreferredSize(JComponent c)
  {
    Dimension d = super.getPreferredSize(c);
    d.height = 35; //XXX:00000000000:20050319iain: move hardcoded var
    return d;
  }

  public Dimension getMinimumSize(JComponent c)
  {
    Dimension d = super.getMinimumSize(c);
    d.height = 35; //XXX:00000000000:20050319iain: move hardcoded var
    return d;
  }

    /**
     * We don't want to paint the pressed state here - the skin does it for us.
     * @see javax.swing.plaf.basic.BasicButtonUI#paintButtonPressed(Graphics, AbstractButton)
     */
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
    }

    public void paint(Graphics g, JComponent c) {
        if (c instanceof JToggleButton) {
            super.paint(g, c);

            return;
        }

        AbstractButton button = (AbstractButton) c;

        if (button.getClientProperty("JToolBar.isToolbarButton") == Boolean.TRUE) {
            toolbarIndexModel.setButton(button);

            int index = toolbarIndexModel.getIndexForState();

            if (button.hasFocus() && (index == 0)) {
                index = 1; // my change
            }

            getSkinToolbar().draw(g, index, button.getWidth(), button.getHeight());
        }
        else
        {
            buttonIndexModel.setButton(button);
            buttonIndexModel.setCheckForDefaultButton(button instanceof JButton);

            int index = buttonIndexModel.getIndexForState();

            if (index > 3)
            {
                index -= 4;
            }

            if (button.hasFocus() && (index == 0))
            {
              //index = 1; // my change
            }

            if ((button.getHeight() < 30) || (button.getWidth() < 30))
            {
              getSkinSmallButton().draw(g, index, button.getWidth(), button.getHeight());

              // don't paint the focus when button is too small
              button.setFocusPainted(false);
            }
            else
            {
                getSkinButton().draw(g, index, button.getWidth(), button.getHeight());
            }

            if ((index == 4) && button.isFocusPainted())
            {
                Rectangle bounds = button.getBounds();
                paintFocus(g, (bounds.height / 2) - 5);
            }
        }

        super.paint(g, c);
    }


//     /**
//      * Creates a new skin from the image file with fileName fileName and the number of
//      * images passed in <code>nrImages</code>. The scaling region of the image is given
//      * by ulX,ulY,lrX,lrY
//      * @param fileName the filename of the image file
//      * @param nrImages the number of subimages in the image file
//      * @param ulX the distance from the left edge to the scaling region of the skin
//      * @param ulY the distance from the top edge to the scaling region of the skin
//      * @param lrX the distance from the right edge to the scaling region of the skin
//      * @param lrY the distance from the bottom edge to the scaling region of the skin
//      */
//     public Skin(String fileName, int nrImages, int ulX, int ulY, int lrX, int lrY) {

    public Skin getSkinButton() {
        if (skinButton == null) {
          //System.out.println("XXX:000000000000000000:iain:>>>>initializing standard skin button");

          skinButton = new Skin("toolbar-button.png", 5, 15, 15, 15, 15); // L, T, R, B
            skinButton.colourImage();
        }

        return skinButton;
    }

    public Skin getSkinToolbar() {
        if (skinToolbar == null) {
//             if (LiquidLookAndFeel.toolbarFlattedButtons) {
//                 skinToolbar = new Skin("toolbar.png", 8, 4, 13, 4, 10);
//             } else {
//                 skinToolbar = new Skin("toolbar-nonflatted.png", 8, 4, 13, 4, 10);
//             }
          //System.out.println("XXX:000000000000000000:iain:>>>>initializing standard toolbar button");

            skinToolbar = new Skin("toolbar-button.png", 5, 10, 10, 12, 12);
            skinToolbar.colourImage();
        }

        return skinToolbar;
    }

    public Skin getSkinSmallButton() {
        if (skinSmallButton == null) {

            skinSmallButton = new Skin("smallbutton.png", 5, 7, 7, 7, 7);
            skinSmallButton.colourImage();
            //skinSmallButton = new Skin("toolbar-nonflatted.png", 8, 4, 13, 4, 10);
        }

        return skinSmallButton;
    }

    public void update(Graphics g, JComponent c) {
        paint(g, c);
    }
}
