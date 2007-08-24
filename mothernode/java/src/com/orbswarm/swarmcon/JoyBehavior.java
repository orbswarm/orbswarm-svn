package com.orbswarm.swarmcon;

import java.lang.Thread;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;

import static com.orbswarm.swarmcon.SwarmCon.*;

import com.orbswarm.swarmcomposer.util.TokenReader;

public class JoyBehavior extends Behavior
{
      double x1;
      double y1;
      double x2;
      double y2;
      boolean buttonPressed = false;
      int buttonNumber;
      int orbNum;
      int joystickNum;
      SwarmCon swarmCon;
      static int[] joystickToOrbMapping = null;

      Thread inputReader;

      // create a joy behavior

    public JoyBehavior(final String joyDataFileName, int joystickNum, SwarmCon swarmCon)
      {
         super("Joy");
         this.joystickNum = joystickNum;
         orbNum = joystickToOrbNum(joystickNum);

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
               swarmCon.joystickXYMotors(orbNum, x1, y1, x2, y2);
            }
            else if (token.equals("button:"))
            {
                i++;
                buttonNumber  = Integer.parseInt(tokens[i]);
                buttonPressed = true;
                swarmCon.joystickButton(orbNum, buttonNumber);
            }
         }
      }
      // update

      public void update(double time, MotionModel model)
      {
         swarmCon.joystickXY(orbNum, x1, y1, x2, y2);
         model.setTargetRollPitchRates(x1, y1);
      }

    // joystick to orb mapping
    public static int joystickToOrbNum(int joystickNum) {
        if (joystickToOrbMapping == null) {
            readJoystickToOrbMapping();
        }
        if (joystickToOrbMapping == null) {
            // if we still don't have the mapping, return the default (1:1 correspondence)
            return joystickNum;
        }
        return joystickToOrbMapping[joystickNum];
    }
    
    public static void readJoystickToOrbMapping() {
        try {
            TokenReader reader = new TokenReader("resources/JoyStickToOrbMapping.txt");
            reader.readUntilToken("num_orbs");
            int numOrbs = reader.readInt();
            int [] mapping = new int[numOrbs];
            String token = reader.readToken();
            while(token != null) {
                System.out.println("token:" + token);
                if (token.equalsIgnoreCase("joystick")) {
                    int j = reader.readInt();
                    String ot = reader.readToken(); // "orb"
                    int o = reader.readInt();
                    mapping[j] = o;
                }
                token = reader.readToken();
            }
            joystickToOrbMapping = mapping;
        } catch (Exception ex) {
            System.out.println("JoyBehaviour.readJoystickToOrbMapping caught exception.");
            System.out.println(" Using default mapping (0=0, ..., 5=5)");
            ex.printStackTrace();
        }
    }
            
    
}
