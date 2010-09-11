package com.orbswarm.swarmcon.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
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
import com.orbswarm.swarmcon.path.Dance;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.store.FileStore;
import com.orbswarm.swarmcon.store.IItemFilter;
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

  public static final double DEFAULT_RADIUS = 1;
  public static final double DEFAULT_CURVE_EXTENT = 30;

  // length constants

  public static final double DEFAULT_LENGTH = 3;
  public static final double DEFAULT_LENGTH_CHANGE = 1;

  // default angle constants

  private static final double DEFAULT_ANGLE_QUANTA = 30;
  private static final double DEFAULT_RADIUS_QUANTA = 0.5;
  private static final double DEFAULT_LENGTH_QUANTA = 1;

  
  public static final IItemFilter PATH_FILTER = new IItemFilter()
  {
    public <T extends IRenderable> boolean accept(IItem<T> item)
    {
      return item.getItem() instanceof IBlockPath;
    }
  };
  
  public static final IItemFilter DANCE_FILTER = new IItemFilter()
  {
    public <T extends IRenderable> boolean accept(IItem<T> item)
    {
      return item.getItem() instanceof IDance;
    }
  };
  
  // private static final double DEFAULT_CURVE_EXTENT = 90;

  private double mCurrentLength = DEFAULT_LENGTH;

  private IItem<?> mArtifact;

  protected final Swarm mSwarm;

  protected ArenaPanel mArena;

  protected JMenu mEditMenu;

  protected JMenuBar mMenuBar;

  protected JMenu mFileMenu;

  protected JMenu mViewMenu;

  private IItemStore mStore;

  private boolean mAutoZoom;

  private boolean mSimulationRunning;

  private boolean mAutoRotate;

  private JMenu mLayoutMenu;

  public static void main(String[] args)
  {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
      "Mother Node");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    new PathBuilder(new FileStore("/Users/trebor/.swarmstore"));
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

    // create a new dance

    createDance();

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
    mFileMenu.add(mNewDanceAction);
    mFileMenu.add(mNewPathAction);
    mFileMenu.addSeparator();
    mFileMenu.add(mSaveAction);
    mFileMenu.add(mLoadDanceAction);
    mFileMenu.add(mLoadPathAction);
    mFileMenu.addSeparator();
    mFileMenu.add(mDanceAction);

    // make edit menu

    mEditMenu = new JMenu("Edit");
    mMenuBar.add(mEditMenu);
    mEditMenu.add(mLengthenAction);
    mEditMenu.add(mShortenAction);
    mEditMenu.add(mCurveLeftAction);
    mEditMenu.add(mCurveRightAction);
    mEditMenu.addSeparator();
    mEditMenu.add(mAddStraightBlockAction);
    mEditMenu.add(mAddLeftCurveAction);
    mEditMenu.add(mAddRightCurveAction);
    mEditMenu.addSeparator();
    mEditMenu.add(mDeleteBlockAction);
    mEditMenu.addSeparator();
    mEditMenu.add(mRightAdjustRadiusAction);
    mEditMenu.add(mLeftAdjustRadiusAction);
    mEditMenu.addSeparator();
    mEditMenu.add(mPreviouseBlockAction);
    mEditMenu.add(mNextBlockAction);
    mEditMenu.add(mPreviousePathAction);
    mEditMenu.add(mNextPathAction);
    mEditMenu.addSeparator();
    mEditMenu.add(mInsertPathAction);
    
    mEditMenu.addSeparator();
    mLayoutMenu = new JMenu("Set Layout");
    mEditMenu.add(mLayoutMenu);
    
    for (Dance.Layout layout: Dance.Layout.values())
    {
      final Dance.Layout finalLayout = layout;
      mLayoutMenu.add(new SwarmAction(layout.name(), null,
        "set dance layout to " + layout.name().toLowerCase())
      {
        public void actionPerformed(ActionEvent e)
        {
          getCurrentDance().setLayout(finalLayout);
          repaint();
        }
      });
    }
    
    // make view menu

    mViewMenu = new JMenu("View");
    mMenuBar.add(mViewMenu);
    mViewMenu.add(mZoomInAction);
    mViewMenu.add(mZoomOutAction);
