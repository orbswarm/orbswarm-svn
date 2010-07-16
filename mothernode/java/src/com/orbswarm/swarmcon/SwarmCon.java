package com.orbswarm.swarmcon;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.ActionMap;
import javax.swing.event.*;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import java.util.Vector;
import java.util.HashMap;
import java.util.Random;
import java.util.Calendar;
import java.util.Collections;

import java.text.NumberFormat;

import org.trebor.pid.PidTuner;
import org.trebor.util.JarTools;
import org.trebor.util.properties.IProperty;
import org.trebor.util.properties.PropertySet;
import org.trebor.util.properties.FlagProperty;
import org.trebor.util.properties.LongProperty;
import org.trebor.util.properties.StringProperty;
import org.trebor.util.properties.BooleanProperty;
import org.trebor.util.properties.IntegerProperty;

import com.orbswarm.swarmcon.IOrbControl;

import static org.trebor.util.ShapeTools.normalize;
import static org.trebor.util.ShapeTools.scale;
import static org.trebor.util.ShapeTools.translate;
import static org.trebor.util.ShapeTools.rotate;
import static org.trebor.util.Angle.Type.DEGREE_RATE;
import static org.trebor.util.Angle.Type.HEADING;
import static java.awt.Color.WHITE;
import static java.awt.Color.RED;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.System.currentTimeMillis;
import static javax.swing.KeyStroke.getKeyStroke;
import static java.awt.event.KeyEvent.SHIFT_MASK;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class SwarmCon extends JFrame
{
  private static final long serialVersionUID = 434565119921549730L;
  private static Logger log = Logger.getLogger(SwarmCon.class);

  /*
   * The following are hard coded constants.
   */

  /** Total maximum anticipated orb count. */
  public static final int MAX_ORB_COUNT = 6;

  /** The offset between internal orb IDs (0-5) and actual orb IDs (60
   * - 65) */
  public static final int ORB_OFFSET_ID = 60;

  /** path to resources directory */
  public static final String RESOURCES_PATH    = "resources";

  /** location of default properties file. */
  public static final String DEFAULT_PROPERTIES_FILE =
    RESOURCES_PATH + "/swarmcon.properties";

  /** default scale */
  public static final double DEFAULT_PIXELS_PER_METER = 30;

  /** size of label font */
  public static final float LABEL_FONT_SIZE = 18;

  /** user modifiable properties file */
  public static final String PROPERTIES_FILE_LOCATION =
    System.getProperty("user.home") +
    System.getProperty("file.separator") + ".swarmcon.properties";

  /** simulation key word */
  public static final String SIMULATION = "simulation";

  /** minimum frame delay in milliseconds */
  public static final long MIN_FRAME_DELAY     =  10;

  /** physical radius of the orb */
  public static final double ORB_RADIUS        =   0.760 / 2; // meters

  /** physical diameter of orb */
  public static final double ORB_DIAMETER      = 2 * ORB_RADIUS; // meters

  /** maximum roll (left or right) */
  public static final double MAX_ROLL          =  35.0; // degrees

  /** maximum rate of roll */
  public static final double MAX_ROLL_RATE     =  50.0; // degrees/second

  /** maximum change in roll rate */
  public static final double DROLL_RATE_DT     =  20.0; // degrees/second

  /** maximum rate of pitch */
  public static final double MAX_PITCH_RATE    = 114.6; // degrees/second

  /** maximum change in pitch range */
  public static final double DPITCH_RATE_DT    =  40.0; // degrees/second

  /** safe distance from other object */
  public static final double SAFE_DISTANCE     =   3.0; // meters

  /** the "way too close do something about it" distance */
  public static final double CRITICAL_DISTANCE =   2.0; // meters

  /** number of spars graphically printed on the orb */
  public static final int    ORB_SPAR_COUNT    =   4  ; // arcs

  /** time in seconds for a phantom to move to it's target position */
  public static final double PHANTOM_PERIOD    =  1   ;

  /*
   * The following are global objects.
   */

  /** The robot used for screen capture. */
  private Robot robot;


  /** scale for graphics */
  private double pixelsPerMeter = DEFAULT_PIXELS_PER_METER;

  /** operational mode (live or simulated) */
  private boolean liveMode = false;

  /** Current active SwarmCon object. */
  private static SwarmCon activeSwarmCon = null;

  IProperty<?>[] propertyList = 
  {
    // help

    new FlagProperty("help", 'h', false, 
      "should help text be displayed"),

    // orbs

    new BooleanProperty("swarmcon.orb0.enabled", false,
      "is orb #0 enabled"),
    new BooleanProperty("swarmcon.orb1.enabled", false,
      "is orb #1 enabled"),
    new BooleanProperty("swarmcon.orb2.enabled", false,
      "is orb #2 enabled"),
    new BooleanProperty("swarmcon.orb3.enabled", false,
      "is orb #3 enabled"),
    new BooleanProperty("swarmcon.orb4.enabled", false,
      "is orb #4 enabled"),
    new BooleanProperty("swarmcon.orb5.enabled", false,
      "is orb #5 enabled"),

    // motion control

    new IntegerProperty("swarmcon.motion.powerRange", 50,
      "dynamic range of power commands"),
    new IntegerProperty("swarmcon.motion.steeringRange", 100,
      "dynamic range of steering commands"),

    // sound

    new StringProperty("swarmcon.sound.soundCatalogs",
      "resources/songs/sounds.catalog", "location of sounds"),
    new StringProperty("swarmcon.sound.errataCatalog",
      "resources/songs/errata.catalog", "location of sound errata"),

    // live commanding of orbs
       
    new StringProperty("swarmcon.comm.serialPort", "simulation",
      "serial port to send command too, either \"simulation\" or something like one of these: /dev/tty.PL2303-0000101D, /dev/tty.PL2303-0000201A, /dev/tty.Bluetooth-PDA-Sync"),
    new BooleanProperty("swarmcon.comm.sendCommandsToOrbs", true,
      "should commands be sent to orbs"),
    new BooleanProperty("swarmcon.comm.multipleMotionCommands", false,
      "sould multiple motion commands be sent"),
    new BooleanProperty("swarmcon.comm.multipleSoundCommands", false,
      "sould multiple motion commands be sent"),
    new LongProperty("swarmcon.comm.commandRefreshDelay", 200, 
      "delay between commands set to orbs in milliseconds"),
    new LongProperty("swarmcon.comm.positionPollPeriod", 200, 
      "the period between position requests in milliseconds"),
  };

  /** Properties for tweaking the system. */
  private PropertySet properties = new PropertySet()
    {
      {
        for (IProperty<?> property: propertyList)
          add(property);
      }
    };



  /** The global source of randomness. */
  public static final Random RND = new Random();

  /** The list time the screen was painted */

  private Calendar lastPaint = Calendar.getInstance();

  /** Grid related values */

  private static Stroke gridStroke = new BasicStroke(
  .025f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
  private static Color grid1Color = new Color(0, 0, 0, 40);
  private static Color grid2Color = new Color(0, 0, 0, 30);
  double grid1Size = 5;
  double grid2Size = 1;

  // stroke used to paint the reticle at the center of the workd

  private static Stroke reticleStroke = new BasicStroke(
    .10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  /*
   * The following are values specified in the properties file.  The
   * default values here will be overwritten by the values in the
   * properties file or command line values, if either exist.
   */

  /** stepped color fades (true == do simulation; false = rely on
   * light board to do the fades) */
  public static boolean steppedColorFades = false;

  /** send multiple sound commands */
  public static String serialPortId = "";

  /** send multiple sound commands */
  public static boolean multipleSoundCommands = true;

  /** send multiple motion commands */
  public static boolean multipleMotionCommands = true;

  /** allowable range of values for power */
  public int powerRange = 50;

  /** allowable range of values for steering */
  public int steeringRange = 100;

  /** delay between sending commands to the orb */
  public long commandRefreshDelay = 200;

  /** delay between sending commands to the orb */
  public long positionPollPeriod = 200;

  /** enable sending commands to orbs */
  public boolean sendCommandsToOrbs = true;

  /** enable colors in simulation */
  public boolean simulateColors = true;

  /** enable sounds in simulation */
  public boolean simulateSounds = false;

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

  /** the number of orbs inferred from the properties file */
  public static int orbCount = 0;

  /** arena in which we play */

  final JPanel arena = new JPanel()
    {
      private static final long serialVersionUID = 6473960062741753128L;

      public void paint(Graphics graphics)
      {
        paintArena(graphics);
      }
    };

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

  private OrbIo orbIo;

  /** communication with GPS */

  GpsIo gpsIo;

  /** Implementation of
   * com.orbswarm.choreography.OrbControl.IOrbControl to control the
   * Orbs from the Specialists. Currently control is split between
   * OrbControl and and OrbIo.  This is a bad thing, and this
   * functionality needs to be all moved into one place.  I would be
   * inclined to have orb IO implement IOrbControl. */

  OrbControl orbControlImpl;

  // TODO: generalize the orbControl facility to use real/fake at
  // appropriate times.
  
  public IOrbControl getOrbControl()
  {
    return orbControlImpl;
  }

  // HACQUE! get the specifically-typed version so we can break our
  // nicely-wrought loose coupling.
  public OrbControl getOrbControlImpl()
  {
    return orbControlImpl;
  }

  public OrbIo getOrbIo()
  {
    return this.orbIo;
  }
  // color

  public static Color BACKGROUND       = WHITE;
  public static Color BUTTON_CLR       = new Color(  0,   0,   0, 164);
  public static Color TEXT_CLR         = new Color(  0,   0,   0, 128);
  public static Color MENU_CLR         = new Color(  0,   0,   0, 128);
  public static Color ORB_CLR          = new Color(196, 196, 196);
  public static Color ORB_FRAME_CLR    = new Color( 64,  64,  64);
  public static Color SEL_ORB_CLR      = new Color(255, 196, 255);
  public static Color VECTOR_CRL       = new Color(255,   0,   0, 128);
  public static Font  MISC_FONT        = new Font("Helvetica", Font.PLAIN, 15);
  public static Font  ORB_FONT         =  new Font("Courier New", Font.PLAIN, 15);
  public static Font  PHANTOM_ORB_FONT =  new Font("Courier New", Font.PLAIN, 3);
  public static Font  MENU_FONT        =  new Font("Helvetica", Font.PLAIN, 15);

  /** Standard button font */

  public static Font BUTTON_FONT = new Font("Lucida Grande", Font.PLAIN, 40);

  static
  {
    ORB_FONT = scaleFont(ORB_FONT, DEFAULT_PIXELS_PER_METER);
    PHANTOM_ORB_FONT = scaleFont(PHANTOM_ORB_FONT, DEFAULT_PIXELS_PER_METER);
  }

  /** Scale a font which are to be used in arena units
   *
   * @param original font to be scale
   * @return a scaled copy of the font
   */

  public static Font scaleFont(Font original, double scale)
  {
    return original.deriveFont((float)(original.getSize() / scale));
  }

  /** the global word offset to correct the orbs postion by */

  private static Point globalOffset = new Point(0, 0);

  /** swarm of mobjects (not just orbs) (maybe this should not be
   * called the swarm?) */

  Swarm swarm;

  public Swarm getSwarm()
  {
    return swarm;
  }

  public Orb getOrb(int orbNum)
  {
    return getSwarm().getOrb(orbNum);
  }

  /** selected objects */

  Mobjects selected = new Mobjects();


  /** phantom objects */

  Vector<Phantom> phantoms = new Vector<Phantom>();

  /** last time mobects were updated */

  Calendar lastUpdate = Calendar.getInstance();

  /** format for printing heading values */

  public static NumberFormat HeadingFmt = NumberFormat.getNumberInstance();
  public static NumberFormat UtmFmt = NumberFormat.getNumberInstance();
  public static NumberFormat StdFmt = NumberFormat.getNumberInstance();

  /** static initializations */

  static
  {
    HeadingFmt.setMaximumIntegerDigits(3);
    HeadingFmt.setMinimumIntegerDigits(3);
    HeadingFmt.setMaximumFractionDigits(0);
    HeadingFmt.setGroupingUsed(false);
    UtmFmt.setMinimumIntegerDigits(1);
    UtmFmt.setMaximumFractionDigits(3);
    UtmFmt.setMinimumFractionDigits(3);
    UtmFmt.setGroupingUsed(false);
    StdFmt.setMinimumIntegerDigits(1);
    StdFmt.setMaximumFractionDigits(2);
    StdFmt.setMinimumFractionDigits(2);
    StdFmt.setGroupingUsed(false);
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
    SwarmCon sc = new SwarmCon(args);
    sc.initialize();
  }

  // construct a swarm

  public SwarmCon(String[] args)
  {
    try
    {
      activeSwarmCon = this;
      readProperties(args);
      setValuesFromProperties();
      robot = new Robot();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /** Get the current active instance of SwarmCom.
   *
   * @return the current instance SwarmCon
   */

  public static SwarmCon getInstance()
  {
    return activeSwarmCon;
  }


  /** Splitting constructor from initializer, so that parameters can
   *  be set in the main routine before starting it up. (e.g. can't
   *  reset the timeline width after constructing the frame).
   */

  public void initialize()
  {
    orbControlImpl = new OrbControl(
      this,
      sendCommandsToOrbs,
      simulateColors,
      simulateSounds);

    // construct the frame

    boolean shouldCreateOrbsNow = constructFrame(getContentPane());

    // get the graphics device from the local graphic environment

    GraphicsDevice gv = GraphicsEnvironment.
      getLocalGraphicsEnvironment().getScreenDevices()[0];

    // if full screen is supported setup frame accordingly

    if (gv.isFullScreenSupported())
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
    }
    cardLayout.first(centerPanel);

    // init Swarm

    if (shouldCreateOrbsNow)
      createOrbs();

    // start motor control thread

    requestFocus();
    
    startControlling();
  }

  /** Reads properties file out of users home directory.  If this
   * file does not exist a default one will be created. */

  private void readProperties(String[] args)
  {
    try
    {
      // if the properties file does not exist, copy a fresh one out
      // of the jar

      File propFile = new File(PROPERTIES_FILE_LOCATION);
      if (!propFile.exists())
        JarTools.copyResource(DEFAULT_PROPERTIES_FILE, propFile);

      // identify where the properties file lives

      log.debug( "Properties file location: " + PROPERTIES_FILE_LOCATION);

      // read in the properties

      properties.applyPropertyFile(propFile);

      // override properties with command line values

      properties.applyArgs(args);

      // if help requested print help text

      if (properties.getFlag("help"))
      {
        System.out.println(properties.toHelpString());
        System.exit(0);
      }

      // output properties so people know what they got

      log.debug(
        "-------------------- SwarmCon Properties --------------------");
      Vector<String> names = new Vector<String>(properties.getNames());
      Collections.sort(names);
      for (String name: names)
        log.debug(name + " = " + properties.getValueAsString(name));
      log.debug(
        "-------------------------------------------------------------");
    }
    catch (Exception e)
    {
      log.error(
        "SwarmCon() caught exception reading properties. Using defaults.");
      e.printStackTrace();
    }
  }

  /** all defaults need to be specified in the properties file, and
   * here, in case the properties file isn't found for some reason. */

  private void setValuesFromProperties()
  {
    PropertySet ps = properties;
    powerRange     = ps.getInteger("swarmcon.motion.powerRange");
    steeringRange  = ps.getInteger("swarmcon.motion.steeringRange");
    commandRefreshDelay = ps.getLong("swarmcon.comm.commandRefreshDelay");
    positionPollPeriod = ps.getLong("swarmcon.comm.positionPollPeriod");
    sendCommandsToOrbs = ps.getBoolean("swarmcon.comm.sendCommandsToOrbs");
    multipleMotionCommands  = ps.getBoolean("swarmcon.comm.multipleMotionCommands");
    serialPortId  = ps.getString("swarmcon.comm.serialPort");

    // init the motorCommandInfo now that we know how many orbs we have

    motorCommandInfo = new MotorCommandInfo[MAX_ORB_COUNT];

    // read orb specific data

    orbCount = 0;
    for (int orbId = 0; orbId < MAX_ORB_COUNT; ++orbId)
    {
      // populate the motor command info array and set enabled state for each orb

      motorCommandInfo[orbId] = new MotorCommandInfo();
      motorCommandInfo[orbId].enabled = ps.getBoolean( 
        "swarmcon.orb" + orbId + ".enabled");

      // compute an accurate orb count

      if (motorCommandInfo[orbId].enabled)
        ++orbCount;
    }
  }

  public void setPowerRange(int val)
  {
    powerRange = val;
  }

  public void setSteeringRange(int val)
  {
    steeringRange = val;
  }

  boolean running = false;
  public void startControlling()
  {
    log.debug("Start controlling");
    // if we are already running, stop that
    if (running)
      stopControlling();

    log.debug("SwarmCon: start controlling");
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
    log.debug("SwarmCon: stop controlling");
  }

  /** Convert milliseconds to decimal seconds.
   *
   * @param milliseconds ya know milliseconds
   *
   * @return decimal seconds.
   */

  public static double millisecondsToSeconds(long milliseconds)
  {
    return milliseconds / 1000d;
  }

  /** Convert decimal seconds to milliseconds.
   *
   * @param decimal seconds
   *
   * @return ya know milliseconds
   */

  public static long secondsToMilliseconds(double seconds)
  {
    return (long)(seconds * 1000);
  }


  /** Create the swarm and the orbs it is composed of. */

  public void createOrbs()
  {
    // set bounds from arena

    Rectangle2D.Double bounds = new Rectangle2D
      .Double(arena.getBounds().getX() / pixelsPerMeter,
        arena.getBounds().getY() / pixelsPerMeter,
        arena.getBounds().getWidth()  / pixelsPerMeter,
        arena.getBounds().getHeight() / pixelsPerMeter);

    // create the swarm object

    swarm = new Swarm(bounds);

    Mobject preveouse = new MouseMobject(arena);
    swarm.add(preveouse);

    // create the orbs

    log.debug("Creating orbs: ");

    // compute the time offset between orbs for orbs to request
    // position updates, so as not to overload the network

    double positionPollPeriodDelta =
      millisecondsToSeconds(positionPollPeriod) / orbCount;
    double positionPollPeriodOffset = 0;

    // now create each orb

    for (int id = 0; id < MAX_ORB_COUNT; ++id)
    {
      // if this orb is not enbabled, don't create it

//       if (!getBooleanProperty("swarmcon.orb" + id + ".enabled", false))
//         continue;

      // report the birth of an orb

      log.debug("id: " + id + " mode: " + (liveMode ? "LIVE" : "SIM"));

      // create the orb model

      MotionModel model = liveMode
        ? new LiveModel(orbIo, id, positionPollPeriodOffset)
        : new SimModel();

      positionPollPeriodOffset += positionPollPeriodDelta;

      // get the controllers for the new orb, but we're not doing
      // anything with them at the moment

      Orb orb = new Orb(swarm, model, id);

      // register the new orb or orb io so it can get messages

      if (orbIo != null)
        orbIo.register(orb);

      // add the orb to the swarm

      swarm.add(orb);

      // if in simulation mode, add behaviors

      if (!liveMode)
      {
        Behavior nb = new NoBehavior();
        Behavior fb = new FollowBehavior(preveouse);
        Behavior wb = new WanderBehavior();
        Behavior rb = new RandomBehavior();
        Behavior cb = new ClusterBehavior();
        Behavior fab = new AvoidBehavior(fb);
        Behavior cab = new AvoidBehavior(cb);
        orb.add(wb);
        orb.add(rb);
        orb.add(cb);
        orb.add(fab);
        orb.add(cab);
        orb.add(fb);
        orb.add(nb);
      }

      // record preveouse for the follow behavior

      preveouse = orb;
    }

    // if in live mode, init the swarm origin

    if (liveMode)
    {
      //         new Thread()
      //         {
      //             public void run()
      //             {
      //               JOptionPane.showMessageDialog(
      //                 activeSwarmCon, "Surveying Orbs, see console for progress.");
      //             }
      //         }.start();

      initSwarmOrigin();
    }
  }

  // collect orb survey positions and inform the orbs what they should
  // all use for the swarm origin (0, 0).  this process can take a
  // while (upto 60 seconds) if the orbs have not completed their
  // de-biasing process.  this method blocks until the process is
  // complete.

  public void initSwarmOrigin()
  {
    try
    {
      // extrect just the orbs from the swarm

      Vector<Orb> orbs = new Vector<Orb>();
      for (Mobject m: swarm)
        if (m instanceof Orb)
          orbs.add((Orb)m);

      // create a place to put survey results

      HashMap<Orb, Point> results = new HashMap<Orb, Point>();

      // keep going until we have has many results as we do orbs

      while (results.size() < orbs.size())
      {
        // walk through orbs

        for (Orb orb: orbs)
        {
          // if we don't have a survey result

          if (results.get(orb) == null)
          {
            MotionModel mm = orb.getModel();

            // if we got a result since we last checked, record that

            Point p = mm.getSurveyPosition();

            if (p != null)
            {
              results.put(orb, p);
              log.debug(
                "survey result from orb [" + orb.getId() + "]: " + p);
            }

            // otherwise request a report

            else
            {
              orbIo.requestSurvayPosition(orb.getId());
              log.debug(
                "survey request to orb [" + orb.getId() + "]");
            }
          }

          // sleep a little between each orb to give the zigbee a break

          Thread.sleep(positionPollPeriod / orbs.size());
        }
      }

      // now we have ALL the survey resutls compute the centroid of
      // the orbs

      Point centroid = new Point();
      for (Point p: results.values())
        centroid.translate(p);
      centroid.scale(results.values().size());

      // now inform all the orbs of the new origin

      while (results.size() > 0)
      {
        for (Orb orb: orbs)
        {
          // if we're not done with this orb yet

          if (results.get(orb) != null)
          {
            MotionModel mm = orb.getModel();

            // if we got an ack, this orb is done

            if (mm.isOriginAcked())
            {
              results.remove(orb);
              log.debug(
                "origin ack from orb [" + orb.getId() + "]: " + centroid);
            }

            // otherwise send the origin to the orb

            else
            {
              orbIo.commandOrigin(orb.getId(), centroid);
              log.debug(
                "sent origin to orb [" + orb.getId() + "]: " + centroid);
            }

            // sleep a little between each orb to give the zigbee a break

            Thread.sleep(positionPollPeriod / orbs.size());
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  // update the world

  public void update()
  {
    // if (for whatever reason) there is no swarm, don't update

    if (swarm == null)
      return;

    synchronized (swarm)
    {
      // sleep until it's been a minimum frame delay

      try
      {
        long start = lastUpdate.getTimeInMillis();
        while (currentTimeMillis() - start < MIN_FRAME_DELAY)
          Thread.sleep(MIN_FRAME_DELAY / 4);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }

      // get now

      Calendar now = Calendar.getInstance();
      long nowMillis = now.getTimeInMillis();

      double time = millisecondsToSeconds(
        nowMillis - lastUpdate.getTimeInMillis());

      // establish the time since last update

      lastUpdate = now;

      // update all the objects

      swarm.update(time);
      swarm.updateOrbDistances();
      // repaint the screen
      arena.repaint();
    }
  }

  /** Compute the time now.
   *
   * @return the current time in seconds since 1970 as double
   */

  public static double getTime()
  {
    return Calendar.getInstance().getTimeInMillis() / 1000d;
  }

  public void repaint()
  {
    arena.repaint();
  }

  public JLabel createBigLabel(String text, Color color)
  {
    JLabel label = new JLabel(text);
    label.setForeground(color);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
    return label;
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

  /** Place gui objects into frame.
   *
   * @param frame container to put stuff into
   *
   * @return weather or not create the orbs after done with this or if
   * orb creation will be handled by the spash screen code.
   */

  public boolean constructFrame(Container frame)
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
    if (serialPortId.equalsIgnoreCase(SIMULATION))
      splashNeeded = false;
    else
      for (String portId: SerialIo.listSerialPorts())
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

    // intermediary panel to put the arena and control UIs side-by-side

    actionPanel = new JPanel();
    actionPanel.setLayout(new GridBagLayout());

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

    // if no splash needed go ahead and start the system

    if (!splashNeeded)
    {
      (new SwarmComPortAction(serialPortId)).actionPerformed(null);
    }

    // if no splash needed then create the orbs in the follow on procedure

    return !splashNeeded;
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

    Point2D.Double center = new Point2D.Double(
      -globalOffset.getX(),
      -globalOffset.getY());

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

  public void paintArena(Graphics graphics)
  {
    // config graphics

    int width = arena.getWidth();
    int height = arena.getHeight();
    Graphics2D g = (Graphics2D)graphics;

    g.setColor(BACKGROUND);
    g.fillRect(0, 0, width, height);
    g.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    // paint frame rate

    g.setColor(TEXT_CLR);
    g.setFont(MISC_FONT);
    g.drawString(
      "frame rate: " + (currentTimeMillis() - lastPaint.getTimeInMillis())
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
              " X: "  + UtmFmt.format(orb.getX()) +
              " Y: "  + UtmFmt.format(orb.getY()) +
              " R: "  + HeadingFmt.format(round(orb.getRoll   ().as(HEADING))) +
              " P: "  + HeadingFmt.format(round(orb.getPitch  ().as(HEADING))) +
              " Y: "  + HeadingFmt.format(round(orb.getYaw    ().as(HEADING))) +
              " YR: " + HeadingFmt.format(round(orb.getYawRate().as(DEGREE_RATE))) +
              " V: "  + round(orb.getSpeed() * 100) / 100d,
              5, 15 + id++ * 15);
          }
      }
    }


    // set 0,0 to lower left corner, and scale for meters

    g.scale(pixelsPerMeter, -pixelsPerMeter);

    // apply the global offset

    g.translate(
      getGlobalOffset().getX(),
      getGlobalOffset().getY());

    // draw the grid

    paintGrid(g, grid1Size, grid1Color, gridStroke);
    paintGrid(g, grid2Size, grid2Color, gridStroke);

    // indicate the center of the world

    g.setColor(new Color(128, 128, 128));
    g.setStroke(reticleStroke);
    g.draw(new Line2D.Double(-grid2Size, 0, grid2Size, 0));
    g.draw(new Line2D.Double(0, -grid2Size, 0, grid2Size));

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

  /** Paint a grid onto the display.
   *
   * @param g graphics context to draw grid onto
   * @param gridSize size between lines on the grid
   * @param gridColor color of the grid lines
   * @param gridStroke the stroke used to draw the grid lines
   */

  public void paintGrid(
    Graphics2D g, double gridSize, Color gridColor, Stroke gridStroke)
  {
    // width and height of grid

    int width = arena.getWidth();
    int height = arena.getHeight();

    // compute the grid starting position

    double gridX =
      -(getGlobalOffset().getX() - getGlobalOffset().getX() % gridSize);
    double gridY =
      -(getGlobalOffset().getY() - getGlobalOffset().getY() % gridSize);

    // set the color and stroke

    g.setColor(gridColor);
    g.setStroke(gridStroke);

    // draw the verticals

    for (double x = gridX; x <= gridX + 1.5 * width / pixelsPerMeter; x += gridSize)
      g.draw(new Line2D.Double(
          x, -getGlobalOffset().getY(),
          x, -getGlobalOffset().getY() - height / pixelsPerMeter));

    // draw the horizontals

    for (double y = gridY; y >= gridY - 1.5 * height / pixelsPerMeter; y -= gridSize)
      g.draw(new Line2D.Double(
          -getGlobalOffset().getX(), y,
          -getGlobalOffset().getX() + width / pixelsPerMeter, y));
  }


  /** Get the global offset. */

  public Point getGlobalOffset()
  {
    return new Point(
      globalOffset.getX() + (arena.getWidth() / pixelsPerMeter / 2),
      globalOffset.getY() - (arena.getHeight() / pixelsPerMeter / 2));
  }

  /** Set the global offset. */

  public void setGlobalOffset(Point _globalOffset)
  {
    globalOffset = _globalOffset;
    log.debug("new global offset: " + globalOffset);
  }

  // object which is always set to the position of the mouse

  public class MouseMobject extends Mobject
  {
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
            setPosition(screenToWorld(e.getPoint()));
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

  /** Convert screen coordinates to world coordinates.
   *
   * @param screenPos a position in screen coordinates
   *
   * @return the point converted to world coodinates.
   */

  public Point2D.Double screenToWorld(java.awt.Point screenPos)
  {
    return new Point2D.Double(
      screenPos.getX() /  pixelsPerMeter - getGlobalOffset().getX(),
      screenPos.getY() / -pixelsPerMeter - getGlobalOffset().getY());
  }

  /** Capture the area from a given component.
   *
   * @param component the component to collect the image from
   *
   * @retrn a buffered image of the component passed in.
   */

  public BufferedImage captureImage(JComponent component)
  {
    Rectangle bounds = component.getBounds();
    java.awt.Point point = new java.awt.Point(bounds.x, bounds.y);
    SwingUtilities.convertPointToScreen(point, component);
    bounds.setBounds(point.x, point.y, bounds.width, bounds.height);
    return robot.createScreenCapture(bounds);
  }

  /** Capture the area from a given component and write it out to the
   * home director of the user.
   *
   * @param component the component to collect the image from
   */

  public void captureAndStoreImage(JComponent component)
  {
    try
    {
      File file = File.createTempFile(
        "swarmcon", ".png", new File(System.getProperty("user.home")));
      ImageIO.write(captureImage(arena), "png", file);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /** Capture the current arean and write it to an image file. */

  public void captureArena()
  {
    log.debug("image capture.");
    captureAndStoreImage(arena);
  }

  /** swarm mouse input adapter */

  class SwarmMia extends MouseInputAdapter
  {
    private MouseEvent clickEvent = null;

    // mouse pressed event

    public void mousePressed(MouseEvent e)
    {
      clickEvent = e;
    }

    // mouse dragged event

    public void mouseDragged(MouseEvent e)
    {
      if (clickEvent != null)
      {
        Point2D start = screenToWorld(clickEvent.getPoint());
        Point2D end = screenToWorld(e.getPoint());
        setGlobalOffset(new Point(
            globalOffset.getX() + (end.getX() - start.getX()),
            globalOffset.getY() + (end.getY() - start.getY())));
        clickEvent = e;
      }
    }

    // mouse released event

    public void mouseReleased(MouseEvent e)
    {
      clickEvent = null;
    }

    // mouse moved event

    public void mouseMoved(MouseEvent e)
    {
    }

    // mouse clicked event

    public void mouseClicked(MouseEvent e)
    {
      if (e.isControlDown())
        commandOrb(e);
      else
        selectOrb(e);
    }

    Orb orbToCommand = null;
    Path commandPath = null;

    public void commandOrb(MouseEvent e)
    {
      Point2D.Double worldPos = screenToWorld(e.getPoint());
      final Mobject nearest = swarm.findSelected(worldPos);

      if (nearest != null && nearest instanceof Mobject)
      {
        orbToCommand = (Orb)nearest;
      }
      else if (orbToCommand != null)
      {
        if (commandPath == null)
        {
          commandPath = new Path();
          commandPath.add(new Target(orbToCommand.getPosition()));
        }

        commandPath.add(new Target(worldPos));

        if (!e.isMetaDown())
        {
          orbToCommand.getModel().setTargetPath(commandPath);
          commandPath = null;
        }
      }
    }

    // select an orb for zoom display

    public void selectOrb(MouseEvent e)
    {
      // find nearest selectable mobject

      final Mobject nearest = swarm
        .findSelected(screenToWorld(e.getPoint()));

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
    private static final long serialVersionUID = 2376655282485450773L;

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

  /**
   * Action class which selects a given serial port with witch to
   * communicate to the orbs.
   */

  class SwarmComPortAction extends SwarmAction
  {
    private static final long serialVersionUID = -8462656494373639651L;
    
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
        if (!portId.equalsIgnoreCase(SIMULATION))
        {
          orbIo = new OrbIo(portId);
          orbControlImpl.setOrbIo(orbIo);
          liveMode = true;
        }
        else
          liveMode = false;

        createOrbs();

        cardLayout.last(centerPanel);
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
        liveMode = false;
        createOrbs();

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

  /** Zoom display in. */

  SwarmAction zoomIn = new SwarmAction(
    "Zoom in",
    getKeyStroke(VK_MINUS, 0),
    "zoom in on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        pixelsPerMeter /= 1.1;
      }
    };

  /** Zoom display out. */

  SwarmAction zoomOut = new SwarmAction(
    "Zoom out",
    getKeyStroke(VK_EQUALS, 0),
    "zoom out on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        pixelsPerMeter *= 1.1;
      }
    };

  /** Emergency stop all orbs. */

  SwarmAction emergencyStop = new SwarmAction(
    "Emergency Stop",
    getKeyStroke(VK_SPACE, 0),
    "stop all the orbs now")
    {
      public void actionPerformed(ActionEvent e)
      {
        for (Mobject mo: swarm)
          if (mo instanceof Orb)
            ((Orb)mo).getModel().stop();
      }
    };

  /** action to exist the system */

  SwarmAction exit = new SwarmAction(
    "Exit",
    getKeyStroke(VK_ESCAPE, 0),
    "exit this program")
    {
      public void actionPerformed(ActionEvent e)
      {
        System.exit(0);
      }
    };

  /** screen capture */

  SwarmAction captureScreen = new SwarmAction(
    "Capture Arena",
    getKeyStroke(VK_SPACE, SHIFT_MASK),
    "save an image of the arena to your home directory")
    {
      public void actionPerformed(ActionEvent e)
      {
        captureAndStoreImage(arena);
      }
    };

  /** all the actions in one handy place */

  SwarmAction[] actions =
  {
    reset,
    nextBehavior,
    previousBehavior,
    emergencyStop,
    zoomIn,
    zoomOut,
    captureScreen,
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
}
