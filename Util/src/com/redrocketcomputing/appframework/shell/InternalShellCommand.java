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
 * $Id: InternalShellCommand.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
abstract public class InternalShellCommand implements Command
{
  protected InputStream in;
  protected PrintStream out;
  protected PrintStream err;
  protected Shell parent;

  /**
   * Constructor for InternalShellCommand.
   * @param in Standard input stream
   * @param out Standard output stream
   * @param err Standard error stream
   * @param environment The enviroment for the command
   */
  public InternalShellCommand(Shell parent)
  {
    // Save the parent
    this.parent = parent;
    this.in = parent.getInputStream();
    this.out = parent.getOutStream();
    this.err = parent.getErrorStream();
  }

  /**
   * Return the specified enviroment string
   * @param name The enviroment string to lookup
   * @return String The value of the environment string
   */
  protected final String getVariable(String name)
  {
    return parent.getEnvironment().getProperty(name);
  }

  /**
   * Set the specified environment string to the provided value
   * @param name The enviroment string to set
   * @param value The value to set
   */
  protected final void setVariable(String name, String value)
  {
    parent.getEnvironment().setProperty(name, value);
  }

  /**
   * Remove the specified enviroment variable.
   * @param name The name of the variable to remove
   */
  protected final void unsetVariable(String name)
  {
    parent.getEnvironment().remove(name);
  }

  /**
   * Return an Enumeration of the properties
   * @return Enumeration
   */
  protected final Properties getEnvironment()
  {
    return parent.getEnvironment();
  }

  /**
   * Return the command name used to lookup the command
   * @return String The name of the command
   */
  abstract public String getCommandName();

  /**
   * Return a text string containing the summary of command purpose
   * @return String
   */
  abstract public String getCommandSummary();

  /**
   * Return the command usage string
   * @return String The usage string
   */
  abstract public String getCommandUsage();

  /**
   * @see com.redrocketcomputing.appframework.shell.Command#execute(String[])
   */
  abstract public int run(String[] args);
}
