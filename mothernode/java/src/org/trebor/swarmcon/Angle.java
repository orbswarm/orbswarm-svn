package org.trebor.swarmcon;

import static java.lang.Math.*;

   /** Angle is a class to handle math for angular values.  It ensures
    * that the internal value are alwasy -360 to +360 degrees (-2PI to
    * 2PI) and handles issues of wrap-around and conversions beteween
    * degrees, radians.  It also understand that a heading (0 degrees ==
    * north) is different from the standard unit cirlce (0 degrees =
    * east).
    */

public class Angle
{
         /** The anglar value internally stored as a double in degrees. */

      private double angle;

         /** Default Angle constructor. */

      public Angle()
      {
      }
         /** Angle constructor which accepts degrees.
          *
          * @param angle angle in degrees
          */

      public Angle(double angle)
      {
         setAngle(angle, false);
      }
         /** Angle constructor which accepts radians or degrees.
          *
          * @param angle angle in radians or degrees
          * @param radians if true, interperets 'angle' as radians, else
          * degrees
          */

      public Angle(double angle, boolean radians)
      {
         setAngle(angle, radians);
      }
         /** Angle constructor which accepts the slope of a line.
          *
          * @param deltaX the change in X value along a line segment
          * @param deltaX the change in Y value along a line segment
          */

      public Angle(double deltaX, double deltaY)
      {
         setAngle(deltaX, deltaY);
      }
         /** Sets the value of this angle in degrees.
          *
          * @param angle angle in degrees
          */

      public void setAngle(double angle)
      {
         this.angle = angle % 360;
      }
         /** Sets the value of this angle.  It accepts radians or degrees.
          *
          * @param angle angle in radians or degrees
          * @param radians if true, interperets 'angle' as radians, else
          * degrees
          */

      public void setAngle(double angle, boolean radians)
      {
         setAngle(radians ? toDegrees(angle) : angle);
      }
         /** Sets the value of this angle to the slope of a line.
          *
          * @param deltaX the change in X value along a line segment
          * @param deltaX the change in Y value along a line segment
          */

      public void setAngle(double deltaX, double deltaY)
      {
         setAngle(atan2(deltaY, deltaY), true);
      }
         /** Change angle by some delta in degrees.
          *
          * @param delta amount to change angel by in degrees
          */

      public void setDeltaAngle(double delta)
      {
         setAngle(degrees() + delta);
      }
         /** Change angle by some delta in radians or degrees.
          *
          * @param delta amount to change angel by
          * @param radians if true, interperets 'delta' as radians, else
          * degrees
          */

      public void setDeltaAngle(double delta, boolean radians)
      {
         setDeltaAngle(radians ? toDegrees(delta) : delta);
      }
         /** Returns the angular value in degrees.
          *
          * @return the angular value in degrees
          */

      public double degrees()
      {
         return angle;
      }
         /** Returns the angular value in radians.
          *
          * @return the angular value in radians
          */

      public double radians()
      {
         return toRadians(angle);
      }
         /** Convert polar coodinates to cartesian (rectangular)
          * coodinates.
          *
          * @param radius radius of polar coodinate
          * @param isHeading corrects math if this is a heading
          * @param deltaX x offset in cartesian space
          * @param deltaY y offset in cartesian space
          *
          * @return point in cartesian space
          */

      public Point cartesian(double radius, boolean isHeading,
                             double deltaX, double deltaY)
      {
         return cartesian(degrees(), false, 
                          radius, isHeading,
                          deltaX, deltaY);
      }
         /** Convert polar coodinates to cartesian (rectangular)
          * coodinates.
          *
          * @param angle angle of polar coodinate
          * @param radius radius of polar coodinate
          * @param isHeading corrects math if this is a heading
          * @param deltaX x offset in cartesian space
          * @param deltaY y offset in cartesian space
          *
          * @return point in cartesian space
          */

