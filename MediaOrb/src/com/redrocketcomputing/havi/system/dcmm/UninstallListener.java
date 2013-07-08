package com.redrocketcomputing.havi.system.dcmm;

import org.havi.system.types.DcmGuestId;

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
   * @param guestId The DcmGuestId of the code unit which has uninstalled
   */
  public void uninstalled(DcmGuestId guestId);
}
