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
 * $Id: StreamManager.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */
package com.redrocketcomputing.havi.system.sm;

import java.io.PrintStream;

import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.streammanager.rmi.StreamManagerServerHelper;
import org.havi.system.streammanager.rmi.StreamManagerSkeleton;
import org.havi.system.types.Connection;
import org.havi.system.types.ConnectionHint;
import org.havi.system.types.ConnectionId;
import org.havi.system.types.FcmPlug;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviStreamManagerException;
import org.havi.system.types.HaviStreamManagerNotImplementedException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.Stream;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.constants.ConstMediaOrbRelease;
import com.redrocketcomputing.havi.system.service.SystemService;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StreamManager extends SystemService implements StreamManagerSkeleton, VersionSkeleton
{
  private StreamManagerServerHelper serverHelper;
  private VersionServerHelper versionServerHelper;
  
  /**
   * @param instanceName
   * @param type
   */
  public StreamManager(String instanceName)
  {
    super(instanceName, ConstSoftwareElementType.STREAM_MANAGER);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public synchronized void start()
  {
    // Check for running
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("already started");
    }
    
    try
    {
      // Create the server helpers
      serverHelper = new StreamManagerServerHelper(getSoftwareElement(), this);
      versionServerHelper = new VersionServerHelper(getSoftwareElement(), this);
      getSoftwareElement().addHaviListener(serverHelper);
      getSoftwareElement().addHaviListener(versionServerHelper);

      // Mark as running
      setServiceState(Service.RUNNING);

      // Log service start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running with version " + ConstMediaOrbRelease.getRelease());
    }
    catch (HaviMsgListenerExistsException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
    catch (HaviException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void terminate()
  {
    // Check for running
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("not started");
    }

    // Close the server helper
    getSoftwareElement().removeHaviListener(serverHelper);
    getSoftwareElement().removeHaviListener(versionServerHelper);
    serverHelper.close();
    versionServerHelper.close();
    serverHelper = null;
    versionServerHelper = null;

    // Last terminate the super class
    super.terminate();
    
    // Mark as terminated
    setServiceState(Service.IDLE);
    
    // Log the start
    LoggerSingleton.logInfo(this.getClass(), "start", "service is idle");
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // TODO Auto-generated method stub
    super.info(printStream, arguments);
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#flowTo(boolean, org.havi.system.types.FcmPlug, org.havi.system.types.FcmPlug, org.havi.system.types.ConnectionHint)
   */
  public ConnectionId flowTo(boolean dynamicBw, FcmPlug source, FcmPlug sink, ConnectionHint hint) throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#sprayOut(boolean, org.havi.system.types.FcmPlug, org.havi.system.types.ConnectionHint)
   */
  public ConnectionId sprayOut(boolean dynamicBw, FcmPlug source, ConnectionHint hint) throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#tapIn(org.havi.system.types.FcmPlug, org.havi.system.types.ConnectionHint)
   */
  public ConnectionId tapIn(FcmPlug sink, ConnectionHint hint) throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#drop(org.havi.system.types.ConnectionId)
   */
  public void drop(ConnectionId connId) throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#getLocalConnectionMap()
   */
  public Connection[] getLocalConnectionMap() throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#getGlobalConnectionMap()
   */
  public Connection[] getGlobalConnectionMap() throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#getConnection(org.havi.system.types.ConnectionId)
   */
  public Connection getConnection(ConnectionId connId) throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.streammanager.rmi.StreamManagerSkeleton#getStream(org.havi.system.types.ConnectionId)
   */
  public Stream getStream(ConnectionId connId) throws HaviStreamManagerException
  {
    throw new HaviStreamManagerNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    // TODO Auto-generated method stub
    return null;
  }
}
