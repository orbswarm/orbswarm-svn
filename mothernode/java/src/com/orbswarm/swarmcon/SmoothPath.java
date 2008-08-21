package com.orbswarm.swarmcon;

import org.trebor.util.Angle;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.Vector;
import java.util.Iterator;

public class SmoothPath extends Vector<Waypoint>
{
   /** Continouse path representing the smooth path */

   GeneralPath continousePath;

   /** indicates that the we have not reached cruise speed */

   public static double NOT_CRUISING = -1;
   
   /** Construct a smoothed path from path which the orb can safely follow.
    *
    * @param velocity the velocity rate constraints
    * @param interval interval to update the path, in seconds
    * @param width width of control points, or roundness of curves
    *
    * @return a smoothed path with waypoints and times
    */

   public SmoothPath(
     Path path, Rate velocity, double interval, 
     double curveWidth, double flatness)
   {
      continousePath = computeContinousePath(path, curveWidth);
      PathIterator pi = continousePath.getPathIterator(null, flatness);

      // variables used during the iteration of the path
      
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

            // add waypoint correct distance along path

            Waypoint wp = new Waypoint(
                        p1.x + (p2.x - p1.x) * segmentPercent,
                        p1.y + (p2.y - p1.y) * segmentPercent,
                        time,
                        velocity.getRate(),
                        new Angle(p1, p2));
            add(wp);
            

            // if the velocity is zero and travel distance is greater
            // then zero, then we have come to a stop, we're done

            if (velocity.getRate() == 0 && travel > 0)
               break;

            // update velocity, time and distance traveled

            velocity.update(interval);
            travel += velocity.getRate() * interval;
            time += interval;
            
            // if we have not yet started slowing down

            if (velocity.getTarget() > 0)
            {
               // if we have reached cruise speed, note the distance
               
               if (cruiseDist == NOT_CRUISING && 
                   velocity.getRate() == velocity.getMax())
                  cruiseDist = travel;
               
               // if we will soon reach the halfway point and not hit
               // cruise, or we're close to the end of the path, start
               // slowing down
               
               if ((cruiseDist == NOT_CRUISING && 
                    travel >= (pathLength / 2)) ||
                   (cruiseDist != NOT_CRUISING && 
                    travel >= (pathLength - cruiseDist)))
               {
                  velocity.setTarget(0);
               }
            }
         }
      }

      // now compute the turn rate

      for (int i = 1; i < this.size() - 1; ++i)
        get(i).setDeltaRadians(get(i - 1), get(i + 1));
   }
   /** Get the continouse path for this smoothed path.
    *
    * @return Continouse general path for this smooth path.
    */

   GeneralPath getContinousePath()
   {
      return continousePath;
   }

   /** Compute the smoothed curves from this path.
    *
    * @param width width between the CubicCurve2D control points on the path.
    * @return a vector of cubic curves which shadow this path.
    */

   public static GeneralPath computeContinousePath(
      Path path, double curveWidth)
   {
      // collection of curves

      Vector<CubicCurve2D.Double> curves = new Vector<CubicCurve2D.Double>();

      // the angel of the preveouse line segment
      
      Angle oldAngle = null;
      
      // work through the points
      
      for (int i = 0; i < path.size() - 1; ++i)
      {
         // get end points of the line segment
         
         Point p1 = path.get(i);
         Point p2 = path.get(i + 1);

         // establish angle of this line

         Angle angle = new Angle(p1, p2);

         // assume the control points are at the end points

         Point cp1 = p1;
         
         // if this is NOT the first segment
         
         if (oldAngle != null)
         {
            // bisect the angle of this line and the previouse one
            
            Angle cpAngle = angle.bisect(oldAngle);
            
            // rotate bisection by 90 degrees
            
            cpAngle.setDeltaAngle(-90);

            // compute control point at this intersection

            cp1 = new Point(cpAngle.cartesian(
                               curveWidth / 2, false,
                               p1.x, p1.y));

            // compute new control point at intersection with old curve
            
            cpAngle.setDeltaAngle(180);
            Point cpOld = new Point(cpAngle.cartesian(
                                       curveWidth / 2, false,
                                       p1.x, p1.y));

            // get the older curve

            CubicCurve2D.Double oldCurve = curves.lastElement();

            // fix old curve to smoothly curve around this point

             oldCurve.setCurve(
                oldCurve.getX1(),
                oldCurve.getY1(),
                oldCurve.getCtrlX1(),
                oldCurve.getCtrlY1(),
                cpOld.x,
                cpOld.y,
                oldCurve.getX2(),
                oldCurve.getY2());
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
      }
      
      // convert curves into one continouse path

      GeneralPath continousePath = new GeneralPath();
      for(CubicCurve2D.Double curve: curves)
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
      final double curveWidth = 120;

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
                                 (double)rnd.nextInt(clip.width),
                                 (double)rnd.nextInt(clip.height)));
                  
                  g.setColor(java.awt.Color.BLACK);
                  for (int i = 0; i < count - 1; ++i)
                     g.drawLine(
                        (int)path.get(i).x,
                        (int)path.get(i).y,
                        (int)path.get(i + 1).x,
                        (int)path.get(i + 1).y);


                  
                  SmoothPath smoothPath = new SmoothPath(
                    path, rate, 1.0d, curveWidth, 1.0);
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
