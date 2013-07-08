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
 * $Id: Registry.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rg;

import java.io.PrintStream;
import java.util.Set;

import org.havi.dcm.types.HUID;
import org.havi.system.cmmip.rmi.GuidListReadyEventNotificationListener;
import org.havi.system.constants.ConstApiCode;
import org.havi.system.constants.ConstSoftwareElementHandle;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.constants.ConstVendorEventType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.registry.rmi.RegistryServerHelper;
import org.havi.system.registry.rmi.RegistrySkeleton;
import org.havi.system.registry.rmi.RegistrySystemEventManagerClient;
import org.havi.system.registry.rmi.RegistryVendorEventManagerClient;
import org.havi.system.types.Attribute;
import org.havi.system.types.ComplexQuery;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviEventManagerException;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.HaviRegistryIdentifierException;
import org.havi.system.types.HaviRegistryLocationException;
import org.havi.system.types.HaviRegistryNetworkException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.Query;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.SimpleQuery;
import org.havi.system.types.SystemEventId;
import org.havi.system.types.VendorEventId;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.constants.ConstMediaOrbRelease;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.havi.system.rmi.RemoteInvocationInformation;
import com.redrocketcomputing.havi.system.rmi.RemoteServerHelperTask;
import com.redrocketcomputing.havi.system.service.SystemService;
import com.redrocketcomputing.util.concurrent.Mutex;
import com.redrocketcomputing.util.concurrent.SynchronizedRef;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * Implementation of RegistryService. Use remotequery object to perform any remote query.
 * 
 * @author george
 *  
 */
public class Registry extends SystemService implements RegistrySkeleton, GuidListReadyEventNotificationListener, MsgWatchOnNotificationListener, VersionSkeleton
{
  private final static OperationCode WATCH_OPCODE = new OperationCode(ConstApiCode.REGISTRY, (byte)0xf0);
  private final static OperationCode EVENT_OPCODE = new OperationCode(ConstApiCode.REGISTRY, (byte)0xf1);

  private SynchronizedRef remoteGuids = new SynchronizedRef(new GUID[0], new Mutex());
  private RegistryDatabase database = null;

  private RegistryServerHelper serverHelper = null;
  private VersionServerHelper versionServerHelper = null;
  private QuerySubscriptionTable subscriptionTable = null;
  private RegistrySystemEventManagerClient registrySystemEventManagerClient = null;
  private RegistryVendorEventManagerClient registryVendorEventManagerClient = null;
  private MsgWatchOnNotificationHelper watchHelper = null;
  private EventManagerNotificationServerHelper eventNotificationServer = null;

  public Registry(String instanceName)
  {
    // Forward
    super(instanceName, ConstSoftwareElementType.REGISTRY);
  }

