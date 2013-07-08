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
 * $Id: Util.java,v 1.11 2005/04/12 21:54:37 iain Exp $
 */

package com.streetfiresound.clientlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.havi.dcm.types.HUID;
import org.havi.fcm.avdisc.types.ItemIndex;

import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaItemType;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;


/**
 * A singleton utility library that provides useful miscellaneous functions
 * associated with the clientlib
 *
 * Extends Util package util class for scoping convenience
 */
public class Util extends com.redrocketcomputing.util.Util
{
  /** token sent by the RBX1600 to indicate an unknown value */
  public static final String RBX1600_TOKEN_UNKNOWN = "UNKNOWN";

	/** token sent by the RBX1600 to indicate a empty slot */
  public static final String RBX1600_TOKEN_EMPTY   = "EMPTY";

	/** token used on the client side to display an unknown value */
  public static final String TOKEN_UNKNOWN         = "(unknown)";

	/** token used on the client side to display an empty slot */
  public static final String TOKEN_EMPTY           = "(empty)";

  /**
   * protected constructor help enforce singleton/zeroton structure
   */
  protected Util() {}

  /**
   * Conversts a byte array to a colon delimited string of hexadecimals.
   * @param byteArray
   * @return
   */
  public static String byteArrayToString(byte[] byteArray)
  {
    //write bytes to a string buffer with brackets
    //surrounding each byte
    if(byteArray== null || byteArray.length==0)
    {
      return "no data";
    }

    StringBuffer buffer = new StringBuffer("");
    for (int i = 0; i < byteArray.length; i++)
    {
      if(i > 0)
      {
        buffer.append(":");
      }
      String hex = Integer.toHexString(byteArray[i]&0xff);
      buffer.append( (hex.length()==1)?("0"+hex):hex );
    }

    return buffer.toString();
  }

  /**
   *  Convert an array of ItemIndexes ("standard" HAVi type) to MediaMetaData objects
   *  @param huid the huid of the items.  If null, the resulting MediaMetaData objects will have a null MLID  XXX:0:20050301iain: allow?
   */
  public static MediaMetaData[] itemIndexArrayToMediaMetaDataArray(HUID huid, ItemIndex[] info)
  {
    if (info == null)
    {
      return null; // hmm, OK I guess (?)
    }

    MediaMetaData[] result = new MediaMetaData[info.length];

    for (int i=0; i<info.length; i++)
    {
      result[i] = itemIndexToMediaMetaData(huid, info[i]);
    }
    return result;
  }

  public static VersionedIndex[] indexArrayToVersionedIndexArray(int version, int[] indices)
  {
    VersionedIndex[] result = new VersionedIndex[indices.length];
    for (int i=0; i<indices.length; i++)
    {
      result[i] = new VersionedIndex(indices[i], version);
    }
    return result;
  }

  public static MLID[] getMlidArray(MediaMetaData[] metaData)
  {
    MLID[] mlids = new MLID[metaData.length];
    for (int i=0; i<metaData.length; i++)
    {
      mlids[i] = metaData[i].getMediaLocationId();
    }
    return mlids;
  }

  public static MLID[] mediaMetaDataCollectionToMlidArray(Collection metaData)
  {
    MLID[] mlids = new MLID[metaData.size()];
    int index = 0;
    for (Iterator i=metaData.iterator(); i.hasNext();)
    {
      mlids[index++] = ((MediaMetaData)i.next()).getMediaLocationId();
    }
    return mlids;
  }

  public static ArrayList getMlidArrayList(Collection mediaMetaData)
  {
    ArrayList mlids = new ArrayList();
    for (Iterator i=mediaMetaData.iterator(); i.hasNext();)
    {
      mlids.add(((MediaMetaData)i.next()).getMediaLocationId());
    }
    return mlids;
  }

