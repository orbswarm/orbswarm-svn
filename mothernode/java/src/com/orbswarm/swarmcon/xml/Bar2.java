/**
 * 
 */
package com.orbswarm.swarmcon.xml;

import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlTransient;


public class Bar2 extends ABar
{
  @XmlTransient
  private long mSize;

  public Bar2()
  {
  }

  public Bar2(String name, double x, double y, long size)
  {
    setName(name);
    setPos(new Point2D.Double(x, y));
    setSize(size);
  }

  public void setSize(long size)
  {
    this.mSize = size;
  }

  public long getSize()
  {
    return mSize;
  }

  public String toString()
  {
    return "Bar2 [getSize()=" + getSize() + ", getName()=" + getName() +
      ", getPos()=" + getPos() + "]";
  }
}