//    mViewMenu.add(mRotateLeftAction);
//    mViewMenu.add(mRotateRightAction);
    mViewMenu.addSeparator();
    JCheckBoxMenuItem cmbiZoom = new JCheckBoxMenuItem(mAutoZoomAction);
    cmbiZoom.setSelected(true);
    setAutoZoom(cmbiZoom);
    mViewMenu.add(cmbiZoom);
    JCheckBoxMenuItem cmbiRotate = new JCheckBoxMenuItem(mAutoRotateAction);
    cmbiRotate.setSelected(false);
    setAutoRotate(cmbiRotate);
    mViewMenu.add(cmbiRotate);

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


  public boolean isAutoZoom()
  {
    return mAutoZoom;
  }
  
  private void setAutoZoom(JCheckBoxMenuItem cmbi)
  {
    mAutoZoom = cmbi.isSelected();
    mZoomInAction.setEnabled(!mAutoZoom);
    mZoomOutAction.setEnabled(!mAutoZoom);
  }

  public boolean isAutoRotate()
  {
    return mAutoRotate;
  }
  
  private void setAutoRotate(JCheckBoxMenuItem cmbi)
  {
    mAutoRotate = cmbi.isSelected();
    mRotateLeftAction.setEnabled(!mAutoRotate);
    mRotateRightAction.setEnabled(!mAutoRotate);
    if (!mAutoRotate && null != mArena)
      mArena.setViewAngle(new Angle());
  }

  @Override
  public void repaint()
  {
    IBlockPath path = getCurrentPath();
    
    if (isAutoRotate() && null != path)
      mArena.setViewAngle(path.getHeading().rotate(path.getFinalAngle()).difference(90,
        Type.DEGREES));

    if (isAutoZoom())
      mArena.setViewPort(getArtifact().getBounds2D(),
        Constants.ARENA_VIEWPORT_BORDER);

    super.repaint();
  }

  protected void save()
  {
    JOptionPane dialog =
      new JOptionPane(String.format("Save %s by %s", mArtifact.getName(),
        mArtifact.getAuthor()), JOptionPane.QUESTION_MESSAGE);
    String save = "Save";
    String cancel = "Cancel";
    dialog.setOptions(new Object[]
    {
      save, cancel
    });
    dialog.createDialog("Save").setVisible(true);
    if (dialog.getValue() == save)
      mStore.update(mArtifact);
  }

  void dance()
  {
    if (!mSimulationRunning)
    {
      mNewDanceAction.setEnabled(false);
      mNewPathAction.setEnabled(false);
      mLoadDanceAction.setEnabled(false);
      mLoadPathAction.setEnabled(false);
      getArtifact().setSuppressed(true);
      createSwarm();
      startSimulation();
    }
    else
    {
      stopSimulation();
      mSwarm.clear();
      getArtifact().setSuppressed(false);
      mNewDanceAction.setEnabled(true);
      mNewPathAction.setEnabled(true);
      mLoadDanceAction.setEnabled(true);
      mLoadDanceAction.setEnabled(true);
      repaint();
    }
  }

  protected void createSwarm()
  {
    if (getArtifact() instanceof IBlockPath)
    {
      IOrb orb = new Orb(new SimModel(), 0);
      mSwarm.add(orb);
      orb.setHeading(getCurrentPath().getHeading());
      orb.setPosition(getCurrentPath().getPosition());
      orb.getModel().setTargetPath(getCurrentPath());
    }
    else
    {
      int orbId = 0;

      for (IBlockPath path : getCurrentDance().getPaths())
      {
        IOrb orb = new Orb(new SimModel(), orbId++);
        mSwarm.add(orb);
        orb.setHeading(path.getHeading());
        orb.setPosition(path.getPosition());
        orb.getModel().setTargetPath(path);
      }
    }
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
              mSwarm.update(SwarmCon.millisecondsToSeconds(now
                .getTimeInMillis() - mLastUpdate.getTimeInMillis()));
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

  protected void load(IItemFilter filter)
  {
    JDialog box = new JDialog(this, "Select Item", true);
    Container frame = box.getContentPane();
    ItemSelecterPanel selector =
      new ItemSelecterPanel(mStore, filter, box);
    frame.add(selector);
    box.pack();
    box.setVisible(true);
    IItem<?> item = selector.getSelectedItem();
    if (null != item)
      setArtifact(item);
  }

  protected IBlockPath selectPath()
  {
    JDialog box = new JDialog(this, "Select Path", true);
    
    IItemFilter pathFilter = new IItemFilter()
    {
      public <T extends IRenderable> boolean accept(IItem<T> item)
      {
        return item.getItem() instanceof IBlockPath;
      }
    };

    Container frame = box.getContentPane();
    ItemSelecterPanel selector =
      new ItemSelecterPanel(mStore, pathFilter, box);
    frame.add(selector);
    box.pack();
    box.setVisible(true);
    IItem<?> item = selector.getSelectedItem();
    return (null != item) ? (IBlockPath)item.getItem() : null;
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
      addBlock(new CurveBlock(DEFAULT_CURVE_EXTENT, DEFAULT_RADIUS,
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

  @SuppressWarnings("unused")
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

  private void nextPath()
  {
    getCurrentDance().nextPath();
    repaint();
  }

  private void previousePath()
  {
    getCurrentDance().previousePath();
    repaint();
  }
  
  public IDance getCurrentDance()
  {
    return (IDance)getArtifact();
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

  @SuppressWarnings("unused")
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

  @SuppressWarnings("unused")
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

  protected void adjustRadiusLeft()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();

      if (cb.getType() == CurveBlock.Type.RIGHT)
        cb.setRadius(Math.max(MINIMUM_RADIUS, cb.getRadius() -
          DEFAULT_RADIUS_QUANTA));
      else
        cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_QUANTA);

      repaint();
    }
  }

  protected void adjustRadiusRight()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();

      if (cb.getType() == CurveBlock.Type.LEFT)
        cb.setRadius(Math.max(MINIMUM_RADIUS, cb.getRadius() -
          DEFAULT_RADIUS_QUANTA));
      else
        cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_QUANTA);

      repaint();
    }
  }

  public IBlock getCurrentBlock()
  {
    return getCurrentPath().getCurrentBlock();
  }

  public IBlockPath getCurrentPath()
  {
    IRenderable artifact = getArtifact();
    if (artifact instanceof IBlockPath)
      return (IBlockPath)artifact;
      
    return ((IDance)artifact).getCurrentPath();
  }

  protected void createBlockPath()
  {
    setArtifact(new Item<BlockPath>(new BlockPath(), NameGenerator.getName(2)));
    getCurrentPath().setSelected(true);
    repaint();
  }

  protected void createDance()
  {
    setArtifact(new Item<IDance>(new Dance(Dance.Layout.CIRLCE, 2), NameGenerator.getName(2)));
    for (int i = 0; i < 6; ++i)
      getCurrentDance().addAfter(new BlockPath());
    getCurrentPath().setSelected(true);

    repaint();
  }

  protected void setArtifact(IItem<?> artifact)
  {
    mArtifact = artifact;
    
    if (mArtifact.getItem() instanceof IBlockPath)
    {
      mPreviousePathAction.setEnabled(false);
      mNextPathAction.setEnabled(false);
      mLayoutMenu.setEnabled(false);
    }
    else if (mArtifact.getItem() instanceof IDance)
    {
      mPreviousePathAction.setEnabled(true);
      mNextPathAction.setEnabled(true);
      mLayoutMenu.setEnabled(true);
    }
    
    setTitle(mArtifact.getName() + " by " + mArtifact.getAuthor());
    repaint();
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

  /** Action to select simulated rather live operation. */

  private SwarmAction mAddStraightBlockAction = new SwarmAction(
    "Add Straight Block", KeyStroke.getKeyStroke(KeyEvent.VK_UP,
      KeyEvent.SHIFT_DOWN_MASK), "add path block wich goes straight forward")
  {
    public void actionPerformed(ActionEvent e)
    {
      addBlock(new StraightBlock(mCurrentLength));
    }
  };

  /** Action to select simulated rather live operation. */

  private SwarmAction mAddLeftCurveAction = new SwarmAction(
    "Add Left Curve Block", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      KeyEvent.SHIFT_DOWN_MASK), "add new curve left block")
  {
    public void actionPerformed(ActionEvent e)
    {
      addBlock(new CurveBlock(DEFAULT_CURVE_EXTENT, DEFAULT_RADIUS,
        CurveBlock.Type.LEFT));
    }
  };

  /** Action to select simulated rather live operation. */

  private SwarmAction mAddRightCurveAction = new SwarmAction(
    "Add Right Curve Block", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
      KeyEvent.SHIFT_DOWN_MASK), "add new curve right block")
  {
    public void actionPerformed(ActionEvent e)
    {
      addBlock(new CurveBlock(DEFAULT_CURVE_EXTENT, DEFAULT_RADIUS,
        CurveBlock.Type.RIGHT));
    }
  };

  private final SwarmAction mLeftAdjustRadiusAction = new SwarmAction(
    "Left Adjust Radius", KeyStroke.getKeyStroke(KeyEvent.VK_E, 0),
    "widen left curvs, narrow right curves")
  {
    public void actionPerformed(ActionEvent e)
    {
      adjustRadiusLeft();
    }
  };

  private final SwarmAction mRightAdjustRadiusAction = new SwarmAction(
    "Right Adjust Radius", KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0),
    "widen right curves, narrow left curves")
  {
    public void actionPerformed(ActionEvent e)
    {
      adjustRadiusRight();
    }
  };

  private final SwarmAction mPreviouseBlockAction = new SwarmAction(
    "Previouse Block", KeyStroke.getKeyStroke(KeyEvent.VK_S, 0),
    "select previouse block on current path")
  {
    public void actionPerformed(ActionEvent e)
    {
      previouseBlock();
    }
  };

  protected final SwarmAction mNextBlockAction = new SwarmAction(
    "Next Block", KeyStroke.getKeyStroke(KeyEvent.VK_W, 0),
    "select next block on current path")
  {
    public void actionPerformed(ActionEvent e)
    {
      nextBlock();
    }
  };

  /** Zoom display in. */

  private final SwarmAction mZoomInAction = new SwarmAction("Zoom in",
    KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoom in on the display")
  {
    public void actionPerformed(ActionEvent e)
    {
      mArena.zoomIn();
    }
  };

  /** Zoom display out. */

  SwarmAction mZoomOutAction = new SwarmAction("Zoom out",
    KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "zoom out on the display")
  {
    public void actionPerformed(ActionEvent e)
    {
      mArena.zoomOut();
    }
  };

  SwarmAction mRotateLeftAction = new SwarmAction("Rotate Left",
    KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0), "rotate view left")
  {
    public void actionPerformed(ActionEvent e)
    {
      mArena.rotateView(new Angle(-2, Type.HEADING_RATE));
    }
  };

  SwarmAction mRotateRightAction = new SwarmAction("Rotate Right",
    KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0), "rotate view right")
  {
    public void actionPerformed(ActionEvent e)
    {
      mArena.rotateView(new Angle(2, Type.HEADING_RATE));
    }
  };

  /** Toggle auto zoom display. */

  SwarmAction mAutoZoomAction = new SwarmAction("Auto Zoom",
    KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK),
    "toggle auto zoom mode")
  {
    public void actionPerformed(ActionEvent e)
    {
      setAutoZoom((JCheckBoxMenuItem)e.getSource());
      repaint();
    }
  };

  /** Toggle auto rotate display. */

  SwarmAction mAutoRotateAction = new SwarmAction("Auto Rotate",
    KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.META_DOWN_MASK),
    "toggle auto rotate mode")
  {
    public void actionPerformed(ActionEvent e)
    {
      setAutoRotate((JCheckBoxMenuItem)e.getSource());
      repaint();
    }
  };

  /** Create new artifact. */

  SwarmAction mNewDanceAction = new SwarmAction("New Dance", KeyStroke.getKeyStroke(
    KeyEvent.VK_N, KeyEvent.META_DOWN_MASK), "create a new dance to edit")
  {
    public void actionPerformed(ActionEvent e)
    {
      createDance();
    }
  };

  /** Create new artifact. */

  SwarmAction mNewPathAction = new SwarmAction("New Path",
    KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_DOWN_MASK |
      KeyEvent.SHIFT_DOWN_MASK), "create a new path to edit")
  {
    public void actionPerformed(ActionEvent e)
    {
      createBlockPath();
    }
  };
  
  /** Save the current artifact. */

  SwarmAction mSaveAction = new SwarmAction("Save", KeyStroke.getKeyStroke(
    KeyEvent.VK_S, KeyEvent.META_DOWN_MASK), "save this artifact")
  {
    public void actionPerformed(ActionEvent e)
    {
      save();
    }
  };

  /** Load an existing artifact. */

  SwarmAction mLoadDanceAction = new SwarmAction("Load Dance", KeyStroke.getKeyStroke(
    KeyEvent.VK_L, KeyEvent.META_DOWN_MASK), "load a dance for editing")
  {
    public void actionPerformed(ActionEvent e)
    {
      load(DANCE_FILTER);
    }
  };

  /** Load an existing artifact. */

  SwarmAction mLoadPathAction = new SwarmAction("Load Path",
    KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_DOWN_MASK |
      KeyEvent.SHIFT_DOWN_MASK), "load a path editing")
  {
    public void actionPerformed(ActionEvent e)
    {
      load(PATH_FILTER);
    }
  };

  /** Make the orbs dance */

  SwarmAction mDanceAction = new SwarmAction("Dance", KeyStroke.getKeyStroke(
    KeyEvent.VK_D, KeyEvent.META_DOWN_MASK), "dance my pritties, dance!")
  {
    public void actionPerformed(ActionEvent e)
    {
      dance();
    }
  };
  
  private final SwarmAction mPreviousePathAction = new SwarmAction(
    "Previouse Path", KeyStroke.getKeyStroke(KeyEvent.VK_A, 0),
    "select previouse path")
  {
    public void actionPerformed(ActionEvent e)
    {
      previousePath();
    }
  };

  private final SwarmAction mNextPathAction = new SwarmAction("Next Path",
    KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "select next path")
  {
    public void actionPerformed(ActionEvent e)
    {
      nextPath();
    }
  };
  
  private final SwarmAction mInsertPathAction = new SwarmAction("Insert Path",
    KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.META_DOWN_MASK),
    "insert blocks from another path into this path")
  {
    public void actionPerformed(ActionEvent e)
    {
      IBlockPath current = getCurrentPath();
      IBlockPath newPath = selectPath();
      if (null != newPath)
      {
        for (IBlock block : newPath.getBlocks())
          current.addAfter(block);
        repaint();
      }
    }
  };
}
