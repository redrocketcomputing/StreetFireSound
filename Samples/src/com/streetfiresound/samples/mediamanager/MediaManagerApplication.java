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
 * $Id: MediaManagerApplication.java,v 1.1 2005/02/22 03:50:49 stephen Exp $
 */
package com.streetfiresound.samples.mediamanager;

import java.io.File;

import org.havi.system.types.HaviApplicationModuleManagerException;

import com.redrocketcomputing.appframework.service.ServiceException;
import com.redrocketcomputing.appframework.service.ServiceManager;
import com.redrocketcomputing.havi.system.HaviApplication;
import com.redrocketcomputing.havi.system.amm.ApplicationModuleManager;

/**
 * @author stephen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MediaManagerApplication extends HaviApplication
{

  /**
   * @param args
   */
  public MediaManagerApplication(String[] args)
  {
    super(args);
    // TODO Auto-generated constructor stub
  }
  
  public void run()
  {
    try
    {
      // Install MediaManagerAm
      ApplicationModuleManager amm = (ApplicationModuleManager)ServiceManager.getInstance().get(ApplicationModuleManager.class);
      amm.install("file:///home/stephen/workspace/nfs-target/opt/streetfire/mediamanageram.jar");
      
      // Wait forever
      synchronized(this)
      {
        wait();
      }
    }
    catch (ServiceException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (HaviApplicationModuleManagerException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    // Create application
    MediaManagerApplication application = new MediaManagerApplication(args);
    
    // Run it
    application.run();
  }
}
