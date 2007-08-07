package com.orbswarm.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;

// abstract mobile object

public class Phantom extends Mobject
{
      /** the amount of windup before moving */

      private double cheeky = 0.125;

      /** origin of phantom */

      private Point2D.Double origin;

      /** target of phantom */

      private Point2D.Double target;

      /** target scale */

      private double targetScale;

      /** original scale */

      private double originScale;

      /** current scale */

      private double scale;

      /** time since phantom was activated */
         
      private double time;

      /** the period overwhich the phatom should move */

      private double period;

      /** mobject that is a phantom of */

      private Mobject mobject;

      /** create a phantom from a given mobject
       *
       * @param mobject Mobject from which to create phantom
       * @param period  period of time overwhich phantom will move
       */

      public Phantom(Mobject mobject, double period)
      {
         super(mobject.getSize());
         this.time = 0;
         this.origin = mobject.getPosition();
         this.setPosition(origin);
         this.target = origin;
         this.period = period;
         this.mobject = mobject;
         this.originScale = 1;
         this.targetScale = 1;
         this.scale = 1;
         this.setMasterAlpha(0.35);
         update(0);
      }
      /** Set target position and scale.  This resets the time.
       *
       * @param position place phantom will move to
       * @param scale size phantom will expand to
       */

      public void setTarget(Point2D.Double position, Double scale)
      {
         this.origin = getPosition();
         this.target = position;
         this.originScale = this.scale;
         this.targetScale = scale;
         this.time = 0;
      }
      /** Is the given point (think mouse click point) eligable to
       * select this object?
       *
       * @param clickPoint the point where the mouse was clicked
       */
      
      public boolean isSelectedBy(Point2D.Double clickPoint)
      {
         return false;
      }
      /** Return mobject that this is a phantom of.
       *
       * @return contained Mobject
       */

      public Mobject getMobject()
      {
         return mobject;
      }
      /** Commpute linear progress of phantom from origin to target.
       *
       * @return a value from 0 to 1
       */

      double progress()
      {
         if (time >= period)
            return 1;

         return time/period;
      }
      /** Compute mapping between linear progress and smooth motion
       * between origin and target.
       *
       * @param progress linear progress
       * @return smooth non-linear motion progress
       */

      public double motion(double progress)
      {
         return .5 + ((sin(3 * progress * PI + PI / 2) * cheeky +
                       sin(progress * PI - PI / 2) * (1 - cheeky)) /
                      (2 * ((1 - cheeky) - cheeky)));
      }
      /** Computes dynamic scale as phantom travels to target.
       *
       * @param progress linear progress
       * @return scale
       */

      public double scale(double progress)
      {
         return (originScale * (1 - progress)) + (progress * targetScale);
      }
      /** Update the position of this phantom.
       *
       * @param time time since last update in seconds
       */
      
      public void update(double time)
      {
         if (isActive())
         {
            // update time

            this.time += time;
         
            // compute linear progress scale and motion

            double progress = progress();
            scale = scale(progress);
            double motion = motion(progress);

            // set position based on motion progress

            setPosition(
               origin.getX() * (1 - motion) + target.getX() * motion,
               origin.getY() * (1 - motion) + target.getY() * motion);
         }
         super.update(time);
      }
      /** Is this phantom activly moving?
       * 
       * @return true if this phantom is still expected to exist
       */

      boolean isActive()
      {
         return mobject.isSelected();
      }
      /** Paint this phantom.
       *
       * @param g graphics object to paint onto
       */

      public void paint(Graphics2D g)
      {
         // record transform and scale

         AffineTransform tmpTransform = g.getTransform();
         g.scale(scale, scale);

         // record alpha and set alpha

         double tmpMasterAlpha = mobject.getMasterAlpha();
         mobject.setMasterAlpha(getMasterAlpha());

         // record and scale current position

         Point2D.Double tmpPos = getPosition();
         setPosition(getX() / scale, getY() / scale);
         
         // paint phantom

         mobject.paint(this, g);

         // restore postion

         setPosition(tmpPos);

         // restore alpha
         
         mobject.setMasterAlpha(tmpMasterAlpha);

         // restore transform

         g.setTransform(tmpTransform);

         // paint any children
         
         super.paint(g);
      }
}
