
package com.orbswarm.swarmcon;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.event.KeyEvent;


import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import java.net.URL;

import java.util.Vector;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import java.text.*;

import org.trebor.pid.*;
import org.trebor.util.JarTools;

import com.orbswarm.choreography.Specialist;
import com.orbswarm.choreography.OrbControl;

import com.orbswarm.choreography.timeline.Timeline;
import com.orbswarm.choreography.timeline.TimelineDisplay;

import com.orbswarm.swarmcomposer.color.*;
import com.orbswarm.swarmcomposer.composer.BotVisualizer;
import com.orbswarm.swarmcomposer.util.TokenReader;


import static org.trebor.util.ShapeTools.*;
import static java.lang.System.*;
import static java.awt.Color.*;
import static java.lang.Math.*;
import static javax.swing.KeyStroke.*;
import static java.awt.event.KeyEvent.*;

public class SwarmCon extends JFrame implements JoystickManager.Listener
{
      /* 
       * The following are hard coded constants.
       */

      /** Joystick access responsible for steering. */
      public static final int JOYSTICK_STEERING_AXIS = 0;
      public int power_range = 50;

      /** Joystick access responsible for driving forward and back. */
      public static final int JOYSTICK_POWER_AXIS = 1;

      /** Joystick button mapped to enter key commands */
      public static final int JOYSTICK_BUTTON_ENTER_KEY = 6;

      /** Joystick button mapped to the space key */
      public static final int JOYSTICK_BUTTON_SPACE_KEY = 4;

      /** Joystick button mapped to the space key */
      public static final int FOCUS_JOYSTICK_PANEL_BUTTON = 11;

      /** location of default properties file. */
      public static final String DEFAULT_PROPERTIES_FILE = 
         "resources/swarmcon.properties";

      /** size of joystick buttton icon */
      public static final int JOY_BUTTON_ICON_SIZE = 30;

      /** size of joystick buttton font */
      public static final float JOY_BUTTON_FONT_SIZE = 19;

      /** size of joystick buttton font */
      public static final float LABEL_FONT_SIZE = 18;

      /** user modifiable properties file */
      public static final String PROPERTIES_FILE_LOCATION =
         System.getProperty("user.home") + 
         System.getProperty("file.separator") + ".swarmcon.properties";

      /** period between commands in milliseconds */
      public static final int MOTOR_CMD_MILLISECS = 100;

      /** minimum frame delay in milliseconds */
      public static final long MIN_FRAME_DELAY = 10;

      /** physical radius of the orb */
      public static final double ORB_RADIUS        =   0.5; // meters

      /** physical diamter of orb */
      public static final double ORB_DIAMETER      = 2 * ORB_RADIUS; // meters

      /** maximum roll (left or right) */
      public static final double MAX_ROLL          =  35.0; // deg

      /** maximum rate of roll */
      public static final double MAX_ROLL_RATE     =  50.0; // deg/sec

      /** maximum change in roll rate */
      public static final double DROLL_RATE_DT     =  20.0; // deg/sec

      /** maximum rate of pitch */
      public static final double MAX_PITCH_RATE    = 114.6; // deg/sec

      /** maximum change in pitch range */
      public static final double DPITCH_RATE_DT    =  40.0; // deg/sec

      /** safe distance from other object */
      public static final double SAFE_DISTANCE     =   3.0; // meters

      /** the "way too close do someting about it" distance */
      public static final double CRITICAL_DISTANCE =   2.0; // meters

      /** number of spars graphically printed on the orb */
      public static final int    ORB_SPAR_COUNT    =   4  ; // arcs

      /** time in seconds for a phantom to move to it's target postion */
      public static final double PHANTOM_PERIOD    =  2  ;

      /** scale for graphics */
      public static final double PIXELS_PER_METER  = 30.0;

      /* 
       * The following are global objects.
       */

      /** Robot used to control the GUI */
      private Robot robot = null;

      /** Current active SwarmCon object. */
      private static SwarmCon activeSwarmCon = null;
      
      /** Properties for tweaking the system. */
      private Properties properties = null;

      /** The global source of randomness. */
      public static final Random RND = new Random();

      /** The joystick manager object */
      private JoystickManager joystickManager = null;


      /* 
       * The following are values specified in the properties file.  The
       * default values here will be overwritten by the values in the
       * properties file or command line values, if either exist.
       */

      /** stepped color fades (true == do simulation; false = rely on
       * light board to do the fades) */
      public boolean steppedColorFades = false;
    
      /** send multiple sound commands */
      public String serialPortId = "";

      /** send multiple sound commands */
      public boolean multipleSoundCommands = true;

      /** send multiple motion commands */
      public boolean multipleMotionCommands = true;

      /** the number of orbs */
      public int orbCount = 6;

      /** allowable range of values for power */
      public int powerRange = 50;

      /** allowable range of values for steering */
      public int steeringRange = 100;

      /** delay between sending steering commands */
      public int steeringRefreshDelay = 200;

      /** enable sending commands to orbs */
      private boolean sendCommandsToOrbs = true;

      /** true if joysticks are allowed to control the gui */
      private boolean joystickGuiControl = false;

      /** enable colors in simulation */
      private boolean simulateColors = true;

      /** enable sounds in simulation */
      private boolean simulateSounds = false;

      /** width of timeline on screen */
      int timelineWidth = 900;

      /** height of timeline on screen */
      int timelineHeight = 150;

      /** Joystick or Orb mapping table. */
      static HashMap<Integer, Integer> joystickToOrbMapping = 
         new HashMap<Integer, Integer>();


      class MotorCommandInfo
      {
            /** steering position */

            int commandedSteering = 0;

            /** old steering position */

            int oldCommandedSteering = 0;

            /** power position */

            int commandedPower = 0;

            /** old power position */

            int oldCommandedPower = 0;

            /** enabled state of this orb */

            boolean enabled = false;
      }

      MotorCommandInfo motorCommandInfo[] = null;

      /** arena in which we play */

      final JPanel arena = new JPanel()
         {
               public void paint(Graphics graphics)
               {
                  paintArena(graphics);
               }
         };

      /** timeline display */
      TimelineDisplay timelineDisplay;
      Timeline timeline = null;
      long timelineStarted = -1;


      /** card layout for main view area */

      CardLayout cardLayout;

      /** center panel which is the main view area */

      JPanel centerPanel;

      /** action panel which contains the arena and the controlUI */
      JPanel actionPanel;

      /** tabbed panel for control UI panes. */
      JTabbedPane controlTabs;

      /** pid tuner object */

      public PidTuner tuner;

      /** communcation with orbs */

      OrbIo orbIo;

      /** communcation with gps */

      GpsIo gpsIo;

      /** Implementation of
       * com.orbswarm.choreography.OrbControl.OrbControl to control the
       * Orbs from the Specialists. */

      OrbControlImpl orbControlImpl;
      // TODO: generalize the orbControl facility to use real/fake at
      // appropriate times.
      public OrbControl getOrbControl()
      {
         return orbControlImpl;
      }

      // HACQUE! get the specifically-typed version so we can break our
      // nicely-wrought loose coupling.
      public OrbControlImpl getOrbControlImpl()
      {
         return orbControlImpl;
      }

