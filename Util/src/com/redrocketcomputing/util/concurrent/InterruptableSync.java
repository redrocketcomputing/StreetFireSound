/**
 * FileName: InterruptableSync.java
 * 
 * Package:  com.redrocketcomputing.util.concurrent
 * 
 * Created on Sep 30, 2003
 *
 * Copyright @ 2003 StreetFire Sound Labs
 */
package com.redrocketcomputing.util.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Stephen
 *
 */
public class InterruptableSync implements Sync
{
  protected Sync sync;
  protected List threads = Collections.synchronizedList(new ArrayList());
  
  /**
   * Constructor for InterruptableSync.
   */
  public InterruptableSync(Sync sync)
  {
    // Save the sync
    this.sync = sync;
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Sync#acquire()
   */
  public void acquire() throws InterruptedException
  {
    // Get the current thread
    Thread currentThread = Thread.currentThread();
    
    // Check for interruption
    if (currentThread.interrupted())
    {
      throw new InterruptedException();
    }
    
    try
    {
      // Add thread to list
      threads.add(currentThread);
      
      // Aquire sync
      sync.acquire();
    }
    finally
    {
      // remove thread
      threads.remove(currentThread);
    }    
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Sync#attempt(long)
   */
  public boolean attempt(long msecs) throws InterruptedException
  {
    // Get the current thread
    Thread currentThread = Thread.currentThread();
    
    // Check for interruption
    if (currentThread.interrupted())
    {
      throw new InterruptedException();
    }
    
    
    try
    {
      // Add thread to list
      threads.add(currentThread);
      
      // Aquire sync
      return sync.attempt(msecs);
    }
    finally
    {
      // remove thread
      threads.remove(currentThread);
    }    
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Sync#release()
   */
  public void release()
  {
    // Forward to the sync
    sync.release();
  }

  /**
   * Returns the sync.
   * @return Sync
   */
  public Sync getSync()
  {
    return sync;
  }
  
  /**
   * Interrupt all threads waiting at the sync
   */
  public void interrupt()
  {
    // Extract thread array
    Thread[] array = (Thread[])threads.toArray(new Thread[threads.size()]);
    
    // Loop through and interrupt
    for (int i = 0; i < array.length && array[i] != null; i++)
    {
      array[i].interrupt();
    }
  }
}
