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
 * $Id: SubscriptionEntry.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */
package com.redrocketcomputing.havi.system.em;

import org.havi.system.types.OperationCode;
import org.havi.system.types.SEID;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class SubscriptionEntry
{
  private SEID seid;
  private OperationCode opCode;
  
  public SubscriptionEntry(SEID seid, OperationCode opCode)
  {
    this.seid = seid;
    this.opCode = opCode;
  }
  
  public boolean equals(Object o)
  {
    // Check type
    if (o instanceof SubscriptionEntry)
    {
      // Cast it up
      SubscriptionEntry other = (SubscriptionEntry)o;
      
      // Equate fields
      return seid.equals(other.seid) && opCode.equals(other.opCode);
    }
    
    // Wrong type
    return false;
  }

  public int hashCode()
  {
    return 198529038 + seid.hashCode() + opCode.hashCode();
  }

  
  public final OperationCode getOpCode()
  {
    return opCode;
  }
  
  public final SEID getSeid()
  {
    return seid;
  }
  
  public String toString()
  {
    return "SubscriptionEntry[ " + seid + ":" + opCode + ']';
  }
}
