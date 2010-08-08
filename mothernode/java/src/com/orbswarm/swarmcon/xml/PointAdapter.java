package com.orbswarm.swarmcon.xml;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PointAdapter extends XmlAdapter<Point2D.Double, Point2D>
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
