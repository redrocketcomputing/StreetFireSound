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
 * $Id: TcpServiceHandler.java,v 1.2 2005/03/17 02:27:32 stephen Exp $
 */

package com.redrocketcomputing.havi.system.cmm.ip.tcp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.havi.system.types.GUID;
import org.havi.system.types.HaviCmmIpAddressException;
import org.havi.system.types.HaviCmmIpConfigurationException;
import org.havi.system.types.HaviCmmIpException;
import org.havi.system.types.HaviCmmIpIoException;
import org.havi.system.types.HaviCmmIpNotReadyException;
import org.havi.system.types.HaviCmmIpUnknownGuidException;

import com.redrocketcomputing.appframework.event.EventDispatch;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.appframework.taskpool.TaskAbortedException;
import com.redrocketcomputing.appframework.taskpool.TaskPool;
import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.system.cmm.ip.ServiceHandler;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpEngine;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpEntry;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpGoneDevicesEventListener;
import com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNetworkReadyEventListener;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 * 
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class TcpServiceHandler implements ServiceHandler, GarpGoneDevicesEventListener, GarpNetworkReadyEventListener
{
  private final static int DEFAULT_RETRIES = 5;
  private final static long DEFAULT_RETRY_DELAY = 1000;
  private final static int DEFAULT_QUEUE_SIZE = 5;

  private int receiveCounter = 0;
  private int sendCounter = 0;
  private int errorCounter = 0;
  private Map readEndPointMap = Collections.synchronizedMap(new HashMap());
  private Map writeEndPointMap = Collections.synchronizedMap(new HashMap());
  private EventDispatch eventDispatcher;
  private GarpEngine garp;
  private volatile GUID localGuid = null;

  /**
   * Construct a TcpServiceHandler
   * 
   * @param localGuid The local GUID for this device
   * @param garp The GUID Address Resolution Protocol engine
   * @throws HaviCmmIpException Thrown if there is a problem creating the service handler
   */
  public TcpServiceHandler(GUID localGuid, GarpEngine garp) throws HaviCmmIpException
  {
    // Check the parameter
    if (localGuid == null || garp == null)
    {
      throw new IllegalArgumentException("TcpServiceHandler.TcpServiceHandler");
    }

    // Save the parameters
    this.localGuid = localGuid;
    this.garp = garp;

    try
    {
      // Try to get task pool service
      TaskPool taskPool = (TaskPool)ServiceManager.getInstance().find(TaskPool.class);
      if (taskPool == null)
      {
        // Opps very bad
        throw new HaviCmmIpConfigurationException("can not find task pool service");
      }

      // Launch acceptor task
      taskPool.execute(new TcpAcceptor(localGuid, garp, DEFAULT_QUEUE_SIZE, readEndPointMap));

      // Try to get the event dispatch service
      eventDispatcher = (EventDispatch)ServiceManager.getInstance().find(EventDispatch.class);
      if (eventDispatcher == null)
      {
        // Opps very bad
        throw new HaviCmmIpConfigurationException("can not find event dispatch service");
      }

      // Register for gone device event
      eventDispatcher.addListener(this);

    }
    catch (TaskAbortedException e)
    {
      // Translate
      throw new HaviCmmIpConfigurationException(e.toString());
    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.ServiceHandler#close()
   */
  public void close()
  {
    // Release resources, this will cause all future service invokation to fail
    localGuid = null;

    // Unregister from the event dispatcher
    eventDispatcher.removeListener(this);

    // Loop through the write end points and close sockets. NOTE THE ORDER OF CLOSE IS IMPORTANT
    for (Iterator writeEndPointIterator = writeEndPointMap.values().iterator(); writeEndPointIterator.hasNext();)
    {
      // Extract the end point
      TcpWriteEndPoint element = (TcpWriteEndPoint)writeEndPointIterator.next();

      // Close the end point
      element.close();
    }

    // Remove all entries
    writeEndPointMap.clear();

    // Loop through the read end points and close the sockets
    for (Iterator readEndPointIterator = readEndPointMap.values().iterator(); readEndPointIterator.hasNext();)
    {
      // Extract end point
      TcpReadEndPoint element = (TcpReadEndPoint)readEndPointIterator.next();

      // Close the end point
      element.close();
    }

    // Remove all entries
    readEndPointMap.clear();
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.ServiceHandler#send(GUID, byte[])
   */
  public void send(GUID guid, byte[] buffer) throws HaviCmmIpException
  {
    // Forward to local send
    send(guid, buffer, 0, buffer.length);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.ServiceHandler#send(GUID, byte[], int, int)
   */
  public void send(GUID guid, byte[] buffer, int offset, int length) throws HaviCmmIpException
  {
    // Check local guid to see if we are ready
    if (localGuid == null)
    {
      // Update error count
      errorCounter++;

      // Not ready
      throw new HaviCmmIpNotReadyException("local GUID not set");
    }

    // Loop try to send message a couple of times
    HaviCmmIpException lastException = null;
    for (int i = 0; i < DEFAULT_RETRIES; i++)
    {
      try
      {
        //        // Validate the guid
        //        if (!garp.isActive(guid))
        //        {
        //          // Update error count
        //          errorCounter++;
        //  
        //          // Uhmmmm
        //          throw new HaviCmmIpUnknownGuidException(guid.toString());
        //        }
        //
        // Get the write end point
        TcpWriteEndPoint writeEndPoint = writeEndPointLookup(guid);

        // Forward to the end point
        writeEndPoint.send(buffer, offset, length);

        // Update send counter
        sendCounter++;

        // All done
        return;
      }
      catch (HaviCmmIpAddressException e)
      {
        // Save this exception
        lastException = e;

        // Wait while
        delay(DEFAULT_RETRY_DELAY);

        // Update error count
        errorCounter++;
      }
      catch (HaviCmmIpIoException e)
      {
        // Save this exception
        lastException = e;

        // Wait while
        delay(DEFAULT_RETRY_DELAY);

        // Update error count
        errorCounter++;
      }
    }

    // Try to remove the end point
    TcpWriteEndPoint writeEndPoint = (TcpWriteEndPoint)writeEndPointMap.remove(guid);
    if (writeEndPoint != null)
    {
      // Close it up
      writeEndPoint.close();
    }

    // Forces a network reset
    garp.forceNetworkReset();

    // Problem
    throw lastException;
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.ServiceHandler#receive(GUID)
   */
  public byte[] receive(GUID guid) throws HaviCmmIpException
  {
    try
    {
      // Check local guid to see if we are ready
      if (localGuid == null)
      {
        // Not ready
        throw new HaviCmmIpNotReadyException("local GUID not set");
      }

      // Validate the guid
      //      if (!garp.isActive(guid))
      //      {
      //        // Uhmmmm
      //        throw new HaviCmmIpUnknownGuidException(guid.toString());
      //      }

      // Lookup the read end point using the GUID
      TcpReadEndPoint readEndPoint = (TcpReadEndPoint)readEndPointMap.get(guid);
      if (readEndPoint == null)
      {
        // Not ready
        throw new HaviCmmIpNotReadyException(guid.toString() + " not in read end point map");
      }

      // Receive from the end point
      byte[] message = readEndPoint.receive();

      // Update the receive counter
      receiveCounter++;

      // All done
      return message;
    }
    catch (HaviCmmIpException e)
    {
      // Update error count
      errorCounter++;

      // Rethrow
      throw e;
    }
    //    catch (HaviCmmIpUnknownGuidException e)
    //    {
    //      // Update error count
    //      errorCounter++;
    //
    //      // Rethrow
    //      throw e;
    //    }
    //    catch (HaviCmmIpNotReadyException e)
    //    {
    //      // Update error count
    //      errorCounter++;
    //
    //      // Rethrow
    //      throw e;
    //    }
    //    catch (HaviCmmIpException e)
    //    {
    //      // Try to remove the end point
    //      TcpReadEndPoint readEndPoint = (TcpReadEndPoint) readEndPointMap.remove(guid);
    //
    //      // Close if is we found it
    //      if (readEndPoint != null)
    //      {
    //        // Close it up
    //        readEndPoint.close();
    //      }
    //
    //      // Update error count
    //      errorCounter++;
    //
    //      // Force a network reset
    //      garp.forceNetworkReset();
    //
    //      // Rethrow
    //      throw e;
    //    }
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpGoneDevicesEventListener#goneDevicesEvent(GUID[])
   */
  public void goneDevicesEvent(GUID[] guids)
  {
    // Flush gone devices
    flushConnections(guids);
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.garp.GarpNetworkReadyEventListener#networkReadyEvent(GUID[], GUID[], GUID[], GUID[])
   */
  public void networkReadyEvent(GUID[] newDevices, GUID[] goneDevices, GUID[] activeDevices, GUID[] nonactiveDevices)
  {
    // Flush nonactive devices
    flushConnections(nonactiveDevices);
  }

  /**
   * Flush connections from end point maps
   * 
   * @param guids The array of end point GUIDs to flush
   */
  private void flushConnections(GUID[] guids)
  {
    // Loop throught the GUIDs and flush from internal maps
    for (int i = 0; i < guids.length; i++)
    {
      // Remove write end point
      TcpWriteEndPoint writeEndPoint = (TcpWriteEndPoint)writeEndPointMap.remove(guids[i]);

      // Close the end point
      if (writeEndPoint != null)
      {
        writeEndPoint.close();
      }

      // Remote read end point
      TcpReadEndPoint readEndPoint = (TcpReadEndPoint)readEndPointMap.remove(guids[i]);

      // Close the end point
      if (readEndPoint != null)
      {
        readEndPoint.close();
      }
    }
  }

  /**
   * Connect to a remote device using the specified GUID
   * 
   * @param remoteGuid The remote device to connect to
   * @return TcpWriteEndPoint The newly connected end point
   * @throws HaviCmmIpException Thrown if there is a problem connecting to the remote device
   */
  private TcpWriteEndPoint connect(GUID remoteGuid) throws HaviCmmIpException
  {
    StringBuffer addressString = null;
    int remotePort = -1;
    Socket socket = null;

    try
    {
      // Lookup to GARP entry for the GUID
      GarpEntry remoteGarpEntry = garp.resolve(remoteGuid);
      if (remoteGarpEntry == null)
      {
        throw new HaviCmmIpAddressException("can't find " + remoteGuid + " in GARP cache");
      }

      // Convert bytes to string
      byte[] address = remoteGarpEntry.getAddress();
      addressString = new StringBuffer();
      addressString.append(address[0] & 0xff);
      addressString.append('.');
      addressString.append(address[1] & 0xff);
      addressString.append('.');
      addressString.append(address[2] & 0xff);
      addressString.append('.');
      addressString.append(address[3] & 0xff);

      // Build remote port
      remotePort = remoteGarpEntry.getPort() + ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS;

      // Create an INET address
      InetAddress inetAddress = InetAddress.getByName(addressString.toString());

      // Create a new socket connected to the address and port
      //      socket = connectSocket(inetAddress, remotePort);
      // Try to connection
      socket = new Socket(inetAddress, remotePort);

      // Create write end point
      return new TcpWriteEndPoint(localGuid, remoteGuid, socket);
    }
    catch (UnknownHostException e)
    {
      // Close the socket
      exceptionSocketClose(socket);

      // Translate
      throw new HaviCmmIpAddressException(remoteGuid.toString() + " bad address: " + addressString + '@' + remotePort);
    }
    catch (SocketException e)
    {
      // Close the socket
      exceptionSocketClose(socket);

      // Translate
      throw new HaviCmmIpIoException(remoteGuid.toString() + " detected SocketException: " + e.getMessage());
    }
    catch (IOException e)
    {
      // Close the socket
      exceptionSocketClose(socket);

      // Translate
      throw new HaviCmmIpIoException(remoteGuid.toString() + " detected IOException: " + e.getMessage());
    }
  }

  /**
   * Close the socket and eat any exceptions
   * 
   * @param socket The socket to close
   */
  private void exceptionSocketClose(Socket socket)
  {
    try
    {
      // Check for null socket
      if (socket != null)
      {
        // Ok, close it up
        socket.close();
      }
    }
    catch (IOException e)
    {
      // Eat the exception
    }
  }

  private Socket connectSocket(InetAddress inetAddress, int port) throws IOException
  {
    // Loop trying to connect to remote end
    IOException lastException = null;
    for (int i = 0; i < DEFAULT_RETRIES; i++)
    {
      try
      {
        // Log last exception if present
        if (lastException != null)
        {
          LoggerSingleton.logWarning(this.getClass(), "connectSocket", lastException.toString() + " retrying");
        }

        // Try to connection
        Socket socket = new Socket(inetAddress, port);

        // Success
        return socket;
      }
      catch (IOException e)
      {
        // Save the last exception
        lastException = e;

        // Delay before trying again
        delay(DEFAULT_RETRY_DELAY);
      }
    }

    // Rethrow the last exception
    throw lastException;
  }

  private void delay(long milliseconds)
  {
    try
    {
      Thread.sleep(milliseconds);
    }
    catch (InterruptedException e)
    {
      // Ignore interruptions
      Thread.currentThread().interrupted();
    }
  }

  private synchronized TcpWriteEndPoint writeEndPointLookup(GUID guid) throws HaviCmmIpException
  {
    // Lookup the write end point using the GUID
    TcpWriteEndPoint writeEndPoint = (TcpWriteEndPoint)writeEndPointMap.get(guid);
    if (writeEndPoint == null)
    {
      // Try to create the write end point
      writeEndPoint = connect(guid);

      // Add to the map
      writeEndPointMap.put(guid, writeEndPoint);
    }

    // Return the end point
    return writeEndPoint;
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.ServiceHandler#getServiceId()
   */
  final public int getServiceId()
  {
    return ConstCmmIpWellKnownAddresses.TCP_MESSAGE_ADDRESS;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "TcpServiceHandler: send: " + sendCounter + " receive: " + receiveCounter + " errors: " + errorCounter;
  }

  /**
   * @see com.redrocketcomputing.havi.system.cmm.ip.ServiceHandler#info(PrintStream, String[])
   */
  public void dump(PrintStream printStream, String[] arguments)
  {
    // Print header
    printStream.println("TcpServiceHandler: " + (localGuid != null ? "ACTIVE" : "CLOSED"));
    printStream.println("  Sent:     " + sendCounter);
    printStream.println("  Received: " + receiveCounter);
    printStream.println("  Errors:   " + errorCounter);

    // Loop through the read end points
    for (Iterator readEndPointIterator = readEndPointMap.values().iterator(); readEndPointIterator.hasNext();)
    {
      // Extract the read end point
      TcpReadEndPoint element = (TcpReadEndPoint)readEndPointIterator.next();

      // Print read end point
      printStream.println("  " + element.toString());
    }

    // Loop through the write end points
    for (Iterator writeEndPointIterator = writeEndPointMap.values().iterator(); writeEndPointIterator.hasNext();)
    {
      // Extract the read end point
      TcpWriteEndPoint element = (TcpWriteEndPoint)writeEndPointIterator.next();

      // Print read end point
      printStream.println("  " + element.toString());
    }
  }
}