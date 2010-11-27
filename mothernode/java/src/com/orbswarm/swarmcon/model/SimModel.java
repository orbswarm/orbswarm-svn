package com.orbswarm.swarmcon.model;

import java.awt.geom.Point2D;

import org.apache.log4j.Logger;
import org.trebor.pid.Controller;
import org.trebor.pid.PController;
import org.trebor.util.Angle;
import org.trebor.util.Rate;

import com.orbswarm.swarmcon.io.Message;
import com.orbswarm.swarmcon.path.Waypoint;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static org.trebor.util.Angle.Type.DEGREE_RATE;
import static org.trebor.util.Angle.Type.RADIAN_RATE;
import static org.trebor.util.Angle.Type.HEADING_RATE;
import static com.orbswarm.swarmcon.util.Constants.*;

/** A simple motion simulation model based on rates */

public class SimModel extends AMotionModel
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(SimModel.class);

  /** pitch rate model */

  private Rate pitchRate = new Rate("pitchRate", -MAX_PITCH_RATE,
    MAX_PITCH_RATE, DPITCH_RATE_DT);

  /** roll rate model */

  private Rate rollRate = new Rate("roll", -MAX_ROLL_RATE, MAX_ROLL_RATE,
    DROLL_RATE_DT);

  /** yaw to yaw rate controller */

  private Controller yawToYawRateCtrl = new PController("yaw", "yawRate",
    -20, 20, -0.34);

  /** yaw rate to roll rate controller */

  private Controller yawRateToRollRateCtrl = new PController("yawRate",
    "rollRate", -.2, .2, 0.125);

  /** roll to roll rate controller */

  private Controller rollToRollRateCtrl = new PController("roll", "rollRate",
    -.5, .5, 0.0125);

  /** distance to velocity controller */

  private Controller distanceToVelocityCtrl = new PController("distance",
    "velocity", 0, 1, .2);

  /** velocity to pitch rate controller */

  private Controller velocityToPitchCtrl = new PController("veloctiy",
    "pitchRate", -1, 1, 320.0);

  /** all the controllers in an array */

  Controller[] controllers =
  {
    yawToYawRateCtrl,
    yawRateToRollRateCtrl,
    distanceToVelocityCtrl,
    velocityToPitchCtrl,
  };

  /**
   * Command low level roll rate control.
   * 
   * @param targetRollRate target roll rate
   */
  public void setTargetRollRate(Angle targetRollRate)
  {
    rollRate.setNormalizedTarget(targetRollRate.as(DEGREE_RATE));
  }

  /**
   * Command low level pitch rate control.
   * 
   * @param targetPitchRate target velocity
   */
  public void setTargetPitchRate(Angle targetPitchRate)
  {
    pitchRate.setNormalizedTarget(targetPitchRate.as(DEGREE_RATE));
  }

  /**
   * Command target roll.
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

  /**
   * Command target yaw rate.
   * 
   * @param targetYawRate target yaw rate
   */
  
  public void setTargetYawRate(Angle targetYawRate)
  {
    yawRateToRollRateCtrl.setTarget(targetYawRate.as(DEGREE_RATE));
    yawRateToRollRateCtrl.setMeasurment(getYawRate().as(DEGREE_RATE));
    setTargetRollRate(new Angle(yawRateToRollRateCtrl.compute(), DEGREE_RATE));
  }

  /**
   * Command target velocity.
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

  /**
   * Command target yaw.
   * 
   * @param targetYaw target yaw
   */

  public void setTargetYaw(Angle targetYaw)
  {
    // update parent

    super.setTargetYaw(targetYaw);

    // compute yaw error

    double yawError = getYaw().difference(getTargetYaw(), DEGREE_RATE).as(
      DEGREE_RATE);

    // compute yaw target rate

    yawToYawRateCtrl.setTarget(0);
    yawToYawRateCtrl.setMeasurment(yawError);
    setTargetYawRate(new Angle(yawToYawRateCtrl.compute(), DEGREE_RATE));
  }

  /**
   * Command distance error.
   * 
   * @param distanceError error between target distance and desired
   *        distance
   */

  public void setDistanceError(double distanceError)
  {
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
    pitchRate.setVelocity(-pitchRate.getVelocity());
  }

  // update position

  public void update(double time)
  {
    // update pitch and roll rate

    pitchRate.update(time);
    rollRate.update(time);

    // compute delta pitch and roll

    Angle dPitch = new Angle(pitchRate.getVelocity() * time, DEGREE_RATE);
    Angle dRoll = new Angle(rollRate.getVelocity() * time, DEGREE_RATE);

    // update absolute pitch and roll, overwrite deltas with achieved
    // values

    dPitch = setDeltaPitch(dPitch);
    dRoll = setDeltaRoll(dRoll);

    // feed back to actual pitch and roll rate
    // in case the orb hit some limit

    pitchRate.setVelocity(dPitch.as(DEGREE_RATE) / time);
    rollRate.setVelocity(dRoll.as(DEGREE_RATE) / time);

    // compute yaw

    Angle dYaw = new Angle(sin(getRoll().as(RADIAN_RATE)) *
      dPitch.as(RADIAN_RATE), RADIAN_RATE);
    dYaw = setDeltaYaw(dYaw);
    setYawRate(new Angle(dYaw.as(DEGREE_RATE) / time, DEGREE_RATE));

    // radius of wide end of the rolling cone

    double p = ORB_RADIUS * cos(getRoll().as(RADIAN_RATE));

    // compute delta x and y

    Point2D delta = getYaw().cartesian(dPitch.as(RADIAN_RATE) * p);

    // update the direction of the orb

    setDirection(new Angle(delta.getX(), delta.getY()));

    // correct for lateral displacement due to roll. for reasons i
    // don't understand if i include the roll correction into the
    // direction above, it breaks the velocity controller. this note
    // stands in place of a proper fix.

    Angle rollDir = new Angle(getYaw(), new Angle(90, DEGREE_RATE));
    Point2D pos = rollDir.cartesian(dRoll.as(RADIAN_RATE) * ORB_RADIUS, 0, 0);
    delta.setLocation(delta.getX() + pos.getX(), delta.getY() + pos.getY());

    // set position and velocity

    setDeltaPosition(delta.getX(), delta.getY());
    setVelocity(hypot(delta.getX(), delta.getY()) / time);
  }

  public void stop()
  {
    log.debug("stop");
    //super.stop();
    setTargetVelocity(0);
    setTargetYawRate(new Angle(0, DEGREE_RATE));
  }
  
  /**
   * Handle messages from the orb. This is stubbed out as the simulation
   * would not be connected to an orb.
   */

  public void onOrbMessage(Message message)
  {
  }

  /** Command the orb to the next waypoint. */

  protected void commandWaypoint(Waypoint wp)
  {
    log.debug("command " + wp);
    
    // set target velocity based on waypoint velocity

    setTargetVelocity(wp.getVelocity());

    // establish the delta yaw between the waypoint and the orb

    // Angle deltaYaw = wp.getYaw().difference(getYaw());
    // setTargetYawRate(
    // wp.getYawRate().rotate(-deltaYaw.as(DEGREE_RATE) * 0.2,
    // DEGREE_RATE));

    double distance = getPosition().distance(wp);
    Angle headingToWp = (new Angle(wp, getPosition())).difference(getYaw());
    boolean toLeft = headingToWp.as(HEADING_RATE) < 0;
    double rotBy = (toLeft
      ? -1
      : 1) * distance * 5.0;

    setTargetYawRate(wp.getYawRate().rotate(rotBy, HEADING_RATE));
  }
}
