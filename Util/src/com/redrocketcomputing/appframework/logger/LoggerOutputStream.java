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
 * $Id: LoggerOutputStream.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoggerOutputStream extends OutputStream
{
  private ByteArrayOutputStream baos;
  private String level;

  /**
   *
   */
  public LoggerOutputStream(String level)
  {
    // Set the level for the stream
    this.level = level + " - ";

    // Create the underlay byte array output stream
    baos = new ByteArrayOutputStream(1024);
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException
  {
    // Check for newline
    if (b == '\n')
    {
      // Convert output stream to string and send to logger
      LoggerSingleton.getLogger().log(level + baos.toString());

      // Clear the byte stream
      baos.reset();
      
      // All done
      return;
    }

    // Add to byte array
    baos.write(b);
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#close()
   */
  public void close() throws IOException
  {
    baos.close();
  }
}
