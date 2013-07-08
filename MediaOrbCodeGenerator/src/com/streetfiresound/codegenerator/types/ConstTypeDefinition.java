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
 * $Id: ConstTypeDefinition.java,v 1.1 2005/02/24 03:03:38 stephen Exp $
 */

package com.streetfiresound.codegenerator.types;

/**
 * @author george
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface ConstTypeDefinition
{

  public final short INTERFACE        = 0x01;
  public final short BOOLEAN          = 0x02;
  public final short SHORT            = 0x03;
  public final short LONG             = 0x04;
  public final short LONGLONG         = 0x05;
  public final short ULONG            = 0x06;
  public final short ULONGLONG        = 0x07;
  public final short USHORT           = 0x08;
  public final short STRUCT           = 0x09;
  public final short MODULE           = 0x0a;
  public final short CONTEXT          = 0x0b;
  public final short DECLARATION      = 0x0c;
  public final short FUNCTION         = 0x0d;
  public final short STRING           = 0x0e;
  public final short WSTRING          = 0x0f;
  public final short OCTET            = 0x10;
  public final short CHAR             = 0x11;
  public final short WCHAR            = 0x12;
  public final short VOID             = 0x13;
  public final short ENUM             = 0x14;
  public final short UNION            = 0x15;

  public final short SEQUENCE         = 0x16;
  public final short ARRAY            = 0x17;
  public final short CTYPEDEF         = 0x18;
  public final short CONST            = 0x19;

  public final short IN               = 0x1a;
  public final short OUT              = 0x1b;
  public final short INOUT            = 0x1c;
  public final short LITERAL          = 0x1d;
  public final short PARAMETERLIST    = 0x1e;
  public final short HOLDER           = 0x1f;
  public final short FLOAT            = 0x20;
  public final short DOUBLE           = 0x21;
  public final short ANY              = 0x22;
  public final short FORWARDDEC       = 0x23;
  public final short LIST             = 0x24;
  public final short INTERFACEHEADER  = 0x25;
  public final short OR               = 0x27;
  public final short MULT             = 0x28;
  public final short XOR              = 0x29;
  public final short ADD              = 0x2a;
  public final short SHIFT            = 0x2b;
  public final short AND              = 0x2c;
  public final short UNARY            = 0x2d;
  public final short EXCEPTION        = 0x2e;
  public final short RAISELIST        = 0x2f;
  public final short CONTEXTLIST      = 0x30;
  public final short TYPEDEF          = 0x31;
  public final short SWITCH           = 0x32;
  public final short UNIONSTRUCT      = 0x33;

}
