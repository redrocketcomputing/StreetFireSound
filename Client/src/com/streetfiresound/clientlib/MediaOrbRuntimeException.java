/*
 * Copyright (C) 2005 by StreetFire Sound Labs
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

import org.havi.system.types.HaviException;
import org.havi.system.types.HaviMarshallingException;

/**
 *  "Converts" a havi or related exception to a runtime exception, with the
 *  original exception properly chained
 *
 *  @author iain huxley
 */
public class MediaOrbRuntimeException extends ClientRuntimeException
{

  /**
   *  @param the cause of this exception, must be either null (if no cause exception) or of type HaviException / HaviMarshallingException
   */
  public MediaOrbRuntimeException(String message, Exception cause)
  {
    super(message, cause);

    // since we're loose in the constructor (for expediency), check this really is a media orb related exception
    if (!(cause instanceof HaviException || cause instanceof HaviMarshallingException))
    {
      throw new IllegalArgumentException("invalid media orb exception cause: " + cause);
    }
  }

  public MediaOrbRuntimeException(Exception cause)
  {
    this("MediaOrbRuntimeException: '" + cause.getMessage() + "'", cause);
  }
}
