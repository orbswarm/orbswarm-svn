package com.orbswarm.swarmcon;

import org.trebor.pid.Controller;
import org.trebor.pid.PController;
import org.trebor.util.Angle;

import static java.lang.Math.*;
import static com.orbswarm.swarmcon.SwarmCon.*;
import static org.trebor.util.Angle.Type.*;

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

    private Controller yawRateToRollRateCtrl =
      new PController("yawRate", "rollRate", -.2, .2, 0.125);

    /** roll to roll rate controller */

    private Controller rollToRollRateCtrl =
      new PController("roll", "rollRate", -.5, .5, 0.0125);

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
      yawRateToRollRateCtrl,
      distanceToVelocityCtrl,
      velocityToPitchCtrl,
    };

    /** Command low level roll rate control.
     *
     * @param targetRollRate target roll rate
     */
    public void setTargetRollRate(Angle targetRollRate)
    {
      super.setTargetRollRate(targetRollRate);
      rollRate.setNormalizedTarget(targetRollRate.as(DEGREE_RATE));
    }

    /** Command low level pitch rate control.
     *
     * @param targetPitchRate target velocity
     */
    public void setTargetPitchRate(Angle targetPitchRate)
    {
      super.setTargetPitchRate(targetPitchRate);
      pitchRate.setNormalizedTarget(targetPitchRate.as(DEGREE_RATE));
    }

    /** Command target roll.
     *
     * @param targetRoll target roll
     */
    public void setTargetRoll(Angle targetRoll)
    {
      super.setTargetRoll(targetRoll);
      rollToRollRateCtrl.setTarget(targetRoll.as(DEGREE_RATE));
      rollToRollRateCtrl.setMeasurment(getRoll().as(DEGREE_RATE));
      setTargetRollRate(new Angle(rollToRollRateCtrl.compute(), DEGREE_RATE));
    }

    /** Command target yaw rate.
     *
     * @param targetYawRate target yaw rate
     */
    public void setTargetYawRate(Angle targetYawRate)
    {
      super.setTargetYawRate(targetYawRate);
      yawRateToRollRateCtrl.setTarget(targetYawRate.as(DEGREE_RATE));
      yawRateToRollRateCtrl.setMeasurment(getYawRate().as(DEGREE_RATE));
      setTargetRollRate(new Angle(yawRateToRollRateCtrl.compute(), DEGREE_RATE));
    }

    /** Command target velocity.
     *
     * @param targetVelocity target velocity
     */
    public void setTargetVelocity(double targetVelocity)
    {
      super.setTargetVelocity(targetVelocity);
      velocityToPitchCtrl.setTarget(targetVelocity);
      velocityToPitchCtrl.setMeasurment(getSpeed());
      setTargetPitchRate(new Angle(velocityToPitchCtrl.compute(), DEGREE_RATE));
    }

    /** Command target yaw.
     *
     * @param targetYaw target yaw
     */

    public void setTargetYaw(Angle targetYaw)
    {
      // update parent

      super.setTargetYaw(targetYaw);

      // compute yaw error

      double yawError = getYaw().difference(getTargetYaw(), DEGREE_RATE)
        .as(DEGREE_RATE);

      // compute yaw target rate

      yawToYawRateCtrl.setTarget(0);
      yawToYawRateCtrl.setMeasurment(yawError);
      setTargetYawRate(new Angle(yawToYawRateCtrl.compute(), DEGREE_RATE));
    }

    /** Command distance error.
     *
     * @param distanceError error between target distance and desired
     *        distance
     */

    public void setDistanceError(double distanceError)
    {
      // update parent

      super.setDistanceError(distanceError);

      // compute target velocity

      distanceToVelocityCtrl.setTarget(0);
      distanceToVelocityCtrl.setMeasurment(distanceError);
      setTargetVelocity(distanceToVelocityCtrl.compute());
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

      // compute delta pitch and roll

      Angle dPitch = new Angle(pitchRate.getRate() * time, DEGREE_RATE);
      Angle dRoll  = new Angle(rollRate.getRate() * time, DEGREE_RATE);

      // update absolute pitch and roll, overwrite deltas with achieved
      // values

      dPitch = setDeltaPitch(dPitch);
      dRoll  = setDeltaRoll(dRoll);

      // feed back to actual pitch and roll rate
      // in case the orb hit some limit

      pitchRate.setRate(dPitch.as(DEGREE_RATE) / time);
      rollRate.setRate(dRoll.as(DEGREE_RATE)   / time);

      // compute yaw

      Angle dYaw = new Angle(
        sin(getRoll().as(RADIAN_RATE)) * dPitch.as(RADIAN_RATE), RADIAN_RATE);
      dYaw = setDeltaYaw(dYaw);
      setYawRate(new Angle(dYaw.as(DEGREE_RATE) / time, DEGREE_RATE));

      // radius of wide end of the rolling cone

      double p = ORB_RADIUS * cos(getRoll().as(RADIAN_RATE));

      // compute delta x and y

      Point delta = new Point(getYaw().cartesian(dPitch.as(RADIAN_RATE) * p));

      // update the direction of the orb


      // correct for latteral displacement due to roll.  for reasons i
      // don't understand if i include the roll correction into the
      // direcion above, it breaks the velocity controller.  this note
      // stands in place of a proper fix.

//       Angle rollDir = new Angle(getYaw(), new Angle(90, DEGREE_RATE));
//       delta.translate(
//         rollDir.cartesian(dRoll.as(RADIAN_RATE) * ORB_RADIUS, 0, 0));

      // set position and velocity

      setDeltaPosition(delta.getX(), delta.getY());
      setVelocity(hypot(delta.getX(), delta.getY()) / time);
      setDirection(new Angle(delta.getX(), delta.getY()));
    }


    /** Handle messages from the orb. This is stubbed out as the
     * simulation would not be connected to an orb. */

    public void onOrbMessage(Message message)
    {
    }

    /** Deactivate active smooth path. */

    public void deactivatePath()
    {
      super.deactivatePath();
      stop();
    }

    /** Command the orb to the next waypoint. */

//     private Controller wayPointYawController =
//       new PController("yaw", "yawRate", -20, 20, -0.34);

    double leadTime = 3.0;

    protected void commandWaypoint(Waypoint wp)
    {
      //double targetTime = wp.getTime() + leadTime;

      setTargetVelocity(wp.getVelocity());
      setTargetYawRate(wp.getYawRate());
//      System.out.println("wp yawRate: " + wp.getYawRate());

//       for (Waypoint wpn: getActivePath())
//       {
//         if (wpn.getTime() >= targetTime)
//         {
//           setTargetYaw(new Angle(getPosition(), wpn));
//           return;
//         }
//       }

      //setTargetYaw(new Angle(getPosition(), getActivePath().lastElement()));

      //System.out.println("      wp " + wp);
//      System.out.println("     yaw " + SwarmCon.StdFmt.format(getYaw().as(HEADING)));
//       System.out.println(
//         "targ vel: " + SwarmCon.StdFmt.format(getTargetVelocity()) +
//         " vel: " + SwarmCon.StdFmt.format(getVelocity()) +
//         " err: " + SwarmCon.StdFmt.format(getTargetVelocity() - getVelocity()));
      //setPosition(wp);
      //setYaw(wp.getYaw());
    }
}
