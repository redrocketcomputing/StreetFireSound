package com.redrocketcomputing.havi.system.dcmm;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.Attributes;

import org.havi.system.DcmCodeUnitInterface;
import org.havi.system.types.DcmProfile;
import org.havi.system.types.HaviDcmManagerBadLocationException;
import org.havi.system.types.HaviDcmManagerBadProfileException;
import org.havi.system.types.HaviDcmManagerClassloaderErrorException;
import org.havi.system.types.HaviDcmManagerException;

/**
 * A class loader for loading jar files, both local and remote.
 */
class DeviceControlModuleCodeUnitClassLoader extends URLClassLoader
{
  private JarURLConnection connection;
  
  /**
   * Creates a new DeviceControlModuleCodeUnitClassLoader for the specified url.
   * @param amUrl the url of the jar file
   */
  public DeviceControlModuleCodeUnitClassLoader(URL amUrl) throws HaviDcmManagerException
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
      throw new HaviDcmManagerBadLocationException(e.toString());
    }
  }
  
  /**
   * Build the DcmProfile for the specifed jar file
   * @return The DcmProfile for Dcm 
   * @throws HaviDcmManagerException Thrown if there is a problem reading the profile from the manifest
   */
  public DcmProfile getProfile() throws HaviDcmManagerException
  {
    try
    {
      // Get the main attributes
      Attributes attributes = connection.getMainAttributes();

      // Get the code unit name
      String codeUnitName = attributes.getValue("DCMCU-NAME");
      if (codeUnitName == null)
      {
        // Bad
        throw new HaviDcmManagerBadProfileException("no DCMCU-NAME found");
      }
      
      // Get the manufacture
      String codeUnitManufacture = attributes.getValue("DCMCU-MANUFACTURE");
      if (codeUnitManufacture == null)
      {
        // Bad
        throw new HaviDcmManagerBadProfileException("no DCMCU-MANUFACTURE found");
      }
      
      // Get the description
      String codeUnitDescription = attributes.getValue("DCMCU-DESCRIPTION");
      if (codeUnitDescription == null)
      {
        // Optional
        codeUnitDescription = "";
      }
      
      // Get code unit class name
      String codeUnitClassName = attributes.getValue("DCMCU-CLASSNAME");
      if (codeUnitClassName == null)
      {
        // Bad
        throw new HaviDcmManagerBadProfileException("no DCMCU-CLASSNAME found");
      }
      
      // Get properties path
      String codeUnitProperties = attributes.getValue("DCMCU-PROPERTIES");
      if (codeUnitProperties == null)
      {
        // Optional
        codeUnitProperties = "";
      }
      
      // Return the profile
      return new DcmProfile(codeUnitName, codeUnitManufacture, codeUnitDescription, codeUnitClassName, codeUnitProperties);
    }
    catch (MalformedURLException e)
    {
      // Translate
      throw new HaviDcmManagerBadLocationException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviDcmManagerBadProfileException(e.toString());
    }
  }
  
  /**
   * Create a DcmCodeUnit from the specified class name
   * @param codeUnitClassName The class name of the DcmCodeUnit to create
   * @return The new DcmCodeUnit
   * @throws HaviDcmManagerException Throw if the code unit was not found in the jars
   */
  public DcmCodeUnitInterface createCodeUnit() throws HaviDcmManagerException
  {
    try
    {
      // Get the main attributes
      Attributes attributes = connection.getMainAttributes();

      // Get code unit class name
      String codeUnitClassName = attributes.getValue("DCMCU-CLASSNAME");
      if (codeUnitClassName == null)
      {
        // Bad
        throw new HaviDcmManagerBadProfileException("no DCMCU-CLASSNAME found");
      }

      // Create the class
      Class codeUnitClass = loadClass(codeUnitClassName);

      // Return new instance of the class
      return (DcmCodeUnitInterface)codeUnitClass.newInstance();
    }
    catch (ClassNotFoundException e)
    {
      // Translate
      throw new HaviDcmManagerClassloaderErrorException(e.toString());
    }
    catch (InstantiationException e)
    {
      // Translate
      throw new HaviDcmManagerClassloaderErrorException(e.toString());
    }
    catch (IllegalAccessException e)
    {
      // Translate
      throw new HaviDcmManagerClassloaderErrorException(e.toString());
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviDcmManagerClassloaderErrorException(e.toString());
    }
  }
  
  /**
   * Return the DcmCodeUnit configuration properties
   * @param codeUnitPropertiesPath The path to the configuration properties
   * @return The Properties or null if not found.
   * @throws HaviDcmManagerException Thrown is a problem is encountered read the properties resource
   */
  public Properties getConfiguration() throws HaviDcmManagerException
  {
    try
    {
      // Create empty properties
      Properties properties = new Properties();

      // Get the main attributes
      Attributes attributes = connection.getMainAttributes();

      // Get properties path
      String codeUnitPropertiesPath = attributes.getValue("DCMCU-PROPERTIES");
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
      
      // Return the properties
      return properties;
    }
    catch (IOException e)
    {
      // Translate
      throw new HaviDcmManagerClassloaderErrorException(e.toString());
    }
  }
}