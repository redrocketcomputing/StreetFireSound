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
 * $Id: HaviMessageHandlerAdaptor.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.message;

/**
 * @author Stephen
 *
 */
public class HaviMessageHandlerAdaptor implements HaviMessageHandler
{

  /**
   * Constructor for HaviMessageHandlerAdaptor.
   */
  public HaviMessageHandlerAdaptor()
  {
    super();
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleReliableMessage(HaviReliableMessage)
   */
  public void handleReliableMessage(HaviReliableMessage message)
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleSimpleMessage(HaviSimpleMessage)
   */
  public void handleSimpleMessage(HaviSimpleMessage message)
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleReliableAckMessage(HaviReliableAckMessage)
   */
  public void handleReliableAckMessage(HaviReliableAckMessage message)
  {
  }

  /**
   * @see com.redrocketcomputing.havi.system.ms.HaviMessageHandler#handleReliableNoackMessage(HaviReliableNoackMessage)
   */
  public void handleReliableNoackMessage(HaviReliableNoackMessage message)
  {
  }

}
