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
 * $Id: GplHeaderFileOutputStream.java,v 1.1 2005/02/22 03:46:08 stephen Exp $
 */

package com.streetfiresound.codegenerator.output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author stephen
 *
 */
public class GplHeaderFileOutputStream extends FileOutputStream
{

  /**
   * Constructor for GplHeaderFileOutputStream.
   * @param fileName The name of the file to open
   * @throws FileNotFoundException
   */
  public GplHeaderFileOutputStream(String fileName) throws FileNotFoundException
  {
    // Construct superclass
    super(fileName);

    try
    {
      // Write the file header
      this.write("/*\n".getBytes());
      this.write(" * Copyright (C) 2004 by StreetFire Sound Labs\n".getBytes());
      this.write(" *\n".getBytes());
      this.write(" * This program is free software; you can redistribute it and/or modify\n".getBytes());
      this.write(" * it under the terms of the GNU General Public License as published by\n".getBytes());
      this.write(" * the Free Software Foundation; either version 2 of the License, or\n".getBytes());
      this.write(" * (at your option) any later version.\n".getBytes());
      this.write(" *\n".getBytes());
      this.write(" * This program is distributed in the hope that it will be useful,\n".getBytes());
      this.write(" * but WITHOUT ANY WARRANTY; without even the implied warranty of\n".getBytes());
      this.write(" * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n".getBytes());
      this.write(" * GNU General Public License for more details.\n".getBytes());
      this.write(" *\n".getBytes());
      this.write(" * You should have received a copy of the GNU General Public License\n".getBytes());
      this.write(" * along with this program; if not, write to the Free Software\n".getBytes());
      this.write(" * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n".getBytes());
      this.write(" */\n".getBytes());
      this.write("\n".getBytes());

    }
    catch (IOException e)
    {
      // Very bad, but do it anyways, Stephen
      throw new FileNotFoundException(e.toString());
    }
  }

}
