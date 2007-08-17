package com.orbswarm.swarmcon;

import java.io.*;
import java.net.URL;
import java.lang.ClassLoader;
 
public class JarLibrary 
{
      public static void load (String name) throws Exception 
      {
         // establish the correct name of the library based on os
         
         String libname = System.mapLibraryName(name);

         try 
         {
            // try to find library in jar file

    	    URL lib = ClassLoader.getSystemClassLoader()
               .getResource("lib/" + libname);

            // if found

	    if (lib != null)
            {
               // copy it into tmp
               
               File temp = File.createTempFile (name + ".", ".lib");
               temp.deleteOnExit ();
               InputStream in = lib.openStream();
               OutputStream out = new FileOutputStream (temp);
               byte [] buffer = new byte [32*1024];
               int len;
               while ((len = in.read (buffer)) != -1) 
               {
                  out.write (buffer, 0, len);
               }
               out.close();

               // load lib file now in tmp

               System.load(temp.getPath());
            }

            // if not found try loading it from the system

            else
            {
               //System.loadLibrary("/usr/lib/rxtx-2/" + libname);
               //System.loadLibrary("/usr/lib/rxtx-2/" + libname);
            }
         }
         
         catch (Exception ex) 
         {
	    String cause = ex.getMessage();
	    if (cause == null) cause = ex.toString();
	    throw new Exception (cause);
         }
         
         catch (UnsatisfiedLinkError er) 
         {
            String sysLibName = "/usr/lib/rxtx-2/" + libname;
//            String sysLibName = "/usr/lib/" + libname;
            System.out.println("loading system library: " + sysLibName);
            System.load(sysLibName);
            
            //System.loadLibrary(libname);
            //throw new Exception (er.getMessage());
         }
      }
}
