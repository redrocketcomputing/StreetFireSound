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
 * $Id: Service.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.service;

import java.io.PrintStream;

import com.redrocketcomputing.util.configuration.*;

/**
 * Interface defining the minimum interface for all service managed by the services package
 *
 * @author stephen Jul 15, 2003
 * @version 1.0
 *
 */
public interface Service
{
  public final static int IDLE = 0;
  public final static int RUNNING = 1;

  public int getServiceId();
  public String getInstanceName();
  public int getServiceState();
  public void start();
  public void terminate();
  public void info(PrintStream printStream, String[] arguments);
}
