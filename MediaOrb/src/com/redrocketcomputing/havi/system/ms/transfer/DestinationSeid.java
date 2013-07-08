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
 * $Id: DestinationSeid.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.transfer;

import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.ms.event.SeidTimeoutEvent;

/**
 * @author stephen
 *
 */
public class DestinationSeid
{
  private final static int MAX_ERRORS = 2;

  private SEID seid;
  private volatile int errors = 0;
  private EventDispatch eventDispatcher;
  private volatile boolean eventFired = false;

  /**
   * Constructor for DestinationSeid.
   * @param seid The remote SEID
   */
  public DestinationSeid(SEID seid)
  {
    // Check parameter
    if (seid == null)
    {
      // Bad
      throw new IllegalArgumentException("seid can not null");
    }

    // Get the event dispatcher
    eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
    if (eventDispatcher == null)
    {
      // Badness
      throw new IllegalStateException("can not find event dispatch service");
    }

    // Save the SEID
    this.seid = seid;
  }

  /**
   * Check to see if the seid is reachable as determined by the number of errors sence the last reset or system ready
   * @return boolean True is the SEID is reachable, otherwise false;
   */
  public boolean isReachable()
  {
    return errors < MAX_ERRORS;
  }

  /**
   * Force the SEID to be unreachable
   */
  public void unreachable()
  {
    // Check to see if were are already unreachable
    if (errors < MAX_ERRORS)
    {
      // Force unreachable by set the error count to max
      errors = MAX_ERRORS;
    }
  }

  /**
   * Increment the error count, and fire a SeidTimeoutEvent is too many errors have be detected
   */
  public void error()
  {
    // Update the error count
    errors++;

    // Check to see if we should fire a seid timeout event
    if (errors >= MAX_ERRORS && !eventFired)
    {
      // Yes we should
      eventDispatcher.dispatch(new SeidTimeoutEvent(seid));

      // Mark as fired
      eventFired = true;
    }
  }

  /**
   * Reset the error state
   */
  public void reset()
  {
    // Clear the errors
    errors = 0;
    eventFired = false;
  }

  /**
   * Returns the seid.
   * @return SEID
   */
  public SEID getSeid()
  {
    return seid;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "DestinationSeid: " + seid + " error = " + errors;
  }

}
