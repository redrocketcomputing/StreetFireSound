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
 * $Id: ConstCmmIpErrorCode.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.constants;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public interface ConstCmmIpErrorCode
{
  public final static short NOT_READY = (short)0x8000;
  public final static short SIZE = (short)0x8001;
  public final static short NOT_INTERESTED = (short)0x8002;
  public final static short UNKNOWN_GUID = (short)0x8003;
  public final static short IO = (short)0x8004;
  public final static short ADDRESS = (short)0x8005;
  public final static short GARP = (short)0x8006;
  public final static short CONFIGURATION = (short)0x8007;
  public final static short NOT_FOUND = (short)0x8008;
}