      public OrbIo getOrbIo()
      {
         return this.orbIo;
      }

      /**  Specialists listening to OrbState messages */
      ArrayList specialists = new ArrayList();

      // color

      public static Color BACKGROUND    = WHITE;
      public static Color BUTTON_CLR    = new Color(  0,   0,   0, 164);
      public static Color TEXT_CLR      = new Color(  0,   0,   0, 128);
      public static Color MENU_CLR      = new Color(  0,   0,   0, 128);
      public static Color ORB_CLR       = new Color(196, 196, 196);
      public static Color ORB_FRAME_CLR = new Color( 64,  64,  64);
      public static Color SEL_ORB_CLR   = new Color(255, 196, 255);
      public static Color VECTOR_CRL    = new Color(255,   0,   0, 128);
      public static Font  MISC_FONT     = new Font("Helvetica",
                                                   Font.PLAIN, 15);
      public static Font  ORB_FONT      =  new Font("Helvetica",
                                                    Font.PLAIN, 10);
      public static Font  MENU_FONT      =  new Font("Helvetica",
                                                     Font.PLAIN, 15);
      /** Standard button font */

      public static Font BUTTON_FONT = new Font("Lucida Grande", Font.PLAIN, 40);

      static
      {
         ORB_FONT = scaleFont(ORB_FONT);
      }

      /** Scale a font which are to be used in arena units
       *
       * @param original font to be scale
       * @return a scaled copy of the font
       */

      public static Font scaleFont(Font original)
      {
         return original.deriveFont(
            (float)(original.getSize()
                    / PIXELS_PER_METER));
      }

      // fix font sizes

      /** swarm of mobjects (not just orbs) (maybe this should not be
       * called the swarm?) */

      Swarm swarm;

      public Swarm getSwarm()
      {
         return swarm;
      }

      /** selected objects */

      Mobjects selected = new Mobjects();

      /** phantom objects */

      Vector<Phantom> phantoms = new Vector<Phantom>();

      /** last time mobects were updated */

      Calendar lastUpdate = Calendar.getInstance();

      /** format for printing heading values */

      public static NumberFormat HeadingFormat = NumberFormat.getNumberInstance();
      public static NumberFormat StdFormat = NumberFormat.getNumberInstance();


      /** static initializations */

      static
      {
         HeadingFormat.setMaximumIntegerDigits(3);
         HeadingFormat.setMinimumIntegerDigits(3);
         HeadingFormat.setMaximumFractionDigits(0);
         StdFormat.setMinimumIntegerDigits(1);
         StdFormat.setMaximumFractionDigits(2);
         StdFormat.setMinimumFractionDigits(2);
      }
      // entry point

      //
      // Some booleans that control whether this is a simulation, sends commands to the
      // actual orbs, or what.
      // Default: sendCommands to the orbs, simulate the colors, but not the sounds.
      // Toggleable with args to the main.
      //

