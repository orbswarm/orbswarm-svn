package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

@XmlSeeAlso({CurveBlock.class, StraightBlock.class})
public abstract class ABlock implements IBlock
{
  private static Logger log = Logger.getLogger(ABlock.class);

  
  private Shape mPathShape;
  private BlockState mDeltaState;
  
  public ABlock()
  {
  }

  /**
   * Set the shape of this path. This shape should be a open path
   * originating at 0,0 and and heading north.
   * 
   * @param pathShape the path shape
   */

  protected void setPathShape(Shape pathShape)
  {
    mPathShape = pathShape;

    PathIterator pi = mPathShape.getPathIterator(null);
    double[] coords = new double[6];

    double x1 = 0;
    double y1 = 0;
    double x2 = 0;
    double y2 = 0;

    while (!pi.isDone())
    {
      int type = pi.currentSegment(coords);
      switch (type)
      {
      case PathIterator.SEG_MOVETO:
        log.debug(String.format(" MOVE: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = x2;
        y1 = y2;
        x2 = coords[0];
        y2 = coords[1];
        break;
      case PathIterator.SEG_LINETO:
        log.debug(String.format(" LINE: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = x2;
        y1 = y2;
        x2 = coords[0];
        y2 = coords[1];
        break;
      case PathIterator.SEG_QUADTO:
        log.debug(String.format(" QUAD: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = coords[0];
        y1 = coords[1];
        x2 = coords[2];
        y2 = coords[3];
        break;
      case PathIterator.SEG_CUBICTO:
        log.debug(String.format("CUBIC: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = coords[2];
        y1 = coords[3];
        x2 = coords[4];
        y2 = coords[5];
        break;
      default:
        throw new Error("unknown path element type: " + type);
      }

      pi.next();
    }

    mDeltaState =
      new BlockState(new Angle(x2 - x1, y2 - y1)
        .rotate(-90, Type.DEGREE_RATE), new Point2D.Double(x2, y2));

    log.debug("new state: " + mDeltaState);
  }

  /**
   * Get the shape of this path. The shape returned will be start at 0,0
   * heading north.
   * 
   * @return path shape.
   */
  
  protected Shape getPathShape()
  {
    return mPathShape;
  }

  /** {@inheritDoc} */

  public Shape getPath(BlockState startState)
  {
    return startState.creatTransformedShape(mPathShape);
  }
  
  /** {@inheritDoc} */
  
  public BlockState getDeltaState()
  {
    return mDeltaState;
  }
}
