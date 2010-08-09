/**
 * 
 */
package com.orbswarm.swarmcon.xml;

import java.awt.geom.Point2D;


public class Bar1 extends ABar
{
  public Bar1()
  {
  }

  public Bar1(String name, double x, double y)
  {
    setName(name);
    setPos(new Point2D.Double(x, y));
  }

  public String toString()
  {
    return "Bar1 [getName()=" + getName() + ", getPos()=" + getPos() + "]";
  }
}