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
 * $Id: ConstEventSubscription.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.constants;

import org.havi.system.types.AppEventId;
import org.havi.system.types.EventId;
import org.havi.system.types.SEID;
import org.havi.system.types.SystemEventId;
import org.havi.system.types.VendorEventId;
import org.havi.system.types.VendorId;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author unascribed
 * @version 1.0
 */

public interface ConstEventSubscription
{
//  public static final EventId ALL_SYSTEM_EVENT_ID = new EventId(ConstEventIdSchema.SYSTEM, new SystemEventId((short)0xff));
//  public static final EventId ALL_VENDOR_EVENT_ID = new EventId(ConstEventIdSchema.VENDOR, new VendorEventId((short)0xff, new VendorId()));
//  public static final EventId ALL_APPLICATION_EVENT_ID = new EventId(ConstEventIdSchema.APPLICATION, new AppEventId((short)0xff, new SEID()));


  public static final EventId ALL_SYSTEM_EVENT_ID = new SystemEventId((short) 0x00ff);
  public static final EventId ALL_VENDOR_EVENT_ID = new VendorEventId((short)0xff, new VendorId());
  public static final EventId ALL_APPLICATION_EVENT_ID = new AppEventId((short)0xff, new SEID());


}
