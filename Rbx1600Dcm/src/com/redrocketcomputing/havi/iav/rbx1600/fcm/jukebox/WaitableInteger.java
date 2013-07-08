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
 * $Id: WaitableInteger.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import com.redrocketcomputing.util.concurrent.SynchronizedInt;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class WaitableInteger extends SynchronizedInt
{
  /**
   * Make a new WaitableInt with the given initial value, and using its own internal lock.
   */
  public WaitableInteger(int initialValue)
  {
    // Initialize super class
    super(initialValue);
  }

  /**
   * Make a new WaitableInt with the given initial value, and using the supplied lock.
   */
  public WaitableInteger(int initialValue, Object lock)
  {
    // Initialize super class
    super(initialValue, lock);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.util.concurrent.SynchronizedInt#set(int)
   */
  public int set(int newValue)
  {
    synchronized (lock_)
    {
      lock_.notifyAll();
      return super.set(newValue);
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.util.concurrent.SynchronizedInt#commit(int, int)
   */
  public boolean commit(int assumedValue, int newValue)
  {
    synchronized (lock_)
    {
      boolean success = super.commit(assumedValue, newValue);
      if (success)
      {
        lock_.notifyAll();
      }
      
      return success;
    }
  }

  /**
   * Wait for the value to match
   * @param c The value to match
   * @throws InterruptedException
   */
  public void waitEqual(int c) throws InterruptedException
  {
    synchronized (lock_)
    {
      while (!(value_ == c))
      {
        lock_.wait();
      }
    }
  }

  /**
   * Wait specified number of milliseconds for the values to match
   * @param c The value to match
   * @param timeout The time to wait in milliseconds
   * @return True is matched, false otherwise
   * @throws InterruptedException
   */
  public boolean waitEqual(int c, long timeout) throws InterruptedException
  {
    synchronized (lock_)
    {
      if (timeout <= 0)
      {
        return value_ == c;
      }

      if (value_ == c)
      {
        return true;
      }

      long waitTime = timeout;
      long start = System.currentTimeMillis();

      while (value_ != c)
      {
        lock_.wait(waitTime);
        waitTime = timeout - (System.currentTimeMillis() - start);
        if (waitTime <= 0)
        {
          return value_ == c;
        }
      }

      return true;
    }
  }
}