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
 * $Id: AttributeValueComparatorFlyweight.java,v 1.1 2005/02/22 03:44:26 stephen Exp $
 */

package com.redrocketcomputing.havi.util;

import org.havi.system.constants.ConstComparisonOperator;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Red Rocket Computing, LLC</p>
 * @author unascribed
 * @version 1.0
 */

public class AttributeValueComparatorFlyweight
{
  private static AttributeValueComparator[] comparators;

  // ANY Operator
  private static class AnyComparator implements AttributeValueComparator
  {
    public AnyComparator(boolean sequenceComparator)
    {
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Alway match
      return true;
    }
  }

  // EQU Operator
  private static class EquComparator implements AttributeValueComparator
  {
    private int start;

    public EquComparator(boolean sequence)
    {
      start = sequence ? 4 : 0;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Check lengths
      if (lhs.length != rhs.length)
      {
        return false;
      }

      // Check bytes
      for (int i = start; i < lhs.length; i++)
      {
        if (lhs[i] != rhs[i])
        {
          return false;
        }
      }

      // All good
      return true;
    }
  }

  // NEQU Operator
  private static class NequComparator implements AttributeValueComparator
  {
    short operation;

    public NequComparator(boolean sequence)
    {
      operation = sequence ? ConstComparisonOperator.SEQU : ConstComparisonOperator.EQU;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Just the logical not of EQU
      return !AttributeValueComparatorFlyweight.getComparator(operation).match(lhs, rhs);
    }
  }

  // LE Operator
  private static class LeComparator implements AttributeValueComparator
  {
    private int start;

    public LeComparator(boolean sequence)
    {
      start = sequence ? 4 : 0;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Get the shortest length
      int smallest = Math.min(lhs.length, rhs.length);

      // Check the bytes
      for (int i = start; i < smallest; i++)
      {
        // If the bytes are not equal the check for LE of the bytes
        if (lhs[i] != rhs[i])
        {
          return lhs[i] <= rhs[i];
        }
      }

      // Bytes are identical to the shortest length, compare the actual lengths
      return lhs.length <= rhs.length;
    }
  }

  // GE Operator
  private static class GeComparator implements AttributeValueComparator
  {
    private int start;

    public GeComparator(boolean sequence)
    {
      start = sequence ? 4 : 0;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Get the shortest length
      int smallest = Math.min(lhs.length, rhs.length);

      // Check the bytes
      for (int i = start; i < smallest; i++)
      {
        // If the bytes are not equal the check for GE of the bytes
        if (lhs[i] != rhs[i])
        {
          return lhs[i] >= rhs[i];
        }
      }

      // Bytes are identical to the shortest length, compare the actual lengths
      return lhs.length >= rhs.length;
    }
  }

  // GT Operator
  private static class GtComparator implements AttributeValueComparator
  {
    short operation;

    public GtComparator(boolean sequence)
    {
      operation = sequence ? ConstComparisonOperator.SLE : ConstComparisonOperator.LE;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Just the logical not of LE
      return !AttributeValueComparatorFlyweight.getComparator(operation).match(lhs, rhs);
    }
  }

  // LT Operator
  private static class LtComparator implements AttributeValueComparator
  {
    short operation;

    public LtComparator(boolean sequence)
    {
      operation = sequence ? ConstComparisonOperator.SGE : ConstComparisonOperator.GE;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Just the logical not of LE
      return !AttributeValueComparatorFlyweight.getComparator(operation).match(lhs, rhs);
    }
  }

  // BWA Operator
  private static class BwaComparator implements AttributeValueComparator
  {
    private int start;

    public BwaComparator(boolean sequence)
    {
      start = sequence ? 4 : 0;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Check the lengths
      if (lhs.length != rhs.length)
      {
        return false;
      }

      // Check the bytes
      for (int i = start; i < lhs.length; i++)
      {
        // See spec for defination
        if ((lhs[i] & rhs[i]) != 0)
        {
          return true;
        }
      }

      // No match
      return false;
    }
  }

  // BW0 Operator
  private static class BwoComparator implements AttributeValueComparator
  {
    private int start;

    public BwoComparator(boolean sequence)
    {
      start = sequence ? 4 : 0;
    }

    public boolean match(byte[] lhs, byte[] rhs)
    {
      // Check the lengths
      if (lhs.length != rhs.length)
      {
        return false;
      }

      // Check the bytes
      for (int i = start; i < lhs.length; i++)
      {
        // See spec for defination
        if ((lhs[i] | rhs[i]) != 0xff)
        {
          return false;
        }
      }

      // MATCHES
      return true;
    }
  }

  static
  {
    // Initialize the flyweights
    comparators = new AttributeValueComparator[17];
    comparators[0] = new AnyComparator(false);
    comparators[1] = new EquComparator(false);
    comparators[2] = new NequComparator(false);
    comparators[3] = new GtComparator(false);
    comparators[4] = new GeComparator(false);
    comparators[5] = new LtComparator(false);
    comparators[6] = new LeComparator(false);
    comparators[7] = new BwaComparator(false);
    comparators[8] = new BwoComparator(false);
    comparators[9] = new EquComparator(true);
    comparators[10] = new NequComparator(true);
    comparators[11] = new GtComparator(true);
    comparators[12] = new GeComparator(true);
    comparators[13] = new LtComparator(true);
    comparators[14] = new LeComparator(true);
    comparators[15] = new BwaComparator(true);
    comparators[16] = new BwoComparator(true);
  }

  /**
   * Return the AttributeValueComparator flyweight for the specified operations
   * @param operation The operation code
   * @return The comparator
   */
  public static AttributeValueComparator getComparator(short operation)
  {
    if ((operation & 0x8000) != 0)
    {
      return comparators[ConstComparisonOperator.BWO + (operation & 0xf)];
    }

    return comparators[operation];
  }

  /**
   * Prohibted constructor
   */
  private AttributeValueComparatorFlyweight()
  {
  }
}
