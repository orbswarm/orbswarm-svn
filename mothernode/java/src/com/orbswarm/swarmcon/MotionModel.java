package com.orbswarm.swarmcon;

import org.trebor.util.Angle;
import com.orbswarm.swarmcon.OrbIo.IOrbListener;

import static com.orbswarm.swarmcon.SwarmCon.*;
import static java.lang.Math.*;

/** This class models the motion of an orb.  It may be a simulated
 * motion model or a linkage to a live orb. */

abstract public class MotionModel implements IOrbListener
{
    // ------- vehicle state --------

    /** yaw of orb */

    private Angle yaw = new Angle();

    /** pitch of orb */

    private Angle pitch = new Angle();

    /** roll of orb */

    private Angle roll = new Angle();

    /** direction of travel (as distinct from yaw) */

    private Angle direction = new Angle();

    /** position of orb */

    private Point position = new Point(0, 0);

    /** velocity of orb */

    private double velocity = 0;

    /** yaw rate */

    private double yawRate = 0;

    // ------- pitch roll rate control parameters --------

    /** target roll rate */

    private double targetRollRate;

    /** target pitch rate */

    private double targetPitchRate;

    // ------- roll, yaw rate & velocity control parameters --------

    /** target roll */

    private Angle targetRoll = new Angle();

    /** target yaw rate */

    private double targetYawRate;

    /** target pitch rate */

    private double targetVelocity;

    // ------- yaw / distance control parameters --------

    /** target yaw */

    private Angle targetYaw = new Angle();

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

    /** Command low level roll rate control.
     *
     * @param targetRollRate target roll rate
     */
    public void setTargetRollRate(double targetRollRate)
    {
      this.targetRollRate = targetRollRate;
    }

    /** Command low level pitch rate control.
     *
     * @param targetPitchRate target velocity
     */
    public void setTargetPitchRate(double targetPitchRate)
    {
      this.targetPitchRate = targetPitchRate;
    }

    /** Command target roll.
     *
     * @param targetRoll target roll
     */
    public void setTargetRoll(double targetRoll)
    {
      this.targetRoll.setAngle(targetRoll);
    }

    /** Command target yaw rate.
     *
     * @param targetYawRate target yaw rate
     */
    public void setTargetYawRate(double targetYawRate)
    {
      this.targetYawRate = targetYawRate;
    }

    /** Command target velocity.
     *
     * @param targetVelocity target velocity
     */
    public void setTargetVelocity(double targetVelocity)
    {
      this.targetVelocity = targetVelocity;
    }

    /** Command target yaw.
     *
     * @param targetYaw target yaw
     */
    public void setTargetYaw(double targetYaw)
    {
      this.targetYaw.setAngle(targetYaw);
    }

    /** Command yaw and distance.
     *
     * @param distanceError error between target and desired distance
     */
    public void setDistanceError(double distanceError)
    {
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
      return targetYaw.degrees();
    }
    /** Get target roll. */

    public double getTargetRoll()
    {
      return targetRoll.degrees();
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

    protected void translate(Point delta)
    {
      position.translate(delta);
    }

    // set delta position

    protected void setDeltaPosition(double dX, double dY)
    {
      setPosition(getX() + dX, getY() + dY);
    }
}
