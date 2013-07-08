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
 * $Id: ServiceHandlerTable.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip;



/**
 * Table manager for all installed CmmIp service handlers.  The class manages multi-threaded access to the
 * underlaying List. Current there are not dynamic inserts by the CmmIp and not synchronization is provided
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class ServiceHandlerTable
{
  private ServiceHandler[] table;

  /**
   * Constructor for ServiceHandlerTable.
   */
  public ServiceHandlerTable(int size)
  {
    // Create the table
    table = new ServiceHandler[size];
  }

  /**
   * Add or replace a service handler
   * @param handler The new service handler
   */
  public void put(ServiceHandler handler)
  {
    // Check to see if this is an update
    table[handler.getServiceId()] = handler;
  }

  /**
   * Remove a service handler from the table.
   * @param handler The service handler to remove
   * @return boolean True is a service handle was removed, otherwise false.
   */
  public boolean remove(ServiceHandler handler)
  {
    // Check to see if the handle is present in the table
    boolean present = table[handler.getServiceId()] != null;

    // Clear anyways
    table[handler.getServiceId()] = null;

    // All done
    return present;
  }

  /**
   * Remove a service handle using the specified service handler ID. The removed service handler
   * is return.
   * @param serviceHandlerId The ID of the service handler to remove
   * @return ServiceHandler The service handler removed or null if not found in the table.
   */
  public ServiceHandler remove(int serviceHandlerId)
  {
    // Remember the service handler
    ServiceHandler handler = table[serviceHandlerId];

    // Clear it
    table[serviceHandlerId] = null;

    // All done
    return handler;
  }

  /**
   * Return a service handler using the specified service handler ID.
   * @param serviceHandlerId The ID of the service handler to get.
   * @return ServiceHandler The service handler or null is not found.
   */
  public ServiceHandler get(int serviceHandlerId)
  {
    // Get the element
    return table[serviceHandlerId];
  }

  /**
   * Return an array of all service handlers in the table.
   * @return ServiceHandler[] The array of installed service handler or a zero lenght array if there
   * are not install service handlers
   */
  public ServiceHandler[] getAll()
  {
    return table;
  }

  /**
   * Checks to see if the underlaying table contains the specified service handler ID
   * @param serviceHandlerId The service handler ID to check
   * @return boolean True is the service handler ID is in the table, false otherwise
   */
  public boolean isValid(int serviceHandlerId)
  {
    return table[serviceHandlerId] != null;
  }
}
