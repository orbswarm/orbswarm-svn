package com.orbswarm.swarmcon.path;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.view.ArenaPanel;
import com.orbswarm.swarmcon.view.Renderer;

@SuppressWarnings("serial")
public class BlockPathBuilder extends JFrame
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPathBuilder.class);

  private static final long serialVersionUID = -770543339999194325L;

  // radius constants

  private static final double DEFAULT_RADIUS_STEP = 0.5;
  private static final double DEFAULT_MINIMUM_RADIUS = 1;
  public static final double DEFAULT_RADIUS = 3;

  // length constants

  public static final double DEFAULT_LENGTH = 4;
  public static final double DEFAULT_LENGTH_CHANGE = 1;

  // default angle constants

  private static final double DEFAULT_ANGLE_CHANGE = 15;
  private static final double DEFAULT_CURVE_EXTENT = 90;

  private double mCurrentRadius = DEFAULT_RADIUS;
  private double mCurrentLength = DEFAULT_LENGTH;
  private double mCurrentCurveExtent = DEFAULT_CURVE_EXTENT;

  private BlockPath mBlockPath;
  private IBlock mCurrentBlock;
  
  @XmlRootElement
  static class Foo
  {
    private Vector<IBar> mBars = new Vector<IBar>();

    public void add(IBar bar)
    {
      getBars().add(bar);
    }

    public void setBars(Vector<IBar> bars)
    {
      mBars = bars;
    }

    public Vector<IBar> getBars()
    {
      return mBars;
    }

    public String toString()
    {
      return "Foo [mBars=" + mBars + "]";
    }
  }
  
  @XmlJavaTypeAdapter(IBarAdapter.class)
  static interface IBar
  {
    public String getName();
    public Point2D getPos();
  }
  
  @XmlSeeAlso({Bar1.class, Bar2.class})
  static abstract class ABar implements IBar
  {
    private String name;

    @XmlTransient
    private Point2D mPos;
    
    public void setName(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }

    @XmlJavaTypeAdapter(Point2DAdapter.class)
    public void setPos(Point2D pos)
    {
      this.mPos = pos;
    }

    public Point2D getPos()
    {
      return mPos;
    }
  }
  
  static class Point2DAdapter extends XmlAdapter<Point2D.Double, Point2D>
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
  
  
  static class IBarAdapter extends XmlAdapter<ABar, IBar>
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
  
  @XmlRootElement
  static class Bar1 extends ABar
  {
    public Bar1()
    {
    }
    
    public Bar1(String name, double x, double y)
    {
      setName(name);
      setPos(new Point2D.Double(x, y));
    }

    public String toString()
    {
      return "Bar1 [getName()=" + getName() + ", getPos()=" + getPos() + "]";
    }
  }
  
  @XmlRootElement
  static class Bar2 extends ABar
  {
    @XmlTransient
    private long mSize;
    
    public Bar2()
    {
    }
    
    public Bar2(String name, double x, double y, long size)
    {
      setName(name);
      setPos(new Point2D.Double(x, y));
      setSize(size);
    }

    public void setSize(long size)
    {
      this.mSize = size;
    }

    public long getSize()
    {
      return mSize;
    }

    public String toString()
    {
      return "Bar2 [getSize()=" + getSize() + ", getName()=" + getName() +
        ", getPos()=" + getPos() + "]";
    }
  }

  public static void main(String[] args)
  {
    BasicConfigurator.configure();
    new BlockPathBuilder();
    //test();
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
    mBlockPath = new BlockPath();
    mCurrentBlock = null;

    // construct the frame

    constructFrame();

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
      JAXBContext context = JAXBContext.newInstance(mBlockPath.getClass());
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(mBlockPath, System.out);
//      marshaller.marshal(mBlockPath, new FileWriter("test.xml"));
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
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
      if (extent > DEFAULT_ANGLE_CHANGE)
      {
        cb.setExtent(extent - DEFAULT_ANGLE_CHANGE);
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
        cb.setExtent(extent + DEFAULT_ANGLE_CHANGE);
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
      else if (cb.getRadius() > DEFAULT_MINIMUM_RADIUS)
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
      else if (cb.getRadius() > DEFAULT_MINIMUM_RADIUS)
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
      "add path block wich curves left")
    {
      private static final long serialVersionUID = -895535667108572039L;

      public void actionPerformed(ActionEvent e)
      {
        addBlock(new CurveBlock(mCurrentCurveExtent, mCurrentRadius,
          CurveBlock.Type.LEFT));
      }
    };

  private Action mCurveRightAction =
    new Action("Right", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
      "add path block wich curves left")
    {
      private static final long serialVersionUID = -191894994114298776L;

      public void actionPerformed(ActionEvent e)
      {
        addBlock(new CurveBlock(mCurrentCurveExtent, mCurrentRadius,
          CurveBlock.Type.RIGHT));
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mGoStraightAction =
    new Action("Straight", KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
      "add path block wich goes straight forward")
    {
      private static final long serialVersionUID = 4754414744672359329L;

      public void actionPerformed(ActionEvent e)
      {
        addBlock(new StraightBlock(mCurrentLength));
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mBackupAction =
    new Action("Backup", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
      "remove last block form path")
    {
      private static final long serialVersionUID = 3836535220370440971L;

      public void actionPerformed(ActionEvent e)
      {
        removeLastBlock();
      }
    };

  /** Action to select simulated rather live operation. */

  private Action mShortenAction =
    new Action("Shorten Block", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
      KeyEvent.SHIFT_DOWN_MASK), "shorten the current path segment")
    {
      private static final long serialVersionUID = 4754414744672359329L;

      public void actionPerformed(ActionEvent e)
      {
        shortenSegment();
      }
    };

  /** Action to select simulated rather live operation. */

  private final Action mLengthenAction =
    new Action("Lengthend Block", KeyStroke.getKeyStroke(KeyEvent.VK_UP,
      KeyEvent.SHIFT_DOWN_MASK), "shorten the current path segment")
    {
      private static final long serialVersionUID = 5229008455547924307L;

      public void actionPerformed(ActionEvent e)
      {
        lengthenSegment();
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
    new Action("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK),
      "save this path")
    {
      public void actionPerformed(ActionEvent e)
      {
        save();
      }
    };
}
