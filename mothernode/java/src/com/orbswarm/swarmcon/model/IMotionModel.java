package com.orbswarm.swarmcon.model;

import static org.trebor.util.Angle.Type.DEGREE_RATE;

import java.awt.geom.Point2D;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.Path;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;

public interface IMotionModel
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

  /** Update the motion model. */

  void update(double time);

  /**
   * Get the yaw rate of the orb.
   * 
   * @return yaw rate in degrees per second
   */

  Angle getYawRate();

  /**
   * Get the current speed of the orb. This takes into account which way
   * the orb is facing and will return negative values if the orb is
   * backing up.
   * 
   * @return velocity in meters per second
   */

  double getSpeed();

  /**
   * Get the current velocity of the orb. Always returns a positive value.
   * 
   * @return velocity in meters per second
   */

  double getVelocity();

  /**
   * Get the current direction of the orb.
   * 
   * @return direction as a heading
   */

  Angle getDirection();

  /**
   * Command target roll.
   * 
   * @param targetRoll target roll
   */
  void setTargetRoll(Angle targetRoll);

  /**
   * Command target velocity.
   * 
   * @param targetVelocity target velocity
   */
  void setTargetVelocity(double targetVelocity);

  /**
   * Report target velocity.
   * 
   * @return target velocity
   */

  double getTargetVelocity();

  /**
   * Command target yaw.
   * 
   * @param targetYaw target yaw
   */

  void setTargetYaw(Angle targetYaw);

  /**
   * Command position.
   * 
   * @param target target position
   */

  SmoothPath setTargetPosition(Target target);

  /**
   * Command a path.
   * 
   * @param path the path the orb should follow
   */

  SmoothPath setTargetPath(Path path);
  
  /**
   * Command a block path.
   * 
   * @param path the path the orb should follow
   */

  SmoothPath setTargetPath(IBlockPath path);

  /** Stop the orb. */

  void stop();

  /** Get the active smooth path. */

  SmoothPath getActivePath();

  /** Deactivate active smooth path. */

  void deactivatePath();

  /** Get target yaw. */

  double getTargetYaw();

  /** Get target roll. */

  double getTargetRoll();

  Angle getYaw();

  Angle setYaw(Angle newYaw);

  Angle getPitch();

  Angle getRoll();

  void reverse();

  Point2D getPosition();

  void setPosition(double x, double y);

  /** Return the orb survey position, or null if we haven't got one yet. */

  Point2D getSurveyPosition();

  /** Return true if the orb has acknowledged the origin command. */

  boolean isOriginAcked();

  String toString();

}