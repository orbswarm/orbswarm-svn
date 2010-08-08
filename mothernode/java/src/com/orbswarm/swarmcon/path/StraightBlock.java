package com.orbswarm.swarmcon.path;

import java.awt.geom.Line2D;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StraightBlock extends ABlock
{
  private double mLength;

  public StraightBlock(double length)
  {
    mLength = length;
    computePath();
  }
  
  public void computePath()
  {
    setPathShape(new Line2D.Double(0, 0, 0, getLength()));
  }

  public void setLength(double length)
  {
    mLength = length;
    computePath();
  }

  public double getLength()
  {
    return mLength;
  }
}
