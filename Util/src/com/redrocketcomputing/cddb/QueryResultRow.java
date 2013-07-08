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
 * $Id: QueryResultRow.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

public class QueryResultRow
{
  private String id = null;
  private String category = "";
  private String artist = "";
  private String title = "";

  public QueryResultRow(String id, String category, String artist, String title)
  {
    this.id = id;
    this.category = category;
    this.artist = artist;
    this.title = title;
  }

  public String getId()
  {
    return this.id;
  }

  public String getCategory()
  {
    return this.category;
  }

  public String getArtist()
  {
    return this.artist;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String toString()
  {
    return ("QueryResultRow[id=" + this.id + "; category=" + this.category + "; artist=" + this.artist + "; title=" + this.title + "]");
  }

}
