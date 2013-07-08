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
 * $Id: HaviServerHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package org.havi.system;

import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

/**
 * @author Daniel Bernstein
 */

public class HaviServerHelper extends HaviListener
{
  protected SoftwareElement se = null;

  /**
   *
   * @param se
   * @throws HaviGeneralException
   */
  public HaviServerHelper(SoftwareElement se) throws HaviMsgListenerExistsException
  {
    // Check parameter
    if (se == null)
    {
      throw new NullPointerException("software element can not be null");
    }

    // Save the software element
    this.se = se;

    // Bind to the software element
    se.addHaviListener(this);
  }

  /**
   * @see org.havi.system.HaviListener#receiveMsg(boolean, byte, SEID, SEID, Status, HaviByteArrayInputStream)
   */
  public boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload)
  {
    return false;
  }

  /**
   * Returns the software element
   * @return SoftwareElement
   */
  public SoftwareElement getSoftwareElement()
  {
    return se;
  }

}
