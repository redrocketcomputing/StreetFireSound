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
 * $Id: UdpLogServer.java,v 1.1 2005/02/22 03:54:49 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger.server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.redrocketcomputing.appframework.Application;
import com.redrocketcomputing.util.configuration.ComponentConfiguration;
import com.redrocketcomputing.util.configuration.ConfigurationProperties;
import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public class UdpLogServer extends Application
{
  private final static String DEFAULT_ROOT_DIRECTORY_NAME = "/var/udplog/";
  private final static int DEFAULT_PORT = 47000;
  private final static int DEFAULT_MESSAGE_SIZE = 1400;
  private final static int DEFAULT_TIMEOUT = 60000;
  private final static int DEFAULT_MAX_AGE = 5;
  private final static String DEFAULT_TAG = "00:50:c2:ff:ff:ff";

  private class TagEntry
  {
    private String tag;
    private PrintStream ps;
    private int age = 0;

    public TagEntry(String tag, String fileName) throws IOException
    {
      // Save the tag
      this.tag = tag;

      // Create the output streams
      ps = new PrintStream(new FileOutputStream(fileName, true));
    }

    public void close()
    {
      // Close the output stream
      ps.close();
    }

    public final void flush()
    {
      // Flush the output stream and age the stream
      ps.flush();
      ++age;
    }

    public final int getAge()
    {
      return age;
    }

    public final String getTag()
    {
      return tag;
    }

    public final void log(String message)
    {
      // Write message and reset the age
      ps.println(message);
      age = 0;
    }
  }

  private Map tagMap = new TreeMap();
  private String rootLogDirectoryName;
  private DatagramSocket socket;
  private DatagramPacket packet;
  private int messageSize;
  private int inetPort;
  private int timeout;
  private int tagAge;

  /**
   * Constructor for UdpLogServer.
   */
  public UdpLogServer(String[] args)
  {
    // Construct super class
    super(args);

    try
    {
      // Construct component configuration
      ComponentConfiguration configuration = ConfigurationProperties.getInstance().getComponentConfiguration("udp.log");

      // Get configuration information
      rootLogDirectoryName = configuration.getProperty("root.dir", DEFAULT_ROOT_DIRECTORY_NAME);
      inetPort = configuration.getIntProperty("inet.port", DEFAULT_PORT);
      messageSize = configuration.getIntProperty("message.size", DEFAULT_MESSAGE_SIZE);
      timeout = configuration.getIntProperty("timeout", DEFAULT_TIMEOUT);
      tagAge = configuration.getIntProperty("tag.age", DEFAULT_MAX_AGE);

      // Create datagram socket and packet
      socket = new DatagramSocket(inetPort);
      packet = new DatagramPacket(new byte[messageSize], messageSize);

      // Log start up
      LoggerSingleton.logInfo(this.getClass(), "UdpLogServer", "initialized in " + rootLogDirectoryName + " and listening on port " + inetPort);
    }
    catch (SocketException e)
    {
      LoggerSingleton.logFatal(this.getClass(), "UdpLogServer", e.toString());
      System.exit(1);
    }
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    // Log  start
    LoggerSingleton.logInfo(this.getClass(), "run", "starting");

    // Loop forever
    while (true)
    {
      try
      {
        // Set socket timeout
        socket.setSoTimeout(timeout);

        // Reset the data packet
        packet.setLength(messageSize);

        // Receive the packet
        socket.receive(packet);

        // Read from the input
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength()));
        String tag = dis.readUTF();
        String message = dis.readUTF();

        // Check for default tag and replace with ip address
        if (tag.equals(DEFAULT_TAG))
        {
          tag = packet.getAddress().getHostAddress();
        }

        // Lookup tag in map
        TagEntry entry = (TagEntry)tagMap.get(tag);
        if (entry == null)
        {
          // Create new print stream
          entry = new TagEntry(tag, rootLogDirectoryName + tag + ".log");

          // Add the tag entry to the tag map
          tagMap.put(tag, entry);

          // Log addition
          LoggerSingleton.logInfo(this.getClass(), "run", "added new device with tag " + tag);
        }

        // Create new Date
        Date currentDate = new Date();

        // Log the new message the the tag entry
        entry.log(currentDate + " " + message);
        LoggerSingleton.logDebugCoarse(this.getClass(), "run", currentDate + " " + tag + " " + message);
      }
      catch (InterruptedIOException e)
      {
        // Flush the tag map
        flushTagMap();
      }
      catch (SocketException e)
      {
        LoggerSingleton.logError(this.getClass(), "run", e.toString());
      }
      catch (IOException e)
      {
        LoggerSingleton.logError(this.getClass(), "run", e.toString());
      }
    }
  }

  private void flushTagMap()
  {
    // Loop through the tag map and flush and remove any aged entries
    for (Iterator iterator = tagMap.values().iterator(); iterator.hasNext();)
    {
      // Extrac the entry
      TagEntry element = (TagEntry) iterator.next();

      // Flush the entry
      element.flush();

      // Check the age and remove if to old
      if (element.getAge() >= tagAge)
      {
        // Remove element
        tagMap.remove(element.getTag());

        // Log removal
        LoggerSingleton.logInfo(this.getClass(), "flushTagMap", "removed device with tag " + element.tag);
      }
    }
  }

  public static void main(String[] args)
  {
    // Create the server
    UdpLogServer server = new UdpLogServer(args);

    // Run it
    server.run();
  }
}
