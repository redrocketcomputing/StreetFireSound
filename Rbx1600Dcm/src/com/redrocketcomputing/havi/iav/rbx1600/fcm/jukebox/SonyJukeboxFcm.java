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
 * $Id: SonyJukeboxFcm.java,v 1.8 2005/03/16 04:25:03 stephen Exp $
 */
package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.havi.dcm.types.HaviDcmException;
import org.havi.fcm.avdisc.types.AvDiscCounterValue;
import org.havi.fcm.avdisc.types.HaviAvDiscException;
import org.havi.fcm.avdisc.types.HaviAvDiscUnidentifiedFailureException;
import org.havi.fcm.avdisc.types.ItemIndex;
import org.havi.fcm.types.HaviFcmException;
import org.havi.fcm.types.HaviFcmUnidentifiedFailureException;
import org.havi.system.constants.ConstDeviceClass;
import org.havi.system.types.HaviByteArrayOutputStream;
import org.havi.system.types.HaviMarshallingException;
import org.havi.system.types.HaviMsgListenerExistsException;
import org.havi.system.types.HaviVersionException;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.fcm.avdisc.AvDiscFcm;
import com.redrocketcomputing.hardware.DigitalAudioQSubcodeInputStream;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmRelease;
import com.redrocketcomputing.havi.constants.ConstStreetFireVendorInformation;
import com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstFcmAttributeIndicator;
import com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstPositionReportMode;
import com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxServerHelper;
import com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxException;
import com.redrocketcomputing.havi.fcm.sonyjukebox.types.HaviSonyJukeboxUnidentifiedFailureException;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.Protocol;
import com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol.ProtocolException;
import com.redrocketcomputing.havi.system.cmm.slink.CmmSlink;
import com.redrocketcomputing.havi.system.dcm.Dcm;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SonyJukeboxFcm extends AvDiscFcm implements SonyJukeboxSkeleton, Observer
{
  private final static String DEFAULT_PATH = File.separator + "opt" + File.separator + "streetfire";
  
  private SonyJukeboxServerHelper serverHelper;
  private String path = DEFAULT_PATH;
  private int deviceId;
  private Protocol protocol;
  private JukeboxInformation jukeboxInformation;
  private PowerStateController powerStateController;
  private TocBuilder tocBuilder;
  private SlotCache slotCache;
  private TransportStateTracker transportStateTracker;
  private DigitalAudioQSubcodeInputStream qsubcodeInputStream;
  private PositionTracker positionTracker;
  private AudioPathManager audioPathManager;
  private PlayModeController playModeController;
  private AbstractTransportController transportController;
  private Object transportControllerLock = new Object();
  private int positionReportMode = ConstPositionReportMode.SECOND;
  
  /**
   * @param dcm
   * @throws HaviFcmException
   * @throws HaviAvDiscException
   */
  public SonyJukeboxFcm(Dcm dcm, int deviceId) throws HaviFcmException, HaviAvDiscException, HaviSonyJukeboxException
  {
    // Construct superclass
    super(dcm);
    
    try
    {
      // Save the device Id
      this.deviceId = deviceId;
      
      // Get the data path
      path = dcm.getConfiguration().getProperty("cache.path", DEFAULT_PATH);
      
      // Setup capabilities
      capabilities = new boolean[]{ false, false, false, false, false, false, false, false, false, false, false, false };
      capabilities[org.havi.fcm.avdisc.constants.ConstAvDiscCapability.MULTI_SLOT] = true;
      capabilities[com.redrocketcomputing.havi.fcm.sonyjukebox.constants.ConstAvDiscCapability.SCAN] = true;

      // Create server helper
      serverHelper = new SonyJukeboxServerHelper(softwareElement, this);
      softwareElement.addHaviListener(serverHelper);
      
      // Create the SLINK protocol
      CmmSlink cmmSlink = (CmmSlink)ServiceManager.getInstance().get(CmmSlink.class);
      protocol = new Protocol((SonyJukeboxSlinkDevice)cmmSlink.getDevice(deviceId));

      // Create power state controller and force power off
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing PowerStateController");
      powerStateControlSupported = true;
      powerStateController = new PowerStateController(protocol);
      powerStateController.addObserver(this);
      
      // Probe the device
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing JukeboxInformation");
      jukeboxInformation = new JukeboxInformation(protocol);
      capacity = jukeboxInformation.getCapacity();
      
      // Create TOC builder
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing TocBuilder");
      tocBuilder = new TocBuilder(protocol);
      
      // Create the slot cache
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing SlotCache");
      slotCache = new SlotCache(protocol, jukeboxInformation.getCapacity(), path);
      slotCache.addObserver(this);
      
      // Create transport state tracker
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing TransportStateTracker");
      transportStateTracker = new TransportStateTracker(protocol);
      
      // Create position tracker
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing PositionTracker");
      qsubcodeInputStream = new DigitalAudioQSubcodeInputStream((deviceId >> 16) & 0x3);
      positionTracker = new PositionTracker(protocol);
      qsubcodeInputStream.addListener(positionTracker);
      
      // Create audio path manager
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing AudioPathManager");
      audioPathManager = new AudioPathManager(protocol);
      
      // Create and bind play mode controller
      LoggerSingleton.logDebugCoarse(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": initializing PlayModeController");
      playModeController = new PlayModeController(protocol);
      playModeController.addObserver(this);
      changeTransportController(playModeController.getPlayMode());
      
      // Initialize state
      changeTransportState(transportStateTracker.getState(), playModeController.getPlayMode());
      
      // Log model and capacity
      LoggerSingleton.logInfo(this.getClass(), "SonyJukeboxFcm", "Device: " + Integer.toHexString(deviceId) + ": Sony Model " + jukeboxInformation.getModel() + " with " + jukeboxInformation.getCapacity() + " slots");
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
    catch (HaviMsgListenerExistsException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#close()
   */
  public void close()
  {
    // Close componets
    playModeController.close();
    audioPathManager.close();
    qsubcodeInputStream.close();
    positionTracker.close();
    slotCache.close();
    tocBuilder.close();
    powerStateController.close();
    protocol.close();
    
    // Forward to super class
    super.close();
  }
  
  /* (non-Javadoc)
   * @see org.havi.system.version.rmi.VersionSkeleton#getVersion()
   */
  public String getVersion() throws HaviVersionException
  {
    return ConstRbx1600DcmRelease.getRelease();
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#getDeviceClass()
   */
  public int getDeviceClass() throws HaviDcmException
  {
    return ConstDeviceClass.IAV;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#getSoftwareElementManufacturer()
   */
  public String getSoftwareElementManufacturer()
  {
    return ConstStreetFireVendorInformation.MANUFACTURER;
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#getDeviceManufacturer()
   */
  public String getDeviceManufacturer()
  {
    return "Sony Corportation";
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#getDeviceModel()
   */
  public String getDeviceModel()
  {
    // TODO Auto-generated method stub
    return super.getDeviceModel();
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.rmi.FcmSkeleton#setPowerState(boolean)
   */
  public boolean setPowerState(boolean powerState) throws HaviFcmException
  {
    try
    {
      // Change power state
      if (this.powerState != powerState)
      {
        LoggerSingleton.logDebugCoarse(this.getClass(), "setPowerState", "Device: " + Integer.toHexString(deviceId) + ": changing power state to " + powerState);

        // Change local power state
        powerStateController.setPowerState(powerState);

        // Forward to superclass
        return super.setPowerState(powerStateController.getPowerState());
      }
      
      // Return current power state
      return powerState;
    }
    catch (ProtocolException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#play(short, short, short)
   */
  public void play(int playMode, short plugNum, short listNumber, short indexNumber) throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);
      
      synchronized(transportControllerLock)
      {
        // Change transport state controller
        changeTransportController(playMode);
      
        // Forward to the transport state
        transportController.play(plugNum, listNumber, indexNumber);
      }
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton#cue(int, short, short)
   */
  public void cue(int playMode, short plugNum, short listNumber, short indexNumber) throws HaviSonyJukeboxException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      synchronized(transportControllerLock)
      {
        // Change transport state controller
        changeTransportController(playMode);
      
        // Forward to the transport state
        transportController.cue(plugNum, listNumber, indexNumber);
      }
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#stop(int, short)
   */
  public void stop(int dir, short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      synchronized(transportControllerLock)
      {
        // Forward to the transport state
        transportController.stop(dir, plugNum);
      }
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#resume(short)
   */
  public void resume(short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      synchronized(transportControllerLock)
      {
        // Forward to the transport state
        transportController.resume(plugNum);
      }
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#pause(short)
   */
  public void pause(short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      synchronized(transportControllerLock)
      {
        // Forward to the transport state
        transportController.pause(plugNum);
      }
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#skip(int, int, int, short)
   */
  public void skip(int direction, int mode, int count, short plugNum) throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      synchronized(transportControllerLock)
      {
        // Forward to the transport state
        transportController.skip(direction, mode, count, plugNum);
      }
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#insertMedia()
   */
  public void insertMedia() throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#ejectMedia()
   */
  public void ejectMedia() throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton#scan(short, short)
   */
  public void scan(int scanMode, short startList, short endList) throws HaviSonyJukeboxException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);
      
      synchronized(transportControllerLock)
      {
        // Change transport state controller
        changeTransportController(scanMode);
      
        // Forward to the transport state
        transportController.scan(startList, endList);
      }
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton#setPositionReportMode(int)
   */
  public void setPositionReportMode(int positionReportMode) throws HaviSonyJukeboxException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      this.positionReportMode = positionReportMode;
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton#getPositionReportMode()
   */
  public int getPositionReportMode() throws HaviSonyJukeboxException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);
      
      // Return current position report mode
      return positionReportMode;
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
  }


  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.fcm.sonyjukebox.rmi.SonyJukeboxSkeleton#getScanMode()
   */
  public int getScanMode() throws HaviSonyJukeboxException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);
      
      // TODO Auto-generated method stub
      return 0;
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviSonyJukeboxUnidentifiedFailureException(e.toString());
    }
  }

  
  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#getItemList(short)
   */
  public ItemIndex[] getItemList(short listNumber) throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      // Forward to slot cache
      return slotCache.getItemIndex(listNumber & 0xffff);
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }

  /* (non-Javadoc)
   * @see org.havi.fcm.avdisc.rmi.AvDiscSkeleton#putItemList(short, org.havi.fcm.avdisc.types.ItemIndex[])
   */
  public void putItemList(short listNumber, ItemIndex[] itemIndexList) throws HaviAvDiscException
  {
    try
    {
      // Turn on power is required
      setPowerState(true);

      // Mark list and index
      itemIndexList[0].setList(listNumber);
      itemIndexList[0].setIndex((short)0);
      for (short i = 1; i < itemIndexList.length; i++)
      {
        itemIndexList[i].setList(listNumber);
        itemIndexList[i].setIndex(i);
      }
      
      // Forward
      slotCache.putItemIndex(itemIndexList);
    }
    catch (HaviFcmException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviAvDiscUnidentifiedFailureException(e.toString());
    }
  }
  
  /**
   * Depending on the PositionReportMode notify any subscribers
   * @param position The new position
   */
  public void changePosition(AvDiscCounterValue position)
  {
    try
    {
      // Take snap shot of current position
      short list = super.position.getList();
      short index = super.position.getIndex();
      byte rHour = super.position.getRelative().getHour();
      byte rMinute = super.position.getRelative().getMinute();
      byte rSec = super.position.getRelative().getSec();
      byte rFrame = super.position.getRelative().getFrame();
      
      // Forward to super class
      super.changePosition(position);
      
      // Check for reporting value
      if (positionReportMode == ConstPositionReportMode.FRAME)
      {
        // Update attribute
        updateAttribute(ConstFcmAttributeIndicator.CURRENT_POSITION, getAttributeValue(ConstFcmAttributeIndicator.CURRENT_POSITION));
      }
      else if (positionReportMode >= ConstPositionReportMode.SECOND && position.getRelative().getSec() != rSec)
      {
        // Update attribute
        updateAttribute(ConstFcmAttributeIndicator.CURRENT_POSITION, getAttributeValue(ConstFcmAttributeIndicator.CURRENT_POSITION));
      }
      else if (positionReportMode >= ConstPositionReportMode.MINUTE && position.getRelative().getMinute() != rMinute)
      {
        // Update attribute
        updateAttribute(ConstFcmAttributeIndicator.CURRENT_POSITION, getAttributeValue(ConstFcmAttributeIndicator.CURRENT_POSITION));
      }
      else if (positionReportMode >= ConstPositionReportMode.HOUR && position.getRelative().getHour() != rHour)
      {
        // Update attribute
        updateAttribute(ConstFcmAttributeIndicator.CURRENT_POSITION, getAttributeValue(ConstFcmAttributeIndicator.CURRENT_POSITION));
      }
      else if (positionReportMode >= ConstPositionReportMode.INDEX && position.getIndex() != index)
      {
        // Update attribute
        updateAttribute(ConstFcmAttributeIndicator.CURRENT_POSITION, getAttributeValue(ConstFcmAttributeIndicator.CURRENT_POSITION));
      }
      else if (positionReportMode >= ConstPositionReportMode.LIST && position.getList() != list)
      {
        // Update attribute
        updateAttribute(ConstFcmAttributeIndicator.CURRENT_POSITION, getAttributeValue(ConstFcmAttributeIndicator.CURRENT_POSITION));
      }
    }
    catch (HaviFcmException e)
    {
      // Log error and drop
      LoggerSingleton.logError(this.getClass(), "reportPosition", e.toString());
    }
  }
  
  /* (non-Javadoc)
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  public void update(Observable o, Object arg)
  {
    if (o == powerStateController)
    {
      try
      {
        // Forward to superclass
        super.setPowerState(((Boolean)arg).booleanValue());
        
        LoggerSingleton.logDebugCoarse(this.getClass(), "update", "Device: " + Integer.toHexString(deviceId) + ": power state changed: " + powerState);
      }
      catch (HaviFcmException e)
      {
        // Just log the error
        LoggerSingleton.logError(this.getClass(), "update", e.toString());
      }
    }
    else if (o == playModeController)
    {
      // Change transport controller to match the new mode
      changeTransportController(((Integer)arg).intValue());
    }
    else if (o == slotCache)
    {
      // Fire list changed event
      super.itemIndexChanged(((Short)arg).shortValue());
    }
    else 
    {
      LoggerSingleton.logWarning(this.getClass(), "update", "unhandled update from " + o.getClass().getName());
    }
  }
  
  
  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.fcm.Fcm#getAttributeValue(short)
   */
  public byte[] getAttributeValue(short attributeIndicator) throws HaviFcmException
  {
    try
    {
      switch (attributeIndicator)
      {
        case ConstFcmAttributeIndicator.CURRENT_POSITION:
        {
          // Create output stream
          HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream(256);
          
          // Marshal it up
          position.marshal(hbaos);
          
          // Return byte array
          return hbaos.toByteArray();
        }
        
        default:
        {
          // Forward to super class
          return super.getAttributeValue(attributeIndicator);
        }
      }
    }
    catch (HaviMarshallingException e)
    {
      // Translate
      throw new HaviFcmUnidentifiedFailureException(e.toString());
    }
  }
  
  /**
   * Change the exiting transport controller
   * @param mode The mode for the new transport controller
   */
  private void changeTransportController(int mode)
  {
    synchronized (transportControllerLock)
    {
      // Check for exiting transport control which handle the new play mode
      if (transportController != null && transportController.handles(mode))
      {
        // Just return
        return;
      }
      
      // Close existing transport controller
      if (transportController != null)
      {
        transportController.close();
      }
      
      // Create new transport controller
      switch (mode)
      {
        case ConstSonyJukeboxPlayMode.DIRECT_1:
        {
          transportController = new Direct1PlayModeTransportController(this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.DIRECT:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.DIRECT_ALL:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.NORMAL:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.RANDOM:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.RANDOM_ALL:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.REPEAT_1:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.REPEAT:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.REPEAT_ALL:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.SHUFFLE:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.SHUFFLE_ALL:
        {
          transportController = new NativePlayModeTransportController(mode, this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.SCAN_ALL:
        {
          transportController = new AllScanModeTransportController(this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.SCAN_EMPTY_ONLY:
        {
          transportController = new EmptyOnlyScanModeTransportController(this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }
        
        case ConstSonyJukeboxPlayMode.SCAN_FOR_EMPTY:
        {
          transportController = new SeekEmptyScanModeTransportController(this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }

        case ConstSonyJukeboxPlayMode.SCAN_CHANGED_SLOTS:
        {
          transportController = new ChangedScanModeTransportController(this, protocol, positionTracker, tocBuilder, transportStateTracker, slotCache);
          break;
        }
      }
    }
  }
}
