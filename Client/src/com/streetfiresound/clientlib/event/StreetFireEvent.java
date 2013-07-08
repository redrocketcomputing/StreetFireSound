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
 * $Id $
 */
package com.streetfiresound.clientlib.event;

import com.streetfiresound.clientlib.event.EventDispatcher;

/**
 * @author iain huxley
 */
public abstract class StreetFireEvent implements Runnable
{
  public static final int NOT_APPLICABLE = -1; // to be used in place of a transactionId when it is not applicable
  public static final int NOT_SET = -2;             // to be used in place of a transactionId when it is in an unset state

  EventDispatcher dispatcher;          // the event dispatcher. A bit odd to keep it here, but the AWT event queue API we use requires an event to know how to dispatch itself
  private int transactionId = NOT_APPLICABLE;  // the transaction ID of the associated HAVi request, or NOT_APPLICABLE/NOT_SET

  /**
   * Construct an event with an associated transaction Id
   * @param transactionId the transaction ID of the associated HAVi request
   */
  public StreetFireEvent() {}

  /**
   * Construct an event with an associated transaction Id
   * @param transactionId the transaction ID of the associated HAVi request, or -1 if not applicable
   */
  public StreetFireEvent(int transactionId)
  {
    this.transactionId = transactionId;
  }

  /**
   * @return the transaction ID of the associated HAVi request, or -1 if not applicable
   */
  public int getTransactionId()
  {
    return transactionId;
  }

  /**
   * dispatch the event to listeners
   */
  public void run()
  {
    dispatcher.dispatchEvent(this);
  }

  public String toString()
  {
    return "StreetFireEvent[tid=" + transactionId + "]";
  }
}
