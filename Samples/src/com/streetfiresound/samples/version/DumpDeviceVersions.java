/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 * 
 * Created on Oct 8, 2004 by stephen
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
 */
package com.streetfiresound.samples.version;

import org.havi.system.SoftwareElement;
import org.havi.system.cmmip.rmi.CmmIpClient;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.SimpleQuery;
import org.havi.system.version.rmi.VersionClient;

import com.redrocketcomputing.appframework.Application;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DumpDeviceVersions extends Application
{
  private SoftwareElement softwareElement;
  
  /**
   * @param args
   */
  public DumpDeviceVersions(String[] args) throws HaviException
  {
    super(args);
    
    softwareElement = new SoftwareElement();
  }
  
  public String getLocalSystemVersion(int systemSoftwareElementType)
  {
    try
    {
      // Create a version client to the remote SEID
      VersionClient versionClient = new VersionClient(softwareElement, softwareElement.getSystemSeid(systemSoftwareElementType));
      
      return versionClient.getVersionSync(5);
    }
    catch (HaviException e)
    {
      return "UNKNOWN";
    }
  }
  
  public String getRemoteSystemVersion(GUID guid, int systemSoftwareElementType)
  {
    try
    {
      // Create a version client to the remote SEID
      VersionClient versionClient = new VersionClient(softwareElement, new SEID(guid, (short)systemSoftwareElementType));
      
      return versionClient.getVersionSync(5);
    }
    catch (HaviException e)
    {
      return "UNKNOWN";
    }
  }
  
  public void run()
  {
    try
    {
      // Create a local software element
      SoftwareElement softwareElement = new SoftwareElement();
      System.out.println("Local " + softwareElement.getSeid());
      Thread.sleep(500);
      
      // Create a version client to the remote SEID
      VersionClient versionClient = new VersionClient(softwareElement, softwareElement.getSystemSeid(ConstSoftwareElementType.MESSAGING_SYSTEM));
      
      System.out.println("Local MessagingSystem Version: " + getLocalSystemVersion(ConstSoftwareElementType.MESSAGING_SYSTEM));
      System.out.println("Local EventManager Version: " + getLocalSystemVersion(ConstSoftwareElementType.EVENTMANAGER));
      System.out.println("Local Registry Version: " + getLocalSystemVersion(ConstSoftwareElementType.REGISTRY));
      System.out.println("Local CmmIp Version: " + getLocalSystemVersion(ConstSoftwareElementType.COMMUNICATION_MEDIA_MANAGER));

      // Create CMM client
      CmmIpClient cmmClient = new CmmIpClient(softwareElement, softwareElement.getSystemSeid(ConstSoftwareElementType.COMMUNICATION_MEDIA_MANAGER));
      
      // Get GUID of all available devices and loop through to ask about the MediaOrb version
      GUID[] guids = cmmClient.getActiveDevicesSync(0);
      for (int i = 0; i < guids.length; i++)
      {
        // Ensure remote GUID
        if (!softwareElement.getSeid().getGuid().equals(guids[i]))
        {
          System.out.println(guids[i].toString() + " MessagingSystem Version: " + getRemoteSystemVersion(guids[i], ConstSoftwareElementType.MESSAGING_SYSTEM));
          System.out.println(guids[i].toString() + " EventManager Version: " + getRemoteSystemVersion(guids[i], ConstSoftwareElementType.EVENTMANAGER));
          System.out.println(guids[i].toString() + " Registry Version: " + getRemoteSystemVersion(guids[i], ConstSoftwareElementType.REGISTRY));
          System.out.println(guids[i].toString() + " CmmIp Version: " + getRemoteSystemVersion(guids[i], ConstSoftwareElementType.COMMUNICATION_MEDIA_MANAGER));
        }
      }
      
      // Create a registry client to find all DCM
      RegistryClient registryClient = new RegistryClient(softwareElement);
      SimpleAttributeTable queryTable = new SimpleAttributeTable();
      queryTable.setSoftwareElementType(ConstSoftwareElementType.DCM);
      
      // Run DCM query and get version
      QueryResult result = registryClient.getElementSync(0, (SimpleQuery)queryTable.toQuery());
      for (int i = 0; i < result.getSeidList().length; i++)
      {
        try
        {
          // Create version client and get version
          VersionClient dcmVersionClient = new VersionClient(softwareElement, result.getSeidList()[i]);
          String version = dcmVersionClient.getVersionSync(5);
          
          // Display version
          System.out.println("DCM at " + result.getSeidList()[i] + " version " + version);
        }
        catch (HaviException e)
        {
          System.out.println("DCM at " + result.getSeidList()[i] + " version UNKNOWN");
        }
      }

      // Run FCM query and get version
      queryTable.setSoftwareElementType(ConstSoftwareElementType.AVDISC_FCM);
      result = registryClient.getElementSync(0, (SimpleQuery)queryTable.toQuery());
      for (int i = 0; i < result.getSeidList().length; i++)
      {
        try
        {
          // Create version client and get version
          VersionClient dcmVersionClient = new VersionClient(softwareElement, result.getSeidList()[i]);
          String version = dcmVersionClient.getVersionSync(5);
          
          // Display version
          System.out.println("AVDISC_FCM at " + result.getSeidList()[i] + " version " + version);
        }
        catch (HaviException e)
        {
          System.out.println("AVDISC_FCM at " + result.getSeidList()[i] + " version UNKNOWN");
        }
      }
    }
    catch (HaviException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    // Create application
    DumpDeviceVersions application = new DumpDeviceVersions(args);
    
    // Execute the application
    application.run();

    // Force exit
    System.exit(0);
  }
}
