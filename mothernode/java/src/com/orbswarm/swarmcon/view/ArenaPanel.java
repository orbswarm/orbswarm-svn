package com.orbswarm.swarmcon.view;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import static com.orbswarm.swarmcon.Constants.*;

import org.apache.log4j.Logger;

public class ArenaPanel extends JPanel
{
  private static final long serialVersionUID = 344644834801700307L;
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(ArenaPanel.class);

  /** scale for graphics */
  
  private double mPixelsPerMeter = DEFAULT_PIXELS_PER_METER;

  /** Grid related values */

  private static final Stroke mGridStroke = new BasicStroke(.025f,
    BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
  private static final Color mGrid1Color = new Color(0, 0, 0, 40);
  private static final Color mGrid2Color = new Color(0, 0, 0, 30);
  private static final double mGrid1Size = 5;
  private static final double mGrid2Size = 1;

  // stroke used to paint the reticle at the center of the worked

  private static final Stroke mReticleStroke = new BasicStroke(.10f,
    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  /** the global word offset to correct the orbs position by */

  private final Point2D mGlobalOffset = new Point2D.Double(0, 0);
  private boolean mPaintReticle;

  public static void main(String[] args)
  {
    JFrame jf = new JFrame();
    Container frame = jf.getContentPane();
    frame.add(new ArenaPanel(true));
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.pack();
    jf.setSize(new Dimension(800, 600));
    jf.setVisible(true);
  }

  // construct a swarm

  public ArenaPanel(boolean paintReticle)
  {
    MouseInputAdapter mia = new MouseListener();
    addMouseMotionListener(mia);
    addMouseListener(mia);
    setPaintReticle(paintReticle);
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

    g.setColor(BACKGROUND);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    // set 0,0 to lower left corner, and scale for meters

    g.scale(mPixelsPerMeter, -mPixelsPerMeter);

    // apply the global offset

    g.translate(getGlobalOffset().getX(), getGlobalOffset().getY());

    // draw the grid

    paintGrid(g, mGrid1Size, mGrid1Color, mGridStroke);
    paintGrid(g, mGrid2Size, mGrid2Color, mGridStroke);

    // indicate the center of the world

    if (mPaintReticle)
    {
      g.setColor(new Color(128, 128, 128));
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

    int width = getWidth();
    int height = getHeight();

    // compute the grid starting position

    double gridX = -(getGlobalOffset().getX() - getGlobalOffset().getX() %
      gridSize);
    double gridY = -(getGlobalOffset().getY() - getGlobalOffset().getY() %
      gridSize);

    // set the color and stroke

    g.setColor(gridColor);
    g.setStroke(gridStroke);

    // draw the verticals

    for (double x = gridX; x <= gridX + 1.5 * width / mPixelsPerMeter; x += gridSize)
      g.draw(new Line2D.Double(x, -getGlobalOffset().getY(), x,
        -getGlobalOffset().getY() - height / mPixelsPerMeter));

    // draw the horizontal

    for (double y = gridY; y >= gridY - 1.5 * height / mPixelsPerMeter; y -= gridSize)
      g.draw(new Line2D.Double(-getGlobalOffset().getX(), y,
        -getGlobalOffset().getX() + width / mPixelsPerMeter, y));
  }

  /** Get the global offset. */

  public Point2D getGlobalOffset()
  {
    return new Point2D.Double(mGlobalOffset.getX() +
      (getWidth() / mPixelsPerMeter / 2), mGlobalOffset.getY() -
      (getHeight() / mPixelsPerMeter / 2));
  }

  /** Set the global offset. */

  public void setGlobalOffset(Point2D globalOffset)
  {
    mGlobalOffset.setLocation(globalOffset);
    repaint();
  }

  /**
   * Convert screen coordinates to world coordinates.
   * 
   * @param screenPos a position in screen coordinates
   * @return the point converted to world coordinates.
   */

  public Point2D screenToWorld(Point2D screenPos)
  {
    return new Point2D.Double(screenPos.getX() / mPixelsPerMeter -
      getGlobalOffset().getX(), screenPos.getY() / -mPixelsPerMeter -
      getGlobalOffset().getY());
  }

  /** swarm mouse input adapter */

  class MouseListener extends MouseInputAdapter
  {
    private MouseEvent clickEvent = null;
    
    // mouse pressed event

    public void mousePressed(MouseEvent e)
    {
      clickEvent = e;
    }

    // mouse dragged event

    public void mouseDragged(MouseEvent e)
    {
      if (clickEvent != null)
      {
        Point2D start = screenToWorld(clickEvent.getPoint());
        Point2D end = screenToWorld(e.getPoint());
        setGlobalOffset(new Point2D.Double(mGlobalOffset.getX() +
          (end.getX() - start.getX()), mGlobalOffset.getY() +
          (end.getY() - start.getY())));
        clickEvent = e;
      }
    }

    // mouse released event

    public void mouseReleased(MouseEvent e)
    {
      clickEvent = null;
    }
  }

  public void zoomIn()
  {
    mPixelsPerMeter /= 1.1;
    repaint();
  }
  
  public void zoomOut()
  {
    mPixelsPerMeter *= 1.1;
    repaint();
  }

  public void setPaintReticle(boolean paintReticle)
  {
    mPaintReticle = paintReticle;
  }

  public boolean isPaintReticle()
  {
    return mPaintReticle;
  }
}
