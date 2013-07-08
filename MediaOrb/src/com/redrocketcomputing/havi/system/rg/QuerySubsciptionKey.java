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
 * $Id: QuerySubsciptionKey.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rg;

import org.havi.system.types.SEID;

/**
 * @author stephen
 *
 */
class QuerySubsciptionKey
{
	private int queryId;
	private SEID seid;

  /**
   * Constructor for QuerySubsciptionKey.
   */
  public QuerySubsciptionKey(SEID seid, int queryId)
  {
  	// Save the parameters
  	this.seid = seid;
  	this.queryId = queryId;
  }

  /**
   * @see java.lang.Object#equals(Object)
   */
  public boolean equals(Object o)
  {
  	// Check instance type
  	if (o instanceof QuerySubsciptionKey)
  	{
  		// Cast it up
  		QuerySubsciptionKey other = (QuerySubsciptionKey)o;

  		// Check contents
  		return seid.equals(other.seid) && queryId == other.queryId;
  	}

  	// Wrong type
  	return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
		return seid.hashCode() + queryId;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
  	return "QuerySubscriptionKey[" + seid.toString() + ',' + queryId + "]";
  }
  /**
   * Returns the queryId.
   * @return int
   */
  public int getQueryId()
  {
    return queryId;
  }

  /**
   * Returns the seid.
   * @return SEID
   */
  public SEID getSeid()
  {
    return seid;
  }

  /**
   * Sets the queryId.
   * @param queryId The queryId to set
   */
  public void setQueryId(int queryId)
  {
    this.queryId = queryId;
  }

  /**
   * Sets the seid.
   * @param seid The seid to set
   */
  public void setSeid(SEID seid)
  {
    this.seid = seid;
  }

}
