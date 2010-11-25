package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.Angle.Type;

import com.orbswarm.swarmcon.util.Path;

@XmlSeeAlso({CurveBlock.class, StraightBlock.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ABlock implements IBlock
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(ABlock.class);
  
  @XmlTransient
  private AffineTransform mTransform;
  @XmlTransient
  private Shape mPathShape;
  @XmlElement(name="selected")
  private boolean mSelected;
  @XmlTransient
  private boolean mSuppressed;
  
  /**
   * Compute path shape, left as an exercise for the implementing class.
   */
  
  protected abstract Shape computePath();
  
  /** {@inheritDoc} */
  
  public Shape getPath()
  {
    if (null == mPathShape)
      mPathShape = computePath();

    return mPathShape;
  }
  
  /** {@inheritDoc} */
  
  public AffineTransform getBlockTransform()
  {
    if (null == mTransform)
    {
      double[] t = Path.computePathTransfrom(getPath());

      double x1 = t[0];
      double y1 = t[1];
      double x2 = t[2];
      double y2 = t[3];
      
      mTransform =
        AffineTransform.getRotateInstance(new Angle(x2 - x1, y2 - y1).rotate(
          -90, Type.DEGREE_RATE).as(Angle.Type.RADIANS));
      mTransform.translate(-x2, y2);
    }
    
    return mTransform;
  }

  public boolean isSelected()
  {
    return mSuppressed ? false : mSelected;
  }

  public void setSelected(boolean selected)
  {
    mSelected = selected;
  }
  
  public void setSuppressed(boolean suppressed)
  {
    mSuppressed = suppressed;
  }

  // clone
  
  public ABlock clone() throws CloneNotSupportedException
  {
    ABlock clone = (ABlock)super.clone();
    return clone;
  }
}
