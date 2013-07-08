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
 * $Id: AmCodeUnitInterface.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */
package org.havi.system;

import org.havi.dcm.types.TargetId;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface AmCodeUnitInterface
{
  /**
   * Install an ApplicationModule code unit
   * @param targetId The TargetId of the ApplicationModule
   * @param n1Uniqueness Indication of whether the n1 field in the TargetId is persistenly unique.
   * @param listener Reference to a listener to be call on uninstallation.
   * @return 0 if the ApplicationModule installed sucessfully, otherwise 1.
   */
  public int install(TargetId targetId, boolean n1Uniqueness, UninstallationListener listener);
  
  /**
   * Uninstall an ApplicationModule code unit
   */
  public void uninstall();
}
