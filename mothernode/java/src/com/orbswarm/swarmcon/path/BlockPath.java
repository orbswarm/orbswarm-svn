package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.vobject.AVobjects;

public class BlockPath extends AVobjects<IBlock> implements IBlockPath
{
  private static final long serialVersionUID = -8911696643772151060L;
  
  ABlock mBlock = new ABlock()
  {
    public void computePath()
    {
      BlockPath.this.computePath();
    }
  };

  public BlockPath()
  {
    computePath();
  }
  
  public Angle getEndAngle()
  {
    return lastElement().getEndAngle();
  }

  public Point2D getEndPosition()
  {
    return lastElement().getEndPosition();
  }

  public void computePath()
  {
    GeneralPath gp = new GeneralPath();
    
    for (IBlock block: this)
      gp.append(block.getPath(), true);
    
    mBlock.setPathShape(gp);
  }
  
  public IBlock getPrevious()
  {
    throw new UnsupportedOperationException();
  }

  public void setPreviouse(IBlock previous)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getPath()
  {
    computePath();
    return mBlock.getPath();
  }
}
