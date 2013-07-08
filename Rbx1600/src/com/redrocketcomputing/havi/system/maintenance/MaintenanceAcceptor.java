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
 * $Id: MaintenanceAcceptor.java,v 1.1 2005/02/22 03:47:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MaintenanceAcceptor extends AbstractTask
{
  private List maintenanceFactories = new ArrayList();
  private ServerSocket serverSocket;

  /**
   * Constructs a new MaintenanceAcceptor which listens on the specified serverSocket
   * for maintenance requests.
   * @param port The IP port to listen for requests on
   */
  public MaintenanceAcceptor(int port) throws MaintenanceException
  {
    try
    {
      // Create the server socket
      serverSocket = new ServerSocket(port);

      // Use the first task pool service found
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Can not find required component
        throw new IllegalStateException("task pool service not found");
      }

      // Lauch the acceptor thread
      taskPool.execute(this);
    }
    catch (IOException e)
    {
      // Translate
      throw new MaintenanceIOException(e.toString());
    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new MaintenanceTaskException(e.toString());
    }
  }

  /**
   * Stop the MaintenanceAcceptor task and release all resources
   */
  public void close()
  {
    try
    {
      // Close the server socket to terminate the thread
      ServerSocket temp = serverSocket;
      serverSocket = null;
      temp.close();
    }
    catch (IOException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
  }

  /**
   * Add a new MaintenanceRequestFactory to the acceptor
   * @param factory The factory to add
   */
  public void addRequestFactory(MaintenanceRequestFactory factory)
  {
    synchronized (maintenanceFactories)
    {
      // Add the factory
      maintenanceFactories.add(factory);
    }
  }

  /**
   * Remove a MaintenanceRequestFactory from the acceptor
   * @param factory
   */
  public void removeRequestFactory(MaintenanceRequestFactory factory)
  {
    synchronized (maintenanceFactories)
    {
      // Remove all matching factories
      while (!maintenanceFactories.remove(factory));
    }
  }

  /**
   * Dump the classes of all maintenance factories to the print stream
   * @param ps The PrintStream to dump the factory class names
   */
  public void dumpFactories(PrintStream ps)
  {
    synchronized (maintenanceFactories)
    {
      // Loop through the factories looking for a factory to match the request
      for (Iterator iterator = maintenanceFactories.iterator(); iterator.hasNext();)
      {
        // Extract the element
        MaintenanceRequestFactory element = (MaintenanceRequestFactory)iterator.next();

        // Write class name to print stream
        ps.println(element.getClass().getName());
      }
    }

  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
   */
  public String getTaskName()
  {
    return "MaintenanceAcceptor";
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // Run until close
    while (serverSocket != null)
    {
      try
      {
        // Wait for a connection
        Socket requestSocket = serverSocket.accept();
        requestSocket.setSoLinger(true, 10);

        // Wrapped of data input stream around the serverSocket
        DataInputStream dis = new DataInputStream(requestSocket.getInputStream());
        DataOutputStream dos = new DataOutputStream(requestSocket.getOutputStream());

        try
        {
          // Perform handshake and fetch the request handler
          MaintenanceRequestHandler requestHandler = performHandshake(dis, dos);

          // Execute the request
          requestHandler.executeRequest();

          // Send success response
          MaintenanceCompletionNotification successResponse = new MaintenanceCompletionNotification((byte)MaintenanceConstants.COMPLETION_RESPONSE_SUCCESS);
          successResponse.write(dos);
        }
        catch (MaintenanceException e)
        {
          // send and log the error
          sendErrorResponse(e, dos);
        }
        catch (IOException e)
        {
          // Log the error
          LoggerSingleton.logError(this.getClass(), "run", e.toString() + " while handling maintenance request");
        }
        finally
        {
          // Close the requestSocket
          close(requestSocket);
        }
      }
      catch (IOException e)
      {
        // Log error only if this is not an acceptor close
        if (serverSocket != null)
        {
          // Log the error
          LoggerSingleton.logError(this.getClass(), "run", e.toString());

          // Force an exit from the thread by clearing the server socket
          serverSocket = null;
        }
      }
    }
  }

  /**
   * Perform the handshake and retreive the request handler
   */
  public MaintenanceRequestHandler performHandshake(DataInputStream dis, DataOutputStream dos) throws IOException, MaintenanceHandshakeException
  {
    // Create a new request
    MaintenanceHandshakeRequest handshakeRequest = new MaintenanceHandshakeRequest(dis);

    // Create handler from the request
    MaintenanceRequestHandler requestHandler = createHandler(handshakeRequest, dis, dos);
    if (requestHandler == null)
    {
      throw new MaintenanceHandshakeException((byte)MaintenanceConstants.HANDSHAKE_RESPONSE_FAIL);
    }

    // Send Success handshake response
    MaintenanceHandshakeResponse successResponse = new MaintenanceHandshakeResponse((byte)MaintenanceConstants.HANDSHAKE_RESPONSE_SUCCESS);
    successResponse.write(dos);

    // Return the created handler
    return requestHandler;
  }

  /**
   * Send and log an error response without throwing any exceptions
   */
  private void sendErrorResponse(MaintenanceException exception, DataOutputStream dos)
  {
    // Log the error
    LoggerSingleton.logError(this.getClass(), "run", exception.toString() + " while handling maintenance request");

    try
    {
      if (exception instanceof MaintenanceHandshakeException)
      {
        // Send failed response
        MaintenanceHandshakeResponse failedResponse = new MaintenanceHandshakeResponse((byte)MaintenanceConstants.HANDSHAKE_RESPONSE_FAIL);
        failedResponse.write(dos);
      }
      else if (exception instanceof MaintenanceInvalidFileException)
      {
        // Send failed completion notification
        MaintenanceCompletionNotification failedResponse = new MaintenanceCompletionNotification(((MaintenanceInvalidFileException)exception).getResponseCode());
        failedResponse.write(dos);
      }
      else
      {
        // Just handle the error
        LoggerSingleton.logError(this.getClass(), "sendErrorResponse", exception.toString());
      }

      // Flush response
      dos.flush();
    }
    catch (IOException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "sendErrorResponse", e.toString());
    }
  }

  /**
   * Create a MaintenanceRequestHandler from a MaintenanceHandshakeRequest and a Socket
   * @param request The MaintenanceHandshakeRequest to search the handler factories for
   * @param socket The Socket to be used by the MaintenanceRequestHandler
   * @return The MaintenanceRequestHandler or null if no factory can create the handler
   */
  private MaintenanceRequestHandler createHandler(MaintenanceHandshakeRequest request, DataInputStream inputStream, DataOutputStream outputStream)
  {
    synchronized (maintenanceFactories)
    {
      // Loop through the factories looking for a factory to match the request
      for (Iterator iterator = maintenanceFactories.iterator(); iterator.hasNext();)
      {
        // Extract the element
        MaintenanceRequestFactory element = (MaintenanceRequestFactory)iterator.next();

        // Try to create the handler
        MaintenanceRequestHandler handler = element.create(request, inputStream, outputStream);
        if (handler != null)
        {
          // All done
          return handler;
        }
      }

      // Not found
      return null;
    }
  }

  /**
   * Close a Socket and swallow any errors
   * @param socket The Socket to close
   */
  private final void close(Socket socket)
  {
    // Abandon if null
    if (socket == null)
    {
      return;
    }

    try
    {
      // Try closing
      socket.close();
    }
    catch (IOException e)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "close", e.toString());
    }
  }
}
