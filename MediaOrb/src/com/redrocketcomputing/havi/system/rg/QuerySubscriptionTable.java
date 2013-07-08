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
 * $Id: QuerySubscriptionTable.java,v 1.2 2005/02/24 03:30:23 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rg;

import java.util.Iterator;
import java.util.Map;

import org.havi.system.SoftwareElement;
import org.havi.system.registry.rmi.MatchFoundMessageBackClient;
import org.havi.system.registry.rmi.NewSoftwareElementAttributesEventNotificationListener;
import org.havi.system.types.Attribute;
import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMsgException;
import org.havi.system.types.HaviRegistryException;
import org.havi.system.types.HaviRegistryNetworkException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.Query;
import org.havi.system.types.SEID;

import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.AbstractTask;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationHelper;
import com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
class QuerySubscriptionTable implements MsgWatchOnNotificationListener, NewSoftwareElementAttributesEventNotificationListener
{
	private class DispatchTask extends AbstractTask
	{
		private SEID[] seids = new SEID[1];
		private Attribute[] table;
    private QuerySubscriptionEntry[] queries;

		public DispatchTask(SEID seid, Attribute[] table, QuerySubscriptionEntry[] queries)
		{
			// Save the parameters
			this.seids[0] = seid;
			this.table = table;
      this.queries = queries;
		}

    /**
     * @see com.redrocketcomputing.appframework.taskpool.Task#getTaskName()
     */
    public String getTaskName()
    {
      return "QuerySubscriptionTable::DispatchTask(" + seids[0] + ')';
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
    	// Loop through looking for match
    	for (int i = 0; i < queries.length; i++)
      {
      	// Match
      	MatchFoundMessageBackClient client = queries[i].matches(table);
      	if (client != null)
      	{
          try
          {
            // Fire the message
            client.matchFoundSync(0, queries[i].getQueryId(), seids);
          }
          catch (HaviException e)
          {
          	// Log the error and continue
          	LoggerSingleton.logError(this.getClass(), "run", e.toString());
          }
      	}
      }
    }
	}

  private Map subscriptions = new ListMap();
  private int nextQueryId = 0;
	private SoftwareElement softwareElement;
	private MsgWatchOnNotificationHelper watchHelper;
	private TaskPool taskPool;

  /**
   * Constructor for QuerySubscriptionTable.
   */
  public QuerySubscriptionTable(SoftwareElement softwareElement, MsgWatchOnNotificationHelper watchHelper)
  {
    // Check parameters
    if (softwareElement == null || watchHelper == null )
    {
    	throw new IllegalArgumentException("softwareElement or watchHelper is null");
    }

    // Save the parameters
    this.softwareElement = softwareElement;
    this.watchHelper = watchHelper;

    // Try to get the task pool
    taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
    if (taskPool == null)
    {
    	// Bad ness
    	throw new IllegalStateException("task pool not found");
    }
  }

	public void close()
	{
    synchronized (subscriptions)
    {
    	// Loop through the subscription any match seids
    	for (Iterator iterator = subscriptions.entrySet().iterator(); iterator.hasNext();)
      {
        // Extract the entery
        QuerySubsciptionKey element = (QuerySubsciptionKey) iterator.next();
  
        // Remove from message watch helper
        watchHelper.removeListenerEx(element.getSeid(), this);
      }
  
      // Clear the subscriptions map
      subscriptions.clear();
    }
	}

	public int addSubscription(SEID seid, OperationCode opCode, Query query) throws HaviRegistryException
	{
    try
    {
      // Add to message watch helper
      watchHelper.addListenerEx(seid, this);
      
      // Create the 
      synchronized(subscriptions)
      {
        // Create new key and entry
        QuerySubsciptionKey key = new QuerySubsciptionKey(seid, ++nextQueryId);
        QuerySubscriptionEntry entry = new QuerySubscriptionEntry(key.getQueryId(), query, new MatchFoundMessageBackClient(opCode, softwareElement, seid));
  
        // Add to the map
        subscriptions.put(key, entry);
  
        // Return the query id
        return key.getQueryId();
      }
    }
    catch (HaviMsgException e)
    {
    	// Watch failed
    	watchHelper.removeListenerEx(seid, this);

   		// Translate
   		throw new HaviRegistryNetworkException(e.toString());
    }
	}

	public void removeSubscription(SEID seid, int queryId)
	{
    // Remove from message watch helper
    watchHelper.removeListener(seid, this);
    
    synchronized(subscriptions)
    {
  		// Remove from map
  		subscriptions.remove(new QuerySubsciptionKey(seid, queryId));
    }
	}

  /**
   * @see com.redrocketcomputing.havi.system.rmi.MsgWatchOnNotificationListener#MsgWatchOnNotification(SEID)
   */
  public void msgWatchOnNotification(SEID targetSeid)
  {
    synchronized(subscriptions)
    {
      // Build key array
      QuerySubsciptionKey[] keys = (QuerySubsciptionKey[])subscriptions.keySet().toArray(new QuerySubsciptionKey[subscriptions.size()]);
      
    	// Loop through the subscription any match seids
      for (int i = 0; i < keys.length; i++)
      {
        // Match seids
        if (targetSeid.equals(keys[i].getSeid()))
        {
        	// Remove this entry
        	subscriptions.remove(keys[i]);
        }
      }
    }
  }

  /**
   * @see org.havi.system.registry.rmi.NewSoftwareElementAttributesEventNotificationListener#newSoftwareElementAttributesEventNotification(SEID, SEID, Attribute[])
   */
  public void newSoftwareElementAttributesEventNotification(SEID posterSeid, SEID seid, Attribute[] table)
  {
    try
    {
      // Build query subscriptions
      QuerySubscriptionEntry[] queries;
      synchronized(subscriptions)
      {
        queries = (QuerySubscriptionEntry[])subscriptions.values().toArray(new QuerySubscriptionEntry[subscriptions.size()]);
      }
      
      // Dispatch the task
      taskPool.execute(new DispatchTask(seid, table, queries));
    }
    catch (TaskAbortedException e)
    {
    	// Log error
    	LoggerSingleton.logError(this.getClass(), "newSoftwareElementAttributes", e.toString());
    }
  }
}
