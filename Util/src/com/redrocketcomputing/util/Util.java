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
 * $Id: Util.java,v 1.4 2005/03/21 21:08:53 iain Exp $
 */

package com.redrocketcomputing.util;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collection;
import java.util.Iterator;

/**
 * Miscellaneous utility methods too small to warrant their own class
 */
public class Util
{
  private static String spacesString = "                                                                                                                                                   ";
  private static String zerosString  = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
  private static long initTime = -1;
  static
  {
    // keep time util was first initialized
    initTime = System.currentTimeMillis();
  }

  /**
   * protected constructor help enforce zeroton structure
   */
  protected Util() {}

  /**
   *  convenience sleep method when sleep interruptions don't matter
   */
  public static void sleep(int millis)
  {
    try
    {
      Thread.sleep(millis);
    }
    catch (InterruptedException e)
    {
    }
  }

  /**
   * formats seconds into mm:ss format
   * @return a string representing the specified number of seconds in above format
   */
  public static String formatSeconds(int seconds)
  {
     SimpleDateFormat format = new SimpleDateFormat("mm:ss");
     return format.format(new Date((long)seconds*1000));
  }

  /**
   * format seconds in hh:mm:ss format
   * @return string representation of time in h:mm:ss format
   */
  public static String formatSecondsWithHour(int seconds)
  {
      int hours = seconds/3600;
      int minutes = (seconds % 3600)/60;
      seconds = seconds % 60;

      return hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" +  (seconds < 10 ? "0" : "") + seconds;
  }

  /**
   *  get the number of milliseconds since first Util initialization
   */
  public static int getMillisSinceInit()
  {
    return (int)(System.currentTimeMillis() - initTime);
  }

  /**
   *  pad a string with spaces so that it takes up pad characters if string.length < pad (otherwise string is returned unchanged)
   *  @param padRight if true, padding will be on the right, if false, on the left.
   */
  public static String padStringWithSpaces(String string, int pad, boolean padRight)
  {
    return padString(string, spacesString, pad, padRight);
  }

  /**
   *  pad a string with zeros so that it takes up pad characters if string.length < pad (otherwise string is returned unchanged)
   *  @param padRight if true, padding will be on the right, if false, on the left.
   */
  public static String padStringWithZeros(String string, int pad, boolean padRight)
  {
    return padString(string, zerosString, pad, padRight);
  }

  /**
   *  pad a string with so that it takes up pad characters if string.length < pad (otherwise string is returned unchanged)
   *  @param pad number of chars to pad to
   *  @param padRight if true, padding will be on the right, if false, on the left.
   *  @param padString a string containing the pad characters (generally all one char), must be as long as pad
   */
  public static String padString(String string, String padString, int pad, boolean padRight)
  {
    if (pad > padString.length())
    {
      throw new IllegalArgumentException("pad may not be more than " + padString.length());
    }
    // if string length exceeds the pad amount, it is returned unchanged
    if (string.length() >= pad)
    {
      return string;
    }
    // append the correct number of spaces on the selected end
    if (padRight)
    {
      return string + padString.substring(0, pad - string.length());
    }
    else
    {
      return padString.substring(0, pad - string.length()) + string;
    }
  }

  /**
   *  Returns a stack trace as a single string
   */
  public static String getStackTrace(Throwable throwable)
  {
    // create the necessary writers
    StringWriter sw = new StringWriter(1000);  // start with a decent size buffer
    PrintWriter  pw = new PrintWriter(sw);

    // start with the throwable's name/message
    pw.println(throwable.toString());

    // write the stack trace
    throwable.printStackTrace(pw);

    // return the result
    return sw.toString();
  }

  /**
   *  Returns a date string in the format YYYY-MM-DD of today's date
   */
  public static String getDateString()
  {
    // create a Calender object for current day
    Calendar calendar = Calendar.getInstance();

    // set up pieces
    String yyyy = String.valueOf(calendar.get(Calendar.YEAR));
    String mm   = String.valueOf(calendar.get(Calendar.MONTH)+1);
    String dd   = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    // return assembled string
    return (yyyy + "-" + mm + "-" + dd);
  }

  /**
   *  Checks to see if a collection's entries are all of a given type exactly (if a subclass will return false - see allEntriesAreAssignableTo(..)
   */
  public static boolean allEntriesAreOfType(Class type, Collection collection)
  {
    for (Iterator i=collection.iterator(); i.hasNext();)
    {
      Object entry = i.next();
      if (!entry.getClass().equals(type))
      {
        return false;
      }
    }
    return true;
  }

  /**
   *  Checks to see if a collection's entries are assignable to a given type
   */
  public static boolean allEntriesAreAssignableTo(Class type, Collection collection)
  {
    for (Iterator i=collection.iterator(); i.hasNext();)
    {
      Object entry = i.next();
      if (!type.isAssignableFrom(entry.getClass()))
      {
        return false;
      }
    }
    return true;
  }

}
