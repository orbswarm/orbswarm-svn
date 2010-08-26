package com.orbswarm.swarmcon.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.store.FileStore;
import com.orbswarm.swarmcon.store.IItemStore;
import com.orbswarm.swarmcon.util.Constants;
import com.orbswarm.swarmcon.view.IRenderable;
import com.orbswarm.swarmcon.view.RendererSet;

import static java.lang.Math.PI;
import static java.lang.Math.round;
import static java.lang.Math.max;
import static java.lang.Math.min;

@SuppressWarnings("serial")
public class BlockPathBuilder extends JFrame
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPathBuilder.class);

  // radius constants

  public static final double MINIMUM_RADIUS = 1;
  public static final double MAXIMUM_RADIUS = 7;
  public static final double START_RADIUS = 7;
  public static final double MINIMUM_LENGTH = 2;

  public static final double DEFAULT_RADIUS_STEP = 1;
  public static final double DEFAULT_RADIUS = 3;

  // length constants

  public static final double DEFAULT_LENGTH = 3;
  public static final double DEFAULT_LENGTH_CHANGE = 1;

  // default angle constants

  private static final double DEFAULT_ANGLE_QUANTA = 15;
  private static final double DEFAULT_RADIUS_QUANTA = 0.5;
  private static final double DEFAULT_LENGTH_QUANTA = 1;

  // private static final double DEFAULT_CURVE_EXTENT = 90;

  // private double mCurrentRadius = DEFAULT_RADIUS;
  private double mCurrentLength = DEFAULT_LENGTH;
  // private double mCurrentCurveExtent = DEFAULT_CURVE_EXTENT;

  private IRenderable mArtifact;
  
  protected ArenaPanel mArena;

  protected JMenu mEditMenu;

  protected JMenuBar mMenuBar;

  protected JMenu mFileMenu;

  protected JMenu mViewMenu;

  private IItemStore mStore;

  public static void main(String[] args)
  {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
      "Arena Test");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    new BlockPathBuilder(new FileStore("/tmp/store"));
  }

  // construct a swarm

  public BlockPathBuilder(IItemStore store)
  {
    // get the item store
    
    mStore = store;

    // make the frame

    constructFrame();
    
    // initialize the object
    
    initializeArtifact();
    
    // show the frame

    pack();
    setSize(800, 600);
    setVisible(true);
    repaint();
  }

  /**
   * Initialize the artifact to be edited.
   */
  
  protected void initializeArtifact()
  {
    setArtifact(new BlockPath());
    getCurrentPath().setSelected(true);
    getCurrentPath().addAfter(new StraightBlock(mCurrentLength));
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
    mFileMenu.add(mSaveAction);
    mFileMenu.add(mLoadAction);

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
      }
    };

    frame.add(mArena, BorderLayout.CENTER);
  }
  
  @Override
  public void repaint()
  {
    mArena.setViewPort(getArtifact().getBounds2D(), Constants.ARENA_VIWPORT_BORDER);
    super.repaint();
  }

  protected void save()
  {
    mStore.add(mArtifact, "name");
  }

  protected void load()
  {
    JDialog box = new JDialog(this, "Select Item", true);
    Container frame = box.getContentPane();
    frame.add(new ItemSelecterPanel(mStore));
    box.pack();
    box.setVisible(true);
  }

  private void curveRight()
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

  private void embiggen()
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
      sb
        .setLength(max(sb.getLength() - DEFAULT_LENGTH_CHANGE, MINIMUM_LENGTH));
      repaint();
    }
    else if (getCurrentBlock() instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)getCurrentBlock();
      cb.setExtent(max(cb.getExtent() - DEFAULT_ANGLE_QUANTA,
        DEFAULT_ANGLE_QUANTA));
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

  public void setArtifact(IRenderable artifact)
  {
    mArtifact = artifact;
  }

  public IRenderable getArtifact()
  {
    return mArtifact;
  }

  private SwarmAction mCurveLeftAction =
    new SwarmAction("Left", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
      "curve to the left")
    {
      public void actionPerformed(ActionEvent e)
      {
        curveLeft();
      }
    };

  private SwarmAction mCurveRightAction =
    new SwarmAction("Right", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
      "curve to the right")
    {
      public void actionPerformed(ActionEvent e)
      {
        curveRight();
      }
    };

  /** Action to select simulated rather live operation. */

  private SwarmAction mGoStraightAction =
    new SwarmAction("Straight", KeyStroke.getKeyStroke(KeyEvent.VK_UP,
      KeyEvent.SHIFT_DOWN_MASK), "add path block wich goes straight forward")
    {
      public void actionPerformed(ActionEvent e)
      {
        addBlock(new StraightBlock(mCurrentLength));
      }
    };

  /** Action to select simulated rather live operation. */

  private SwarmAction mDeleteBlockAction =
    new SwarmAction("Delete", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
      "delete current block")
    {
      public void actionPerformed(ActionEvent e)
      {
        removeBlock();
      }
    };

  /** Action to select simulated rather live operation. */

  private SwarmAction mShortenAction =
    new SwarmAction("Ensmallen", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
      "decrease the length of the block")
    {
      public void actionPerformed(ActionEvent e)
      {
        ensmallen();
      }
    };

  /** Action to select simulated rather live operation. */

  private final SwarmAction mLengthenAction =
    new SwarmAction("Embiggen", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
      "increase the length of the block")
    {
      public void actionPerformed(ActionEvent e)
      {
        embiggen();
      }
    };

  private final SwarmAction mShiftLeft =
    new SwarmAction("Left Adjust", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      KeyEvent.SHIFT_DOWN_MASK), "widen right curves, narrow left curves")
    {
      public void actionPerformed(ActionEvent e)
      {
        shiftLeft();
      }
    };

  private final SwarmAction mShiftRight =
    new SwarmAction("Right Adjust", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
      KeyEvent.SHIFT_DOWN_MASK), "widen left curvs, narrow right curves")
    {
      public void actionPerformed(ActionEvent e)
      {
        shiftRight();
      }
    };

  private final SwarmAction mPreviouseBlock =
    new SwarmAction("Previouse Block", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
      KeyEvent.META_DOWN_MASK), "select previouse block on current path")
    {
      public void actionPerformed(ActionEvent e)
      {
        previouseBlock();
      }
    };

  protected final SwarmAction mNextBlock =
    new SwarmAction("Next Block", KeyStroke.getKeyStroke(KeyEvent.VK_UP,
      KeyEvent.META_DOWN_MASK), "select next block on current path")
    {
      public void actionPerformed(ActionEvent e)
      {
        nextBlock();
      }
    };

  private final SwarmAction mZoomIn =
    new SwarmAction("Zoom in", KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0),
      "zoom in on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        mArena.zoomIn();
      }
    };

  /** Zoom display out. */

  SwarmAction mZoomOut =
    new SwarmAction("Zoom out", KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0),
      "zoom out on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        mArena.zoomOut();
      }
    };

  /** Zoom display out. */

  SwarmAction mSaveAction =
    new SwarmAction("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S,
      KeyEvent.META_DOWN_MASK), "save this path")
    {
      public void actionPerformed(ActionEvent e)
      {
        save();
      }
    };

  SwarmAction mLoadAction =
    new SwarmAction("Load", KeyStroke.getKeyStroke(KeyEvent.VK_L,
      KeyEvent.META_DOWN_MASK), "load a path")
    {
      public void actionPerformed(ActionEvent e)
      {
        load();
      }
    };
}
