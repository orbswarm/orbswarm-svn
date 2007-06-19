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

      private PDController velocityToDistanceCtrl = 
         new PDController("2:dist->vel", .2, 0);

         /** roll rate controller */

      private PDController yawRateToYawCtrl = 
         new PDController("2:yaw->yawRate", 1, 0);

         /** pitch rate to velocity controller */

      private PDController pitchToVelocityCtrl = 
         new PDController("1:vel->pitchRate", 3.2E2, 1E36);

         /** roll rate to yaw rate controller */

      private PDController rollToYawRateCtrl = 
         new PDController("1:yawRate->roll", 1.25E-1, 0);

      Controller[] controllers =
      {
         pitchToVelocityCtrl,
         rollToYawRateCtrl,
         velocityToDistanceCtrl,
         yawRateToYawCtrl,
      };

         /** Command low level rate control.
          *
          * @param targetRollRate target rate of roll
          * @param targetPitchRate target rate of pitch
          */

      public void setTargetRollPitchRates(double targetRollRate,
                                          double targetPitchRate)
      {
         super.setTargetRollPitchRates(targetRollRate, targetPitchRate);
         rollRate.setNormalizedTarget(targetRollRate);
         pitchRate.setNormalizedTarget(.2); //targetPitchRate);
      }
         /** Command target yaw rate and velocity.
          *
          * @param targetYawRate target yaw rate
          * @param targetVelocity target velocity
          */

      public void setTargetYawRateVelocity(double targetYawRate, 
                                          double targetVelocity)
      {
         super.setTargetYawRateVelocity(targetYawRate, targetVelocity);
         rollToYawRateCtrl.setTarget(targetYawRate);
         pitchToVelocityCtrl.setTarget(targetVelocity);
         setTargetRollPitchRates(
            -rollToYawRateCtrl.compute(getYawRate()),
            pitchToVelocityCtrl.compute(getSpeed()));
      }
         /** Command yaw and distance.
          *
          * @param targetYaw target yaw
          * @param distanceError error between target and desired distance
          */

      public void setYawDistance(double targetYaw, double distanceError)
      {
            // update parent

         super.setYawDistance(targetYaw, distanceError);

            // compute yaw error

         double yawError = Angle.difference(getYaw(), getTargetYaw());


            // set yaw goal correcting for yaw wrap around

            //rollCtrl.setTarget(yawError < -180
            //? - (360 - getTargetYaw())
            //: (yawError > 180 
            //? targetYaw + 360
            //: targetYaw));

            // set pitchRate goal based on error


            // set targets

         yawRateToYawCtrl.setTarget(0);
         velocityToDistanceCtrl.setTarget(0);

         setTargetYawRateVelocity(
            yawRateToYawCtrl.compute(yawError),
            velocityToDistanceCtrl.compute(distanceError));
      }
         /** Get controllers in the system. */

      public Controller[] getControllers()
      {
         return controllers;
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
         
         Angle dPitch = new Angle(pitchRate.getRate() * time);
         Angle dRoll  = new Angle(rollRate.getRate() * time);


            // update absolute pitch and roll

         dPitch.setAngle(setDeltaPitch(dPitch.degrees()));
         dRoll .setAngle(setDeltaRoll (dRoll .degrees()));

            // feed back to actual pitch and roll rate in case
            // in case the orb hit some limit
         
         pitchRate.setRate(dPitch.degrees() / time);
         rollRate.setRate(dRoll.degrees()   / time);

            // compute yaw

         double dYaw = toDegrees(sin(toRadians(getRoll())) * 
                                 dPitch.radians());
         setDeltaYaw(dYaw);
         setYawRate(dYaw / time);

            // radius of wide end of the rolling cone

         double p = ORB_RADIUS * cos(toRadians(getRoll()));

            // compute delta x and y

         Point delta = Angle.cartesian(
            getYaw(), false, dPitch.radians() * p, true, 0, 0);

            // correct for latteral displacement due to roll

         delta.translate(
            Angle.cartesian(
               getYaw() + 90, false, dRoll.radians() * ORB_RADIUS,
               true, 0, 0));
         
            // set position and velocity and direction
         
         setDeltaPosition(delta.getX(), delta.getY());
         setVelocity(hypot(delta.getX(), delta.getY()) / time);
         setDirection(toDegrees(atan2(delta.getX(), delta.getY())));
      }
}
