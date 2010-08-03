package com.orbswarm.swarmcon.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.trebor.util.Angle;

public class Head extends ABlock
{
  private GeneralPath mPath = new GeneralPath();
  
  @Override
  public Point2D getEndPosition()
  {
    return getPosition();
  }

  @Override
  public GeneralPath getPath()
  {
    return mPath;
  }

  @Override
  public Angle getEndAngle()
  {
    return new Angle();
  }
}
