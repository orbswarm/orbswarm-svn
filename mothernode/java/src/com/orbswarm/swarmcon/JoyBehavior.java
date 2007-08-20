package com.orbswarm.swarmcon;

import java.lang.Thread;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import static com.orbswarm.swarmcon.SwarmCon.*;

public class JoyBehavior extends Behavior
{
      double x1;
      double y1;
      double x2;
      double y2;
      boolean buttonPressed = false;
      int buttonNumber;
      int orbNum;
      SwarmCon swarmCon;

      Thread inputReader;

      // create a joy behavior

    public JoyBehavior(final String joyDataFileName, int orbNum, SwarmCon swarmCon)
      {
         super("Joy");
         this.orbNum = orbNum;
         this.swarmCon = swarmCon;
         
         inputReader = new Thread()
            {
                  public void run()
                  {
                     try
                     {
                        // create input file

                        final File joyDataFile = new File(joyDataFileName);

                        // while it doesn't exist, sleep

                        while (!joyDataFile.exists())
                           sleep(100);

                        // open the input file in a way to read lines
                        
                        LineNumberReader lnr = new LineNumberReader(
                           new InputStreamReader(
                              new FileInputStream(joyDataFile)));
                        String line;

                        // seek to the end of the file, only the
                        // freshest bits for this purpose
                        
                        while (lnr.readLine() != null);

                        // loop forever
                        
                        while(true)
                        {
                           line = lnr.readLine();
                           if (line != null)
                              parseEvent(line);
                           else
                              sleep(10);
                        }
                     }
                     catch (Exception e)
                     {
                        e.printStackTrace();
                     }
                  }
            };

         inputReader.start();
      }

      // parse joystick event

      public void parseEvent(String eventStr)
      {
         String[] tokens = eventStr.split(" ");

         for (int i = 0; i < tokens.length; ++i)
         {
            String token = tokens[i];
            if (token.equals("axis:"))
            {
               String axis = tokens[i + 1];
               String value = tokens[i + 3];
               i += 3;
               if (axis.equals("x1"))
                  x1 = Double.valueOf(value);
               else if (axis.equals("y1"))
                  y1 = -1 * Double.valueOf(value);
               else if (axis.equals("x2"))
                  x2 = Double.valueOf(value);
               else if (axis.equals("y2"))
                  y2 = -1 * Double.valueOf(value);
            }
            else if (token.equals("button:"))
            {
                i++;
                buttonNumber  = Integer.parseInt(tokens[i]);
                buttonPressed = true;
            }
         }
      }
      // update

      public void update(double time, MotionModel model)
      {
         System.out.println("Joy:[Orb: " + orbNum + "](" + x1 +", " + y1 + ")");
         model.setTargetRollPitchRates(x1, y1);
         swarmCon.joystickXY(orbNum, x1, y1, x2, y2);
         if (buttonPressed)
         {
             System.out.println("             Joy:[Orb: " + orbNum + "] Button " + buttonNumber);
             swarmCon.joystickButton(orbNum, buttonNumber);
             buttonPressed = false;
         }
      }
}
