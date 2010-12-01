package com.orbswarm.swarmcon.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

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
import org.trebor.util.Rate;

import com.orbswarm.swarmcon.model.SimModel;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.Dance;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.performance.IPerformance;
import com.orbswarm.swarmcon.performance.Performance;
import com.orbswarm.swarmcon.performance.PerformanceFactory;
import com.orbswarm.swarmcon.store.FileStore;
import com.orbswarm.swarmcon.store.IItemFilter;
import com.orbswarm.swarmcon.store.Item;
import com.orbswarm.swarmcon.store.IItem;
import com.orbswarm.swarmcon.store.IItemStore;
import com.orbswarm.swarmcon.store.TestStore;
import com.orbswarm.swarmcon.util.Constants;
import com.orbswarm.swarmcon.util.NameGenerator;
import com.orbswarm.swarmcon.view.IRenderable;
import com.orbswarm.swarmcon.view.RendererSet;

import static com.orbswarm.swarmcon.util.Constants.MIN_FRAME_DELAY;
import static java.lang.Math.PI;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

@SuppressWarnings("serial")
public class Builder extends JFrame
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Builder.class);

  // default store path
  
  public static final String DEFALUT_STORE_PATH = System.getProperty("user.home") + File.separator + ".swarmstore";

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

  protected final List<IOrb> mSwarm;

  protected IPerformance mPerformance;
  
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
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    File pathStore = new File(DEFALUT_STORE_PATH);

    IItemStore store = null;

    // if the path store does not exist

    if (!pathStore.exists())
    {
      if (!pathStore.mkdir())
      {
        log
          .error("unable to create storage directory: " + pathStore +
            "\nUsing memory store which will NOT perminently store your items.");

        store = new TestStore();
      }
      else
        store = new FileStore(pathStore.toString());
    }
    else if (!pathStore.isDirectory())
    {
      log.error("path store is not a directory: " + pathStore +
        "\nUsing memory store which will NOT perminently store your items.");

      store = new TestStore();
    }
    else
      store = new FileStore(pathStore.toString());
    
    new Builder(store);
  }

  // construct a builder

  public Builder(IItemStore store)
  {
    // get the item store

    mStore = store;

    // make a swarm

    mSwarm = new Vector<IOrb>();

    // make the frame

    constructFrame();

    // create a new dance

    createDance();

    // show the frame

    pack();
    setExtendedState(MAXIMIZED_BOTH);
    setVisible(true);

    // be sure to repaint later to clean up view

    EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        repaint();
        if (mStore instanceof TestStore)
        {
          JOptionPane warning =
            new JOptionPane(
              "Unable to create a perminant store:\n\n   " +
                DEFALUT_STORE_PATH +
                "\n\nYou will be able to \"save\" and \"load\" paths and dances\n" +
                "but they will NOT persist beyond this session.  Talk to trebor\n" +
                "and he'll get it fix for you.", JOptionPane.WARNING_MESSAGE);

          warning.createDialog(Builder.this, "Temporary Storage Only!")
            .setVisible(true);
        }
      }
    });
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
    mFileMenu.add(mPerformAction);

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
    mEditMenu.add(mInsertPathAllAction);

    mEditMenu.addSeparator();
    mLayoutMenu = new JMenu("Set Layout");
    mEditMenu.add(mLayoutMenu);

    for (Dance.Layout layout : Dance.Layout.values())
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
    // mViewMenu.add(mRotateLeftAction);
    // mViewMenu.add(mRotateRightAction);
    mViewMenu.addSeparator();
    JCheckBoxMenuItem cmbiGrid = new JCheckBoxMenuItem(mDisplaGridAction);
    cmbiGrid.setSelected(true);
    setAutoZoom(cmbiGrid);
    mViewMenu.add(cmbiGrid);
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
        if (null != mPerformance)
        {
          RendererSet.render(g, mPerformance);
          //mPerformance = null;
        }
        for (IRenderable orb: mSwarm)
          RendererSet.render(g, orb);
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
      mArena.setViewAngle(path.getHeading().rotate(path.getEndPoint().getAngle())
        .difference(90, Type.DEGREES));

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

  protected void dance()
  {
    if (!mSimulationRunning)
    {
      for (SwarmAction action: mDisabledDuringDanceActions)
        action.setEnabled(false);
      mLayoutMenu.setEnabled(false);
      getArtifact().setSuppressed(true);
      createSwarm();
      startSimulation();
    }
    else
    {
      stopSimulation();
      mSwarm.clear();
      getArtifact().setSuppressed(false);
      for (SwarmAction action: mDisabledDuringDanceActions)
        action.setEnabled(true);
      mLayoutMenu.setEnabled(true);
      repaint();
    }
  }

  protected void perform()
  {
    if (mPerformance != null)
    {
      mPerformance = null;
      repaint();
      return;
    }

    double timeStep = 0.1;

    // establish a collection of paths

    List<IBlockPath> paths = null;
    if (getArtifact() instanceof IBlockPath)
    {
      paths = new Vector<IBlockPath>();
      paths.add(getCurrentPath());
    }
    else
      paths = getCurrentDance().getPaths();

    log.debug("paths size: " + paths.size());

    // establish a collections of rates and orbs

    List<IOrb> orbs = new Vector<IOrb>();
    List<Rate> rates = new Vector<Rate>();
    for (int i = 0; i < paths.size(); ++i)
    {
      rates.add(new Rate("Velocity", 0, Constants.ORB_MAX_SPEED,
        Constants.ORB_ACCELERATION));
      IOrb orb = new Orb(new SimModel(), i);
      orbs.add(orb);
      IBlockPath path = paths.get(i);
      log.debug("path: " + path.hashCode());
      orb.setHeading(path.getHeading());
      orb.setPosition(path.getPosition());
    }

    // create the performance

    mPerformance = new Performance();
    for (int i = 0; i < paths.size(); ++i)
    {
      PerformanceFactory.append(mPerformance, paths.get(i), orbs.get(i),
        rates.get(i), timeStep);
    }
    log.debug("performance: " + mPerformance);

    repaint();
  }
  
  protected void createSwarm()
  {
    Collection<IBlockPath> paths = null;

    if (getArtifact() instanceof IBlockPath)
    {
      paths = new Vector<IBlockPath>();
      paths.add(getCurrentPath());
    }
    else
      paths = getCurrentDance().getPaths();

    int orbId = 0;
    for (IBlockPath path : paths)
    {
      IOrb orb = new Orb(new SimModel(), orbId++);
      mSwarm.add(orb);
      orb.setHeading(path.getHeading());
      orb.setPosition(path.getPosition());
      orb.getModel().setTargetPath(path);
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
              double timeStep = SwarmCon.millisecondsToSeconds(now
                .getTimeInMillis() - mLastUpdate.getTimeInMillis());
              for (IOrb orb: mSwarm)
                orb.update(timeStep);
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
    ItemSelecterPanel selector = new ItemSelecterPanel(mStore, filter, box);
    frame.add(selector);
    box.pack();
    box.setVisible(true);
    IItem<?> item = selector.getSelectedItem();
    if (null != item)
    {
      try
      {
        item = item.clone();
      }
      catch (CloneNotSupportedException e)
      {
        e.printStackTrace();
      }
      setArtifact(item);
    }
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
    return (null != item)
      ? (IBlockPath)item.getItem()
      : null;
  }

  private void curveLeft()
  {
    IBlock b = getCurrentBlock();

    if (b instanceof CurveBlock &&
      ((CurveBlock)b).getType() == CurveBlock.Type.LEFT)
    {
      CurveBlock cb = (CurveBlock)b;
      getCurrentPath().replace(cb.setExtent(cb.getExtent() + DEFAULT_ANGLE_QUANTA));
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
      getCurrentPath().replace(cb.setExtent(cb.getExtent() + DEFAULT_ANGLE_QUANTA));
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
      getCurrentPath().replace(sb.setLength(sb.getLength() + DEFAULT_LENGTH_QUANTA));
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
    getCurrentPath().replace(cb.setExtent(quantize(cb.getExtent() + dExtent, DEFAULT_ANGLE_QUANTA)));
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
        getCurrentPath().replace(sb.setLength(newLength));
      repaint();
    }
    else if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      double newExtent = cb.getExtent() - DEFAULT_ANGLE_QUANTA;
      if (newExtent < DEFAULT_ANGLE_QUANTA)
        getCurrentPath().remove();
      else
        getCurrentPath().replace(cb.setExtent(newExtent));
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
        getCurrentPath().replace(cb.setExtent(extent - DEFAULT_ANGLE_QUANTA));
        repaint();
      }
    }
    else if (getCurrentBlock() instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)getCurrentBlock();
      double length = sb.getLength();
      if (length > DEFAULT_LENGTH_CHANGE)
        getCurrentPath().replace(sb.setLength(length - DEFAULT_LENGTH_CHANGE));
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
        getCurrentPath().replace(cb.setExtent(extent + DEFAULT_ANGLE_QUANTA));
        repaint();
      }
    }
    else if (getCurrentBlock() instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)getCurrentBlock();
      getCurrentPath().replace(
        sb.setLength(sb.getLength() + DEFAULT_LENGTH_CHANGE));
      repaint();
    }
  }

  protected void adjustRadiusLeft()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();

      if (cb.getType() == CurveBlock.Type.RIGHT)
        getCurrentPath().replace(cb.setRadius(Math.max(MINIMUM_RADIUS, cb.getRadius() -
          DEFAULT_RADIUS_QUANTA)));
      else
        getCurrentPath().replace(cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_QUANTA));

      repaint();
    }
  }

  protected void adjustRadiusRight()
  {
    if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();

      if (cb.getType() == CurveBlock.Type.LEFT)
        getCurrentPath().replace(cb.setRadius(Math.max(MINIMUM_RADIUS, cb.getRadius() -
          DEFAULT_RADIUS_QUANTA)));
      else
        getCurrentPath().replace(cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_QUANTA));

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
    setArtifact(new Item<IDance>(new Dance(Dance.Layout.CIRLCE, 2),
      NameGenerator.getName(2)));
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
      mInsertPathAllAction.setEnabled(false);
    }
    else if (mArtifact.getItem() instanceof IDance)
    {
      mPreviousePathAction.setEnabled(true);
      mNextPathAction.setEnabled(true);
      mLayoutMenu.setEnabled(true);
      mInsertPathAllAction.setEnabled(true);
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

  /** Toggle display graph. */

  SwarmAction mDisplaGridAction = new SwarmAction("Display Grid",
    KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.META_DOWN_MASK),
    "toggle display of background grid")
  {
    public void actionPerformed(ActionEvent e)
    {
      mArena.setPaintGrid(((JCheckBoxMenuItem)e.getSource()).isSelected());
      repaint();
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

  SwarmAction mNewDanceAction = new SwarmAction("New Dance",
    KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_DOWN_MASK),
    "create a new dance to edit")
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

  SwarmAction mLoadDanceAction = new SwarmAction("Load Dance",
    KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_DOWN_MASK),
    "load a dance for editing")
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

  /** Make the orbs perform */

  SwarmAction mPerformAction = new SwarmAction("Perform", KeyStroke.getKeyStroke(
    KeyEvent.VK_P, KeyEvent.META_DOWN_MASK), "create and diplay a performance from a given artifact")
  {
    public void actionPerformed(ActionEvent e)
    {
      perform();
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

  private final SwarmAction mInsertPathAction = new SwarmAction(
    "Insert Path", KeyStroke.getKeyStroke(KeyEvent.VK_I,
      KeyEvent.META_DOWN_MASK),
    "insert blocks from another path into this path")
  {
    public void actionPerformed(ActionEvent e)
    {
      IBlockPath current = getCurrentPath();
      IBlockPath newPath = selectPath();
      if (null != newPath)
      {
        try
        {
          for (IBlock block : newPath.getBlocks())
            current.addAfter(block.clone());
        }
        catch (CloneNotSupportedException e1)
        {
          e1.printStackTrace();
        }
        repaint();
      }
    }
  };

  private final SwarmAction mInsertPathAllAction = new SwarmAction(
    "Insert Path All", KeyStroke.getKeyStroke(KeyEvent.VK_I,
      KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
    "insert blocks from another path into all the paths in this dance")
  {
    public void actionPerformed(ActionEvent e)
    {
      IBlockPath newPath = selectPath();
      if (null != newPath)
      {
        IDance dance = getCurrentDance();

        try
        {
          for (IBlockPath path : dance.getPaths())
            for (IBlock block : newPath.getBlocks())
              path.addAfter(block.clone());
        }
        catch (CloneNotSupportedException e1)
        {
          e1.printStackTrace();
        }
        repaint();
      }
    }
  };

  private SwarmAction[] mDisabledDuringDanceActions =
  {
    mNewDanceAction,
    mNewPathAction,
    mLoadDanceAction,
    mLoadPathAction,
    mCurveRightAction,
    mCurveLeftAction,
    mDeleteBlockAction,
    mShortenAction,
    mLengthenAction,
    mAddRightCurveAction,
    mAddLeftCurveAction,
    mAddStraightBlockAction,
    mNextPathAction,
    mPreviousePathAction,
    mNextBlockAction,
    mPreviouseBlockAction,
    mLeftAdjustRadiusAction,
    mRightAdjustRadiusAction,
    mInsertPathAction,
    mInsertPathAllAction,
  };
}
