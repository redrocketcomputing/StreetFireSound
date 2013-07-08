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
 * $Id: EventManager.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.em;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.havi.system.constants.ConstEventIdSchema;
import org.havi.system.constants.ConstEventManagerOperationId;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.constants.ConstSoftwareElementHandle;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.eventmanager.rmi.EventManagerNotificationMessageBackClient;
import org.havi.system.eventmanager.rmi.EventManagerServerHelper;
import org.havi.system.eventmanager.rmi.EventManagerSkeleton;
import org.havi.system.types.EventId;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviEventManagerDeliveryException;
import org.havi.system.types.HaviEventManagerException;
import org.havi.system.types.HaviEventManagerNotFoundException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviInvalidValueException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviMsgElementException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviMsgNotReadyException;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.constants.ConstMediaOrbRelease;
import com.redrocketcomputing.havi.system.rmi.RemoteInvocationInformation;
import com.redrocketcomputing.havi.system.rmi.RemoteServerHelperTask;
import com.redrocketcomputing.havi.system.service.SystemService;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.concurrent.SynchronizedRef;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *  
 */
public class EventManager extends SystemService implements EventManagerSkeleton, VersionSkeleton
{
  private SubscriptionTable table = null;
  private EventManagerServerHelper serverHelper = null;
  private VersionServerHelper versionServerHelper = null;
  private Map eventMap = new ListMap();
  private Map seidMap = new ListMap();
  private SynchronizedRef activeDevices = new SynchronizedRef(new SEID[0]);
  
