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
package com.streetfiresound.clientlib;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.client.UISettings;
import com.streetfiresound.clientlib.event.StreetFireEvent;
import com.streetfiresound.clientlib.event.ThumbnailsReadyEvent;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 *  Searches for album art covers via google image search
 *  @author iain huxley
 */
public class RequestDiscArtTask extends AbstractTask
{
  private StreetFireClient client;          // the primary client classx
  private LinkedList       urlList =  null; // list of results
  private ContentMetadata  discMetadata;    // disc that we're trying to find art for
  private Component        renderComponent; // component used as rendering target
  private int              lastSavedWidth = -1;  //

  /**
   * @param discMetaData the metadata for the disc in question
   */
  public RequestDiscArtTask(StreetFireClient client, ContentMetadata discMetadata, Component renderComponent)
  {
    // keep params
    this.client          = client;
    this.discMetadata    = discMetadata;
    this.renderComponent = renderComponent;
  }

  /**
   * for debugging
   */
  public String getTaskName()
  {
    return "Disc art request task";
  }

  /*
   *  The main method of the task:
   *
   *  o perform a google image search
   *  o filter the image list to get rid of non-square images
   *  o download the largest image
   *  o create and save thumbnails of various sizes
   */
  public void run()
  {
    LoggerSingleton.logDebugCoarse(this.getClass(), "run", "searching for cover art for disc '" + discMetadata.getTitle() + "'");

    // big try block, several methods throw IOExceptions
    try
    {
      // reset last width
      lastSavedWidth = -1;

      // prepare image query for google image search (advanced search), specifying medium size imagesx
      String urlString =   "http://images.google.com/images?as_q=" + createDiscSearchParameter(discMetadata)
                         + "&svnum=10&hl=en&btnG=Google+Search&as_epq=&as_oq=&as_eq=&imgsz=small%7Cmedium%7Clarge%7Cxlarge&as_filetype=&imgc=&as_sitesearch=&safe=images";

      LoggerSingleton.logDebugCoarse(this.getClass(), "run", " searching for cover art, search URL=\"" + urlString + "\"");
      URL url = new URL(urlString);

      // set up the connection
      URLConnection connection = url.openConnection();
      //connection.setRequestProperty("Cookie", "PREF=ae6777143f296193:TM=1106189529:LM=1106189529:S=B_C9SkjHDUew_Td9");
      //connection.setRequestProperty("User-agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.5) Gecko/20041107 Firefox/1.0");//  Mozilla/5.0");//(compatible; HADES)");
      connection.setRequestProperty("User-agent", "Mozilla/5.0 (compatible; HADES)");
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

      // read line by line
      String inputLine;
      urlList = new LinkedList();
      while ((inputLine = in.readLine()) != null)
      {
        // all image results are in currently in one line, find it
        int startIndex = inputLine.indexOf("imgurl=", 0);

        // load image with highest number of pixels
        int highestPixelNumber = 0;

        // loop finding image urls
        while (startIndex != -1)
        {
          // this is the line with all the iamge results, which have the image's URL in the imgurl= parameter, found above
          int endIndex = inputLine.indexOf("&", startIndex); // ends in '&'
          String urlText = inputLine.substring(startIndex + 7, endIndex);

          // image width comes afterwards via w and h params, parse it out
          int w = 0;
          int h = 0;
          try
          {
            int wIndex = inputLine.indexOf("w=", startIndex);
            w = Integer.parseInt(inputLine.substring(wIndex + 2, inputLine.indexOf("&", wIndex)));

            int hIndex = inputLine.indexOf("h=", startIndex);
            h = Integer.parseInt(inputLine.substring(hIndex + 2, inputLine.indexOf("&", hIndex)));
          }
          catch (NumberFormatException e)
          {
            // don't care, url will be skipped if w or h are 0
          }

          // see if it's a viable album art cover (squarish, reasonable size)
          double aspectRatio = (double)w/h;
          if (aspectRatio < 1.1 && aspectRatio > 0.9 && w > 40 && w < 500)
          {
            LoggerSingleton.logDebugCoarse(CreateImageThumbsTask.class, "run", "found    " + w + " x " + h + " image at: \t\"" + urlText + "\"");

//             if (w*h > highestPixelNumber)
//             {
              highestPixelNumber = w*h;
              // add the url to the list
              urlList.add(urlText);
              //            }
          }
          else
          {
            LoggerSingleton.logDebugCoarse(CreateImageThumbsTask.class, "run", "rejected " + w + " x " + h + " image at: \t\"" + urlText + "\"");
          }

          // find the next image
          startIndex = inputLine.indexOf("imgurl=", endIndex);
        }
      }
      in.close();
    }
    catch (IOException ex)
    {
      ex.printStackTrace();
    }

    // put a message if nothing found
    if (urlList.isEmpty())
    {
      LoggerSingleton.logDebugCoarse(RequestDiscArtTask.class, "run", "zero images found via google image serach");
    }

    // fire off image requesters
    // XXX:000000:20050119iain: don't use threads, use a media tracker!
    int count =0;
    for (Iterator i=urlList.iterator(); i.hasNext(); )
    {
      if (count++ >= 10)
      {
        break;
      }
      try
      {
        client.getTaskPool().execute(new CreateImageThumbsTask((String)i.next(), discMetadata.getContentId()));
      }
      catch (TaskAbortedException e)
      {
        e.printStackTrace(); //XXX:0000:20050224iain:
      }
    };
  }

