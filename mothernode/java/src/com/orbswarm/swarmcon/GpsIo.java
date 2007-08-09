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

      public static void main(String[] args)
      {
         new GpsIo("/dev/cu.usbserial0");
      }

      /** open a serial port */
      
      public void open() throws Exception
      {
         // open serial port
         
         super.open();
         
         // activate thred

         activate();
      }

      /** Activate line listening thread */

      public void activate()
      {
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
                        //System.out.println("line: " + line);
                        try 
                        {
                           NMEA.parse(line, record);
                           record.latitude /= 100;
                           record.longitude /= 100;
                           //System.out.println("east hemi: " + record.eastHemi);
                           XY utm = coord.convertToGaussKrueger(record.latitude, record.longitude);
                           System.out.println("  lat: " + record.latitude + " long: " + record.longitude);
                           //System.out.println("north: " + utm.x + " east: " + utm.y);
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
      }
      /** Configure the gps for WAAS and WGS84 (i think) */
      void configure()
      {
         sleep(2000);
         System.out.println("line 10");
         send("$PMTK313,1*2E\r\n");
         System.out.println("line 11");
         sleep(2000);
         //send("$PMTK301,2*2D\r\n");
         System.out.println("line 20");
         send("$PMTK501,2*2B\r\n");
         System.out.println("line 21");
         sleep(2000);
         System.out.println("line 30");
         send("$PMTK401*37\r\n");
         System.out.println("line 31");
         sleep(2000);
      }
}
