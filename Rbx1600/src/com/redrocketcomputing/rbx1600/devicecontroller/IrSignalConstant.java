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
 * $Id: IrSignalConstant.java,v 1.1 2005/03/16 04:24:31 stephen Exp $
 */

package com.redrocketcomputing.rbx1600.devicecontroller;


/**
 * @author david
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface IrSignalConstant
{
  public static final byte PLAYER_1 = (byte)0x88; //10001000
  public static final byte PLAYER_2 = (byte)0x9C; //10011100
  public static final byte PLAYER_3 = (byte)0x8A; //10001010

  public static final byte TRACK_1 = (byte)0x00;
  public static final byte TRACK_0 = (byte)0x02;
  public static final byte PREVIOUS_TRACK = (byte)0x06;
  public static final byte TRACK_9 = (byte)0x08;
  public static final byte STOP = (byte)0x0E;
  public static final byte TRACK_5 = (byte)0x10;
  public static final byte SCAN_FORWARD = (byte)0x16;
  public static final byte BLOCK = (byte)0x19;
  public static final byte REPEAT = (byte)0x1A;
  public static final byte TRACK_3 = (byte)0x20;
  public static final byte PLAY = (byte)0x26;
  public static final byte DISC = (byte)0x29;
  public static final byte TRACK_7 = (byte)0x30;
  public static final byte NEXT_DISC = (byte)0x3E;
  public static final byte TRACK_2 = (byte)0x40;
  public static final byte NEXT_TRACK = (byte)0x46;
  public static final byte PAUSE = (byte)0x4E;
  public static final byte TRACK_6 = (byte)0x50;
  public static final byte POWER = (byte)0x54;
  public static final byte SHUFFLE =(byte)0x56;
  public static final byte CHECK = (byte)0x59;
  public static final byte CONTINUE = (byte)0x5C;
  public static final byte PREVIOUS_DISC = (byte)0x5E;
  public static final byte TRACK_4 = (byte)0x60;
  public static final byte SCAN_BACK = (byte)0x66;
  public static final byte ENTER = (byte)0x68;
  public static final byte TRACK = (byte)0x69;
  public static final byte TRACK_8 = (byte)0x70;
  public static final byte CLEAR = (byte)0x78;
  public static final byte PROGRAM = (byte)0x7C;
}
