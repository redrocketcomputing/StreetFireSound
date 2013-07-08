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
 * $Id: AvDiscFcm.java,v 1.4 2005/03/02 21:06:58 stephen Exp $
 */
package com.redrocketcomputing.fcm.avdisc;

import org.havi.fcm.avdisc.constants.ConstAvDiscTransportMode;
import org.havi.fcm.avdisc.constants.ConstFcmAttributeIndicator;
import org.havi.fcm.avdisc.rmi.AvDiscServerHelper;
import org.havi.fcm.avdisc.rmi.AvDiscSkeleton;
import org.havi.fcm.avdisc.rmi.AvDiscSystemEventManagerClient;
import org.havi.fcm.avdisc.types.AvDiscCapabilities;
import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.AvDiscCurrentState;
import org.havi.fcm.avdisc.types.AvDiscFormat;
import org.havi.fcm.avdisc.types.AvDiscTransportState;
import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.avdisc.types.HaviAvDiscNotImplementedException;
import org.havi.fcm.avdisc.types.HaviAvDiscUnidentifiedFailureException;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.constants.ConstWriteProtectStatus;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.fcm.types.TimeCode;
import org.havi.system.constants.ConstDirection;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.MediaFormatId;

import com.redrocketcomputing.havi.constants.ConstMediaFormatId;
import com.redrocketcomputing.havi.system.dcm.Dcm;
import com.redrocketcomputing.havi.system.fcm.Fcm;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AvDiscFcm extends Fcm implements AvDiscSkeleton
{
  private final static int AVDISC_CAPABILITIES_MAX = 32;
  private final static int AVDISC_MEDIA_FORMAT_ID_MAX = 14;
  
  private AvDiscServerHelper serverHelper;
  
  protected boolean[] capabilities = { false, false, false, false, false, false, false, false, false };
  protected MediaFormatId[] playFormats = {};
  protected MediaFormatId[] recordFormats = {};
  protected int capacity = 1;
  protected AvDiscTransportState transportState = new AvDiscTransportState(ConstAvDiscTransportMode.NO_MEDIA, 0);
  protected AvDiscCounterValue position = new AvDiscCounterValue((short)0, (short)0, new TimeCode((byte)0, (byte)0, (byte)0, (byte)0), new TimeCode((byte)0, (byte)0, (byte)0, (byte)0));
  protected int direction = ConstDirection.OUT;
  protected short plug = 0;
  protected MediaFormatId format = ConstMediaFormatId.DISC_NO_MEDIA;
  protected int writeProtectedStatus = ConstWriteProtectStatus.UNKNOWN_WRITABLE;
  
  /**
   * @param dcm
   * @throws HaviFcmException
   */
  public AvDiscFcm(Dcm dcm) throws HaviFcmException, HaviAvDiscException
  {
    super(dcm);
    
    try
    {
      // Create and bind server helper
      serverHelper = new AvDiscServerHelper(softwareElement, this);
      softwareElement.addHaviListener(serverHelper);
    }
    catch (HaviMsgListenerExistsException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#close()
   */
  public void close()
  {
    // Unbind server helper
    serverHelper.close();
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#getFcmType()
   */
  public int getFcmType() throws HaviFcmException
  {
    return ConstSoftwareElementType.AVDISC_FCM;
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#getItemList(short)
   */
  public ItemIndex[] getItemList(short listNumber) throws HaviAvDiscException
  {
    // Optional
    throw new HaviAvDiscNotImplementedException("getItemList");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#record(short, short, short, org.havi.fcm.types.TimeCode, long)
   */
  public void record(int recordMode, short plugNum, short listNumber, short indexNumber, TimeCode recordingTime, long recordingSize) throws HaviAvDiscException
  {
    // Optional
    throw new HaviAvDiscNotImplementedException("record");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#variableForward(int, short)
   */
  public void variableForward(int speed, short plugNum) throws HaviAvDiscException
  {
    // Optional
    throw new HaviAvDiscNotImplementedException("variableForward");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#variableReverse(int, short)
   */
  public void variableReverse(int speed, short plugNum) throws HaviAvDiscException
  {
    // Optional
    throw new HaviAvDiscNotImplementedException("variableForward");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#skip(int, int, int, short)
   */
  public void skip(int direction, int mode, int count, short plugNum) throws HaviAvDiscException
  {
    // Optional
    throw new HaviAvDiscNotImplementedException("skip");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#erase(short, short)
   */
  public void erase(short listNumber, short indexNumber) throws HaviAvDiscException
  {
    // Optional
    throw new HaviAvDiscNotImplementedException("erase");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#putItemList(short, org.havi.fcm.avdisc.types.ItemIndex[])
   */
  public void putItemList(short listNumber, ItemIndex[] itemIndexList) throws HaviAvDiscException
  {
    // Optional
    throw new HaviAvDiscNotImplementedException("putItemList");
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#getCapability()
   */
  public AvDiscCapabilities getCapability() throws HaviAvDiscException
  {
    return new AvDiscCapabilities(capabilities, playFormats, recordFormats, capacity);
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#getPosition(int, short)
   */
  public AvDiscCounterValue getPosition(int dir, short plugNum) throws HaviAvDiscException
  {
    return position;
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#getState(int, short)
   */
  public AvDiscTransportState getState(int dir, short plugNum) throws HaviAvDiscException
  {
    return transportState;
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#getFormat()
   */
  public AvDiscFormat getFormat() throws HaviAvDiscException
  {
    return new AvDiscFormat(format, writeProtectedStatus);
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#getAttributeValue(short)
   */
  public byte[] getAttributeValue(short attributeIndicator) throws HaviFcmException
  {
    try
    {
      // Create current state
      AvDiscCurrentState state = new AvDiscCurrentState(transportState, direction, plug); 
      
      // Create output stream
      HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(256);
      
      // Marshal it up
      state.marshal(hbaos);
      
      // Return byte array
      return hbaos.toByteArray();
    }
    catch (HaviMarshallingException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }
  
  public void itemIndexChanged(short list)
  {
    try
    {
      // Fire event
      AvDiscSystemEventManagerClient client = new AvDiscSystemEventManagerClient(softwareElement);
      client.fireAvDiscItemListChangedSync(list);
    }
    catch (HaviException e)
    {
      // Just log an error
      LoggerSingleton.logError(this.getClass(), "itemIndexChanged", e.toString());
    }
  }
  
  public void changeTransportState(int state, int mode)
  {
    try
    {
      // Check for changed state
      if (transportState.getState() != state || transportState.getMode() != mode)
      {
        //LoggerSingleton.logDebugCoarse(this.getClass(), "changeTransportState", "current state: " + transportState.getState() + "," + transportState.getMode() + " new state: " + state + "," + mode);
        
        // State new state
        transportState = new AvDiscTransportState(state, mode);
        
        // Fire event
        AvDiscSystemEventManagerClient client = new AvDiscSystemEventManagerClient(softwareElement);
        client.fireAvDiscStateChangedSync(transportState, direction, plug);
        
        // Notify subscriptions
        updateAttribute(ConstFcmAttributeIndicator.CURRENT_STATE, getAttributeValue(ConstFcmAttributeIndicator.CURRENT_STATE));
      }
    }
    catch (HaviException e)
    {
      // Just log an error
      LoggerSingleton.logError(this.getClass(), "changeTransportState", e.toString());
    }
  }
  
  public void changePosition(AvDiscCounterValue newPosition)
  {
    // Transfer data to save allocation
    this.position.setList(newPosition.getList());
    this.position.setIndex(newPosition.getIndex());
    this.position.getRelative().setHour(newPosition.getRelative().getHour());
    this.position.getRelative().setMinute(newPosition.getRelative().getMinute());
    this.position.getRelative().setSec(newPosition.getRelative().getSec());
    this.position.getRelative().setFrame(newPosition.getRelative().getFrame());
    this.position.getAbsolute().setHour(newPosition.getAbsolute().getHour());
    this.position.getAbsolute().setMinute(newPosition.getAbsolute().getMinute());
    this.position.getAbsolute().setSec(newPosition.getAbsolute().getSec());
    this.position.getAbsolute().setFrame(newPosition.getAbsolute().getFrame());
  }
}
