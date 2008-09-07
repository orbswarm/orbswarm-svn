package com.orbswarm.swarmcon;

import org.trebor.util.Angle;
import com.orbswarm.swarmcon.OrbIo.IOrbListener;

import static com.orbswarm.swarmcon.SwarmCon.*;
import static java.lang.Math.*;
import static org.trebor.util.Angle.Type.*;

/** This class models the motion of an orb.  It may be a simulated
 * motion model or a linkage to a live orb. */

abstract public class MotionModel implements IOrbListener
{
    /** Distance to travel to give the orb some room to turn (meters). */
    
    public static final double HEAD_ROOM = 4;

    /** Time between points on a smooth path (seconds). */
    
    public static final double SMOOTH_PATH_UPDATE_RATE = .1;

    /** Curve width for smooth path calculations. */

    public static final double CURVE_WIDTH = HEAD_ROOM * 0.60;

    /** Flatness of waypoints. */

    public static final double CURVE_FLATNESS = 0.01;
    
    /** Velocity rate profile to folow */

    private static Rate velocityRate = new Rate("Velocity", 0, 1.0, .15);

    // ------- vehicle state --------

    /** yaw of orb */

    protected Angle yaw = new Angle(0, HEADING);

    /** pitch of orb (shell not ballest) */

    protected Angle pitch = new Angle();

    /** roll of orb (shell not ballest) */

    protected Angle roll = new Angle();

    /** direction of travel (as distinct from yaw) */

    protected Angle direction = new Angle();

    /** position of orb */

    protected Point position = new Point(0, 0);

    /** velocity of orb */

    protected double velocity = 0;

    /** yaw rate */

    protected Angle yawRate = new Angle();

    // ------- pitch roll rate control parameters --------

    /** target roll rate */

    private Angle targetRollRate;

    /** target pitch rate */

    private Angle targetPitchRate;

    // ------- roll, yaw rate & velocity control parameters --------

    /** target roll */

    private Angle targetRoll = new Angle();

    /** target yaw rate */

    private Angle targetYawRate;

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

    public Angle getYawRate()
    {
      return yawRate;
    }
    /** Set the yaw rate of the orb.
     *
     * @param yawRate yaw rate in degrees per second
     */

    protected void setYawRate(Angle yawRate)
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
      return abs(yaw.difference(direction).as(DEGREES)) < 90
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
     * @return direction as a heading
     */

    public Angle getDirection()
    {
      return new Angle(direction);
    }

    /** Set the current direction of the orb.
     *
     * @param direction orb direction (heading)
     */
    protected void setDirection(Angle direction)
    {
      this.direction.setAngle(direction);
    }

    /** Command low level roll rate control.
     *
     * @param targetRollRate target roll rate
     */
    public void setTargetRollRate(Angle targetRollRate)
    {
      this.targetRollRate = targetRollRate;
    }

    /** Command low level pitch rate control.
     *
     * @param targetPitchRate target velocity
     */
    public void setTargetPitchRate(Angle targetPitchRate)
    {
      this.targetPitchRate = targetPitchRate;
    }

    /** Command target roll.
     *
     * @param targetRoll target roll
     */
    public void setTargetRoll(Angle targetRoll)
    {
      this.targetRoll = targetRoll;
    }

    /** Command target yaw rate.
     *
     * @param targetYawRate target yaw rate
     */
    public void setTargetYawRate(Angle targetYawRate)
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

    public void setTargetYaw(Angle targetYaw)
    {
      this.targetYaw = targetYaw;
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
    
    public SmoothPath setTargetPosition(Target target)
    {
      //throw(new Error("Method not yet implemented."));
      // create a path

      Path path = new Path();

      // add the current orb position

      path.add(new Target(position));

      // add the intermediate point between the orb and the target

      path.add(new Target(yaw.cartesian(HEAD_ROOM / 4, getX(), getY())));
      path.add(new Target(yaw.cartesian(HEAD_ROOM, getX(), getY())));

      // add the actual target

      path.add(target);

      // command out the path

      return setTargetPath(path);
    }

    /** Command a path.
     *
     * @param path the path the orb should follow
     */

    public SmoothPath setTargetPath(Path path)
    {
      SmoothPath sp = new SmoothPath(
        path, velocityRate, 
        SMOOTH_PATH_UPDATE_RATE, 
        CURVE_WIDTH, CURVE_FLATNESS);
      return sp;
    }

    /** Get target yaw. */

    public double getTargetYaw()
    {
      return targetYaw.as(DEGREES);
    }
    /** Get target roll. */

    public double getTargetRoll()
    {
      return targetRoll.as(DEGREES);
    }
    // get yaw

    public Angle getYaw()
    {
      return yaw; //.as(DEGREES);
    }
    // set yaw

    protected void setYaw(Angle yaw)
    {
      this.yaw = yaw;
    }
    // set delta yaw

    protected void setDeltaYaw(double dYaw)
    {
      yaw.rotate(dYaw, DEGREE_RATE);
    }
    // get pitch

    public Angle getPitch()
    {
      return pitch;
    }
    // set pitch

    protected double setPitch(double pitch)
    {
      double deltaPitch = pitch - this.pitch.as(DEGREES);
      this.pitch.setAngle(pitch, DEGREES);
      return deltaPitch;
    }
    // set delta pitch

    protected double setDeltaPitch(double dPitch)
    {
      return setPitch(this.pitch.as(DEGREES) + dPitch);
    }
    // get roll

    public Angle getRoll()
    {
      return roll;
    }
    // set roll

    protected double setRoll(double roll)
    {
      double newRoll = max(min(MAX_ROLL, roll), -MAX_ROLL);
      double deltaRoll = newRoll - this.roll.as(DEGREE_RATE);
      this.roll.setAngle(newRoll, DEGREES);
      return deltaRoll;
    }
    // set delta roll

    protected double setDeltaRoll(double dRoll)
    {
      return setRoll(this.roll.as(DEGREE_RATE) + dRoll);
    }
    // reverse the sense of the vehicle

    public void reverse()
    {
      yaw.rotate(180, DEGREES);
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

    /** Return the orb survey position, or null if we haven't got one yet. */

    public Point getSurveyPosition()
    {
      return null;
    }

    /** Return true if the orb has acked the origin command. */

    public boolean isOriginAcked()
    {
      return false;
    }
}
