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
 * $Id: IDLParserConstants.java,v 1.1 2005/02/22 03:46:07 stephen Exp $
 */

package com.streetfiresound.codegenerator.parser;

public interface IDLParserConstants {

  int EOF = 0;
  int ID = 66;
  int OCTALINT = 67;
  int DECIMALINT = 68;
  int HEXADECIMALINT = 69;
  int FLOATONE = 70;
  int FLOATTWO = 71;
  int CHARACTER = 72;
  int STRING = 73;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "<token of kind 4>",
    "<token of kind 5>",
    "<token of kind 6>",
    "\";\"",
    "\"module\"",
    "\"{\"",
    "\"}\"",
    "\"interface\"",
    "\":\"",
    "\",\"",
    "\"::\"",
    "\"const\"",
    "\"=\"",
    "\"|\"",
    "\"^\"",
    "\"&\"",
    "\">>\"",
    "\"<<\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"%\"",
    "\"~\"",
    "\"(\"",
    "\")\"",
    "\"TRUE\"",
    "\"FALSE\"",
    "\"typedef\"",
    "\"float\"",
    "\"double\"",
    "\"long\"",
    "\"short\"",
    "\"unsigned\"",
    "\"char\"",
    "\"wchar\"",
    "\"boolean\"",
    "\"octet\"",
    "\"any\"",
    "\"struct\"",
    "\"union\"",
    "\"switch\"",
    "\"case\"",
    "\"default\"",
    "\"enum\"",
    "\"sequence\"",
    "\"<\"",
    "\">\"",
    "\"string\"",
    "\"wstring\"",
    "\"[\"",
    "\"]\"",
    "\"readonly\"",
    "\"attribute\"",
    "\"exception\"",
    "\"oneway\"",
    "\"void\"",
    "\"in\"",
    "\"out\"",
    "\"inout\"",
    "\"raises\"",
    "\"context\"",
    "<ID>",
    "<OCTALINT>",
    "<DECIMALINT>",
    "<HEXADECIMALINT>",
    "<FLOATONE>",
    "<FLOATTWO>",
    "<CHARACTER>",
    "<STRING>",
  };

}
