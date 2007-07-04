package org.trebor.swarmcon;

import org.trebor.pid.Controller;
import org.trebor.pid.PController;

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

         /** yaw to yaw rate controller */

      private Controller yawToYawRateCtrl = 
         new PController("yaw", "yawRate", -20, 20, -0.34);

         /** yaw rate to roll rate controller */

      private Controller yawRateToRollCtrl = 
         new PController("yawRate", "roll", -.2, 2, 0.125);

         /** distance to veloctiy controller */

      private Controller distanceToVelocityCtrl = 
         new PController("distance", "velocity", 0, 1, .2);

         /** velocity to pitch rate controller */

      private Controller velocityToPitchCtrl = 
         new PController("veloctiy", "pitchRate", -1, 1, 320.0);

         /** all the controllers in an array */

      Controller[] controllers =
      {
         yawToYawRateCtrl,
         yawRateToRollCtrl,
         distanceToVelocityCtrl,
         velocityToPitchCtrl,
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
         pitchRate.setNormalizedTarget(targetPitchRate);
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
         yawRateToRollCtrl.setTarget(targetYawRate);
         velocityToPitchCtrl.setTarget(targetVelocity);
         yawRateToRollCtrl.setMeasurment(getYawRate());
         velocityToPitchCtrl.setMeasurment(getSpeed());
         setTargetRollPitchRates(
            yawRateToRollCtrl.compute(),
            velocityToPitchCtrl.compute());
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

         yawToYawRateCtrl.setTarget(0);
         distanceToVelocityCtrl.setTarget(0);
         yawToYawRateCtrl.setMeasurment(yawError);
         distanceToVelocityCtrl.setMeasurment(distanceError);
         
         setTargetYawRateVelocity(
            yawToYawRateCtrl.compute(),
            distanceToVelocityCtrl.compute());
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
