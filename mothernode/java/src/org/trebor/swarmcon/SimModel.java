package org.trebor.swarmcon;

import static java.lang.Math.*;
import static org.trebor.swarmcon.SwarmCon.*;

   /** A simple motion simulation model based on rates */

public class SimModel extends MotionModel
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
         /** Command heading and distance.
          *
          * @param targetHeading target heading
          * @param distanceError error between target and desired distance
          */

      public void setHeadingDistance(double targetHeading, 
                                     double distanceError)
      {
         super.setHeadingDistance(targetHeading, distanceError);

            // compute heading error

         double headingError = (getHeading() - getTargetHeading());

            // if it would be better to reverse direction, do so
         
         if (abs(headingError) > 90 && abs(headingError) < 270)
               reverse();

            // set heading goal correcting for heading wrap around

         yawCtrl.setTarget(headingError < -180
                           ? - (360 - getTargetHeading())
                           : (headingError > 180 
                              ? targetHeading + 360
                              : targetHeading));

            // set velocity goal based on error

         velocityCtrl.setTarget(0);

            // set target rates

         setTargetRates(yawCtrl.compute(getHeading()),
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
               
            // if moving, update heading and position

         if (velocity.getRate() != 0)
         {
            setHeading(getHeading() + yawRate.getRate() * time);
            deltaPosition(
               velocity.getRate() * time * 
               cos(toRadians(getHeading() - 90)),
               velocity.getRate() * time * 
               sin(toRadians(getHeading() - 90)));
         }
      }
         /** Get the yaw rate of the orb.
          *
          * @returns yaw reate in degrees per second
          */

      public double getYawRate()
      {
         return yawRate.getRate();
      }
         /** Get the current velocity of the orb.
          *
          * @returns velocity in meters per second
          */

      public double getVelocity()
      {
         return velocity.getRate();
      }
}
