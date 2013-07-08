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
 * $Id: LsCatResponse.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.cddb;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LsCatResponse extends CddbResponse
{
  private List categoryList = null;

  /**
   * Unmarshals information specific this response.
   * 
   * @param br
   * @throws IOException
   */
  protected void unmarshalBody(BufferedReader br) throws IOException
  {
    this.categoryList = new ArrayList();

    switch (this.code)
    {
      case 210:
        String line = null;
        while ((line = br.readLine()) != null && !line.equals("."))
        {
          this.categoryList.add(line);
        }
        break;
      default:
    }
  }

  public String getMessage()
  {
    switch (this.code)
    {
      case 210:
        return ("OK - category list found.");
      default:
        return getDefaultMessage(this.code);
    }
  }

  /**
   * If command is successful, returns list of categories. Otherwise list returns empty.
   * 
   * @return
   */
  public List getCategoryList()
  {
    return this.categoryList;
  }
}
