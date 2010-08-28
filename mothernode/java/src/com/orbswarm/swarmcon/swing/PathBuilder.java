package com.orbswarm.swarmcon.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

import com.orbswarm.swarmcon.model.SimModel;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.orb.Swarm;
import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.store.FileStore;
import com.orbswarm.swarmcon.store.Item;
import com.orbswarm.swarmcon.store.IItem;
import com.orbswarm.swarmcon.store.IItemStore;
import com.orbswarm.swarmcon.util.Constants;
import com.orbswarm.swarmcon.util.NameGenerator;
import com.orbswarm.swarmcon.view.IRenderable;
import com.orbswarm.swarmcon.view.RendererSet;

import static com.orbswarm.swarmcon.util.Constants.MIN_FRAME_DELAY;
import static java.lang.Math.PI;
import static java.lang.Math.round;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;

@SuppressWarnings("serial")
public class PathBuilder extends JFrame
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(PathBuilder.class);

  // radius constants

  public static final double MINIMUM_RADIUS = 1;
  public static final double MAXIMUM_RADIUS = 7;
  public static final double START_RADIUS = 7;
  public static final double MINIMUM_LENGTH = 1;

  public static final double DEFAULT_RADIUS_STEP = 1;
  public static final double DEFAULT_RADIUS = 1;
  public static final double DEFAULT_CURVE_EXTENT = 30;

  // length constants

  public static final double DEFAULT_LENGTH = 3;
  public static final double DEFAULT_LENGTH_CHANGE = 1;

  // default angle constants

  private static final double DEFAULT_ANGLE_QUANTA = 30;
  private static final double DEFAULT_RADIUS_QUANTA = 0.5;
  private static final double DEFAULT_LENGTH_QUANTA = 1;

  // private static final double DEFAULT_CURVE_EXTENT = 90;

  private double mCurrentRadius = DEFAULT_RADIUS;
  private double mCurrentLength = DEFAULT_LENGTH;
  private double mCurrentCurveExtent = DEFAULT_CURVE_EXTENT;

  private IItem<?> mArtifact;

  protected final Swarm mSwarm;
  
  protected ArenaPanel mArena;

  protected JMenu mEditMenu;

  protected JMenuBar mMenuBar;

  protected JMenu mFileMenu;

  protected JMenu mViewMenu;

  private IItemStore mStore;

  private boolean mSimulationRunning;

  public static void main(String[] args)
  {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
      "Arena Test");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    new PathBuilder(new FileStore("/tmp/store"));
  }

  // construct a swarm

  public PathBuilder(IItemStore store)
  {
    // get the item store

    mStore = store;

    // make a swarm
    
    mSwarm = new Swarm();
    
    // make the frame

    constructFrame();

    // initialize the object

    createNewArtifact();

    // show the frame

    pack();
    setSize(800, 600);
    setVisible(true);
    repaint();
  }

  /**
   * Place GUI objects into frame.
   */

  protected void constructFrame()
  {
    Container frame = getContentPane();

    // frame closes on exit

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // set frame to box layout

    frame.setLayout(new BorderLayout());

    // make menu bar

    mMenuBar = new JMenuBar();
    setJMenuBar(mMenuBar);

    // make edit menu

    mFileMenu = new JMenu("File");
    mMenuBar.add(mFileMenu);
    mFileMenu.add(mNewAction);
    mFileMenu.addSeparator();
    mFileMenu.add(mSaveAction);
    mFileMenu.add(mLoadAction);
    mFileMenu.addSeparator();
    mFileMenu.add(mDanceAction);

    // make edit menu

    mEditMenu = new JMenu("Edit");
    mMenuBar.add(mEditMenu);
    mEditMenu.add(mCurveLeftAction);
    mEditMenu.add(mGoStraightAction);
    mEditMenu.add(mCurveRightAction);
    mEditMenu.add(mDeleteBlockAction);
    mEditMenu.addSeparator();
    mEditMenu.add(mLengthenAction);
    mEditMenu.add(mShortenAction);
    mEditMenu.add(mShiftRight);
    mEditMenu.add(mShiftLeft);
    mEditMenu.addSeparator();
    mEditMenu.add(mNextBlock);
    mEditMenu.add(mPreviouseBlock);

    // make view menu

    mViewMenu = new JMenu("View");
    mMenuBar.add(mViewMenu);
    mViewMenu.add(mZoomIn);
    mViewMenu.add(mZoomOut);

    // add drawing area

    mArena = new ArenaPanel()
    {
      public void paint(Graphics graphics)
      {
        Graphics2D g = (Graphics2D)graphics;
        super.paint(g);
        RendererSet.render(g, getArtifact());
        RendererSet.render(g, mSwarm);
      }
    };

    frame.add(mArena, BorderLayout.CENTER);
  }

  @Override
  public void repaint()
  {
    mArena.setViewPort(getArtifact().getBounds2D(),
      Constants.ARENA_VIWPORT_BORDER);
    super.repaint();
  }

  protected void save()
  {
    mStore.update(mArtifact);
  }

  void dance()
  {
    if (!mSimulationRunning)
    {
      getArtifact().setSuppressed(true);
      createSwarm();
      startSimulation();
    }
    else
    {
      stopSimulation();
      mSwarm.clear();
      getArtifact().setSuppressed(false);
      repaint();
    }
  }

  protected void createSwarm()
  {
    IOrb orb = new Orb(new SimModel(), 0);
    mSwarm.add(orb);
    orb.setHeading(getCurrentPath().getHeading());
    orb.setPosition(getCurrentPath().getPosition());
    orb.getModel().setTargetPath(getCurrentPath());
  }
  
  public void stopSimulation()
  {
    mSimulationRunning = false;
  }
  
  public void startSimulation()
  {
    mSimulationRunning = true;

    // start the animation thread

    new Thread()
    {
      private Calendar mLastUpdate;

      public void run()
      {
        try
        {
          repaint();
          mLastUpdate = Calendar.getInstance();

          // while still running

          while (mSimulationRunning)
          {
            // delay until it's time for the next update

            sleep(Math.max(0, MIN_FRAME_DELAY -
              (currentTimeMillis() - mLastUpdate.getTimeInMillis())));

            // if simulation stopped, stop this non-sense
            
            if (!mSimulationRunning)
              break;
            
            // get now

            Calendar now = Calendar.getInstance();

            // update with the difference between now and last update

            synchronized (mSwarm)
            {
              mSwarm.update(SwarmCon.millisecondsToSeconds(now.getTimeInMillis() -
                mLastUpdate.getTimeInMillis()));
              repaint();
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
    
  protected void load()
  {
    JDialog box = new JDialog(this, "Select Item", true);
    Container frame = box.getContentPane();
    ItemSelecterPanel selector = new ItemSelecterPanel(mStore, box);
    frame.add(selector);
    box.pack();
    box.setVisible(true);
    IItem<?> item = selector.getSelectedItem();
    if (null != item)
      mArtifact = item;
    repaint();
  }

  private void oldCurveRight()
  {
    log.debug("curveRight");
    if (null == getCurrentBlock())
      return;

    if (getCurrentBlock() instanceof StraightBlock)
    {
      getCurrentPath().replace(
        convertToCurve((StraightBlock)getCurrentBlock(),
          CurveBlock.Type.RIGHT));
      repaint();
    }
    else if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();

      if (cb.getType() == CurveBlock.Type.LEFT)
      {
        double radius = computeRadius(cb.getRadius(), true);
        if (radius < MAXIMUM_RADIUS)
          setRadiusFixLength(cb, radius);
        else
          getCurrentPath().replace(convertToStraight(cb));
      }
      else
      {
        double radius = computeRadius(cb.getRadius(), false);
        if (radius >= MINIMUM_RADIUS)
          setRadiusFixLength(cb, Math.max(radius, MINIMUM_RADIUS));
      }

      repaint();
    }
  }

  private void curveLeft()
  {
    IBlock b = getCurrentBlock();

    if (b instanceof CurveBlock &&
      ((CurveBlock)b).getType() == CurveBlock.Type.LEFT)
    {
      CurveBlock cb = (CurveBlock)b;
      cb.setExtent(cb.getExtent() + DEFAULT_ANGLE_QUANTA);
    }
    else
    {
      getCurrentPath().addAfter(
        new CurveBlock(DEFAULT_CURVE_EXTENT, DEFAULT_RADIUS,
          CurveBlock.Type.LEFT));
    }

    repaint();
  }
  
  private void curveRight()
  {
    IBlock b = getCurrentBlock();

    if (b instanceof CurveBlock &&
      ((CurveBlock)b).getType() == CurveBlock.Type.RIGHT)
    {
      CurveBlock cb = (CurveBlock)b;
      cb.setExtent(cb.getExtent() + DEFAULT_ANGLE_QUANTA);
    }
    else
    {
      getCurrentPath().addAfter(
        new CurveBlock(DEFAULT_CURVE_EXTENT, DEFAULT_RADIUS,
          CurveBlock.Type.RIGHT));
    }

    repaint();
  }

  private void embiggen()
  {
    IBlock b = getCurrentBlock();

    if (b instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)b;
      sb.setLength(sb.getLength() + DEFAULT_LENGTH_QUANTA);
    }
    else
    {
      getCurrentPath().addAfter(new StraightBlock(DEFAULT_LENGTH_QUANTA));
    }

    repaint();
  }
  
  

  private void oldCurveLeft()
  {
    log.debug("curveLeft");
    if (null == getCurrentBlock())
      return;

    if (getCurrentBlock() instanceof StraightBlock)
    {
      getCurrentPath()
        .replace(
          convertToCurve((StraightBlock)getCurrentBlock(),
            CurveBlock.Type.LEFT));
      repaint();
    }
    else if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();

      if (cb.getType() == CurveBlock.Type.RIGHT)
      {
        double radius = computeRadius(cb.getRadius(), true);
        if (radius < MAXIMUM_RADIUS)
          setRadiusFixLength(cb, radius);
        else
          getCurrentPath().replace(convertToStraight(cb));
      }
      else
      {
        double radius = computeRadius(cb.getRadius(), false);
        if (radius >= MINIMUM_RADIUS)
          setRadiusFixLength(cb, Math.max(radius, MINIMUM_RADIUS));
      }

      repaint();
    }
  }

  public void setRadiusFixLength(CurveBlock cb, double radius)
  {
    double dLength =
      cb.getLength() - CurveBlock.getArcLength(cb.getExtent(), radius);
    double dExtent = 360 * (dLength / (2 * PI * radius));
    cb.setRadius(radius);
    cb.setExtent(quantize(cb.getExtent() + dExtent, DEFAULT_ANGLE_QUANTA));
  }

  public static double quantize(double value, double quanta)
  {
    return round(value / quanta) * quanta;
  }

  private double computeRadius(double radius, boolean increase)
  {
    double exponent = 1.1;
    double result = (increase
      ? Math.pow(radius, exponent)
      : Math.pow(radius, 1 / exponent));

    log.debug(String.format(" pre quantize: %f", result));
    result = quantize(result, DEFAULT_RADIUS_QUANTA);
    log.debug(String.format("post quantize: %f", result));

    if (result == radius)
    {
      if (increase)
        result += DEFAULT_RADIUS_QUANTA;
      if (!increase && result > DEFAULT_RADIUS_QUANTA)
        result -= DEFAULT_RADIUS_QUANTA;
    }

    log.debug(String
      .format("%f -> %f: increase %s", radius, result, increase));
    return result;
  }

  private void previouseBlock()
  {
    getCurrentPath().previouseBlock();
    repaint();
  }

  protected void nextBlock()
  {
    getCurrentPath().nextBlock();
    repaint();
  }

  private void oldEmbiggen()
  {
    if (null == getCurrentBlock())
      return;

    if (getCurrentBlock() instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)getCurrentBlock();
      sb.setLength(sb.getLength() + DEFAULT_LENGTH_CHANGE);
      repaint();
    }
    else if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      cb.setExtent(min(cb.getExtent() + DEFAULT_ANGLE_QUANTA, 360));
      repaint();
    }
  }

  private void ensmallen()
  {
    if (null == getCurrentBlock())
      return;

    if (getCurrentBlock() instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)getCurrentBlock();
      double newLength = sb.getLength() - DEFAULT_LENGTH_CHANGE;
      if (newLength < MINIMUM_LENGTH)
        getCurrentPath().remove();
      else
        sb.setLength(newLength);
      repaint();
    }
    else if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      double newExtent = cb.getExtent() - DEFAULT_ANGLE_QUANTA;
      if (newExtent < DEFAULT_ANGLE_QUANTA)
        getCurrentPath().remove();
      else
        cb.setExtent(newExtent);
      repaint();
    }
  }

  private static CurveBlock convertToCurve(StraightBlock sb,
    CurveBlock.Type type)
  {
    Angle extent =
      new Angle((2 * PI) * (sb.getLength() / (START_RADIUS * 2 * PI)),
        Type.RADIANS);
    double extentDegrees =
      round(extent.as(Angle.Type.DEGREES) / DEFAULT_ANGLE_QUANTA) *
        DEFAULT_ANGLE_QUANTA;

    return new CurveBlock(extentDegrees, START_RADIUS, type);
  }

  private static StraightBlock convertToStraight(CurveBlock cb)
  {
    return new StraightBlock(quantize(cb.getLength(), DEFAULT_LENGTH_QUANTA));
  }

  private void addBlock(IBlock block)
  {
    getCurrentPath().addAfter(block);
    repaint();
  }

  private void removeBlock()
  {
    getCurrentPath().remove();
    repaint();
  }

  protected void shortenSegment()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      double extent = cb.getExtent();
      if (extent > DEFAULT_ANGLE_QUANTA)
      {
        cb.setExtent(extent - DEFAULT_ANGLE_QUANTA);
        repaint();
      }
    }
    else if (getCurrentBlock() instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)getCurrentBlock();
      double length = sb.getLength();
      if (length > DEFAULT_LENGTH_CHANGE)
        sb.setLength(length - DEFAULT_LENGTH_CHANGE);
      repaint();
    }
  }

  protected void lengthenSegment()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      double extent = cb.getExtent();
      if (extent < 360)
      {
        cb.setExtent(extent + DEFAULT_ANGLE_QUANTA);
        repaint();
      }
    }
    else if (getCurrentBlock() instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)getCurrentBlock();
      sb.setLength(sb.getLength() + DEFAULT_LENGTH_CHANGE);
      repaint();
    }
  }

  protected void shiftLeft()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      if (cb.getType() == CurveBlock.Type.RIGHT)
        cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_STEP);
      else if (cb.getRadius() > MINIMUM_RADIUS)
        cb.setRadius(cb.getRadius() - DEFAULT_RADIUS_STEP);

      repaint();
    }
  }

  protected void shiftRight()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      if (cb.getType() == CurveBlock.Type.LEFT)
        cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_STEP);
      else if (cb.getRadius() > MINIMUM_RADIUS)
        cb.setRadius(cb.getRadius() - DEFAULT_RADIUS_STEP);

      repaint();
    }
  }

  public IBlock getCurrentBlock()
  {
    return getCurrentPath().getCurrentBlock();
  }

  public IBlockPath getCurrentPath()
  {
    return (IBlockPath)getArtifact();
  }

  protected void createNewArtifact()
  {
    setArtifact(new Item<BlockPath>(new BlockPath(), NameGenerator.getName(2)));
    getCurrentPath().addAfter(new StraightBlock(mCurrentLength));
    getCurrentPath().setSelected(true);
    repaint();
  }

  protected void setArtifact(IItem<?> artifact)
  {
    mArtifact = artifact;
  }

  public IRenderable getArtifact()
  {
    return mArtifact.getItem();
  }

  private SwarmAction mCurveLeftAction = new SwarmAction("Left",
    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "curve to the left")
  {
    public void actionPerformed(ActionEvent e)
    {
      curveLeft();
    }
  };

  private SwarmAction mCurveRightAction = new SwarmAction("Right",
    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "curve to the right")
  {
    public void actionPerformed(ActionEvent e)
    {
      curveRight();
    }
  };

  /** Action to select simulated rather live operation. */

  private SwarmAction mGoStraightAction = new SwarmAction("Straight",
    KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK),
    "add path block wich goes straight forward")
  {
    public void actionPerformed(ActionEvent e)
    {
      addBlock(new StraightBlock(mCurrentLength));
    }
  };

  /** Action to select simulated rather live operation. */

  private SwarmAction mDeleteBlockAction =
    new SwarmAction("Delete", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
      0), "delete current block")
    {
      public void actionPerformed(ActionEvent e)
      {
        removeBlock();
      }
    };

  /** Action to select simulated rather live operation. */

  private SwarmAction mShortenAction = new SwarmAction("Ensmallen",
    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
    "decrease the length of the block")
  {
    public void actionPerformed(ActionEvent e)
    {
      ensmallen();
    }
  };

  /** Action to select simulated rather live operation. */

  private final SwarmAction mLengthenAction = new SwarmAction("Embiggen",
    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
    "increase the length of the block")
  {
    public void actionPerformed(ActionEvent e)
    {
      embiggen();
    }
  };

  private final SwarmAction mShiftLeft = new SwarmAction("Left Adjust",
    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK),
    "widen right curves, narrow left curves")
  {
    public void actionPerformed(ActionEvent e)
    {
      shiftLeft();
    }
  };

  private final SwarmAction mShiftRight = new SwarmAction("Right Adjust",
    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK),
    "widen left curvs, narrow right curves")
  {
    public void actionPerformed(ActionEvent e)
    {
      shiftRight();
    }
  };

  private final SwarmAction mPreviouseBlock = new SwarmAction(
    "Previouse Block", KeyStroke.getKeyStroke(KeyEvent.VK_S, 0),
    "select previouse block on current path")
  {
    public void actionPerformed(ActionEvent e)
    {
      previouseBlock();
    }
  };

  protected final SwarmAction mNextBlock = new SwarmAction("Next Block",
    KeyStroke.getKeyStroke(KeyEvent.VK_W, 0),
    "select next block on current path")
  {
    public void actionPerformed(ActionEvent e)
    {
      nextBlock();
    }
  };

  private final SwarmAction mZoomIn = new SwarmAction("Zoom in",
    KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoom in on the display")
  {
    public void actionPerformed(ActionEvent e)
    {
      mArena.zoomIn();
    }
  };

  /** Zoom display out. */

  SwarmAction mZoomOut = new SwarmAction("Zoom out", KeyStroke.getKeyStroke(
    KeyEvent.VK_EQUALS, 0), "zoom out on the display")
  {
    public void actionPerformed(ActionEvent e)
    {
      mArena.zoomOut();
    }
  };

  /** Zoom display out. */

  SwarmAction mNewAction = new SwarmAction("New", KeyStroke.getKeyStroke(
    KeyEvent.VK_N, KeyEvent.META_DOWN_MASK), "create a new artifact to edit")
  {
    public void actionPerformed(ActionEvent e)
    {
      createNewArtifact();
    }
  };

  /** Zoom display out. */

  SwarmAction mSaveAction = new SwarmAction("Save", KeyStroke.getKeyStroke(
    KeyEvent.VK_S, KeyEvent.META_DOWN_MASK), "save this artifact")
  {
    public void actionPerformed(ActionEvent e)
    {
      save();
    }
  };

  SwarmAction mLoadAction = new SwarmAction("Load", KeyStroke.getKeyStroke(
    KeyEvent.VK_L, KeyEvent.META_DOWN_MASK), "load an artifact for editing")
  {
    public void actionPerformed(ActionEvent e)
    {
      load();
    }
  };
  
  SwarmAction mDanceAction = new SwarmAction("Dance", KeyStroke.getKeyStroke(
    KeyEvent.VK_D, KeyEvent.META_DOWN_MASK), "dance my pritties, dance!")
  {
    public void actionPerformed(ActionEvent e)
    {
      dance();
    }
  };
}
