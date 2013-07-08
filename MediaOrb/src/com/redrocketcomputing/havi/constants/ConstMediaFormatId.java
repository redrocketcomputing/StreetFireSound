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
 * $Id: ConstMediaFormatId.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */
package com.redrocketcomputing.havi.constants;

import org.havi.system.types.MediaFormatId;
import org.havi.system.types.VendorId;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ConstMediaFormatId
{
  public final static MediaFormatId NO_MEDIA_PRESENT = new MediaFormatId(VendorId.ZERO, (byte)0xff, (short)0xffff, (short)0xffff);
  public final static MediaFormatId UNKNOWN_FORMAT = new MediaFormatId(VendorId.ZERO, (byte)0x00, (short)0x0000, (short)0x0000);
  public final static MediaFormatId DISC_NO_MEDIA = new MediaFormatId(VendorId.ZERO, (byte)0x02, (short)0xffff, (short)0xffff);
  public final static MediaFormatId DISC_UNKNOWN_FORMAT = new MediaFormatId(VendorId.ZERO, (byte)0x02, (short)0x0000, (short)0x0000);
  public final static MediaFormatId DISC_CD_DA = new MediaFormatId(VendorId.ZERO, (byte)0x02, (short)0x0001, (short)0x0001);
}
