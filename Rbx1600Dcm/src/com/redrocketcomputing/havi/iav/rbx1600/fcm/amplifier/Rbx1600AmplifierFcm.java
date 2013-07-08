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
 * $Id: Rbx1600AmplifierFcm.java,v 1.2 2005/03/16 04:25:03 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.amplifier;

import java.io.IOException;

import org.havi.fcm.amplifier.types.HaviAmplifierException;
import org.havi.fcm.amplifier.types.HaviAmplifierLocalException;
import org.havi.system.constants.ConstDeviceClass;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviVersionException;
import org.havi.system.types.VendorId;

import com.redrocketcomputing.hardware.DigitalAudioController;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmRelease;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.fcm.amplifier.AmplifierFcm;
import com.redrocketcomputing.havi.system.dcm.Dcm;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Rbx1600AmplifierFcm extends AmplifierFcm
{
  private DigitalAudioController digitalAudioController;

  /**
   * @param softwareElement
   * @param dcmSeid
   */
  public Rbx1600AmplifierFcm(Dcm dcm) throws HaviException
  {
    // Construct super class
    super(dcm);
    try
    {
      // Create a digital audio control for hardward control
      digitalAudioController = new DigitalAudioController();

      // Enable if not enabled
      if (!digitalAudioController.isEnabled())
      {
        // Enable the DAC
        digitalAudioController.setEnabled(true);
      }

      // Initialize to unmute and get current volume
      digitalAudioController.setMuted(false);
      mute = false;
      volume = (byte)(digitalAudioController.getVolume() & 0xff);
      
      // Log start
      LoggerSingleton.logInfo(this.getClass(), "Rbx1600AmplifierFcm", "started on " + softwareElement.getSeid());
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviAmplifierLocalException(e.toString());
    }
  }


  public void close()
  {
    // Close controller
    digitalAudioController.close();
    
    // Forward on
    super.close();
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setMute(boolean)
   */
  public void setMute(boolean muteState) throws HaviAmplifierException
  {
    try
    {
      // Forward to super class to get attribute notification and parameter checking
      super.setMute(muteState);

      // Change mute state
      digitalAudioController.setMuted(muteState);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviAmplifierLocalException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setVolume(byte)
   */
  public void setVolume(byte volumeValue) throws HaviAmplifierException
  {
    try
    {
      // Forward to super class to get attribute notification and parameter checking
      super.setVolume(volumeValue);

      // Change mute state
      digitalAudioController.setVolume(volumeValue & 0xff);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviAmplifierLocalException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getAudioLatency()
   */
  public short getAudioLatency() throws HaviAmplifierException
  {
    return 0;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.AbstractFcm#getVendorId()
   */
  public VendorId getVendorId()
  {
    return ConstStreetFireVendorInformation.VENDOR_ID;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.AbstractFcm#getInterfaceId()
   */
  public short getInterfaceId()
  {
    return ConstRbx1600DcmInterfaceId.RBX1600_AMPLIFIER_FCM;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.AbstractFcm#getSoftwareElementManufacturer()
   */
  public String getSoftwareElementManufacturer()
  {
    return ConstStreetFireVendorInformation.MANUFACTURER;
  }

  public int getDeviceClass()
  {
    return ConstDeviceClass.IAV;
  }

  public String getDeviceManufacturer()
  {
    return ConstStreetFireVendorInformation.MANUFACTURER;
  }

  public String getVersion() throws HaviVersionException
  {
    return ConstRbx1600DcmRelease.getRelease();
  }
}
