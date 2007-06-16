package org.trebor.swarmcon;

import static java.lang.Math.*;
import static org.trebor.swarmcon.SwarmCon.*;

   /** A simple motion simulation model based on rates */

public class SimpleSimModel extends MotionModel
{
         // rates

      private Rate velocity = 
         new Rate("velocity", -MAX_VELOCITY, MAX_VELOCITY, DVELOCITY_DT);
      private Rate yawRate  = 
         new Rate("yaw", -MAX_YAW_RATE, MAX_YAW_RATE, DYAW_RATE_DT);

         // controllers

      Controller yawCtrl      = new PDController(0.13f, 10000);
      Controller velocityCtrl = new PDController(2f, 1000);

         /** Command low level rate control.
          *
          * @param targetYawRate target yaw rate
          * @param targetVelocity target velocity
          */

      public void setTargetRates(double targetYawRate, 
                                 double targetVelocity)
      {
         super.setTargetRates(targetYawRate, targetVelocity);
         yawRate.setNormalizedTarget(targetYawRate);
         velocity.setNormalizedTarget(targetVelocity);
      }
         /** Command yaw and distance.
          *
          * @param targetYaw target yaw
          * @param distanceError error between target and desired distance
          */

      public void setYawDistance(double targetYaw, double distanceError)
      {
         super.setYawDistance(targetYaw, distanceError);

            // compute yaw error

         double yawError = (getYaw() - getTargetYaw());

            // if it would be better to reverse direction, do so
         
         if (abs(yawError) > 90 && abs(yawError) < 270)
               reverse();

            // set yaw goal correcting for yaw wrap around

         yawCtrl.setTarget(yawError < -180
                           ? - (360 - getTargetYaw())
                           : (yawError > 180 
                              ? targetYaw + 360
                              : targetYaw));

            // set velocity goal based on error

         velocityCtrl.setTarget(0);

            // set target rates

         setTargetRates(yawCtrl.compute(getYaw()),
                        velocityCtrl.compute(distanceError));
         
      }
         /** Reverse direction of the orb */

      public void reverse()
      {
         super.reverse();
         velocity.setRate(-velocity.getRate());
      }
         // update positon

      public void update(double time)
      {
            // update velocity and yaw rate

         velocity.update(time);
         yawRate.update(time);
               
            // if moving, update yaw and position

         if (velocity.getRate() != 0)
         {
            setYaw(getYaw() + yawRate.getRate() * time);
            setDeltaPosition(
               velocity.getRate() * time * 
               cos(toRadians(getYaw() - 90)),
               velocity.getRate() * time * 
               sin(toRadians(getYaw() - 90)));
         }
      }
         /** Get the yaw rate of the orb.
          *
          * @return yaw reate in degrees per second
          */

      public double getYawRate()
      {
         return yawRate.getRate();
      }
         /** Get the current velocity of the orb.
          *
          * @return velocity in meters per second
          */

      public double getVelocity()
      {
         return velocity.getRate();
      }
}
