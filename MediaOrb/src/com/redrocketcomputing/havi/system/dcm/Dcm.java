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
 * $Id: Dcm.java,v 1.2 2005/03/16 04:24:06 stephen Exp $
 */
package com.redrocketcomputing.havi.system.dcm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.havi.dcm.constants.ConstFCAssigner;
import org.havi.dcm.constants.ConstTargetType;
import org.havi.dcm.rmi.DcmServerHelper;
import org.havi.dcm.rmi.DcmSkeleton;
import org.havi.dcm.rmi.DcmSystemEventManagerClient;
import org.havi.dcm.types.ByteRow;
import org.havi.dcm.types.ContentIconRef;
import org.havi.dcm.types.ControlCapability;
import org.havi.dcm.types.DcmConnectionList;
import org.havi.dcm.types.DcmHavletCodeUnitProfile;
import org.havi.dcm.types.DcmPlugCount;
import org.havi.dcm.types.DeviceIcon;
import org.havi.dcm.types.HUID;
import org.havi.dcm.types.HaviDcmException;
import org.havi.dcm.types.HaviDcmLocalException;
import org.havi.dcm.types.HaviDcmNoProtException;
import org.havi.dcm.types.HaviDcmNotImplementedException;
import org.havi.dcm.types.HaviDcmUnidentifiedFailureException;
import org.havi.dcm.types.TargetId;
import org.havi.fcm.rmi.FcmSystemEventManagerClient;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstAttributeName;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.Attribute;
import org.havi.system.types.EventId;
import org.havi.system.types.FcmPlug;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.IecPlug;
import org.havi.system.types.Plug;
import org.havi.system.types.PlugStatus;
import org.havi.system.types.SEID;
import org.havi.system.types.StreamType;
import org.havi.system.types.StreamTypeId;
import org.havi.system.types.TransmissionFormat;
import org.havi.system.types.VendorId;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.appframework.service.AbstractService;
import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;
import com.redrocketcomputing.havi.system.rmi.EventNotificationListener;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.havi.system.rmi.RemoteInvocationInformation;
import com.redrocketcomputing.havi.system.rmi.RemoteServerHelperTask;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Dcm extends AbstractService implements DcmSkeleton, VersionSkeleton
{
  private DcmServerHelper serverHelper = null;
  private VersionServerHelper versionServerHelper = null;
  private HUID huid;
  private boolean isRegistered = false;
  private List fcmSeidList = new ArrayList();
  private EventManagerNotificationServerHelper eventServer = null;
  private MsgWatchOnNotificationHelper watchHelper = null;

  protected String userPreferredName = "";
  protected SoftwareElement softwareElement = null;
  protected boolean powerStateControlSupported = false;
  protected boolean powerState = false;
  
  /**
   * @param instanceName
   */
  public Dcm(String instanceName, TargetId targetId, short interfaceId, VendorId vendorId, boolean n1Uniqueness, int n2Assigner) throws HaviDcmException
  {
    // Forward to super class
    super(instanceName);
    
    // Build HUID
    huid = new HUID(targetId, interfaceId, vendorId, n1Uniqueness, n2Assigner);
  }

  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#start()
   */
  public void start()
  {
    // Check for running
    if (getServiceState() != Service.IDLE)
    {
      // Bad
      throw new ServiceException("already started");
    }
    
    try
    {
      // Create the system software element
      softwareElement = new SoftwareElement();
      
      // Create and bind server helpers
      serverHelper = new DcmServerHelper(softwareElement, this);
      versionServerHelper = new VersionServerHelper(softwareElement, this);
      softwareElement.addHaviListener(serverHelper);
      softwareElement.addHaviListener(versionServerHelper);
      eventServer = new EventManagerNotificationServerHelper(softwareElement);
      watchHelper = new MsgWatchOnNotificationHelper(softwareElement);

      // Mark for invocation information
      serverHelper.setThreadLocal(true);
    }
    catch (HaviDcmException e)
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
   * @see com.redrocketcomputing.appframework.service.Service#terminate()
   */
  public void terminate()
  {
    // Check for running
    if (getServiceState() != Service.RUNNING)
    {
      // Bad
      throw new ServiceException("not started");
    }
    
    try
    {
      // Unbind server helpers
      eventServer.close();
      watchHelper.close();
      versionServerHelper.close();
      serverHelper.close();
      
      // Close the software element
      softwareElement.close();
      softwareElement = null;
      serverHelper = null;
      versionServerHelper = null;
      fcmSeidList = null;
    }
    catch (HaviException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
  }


  /* (non-Javadoc)
   * @see com.redrocketcomputing.appframework.service.Service#info(java.io.PrintStream, java.lang.String[])
   */
  public void info(PrintStream printStream, String[] arguments)
  {
    printStream.println("not implements");
  }
  
  public SEID getSeid()
  {
    return softwareElement.getSeid();
  }

  /**
   * Add SystemEvent subscriptions
   * @param eventId The SystemEventId of the event to subscribe to
   * @param listener The EventNotificationListener to receive the event
   * @throws HaviException The if a problem subscribing to the event is detected
   */
  public final void addEventSubscription(EventId eventId, EventNotificationListener listener) throws HaviException
  {
    // Forward
    eventServer.addEventSubscription(eventId, listener);
  }

  /**
   * Remove the event subscriptions
   * @param eventId The EventId to remove
   * @param listener The matching listener;
   */
  public final void removeEventSubscription(EventId eventId, EventNotificationListener listener)
  {
    // Forward
    eventServer.removeEventSubscription(eventId, listener);
  }

  /**
   * Add a listener for the specified event. The listener must be of the correct type for the event or a IllegalArgumentException
   * will be thrown
   * @param eventId The event ID to listener on
   * @param listener The event listener
   */
  public final void addMsgWatch(SEID targetSeid, MsgWatchOnNotificationListener listener) throws HaviException
  {
    // Forward
    watchHelper.addListenerEx(targetSeid, listener);
  }

  /**
   * Remove the listener for the specified event.
   * @param eventId The event ID to which the listener is bound
   * @param listener The listener to remove
   */
  public final void removeMsgWatch(SEID targetSeid, MsgWatchOnNotificationListener listener)
  {
    // Forward
    watchHelper.removeListenerEx(targetSeid, listener);
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getHuid()
   */
  public HUID getHuid() throws HaviDcmException
  {
    return huid;
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getFcmCount()
   */
  public short getFcmCount() throws HaviDcmException
  {
    return (short)(fcmSeidList.size() & 0xffff);
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getFcmSeidList()
   */
  public SEID[] getFcmSeidList() throws HaviDcmException
  {
    // Return the array
    return (SEID[])fcmSeidList.toArray(new SEID[fcmSeidList.size()]);
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getUserPreferredName()
   */
  public String getUserPreferredName() throws HaviDcmException
  {
    return userPreferredName;
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#setUserPreferredName(java.lang.String)
   */
  public void setUserPreferredName(String name) throws HaviDcmException
  {
    try
    {
      // Save name
      userPreferredName = name;
      
      // Modify registration
      if (isRegistered)
      {
        // Modify registration
        register();
      }
      
      // Fire user preferred named changed event
      DcmSystemEventManagerClient client = new DcmSystemEventManagerClient(softwareElement);
      client.fireUserPreferredNameChangedSync(name);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviDcmUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getPowerState()
   */
  public boolean getPowerState() throws HaviDcmException
  {
    return powerState;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.DcmSkeleton#setPowerState(boolean)
   */
  public boolean setPowerState(boolean powerState) throws HaviDcmException
  {
    try
    {
      // Check to see if the power state control is enable
      if (!powerStateControlSupported)
      {
        throw new HaviDcmNotImplementedException("setPowerState");
      }
      
      // Only change if required
      if (this.powerState != powerState)
      {
        // Change power state
        this.powerState = powerState;
        
        // Fire the power state changed event
        DcmSystemEventManagerClient client = new DcmSystemEventManagerClient(softwareElement);
        client.firePowerStateChangedSync(powerState);
      }

      // Return new power state
      return powerState;
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviDcmUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#nativeCommand(int, org.havi.dcm.types.ByteRow)
   */
  public ByteRow nativeCommand(int protocol, ByteRow command) throws HaviDcmException
  {
    // Not supported
    throw new HaviDcmNoProtException("protocol: " + protocol);
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getHavletCodeUnit(int, int)
   */
  public byte[] getHavletCodeUnit(int firstByte, int lastByte) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getPlugCount(int)
   */
  public DcmPlugCount getPlugCount(int type) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getPlugStatus(org.havi.system.types.Plug)
   */
  public PlugStatus getPlugStatus(Plug plug) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#connect(org.havi.system.types.SEID, org.havi.system.types.Plug, org.havi.system.types.Plug)
   */
  public void connect(SEID caller, Plug src, Plug dest) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#disconnect(org.havi.system.types.SEID, org.havi.system.types.Plug, org.havi.system.types.Plug)
   */
  public void disconnect(SEID caller, Plug src, Plug dest) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getConnectionList(org.havi.system.types.Plug)
   */
  public DcmConnectionList getConnectionList(Plug plug) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getChannelIUsage(org.havi.system.types.IecPlug)
   */
  public short getChannelIUsage(IecPlug plug) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getPlugUsage(short)
   */
  public IecPlug[] getPlugUsage(short channel) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#setIecBandwidthAllocation(org.havi.system.types.SEID, org.havi.system.types.IecPlug, int)
   */
  public void setIecBandwidthAllocation(SEID caller, IecPlug plug, int maxBandwidth) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#iecSprayOut(org.havi.system.types.SEID, org.havi.system.types.IecPlug, short, short)
   */
  public void iecSprayOut(SEID caller, IecPlug plug, short channel, short playload) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#iecTapIn(org.havi.system.types.SEID, org.havi.system.types.IecPlug, short)
   */
  public void iecTapIn(SEID caller, IecPlug plug, short isocChannel) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getSupportedTransmissionFormats(org.havi.system.types.StreamType, org.havi.system.types.Plug)
   */
  public TransmissionFormat[] getSupportedTransmissionFormats(StreamType type, Plug plug) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getTransmissionFormat(org.havi.system.types.Plug)
   */
  public TransmissionFormat getTransmissionFormat(Plug plug) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getTransmissionFormat(org.havi.system.types.SEID, org.havi.system.types.Plug, org.havi.system.types.TransmissionFormat)
   */
  public void getTransmissionFormat(SEID caller, Plug plug, TransmissionFormat format) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getContentIconList(int)
   */
  public ContentIconRef[] getContentIconList(int type) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#selectContent(int, int, boolean, org.havi.system.types.FcmPlug)
   */
  public void selectContent(int contentType, int handle, boolean dynamicBw, FcmPlug sink) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#stopContent(int, int)
   */
  public void stopContent(int contentType, int handle) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getAvailableStreamTypes(org.havi.system.types.Plug, org.havi.system.types.Plug)
   */
  public StreamType[] getAvailableStreamTypes(Plug DcmPlug, Plug FcmPlug) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getStreamType(org.havi.system.types.Plug)
   */
  public StreamType getStreamType(Plug DcmPlug) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#setStreamTypeId(org.havi.system.types.SEID, org.havi.system.types.Plug, org.havi.system.types.StreamTypeId)
   */
  public void setStreamTypeId(SEID caller, Plug DcmPlug, StreamTypeId typeId) throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getHavletCodeUnitProfile()
   */
  public abstract DcmHavletCodeUnitProfile getHavletCodeUnitProfile() throws HaviDcmException;

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getDeviceIcon()
   */
  public abstract DeviceIcon getDeviceIcon() throws HaviDcmException;

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getDeviceClass()
   */
  public abstract int getDeviceClass() throws HaviDcmException;

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getDeviceManufacturer()
   */
  public abstract String getDeviceManufacturer() throws HaviDcmException;

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getControlCapability()
   */
  public abstract ControlCapability getControlCapability() throws HaviDcmException;

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public abstract String getVersion() throws HaviVersionException;

  public abstract String getSoftwareElementManufacturer();

  public abstract int getGuiReq();

  public abstract String getSoftwareElementVersion();

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#addVirtualFcm()
   */
  public HUID addVirtualFcm() throws HaviDcmException
  {
    // Get invocation information
    RemoteInvocationInformation invocation = RemoteServerHelperTask.getInvocationInformation();
    
    try
    {
      // Add to the fcm seid list
      fcmSeidList.add(invocation.getSourceSeid());
      
      // Query registry
      RegistryClient client = new RegistryClient(softwareElement);
      Attribute[] attributes = client.retrieveAttributesSync(0, invocation.getSourceSeid());
      SimpleAttributeTable attributeTable = new SimpleAttributeTable(attributes);
      
      // Verify InterfaceId and VendorId are set
      if (!attributeTable.isValid(ConstAttributeName.ATT_INTERFACE_ID) || !attributeTable.isValid(ConstAttributeName.ATT_VENDOR_ID))
      {
        // Bad
        throw new HaviDcmLocalException("bad registration");
      }
      
      // Create new HUID
      TargetId targetId = new TargetId(ConstTargetType.FCM_NON61883, huid.getTargetId().getGuid(), huid.getTargetId().getN1(), (short)((fcmSeidList.size() - 1)& 0xffff));
      HUID huid = new HUID(targetId, attributeTable.getInterfaceId(), attributeTable.getVendorId(), true, ConstFCAssigner.DCM);
      
      // Set the SoftwareElementType and HUID attriubtes
      attributeTable.setSoftwareElementType(ConstSoftwareElementType.GENERIC_FCM);
      attributeTable.setHuid(huid);
      client.registerElementSync(0, invocation.getSourceSeid(), attributeTable.toAttributeArray());
      
      // Return new HUID
      return huid;
    }
    catch (HaviMsgException e)
    {
      // Remove from seid list
      fcmSeidList.remove(fcmSeidList.size() - 1);
      
      // Translate
      throw new HaviDcmUnidentifiedFailureException(e.toString());
    }
    catch (HaviRegistryException e)
    {
      // Remove from seid list
      fcmSeidList.remove(fcmSeidList.size() - 1);
      
      // Translate
      throw new HaviDcmUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#removeVirtualFcm()
   */
  public void removeVirtualFcm() throws HaviDcmException
  {
    // Get invocation information
    RemoteInvocationInformation invocation = RemoteServerHelperTask.getInvocationInformation();
    
    // Find the index of the FCM
    int index = fcmSeidList.indexOf(invocation.getSourceSeid());
    if (index != -1)
    {
      // Ignore
      return;
    }
    
    // Set the FCM to null
    fcmSeidList.set(index, SEID.ZERO);
  }

  /**
   * Publish the application module by registering with the network
   * @throws HaviException
   */
  public void register() throws HaviException
  {
    // Build attribute table
    SimpleAttributeTable attributeTable = new SimpleAttributeTable();
    attributeTable = new SimpleAttributeTable();
    attributeTable.setHuid(huid);
    attributeTable.setTargetId(huid.getTargetId());
    attributeTable.setSoftwareElementType(ConstSoftwareElementType.DCM);
    attributeTable.setInterfaceId(huid.getInterfaceId());
    attributeTable.setVendorId(huid.getVendorId());
    attributeTable.setDeviceClass(getDeviceClass());
    attributeTable.setUserPreferredName(userPreferredName);
    attributeTable.setSoftwareElementManufacturer(getSoftwareElementManufacturer());
    attributeTable.setDeviceManufacturer(getDeviceManufacturer());
    attributeTable.setGuiReq(getGuiReq());
    attributeTable.setSoftwareElementVersion(getSoftwareElementVersion());

    // Create registry client
    RegistryClient client = new RegistryClient(softwareElement);
    
    // Register
    client.registerElementSync(0, softwareElement.getSeid(), attributeTable.toAttributeArray());
    
    // Mark as registered
    isRegistered = true;
  }
  
  /**
   * Unpublish the application module by unregistering with the network 
   * @throws HaviException
   */
  public void unregister() throws HaviException
  {
    // Ensure registered
    if (isRegistered)
    {
      // Create registry client
      RegistryClient client = new RegistryClient(softwareElement);
      
      // Unregister
      client.unregisterElement(softwareElement.getSeid());
    }
  }

  protected HUID addFcm(SEID seid, short interfaceId, VendorId vendorId) throws HaviFcmUnidentifiedFailureException
  {
    // Create new target 
    TargetId targetId = new TargetId(ConstTargetType.FCM_NON61883, huid.getTargetId().getGuid(), huid.getTargetId().getN1(), (short)(fcmSeidList.size() & 0xffff));
    
    // Add to list
    fcmSeidList.add(seid);
    
    // Return it
    return new HUID(targetId, interfaceId, vendorId, true, ConstFCAssigner.DCM);
  }
  
  protected void removeFcm(SEID seid)
  {
    // Find the index of the FCM
    int index = fcmSeidList.indexOf(seid);
    if (index != -1)
    {
      // Ignore
      return;
    }
    
    // Set the FCM to null
    fcmSeidList.set(index, SEID.ZERO);
  }
  
  protected boolean isOwner(SEID seid)
  {
    return fcmSeidList.indexOf(seid) != -1;
  }
}
