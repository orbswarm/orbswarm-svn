package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D.Double;

import com.orbswarm.swarmcon.util.Constants;

public class RenderingConstants
{
  public static final double PATH_WIDTH = Constants.ORB_DIAMETER * 1.5;
  public static final double HEAD_WIDTH = 2;
  public static final Color PATH_COLOR = new Color(64, 0, 0, 128);
  public static final Color SELECTED_PATH_COLOR = new Color(128, 0, 0, 128);
  public static final Color SELECTED_BLOCK_COLOR = new Color(255, 0, 0, 128);
  public static final Stroke BLOCK_STROKE = new BasicStroke(
    (float)PATH_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
  public static final Stroke PATH_STROKE = new BasicStroke((float)PATH_WIDTH,
    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  
  public static final double SYNC_WIDTH = PATH_WIDTH / 10;
  public static final Color SYNC_COLOR = new Color(64, 64, 64);
  public static final Stroke SYNC_STROKE = new BasicStroke((float)SYNC_WIDTH,
    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  
  public static final double MARKER_WIDTH = PATH_WIDTH / 32;
  public static final Color MARKER_COLOR = SYNC_COLOR;
  public static final double MARKER_INSIDE_OFFSET = PATH_WIDTH / 2;
  public static final double MARKER_LENGTH = SYNC_WIDTH / 2;
  public static final Stroke MARKER_STROKE = new BasicStroke((float)MARKER_WIDTH,
    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  public static final Shape MARKER_SHAPE = computeMarkerShape();
  
  public static Shape computeMarkerShape()
  {
    Area rightMark =
      new Area(SYNC_STROKE.createStrokedShape(new Line2D.Double(
        MARKER_INSIDE_OFFSET, 0, MARKER_INSIDE_OFFSET + MARKER_LENGTH, 0)));
    rightMark.add(new Area(MARKER_STROKE
      .createStrokedShape(new Line2D.Double(0, 0, MARKER_INSIDE_OFFSET, 0))));

    Shape leftMark =
      AffineTransform.getRotateInstance(Math.PI).createTransformedShape(
        rightMark);
    Area markerShape = new Area(leftMark);
    markerShape.add(new Area(rightMark));

    return markerShape;
  }
}
