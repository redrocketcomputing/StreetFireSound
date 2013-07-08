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
 * $Id: RemoteShellService.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.remoteshell;

import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.shell.Shell;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.remoteshell.commands.RemoteCommand;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Implements a remote telnet style service
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class RemoteShellService extends AbstractService
{
  private ComponentConfiguration configuration;
  private Properties remoteShellProperties;
  private List openConnections = Collections.synchronizedList(new LinkedList());
  private TaskPool taskPool = null;
  private AcceptorTask acceptor = null;

  /**
   * Constructor for RemoteShellService.
   */
  public RemoteShellService(String instanceName)
  {
    // Initialize superclass
    super(instanceName);

    // Create a component configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public synchronized void start() throws ServiceException
  {

    // Check to see if we are already started
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException("RemoteShellService already running");
    }

    try
    {
      // Find the GADP service
      Gadp gadp = (Gadp)ServiceManager.getInstance().find(Gadp.class);
      if (gadp == null)
      {
        // Umm, service exception due to configuration problem
        throw new ServiceException("can not find GADP service");
      }

      // Build service address
      byte[] rawGuid = gadp.getLocalGuid().getValue();
      int servicePort = ((rawGuid[4] & 0xff) << 24) + ((rawGuid[5] & 0xff) << 16) + ((rawGuid[6] & 0xff) << 8) + (rawGuid[7] & 0xff) + ConstCmmIpWellKnownAddresses.REMOTE_SHELL_ADDRESS;

      // Use the first task pool service found
      taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Can not find required component
        throw new ServiceException("task pool service not found");
      }

      // Create remote shell properties
      remoteShellProperties = new Properties();
      remoteShellProperties.setProperty("shell.prompt", "> ");
      remoteShellProperties.setProperty("remote.shell.server.name", getInstanceName());

      // Bind remote command to shell globally
      Shell.installGlobally(RemoteCommand.class);

      // Create and start the acceptor task
      acceptor = new AcceptorTask(this, servicePort);

      // Launch acceptor tak
      taskPool.execute(acceptor);

      // Change state to running
      setServiceState(Service.RUNNING);

      // Log start of service
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running on port " + servicePort);

    }
    catch (IOException e)
    {
      // Throw a service exception
      throw new ServiceException("start detected an IOException: " + e.getMessage());
    }
    catch (TaskAbortedException e)
    {
      // Umm, task stopped running or is full or something, do not start the remote shell
      throw new ServiceException("task abort exception while starting the acceptor: " + e.getMessage());
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public synchronized void terminate() throws ServiceException
  {
    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      throw new ServiceException("RemoteShellService not running");
    }

    // Close the acceptor task
    acceptor.close();
    acceptor = null;

    // Kill all connected shell
    killAll();

    // Log start of service
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is stopped");
    System.out.println("remoteshell service start done");

  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public synchronized void info(PrintStream printStream, String[] arguments) throws ServiceException
  {
    // Print information
    printStream.println("Total Connections: " + (acceptor != null ? acceptor.getConnectionCount() : -1));
    printStream.println("Active Connections: " + openConnections.size());

    // List the connections
    list(printStream);
  }

  /**
   * Force the termination of the specified remote shell
   * @param id The ID of the remote shell to kill
   */
  public void kill(int id)
  {
    // Loop through the open connections look for shell task to kill
    for (Iterator iterator = openConnections.iterator(); iterator.hasNext();)
    {
      // Extract the shell task
      ShellTask element = (ShellTask) iterator.next();

      // Check for match id
      if (element.getShellId() == id)
      {
        // Terminate the shell
        element.close();

        // All done
        return;
      }
    }
  }

  public void killAll()
  {
    // Loop through the open connections look for shell task to kill
    for (Iterator iterator = openConnections.iterator(); iterator.hasNext();)
    {
      // Extract the shell task
      ShellTask element = (ShellTask) iterator.next();

      // Terminate the shell
      element.close();
    }
  }

  public void list(PrintStream printStream)
  {
    // Set up for message format
    Object[] messageParameters = new Object[3];
    String formatString = "{0,number,000000} {1,number,########}    {2}";

    // Display table header
    printStream.println("ID     Duration Login");

    // Loop through the open connections
    for (Iterator iter = openConnections.iterator(); iter.hasNext();)
    {
      // Extract the task
      ShellTask element = (ShellTask) iter.next();

      // Build message parameters
      messageParameters[0] = new Integer(element.getShellId());
      messageParameters[1] = new Long(System.currentTimeMillis() - element.getStartTime());
      messageParameters[2] = element.getLoginName();

      // Output the message
      printStream.println(MessageFormat.format(formatString, messageParameters));
    }
  }

  /**
   * Return a clone of the remote shell properties built from the configuration
   * @return Properties The cloned properties
   */
  Properties createRemoteShellProperties()
  {
    synchronized (remoteShellProperties)
    {
      return new Properties(remoteShellProperties);
    }
  }

  /**
   * Launch the specified shell task by adding the task to the open connections list and handing the task
   * to the internal executor.
   * @param shellTask The shell task to launch
   */
  void launchRemoteShell(ShellTask shellTask)
  {
    // Check arguments
    if (shellTask == null)
    {
      throw new IllegalArgumentException("ShellTask cannot be null");
    }

    try
    {
      // Add task to the executor
      taskPool.execute(shellTask);

      // Add the shell task to the open connections list
      openConnections.add(shellTask);
    }
    catch (TaskAbortedException e)
    {
      LoggerSingleton.logError(this.getClass(), "launchRemoteShell", "task aborted while launching shell: " + e.getMessage());
    }
  }

  /**
   * Terminate the specified shell task by removing from the open connections list and closing the task
   * @param shellTask The shell task to terminate
   */
  void terminateShellTask(ShellTask shellTask)
  {
    // Check arguments
    if (shellTask == null)
    {
      throw new IllegalArgumentException("ShellTask cannot be null");
    }

    // Remove the shell task from the open connections
    openConnections.remove(shellTask);
  }

}
