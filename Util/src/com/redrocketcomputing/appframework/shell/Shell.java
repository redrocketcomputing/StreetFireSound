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
 * $Id: Shell.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

package com.redrocketcomputing.appframework.shell;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.redrocketcomputing.appframework.shell.commands.EnvCommand;
import com.redrocketcomputing.appframework.shell.commands.ExitCommand;
import com.redrocketcomputing.appframework.shell.commands.HelpCommand;
import com.redrocketcomputing.appframework.shell.commands.InstallCommand;
import com.redrocketcomputing.appframework.shell.commands.SetCommand;
import com.redrocketcomputing.appframework.shell.commands.SystemPropertiesCommand;
import com.redrocketcomputing.appframework.shell.commands.SystemShellCommand;
import com.redrocketcomputing.appframework.shell.commands.UnsetCommand;
import com.redrocketcomputing.appframework.shell.commands.UsageCommand;

/**
 * @author stephen
 *
 * Copyright @ StreetFire Sound Labs, LLC
 */
public class Shell
{
  private static List internalCommandClasses = new ArrayList(25);
  private static Class[] constructorParameterTypes = {Shell.class};

  private Object[] constructorArguments = new Object[1];

  private Map internalCommands = new TreeMap();

  private InputStream in;
  private PrintStream out;
  private PrintStream err;
  private Properties environment;
  private int exitValue;
  private boolean exit;

  static
  {
    // Install default commands
    Shell.installGlobally(ExitCommand.class);
    Shell.installGlobally(SetCommand.class);
    Shell.installGlobally(UnsetCommand.class);
    Shell.installGlobally(EnvCommand.class);
    Shell.installGlobally(HelpCommand.class);
    Shell.installGlobally(UsageCommand.class);
    Shell.installGlobally(InstallCommand.class);
    Shell.installGlobally(SystemShellCommand.class);
  }

  /**
   * Constructor for Shell.
   * @param in Standard input stream
   * @param out Standard output stream
   * @param err Standard error stream
   * @param environment The enviroment for the command
   */
  public Shell(InputStream in, PrintStream out, PrintStream err, Properties environment)
  {
    // save the parameters
    this.in = in;
    this.out = out;
    this.err = err;
    this.environment = environment;

    // Create constructor arguments
    constructorArguments[0] = this;

    // Loop through creating internal commands
    Class element = null;
    for (Iterator iter = internalCommandClasses.iterator(); iter.hasNext();)
    {
      // Get the internal command class
      element = (Class) iter.next();

      // Install the command
      installLocally(element);

    }
  }

  public static void installGlobally(Class internalShellCommandClass)
  {
    // Make sure this is an instance of an internal shell command
    if (!InternalShellCommand.class.isAssignableFrom(internalShellCommandClass))
    {
      throw new IllegalArgumentException(internalShellCommandClass.getName() + " is not a InternalShellCommand");
    }

    // Add to the internal command array if not already installed
    if (!internalCommandClasses.contains(internalShellCommandClass))
    {
      internalCommandClasses.add(internalShellCommandClass);
    }
  }

  /**
   * Start the shell running in the current thread
   */
  public int run(String[] args)
  {
    // Create input stream reader
    InputStreamReader inputStreamReader = new InputStreamReader(in);

    // Create a stream tokenizer
    StreamTokenizer tokenizer = new StreamTokenizer(inputStreamReader);
    tokenizer.resetSyntax();
    tokenizer.wordChars('!', '~');
    tokenizer.whitespaceChars(0, 32);
    tokenizer.quoteChar('"');
    tokenizer.quoteChar('\'');
    tokenizer.eolIsSignificant(true);

    // Create argument list
    List commandLine = new ArrayList();

    // Initizalize the exit value
    exitValue = 0;
    exit = false;

    // Loop until exist requested
    while (!exit)
    {
      try
      {
        // Display the prompt
        out.print(environment.getProperty("shell.prompt", "> "));
        out.flush();

        // Clear the parsed command line
        commandLine.clear();

        // Read the first token
        int token = tokenizer.nextToken();
        if (token != StreamTokenizer.TT_EOL)
        {
          // Save the command name
          String commandName = tokenizer.sval;

          // Read the rest of the command line
          while (tokenizer.nextToken() != StreamTokenizer.TT_EOL)
          {
            // Add argument to command line array
            commandLine.add(tokenizer.sval);
          }

          // Try to find command to execute
          Command command = getInternalCommand(commandName);
          if (command != null)
          {
            // Execute the command and check the result
            command.run((String[])commandLine.toArray(new String[commandLine.size()]));
          }
          else
          {
            // Log unknow command
            err.println("unknown command: " + commandName);
          }
        }
        else if (token == StreamTokenizer.TT_EOF)
        {
          // Goodbye
          exit(0);
        }

      }
      catch (EOFException e)
      {
        // All done exit
        exit(0);
      }
      catch (IOException e)
      {
        // Log error
        err.println("input stream failure");

        // Force the shell to exit
        exit(-1);
      }
    }

    // Return the exit value
    return exitValue;
  }

