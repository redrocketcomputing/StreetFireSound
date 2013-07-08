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
 * $Id: CddbIdCalculator.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;


public class CddbIdCalculator
{
  public static String buildId(DiscInfo disc, TrackInfo[] tracks)
  {
    int t = disc.getMinutes() * 60 + disc.getSeconds();

    int offset = 2;
    int n = 0;
    for (int i = 0; i < disc.getTracks(); i++)
    {
      offset = offset + tracks[i].getMinutes() * 60 + tracks[i].getSeconds();
      n = n + sum(offset);
    }

    int nshift = (n % 0xff) << 24;
    int tshift = t << 8;
    int id = (n % 0xff) << 24 | t << 8 | disc.getTracks();
    return Integer.toHexString(id);
  }

  public static int sum(int n)
  {
    int ret = 0;
    while (n > 0)
    {
      ret = ret + (n % 10);
      n = n / 10;
    }
    return ret;
  }

  public static int total(TrackInfo[] tracks)
  {
    int t = 0;
    for (int i = 0; i < tracks.length; i++)
    {
      t = t + tracks[i].getMinutes() * 60 + tracks[i].getSeconds();
    }
    return t;
  }

  public static String buildOffsets(DiscInfo disc, TrackInfo[] tracks)
  {
    int frames = 150;
    String offsets = "150";
    for (int i = 0; i < disc.getTracks() - 1; i++)
    {
      frames = frames + (tracks[i].getMinutes() * 60 * 75) + (tracks[i].getSeconds() * 75);
      offsets = offsets + "+" + frames;
    }
    return offsets;
  }

  //   public static int HexStringToInt(String hex)
  //   {
  //        int i, length, retval;
  //        char c;
  //
  //        length = hex.length();
  //        retval = 0;
  //
  //        for (i = 0; i < length; i++)
  //        {
  //           c = hex.charAt(i);
  //           retval += HexCharToInt(c) << (4*((length-1)-i)) ;
  //        }
  //
  //
  //        return retval;
  //   }

}