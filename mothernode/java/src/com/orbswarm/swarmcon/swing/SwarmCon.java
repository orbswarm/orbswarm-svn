package com.orbswarm.swarmcon.swing;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Calendar;

import com.orbswarm.swarmcon.behavior.Behavior;
import com.orbswarm.swarmcon.behavior.ClusterBehavior;
import com.orbswarm.swarmcon.behavior.FollowBehavior;
import com.orbswarm.swarmcon.behavior.NoBehavior;
import com.orbswarm.swarmcon.behavior.RandomBehavior;
import com.orbswarm.swarmcon.behavior.WanderBehavior;
import com.orbswarm.swarmcon.io.OrbIo;
import com.orbswarm.swarmcon.io.SerialIo;
import com.orbswarm.swarmcon.model.IMotionModel;
import com.orbswarm.swarmcon.model.LiveModel;
import com.orbswarm.swarmcon.model.AMotionModel;
import com.orbswarm.swarmcon.model.SimModel;
import com.orbswarm.swarmcon.orb.IOrbControl;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.orb.OrbControl;
import com.orbswarm.swarmcon.orb.Phantom;
import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.Path;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.util.Constants;
import com.orbswarm.swarmcon.util.SwarmProperties;
import com.orbswarm.swarmcon.view.APositionable;
import com.orbswarm.swarmcon.view.IRenderable;
import com.orbswarm.swarmcon.view.RendererSet;

