package org.trebor.swarmcon;

import static java.lang.Math.*;
import static org.trebor.swarmcon.SwarmCon.*;

   /** A simple motion simulation model based on rates */

public class SimModel extends MotionModel
{
         // rates

      private Rate pitchRate = new Rate(
         "pitchRate", -MAX_PITCH_RATE, MAX_PITCH_RATE, DPITCH_RATE_DT);
      private Rate rollRate  = new Rate(
         "roll", -MAX_ROLL_RATE, MAX_ROLL_RATE, DROLL_RATE_DT);

         // controllers

      Controller rollCtrl      = new PDController(0.13f, 10000);
      Controller pitchRateCtrl = new PDController(2f, 1000);

         /** Command low level rate control.
          *
          * @param targetYawRate target yaw rate
          * @param targetPitchRate target pitchRate
          */

      public void setTargetRates(double targetRollRate,
                                 double targetPitchRate)
      {
         super.setTargetRates(targetRollRate, targetPitchRate);
         rollRate.setNormalizedTarget(targetRollRate);
         pitchRate.setNormalizedTarget(targetPitchRate);
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

         rollCtrl.setTarget(yawError < -180
                           ? - (360 - getTargetYaw())
                           : (yawError > 180 
                              ? targetYaw + 360
                              : targetYaw));

            // set pitchRate goal based on error

         pitchRateCtrl.setTarget(0);

            // set target rates

         setTargetRates(rollCtrl.compute(getYaw()),
                        pitchRateCtrl.compute(distanceError));
         
      }
         /** Reverse direction of the orb */

      public void reverse()
      {
         super.reverse();
         pitchRate.setRate(-pitchRate.getRate());
      }
         // update positon

      public void update(double time)
      {
            // update pitch and roll rate

         pitchRate.update(time);
         rollRate .update(time);
               
            // copute delta pitch and roll
         
         double dPitch = pitchRate.getRate() * time;
         double dRoll  =  rollRate.getRate() * time;

            // update absolute pitch and roll

         setDeltaPitch(dPitch);
         rollRate.setRate(setDeltaRoll(dRoll ) / time);

            // compute yaw

         setDeltaYaw(toDegrees(sin(toRadians(getRoll())) * 
                               toRadians(dPitch)));

            //double dYaw = ;
            //System.out.println(" dp: " + dPitch + 
            //" dr: " + dRoll + 
            //" dy: " + dYaw);

            // radius of wide end of the rolling cone

         double p = ORB_RADIUS * cos(toRadians(getRoll()));

            // compute delta x and y

         double dX = toRadians(dPitch) * p * cos(toRadians(getYaw() - 90));
         double dY = toRadians(dPitch) * p * sin(toRadians(getYaw() - 90));
         
            // set position and velocity

         setVelocity(sqrt(pow(dX, 2) + pow(dY, 2)) / time);
         setDeltaPosition(dX, dY);
      }
         /** Get the yaw rate of the orb.
          *
          * @returns yaw reate in degrees per second
          */

      public double getYawRate()
      {
         return rollRate.getRate();
      }
}