  /**
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {

    // Check service state
    if (getServiceState() != Service.IDLE)
    {
      throw new ServiceException("service is not idle");
    }

    try
    {
      // Forward to super class
      super.start();

      // Create and bind the watch and event helpers to the software element
      watchHelper = new MsgWatchOnNotificationHelper(getSoftwareElement(), WATCH_OPCODE);
      eventNotificationServer = new EventManagerNotificationServerHelper(getSoftwareElement(), EVENT_OPCODE);
      
      // Create support components
      database = new RegistryDatabase();
      subscriptionTable = new QuerySubscriptionTable(getSoftwareElement(), watchHelper);
      registrySystemEventManagerClient = new RegistrySystemEventManagerClient(getSoftwareElement());
      registryVendorEventManagerClient = new RegistryVendorEventManagerClient(getSoftwareElement(), ConstStreetFireVendorInformation.VENDOR_ID);

      // Bind to the event listeners
      eventNotificationServer.addEventSubscription(new SystemEventId(ConstSystemEventType.GUID_LIST_READY), this);
      eventNotificationServer.addEventSubscription(new VendorEventId(ConstVendorEventType.NEW_SOFTWARE_ELEMENT_ATTRIBUTES, ConstStreetFireVendorInformation.VENDOR_ID), subscriptionTable);

      // Create the server helper
      serverHelper = new RegistryServerHelper(getSoftwareElement(), this);
      getSoftwareElement().addHaviListener(serverHelper);
      versionServerHelper = new VersionServerHelper(getSoftwareElement(), this);
      getSoftwareElement().addHaviListener(versionServerHelper);

      // Turn on multi-threading model
      serverHelper.setMultiThread(true);
      serverHelper.setThreadLocal(true);

      // Mark service as started
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

  /**
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Check state
    if (getServiceState() != Service.RUNNING)
    {
      // Ummm
      throw new ServiceException("service is not running");
    }

    // Close all componets
    subscriptionTable.close();
    watchHelper.close();
    eventNotificationServer.close();

    // Unbind helpers
    getSoftwareElement().removeHaviListener(versionServerHelper);
    getSoftwareElement().removeHaviListener(serverHelper);

    // Terminate parent
    super.terminate();

    // Release components
    database = null;
    subscriptionTable = null;
    serverHelper = null;
    versionServerHelper = null;
    watchHelper = null;
    eventNotificationServer = null;

    // Change service state
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

    // Print the local guid
    printStream.println("Local " + getSoftwareElement().getSeid().getGuid().toString());
    printStream.println();

    // Print the know GUIDS
    GUID[] knownGuids = (GUID[])remoteGuids.get();
    printStream.print("Known: ");
    for (int i = 0; i < knownGuids.length; i++)
    {
      printStream.print(knownGuids[i].toString() + ' ');
    }
    printStream.println();

    // Print the database
    SEID[] seids = database.getSeids();
    printStream.println("Entry size: " + seids.length);
    printStream.println();

    for (int i = 0; i < seids.length; i++)
    {
      Attribute[] attList = database.get(seids[i]);
      printStream.println("Entry " + i + " - " + seids[i] + " Number of attributes: " + attList.length);
      for (int j = 0; j < attList.length; j++)
      {
        printStream.println("     " + SimpleAttributeTable.toAttributeClass(attList[j]).toString());
      }
    }
  }

  /**
   * Registers element if new, otherwise updates Attributes. If the element is new and it is not a system component then all listeners are notified.
   */
  public void registerElement(SEID seid, Attribute[] table) throws HaviRegistryException
  {
    // Check parameters
    if (seid == null || table == null)
    {
      // Badness
      throw new IllegalArgumentException("seid or table is null");
    }

    try
    {
      // Verify that the seid being register is local
      if (!getSoftwareElement().getSeid().getGuid().equals(seid.getGuid()))
      {
        // Opps
        throw new HaviRegistryLocationException(seid.toString());
      }

      // Check for new registration
      boolean isNew = !database.contains(seid);

      // Add or update the database
      database.put(seid, table);

      // Fire new registration event require
      if (isNew)
      {
        // Add watch
        watchHelper.addListenerEx(seid, this);

        // Try to get an HUID
        HUID huid = database.getHuid(seid);

        boolean hasHuid = true;
        if (huid == null)
        {
          hasHuid = false;
          huid = HUID.ZERO;
        }

        // Fire the events
        registrySystemEventManagerClient.fireNewSoftwareElementSync(seid, hasHuid, huid);
      }

      // Always fire new attribute event
      registryVendorEventManagerClient.fireNewSoftwareElementAttributesSync(seid, table);
    }
    catch (HaviMsgException e)
    {
      // Just log the exception
      LoggerSingleton.logError(this.getClass(), "registerElement", e.toString());
    }
    catch (HaviEventManagerException e)
    {
      // Just log the exception
      LoggerSingleton.logError(this.getClass(), "registerElement", e.toString());
    }
  }

  /**
   * Unregisters a given software element. If the softwrare element is registered, it is removed and the all RegistryListeners are notified
   */
  public void unregisterElement(SEID seid) throws HaviRegistryException
  {
    // Check parameters
    if (seid == null)
    {
      // Badness
      throw new IllegalArgumentException("seid is null");
    }

    // Remove the watch
    watchHelper.removeListenerEx(seid, this);

    // Try to remove the element
    Attribute[] table = database.remove(seid);

    // Check to see if we remove one
    if (table == null)
    {
      // Bad
      throw new HaviRegistryIdentifierException(seid.toString());
    }

    try
    {
      // Fire gone software element
      registrySystemEventManagerClient.fireGoneSoftwareElementSync(seid);
    }
    catch (HaviException ex)
    {
      LoggerSingleton.logError(this.getClass(), "unregisterElement", ex.toString());
    }
  }

