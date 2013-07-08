package com.redrocketcomputing.havi.system.amm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.Attributes;

import org.havi.system.AmCodeUnitInterface;
import org.havi.system.types.ApplicationModuleProfile;
import org.havi.system.types.HaviApplicationModuleManagerBadLocationException;
import org.havi.system.types.HaviApplicationModuleManagerBadProfileException;
import org.havi.system.types.HaviApplicationModuleManagerClassloaderErrorException;
import org.havi.system.types.HaviApplicationModuleManagerException;

import com.redrocketcomputing.util.log.LoggerSingleton;

/**
 * A class loader for loading jar files, both local and remote.
 */
class ApplicationModuleCodeUnitClassLoader extends URLClassLoader
{
  private JarURLConnection connection;
  
  /**
   * Creates a new ApplicationModuleCodeUnitClassLoader for the specified url.
   * @param amUrl the url of the jar file
   */
  public ApplicationModuleCodeUnitClassLoader(URL amUrl) throws HaviApplicationModuleManagerException
  {
    // Construct super class
    super(new URL[]{amUrl});
    
    try
    {
      // Create Jar URL
      URL jarUrl = new URL("jar", "", amUrl + "!/");
      
      // Create URL connection
      connection = (JarURLConnection)jarUrl.openConnection();
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerBadLocationException(e.toString());
    }
  }
  
  /**
   * Build the ApplicationModuleProfile for the specifed jar file
   * @return The ApplicationModuleProfile for ApplicationModule 
   * @throws HaviApplicationModuleManagerException Thrown if there is a problem reading the profile from the manifest
   */
  public ApplicationModuleProfile getProfile() throws HaviApplicationModuleManagerException
  {
    try
    {
      // Get the main attributes
      Attributes attributes = connection.getMainAttributes();

      // Get the code unit name
      String codeUnitName = attributes.getValue("AMCU-NAME");
      if (codeUnitName == null)
      {
        // Bad
        throw new HaviApplicationModuleManagerBadProfileException("no AMCU-NAME found");
      }
      
      // Get the manufacture
      String codeUnitManufacture = attributes.getValue("AMCU-MANUFACTURE");
      if (codeUnitManufacture == null)
      {
        // Bad
        throw new HaviApplicationModuleManagerBadProfileException("no AMCU-MANUFACTURE found");
      }
      
      // Get the description
      String codeUnitDescription = attributes.getValue("AMCU-DESCRIPTION");
      if (codeUnitDescription == null)
      {
        // Optional
        codeUnitDescription = "";
      }
      
      // Get code unit class name
      String codeUnitClassName = attributes.getValue("AMCU-CLASSNAME");
      if (codeUnitClassName == null)
      {
        // Bad
        throw new HaviApplicationModuleManagerBadProfileException("no AMCU-CLASSNAME found");
      }
      
      // Get properties path
      String codeUnitProperties = attributes.getValue("AMCU-PROPERTIES");
      if (codeUnitProperties == null)
      {
        // Optional
        codeUnitProperties = "";
      }
      
      // Return the profile
      return new ApplicationModuleProfile(codeUnitName, codeUnitManufacture, codeUnitDescription, codeUnitClassName, codeUnitProperties);
    }
    catch (MalformedURLException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerBadLocationException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerBadProfileException(e.toString());
    }
  }
  
  /**
   * Create a ApplicationModuleCodeUnit from the specified class name
   * @param codeUnitClassName The class name of the ApplicationModuleCodeUnit to create
   * @return The new ApplicationCodeUnit
   * @throws HaviApplicationModuleManagerException Throw if the code unit was not found in the jars
   */
  public AmCodeUnitInterface createCodeUnit() throws HaviApplicationModuleManagerException
  {
    try
    {
      // Get the main attributes
      Attributes attributes = connection.getMainAttributes();

      // Get code unit class name
      String codeUnitClassName = attributes.getValue("AMCU-CLASSNAME");
      if (codeUnitClassName == null)
      {
        // Bad
        throw new HaviApplicationModuleManagerBadProfileException("no AMCU-CLASSNAME found");
      }

      // Create the class
      Class codeUnitClass = loadClass(codeUnitClassName);
      
      // Build constructor query
      Class[] parameterTypes = new Class[1];
      parameterTypes[0] = ApplicationModuleProfile.class;

      // Get the constructor
      Constructor constructor = codeUnitClass.getConstructor(parameterTypes);

      // Build arguments
      Object[] arguments = new Object[1];
      arguments[0] = getProfile();

      // Return new instance of the class
      return (AmCodeUnitInterface)constructor.newInstance(arguments);
    }
    catch (ClassNotFoundException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
    catch (InstantiationException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
    catch (SecurityException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
    catch (NoSuchMethodException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
    catch (IllegalArgumentException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
    catch (InvocationTargetException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
  }
  
  /**
   * Return the ApplicationModuleCodeUnit configuration properties
   * @param codeUnitPropertiesPath The path to the configuration properties
   * @return The Properties or null if not found.
   * @throws HaviApplicationModuleManagerException Thrown is a problem is encountered read the properties resource
   */
  public Properties getConfiguration() throws HaviApplicationModuleManagerException
  {
    try
    {
      // Create empty properties
      Properties properties = new Properties();

      // Get the main attributes
      Attributes attributes = connection.getMainAttributes();

      // Get properties path
      String codeUnitPropertiesPath = attributes.getValue("AMCU-PROPERTIES");
      if (codeUnitPropertiesPath != null)
      {
        // Create input stream for properties file
        InputStream resourceStream = getResourceAsStream(codeUnitPropertiesPath);
        if (resourceStream != null)
        {
          // Read the properties
          properties.load(resourceStream);
          
          // Close the input stream
          resourceStream.close();
        }
      }
      else
      {
        LoggerSingleton.logWarning(this.getClass(), "getConfiguration", "no properties found");
      }
      
      // Return the properties
      return properties;
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviApplicationModuleManagerClassloaderErrorException(e.toString());
    }
  }
}