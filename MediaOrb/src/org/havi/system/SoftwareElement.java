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
 * $Id: SoftwareElement.java,v 1.3 2005/03/02 21:06:58 stephen Exp $
 */

package org.havi.system;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstMsgErrorCode;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.HaviMsgNotReadyException;
import org.havi.system.types.HaviMsgRemoteApiException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.system.ms.MessagingSystem;
import com.redrocketcomputing.havi.system.rmi.HaviRmiHeader;
import com.redrocketcomputing.util.concurrent.BoundedBuffer;
import com.redrocketcomputing.util.concurrent.InterruptableChannel;
import com.redrocketcomputing.util.concurrent.Latch;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author Stephen Street
 */
public class SoftwareElement extends AbstractTask implements MsgCallback
{
  private class DispatchEntry extends HashSet
  {
    private int protocolType;
    private SEID sourceId;
    private SEID destinationId;
    private Status state;
    private byte[] buffer;

    public DispatchEntry(int protocolType, SEID sourceId, SEID destinationId, Status state, byte buffer[])
    {
      // Save parameters
      this.protocolType = protocolType;
      this.sourceId = sourceId;
      this.destinationId = destinationId;
      this.state = state;
      this.buffer = buffer;
    }

    public void dispatch()
    {
      // Allocate input stream
      HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(buffer);

      // Dispatch to all listener and track the handled flag
      boolean handled = false;
      HaviListener element = null;
      for (Iterator iterator = iterator(); iterator.hasNext();)
      {
        // Extract element
        element = (HaviListener) iterator.next();

        // Do dispatch
        if (element.receiveMsg(handled, (byte) protocolType, sourceId, destinationId, state, hbais))
        {
          handled = true;
        }

        // Reset the input stream
        hbais.reset();
      }

      // Logger warning about unhandled message
      if (!handled)
      {
        // Check for RMI
        String headerString = "";
        if (protocolType == ConstProtocolType.HAVI_RMI)
        {
          try
          {
            // Try to unmarshall the header
            HaviRmiHeader header = new HaviRmiHeader(hbais);
            headerString = header.toString();
          }
          catch (HaviUnmarshallingException e)
          {
            // Ignore
          }
        }
        
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "dispatch", localSeid + ": unhandled message from se " + sourceId + " " + headerString);
      }
    }
  }

  private final static int DEFAULT_QUEUE_SIZE = 30;
  private final static byte[] EMPTY_BYTE_ARRAY = new byte[0];
  private final static Status OK = new Status(ConstApiCode.ANY, ConstGeneralErrorCode.SUCCESS);
  private final static Status TARGET_REJECT = new Status(ConstApiCode.MSG, ConstMsgErrorCode.TARGET_REJECT);
  private final static Status SYSTEM_OVERFLOW = new Status(ConstApiCode.MSG, ConstMsgErrorCode.OVERFLOW);

  private ComponentConfiguration configuration;
  private SEID localSeid;
  private MessagingSystem messagingSystem;
  private InterruptableChannel dispatchQueue;
  private Map listenerMap = new HashMap();
  private volatile AbstractTask task = null;
  private Latch closeLatch = new Latch();
  private boolean debug = false;

  /**
   * Default Constructort. Connects to the MessageSystem and start a dispatch queue for handling message
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public SoftwareElement() throws HaviMsgException
  {
    try
    {
      // Get configuration
      configuration = ConfigurationProperties.getInstance().getComponentConfiguration("software.element");

      // Get dispatch queue size
      int dispatchQueueSize = configuration.getIntProperty("queue.size", DEFAULT_QUEUE_SIZE);

      // Create dispatch queue
      dispatchQueue = new InterruptableChannel(new BoundedBuffer(dispatchQueueSize));

      // Get the messaging system
      messagingSystem = (MessagingSystem)ServiceManager.getInstance().find(MessagingSystem.class);
      if (messagingSystem == null)
      {
        // Very bad
        throw new IllegalStateException("can not find MessagingSystemService");
      }

      // Try open seid
      boolean retrying = false;
      for (int i = 0; i < 10; i++)
      {
        try
        {
          // Check for retry
          if (retrying)
          {
            Thread.sleep(1000);
          }

          // Ask messaging system for a seid
          localSeid = messagingSystem.open(this);

          // Open all done
          break;
        }
        catch (HaviMsgNotReadyException ex)
        {
          LoggerSingleton.logWarning(this.getClass(), "SoftwareElement", "messaging system not ready");
          retrying = true;
        }
        catch (InterruptedException ex)
        {
          // Very bad
          throw new IllegalStateException("interrupted while waiting for MessagingSystem to start");
        }
      }

      // Check for open
      if (localSeid == null)
      {
        throw new HaviMsgNotReadyException();
      }

      // Retrieve the task pool service
      TaskPool taskPool = (TaskPool) ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Ummm
        throw new IllegalStateException("can not find task pool");
      }

      // Start the dispatcher and save the task reference
      task = this;
      taskPool.execute(task);
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new HaviMsgNotReadyException("task pool aborted");
    }
  }

  /**
   * Construct. Connects to the MessageSystem, add the specified listener to the element
   * and starts a dispatch queue for handling message
   * @param haviListener
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public SoftwareElement(HaviListener haviListener) throws  HaviMsgException
  {
    // Forward
    this();

    // Create lister set
    Set noSeidSet = new HashSet();

    // Add listener
    noSeidSet.add(haviListener);

    // Add listener using empty seid
    listenerMap.put(SEID.ZERO, noSeidSet);
  }

  /**
   * Constructor for system software elements
   * @param type
   * @throws HaviMsgNotReadyException
   * @throws HaviMsgException
   */
  protected SoftwareElement(int type) throws HaviMsgNotReadyException, HaviMsgException
  {
    try
    {
      // Get configuration
      configuration = ConfigurationProperties.getInstance().getComponentConfiguration("system.software.element");

      // Get dispatch queue size
      int dispatchQueueSize = configuration.getIntProperty("queue.size", 20);

      // Create dispatch queue
      dispatchQueue = new InterruptableChannel(new BoundedBuffer(dispatchQueueSize));

      // Get the messaging system
      messagingSystem = (MessagingSystem)ServiceManager.getInstance().find(MessagingSystem.class);
      if (messagingSystem == null)
      {
        // Very bad
        throw new IllegalStateException("can not find MessagingSystemService");
      }

      // Ask messaging system for a seid
      localSeid = messagingSystem.sysOpen(this, type);

      // Retrieve the task pool service
      TaskPool taskPool = (TaskPool) ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Ummm
        throw new IllegalStateException("can not find task pool");
      }

      // Start the dispatcher and save the task reference
      task = this;
      taskPool.execute(task);
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new HaviMsgNotReadyException("task pool aborted");
    }
  }

  /**
   * Constructor for system software elements.
   * @param type
   * @param haviListener
   * @throws HaviMsgNotReadyException
   * @throws HaviMsgException
   */
  protected SoftwareElement(int type, HaviListener haviListener) throws HaviMsgNotReadyException, HaviMsgException
  {
    // Forward
    this(type);

    // Create lister set
    Set noSeidSet = new HashSet();

    // Add listener
    noSeidSet.add(haviListener);

    // Add listener using empty seid
    listenerMap.put(SEID.ZERO, noSeidSet);
  }

  /**
   * Add a listener to the software element dispatch queue on all traget SEIDs
   * @param haviListener
   * @throws HaviGeneralException
   * @throws HaviMsgListenerExistsException
   */
  public final void addHaviListener(HaviListener haviListener) throws HaviMsgListenerExistsException
  {
  	// Check for open
  	ensureOpen();

    synchronized (listenerMap)
    {
      // Get the no seid set and create if it does not exist
      Set noSeidSet = (Set) listenerMap.get(SEID.ZERO);
      if (noSeidSet == null)
      {
        // Create one
        noSeidSet = new HashSet();
  
        // Add to the map
        listenerMap.put(SEID.ZERO, noSeidSet);
      }
  
      // Check to see if a listern is already registered
      if (noSeidSet.contains(haviListener))
      {
        throw new HaviMsgListenerExistsException(haviListener.toString());
      }
  
      // Add to the set
      noSeidSet.add(haviListener);
    }
  }

  /**
   * Add a listener to the software element dispatch queue for the specified target SEID
   * @param haviListener
   * @param targetSeid
   * @throws HaviGeneralException
   * @throws HaviMsgListenerExistsException
   */
  public final void addHaviListener(HaviListener haviListener, SEID targetSeid) throws HaviMsgListenerExistsException
  {
  	// Check for open
  	ensureOpen();

    synchronized (listenerMap)
    {
      // Get the no seid set and create if it does not exist
      Set seidSet = (Set) listenerMap.get(targetSeid);
      if (seidSet == null)
      {
        // Create one
        seidSet = new HashSet();
  
        // Add to the map
        listenerMap.put(targetSeid, seidSet);
      }
  
      // Check to see if a listern is already registered
      if (seidSet.contains(haviListener))
      {
        throw new HaviMsgListenerExistsException(haviListener.toString());
      }
  
      // Add to the set
      seidSet.add(haviListener);
    }
  }

  /**
   * Remote the specified listener from all target SEID
   * @param haviListener
   * @throws HaviGeneralException
   * @throws HaviMsgListenerNotFoundException
   */
  public final void removeHaviListener(HaviListener haviListener) throws HaviMsgListenerNotFoundException
  {
  	// Check for open
  	ensureOpen();

    synchronized (listenerMap)
    {
      // What a pain, loop throught the entire
      boolean found = false;
      for (Iterator iterator = listenerMap.values().iterator(); iterator.hasNext();)
      {
        // Extract the set
        Set element = (Set) iterator.next();
  
        // Check to see if the listener is in the set
        if (element.remove(haviListener))
        {
          found = true;
        }
      }
  
      // Throw exception if not found
      if (!found)
      {
        throw new HaviMsgListenerNotFoundException(haviListener.toString());
      }
    }
  }

  /**
   * Terminate the dispatch queue and release all resources
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public final void close() throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    synchronized (listenerMap)
    {
    	// Check local seid
    	if (localSeid == null)
    	{
    		throw new IllegalStateException("localSeid is null");
    	}
  
  		// Clear the task
  		task = null;
  
      // Interrupt the queue, the thread will flush the queue
      dispatchQueue.interrupt();
  
      try
      {
  	    // Wait for close latch to set
        closeLatch.acquire();
      }
      catch (InterruptedException e)
      {
      	// Log warning
      	LoggerSingleton.logWarning(this.getClass(), "close", e.toString() + " while waiting for dispatch task to complete");
      }
  
      // Flush listener map
      listenerMap.clear();
  
      // Release everything
      dispatchQueue = null;
      listenerMap = null;
      localSeid = null;
      configuration = null;
      closeLatch = null;
    }
  }

  /**
   * Get the local SEID for the software element
   * @return SEID
   */
  public final SEID getSeid()
  {
    return localSeid;
  }

  public final SEID getSystemSeid(SEID seid, int softwareElementType) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    return messagingSystem.getSystemSeid(seid, softwareElementType);
  }

  /**
   * Return the SEID of a local system software element using this software elements SEID.
   * @param softwareElementType The type of system software element to get
   * @return SEID The SEID of the requested software element
   * @throws HaviMsgException Thrown if the software element type is wrong, or there is some other problem
   * with the messaging system
   */
  public final SEID getSystemSeid(int softwareElementType) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

  	return getSystemSeid(localSeid, softwareElementType);
  }

  public final boolean msgIdTrusted(SEID seid) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    return messagingSystem.isTrusted(seid);
  }

  /**
   * Send message reliably
   * @param protocol
   * @param destinationSeid
   * @param buffer
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public final void msgSendReliable(byte protocol, SEID destinationSeid, byte[] buffer) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    messagingSystem.sendReliable(protocol, localSeid, destinationSeid, buffer);
  }

  /**
   * Send request message
   * @param destinationSeid
   * @param operationCode
   * @param buffer
   * @return int
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public final int msgSendRequest(SEID destinationSeid, OperationCode operationCode, byte[] buffer) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    return messagingSystem.sendRequest(localSeid, destinationSeid, operationCode, buffer);
  }

  /**
   * Invoke a remote method
   * @param destinationSeid
   * @param operationCode
   * @param timeout
   * @param buffer
   * @return byte[]
   * @throws HaviGeneralException
   * @throws HaviMsgRemoteApiException
   * @throws HaviMsgException
   */

  public final byte[] msgSendRequestSync(SEID destinationSeid, OperationCode operationCode, int timeout, byte[] buffer) throws HaviMsgRemoteApiException, HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    return messagingSystem.sendRequestSync(localSeid, destinationSeid, operationCode, timeout, buffer);
  }

  /**
   * Send a message response
   * @param destinationSeid
   * @param operationCode
   * @param transferMode
   * @param returnCode
   * @param buffer
   * @param transactionId
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public final void msgSendResponse(SEID destinationSeid, OperationCode operationCode, int transferMode, Status returnCode, byte[] buffer, int transactionId) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    messagingSystem.sendResponse(localSeid, destinationSeid, operationCode, transferMode, returnCode, buffer, transactionId);
  }

  /**
   * Send a simple message
   * @param protocol
   * @param destinationSeidList
   * @param buffer
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public final void msgSendSimple(byte protocol, SEID[] destinationSeidList, byte[] buffer) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    messagingSystem.sendSimple(protocol, localSeid, destinationSeidList, buffer);
  }

  /**
   * Remove a SEID watcher
   * @param destinationSeid
   * @param opCode
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public final void msgWatchOff(SEID destinationSeid, OperationCode opCode) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    messagingSystem.watchOff(localSeid, destinationSeid, opCode);
  }

  /**
   * Add a SEID watcher
   * @param destinationSeid
   * @param opCode
   * @throws HaviGeneralException
   * @throws HaviMsgException
   */
  public final void msgWatchOn(SEID destinationSeid, OperationCode opCode) throws HaviMsgException
  {
  	// Check for open
  	ensureOpen();

    // Forward
    messagingSystem.watchOn(localSeid, destinationSeid, opCode);
  }

  /**
   * Turn on/off debug for this software element
   * @param debug True to turn on debugging, false otherwise
   */
  public final void setDebug(boolean debug)
  {
    this.debug = debug;
  }

  /**
   * Get the debug state of the software element
   * @return True is debugging is on, false otherwise
   */
  public final boolean isDebug()
  {
    return debug;
  }

  /**
   * @see org.havi.system.MsgCallback#callback(int, SEID, SEID, Status, byte[])
   */
  public Status callback(int protocolType, SEID sourceId, SEID destId, Status state, byte buffer[])
  {
  	// If the software element is not open, silently drop the message
  	if (task == null)
  	{
  		// Log warning and drop
  		LoggerSingleton.logWarning(this.getClass(), "callback", "not open, dropping message " + sourceId + "->" + destId);

  		// Drop
  		return OK;
  	}

    try
    {
      // Build base dispatch set
      DispatchEntry entry = new DispatchEntry(protocolType, sourceId, destId, state, buffer);

      synchronized(listenerMap)
      {
        // Get all default listeners
        Set defaultListeners = (Set)listenerMap.get(SEID.ZERO);
        if (defaultListeners != null)
        {
          // Add all default listeners
          entry.addAll((Set) listenerMap.get(SEID.ZERO));
        }
  
        // Add target specific listeners
        Set targetListeners = (Set) listenerMap.get(sourceId);
        if (targetListeners != null)
        {
          // Add targeted listeners
          entry.addAll(targetListeners);
        }
      }

      // Check size
      if (entry.size() == 0)
      {
        // Build log information
        String info = " No listeners. Target rejected";
        if (protocolType == ConstProtocolType.HAVI_RMI)
        {
          try
          {
            // Try to unmarshall the header
            HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(buffer);
            HaviRmiHeader header = new HaviRmiHeader(hbais);
            
            info = info + ": "  + header;
          }
          catch (HaviUnmarshallingException e1)
          {
            // Umm, just use source id
            info = info + ": " + sourceId;
          }
        }
        else
        {
          // Just for SEID
          info = info + ": " + sourceId;
        }
        
        // Umm, no listeners, reject
        LoggerSingleton.logError(this.getClass(), "CallBack", localSeid.toString() + info);
        return TARGET_REJECT;
      }

      // Try to add to queue
      if (!dispatchQueue.offer(entry, 0))
      {
        // Opp all full up
        LoggerSingleton.logError(this.getClass(), "CallBack", localSeid.toString() + " Dispatch queue overflowed with message from " + sourceId);
        return SYSTEM_OVERFLOW;
      }

      // All good
      return OK;
    }
    catch (InterruptedException e)
    {
      // Opp, let return target reject
      LoggerSingleton.logError(this.getClass(), "CallBack", localSeid.toString() + " Interrupted.  Target rejected " + sourceId);
      return TARGET_REJECT;
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return localSeid.toString();
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // Loop until interrupted or escaping exception detected
    while (task != null)
    {
      try
      {
        // Wait for dispatch entry
        DispatchEntry entry = (DispatchEntry) dispatchQueue.take();
        
        synchronized(this)
        {
          // Dispatch the entry
          entry.dispatch();
        }
      }
      // Catch everyting
      catch (Exception e)
      {
        // Check for interrupted exception
        if (e instanceof InterruptedException)
        {
          // Clear thread interruption
          Thread.currentThread().interrupted();
        }
        else
        {
          // Log exception error
          e.printStackTrace();
          LoggerSingleton.logError(this.getClass(), "run", localSeid.toString() + " detected " + e.toString());
        }

        // Always exist the loop
        task = null;
      }
    }

    try
    {
      // Flush the queue
      while (dispatchQueue.poll(0) != null);

      // Close the SEID
      messagingSystem.close(localSeid);
    }
    catch (InterruptedException e)
    {
      LoggerSingleton.logInfo(this.getClass(), "run", "interrupted while flushing queue");
    }
    catch (HaviMsgException e)
    {
      LoggerSingleton.logError(this.getClass(), "run", e.toString() + " detected while closing");
    }

    // Alway set the close latch
    closeLatch.release();
  }

  /**
   * Throw a not ready exception if the software element is not opened
   * @throws HaviMsgNotReadyException Thrown if not open
   */
  private final void ensureOpen() throws IllegalStateException
  {
  	// Check task to verify that the software element is open
  	if (task == null)
  	{
  		// Not open
  		throw new IllegalStateException("not opened");
  	}
  }
}
