package com.orbswarm.swarmcon;

import org.trebor.pid.Controller;
import org.trebor.pid.PController;
import org.trebor.util.Angle;

import static java.lang.Math.*;
import static com.orbswarm.swarmcon.SwarmCon.*;

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
    public void setTargetRollRate(double targetRollRate)
    {
      super.setTargetRollRate(targetRollRate);
      rollRate.setNormalizedTarget(targetRollRate);
    }

    /** Command low level pitch rate control.
     *
     * @param targetPitchRate target velocity
     */
    public void setTargetPitchRate(double targetPitchRate)
    {
      super.setTargetPitchRate(targetPitchRate);
      pitchRate.setNormalizedTarget(targetPitchRate);
    }

    /** Command target roll.
     *
     * @param targetRoll target roll
     */
    public void setTargetRoll(double targetRoll)
    {
      super.setTargetRoll(targetRoll);
      rollToRollRateCtrl.setTarget(targetRoll);
      rollToRollRateCtrl.setMeasurment(getRoll());
      setTargetRollRate(rollToRollRateCtrl.compute());
    }

    /** Command target yaw rate.
     *
     * @param targetYawRate target yaw rate
     */
    public void setTargetYawRate(double targetYawRate)
    {
      super.setTargetYawRate(targetYawRate);
      yawRateToRollRateCtrl.setTarget(targetYawRate);
      yawRateToRollRateCtrl.setMeasurment(getYawRate());
      setTargetRollRate(yawRateToRollRateCtrl.compute());
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
      setTargetPitchRate(velocityToPitchCtrl.compute());
    }

    /** Command target yaw.
     *
     * @param targetYaw target yaw
     */
    public void setTargetYaw(double targetYaw)
    {
      // update parent

      super.setTargetYaw(targetYaw);

      // compute yaw error

      double yawError = Angle.difference(getYaw(), getTargetYaw());

      // compute yaw target rate

      yawToYawRateCtrl.setTarget(0);
      yawToYawRateCtrl.setMeasurment(yawError);
      setTargetYawRate(yawToYawRateCtrl.compute());
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
      // if the commander is active don't update

      if (pathCommander != null)
        return;

      // update pitch and roll rate

      pitchRate.update(time);
      rollRate .update(time);

      // compute delta pitch and roll

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

      Point delta = new Point(
        Angle.cartesian(
          getYaw(), false, dPitch.radians() * p, true, 0, 0));

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


    /** Handle messages from the orb. This is stubbed out as the
     * simulation would not be connected to an orb. */

    public void onOrbMessage(Message message)
    {
    }

    /** Command a path.
     *
     * @param path the path the orb should follow
     */

    public SmoothPath setTargetPath(Path path)
    {
      SmoothPath sp = super.setTargetPath(path);
      // if there is a live path commander kill it

      if (pathCommander != null)
        pathCommander.requestDeath();

      // make a fresh new commander on this smooth path, and follow it

      pathCommander = new PathCommander(sp);
      pathCommander.start();

      // return the smooth path

      return sp;
    }
    
    // the one true path commander

    private PathCommander pathCommander = null;

    // thread for commanding orb

    class PathCommander extends Thread
    {
        private SmoothPath path;

        private boolean deathRequest = false;

        private Swarm swarm = SwarmCon.getInstance().getSwarm();

        public PathCommander(SmoothPath path)
        {
          this.path = path;
        }

        public void requestDeath()
        {
          deathRequest = true;
        }

        public void run()
        {
          try
          {
            System.out.println("started commander: " + path.size());

            // make the path visable
            
            SmoothMobject smob = new SmoothMobject(path);
            swarm.add(smob);

            double lastTime = 0;
            
            for (Waypoint wp: path)
            {
              // sleep until it's time to send it

              sleep(SwarmCon.secondsToMilliseconds(wp.getTime() - lastTime));

              // if requested to die, do so

              if (deathRequest)
              {
                swarm.remove(smob);
                return;
              }

              // move orb to this waypoint

              setPosition(wp);
              setYaw(wp.getYaw());
              smob.setCurrentWaypoint(wp);

              // update the last time a way point was set

              lastTime = wp.getTime();
            }
            
            // nolonger show smod

            swarm.remove(smob);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
    }
}
