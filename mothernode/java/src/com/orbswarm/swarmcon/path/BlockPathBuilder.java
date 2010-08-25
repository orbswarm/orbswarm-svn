package com.orbswarm.swarmcon.path;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

import com.orbswarm.swarmcon.swing.ArenaPanel;
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

  private JAXBContext mContext;

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

  private IDance mDance;

  private ArenaPanel mArena;

  private double mBorder = 2;

  protected IBlockPath mTestPath;

  public static void main(String[] args)
  {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
      "Arena Test");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    new BlockPathBuilder();
  }

  // construct a swarm

  public BlockPathBuilder()
  {
    try
    {
      mContext =
        JAXBContext.newInstance(BlockPath.class, StraightBlock.class, 
          CurveBlock.class, ABlock.class, Dance.class, AffineTransform.class);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }

    // make the frame

    constructFrame();

    // make the path

    mDance = new Dance(Dance.Layout.CIRLCE, 2);
    for (int i = 0; i < 6; ++i)
    {
      addPath(new BlockPath());
      addBlock(new StraightBlock(mCurrentLength));
    }
    getCurrentPath().setSelected(true);

    // show the frame

    pack();
    setSize(800, 600);
    setVisible(true);
    repaint();
    // mArena.setViewCenterLater();
  }

  /**
   * Place GUI objects into frame.
   */

  public void constructFrame()
  {
    Container frame = getContentPane();

    // frame closes on exit

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // set frame to box layout

    frame.setLayout(new BorderLayout());

    // make menu bar

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    // make edit menu

    JMenu fileMenu = new JMenu("File");
    menuBar.add(fileMenu);
    fileMenu.add(new JMenuItem(mSaveAction));
    fileMenu.add(new JMenuItem(mLoadAction));

    // make edit menu

    JMenu editMenu = new JMenu("Edit");
    menuBar.add(editMenu);
    editMenu.add(new JMenuItem(mCurveLeftAction));
    editMenu.add(new JMenuItem(mGoStraightAction));
    editMenu.add(new JMenuItem(mCurveRightAction));
    editMenu.add(new JMenuItem(mDeleteBlockAction));
    editMenu.addSeparator();
    editMenu.add(new JMenuItem(mLengthenAction));
    editMenu.add(new JMenuItem(mShortenAction));
    editMenu.add(new JMenuItem(mShiftRight));
    editMenu.add(new JMenuItem(mShiftLeft));
    editMenu.addSeparator();
    editMenu.add(new JMenuItem(mNextBlock));
    editMenu.add(new JMenuItem(mPreviouseBlock));
    editMenu.add(new JMenuItem(mNextPath));
    editMenu.add(new JMenuItem(mPreviousePath));

    // make view menu

    JMenu viewMenu = new JMenu("View");
    menuBar.add(viewMenu);
    viewMenu.add(mZoomIn);
    viewMenu.add(mZoomOut);

    // add drawing area

    mArena = new ArenaPanel()
    {
      private static final long serialVersionUID = -4817333022169216465L;

      public void paint(Graphics graphics)
      {
        Graphics2D g = (Graphics2D)graphics;
        super.paint(g);
        g.setColor(new Color(0, 0, 255, 16));
        g.fill(computeViewBounds());
        RendererSet.render(g, mDance);
        if (mTestPath != null)
          RendererSet.render(g, mTestPath);
//        if (getCurrentPath() != null)
//          RendererSet.render(g, getCurrentPath());
      }
    };

    frame.add(mArena, BorderLayout.CENTER);
  }

  public Rectangle2D computeViewBounds()
  {
    Rectangle2D bounds = mDance.getBounds();
    bounds.setRect(bounds.getX() - mBorder, bounds.getY() - mBorder, bounds
      .getWidth() +
      2 * mBorder, bounds.getHeight() + 2 * mBorder);
    return bounds;
  }

  @Override
  public void repaint()
  {
    mArena.setViewPort(computeViewBounds());
    super.repaint();
  }

  protected void save()
  {
    try
    {
      Marshaller marshaller = mContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(mDance, new FileWriter("test.xml"));
      StringWriter sw = new StringWriter();
      marshaller.marshal(mDance, sw);
      log.debug(sw.toString());
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected void load()
  {
    try
    {
      Unmarshaller unmarshaller = mContext.createUnmarshaller();
      log.debug(" pre: " + mDance);
      mDance = (IDance)unmarshaller.unmarshal(new FileReader("test.xml"));
      log.debug("post:" + mDance);
      
      //mDance = (IDance)unmarshaller.unmarshal(new FileReader("test.xml"));
      repaint();
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
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

  private void previousePath()
  {
    mDance.previousePath();
    repaint();
  }

  private void nextPath()
  {
    mDance.nextPath();
    repaint();
  }

  private void previouseBlock()
  {
    getCurrentPath().previouseBlock();
    repaint();
  }

  private void nextBlock()
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

  private void addPath(IBlockPath path)
  {
    mDance.addAfter(path);
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
    return mDance.getCurrentPath();
  }

  /** SwarmCon action class */

  protected abstract class Action extends AbstractAction
  {
    private static final long serialVersionUID = 2376655282485450773L;

    // construct the action
    public Action(String name, KeyStroke key, String description)
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

  private Action mCurveLeftAction =
    new Action("Left", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
      "curve to the left")
    {
      public void actionPerformed(ActionEvent e)
      {
        curveLeft();
      }
    };

  private Action mCurveRightAction =
    new Action("Right", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
      "curve to the right")
    {
      public void actionPerformed(ActionEvent e)
      {
        curveRight();
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mGoStraightAction =
    new Action("Straight", KeyStroke.getKeyStroke(KeyEvent.VK_UP,
      KeyEvent.SHIFT_DOWN_MASK), "add path block wich goes straight forward")
    {
      public void actionPerformed(ActionEvent e)
      {
        addBlock(new StraightBlock(mCurrentLength));
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mDeleteBlockAction =
    new Action("Delete", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
      "delete current block")
    {
      public void actionPerformed(ActionEvent e)
      {
        removeBlock();
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mShortenAction =
    new Action("Ensmallen", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
      "decrease the length of the block")
    {
      public void actionPerformed(ActionEvent e)
      {
        ensmallen();
      }
    };

  /** Action to select simulated rather live operation. */

  private final Action mLengthenAction =
    new Action("Embiggen", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
      "increase the length of the block")
    {
      public void actionPerformed(ActionEvent e)
      {
        embiggen();
      }
    };

  private final Action mShiftLeft =
    new Action("Left Adjust", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      KeyEvent.SHIFT_DOWN_MASK), "widen right curves, narrow left curves")
    {
      public void actionPerformed(ActionEvent e)
      {
        shiftLeft();
      }
    };

  private final Action mShiftRight =
    new Action("Right Adjust", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
      KeyEvent.SHIFT_DOWN_MASK), "widen left curvs, narrow right curves")
    {
      public void actionPerformed(ActionEvent e)
      {
        shiftRight();
      }
    };

  private final Action mPreviousePath =
    new Action("Previouse Path", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      KeyEvent.META_DOWN_MASK), "select previouse bath")
    {
      public void actionPerformed(ActionEvent e)
      {
        previousePath();
      }
    };

  private final Action mNextPath =
    new Action("Previouse Path", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
      KeyEvent.META_DOWN_MASK), "select next bath")
    {
      public void actionPerformed(ActionEvent e)
      {
        nextPath();
      }
    };

  private final Action mPreviouseBlock =
    new Action("Previouse Block", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
      KeyEvent.META_DOWN_MASK), "select previouse block on current path")
    {
      public void actionPerformed(ActionEvent e)
      {
        previouseBlock();
      }
    };

  private final Action mNextBlock =
    new Action("Next Block", KeyStroke.getKeyStroke(KeyEvent.VK_UP,
      KeyEvent.META_DOWN_MASK), "select next block on current path")
    {
      public void actionPerformed(ActionEvent e)
      {
        nextBlock();
      }
    };

  private final Action mZoomIn =
    new Action("Zoom in", KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0),
      "zoom in on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        mArena.zoomIn();
      }
    };

  /** Zoom display out. */

  Action mZoomOut =
    new Action("Zoom out", KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0),
      "zoom out on the display")
    {
      public void actionPerformed(ActionEvent e)
      {
        mArena.zoomOut();
      }
    };

  /** Zoom display out. */

  Action mSaveAction =
    new Action("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S,
      KeyEvent.META_DOWN_MASK), "save this path")
    {
      public void actionPerformed(ActionEvent e)
      {
        save();
      }
    };

  Action mLoadAction =
    new Action("Load", KeyStroke.getKeyStroke(KeyEvent.VK_L,
      KeyEvent.META_DOWN_MASK), "load a path")
    {
      public void actionPerformed(ActionEvent e)
      {
        load();
      }
    };
}
