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
 * $Id: Tam.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.tam.cmm.ip;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgFailException;
import org.havi.system.types.HaviMsgNotReadyException;
import org.havi.system.types.HaviMsgTimeoutException;
import org.havi.system.types.HaviUnmarshallingException;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.constants.ConstCmmIpIndications;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.system.cmm.ip.CmmIp;
import com.redrocketcomputing.havi.system.cmm.ip.IndicationEventListener;
import com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener;
import com.redrocketcomputing.havi.system.ms.message.HaviMessage;
import com.redrocketcomputing.havi.system.ms.tam.TransportAdaptationModule;
import com.redrocketcomputing.havi.system.ms.tam.TransportAdaptationModuleListener;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author Stephen
 *  
 */
public class Tam implements TransportAdaptationModule, IndicationEventListener, NetworkReadyEventListener
{
  private final static int LOCK_TIMEOUT = 10000;
  private final static int OUTPUT_BUFFER_SIZE = 1450;

  private TransportAdaptationModuleListener listener;
  private EventDispatch eventDispatcher;
  private CmmIp cmm = null;
  private DeviceTable deviceTable = new DeviceTable();

  /**
   * Constructor for Tam.
   */
  public Tam(TransportAdaptationModuleListener listener) throws HaviMsgException
  {
    // Save the listener
    this.listener = listener;

    // Try to get the event dispatch service
    eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
    if (eventDispatcher == null)
    {
      // Very bad
      throw new IllegalStateException("can not find event dispatch service");
    }

    // Register for events
    eventDispatcher.addListener(this);
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.tam.TransportAdaptationModule#send(HaviMessage)
   */
  public void send(HaviMessage message) throws HaviMsgException
  {
    // Check parameter
    if (message == null)
    {
      throw new IllegalArgumentException("message is null");
    }

    // Make sure we are ready
    if (cmm == null)
    {
      // Not ready yet
      throw new HaviMsgNotReadyException("cmm is null");
    }

    // Look up the device
    Device device = deviceTable.get(message.getDestination().getGuid());

    try
    {
      // Aquire the lock
      if (!device.attempt(LOCK_TIMEOUT))
      {
        throw new HaviMsgTimeoutException("getting sequence lock");
      }

      // Marshall the message
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(OUTPUT_BUFFER_SIZE);
      message.marshal(hbaos);

      // Send the message
      cmm.write(message.getDestination().getGuid(), ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS, hbaos.toByteArray());
    }
    catch (HaviCmmIpException e)
    {
      // Translate
      throw new HaviMsgFailException(e.toString());
    }
    catch (InterruptedException e)
    {
      // Translate
      throw new HaviMsgFailException(e.toString());
    }
    catch (HaviMarshallingException e)
    {
      // Translate
      throw new HaviMsgFailException(e.toString());
    }
    finally
    {
      // Release the local
      device.release();
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.IndicationEventListener#indicationEvent(GUID, int, int, Object)
   */
  public void indicationEvent(GUID guid, int address, int indication, Object data)
  {
    try
    {
      // Check indication type
      if (indication == ConstCmmIpIndications.READ_AVAILABLE)
      {
        if (cmm == null)
        {
          // Try to get the CmmIp services
          cmm = (CmmIp)ServiceManager.getInstance().find(CmmIp.class);
          if (cmm == null)
          {
            // Umm, bad bad problme
            throw new IllegalStateException("can not find CmmIp");
          }
        }

        // Read from the guid until buffer is empty
        byte[] buffer;
        while ((buffer = cmm.read(guid, address)) != null)
        {
          try
          {
            // Allocate the input stream
            HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(buffer);

            // Unmarshall the message
            HaviMessage message = HaviMessage.create(hbais);

            // Dispatch it to the listener
            listener.received(message);
          }
          catch (HaviUnmarshallingException e)
          {
            // Log the error
            LoggerSingleton.logError(this.getClass(), "indicationEvent", e.toString());
          }
        }
      }
    }
    catch (HaviCmmIpException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "indicationEvent", e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.event.NetworkReadyEventListener#networkReadyEvent(GUID[], GUID[], GUID[], GUID[])
   */
  public void networkReadyEvent(GUID[] activeDevices, GUID[] nonactiveDevices, GUID[] newDevices, GUID[] goneDevices)
  {
    try
    {
      // Check to see if we are already initialized
      if (cmm == null)
      {
        // Try to get the CmmIp services
        cmm = (CmmIp)ServiceManager.getInstance().find(CmmIp.class);
        if (cmm == null)
        {
          // Umm, bad bad problme
          throw new IllegalStateException("can not find CmmIp");
        }

        // Enroll for indications
        cmm.enrollIndication(this, GUID.BROADCAST, ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS);
      }
    }
    catch (HaviCmmIpException e)
    {
      // Log fatal error
      LoggerSingleton.logFatal(this.getClass(), "networkReadyEvent", e.toString());
    }
  }
}