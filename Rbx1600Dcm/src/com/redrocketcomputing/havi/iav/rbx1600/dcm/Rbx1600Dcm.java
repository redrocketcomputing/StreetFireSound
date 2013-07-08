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
 * $Id: Rbx1600Dcm.java,v 1.4 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.dcm;

import java.io.IOException;

import org.havi.dcm.rmi.DcmClient;
import org.havi.dcm.types.ControlCapability;
import org.havi.dcm.types.DcmHavletCodeUnitProfile;
import org.havi.dcm.types.DeviceIcon;
import org.havi.dcm.types.HUID;
import org.havi.dcm.types.HaviDcmException;
import org.havi.dcm.types.HaviDcmNotImplementedException;
import org.havi.dcm.types.HaviDcmUnidentifiedFailureException;
import org.havi.dcm.types.TargetId;
import org.havi.fcm.rmi.PowerStateChangedEventNotificationListener;
import org.havi.fcm.types.HaviFcmException;
import org.havi.system.constants.ConstDeviceClass;
import org.havi.system.constants.ConstGuiReq;
import org.havi.system.constants.ConstSystemEventType;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;

import com.redrocketcomputing.appframework.service.Service;
import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.hardware.LedController;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmRelease;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.fcm.amplifier.AmplifierFcm;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.amplifier.Rbx1600AmplifierFcm;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.SonyJukeboxFcm;
import com.redrocketcomputing.havi.system.dcm.Dcm;
import com.redrocketcomputing.rbx1600.devicecontroller.IrDevice;
import com.redrocketcomputing.rbx1600.devicecontroller.PowerSwitchDevice;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Rbx1600Dcm extends Dcm implements PowerStateChangedEventNotificationListener
{
  private AmplifierFcm amplifier = null;
  private SonyJukeboxFcm[] jukeboxes = {null, null, null, null};
  private JukeboxDeviceMonitor deviceMonitor = null;
  private LedController ledController = null;
  private DcmCommandFactory commandFactory = null;
  
  /**
   * @param targetId
   * @param n1Uniqueness
   * @param n2Assigner
   * @throws HaviDcmException
   */
  public Rbx1600Dcm(String instanceName, TargetId targetId, boolean n1Uniqueness, int n2Assigner) throws HaviException
  {
    // Contruct super class
    super(instanceName, targetId, ConstRbx1600DcmInterfaceId.RBX1600_DCM, ConstStreetFireVendorInformation.VENDOR_ID, n1Uniqueness, n2Assigner);
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
      // Forward to super class
      super.start();
      
      // Subscribe to events
      addEventSubscription(new SystemEventId(ConstSystemEventType.POWER_STATE_CHANGED), this);
      
      // Enable power control
      powerStateControlSupported = true;
      ledController = new LedController();
      ledController.setPower(LedController.POWER_OFF);
      
      // Create command factory and bind to the power switch device and ir device
      PowerSwitchDevice powerSwitch = (PowerSwitchDevice)ServiceManager.getInstance().get(PowerSwitchDevice.class);
      IrDevice ir = (IrDevice)ServiceManager.getInstance().get(IrDevice.class);
      commandFactory = new DcmCommandFactory(new DcmClient(softwareElement, softwareElement.getSeid()));
      powerSwitch.addFactory(commandFactory);
      ir.addFactory(commandFactory);
      
      // Create Amplifiler FCM
      amplifier = new Rbx1600AmplifierFcm(this);
      HUID amplifierHuid = addFcm(amplifier.getSeid(), ConstRbx1600DcmInterfaceId.RBX1600_AMPLIFIER_FCM, ConstStreetFireVendorInformation.VENDOR_ID);
      amplifier.setHuid(amplifierHuid);
      
      // Create jukebox device monitor
      deviceMonitor = new JukeboxDeviceMonitor();
      
      // Install current jukeboxes
      installJukeboxes();
      
      // Register
      register();
      registerFcms();
      
      // Mark as running
      setServiceState(Service.RUNNING);
      
      // Log start
      LoggerSingleton.logInfo(this.getClass(), "Rbx1600Dcm", "start on " + softwareElement.getSeid());
    }
    catch (HaviException e)
    {
      // Translate
      throw new ServiceException(e.toString());
    }
    catch (IOException e)
    {
      
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
      // Unscribe to events
      removeEventSubscription(new SystemEventId(ConstSystemEventType.POWER_STATE_CHANGED), this);
      
      // Unregister
      unregister();
      unregisterFcms();
    }
    catch (HaviException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "close", e.toString());
    }
    
    // Unbind from the power switch device
    PowerSwitchDevice powerSwitch = (PowerSwitchDevice)ServiceManager.getInstance().get(PowerSwitchDevice.class);
    IrDevice ir = (IrDevice)ServiceManager.getInstance().get(IrDevice.class);
    powerSwitch.removeFactory(commandFactory);
    ir.removeFactory(commandFactory);
    commandFactory = null;
    
    // Close up
    amplifier.close();
    amplifier = null;
    for (int i = 0; i < jukeboxes.length; i++)
    {
      jukeboxes[i].close();
      jukeboxes[i] = null;
    }

    // Mark as idle
    setServiceState(Service.IDLE);
  }

  
  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#setPowerState(boolean)
   */
  public boolean setPowerState(boolean powerState) throws HaviDcmException
  {
    try
    {
      // Change Jukebox FCM power state
      for (int i = 0; i < jukeboxes.length; i++)
      {
        // If present change power state
        if (jukeboxes[i] != null)
        {
          jukeboxes[i].setPowerState(powerState);
        }
      }
      
      // Forward to super class
      super.setPowerState(powerState);
      
      // Change LED state
      ledController.setPower(powerState ? LedController.POWER_ON : LedController.POWER_OFF);
      
      // Return new power state
      return powerState;
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviDcmUnidentifiedFailureException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviDcmUnidentifiedFailureException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getHavletCodeUnitProfile()
   */
  public DcmHavletCodeUnitProfile getHavletCodeUnitProfile() throws HaviDcmException
  {
    // Not implemented
    throw new HaviDcmNotImplementedException();
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getDeviceIcon()
   */
  public DeviceIcon getDeviceIcon() throws HaviDcmException
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getDeviceClass()
   */
  public int getDeviceClass() throws HaviDcmException
  {
    return ConstDeviceClass.IAV;
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getDeviceManufacturer()
   */
  public String getDeviceManufacturer() throws HaviDcmException
  {
    return ConstStreetFireVendorInformation.MANUFACTURER;
  }

  /* (non-Javadoc)
   * @see org.havi.dcm.rmi.DcmSkeleton#getControlCapability()
   */
  public ControlCapability getControlCapability() throws HaviDcmException
  {
    return new ControlCapability(true, true);
  }

  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    return ConstRbx1600DcmRelease.getRelease();
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.dcm.Dcm#getSoftwareElementManufacturer()
   */
  public String getSoftwareElementManufacturer()
  {
    return ConstStreetFireVendorInformation.MANUFACTURER;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.dcm.Dcm#getGuiReq()
   */
  public int getGuiReq()
  {
    return ConstGuiReq.NO_GUI;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.dcm.Dcm#getSoftwareElementVersion()
   */
  public String getSoftwareElementVersion()
  {
    return ConstRbx1600DcmRelease.getRelease();
  }

  
  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.PowerStateChangedEventNotificationListener#powerStateChangedEventNotification(org.havi.system.types.SEID, boolean)
   */
  public void powerStateChangedEventNotification(SEID posterSeid, boolean powerState)
  {
    // Check to see if this from one of our FCMS
    if (!isOwner(posterSeid))
    {
      // drop
      return;
    }
    
    try
    {
      // Forward
      setPowerState(powerState);
    }
    catch (HaviDcmException e)
    {
      // Just log an error
      LoggerSingleton.logError(this.getClass(), "powerStateChangedEventNotification", e.toString());
    }
  }
  
  private void installJukeboxes()
  {
    try
    {
      // Log some information
      LoggerSingleton.logInfo(this.getClass(), "installJukeboxes", "starting SonyJukeboxFcm probe");
      
      // Force probe
      if (!deviceMonitor.probe())
      {
        // Log warning
        LoggerSingleton.logWarning(this.getClass(), "installJukeboxes", "probe failed");
        
        // All done
        return;
      }
      
      // Remove any gone devices
      LoggerSingleton.logDebugFine(this.getClass(), "installJukeboxes", "removing " + deviceMonitor.getGoneDevices().length + " devices");
      for (int i = 0; i < deviceMonitor.getGoneDevices().length; i++)
      {
        // Get the device ID
        int deviceId = deviceMonitor.getGoneDevices()[i];
        
        // Match sony device type
        if ((deviceId & 0x90000000) != 0)
        {
          // Extract channel number
          int channel = (deviceId >> 16) & 0x3;
          
          // Close device is present
          if (jukeboxes[channel] != null)
          {
            // Log some information
            LoggerSingleton.logError(this.getClass(), "installedJukeboxes", "uninstalling jukebox on " + Integer.toHexString(deviceId));

            // Remove from DCM
            removeFcm(jukeboxes[i].getSeid());
            
            // Close jukebox
            jukeboxes[channel].close();
            
            // Release
            jukeboxes[channel] = null;
          }
        }
      }
      
      // Add new devices
      LoggerSingleton.logDebugFine(this.getClass(), "installJukeboxes", "adding " + deviceMonitor.getNewDevices().length + " devices");
      for (int i = 0; i < deviceMonitor.getNewDevices().length; i++)
      {
        // Get the device ID
        int deviceId = deviceMonitor.getNewDevices()[i];
        
        // Match sony device type
        if ((deviceId & 0x90000000) != 0)
        {
          // Extract channel number
          int channel = (deviceId >> 16) & 0x3;
          
          // Check for error
          if (jukeboxes[channel] != null) 
          {
            // Log error and exit
            LoggerSingleton.logError(this.getClass(), "installJukeboxes", "channel " + channel + " is not null and listed a new device from probe");
            
            // All done
            return;
          }
          
          // Log some information
          LoggerSingleton.logInfo(this.getClass(), "installedJukeboxes", "installing jukebox on " + Integer.toHexString(deviceId));

          // Create new jukebox
          jukeboxes[channel] = new SonyJukeboxFcm(this, deviceId);
          HUID huid = addFcm(jukeboxes[channel].getSeid(), ConstRbx1600DcmInterfaceId.RBX1600_JUKEBOX_FCM, ConstStreetFireVendorInformation.VENDOR_ID);
          jukeboxes[channel].setHuid(huid);

          // Log some information
          LoggerSingleton.logDebugFine(this.getClass(), "installedJukeboxes", "done with N2: " + huid.getTargetId().getN2());
        }
      }
    }
    catch (HaviException e)
    {
      // Log error
      LoggerSingleton.logError(this.getClass(), "installJukeboxes", e.toString());
    }
  }
  
  private void registerFcms() throws HaviException
  {
    // Register FCMs
    amplifier.register();
    for (int i = 0; i < jukeboxes.length; i++)
    {
      if (jukeboxes[i] != null)
      {
        jukeboxes[i].register();
      }
    }
  }
  
  private void unregisterFcms() throws HaviException
  {
    // Register FCMs
    amplifier.unregister();
    for (int i = 0; i < jukeboxes.length; i++)
    {
      if (jukeboxes[i] != null)
      {
        jukeboxes[i].unregister();
      }
    }
  }
}