  /**
   *  Create the text part of the disc search parameter
   *  Applies some basic prefiltering to get rid of terms which may reduce the effectiveness of the search
   */
  private String createDiscSearchParameter(ContentMetadata metadata)
  {
    String initialTerms = metadata.getTitle() + " " + metadata.getArtist();

    // get rid of some special characters
    initialTerms.replace('(', ' ');
    initialTerms.replace(')', ' ');
    initialTerms.replace('\'', ' ');
    initialTerms.replace('-', ' ');
    initialTerms.replace('&', ' ');
    initialTerms.replace('_', ' ');
    initialTerms.replace('@', ' ');
    initialTerms.replace('.', ' ');

    // iterate over the remaining tokenss
    StringBuffer result = new StringBuffer();
    for (StringTokenizer st = new StringTokenizer(initialTerms, " "); st.hasMoreTokens(); )
    {
      String token = st.nextToken();

//       // skip short tokens
//       if (token.length() <= 1)
//       {
//         continue;
//       }

      // ignore common tokens
      if (      token.equalsIgnoreCase("vol")
                || token.equalsIgnoreCase("volume")
                || token.equalsIgnoreCase("disc")
                || token.equalsIgnoreCase("self")
                || token.equalsIgnoreCase("titled")
                || token.equalsIgnoreCase("one")
                || token.equalsIgnoreCase("two"))
      {
        // continue without adding to search terms
        continue;
      }

//       // skip numbers
//       try
//       {
//         Integer.parseInt(token);

//         // if successfully parsed, continue without adding to search terms
//         continue;
//       }
//       catch (NumberFormatException e)
//       {
//       }

      // this one's alright
      result.append(token);
      if (st.hasMoreTokens())
      {
        result.append("+");
      }
    }
    // done
    return result.toString();
  }

  /**
   *  Synchronously save a thumbnail of an image
   *  @return an image containing the generated (and saved) thumbnail
   */ //XXX:0:20050224iain: could move to a generic util class
  public static synchronized Image saveThumb(int size, Image image, ContentId contentId, Component renderComponent)
  {
    // synchronized so that different tasks can't simultaneously write to the same image, which otherwise may be possible

    // set up a tracker to wait for images
    MediaTracker mediaTracker = new MediaTracker(renderComponent);
    mediaTracker.addImage(image, 0);
    try
    {
      mediaTracker.waitForID(0);
    }
    catch (InterruptedException e)
    {
    }

    // prepare for a little pixel math
    int    thumbSize   = size;
    int    thumbWidth  = thumbSize;
    int    thumbHeight = thumbSize;
    double thumbRatio  = (double)thumbWidth / (double)thumbHeight;
    int    imageWidth  = image.getWidth(null);
    int    imageHeight = image.getHeight(null);
    double imageRatio  = (double)imageWidth / (double)imageHeight;

    // correct one of the dimensions (depends on aspect ratio)
    if (thumbRatio < imageRatio)
    {
      thumbHeight = (int)(thumbWidth / imageRatio);
    }
    else
    {
      thumbWidth = (int)(thumbHeight * imageRatio);
    }

    // draw original image to thumbnail image object and
    // scale it to the new size on-the-fly
    Image thumbImageTest = image.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);

