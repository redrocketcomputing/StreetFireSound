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
 * $Id: CddbConstant.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

public interface CddbConstant
{
  //keys
  public static final String COMMAND_PARAMETERS_KEY = "cpk";
  public static final String CDDB_HOST_KEY = "cdhk";
  public static final String USER_NAME_KEY = "unk";
  public static final String CLIENT_HOST_KEY = "chk";
  public static final String CLIENT_NAME_KEY = "cnk";
  public static final String CLIENT_VERSION_KEY = "cvk";
  public static final String PROTOCOL_VERSION = "pv";

  //cddb commands
  public static final String COMMAND_LSCAT = "cddb+lscat";
  public static final String COMMAND_QUERY = "cddb+query";
  public static final String COMMAND_READ = "cddb+read";
  public static final String COMMAND_SITES = "sites";
  public static final String COMMAND_STATUS = "stat";

}