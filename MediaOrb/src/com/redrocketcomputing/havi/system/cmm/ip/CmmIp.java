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
 * $Id: CmmIp.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip;


import java.io.PrintStream;

import org.havi.system.cmmip.rmi.CmmIpServerHelper;
import org.havi.system.cmmip.rmi.CmmIpSkeleton;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviCmmIpAddressException;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviCmmIpNotReadyException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.constants.ConstMediaOrbRelease;
import com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpEngine;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpException;
import com.redrocketcomputing.havi.system.cmm.ip.tcp.TcpServiceHandler;
import com.redrocketcomputing.havi.system.service.SystemService;
import com.redrocketcomputing.util.log.LoggerSingleton;
/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class CmmIp extends SystemService implements VersionSkeleton, CmmIpSkeleton
{
  private final static int DEFAULT_GARP_READY_TIMEOUT = 1000;
  private final static int DEFAULT_HEARTBEAT_TIMEOUT = 1000;

  private CmmIpServerHelper serverHelper = null;
  private VersionServerHelper versionServerHelper = null;
  private GUID localGuid = null;
  private GarpEngine garp = null;
  private EventDispatch eventDispatcher = null;
  private ServiceHandlerTable serviceHandlerTable = null;
  private IndicationRouter indicationRouter = null;
  private EventRouter eventRouter = null;

  
  public CmmIp(String instanceName)
  {
    // Forward to super class
    super(instanceName, ConstSoftwareElementType.COMMUNICATION_MEDIA_MANAGER);
  }
  
  /**
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {

    // Check service state
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException("CmmIpService is not idle");
    }

    try
    {
      // Forward to super class
      super.start();
      
      // Find the GADP service
      Gadp gadp = (Gadp)ServiceManager.getInstance().find(Gadp.class);
      if (gadp == null)
      {
        // Umm, service exception due to configuration problem
        throw new ServiceException("can not find GADP service");
      }

      // Get the local guid
      localGuid = gadp.getLocalGuid();

      // Find the event dispatch service
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Umm, configuration problem
        throw new ServiceException("can not find EventDispatch service");
      }

      // Get GARP configuration information
      int readyTimeout = getConfiguration().getIntProperty("garp.ready.timeout", DEFAULT_GARP_READY_TIMEOUT);
      int heartbeatTimeout = getConfiguration().getIntProperty("garp.heartbeat.timeout", DEFAULT_HEARTBEAT_TIMEOUT);

      // Create GARP engine
      garp = new GarpEngine(localGuid, ConstCmmIpWellKnownAddresses.MULTICAST_ADDRESS, ConstCmmIpWellKnownAddresses.ROOT_PORT_ADDRESS + ConstCmmIpWellKnownAddresses.GARP_ADDRESS, readyTimeout, heartbeatTimeout);

      // Start the garp engine running by forcing a network reset
      garp.forceNetworkReset();

      // Create the service handler table
      serviceHandlerTable = new ServiceHandlerTable(ConstCmmIpWellKnownAddresses.ADDRESS_SPACE_SIZE);

      // Add the TCP service handler
      serviceHandlerTable.put(new TcpServiceHandler(localGuid, garp));

      // Create the indication handler
      indicationRouter = new IndicationRouter(garp);

      // Create the event handler
      eventRouter = new EventRouter(getSoftwareElement());

      // Create the server helpers
      serverHelper = new CmmIpServerHelper(getSoftwareElement(), this);
      getSoftwareElement().addHaviListener(serverHelper);
      versionServerHelper = new VersionServerHelper(getSoftwareElement(), this);
      getSoftwareElement().addHaviListener(versionServerHelper);

      // Change state
      setServiceState(Service.RUNNING);

      // HACK ALERT
      while (!garp.isReady())
      {
        // Wait some
        Thread.sleep(500);
      }

      // Log start of service
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running with version " + ConstMediaOrbRelease.getRelease());

    }
    catch (InterruptedException e)
    {
      // Translate
      throw new ServiceException(e.toString());
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
    catch (GarpException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Check service state
    if (getServiceState() != Service.RUNNING)
    {
      throw new ServiceException("Gadp is not running");
    }

    // Close all service handlers
    ServiceHandler[] installedServiceHandlers = serviceHandlerTable.getAll();
    for (int i = 0; i < installedServiceHandlers.length; i++)
    {
      // Check for valid service
      if (installedServiceHandlers[i] != null)
      {
        // Close the service handlers
        installedServiceHandlers[i].close();
      }
    }

    // Close the indication handler and event router
    indicationRouter.close();
    eventRouter.close();
    
    // Forward to super class
    super.terminate();

    // Release objects
    indicationRouter = null;
    eventRouter = null;
    eventDispatcher = null;
    garp = null;
    serviceHandlerTable = null;
    versionServerHelper = null;

    // Change state
    setServiceState(Service.IDLE);

    // Log terminate
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Check to see if the service is running
    if (getServiceState() != Service.RUNNING)
    {
      // Just print idle
      printStream.println("Service is IDLE");

      // All done
      return;
    }

    try
    {
      // Print the local guid
      printStream.println("Local " + localGuid.toString());

      // Print garp system information
      printStream.println("GARP State: " + garp.toString());
      printStream.println("GARP Resets: " + garp.getNetworkResetCounter());

      // Print the active and nonactive devices
      GUID[] activeGuids = (getServiceState() != Service.RUNNING ? new GUID[0] : getActiveDevices());
      GUID[] nonactiveGuids = (getServiceState() != Service.RUNNING ? new GUID[0] : getNonactiveDevices());
      printStream.println("Active Devices: " + toString(activeGuids));
      printStream.println("Nonactive Devices: " + toString(nonactiveGuids));

      // Add empty line
      printStream.println();

      // Ask all service handles to display some information
      ServiceHandler[] installedServiceHandlers = serviceHandlerTable.getAll();
      for (int i = 0; i < installedServiceHandlers.length; i++)
      {
        // Check to null service
        if (installedServiceHandlers[i] != null)
        {
          // Display information
          installedServiceHandlers[i].dump(printStream, arguments);
        }
      }

      // Add empty line
      printStream.println();

      // Ask indication router to dump the
      indicationRouter.dump(printStream, arguments);
    }
    catch (HaviCmmIpException e)
    {
      // print the exception
      printStream.println(e.toString());
    }
  }
  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.CmmIp#dropIndication(IndicationEventListener, GUID, int)
   */
  public void dropIndication(IndicationEventListener listener, GUID guid, int address) throws HaviCmmIpException
  {
    // Check the parameters
    if (listener == null || guid == null)
    {
      // Bad bad...
      throw new IllegalArgumentException();
    }

    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Not ready
      throw new HaviCmmIpNotReadyException("service is not running");
    }

    // Forward to the indication handler
    indicationRouter.dropIndication(listener, guid, address);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.CmmIp#enrollIndication(IndicationEventListener, GUID, int)
   */
  public boolean enrollIndication(IndicationEventListener listener, GUID guid, int address) throws HaviCmmIpException
  {
    // Check the parameters
    if (listener == null || guid == null)
    {
      // Bad bad...
      throw new IllegalArgumentException();
    }

    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Not ready
      throw new HaviCmmIpNotReadyException("service is not running");
    }

    // Forward to the indication handler
    return indicationRouter.enrollIndication(listener, guid, address);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.CmmIp#getActiveDevices()
   */
  public GUID[] getActiveDevices() throws HaviCmmIpException
  {
    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Not ready
      throw new HaviCmmIpNotReadyException("service is not running");
    }

    // Forward to the GARP
    return garp.getActiveDevices();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.CmmIp#getNonactiveDevices()
   */
  public GUID[] getNonactiveDevices() throws HaviCmmIpException
  {
    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Not ready
      throw new HaviCmmIpNotReadyException("service is not running");
    }

    // Forward to the GARP
    return garp.getNonactiveDevices();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.CmmIp#resetNetwork()
   */
  public void resetNetwork() throws HaviCmmIpException
  {
    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Not ready
      throw new HaviCmmIpNotReadyException("service is not running");
    }

    // Forward to the GARP
    garp.forceNetworkReset();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.CmmIp#read(GUID, int, int)
   */
  public byte[] read(GUID guid, int address) throws HaviCmmIpException
  {
    // Check the parameters
    if (guid == null)
    {
      // Bad bad...
      throw new IllegalArgumentException();
    }

    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Not ready
      throw new HaviCmmIpNotReadyException("service is not running");
    }

    // Try to get the service handler
    ServiceHandler serviceHandler = serviceHandlerTable.get(address);
    if (serviceHandler == null)
    {
      throw new HaviCmmIpAddressException("bad address: " + address);
    }

    // Forward the service handler
    return serviceHandler.receive(guid);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.CmmIp#write(GUID, int, byte[])
   */
  public void write(GUID guid, int address, byte[] data) throws HaviCmmIpException
  {
    // Check the parameters
    if (guid == null || data == null)
    {
      // Bad bad...
      throw new IllegalArgumentException();
    }

    // Make sure we are running
    if (getServiceState() != Service.RUNNING)
    {
      // Not ready
      throw new HaviCmmIpNotReadyException("service is not running");
    }

    // Try to get the service handler
    ServiceHandler serviceHandler = serviceHandlerTable.get(address);
    if (serviceHandler == null)
    {
      throw new HaviCmmIpAddressException("bad address: " + address);
    }

    // Forward the service handler
    serviceHandler.send(guid, data);
  }

  public String toString(GUID[] guids)
  {
    // Print header
    StringBuffer buffer = new StringBuffer("GUID[");
    buffer.append(guids.length);
    buffer.append(']');

    // Check for guids
    if (guids.length > 0)
    {
      // Add spacer
      buffer.append(" - ");

      // Loop through the guids
      for (int i = 0; i < guids.length; i++)
      {
        // Get the value
        byte[] guidValue = guids[i].getValue();

        // Add the guid bytes to the buffer
        buffer.append(Integer.toHexString(guidValue[0] & 0xff));
        buffer.append(':');
        buffer.append(Integer.toHexString(guidValue[1] & 0xff));
        buffer.append(':');
        buffer.append(Integer.toHexString(guidValue[2] & 0xff));
        buffer.append(':');
        buffer.append(Integer.toHexString(guidValue[3] & 0xff));
        buffer.append(':');
        buffer.append(Integer.toHexString(guidValue[4] & 0xff));
        buffer.append(':');
        buffer.append(Integer.toHexString(guidValue[5] & 0xff));
        buffer.append(':');
        buffer.append(Integer.toHexString(guidValue[6] & 0xff));
        buffer.append(':');
        buffer.append(Integer.toHexString(guidValue[7] & 0xff));

        // Append comma if this is not the end of the array
        if (i != guids.length - 1)
        {
          buffer.append(", ");
        }
      }
    }

    // Return the string
    return buffer.toString();
  }

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    return ConstMediaOrbRelease.getRelease();
  }
}
