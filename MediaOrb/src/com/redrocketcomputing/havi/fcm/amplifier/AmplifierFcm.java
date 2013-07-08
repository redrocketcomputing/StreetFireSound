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
 * $Id: AmplifierFcm.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.fcm.amplifier;

import java.io.IOException;

import org.havi.fcm.amplifier.constants.ConstAmplifierCapabiliy;
import org.havi.fcm.amplifier.constants.ConstAmplifierPresetMode;
import org.havi.fcm.amplifier.constants.ConstFcmAttributeIndicator;
import org.havi.fcm.amplifier.rmi.AmplifierServerHelper;
import org.havi.fcm.amplifier.rmi.AmplifierSkeleton;
import org.havi.fcm.amplifier.types.HaviAmplifierException;
import org.havi.fcm.amplifier.types.HaviAmplifierInvalidParameterException;
import org.havi.fcm.amplifier.types.HaviAmplifierNotImplementedException;
import org.havi.fcm.amplifier.types.HaviAmplifierUnidentifiedFailureException;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviInvalidValueException;
import org.havi.system.types.HaviMarshallingException;

import com.redrocketcomputing.havi.system.dcm.Dcm;
import com.redrocketcomputing.havi.system.fcm.Fcm;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AmplifierFcm extends Fcm implements AmplifierSkeleton
{
  private final static int EQUALIZER_CAPABILITY_MAX = 20;
  private final static int PRESET_CAPABILITY_MAX = ConstAmplifierPresetMode.MUSIC_ROCK + 1;
  
  private AmplifierServerHelper serverHelper;
  
  protected boolean capabilities[] = { false, false, false, false };
  protected boolean equalizer_capabilities[] = new boolean[0];
  protected boolean preset_capabilities[] = { false, false, false, false, false, false };
  protected byte volume = 0;
  protected boolean mute = true;
  protected byte balance = 0;
  protected boolean loudness = false;
  protected byte[] equalizer = new byte[0];
  protected int preset = ConstAmplifierPresetMode.OFF;

  /**
   * @param softwareElement
   * @param dcmSeid
   */
  public AmplifierFcm(Dcm dcm) throws HaviException
  {
    // Construct super class
    super(dcm);
    
    // Bind server helper
    serverHelper = new AmplifierServerHelper(softwareElement, this);
    softwareElement.addHaviListener(serverHelper);
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#close()
   */
  public void close()
  {
    // Unbind server helper
    softwareElement.removeHaviListener(serverHelper);
    serverHelper = null;
    
    // Forward to super class
    super.close();
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.AbstractFcm#getSoftwareElementType()
   */
  public int getSoftwareElementType()
  {
    return ConstSoftwareElementType.AMPLIFIER_FCM;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getBalance()
   */
  public byte getBalance() throws HaviAmplifierException
  {
    return balance;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getCapability()
   */
  public boolean[] getCapability() throws HaviAmplifierException
  {
    return capabilities;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getEqualizer()
   */
  public byte[] getEqualizer() throws HaviAmplifierException
  {
    return equalizer;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getLoudness()
   */
  public boolean getLoudness() throws HaviAmplifierException
  {
    return loudness;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getMute()
   */
  public boolean getMute() throws HaviAmplifierException
  {
    return mute;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getPresetCapability()
   */
  public boolean[] getPresetCapability() throws HaviAmplifierException
  {
    return preset_capabilities;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getPresetMode()
   */
  public int getPresetMode() throws HaviAmplifierException
  {
    return preset;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getVolume()
   */
  public byte getVolume() throws HaviAmplifierException
  {
    return volume;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setBalance(byte)
   */
  public void setBalance(byte balanceValue) throws HaviAmplifierException
  {
    try
    {
      // Check capabities
      if (!capabilities[ConstAmplifierCapabiliy.BALANCE])
      {
        // Not supported
        throw new HaviAmplifierNotImplementedException("Balance not implemented");
      }

      // Change the balance
      balance = balanceValue;

      // Post attribute change
      updateAttribute(ConstFcmAttributeIndicator.AMPLIFIER_BALANCE, getAttributeValue(ConstFcmAttributeIndicator.AMPLIFIER_BALANCE));
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAmplifierUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setEqualizer(byte[])
   */
  public void setEqualizer(byte[] equalizerValue) throws HaviAmplifierException
  {
    try
    {
      // Check capabities
      if (!capabilities[ConstAmplifierCapabiliy.EQUALIZER])
      {
        // Not supported
        throw new HaviAmplifierNotImplementedException("equalizer not implemented");
      }

      // Check for matching range
      if (equalizerValue.length != equalizer.length)
      {
        // Bad parameter
        throw new HaviAmplifierInvalidParameterException("expecting band range to be " + equalizer.length + " got " + equalizerValue.length);
      }

      // Change the equalizer value
      equalizer = equalizerValue;

      // Post attribute change
      updateAttribute(ConstFcmAttributeIndicator.AMPLIFIER_EQUALIZER, getAttributeValue(ConstFcmAttributeIndicator.AMPLIFIER_EQUALIZER));
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAmplifierUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setLoudness(byte)
   */
  public void setLoudness(boolean loudnessValue) throws HaviAmplifierException
  {
    try
    {
      // Check capabities
      if (!capabilities[ConstAmplifierCapabiliy.LOUDNESS])
      {
        // Not supported
        throw new HaviAmplifierNotImplementedException("Loudness not implemented");
      }

      // Change the loudness
      loudness = loudnessValue;

      // Post attribute change
      updateAttribute(ConstFcmAttributeIndicator.AMPLIFIER_LOUDNESS, getAttributeValue(ConstFcmAttributeIndicator.AMPLIFIER_LOUDNESS));
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAmplifierUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setMute(boolean)
   */
  public void setMute(boolean muteState) throws HaviAmplifierException
  {
    try
    {
      // Change the mute
      mute = muteState;

      // Post attribute change
      updateAttribute(ConstFcmAttributeIndicator.AMPLIFIER_MUTE, getAttributeValue(ConstFcmAttributeIndicator.AMPLIFIER_MUTE));
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAmplifierUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setPresetMode(int)
   */
  public void setPresetMode(int amplifierPresetMode) throws HaviAmplifierException
  {
    try
    {
      // Check capabities
      if (!capabilities[ConstAmplifierCapabiliy.PRESET])
      {
        // Not supported
        throw new HaviAmplifierNotImplementedException("preset not implemented");
      }

      // Check for matching range
      if (amplifierPresetMode < 0 || amplifierPresetMode >= preset_capabilities.length || !preset_capabilities[amplifierPresetMode])
      {
        // Bad parameter
        throw new HaviAmplifierInvalidParameterException("preset " + amplifierPresetMode + " not supported");
      }

      // Change the preset mode
      preset = amplifierPresetMode;

      // Post attribute change
      updateAttribute(ConstFcmAttributeIndicator.AMPLIFIER_PRESET_MODE, getAttributeValue(ConstFcmAttributeIndicator.AMPLIFIER_PRESET_MODE));
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAmplifierUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#setVolume(byte)
   */
  public void setVolume(byte volumeValue) throws HaviAmplifierException
  {
    try
    {
      // Change the volume
      volume = volumeValue;

      // Post attribute change
      updateAttribute(ConstFcmAttributeIndicator.AMPLIFIER_VOLUME, getAttributeValue(ConstFcmAttributeIndicator.AMPLIFIER_VOLUME));
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAmplifierUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.amplifier.rmi.AmplifierSkeleton#getAudioLatency()
   */
  public abstract short getAudioLatency() throws HaviAmplifierException;

  /**
   * Marshall the specifed attribute value into a FcmAttributeValue
   * @param attributeIndicator The attribute value to marshall up
   * @return The corresponding FcmAttributeValue object
   * @throws HaviInvalidValueException
   * @throws HaviMarshallingException
   */
  protected byte[] getAttributeValue(short attributeIndicator) throws HaviFcmException
  {
    try
    {
      // Create a havi output stream
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();

      // Ugly
      switch (attributeIndicator)
      {
        case ConstFcmAttributeIndicator.AMPLIFIER_VOLUME:
        {
          // Marshal it up
          hbaos.writeByte(volume);

          // All done
          break;
        }

        case ConstFcmAttributeIndicator.AMPLIFIER_MUTE:
        {
          // Marshall it up
          hbaos.writeBoolean(mute);

          // All done
          break;
        }

        case ConstFcmAttributeIndicator.AMPLIFIER_BALANCE:
        {
          // Marshall it up
          hbaos.writeByte(balance);

          // All done
          break;
        }

        case ConstFcmAttributeIndicator.AMPLIFIER_LOUDNESS:
        {
          // Marshall it up
          hbaos.writeBoolean(mute);

          // All done
          break;
        }
        case ConstFcmAttributeIndicator.AMPLIFIER_EQUALIZER:
        {
          // Marshal it up
          hbaos.writeInt(equalizer.length);
          hbaos.write(equalizer);

          // All done
          break;
        }

        default:
        {
          throw new HaviInvalidValueException("bad FcmAttributeIndicator: " + attributeIndicator);
        }
      }

      // Return it
      return hbaos.toByteArray();
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }

  public int getFcmType() throws HaviFcmException
  {
    return ConstSoftwareElementType.AMPLIFIER_FCM;
  }
}
