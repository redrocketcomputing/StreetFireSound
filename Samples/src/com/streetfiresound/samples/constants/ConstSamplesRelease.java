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
 * $Id: ConstSamplesRelease.java,v 1.1 2005/02/22 03:50:49 stephen Exp $
 */

package com.streetfiresound.samples.constants;

/**
 * @author stephen
 */
public final class ConstSamplesRelease
{
  private final static String RELEASE = "$Name:  $";

  public final static String getRelease()
  {
    // Check for space
    int start = RELEASE.indexOf(' ');
    if (start == -1)
    {
      return "DEVELOPMENT";
    }

    // String the release string
    String stripped = RELEASE.substring(start, RELEASE.length() - 1).trim();
    if (stripped.length() > 0)
    {
      return stripped;
    }

    // Development version
    return "DEVELOPMENT";
  }
}
