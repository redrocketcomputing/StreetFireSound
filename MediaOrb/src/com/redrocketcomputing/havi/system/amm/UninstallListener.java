package com.redrocketcomputing.havi.system.amm;

import org.havi.system.types.ApplicationModuleGuestId;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
interface UninstallListener
{
  /**
   * Invoked when an code unit has uninstalled
   * @param guestId The ApplicationModuleGuestId of the code unit which has uninstalled
   */
  public void uninstalled(ApplicationModuleGuestId guestId);
}
