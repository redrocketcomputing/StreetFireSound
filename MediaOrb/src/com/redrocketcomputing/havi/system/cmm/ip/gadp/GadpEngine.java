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
 * $Id: GadpEngine.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.havi.system.types.GUID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.log.LoggerSingleton;



/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class GadpEngine extends AbstractTask
{
  final static GadpState IDLE = new GadpIdleState();
  final static GadpState RESERVING = new GadpReservingState();
  final static GadpState READY = new GadpReadyState();

  private final static int MAX_PACKET_SIZE = 64;

  private GadpState currentState = IDLE;
  private GUID localGuid = GUID.ZERO;
  private GadpMessageSocket socket;
  private int timeStamp;
  private int portBase;
  private int portSpaceSize;
  private EventDispatch eventDispatcher;
  private int reserveTimeout;
  private int currentTimeout;

  /**
   * Constructor for GadpEngine.
   * @param multicastAddress
   * @param multicastPort
   * @param portBase
   * @param portSpaceSize
   * @param reserveTimeout
   * @throws GadpException
   */
  public GadpEngine(String multicastAddress, int multicastPort, int portBase, int portSpaceSize, int reserveTimeout) throws GadpException
  {
    // Construct the super class
    super();

    try
    {
      // Save the parameters
      this.portBase = portBase;
      this.portSpaceSize = portSpaceSize;
      this.reserveTimeout = reserveTimeout;
      this.currentTimeout = reserveTimeout;

      // Bind this as context to the GadpState
      GadpState.setContext(this);

      // Get the event dispatcher service
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Throw exception
        throw new GadpSystemException("can not find event dispatch service");
      }

      // Open the socket
      socket = new GadpMessageSocket(multicastAddress, multicastPort, MAX_PACKET_SIZE);

      // Create the random stamp
      timeStamp = SecureRandom.getInstance("SHA1PRNG").nextInt();

      // Change state to reserving
      changeState(RESERVING);

      // Find the task pool service
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Throw exception
        throw new GadpSystemException("can not find task pool service");
      }

      // Execute this as a task
      taskPool.execute(this);
    }
    catch (NoSuchAlgorithmException e)
    {
      // Translate
      throw new GadpSystemException(e.toString());
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new GadpSystemException(e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "GADP";
  }

  /**
   * Method getLocalGuid.
   * @return GUID
   */
  public GUID getLocalGuid()
  {
    return localGuid;
  }

  /**
   * Method close.
   */
  public void close()
  {
    // Close the socket
    socket.close();

    // Release the socket
    socket = null;
  }

  /**
   * Method changeState.
   * @param newState
   */
  final synchronized void changeState(GadpState newState)
  {
    // Leave current state if current is available
    if (currentState != null)
    {
      currentState.leaveState();
    }

    // Save the new state
    currentState = newState;

    // Enter the new state
    currentState.enterState();
  }

  /**
   * Method sendGadpMessage.
   * @param message
   * @throws GadpException
   */
  final void sendGadpMessage(GadpMessage message) throws GadpException
  {
    // Send the message
    socket.send(message);
  }

  /**
   * Method getNextCanidateGuid.
   * @return GUID
   * @throws GadpSystemException
   */
  final GUID getNextCanidateGuid() throws GadpSystemException
  {
    try
    {
      // Create raw GUID
      byte[] rawGuid = new byte[GUID.SIZE];

      // Copy IP address into the raw guid
      byte[] localIpAddress = InetAddress.getLocalHost().getAddress();
      System.arraycopy(localIpAddress, 0, rawGuid, 0, localIpAddress.length);

      // Add current port base
      rawGuid[4] = (byte)((portBase >> 24) & 0xff);
      rawGuid[5] = (byte)((portBase >> 16) & 0xff);
      rawGuid[6] = (byte)((portBase >> 8) & 0xff);
      rawGuid[7] = (byte)(portBase & 0xff);

      // Update the base port
      portBase = portBase + portSpaceSize;

      // Return the new canidate guid
      return new GUID(rawGuid);
    }
    catch (UnknownHostException e)
    {
      // Translate
      throw new GadpSystemException(e.toString());
    }
  }

  /**
   * Method setLocalGuid.
   * @param guid
   */
  final void setLocalGuid(GUID guid)
  {

    // Save the local guid
    this.localGuid = guid;
  }

  /**
   * Method getLocalTimeStamp.
   * @return int
   */
  final int getLocalTimeStamp()
  {
    return timeStamp;
  }

  /**
   * Method fireReadyEvent.
   */
  final void fireReadyEvent()
  {
    // Fire the ready event
    eventDispatcher.dispatch(new GadpReadyEvent(localGuid));
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Loop until an error or the socket is null
      while (socket != null)
      {
        // Read from socket
        GadpMessage message = socket.receive(currentTimeout);

        LoggerSingleton.logDebugCoarse(this.getClass(), "run", message.toString());

        // Dispatch the message
        message.dispatch(currentState);
      }
    }
    catch (GadpIOException e)
    {
      // Change state to IDLE
      changeState(IDLE);
    }
    catch (GadpException e)
    {
      // Log an error
      LoggerSingleton.logError(this.getClass(), "run", "exiting due to " + e.toString());
    }
  }

  final void enableTimeout(boolean enable)
  {
    // Check for enable
    if (enable)
    {
      // Use the reserve timeout
      currentTimeout = reserveTimeout;
    }
    else
    {
      // No timeout
      currentTimeout = 0;
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return currentState.toString();
  }
}
