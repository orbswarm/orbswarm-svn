package org.trebor.swarmcon;

import static org.trebor.swarmcon.SwarmCon.*;
import static java.lang.Math.*;

   /** This class models the motion of an orb.  It may be a simulated
    * motion model or a linkage to a live orb */

abstract public class MotionModel
{
         // ------- vehicle state --------

         /** position of orb */

      private Point position = new Point(0, 0);

         /** yaw of orb */
      
      private double yaw;

         /** pitch of orb */

      private double pitch;

         /** roll of orb */

      private double roll;

         /** velocity of orb */

      private double velocity;

         /** actual direction of travel */

      private double direction;

         // ------- rate control parameters --------

         /** target yaw rate */

      private double targetYawRate;

         /** target velocity */

      private double targetPitchRate;

         // ------- yaw / distance control parameters --------

         /** target yaw */

      private double targetYaw;

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
          * @return yaw reate in degrees per second
          */

      abstract public double getYawRate();

         /** Get the current velocity of the orb.
          *
          * @return velocity in meters per second
          */

      public double getVelocity()
      {
         return velocity;
      }
         /** Set the current velocity of the orb.
          *
          * @param velocity orb velocity in meters per second
          */

      protected void setVelocity(double velocity)
      {
         this.velocity = velocity;
      }
         /** Get the current direction of the orb.
          *
          * @return direction in meters per second
          */

      public double getDirection()
      {
         return direction;
      }
         /** Set the current direction of the orb.
          *
          * @param direction orb direction in meters per second
          */

      protected void setDirection(double direction)
      {
         this.direction = direction;
      }
         /** Command low level rate control.
          *
          * @param targetYawRate target yaw rate
          * @param targetPitchRate target velocity
          */

      public void setTargetRates(double targetYawRate, 
                                 double targetPitchRate)
      {
         this.targetYawRate = targetYawRate;
         this.targetPitchRate = targetPitchRate;
      }
         /** Command yaw and distance.
          *
          * @param targetYaw target yaw
          * @param distanceError error between target and desired distance
          */

      public void setYawDistance(double targetYaw, 
                                 double distanceError)
      {
         this.targetYaw = targetYaw % 360;
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
         /** Get target yaw. */
      
      public double getTargetYaw()
      {
         return targetYaw;
      }
         // get yaw

      public double getYaw()
      {
         return yaw;
      }
         // set yaw

      protected void setYaw(double yaw)
      {
         this.yaw = (yaw + 360) % 360;
      }
         // set delta yaw

      protected void setDeltaYaw(double dYaw)
      {
         setYaw(this.yaw + dYaw);
      }
         // get pitch

      public double getPitch()
      {
         return pitch;
      }
         // set pitch

      protected double setPitch(double pitch)
      {
         double deltaPitch = pitch - this.pitch;
         this.pitch = (pitch + 360) % 360;
         return deltaPitch;
      }
         // set delta pitch

      protected double setDeltaPitch(double dPitch)
      {
         return setPitch(this.pitch + dPitch);
      }
         // get roll

      public double getRoll()
      {
         return roll;
      }
         // set roll

      protected double setRoll(double roll)
      {
         double newRoll = max(min(MAX_ROLL, roll), -MAX_ROLL);
         double deltaRoll = newRoll - this.roll;
         this.roll = newRoll;
         return deltaRoll;
      }
         // set delta roll

      protected double setDeltaRoll(double dRoll)
      {
         return setRoll(this.roll + dRoll);
      }
         // reverse the sense of the vehicle

      public void reverse()
      {
         setYaw(getYaw() + 180);
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

      protected void setPosition(Point position)
      {
         setPosition(position.getX(), position.getY());
      }
         // position setter

      protected void setPosition(double x, double y)
      {
         position.setLocation(x, y);
      }
         // set delta position

      protected void setDeltaPosition(double dX, double dY)
      {
         setPosition(getX() + dX, getY() + dY);
      }      
}
