package com.orbswarm.swarmcon.sys;

import java.io.*;
import java.net.URL;
import java.lang.ClassLoader;
import org.apache.log4j.Logger;

public class JarLibrary
{
  private static Logger log = Logger.getLogger(JarLibrary.class);

  public static void load(String name) throws Exception
  {
    log.debug("loading library: " + name);

    // establish the correct name of the library based on os

    String libname = System.mapLibraryName(name);

    try
    {
      // try to find library in jar file

      URL lib = ClassLoader.getSystemClassLoader().getResource(
        "lib/" + libname);

      // if found

      if (lib != null)
      {
        // copy it into tmp

        File temp = File.createTempFile(name + ".", ".lib");
        temp.deleteOnExit();
        InputStream in = lib.openStream();
        OutputStream out = new FileOutputStream(temp);
        byte[] buffer = new byte[32 * 1024];
        int len;
        while ((len = in.read(buffer)) != -1)
        {
          out.write(buffer, 0, len);
        }
        out.close();

        // load lib file now in tmp

        System.load(temp.getPath());
      }

      // if not found try loading it from the system

      else
      {
        log.debug("should not get here!");
        // System.loadLibrary("/usr/lib/rxtx-2/" + libname);
        // System.loadLibrary("/usr/lib/rxtx-2/" + libname);
      }
    }

    catch (Exception ex)
    {
      String cause = ex.getMessage();
      if (cause == null)
        cause = ex.toString();
      throw new Exception(cause);
    }

    catch (UnsatisfiedLinkError er)
    {
      String sysLibName = "/usr/lib/" + libname;
      log.debug("loading system library: " + sysLibName);
      System.load(sysLibName);

      // System.loadLibrary(libname);
      // throw new Exception (er.getMessage());
    }
  }
}
