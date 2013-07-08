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
 * $Id: AttributeSubscription.java,v 1.1 2005/02/22 03:41:17 stephen Exp $
 */

package com.streetfiresound.mediamanager.player;

import org.havi.system.constants.ConstApiCode;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AttributeSubscription
{
  private short notficationId = -1;
  private SEID seid = SEID.ZERO;
  private OperationCode opCode = new OperationCode(ConstApiCode.ANY, (byte)0xff);
  private int indicator = -1;

  /**
   * Construct empty AttributeSubscription
   */
  public AttributeSubscription()
  {
  }

  /**
   * Construct a initialized AttributeSubscription
   * @param notficationId A unique subscription ID
   * @param seid The SEID of the subscribing software element
   * @param opCode The OperationCode of the subscribing software element
   * @param indicator The FcmAttributeIndicator for this subscriptions
   */
  public AttributeSubscription(short notficationId, SEID seid, OperationCode opCode, int indicator)
  {
    this.notficationId = notficationId;
    this.seid = seid;
    this.opCode = opCode;
    this.indicator = indicator;
  }

  /**
   * @return Returns the notficationId.
   */
  public short getNotficationId()
  {
    return notficationId;
  }

  /**
   * @param notficationId The notficationId to set.
   */
  public void setNotficationId(short notficationId)
  {
    this.notficationId = notficationId;
  }

  /**
   * @return Returns the opCode.
   */
  public OperationCode getOpCode()
  {
    return opCode;
  }

  /**
   * @param opCode The opCode to set.
   */
  public void setOpCode(OperationCode opCode)
  {
    this.opCode = opCode;
  }

  /**
   * @return Returns the seid.
   */
  public SEID getSeid()
  {
    return seid;
  }

  /**
   * @param seid The seid to set.
   */
  public void setSeid(SEID seid)
  {
    this.seid = seid;
  }

  /**
   * @return Returns the indicator.
   */
  public int getIndicator()
  {
    return indicator;
  }

  /**
   * @param indicator The indicator to set.
   */
  public void setIndicator(int indicator)
  {
    this.indicator = indicator;
  }
}
