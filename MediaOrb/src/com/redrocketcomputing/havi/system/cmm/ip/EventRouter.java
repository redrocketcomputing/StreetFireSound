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
 * $Id: EventRouter.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip;

import java.io.IOException;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.eventmanager.rmi.EventManagerClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviCmmIpConfigurationException;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviCmmIpUnidentifiedFailureException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.SystemEventId;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpGoneDevicesEventListener;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNetworkReadyEventListener;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNetworkResetEventListener;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNewDevicesEventListener;
import com.redrocketcomputing.havi.system.em.EventManager;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Interfaces the CMM IP services to the Havi EventManagerService service by monitoring GARP event and dispatching to the EventManagerService
 * 
 * @author stephen
 * 
 * Copyright @ StreetFire Sound Labs, LLC
 */
class EventRouter implements GarpGoneDevicesEventListener, GarpNetworkReadyEventListener, GarpNetworkResetEventListener, GarpNewDevicesEventListener
{
  private final static byte[] EMPTY_BYTE_ARRAY = new byte[0];

  private EventManagerClient client;
  private EventDispatch eventDispatcher;
  private EventManager eventManager;

  /**
   * Constructor for EventRouter.
   */
  public EventRouter(SoftwareElement softwareElement) throws HaviCmmIpException
  {
    try
    {
      // Create event manager client
      client = new EventManagerClient(softwareElement);

      // Get the event dispatcher
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // This is very bad
        throw new HaviCmmIpConfigurationException("can not find event dispatch service");
      }

      // Register for indication events
      eventDispatcher.addListener(this);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviCmmIpUnidentifiedFailureException(e.toString());
    }
  }

  /**
   * Release a event handler resources
   */
  public void close()
  {
    // Unregister from the event dispatcher service
    eventDispatcher.removeListener(this);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpGoneDevicesEventListener#goneDevicesEvent(GUID[])
   */
  public void goneDevicesEvent(GUID[] guids)
  {
    try
    {
      // Log debug
      // LoggerSingleton.logDebugCoarse(this.getClass(), "goneDevicesEvent", toString(guids));

      // Create output stream for event info
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(guids.length * GUID.SIZE + 4);

      // Marshall the guids
      marshalGuidList(guids, hbaos);

      // Post the event
      client.postEventSync(0, new SystemEventId(ConstSystemEventType.GONE_DEVICES), false, hbaos.toByteArray());
    }
    catch (HaviMarshallingException e)
    {
      // Log message
      LoggerSingleton.logFatal(this.getClass(), "goneDevicesEvent", e.toString());
    }
    catch (HaviException e)
    {
      // Log message
      LoggerSingleton.logError(this.getClass(), "goneDevicesEvent", e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNetworkReadyEventListener#networkReadyEvent(GUID[], GUID[], GUID[], GUID[])
   */
  public void networkReadyEvent(GUID[] newDevices, GUID[] goneDevices, GUID[] activeDevices, GUID[] nonactiveDevices)
  {
    try
    {
      // Log debug
      // LoggerSingleton.logDebugCoarse(this.getClass(), "networkReadyEvent", toString(activeDevices));

      // Create output stream for event info
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();

      // Marshall the guids
      marshalGuidList(activeDevices, hbaos);
      marshalGuidList(nonactiveDevices, hbaos);
      marshalGuidList(newDevices, hbaos);
      marshalGuidList(goneDevices, hbaos);

      // Post the event
      client.postEventSync(0, new SystemEventId(ConstSystemEventType.GUID_LIST_READY), false, hbaos.toByteArray());
    }
    catch (HaviException e)
    {
      // Log message
      LoggerSingleton.logError(this.getClass(), "networkReadyEvent", e.toString());
    }
    catch (HaviMarshallingException e)
    {
      // Log message
      LoggerSingleton.logFatal(this.getClass(), "networkReadyEvent", e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNetworkResetEventListener#networkResetEvent()
   */
  public void networkResetEvent()
  {
    try
    {
      // Log debug
      // LoggerSingleton.logDebugCoarse(this.getClass(), "networkResetEvent", "");

      // Post the event
      client.postEventSync(0, new SystemEventId(ConstSystemEventType.NETWORK_RESET), false, EMPTY_BYTE_ARRAY);
    }
    catch (HaviException e)
    {
      // Log message
      LoggerSingleton.logError(this.getClass(), "networkResetEvent", e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNewDevicesEventListener#newDevicesEvent(GUID[])
   */
  public void newDevicesEvent(GUID[] guids)
  {
    try
    {
      // Log debug
      // LoggerSingleton.logDebugCoarse(this.getClass(), "newDevicesEvent", toString(guids));

      // Create output stream for event info
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(guids.length * GUID.SIZE + 4);

      // Marshall the guids
      marshalGuidList(guids, hbaos);

      // Post the event
      client.postEventSync(0, new SystemEventId(ConstSystemEventType.NEW_DEVICES), false, hbaos.toByteArray());
    }
    catch (HaviException e)
    {
      // Log message
      LoggerSingleton.logError(this.getClass(), "newDevicesEvent", e.toString());
    }
    catch (HaviMarshallingException e)
    {
      // Log message
      LoggerSingleton.logFatal(this.getClass(), "newDevicesEvent", e.toString());
    }
  }

  private void marshalGuidList(GUID[] guidList, HaviByteArrayOutputStream hbaos) throws HaviMarshallingException
  {
    try
    {
      // Marshal length
      hbaos.writeInt(guidList.length);

      // Marshall the array
      for (int i = 0; i < guidList.length; i++)
      {
        guidList[i].marshal(hbaos);
      }
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviMarshallingException();
    }
  }

  /**
   * Return a string representing a GUID array
   * 
   * @param prefix
   * @param guids
   * @return String
   */
  private String toString(String prefix, GUID[] guids)
  {
    StringBuffer stringBuffer = new StringBuffer(prefix);
    stringBuffer.append('[');
    stringBuffer.append(guids.length);
    stringBuffer.append("]:");
    for (int i = 0; i < guids.length; i++)
    {
      if (i != 0)
      {
        stringBuffer.append(',');
      }

      stringBuffer.append(guids[i].toString());
    }
    return stringBuffer.toString();
  }

  private String toString(GUID[] guids)
  {
    // Build GUID array string
    StringBuffer stringBuffer = new StringBuffer("GUID[");
    stringBuffer.append(guids.length);
    stringBuffer.append("]: ");
    for (int i = 0; i < guids.length; i++)
    {
      stringBuffer.append(guids[i].toString());
      stringBuffer.append(' ');
    }

    return stringBuffer.toString();
  }
}