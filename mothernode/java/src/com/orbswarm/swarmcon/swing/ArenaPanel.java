package com.orbswarm.swarmcon.swing;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static com.orbswarm.swarmcon.util.Constants.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class ArenaPanel extends JPanel
{
  private static final long serialVersionUID = 344644834801700307L;
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(ArenaPanel.class);

  /** Grid related values */

  private static final Stroke mGridStroke = new BasicStroke(.025f,
    BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
  private static final Color mGrid1Color = new Color(0, 0, 0, 40);
  private static final Color mGrid2Color = new Color(0, 0, 0, 30);
  private static final double mGrid1Size = 5;
  private static final double mGrid2Size = 1;

  // stroke used to paint the reticle at the center of the worked

  private static final Color mReticleColor = new Color(128, 128, 128);
  private static final Stroke mReticleStroke = new BasicStroke(.10f,
    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  // transform to adjust view
  
  private final AffineTransform mViewTransform;
  
  // background color
  
  private Color mBackground = DEFAULT_ARENA_BACKGROUND;

  /** should the reticle be painted */

  private boolean mPaintReticle;

  /** if set scale and translate view to said view port */
  
  private boolean mPaintGrid;

  @SuppressWarnings("serial")
  public static void main(String[] args)
  {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
      "Arena Test");

    BasicConfigurator.configure();
    JFrame jf = new JFrame();
    final ArenaPanel arena = new ArenaPanel();
    final Container frame = jf.getContentPane();
    JMenuBar menuBar = new JMenuBar();
    JMenu view = new JMenu("View");
    JMenuItem zoomIn = new JMenuItem("zoomIn");
    view.add(zoomIn);
    menuBar.add(view);
    jf.setJMenuBar(menuBar);
    arena.setPreferredSize(new Dimension(800, 600));
    frame.add(arena);
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);

    arena.setViewCenter();
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
      }
    });
  }

  public ArenaPanel()
  {
    this(true, true, true);
  }

  public ArenaPanel(boolean paintReticle, boolean paintGrid, boolean dragable)
  {
    mViewTransform = new AffineTransform();
    mViewTransform.scale(DEFAULT_PIXELS_PER_METER, -DEFAULT_PIXELS_PER_METER);
    
    setPaintGrid(paintGrid);
    setPaintReticle(paintReticle);
    MouseInputAdapter mia = new MouseListener();
    if (dragable)
    {
      addMouseMotionListener(mia);
      addMouseListener(mia);
    }
  }

  /**
   * Paint all objects in arena.
   * 
   * @param graphics graphics object to paint onto
   */

  public void paint(Graphics graphics)
  {
    // configure graphics

    Graphics2D g = (Graphics2D)graphics;

    g.setColor(mBackground);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    // set to view transform
    
    g.setTransform(mViewTransform);

    // draw the grid

    if (isPaintGrid())
    {
      paintGrid(g, mGrid1Size, mGrid1Color, mGridStroke);
      paintGrid(g, mGrid2Size, mGrid2Color, mGridStroke);
    }

    // indicate the center of the world

    if (isPaintReticle())
    {
      g.setColor(mReticleColor);
      g.setStroke(mReticleStroke);
      g.draw(new Line2D.Double(-mGrid2Size, 0, mGrid2Size, 0));
      g.draw(new Line2D.Double(0, -mGrid2Size, 0, mGrid2Size));
    }
  }

  /**
   * Paint a grid onto the display.
   * 
   * @param g graphics context to draw grid onto
   * @param gridSize size between lines on the grid
   * @param gridColor color of the grid lines
   * @param gridStroke the stroke used to draw the grid lines
   */

  private void paintGrid(Graphics2D g, double gridSize, Color gridColor,
    Stroke gridStroke)
  {
    // width and height of grid
    
    Rectangle2D frame = screenToWorld(getVisibleRect()).getBounds2D();

    // set the color and stroke

    g.setColor(gridColor);
    g.setStroke(gridStroke);

    double xOff =
      (mViewTransform.getTranslateX() / mViewTransform.getScaleX()) %
        gridSize;
    for (double x = frame.getMinX(); x < frame.getMaxX(); x += gridSize)
      g.draw(new Line2D.Double(x + xOff, frame.getMinY(), x + xOff, frame
        .getMaxY()));

    double yOff =
      ((mViewTransform.getTranslateY() / mViewTransform.getScaleY()) + frame
        .getHeight()) %
        gridSize;
    for (double y = frame.getMinY(); y < frame.getMaxY(); y += gridSize)
      g.draw(new Line2D.Double(frame.getMinX(), y + yOff, frame.getMaxX(), y +
        yOff));
  }
  
  /** Get the global offset. */

  public Point2D getGlobalOffset()
  {
    return new Point2D.Double(mViewTransform.getTranslateX(), mViewTransform
      .getTranslateY());
  }

  /** Set the global offset. */

  public void changeGlobalOffset(double deltaX, double deltaY)
  {
    mViewTransform.translate(deltaX, deltaY);
    repaint();
  }

  /**
   * Convert a screen coordinate to a world coordinates.
   * 
   * @param screenPos a position in screen coordinates
   * @return the point converted to world coordinates.
   */

  public Point2D screenToWorld(double x, double y)
  {
    return screenToWorld(new Point2D.Double(x, y));
  }

  /**
   * Convert a screen coordinate to a world coordinates.
   * 
   * @param screenPos a position in screen coordinates
   * @return the point converted to world coordinates.
   */

  public Point2D screenToWorld(Point2D screenPos)
  {
    invertViewTransform();
    Point2D worldPos = new Point2D.Double();
    mViewTransform.transform(screenPos, worldPos);
    invertViewTransform();
    return worldPos;
  }

  /**
   * Convert a shape in screen units to a shape in world units.
   * 
   * @param screenUnitShape the shape represented in screen units.
   * @return the shape converted to world units.
   */

  public Shape screenToWorld(Shape screenUnitShape)
  {
    invertViewTransform();
    Shape worldUnitShape = mViewTransform.createTransformedShape(screenUnitShape);
    invertViewTransform();
    return worldUnitShape;
  }

  private void invertViewTransform()
  {
    try
    {
      mViewTransform.invert();
    }
    catch (NoninvertibleTransformException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Zoom in view.
   */
  
  public void zoomIn()
  {
    double scale = 0.9;
    mViewTransform.scale(scale, scale);
    repaint();
  }
  
  /**
   * Zoom out view.
   */
  
  public void zoomOut()
  {
    double scale = 1.1;
    mViewTransform.scale(scale, scale);
    repaint();
  }

  /**
   * Set this view to paint a big cross at 0,0 in the arena.
   * 
   * @param paintReticle
   */
  
  public void setPaintReticle(boolean paintReticle)
  {
    mPaintReticle = paintReticle;
  }

  /**
   * @return true if the reticle is going to be painted.
   */
  
  public boolean isPaintReticle()
  {
    return mPaintReticle;
  }


  /**
   * Sets the view to center on provided point in world units.
   * 
   * @param the point to place at the center of the display.
   */

  public void setViewCenter(Point2D target)
  {
    if (getVisibleRect().getWidth() == 0)
      return;

    // reset the view port to zero zero

    Point2D center = screenToWorld(getVisibleRect().getCenterX(), getVisibleRect().getCenterY());
    mViewTransform.translate(center.getX() - target.getX(), center.getY() - target.getY());
    repaint();
  }

  
  /**
   * Set the view to the center of the window later, (presumably after the
   * frame has been laid out).
   */

  public void setViewCenterLater()
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        setViewCenter();
      }
    });
  }

  /**
   * Sets the view center to the point 0,0.
   */

  public void setViewCenter()
  {
    setViewCenter(new Point2D.Double());
  }

  /**
   * Sets the view port to completely view the given rectangle in world
   * units.
   * 
   * @param viewPort the view port in world units to view
   * @param border the border to add around the view
   */

  public void setViewPort(Rectangle2D viewPort, double border)
  {
    if (getVisibleRect().getWidth() == 0)
      return;

    if (0 != border)
      viewPort.setRect(viewPort.getX() - border, viewPort.getY() - border,
        viewPort.getWidth() + 2 * border, viewPort.getHeight() + 2 * border);

    // reset the view transform

    mViewTransform.setToIdentity();

    // set scale

    double scale =
      Math.min(getWidth() / viewPort.getWidth(),
        getHeight() / viewPort.getHeight());
    mViewTransform.scale(scale, -scale);

    // set translate

    Rectangle2D frame = screenToWorld(getVisibleRect()).getBounds2D();
    double centerX = (viewPort.getWidth() - frame.getWidth()) / 2;
    double centerY = (viewPort.getHeight() - frame.getHeight()) / 2;
    mViewTransform.translate(-(viewPort.getX() + centerX), -(viewPort.getY() +
      viewPort.getHeight() - centerY));

    // repaint

    repaint();
  }
  
  /**
   * Set this view to paint a grid.
   * 
   * @param paintGrid true if grid is to be painted.
   */
  
  public void setPaintGrid(boolean paintGrid)
  {
    mPaintGrid = paintGrid;
    repaint();
  }

  /**
   * @return true if grid will be painted.
   */
  
  public boolean isPaintGrid()
  {
    return mPaintGrid;
  }
  
  /** swarm mouse input adapter */

  private class MouseListener extends MouseInputAdapter
  {
    private MouseEvent clickEvent = null;
    
    // mouse pressed event

    public void mousePressed(MouseEvent e)
    {
      clickEvent = e;
      log.debug(String.format("screen: %s, world: %s", e.getPoint(),
        screenToWorld(e.getPoint())));
    }

    // mouse dragged event

    public void mouseDragged(MouseEvent e)
    {
      if (clickEvent != null)
      {
        Point2D start = screenToWorld(clickEvent.getPoint());
        Point2D end = screenToWorld(e.getPoint());
        mViewTransform.translate(end.getX() - start.getX(), end.getY() - start.getY());
        repaint();
        clickEvent = e;
      }
    }

    // mouse released event

    public void mouseReleased(MouseEvent e)
    {
      clickEvent = null;
    }
  }

  public Color getBackground()
  {
    return mBackground;
  }

  public void setBackground(Color background)
  {
    mBackground = background;
  }
}
