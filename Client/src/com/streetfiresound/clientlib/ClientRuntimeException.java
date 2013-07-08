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
package com.streetfiresound.clientlib;

/**
 *  Base class for any kind of exception passed on the client (the cause may
 *  be on the remote side).
 *
 *  "Converts" an exception to a runtime exception, with the
 *  original exception chained.  Ideally implementations should use
 *  a subclass of this class, but it may be used directly for
 *  generic exceptions which don't warrant their own class, or before code
 *   has been refined.
 *
 *  @author iain huxley
 */
public class ClientRuntimeException extends RuntimeException
{
  /**
   *  @param cause exception that caused this.  may be null
   */
  public ClientRuntimeException(String message, Exception cause)
  {
    // basic wrapper only
    super(message, cause);
  }
}