    mediaTracker.addImage(thumbImageTest, 1);
    try
    {
      mediaTracker.waitForID(1);
    }
    catch (InterruptedException e)
    {
    }

    // render the image to a buffer
    BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
    Graphics graphics = thumbImage.createGraphics();
    graphics.drawImage(thumbImageTest, 0, 0, thumbWidth, thumbHeight, null);

    // write the jpeg
    BufferedOutputStream out = null;
    String fileName = getThumbFilePath(contentId, thumbSize);
    try
    {
      LoggerSingleton.logDebugCoarse(RequestDiscArtTask.class, "saveThumb", "writing thumb, fileName is '" + fileName + "'");

      out = new BufferedOutputStream(new FileOutputStream(fileName));
      JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
      JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
      int quality = 90; // high quality
      //quality = Math.max(0, Math.min(quality, 100));
      param.setQuality((float)quality / 100.0f, false);
      encoder.setJPEGEncodeParam(param);
      encoder.encode(thumbImage);
      out.flush();
      out.close();
    }
    catch (IOException e)
    {
      throw new ClientRuntimeException("error encoding thumb to file '" + fileName + "'", e);
    }
    return thumbImage;
  }

  /**
   *  save thumbs of the passed image in the standard sizes (each of UISettings.ALBUM_THUMB_SIZE_XXX)
   */
  public static void saveThumbs(Image fullSizeImage, ContentMetadata discMetadata, StreetFireClient client, Component renderComponent)
  {
    // save a thumb in each of the configured sizes
    saveThumb(UISettings.ALBUM_THUMB_SIZE_SMALL,  fullSizeImage, discMetadata.getContentId(), renderComponent);
    saveThumb(UISettings.ALBUM_THUMB_SIZE_MEDIUM, fullSizeImage, discMetadata.getContentId(), renderComponent);
    saveThumb(UISettings.ALBUM_THUMB_SIZE_LARGE,  fullSizeImage, discMetadata.getContentId(), renderComponent);
    Image largestThumb = saveThumb(UISettings.ALBUM_THUMB_SIZE_HUGE,   fullSizeImage, discMetadata.getContentId(), renderComponent);

    // queue a notification
    // XXX:0:20050224iain: need to get a transaction id from the pool on startup and provide an accessor
    client.getEventDispatcher().queueEvent(new ThumbnailsReadyEvent(StreetFireEvent.NOT_APPLICABLE, discMetadata, largestThumb));
  }

  /**
   *  get a fully qualified pathfor identifying the thumbnail for this disc
   */
  public static String getThumbFilePath(ContentId contentId, int thumbSize)
  {
    return  "./thumbs/tn_" + thumbSize + "_" + contentId.getCompactStringId() + ".jpg";
  }

  /**********************************************************************************
   * INNER CLASS - image requester task
   * XXX:00000:20050121iain: mediatracker implementation probably better (possibly even in other the RequestDiscArtTask)
   *********************************************************************************/
  class CreateImageThumbsTask extends AbstractTask
  {
    private ContentId contentId;  // content id of the image, used to create the filenames
    private String url; // url to make thumbnails

    /**
     *  @param url the url for the image to save
     */
    public CreateImageThumbsTask(String url, ContentId contentId)
    {
      this.url = url;
      this.contentId = contentId;
    }

    /**
     *  For debugging
     */
    public String getTaskName()
    {
      return "Disc art request task";
    }

    /**
     * Download & save the picture
     */
    public void run()
    {
      try
      {
        final ImageIcon imageIcon = new ImageIcon(new URL(url));
        int w = imageIcon.getImage().getWidth(null);
        if (w > lastSavedWidth)
        {
          lastSavedWidth = w;
          saveThumbs(imageIcon.getImage(), discMetadata, client, renderComponent);
        }
      }
      catch (MalformedURLException ex)
      {
        ex.printStackTrace();
      }
    }
  }
}
