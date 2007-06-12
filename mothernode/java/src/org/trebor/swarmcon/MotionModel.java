package org.trebor.swarmcon;

   /** This class models the motion of an orb.  It may be a simulated
    * motion model or a linkage to a live orb */


abstract public class MotionModel
{
         // ------- vehicle state --------

         /** position of orb */

      private Point position = new Point(0, 0);

         /** heading of orb */
      
      private double heading;

         /** velocity of orb */

      private double velocity;

         // ------- rate control parameters --------

         /** target yaw rate */

      private double targetYawRate;

         /** target velocity */

      private double targetVelocity;

         // ------- heading / distance control parameters --------

         /** target heading */

      private double targetHeading;

         /** error between target distance and current distance */

      private double distanceError;

         // ------- position control parameters --------
      
         /** target position */

      private Point targetPosition;
      
         /** Update the state of this model.
          *
          * @param time seconds since last update
          */

      abstract public void update(double time);

         /** Get the yaw rate of the orb.
          *
          * @returns yaw reate in degrees per second
          */

      abstract public double getYawRate();

         /** Get the current velocity of the orb.
          *
          * @returns velocity in meters per second
          */

      abstract public double getVelocity();

         /** Command low level rate control.
          *
          * @param targetYawRate target yaw rate
          * @param targetVelocity target velocity
          */

      public void setTargetRates(double targetYawRate, 
                                 double targetVelocity)
      {
         this.targetYawRate = targetYawRate;
         this.targetVelocity = targetVelocity;
      }
         /** Command heading and distance.
          *
          * @param targetHeading target heading
          * @param distanceError error between target and desired distance
          */

      public void setHeadingDistance(double targetHeading, 
                                     double distanceError)
      {
         this.targetHeading = targetHeading % 360;
         this.distanceError = distanceError;
      }
         /** Command position.
          *
          * @param target target position
          */

      public void setTargetPosition(Point target)
      {
         targetPosition = target;
      }
         /** Get target heading */
      
      public double getTargetHeading()
      {
         return targetHeading;
      }
         // get heading

      public double getHeading()
      {
         return heading;
      }
         // set heading

      protected void setHeading(double heading)
      {
         this.heading = (heading + 360) % 360;
      }
         // reverse the sense of the vehicle

      public void reverse()
      {
         setHeading(getHeading() + 180);
      }
         // positon getter 

      Point getPosition()
      {
         return new Point(getX(), getY());
      }
         // get x position

      double getX()
      {
         return position.getX();
      }
         // get y position

      double getY()
      {
         return position.getY();
      }
         // position setter

      void setPosition(Point position)
      {
         setPosition(position.getX(), position.getY());
      }
         // position setter

      void setPosition(double x, double y)
      {
         this.position.setLocation(x, y);
      }
         // set delta position

      void deltaPosition(double dX, double dY)
      {
         this.position.setLocation(getX() + dX, getY() + dY);
      }      
}
