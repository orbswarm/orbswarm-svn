package com.orbswarm.swarmcon;

import org.trebor.util.Angle;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.Vector;
import java.util.Iterator;

import static org.trebor.util.Angle.Type.*;
import static java.lang.Math.*;

public class SmoothPath extends Vector<Waypoint>
{
  /** Continouse path representing the smooth path */

  private GeneralPath continousePath;

  /** Continouse path representing the smooth path */

  Vector<CubicCurve2D.Double> curves;

  /** The current waypoint being processed. */

  private Waypoint currentWaypoint;

  /** The path of origonal targets */

  private Path targets;

  /** indicates that the we have not reached cruise speed */

  public static double NOT_CRUISING = -1;

  /** the angle range to the left or right of the orb for which an
   * initial point must be added to swoop the orb around correctly. */

  public static Angle BEHIND_RANGE = new Angle(60, DEGREES);

  /** Construct a smoothed path from path which the orb can safely
   * follow.  The initial direction is assumed to be that of the first
   * two points of the path, and the headroom is assumed to be 5 units.
   *
   * @param path the path of target points to generat a smooth path from
   * @param velocity the velocity rate constraints
   * @param interval interval to update the path, in seconds
   * @param smoothness the amount of smoothing which occurs at the targets
   * @param flatness the granularity of the smoothed path, smaller
   * numbers produce smoother paths
   *
   * @return a smoothed path with waypoints and times
   */

  public SmoothPath(
    Path path, Rate velocity, double interval,
    double smoothness, double flatness)
  {
    this(path, velocity, interval, smoothness, flatness, new Angle(
      path.get(0), path.get(1)), 5);
  }

  /** Construct a smoothed path from path which the orb can safely follow.
   *
   * @param path the path of target points to generat a smooth path from
   * @param velocity the velocity rate constraints
   * @param interval interval to update the path, in seconds
   * @param smoothness the amount of smoothing which occurs at the targets
   * @param flatness the granularity of the smoothed path, smaller
   * numbers produce smoother paths
   * @param initialDirection the initial direction the path should start in
   * @param headRoom the distance from the orb to place a point to give
   * the orb some room turn
   *
   * @return a smoothed path composed of waypoints
   */

