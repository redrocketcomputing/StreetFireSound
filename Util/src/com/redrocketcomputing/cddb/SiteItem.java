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
 * $Id: SiteItem.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

public class SiteItem
{
  private String host = null;
  private String relativeUrl = null;
  private String protocol = null;
  private int port = 0;
  private String latitude = null;
  private String longitude = null;
  private String description = null;

  public SiteItem()
  {
  }

  public void setHost(String host)
  {
    this.host = host;
  }

  public String getHost()
  {
    return this.host;
  }

  public void setProtocol(String protocol)
  {
    this.protocol = protocol;
  }

  public String getProtocol()
  {
    return this.protocol;
  }

  public void setPort(int port)
  {
    this.port = port;
  }

  public int getPort()
  {
    return this.port;
  }

  public void setRelativeUrl(String relativeUrl)
  {
    this.relativeUrl = relativeUrl;
  }

  public String getRelativeUrl()
  {
    return this.relativeUrl;
  }

  public void setLatitude(String latitude)
  {
    this.latitude = latitude;
  }

  public String getLatitude()
  {
    return this.latitude;
  }

  public void setLongitude(String longitude)
  {
    this.longitude = longitude;
  }

  public String getLongitude()
  {
    return this.longitude;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return this.description;
  }

  public String toString()
  {
    return ("SiteItem[host=" + this.host + "; protocol=" + this.protocol + "; port=" + this.port + "; relativeUrl=" + this.relativeUrl + "; latitude=" + this.latitude + "; longitude=" + this.longitude + "; description=" + this.description + "]");
  }
}
