package com.orbswarm.swarmcon.io;

import java.util.Vector;
import java.util.HashMap;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.File;

import org.trebor.util.JarTools;

import org.apache.log4j.Logger;

/** JoystickManager provides access to joystick events.  The
 * JoystickManager also is a vector of StickInfo, so it functions as it's
 * own database of joystick information. */

@SuppressWarnings("serial")
public class JoystickManager extends Vector<JoystickManager.StickInfo>
{
    private static Logger log = Logger.getLogger(JoystickManager.class);

      /** indicates listeners which should get events for all joysticks */
      public static final int ALL_STICKS = -1;

      /** array used to "loop" through listeners. */
      private static int[] stickArray = {ALL_STICKS, ALL_STICKS};

      /** Class to represent a collections of listeners. */
      class Listeners extends Vector<Listener> {}

      /** Joystick listeners which are mapped to a particualr joystick. */

      HashMap<Integer, Listeners> filteredListeners = 
         new HashMap<Integer, Listeners>();

      /** Construct a joystick object which starts an independent
       * joystick monitoring thread.
       */

      public JoystickManager()
      {
         // activate joystick reader thread

         new Thread()
         {
               public void run()
               {
                  readJoystickEvents();
               }
         }.start();

         // blindly wait a bit for joystick information to flow in

         try
         {
            Thread.sleep(500);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /** Register a Listener which handels joystick events from
       * all joysticks.
       *
       * @param listener the joystick listener which will recieve joystick events
       */
 
      public void registerListener(Listener listener)
      {
         registerListener(ALL_STICKS, listener);
      }

      /** Register a Listener which handles joystick events from
       * a particular joystick.
       *
       * @param listener the joystick listener which will recieve joystick events
       * @param stick the joystick for witch this listener should get events
       */
 
      public void registerListener(int stick, Listener listener)
      {
         Listeners jls = filteredListeners.get(stick);
         if (jls == null)
         {
            jls = new Listeners();
            filteredListeners.put(stick, jls);
         }
         jls.add(listener);
      }

      /** Read joystick events from USB ports. */

      public void readJoystickEvents()
      {
         try
         {
            File tmpMultijoy = File.createTempFile("multijoy.", ".py");
            tmpMultijoy.deleteOnExit();
            JarTools.copyResource("resources/multijoy.py", tmpMultijoy);

            // create python process to read in joystick data, this
            // implemention requires PyGame to be install on the system
            // to work properly

            final Process p = Runtime.getRuntime()
               .exec("python " + tmpMultijoy.toString());

            // create a line number reader so i can read lines from this
            // stream

            LineNumberReader lnr = new LineNumberReader(
               new InputStreamReader(p.getInputStream()));
            
            // loop forever collecting events from the python process
            
            while (true)
               parseJoystickEvent(lnr.readLine());
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /** Parse a single joystick event and dispatch it listeners. */

      public void parseJoystickEvent(String eventStr)
      {
         int stick = -1;

         // if the event string is empty or null, there is nothing to do

         if (eventStr == null || eventStr.length() == 0)
            return;

         // tokenize the string

         String[] tokens = eventStr.split(" ");
         
         // work through the tokens

         for (int i = 0; i < tokens.length; ++i)
         {
            String token = tokens[i];

            // handle "stickinfo" token
            
            if (token.equals("stickinfo:"))
            {
               int number = Integer.valueOf(tokens[++i]);
               int buttons = Integer.valueOf(tokens[i += 2]);
               int axes = Integer.valueOf(tokens[i += 2]);
               int hats = Integer.valueOf(tokens[i += 2]);
               String name = ""; ++i;
               while (++i < tokens.length)
                  name += tokens[i] + (i < tokens.length - 1 ? " " : "");
               add(new StickInfo(number, buttons, axes, hats, name));
            }

            // handle "stick" token
            
            if (token.equals("stick:"))
               stick = Integer.valueOf(tokens[++i]);

            // handle "axis" token
            
            else if (token.equals("axis:"))
            {
               int axis = Integer.valueOf(tokens[++i]);
               double value = Double.valueOf(tokens[i += 2]);

               // loop through stick array and lookup listeners for each
               // specific stick, and dispatch events to those
               // listeners. the stick array contains the value
               // ALL_STICKS for listeners which wish to receive events
               // for all joysticks
               
               stickArray[0] = stick;
               for (int s: stickArray)
               {
                  Listeners jls = filteredListeners.get(s);
                  if (jls != null)
                     for (Listener jl: jls)
                        jl.joystickAxisChanged(stick, axis, value);
               }
            }

            // handle "hat" token

            else if (token.equals("hat:"))
            {
               int hat = Integer.parseInt(tokens[++i]);
               int x = Integer.valueOf(tokens[i += 2]);
               int y = Integer.valueOf(tokens[i += 2]);

               // loop through stick array and lookup listeners for each
               // specific stick, and dispatch events to those
               // listeners. the stick array contains the value
               // ALL_STICKS for listeners which wish to receive events
               // for all joysticks
               
               stickArray[0] = stick;
               for (int s: stickArray)
               {
                  Listeners jls = filteredListeners.get(s);
                  if (jls != null)
                     for (Listener jl: jls)
                        jl.joystickHatChanged(stick, hat, x, y);
               }
            }

            // handle "button" token

            else if (token.equals("button:"))
            {
                int button = Integer.parseInt(tokens[++i]);
                boolean isPress = Integer.valueOf(tokens[i += 2]) == 1;

               // loop through stick array and lookup listeners for each
               // specific stick, and dispatch events to those
               // listeners. the stick array contains the value
               // ALL_STICKS for listeners which wish to receive events
               // for all joysticks
               
               stickArray[0] = stick;
               for (int s: stickArray)
               {
                  Listeners jls = filteredListeners.get(s);
                  if (jls != null)
                     for (Listener jl: jls)
                        if (isPress)
                           jl.joystickButtonPressed(stick, button);
                        else
                           jl.joystickButtonReleased(stick, button);
               }
            }
         }
      }

      /** Joystick information storage class. */

      public static class StickInfo
      {
            private int number;
            private int buttons;
            private int axes;
            private int hats;
            private String name;
            
            /** Construct a StickInfo object.
             *
             * @param number joystick number
             * @param buttons number of buttons on the joystick
             * @param axes number of analog axes on the stick
             * @param hats number of hats on the sick
             * @param name the manufactures name for the joystick
             */

            public StickInfo(int number, int buttons, int axes, int hats, 
                             String name)
            {
               this.number = number;
               this.buttons = buttons;
               this.axes = axes;
               this.hats = hats;
               this.name = name;
            }
            
            /** Get this joystick's number, which is just a unique
             * identifer for the joystick.
             * 
             * @return this joystick's number
             */
            public int getNumber()
            {
               return number;
            }
            
            /** Get this joystick's number of buttons.
             * 
             * @return this joystick's buttons count
             */

            public int getButtons()
            {
               return buttons;
            }
            
            /** Get this joystick's number of analog axes.
             * 
             * @return this joystick's axes count
             */

            public int getAxes()
            {
               return axes;
            }
            
            /** Get this joystick's number of hats.
             * 
             * @return this joystick's hat count
             */

            public int getHats()
            {
               return hats;
            }
            
            /** Get this joystick's name as provided by the manufacture.
             * 
             * @return this joystick's name
             */

            public String getName()
            {
               return name;
            }
            
            /** Convert this joystic info into a printable string. */

            public String toString()
            {
               return 
                  "joystick: " + number +
                  " buttons: " + buttons +
                  " axes: " + axes +
                  " hats: " + hats +
                  " name: \"" + name + "\"";
            }
      }

      /** Main for testing joystick code. */

      public static void main(String[] args)
      {
         JoystickManager jm = new JoystickManager();
         Listener jl = new Listener()
            {
                  /** Distpatch joystick axis event.
                   *
                   * @param stick joystick on which this event occured
                   * @param axis number of axis which has changed
                   * @param value value axis has changed to
                   */
                  
                  public void joystickAxisChanged(int stick, int axis, double value)
                  {
                     log.debug(
                        "stick: " + stick + " axis: " + axis + " value: " + value);
                  }
                  
                  /** Distpatch joystick hat event.
                   *
                   * @param orb orb associated with this stick
                   * @param hat number of hat which has changed
                   * @param x   X value of hat postion
                   * @param x   Y value of hat postion
                   */
                  
                  public void joystickHatChanged(int stick, int hat, int x, int y)
                  {
                     log.debug(
                        "stick: " + stick + " hat: " + hat + " x: " + x + " y: " + y);
                  }
                  
                  /** Distpatch joystick button press event.
                   *
                   * @param stick joystick on which this event occured
                   * @param button number of button which has changed
                   */
                  
                  public void joystickButtonPressed(int stick, int button)
                  {
                     log.debug(
                        "stick: " + stick + " button: " + button + " press: 1");
                  }
                  
                  /** Distpatch joystick button release event.
                   *
                   * @param stick joystick on which this event occured
                   * @param button number of button which has changed
                   */
                  
                  public void joystickButtonReleased(int stick, int button)
                  {
                     log.debug(
                        "stick: " + stick + " button: " + button + " press: 0");
                  }
                  
            };
         
         jm.registerListener(1, jl);
         jm.registerListener(jl);
      }

      /** The interface of a listener of joystick events. */
      
      public static interface Listener
      {
            /** Distpatch joystick axis event.
             *
             * @param stick joystick on which this event occured
             * @param axis number of axis which has changed
             * @param value value axis has changed to
             */
            
            public void joystickAxisChanged(int stick, int axis, double value);
            
            /** Distpatch joystick hat event.
             *
             * @param orb orb assocated with this stick
             * @param hat number of hat which has changed
             * @param x   X value of hat postion
             * @param x   Y value of hat postion
             */
            
            public void joystickHatChanged(int stick, int hat, int x, int y);
            
            /** Distpatch joystick button press event.
             *
             * @param stick joystick on which this event occured
             * @param button number of button which has changed
             */
            
            public void joystickButtonPressed(int stick, int button);
            
            
            /** Distpatch joystick button release event.
             *
             * @param stick joystick on which this event occured
             * @param button number of button which has changed
             */
            
            public void joystickButtonReleased(int stick, int button);
      }

      /** Joystick event adapter. */
      
      public static class Adapter implements Listener
      {
            /** Distpatch joystick axis event.
             *
             * @param stick joystick on which this event occured
             * @param axis number of axis which has changed
             * @param value value axis has changed to
             */
            
            public void joystickAxisChanged(int stick, int axis, double value)
            {
            }
            
            /** Distpatch joystick hat event.
             *
             * @param orb orb assocated with this stick
             * @param hat number of hat which has changed
             * @param x   X value of hat postion
             * @param x   Y value of hat postion
             */
            
            public void joystickHatChanged(int stick, int hat, int x, int y)
            {
            }
            
            /** Distpatch joystick button press event.
             *
             * @param stick joystick on which this event occured
             * @param button number of button which has changed
             */
            
            public void joystickButtonPressed(int stick, int button)
            {
            }
            
            /** Distpatch joystick button release event.
             *
             * @param stick joystick on which this event occured
             * @param button number of button which has changed
             */
            
            public void joystickButtonReleased(int stick, int button)
            {
            }
      }
}
