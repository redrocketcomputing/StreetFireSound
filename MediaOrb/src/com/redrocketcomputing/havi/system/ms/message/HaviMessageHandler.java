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
 * $Id: HaviMessageHandler.java,v 1.1 2005/02/22 03:44:27 stephen Exp $
 */

package com.redrocketcomputing.havi.system.ms.message;

/**
 * Interface class for handling HAVI messages.  This follows a modified visitor pattern.
 *
 * @author Stephen
 *
 */
public interface HaviMessageHandler
{
  /**
   * Handle reliable message
   * @param message The reliable message
   */
  public void handleReliableMessage(HaviReliableMessage message);

  /**
   * Handle simple message.
   * @param message The simple message
   */
  public void handleSimpleMessage(HaviSimpleMessage message);

  /**
   * Handle reliable ack message.
   * @param message The reliable ack message
   */
  public void handleReliableAckMessage(HaviReliableAckMessage message);

  /**
   * Handle reliable no ack message.
   * @param message The reliable no ack message
   */
  public void handleReliableNoackMessage(HaviReliableNoackMessage message);
}
