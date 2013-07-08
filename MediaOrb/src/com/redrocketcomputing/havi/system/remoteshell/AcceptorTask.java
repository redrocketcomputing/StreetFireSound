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
 * $Id: AcceptorTask.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.remoteshell;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class AcceptorTask extends AbstractTask
{
  private RemoteShellService parent;
  private ServerSocket serverSocket;
  private int connectionCount = 0;
  private int port;

  /**
   * Constructor for AcceptorTask.
   * @param parent The remote shell service
   * @param port The port to listen on
   * @throws IOException Thrown is there is a problem opening the server socket
   */
  public AcceptorTask(RemoteShellService parent, int port) throws IOException
  {
    // Save the parent and port
    this.parent = parent;
    this.port = port;

    // Open the server socket
    serverSocket = new ServerSocket(port);
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // Loop while the server socket is not null
    while (serverSocket != null)
    {
      try
      {
        // Accept a new connection
        Socket socket = serverSocket.accept();

        // Update the connection count
        connectionCount++;

        // Create the new shell task
        ShellTask shellTask = new ShellTask(parent, socket);

        // Ask the parent to launch the new shell
        parent.launchRemoteShell(shellTask) ;
      }
      catch (IOException e)
      {
        // Check to see if the socket was close
        if (serverSocket != null)
        {
          // Must be somekind of error
          LoggerSingleton.logError(this.getClass(), "run", "detected IOException during accept " + e.getMessage());

          // Clear the server socket
          serverSocket = null;
        }
      }
    }
  }

  /**
   * Returns the connectionCount.
   * @return int The number of connections requests handled
   */
  public int getConnectionCount()
  {
    return connectionCount;
  }

  /**
   * Close the underlaying server socket and force the thread to stop
   */
  public void close()
  {
    try
    {
      // Save the socket and close it to terminate the thread
      ServerSocket temp = serverSocket;
      serverSocket = null;
      temp.close();
    }
    catch (IOException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "close", "IOException: " + e.getMessage());
    }
  }
  /**
   * Returns the port.
   * @return int
   */
  public int getPort()
  {
    return port;
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return parent.getInstanceName() + "::AcceptorTask";
  }

}
