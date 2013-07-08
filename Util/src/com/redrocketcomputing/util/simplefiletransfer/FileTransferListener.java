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
 * $Id: FileTransferListener.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.util.simplefiletransfer;

import java.io.IOException;

/**
 *  Used to provide notification during an asynchronous file transfer
 */
public interface FileTransferListener
{
  /**
   *  Called to notify a listener of progress during file transfer
   *
   *  If bytes transferred = totalbytes, the transfer is complete and no further
   *  notifications will be provided
   */
  public void progressNotification(int bytesTransferred, int totalBytes);

  /**
   *  Called to notify a listener of an error during file transfer
   *
   *  If this method is called transfer has failed and no further notifications will be provided
   */
  public void errorNotification(IOException e);
}
