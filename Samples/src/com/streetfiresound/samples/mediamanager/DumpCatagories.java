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
 * $Id: DumpCatagories.java,v 1.1 2005/03/17 02:28:49 stephen Exp $
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
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstCategoryType;
import com.streetfiresound.mediamanager.mediacatalog.rmi.GetCategorySummaryAsyncResponseListener;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogAsyncResponseHelper;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogClient;
import com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogException;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DumpCatagories extends HaviCommandLineApplication
{
  private class AsyncInvocation implements GetCategorySummaryAsyncResponseListener
  {
    private MediaCatalogClient client;
    private MediaCatalogAsyncResponseHelper helper;
    private int type;
    private long start;
    private long end;
    private int transactionId;
    
    public AsyncInvocation(MediaCatalogClient client, MediaCatalogAsyncResponseHelper helper, int type)
    {
      this.client = client;
      this.helper = helper;
      this.type = type;
    }
    
    public void execute() throws HaviException
    {
      // Issue request
      start = System.currentTimeMillis();
      synchronized(client.getSoftwareElement())
      {
        transactionId = client.getCategorySummary(type);
        helper.addAsyncResponseListener(30000, transactionId, this);
      }
    }
    
    
    /* (non-Javadoc)
     * @see com.streetfiresound.mediamanager.mediacatalog.rmi.GetCategorySummaryAsyncResponseListener#handleGetCategorySummary(int, com.streetfiresound.mediamanager.mediacatalog.types.CategorySummary[], org.havi.system.types.Status)
     */
    public void handleGetCategorySummary(int transactionId, CategorySummary[] result, Status returnCode)
    {
      // Match transaction id
      if (transactionId != this.transactionId)
      {
        System.out.println("bad transation id");
        System.exit(1);
      }
      
      // Display some information
      end = System.currentTimeMillis();
      long duration = end - start;
      sumAsyncInvocationDuration[type] += duration;
      maxAsyncInvocationDuration[type] = Math.max(maxAsyncInvocationDuration[type], duration);
      System.out.println("type: " + type + " retrieved in " + duration + " milliseconds");
    }

    /* (non-Javadoc)
     * @see com.redrocketcomputing.havi.system.rmi.AsyncResponseListener#timeout(int)
     */
    public void timeout(int transactionId)
    {
      // Match transaction id
      if (transactionId != this.transactionId)
      {
        System.out.println("bad transation id");
        System.exit(1);
      }
      
      System.out.println("timeout");
    }
  }
  
  private long[] maxAsyncInvocationDuration = {0, 0, 0};
  private long[] maxSyncInvocationDuration = {0, 0, 0};
  private long[] sumAsyncInvocationDuration = {0, 0, 0};
  private long[] sumSyncInvocationDuration = {0, 0, 0};
  
  /**
   * @param args
   */
  public DumpCatagories()
  {
    // Forward to superclass to initialize the HAVI subsystem
    super();
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
      
      // Get type
      long start = 0;
      long end = 0;
      long duration = 0;
      for (int i = 0; i < 1000; i++)
      {
        System.out.println("Query " + i);
        
        // Issue async requests
        AsyncInvocation typeInvocation = new AsyncInvocation(mediaManagerClient, mediaManagerHelper, ConstCategoryType.TYPE);
        typeInvocation.execute();
        AsyncInvocation artistInvocation = new AsyncInvocation(mediaManagerClient, mediaManagerHelper, ConstCategoryType.ARTIST);
        artistInvocation.execute();
        AsyncInvocation genreInvocation = new AsyncInvocation(mediaManagerClient, mediaManagerHelper, ConstCategoryType.GENRE);
        genreInvocation.execute();
        
        start = System.currentTimeMillis();
        CategorySummary[] typeSummary = mediaManagerClient.getCategorySummarySync(0, ConstCategoryType.TYPE);
        end = System.currentTimeMillis();
        duration = end - start;
        sumSyncInvocationDuration[ConstCategoryType.TYPE] += duration;
        maxSyncInvocationDuration[ConstCategoryType.TYPE] = Math.max(maxSyncInvocationDuration[ConstCategoryType.TYPE], duration);
        System.out.println("Type Summary retrieved in " + duration + " milliseconds");
        
        // Get artist
        start = System.currentTimeMillis();
        CategorySummary[] artistSummary = mediaManagerClient.getCategorySummarySync(0, ConstCategoryType.ARTIST);
        end = System.currentTimeMillis();
        duration = end - start;
        sumSyncInvocationDuration[ConstCategoryType.ARTIST] += duration;
        maxSyncInvocationDuration[ConstCategoryType.ARTIST] = Math.max(maxSyncInvocationDuration[ConstCategoryType.ARTIST], duration);
        System.out.println("Artist Summary retrieved in " + duration + " milliseconds");
        
        // Get genre
        start = System.currentTimeMillis();
        CategorySummary[] genreSummary = mediaManagerClient.getCategorySummarySync(0, ConstCategoryType.GENRE);
        end = System.currentTimeMillis();
        duration = end - start;
        sumSyncInvocationDuration[ConstCategoryType.GENRE] += duration;
        maxSyncInvocationDuration[ConstCategoryType.GENRE] = Math.max(maxSyncInvocationDuration[ConstCategoryType.GENRE], duration);
        System.out.println("Genre Summary retrieved in " + duration + " milliseconds");
      }
      
      for (int i = 0; i < 3; i++)
      {
        System.out.println("type: " + i + " max async: " + maxAsyncInvocationDuration[i] + " avg async: " + (sumAsyncInvocationDuration[i] / 1000) + " max sync: " + maxSyncInvocationDuration[i] + " avg sync: " + (sumSyncInvocationDuration[i] / 1000));
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
    catch (HaviException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public static void main(String[] args)
  {
    // Create the application
    DumpCatagories application = new DumpCatagories();
    
    // Run the application
    application.run();
    
    // All done
    System.exit(0);
  }
}
