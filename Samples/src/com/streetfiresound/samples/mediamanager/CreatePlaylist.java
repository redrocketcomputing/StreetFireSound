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
 * $Id: CreatePlaylist.java,v 1.1 2005/03/20 00:20:59 stephen Exp $
 */
package com.streetfiresound.samples.mediamanager;

import java.util.ArrayList;
import java.util.List;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.DateTime;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.havi.util.TimeDateUtil;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.streetfiresound.commands.HaviCommandLineApplication;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;
import com.streetfiresound.mediamanager.mediacatalog.constants.ConstMediaCatalogErrorCode;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogAsyncResponseHelper;
import com.streetfiresound.mediamanager.mediacatalog.rmi.MediaCatalogClient;
import com.streetfiresound.mediamanager.mediacatalog.types.HaviMediaCatalogException;
import com.streetfiresound.mediamanager.mediacatalog.types.MLID;
import com.streetfiresound.mediamanager.mediacatalog.types.MediaMetaData;
import com.streetfiresound.mediamanager.playlistcatalog.rmi.PlayListCatalogClient;
import com.streetfiresound.mediamanager.playlistcatalog.types.HaviPlayListCatalogException;
import com.streetfiresound.mediamanager.playlistcatalog.types.PlayList;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CreatePlaylist extends HaviCommandLineApplication
{
  /**
   * @param args
   */
  public CreatePlaylist()
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
      
      // Search catalog for rock entries
      System.out.println("searching catalog for 'rock'");
      MediaCatalogClient mediaManagerClient = new MediaCatalogClient(softwareElement, remoteSeid);
      MediaMetaData[] rockMetaData = mediaManagerClient.searchMetaDataSync(60000, "rock");
      System.out.println("found " + rockMetaData.length + " items");
      
      // Create play with these items
      System.out.println("creating 'Rock Play List'");
      PlayListCatalogClient playListClient = new PlayListCatalogClient(softwareElement, remoteSeid);
      DateTime currentDateTime = TimeDateUtil.getCurrentDateTime();
      PlayList newPlayList = new PlayList();
      newPlayList.setTitle("Rock Play List");
      newPlayList.setArtist("various");
      newPlayList.setGenre("rock");
      newPlayList.setIrcode((byte)1);
      newPlayList.setInitialTimeStamp(currentDateTime);
      newPlayList.setLastUpdateTimeStamp(currentDateTime);
      List contentList = new ArrayList(rockMetaData.length);
      long frames = 0;
      for (int i = 0; i < rockMetaData.length; i++)
      {
        // Add item
        contentList.add(rockMetaData[i].getMediaLocationId());
        frames += TimeDateUtil.toFrames(rockMetaData[i].getPlaybackTime());
      }
      newPlayList.setContent((MLID[])contentList.toArray(new MLID[contentList.size()]));
      MLID newPlayListMlid = playListClient.createPlayListSync(0, newPlayList);
      System.out.println("'Rock Play List' has " + newPlayList);
      
      // Create another playlist 
      System.out.println("creating 'Rock Play List 2'");
      newPlayList.setTitle("Rock Play List 2");
      newPlayList.setIrcode((byte)2);
      MLID newPlayListMlid2 = playListClient.createPlayListSync(0, newPlayList);
      System.out.println("'Rock Play List 2' has " + newPlayListMlid2);
      
      // Retrieve the second play list
      System.out.println("getting 'Rocket Play List 2'");
      PlayList playList2 = playListClient.getPlayListSync(0, newPlayListMlid2);
      
      // Trim the contents and update
      System.out.println("Triming 'Rocket Play List 2' to " + playList2.getContent().length / 2 + " items");
      MLID[] newContents = new MLID[playList2.getContent().length / 2];
      System.arraycopy(playList2.getContent(), 0, newContents, 0, newContents.length);
      playList2.setContent(newContents);
      System.out.println("updating 'Rocket Play List 2'");
      playListClient.updatePlayListSync(0, newPlayListMlid2, playList2);
      
      // Create PlayList meta data from the catalog
      MediaMetaData[] playListMetaData = mediaManagerClient.getMetaDataSync(0, newPlayListMlid);
      MediaMetaData[] playListMetaData2 = mediaManagerClient.getMetaDataSync(0, newPlayListMlid2);
      
      // Remove the new play list
      System.out.println("removing 'Rocket Play List 2'");
      playListClient.removePlayListSync(0, newPlayListMlid2);
      
      // All done clean up
      System.out.println("done");
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
    catch (HaviPlayListCatalogException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }
  
  public static void main(String[] args)
  {
    // Create the application
    CreatePlaylist application = new CreatePlaylist();
    
    // Run the application
    application.run();
    
    // All done
    System.exit(0);
  }
}