  public static ArrayList contentMetadataCollectionToContentIdArrayList(Collection contentMetadata)
  {
    ArrayList contentIds = new ArrayList();
    for (Iterator i=contentMetadata.iterator(); i.hasNext();)
    {
      contentIds.add(((ContentMetadata)i.next()).getContentId());
    }
    return contentIds;
  }

  public static ArrayList contentMetadataArrayToContentIdArrayList(ContentMetadata[] contentMetadata)
  {
    ArrayList contentIds = new ArrayList();
    for (int i=0; i<contentMetadata.length; i++)
    {
      contentIds.add(contentMetadata[i].getContentId());
    }
    return contentIds;
  }

  public static ContentId[] contentMetadataArrayToContentIdArray(ContentMetadata[] contentMetadata)
  {
    return contentMetadataArrayToContentIdArray(contentMetadata, false);
  }
  public static ContentId[] contentMetadataArrayToContentIdArray(ContentMetadata[] contentMetadata, boolean dropFirst)
  {
    // indices, length will be one less if dropping the first item
    int offset = dropFirst ? 1 : 0;

    ContentId[] contentIds = new ContentId[contentMetadata.length - offset];
    for (int i=1; i<contentMetadata.length; i++)
    {
      contentIds[i-offset] = contentMetadata[i].getContentId();
    }
    return contentIds;
  }

  public static ContentId[] mlidArrayToContentIdArray(MLID[] mlids)
  {
    ContentId[] contentIds = new ContentId[mlids.length];
    for (int i=0; i<mlids.length; i++)
    {
      contentIds[i] = new MlidContentId(mlids[i]);
    }
    return contentIds;
  }

  public static ArrayList mlidArrayToContentIdArrayList(MLID[] mlids)
  {
    ArrayList contentIds = new ArrayList();
    for (int i=0; i<mlids.length; i++)
    {
      contentIds.add(new MlidContentId(mlids[i]));
    }
    return contentIds;
  }

  /**
   *  @param mlidContentIds MUST be all of type MlidContentId
   */
  public static MLID[] mlidContentIdArrayToMlidArray(ContentId[] mlidContentIds)
  {
    MLID[] mlids = new MLID[mlidContentIds.length];
    for (int i=0; i<mlidContentIds.length; i++)
    {
      mlids[i] = ((MlidContentId)mlidContentIds[i]).getMlid();
    }
    return mlids;
  }

  public static ContentMetadata[] mediaMetaDataArrayToContentMetadataArray(MediaMetaData[] metadata)
  {
    ContentMetadata[] contentMetadata = new ContentMetadata[metadata.length];
    for (int i=0; i<metadata.length; i++)
    {
      contentMetadata[i] = new ContentMetadata(metadata[i]);
    }
    return contentMetadata;
  }

  public static MediaMetaData[] contentMetadataArrayToMediaMetaDataArray(ContentMetadata[] metadata)
  {
    MediaMetaData[] mediaMetaData = new MediaMetaData[metadata.length];
    for (int i=0; i<metadata.length; i++)
    {
      mediaMetaData[i] = Util.contentMetadataToMediaMetaData(metadata[i]);
    }
    return mediaMetaData;
  }

  public static ArrayList contentIdArrayToContentIdArrayList(ContentId[] contentIds)
  {
    ArrayList result = new ArrayList();
    for (int i=0; i<contentIds.length; i++)
    {
      result.add(contentIds[i]);
    }
    return result;
  }

  /**
   *  Convert an ItemIndex ("standard" HAVi type, not really an index but instead metadata) to MediaMetaData
   *  @param huid the huid of the item.  If null, the resulting MediaMetaData object will have a null MLID  XXX:0:20050301iain: allow?
   */
  public static MediaMetaData contentMetadataToMediaMetaData(ContentMetadata metadata)
  {
    // ContentMetaData must be mlid based if it can be converted to a MediaMetaData
    assert metadata.getContentId() instanceof MlidContentId;

    // get mlid, must not be null
    MLID mlid = ((MlidContentId)metadata.getContentId()).getMlid();
    assert mlid != null;

    MediaMetaData result = new MediaMetaData(mlid,
                                             metadata.getTitle(),
                                             metadata.getArtist(),
                                             metadata.getGenre(),
                                             metadata.getMediaType(),
                                             metadata.getPlaybackTime(),
                                             metadata.getContentSize(),
                                             metadata.getInitialTimeStamp(),
                                             metadata.getLastUpdateTimeStamp());

    // XXX:0:20050301iain: should go away
    unconvertSpecialTokens(result);
    return result;
  }

