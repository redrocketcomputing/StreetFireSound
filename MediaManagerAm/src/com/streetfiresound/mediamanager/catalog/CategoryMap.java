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
 * $Id: CategoryMap.java,v 1.2 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.havi.dcm.types.HUID;

import com.redrocketcomputing.util.ListSet;
import com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class CategoryMap
{
  private Map map = new TreeMap();
  private int type;
  
  /**
   * Contruct an empty category cache
   */
  public CategoryMap(int type)
  {
    this.type = type;
  }
  
  public MLID[] get(String category)
  {
    // Lookup the catagory list
    Set set = (Set)map.get(category);
    if (set == null)
    {
      // Not found
      return new MLID[0];
    }
    
    // Convert list to array
    return (MLID[])set.toArray(new MLID[set.size()]);
  }
  
  /**
   * Add a MLID to the specified category
   * @param category The category key
   * @param mediaLocationId The MLID to add
   */
  public void put(String category, MLID mediaLocationId)
  {
    // Lookup the category
    Set set = (Set)map.get(category);
    if (set == null)
    {
      // Add new entry
      set = new ListSet();
      map.put(category, set);
    }
    
    // Add MLID
    set.add(mediaLocationId);
  }
  
  /**
   * Remove a MLID from all entries
   * @param mediaLocationId The MLID to remove
   */
  public void remove(MLID mediaLocationId)
  {
    // Loop through map to find matching MLIDs
    for (Iterator iterator = map.values().iterator(); iterator.hasNext();)
    {
      // Extract the category set
      Set element = (Set)iterator.next();
      
      // Try to remove from the set
      element.remove(mediaLocationId);
    }
  }
  
  /**
   * Remove all entries with match HUID
   * @param huid The HUID to remove
   */
  public void flush(HUID huid)
  {
    // Loop through all the categories looking for matches
    for (Iterator iterator = map.values().iterator(); iterator.hasNext();)
    {
      // Extract the category set
      Set element = (Set)iterator.next();
      
      // Loop through the MLID looking for matching HUID
      for (Iterator setIterator = element.iterator(); setIterator.hasNext();)
      {
        // Extract MLID
        MLID mlid = (MLID)setIterator.next();
        
        // Look for match
        if (huid.equals(mlid.getHuid()))
        {
          setIterator.remove();
        }
      }
    }
  }
  
  public CategorySummary[] getSummaries()
  {
    // Loop through the 
    CategorySummary[] summaries = new CategorySummary[map.size()];
    int position = 0;
    for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
    {
      // Extract the map entry
      Map.Entry element = (Map.Entry)iterator.next();
      String key = (String)element.getKey();
      Set set = (Set)element.getValue();
      
      // Build summary
      summaries[position++] = new CategorySummary(key, type, set.size());
    }
    
    // All done
    return summaries;
  }
}
