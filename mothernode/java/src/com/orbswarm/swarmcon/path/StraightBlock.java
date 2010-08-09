package com.orbswarm.swarmcon.path;

import java.awt.geom.Line2D;

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
