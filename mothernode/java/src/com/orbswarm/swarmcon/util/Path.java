package com.orbswarm.swarmcon.util;

import java.awt.Shape;
import java.awt.geom.PathIterator;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.path.ABlock;

public class Path
{
  private static Logger log = Logger.getLogger(ABlock.class);

  public static double[] computePathTransfrom(Shape path)
  {
    PathIterator pi = path.getPathIterator(null);
    double[] coords = new double[6];

    double x1 = 0;
    double y1 = 0;
    double x2 = 0;
    double y2 = 0;

    while (!pi.isDone())
    {
      int type = pi.currentSegment(coords);
      switch (type)
      {
      case PathIterator.SEG_MOVETO:
        log.debug(String.format(" MOVE: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = x2;
        y1 = y2;
        x2 = coords[0];
        y2 = coords[1];
        break;
      case PathIterator.SEG_LINETO:
        log.debug(String.format(" LINE: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = x2;
        y1 = y2;
        x2 = coords[0];
        y2 = coords[1];
        break;
      case PathIterator.SEG_QUADTO:
        log.debug(String.format(" QUAD: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = coords[0];
        y1 = coords[1];
        x2 = coords[2];
        y2 = coords[3];
        break;
      case PathIterator.SEG_CUBICTO:
        log.debug(String.format("CUBIC: %fx%f, %fx%f, %fx%f", coords[0],
          coords[1], coords[2], coords[3], coords[4], coords[5]));
        x1 = coords[2];
        y1 = coords[3];
        x2 = coords[4];
        y2 = coords[5];
        break;
      default:
        throw new Error("unknown path element type: " + type);
      }

      pi.next();
    }

    double[] result = {x1, y1, x2, y2};
    return result;
  }
}
