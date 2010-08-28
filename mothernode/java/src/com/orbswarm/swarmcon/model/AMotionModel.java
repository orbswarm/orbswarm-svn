package com.orbswarm.swarmcon.model;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import com.orbswarm.swarmcon.io.OrbIo.IOrbListener;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.Path;
import com.orbswarm.swarmcon.path.Point;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.path.Waypoint;
import com.orbswarm.swarmcon.swing.SwarmCon;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static org.trebor.util.Angle.Type.DEGREE_RATE;
import static org.trebor.util.Angle.Type.DEGREES;
import static org.trebor.util.Angle.Type.HEADING;
import static com.orbswarm.swarmcon.util.Constants.*;

/**
 * This class models the motion of an orb. It may be a simulated motion
 * model or a linkage to a live orb.
 */

abstract public class AMotionModel implements IOrbListener, IMotionModel
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(AMotionModel.class);

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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#update(double)
   */

  abstract public void update(double time);

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getYawRate()
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getSpeed()
   */

  public double getSpeed()
  {
    return abs(yaw.difference(direction).as(DEGREES)) < 90
      ? getVelocity()
      : -getVelocity();
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getVelocity()
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getDirection()
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setTargetRoll(org.trebor.util.Angle)
   */
  public void setTargetRoll(Angle targetRoll)
  {
    this.targetRoll = targetRoll;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setTargetVelocity(double)
   */
  public void setTargetVelocity(double targetVelocity)
  {
    this.targetVelocity = targetVelocity;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getTargetVelocity()
   */

  public double getTargetVelocity()
  {
    return targetVelocity;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setTargetYaw(org.trebor.util.Angle)
   */

  public void setTargetYaw(Angle targetYaw)
  {
    this.targetYaw = targetYaw;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setTargetPosition(com.orbswarm.swarmcon.path.Target)
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setTargetPath(com.orbswarm.swarmcon.path.Path)
   */

  public SmoothPath setTargetPath(Path path)
  {
    activeSmoothPath = new SmoothPath(path, velocityRate,
      SMOOTH_PATH_UPDATE_RATE, SMOOTHNESS, CURVE_FLATNESS, yaw, HEADROOM);

    return startPathCommander();
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setTargetPath(com.orbswarm.swarmcon.path.IBlockPath)
   */
  public SmoothPath setTargetPath(IBlockPath path)
  {
    activeSmoothPath = new SmoothPath(path, velocityRate,
      SMOOTH_PATH_UPDATE_RATE, CURVE_FLATNESS);

    return startPathCommander();
  }
  
  private SmoothPath startPathCommander()
  {
    // if there is a live path commander kill it

    if (pathCommander != null)
      pathCommander.requestDeath();

    // make a fresh new commander on this smooth path, and follow it

    pathCommander = new PathCommander();
    pathCommander.start();

    // return the smooth path

    return activeSmoothPath;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#stop()
   */

  public void stop()
  {
    pathCommander.requestDeath();
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getActivePath()
   */

  public SmoothPath getActivePath()
  {
    return activeSmoothPath;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#deactivatePath()
   */

  public void deactivatePath()
  {
    activeSmoothPath = null;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getTargetYaw()
   */

  public double getTargetYaw()
  {
    return targetYaw.as(DEGREES);
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getTargetRoll()
   */

  public double getTargetRoll()
  {
    return targetRoll.as(DEGREES);
  }

  // get yaw

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getYaw()
   */
  public Angle getYaw()
  {
    return yaw; // .as(DEGREES);
  }

  // set yaw

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setYaw(org.trebor.util.Angle)
   */
  public Angle setYaw(Angle newYaw)
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getPitch()
   */
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getRoll()
   */
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#reverse()
   */
  public void reverse()
  {
    yaw = yaw.rotate(180, DEGREES);
  }

  // position getter

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getPosition()
   */
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#setPosition(double, double)
   */
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

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#getSurveyPosition()
   */

  public Point getSurveyPosition()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#isOriginAcked()
   */

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
        Waypoint wp = null;

        Iterator<Waypoint> wpIter = sp.iterator();

        while (!deathRequest)
        {
          if (wpIter.hasNext())
          {
            wp = wpIter.next();
            sleep(SwarmCon.secondsToMilliseconds(wp.getTime() - lastTime));
            commandWaypoint(wp);
            sp.setCurrentWaypoint(wp);
            lastTime = wp.getTime();
          }
          else
          {
            sleep(SwarmCon.secondsToMilliseconds(SMOOTH_PATH_UPDATE_RATE));
            AMotionModel.this.stop();
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.model.IMotionModel#toString()
   */
  public String toString()
  {
    return "MotionModel [yaw=" + yaw + ", pitch=" + pitch + ", roll=" + roll +
      ", direction=" + direction + ", position=" + position + ", velocity=" +
      velocity + ", yawRate=" + yawRate + ", targetRoll=" + targetRoll +
      ", targetVelocity=" + targetVelocity + ", targetYaw=" + targetYaw +
      ", pathCommander=" + pathCommander + "]";
  }
}
