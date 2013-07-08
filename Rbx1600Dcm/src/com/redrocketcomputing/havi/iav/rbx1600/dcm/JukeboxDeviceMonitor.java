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
 * $Id: JukeboxDeviceMonitor.java,v 1.1 2005/02/22 03:49:21 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.dcm;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.SlinkDeviceProbe;
import com.redrocketcomputing.havi.system.cmm.slink.CmmSlink;
import com.redrocketcomputing.havi.system.cmm.slink.ProbeStrategy;
import com.redrocketcomputing.havi.system.cmm.slink.SlinkMonitorProbeCompletedEventListener;
import com.redrocketcomputing.havi.system.cmm.slink.SlinkMonitorProbeStartedEventListener;
import com.redrocketcomputing.havi.system.cmm.slink.SlinkProbeAbortedException;
import com.redrocketcomputing.util.concurrent.Gate;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class JukeboxDeviceMonitor implements SlinkMonitorProbeStartedEventListener, SlinkMonitorProbeCompletedEventListener
{
  private CmmSlink cmmSlink = (CmmSlink)ServiceManager.getInstance().get(CmmSlink.class);
  private ProbeStrategy probe = new SlinkDeviceProbe();
  private Gate probeComplete = new Gate();
  private int[] newDevices = new int[0];
  private int[] goneDevices = new int[0];
  private int[] activeDevice = new int[0];
  
  /**
   * Contruct a new JukeboxDeviceMonitor
   */
  public JukeboxDeviceMonitor()
  {
    // Attach probe
    cmmSlink.addStrategy(probe);
    
    // Attach event listener
    cmmSlink.addEventListener(this);
  }
  
  /**
   * Release all resources
   */
  public void close()
  {
    // Remove listener
    cmmSlink.removeEventListener(this);
    
    // Remove probe
    cmmSlink.removeStrategy(probe);
  }
  
  /**
   * @return Returns the activeDevice.
   */
  public final int[] getActiveDevice()
  {
    return activeDevice;
  }

  /**
   * @return Returns the goneDevices.
   */
  public final int[] getGoneDevices()
  {
    return goneDevices;
  }

  /**
   * @return Returns the newDevices.
   */
  public final int[] getNewDevices()
  {
    return newDevices;
  }
  
  /**
   * Start a Cmm probe and wait for completions
   * @return True is probe completed successfully, False otherwise
   */
  public final boolean probe()
  {
    try
    {
      // Clear gate
      probeComplete.reset();
      
      // Lanch probe
      cmmSlink.probe();
      
      // Wait for completion
      probeComplete.acquire();

      // All good
      return true;
    }
    catch (SlinkProbeAbortedException e)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "probe", "aborted");

      // Aborted
      return false;
    }
    catch (InterruptedException e)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "probe", "interrupted");

      // Clear Interrupted
      Thread.currentThread().interrupted();
      
      // Return false
      return false;
    }
  }


  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.cmm.slink.SlinkMonitorProbeStartedEventListener#probeStarted()
   */
  public void probeStarted()
  {
    // LoggerSingleton.logDebugCoarse(this.getClass(), "probeStarted", "started");
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.cmm.slink.SlinkMonitorProbeCompletedEventListener#probeCompleted(int[], int[], int[])
   */
  public void probeCompleted(int[] newDevices, int[] goneDevices, int[] activeDevices)
  {
    // LoggerSingleton.logDebugCoarse(this.getClass(), "probeCompleted", newDevices.length + " new, " + goneDevices.length + " gone, " + activeDevices.length + " active");

    // Save the probe information
    this.newDevices = newDevices;
    this.goneDevices = goneDevices;
    this.activeDevice = activeDevices;
    
    // Mark as completed
    probeComplete.release();
  }
}
