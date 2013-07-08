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
 * $Id: RegistryDatabase.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rg;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.havi.dcm.types.HUID;
import org.havi.system.constants.ConstAttributeName;
import org.havi.system.constants.ConstBoolOperation;
import org.havi.system.types.Attribute;
import org.havi.system.types.ComplexQuery;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.Query;
import org.havi.system.types.SEID;
import org.havi.system.types.SimpleQuery;

import com.redrocketcomputing.havi.util.AttributeValueComparator;
import com.redrocketcomputing.havi.util.AttributeValueComparatorFlyweight;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.ListSet;
import com.redrocketcomputing.util.concurrent.ReadWriteLock;
import com.redrocketcomputing.util.concurrent.Sync;
import com.redrocketcomputing.util.concurrent.WriterPreferenceReadWriteLock;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author unascribed
 * @version 1.0
 */

class RegistryDatabase
{
  private ReadWriteLock lock = new WriterPreferenceReadWriteLock();
  private Sync readLock = lock.readLock();
  private Sync writeLock = lock.writeLock();
  private Map database = new ListMap();

  public RegistryDatabase()
  {
  }

  public final Attribute[] get(SEID seid)
  {
    try
    {
      readLock.acquire();
      return (Attribute[])database.get(seid);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }

  public final void put(SEID seid, Attribute[] table)
  {
    try
    {
      writeLock.acquire();
      
      database.put(seid, table);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    finally
    {
      // Always unlock
      writeLock.release();
    }
  }

  public final Attribute[] remove(SEID seid)
  {
    try
    {
      writeLock.acquire();
      return (Attribute[])database.remove(seid);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    finally
    {
      // Always unlock
      writeLock.release();
    }
  }

  public final boolean contains(SEID seid)
  {
    try
    {
      readLock.acquire();
      return database.containsKey(seid);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }
  
  public final SEID[] getSeids()
  {
    try
    {
      readLock.acquire();
      return (SEID[])database.keySet().toArray(new SEID[database.size()]);
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }

  public final Set run(Query query)
  {
    try
    {
      readLock.acquire();
      
      // Start processing
      Set result = process(query);

      // Return the result
      return result;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }

  public HUID getHuid(SEID seid)
  {
    try
    {
      readLock.acquire();
      
      // Get the attribute table for the seid
      Attribute[] table = (Attribute[])database.get(seid);
      if (table == null)
      {
        return null;
      }

      // Look for huid
      for (int i = 0; i < table.length; i++)
      {
        // Check huid attribute name
        if (table[i].getName() == ConstAttributeName.ATT_HUID)
        {
          return new HUID(new HaviByteArrayInputStream(table[i].getValue()));
        }
      }

      // Not found
      return null;
    }
    catch (HaviUnmarshallingException e)
    {
      LoggerSingleton.logError(this.getClass(), "getHuid", e.toString());
      return null;
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new IllegalStateException(e.toString());
    }
    finally
    {
      // Always unlock
      readLock.release();
    }
  }

  private Set process(Query query)
  {
    // Check for simple query
    if( query instanceof SimpleQuery)
    {
      // Simple query, get match seids
      return match((SimpleQuery)query);
    }

    // Must be a complexe query
    ComplexQuery complex = (ComplexQuery)query;

    // Decend based on complex query operation tyoe
    if (complex.getBoolOperation() == ConstBoolOperation.AND)
    {
      // AND decend
      return and(complex.getQuery1(), complex.getQuery2());
    }
    else
    {
      // OR decend
      return or(complex.getQuery1(), complex.getQuery2());
    }
  }

  private Set match(SimpleQuery a)
  {
		// Allocate a new set to hold the result
		Set result = new ListSet();

		// Loop through the entries
		for (Iterator i = database.entrySet().iterator(); i.hasNext(); )
		{
		  // Get the database record
		  Map.Entry record = (Map.Entry)i.next();

		  // Get the registry entry
		  Attribute[] table = (Attribute[])record.getValue();
		  if (table == null)
		  {
		    LoggerSingleton.logFatal(this.getClass(), "match", "table is null");
		    throw new IllegalStateException("table is null");
		  }

		  // Get comparator fly wieght
		  AttributeValueComparator comparator = AttributeValueComparatorFlyweight.getComparator(a.getCompareOperation());

		  // Loop through the table looking for matching attributes
		  for (int j = 0; j < table.length; j++)
		  {
		    // Check for matching name and value
		    if (a.getAttributeName() == table[j].getName() && comparator.match(table[j].getValue(), a.getCompareValue()))
		    {
		      // Add to result set
		      result.add(record.getKey());
		    }
		  }
		}

		// All done
		return result;
  }

  private Set and(Query a, Query b)
  {
    // Get result left and right hand sides
    Set ra = process(a);
    Set rb = process(b);

    // Set intersection
    ra.retainAll(rb);

    // Return the result
    return ra;
  }

  private Set or(Query a, Query b)
  {
    // Get result left and right hand sides
    Set ra = process(a);
    Set rb = process(b);

    // Set union
    ra.addAll(rb);

    // Return result
    return ra;
  }

  /**
   * Returns the database.
   * @return SyncMap
   */
  public Map getDatabase()
  {
    return database;
  }
}
