package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.Line2D;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@XmlRootElement(name = "straightBlock")
@XmlAccessorType(XmlAccessType.FIELD)
public class StraightBlock extends ABlock
{
  private static Logger log = Logger.getLogger(StraightBlock.class);
  @XmlElement(name="length")
  private double mLength;

  public StraightBlock()
  {
    mLength = 0;
  }

  public StraightBlock(double length)
  {
    mLength = length;
  }

  public Shape computePath()
  {
    return new Line2D.Double(0, 0, 0, getLength());
  }

  public StraightBlock setLength(double length)
  {
    log.debug(this.hashCode() + ": set length: " + length);
    return new StraightBlock(length);
  }

  public double getLength()
  {
    log.debug(this.hashCode() + ": get length: " + mLength);
    return mLength;
  }
  
  public StraightBlock clone() throws CloneNotSupportedException
  {
    StraightBlock clone = (StraightBlock)super.clone();
    return clone;
  }

  public String toString()
  {
    return "StraightBlock [mLength=" + mLength + "]";
  }
}
