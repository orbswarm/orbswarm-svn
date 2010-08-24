package com.orbswarm.swarmcon.store;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Point2DAdapter extends XmlAdapter<Point2D.Double, Point2D>
{

  public Double marshal(Point2D v) throws Exception
  {
    return (Point2D.Double)v;
  }

  public Point2D unmarshal(Double v) throws Exception
  {
    return v;
  }
}
