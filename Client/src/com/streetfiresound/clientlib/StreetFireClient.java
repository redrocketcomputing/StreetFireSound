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
 * $Id $
 */
package com.streetfiresound.clientlib;

import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstSoftwareElementType;
import org.havi.system.registry.rmi.RegistryClient;
import org.havi.system.types.GUID;
import org.havi.system.types.HaviException;
import org.havi.system.types.QueryResult;
import org.havi.system.types.SEID;
import org.havi.system.types.SimpleQuery;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.Task;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.constants.ConstRbx1600DcmInterfaceId;
import com.redrocketcomputing.havi.system.HaviApplication;
import com.redrocketcomputing.havi.system.rg.SimpleAttributeTable;
import com.redrocketcomputing.havi.system.rmi.EventManagerNotificationServerHelper;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.streetfiresound.clientlib.event.EventDispatcher;
import com.streetfiresound.mediamanager.constants.ConstMediaManagerInterfaceId;
import com.streetfiresound.client.AppFramework;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;
import java.awt.Component;


/**
 *  Primary class for the StreetFire Client Library
 *
 *  Client UI builders should extend this class and use its facilities
 *  @author iain huxley
 */
public abstract class StreetFireClient  //,MsgWatchOnNotificationListener XXX:00000000000000:20050301iain: implement for when the device gets shut off
{
  public static final int MAX_PLAYERS = 4; // maximum number of players the RBX1600 can control XXX:0000000000:20050301iain: retreive this from elsewhere

  private TaskPool              taskPool;           // global app taskpool

  private EventDispatcher       eventDispatcher;    // for dissemination of device side events

  private SEID                  mediaManagerSeid;   // software element id of media manager, initialized only when requested
  private SEID                  rbx1600DcmSeid;     // software element id of rbx1600Dcm, initialized only when requested

  private MediaCatalog          mediaCatalog;       // abstraction of remote media catalog
  private RemotePlayListCatalog playListCatalog;    // abstraction of remote playlist catalog
  private RemoteMediaPlayer     mediaPlayer;        // abstraction of remote media player
  private SlotScanner           slotScanner;        // abstraction of slot scanner

  private ContentMetadataCache  metadataCache;      // cache for metadata (indexed by MLID)

  private EventManagerNotificationServerHelper eventHelper; // the event helper for the local software element
  private SoftwareElement       softwareElement;    // local software element

  private int asyncRequestsOutstanding = 0;

  /**
   *  construct a client
   *
   *  XXX:0:20050325iain: consider renaming StreetFireMediaManagerClient ??
   *
   *  @param mediaManagerSeid seid of media manager to connect to
   */
  public StreetFireClient(SEID mediaManagerSeid) // throws HaviException
  {
    // Log start up
    LoggerSingleton.logInfo(this.getClass(), "<init>", "initializing...");

    // init members
    this.mediaManagerSeid = mediaManagerSeid;

    // Try to get the task pool
    taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
    if (taskPool == null)
    {
      throw new IllegalStateException("Can't find task pool");
    }

    //XXX:0000:20041214iain: hack, wait for havi init
    Util.sleep(2500);

    try
    {
      // Create local software element
      softwareElement = new SoftwareElement();
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("error initializing software element", e);
    }

    try
    {
      // init event manager client (needs to be common as only one allowed per local software element)
      eventHelper = new EventManagerNotificationServerHelper(softwareElement);
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("error initializing event manager notification helper", e);
    }

    // set up event dispatcher
    eventDispatcher = createEventDispatcher();


    //DatabaseMediaCatalog dbTest = new DatabaseMediaCatalog(this);
    //mediaCatalog    = new DatabaseMediaCatalog(this);


    // init catalog abstractions
    try
    {
      mediaCatalog    = new HaviRemoteMediaCatalog(this);
      playListCatalog = new RemotePlayListCatalog(this);
      mediaPlayer     = new RemoteMediaPlayer(this);
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("init failure", e);
    }

    // init metadata cache
    metadataCache = new ContentMetadataCache(this, 2048);
  }

  /**
   *  Abstract factory method - called (once) to create the event dispatcher
   */
  public abstract EventDispatcher createEventDispatcher();

  /**
   *  implementations should somehow present the passed error message (e.g. in a dialog) and then quit
   */
  public abstract void fatalError(String message);

  /**
   *  implementations should somehow present the passed error message (e.g. in a dialog)
   */
  public abstract void showError(String message);

  /**
   *  implementations should somehow present the passed info message (e.g. in a dialog)
   */
  public abstract void showInfo(String message);