import static org.trebor.util.ShapeTools.normalize;
import static org.trebor.util.ShapeTools.scale;
import static org.trebor.util.ShapeTools.translate;
import static org.trebor.util.Angle.Type.DEGREE_RATE;
import static org.trebor.util.Angle.Type.HEADING;
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
import static java.awt.event.KeyEvent.VK_X;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;
import static com.orbswarm.swarmcon.util.Constants.*;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class SwarmCon extends JFrame
{
  protected IBlockPath mTestBlockPath;

  private static Logger log = Logger.getLogger(SwarmCon.class);

  /** The robot used for screen capture. */
  private final Robot mRobot;

  /** operational mode (live or simulated) */

  private boolean mLiveMode = false;

  /** Properties for tweaking the system. */

  private final SwarmProperties mProperties;

  /** The list time the screen was painted */

  private Calendar mLastPaint = Calendar.getInstance();

  /** send multiple sound commands */
  private static String mSerialPortId = "";

  /** delay between sending commands to the orb */
  private long mCommandRefreshDelay = 200;

  private long mPositionPollPeriod = 200;

  /** enable sending commands to orbs */
  private boolean mSendCommandsToOrbs = true;

  /** enable colors in simulation */
  private boolean mSimulateColors = true;

  /** enable sounds in simulation */
  private boolean mSimulateSounds = false;

  /** arena in which we play */

  private final ArenaPanel mArena = new ArenaPanel()
  {
    private static final long serialVersionUID = 6473960062741753128L;

    public void paint(Graphics graphics)
    {
      Graphics2D g = (Graphics2D)graphics;

      AffineTransform baseTransform = g.getTransform();
      super.paint(graphics);
      synchronized (mVisualObjects)
      {
        paintArena((Graphics2D)graphics, baseTransform);
      }
    }
  };

  private CardLayout mCardLayout;

  /** center panel which is the main view area */

  private JPanel mCenterPanel;

  /** action panel which contains the arena and the controlUI */

  private JPanel mActionPanel;

  /** communication with orbs */

  private OrbIo mOrbIo;

  /**
   * Implementation of com.orbswarm.choreography.OrbControl.IOrbControl to
   * control the Orbs from the Specialists. Currently control is split
   * between OrbControl and and OrbIo. This is a bad thing, and this
   * functionality needs to be all moved into one place. I would be
   * inclined to have orb IO implement IOrbControl.
   */

  private OrbControl mOrbControlImpl;

  /** the global word offset to correct the orbs position by */

  private final Point2D mGlobalOffset = new Point2D.Double();

  /** swarm of orbs */

  private final List<IOrb> mSwarm;

  /** all visual objects in the system */

  private final List<IRenderable> mVisualObjects;

  /** selected objects */

  private final List<IRenderable> mSelected;

  /** phantom objects */

  private final List<Phantom> mPhantoms;

  /** last time mobects were updated */

  private Calendar mLastUpdate = Calendar.getInstance();

  /** static initializations */

  static
  {
    Constants.HEADING_FORMAT.setMaximumIntegerDigits(3);
    Constants.HEADING_FORMAT.setMinimumIntegerDigits(3);
    Constants.HEADING_FORMAT.setMaximumFractionDigits(0);
    Constants.HEADING_FORMAT.setGroupingUsed(false);
    Constants.UTM_FORMAT.setMinimumIntegerDigits(1);
    Constants.UTM_FORMAT.setMaximumFractionDigits(3);
    Constants.UTM_FORMAT.setMinimumFractionDigits(3);
    Constants.UTM_FORMAT.setGroupingUsed(false);
    Constants.STANDARD_FORMAT.setMinimumIntegerDigits(1);
    Constants.STANDARD_FORMAT.setMaximumFractionDigits(2);
    Constants.STANDARD_FORMAT.setMinimumFractionDigits(2);
    Constants.STANDARD_FORMAT.setGroupingUsed(false);
  }

  // color

  static
  {
    ORB_FONT = scaleFont(ORB_FONT, DEFAULT_PIXELS_PER_METER);
    PHANTOM_ORB_FONT = scaleFont(PHANTOM_ORB_FONT, DEFAULT_PIXELS_PER_METER);
  }

  public IOrbControl getOrbControl()
  {
    return mOrbControlImpl;
  }

  // HACQUE! get the specifically-typed version so we can break our
  // nicely-wrought loose coupling.
  public OrbControl getOrbControlImpl()
  {
    return mOrbControlImpl;
  }

  public OrbIo getOrbIo()
  {
    return this.mOrbIo;
  }

  /**
   * Scale a font which are to be used in arena units
   * 
   * @param original font to be scale
   * @return a scaled copy of the font
   */

  public static Font scaleFont(Font original, double scale)
  {
    return original.deriveFont((float)(original.getSize() / scale));
  }

  public IOrb getOrb(int orbNum)
  {
    for (IOrb orb: mSwarm)
      if (orb.getId() == orbNum)
        return orb;
        
    return null;
  }

  public static void main(String[] args)
  {
    try
    {
      new SwarmCon(args);
    }
    catch (AWTException e)
    {
      log.error(e.toString());
      e.printStackTrace();
    }
  }

  // construct a swarm

  public SwarmCon(String[] args) throws AWTException
  {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    
    // establish all properties

    mProperties = new SwarmProperties(args);
    setValuesFromProperties();

    // create a place to put all visual objects

    mVisualObjects = new Vector<IRenderable>();

    // create a test block path

    IBlockPath path = new BlockPath();
    IBlock curve1 =
      new CurveBlock(90, 5, CurveBlock.Type.RIGHT);
    IBlock straight1 = new StraightBlock(5);
    IBlock curve2 =
      new CurveBlock(180, 3,
        CurveBlock.Type.LEFT);
    IBlock curve3 =
      new CurveBlock(90, 2,
        CurveBlock.Type.LEFT);
    IBlock straight2 = new StraightBlock(5);

    IBlock[] blocks =
    {
      curve1, straight1, curve2, curve3, straight2,
    };

    path.addAfter(blocks);

    mTestBlockPath = path;
    mVisualObjects.add(path);

    // create the place for the swarm and add it to visual objects

    mSwarm = new Vector<IOrb>();
    mVisualObjects.addAll(mSwarm);

    // create a place to put phantoms and add it to visual objects

    mPhantoms = new Vector<Phantom>();
    mVisualObjects.addAll(mPhantoms);

    // create a place record selected vobject

    mSelected = new Vector<IRenderable>();

    // create robot for capturing images

    mRobot = new Robot();

    // initialize

    initialize();
  }

  /**
   * Splitting constructor from initializer, so that parameters can be set
   * in the main routine before starting it up. (e.g. can't reset the
   * time-line width after constructing the frame).
   */

  public void initialize()
  {
    mOrbControlImpl =
      new OrbControl(this, mSendCommandsToOrbs, mSimulateColors,
        mSimulateSounds);

    // get the graphics device from the local graphic environment

    GraphicsDevice gv =
      GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];

    // if full screen is supported setup frame accordingly

    boolean fullScreenSupported = gv.isFullScreenSupported();
    fullScreenSupported = false;

    if (fullScreenSupported)
    {
      System.setProperty("apple.laf.useScreenMenuBar", "false");
      constructFrame();
      setUndecorated(true);
      setVisible(true);
      pack();
      gv.setFullScreenWindow(this);
    }
    // otherwise just make a big frame

    else
    {
      constructFrame();
      pack();
      //setExtendedState(MAXIMIZED_BOTH);
      setSize(new Dimension(800, 600));
      setVisible(true);
    }
    mCardLayout.first(mCenterPanel);
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        mArena.setViewCenter();
      }
    });
    
    // initialization Swarm

    createOrbs();

    // configure the orbs

    resetSwarm();

    // start motor control thread

    requestFocus();

    startControlling();
  }

  /**
   * all defaults need to be specified in the properties file, and here, in
   * case the properties file isn't found for some reason.
   */

  private void setValuesFromProperties()
  {
    SwarmProperties ps = mProperties;
    setCommandRefreshDelay(ps.getLong("swarmcon.comm.commandRefreshDelay"));
    mPositionPollPeriod = ps.getLong("swarmcon.comm.positionPollPeriod");
    mSendCommandsToOrbs = ps.getBoolean("swarmcon.comm.sendCommandsToOrbs");
    mSerialPortId = ps.getString("swarmcon.comm.serialPort");
  }

  boolean running = false;

  public void startControlling()
  {
    log.debug("start controlling");

    running = true;

    // start the animation thread

    new Thread()
    {
      public void run()
      {
        try
        {
          repaint();
          mLastUpdate = Calendar.getInstance();

          // while still running

          while (running)
          {
            // delay until it's time for the next update

            sleep(Math.max(0, MIN_FRAME_DELAY -
              (currentTimeMillis() - mLastUpdate.getTimeInMillis())));

            // get now

            Calendar now = Calendar.getInstance();

            // update with the difference between now and last update

            synchronized (mVisualObjects)
            {
              update(millisecondsToSeconds(now.getTimeInMillis() -
                mLastUpdate.getTimeInMillis()));
            }

            // establish the time since last update

            mLastUpdate = now;
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }.start();
  }

  public void stopControlling()
  {
    running = false;
    log.debug("stop controlling");
  }

  // update the world

  public void update(double time)
  {
    // update all the objects

    for (IOrb orb: mSwarm)
      orb.update(time);

    // repaint the screen

    mArena.repaint();
  }

  /** Create the swarm and the orbs it is composed of. */

  public void createOrbs()
  {
    IRenderable mouseDot = new MouseMobject(mArena);
    mVisualObjects.add(mouseDot);

    // create the orbs

    log.debug("Creating orbs: ");

    // compute the time offset between orbs for orbs to request
    // position updates, so as not to overload the network TODO: fix this

    double positionPollPeriodDelta = 200;
    double positionPollPeriodOffset = 0;

    // now create each orb

    for (int id = 0; id < MAX_ORB_COUNT; ++id)
    {
      // report the birth of an orb

      log.debug("id: " + id + " mode: " + (mLiveMode
        ? "LIVE"
        : "SIM"));

      // create the orb model

      AMotionModel model = mLiveMode
        ? new LiveModel(mOrbIo, id, positionPollPeriodOffset)
        : new SimModel();

      positionPollPeriodOffset += positionPollPeriodDelta;

      // get the controllers for the new orb, but we're not doing
      // anything with them at the moment

      Orb orb = new Orb(model, id);

      // register the new orb or orb IO so it can get messages

      if (mOrbIo != null)
        mOrbIo.register(orb);

      // add the orb to the swarm

      mSwarm.add(orb);

      // if in simulation mode, add behaviors

      if (!mLiveMode)
      {
        Behavior nb = new NoBehavior();
        Behavior fb = new FollowBehavior(mouseDot);
        Behavior wb = new WanderBehavior();
        Behavior rb = new RandomBehavior();
        Behavior cb = new ClusterBehavior(mSwarm);
        orb.add(wb);
        orb.add(rb);
        orb.add(cb);
        orb.add(fb);
        orb.add(nb);
      }

      // record previous for the follow behavior

      mouseDot = orb;
    }

    // if in live mode, initialization the swarm origin

    if (mLiveMode)
    {
      // new Thread()
      // {
      // public void run()
      // {
      // JOptionPane.showMessageDialog(
      // activeSwarmCon, "Surveying Orbs, see console for progress.");
      // }
      // }.start();

      initSwarmOrigin();
    }
  }

  // collect orb survey positions and inform the orbs what they should
  // all use for the swarm origin (0, 0). this process can take a
  // while (up to 60 seconds) if the orbs have not completed their
  // debiasing process. this method blocks until the process is
  // complete.

  public void initSwarmOrigin()
  {
    try
    {
      // extract just the orbs from the swarm

      Vector<Orb> orbs = new Vector<Orb>();
      for (IRenderable m : mSwarm)
        if (m instanceof Orb)
          orbs.add((Orb)m);

      // create a place to put survey results

      HashMap<Orb, Point2D> results = new HashMap<Orb, Point2D>();

      // keep going until we have has many results as we do orbs

      while (results.size() < orbs.size())
      {
        // walk through orbs

        for (Orb orb : orbs)
        {
          // if we don't have a survey result

          if (results.get(orb) == null)
          {
            IMotionModel mm = orb.getModel();

            // if we got a result since we last checked, record that

            Point2D p = mm.getSurveyPosition();

            if (p != null)
            {
              results.put(orb, p);
              log.debug("survey result from orb [" + orb.getId() + "]: " + p);
            }

            // otherwise request a report

            else
            {
              mOrbIo.requestSurvayPosition(orb.getId());
              log.debug("survey request to orb [" + orb.getId() + "]");
            }
          }

          // sleep a little between each orb to give the zigbee a break

          Thread.sleep(mPositionPollPeriod / orbs.size());
        }
      }

      // now we have ALL the survey results compute the centroid of
      // the orbs

      Point2D centroid = new Point2D.Double();
      for (Point2D p : results.values())
        centroid.setLocation(centroid.getX() + p.getX(),
          centroid.getY() + p.getY());
      centroid.setLocation(centroid.getX() / results.values().size(),
        centroid.getY() / results.values().size());

      // now inform all the orbs of the new origin

      while (results.size() > 0)
      {
        for (IOrb orb : orbs)
        {
          // if we're not done with this orb yet

          if (results.get(orb) != null)
          {
            IMotionModel mm = orb.getModel();

            // if we got an acknowledged, this orb is done

            if (mm.isOriginAcked())
            {
              results.remove(orb);
              log.debug("origin ack from orb [" + orb.getId() + "]: " +
                centroid);
            }

            // otherwise send the origin to the orb

            else
            {
              mOrbIo.commandOrigin(orb.getId(), centroid);
              log.debug("sent origin to orb [" + orb.getId() + "]: " +
                centroid);
            }

            // sleep a little between each orb to give the zigbee a break

            Thread.sleep(mPositionPollPeriod / orbs.size());
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

  /**
   * Convert milliseconds to decimal seconds.
   * 
   * @param milliseconds you know milliseconds
   * @return decimal seconds.
   */

  public static double millisecondsToSeconds(long milliseconds)
  {
    return milliseconds / 1000d;
  }

  /**
   * Convert decimal seconds to milliseconds.
   * 
   * @param decimal seconds
   * @return you know milliseconds
   */

  public static long secondsToMilliseconds(double seconds)
  {
    return (long)(seconds * 1000);
  }

  /**
   * Compute the time now.
   * 
   * @return the current time in seconds since 1970 as double
   */

  public static double getTime()
  {
    return Calendar.getInstance().getTimeInMillis() / 1000d;
  }

  public void repaint()
  {
    mArena.repaint();
  }

  public JLabel createBigLabel(String text, Color color)
  {
    JLabel label = new JLabel(text);
    label.setForeground(color);
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.setFont(label.getFont().deriveFont(LABEL_FONT_SIZE));
    return label;
  }

  public Image createTitledShape(int width, int height, Shape shape,
    String title, Color diskColor, Color textColor)
  {
    return createTitledShape(width, height, shape, title, diskColor,
      textColor, null);
  }

  public Image createTitledShape(int width, int height, Shape shape,
    String title, Color diskColor, Color textColor, Font font)
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
    Rectangle2D tBounds =
      g.getFont().createGlyphVector(g.getFontRenderContext(), title)
        .getVisualBounds();

    // paint the text

    g.setColor(textColor);
    int x = (int)((width - tBounds.getWidth()) / 2) - 1;
    int y = (int)((height - tBounds.getHeight()) / 2);
    g.drawString(title, x, y + (int)tBounds.getHeight());

    return image;
  }

  /**
   * Place GUI objects into frame.
   * 
   * @param frame container to put stuff into
   * @return weather or not create the orbs after done with this or if orb
   *         creation will be handled by the splash screen code.
   */

  public boolean constructFrame()
  {
    Container frame = getContentPane();

    // frame closes on exit

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // set frame to box layout

    frame.setLayout(new BorderLayout());

    // paint area mouse listener

    MouseInputAdapter mia = new SwarmMia();

    // create center Panel

    mCenterPanel = new JPanel();
    mCardLayout = new CardLayout();
    mCenterPanel.setLayout(mCardLayout);
    mCenterPanel.setBorder(BorderFactory.createLineBorder(Color.gray));

    // identify if we need a splash panel

    boolean splashNeeded = true;
    if (mSerialPortId.equalsIgnoreCase(SIMULATION))
      splashNeeded = false;
    else
      for (String portId : SerialIo.listSerialPorts())
        if (mSerialPortId.equals(portId))
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
      for (String portId : SerialIo.listSerialPorts())
      {
        button = new BigButton(new SwarmComPortAction(portId));
        splash.add(button);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setAlignmentY(Component.CENTER_ALIGNMENT);
      }
      splash.add(Box.createVerticalGlue());
      mCenterPanel.add(splash, "splash");
    }

    // intermediary panel to put the arena and control UIs side-by-side

    mActionPanel = new JPanel();
    mActionPanel.setLayout(new GridBagLayout());

    // setup paint area

    mArena.addMouseMotionListener(mia);
    mArena.addMouseListener(mia);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1d;
    gbc.weighty = 1d;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    mActionPanel.add(mArena, gbc);

    mCenterPanel.add(mActionPanel, "arena");
    frame.add(mCenterPanel, BorderLayout.CENTER);

    // add actions

    InputMap inputMap = getRootPane().getInputMap();
    ActionMap actionMap = getRootPane().getActionMap();
    for (SwarmAction a : actions)
    {
      inputMap.put(a.getAccelerator(), a.getName());
      actionMap.put(a.getName(), a);
    }
    // add menu

    JMenuBar menuBar = new JMenuBar();
    menuBar.setBorder(null);
    setJMenuBar(menuBar);
    JMenu fileMenu = new JMenu("file");
    fileMenu.setForeground(MENU_CLR);
    fileMenu.setFont(MENU_FONT);
    menuBar.add(fileMenu);
    for (SwarmAction a : actions)
    {
      JMenuItem menu = new JMenuItem(a);
      menu.setFont(MENU_FONT);
      menu.setForeground(MENU_CLR);
      fileMenu.add(menu);
    }

    // if no splash needed go ahead and start the system

    if (!splashNeeded)
    {
      (new SwarmComPortAction(mSerialPortId)).actionPerformed(null);
    }

    // if no splash needed then create the orbs in the follow on procedure

    return !splashNeeded;
  }

  /** Establish the pattern of phantoms on the screen. */

  public void configurePhantoms()
  {
    int count = mPhantoms.size();

    // compute 90 % of minimum dimension which is the maximum
    // size to take up

    double maxSize = 10;
//      min(mArena.getWidth(), mArena.getHeight()) * 0.9d / mPixelsPerMeter;
    // TODO: fix

    // find the center of the arena

    Point2D.Double center =
      new Point2D.Double(-mGlobalOffset.getX(), -mGlobalOffset.getY());

    // if we've got 1 orb, size it real big

    if (count == 1)
    {
      Phantom p = mPhantoms.get(0);
      p.setTarget(center, maxSize / ORB_DIAMETER);
    }
    else if (count > 1)
    {
      double size = ORB_DIAMETER;
      double scale = maxSize / ((3 * size) - (size / 4 * (6 - count)));
      double radius = scale * size;
      double dAngle = 2 * PI / mPhantoms.size();
      double angle = 0;
      for (Phantom p : mPhantoms)
      {
        p.setTarget(new Point2D.Double(center.getX() + cos(angle) * radius,
          center.getY() + sin(angle) * radius), scale);
        angle += dAngle;
      }
    }
  }

  // randomize position of items in swarm

  public void resetSwarm()
  {
    Rectangle2D.Double range = new Rectangle2D.Double(-3, -3, 6, 6);
    for (IOrb orb : mSwarm)
      randomizePos(orb, range);
  }

  // randomize position of orb

  public void randomizePos(IOrb orb, Rectangle2D range)
  {
    // keep the initial positions within a smaller bounding box
    double boundX = Math.min(10., range.getWidth());
    double boundY = Math.min(10., range.getHeight());
    orb.setPosition(range.getX() + RND.nextDouble() * boundX, range.getY() +
      RND.nextDouble() * boundY);
  }

  /**
   * Paint all objects in arena.
   * 
   * @param baseTransform
   * @param graphics graphics object to paint onto
   */

  public void paintArena(Graphics2D g, AffineTransform baseTransform)
  {
    // render vobjects

    for (IRenderable renderable: mVisualObjects)
      RendererSet.render(g, renderable);

    // return to a pristine transform to draw text onto

    g.setTransform(baseTransform);

    // paint frame rate

    g.setColor(TEXT_CLR);
    g.setFont(MISC_FONT);
    g.drawString("frame rate: " +
      (currentTimeMillis() - mLastPaint.getTimeInMillis()) + "ms", 5, 15);
    mLastPaint = Calendar.getInstance();

    // draw current behavior

    int id = 1;
    for (IOrb orb : mSwarm)
    {
      Behavior behavior = orb.getBehavior();

      g.setColor(TEXT_CLR);
      g.setFont(MISC_FONT);
      g.drawString(orb.getId() +
        ": " +
        (behavior != null
          ? behavior.toString()
          : "[none]") +
        " X: " +
        Constants.UTM_FORMAT.format(orb.getX()) +
        " Y: " +
        Constants.UTM_FORMAT.format(orb.getY()) +
        " R: " +
        Constants.HEADING_FORMAT.format(round(orb.getRoll().as(HEADING))) +
        " P: " +
        Constants.HEADING_FORMAT.format(round(orb.getPitch().as(HEADING))) +
        " Y: " +
        Constants.HEADING_FORMAT.format(round(orb.getYaw().as(HEADING))) +
        " YR: " +
        Constants.HEADING_FORMAT.format(round(orb.getYawRate()
          .as(DEGREE_RATE))) + " V: " + round(orb.getSpeed() * 100) / 100d,
        5, 15 + id++ * 15);
    }

  }

  public BufferedImage captureImage(JComponent component)
  {
    Rectangle bounds = component.getBounds();
    java.awt.Point point = new java.awt.Point(bounds.x, bounds.y);
    SwingUtilities.convertPointToScreen(point, component);
    bounds.setBounds(point.x, point.y, bounds.width, bounds.height);
    return mRobot.createScreenCapture(bounds);
  }

  /**
   * Capture the area from a given component and write it out to the home
   * director of the user.
   * 
   * @param component the component to collect the image from
   */

  public void captureAndStoreImage(JComponent component)
  {
    try
    {
      File file =
        File.createTempFile("swarmcon", ".png", new File(System
          .getProperty("user.home")));
      ImageIO.write(captureImage(mArena), "png", file);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /** Capture the current area and write it to an image file. */

  public void captureArena()
  {
    log.debug("image capture.");
    captureAndStoreImage(mArena);
  }

  public void setCommandRefreshDelay(long commandRefreshDelay)
  {
    mCommandRefreshDelay = commandRefreshDelay;
  }

  public long getCommandRefreshDelay()
  {
    return mCommandRefreshDelay;
  }

  // object which is always set to the position of the mouse

  public class MouseMobject extends APositionable implements IRenderable
  {
    // construct a MouseMobject

    public MouseMobject(Component arena)
    {
      MouseInputAdapter mia = new MouseInputAdapter()
      {
        public void mouseMoved(MouseEvent e)
        {
          MouseMobject.this.setPosition(mArena.screenToWorld(e.getPoint()));
        }
      };

      arena.addMouseListener(mia);
      arena.addMouseMotionListener(mia);
    }

    /**
     * Is the given point (think mouse click point) eligable to select this
     * object?
     * 
     * @param clickPoint the point where the mouse was clicked
     */

    public boolean isSelectedBy(Point2D.Double clickPoint)
    {
      return false;
    }

    // update position of this object

    public void update(double time)
    {
    }
  }

  /** swarm mouse input adapter */

  class SwarmMia extends MouseInputAdapter
  {
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
      Point2D worldPos = mArena.screenToWorld(e.getPoint());
      final IRenderable selected = RendererSet.getSelected(worldPos, mSwarm);

      if (selected != null)
      {
        orbToCommand = (Orb)selected;
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

    private void selectOrb(MouseEvent e)
    {
      // find nearest selectable mobject

      final IRenderable selected = RendererSet.getSelected(mArena.screenToWorld(e.getPoint()), mSwarm);

      // if shift is not down, clear selected

      if (!e.isShiftDown())
      {
        //mSelected.setSelected(false);
        mSelected.clear();
        mPhantoms.clear();
      }

      // if nearest found, ad to selected set

      if (selected != null)
      {
        // set selected

        selected.setSelected(true);

        // add to selected mobjects

        mSelected.add(selected);

        // add phantom for this mobject

        Phantom p = new Phantom(selected, PHANTOM_PERIOD);
        mPhantoms.add(p);

        // tell the phantoms to reconfigure themselves

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

    /**
     * Return accelerator key for this action.
     * 
     * @return accelerator key for this action
     */

    public KeyStroke getAccelerator()
    {
      return (KeyStroke)getValue(ACCELERATOR_KEY);
    }

    /**
     * Return name of this action.
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

    /**
     * Construct SwarmComPortAction with a given com port id.
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
          mOrbIo = new OrbIo(portId);
          mOrbControlImpl.setOrbIo(mOrbIo);
          mLiveMode = true;
        }
        else
          mLiveMode = false;

        mCardLayout.last(mCenterPanel);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  /** Action to select simulated rather live operation. */

  SwarmAction simulation =
    new SwarmAction("simulate orbs", getKeyStroke(VK_S, 0),
      "simulate orb motion rather then connect to live orbs")
    {
      public void actionPerformed(ActionEvent e)
      {
        mLiveMode = false;
        mCardLayout.last(mCenterPanel);
      }
    };

  /** Action to reset simulation state */

  SwarmAction reset =
    new SwarmAction("reset sim", getKeyStroke(VK_R, 0),
      "reset simulation state")
    {
      public void actionPerformed(ActionEvent e)
      {
        resetSwarm();
      }
    };

  /** action to select next orb behavior */

  SwarmAction nextBehavior =
    new SwarmAction("next behavior", getKeyStroke(VK_UP, 0),
      "select next orb behavior")
    {
      public void actionPerformed(ActionEvent e)
      {
        for (IOrb orb: mSwarm)
          orb.nextBehavior();
      }
    };

  /** action to select previous orb behavior */

  SwarmAction previousBehavior =
    new SwarmAction("previous behavior", getKeyStroke(VK_DOWN, 0),
      "select previous orb behavior")
    {
      public void actionPerformed(ActionEvent e)
      {
        for (IOrb orb: mSwarm)
          orb.previousBehavior();
      }
    };

  /** Zoom display in. */

  SwarmAction zoomIn =
    new SwarmAction("Zoom in", getKeyStroke(VK_MINUS, 0),
      "zoom in on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        mArena.zoomIn();
      }
    };

  /** Zoom display out. */

  SwarmAction zoomOut =
    new SwarmAction("Zoom out", getKeyStroke(VK_EQUALS, 0),
      "zoom out on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        mArena.zoomOut();
      }
    };

  /** Emergency stop all orbs. */

  SwarmAction emergencyStop =
    new SwarmAction("Emergency Stop", getKeyStroke(VK_SPACE, 0),
      "stop all the orbs now")
    {
      public void actionPerformed(ActionEvent e)
      {
        for (IRenderable mo : mSwarm)
          if (mo instanceof Orb)
            ((IOrb)mo).getModel().stop();
      }
    };

  /** Emergency stop all orbs. */

  SwarmAction testBlockPath =
    new SwarmAction("Test Block Path", getKeyStroke(VK_X, 0),
      "run a block path test")
    {
      public void actionPerformed(ActionEvent e)
      {
        IOrb orb = getOrb(0);
        orb.setPosition(mTestBlockPath.getPosition());
        orb.getModel().setTargetPath(mTestBlockPath);
      }
    };

  /** action to exist the system */

  SwarmAction exit =
    new SwarmAction("Exit", getKeyStroke(VK_ESCAPE, 0), "exit this program")
    {
      public void actionPerformed(ActionEvent e)
      {
        System.exit(0);
      }
    };

  /** screen capture */

  SwarmAction captureScreen =
    new SwarmAction("Capture Arena", getKeyStroke(VK_SPACE, SHIFT_MASK),
      "save an image of the arena to your home directory")
    {
      public void actionPerformed(ActionEvent e)
      {
        captureAndStoreImage(mArena);
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
      testBlockPath,
    };

  /** a convenience class for a really big button */

  private class BigButton extends JButton
  {
    public BigButton(AbstractAction action)
    {
      super(action);
      setFont(BUTTON_FONT);
      setForeground(BUTTON_CLR);
    }
  }
}
