package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static com.orbswarm.swarmcon.SwarmCon.*;
import static java.lang.Math.*;

   /** This class models the motion of an orb.  It may be a simulated
    * motion model or a linkage to a live orb */

abstract public class MotionModel
{
         // ------- vehicle state --------

         /** position of orb */

      private Point position = new Point(0, 0);

         /** yaw of orb */
      
      private Angle yaw = new Angle();

         /** pitch of orb */

      private Angle pitch = new Angle();

         /** roll of orb */

      private Angle roll = new Angle();

         /** velocity of orb */

      private double velocity;

         /** direction of travel */

      private Angle direction = new Angle();

         /** yaw rate */

      private double yawRate;

         // ------- pitch roll rate control parameters --------

         /** target roll rate */

      private double targetRollRate;

         /** target pitch rate */

      private double targetPitchRate;

         // ------- yaw rate velocity control parameters --------

         /** target yaw rate */

      private double targetYawRate;

         /** target pitch rate */

      private double targetVelocity;

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
          * @return yaw rate in degrees per second
          */

      public double getYawRate()
      {
         return yawRate;
      }
         /** Set the yaw rate of the orb.
          *
          * @param yawRate yaw rate in degrees per second
          */

      protected void setYawRate(double yawRate)
      {
         this.yawRate = yawRate;
      }
         /** Get the current speed of the orb. This takes into account
          * which way the orb is facing and will return negitive values
          * if the orb is backing up.
          *
          * @return velocity in meters per second
          */

      public double getSpeed()
      {
         return abs(yaw.difference(direction).degrees()) < 90
            ? getVelocity()
            : -getVelocity();
      }
         /** Get the current velocity of the orb. Always returns a
          * postive value.
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
         return direction.degrees();
      }
         /** Set the current direction of the orb.
          *
          * @param direction orb direction (headign in degrees)
          */

      protected void setDirection(double direction)
      {
         this.direction.setAngle(direction);
      }
         /** Command low level rate control.
          *
          * @param targetRollRate target roll rate
          * @param targetPitchRate target velocity
          */

      public void setTargetRollPitchRates(double targetRollRate, 
                                          double targetPitchRate)
      {
         this.targetRollRate = targetRollRate;
         this.targetPitchRate = targetPitchRate;
      }
         /** Command target yaw rate and velocity.
          *
          * @param targetYawRate target yaw rate
          * @param targetVelocity target velocity
          */

      public void setTargetYawRateVelocity(double targetYawRate, 
                                          double targetVelocity)
      {
         this.targetYawRate = targetYawRate;
         this.targetVelocity = targetVelocity;
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
         return yaw.degrees();
      }
         // set yaw

      protected void setYaw(double yaw)
      {
         this.yaw.setAngle(yaw);
      }
         // set delta yaw

      protected void setDeltaYaw(double dYaw)
      {
         yaw.setDeltaAngle(dYaw);
      }
         // get pitch

      public double getPitch()
      {
         return pitch.degrees();
      }
         // set pitch

      protected double setPitch(double pitch)
      {
         double deltaPitch = pitch - this.pitch.degrees();
         this.pitch.setAngle(pitch);
         return deltaPitch;
      }
         // set delta pitch

      protected double setDeltaPitch(double dPitch)
      {
         return setPitch(this.pitch.degrees() + dPitch);
      }
         // get roll

      public double getRoll()
      {
         return roll.degrees();
      }
         // set roll

      protected double setRoll(double roll)
      {
         double newRoll = max(min(MAX_ROLL, roll), -MAX_ROLL);
         double deltaRoll = newRoll - this.roll.degrees();
         this.roll.setAngle(newRoll);
         return deltaRoll;
      }
         // set delta roll

      protected double setDeltaRoll(double dRoll)
      {
         return setRoll(this.roll.degrees() + dRoll);
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
