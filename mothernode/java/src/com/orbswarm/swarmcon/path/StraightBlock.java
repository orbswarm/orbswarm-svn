package com.orbswarm.swarmcon.path;

import java.awt.geom.Line2D;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "straightBlock")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class StraightBlock extends ABlock
{
  private double mLength;

  public StraightBlock()
  {
    mLength = 0;
  }

  public StraightBlock(double length)
  {
    mLength = length;
    computePath();
  }

  public void computePath()
  {
    setPathShape(new Line2D.Double(0, 0, 0, getLength()));
  }

  public StraightBlock setLength(double length)
  {
    return new StraightBlock(length);
  }

  @XmlElement(name="length")
  public double getLength()
  {
    return mLength;
  }
  
  public StraightBlock clone() throws CloneNotSupportedException
  {
    StraightBlock clone = (StraightBlock)super.clone();
    return clone;
  }
}
