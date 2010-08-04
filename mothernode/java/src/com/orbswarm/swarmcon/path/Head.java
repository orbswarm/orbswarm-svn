package com.orbswarm.swarmcon.path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.trebor.util.Angle;

public class Head extends ABlock
{
  public Head()
  {
    computePath();
  }
  
  @Override
  public Point2D getEndPosition()
  {
    return getPosition();
  }
  
  @Override
  public Angle getEndAngle()
  {
    return new Angle();
  }

  public void computePath()
  {
    setPathShape(new GeneralPath());
  }
}
