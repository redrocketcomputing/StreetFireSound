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
 * $Id: MaintenanceConstants.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;

/**
 *  Provides access to a maintenance service via TCP/IP, e.g. for
 *  backup/restore/update
 */
public interface MaintenanceConstants
{
  /**
   * TCP/IP port for service
   *
   * XXX:0000:20040916iain: should use existing system (offset x from base), read from properties
   */
  public static final int PORT = ConstCmmIpWellKnownAddresses.MAINTENANCE_ADDRESS;

  /**
   * Major Protocol version
   * Any incompatible change requires a bump in this version
   * (hopefully this will not change)
   */
  public static final int VERSION_MAJOR = 1;

  /**
   * Minor Protocol Version
   * Should be bumped as when functionality is added without breaking
   * prior functionality
   */
  public static final int VERSION_MINOR = 1;

  // codes sent in a MaintenanceHandshakeResponse
  public static final int HANDSHAKE_RESPONSE_SUCCESS = 0;
  public static final int HANDSHAKE_RESPONSE_FAIL    = 1;

  // codes sent in a MaintenanceCompleteNotification
  public static final int COMPLETION_RESPONSE_SUCCESS                         = 0;
  public static final int COMPLETION_RESPONSE_FAILED_INVALID_FILE_TYPE        = 1;
  public static final int COMPLETION_RESPONSE_FAILED_INVALID_FILE_VERSION     = 2;
  public static final int COMPLETION_RESPONSE_FAILED_INVALID_FILE_CORRUPT     = 3;
  public static final int COMPLETION_RESPONSE_FAILED_INVALID_PROTOCOL_VERSION = 4;
  public static final int COMPLETION_RESPONSE_FAILED_UNKNOWN_REASON           = 5;
}
