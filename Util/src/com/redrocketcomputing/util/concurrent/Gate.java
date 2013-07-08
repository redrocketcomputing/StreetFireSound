/**
 * Created on Jul 15, 2003
 *
 * Copyright @ StreetFireSound Labs, LLC
 */
package com.redrocketcomputing.util.concurrent;

/**
 * A Gate is a Latch which can be reset to stop threads from passing through.
 * @see com.redrocketcomputing.util.concurrent.Latch
 * 
 * @author stephen Jul 15, 2003
 * @version 1.0
 * 
 */
public class Gate extends Latch
{
  /**
   * Prevent threads from passing through the gate
   */
  public synchronized void reset()
  {
    latched_ = false;
  }
}
