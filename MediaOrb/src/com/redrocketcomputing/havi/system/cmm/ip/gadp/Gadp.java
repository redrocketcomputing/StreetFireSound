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
 * $Id: Gadp.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.gadp;

import java.io.PrintStream;

import org.havi.system.types.GUID;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.util.concurrent.Latch;
import com.redrocketcomputing.util.concurrent.Sync;
import com.redrocketcomputing.util.concurrent.TimeoutSync;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class Gadp extends AbstractService implements GadpReadyEventListener
{
  private final static int DEFAULT_RESERVE_TIMEOUT = 5000;

  private GadpEngine protocolEngine = null;
  private EventDispatch eventDispatcher;
  private ComponentConfiguration configuration;
  private int reserveTimeout = DEFAULT_RESERVE_TIMEOUT;
  private String multicastAddress = ConstCmmIpWellKnownAddresses.MULTICAST_ADDRESS;
  private int multicastPort = ConstCmmIpWellKnownAddresses.ROOT_PORT_ADDRESS + ConstCmmIpWellKnownAddresses.GADP_ADDRESS;
  private int addressSpaceSize = ConstCmmIpWellKnownAddresses.ADDRESS_SPACE_SIZE;
  private int addressBase = ConstCmmIpWellKnownAddresses.ROOT_PORT_ADDRESS;
  private Sync ready;

  /**
   * Constructor for Gadp.
   * @param instanceName
   */
  public Gadp(String instanceName)
  {
    // Construct super class
    super(instanceName);

    // Build configuration
    configuration = ConfigurationProperties.getInstance().getComponentConfiguration(instanceName);
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Check service state
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException("Gadp is not idle");
    }

    try
    {
       // Get the event dispatcher service
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Throw exception
        throw new ServiceException("can not find event dispatch service");
      }

      // Read configuration
      reserveTimeout = configuration.getIntProperty("reserve.timeout", DEFAULT_RESERVE_TIMEOUT);

      // Register for ready event
      eventDispatcher.addListener(this);

      // Create the ready timeout latch wait for double the reserve timeout
      ready = new TimeoutSync(new Latch(), reserveTimeout * 4);

      // Create protocol engine
      protocolEngine = new GadpEngine(multicastAddress, multicastPort, addressBase, addressSpaceSize, reserveTimeout);

      // Wait on the ready latch
      ready.acquire();

      // Change state to running
      setServiceState(Service.RUNNING);

      // Log start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running with " + getLocalGuid());
    }
    catch (InterruptedException e)
    {
      // Throw service exception
      throw new ServiceException("failed to acquire a GUID");
    }
    catch (GadpException e)
    {
      // Throw exception
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

    // Close the protocol engine
    protocolEngine.close();

    // Release resources
    protocolEngine = null;
    eventDispatcher = null;

    // Change state to idle
    setServiceState(Service.IDLE);

    // Log terminate
    LoggerSingleton.logInfo(this.getClass(), "start", "service is idle");
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    // Dump state information
    printStream.println("GADP State: " + (protocolEngine == null ? "IDLE" : protocolEngine.toString()));
    printStream.println("Local " + getLocalGuid());
    printStream.println("Multicast Address: " + multicastAddress + '@' + multicastPort);
    printStream.println("Address Base: " + addressBase);
    printStream.println("Address Space Size: " + addressSpaceSize);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.Gadp#getLocalGuid()
   */
  public GUID getLocalGuid()
  {
    // Make sure the service is running
    if (getServiceState() != Service.RUNNING)
    {
      // Opps
      throw new ServiceException("service is not running");
    }

    return protocolEngine.getLocalGuid();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.gadp.GadpReadyEventListener#readyEvent(GUID)
   */
  public void readyEvent(GUID localGuid)
  {
    // Release ready latch
    ready.release();

    // Unregister
    eventDispatcher.removeListener(this);
  }
}
