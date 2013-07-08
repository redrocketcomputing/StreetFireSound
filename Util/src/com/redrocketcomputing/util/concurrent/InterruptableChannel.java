/**
 * FileName: InterruptableChannel.java
 * 
 * Package:  com.redrocketcomputing.util.concurrent
 * 
 * Created on Oct 2, 2003
 *
 * Copyright @ 2003 StreetFire Sound Labs
 */
package com.redrocketcomputing.util.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An Channel decorator which allows waiting threads to be interrupted.
 * 
 * @author Stephen
 *
 */
public class InterruptableChannel implements Channel
{
  private Channel channel;
  protected List threads = Collections.synchronizedList(new ArrayList());
  
  /**
   * Constructor for InterruptableChannel.
   * @param channel The underlaying channel to wrap
   */
  public InterruptableChannel(Channel channel)
  {
    // Save the channel
    this.channel = channel;
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Puttable#offer(Object, long)
   */
  public boolean offer(Object item, long msecs) throws InterruptedException
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

      // Foward to the channel
      return channel.offer(item, msecs);
    }
    finally
    {
      // remove thread
      threads.remove(currentThread);
    }    
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Channel#peek()
   */
  public Object peek()
  {
    // Get the current thread
    Thread currentThread = Thread.currentThread();
    
    try
    {
      // Add thread to list
      threads.add(currentThread);
      
      // Foward to the channel
      return channel.peek();
    }
    finally
    {
      // remove thread
      threads.remove(currentThread);
    }    
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Takable#poll(long)
   */
  public Object poll(long msecs) throws InterruptedException
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
      
      // Foward to the channel
      return channel.poll(msecs);
    }
    finally
    {
      // remove thread
      threads.remove(currentThread);
    }    
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Puttable#put(Object)
   */
  public void put(Object item) throws InterruptedException
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
      
      // Foward to the channel
      channel.put(item);
    }
    finally
    {
      // remove thread
      threads.remove(currentThread);
    }    
  }

  /**
   * @see com.redrocketcomputing.util.concurrent.Takable#take()
   */
  public Object take() throws InterruptedException
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
      
      // Foward to the channel
      return channel.take();
    }
    finally
    {
      // remove thread
      threads.remove(currentThread);
    }    
  }
  
  /**
   * Interrupt all threads waiting on the channel
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
