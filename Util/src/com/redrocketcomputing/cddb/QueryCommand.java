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
 * $Id: QueryCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

import java.util.Map;


public class QueryCommand extends CddbCommand
{
  public QueryCommand(Map params, DiscInfo discInfo, TrackInfo[] trackInfo)
  {
    super(params);
    //format command
    StringBuffer sb = new StringBuffer("");
    sb.append(CddbConstant.COMMAND_QUERY);
    sb.append("+");
    sb.append(CddbIdCalculator.buildId(discInfo, trackInfo));
    sb.append("+");
    sb.append(discInfo.getTracks());
    sb.append("+");
    sb.append(CddbIdCalculator.buildOffsets(discInfo, trackInfo));
    sb.append("+");
    sb.append((discInfo.getMinutes() * 60 + discInfo.getSeconds()));
    addCommandParameter(sb.toString());

  }

  protected CddbResponse createCddbResponse()
  {
    return new QueryResponse();
  }
}
