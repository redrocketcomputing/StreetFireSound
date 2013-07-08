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
 * $Id: FileSender.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.util.simplefiletransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 *  Utility class for a simple TCP/IP binary file send
 *
 *  Sends the length of the file as an integer (limits file size to 2Gb)
 *  then the contents of the file
 */
public class FileSender
{
  /** buffer size = 16 Kb */
  public static final int SEND_BUFFER_SIZE = 16 * 1024;

  private DataOutputStream         outputStream;     // output stream on which to send the file
  private File                 file;             // the file to send
  private FileTransferListener listener = null;  // listener for asynchronous send

  /**
   *  @param outputStream the output stream on which to send the file
   */
  public FileSender(File file, DataOutputStream outputStream)
  {
    // set members
    this.file = file;
    this.outputStream = outputStream;
  }

  /**
   *  Start sending the file asynchronously
   *  @param listener for notification of progress or error
   */
  public void startAsynchronousSend(FileTransferListener listener)
  {
    // set listener
    this.listener = listener;

    // create and launch a thread
    (new SendThread()).start();
  }

  /**
   *  Send the file data
   *
   *  This method will block until the file is fully sent
   */
  public void send() throws IOException
  {
    DataInputStream fileInputStream = null;

    // open the file
    fileInputStream = new DataInputStream(new FileInputStream(file));

    // create buffer
    byte[] buffer = new byte[SEND_BUFFER_SIZE];

    // get the file length
    int length = (int)file.length();  //XXX:0:20040916iain: ignore 2Gb+ size prob

    // write the file length
    outputStream.writeInt(length);

    // loop sending the file data one chunk at a time
    int bytesSent = 0;
    while (bytesSent < length)
    {
      // calc the amount to send in this loop iteration
      int chunkSize = Math.min(length - bytesSent, buffer.length);

      // read the amount from the file
      fileInputStream.read(buffer, 0, chunkSize);

      // send it
      outputStream.write(buffer, 0, chunkSize);

      // increment bytes sent
      bytesSent += chunkSize;

      // notify listener of progress
      if (listener != null)
      {
        listener.progressNotification(bytesSent, length);
      }
    }

    // flush the stream to make sure it's all done
    outputStream.flush();
    fileInputStream.close();
  }

  /**
   *  Simple thread for async use
   */
  class SendThread extends Thread
  {
    public void run()
    {
      try
      {
        // perform the send
        send();
      }
      catch (IOException e)
      {
        // report the error
        listener.errorNotification(e);
      }
    }
  }
}
