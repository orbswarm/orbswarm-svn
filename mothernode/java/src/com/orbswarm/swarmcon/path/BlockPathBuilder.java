package com.orbswarm.swarmcon.path;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import com.orbswarm.swarmcon.view.ArenaPanel;
import com.orbswarm.swarmcon.view.Renderer;

@SuppressWarnings("serial")
public class BlockPathBuilder extends JFrame
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPathBuilder.class);

  private static final long serialVersionUID = -770543339999194325L;

  // radius constants

  private static final double DEFAULT_RADIUS_STEP = 1;
  private static final double DEFAULT_MINIMUM_RADIUS = 1;
  public static final double DEFAULT_RADIUS = 3;

  // length constants

  public static final double DEFAULT_LENGTH = 4;
  public static final double DEFAULT_LENGTH_CHANGE = 1;

  // default angle constants

  private static final double DEFAULT_ANGLE_CHANGE = 15;
  private static final Angle DEFAULT_ANGLE =
    new Angle(180, Angle.Type.DEGREES);
  private static final Angle DEFAULT_ANGLE_SHORTEN =
    new Angle(-DEFAULT_ANGLE_CHANGE, Angle.Type.DEGREE_RATE);
  private static final Angle DEFAULT_ANGLE_LENGTHEN =
    new Angle(DEFAULT_ANGLE_CHANGE, Angle.Type.DEGREE_RATE);

  private double mCurrentRadius = DEFAULT_RADIUS;
  private double mCurrentLength = DEFAULT_LENGTH;
  private Angle mCurrentAngle = DEFAULT_ANGLE;

  private BlockPath mBlockPath;
  private IBlock mCurrentBlock;

  public static void main(String[] args)
  {
    new BlockPathBuilder();
  }

  private ArenaPanel mArena;

  // construct a swarm

  public BlockPathBuilder()
  {
    mBlockPath = new BlockPath();
    mCurrentBlock = new Head();
    mBlockPath.add(mCurrentBlock);

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

  private void addBlock(IBlock block)
  {
    mCurrentBlock = block;
    mBlockPath.add(mCurrentBlock);
    mArena.repaint();
  }

  private void removeLastBlock()
  {
    if (mBlockPath.size() > 1)
    {
      mBlockPath.removeElement(mBlockPath.lastElement());
      mCurrentBlock = mBlockPath.lastElement();
      mArena.repaint();
    }
  }

  protected void shortenSegment()
  {
    if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      Angle dAngle = cb.getDeltaAngle();
      if (Math.abs(dAngle.as(Angle.Type.DEGREE_RATE)) > DEFAULT_ANGLE_CHANGE)
        if (cb.getType() == CurveBlock.Type.LEFT)
          cb.setDeltaAngle(cb.getDeltaAngle().rotate(DEFAULT_ANGLE_SHORTEN));
        else
          cb.setDeltaAngle(cb.getDeltaAngle().difference(
            DEFAULT_ANGLE_SHORTEN));

      mArena.repaint();
    }
    else if (mCurrentBlock instanceof StraightBlock)
    {
      StraightBlock sb = (StraightBlock)mCurrentBlock;
      if (sb.getLength() > DEFAULT_LENGTH_CHANGE)
        sb.setLength(sb.getLength() - DEFAULT_LENGTH_CHANGE);
      mArena.repaint();
    }
  }

  protected void lengthenSegment()
  {
    if (mCurrentBlock instanceof CurveBlock)
    {
      CurveBlock cb = (CurveBlock)mCurrentBlock;
      if (cb.getType() == CurveBlock.Type.LEFT)
        cb.setDeltaAngle(cb.getDeltaAngle().rotate(DEFAULT_ANGLE_LENGTHEN));
      else
        cb.setDeltaAngle(cb.getDeltaAngle()
          .difference(DEFAULT_ANGLE_LENGTHEN));

      mArena.repaint();
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
        addBlock(new CurveBlock(mBlockPath.lastElement(), mCurrentAngle,
          mCurrentRadius, CurveBlock.Type.LEFT));
      }
    };

  private Action mCurveRightAction =
    new Action("Right", KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
      "add path block wich curves left")
    {
      private static final long serialVersionUID = -191894994114298776L;

      public void actionPerformed(ActionEvent e)
      {
        addBlock(new CurveBlock(mBlockPath.lastElement(), mCurrentAngle,
          mCurrentRadius, CurveBlock.Type.RIGHT));
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
        addBlock(new StraightBlock(mBlockPath.lastElement(), mCurrentLength));
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
}