  /**
   * Cause the shell to exit with the specified exit value
   * @param exitValue The shell exist value
   */
  public void exit(int exitValue)
  {
    this.exitValue = exitValue;
    this.exit = true;
  }

  /**
   * Return the set on currently installed internal commands
   * @return InternalShellCommand[] The array of internal commands
   */
  public InternalShellCommand[] getInternalCommands()
  {
    return (InternalShellCommand[])internalCommands.values().toArray(new InternalShellCommand[internalCommands.size()]);
  }

  /**
   * Lookup an internal shell command using the specified name
   * @param name The name of the command to lookup
   * @return InternalShellCommand The internal shell command found or null is not found
   */
  public final InternalShellCommand getInternalCommand(String name)
  {
    return (InternalShellCommand)internalCommands.get(name);
  }

  /**
   * Returns the environment.
   * @return Properties
   */
  Properties getEnvironment()
  {
    return environment;
  }

  /**
   * Returns the error stream.
   * @return PrintStream
   */
  PrintStream getErrorStream()
  {
    return err;
  }

  /**
   * Returns the input stream
   * @return InputStream
   */
  InputStream getInputStream()
  {
    return in;
  }

  /**
   * Returns the output stream
   * @return PrintStream
   */
  PrintStream getOutStream()
  {
    return out;
  }

  /**
   * Sets the input stream.
   * @param in The in to set
   */
  void setInputStream(InputStream in)
  {
    this.in = in;
  }

  /**
   * Sets the output stream.
   * @param out The out to set
   */
  void setOutputStream(PrintStream out)
  {
    this.out = out;
  }

  /**
   * Sets the error stream.
   * @param err The err to set
   */
  void setErrorStream(PrintStream err)
  {
    this.err = err;
  }

  /**
   * Return the specified enviroment string
   * @param name The enviroment string to lookup
   * @return String The value of the environment string
   */
  public final String getVariable(String name)
  {
    return environment.getProperty(name);
  }

  /**
   * Set the specified environment string to the provided value
   * @param name The enviroment string to set
   * @param value The value to set
   */
  public final void setVariable(String name, String value)
  {
    environment.setProperty(name, value);
  }

	public void installLocally(InternalShellCommand command)
	{
      // Add to the internal command map
      internalCommands.put(command.getCommandName(), command);
	}

  public void installLocally(Class commandClass)
  {
    try
    {
      // Get the contructor
      Constructor constructor = commandClass.getConstructor(constructorParameterTypes);

      // Create the internal command
      InternalShellCommand command = (InternalShellCommand)constructor.newInstance(constructorArguments);

      // Add to the internal command map
      internalCommands.put(command.getCommandName(), command);
    }
    catch (NoSuchMethodException e)
    {
      // Log error
      err.println("NoSuchMethodException installing locally class: " + commandClass.getName());
    }
    catch (InstantiationException e)
    {
      // Log error
      err.println("InstantiationException installing locally class: " + commandClass.getName());
    }
    catch (IllegalAccessException e)
    {
      // Log error
      err.println("IllegalAccessException installing locally class: " + commandClass.getName());
    }
    catch (InvocationTargetException e)
    {
      // Log error
      err.println("InvocationTargetException installing locally class: " + commandClass.getName());
    }
  }
}
