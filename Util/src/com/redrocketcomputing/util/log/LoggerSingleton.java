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
 * $Id: LoggerSingleton.java,v 1.5 2005/03/21 21:29:13 iain Exp $
 */

package com.redrocketcomputing.util.log;

import com.redrocketcomputing.appframework.logger.*;
import com.redrocketcomputing.util.Util;

/**
 *
 * Copyright (c) 2002</p>
 * Red Rocket Computing, Inc</p>
 * @author Daniel Bernstein, Stephen Street
 * @version 1.0
 * @see Logger
 */
public class LoggerSingleton
{
  private static volatile Logger logger = new DefaultLogger();

	/**
	 * Method logDebugFine.
	 * @param aClass
	 * @param method
	 * @param message
	 */
  public static final void logDebugFine(Class aClass, String method, String message)
  {
    logger.log(makeMessage("DEBUGF ", aClass, method, message));
  }

  /**
   *
   * @param aClass
   * @param method
   * @param message
   */
  public static final void logDebugCoarse(Class aClass, String method, String message)
  {
    logger.log(makeMessage("DEBUGC ", aClass, method, message));
  }

  /**
   *
   * @param aClass
   * @param method
   * @param message
   */
  public static final void logInfo(Class aClass, String method, String message)
  {
    logger.log(makeMessage("INFO   ", aClass, method, message));
  }

  /**
   *
   * @param aClass
   * @param method
   * @param message
   */
  public static final void logWarning(Class aClass, String method, String message)
  {
    logger.log(makeMessage("WARNING", aClass, method, message));
  }

  /**
   *
   * @param aClass
   * @param method
   * @param message
   */
  public static final void logError(Class aClass, String method, String message)
  {
    logger.log(makeMessage("ERROR  ", aClass, method, message));
  }

  /**
   *
   * @param aClass
   * @param method
   * @param message
   */
  public static final void logFatal(Class aClass, String method, String message)
  {
    logger.log(makeMessage("FATAL  ", aClass, method, message));
  }

  public static final Logger bindLogger(Logger newLogger)
  {
    // Save old loger
    Logger oldLogger = logger;

    // Bind a difference logger
    logger = newLogger;

    // Return the old logger
    return oldLogger;
  }

  /**
   * Return the current Logger interface
   * @return The current Logger
   */
  public final static Logger getLogger()
  {
    return logger;
  }

  private final static String makeMessage(String level, Class componentClass, String method, String message)
  {
    // Get index to strip package name from the class name
    String className = componentClass.getName();
    int index = className.lastIndexOf('.') + 1;

    // Create string buffer
    StringBuffer buffer = new StringBuffer();
    buffer.append(level);

    // XXX:0000:20050317iain: the following will add time since app init @ millisecond resolution and align columns
    // XXX:0000:20050317iain: disabled, as should somehow be implemented as a log filter
    boolean addTimeAndAlign = false; // change to true if your name is iain
    if (addTimeAndAlign)
    {
      buffer.append(" - ");

      // Create string buffer, padding to align columns where possible
      int millis = Util.getMillisSinceInit();
      buffer.append("[");
      buffer.append(Util.padStringWithSpaces(Util.formatSecondsWithHour(millis/1000), 8, false));
      buffer.append(":");
      buffer.append(Util.padStringWithZeros(String.valueOf(millis % 1000), 3, true));
      buffer.append("] ");

      // optionally print the thread name
      boolean printThread = true;
      if (printThread)
      {
        buffer.append("[");
        String threadName = Thread.currentThread().getName();

        // truncate
        threadName = threadName.length() > 6 ? threadName.substring(0, 6) : threadName;
        buffer.append(Util.padStringWithSpaces(threadName, 6, true));
        buffer.append("] ");
      }
      buffer.append(Util.padStringWithSpaces(className.substring(index) + '.' + method, 45, true));
      buffer.append(" - ");
      buffer.append(message);

      // done
      return buffer.toString();
    }

    buffer.append(" - ");
    buffer.append(className.substring(index));
    buffer.append('.');
    buffer.append(method);
    buffer.append(" - ");
    buffer.append(message);

    return buffer.toString();
  }
}

