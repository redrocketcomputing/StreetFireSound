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
 * $Id: IrCommandParameter.java,v 1.1 2005/03/16 04:24:31 stephen Exp $
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
public class IrCommandParameter
{
  private String metaSignal;
  private String numericSignal;

  /**
   * Constructor for IrCommandParameter.
   */
  public IrCommandParameter(String metaSignal, String numericSignal)
  {
    this.metaSignal = metaSignal;
    this.numericSignal = numericSignal;
  }
  /**
   * Returns the metaSignal.
   * @return String
   */
  public String getMetaSignal()
  {
    return metaSignal;
  }

  /**
   * Returns the numericSignal.
   * @return String
   */
  public String getNumericSignal()
  {
    return numericSignal;
  }
}
