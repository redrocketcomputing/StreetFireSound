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
 * $Id: SeidWatcher.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms;

import gnu.trove.TLinkableAdaptor;

import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

/**
 * @author stephen
 *
 */
class SeidWatcher extends TLinkableAdaptor
{
  private SEID sourceSeid;
  private SEID destinationSeid;
  private OperationCode opCode;

  /**
   * Constructor for SeidWatcher.
   */
  public SeidWatcher(SEID sourceSeid, SEID destinationSeid, OperationCode opCode)
  {
    // Construct super class
    super();

    // Check the parameters
    if (sourceSeid == null || destinationSeid == null || opCode == null)
    {
      // Bad stuff
      throw new IllegalArgumentException("parameter is null");
    }

    // Save the parameters
    this.sourceSeid = sourceSeid;
    this.destinationSeid = destinationSeid;
    this.opCode = opCode;
  }

  /**
   * Returns the destinationSeid.
   * @return SEID
   */
  public SEID getDestinationSeid()
  {
    return destinationSeid;
  }

  /**
   * Returns the opCode.
   * @return OperationCode
   */
  public OperationCode getOpCode()
  {
    return opCode;
  }

  /**
   * Returns the sourceSeid.
   * @return SEID
   */
  public SEID getSourceSeid()
  {
    return sourceSeid;
  }
}
