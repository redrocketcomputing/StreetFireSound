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
 * $Id: TimeDateUtil.java,v 1.3 2005/03/22 20:43:27 stephen Exp $
 */
package com.redrocketcomputing.havi.util;

import java.util.Calendar;
import java.util.Date;

import org.havi.fcm.types.TimeCode;
import org.havi.system.types.DateTime;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TimeDateUtil
{
  public final static TimeCode TIMECODE_ZERO = new TimeCode((byte)0, (byte)0, (byte)0, (byte)0);
  public final static DateTime DATETIME_ZERO = new DateTime((short)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0);
  
  public final static DateTime getCurrentDateTime()
  {
    Calendar current = Calendar.getInstance();
    current.setTime(new Date());
    return new DateTime((short)current.get(Calendar.YEAR), (byte)current.get(Calendar.MONTH), (byte)current.get(Calendar.DAY_OF_MONTH), (byte)current.get(Calendar.HOUR_OF_DAY), (byte)current.get(Calendar.MINUTE), (byte)current.get(Calendar.SECOND), (byte)current.get(Calendar.MILLISECOND));
  }
  
  public final static long toLong(TimeCode timeCode)
  {
    return (timeCode.getHour() << 24) | (timeCode.getMinute() << 16) | (timeCode.getSec() << 8) | timeCode.getFrame();
  }
  
  public final static long toFrames(TimeCode timeCode)
  {
    return (timeCode.getHour() * 60 * 60 * 75) + (timeCode.getMinute() * 60 * 75) + (timeCode.getSec() * 75) + timeCode.getFrame(); 
  }
  
  public final static TimeCode fromFrames(long frames)
  {
    byte timeCodeHours = (byte)(frames / (60 * 60 * 75));
    byte timeCodeMinutes = (byte)((frames / (60 * 75)) - (timeCodeHours * 60));
    byte timeCodeSeconds = (byte)((frames / 75) - (timeCodeHours * 60 * 60) - (timeCodeMinutes * 60));
    byte timeCodeFrames = (byte)(frames - (timeCodeHours * 60 * 60 * 75) - (timeCodeMinutes * 60 * 75) - (timeCodeSeconds * 75));
    return new TimeCode(timeCodeHours, timeCodeMinutes, timeCodeSeconds, timeCodeFrames);
  }
}
