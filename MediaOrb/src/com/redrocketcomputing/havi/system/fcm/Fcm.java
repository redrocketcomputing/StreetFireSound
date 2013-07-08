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
 * $Id: Fcm.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */
package com.redrocketcomputing.havi.system.fcm;

import org.havi.dcm.types.ByteRow;
import org.havi.dcm.types.HUID;
import org.havi.dcm.types.HaviDcmException;
import org.havi.fcm.rmi.FcmServerHelper;
import org.havi.fcm.rmi.FcmSkeleton;
import org.havi.fcm.rmi.FcmSystemEventManagerClient;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.HaviFcmNotImplementedException;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.fcm.types.PlugCount;
import org.havi.fcm.types.SubscribeNotification;
import org.havi.system.SoftwareElement;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.IecPlug;
import org.havi.system.types.InternalPlug;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.StreamType;
import org.havi.system.version.rmi.VersionServerHelper;
import org.havi.system.version.rmi.VersionSkeleton;

import com.redrocketcomputing.havi.system.dcm.Dcm;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.RemoteInvocationInformation;
import com.redrocketcomputing.havi.system.rmi.RemoteServerHelperTask;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Fcm implements FcmSkeleton, VersionSkeleton
{
  private FcmServerHelper serverHelper;
  private VersionServerHelper versionServerHelper;
  private boolean isRegistered = false;
  private HUID huid;
  private AttributeNotifier attributeNotifier;
  
  protected Dcm dcm;
  protected SoftwareElement softwareElement;
  protected boolean powerStateControlSupported = false;
  protected boolean powerState = false;

  /**
   * 
   */
  public Fcm(Dcm dcm) throws HaviFcmException
  {
    try
    {
      // Save parameters
      this.dcm = dcm;
      
      // Create a software element
      softwareElement = new SoftwareElement();
      
      // Create and bind server helpers
      serverHelper = new FcmServerHelper(softwareElement, this);
      versionServerHelper = new VersionServerHelper(softwareElement, this);
      softwareElement.addHaviListener(serverHelper);
      softwareElement.addHaviListener(versionServerHelper);
      
      // Mark for invocation information
      serverHelper.setThreadLocal(true);
      
      // Create Attribute notifier
      attributeNotifier = new AttributeNotifier(softwareElement);
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
    catch (HaviMsgListenerExistsException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }
  
  /**
   * Release all resources
   */
  public void close()
  {
    try
    {
      // Unregister
      unregister();
      
      // Close the attribute notifier
      attributeNotifier.close();
      
      // Close the software element
      softwareElement.close();
    }
    catch (HaviException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
  }
  
  public final SEID getSeid()
  {
    return softwareElement.getSeid();
  }
  
  public final void setHuid(HUID huid)
  {
    this.huid = huid;
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getHuid()
   */
  public HUID getHuid() throws HaviFcmException
  {
    return huid;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getDcmSeid()
   */
  public SEID getDcmSeid() throws HaviFcmException
  {
    return dcm.getSeid();
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getPowerState()
   */
  public boolean getPowerState() throws HaviFcmException
  {
    return powerState;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#setPowerState(boolean)
   */
  public boolean setPowerState(boolean powerState) throws HaviFcmException
  {
    try
    {
      // Check to see if the power state control is enable
      if (!powerStateControlSupported)
      {
        throw new HaviFcmNotImplementedException("setPowerState");
      }
      
      // Only change if required
      if (this.powerState != powerState)
      {
        // Change power state
        this.powerState = powerState;
        
        // Fire the power state changed event
        FcmSystemEventManagerClient client = new FcmSystemEventManagerClient(softwareElement);
        client.firePowerStateChangedSync(powerState);
      }
      
      // Return new power state
      return powerState;
    }
    catch (HaviException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#nativeCommand(int, org.havi.dcm.types.ByteRow)
   */
  public ByteRow nativeCommand(int protocol, ByteRow command) throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException("nativeCommand");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#subscribeNotification(org.havi.system.types.SEID, short, org.havi.fcm.types.FcmAttributeValue, short, org.havi.system.types.OperationCode)
   */
  public SubscribeNotification subscribeNotification(short attributeIndicator, byte[] value, short comparator, OperationCode opCode) throws HaviFcmException
  {
    // Get invocation information
    RemoteInvocationInformation invocationInformation = RemoteServerHelperTask.getInvocationInformation();
    
    // Add subscription
    short notificationId = attributeNotifier.addSubscription(invocationInformation.getSourceSeid(), opCode, attributeIndicator, comparator, value);

    // Create the FcmAttributeValue
    byte[] fcmAttributeValue = getAttributeValue(attributeIndicator);

    // Return the subscription
    return new SubscribeNotification(fcmAttributeValue, notificationId);
  }

  protected abstract byte[] getAttributeValue(short attributeIndicator) throws HaviFcmException;

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#unsubscribeNotification(short)
   */
  public void unsubscribeNotification(short notificationId) throws HaviFcmException
  {
    // Forward to the AttributeNotifier
    attributeNotifier.removeSubscription(notificationId);
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getPlugCount()
   */
  public PlugCount getPlugCount() throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getSupportedStreamTypes(short, int)
   */
  public StreamType[] getSupportedStreamTypes(short plugNum, int direction) throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#wink()
   */
  public void wink() throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException("wink");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#unwink()
   */
  public void unwink() throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException("unwink");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#canWink()
   */
  public boolean canWink() throws HaviFcmException
  {
    return false;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getWorstCaseStartupTime()
   */
  public int getWorstCaseStartupTime() throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException("getWorstCaseStartupTime");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#setPlugSharing(short, boolean)
   */
  public void setPlugSharing(short plugNum, boolean canShare) throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#iecAttach(org.havi.system.types.IecPlug, org.havi.system.types.InternalPlug)
   */
  public void iecAttach(IecPlug pcr, InternalPlug plug) throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException("iecAttach");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#iecDetach(org.havi.system.types.IecPlug, org.havi.system.types.InternalPlug)
   */
  public void iecDetach(IecPlug pcr, InternalPlug plug) throws HaviFcmException
  {
    throw new HaviFcmNotImplementedException("iecDetach");
  }

  public String getDeviceModel()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public abstract String getVersion() throws HaviVersionException;

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getDeviceClass()
   */
  public abstract int getDeviceClass() throws HaviDcmException;

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getFcmType()
   */
  public abstract int getFcmType() throws HaviFcmException;

  public abstract String getSoftwareElementManufacturer();
  public abstract String getDeviceManufacturer();
  
  /**
   * Publish the application module by registering with the network
   * @throws HaviException
   */
  public void register() throws HaviException
  {
    // Initialize attribute table
    SimpleAttributeTable attributeTable = new SimpleAttributeTable();
    attributeTable.setHuid(huid);
    attributeTable.setTargetId(huid.getTargetId());
    attributeTable.setSoftwareElementType(getFcmType());
    attributeTable.setInterfaceId(huid.getInterfaceId());
    attributeTable.setVendorId(huid.getVendorId());
    attributeTable.setDeviceManufacturer(getDeviceManufacturer());
    attributeTable.setSoftwareElementManufacturer(getSoftwareElementManufacturer());
    attributeTable.setDeviceClass(getDeviceClass());
    
    // Set optional attributes
    String model = getDeviceModel();
    if (model != null)
    {
      attributeTable.setDeviceModel(model);
    }

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
  
  protected final void updateAttribute(short indicator, byte[] attributeValue)
  {
    // Forward
    attributeNotifier.updateAttribute(indicator, attributeValue);
  }
}