  public SmoothPath(
    Path path, Rate velocity, double interval,
    double smoothness, double flatness, Angle initialDirection, double headRoom)
  {
    // if the second target is directly behind the orb, we're gonna need
    // to add a point inbetween to be sure the orb can take a nice
    // sweeping path to get there
    
    Angle behindness = initialDirection
      .difference(new Angle(path.get(0), path.get(1)));
    boolean isBehind = abs(behindness.as(DEGREE_RATE)) > BEHIND_RANGE.as(DEGREES);
    
    if (isBehind)
    {
      boolean isRight = behindness.compareTo(new Angle(180, HEADING_RATE)) > 0;
      Angle a = new Angle(isRight ? 60 : 300, HEADING_RATE);
      a = a.rotate(initialDirection);
      Point p = new Point(a.cartesian(headRoom, path.get(0)));
      double speed = (path.get(0).getSpeed() + path.get(1).getSpeed()) / 2;
      path.add(1, new Target(p, speed));
    }

    // record the source targets for this path

    this.targets = path;

    // compute the smoothed continouse path to work from

    continousePath = computeContinousePath(path, smoothness, initialDirection, 
      curves = new Vector<CubicCurve2D.Double>());
    PathIterator pi = continousePath.getPathIterator(null, flatness);

    // variables used during the iteration along the path

    Vector<Line2D> lines  = new Vector<Line2D>();
    double[] choords = new double[6];
    double   pathLength = 0;
    Point    previouse = null;
    Point    p = null;
    Point    open = null;

    // iterate along the path to compute length and collect line segments

    while (!pi.isDone())
    {
      int type = pi.currentSegment(choords);
      switch (type)
      {
        case PathIterator.SEG_MOVETO:
          previouse = new Point(choords[0], choords[1]);
          open = previouse;
          break;
        case PathIterator.SEG_LINETO:
          p = new Point(choords[0], choords[1]);
          lines.add(new Line2D.Double(previouse, p));
          pathLength += previouse.distance(p);
          previouse = p;
          break;
        case PathIterator.SEG_CLOSE:
          lines.add(new Line2D.Double(previouse, open));
          pathLength += previouse.distance(open);
          previouse = open;
          break;
      }
      pi.next();
    }

    // start at time zero and speed zero and traveled zero

    double time = 0;
    velocity.setRate(0);
    double travel = 0;
    double cruiseDist = NOT_CRUISING;

    // total length of segments we've travled is zero

    double totalSegmentLen = 0;

    // assume we want to go as fast as we can

    velocity.setTarget(velocity.getMax());

    // work through all the segments in the curve

    for (Line2D segment: lines)
    {
      // establish segment metrics

      Point p1 = new Point(segment.getP1());
      Point p2 = new Point(segment.getP2());
      double segmentLen = p1.distance(p2);
      totalSegmentLen += segmentLen;

      // while we have not over stepped the segments, compute waypoints

      while (travel < totalSegmentLen)
      {
        // compute the percentage down the segment to travel

        double segmentPercent =
          (travel - (totalSegmentLen - segmentLen)) / segmentLen;

        // add waypoint the correct distance along path

        Waypoint wp = new Waypoint(
          p1.x + (p2.x - p1.x) * segmentPercent,
          p1.y + (p2.y - p1.y) * segmentPercent,
          time,
          velocity.getRate(),
          new Angle(p1, p2));
        add(wp);

        // update velocity, time and distance traveled

        velocity.update(interval);
        travel += velocity.getRate() * interval;
        time += interval;

        // if the velocity is zero and travel distance is greater
        // then zero, then we have come to a stop, we're done

        if (velocity.getRate() == 0 && travel > 0)
          break;

        // if we have not yet started slowing down

        if (velocity.getTarget() > 0)
        {
          // if we have reached cruise speed, note the distance

          if (
            cruiseDist == NOT_CRUISING &&
            velocity.getRate() == velocity.getMax())
          {
            cruiseDist = travel;
          }

          // if we will soon reach the halfway point and not hit
          // cruise, or we're close to the end of the path, start
          // slowing down

          if (
            (cruiseDist == NOT_CRUISING && travel >= (pathLength / 2)) ||
            (cruiseDist != NOT_CRUISING && travel >= (pathLength - cruiseDist)))
          {
            velocity.setTarget(0);
          }
        }
      }

      // if the velocity is zero, we've stopped, we don't need to
      // process more line segments

      if (velocity.getRate() == 0 && travel > 0)
        break;
    }

    // now compute the turn rate

    for (int i = 0; i < this.size(); ++i)
    {
      Waypoint prev = i > 0 ? get(i - 1) : null;
      Waypoint next = i < this.size() - 1 ? get(i + 1) : null;
      get(i).setNextPrevious(next, prev);
    }
  }

  /** Set the current waypoint which is being commanded to the orb. 
   *
   * @param currentWaypoint current waypoint
   */

  public void setCurrentWaypoint(Waypoint currentWaypoint)
  {
    this.currentWaypoint = currentWaypoint;
  }

  /** Get the curren waypoint.
   *
   * @return the current waypoint being commanded to the orb.
   */
  
  public Waypoint getCurrentWaypoint()
  {
    return currentWaypoint;
  }

  /** Get the continouse path for this smoothed path.
   *
   * @return Continouse general path for this smooth path.
   */

  public GeneralPath getContinousePath()
  {
    return continousePath;
  }

  /** Get the curves used for this smoothed path.
   *
   * @return Curves that this smooth path is composed of.
   */

  public Vector<CubicCurve2D.Double> getCurves()
  {
    return curves;
  }

  /** Get the targers used to generate this path path.
   *
   * @return path containing targets.
   */

  public Path getTargets()
  {
    return targets;
  }

  /** Compute the smoothed curves from this path.
   *
   * @param path the path containing the source targets
   * @param smoothness the amount of smoothing which occurs at the targets
   * @param initialDirection the initial direction the path should start 
   * @param curves a vector of cubic curves in which to put the curves
   * the smooth path is composed of
   *
   * @return the continouse path to append the smooth curves too
   */

