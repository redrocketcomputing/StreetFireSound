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
 * $Id: AbstractStateMachine.java,v 1.1 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.havi.commandcontroller;

/**
 * @author david
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractStateMachine
{
  protected static final int EVENT_IGNORED = 0xFFFE;
  protected static final int CANNOT_HAPPEN = 0xFFFF;

  protected int currentState;
  protected boolean eventGenerated = false;
  protected Object eventData;
  protected AbstractState[] stateMap;

  private int maxStates;

  public AbstractStateMachine(int maxStates)
  {
    this.maxStates = maxStates;
    this.stateMap = new AbstractState[this.maxStates];
  }

  protected void externalEvent(int newState, Object data)
  {
    if(newState != EVENT_IGNORED)
    {
      internalEvent(newState, data);
      executeStateEngine();
    }
  }

  protected void internalEvent(int newState, Object data)
  {
    this.eventData = data;
    this.eventGenerated = true;
    this.currentState = newState;
  }

  private void executeStateEngine()
  {
    synchronized(stateMap)
    {
      Object tempData = null;

      while(this.eventGenerated)
      {
        tempData = this.eventData;
        eventData = null;
        this.eventGenerated = false;

        AbstractState[] tempStateMap = stateMap;

        if(tempStateMap == null || currentState > tempStateMap.length)
        {
          return;
        }

        tempStateMap[currentState].execute(tempData);
      }
    }
  }
}
