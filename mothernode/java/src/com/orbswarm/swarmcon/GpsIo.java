package com.orbswarm.swarmcon;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import org.iu.gps.NMEA;
import org.iu.gps.GPSInfo;
import org.iu.gps.COORD;
import org.iu.gps.DATUM;
import org.iu.gps.XY;

/** GpsIo provids communication between this software and a GPS. */

public class GpsIo extends SerialIo
{
         /** Construct a GpsIo object. */

      public GpsIo(String portName)
      {
         super(portName);
      }
         /** open a serial port */

      public void open() throws Exception
      {
            // open serial port

         super.open();

            // construct a thread to read GPS data from

         new Thread()
         {
               public void run()
               {
                  try
                  {
                     LineNumberReader lnr = new LineNumberReader(
                        new InputStreamReader(in));
                     GPSInfo record = new GPSInfo();
                     COORD coord = new COORD(0);
                     
                     byte[] buffer = new byte[256];


                        // clean out any  partial lines

                     for (int i = 0; i < 5; ++i)
                        lnr.readLine();

                        // now start working

                     while (true)
                     {
                        String line = lnr.readLine();
                        System.out.println("line: " + line);
                        try 
                        {
                           NMEA.parse(line, record);
                           record.latitude /= 100;
                           record.longitude /= 100;
                           System.out.println("east hemi: " + record.eastHemi);
                           XY utm = coord.convertToGaussKrueger(record.latitude, record.longitude);
                           System.out.println("  lat: " + record.latitude + " long: " + record.longitude);
                           System.out.println("north: " + utm.x + " east: " + utm.y);
                        }
                        catch (Exception e)
                        {
                           e.printStackTrace();
                        }
                     }
                  }
                  catch (Exception e)
                  {
                     e.printStackTrace();
                  }
               }
         }.start();
         
         System.out.println("sucess!");
      }
}