  /**
   *  Convert an ItemIndex ("standard" HAVi type, not really an index but instead metadata) to MediaMetaData
   *  @param huid the huid of the item.  If null, the resulting MediaMetaData object will have a null MLID  XXX:0:20050301iain: allow?
   */
  public static MediaMetaData itemIndexToMediaMetaData(HUID huid, ItemIndex info)
  {
    MediaMetaData result = new MediaMetaData(huid == null ? null : new MLID(huid, info.getList(), info.getIndex()),
                                             info.getTitle(),
                                             info.getArtist(),
                                             info.getGenre(),
                                             ConstMediaItemType.CDDA,
                                             info.getPlaybackTime(),
                                             info.getContentSize(),
                                             info.getInitialTimeStamp(),
                                             info.getLastUpdateTimeStamp());

    // XXX:0:20050301iain: should go away
    if (huid != null)
    {
      convertSpecialTokens(result, false);
    }
    return result;
  }


  /**
   *  Convert a media metadata object to an ItemIndex object ("standard" HAVi type, not really an index but instead metadata)
   *  @param huid the huid of the item.  If null, the resulting MediaMetaData object will have a null MLID  XXX:0:20050301iain: allow?
   */
  public static ItemIndex mediaMetaDataToItemIndex(MediaMetaData info)
  {
    MLID mlid = info.getMediaLocationId();
    ItemIndex result = new ItemIndex(mlid.getList(),
                                     mlid.getIndex(),
                                     info.getTitle(),
                                     info.getArtist(),
                                     info.getGenre(),
                                     ConstMediaItemType.CDDA,
                                     info.getPlaybackTime(),
                                     info.getContentSize(),
                                     info.getInitialTimeStamp(),
                                     info.getLastUpdateTimeStamp());
    return result;
  }

  /**
   *  XXX:0:20050301iain: temporary hack, converts the special tokens such as "EMPTY" or "UNKNOWN", also maps the empty string to unknown
   */
  public static ContentMetadata[] convertSpecialTokens(MediaMetaData[] metaData)
  {
    ContentMetadata[] result = new ContentMetadata[metaData.length];
    for (int i=0; i<metaData.length; i++)
    {
      //XXX:0:20050113iain: debug: uncomment following to prepend numbers to titles for list actions debugging etc.
      //       metaData[i].setTitle("" + i + ". " + metaData[i].getTitle());
      result[i] = convertSpecialTokens(metaData[i], false);
    }
    return result;
  }