      public static Point cartesian(double angle, boolean radians,
                                    double radius, boolean isHeading,
                                    double deltaX, double deltaY)
      {
         angle = radians ? angle : toRadians(angle);
         angle = isHeading ? PI / 2 - angle : angle;
         return new Point(deltaX + radius * cos(angle),
                          deltaY + radius * sin(angle));
      }
         /** Convert polar coodinates to cartesian (rectangular)
          * coodinates.
          *
          * @param radius radius of polar coodinate
          *
          * @return point in cartesian space
          */

      public Point cartesian(double radius)
      {
         return cartesian(radius, false, 0, 0);
      }
         /** Compute difference between this and another angel in degrees.
          *
          * @param other other angle to compute difference to, in degrees
          *
          * @return difference between this angle and the other.
          */

      public Angle difference(double angle, boolean radians)
      {
         return difference(this, new Angle(angle, radians));
      }
         /** Compute difference between this and another angel.
          *
          * @param other other angle to compute difference to.
          *
          * @return difference between this angle and the other.
          */

      public Angle difference(Angle other)
      {
         return difference(this, other);
      }
         /** Compute difference between two angles which handles wrap
          * around.
          *
          * @param angle1 angle to compute difference from
          * @param angle2 angle to compute difference to
          *
          * @return difference between angle1 and angle2
          */

      public static Angle difference(Angle angle1, Angle angle2)
      {
         return new Angle(difference(angle1.degrees(),
                                     angle2.degrees(),
                                     false));
      }
         /** Compute difference between two angles which handles wrap
          * around.
          *
          * @param angle1 angle to compute difference from
          * @param angle2 angle to compute difference to
          * @param radians true if angles are in radians
          *
          * @return difference between angle1 and angle2
          */

      public static double difference(double angle1, double angle2,
                                      boolean radians)
      {
         return radians 
            ? toRadians(difference(toDegrees(angle1), toDegrees(angle2)))
            : difference(angle1, angle2);
      }
         /** Compute difference between two angles which handles wrap
          * around.
          *
          * @param angle1 angle to compute difference from, in degreees
          * @param angle2 angle to compute difference to, in degreees
          *
          * @return difference between angle1 and angle2 in degrees
          */

      public static double difference(double angle1, double angle2)
      {
         double delta = (angle2 % 360) - (angle1 % 360);

         if (delta < -180)
            return 360 + delta;

         if (delta > 180)
            return delta - 360;

         return delta % 360;
      }
         /** Convert angel to a string.
          *
          * @return the value is the angle in degrees followed by the
          * word "degrees".
          */

      public String toString()
      {
         return degrees() + " degrees";
      }
         /** Some crapy unit tests. */

      public static void unitTest()
      {
         Angle a1 = new Angle(10);
         Angle a2 = new Angle(350);
         Angle a3 = new Angle(80);
         System.out.println(": " + (a1.degrees() == 10));
         System.out.println(": " + (a2.degrees() == 350));
         System.out.println(": " + (a3.degrees() == 80));
         System.out.println(": " + (a2.difference(a1).degrees() == 20));
         System.out.println(": " + (a1.difference(a2).degrees() == -20));
         System.out.println(": " + (a1.difference(a3).degrees() == 70));
         System.out.println(": " + (a3.difference(a1).degrees() == -70));
         System.out.println(": " + (a2.difference(a3).degrees() == 90));
         System.out.println(": " + (a3.difference(a2).degrees() == -90));
         System.out.println(": " + (a3.difference(a2).radians()));
         System.out.println(": " + new Angle(-45).cartesian(3, true, 0, 0));
         System.out.println(": " + new Angle(0)  .cartesian(3, true, 0, 0));
         System.out.println(": " + new Angle(45) .cartesian(3, true, 0, 0));
         System.out.println(": " + new Angle(90) .cartesian(3, true, 0, 0));
         System.out.println(": " + new Angle(-45).cartesian(3, false, 0, 0));
         System.out.println(": " + new Angle(0)  .cartesian(3, false, 0, 0));
         System.out.println(": " + new Angle(45) .cartesian(3, false, 0, 0));
         System.out.println(": " + new Angle(90) .cartesian(3, false, 0, 0));
      }
}