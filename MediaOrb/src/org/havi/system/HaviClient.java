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
 * $Id: HaviClient.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package org.havi.system;

import org.havi.system.types.SEID;
/**
 * <p>Title: HaviClient</p>
 * <p>Description: Base class for all client interfaces in the havi system.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, Inc</p>
 * @author Daniel Bernstein
 * @version 1.0
 */

public class HaviClient
{
  protected SEID destSeid = null;
  protected SoftwareElement se = null;

  /**
   * Empty Constructor.
   */
  protected HaviClient()
  {
  }

  /**
   * Construct object.
   * @param se
   * @param destSeid
   * @throws HaviGeneralException
   */
  protected HaviClient(SoftwareElement se, SEID destSeid)
  {
    // Check the parameters
    if (se == null || destSeid == null)
    {
      // Bad
      throw new IllegalArgumentException("SoftwareElement or SEID is null");
    }

    // Save the parameters
    this.se = se;
    this.destSeid = destSeid;
  }

  /**
   * Returns reference to internal object.
   * @return
   */
  public final SoftwareElement getSoftwareElement()
  {
    return se;
  }

  /**
   * Returns reference to internal object.
   * @return
   */
  public final SEID getDestSeid()
  {
    return destSeid;
  }
}
