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
 * $Id: IrSignalProcessor.java,v 1.1 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.devicecontroller;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.redrocketcomputing.havi.commandcontroller.AbstractState;
import com.redrocketcomputing.havi.commandcontroller.AbstractStateMachine;
import com.redrocketcomputing.havi.commandcontroller.CommandManager;
import com.redrocketcomputing.util.ListMap;
import com.redrocketcomputing.util.ListSet;

/**
 * @author david
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
class IrSignalProcessor extends AbstractStateMachine
{
  // class representing the init state
  private class InitState extends AbstractState
  {
      /**
     * @see com.redrocketcomputing.havi.iav.rbx1600.DeviceController.fcm.AbstractState#execute(Object)
     */
    public void execute(Object data)
    {
      boolean startTimer = false;

      // Cancel timer
      if(bufferSweeperTimer != null)
      {
        bufferSweeperTimer.cancel();
        bufferSweeperTimer = null;
      }

      synchronized(bufferLock)
      {
        //initialize/reset buffers
        if(data !=null && isMetaSignal(((Byte)data).byteValue()))
        {
          // assign buffer
          metaSignal = (String)signalToStringTable.get((Byte)data);
          startTimer = true;
        }
        else
        {
          metaSignal = "";
        }

        numericSignalBuffer = new StringBuffer(5);
        executionSignal = "";
      }

      if(startTimer)
      {
        // start timer
        bufferSweeperTimer = new Timer();
        bufferSweeperTimer.schedule(new BufferSweeper(), bufferTimeout);
      }
    }
  }

  // class representing the numeric state
  private class NumericInputState extends AbstractState
  {
    /**
     * @see com.redrocketcomputing.havi.iav.rbx1600.DeviceController.fcm.AbstractState#execute(Object)
     */
    public void execute(Object data)
    {
      // Cancel timer
      if(bufferSweeperTimer != null)
      {
        bufferSweeperTimer.cancel();
        bufferSweeperTimer = null;
      }

      synchronized(bufferLock)
      {
        numericSignalBuffer.append((String)signalToStringTable.get((Byte)data));
      }

      // start timer
      bufferSweeperTimer = new Timer();
      bufferSweeperTimer.schedule(new BufferSweeper(), bufferTimeout);
    }
  }

  // class representing the execution state
  private class ExecutionState extends AbstractState
  {
    /**
     * @see com.redrocketcomputing.havi.iav.rbx1600.DeviceController.fcm.AbstractState#execute(Object)
     */
    public void execute(Object data)
    {
      // Cancel the sweeper timer
      if(bufferSweeperTimer != null)
      {
        bufferSweeperTimer.cancel();
        bufferSweeperTimer = null;
      }

      // Process signal
      synchronized(bufferLock)
      {
        executionSignal = (String)signalToStringTable.get((Byte)data);
      }
      
      // Issue command
      issueCommand();

      // Back to init state
      internalEvent(INIT_STATE, data);
    }
  }

  private class BufferSweeper extends TimerTask
  {
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      synchronized(bufferLock)
      {
        metaSignal = "";
        numericSignalBuffer = new StringBuffer(5);
        executionSignal = "";
      }
    }
  }

  // Possible states
  private static final int INIT_STATE = 0;
  private static final int NUMERIC_INPUT_STATE = 1;
  private static final int EXECUTION_STATE = 2;
  private static final int MAX_STATES = 3;

  // Possible inputs
  private static final int META_SIGNAL = 0;
  private static final int NUMERIC_SIGNAL = 1;
  private static final int EXECUTION_SIGNAL = 2;

  // state transition map
  // transitionMap[input][currentState]->nextState
  private int[][] transitionMap =
  {
    //INIT_STATE  NUMERIC_INPUT_STATE  EXECUTION_STATE
    { INIT_STATE, INIT_STATE, CANNOT_HAPPEN }, //META_SIGNAL
    { NUMERIC_INPUT_STATE, NUMERIC_INPUT_STATE, CANNOT_HAPPEN}, //NUMERIC_SIGNAL
    { EXECUTION_STATE, EXECUTION_STATE, CANNOT_HAPPEN} //EXECUTION_SIGNAL
  };

  private static final Set executionSignalSet;
  private static final Set numericSignalSet;
  private static final Set metaSignalSet;
  private static final Set supportedSignalSet;
  private static final Map signalToStringTable;

  private Object bufferLock;
  private String metaSignal;
  private StringBuffer numericSignalBuffer;
  private String executionSignal;

  private CommandManager commandManager;
  private Timer bufferSweeperTimer;
  private int bufferTimeout = 7000;

  static
  {
//    signalSet = new ListSet(30);
//    signalSet.add(new Byte(IrSignalConstant.TRACK_1));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_0));
//    signalSet.add(new Byte(IrSignalConstant.PREVIOUS_TRACK));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_9));
//    signalSet.add(new Byte(IrSignalConstant.STOP));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_5));
//    signalSet.add(new Byte(IrSignalConstant.SCAN_FORWARD));
//    signalSet.add(new Byte(IrSignalConstant.BLOCK));
//    signalSet.add(new Byte(IrSignalConstant.REPEAT));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_3));
//    signalSet.add(new Byte(IrSignalConstant.PLAY));
//    signalSet.add(new Byte(IrSignalConstant.DISC));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_7));
//    signalSet.add(new Byte(IrSignalConstant.NEXT_DISC));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_2));
//    signalSet.add(new Byte(IrSignalConstant.NEXT_TRACK));
//    signalSet.add(new Byte(IrSignalConstant.PAUSE));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_6));
//    signalSet.add(new Byte(IrSignalConstant.POWER));
//    signalSet.add(new Byte(IrSignalConstant.SHUFFLE));
//    signalSet.add(new Byte(IrSignalConstant.CHECK));
//    signalSet.add(new Byte(IrSignalConstant.CONTINUE));
//    signalSet.add(new Byte(IrSignalConstant.PREVIOUS_DISC));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_4));
//    signalSet.add(new Byte(IrSignalConstant.SCAN_BACK));
//    signalSet.add(new Byte(IrSignalConstant.ENTER));
//    signalSet.add(new Byte(IrSignalConstant.TRACK));
//    signalSet.add(new Byte(IrSignalConstant.TRACK_8));
//    signalSet.add(new Byte(IrSignalConstant.CLEAR));
//    signalSet.add(new Byte(IrSignalConstant.PROGRAM));

    // execution signal set
    executionSignalSet = new ListSet(11);
    executionSignalSet.add(new Byte(IrSignalConstant.PREVIOUS_TRACK));
    executionSignalSet.add(new Byte(IrSignalConstant.STOP));
    executionSignalSet.add(new Byte(IrSignalConstant.REPEAT));
    executionSignalSet.add(new Byte(IrSignalConstant.PLAY));
    executionSignalSet.add(new Byte(IrSignalConstant.NEXT_TRACK));
    executionSignalSet.add(new Byte(IrSignalConstant.PAUSE));
    executionSignalSet.add(new Byte(IrSignalConstant.POWER));
    executionSignalSet.add(new Byte(IrSignalConstant.SHUFFLE));
    executionSignalSet.add(new Byte(IrSignalConstant.CONTINUE));
    executionSignalSet.add(new Byte(IrSignalConstant.CLEAR));
    executionSignalSet.add(new Byte(IrSignalConstant.ENTER));

    // numeric signal set
    numericSignalSet = new ListSet(10);
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_0));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_1));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_2));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_3));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_4));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_5));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_6));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_7));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_8));
    numericSignalSet.add(new Byte(IrSignalConstant.TRACK_9));

    // meta signal set
    metaSignalSet = new ListSet();
    metaSignalSet.add(new Byte(IrSignalConstant.PROGRAM));

    // all supported signals
    supportedSignalSet = new ListSet(22);
    supportedSignalSet.addAll(executionSignalSet);
    supportedSignalSet.addAll(numericSignalSet);
    supportedSignalSet.addAll(metaSignalSet);

    // signal to string mapping
    signalToStringTable = new ListMap(30);
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_1), "1");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_0), "0");
    signalToStringTable.put(new Byte(IrSignalConstant.PREVIOUS_TRACK), "PREVIOUS_TRACK");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_9), "9");
    signalToStringTable.put(new Byte(IrSignalConstant.STOP), "STOP");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_5), "5");
    signalToStringTable.put(new Byte(IrSignalConstant.SCAN_FORWARD), "SCAN_FORWARD");
    signalToStringTable.put(new Byte(IrSignalConstant.BLOCK), "BLOCK");
    signalToStringTable.put(new Byte(IrSignalConstant.REPEAT), "REPEAT");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_3), "3");
    signalToStringTable.put(new Byte(IrSignalConstant.PLAY), "PLAY");
    signalToStringTable.put(new Byte(IrSignalConstant.DISC), "DISC");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_7), "7");
    signalToStringTable.put(new Byte(IrSignalConstant.NEXT_DISC), "NEXT_DISC");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_2), "2");
    signalToStringTable.put(new Byte(IrSignalConstant.NEXT_TRACK), "NEXT_TRACK");
    signalToStringTable.put(new Byte(IrSignalConstant.PAUSE), "PAUSE");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_6), "6");
    signalToStringTable.put(new Byte(IrSignalConstant.POWER), "POWER");
    signalToStringTable.put(new Byte(IrSignalConstant.SHUFFLE), "SHUFFLE");
    signalToStringTable.put(new Byte(IrSignalConstant.CHECK), "CHECK");
    signalToStringTable.put(new Byte(IrSignalConstant.CONTINUE), "CONTINUE");
    signalToStringTable.put(new Byte(IrSignalConstant.PREVIOUS_DISC), "PREVIOUS_DISC");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_4), "4");
    signalToStringTable.put(new Byte(IrSignalConstant.SCAN_BACK), "SCAN_BACK");
    signalToStringTable.put(new Byte(IrSignalConstant.ENTER), "ENTER");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK), "TRACK");
    signalToStringTable.put(new Byte(IrSignalConstant.TRACK_8), "8");
    signalToStringTable.put(new Byte(IrSignalConstant.CLEAR), "CLEAR");
    signalToStringTable.put(new Byte(IrSignalConstant.PROGRAM), "PROGRAM");
  }

  public static boolean isExecutionSignal(byte signal)
  {
    return executionSignalSet.contains(new Byte(signal));
  }

  public static boolean isSupportedSignal(byte signal)
  {
    return supportedSignalSet.contains(new Byte(signal));
  }

  public static boolean isNumericSignal(byte signal)
  {
    return numericSignalSet.contains(new Byte(signal));
  }

  public static boolean isMetaSignal(byte signal)
  {
    return metaSignalSet.contains(new Byte(signal));
  }

  public IrSignalProcessor(CommandManager commandManager, int bufferTimeout)
  {
    // Construct super class
    super(MAX_STATES);
    super.stateMap[INIT_STATE] = new InitState();
    super.stateMap[NUMERIC_INPUT_STATE] = new NumericInputState();
    super.stateMap[EXECUTION_STATE] = new ExecutionState();
    super.currentState = INIT_STATE;
    
    // Initialize processor
    this.bufferLock = new Object();
    this.numericSignalBuffer = new StringBuffer(5);
    this.commandManager = commandManager;
    this.bufferTimeout = bufferTimeout;
  }

  public void close()
  {
    // Check to see if we have a sweeper
    if(bufferSweeperTimer != null)
    {
      bufferSweeperTimer.cancel();
    }

    // Release internal resources
    bufferSweeperTimer = null;
    commandManager = null;
  }

  public void processMetaSignal(byte signal)
  {
    externalEvent(transitionMap[META_SIGNAL][currentState], new Byte(signal));
  }

  public void processNumericSignal(byte signal)
  {
    externalEvent(transitionMap[NUMERIC_SIGNAL][currentState], new Byte(signal));
  }

  public void processExecutionSignal(byte signal)
  {
    externalEvent(transitionMap[EXECUTION_SIGNAL][currentState], new Byte(signal));
  }

  private void issueCommand()
  {
    // Create and execute command using command factory
    commandManager.queueCommand(commandManager.createCommands(executionSignal, new IrCommandParameter(metaSignal, numericSignalBuffer.toString())));
  }
}
