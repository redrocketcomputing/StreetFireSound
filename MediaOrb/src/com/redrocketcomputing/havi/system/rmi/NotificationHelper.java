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
 * $Id: NotificationHelper.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.system.rmi;

import org.havi.system.HaviListener;
import org.havi.system.SoftwareElement;
import org.havi.system.constants.ConstGeneralErrorCode;
import org.havi.system.constants.ConstProtocolType;
import org.havi.system.types.HaviByteArrayInputStream;
import org.havi.system.types.HaviUnmarshallingException;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;
import org.havi.system.types.Status;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * @author stephen
 *
 */
public abstract class NotificationHelper extends HaviListener
{
  protected SoftwareElement softwareElement;
  protected OperationCode opCode;

  /**
   * Constructor for NotificationHelper.
   */
  public NotificationHelper(SoftwareElement softwareElement, OperationCode opCode)
  {
    // Check the parameter
    if (softwareElement == null || opCode == null)
    {
      // Bad
      throw new IllegalArgumentException("parameter is null");
    }

    // Save the parameters
    this.softwareElement = softwareElement;
    this.opCode = opCode;
  }

  /**
   * @see org.havi.system.HaviListener#receiveMsg(boolean, byte, SEID, SEID, Status, HaviByteArrayInputStream)
   */
  public boolean receiveMsg(boolean haveReplied, byte protocolType, SEID sourceId, SEID destId, Status state, HaviByteArrayInputStream payload)
  {
    // Check for debug
    if (softwareElement.isDebug())
    {
      // Log some debug
      LoggerSingleton.logDebugCoarse(this.getClass(), "receiveMsg", softwareElement.getSeid() + " haveReplied: " + haveReplied + " protocolType: " + protocolType + " sourceId: " + sourceId + " destinationId: " + destId + " state: " + state);
    }

    //ignore if replied already
//    if (haveReplied || protocolType != ConstProtocolType.HAVI_RMI || state.getErrCode() != ConstGeneralErrorCode.SUCCESS)
    if (protocolType != ConstProtocolType.HAVI_RMI || state.getErrCode() != ConstGeneralErrorCode.SUCCESS)
    {
      return false;
    }

    try
    {
      // Read RMI header
      HaviRmiHeader header = new HaviRmiHeader(payload);

      // Check for debug
      if (softwareElement.isDebug())
      {
        // Log some debug
        LoggerSingleton.logDebugCoarse(this.getClass(), "receiveMsg", "sourceId: " + softwareElement.getSeid() + " destinationId: " + destId + " "+ header);
      }

      // Match the operation code and request
      if (!opCode.equals(header.getOpCode()) || (header.getControlFlags() & 0x01) != 0)
      {
        // Not for use
        return false;
      }

      // Forward on
      return receiveNotification(sourceId, header, payload);
    }
    catch (HaviUnmarshallingException e)
    {
      // Log the error
      LoggerSingleton.logError(this.getClass(), "recieveMsg", e.toString());

      // We did not handle it
      return false;
    }
  }

  /**
   * Abstract method which must be implement by all subclass. At this point we know that the OperationCode of the message
   * matches the configured OperationCode and that all other message parameters have been met.
   * @param sourceId The source of the message
   * @param header The header of the message
   * @param payload The remaining bytes of the message
   * @return boolean True is the message was handled, False otherwise
   */
  public abstract boolean receiveNotification(SEID sourceId, HaviRmiHeader header, HaviByteArrayInputStream payload);

}
