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
 * $Id: CodeUnitEntry.java,v 1.2 2005/02/24 03:30:22 stephen Exp $
 */
package com.redrocketcomputing.havi.system.amm;

import org.havi.system.AmCodeUnitInterface;
import org.havi.system.UninstallationListener;
import org.havi.system.types.ApplicationModuleGuestId;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class CodeUnitEntry implements UninstallationListener
{
  private ApplicationModuleGuestId guestId = null;
  private AmCodeUnitInterface codeUnit = null;
  private ApplicationModuleCodeUnitClassLoader classLoader = null;
  private UninstallListener listener;
  

  /**
   * @param guestId
   * @param codeUnit
   * @param classLoader
   * @param listener
   */
  public CodeUnitEntry(ApplicationModuleGuestId guestId, AmCodeUnitInterface codeUnit, ApplicationModuleCodeUnitClassLoader classLoader, UninstallListener listener)
  {
    super();
    this.guestId = guestId;
    this.codeUnit = codeUnit;
    this.classLoader = classLoader;
    this.listener = listener;
  }
  /**
   * @return Returns the codeUnit.
   */
  public final AmCodeUnitInterface getCodeUnit()
  {
    return codeUnit;
  }
  
  /**
   * @return Returns the guestId.
   */
  public final ApplicationModuleGuestId getGuestId()
  {
    return guestId;
  }

  /**
   * @return Returns the classLoader.
   */
  public final ApplicationModuleCodeUnitClassLoader getClassLoader()
  {
    return classLoader;
  }

  /* (non-Javadoc)
   * @see org.havi.system.UninstallationListener#uninstalled()
   */
  public void uninstalled()
  {
    // Forward
    listener.uninstalled(guestId);
  }
}
