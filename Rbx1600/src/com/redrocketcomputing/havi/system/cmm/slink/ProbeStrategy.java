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
 * $Id: ProbeStrategy.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.slink;

import java.util.Set;

import com.redrocketcomputing.hardware.SlinkChannelController;

/**
 * @author stephen
 *
 */
public interface ProbeStrategy
{
  /**
   * Probe the specifed SLINK channels.
   * @param channels The array of channel to proble
   * @param activeDevices The set of currently known devices on all channels
   */
  public void probe(SlinkChannelController[] channels, Set activeDevices);

  /**
   * Return the set of new devices found on specified channel.  These devices must be bound to the corresponding SLINK
   * channel controller.
   * @return Set The set of new devices
   */
  public Set getNewDevices();

  /**
   * Return the set of devices which have disappear from specified channel. The devices in this set must be the
   * same objects as the activeDevices set provided in the probe method.
   * @return Set The set of new devices
   */
  public Set getGoneDevices();
}
