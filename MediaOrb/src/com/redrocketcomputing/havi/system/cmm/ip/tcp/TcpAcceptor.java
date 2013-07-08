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
 * $Id: TcpAcceptor.java,v 1.2 2005/03/17 02:27:32 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.tcp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviCmmIpConfigurationException;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviCmmIpIoException;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.constants.ConstCmmIpIndications;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.system.cmm.ip.IndicationEvent;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpEngine;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpEntry;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * TCP Acceptor task to listen for new connections from remote devices. The task runs until the server socket
 * is closed. Every new connection is added to the specified read end point map and is associated with a
 * GUID received on the new connection.
 *
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class TcpAcceptor extends AbstractTask
{
  private TaskPool taskPool;
  private EventDispatch eventDispatcher;
  private GarpEngine garp;
  private ServerSocket serverSocket = null;
  private Map readEndPointMap;
  private int queueSize;

  /**
   * Constructor for TcpAcceptor.
   */
  public TcpAcceptor(GUID localGuid, GarpEngine garp, int queueSize, Map readEndPointMap) throws HaviCmmIpException
  {
    try
    {
      // Check parameters
      if (localGuid == null || garp == null | readEndPointMap == null)
      {
        // Program screw up
        throw new IllegalArgumentException();
      }


      // Save parameters
      this.garp = garp;
      this.readEndPointMap = readEndPointMap;
      this.queueSize = queueSize;

      // Try to get the event dispatch service
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Opps very bad
        throw new HaviCmmIpConfigurationException("can not find task pool service");
      }

      // Try to get the task pool
      taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Opps very bad
        throw new HaviCmmIpConfigurationException("can not find task pool service");
      }

      // Get the local guid entry
      GarpEntry localGarpEntry = garp.resolve(localGuid);

      // Open the server socket
      serverSocket = new ServerSocket(localGarpEntry.getPort() + ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviCmmIpIoException(e.getMessage());
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "Cmm::TcpAcceptor";
  }

  /**
   * Accept a connection from a remote device and create a new read end point
   * @return TcpReadEndPoint The new read end point
   * @throws HaviCmmIpException Thrown is there is a problem connecting with the remote device
   */
  private TcpReadEndPoint accept() throws HaviCmmIpException
  {
    // Local variable for exceptional close of the socket
    Socket socket = null;

    try
    {
      // Wait for a new connection and check for timeout
      socket = serverSocket.accept();

      // Create the remote end point
      return new TcpReadEndPoint(socket, queueSize, garp);
    }
    catch (InterruptedIOException e)
    {
      // Timeout, return null
      return null;
    }
    catch (IOException e)
    {
      // Close the socket
      exceptionSocketClose(socket);

      // Translate
      throw new HaviCmmIpIoException(e.getMessage());
    }
  }

  /**
   * TCP Acceptor task body.  The task accepts connections from the remote devices listed in accept array and
   * adds them to the read end point map.  Existing end points are flushed from the read end point map.
   * Connection attempts from GUIDs not in the accept array cause a GARP network reset.
   */
  public void run()
  {
    try
    {
      while (true)
      {
        // Try to accept a connection
        TcpReadEndPoint readEndPoint = accept();
        
        synchronized(readEndPointMap)
        {
          // Remote existing end point
          TcpReadEndPoint existingReadEndPoint = (TcpReadEndPoint)readEndPointMap.remove(readEndPoint.getRemoteGuid());

          // Add the new end point
          readEndPointMap.put(readEndPoint.getRemoteGuid(), readEndPoint);

          // Launch the end point task
          taskPool.execute(readEndPoint);

          // Check for existing end points
          if (existingReadEndPoint != null)
          {
            // Log warning
            LoggerSingleton.logWarning(this.getClass(), "run", "removing existing " + existingReadEndPoint.getRemoteGuid());
  
            // Close the end point
            existingReadEndPoint.close();
          }
        }

        // Dispatch an indication event
        eventDispatcher.dispatch(new IndicationEvent(readEndPoint.getRemoteGuid(), ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS, ConstCmmIpIndications.READ_CONNECTED, null));
      }
    }
    catch (TaskAbortedException e)
    {
      LoggerSingleton.logError(this.getClass(), "run", e.toString());
    }
    catch (HaviCmmIpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "run", e.toString());
    }
    finally
    {
      // Close the server socket
      closeServerSocket();
    }
  }

  /**
   * Close the server socket and eat any exceptions
   */
  private void closeServerSocket()
  {
    try
    {
      // Check to see if the socket is open
      if (serverSocket != null)
      {
        serverSocket.close();
      }
    }
    catch (IOException e)
    {
    }
  }

  /**
   * Close the socket and eat any exceptions
   * @param socket The socket to close
   */
  private void exceptionSocketClose(Socket socket)
  {
    try
    {
      // Check for null socket
      if (socket != null)
      {
        // Ok, close it up
        socket.close();
      }
    }
    catch (IOException e)
    {
      // Eat the exception
    }
  }

}
