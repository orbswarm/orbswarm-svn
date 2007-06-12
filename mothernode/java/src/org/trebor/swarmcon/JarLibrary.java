package org.trebor.swarmcon;

import java.io.*;
import java.net.URL;
 
public class JarLibrary 
{
      public static void load (String name) throws Exception 
      {
         String osname = System.getProperties().getProperty ("os.name");
         String libname;
         
         if (osname.equals ("Mac OS X")) 
         {
	    libname = "./lib/lib" + name + ".jnilib";
         }
         else
         {
	    throw new Exception ("Unsupported platform: \"" + osname + "\"");
         }
         
         try 
         {
    	    URL lib = java.lang.ClassLoader.getSystemClassLoader().getResource(libname);
	    if (lib == null)
               throw new Exception ("Library \"" + libname + "\" not in Jar file");
            
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
            
    	    out.close ();
    	    System.load (temp.getPath());
         }
         
	catch (Exception ex) 
        {
	    String cause = ex.getMessage();
	    if (cause == null) cause = ex.toString();
	    throw new Exception (cause);
        }
         
	catch (UnsatisfiedLinkError er) 
        {
           throw new Exception (er.getMessage());
        }
      }
}
