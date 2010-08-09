package com.orbswarm.swarmcon.path;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
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
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

import com.orbswarm.swarmcon.view.ArenaPanel;
import com.orbswarm.swarmcon.view.Renderer;
import com.orbswarm.swarmcon.xml.ABar;
import com.orbswarm.swarmcon.xml.Bar1;
import com.orbswarm.swarmcon.xml.Bar2;
import com.orbswarm.swarmcon.xml.Foo;
import com.orbswarm.swarmcon.xml.IBar;

import static java.lang.Math.PI;
import static java.lang.Math.round;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

@SuppressWarnings("serial")
public class BlockPathBuilder extends JFrame
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPathBuilder.class);

  private static final long serialVersionUID = -770543339999194325L;

  // radius constants

  public static final double MINIMUM_RADIUS = 1;
  public static final double MAXIMUM_RADIUS = 10;
  public static final double START_RADIUS = 2;
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

  private static final double DEFAULT_CURVE_EXTENT = 90;

  private double mCurrentRadius = DEFAULT_RADIUS;
  private double mCurrentLength = DEFAULT_LENGTH;
  private double mCurrentCurveExtent = DEFAULT_CURVE_EXTENT;

  private BlockPath mBlockPath;
  private IBlock mCurrentBlock;

  public static class Point2DAdapter extends
    XmlAdapter<Point2D.Double, Point2D>
  {
    public Point2D.Double marshal(Point2D v) throws Exception
    {
      return (Point2D.Double)v;
    }

    public Point2D unmarshal(Point2D.Double v) throws Exception
    {
      return v;
    }
  }

  public static class IBarAdapter extends XmlAdapter<ABar, IBar>
  {
    public ABar marshal(IBar v) throws Exception
    {
      return (ABar)v;
    }

    public IBar unmarshal(ABar v) throws Exception
    {
      return v;
    }
  }

  public static void main(String[] args)
  {
    BasicConfigurator.configure();
    new BlockPathBuilder();
    // test();
  }

  public static void test()
  {
    Foo foo = new Foo();
    IBar bar1 = new Bar1("fred", 1.1d, 2.2d);
    IBar bar2 = new Bar2("barny", 3.3d, 4.4d, 999);

    foo.add(bar1);
    foo.add(bar2);
    System.out.println("foo: " + foo);
    try
    {
      StringWriter writer = new StringWriter();

      JAXBContext context = JAXBContext.newInstance(foo.getClass());
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(foo, writer);

      System.out.println(writer.getBuffer());
      StringReader reader = new StringReader(writer.getBuffer().toString());

      Unmarshaller unmarshaller = context.createUnmarshaller();
      Foo qux = (Foo)unmarshaller.unmarshal(reader);
      System.out.println("qux: " + qux);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
  }

  private ArenaPanel mArena;

  // construct a swarm

  public BlockPathBuilder()
  {
    // make the frame

    constructFrame();

    // make the path

    mBlockPath = new BlockPath();
    addBlock(new StraightBlock(mCurrentLength));

    // show the frame

    pack();
    setSize(800, 600);
    setVisible(true);
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
    add(menuBar, BorderLayout.NORTH);

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
    editMenu.add(new JMenuItem(mBackupAction));
    editMenu.addSeparator();
    editMenu.add(new JMenuItem(mLengthenAction));
    editMenu.add(new JMenuItem(mShortenAction));
    editMenu.add(new JMenuItem(mShiftRight));
    editMenu.add(new JMenuItem(mShiftLeft));

    // make view menu

    JMenu viewMenu = new JMenu("View");
    menuBar.add(viewMenu);
    viewMenu.add(mZoomIn);
    viewMenu.add(mZoomOut);

    // add drawing area

    mArena = new ArenaPanel(false)
    {
      private static final long serialVersionUID = -4817333022169216465L;

      public void paint(Graphics g)
      {
        super.paint(g);
        Renderer.render((Graphics2D)g, mBlockPath);
      }
    };
    frame.add(mArena, BorderLayout.CENTER);
  }

  protected void save()
  {
    try
    {
      JAXBContext context =
        JAXBContext.newInstance(mBlockPath.getClass(), ABlock.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(mBlockPath, new FileWriter("test.xml"));
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
      JAXBContext context =
        JAXBContext.newInstance(mBlockPath.getClass(), ABlock.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      mBlockPath =
        (BlockPath)unmarshaller.unmarshal(new FileReader("test.xml"));
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
    if (null == mCurrentBlock)
      return;

    if (mCurrentBlock instanceof StraightBlock)
    {
      CurveBlock cb =
        convertToCurve((StraightBlock)mCurrentBlock, CurveBlock.Type.RIGHT);
      removeLastBlock();
      addBlock(cb);
    }
    else if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      double radius = cb.getRadius();
      double radiusStep = computeRadiusStep(radius);

      if (cb.getType() == CurveBlock.Type.LEFT)
      {
        if (radius < MAXIMUM_RADIUS)
          cb.setRadius(radius + radiusStep);
        else
        {
          StraightBlock sb = convertToStraight(cb);
          removeLastBlock();
          addBlock(sb);
        }
      }
      else
      {
        if (radius > MINIMUM_RADIUS)
          cb.setRadius(Math.max(radius - radiusStep, MINIMUM_RADIUS));
      }

      repaint();
    }
  }

  private void curveLeft()
  {
    if (null == mCurrentBlock)
      return;

    if (mCurrentBlock instanceof StraightBlock)
    {
      CurveBlock cb =
        convertToCurve((StraightBlock)mCurrentBlock, CurveBlock.Type.LEFT);
      removeLastBlock();
      addBlock(cb);
    }
    else if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      double radius = cb.getRadius();
      double radiusStep = computeRadiusStep(radius);

      if (cb.getType() == CurveBlock.Type.RIGHT)
      {
        if (radius < MAXIMUM_RADIUS)
          cb.setRadius(radius + radiusStep);
        else
        {
          StraightBlock sb = convertToStraight(cb);
          removeLastBlock();
          addBlock(sb);
        }
      }
      else
      {
        if (radius > MINIMUM_RADIUS)
          cb.setRadius(Math.max(radius - radiusStep, MINIMUM_RADIUS));
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

  private double computeRadiusStep(double radius)
  {
    double range = MAXIMUM_RADIUS - MINIMUM_RADIUS;
    double theta = (((radius - MINIMUM_RADIUS) / range) * PI / 2) - PI / 2;

    double step =
      max(quantize((MAXIMUM_RADIUS / 2) * (sin(theta) + 1),
        DEFAULT_RADIUS_QUANTA), DEFAULT_RADIUS_QUANTA);

    log.debug("step: " + step);
    return step;

    // pow(radius, 4) / 2000
  }

  private void embiggen()
  {
    if (null == mCurrentBlock)
      return;

    if (mCurrentBlock instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)mCurrentBlock;
      sb.setLength(sb.getLength() + DEFAULT_LENGTH_CHANGE);
      repaint();
    }
    else if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      cb.setExtent(min(cb.getExtent() + DEFAULT_ANGLE_QUANTA, 360));
      repaint();
    }
  }

  private void ensmallen()
  {

    if (null == mCurrentBlock)
      return;

    if (mCurrentBlock instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)mCurrentBlock;
      sb
        .setLength(max(sb.getLength() - DEFAULT_LENGTH_CHANGE, MINIMUM_LENGTH));
      repaint();
    }
    else if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
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
    mCurrentBlock = block;
    mBlockPath.add(mCurrentBlock);
    mArena.repaint();
  }

  private void removeLastBlock()
  {
    if (mBlockPath.size() > 0)
    {
      mBlockPath.removeElement(mBlockPath.lastElement());
      if (mBlockPath.size() > 0)
        mCurrentBlock = mBlockPath.lastElement();
      else
        mCurrentBlock = null;
      mArena.repaint();
    }
  }

  protected void shortenSegment()
  {
    if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      double extent = cb.getExtent();
      if (extent > DEFAULT_ANGLE_QUANTA)
      {
        cb.setExtent(extent - DEFAULT_ANGLE_QUANTA);
        mArena.repaint();
      }
    }
    else if (mCurrentBlock instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)mCurrentBlock;
      double length = sb.getLength();
      if (length > DEFAULT_LENGTH_CHANGE)
        sb.setLength(length - DEFAULT_LENGTH_CHANGE);
      mArena.repaint();
    }
  }

  protected void lengthenSegment()
  {
    if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      double extent = cb.getExtent();
      if (extent < 360)
      {
        cb.setExtent(extent + DEFAULT_ANGLE_QUANTA);
        mArena.repaint();
      }
    }
    else if (mCurrentBlock instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)mCurrentBlock;
      sb.setLength(sb.getLength() + DEFAULT_LENGTH_CHANGE);
      mArena.repaint();
    }
  }

  protected void shiftLeft()
  {
    if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      if (cb.getType() == CurveBlock.Type.RIGHT)
        cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_STEP);
      else if (cb.getRadius() > MINIMUM_RADIUS)
        cb.setRadius(cb.getRadius() - DEFAULT_RADIUS_STEP);

      mArena.repaint();
    }
  }

  protected void shiftRight()
  {
    if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      if (cb.getType() == CurveBlock.Type.LEFT)
        cb.setRadius(cb.getRadius() + DEFAULT_RADIUS_STEP);
      else if (cb.getRadius() > MINIMUM_RADIUS)
        cb.setRadius(cb.getRadius() - DEFAULT_RADIUS_STEP);

      mArena.repaint();
    }
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
      private static final long serialVersionUID = -895535667108572039L;

      public void actionPerformed(ActionEvent e)
      {
        curveLeft();
      }
    };

  private Action mCurveRightAction =
    new Action("Right", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
      "curve to the right")
    {
      private static final long serialVersionUID = -191894994114298776L;

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
      private static final long serialVersionUID = 4754414744672359329L;

      public void actionPerformed(ActionEvent e)
      {
        addBlock(new StraightBlock(mCurrentLength));
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mBackupAction =
    new Action("Backup", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
      KeyEvent.SHIFT_DOWN_MASK), "remove last block form path")
    {
      private static final long serialVersionUID = 3836535220370440971L;

      public void actionPerformed(ActionEvent e)
      {
        removeLastBlock();
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mShortenAction =
    new Action("Ensmallen", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
      "decrease the length of the block")
    {
      private static final long serialVersionUID = 4754414744672359329L;

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
      private static final long serialVersionUID = 5229008455547924307L;

      public void actionPerformed(ActionEvent e)
      {
        embiggen();
      }
    };

  private final Action mShiftLeft =
    new Action("Left Adjust", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      KeyEvent.SHIFT_DOWN_MASK), "widen right curves, narrow left curves")
    {
      private static final long serialVersionUID = 7782924027907598941L;

      public void actionPerformed(ActionEvent e)
      {
        shiftLeft();
      }
    };

  private final Action mShiftRight =
    new Action("Right Adjust", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
      KeyEvent.SHIFT_DOWN_MASK), "widen left curvs, narrow right curves")
    {
      private static final long serialVersionUID = -4713332280456535335L;

      public void actionPerformed(ActionEvent e)
      {
        shiftRight();
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
