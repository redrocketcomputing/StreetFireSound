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
 * $Id: PathConfiguration.java,v 1.1 2005/02/22 03:46:07 stephen Exp $
 */

package com.streetfiresound.codegenerator.parser;

/**
 * @author george
 */
public interface PathConfiguration
{
  public final String COMPARETYPE  = "havi";

  public final String REDROCKETBASEPATH = "com.redrocketcomputing.havi.system";


  public final String JAVA_IO = "java.io.*";
  public final String JAVA_UTIL  = "java.util.*";

  public final String SOFTWARE_ELEMENT_PATH = "org.havi.system";
  public final String HAVI_CLIENT_PATH = "org.havi.system";

  public final String GENERAL_PACKAGE = "org.havi.system";
  public final String GENERAL_CONSTANT_PATH = "constants";
  public final String GENERAL_TYPE_PATH = "types";
  public final String GENERAL_EXCEPTION_PATH = "types";
  public final String GENERAL_SYSTEM_PATH="rmi";

  public final String DCM_PATH = "org.havi.dcm";
  public final String FCM_PATH = "org.havi.fcm";
}
