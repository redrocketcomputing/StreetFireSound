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
 * $Id: ImageCache.java,v 1.3 2005/03/22 08:41:03 iain Exp $
 */

package com.streetfiresound.client;

import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 *  This Singleton utility class loads and caches images for reuse
 *  @author iain huxley
 */
public class ImageCache
{
  private static Map imageMap = new HashMap();
  private static ImageIcon unknownImageIcon;
  static
  {
    // init image for unknown placeholder
    unknownImageIcon = getImageIcon("unknown.gif");
  }

  /**
   * Private intitalizer to ensure singleton/zeroton
   */
  private ImageCache() {}

  /**
   * returns placeholder image  can be used to determine if image was found
   */
  public static ImageIcon getUnknownImageIcon()
  {
    return unknownImageIcon;
  }

  public static ImageIcon getImageIcon(String imageFileName)
  {
    return getImageIcon(imageFileName, true);
  }

  /**
   * Returns an image based on the specified image path
   * @param imageName
   * @param fromResources if true, load from resources (i.e. from jar), otherwise load directly from the provided path
   * @return
   */
  public static ImageIcon getImageIcon(String imageFileName, boolean fromResources)
  {

    String path = (fromResources ? "/images/" : "") + imageFileName;
    ImageIcon image = (ImageIcon)imageMap.get(path);

    if (image == null)
    {
      URL imageResource = null;

      //XXX:0:20050126iain: cleanup
      if (fromResources)
      {
        imageResource = ImageCache.class.getResource(path);
        image = new ImageIcon(Toolkit.getDefaultToolkit().createImage(imageResource));

        if (image == null)
        {
          LoggerSingleton.logWarning(ImageCache.class, "getImage", "failed: Image '" + path + "' not found, using dummy image");
          image = unknownImageIcon;
        }
      }
      else
      {
        image = new ImageIcon(path);
        if (image == null || !(new File(path)).exists())
        {
          //LoggerSingleton.logWarning(ImageCache.class, "getImage", "failed: Image '" + path + "' not found, using dummy image");
          image = unknownImageIcon;
        }
      }
    }
    return image;
  }
}
