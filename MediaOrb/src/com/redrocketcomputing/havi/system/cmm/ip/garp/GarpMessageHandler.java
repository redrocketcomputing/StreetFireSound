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
 * $Id: GarpMessageHandler.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.garp;

import org.havi.system.types.GUID;

/**
 * GARP message handler interface.  This is used to dispatch message via GOF Visitor Pattern
 *
 * @author stephen Jul 23, 2003
 * @version 1.0
 *
 */
public interface GarpMessageHandler
{
  /**
   * Timeout message from the GARP socket
   */
  public void handleTimeout();

  /**
   * Device information message
   * @param guid The guid of the device
   * @param address The IP address
   * @param port The base IP port
   */
  public void handleDeviceInfo(GUID guid, byte[] address, int port);


  /**
   * Gone device message generated with a device is shutting down or an error has been detected
   * @param guid The address of the device disappearing
   */
  public void handleGoneDevice(GUID guid);

  /**
   * Reset network message generated when a device wishes to re-detect the network
   * @param guid The address of the device forcing a network reset
   */
  public void handleResetNetwork(GUID guid);
}