  public static GeneralPath computeContinousePath(
    Path path, double smoothness, Angle initialDirection, 
    Vector<CubicCurve2D.Double> curves)
  {
    GeneralPath continousePath = new GeneralPath();

    // the angel of the preveouse line segment

    Angle oldAngle = null;

    // the lenght of the preveouse line segment

    double oldLength = 0;

    // work through the points

    for (int i = 0; i < path.size() - 1; ++i)
    {
      // get end points of the line segment

      Point p1 = path.get(i);
      Point p2 = path.get(i + 1);
      double length = p1.distance(p2);
      
      // establish angle of this line

      Angle angle = new Angle(p1, p2);

      // define a place for the first control point

      Point cp1;

      // if this is the first segment the control point is headed in the
      // initial direction

      if (i == 0)
        cp1 = new Point(initialDirection.cartesian(length * smoothness, p1));
      
      // else this is NOT the first segment

      else
      {
        // bisect the angle of this line and the previouse one

        Angle cpAngle = angle.bisect(oldAngle);

        // compute control point at this intersection

        cp1 = new Point(cpAngle.cartesian(length * smoothness, p1));

        // compute new control point at intersection with old curve

        cpAngle = cpAngle.rotate(180, DEGREES);
        Point cpOld = new Point(cpAngle.cartesian(oldLength * smoothness, p1));

        // get the older curve

        CubicCurve2D.Double oldCurve = curves.lastElement();

        // fix old curve to smoothly curve around this point

        oldCurve.setCurve(
          oldCurve.getP1(),
          oldCurve.getCtrlP1(),
          cpOld,
          oldCurve.getP2());
        
      }

      // create curve from end points and control points

      CubicCurve2D.Double curve = new CubicCurve2D.Double(
        p1 .x, p1 .y,
        cp1.x, cp1.y,
        p2 .x, p2 .y,
        p2 .x, p2 .y);

      // add curve to curves

      curves.add(curve);

      // record this angle for later use

      oldAngle = angle;

      // record the old length
      
      oldLength = length;
    }

    // convert curves into one continouse path

    for (CubicCurve2D.Double curve: curves)
      continousePath.append(curve, true);

    // return path

    return continousePath;
  }

  /**
   * return the duration (double seconds) of the path.
   */
  public double getDuration()
  {
    // the arrival time of the last waypoint is essentially the
    // duration of the SmoothPath (the 1st waypoint's time is 0)
    double duration = 0.;
    if (this.size() > 0)
    {
      Waypoint theEnd = (Waypoint)this.lastElement();
      duration = theEnd.getTime();
    }
    return duration;
  }

  /** Test path code. */

  public static void main(String[] args)
  {
    final int count = 5;
    final double smoothness = 120;

    final Rate rate = new Rate("test", 0, 30, 1);
    final java.util.Random rnd = new java.util.Random();

    javax.swing.JFrame frame = new javax.swing.JFrame();
    javax.swing.JPanel panel = new javax.swing.JPanel()
      {
          public void paint (java.awt.Graphics graphics)
          {
            java.awt.Graphics2D g = (java.awt.Graphics2D)graphics;
            g.setRenderingHint(
              java.awt.RenderingHints.KEY_ANTIALIASING,
              java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Rectangle clip = g.getClipBounds();
            g.setColor(java.awt.Color.WHITE);
            g.fill(clip);

            Path path = new Path();
            for (int i = 0; i < count; ++i)
              path.add(new Target(
                (double)rnd.nextInt(
                  clip.width / 2) + clip.width / 4,
                (double)rnd.nextInt(
                  clip.height / 2) + clip.height / 4));

            g.setColor(java.awt.Color.BLACK);
            for (int i = 0; i < count - 1; ++i)
              g.drawLine(
                (int)path.get(i).x,
                (int)path.get(i).y,
                (int)path.get(i + 1).x,
                (int)path.get(i + 1).y);



            SmoothPath smoothPath = new SmoothPath(
              path, rate, 1.0d, smoothness, 1.0);
            g.setColor(new java.awt.Color(0, 0, 0, 64));
            g.setStroke(new java.awt.BasicStroke(
              20,
              java.awt.BasicStroke.CAP_ROUND,
              java.awt.BasicStroke.JOIN_ROUND));
            g.draw(smoothPath.getContinousePath());

            g.setStroke(new java.awt.BasicStroke(
              40,
              java.awt.BasicStroke.CAP_ROUND,
              java.awt.BasicStroke.JOIN_ROUND));
            g.setColor(new java.awt.Color(0, 0, 255, 64));
            g.drawLine(
              (int)smoothPath.get(0).x,
              (int)smoothPath.get(0).y,
              (int)smoothPath.get(0).x,
              (int)smoothPath.get(0).y);

            g.setStroke(new java.awt.BasicStroke(
              10,
              java.awt.BasicStroke.CAP_ROUND,
              java.awt.BasicStroke.JOIN_ROUND));
            g.setColor(new java.awt.Color(255, 0, 0, 64));
            for (Waypoint p: smoothPath)
              g.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
          }
      };

    frame.add(panel);
    frame.setSize(400, 400);
    frame.setVisible(true);
  }
}
