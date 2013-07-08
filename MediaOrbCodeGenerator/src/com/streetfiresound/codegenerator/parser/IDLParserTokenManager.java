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
 * $Id: IDLParserTokenManager.java,v 1.1 2005/02/22 03:46:07 stephen Exp $
 */

package com.streetfiresound.codegenerator.parser;

public class IDLParserTokenManager implements IDLParserConstants
{
  public static  java.io.PrintStream debugStream = System.out;
  public static  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private static final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x2000000L) != 0L)
            return 58;
         if ((active0 & 0xff33ffffc0008900L) != 0L || (active1 & 0x3L) != 0L)
         {
            jjmatchedKind = 66;
            return 13;
         }
         return -1;
      case 1:
         if ((active0 & 0x5f33ffffc0008100L) != 0L || (active1 & 0x3L) != 0L)
         {
            if (jjmatchedPos != 1)
            {
               jjmatchedKind = 66;
               jjmatchedPos = 1;
            }
            return 13;
         }
         if ((active0 & 0xa000000000000800L) != 0L)
            return 13;
         return -1;
      case 2:
         if ((active0 & 0x9f33fbffc0008900L) != 0L || (active1 & 0x3L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 2;
            return 13;
         }
         if ((active0 & 0x4000040000000000L) != 0L)
            return 13;
         return -1;
      case 3:
         if ((active0 & 0x1001404840000000L) != 0L)
            return 13;
         if ((active0 & 0x8f32bbb780008900L) != 0L || (active1 & 0x3L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 3;
            return 13;
         }
         return -1;
      case 4:
         if ((active0 & 0xf32a92500000900L) != 0L || (active1 & 0x3L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 4;
            return 13;
         }
         if ((active0 & 0x8000129280008000L) != 0L)
            return 13;
         return -1;
      case 5:
         if ((active0 & 0x722812100000800L) != 0L || (active1 & 0x2L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 5;
            return 13;
         }
         if ((active0 & 0x810280400000100L) != 0L || (active1 & 0x1L) != 0L)
            return 13;
         return -1;
      case 6:
         if ((active0 & 0x702002000000800L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 6;
            return 13;
         }
         if ((active0 & 0x20810100000000L) != 0L || (active1 & 0x2L) != 0L)
            return 13;
         return -1;
      case 7:
         if ((active0 & 0x102002000000000L) != 0L)
            return 13;
         if ((active0 & 0x600000000000800L) != 0L)
         {
            jjmatchedKind = 66;
            jjmatchedPos = 7;
            return 13;
         }
         return -1;
      default :
         return -1;
   }
}
private static final int jjStartNfa_0(int pos, long active0, long active1)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
}
static private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
static private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 37:
         return jjStopAtPos(0, 26);
      case 38:
         return jjStopAtPos(0, 19);
      case 40:
         return jjStopAtPos(0, 28);
      case 41:
         return jjStopAtPos(0, 29);
      case 42:
         return jjStopAtPos(0, 24);
      case 43:
         return jjStopAtPos(0, 22);
      case 44:
         return jjStopAtPos(0, 13);
      case 45:
         return jjStopAtPos(0, 23);
      case 47:
         return jjStartNfaWithStates_0(0, 25, 58);
      case 58:
         jjmatchedKind = 12;
         return jjMoveStringLiteralDfa1_0(0x4000L, 0x0L);
      case 59:
         return jjStopAtPos(0, 7);
      case 60:
         jjmatchedKind = 50;
         return jjMoveStringLiteralDfa1_0(0x200000L, 0x0L);
      case 61:
         return jjStopAtPos(0, 16);
      case 62:
         jjmatchedKind = 51;
         return jjMoveStringLiteralDfa1_0(0x100000L, 0x0L);
      case 70:
         return jjMoveStringLiteralDfa1_0(0x80000000L, 0x0L);
      case 84:
         return jjMoveStringLiteralDfa1_0(0x40000000L, 0x0L);
      case 91:
         return jjStopAtPos(0, 54);
      case 93:
         return jjStopAtPos(0, 55);
      case 94:
         return jjStopAtPos(0, 18);
      case 97:
         return jjMoveStringLiteralDfa1_0(0x200040000000000L, 0x0L);
      case 98:
         return jjMoveStringLiteralDfa1_0(0x10000000000L, 0x0L);
      case 99:
         return jjMoveStringLiteralDfa1_0(0x404000008000L, 0x2L);
      case 100:
         return jjMoveStringLiteralDfa1_0(0x800400000000L, 0x0L);
      case 101:
         return jjMoveStringLiteralDfa1_0(0x401000000000000L, 0x0L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x200000000L, 0x0L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0xa000000000000800L, 0x0L);
      case 108:
         return jjMoveStringLiteralDfa1_0(0x800000000L, 0x0L);
      case 109:
         return jjMoveStringLiteralDfa1_0(0x100L, 0x0L);
      case 111:
         return jjMoveStringLiteralDfa1_0(0x4800020000000000L, 0x0L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x100000000000000L, 0x1L);
      case 115:
         return jjMoveStringLiteralDfa1_0(0x12281000000000L, 0x0L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x100000000L, 0x0L);
      case 117:
         return jjMoveStringLiteralDfa1_0(0x102000000000L, 0x0L);
      case 118:
         return jjMoveStringLiteralDfa1_0(0x1000000000000000L, 0x0L);
      case 119:
         return jjMoveStringLiteralDfa1_0(0x20008000000000L, 0x0L);
      case 123:
         return jjStopAtPos(0, 9);
      case 124:
         return jjStopAtPos(0, 17);
      case 125:
         return jjStopAtPos(0, 10);
      case 126:
         return jjStopAtPos(0, 27);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
static private final int jjMoveStringLiteralDfa1_0(long active0, long active1)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0, active1);
      return 1;
   }
   switch(curChar)
   {
      case 58:
         if ((active0 & 0x4000L) != 0L)
            return jjStopAtPos(1, 14);
         break;
      case 60:
         if ((active0 & 0x200000L) != 0L)
            return jjStopAtPos(1, 21);
         break;
      case 62:
         if ((active0 & 0x100000L) != 0L)
            return jjStopAtPos(1, 20);
         break;
      case 65:
         return jjMoveStringLiteralDfa2_0(active0, 0x80000000L, active1, 0L);
      case 82:
         return jjMoveStringLiteralDfa2_0(active0, 0x40000000L, active1, 0L);
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x400000000000L, active1, 0x1L);
      case 99:
         return jjMoveStringLiteralDfa2_0(active0, 0x28000000000L, active1, 0L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x102800000000000L, active1, 0L);
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x5000000000L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x200000000L, active1, 0L);
      case 110:
         if ((active0 & 0x2000000000000000L) != 0L)
         {
            jjmatchedKind = 61;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0x8801142000000800L, active1, 0L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x1000010c00008100L, active1, 0x2L);
      case 115:
         return jjMoveStringLiteralDfa2_0(active0, 0x20000000000000L, active1, 0L);
      case 116:
         return jjMoveStringLiteralDfa2_0(active0, 0x210080000000000L, active1, 0L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000000000000000L, active1, 0L);
      case 119:
         return jjMoveStringLiteralDfa2_0(active0, 0x200000000000L, active1, 0L);
      case 120:
         return jjMoveStringLiteralDfa2_0(active0, 0x400000000000000L, active1, 0L);
      case 121:
         return jjMoveStringLiteralDfa2_0(active0, 0x100000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0, active1);
}
static private final int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(0, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0, active1);
      return 2;
   }
   switch(curChar)
   {
      case 76:
         return jjMoveStringLiteralDfa3_0(active0, 0x80000000L, active1, 0L);
      case 85:
         return jjMoveStringLiteralDfa3_0(active0, 0x40000000L, active1, 0L);
      case 97:
         return jjMoveStringLiteralDfa3_0(active0, 0x100004000000000L, active1, 0L);
      case 99:
         return jjMoveStringLiteralDfa3_0(active0, 0x400000000000000L, active1, 0L);
      case 100:
         return jjMoveStringLiteralDfa3_0(active0, 0x100L, active1, 0L);
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x800000000000000L, active1, 0L);
      case 102:
         return jjMoveStringLiteralDfa3_0(active0, 0x800000000000L, active1, 0L);
      case 104:
         return jjMoveStringLiteralDfa3_0(active0, 0x8000000000L, active1, 0L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000300000000000L, active1, 0x1L);
      case 110:
         return jjMoveStringLiteralDfa3_0(active0, 0x800008000L, active1, 0x2L);
      case 111:
         return jjMoveStringLiteralDfa3_0(active0, 0x8000011200000000L, active1, 0L);
      case 112:
         return jjMoveStringLiteralDfa3_0(active0, 0x100000000L, active1, 0L);
      case 113:
         return jjMoveStringLiteralDfa3_0(active0, 0x2000000000000L, active1, 0L);
      case 114:
         return jjMoveStringLiteralDfa3_0(active0, 0x10080000000000L, active1, 0L);
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0x402000000000L, active1, 0L);
      case 116:
         if ((active0 & 0x4000000000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 62, 13);
         return jjMoveStringLiteralDfa3_0(active0, 0x220020000000800L, active1, 0L);
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000400000000L, active1, 0L);
      case 121:
         if ((active0 & 0x40000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 42, 13);
         break;
      default :
         break;
   }
   return jjStartNfa_0(1, active0, active1);
}
static private final int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(1, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0, active1);
      return 3;
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x40000000L) != 0L)
            return jjStartNfaWithStates_0(3, 30, 13);
         break;
      case 83:
         return jjMoveStringLiteralDfa4_0(active0, 0x80000000L, active1, 0L);
      case 97:
         return jjMoveStringLiteralDfa4_0(active0, 0x808200000000L, active1, 0L);
      case 98:
         return jjMoveStringLiteralDfa4_0(active0, 0x400000000L, active1, 0L);
      case 100:
         if ((active0 & 0x1000000000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 60, 13);
         return jjMoveStringLiteralDfa4_0(active0, 0x100000000000000L, active1, 0L);
      case 101:
         if ((active0 & 0x400000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 46, 13);
         return jjMoveStringLiteralDfa4_0(active0, 0x400020100000800L, active1, 0L);
      case 103:
         if ((active0 & 0x800000000L) != 0L)
            return jjStartNfaWithStates_0(3, 35, 13);
         break;
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x10002000000000L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa4_0(active0, 0x10000000000L, active1, 0L);
      case 109:
         if ((active0 & 0x1000000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 48, 13);
         break;
      case 111:
         return jjMoveStringLiteralDfa4_0(active0, 0x100000000000L, active1, 0L);
      case 114:
         if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 38, 13);
         return jjMoveStringLiteralDfa4_0(active0, 0x220001000000000L, active1, 0L);
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000L, active1, 0x1L);
      case 116:
         return jjMoveStringLiteralDfa4_0(active0, 0x200000000000L, active1, 0x2L);
      case 117:
         return jjMoveStringLiteralDfa4_0(active0, 0x8002080000000100L, active1, 0L);
      case 119:
         return jjMoveStringLiteralDfa4_0(active0, 0x800000000000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0, active1);
}
static private final int jjMoveStringLiteralDfa4_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(2, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0, active1);
      return 4;
   }
   switch(curChar)
   {
      case 69:
         if ((active0 & 0x80000000L) != 0L)
            return jjStartNfaWithStates_0(4, 31, 13);
         break;
      case 97:
         return jjMoveStringLiteralDfa5_0(active0, 0x800000000000000L, active1, 0L);
      case 99:
         return jjMoveStringLiteralDfa5_0(active0, 0x280000000000L, active1, 0L);
      case 100:
         return jjMoveStringLiteralDfa5_0(active0, 0x100000000L, active1, 0L);
      case 101:
         return jjMoveStringLiteralDfa5_0(active0, 0x2010000000000L, active1, 0x3L);
      case 103:
         return jjMoveStringLiteralDfa5_0(active0, 0x2000000000L, active1, 0L);
      case 105:
         return jjMoveStringLiteralDfa5_0(active0, 0x220000000000000L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa5_0(active0, 0x400000100L, active1, 0L);
      case 110:
         if ((active0 & 0x100000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 44, 13);
         return jjMoveStringLiteralDfa5_0(active0, 0x10000000000000L, active1, 0L);
      case 111:
         return jjMoveStringLiteralDfa5_0(active0, 0x100000000000000L, active1, 0L);
      case 112:
         return jjMoveStringLiteralDfa5_0(active0, 0x400000000000000L, active1, 0L);
      case 114:
         if ((active0 & 0x8000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 39, 13);
         return jjMoveStringLiteralDfa5_0(active0, 0x800L, active1, 0L);
      case 116:
         if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(4, 15, 13);
         else if ((active0 & 0x200000000L) != 0L)
            return jjStartNfaWithStates_0(4, 33, 13);
         else if ((active0 & 0x1000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 36, 13);
         else if ((active0 & 0x20000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 41, 13);
         else if ((active0 & 0x8000000000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 63, 13);
         break;
      case 117:
         return jjMoveStringLiteralDfa5_0(active0, 0x800000000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(3, active0, active1);
}
static private final int jjMoveStringLiteralDfa5_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(3, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0, active1);
      return 5;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa6_0(active0, 0x10000000000L, active1, 0L);
      case 98:
         return jjMoveStringLiteralDfa6_0(active0, 0x200000000000000L, active1, 0L);
      case 101:
         if ((active0 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(5, 8, 13);
         else if ((active0 & 0x400000000L) != 0L)
            return jjStartNfaWithStates_0(5, 34, 13);
         return jjMoveStringLiteralDfa6_0(active0, 0x100000000L, active1, 0L);
      case 102:
         return jjMoveStringLiteralDfa6_0(active0, 0x800L, active1, 0L);
      case 103:
         if ((active0 & 0x10000000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 52, 13);
         break;
      case 104:
         if ((active0 & 0x200000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 45, 13);
         break;
      case 108:
         return jjMoveStringLiteralDfa6_0(active0, 0x800000000000L, active1, 0L);
      case 110:
         return jjMoveStringLiteralDfa6_0(active0, 0x122002000000000L, active1, 0L);
      case 115:
         if ((active1 & 0x1L) != 0L)
            return jjStartNfaWithStates_0(5, 64, 13);
         break;
      case 116:
         if ((active0 & 0x80000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 43, 13);
         return jjMoveStringLiteralDfa6_0(active0, 0x400000000000000L, active1, 0L);
      case 120:
         return jjMoveStringLiteralDfa6_0(active0, 0L, active1, 0x2L);
      case 121:
         if ((active0 & 0x800000000000000L) != 0L)
            return jjStartNfaWithStates_0(5, 59, 13);
         break;
      default :
         break;
   }
   return jjStartNfa_0(4, active0, active1);
}
static private final int jjMoveStringLiteralDfa6_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(4, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0, active1);
      return 6;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa7_0(active0, 0x800L, active1, 0L);
      case 99:
         return jjMoveStringLiteralDfa7_0(active0, 0x2000000000000L, active1, 0L);
      case 101:
         return jjMoveStringLiteralDfa7_0(active0, 0x2000000000L, active1, 0L);
      case 102:
         if ((active0 & 0x100000000L) != 0L)
            return jjStartNfaWithStates_0(6, 32, 13);
         break;
      case 103:
         if ((active0 & 0x20000000000000L) != 0L)
            return jjStartNfaWithStates_0(6, 53, 13);
         break;
      case 105:
         return jjMoveStringLiteralDfa7_0(active0, 0x400000000000000L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa7_0(active0, 0x100000000000000L, active1, 0L);
      case 110:
         if ((active0 & 0x10000000000L) != 0L)
            return jjStartNfaWithStates_0(6, 40, 13);
         break;
      case 116:
         if ((active0 & 0x800000000000L) != 0L)
            return jjStartNfaWithStates_0(6, 47, 13);
         else if ((active1 & 0x2L) != 0L)
            return jjStartNfaWithStates_0(6, 65, 13);
         break;
      case 117:
         return jjMoveStringLiteralDfa7_0(active0, 0x200000000000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(5, active0, active1);
}
static private final int jjMoveStringLiteralDfa7_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(5, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0, 0L);
      return 7;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa8_0(active0, 0x800L);
      case 100:
         if ((active0 & 0x2000000000L) != 0L)
            return jjStartNfaWithStates_0(7, 37, 13);
         break;
      case 101:
         if ((active0 & 0x2000000000000L) != 0L)
            return jjStartNfaWithStates_0(7, 49, 13);
         break;
      case 111:
         return jjMoveStringLiteralDfa8_0(active0, 0x400000000000000L);
      case 116:
         return jjMoveStringLiteralDfa8_0(active0, 0x200000000000000L);
      case 121:
         if ((active0 & 0x100000000000000L) != 0L)
            return jjStartNfaWithStates_0(7, 56, 13);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0, 0L);
}
static private final int jjMoveStringLiteralDfa8_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(6, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(7, active0, 0L);
      return 8;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x800L) != 0L)
            return jjStartNfaWithStates_0(8, 11, 13);
         else if ((active0 & 0x200000000000000L) != 0L)
            return jjStartNfaWithStates_0(8, 57, 13);
         break;
      case 110:
         if ((active0 & 0x400000000000000L) != 0L)
            return jjStartNfaWithStates_0(8, 58, 13);
         break;
      default :
         break;
   }
   return jjStartNfa_0(7, active0, 0L);
}
static private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
static private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
static private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
static private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
static private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 77;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 58:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(62, 63);
                  else if (curChar == 47)
                     jjCheckNAddTwoStates(59, 60);
                  break;
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(0, 5);
                  else if (curChar == 47)
                     jjAddStates(6, 7);
                  else if (curChar == 34)
                     jjCheckNAddStates(8, 10);
                  else if (curChar == 39)
                     jjAddStates(11, 12);
                  else if (curChar == 46)
                     jjCheckNAdd(21);
                  else if (curChar == 35)
                     jjCheckNAddTwoStates(1, 2);
                  if ((0x3fe000000000000L & l) != 0L)
                  {
                     if (kind > 68)
                        kind = 68;
                     jjCheckNAddTwoStates(18, 19);
                  }
                  else if (curChar == 48)
                     jjAddStates(13, 14);
                  if (curChar == 48)
                  {
                     if (kind > 67)
                        kind = 67;
                     jjCheckNAddTwoStates(15, 16);
                  }
                  break;
               case 1:
                  if ((0x100000200L & l) != 0L)
                     jjCheckNAddTwoStates(1, 2);
                  break;
               case 2:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(15, 18);
                  break;
               case 3:
                  if ((0x100000200L & l) != 0L)
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 4:
                  if (curChar == 34)
                     jjCheckNAdd(5);
                  break;
               case 5:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(5, 6);
                  break;
               case 6:
                  if (curChar == 34)
                     jjCheckNAddStates(19, 21);
                  break;
               case 7:
                  if (curChar == 10 && kind > 6)
                     kind = 6;
                  break;
               case 8:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(22, 25);
                  break;
               case 9:
                  if ((0x100000200L & l) != 0L)
                     jjCheckNAddStates(26, 28);
                  break;
               case 10:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(10, 7);
                  break;
               case 11:
                  if ((0x100000200L & l) != 0L)
                     jjCheckNAddStates(29, 33);
                  break;
               case 13:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 66)
                     kind = 66;
                  jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 14:
                  if (curChar != 48)
                     break;
                  if (kind > 67)
                     kind = 67;
                  jjCheckNAddTwoStates(15, 16);
                  break;
               case 15:
                  if ((0xff000000000000L & l) == 0L)
                     break;
                  if (kind > 67)
                     kind = 67;
                  jjCheckNAddTwoStates(15, 16);
                  break;
               case 17:
                  if ((0x3fe000000000000L & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAddTwoStates(18, 19);
                  break;
               case 18:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 68)
                     kind = 68;
                  jjCheckNAddTwoStates(18, 19);
                  break;
               case 20:
                  if (curChar == 46)
                     jjCheckNAdd(21);
                  break;
               case 21:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 70)
                     kind = 70;
                  jjCheckNAddStates(34, 36);
                  break;
               case 23:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(24);
                  break;
               case 24:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 70)
                     kind = 70;
                  jjCheckNAddTwoStates(24, 25);
                  break;
               case 26:
                  if (curChar == 39)
                     jjAddStates(11, 12);
                  break;
               case 27:
                  if ((0xffffff7fffffdbffL & l) != 0L)
                     jjCheckNAdd(28);
                  break;
               case 28:
                  if (curChar == 39 && kind > 72)
                     kind = 72;
                  break;
               case 30:
                  if ((0x8000008400000000L & l) != 0L)
                     jjCheckNAdd(28);
                  break;
               case 31:
                  if (curChar == 48)
                     jjCheckNAddTwoStates(32, 28);
                  break;
               case 32:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(32, 28);
                  break;
               case 33:
                  if ((0x3fe000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(34, 28);
                  break;
               case 34:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(34, 28);
                  break;
               case 35:
                  if (curChar == 48)
                     jjAddStates(37, 38);
                  break;
               case 37:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(37, 28);
                  break;
               case 39:
                  if (curChar == 34)
                     jjCheckNAddStates(8, 10);
                  break;
               case 40:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddStates(8, 10);
                  break;
               case 42:
                  if ((0x8000008400000000L & l) != 0L)
                     jjCheckNAddStates(8, 10);
                  break;
               case 43:
                  if (curChar == 34 && kind > 73)
                     kind = 73;
                  break;
               case 44:
                  if (curChar == 48)
                     jjCheckNAddStates(39, 42);
                  break;
               case 45:
                  if ((0xff000000000000L & l) != 0L)
                     jjCheckNAddStates(39, 42);
                  break;
               case 46:
                  if ((0x3fe000000000000L & l) != 0L)
                     jjCheckNAddStates(43, 46);
                  break;
               case 47:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(43, 46);
                  break;
               case 48:
                  if (curChar == 48)
                     jjAddStates(47, 48);
                  break;
               case 50:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(49, 52);
                  break;
               case 52:
                  if (curChar == 48)
                     jjAddStates(13, 14);
                  break;
               case 54:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 69)
                     kind = 69;
                  jjAddStates(53, 54);
                  break;
               case 57:
                  if (curChar == 47)
                     jjAddStates(6, 7);
                  break;
               case 59:
                  if ((0xfffffffffffffbffL & l) != 0L)
                     jjCheckNAddTwoStates(59, 60);
                  break;
               case 60:
                  if (curChar == 10 && kind > 4)
                     kind = 4;
                  break;
               case 61:
                  if (curChar == 42)
                     jjCheckNAddTwoStates(62, 63);
                  break;
               case 62:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(62, 63);
                  break;
               case 63:
                  if (curChar == 42)
                     jjAddStates(55, 56);
                  break;
               case 64:
                  if ((0xffff7fffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(65, 63);
                  break;
               case 65:
                  if ((0xfffffbffffffffffL & l) != 0L)
                     jjCheckNAddTwoStates(65, 63);
                  break;
               case 66:
                  if (curChar == 47 && kind > 5)
                     kind = 5;
                  break;
               case 67:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(0, 5);
                  break;
               case 68:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(68, 69);
                  break;
               case 69:
                  if (curChar != 46)
                     break;
                  if (kind > 70)
                     kind = 70;
                  jjCheckNAddStates(57, 59);
                  break;
               case 70:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 70)
                     kind = 70;
                  jjCheckNAddStates(57, 59);
                  break;
               case 71:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(71, 20);
                  break;
               case 72:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(72, 73);
                  break;
               case 74:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(75);
                  break;
               case 75:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 71)
                     kind = 71;
                  jjCheckNAddTwoStates(75, 76);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
               case 13:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 66)
                     kind = 66;
                  jjCheckNAdd(13);
                  break;
               case 5:
                  jjAddStates(60, 61);
                  break;
               case 16:
                  if ((0x20100000201000L & l) != 0L && kind > 67)
                     kind = 67;
                  break;
               case 19:
                  if ((0x20100000201000L & l) != 0L && kind > 68)
                     kind = 68;
                  break;
               case 22:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(62, 63);
                  break;
               case 25:
                  if ((0x104000001040L & l) != 0L && kind > 70)
                     kind = 70;
                  break;
               case 27:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAdd(28);
                  break;
               case 29:
                  if (curChar == 92)
                     jjAddStates(64, 67);
                  break;
               case 30:
                  if ((0x54404610000000L & l) != 0L)
                     jjCheckNAdd(28);
                  break;
               case 36:
                  if (curChar == 120)
                     jjCheckNAdd(37);
                  break;
               case 37:
                  if ((0x7e0000007eL & l) != 0L)
                     jjCheckNAddTwoStates(37, 28);
                  break;
               case 38:
                  if (curChar == 88)
                     jjCheckNAdd(37);
                  break;
               case 40:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(8, 10);
                  break;
               case 41:
                  if (curChar == 92)
                     jjAddStates(68, 71);
                  break;
               case 42:
                  if ((0x54404610000000L & l) != 0L)
                     jjCheckNAddStates(8, 10);
                  break;
               case 49:
                  if (curChar == 120)
                     jjCheckNAdd(50);
                  break;
               case 50:
                  if ((0x7e0000007eL & l) != 0L)
                     jjCheckNAddStates(49, 52);
                  break;
               case 51:
                  if (curChar == 88)
                     jjCheckNAdd(50);
                  break;
               case 53:
                  if (curChar == 120)
                     jjCheckNAdd(54);
                  break;
               case 54:
                  if ((0x7e0000007eL & l) == 0L)
                     break;
                  if (kind > 69)
                     kind = 69;
                  jjCheckNAddTwoStates(54, 55);
                  break;
               case 55:
                  if ((0x20100000201000L & l) != 0L && kind > 69)
                     kind = 69;
                  break;
               case 56:
                  if (curChar == 88)
                     jjCheckNAdd(54);
                  break;
               case 59:
                  jjAddStates(72, 73);
                  break;
               case 62:
                  jjCheckNAddTwoStates(62, 63);
                  break;
               case 64:
               case 65:
                  jjCheckNAddTwoStates(65, 63);
                  break;
               case 73:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(74, 75);
                  break;
               case 76:
                  if ((0x104000001040L & l) != 0L && kind > 71)
                     kind = 71;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 5:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(60, 61);
                  break;
               case 27:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjstateSet[jjnewStateCnt++] = 28;
                  break;
               case 40:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(8, 10);
                  break;
               case 59:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(72, 73);
                  break;
               case 62:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjCheckNAddTwoStates(62, 63);
                  break;
               case 64:
               case 65:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjCheckNAddTwoStates(65, 63);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 77 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   68, 69, 71, 20, 72, 73, 58, 61, 40, 41, 43, 27, 29, 53, 56, 2,
   3, 4, 7, 7, 8, 11, 9, 10, 7, 8, 9, 10, 7, 9, 10, 7,
   8, 11, 21, 22, 25, 36, 38, 40, 41, 45, 43, 40, 41, 47, 43, 49,
   51, 40, 41, 50, 43, 54, 55, 64, 66, 70, 22, 25, 5, 6, 23, 24,
   30, 31, 33, 35, 42, 44, 46, 48, 59, 60, 74, 75,
};
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, "\73", "\155\157\144\165\154\145",
"\173", "\175", "\151\156\164\145\162\146\141\143\145", "\72", "\54", "\72\72",
"\143\157\156\163\164", "\75", "\174", "\136", "\46", "\76\76", "\74\74", "\53", "\55", "\52", "\57",
"\45", "\176", "\50", "\51", "\124\122\125\105", "\106\101\114\123\105",
"\164\171\160\145\144\145\146", "\146\154\157\141\164", "\144\157\165\142\154\145", "\154\157\156\147",
"\163\150\157\162\164", "\165\156\163\151\147\156\145\144", "\143\150\141\162",
"\167\143\150\141\162", "\142\157\157\154\145\141\156", "\157\143\164\145\164", "\141\156\171",
"\163\164\162\165\143\164", "\165\156\151\157\156", "\163\167\151\164\143\150", "\143\141\163\145",
"\144\145\146\141\165\154\164", "\145\156\165\155", "\163\145\161\165\145\156\143\145", "\74", "\76",
"\163\164\162\151\156\147", "\167\163\164\162\151\156\147", "\133", "\135",
"\162\145\141\144\157\156\154\171", "\141\164\164\162\151\142\165\164\145",
"\145\170\143\145\160\164\151\157\156", "\157\156\145\167\141\171", "\166\157\151\144", "\151\156", "\157\165\164",
"\151\156\157\165\164", "\162\141\151\163\145\163", "\143\157\156\164\145\170\164", null, null, null,
null, null, null, null, null, };
public static final String[] lexStateNames = {
   "DEFAULT",
};
static final long[] jjtoToken = {
   0xffffffffffffff81L, 0x3ffL,
};
static final long[] jjtoSkip = {
   0x7eL, 0x0L,
};
static protected SimpleCharStream input_stream;
static private final int[] jjrounds = new int[77];
static private final int[] jjstateSet = new int[154];
static protected char curChar;
public IDLParserTokenManager(SimpleCharStream stream)
{
   if (input_stream != null)
      throw new TokenMgrError("ERROR: Second call to constructor of static lexer. You must use ReInit() to initialize the static variables.", TokenMgrError.STATIC_LEXER_ERROR);
   input_stream = stream;
}
public IDLParserTokenManager(SimpleCharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
static public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
static private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 77; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
static public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
static public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

static protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

static int curLexState = 0;
static int defaultLexState = 0;
static int jjnewStateCnt;
static int jjround;
static int jjmatchedPos;
static int jjmatchedKind;

public static Token getNextToken()
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100000600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