  public EventManager(String instanceName)
  {
    super(instanceName, ConstSoftwareElementType.EVENTMANAGER);
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public synchronized void start()
  {

    // Check service state
    if (getServiceState() != Service.IDLE)
    {
      // Opps
      throw new ServiceException("service is not idle");
    }

    try
    {
      // Allocate subscription table
      table = new SubscriptionTable();
      
      // Forward to super class
      super.start();

      // Create the server helper
      serverHelper = new EventManagerServerHelper(getSoftwareElement(), this);
      versionServerHelper = new VersionServerHelper(getSoftwareElement(), this);

      getSoftwareElement().addHaviListener(serverHelper);
      getSoftwareElement().addHaviListener(new EventManagerProtocolListener(this));
      getSoftwareElement().addHaviListener(versionServerHelper);

      // Mark for thread local support
      serverHelper.setThreadLocal(true);

      // Mark service as started
      setServiceState(Service.RUNNING);

      // Log service start
      LoggerSingleton.logInfo(this.getClass(), "start", "service is running with version " + ConstMediaOrbRelease.getRelease());

    }
    catch (HaviMsgListenerExistsException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "start", e.toString());

      // Translate exception
      throw new ServiceException("detected " + e.toString());
    }
    catch (HaviException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public synchronized void terminate()
  {
    // Check state
    if (getServiceState() != Service.RUNNING)
    {
      // Ummm
      throw new ServiceException("service is not running");
    }

    // Remove listeners
    getSoftwareElement().removeHaviListener(serverHelper);
    getSoftwareElement().removeHaviListener(versionServerHelper);

    // Forward to super class
    super.terminate();

    // Release components
    serverHelper = null;
    versionServerHelper = null;
    table = null;

    // Change service state
    setServiceState(Service.IDLE);

    // Log terminate
    LoggerSingleton.logInfo(this.getClass(), "terminate", "service is idle");
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#info(PrintStream, String[])
   */
  public synchronized void info(PrintStream printStream, String[] arguments)
  {
    // Display local seid
    printStream.println("LocalSeid:" + getSoftwareElement().getSeid());
    printStream.println();

    // Display the subscription map
    printStream.println("EventId Subscriptions:");
    
    EventId[] eventIds = table.getEntries();
    for (int i = 0; i < eventIds.length; i++)
    {
      // Array of subscriptions
      SubscriptionEntry[] entries = table.getEntries(eventIds[i]);

      // Display
      printStream.print("EventId -> " + eventIds[i].toString() + " Subscriptions[" + entries.length + "]:");
      for (int j = 0; j < entries.length; j++)
      {
        // Display
        printStream.print(entries[i].toString());
      }

      // Add newline
      printStream.println();
    }

    // Display the active device list
    printStream.println();
    printStream.println("Current Active Device SEID list:");
    SEID[] seidList = (SEID[])activeDevices.get();
    for (int i = 0; i < seidList.length; i++)
    {
      printStream.println(seidList[i]);
    }
  }

  /**
   * The calling software element has to be aware that it has to listen to incoming requests with the specified operation code. The easiest way to do this, is to install an EventManagerNotificationListener-derived object via
   * SoftwareElement::addHaviListener before calling this method. It is the responsibility of the calling Software Element to remove the EventManagerNotificationListener-derived object again (using SoftwareElement::removeHaviListener) when this method
   * fails.
   * @param eidList
   * @param opCode
   */
  public void subscribe(EventId[] eidList, OperationCode opCode) throws HaviEventManagerException
  {
    // Get the caller SEID from the thread local
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Register the software element
    table.add(invocationInformation.getSourceSeid(), opCode);
    
    // Add events
    table.add(invocationInformation.getSourceSeid(), eidList);
    
  }
  
  /**
   * The calling software element has to be aware that it has to listen to incoming requests with the specified operation code. The easiest way to do this, is to install an EventManagerNotificationListener-derived object via
   * SoftwareElement::addHaviListener before calling this method. It is the responsibility of the calling Software Element to remove the EventManagerNotificationListener-derived object again (using SoftwareElement::removeHaviListener) when this method
   * fails.
   * @param eidList
   * @param opCode
   */
  public void internalSubscribe(SEID seid, EventId[] eidList, OperationCode opCode) throws HaviEventManagerException
  {
    // Register the software element
    table.add(seid, opCode);
    
    // Add events
    table.add(seid, eidList);
  }

  /**
   * When this method succeeds the calling SoftwareElement should call SoftwareElement.removeHaviListener to remove the installed EventManagerNotificationListener-derived object.
   * 
   * @param seid
   */
  public void unsubscribe() throws HaviEventManagerException
  {
    // Get the caller SEID from the thread local
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Remove all subscriptions
    table.remove(invocationInformation.getSourceSeid());
  }

  /**
   * The calling software element has to be aware that it has to listen to incoming requests with the specified operation code. The easiest way to do this, is to install an EventManagerNotificationListener-derived object via
   * SoftwareElement::addHaviListener before calling this method. It is the responsibility of the calling Software Element to remove the EventManagerNotificationListener-derived object again (using SoftwareElement::removeHaviListener) when this method
   * fails.
   * @param eidList
   * @param opCode
   */
  public void replace(EventId[] eidList, OperationCode opCode) throws HaviEventManagerException
  {
    // Get the caller SEID from the thread local
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Replace
    table.replace(invocationInformation.getSourceSeid(), opCode, eidList);
  }

  /**
   * Adds a single event to the specified SEID's subscription. The SEID must already be subscribed to add an event.
   * @param eventId
   */
  public void addEvent(EventId eventId) throws HaviEventManagerException
  {
    // Get the caller SEID from the thread local
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Forward
    table.add(invocationInformation.getSourceSeid(), eventId);
  }

  /**
   * Removes a single event from the specified SEID's subscription. The SEID must already be subscribed to remove an event.
   * 
   * @param eventId
   */
  public void removeEvent(EventId eventId) throws HaviEventManagerException
  {
    // Get the caller SEID from the thread local
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Forward
    table.remove(invocationInformation.getSourceSeid(), eventId);
  }

  /**
   * Post event locally or globally.
   * 
   * @param eventId
   * @param global
   * @param eventInfo
   * @throws HaviGeneralException
   * @throws HaviEventManagerDeliveryException
   * @throws HaviMsgNotReadyException
   * @throws HaviMsgElementException
   * @throws HaviInvalidValueException
   */
  public void postEvent(EventId eventId, boolean global, byte[] eventInfo) throws HaviEventManagerException
  {
    // Get the caller SEID from the thread local
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Spy on the events
    spy(eventId, eventInfo);
    
    // Check for global dispatch
    if (global)
    {
      SEID[] remoteSeids = (SEID[]) activeDevices.get();
      if (remoteSeids.length > 0)
      {
        try
        {
          // Marshall up the message
          HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
          ConstEventManagerOperationId.FORWARD_EVENT_OPCODE.marshal(hbaos);
          invocationInformation.getSourceSeid().marshal(hbaos);
          eventId.marshal(hbaos);
          hbaos.writeInt(eventInfo.length);
          hbaos.write(eventInfo);
 
            // Send the message
          getSoftwareElement().msgSendSimple((byte) ConstProtocolType.EVENT_MANAGER, remoteSeids, hbaos.toByteArray());
        }
        catch (HaviMsgException e)
        {
          // Log error
          LoggerSingleton.logError(this.getClass(), "postEvent", e.toString());
        }
        catch (HaviMarshallingException e)
        {
          // Log error
          LoggerSingleton.logError(this.getClass(), "postEvent", e.toString());
        }
        catch (IOException e)
        {
          // Log error
          LoggerSingleton.logError(this.getClass(), "postEvent", e.toString());
        }
      }
    }
    
    // Dispatch local events
    SubscriptionEntry[] entries = table.getEntries(eventId);
    for (int i = 0; i < entries.length; i++)
    {
      try
      {
        // Create message back client
        EventManagerNotificationMessageBackClient client = new EventManagerNotificationMessageBackClient(entries[i].getOpCode(), getSoftwareElement(), entries[i].getSeid());
        
        // Dispatch
        client.eventManagerNotification(invocationInformation.getSourceSeid(), eventId, eventInfo);
      }
      catch (HaviException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "postEvent", "flushing subscription to " + eventId + " due to: " + e.toString());
        
        // Flush subscription
        flush(entries[i].getSeid());
      }
    }
  }

  /**
   * Post event locally or globally.
   *
   * @param seid 
   * @param eventId
   * @param global
   * @param eventInfo
   * @throws HaviGeneralException
   * @throws HaviEventManagerDeliveryException
   * @throws HaviMsgNotReadyException
   * @throws HaviMsgElementException
   * @throws HaviInvalidValueException
   */
  public void internalPostEvent(SEID seid, EventId eventId, boolean global, byte[] eventInfo) throws HaviEventManagerException
  {
    // Spy on the events
    spy(eventId, eventInfo);
    
    // Check for global dispatch
    if (global)
    {
      SEID[] remoteSeids = (SEID[]) activeDevices.get();
      if (remoteSeids.length > 0)
      {
        try
        {
          // Marshall up the message
          HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
          ConstEventManagerOperationId.FORWARD_EVENT_OPCODE.marshal(hbaos);
          seid.marshal(hbaos);
          eventId.marshal(hbaos);
          hbaos.writeInt(eventInfo.length);
          hbaos.write(eventInfo);
 
            // Send the message
          getSoftwareElement().msgSendSimple((byte) ConstProtocolType.EVENT_MANAGER, remoteSeids, hbaos.toByteArray());
        }
        catch (HaviMsgException e)
        {
          // Log error
          LoggerSingleton.logError(this.getClass(), "internalPostEvent", e.toString());
        }
        catch (HaviMarshallingException e)
        {
          // Log error
          LoggerSingleton.logError(this.getClass(), "internalPostEvent", e.toString());
        }
        catch (IOException e)
        {
          // Log error
          LoggerSingleton.logError(this.getClass(), "internalPostEvent", e.toString());
        }
      }
    }
    
    // Dispatch local events
    SubscriptionEntry[] entries = table.getEntries(eventId);
    for (int i = 0; i < entries.length; i++)
    {
      try
      {
        // Create message back client
        EventManagerNotificationMessageBackClient client = new EventManagerNotificationMessageBackClient(entries[i].getOpCode(), getSoftwareElement(), entries[i].getSeid());
        
        // Dispatch
        client.eventManagerNotification(seid, eventId, eventInfo);
      }
      catch (HaviException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "internalPostEvent", "flushing subscription due to: " + e.toString());
        
        // Flush subscription
        flush(entries[i].getSeid());
      }
    }
  }

  public void forwardEvent(SEID posterSeid, EventId eventId, byte[] eventInfo) throws HaviEventManagerException
  {
    // Dispatch local events
    SubscriptionEntry[] entries = table.getEntries(eventId);
    for (int i = 0; i < entries.length; i++)
    {
      try
      {
        // Create message back client
        EventManagerNotificationMessageBackClient client = new EventManagerNotificationMessageBackClient(entries[i].getOpCode(), getSoftwareElement(), entries[i].getSeid());
        
        // Dispatch
        client.eventManagerNotification(posterSeid, eventId, eventInfo);
      }
      catch (HaviException e)
      {
        // Log error
        LoggerSingleton.logError(this.getClass(), "forwardEvent", "flushing subscription to " + eventId + " due to: " + e.toString());
        
        // Flush subscription
        flush(entries[i].getSeid());
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    return ConstMediaOrbRelease.getRelease();
  }

  private void spy(EventId eventId, byte[] eventInfo)
  {
    try
    {
      if (eventId == null || eventInfo == null)
      {
        LoggerSingleton.logError(this.getClass(), "spy", "bad parameters");
      }

      //spy on the events coming through:
      if (eventId.getDiscriminator() == ConstEventIdSchema.SYSTEM)
      {
        //if new or gone device handle
        GUID guid = null;
        int count = 0;

        switch (((SystemEventId)eventId).getBase())
        {
          // We use the active devices for ing events
          case ConstSystemEventType.GUID_LIST_READY:
          {
            // Create input stream
            HaviByteArrayInputStream hbais = new HaviByteArrayInputStream(eventInfo);
            
            // Read the size
            int length = hbais.readInt();

            // Allocate the array
            GUID[] guids = new GUID[length];

            // Read the guids
            for (int i = 0; i < guids.length; i++)
            {
              guids[i] = new GUID(hbais);
            }

            // Convert to SEIDs
            SEID[] seids = new SEID[guids.length - 1];
            int position = 0;
            for (int i = 0; i < guids.length; i++)
            {
              // Check for local guid
              if (!guids[i].equals(getSoftwareElement().getSeid().getGuid()))
              {
                seids[position++] = new SEID(guids[i], ConstSoftwareElementHandle.EVENT_MANAGER);
              }
            }

            // Save to active devices
            activeDevices.set(seids);

            break;
          }

          // Message Leave Event
          case ConstSystemEventType.MSG_LEAVE:
          {
            // Extract SEID
            HaviByteArrayInputStream hbias = new HaviByteArrayInputStream(eventInfo);
            SEID goneSeid = new SEID(hbias);

            // Remove from maps
            flush(goneSeid);
                
            // All done
            break;
          }
        }
      }
    }
    catch (IOException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "spy", e.toString());
    }
    catch (HaviUnmarshallingException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "spy", e.toString());
    }
  }

  private void flush(SEID seid)
  {
    try
    {
      table.remove(seid);
    }
    catch (HaviEventManagerNotFoundException e)
    {
      // Ignore
    }
  }
}