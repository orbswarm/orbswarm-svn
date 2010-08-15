/**
 * 
 */
package com.orbswarm.swarmcon.xml;

import java.awt.Shape;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.orbswarm.swarmcon.path.BlockState;
import com.orbswarm.swarmcon.path.BlockPathBuilder.Point2DAdapter;

@XmlSeeAlso(
{
  Bar1.class, Bar2.class
})
public abstract class ABar implements IBar
{
  private String name;

  @XmlTransient
  private Point2D mPos;

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  @XmlJavaTypeAdapter(Point2DAdapter.class)
  public void setPos(Point2D pos)
  {
    this.mPos = pos;
  }

  public Point2D getPos()
  {
    return mPos;
  }

  public void computePath()
  {
    // TODO Auto-generated method stub
    
  }

  public BlockState getDeltaState()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public double getLength()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public Shape getPath(BlockState startState)
  {
    // TODO Auto-generated method stub
    return null;
  }
}