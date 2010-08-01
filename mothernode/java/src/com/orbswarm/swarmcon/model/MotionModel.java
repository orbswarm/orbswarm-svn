package com.orbswarm.swarmcon.model;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.io.OrbIo.IOrbListener;
import com.orbswarm.swarmcon.path.Path;
import com.orbswarm.swarmcon.path.Point;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.path.Waypoint;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static org.trebor.util.Angle.Type.DEGREE_RATE;
import static org.trebor.util.Angle.Type.DEGREES;
import static org.trebor.util.Angle.Type.HEADING;
import static com.orbswarm.swarmcon.Constants.*;

/**
 * This class models the motion of an orb. It may be a simulated motion
 * model or a linkage to a live orb.
 */

abstract public class MotionModel implements IOrbListener
{
  /** a nice clearly named zero degrees per second */

  public static final Angle ZERO_DEGREES_PER_SECOND = new Angle(0,
    DEGREE_RATE);

  /** a nice clearly named zero degrees per second */

  public static final Angle ZERO_DEGREES = new Angle(0, DEGREE_RATE);

  /** Curve width for smooth path calculations. */

  public static final double SMOOTHNESS = .4;

  /**
   * The distance from the orb to place a point to give the orb some room
   * to turn.
   */

  public static final double HEADROOM = 5;

  /** Time between points on a smooth path (seconds). */

  public static final double SMOOTH_PATH_UPDATE_RATE = .1;

  /** Flatness of waypoints. */

  public static final double CURVE_FLATNESS = 0.00000001;

  /** Velocity rate profile to follow */

  private static Rate velocityRate = new Rate("Velocity", 0, 1.0, 0.08);

  // ------- vehicle state --------

  /** yaw of orb */

  private Angle yaw = new Angle(45, HEADING);

  /** pitch of orb (shell not ballast) */

  private Angle pitch = new Angle();

  /** roll of orb (shell not ballast) */

  private Angle roll = new Angle();

  /** direction of travel (as distinct from yaw) */

  private Angle direction = new Angle();

  /** position of orb */

  private Point position = new Point(0, 0);

  /** velocity of orb */

  private double velocity = 0;

  /** yaw rate */

  private Angle yawRate = new Angle();

  // ------- roll, yaw rate & velocity control parameters --------

  /** target roll */

  private Angle targetRoll = new Angle();

  /** target pitch rate */

  private double targetVelocity;

  // ------- yaw / distance control parameters --------

  /** target yaw */

  private Angle targetYaw = new Angle();

  // ------- position control parameters --------

  /**
   * Update the state of this model.
   * 
   * @param time seconds since last update
   */

  /**
   * the current active smoothed path being followed. this will be null if
   * there is no such path.
   */

  private SmoothPath activeSmoothPath;

  /** Update the motion model. */

  abstract public void update(double time);

  /**
   * Get the yaw rate of the orb.
   * 
   * @return yaw rate in degrees per second
   */

  public Angle getYawRate()
  {
    return yawRate;
  }

  /**
   * Set the yaw rate of the orb.
   * 
   * @param yawRate yaw rate in degrees per second
   */

  protected void setYawRate(Angle yawRate)
  {
    this.yawRate = yawRate;
  }

  /**
   * Get the current speed of the orb. This takes into account which way
   * the orb is facing and will return negative values if the orb is
   * backing up.
   * 
   * @return velocity in meters per second
   */

  public double getSpeed()
  {
    return abs(yaw.difference(direction).as(DEGREES)) < 90
      ? getVelocity()
      : -getVelocity();
  }

  /**
   * Get the current velocity of the orb. Always returns a positive value.
   * 
   * @return velocity in meters per second
   */

  public double getVelocity()
  {
    return velocity;
  }

  /**
   * Set the current velocity of the orb.
   * 
   * @param velocity orb velocity in meters per second
   */

  protected void setVelocity(double velocity)
  {
    this.velocity = velocity;
  }

  /**
   * Get the current direction of the orb.
   * 
   * @return direction as a heading
   */

  public Angle getDirection()
  {
    return new Angle(direction);
  }

  /**
   * Set the current direction of the orb.
   * 
   * @param direction orb direction (heading)
   */
  protected void setDirection(Angle direction)
  {
    this.direction = direction;
  }

  /**
   * Command target roll.
   * 
   * @param targetRoll target roll
   */
  public void setTargetRoll(Angle targetRoll)
  {
    this.targetRoll = targetRoll;
  }

  /**
   * Command target velocity.
   * 
   * @param targetVelocity target velocity
   */
  public void setTargetVelocity(double targetVelocity)
  {
    this.targetVelocity = targetVelocity;
  }

