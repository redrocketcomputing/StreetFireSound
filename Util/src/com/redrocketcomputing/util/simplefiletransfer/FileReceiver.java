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
 * $Id: FileReceiver.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.util.simplefiletransfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;

import com.redrocketcomputing.util.log.LoggerSingleton;


/**
 *  Utility class for a simple TCP/IP binary file receive
 */
public class FileReceiver
{
  /** buffer size = 16 Kb */
  public static final int RECEIVE_BUFFER_SIZE = 16 * 1024;

  private DataInputStream inputStream;
  private File file;
  private FileTransferListener listener = null; // listener for asynchronous receive

  public FileReceiver(File file, DataInputStream inputStream)
  {
    // set members
    this.inputStream = inputStream;
    this.file = file;
  }

  /**
   *  Start receiving the file asynchronously
   *  @param listener for notification of progress or error
   */
  public void startAsynchronousReceive(FileTransferListener listener)
  {
    // set listener
    this.listener = listener;

    // create and launch a thread
    (new ReceiveThread()).start();
  }

  /**
   *  Receive the file data and write it to the file
   *  This method will block until the file is fully received and written to
   *  disk.
   *  @see startAsynchronousReceive
   */
  public void receive() throws IOException
  {
    // open the file
    FileOutputStream fileOutputStream = new FileOutputStream(file);

    // create buffer
    byte[] buffer = new byte[RECEIVE_BUFFER_SIZE];

    // convert from bytes in network byte order to int
    int length = inputStream.readInt();

    // loop receiving the file data one chunk at a time
    int bytesReceived = 0;
    while (bytesReceived < length)
    {
      // calc the amount to receive in this loop iteration
      int chunkSize = Math.min(length - bytesReceived, buffer.length);

      // read the amount from the file
      inputStream.readFully(buffer, 0, chunkSize);

      // send it
      fileOutputStream.write(buffer, 0, chunkSize);

      // increment bytes sent
      bytesReceived += chunkSize;

      // notify listener of progress
      if (listener != null)
      {
        listener.progressNotification(bytesReceived, length);
      }
    }

    // close the file stream to flush any unwritten data
    fileOutputStream.close();
  }

  /**
   *  Simple thread for async use
   */
  class ReceiveThread extends Thread
  {
    public void run()
    {
      try
      {
        // receive the file
        receive();
      }
      catch (IOException e)
      {
        // report the error
        listener.errorNotification(e);
      }
    }
  }
}

