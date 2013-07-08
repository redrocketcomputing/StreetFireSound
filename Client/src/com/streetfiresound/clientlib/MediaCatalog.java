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

import java.util.List;


/**
 *  This class represents a media catalog provided on the network via HAVi
 *  It is a facade class which attempts to simplify access to a remote media catalog.
 *  In particular, it subscribes to necessary HAVi callbacks or events and delivers
 *  notification via StreetFireClient's event mechanism
 *
 *  NOTE:  Instead of the event(s) specified, all requests can generate RequestTimedoutEvent XXX:00000:20050321iain: implement
 *
 *  @author iain huxley
 */
public interface MediaCatalog
{


  //--------------------------------------------------------------------------------------------------------
  // SECTION: async request methods
  //--------------------------------------------------------------------------------------------------------

  /**
   * Asynchronously request a category summary
   * @return the transaction Id of the generated HAVi request or -1 if failed
   *
   * EVENT GENERATED: CategorySummaryArrivedEvent
   */
  public int requestCategorySummaries(int categoryType);

	/**
   * Asynchronously request media summaries matching a string with a chosen category
   * @param categoryType the type of media category, ConstCategoryType.ARTIST etc.
   * @param match requests items where the selected category type matches this string
   * @return the transaction Id of the generated HAVi request. or -1 if failed
   *
   * EVENT GENERATED: ContentIdsArrivedEvent
   */
  public int requestMediaSummaries(int categoryType, String match);


  /**
   * EVENT GENERATED: ContentIdsArrivedEvent
   */
  public int requestSearch(String contains);

  /**
   * Asynchronously request media metadata, expanding to provide more details
   * (e.g. if passed an MLID of a CD, will return all tracks, if passed an MLID of
   * a playlist, will return all items in the playlist)
   *
   * EVENT GENERATED: ContentIdsArrivedEvent
   *
   * @param categoryType the type of media category, ConstCategoryType.ARTIST etc.
   * @param match requests items where the selected category type matches this string
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  public int requestExpandItem(ContentId itemId);

  /**
   * Asynchronously request media meta data for multiple MLIDs.  Will not expand (i.e. it will only return one MediaMetaData object per MLID passed)
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   *
   * EVENT GENERATED: ContentMetadataArrivedEvent
   */
  int requestMetadata(List contentIds);

  /**
   * Asynchronously request save (update) of meta data
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   *
   * EVENT GENERATED: OperationSuccessfulEvent
   */
  public int requestPutMetadata(ContentMetadata discMetadata, List trackMetadata);
}