  /**
   *  Get the RBX1600 DCM seid associated with this client's media manager
   *
   *  @return the corresponding RBX1600Dcm SEID, or null if not found
   */
  public SEID getRbx1600DcmSied()
  {
    try
    {
      // Build interface based registry query to find SEIDs of Rbx1600 DCMs
      SimpleAttributeTable attributes = new SimpleAttributeTable();
      attributes.setSoftwareElementType(ConstSoftwareElementType.DCM);
      attributes.setInterfaceId(ConstRbx1600DcmInterfaceId.RBX1600_DCM);
      RegistryClient registryClient = new RegistryClient(softwareElement);
      SEID[] resultSeids = registryClient.getElementSync(0, attributes.toQuery()).getSeidList();

      // Look for matching GUID
      for (int i = 0; i < resultSeids.length; i++)
      {
        // Check for match GUID
        if (resultSeids[i].getGuid().equals(mediaManagerSeid.getGuid()))
        {
          // Found it
          return resultSeids[i];
        }
      }
    }
    catch (HaviException e)
    {
      throw new MediaOrbRuntimeException("error searching for Rbx1600Dcm SEID", e);
    }
    return null;
  }

  /**
   *  Get the software element to be used with this client
   */
  public TaskPool getTaskPool()
  {
    return taskPool;
  }

  /**
   *  Execute a task in the task pool.  Particularly useful for code which can block, these
   *  should not be run on the AWT event dispatch thread.
   *  @throws ClientRuntimeException thrown when task is aborted, if caller needs TaskAbortedException
   *                                 getTaskPool().execute(task) should be used instead
   */
  public void executeTask(Task task)
  {
    try
    {
      // fire off the task
      taskPool.execute(task);
    }
    catch (TaskAbortedException e)
    {
      throw new ClientRuntimeException("task '" + task.getTaskName() + "' aborted", e);
    }
  }


  /**
   *  Get the event dispatcher associated with this client.
   *  Typically used to subscribe to events
   */
  public EventDispatcher getEventDispatcher()
  {
    return eventDispatcher;
  }

  /**
   *  Get the abstraction object for the remote media catalog
   */
  public MediaCatalog getMediaCatalog()
  {
    return mediaCatalog;
  }

  /**
   *  Get the abstraction object for the remote playlist catalog
   */
  public RemotePlayListCatalog getPlayListCatalog()
  {
    return playListCatalog;
  }

  public SEID getMediaManagerSeid()
  {
    return mediaManagerSeid;
  }

  /**
   *  Get the abstraction object for the remote playlist catalog
   */
  public RemoteMediaPlayer getMediaPlayer()
  {
    return mediaPlayer;
  }

  public ContentMetadataCache getMetadataCache()
  {
    return metadataCache;
  }

  public synchronized SlotScanner getSlotScanner(Component renderComponent)
  {
    if (slotScanner == null)
    {
      slotScanner = new SlotScanner(this, renderComponent);
    }
    return slotScanner;

  }

  /**
   *  Get the software element to be used with this client
   */
  public SoftwareElement getSoftwareElement()
  {
    return softwareElement;
  }

  /**
   *  return the event helper for the local software element
   */
  public EventManagerNotificationServerHelper getEventHelper()
  {
    return eventHelper;
  }

  /**
   *  Clients must call this method when they initiate an asynchronous request,
   *  and must call decrementAsyncRequests() when the response or error arrives.
   *
   *  Used to update the user interface state via setUIEnabled
   */
  public synchronized void incrementAsyncRequests()
  {
    assert asyncRequestsOutstanding >= 0;

    if (asyncRequestsOutstanding == 0)
    {
      assert lastAsyncRequestTime == -1;
      assert totalAsyncRequestsTimed == 0;
      lastAsyncRequestTime = System.currentTimeMillis();
      totalAsyncRequestsTimed = 1;
    }
    else
    {
      assert totalAsyncRequestsTimed > 0;
      totalAsyncRequestsTimed++;
    }

    asyncRequestsOutstanding++;
    updateUIState();
  }

  long lastAsyncRequestTime = -1;
  int totalAsyncRequestsTimed = 0;

  /**
   *  Clients must call this method when an asynchronous network response or error
   *  arrives
   *
   *  Used to update the user interface state via setUIEnabled
   */
  public synchronized void decrementAsyncRequests()
  {
    asyncRequestsOutstanding--;
    assert asyncRequestsOutstanding >= 0;
    assert totalAsyncRequestsTimed > 0;

    if (asyncRequestsOutstanding == 0)
    {
      assert lastAsyncRequestTime != -1;
      long timeTaken = System.currentTimeMillis() - lastAsyncRequestTime;
      LoggerSingleton.logDebugCoarse(StreetFireClient.class, "decrementAsyncRequests", "last havi async " + (totalAsyncRequestsTimed == 1 ? "request completed in " : String.valueOf(totalAsyncRequestsTimed) + " requests completed in a total time of ") + timeTaken + "ms");

      // log warning if it took over a second
      if (timeTaken > 1000)
      {
        LoggerSingleton.logWarning(StreetFireClient.class, "decrementAsyncRequests", "\n\n !!!WARNING!!! previous async request was slow to complete\n");
      }

      totalAsyncRequestsTimed = 0;
      lastAsyncRequestTime = -1;
    }
    updateUIState();
  }