  /**
   *  XXX:0:20050301iain: temporary hack, converts the special tokens such as "EMPTY" or "UNKNOWN", also maps the empty string to unknown
   */
  public static ContentMetadata convertSpecialTokens(MediaMetaData metaData, boolean requestNeededMetadata)
  {
    MLID mlid = metaData.getMediaLocationId();

    ContentMetadata resultMetadata = new ContentMetadata(metaData);


    // XXX:0000:20050125iain:HACK fill out unknown titles
    // hacks for unknown
    if (resultMetadata.getTitle() == null || resultMetadata.getTitle().equals("") || resultMetadata.getTitle().equalsIgnoreCase(Util.RBX1600_TOKEN_UNKNOWN))
    {
      resultMetadata.setTitle(TOKEN_UNKNOWN);
    }
    if (resultMetadata.getArtist() == null || resultMetadata.getArtist().equals("") || resultMetadata.getArtist().equalsIgnoreCase(Util.RBX1600_TOKEN_UNKNOWN))
    {
      resultMetadata.setArtist(TOKEN_UNKNOWN);
    }
    if (resultMetadata.getGenre() == null || resultMetadata.getGenre().equals("") || resultMetadata.getGenre().equalsIgnoreCase(Util.RBX1600_TOKEN_UNKNOWN))
    {
      resultMetadata.setGenre(TOKEN_UNKNOWN);
    }

    // hacks for empty
    if (resultMetadata.getTitle().equals(Util.RBX1600_TOKEN_EMPTY))
    {
      resultMetadata.setTitle(TOKEN_EMPTY);
    }
    if (resultMetadata.getArtist().equals(RBX1600_TOKEN_EMPTY))
    {
      resultMetadata.setArtist(TOKEN_EMPTY);
    }
    if (resultMetadata.getGenre().equals(RBX1600_TOKEN_EMPTY))
    {
      resultMetadata.setGenre(TOKEN_EMPTY);
    }

    //XXX:00000000:20050322iain: HACK metadata retrieval has arcane symantics, patch it up
    if (mlid.getIndex() > 0)
    {
      // track artists need to have the disc artist added

//       if (!initialArtist.equals(TOKEN_UNKNOWN)) // only adjust if the initial artist has a value
//       {
        // look for disc artist
        ContentMetadata rootContentMetadata = ContentMetadataCache.instance.getMetadata(new MlidContentId(new MLID(mlid.getHuid(), mlid.getList(), (short)0)), false, false);

        // if we found the disc item metadata, and it's not unknown, include it,
        if (rootContentMetadata != null)
        {
          if (!rootContentMetadata.getArtist().equals(TOKEN_UNKNOWN))
          {
            resultMetadata.setAlbumArtist(rootContentMetadata.getArtist());// + (equals.initialArtist("") ? "" : (" [" + initialArtist + "]")));
            if (resultMetadata.getArtist().equals(TOKEN_UNKNOWN))
            {
              resultMetadata.setArtist(rootContentMetadata.getArtist());
            }
          }
          if (!rootContentMetadata.getTitle().equals(TOKEN_UNKNOWN))
          {
            resultMetadata.setAlbumTitle(rootContentMetadata.getTitle());// + (initialArtist.equals("") ? "" : (" [" + initialArtist + "]")));
          }

        else
        {
          //XXX:000000:20050322iain: if not in cache, no attempt is currently made to fill it in later when the item arrives
        }
      }
    }


    return resultMetadata;
  }

  /**
   *  XXX:0:20050301iain: temporary hack, converts the special tokens used on the client side such as "(empty)" or "(unknown)", back to the rbx1600 version
   */
  public static void unconvertSpecialTokens(MediaMetaData metaData)
  {
    MLID mlid = metaData.getMediaLocationId();

    // XXX:0000:20050125iain:HACK fill out unknown titles
    // hacks for unknown
    if (metaData.getTitle().equalsIgnoreCase(Util.TOKEN_UNKNOWN))
    {
      metaData.setTitle(RBX1600_TOKEN_UNKNOWN);
    }
    if (metaData.getArtist().equalsIgnoreCase(Util.TOKEN_UNKNOWN))
    {
      metaData.setArtist(RBX1600_TOKEN_UNKNOWN);
    }
    if (metaData.getGenre().equalsIgnoreCase(Util.TOKEN_UNKNOWN))
    {
      metaData.setGenre(RBX1600_TOKEN_UNKNOWN);
    }

    // hacks for empty
    if (metaData.getTitle().equals(Util.TOKEN_EMPTY))
    {
      metaData.setTitle(RBX1600_TOKEN_EMPTY);
    }
    if (metaData.getArtist().equals(TOKEN_EMPTY))
    {
      metaData.setArtist(RBX1600_TOKEN_EMPTY);
    }
    if (metaData.getGenre().equals(TOKEN_EMPTY))
    {
      metaData.setGenre(RBX1600_TOKEN_EMPTY);
    }
  }
}
