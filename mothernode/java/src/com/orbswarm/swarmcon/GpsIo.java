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

import org.apache.log4j.Logger;

import static java.lang.Math.*;

/** GpsIo provids communication between this software and a GPS. */

public class GpsIo extends SerialIo
{
      private static Logger log = Logger.getLogger(GpsIo.class);

      /** Line listener for NMEA GPS. */

      LineListener gpsLineListener = new  LineListener()
         {
               public void lineEvent(String line)
               {
                  GPSInfo record = new GPSInfo();
                  NMEA.parse(line, record);
                  if (record.quality > 0)
                  {
                     log.debug("east hemi: " + record.eastHemi);
                     log.debug("north hemi: " + record.northHemi);
                     log.debug("utc: " + record.utc);
                     log.debug("quality: " + record.quality);
                     record.latitude = nmeaToDecimalDegress(record.latitude) *
                        (record.northHemi == 'S' ? -1 : 1);
                     record.longitude = nmeaToDecimalDegress(record.longitude) *
                        (record.eastHemi == 'W' ? -1 : 1);
                     log.debug("lat: " + record.latitude +
                                        " lon: " + record.longitude);
                     Point utm = latLonToUtm(record.latitude, record.longitude);
                     log.debug("easting: " + utm.y + " northing: " + utm.x);
                  }
                  else
                  {
                    log.debug("no GPS data.");
                  }
               }
         };
      /** Construct a GpsIo object.
       *
       * @param port serial port gps is attached to
       */

      public GpsIo(String port)
      {
         super(port);

         // configure the gps

         configure();

         // register line listener

         registerLineListener(gpsLineListener);
      }

      public static void main(String[] args)
      {
         try
         {
            //new GpsIo("/dev/tty1");  // swarm computer
            new GpsIo("/dev/cu.usbserial0"); // trebor's mac
            //log.debug("utm: " + nmeaLatLonToUtm(3745.762031, -12224.047551));
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /** Convert NMEA latitude longitude to a UTM coordinate.
       *
       * @param latitude NMEA formatted latitude
       * @param longitude NMEA formatted longitude
       *
       * @return a UTM coordinate
       */
      
      static public Point nmeaLatLonToUtm(double latitude, double longitude)
      {
         return latLonToUtm(
            nmeaToDecimalDegress(latitude),
            nmeaToDecimalDegress(longitude));
      }
      
      /** Convert decimal latitude longitude to a UTM coordinate.
       *
       * @param latitude decimal degrees latitude
       * @param longitude decimal degrees longitude
       *
       * @return a UTM coordinate 
       */

      static public Point latLonToUtm(double latitude, double longitude)
      {
         com.genlogic.GlgUtmPoint utm = new com.genlogic.GlgUtmPoint();
         com.genlogic.GlgUtmMgrs.LatLonToUtm(latitude, longitude, utm);
         return new Point(utm.easting, utm.northing);
      }

      /** Convert NMEA to decimal degrees.
       *
       * @param nmea latitute or longitude value
       * @return decimal degress
       */

      static public double nmeaToDecimalDegress(double nmea)
      {
         int D = (int)(nmea / 100);
         double m = nmea - (D * 100);
         return D+(m/60);
      }
      
      /** Configure the gps for WAAS and WGS84 (i think) */
      
      void configure()
      {
         sleep(2000);
         send("$PMTK313,1*2E\r\n");
         sleep(2000);
         send("$PMTK501,2*2B\r\n");
         sleep(2000);
         send("$PMTK401*37\r\n");
         sleep(2000);
      }
}
