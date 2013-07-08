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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.event.ContentIdsArrivedEvent;
import java.util.List;


/**
 *  This class represents a media catalog provided on the network via HAVi
 *  It is a facade class which attempts to simplify access to a remote media catalog.
 *  In particular, it subscribes to necessary HAVi callbacks or events and delivers
 *  notification via StreetFireClient's event mechanism
 *
 *  @author iain huxley
 */
public class DatabaseMediaCatalog implements MediaCatalog
{
  StreetFireClient client;
  Connection connnection;

  /**
   * Constructor - connects to remote media catalog
   *
   * @param client the associated streetfireclient XXX:0:20041217iain: does not
   */
  public DatabaseMediaCatalog(StreetFireClient client)
  {
    this.client = client;

    try
    {
      // Load the HSQL Database Engine JDBC driver
      // hsqldb.jar should be in the class path or made part of the current jar
      Class.forName("org.hsqldb.jdbcDriver");
    }
    catch (ClassNotFoundException e)
    {
      throw new ClientRuntimeException("could not load hsql driver", e);
    }

    try
    {
      connnection = DriverManager.getConnection("jdbc:hsqldb:file:db/dc", "sa", "");


//       Statement st = null;
//       ResultSet rs = null;

//       st = connnection.createStatement();         // statement objects can be reused with

//       // repeated calls to execute but we
//       // choose to make a new one each time
//       rs = st.executeQuery("select * from mp3");    // run the query

//       // do something with the result set.
//       dump(rs);
//       st.close();    // NOTE!! if you close a statement the associated ResultSet is

      // closed too
      // so you should copy the contents to some other object.
      // the result set is invalidated also  if you recycle an Statement
      // and try to execute some other query before the result set has been
      // completely examined.
    }
    catch (SQLException e)
    {
      throw new ClientRuntimeException("error executing query", e);
    }
  }


  public static void dump(ResultSet rs) throws SQLException
  {
    // the order of the rows in a cursor
    // are implementation dependent unless you use the SQL ORDER statement
    ResultSetMetaData meta   = rs.getMetaData();
    int               colmax = meta.getColumnCount();
    int               i;
    Object            o = null;

    // the result set is a cursor into the data.  You can only
    // point to one row at a time
    // assume we are pointing to BEFORE the first row
    // rs.next() points to next row and returns true
    // or false if there is no next row, which breaks the loop
    for (; rs.next(); )
    {
      for (i = 0; i < colmax; ++i)
      {
        o = rs.getObject(i + 1);    // Is SQL the first column is indexed

        // with 1 not 0
        System.out.print(o.toString() + " ");
      }

      System.out.println(" ");
    }
  }


  /**
   * EVENT GENERATED: ContentIdsArrivedEvent
   */
  public int requestSearch(String contains)
  {
    return -1;
  }

  /**
   * Asynchronously request a category summary
   * @return the transaction Id of the generated HAVi request or -1 if failed
   */
  public int requestCategorySummaries(int categoryType)
  {
    LoggerSingleton.logDebugCoarse(DatabaseMediaCatalog.class, "requestCategorySummaries", "requesting category summaries for category type " + categoryType);
    return 0;
  }


  /**
   * Asynchronously request media summaries matching a string with a chosen category
   * @param categoryType the type of media category, ConstCategoryType.ARTIST etc.
   * @param match requests items where the selected category type matches this string
   * @return the transaction Id of the generated HAVi request. or -1 if failed
   */
  public int requestMediaSummaries(int categoryType, String match)
  {
    LoggerSingleton.logDebugCoarse(DatabaseMediaCatalog.class, "requestMediaSummaries", "requesting media summaries for category " + categoryType + ", match='" + match + "'");


    client.executeTask(new RetrieveMetaDataTask(55));
    return 55;
  }

