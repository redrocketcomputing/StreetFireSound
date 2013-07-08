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
 * $Id: ShellTask.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.remoteshell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import com.redrocketcomputing.appframework.shell.Shell;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.util.concurrent.SynchronizedInt;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
class ShellTask extends AbstractTask
{
  private static SynchronizedInt nextId = new SynchronizedInt(0);

  private RemoteShellService parent;
  private volatile Socket socket;

  private String loginName = "";
  private int shellId = -1;
  private long startTime = 0;
  private Shell shell = null;

  /**
   * Constructor for ShellTask.
   */
  public ShellTask(RemoteShellService parent, Socket socket)
  {
    // Save the parameters
    this.parent = parent;
    this.socket = socket;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    try
    {
      // Mark the start time
      startTime = System.currentTimeMillis();

      // Mark socket to keep alive
      socket.setKeepAlive(true);

      // Create shell id
      shellId = nextId.increment();

      // Create the streams
      InputStream in = socket.getInputStream();
      PrintStream out = new PrintStream(socket.getOutputStream());
      PrintStream err = new PrintStream(socket.getOutputStream());

      // Create buffered login streams
      InputStreamReader input = new InputStreamReader(socket.getInputStream());
      BufferedReader bufferedInput = new BufferedReader(input);

      // Log in the user
      while (loginName == null || loginName.equals(""))
      {
        // Display prompt
        out.print("login: ");
        out.flush();

        // Read the a line
        loginName = bufferedInput.readLine();
      }

      // Create the shell
      shell = new Shell(in, out, err, parent.createRemoteShellProperties());

      // Run the shell
      shell.run(new String[0]);
    }
    catch (IOException e)
    {
      // Check to see if the socket was close
      if (socket != null)
      {
        // Must be somekind of error
        LoggerSingleton.logError(this.getClass(), "run", "detected IOException while running the shell: " + e.getMessage());
      }
    }
    finally
    {
      // Ask parent to terminate the shell
      parent.terminateShellTask(this);

      // Close the socket
      close();
    }
  }

  /**
   * Close the shell connection and the underlaying socket
   */
  public synchronized void close()
  {
    // Ignore is already closed
    if (socket == null)
    {
      return;
    }

    try
    {
      // Save the socket and close it to terminate the thread
      Socket temp = socket;
      socket = null;
      temp.close();
    }
    catch (IOException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "close", "IOException: " + e.getMessage());
    }
  }
  /**
   * Returns the loginName.
   * @return String
   */
  public String getLoginName()
  {
    return loginName;
  }

  /**
   * Returns the shellId.
   * @return int
   */
  public int getShellId()
  {
    return shellId;
  }

  /**
   * Returns the startTime.
   * @return long
   */
  public long getStartTime()
  {
    return startTime;
  }

  /**
   * Returns the socket.
   * @return Socket
   */
  public Socket getSocket()
  {
    return socket;
  }

  /**
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return parent.getInstanceName() + "::ShellTask";
  }

}
