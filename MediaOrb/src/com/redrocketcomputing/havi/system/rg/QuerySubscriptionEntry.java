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
 * $Id: QuerySubscriptionEntry.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rg;

import org.havi.system.constants.ConstBoolOperation;
import org.havi.system.registry.rmi.MatchFoundMessageBackClient;
import org.havi.system.types.Attribute;
import org.havi.system.types.ComplexQuery;
import org.havi.system.types.Query;
import org.havi.system.types.SimpleQuery;

import com.redrocketcomputing.havi.util.AttributeValueComparator;
import com.redrocketcomputing.havi.util.AttributeValueComparatorFlyweight;

/**
 * @author stephen
 *
 */
class QuerySubscriptionEntry
{
	private MatchFoundMessageBackClient client;
	private Query query;
	private int queryId;

  /**
   * Constructor for QuerySubscriptionEntry.
   */
  public QuerySubscriptionEntry(int queryId, Query query, MatchFoundMessageBackClient client)
  {
  	// Check parameters
  	if (client == null || query == null)
  	{
  		throw new IllegalArgumentException("query or client is null");
  	}

  	// Save the parameters
  	this.queryId = queryId;
  	this.client = client;
  	this.query = query;
  }

  /**
   * Run a internal query against the provided attribute table
   * @param table The attribute table to check
   * @return MatchFoundMessageBackClient No null result if the query match, null otherwise
   */
  public MatchFoundMessageBackClient matches(Attribute[] table)
  {
  	// Return client if the table matches the query
  	if (process(query, table))
  	{
  		return client;
  	}

  	// No match
  	return null;
  }

  /**
   * Process query using left recurive decent
   * @param query The query to use when matching against the attribute table
   * @param table The attribute table to match against
   * @return boolean True is the query matchs the table, false otherwise
   */
  private boolean process(Query query, Attribute[] table)
  {
    // Check for simple query
    if (query instanceof SimpleQuery)
    {
      // Simple query, get match seids
      return match((SimpleQuery)query, table);
    }

    // Must be a complexe query
    ComplexQuery complex = (ComplexQuery)query;

    // Decend based on complex query operation tyoe
    if (complex.getBoolOperation() == ConstBoolOperation.AND)
    {
      // AND decend
      return and(complex.getQuery1(), complex.getQuery2(), table);
    }
    else
    {
      // OR decend
      return or(complex.getQuery1(), complex.getQuery2(), table);
    }
  }

  /**
   * Match a simple query against the attribute table. This is the recurive terminating function
   * @param a The simple query
   * @param table The attribute table to match
   * @return boolean True if the table matches the query
   */
  private boolean match(SimpleQuery a, Attribute[] table)
  {
    // Get comparator fly wieght
    AttributeValueComparator comparator = AttributeValueComparatorFlyweight.getComparator(a.getCompareOperation());

    // Loop through the table looking for matching attributes
    for (int j = 0; j < table.length; j++)
    {
      // Check for matching name and value
      if (a.getAttributeName() == table[j].getName() && comparator.match(table[j].getValue(), a.getCompareValue()))
      {
      	// Matched
      	return true;
      }
    }

    // All done no match
    return false;
  }

  /**
   * Recursive method for handle complex querys with an "and" cause
   * @param a The left hand query
   * @param b The right hand query
   * @param table The attribute table to query
   * @return boolean True if a && b, false otherwise
   */
  private boolean and(Query a, Query b, Attribute[] table)
  {
    // Return the result
    return process(a, table) && process(b, table);
  }

  /**
   * Recursive method for handle complex querys with an "or" cause
   * @param a The left hand query
   * @param b The right hand query
   * @param table The attribute table to query
   * @return boolean True if a || b, false otherwise
   */
  private boolean or(Query a, Query b, Attribute[] table)
  {
    // Return result
    return process(a, table) || process(b, table);
  }

  /**
   * Returns the queryId.
   * @return int
   */
  public int getQueryId()
  {
    return queryId;
  }
}