      public static void main(String[] args)
      {
         registerSpecialists();
         SwarmCon sc = new SwarmCon();
         int i=0;
         while (i < args.length)
         {
            if (args[i].equalsIgnoreCase("--simulateSounds"))
            {
               i++;
               boolean simulateSounds = args[i].equalsIgnoreCase("true");
               sc.simulateSounds = simulateSounds;
            }

            else if (args[i].equalsIgnoreCase("--simulateColors"))
            {
               i++;
               boolean simulateColors = args[i].equalsIgnoreCase("true");
               sc.simulateColors = simulateColors;
            }


            else if (args[i].equalsIgnoreCase("--power") || args[i].equalsIgnoreCase("--powerRange"))
            {
               i++;
               int da_powah = Integer.parseInt(args[i]);
               i++;
               sc.setPowerRange(da_powah);
            }

            else if (args[i].equalsIgnoreCase("--steering") || args[i].equalsIgnoreCase("--steerinRange"))
            {
               i++;
               int da_steer = Integer.parseInt(args[i]);
               i++;
               sc.setSteeringRange(da_steer);
            }

            else if (args[i].equalsIgnoreCase("--timelineWidth"))
            {
               i++;
               int w = Integer.parseInt(args[i]);
               i++;
               sc.timelineWidth = w;
            }

            else if (args[i].equalsIgnoreCase("--timelineHeight"))
            {
               i++;
               int w = Integer.parseInt(args[i]);
               i++;
               sc.timelineHeight = w;
            }

            else if (args[i].equalsIgnoreCase("--steeringrefresh"))
            {
               i++;
               int refresh = Integer.parseInt(args[i]);
               i++;
               sc.steeringRefreshDelay = refresh;
            }
            // Note: not giving the option to turn off sending commands to orbs right now.

            else if (args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-help"))
            {
               usage();
               System.exit(0);
            }
            i++;
         }

         sc.initialize();
      }

      public static void usage()
      {
         System.out.println("java com.orbswarm.swarmcon.SwarmCon [options]");
         System.out.println("  options: ");
         System.out.println("     --simulateSounds true|false   default: false");
         System.out.println("     --simulateColors true|false   default: true");
         System.out.println("     --steeringrefresh <ms>        default: 200");
         System.out.println("     --power <0-100>               default:  50");
         System.out.println("     --steering <0-100>            default: 100");
         System.out.println("     --timelineWidth <int>        default: 900");
         System.out.println("     --timelineHeight <int>       default: 150");
      }

      public ColorSchemer colorSchemer;
      public BotVisualizer botVisualizer;

      public void constructControlUI(SwarmCon sc)
      {
         ColorSchemer schemer = setupColorSchemer(sc);
         this.colorSchemer = schemer; // too close coupling here, but it's late in the game...
         BotVisualizer bv = setupBotVisualizer(sc);
         this.botVisualizer = bv;
         TimelineDisplay timelineDisplay = new TimelineDisplay(sc, timelineWidth, timelineHeight);
         sc.setTimelineDisplay(timelineDisplay);
         timelineDisplay.setSwarmCon(sc);
         sc.setupControlPanel(schemer, bv, timelineDisplay);
         //sc.startControlling();
      }


      // construct a swarm

      public SwarmCon()
      {
         try
         {
            activeSwarmCon = this;
            robot = new Robot();
            readProperties();
            setValuesFromProperties();
         }
         catch (IOException ex)
         {
            System.err.println(
               "SwarmCon() caught exception reading properties. Using defaults.");
            ex.printStackTrace();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      
      /** Splitting constructor from initializer, so that parameters can
       *  be set in the main routine before starting it up. (e.g. can't
       *  reset the timeline width after constructing the frame).
       */

      public void initialize()
      {
         // OrbControl for Specialists

         orbControlImpl = new OrbControlImpl(this,
                                             sendCommandsToOrbs,
                                             simulateColors,
                                             simulateSounds);

         // start joystick managner
         
         joystickManager = new JoystickManager();
         joystickManager.registerListener(this);
         
         // construct the frame
         
         constructFrame(getContentPane());

         // get the graphics device from the local graphic environment

         GraphicsDevice gv = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getScreenDevices()[0];

         // if full screen is supported setup frame accoringly

         if (false && gv.isFullScreenSupported())
         {
            setUndecorated(true);
            setVisible(true);
            pack();
            gv.setFullScreenWindow(this);
         }
         // otherwise just make a big frame

         else
         {
            pack();
            setExtendedState(MAXIMIZED_BOTH);
            setVisible(true);
            resizeTimeline();
         }
         cardLayout.first(centerPanel);

         // init Swarm

         Controller[] controllers = addOrbs(new Rectangle2D
             .Double(arena.getBounds().getX() / PIXELS_PER_METER,
                     arena.getBounds().getY() / PIXELS_PER_METER,
                     arena.getBounds().getWidth()  / PIXELS_PER_METER,
                     arena.getBounds().getHeight() / PIXELS_PER_METER));

         // start motor control thread

         activateMotorControllThread();
         requestFocus();
      }
      /** Reads properties file out of users home directory.  If this
       * file does not exist a default one will be created. */

      private void readProperties() throws IOException
      {
         try
         {
            // if the properties file does not exist, copy a fresh one out
            // of the jar

            File propFile = new File(PROPERTIES_FILE_LOCATION);
            if (!propFile.exists())
               JarTools.copyResource(DEFAULT_PROPERTIES_FILE, propFile);
            
            // identify where the properties file lives

            System.out.println("Props file location: " + PROPERTIES_FILE_LOCATION);

            // read in the properties

            properties = new Properties();
            properties.load(new FileInputStream(propFile));

            // debug print properties

            System.out.println(
               "-------------------- SwarmCon Properties --------------------");
            properties.list(System.out);
            System.out.println(
               "-------------------------------------------------------------");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /** all defaults need to be specified in the properties file, and
       * here, in case the properties file isn't found for some reason. */

      private void setValuesFromProperties()
      {
         orbCount = getIntProperty(
            "swarmcon.orb.count", this.orbCount);
         powerRange = getIntProperty(
               "swarmcon.motion.powerRange", powerRange);
         steeringRange = getIntProperty(
               "swarmcon.motion.steeringRange", steeringRange);
         steeringRefreshDelay = getIntProperty(
               "swarmcon.motion.steeringRefreshDelay", steeringRefreshDelay);
         sendCommandsToOrbs = getBooleanProperty(
            "swarmcon.comm.sendCommandsToOrbs", sendCommandsToOrbs);
         joystickGuiControl = getBooleanProperty(
            "swarmcon.joystickGuiControl", joystickGuiControl);
         simulateColors = getBooleanProperty(
            "swarmcon.color.simulateColors", simulateColors);
         simulateSounds = getBooleanProperty(
            "swarmcon.sound.simulateSounds", simulateSounds);
         timelineWidth = getIntProperty(
            "swarmcon.timeline.width", timelineWidth);
         timelineHeight = getIntProperty(
            "swarmcon.timeline.height", timelineHeight);
         steppedColorFades = getBooleanProperty(
            "swarmcon.color.steppedColorFades", false);
         multipleMotionCommands  = getBooleanProperty(
            "swarmcon.comm.multipleMotionCommands", false);
         multipleSoundCommands  = getBooleanProperty(
            "swarmcon.comm.multipleSoundCommands", false);
         serialPortId  = getProperty(
            "swarmcon.comm.serialPort", serialPortId);

         // init the motorCommandInfo now that we know how many orbs we have

         motorCommandInfo = new MotorCommandInfo[orbCount];

         // read orb specific data

         for (int orbId = 0; orbId < orbCount; ++orbId)
         {

            // get stick to orb mapping

            joystickToOrbMapping.put(
               getIntProperty("swarmcon.orb" + orbId + ".joystick", orbId),
               orbId);

            // populate the motor command info array and set enabled state for each orb
            
            motorCommandInfo[orbId] = new MotorCommandInfo();
            motorCommandInfo[orbId].enabled = getBooleanProperty(
               "swarmcon.orb" + orbId + ".enabled", motorCommandInfo[orbId].enabled);
         }
      }

      public String getProperty(String key, String defaultVal)
      {
         return this.properties.getProperty(key, defaultVal);
      }

      public int getIntProperty(String key, int defaultVal)
      {
         int val = defaultVal;
         try
         {
            String vstr = this.properties.getProperty(key, null);
            val = Integer.parseInt(vstr);
         }
         catch (Exception ex)
         {
            // pass
         }
         return val;
      }

      public boolean getBooleanProperty(String key, boolean defaultVal)
      {
         boolean val = defaultVal;
         try
         {
            String vstr = this.properties.getProperty(key, null);
            val = (new Boolean(vstr)).booleanValue();
         }
         catch (Exception ex)
         {
            // pass
         }
         return val;
      }

      public float getFloatProperty(String key, float defaultVal)
      {
         float val = defaultVal;
         try
         {
            String vstr = this.properties.getProperty(key, null);
            val = Float.parseFloat(vstr);
         }
         catch (Exception ex)
         {
            // pass
         }
         return val;
      }

      public double getDoubleProperty(String key, double defaultVal)
      {
         double val = defaultVal;
         try
         {
            String vstr = this.properties.getProperty(key, null);
            val = Double.parseDouble(vstr);
         }
         catch (Exception ex)
         {
            // pass
         }
         return val;
      }
      public void setPowerRange(int val)
      {
         this.powerRange = val;
      }

      public void setSteeringRange(int val)
      {
         this.steeringRange = val;
      }

      boolean running;
      public void startControlling()
      {
         System.out.println("SWARM CON: start controlling");
         running = true;
         new Thread()
         {
               public void run()
               {

                  try
                  {
                     sleep(1000);
                     repaint();
                  }
                  catch (Exception e)
                  {
                     e.printStackTrace();
                  }
                  lastUpdate = Calendar.getInstance();
                  while (running)
                  {
                     update();
                  }
               }
         }
            .start();
      }

      public void stopControlling()
      {
         running = false;
      }

      public Controller[] addOrbs(Rectangle2D.Double bounds)
      {
         swarm = new Swarm(bounds);

         Mobject preveouse = new MouseMobject(arena);
         swarm.add(preveouse);
         Controller[] controllers = null;

         // construct the swarm

         for (int i = 0; i < orbCount; ++i)
         {
            // create an orb

            Orb orb = new Orb(swarm, new SimModel(), i);
            controllers = ((SimModel)orb.getModel()).getControllers();

            // add behvaiors

            swarm.add(orb);
            JoyBehavior jb = new JoyBehavior();
            joystickManager.registerListener(orbIdtoJoystick(i), jb);
            Behavior wb = new WanderBehavior();
            Behavior fb = new FollowBehavior(preveouse);
            Behavior rb = new RandomBehavior();
            Behavior cb = new ClusterBehavior();
            Behavior fab = new AvoidBehavior(fb);
            Behavior cab = new AvoidBehavior(cb);
            orb.add(jb);
            orb.add(fb);
            orb.add(rb);
            orb.add(cb);
            orb.add(fab);
            orb.add(cab);


            // register the joystick behavoir as a reciever of joystick events

            preveouse = orb;
         }
         swarm.nextBehavior();

         return controllers;
      }
      // update the world
      
      public void update()
      {
         synchronized (swarm)
         {
            // sleep until it's been a minimum frame delay

            try
            {
               long start = lastUpdate.getTimeInMillis();
               while (currentTimeMillis() - start < MIN_FRAME_DELAY)
                  Thread.sleep(10);
            }
            catch (Exception ex)
            {
               System.out.println(ex);
            }
            // get now

            Calendar now = Calendar.getInstance();
            long nowMillis = now.getTimeInMillis();
            float timeSinceTimelineStarted = (nowMillis - timelineStarted) / 1000.f;

            double time = (now.getTimeInMillis()
                           - lastUpdate.getTimeInMillis()) / 1000d;

            // establish the time since last update

            lastUpdate = now;

            // give the timeline a cycle
            timelineDisplay.cycle(timeSinceTimelineStarted);

            // update all the objects

            swarm.update(time);

            swarm.updateOrbDistances();
            broadcastOrbState();

            // repaint the screen

            arena.repaint();
         }
      }

      public void repaint()
      {
         arena.repaint();
      }

      // add a Specialist to list of OrbState receivers
      public void addSpecialist(Specialist sp)
      {
         System.out.println("SWARMCON: adding specialist... " + sp);
         specialists.add(sp);
      }

      public void removeSpecialist(Specialist sp)
      {
         System.out.println("SWARMCON: removing specialist... " + sp);
         specialists.remove(sp);
      }

      // broadcast OrbState messages to all the Specialists\
      public void broadcastOrbState()
      {
         synchronized (specialists)
         {
            for (Iterator it = specialists.iterator(); it.hasNext(); )
            {
               Specialist specialist = (Specialist)it.next();
               specialist.orbState(swarm);
            }
         }
      }

      public JLabel createBigLabel(String text, Color color)
      {
         JLabel label = new JLabel(text);
         label.setForeground(color);
         label.setAlignmentX(Component.LEFT_ALIGNMENT);
         label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
         return label;
      }

      public JPanel createJoystickPanel(JoystickManager jm)
      {
         // create the joystick panel

         final JPanel jp = new JPanel();
         jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

         // work through the joystick info

         for (JoystickManager.StickInfo si: jm)
         {
            // create a panel for this joystick
            
            final JPanel stickPanel = new JPanel();
            stickPanel.setBackground(Color.WHITE);
            stickPanel.setBorder(BorderFactory.createTitledBorder(si.getName()));
            stickPanel.setLayout(new BoxLayout(stickPanel, BoxLayout.Y_AXIS));
            jp.add(stickPanel);

            // add stick title
            
            JLabel title = new JLabel("Stick: " + 
                                      si.getNumber() +
                                      "  Orb: " +
                                      joystickToOrbId(si.getNumber()));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            title.setFont(title.getFont().deriveFont(LABEL_FONT_SIZE));
            stickPanel.add(title);

            // add axes panel
            
            JPanel axesPanel = new JPanel();
            axesPanel.setBackground(stickPanel.getBackground());
            axesPanel.setLayout(new BoxLayout(axesPanel, BoxLayout.X_AXIS));
            axesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // add axes title

            JLabel axesTitle = new JLabel("Axes: ");
            axesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            axesTitle.setFont(axesTitle.getFont().deriveFont(LABEL_FONT_SIZE));
            axesPanel.add(axesTitle); 
            stickPanel.add(axesPanel);

            // add axes sliders

            final JSlider[] axes = new JSlider[si.getAxes()];
            for (int i = 0; i < axes.length; ++i)
            {
               axes[i] = new JSlider(JSlider.HORIZONTAL);
               Dimension dim = axes[i].getMaximumSize();
               dim.width = 50;
               axes[i].setPreferredSize(dim);
               axesPanel.add(axes[i]);
            }

            // make a label for each hat

//             final JLabel[] hats = new JLabel[si.getHats()];
//             for (int i = 0; i < hats.length; ++i)
//                stickPanel.add(hats[i] = new JLabel("hat[" + i + "]: -"));

            // make a label for each button

            final JButton[] buttons = new JButton[si.getButtons()];
            JPanel buttonPanel = null;
            for (int i = 0; i < buttons.length; ++i)
            {
               if (i % 12 == 0)
               {
                  buttonPanel = new JPanel();
                  buttonPanel.setBackground(stickPanel.getBackground());
                  buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
                  buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                  stickPanel.add(buttonPanel);
               }
               buttonPanel.add(buttons[i] = new JButton());
               buttons[i].setFont(buttons[i].getFont().deriveFont(JOY_BUTTON_FONT_SIZE));
               setButtonIcons(buttons[i], i);
            }


            // create a listener to update this panel
            JoystickManager.Listener jl = new JoystickManager.Listener()
            {
                  public void joystickAxisChanged(int stick, int axis, double value)
                  {
                     axes[axis].setValue((int)((value + 1) * 50));
                     stickPanel.repaint();
                  }
                  
                  public void joystickHatChanged(int stick, int hat, int x, int y)
                  {
//                      hats[hat].setText("hat[" + hat + "]: x:" + x + " y: " + y);
//                      stickPanel.repaint();
                  }
                  public void joystickButtonPressed(int stick, int button)
                  {
                     // if pressed the "focus joystick panel" button, do that

                     if (button == FOCUS_JOYSTICK_PANEL_BUTTON)
                        controlTabs.setSelectedComponent(jp);

                     // update button state

                     buttons[button].setSelected(true);
                     stickPanel.repaint();
                  }
                  
                  public void joystickButtonReleased(int stick, int button)
                  {
                     buttons[button].setSelected(false);
                     stickPanel.repaint();
                  }
            };

            jm.registerListener(si.getNumber(), jl);
         }
         return jp;
      }

      public void setButtonIcons(JButton button, int number)
      {
         String title = number + "";
         Image unpressed = createTitledShape(
            JOY_BUTTON_ICON_SIZE, JOY_BUTTON_ICON_SIZE, CIRCLE,
            title, Color.LIGHT_GRAY, Color.GRAY, button.getFont());

         Image pressed = createTitledShape(
            JOY_BUTTON_ICON_SIZE, JOY_BUTTON_ICON_SIZE, CIRCLE,
            title, Color.RED, Color.BLACK, button.getFont());
         
         button.setIcon(new ImageIcon(unpressed));
         button.setPressedIcon(new ImageIcon(pressed));
         button.setSelectedIcon(new ImageIcon(pressed));
         button.setOpaque(false);
         button.setBorder(null);
      }

      public Image createTitledShape(int width, int height, Shape shape, String title, 
                                     Color diskColor, Color textColor)
      {
         return createTitledShape(width, height, shape, title, diskColor, textColor, null);
      }
      public Image createTitledShape(int width, int height, Shape shape, String title, 
                                     Color diskColor, Color textColor, Font font)
      {
         BufferedImage image = 
            new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         shape = normalize(shape);
         shape = scale(shape, width, height);
         shape = translate(shape, width / 2, height / 2);
         Graphics2D g = (Graphics2D)image.getGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
         g.setClip(0, 0, image.getWidth(), image.getHeight());

         g.setColor(diskColor);
         g.fill(shape);

            // identify the bounds of the text

         if (font != null)
            g.setFont(font);
         Rectangle2D tBounds = g.getFont().createGlyphVector(g.getFontRenderContext(),
                                                             title).getVisualBounds();

            // paint the text

         g.setColor(textColor);
         int x = (int)((width   - tBounds. getWidth()) / 2) - 1;
         int y = (int)((height - tBounds.getHeight()) / 2);
         g.drawString(title, x, y + (int)tBounds.getHeight());
         
         return image;
      }

      // place gui object into frame

      public void constructFrame(Container frame)
      {
         // frame closes on exit

         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

         // set frame to box layout

         frame.setLayout(new BorderLayout());

         // paint area mouse listener

         MouseInputAdapter mia = new SwarmMia();

         // create center Panel

         centerPanel = new JPanel();
         cardLayout = new CardLayout();
         centerPanel.setLayout(cardLayout);
         centerPanel.setBorder(BorderFactory.createLineBorder(Color.gray));


         // identify if we need a splash panel

         boolean splashNeeded = true;
         for (String portId :SerialIo.listSerialPorts())
            if (serialPortId.equals(portId))
               splashNeeded = false;

         if (splashNeeded)
         {
            // splash panel
            
            JPanel splash = new JPanel();
            splash.setLayout(new BoxLayout(splash, BoxLayout.Y_AXIS));
            splash.add(Box.createVerticalGlue());
            JButton button = new BigButton(simulation);
            splash.add(button);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setAlignmentY(Component.CENTER_ALIGNMENT);
            splash.add(Box.createVerticalGlue());
            for (String portId :SerialIo.listSerialPorts())
            {
               button = new BigButton(new SwarmComPortAction(portId));
               splash.add(button);
               button.setAlignmentX(Component.CENTER_ALIGNMENT);
               button.setAlignmentY(Component.CENTER_ALIGNMENT);
            }
            splash.add(Box.createVerticalGlue());
            centerPanel.add(splash, "splash");
         }
         else
         {
            (new SwarmComPortAction(serialPortId)).actionPerformed(null);
         }

         // intermediary panel to put the arena and control UIs side-by-side

         actionPanel = new JPanel();
         actionPanel.setLayout(new GridBagLayout());
         actionPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));

         // setup paint area

         arena.addMouseMotionListener(mia);
         arena.addMouseListener(mia);
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx   = 0;
         gbc.gridy   = 0;
         gbc.weightx = 1d;
         gbc.weighty = 1d;
         gbc.fill    = GridBagConstraints.BOTH;
         gbc.anchor  = GridBagConstraints.NORTHWEST;

         actionPanel.add(arena, gbc);

         constructControlUI(this);

         centerPanel.add(actionPanel, "arena");
         frame.add(centerPanel, BorderLayout.CENTER);

         // add actions

         InputMap inputMap = getRootPane().getInputMap();
         ActionMap actionMap = getRootPane().getActionMap();
         for (SwarmAction a: actions)
         {
            inputMap.put(a.getAccelerator(), a.getName());
            actionMap.put(a.getName(), a);
         }
         // add menu

         JMenuBar menuBar = new JMenuBar();
         menuBar.setBorder(null);
         add(menuBar, BorderLayout.NORTH);
         JMenu fileMenu = new JMenu("file");
         fileMenu.setForeground(MENU_CLR);
         fileMenu.setFont(MENU_FONT);
         menuBar.add(fileMenu);
         for (SwarmAction a: actions)
         {
            JMenuItem menu = new JMenuItem(a);
            menu.setFont(MENU_FONT);
            menu.setForeground(MENU_CLR);
            fileMenu.add(menu);
         }
      }

      /** Establish the pattern of phantoms on the screen. */

      public void configurePhantoms()
      {
         int count = phantoms.size();

         // compute 90 % of minimum dimension which is the maximum
         // size to take up

         double maxSize = min(
            swarm.getArena().getWidth(),
            swarm.getArena().getHeight()) * 0.9d;

         // find the center of the arena

         Point2D.Double center = swarm.getCenter();

         // if we've got 1 orb, size it real big

         if (count == 1)
         {
            Phantom p = phantoms.get(0);
            p.setTarget(center, maxSize / p.getSize());
         }
         else if (count > 1)
         {
            double size = phantoms.get(0).getSize();
            double scale = maxSize / ((3 * size) -
                                      (size / 4 * (6 - count)));
            double radius = scale * size;
            double dAngle = 2 * PI / phantoms.size();
            double angle = 0;
            for (Phantom p: phantoms)
            {
               p.setTarget(new Point2D.Double(
                              center.getX() + cos(angle) * radius,
                              center.getY() + sin(angle) * radius),
                           scale);
               angle += dAngle;
            }
         }
      }
      /** Paint all objcts in arena.
       *
       * @param graphics graphics object to paint onto
       */

      Calendar lastPaint = Calendar.getInstance();

      public void paintArena(Graphics graphics)
      {
         // config graphics

         int width = arena.getWidth();
         int height = arena.getHeight();
         Graphics2D g = (Graphics2D)graphics;

         g.setColor(BACKGROUND);
         g.fillRect(0, 0, width, height);
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

         g.setColor(TEXT_CLR);
         g.setFont(MISC_FONT);
         g.drawString("frame delay: " +
                      (currentTimeMillis() - lastPaint.getTimeInMillis())
                      + "ms", 5, 15);
         lastPaint = Calendar.getInstance();

         // draw current behavior

         if (swarm != null)
         {
            synchronized (swarm)
            {
               int id = 1;
               for (Mobject mobject: swarm)
                  if (mobject instanceof Orb)
                  {
                     Orb orb = (Orb)mobject;
                     Behavior behavior = orb.getBehavior();

                     g.setColor(TEXT_CLR);
                     g.setFont(MISC_FONT);
                     g.drawString(
                        orb.getId() + ": " +
                        (behavior != null
                         ? behavior.toString()
                         : "[none]") +
                        " R: " + HeadingFormat.
                        format(round(orb.getRoll ())) +
                        " P: " + HeadingFormat.
                        format(round(orb.getPitch())) +
                        " Y: " + HeadingFormat.
                        format(round(orb.getYaw  ())) +
                        " YR: " + HeadingFormat.
                        format(round(orb.getYawRate())) +
                        " V: " + round(orb.getSpeed() * 100) / 100d,
                        5, 15 + id++ * 15);
                  }
            }
         }
         // set 0,0 to lower left corner, and scale for meters

         g.translate(0, height);
         g.scale(PIXELS_PER_METER, -PIXELS_PER_METER);

         // draw mobjects

         if (swarm != null)
         {
            synchronized (swarm)
            {
               for (Mobject mobject: swarm)
                  mobject.paint(g);
            }
         }
      }

      // object which is always set to the position of the mouse

      public class MouseMobject extends Mobject
      {
            JPanel arenax = arena;

            // shape to be drawn

            Shape shape = new Ellipse2D.Double(
               -ORB_DIAMETER / 4, -ORB_DIAMETER / 4,
               ORB_DIAMETER / 2, ORB_DIAMETER / 2);

            // construct a MouseMobject

            public MouseMobject(Component arena)
            {
               super(ORB_DIAMETER / 2);
               MouseInputAdapter mia = new MouseInputAdapter()
                  {
                        public void mouseMoved(MouseEvent e)
                        {
                           setPosition(e.getX() / PIXELS_PER_METER,
                                       (arenax.getHeight() - e.getY()) /
                                       PIXELS_PER_METER);
                        }
                  };

               arena.addMouseListener(mia);
               arena.addMouseMotionListener(mia);
            }
            /** Is the given point (think mouse click point) eligable to
             * select this object?
             *
             * @param clickPoint the point where the mouse was clicked
             */

            public boolean isSelectedBy(Point2D.Double clickPoint)
            {
               return false;
            }
            // update positon of this object

            public void update(double time) {}

            // paint this object onto a graphics area

            public void paint(Graphics2D g)
            {
               g.setColor(RED);
               g.fill(translate(shape, getX(), getY()));
            }
      }
      // create arrow shape

      public static Shape createArrow()
      {
         GeneralPath gp = new GeneralPath();
         Shape square = new Rectangle2D.Double(-.5, -.5, 1, 1);
         gp.append(square, false);
         gp.append(translate(createRightTriangle(), 0, - .5), false);
         return normalize(gp);
      }
      // create right triangle

      public static Shape createRightTriangle()
      {
         Area rTriangle = new Area();
         rTriangle.add(new Area(rotate(new Rectangle2D.Double(-0.5, -0.5, 1, 1), 45)));
         rTriangle.subtract(new Area(new Rectangle2D.Double(-2, 0, 4, 2)));
         return rTriangle;
      }

      /** swarm mouse input adapter */

      class SwarmMia extends MouseInputAdapter
      {
            // mouse dragged event

            public void mouseDragged(MouseEvent e)
            {
            }
            // mouse moved event

            public void mouseMoved(MouseEvent e)
            {
            }
            // mouse clicked event

            public void mouseClicked(MouseEvent e)
            {
               // convert point to meters

               Point2D.Double point = new Point2D.Double(
                  e.getX() / PIXELS_PER_METER,
                  (arena.getHeight() - e.getY()) / PIXELS_PER_METER);

               // find nearest selectable mobject

               final Mobject nearest = swarm.findSelected(point);

               // if shift is not down, clear selected

               if (!e.isShiftDown())
               {
                  for (Mobject m: selected)
                     m.setSelected(false);
                  for (Phantom p: phantoms)
                     swarm.remove(p);

                  selected.clear();
                  phantoms.clear();
               }
               // if nearest found, ad to selected set

               if (nearest != null)
               {
                  // set selected

                  nearest.setSelected(true);

                  // add to selected mobjects

                  selected.add(nearest);

                  // add phatom for this mobject

                  Phantom p = new Phantom(
                     nearest, PHANTOM_PERIOD);
                  phantoms.add(p);
                  swarm.add(p);

                  // tell the phatoms to reconfigure themselfs

                  configurePhantoms();
               }
            }
      }
      /** SwarmCon action class */

      abstract class SwarmAction extends AbstractAction
      {
            // construct the action

            public SwarmAction(String name, KeyStroke key, String description)
            {
               super(name);
               putValue(NAME, name);
               putValue(SHORT_DESCRIPTION, description);
               putValue(ACCELERATOR_KEY, key);
            }
            /** Return accelerator key for this action.
             *
             * @return accelerator key for this action
             */

            public KeyStroke getAccelerator()
            {
               return (KeyStroke)getValue(ACCELERATOR_KEY);
            }
            /** Return name of this action.
             *
             * @return name of this action
             */

            public String getName()
            {
               return (String)getValue(NAME);
            }
      }

      /** Action class wich selects a given serial port with witch to
       * commucate to the orbs. */

      class SwarmComPortAction extends SwarmAction
      {
            /** communications port id */

            private String portId;

            /** Construct SwarmComPortAction with a given com port
             * id.
             *
             * @param portId a string representation of com port
             */

            public SwarmComPortAction(String portId)
            {
               super("connect via " + portId, null,
                     "connect to orbs via serial port " + portId);
               this.portId = portId;
            }
            public void actionPerformed(ActionEvent e)
            {
               try
               {
                  orbIo = new OrbIo(portId, true);
                  cardLayout.last(centerPanel);
                  orbControlImpl.setOrbIo(orbIo);
               }
               catch (Exception ex)
               {
                  ex.printStackTrace();
               }
            }
      }
      /** Action to select simulated rather live operation. */

      SwarmAction simulation = new SwarmAction(
         "simulate orbs",
         getKeyStroke(VK_S, 0),
         "simulate orb motion rather then connect to live orbs")
         {
               public void actionPerformed(ActionEvent e)
               {
                  cardLayout.last(centerPanel);
               }
         };

      /** Action to reset simulation state */

      SwarmAction reset = new SwarmAction(
         "reset sim",
         getKeyStroke(VK_R, 0),
         "reset simulation state")
         {
               public void actionPerformed(ActionEvent e)
               {
                  swarm.randomize();
               }
         };

      /** action to select next orb behavior */

      SwarmAction nextBehavior = new SwarmAction(
         "next behavior",
         getKeyStroke(VK_UP, 0),
         "select next orb behavior")
         {
               public void actionPerformed(ActionEvent e)
               {
                  swarm.nextBehavior();
               }
         };

      /** action to select previous orb behavior */

      SwarmAction previousBehavior = new SwarmAction(
         "previous behavior",
         getKeyStroke(VK_DOWN, 0),
         "select previous orb behavior")
         {
               public void actionPerformed(ActionEvent e)
               {
                  swarm.previousBehavior();
               }
         };
      /** action to exist the system */

      SwarmAction exit = new SwarmAction(
         "exit",
         getKeyStroke(VK_ESCAPE, 0),
         "exit this program")
         {
               public void actionPerformed(ActionEvent e)
               {
                  System.exit(0);
               }
         };

      /** all the actions in one handy place */

      SwarmAction[] actions =
      {
         reset,
         nextBehavior,
         previousBehavior,
         exit,
      };
      /** a convience class for a really big button */

      public class BigButton extends JButton
      {
            public BigButton(AbstractAction action)
            {
               super(action);
               setFont(BUTTON_FONT);
               setForeground(BUTTON_CLR);
            }
      }

      /** Register stock color schemes */

      public static void registerColorSchemes()
      {
         ColorScheme.registerColorScheme("Analogous", ColorSchemeAnalogous.class);
         ColorScheme.registerColorScheme("Split Complement", ColorSchemeSplitComplement.class);
         ColorScheme.registerColorScheme("Split Complement 3", ColorSchemeSplitComplement3.class);
         ColorScheme.registerColorScheme("Triad", ColorSchemeTriad.class);
         ColorScheme.registerColorScheme("Tetrad", ColorSchemeTetrad.class);
         ColorScheme.registerColorScheme("Crown", ColorSchemeCrown.class);
      }
      
      /** Setup the ColorSchemeSpecialist and it's controller interface */
      public static ColorSchemer setupColorSchemer(SwarmCon swarmCon)
      {
         registerColorSchemes();

         /* not doing the specialists this way anymore!
            ColorSchemeSpecialist colorSchemeSpecialist = new ColorSchemeSpecialist();
            colorSchemeSpecialist.setup(swarmCon.orbControlImpl, null, null);
              
            swarmCon.addSpecialist(colorSchemeSpecialist);
         */

         ColorSchemer schemer = new ColorSchemer("Crown");
         //schemer.addColorSchemeListener(colorSchemeSpecialist);
         schemer.broadcastNewColorScheme();
         schemer.broadcastColorSchemeChanged();

         // todo: need to set this stuff up inthe ColorSchemeSpecialist.
         //colorSchemeSpecialist.addBotColorListener(schemer);

         return schemer;
      }

      /** Setup the ColorSchemeSpecialist and it's controller interface */
      public static BotVisualizer setupBotVisualizer(SwarmCon swarmCon)
      {

         /* later...
            RandomSongSpecialist randomSongSpecialist = new RandomSongSpecialist();
            swarmCon.addSpecialist(randomSongSpecialist);
            randomSongSpecialist.setup(swarmCon.orbControlImpl, null, null);
         */

         int numbots = 6; // TODO: Whither Data?
         BotVisualizer bv = new BotVisualizer(numbots);
         return bv;
      }

      public  void setupControlPanel(ColorSchemer schemer,
                                     BotVisualizer bv,
                                     TimelineDisplay timelineDisplay)
      {
         controlTabs = new JTabbedPane();
         JPanel joystickInfoPanel = createJoystickPanel(joystickManager);
         controlTabs.addTab("Joysticks", joystickInfoPanel);
         
         JPanel motionControlUIPanel = createMotionControlUIPanel();
         controlTabs.addTab("Motion", motionControlUIPanel);
         
         JPanel colorControlUIPanel = createColorControlUIPanel(schemer);
         controlTabs.addTab("ColorScheme", colorControlUIPanel);
         
         JPanel soundControlUIPanel = createSoundControlUIPanel(bv);
         controlTabs.addTab("Sound", soundControlUIPanel);
         
         
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx   = 1;
         gbc.gridy   = 0;
         gbc.gridheight = 1;
         gbc.weightx = 0.;
         gbc.weighty = 0.;
         gbc.fill    = GridBagConstraints.VERTICAL;
         gbc.anchor  = GridBagConstraints.EAST;
         
         actionPanel.add(controlTabs, gbc);
         
         gbc = new GridBagConstraints();
         gbc.gridx      = 0;
         gbc.gridy      = 1;
         gbc.gridwidth  = 2;
         //gbc.gridheight = 1;
         gbc.weightx    = 0.;
         gbc.weighty    = 0.;
         gbc.fill       = GridBagConstraints.NONE;
         gbc.anchor     = GridBagConstraints.SOUTHEAST;
         actionPanel.add(timelineDisplay.getPanel(), gbc);
      }

    public void resizeTimeline() {
        JPanel p = timelineDisplay.getPanel();
    }
    
    private JPanel createMotionControlUIPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        //panel.setBackground(colorSchemer.bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        //gbc.weightx = 1.0;
        //gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(new JLabel("Power Range"), gbc);
        gbc.gridy = 1;
        JSlider powerSlider = makeRangeSlider(20, 80, powerRange);
        final JLabel powerSliderValue = new JLabel("" + powerRange);

        powerSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    //if (!source.getValueIsAdjusting())
                    powerRange = source.getValue();
                    powerSliderValue.setText("" + powerRange);
                }
            });
        panel.add(powerSlider, gbc);
        gbc.gridx = 1;
        panel.add(powerSliderValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Steering Range"), gbc);
        gbc.gridy = 3;
        //gbc.weightx = 1.0;
        JSlider steeringSlider = makeRangeSlider(50, 120, steeringRange);
        final JLabel steeringSliderValue = new JLabel("" + steeringRange);
        steeringSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    //if (!source.getValueIsAdjusting())
                    powerRange = source.getValue();
                    steeringSliderValue.setText("" + steeringRange);
                }
            });
        panel.add(steeringSlider, gbc);
        gbc.gridx = 1;
        panel.add(steeringSliderValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JCheckBox multipleMotionCmdsCheck = new JCheckBox("Multiple Motion Commands");
        multipleMotionCmdsCheck.setSelected(multipleMotionCommands);
        multipleMotionCmdsCheck.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    int state = itemEvent.getStateChange();
                    boolean sel = (state == ItemEvent.SELECTED);
                    setMultipleMotionCommands(sel);
                }
            });
        panel.add(multipleMotionCmdsCheck, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JCheckBox multipleSoundCmdsCheck = new JCheckBox("Multiple Sound Commands");
        multipleSoundCmdsCheck.setSelected(multipleSoundCommands);
        multipleSoundCmdsCheck.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    int state = itemEvent.getStateChange();
                    boolean sel = (state == ItemEvent.SELECTED);
                    setMultipleSoundCommands(sel);
                }
            });
        panel.add(multipleSoundCmdsCheck, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        JCheckBox steppedColorFadesCheck = new JCheckBox("Stepped Color Fades");
        steppedColorFadesCheck.setSelected(steppedColorFades);
        steppedColorFadesCheck.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    int state = itemEvent.getStateChange();
                    boolean sel = (state == ItemEvent.SELECTED);
                    steppedColorFades = sel;
                }
            });
        panel.add(steppedColorFadesCheck, gbc);

        return panel;
    }

    public JSlider makeRangeSlider(int low, int high, int val) {
        System.out.println("Make range slider(low: " + low + " hi: " + high + " val: " + val);
        JSlider slider = new JSlider(JSlider.HORIZONTAL, low, high, val);
        // size matters.
        Dimension size = slider.getSize();
        int sliderWidth = 150;
        int sliderHeight = 20;
        slider.setMinimumSize(new Dimension(sliderWidth, sliderHeight));
        slider.setPreferredSize(new Dimension(sliderWidth, sliderHeight));
        return slider;
    }

      private JPanel createColorControlUIPanel(ColorSchemer colorSchemer) {
         JPanel panel = new JPanel();
         panel.setLayout(new GridBagLayout());
         panel.setBackground(colorSchemer.bgColor);
         GridBagConstraints gbc = new GridBagConstraints();

         gbc.gridx = 0;
         gbc.gridy = 0;
         //gbc.weightx = 1.0;
         //gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel.add(colorSchemer.getPanel(), gbc);
         return panel;
      }

      private JPanel createSoundControlUIPanel(BotVisualizer bv) {
         JPanel panel = new JPanel();
         panel.setLayout(new GridBagLayout());
         panel.setBackground(colorSchemer.bgColor);
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0;
         if (bv != null)
         {
            panel.add(bv.getPanel(), gbc);
         }

         return panel;
      }

      public void setMultipleSoundCommands(boolean val) 
      {
         this.multipleSoundCommands = val;
      }
      
      public void setMultipleMotionCommands(boolean val) 
      {
         this.multipleMotionCommands = val;
      }
            
      ////////////////////////////////
      /// dispatch joystick events ///
      ////////////////////////////////

      /** Distpatch joystick axis event.
       *
       * @param stick joystick on which this event occured
       * @param axis number of axis which has changed
       * @param value value axis has changed to
       */

      public void joystickAxisChanged(int stick, int axis, double value)
      {
         int orbNum = joystickToOrbId(stick);

         switch (axis)
         {
            case JOYSTICK_STEERING_AXIS:
               motorCommandInfo[orbNum].commandedSteering = 
                  (int)(value * steeringRange);
               break;
            case JOYSTICK_POWER_AXIS:
               motorCommandInfo[orbNum].commandedPower = 
                  (int)(-1 * value * powerRange);
               break;
         }
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
         // if joysticks are allowed to control the gui

         if (joystickGuiControl)
         {
            // if hat right, tab to next gui object
            
            if (x == 1)
            {
               robot.keyPress(VK_TAB);
               robot.keyRelease(VK_TAB);
            }
            
            // if hat left, tab to prev gui object
            
            else if (x == -1)
            {
               robot.keyPress(VK_SHIFT);
               robot.keyPress(VK_TAB);
               robot.keyRelease(VK_TAB);
               robot.keyRelease(VK_SHIFT);
            }
            
            // if hat up, increase value of object
            
            else if (y == 1)
            {
               robot.keyPress(VK_UP);
               robot.keyRelease(VK_UP);
            }
            
            // if hat down, decrease value of object
            
            else if (y == -1)
            {
               robot.keyPress(VK_DOWN);
               robot.keyRelease(VK_DOWN);
            }
         }

         out.println("stick: " + stick + " hat: " + hat + " x: " + x + " y: " + y);
      }

      /** Distpatch joystick button press event.
       *
       * @param stick joystick on which this event occured
       * @param button number of button which has changed
       */

      public void joystickButtonPressed(int stick, int button)
      {
         // if joysticks are allowed to control the gui

         if (joystickGuiControl)
         {
            // handel space button
            
            if (button == JOYSTICK_BUTTON_SPACE_KEY)
               robot.keyPress(VK_SPACE);
            
            // handel enter button
            
            else if (button == JOYSTICK_BUTTON_ENTER_KEY)
               robot.keyPress(VK_ENTER);
         }
      }

      /** Distpatch joystick button release event.
       *
       * @param stick joystick on which this event occured
       * @param button number of button which has changed
       */

      public void joystickButtonReleased(int stick, int button)
      {
         // if joysticks are allowed to control the gui

         if (joystickGuiControl)
         {
            // handel space button
            
            if (button == JOYSTICK_BUTTON_SPACE_KEY)
               robot.keyRelease(VK_SPACE);
            
            // handel enter button
            
            else if (button == JOYSTICK_BUTTON_ENTER_KEY)
               robot.keyRelease(VK_ENTER);
         }
      }

      /** Activate the thread which sends motor commands to the orbs. */

      public void activateMotorControllThread()
      {
         new Thread()
         {
               public void run()
               {
                  while (true)
                  {
                     try
                     {
                        commandMotors();
                        sleep(MOTOR_CMD_MILLISECS);
                     }
                     catch (Exception e)
                     {
                        e.printStackTrace();
                     }
                  }
               }
         }.start();
      }

      /** Send the one round motor commands to the all the active orbs. */

      public void commandMotors()
      {
         if (orbIo != null)
         {
            for (int i = 0; i < motorCommandInfo.length; ++i)
            {
               MotorCommandInfo mci = motorCommandInfo[i];
               if (mci.enabled && 
                   (mci.commandedSteering != mci.oldCommandedSteering ||
                    multipleMotionCommands))
               {
                  orbIo.steerOrb(i, mci.commandedSteering);
                  mci.oldCommandedSteering = mci.commandedSteering;
               }

               if (mci.enabled &&
                   (mci.commandedPower != mci.oldCommandedPower ||
                    multipleMotionCommands))
               {
                  orbIo.powerOrb(i, mci.commandedPower);
                  mci.oldCommandedPower = mci.commandedPower;
               }
            }
         }
      }

      /** Lookup joystick to orb mapping. 
       *
       * @param joystick number of joystick which needs mapping to an orb
       */

      public static int joystickToOrbId(int joystickNum)
      {
         // lookup orb id

         Integer orbId = joystickToOrbMapping.get(joystickNum);

         // if no mapping exists, return the default 1:1 correspondence,
         // otherwise return looked up orb id

         return orbId == null ? joystickNum : orbId;
      }

      /** Lookup orb to joystick mapping. 
       *
       * @param orbId id of orb to match to a joystick number
       */

      public static int orbIdtoJoystick(int orbId)
      {
         // if the orb number appers in the map, return the associated stick
         for (Entry<Integer, Integer> entry: joystickToOrbMapping.entrySet())
            if (entry.getValue() == orbId)
               return entry.getKey();

         // if no mapping exists, return the default 1:1 correspondence
         return orbId;
      }


      ///////////////////////////////////
      /// Joystick handling           ///
      ///////////////////////////////////

      public void joystickXY(int orbNum, double x1, double y1,
                             double x2, double y2)
      {
         timelineDisplay.joystickXY(orbNum, x1, y1, x2, y2);
      }

      public void joystickButton(int orbNum, int buttonNumber)
      {
         timelineDisplay.joystickButton(orbNum, buttonNumber);
      }

      ///////////////////////////////////
      /// Timeline handling           ///
      ///////////////////////////////////

      public void setTimelineDisplay(TimelineDisplay val)
      {
         this.timelineDisplay = val;
      }

      public void setTimeline(String timelinePath)
      {

         if (!timelinePath.startsWith("/"))
         {
            String timelineDirectory = "resources/timelines";
            timelinePath = timelineDirectory + "/" + timelinePath;
         }
         try
         {
            Timeline timeline = Timeline.readTimeline(timelinePath);
            this.timeline = timeline;
            timelineDisplay.setTimeline(timeline);
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
         }
      }

      public void startTimeline()
      {
         this.timelineStarted = System.currentTimeMillis();
         System.out.println("Swarmcon: startTimeline!!!!!!111!1eleven!!");
         startControlling();
      }

      public void stopTimeline()
      {
         System.out.println("Swarmcon: STOP Timeline!!!!!!111!1eleven!!");
         stopControlling();
         timelineDisplay.stopAllRunningEvents();
      }

      public static void registerSpecialists()
      {
         String chpkg = "com.orbswarm.choreography";
         Timeline.registerSpecialist("SimpleColor",  chpkg + "." + "SingleColorSpecialist");
         Timeline.registerSpecialist("ColorScheme",  "com.orbswarm.swarmcomposer.color.ColorSchemeSpecialist");
         Timeline.registerSpecialist("ColorSchemer", "com.orbswarm.swarmcomposer.color.ColorSchemeSpecialist");
         Timeline.registerSpecialist("RandomSongPlayer", "com.orbswarm.swarmcomposer.composer.RandomSongSpecialist");
         Timeline.registerSpecialist("SimpleSound",  chpkg + "." + "SingleSoundSpecialist");
         Timeline.registerSpecialist("Multitrack",   chpkg + "." + "MultitrackSongSpecialist");
      }
}
