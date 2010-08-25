package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

@XmlSeeAlso({CurveBlock.class, StraightBlock.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ABlock implements IBlock
{
  private static Logger log = Logger.getLogger(ABlock.class);
  
  private AffineTransform mTransform;
  @XmlTransient
  private Shape mPathShape;
  @XmlElement(name="selected")
  private boolean mSelected;
  
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

    mTransform =
      AffineTransform.getRotateInstance(new Angle(x2 - x1, y2 - y1).rotate(
        -90, Type.DEGREE_RATE).as(Angle.Type.RADIANS));
    mTransform.translate(-x2, y2);
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
  
  public Shape getPath()
  {
    return mPathShape;
  }
  
  /** {@inheritDoc} */
  
  public AffineTransform getBlockTransform()
  {
    return mTransform;
  }

  public boolean isSelected()
  {
    return mSelected;
  }

  public void setSelected(boolean selected)
  {
    mSelected = selected;
  }
}
