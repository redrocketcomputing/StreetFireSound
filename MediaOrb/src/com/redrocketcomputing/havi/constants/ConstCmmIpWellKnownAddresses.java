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
 * $Id: ConstCmmIpWellKnownAddresses.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.constants;

/**
 * Manifest service handler constants.  These constants glue service handler classes across implementations
 * of CmmIp service handlers
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public interface ConstCmmIpWellKnownAddresses
{
  public static final int ROOT_PORT_ADDRESS = 46000;
  public static final String MULTICAST_ADDRESS = "239.192.0.1";

  public static final int ADDRESS_SPACE_SIZE = 10;
  public static final int GADP_ADDRESS = 0x00000005;
  public static final int GARP_ADDRESS = 0x00000006;
  public static final int TCP_MESSAGE_ADDRESS = 0x00000002;
  public static final int REMOTE_SHELL_ADDRESS = 0x00000003;
  public static final int MAINTENANCE_ADDRESS = 0x00000004;
}
