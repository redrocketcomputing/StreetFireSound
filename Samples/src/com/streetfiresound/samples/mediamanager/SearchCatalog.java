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
 * $Id: SearchCatalog.java,v 1.1 2005/03/17 02:29:05 stephen Exp $
 */
package com.streetfiresound.samples.mediamanager;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.streetfiresound.commands.HaviCommandLineApplication;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaCatalogErrorCode;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogAsyncResponseHelper;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogClient;
import com.streetfiresound.mediamanager.mediacatalog.rmi.SearchMetaDataAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogException;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SearchCatalog extends HaviCommandLineApplication implements SearchMetaDataAsyncResponseListener
{
  private long startTime = 0;
  private long endTime = 0;
  private Object done = new Object();
  private int transactionId = -1;
  private MediaMetaData[] searchResult = new MediaMetaData[0];
  
  /**
   * @param args
   */
  public SearchCatalog()
  {
    // Forward to superclass to initialize the HAVI subsystem
    super();
  }

  /* (non-Javadoc)
   * @see com.streetfiresound.mediamanager.mediacatalog.rmi.SearchMetaDataAsyncResponseListener#handleSearchMetaData(int, com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData[], org.havi.system.types.Status)
   */
  public void handleSearchMetaData(int transactionId, MediaMetaData[] result, Status returnCode)
  {
    // Check transaction if
    if (transactionId != this.transactionId)
    {
      System.out.println("bad transaction id");
      System.exit(1);
    }
    
    // Check result code
    if (returnCode.getErrCode() != ConstMediaCatalogErrorCode.SUCCESS)
    {
      System.out.println("bad return code" +  returnCode);
      System.exit(1);
    }
    
    // Save result and notify waitier
    endTime = System.currentTimeMillis();
    synchronized(done)
    {
      searchResult = result;
      done.notifyAll();
    }
  }

  /* (non-Javadoc)
   * @see com.redrocketcomputing.havi.system.rmi.AsyncResponseListener#timeout(int)
   */
  public void timeout(int transactionId)
  {
    // Check transaction if
    if (transactionId != this.transactionId)
    {
      System.out.println("bad transaction id");
    }
    else
    {
      System.out.println("timeout");
    }
    
    System.exit(1);
  }
  
  public void run()
  {
    try
    {
      // Sleep for 5 seconds to let the network settle
      Thread.sleep(5000);
      
      // Create software element
      SoftwareElement softwareElement = new SoftwareElement();
      
      // Find a MediaManager Application Module on the network
      SimpleAttributeTable attributes = new SimpleAttributeTable();
      attributes.setSoftwareElementType(ConstSoftwareElementType.APPLICATION_MODULE);
      attributes.setInterfaceId(ConstMediaManagerInterfaceId.MEDIA_MANAGER);
      RegistryClient registryClient = new RegistryClient(softwareElement);
      QueryResult result = registryClient.getElementSync(0, attributes.toQuery());

      // Look for matching GUID
      SEID remoteSeid = null;
      GUID matchGuid = GuidUtil.fromString(ConfigurationProperties.getInstance().getProperties().getProperty("match.guid", "ff:ff:ff:ff:ff:ff:ff:ff"));
      for (int i = 0; i < result.getSeidList().length; i++)
      {
        // Check for match GUID
        if (matchGuid.equals(GUID.BROADCAST) || result.getSeidList()[i].getGuid().equals(matchGuid))
        {
          // Found it
          remoteSeid = result.getSeidList()[i];
          break;
        }
      }

      // Check not found
      if (remoteSeid == null)
      {
        System.out.println(matchGuid.toString() + " not found");
        System.exit(1);
      }
      
      // Create a media catalog client with the first MediaManager found and get the Discs
      MediaCatalogClient mediaManagerClient = new MediaCatalogClient(softwareElement, remoteSeid);
      MediaCatalogAsyncResponseHelper mediaManagerHelper = new MediaCatalogAsyncResponseHelper(softwareElement);
      softwareElement.addHaviListener(mediaManagerHelper);
      
      // Search catalog
      synchronized(softwareElement)
      {
        startTime = System.currentTimeMillis();
        transactionId = mediaManagerClient.searchMetaData("rock");
        mediaManagerHelper.addAsyncResponseListener(120000, transactionId, this);
      }
      
      // Wait until done
      synchronized(done)
      {
        done.wait();
      }
      
      // Dump the meta data
      System.out.println("found " + searchResult.length + " items in " + (endTime - startTime) + " milliseconds");
      for (int i = 0; i < searchResult.length; i++)
      {
        System.out.println(searchResult[i].getMediaLocationId().toString() + " -> " + searchResult[i].getTitle());
      }
      
      // All done clean up
      softwareElement.close();
    }
    catch (HaviMsgException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    catch (HaviRegistryException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    catch (HaviMediaCatalogException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public static void main(String[] args)
  {
    // Create the application
    SearchCatalog application = new SearchCatalog();
    
    // Run the application
    application.run();
    
    // All done
    System.exit(0);
  }
}
