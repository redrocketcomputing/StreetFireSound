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
 * $Id: RemoteServerHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import org.havi.system.HaviListener;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviMsgListenerNotFoundException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class RemoteServerHelper extends HaviListener
{
  private RemoteInvocationFactory invocationFactory;
  private SoftwareElement softwareElement;
  private RemoteSkeleton remoteSkeleton;
  private boolean multiThread = false;
  private boolean threadLocal = false;
  private TaskPool taskPool;

  /**
   * Constructor for RemoteServerHelper.
   */
  public RemoteServerHelper(SoftwareElement softwareElement, RemoteInvocationFactory invocationFactory, RemoteSkeleton remoteSkeleton) throws HaviMsgListenerExistsException
  {
    // Check the parameter
    if (invocationFactory == null || softwareElement == null || remoteSkeleton == null)
    {
      // Opps
      throw new IllegalArgumentException("parameter is null");
    }

    // Save the parameters
    this.softwareElement = softwareElement;
    this.invocationFactory = invocationFactory;
    this.remoteSkeleton = remoteSkeleton;

    // Get the task pool server
    taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
    if (taskPool == null)
    {
      // Very bad
      throw new IllegalStateException("can not find task pool service");
    }
  }

  public void close() throws HaviMsgListenerNotFoundException
  {
    // Remove ourselves from the software element
    softwareElement.removeHaviListener(this);
  }

  /**
   * @see org.havi.system.HaviListener#receiveMsg(boolean, byte, SEID, SEID, Status, HaviByteArrayInputStream)
   */
  public boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload)
  {
    //ignore if replied already
    if (haveReplied)
    {
      return false;
    }

    //ignore if not havi rmi
    if (protocolType != ConstProtocolType.HAVI_RMI)
    {
      return false;
    }

    //try to handle.
    try
    {
      // Read RMI header
      HaviRmiHeader header = new HaviRmiHeader(payload);
      if((header.getControlFlags() & 0x01) == 1)
      {
        return false;
      }

      // Try to create a remote invocation object
      RemoteInvocation invocation = invocationFactory.createInvocation(header, payload);

      if (invocation == null)
      {
        // Not for us
        return false;
      }

      // Create task
      RemoteServerHelperTask invocationTask = new RemoteServerHelperTask(softwareElement, sourceId, header, invocation, remoteSkeleton, threadLocal);

      // Check dispatch method
      if (multiThread)
      {
        // Launch the task
        taskPool.execute(invocationTask);
      }
      else
      {
        // Execute directly
        invocationTask.run();
      }

      // All gone
      return true;
    }
    catch (TaskAbortedException e)
    {
      LoggerSingleton.logError(this.getClass(), "receiveMsg", e.toString());
    }
    catch(HaviUnmarshallingException e)
    {
      LoggerSingleton.logError(this.getClass(), "receiveMsg", e.toString());
    }

    // We did not handle it
    return false;
  }

  /**
   * Returns the softwareElement.
   * @return SoftwareElement
   */
  public SoftwareElement getSoftwareElement()
  {
    return softwareElement;
  }

  /**
   * Returns the multiThread.
   * @return boolean
   */
  public boolean isMultiThread()
  {
    return multiThread;
  }

  /**
   * Sets the multiThread.
   * @param multiThread The multiThread to set
   */
  public void setMultiThread(boolean multiThread)
  {
    this.multiThread = multiThread;
  }


  /**
   * @return Returns the threadLocal.
   */
  public boolean isThreadLocal()
  {
    return threadLocal;
  }
  
  /**
   * @param threadLocal The threadLocal to set.
   */
  public void setThreadLocal(boolean threadLocal)
  {
    this.threadLocal = threadLocal;
  }
}