  /**
   * Report target velocity.
   * 
   * @return target velocity
   */

  public double getTargetVelocity()
  {
    return targetVelocity;
  }

  /**
   * Command target yaw.
   * 
   * @param targetYaw target yaw
   */

  public void setTargetYaw(Angle targetYaw)
  {
    this.targetYaw = targetYaw;
  }

  /**
   * Command position.
   * 
   * @param target target position
   */

  public SmoothPath setTargetPosition(Target target)
  {
    // create a path

    Path path = new Path();

    // add the current orb position

    path.add(new Target(position));

    // add the actual target

    path.add(target);

    // command out the path

    return setTargetPath(path);
  }

  /**
   * Command a path.
   * 
   * @param path the path the orb should follow
   */

  public SmoothPath setTargetPath(Path path)
  {
    activeSmoothPath = new SmoothPath(path, velocityRate,
      SMOOTH_PATH_UPDATE_RATE, SMOOTHNESS, CURVE_FLATNESS, yaw, HEADROOM);

    // if there is a live path commander kill it

    if (pathCommander != null)
      pathCommander.requestDeath();

    // make a fresh new commander on this smooth path, and follow it

    pathCommander = new PathCommander();
    pathCommander.start();

    // return the smooth path

    return activeSmoothPath;
  }

  /** Stop the orb. */

  public void stop()
  {
    pathCommander.requestDeath();
  }

  /** Get the active smooth path. */

  public SmoothPath getActivePath()
  {
    return activeSmoothPath;
  }

  /** Deactivate active smooth path. */

  public void deactivatePath()
  {
    activeSmoothPath = null;
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
    return yaw; // .as(DEGREES);
  }

  // set yaw

  protected Angle setYaw(Angle newYaw)
  {
    Angle deltaYaw = yaw.difference(newYaw);
    yaw = newYaw;
    return deltaYaw;
  }

  // set delta yaw

  protected Angle setDeltaYaw(Angle deltaYaw)
  {
    return setYaw(new Angle(yaw, deltaYaw));
  }

  // get pitch

  public Angle getPitch()
  {
    return pitch;
  }

  // set pitch

  protected Angle setPitch(Angle newPitch)
  {
    Angle deltaPitch = pitch.difference(newPitch);
    pitch = newPitch;
    return deltaPitch;
  }

  // set delta pitch

  protected Angle setDeltaPitch(Angle deltaPitch)
  {
    return setPitch(new Angle(pitch, deltaPitch));
  }

  // get roll

  public Angle getRoll()
  {
    return roll;
  }

  // set roll

  protected Angle setRoll(Angle unlimitedRoll)
  {
    double degrees = unlimitedRoll.as(DEGREE_RATE);
    Angle limitedRoll = new Angle(max(min(MAX_ROLL, degrees), -MAX_ROLL),
      DEGREE_RATE);

    Angle deltaRoll = roll.difference(limitedRoll);
    roll = limitedRoll;
    return deltaRoll;
  }

  /**
   * Set delta roll. The roll is limited, so the value returned is the
   * actual achieved delta roll.
   * 
   * @param droll the delta roll
   * @return The achieved delta roll.
   */

  protected Angle setDeltaRoll(Angle deltaRoll)
  {
    return setRoll(new Angle(roll, deltaRoll));
  }

  // reverse the sense of the vehicle

  public void reverse()
  {
    yaw = yaw.rotate(180, DEGREES);
  }

  // position getter

  public Point getPosition()
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

  public void setPosition(double x, double y)
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

  /** Return true if the orb has acknowledged the origin command. */

  public boolean isOriginAcked()
  {
    return false;
  }

  /** Command the orb to the next waypoint. */

  protected abstract void commandWaypoint(Waypoint wp);

  // the one true path commander

  protected PathCommander pathCommander = null;

  // thread for commanding orb

  protected class PathCommander extends Thread
  {
    private boolean deathRequest = false;

    public void requestDeath()
    {
      deathRequest = true;
    }

    public void run()
    {
      try
      {
        double lastTime = 0;
        SmoothPath sp = getActivePath();

        for (Waypoint wp : sp)
        {
          // sleep until it's time to send it

          sleep(SwarmCon.secondsToMilliseconds(wp.getTime() - lastTime));

          // if requested to die, do so

          if (deathRequest)
            return;

          // move orb to this waypoint

          commandWaypoint(wp);
          sp.setCurrentWaypoint(wp);

          // update the last time a way point was set

          lastTime = wp.getTime();
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}
