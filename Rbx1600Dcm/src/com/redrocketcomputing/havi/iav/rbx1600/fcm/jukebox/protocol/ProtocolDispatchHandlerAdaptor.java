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
 * $Id: ProtocolDispatchHandlerAdaptor.java,v 1.1 2005/02/22 03:49:20 stephen Exp $
 */

package com.redrocketcomputing.havi.iav.rbx1600.fcm.jukebox.protocol;

import java.io.IOException;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author Stephen Street
 * @version 1.0
 */

public abstract class ProtocolDispatchHandlerAdaptor implements ProtocolDispatchHandler
{
  protected ProtocolByteArrayInputStream pbais = new ProtocolByteArrayInputStream();

  public ProtocolDispatchHandlerAdaptor()
  {
  }

  public void dispatch(ProtocolEventListener listener, byte[] data, int offset, int length)
  {
    try
    {
      // Bind input stream
      pbais.fromByteArray(data, offset, length);

      // Forward
      dispatch(listener, pbais);
    }
    catch (IOException ex)
    {
    	StringBuffer buffer = new StringBuffer(" data:");
    	for (int i = 0; i < data.length; i++)
      {
      	buffer.append(':');
      	buffer.append(Integer.toHexString(data[i]&0xff));
      }
      LoggerSingleton.logError(this.getClass(), "dispatch", "IOException: " + ex.getMessage() + buffer.toString());
    }
  }

  protected abstract void dispatch(ProtocolEventListener listener, ProtocolByteArrayInputStream sbais) throws IOException;
}