  /**
   * Asynchronously request media metadata, expanding to provide more details
   * (e.g. if passed an MLID of a CD, will return all tracks, if passed an MLID of
   * a playlist, will return all items in the playlist)
   *
   * @param categoryType the type of media category, ConstCategoryType.ARTIST etc.
   * @param match requests items where the selected category type matches this string
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  public int requestExpandItem(ContentId contentId)
  {
    LoggerSingleton.logDebugCoarse(DatabaseMediaCatalog.class, "requestExpandedMediaMetaData", "requesting expanded meta data for content id " + contentId);
    return 0;
  }

  /**
   * Asynchronously request media meta data for multiple MLIDs.  Will not expand (i.e. it will only return one MediaMetaData object per MLID passed)
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  public int requestMetadata(List contentIds)
  {
    // convert to array
    ContentId[] contentIdArray = new ContentId[contentIds.size()];
    int index = 0;
    for (Iterator i=contentIds.iterator(); i.hasNext();)
    {
      contentIdArray[index++] = (ContentId)i.next();
    }

    // forward to method with array signature
    return requestMetadata(contentIdArray);
  }

  /**
   * Asynchronously request media meta data for multiple MLIDs.  Will not expand (i.e. it will only return one MediaMetaData object per MLID passed)
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  private int requestMetadata(ContentId[] contentIds)
  {
    LoggerSingleton.logDebugCoarse(DatabaseMediaCatalog.class, "requestMetaData", "requesting metadata for " + contentIds.length  + " mlids");

    return 0;
  }


  /**
   * Asynchronously request save (update) of meta data
   * @return the transaction Id of the generated HAVi request, or -1 if failed
   */
  public int requestPutMetadata(ContentMetadata discMetadata, List trackMetadata)
  {
    LoggerSingleton.logDebugCoarse(DatabaseMediaCatalog.class, "requestPutMediaMetadata", "requesting metadata store for root contentId " + discMetadata.getContentId());
    return 0;
  }


  public class RetrieveMetaDataTask extends AbstractTask
  {
    private int transactionId;

    public RetrieveMetaDataTask(int transactionId)
    {
      this.transactionId = transactionId;
    }

    public void run()
    {
      try
      {
        Statement st = null;
        ResultSet rs = null;

        st = connnection.createStatement();         // statement objects can be reused with

        // repeated calls to execute but we
        // choose to make a new one each time
        rs = st.executeQuery("select ID from mp3");    // run the query

        System.out.println("XXX:000000000000000000:iain:>>>>rs.getFetchSize() is '" + rs.getFetchSize() + "'");

        // set to end temporarily to find # of rows
        rs.last();
        int numRows = rs.getRow();
        rs.first();

        LoggerSingleton.logDebugCoarse(RetrieveMetaDataTask.class, "run", "found " + numRows + " results");


        ContentId[] result = new ContentId[numRows];
        while (!rs.isAfterLast())
        {
          int currentRow = rs.getRow();


          //XXX:0:20050320iain: data crow uses stupid id
          String idString = rs.getString(0);
          int id = Integer.parseInt(idString.substring(7));
          result[currentRow] = new DbContentId(id);
          rs.next();
        }

        // queue the event for delivery
        client.getEventDispatcher().queueEvent(new ContentIdsArrivedEvent(transactionId, result));


        st.close();
        // NOTE!! if you close a statement the associated ResultSet is closed too
        // so you should copy the contents to some other object.
        // the result set is invalidated also  if you recycle an Statement
        // and try to execute some other query before the result set has been
        //completely examined.
      }
      catch (SQLException e)
      {
        throw new ClientRuntimeException("error executing query", e);
      }
    }
  }
}

/*
        MediaMetaData[] result = new MediaMetaData[numRows];
        while (!rs.isAfterLast())
        {
          int currentRow = rs.getRow();
          result[currentRow] = new MediaMetaData();
          result[currentRow].setTitle( rs.getString("TITLE"));
          result[currentRow].setArtist(rs.getString("ARTIST"));
          result[currentRow].setGenre( rs.getString("GENRE"));
          rs.next();
        }
*/

