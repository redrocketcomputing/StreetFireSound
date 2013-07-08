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
 * $Id: AttributeSubscription.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.fcm;

import org.havi.system.constants.ConstApiCode;
import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

import com.redrocketcomputing.havi.util.AttributeValueComparatorFlyweight;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AttributeSubscription
{
  private final static byte[] EMPTY = new byte[0];

  private short notficationId = -1;
  private SEID seid = SEID.ZERO;
  private OperationCode opCode = new OperationCode(ConstApiCode.ANY, (byte)0xff);
  private short indicator = -1;
  private short comparator = -1;
  private byte[] matchValue = EMPTY;

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
   * @param comparator The comparator operator to used for matching attribute values
   * @param indicator The FcmAttributeIndicator for this subscriptions
   * @param matchValue The matchValue to use as the left hand side of the comparison operation
   */
  public AttributeSubscription(short notficationId, SEID seid, OperationCode opCode, short indicator, short comparator, byte[] matchValue)
  {
    this.notficationId = notficationId;
    this.seid = seid;
    this.opCode = opCode;
    this.indicator = indicator;
    this.comparator = comparator;
    this.matchValue = matchValue;
  }

  /**
   * Match an attribute value against the subscription match value using the specified comparator
   * @param value
   * @return
   */
  public boolean match(byte[] value)
  {
    // Match sure we have a comparator set
    if (comparator == -1)
    {
      return false;
    }

    return AttributeValueComparatorFlyweight.getComparator(comparator).match(matchValue, value);
  }

  /**
   * @return Returns the comparator.
   */
  public short getComparator()
  {
    return comparator;
  }

  /**
   * @param comparator The comparator to set.
   */
  public void setComparator(short comparator)
  {
    this.comparator = comparator;
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
   * @return Returns the matchValue.
   */
  public byte[] getValue()
  {
    return matchValue;
  }

  /**
   * @param matchValue The matchValue to set.
   */
  public void setValue(byte[] value)
  {
    this.matchValue = value;
  }
  /**
   * @return Returns the indicator.
   */
  public short getIndicator()
  {
    return indicator;
  }
  /**
   * @param indicator The indicator to set.
   */
  public void setIndicator(short indicator)
  {
    this.indicator = indicator;
  }
}
