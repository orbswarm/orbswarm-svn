package org.trebor.swarmcon;

import static java.lang.Math.*;
import static org.trebor.swarmcon.SwarmCon.*;

   /** A simple motion simulation model based on rates */

public class SimModel extends MotionModel
{
         /** pitch rate model */

      private Rate pitchRate = new Rate(
         "pitchRate", -MAX_PITCH_RATE, MAX_PITCH_RATE, DPITCH_RATE_DT);

         /** roll rate model */

      private Rate rollRate  = new Rate(
         "roll", -MAX_ROLL_RATE, MAX_ROLL_RATE, DROLL_RATE_DT);

         /** pitch rate controller */

      private PDController pitchCtrl = 
         new PDController(2, 1000);

         /** roll rate controller */

      private PDController rollCtrl = 
         new PDController(1, Float.MAX_VALUE);

         /** Command low level rate control.
          *
          * @param targetRollRate target rate of roll
          * @param targetPitchRate target rate of pitch
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
         assert(false);

            // update parent

         super.setYawDistance(targetYaw, distanceError);

            // compute yaw error

         double yawError = (getYaw() - getTargetYaw());

            // if it would be better to reverse direction, do so
         
            //if (abs(yawError) > 90 && abs(yawError) < 270)
            //reverse();

            // set yaw goal correcting for yaw wrap around

         rollCtrl.setTarget(yawError < -180
                           ? - (360 - getTargetYaw())
                           : (yawError > 180 
                              ? targetYaw + 360
                              : targetYaw));

            // set pitchRate goal based on error

         pitchCtrl.setTarget(0);

            // set target rates

         setTargetRates(rollCtrl.compute(getYaw()),
                        pitchCtrl.compute(distanceError));
      }
         /** Set pitch tuner. */

      public void setPitchTuner(PidTuner tuner)
      {
         pitchCtrl.setTuner(tuner);
      }
         /** Set roll tuner. */

      public void setRollTuner(PidTuner tuner)
      {
         rollCtrl.setTuner(tuner);
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

         dPitch = setDeltaPitch(dPitch);
         dRoll  = setDeltaRoll (dRoll );

            // feed back to actual pitch and roll rate in case
            // in case the orb hit some limit
         
         pitchRate.setRate(dPitch / time);
         rollRate.setRate(dRoll / time);

            // compute yaw

         setDeltaYaw(toDegrees(sin(toRadians(getRoll())) * 
                               toRadians(dPitch)));

            // radius of wide end of the rolling cone

         double p = ORB_RADIUS * cos(toRadians(getRoll()));

            // compute delta x and y

         double dX = toRadians(dPitch) * p * cos(toRadians(getYaw() - 90));
         double dY = toRadians(dPitch) * p * sin(toRadians(getYaw() - 90));
         
            // correct for latteral displacement due to roll
         
         dX += toRadians(dRoll) * ORB_RADIUS * cos(toRadians(getYaw()));
         dY += toRadians(dRoll) * ORB_RADIUS * sin(toRadians(getYaw()));
         
            // set position and velocity and direction
         
         setDeltaPosition(dX, dY);
         setVelocity(hypot(dX, dY) / time);
         setDirection(toDegrees(PI - atan2(dX, dY)));
      }
         /** Get the yaw rate of the orb.
          *
          * @return yaw reate in degrees per second
          */

      public double getYawRate()
      {
         return rollRate.getRate();
      }
}
