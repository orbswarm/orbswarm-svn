package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;

import com.orbswarm.swarmcon.Constants;

public class RenderingConstants
{
  public static final double PATH_WIDTH = Constants.ORB_DIAMETER * 1.5;
  public static final double HEAD_WIDTH = 2;
  public static final Color PATH_COLOR = new Color(64, 0, 0, 128);
  public static final Color SELECTED_PATH_COLOR = new Color(128, 0, 0, 128);
  public static final Color SELECTED_BLOCK_COLOR = new Color(192, 0, 0, 128);
  public static final Stroke PATH_STROKE =
    new BasicStroke((float)PATH_WIDTH, BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_ROUND);
}
