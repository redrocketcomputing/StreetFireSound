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
 * $Id: Garp.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import org.havi.system.types.GUID;

/**
 * GUID Address Resolution Protocol.  This is a GOF Facade for the GARP components
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public interface Garp
{
  /**
   * Translate the specified guid into a encode IP address
   * @param guid The guid to translate
   * @return GarpEntry The resolved GUID entry or null if not found
   */
  public GarpEntry resolve(GUID guid) throws GarpException;

  /**
   * Checks to see if the GUID is present on the network
   * @param guid The guid to check
   * @return boolean True if the GUID is present or false if the state is not RESETTTING OR READY or
   * the GUID is not in the map.
   */
  public boolean isValid(GUID guid);

  /**
   * Returns the activeDevices.
   * @return GUID[]
   */
  public GUID[] getActiveDevices();

  /**
   * Returns the nonactiveDevices.
   * @return GUID[]
   */
  public GUID[] getNonactiveDevices();

  /**
   * Force the GARP to perform a network reset
   */
  public void forceNetworkReset();

  /**
   * Check the current state of the protocol
   * @return boolean True is the protocol is ready, false otherwise
   */
  public boolean isReady();

  /**
   * Add a new listener to the internal listener dispatch map.  The listener class is inspected to determine
   * the types of event to handle.  This resolved by identifing the GarpEventListener interfaces implemented.
   * @param listener The new GARP event listener
   */
  public void addListener(GarpEventListener listener);

  /**
   * Remove the specified listener from the interanl dispatch map.
   * @param listener The listener to remove
   */
  public void removeListener(GarpEventListener listener);
}