  /**
   * Returns Attributes for a given SEID in AttributeSeqHolder if the SEID exists. Otherwise it returns null
   */
  public Attribute[] retrieveAttributes(SEID seid) throws HaviRegistryException
  {
    // Check parameters
    if (seid == null)
    {
      // Badness
      throw new IllegalArgumentException("seid is null");
    }

    // Check for local seid
    if (getSoftwareElement().getSeid().getGuid().equals(seid.getGuid()))
    {
      // Retrieve from the local database
      Attribute[] table = database.get(seid);

      if (table == null)
      {
        // Not found
        throw new HaviRegistryIdentifierException(seid.toString() + " not found");
      }

      // All good
      return table;

    }
    else
    {
      // Remote query
      try
      {
        // Query remote registry
        RegistryClient client = new RegistryClient(getSoftwareElement(), getSoftwareElement().getSystemSeid(seid, ConstSoftwareElementType.REGISTRY));
        Attribute[] table = client.retrieveAttributesSync(0, seid);

        // Check length of the table
        if (table.length == 0)
        {
          // Not found
          throw new HaviRegistryIdentifierException(seid.toString() + " not found");
        }

        return table;
      }
      catch (HaviMsgException e)
      {
        throw new HaviRegistryNetworkException(e.toString());
      }
    }
  }

  /**
   * Queries the local and remote registries Return partial result without throwing exception
   * 
   * @see org.havi.system.rmi.registry.RegistrySkeleton#getElement(SimpleQuery)
   */
  /*
   */
  public QueryResult getElement(Query query) throws HaviRegistryException
  {
    boolean isComplete = true;

    // Get remote invocation information
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();

    // Get local seids which match query
    Set localResults = database.run(query);

    // Check for local query
    if (invocationInformation.getSourceSeid().getGuid().equals(getSoftwareElement().getSeid().getGuid()))
    {
      // Get the list of remote devices
      GUID[] guids = (GUID[])remoteGuids.get();
      
      // Loop through all remote registries
      for (int i = 0; i < guids.length; i++)
      {
        // Make sure we don't query the local registry again
        if (!getSoftwareElement().getSeid().getGuid().equals(guids[i]))
        {
          try
          {
            // Query remote registry
            RegistryClient client = new RegistryClient(getSoftwareElement(), new SEID(guids[i], ConstSoftwareElementHandle.REGISTRY));
            QueryResult result = client.getElementSync(0, query);

            // Merge results
            for (int j = 0; j < result.getSeidList().length; j++)
            {
              // Merge remote result to local result
              localResults.add(result.getSeidList()[j]);
            }
          }
          catch (HaviException e)
          {
            // Log warning
            LoggerSingleton.logWarning(this.getClass(), "getElement", e.toString());
            
            // Mark as incomplete
            isComplete = false;
          }
        }
      }
    }

    // Return merged results
    return new QueryResult((SEID[])localResults.toArray(new SEID[localResults.size()]), isComplete);
  }

  /**
   * @see org.havi.system.registry.rmi.RegistrySkeleton#subscribeComplex(SEID, OperationCode, ComplexQuery)
   */
  public int subscribe(OperationCode opCode, Query query) throws HaviRegistryException
  {
    // Get remote invocation information
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();

    // Forward to the query subscription table
    return subscriptionTable.addSubscription(invocationInformation.getSourceSeid(), opCode, query);
  }

  /**
   * @see org.havi.system.registry.rmi.RegistrySkeleton#unsubscribe(int)
   */
  public void unsubscribe(int queryId) throws HaviRegistryException
  {
    // Get remote invocation information
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();

    // Forward to the query subscription table
    subscriptionTable.removeSubscription(invocationInformation.getSourceSeid(), queryId);
  }

  /**
   * Returns the database.
   * 
   * @return RegistryDatabase
   */
  public RegistryDatabase getDatabase()
  {
    return database;
  }

  /**
   * @see org.havi.system.cmmip.rmi.GuidListReadyEventNotificationListener#guidListReadyEventNotification(GUID[], GUID[], GUID[], GUID[])
   */
  public void guidListReadyEventNotification(SEID posterSeid, GUID[] activeGuidList, GUID[] nonactiveGuidList, GUID[] goneDevices, GUID[] newDevices)
  {
    // Update the remote guid list, byte co
    GUID[] remote = new GUID[activeGuidList.length];
    System.arraycopy(activeGuidList, 0, remote, 0, remote.length);

    // Update the internal list
    remoteGuids.set(remote);
  }

  /**
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#MsgWatchOnNotification(SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    // Flush from the data base
    database.remove(targetSeid);
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
}