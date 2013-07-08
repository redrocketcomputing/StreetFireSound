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
 * $Id: SimpleDeviceAdaptorFactory.java,v 1.3 2005/03/16 04:23:42 stephen Exp $
 */
package com.streetfiresound.mediamanager.catalog;

import org.havi.system.constants.ConstAttributeName;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.types.Attribute;
import org.havi.system.types.HaviException;
import org.havi.system.types.Query;
import org.havi.system.types.SEID;

import com.redrocketcomputing.havi.system.am.ApplicationModule;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class SimpleDeviceAdaptorFactory implements DeviceAdaptorFactory
{
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptorFactory#create(org.havi.system.SoftwareElement, org.havi.system.types.SEID, org.havi.system.types.Attribute[])
   */
  public DeviceAdaptor create(ApplicationModule parent, SEID seid, Attribute[] attributes)
  {
    try
    {
      // Convert to attribute table
      SimpleAttributeTable simpleAttributeTable = new SimpleAttributeTable(attributes);
      
      // Make sure the InterfaceId is set
      if (!simpleAttributeTable.isValid(ConstAttributeName.ATT_INTERFACE_ID))
      {
        // Log an warning
        LoggerSingleton.logWarning(this.getClass(), "create", "InterfaceId is not setting in attributes");
        
        // Do not build anything
        return null;
      }
      
      // Use InterfaceId to determine type of adaptor
      switch (simpleAttributeTable.getInterfaceId())
      {
        case ConstMediaManagerInterfaceId.MEDIA_MANAGER:
        {
          LoggerSingleton.logDebugCoarse(this.getClass(), "create", "creating PLAY_LIST adaptor");
          return new PlayListDeviceAdaptor(parent, seid);
        }
        
        default:
        {
          LoggerSingleton.logDebugCoarse(this.getClass(), "create", "creating AVDISC_FCM adaptor");
          return new AvDiscDeviceAdaptor(parent, seid);
        }
      }
    }
    catch (HaviException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "create", e.toString());
      
      // Return null
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.catalog.DeviceAdaptorFactory#getQueries()
   */
  public Query[] getQueries()
  {
    // Create result array
    Query[] queries = new Query[2];
    
    // Build SonyJukebox query
    SimpleAttributeTable sonyJukeboxAttributeTable = new SimpleAttributeTable();
    sonyJukeboxAttributeTable.setSoftwareElementType(ConstSoftwareElementType.AVDISC_FCM);
    queries[0] = sonyJukeboxAttributeTable.toQuery();
    
    // Build PlayListCatalog query
    SimpleAttributeTable playListAttributeTable = new SimpleAttributeTable();
    playListAttributeTable.setSoftwareElementType(ConstSoftwareElementType.APPLICATION_MODULE);
    playListAttributeTable.setInterfaceId(ConstMediaManagerInterfaceId.MEDIA_MANAGER);
    queries[1] = playListAttributeTable.toQuery();
    
    // Return queries
    return queries;
  }
}
