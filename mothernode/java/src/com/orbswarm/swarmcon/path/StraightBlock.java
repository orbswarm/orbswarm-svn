package com.orbswarm.swarmcon.path;

import java.awt.geom.Line2D;

import org.trebor.util.Angle;

public class StraightBlock extends ABlock
{
  private double mLength;

  public StraightBlock(IBlock previous, double length)
  {
    super(previous);
    mLength = length;
    setDeltaAngle(new Angle());
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

  public void computePath()
  {
    setPathShape(new Line2D.Double(0, 0, 0, getLength()));
  }
}
