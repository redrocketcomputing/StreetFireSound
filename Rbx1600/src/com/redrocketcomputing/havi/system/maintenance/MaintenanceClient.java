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
 * $Id: MaintenanceClient.java,v 1.2 2005/03/28 16:43:53 iain Exp $
 */

package com.redrocketcomputing.havi.system.maintenance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.havi.system.types.GUID;

import com.redrocketcomputing.havi.constants.ConstCmmIpWellKnownAddresses;
import com.redrocketcomputing.havi.util.GuidUtil;
import com.redrocketcomputing.util.log.LoggerSingleton;
import com.redrocketcomputing.util.simplefiletransfer.FileReceiver;
import com.redrocketcomputing.util.simplefiletransfer.FileSender;
import com.redrocketcomputing.util.simplefiletransfer.FileTransferListener;

/**
 *  Simple mechanism to provide access to a maintenance service via TCP/IP, e.g. for
 *  backup/restore and update
 */
public abstract class MaintenanceClient
{
  private Socket socket;                // network socket for communication
  private DataInputStream  in;          // network input stream
  private DataOutputStream out;         // network output stream
  private byte handshakeFeatureCode;    // the feature requested in the handshake

  /**
   * Synchronously opens the connection to the server and perform the handshake
   * @param guid The GUID to which the connection is made
   * @param featureCode The maintenance command to execute
   * @throws MaintenanceException Throw if a problem is detected establishing a connection
   * with the remote maintenance server
   */
  public MaintenanceClient(GUID guid, byte featureCode) throws MaintenanceException
  {
    // Check the guid
    if (guid == null)
    {
      // Badness
      throw new IllegalArgumentException("GUID is null");
    }

    // Crack the GUID
    String ipAddress = GuidUtil.extractIpAddress(guid);
    int ipPort = GuidUtil.extractIpPort(guid);

    // connect to maintenance port (offset from GUID's base port)
    connect(ipAddress, ipPort + ConstCmmIpWellKnownAddresses.MAINTENANCE_ADDRESS, featureCode);
  }

 /**
  * Synchronously opens the connection to the server, and perform the handshake
  * @param ipAddress The IP address to which the connection is made
  * @param ipPort The IP port number to which the connection is made
  * @param featureCode The maintenance command to execute
  * @throws MaintenanceException Throw if a problem is detected establishing a connection
  * with the remote maintenance server
  */
  public MaintenanceClient(String ipAddress, int ipPort, byte featureCode) throws MaintenanceException
  {
    // Check the ip address
    if (ipAddress == null)
    {
      // Badness
      throw new IllegalArgumentException("ipAddress is null");
    }

    // connect
    connect(ipAddress, ipPort, featureCode);
  }

  /**
   *  Synchronously opens the connection to the server, and performs the handshake
   *  @param socket a freshly opened socket connected to the maintenance server
   *  @throws MaintenanceIOException in the case of an IO error during handshake
   *  @throws MaintenanceHandshakeException in the case of handshake failure
   */
  private void connect(String ipAddress, int ipPort, byte featureCode) throws MaintenanceHandshakeException, MaintenanceIOException
  {
    try
    {
      // Log debug
      LoggerSingleton.logDebugCoarse(this.getClass(), "connect", "attempting connection to " + ipAddress + ":" + ipPort);

      // Create the socket
      socket = new Socket(ipAddress, ipPort);
      socket.setSoLinger(true, 10);

      // set up the streams
      in  = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());

      // create the handshake message
      MaintenanceHandshakeRequest handshakeRequest = new MaintenanceHandshakeRequest(featureCode);

      // send it
      handshakeRequest.write(out);

      // Read response
      MaintenanceHandshakeResponse handshakeResponse = new MaintenanceHandshakeResponse(in);

      // throw exception in error case, don't want to provide an invalid object
      if (handshakeResponse.getResponseCode() != MaintenanceConstants.HANDSHAKE_RESPONSE_SUCCESS)
      {
        LoggerSingleton.logError(MaintenanceClient.class, "connect", "response code was " + handshakeResponse.getResponseCode());
        throw new MaintenanceHandshakeException(featureCode);
      }

      // Log debug
      LoggerSingleton.logDebugCoarse(this.getClass(), "connect", "handshake successful");
    }
    catch (UnknownHostException e)
    {
      // Translate
      throw new MaintenanceIOException(e.toString());
    }
    catch (IOException e)
    {
      e.printStackTrace();
      // Translate
      throw new MaintenanceIOException(e.toString());
    }
  }

  /**
   * Close and release all MaintenanceClient resources.
   */
  public void close()
  {
    // Check to make sure the socket is not null
    if (socket == null)
    {
      return;
    }

    try
    {
      socket.close();
      socket = null;
    }
    catch (IOException e)
    {
      // Log warning
      LoggerSingleton.logWarning(this.getClass(), "close", e.toString());
    }
  }

  /**
   * Start the maintenance request running, which mostly involves
   * a file transfer. This must be provided by the subclass.
   * @param file The file to transfer
   * @param listener A progress listener
   */
  public abstract void start(File file, FileTransferListener listener);

  /**
   *  Asynchronously receive and save a file from the device
   *
   *  The feature code specified in the constructor must correspond to a feature
   *  which involves receiving a file.
   *  The file receive will commence immediately
   *
   *  @param listener a listener for progress/error notification
   */
  protected void startAsynchronousReceive(File file, FileTransferListener listener)
  {
    // create the receiver and set it going
    FileReceiver fileReceiver = new FileReceiver(file, in);
    fileReceiver.startAsynchronousReceive(listener);
  }

  /**
   *  Asynchronously send a file to the device
   *
   *  The feature code specified in the constructor must correspond to a feature
   *  which involves sending a file.
   *  The file send will commence immediately
   *
   *  @param listener a listener for progress/error notification
   */
  protected void startAsynchronousSend(File file, FileTransferListener listener)
  {
    // create the sender and set it going
    FileSender fileSender = new FileSender(file, out);
    fileSender.startAsynchronousSend(listener);
  }

  /**
   *  Read a completion notification.  Should only be called after a file of the correct length
   *  has been transferred.  Will block until received.
   */
  public MaintenanceCompletionNotification receiveCompletionNotification() throws IOException
  {
    return new MaintenanceCompletionNotification(in);
  }
}