  /** enabled/disable the UI according to whether there are requests outstanding */
  private void updateUIState()
  {
    setUIEnabled(asyncRequestsOutstanding == 0);
  }

//   /**
//    *  Notifies us when the device goes away
//    *  @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#msgWatchOnNotification(org.havi.system.types.SEID)
//    */
//   public void msgWatchOnNotification(SEID targetSeid)
//   {
//     System.out.println("lost contact with " + targetSeid);
//     //System.exit(1);
//   }

  /**
   *  Called by the system when an unexpected exception occurred.
   *  Override to show an informative error dialog, etc.
   */
  public void handleException(Exception e)
  {
    //XXX:00000000000000000000000
    LoggerSingleton.logError(getClass(), "handleException",  " client exception occurred: " + e);
    e.printStackTrace();
  }

  /**
   *  Called when the UI should be disabled until an action is complete.
   *  Implementation should prevent further user action and show an indication
   *  of waiting state, such as an hourglass cursor etc.
   *
   *  Noop default implementation supplied
   */
  public void setUIEnabled(boolean enabled) {}

  /**
   *  @param cds a list of ContentMetadata CD entries
   */
  public synchronized void saveCDList(File f, List cds) throws IOException
  {
    //XXX:0:20050330iain: sort cds by player then slot

    // open the file
    PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)));

    // write a header
    // XXX:0000:20050216iain: consider using a template file for this stuff
    writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
    writer.println("<html>");
    writer.println(" <head><title>RBX1600 CD catalog</title>");
    writer.println("   <style type=\"text/css\">");
    writer.println("     p { font-family: Verdana, sans-serif; font-size: 14 pt; }");
    writer.println("     .columntitles { font-size : 12pt; font-weight: bold; background: #FFE495; }");
    writer.println("     .odd { background : #EEEEEE }");
    writer.println("     .title { font-family: Verdana, sans-serif; font-size : 16pt; font-weight : bold }");
    writer.println("     td  { font-size : 8pt; text-align:left; font-family: Verdana, sans-serif; color : black; }");
    writer.println("    </style>");
    writer.println("  </head>");
    writer.println("  <body>");
    writer.println("    <center>");

    //     // create a map sorted by slot
    //     HashMap mapBySlot = new HashMap();
    //     Collection discs = this.discMap.values();


    //     int cumulativeTime = 0;
    //     for (Iterator i = discs.iterator(); i.hasNext() ;)
    //     {
    //       Disc disc = (Disc)i.next();

    //       // use a simple key, a string comprised of the player and slot
    //       mapBySlot.put(disc.getPlayerChannel() + ":" + disc.getPhysicalSlot(), disc);

    //       // total up time
    //       cumulativeTime += disc.playbackTimeValue();
    //     }

    // convert the time in seconds to a string
    String time = "?"; //Util.getInstance().formatSecondsWithHour(cumulativeTime);

    // lose the seconds
    //time = time.substring(0, time.length() - 3);

    // begin table with some summary info, logo
    writer.println("      <table border=1 cellpadding=2 cellspacing=0>");
    writer.println("        <tr>");
    writer.println("          <td valign=top colspan=3 style=\"border-width: 0; padding : 4px;\">");
    writer.println("            <p class=\"title\">RBX1600 CD Catalog</p>");
    writer.println("            <p>" + cds.size() + " discs</p><p>Generated " + Util.getDateString() + "</p>");
    writer.println("          </td>");
    writer.println("          <td colspan=4 valign=top align=right style=\"text-align: right; border-width: 0; padding : 4px;\">");
    writer.println("            <a href=\"http://www.streetfiresound.com\">");
    writer.println("              <img border=0 src=\"http://www.streetfiresound.com/HADES/CDListLogo.gif\">");
    writer.println("            </a><br>");
    writer.println("            <a href=\"http://www.streetfiresound.com\" style=\"font-size:10pt; text-decoration:none; color:#777777\">");
    writer.println("              www.streetfiresound.com");
    writer.println("            </a>");
    writer.println("          </td>");
    writer.println("        </tr>");

    // header row
    writer.println("        " + ContentMetadata.TO_HTML_STRING_FORMAT);

    int count = 0;
    for (Iterator i=cds.iterator(); i.hasNext(); )
    {
      String classString = (count++ % 2 == 0 ? "even" : "odd");

      // write disc info
      writer.println("      " + ((ContentMetadata)i.next()).toHtmlString(classString));
    }

    // close up the tags and stream
    writer.println("      </table>");
    writer.println("    </center>");
    writer.println("  </body>");
    writer.println("</html>");
    writer.flush();
    writer.close();
  }
}
