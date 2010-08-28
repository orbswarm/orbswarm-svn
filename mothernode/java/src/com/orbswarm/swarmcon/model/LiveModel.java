package com.orbswarm.swarmcon.model;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.io.Message;
import com.orbswarm.swarmcon.io.OrbIo;
import com.orbswarm.swarmcon.path.Point;
import com.orbswarm.swarmcon.path.Waypoint;
import com.orbswarm.swarmcon.swing.SwarmCon;

import static com.orbswarm.swarmcon.io.Message.Field.*;
import static com.orbswarm.swarmcon.io.Message.Type.*;
import static org.trebor.util.Angle.Type.*;

/** This class is linkage to a live orb */

public class LiveModel extends AMotionModel
{
  /**
   * Note that we have not yet initialized the global offset, which is
   * going to be some big ugly number based on on the UTM values we're
   * getting from the GPS. Once we start getting values we'll want to
   * assume that the very first orb position is at (0,0) for display
   * purposes. The actual math for that is handled in SwarmCon.
   */

  /**
   * Note that the orb has not yet received, real world information about
   * is location.
   */

  private boolean orbPositionInitialized = false;

  /** The initial location of the orb when it woke up. */

  private Point surveyPosition = null;

  /** Set to true if the orb has acknowledged the origin commanded. */

  private boolean originAcked = false;

  /** Time of the last position report. */

  private double lastPositionReportTime;

  /** Time of the last position request. */

  private double lastPositionRequestTime;

  /** Period to wait between position requests. */

  private double positionPollPeriod;

  /** The id of the orb of which this is a model. */

  private int orbId;

  /** orb communications object */

  private OrbIo orbIo;

  /**
   * Construct a live motion model which links to a real orb rolling around
   * in the world.
   * 
   * @param orbIo the communications link to the physical orb
   */

  public LiveModel(OrbIo orbIo, int orbId, double reportOffset)
  {
    this.orbIo = orbIo;
    this.orbId = orbId;
    double now = SwarmCon.getTime();
    lastPositionReportTime = now;
    lastPositionRequestTime = now + reportOffset;
    positionPollPeriod = positionPollPeriod / 1000d;
  }

  /** Handle messages from the orb. */

  public void onOrbMessage(Message message)
  {
    // handle survey report

    if (message.getType() == SURVEY_REPORT)
    {
      surveyPosition = new Point(message.getDoubleProperty(EASTING), message
        .getDoubleProperty(NORTHING));
    }

    // handle origin ACK

    else if (message.getType() == ORIGIN_ACK)
    {
      originAcked = true;
    }

    // handle position report

    else if (message.getType() == POSITION_REPORT)
    {
      // compute times

      double newPositionReportTime = SwarmCon.getTime();

      // get current position

      Point position = new Point(message.getDoubleProperty(EASTING), message
        .getDoubleProperty(NORTHING));

      // if the global offset has not yet been initialized do that with
      // the current position of this very first orb message

      // if (!globalOffsetInitialized)
      // {
      // SwarmCon.getInstance().setGlobalOffset(
      // new Point(-position.getX(), -position.getY()));
      // globalOffsetInitialized = true;
      // }

      // if the orb has already received some real world data then we
      // can go ahead and infer speed and heading from previous
      // position

      if (orbPositionInitialized)
      {
        // compute distance and time

        double time = newPositionReportTime - lastPositionReportTime;
        double distance = getPosition().distance(position);

        // and from that the velocity

        setVelocity(distance / time);
      }
      else
        orbPositionInitialized = true;

      // update our current position, and time

      setYaw(new Angle(message.getDoubleProperty(YAW), RADIANS));
      setPosition(position);
      lastPositionReportTime = newPositionReportTime;
    }
  }

  /** Return the survey position, or null if we haven't got one yet. */

  public Point getSurveyPosition()
  {
    return surveyPosition;
  }

  /** Return true if the orb has ACKed the origin command. */

  public boolean isOriginAcked()
  {
    return originAcked;
  }

  /**
   * Update the model state by sending a request to the orb for state
   * information. When receive the local model will be updated to reflect
   * that state information.
   */

  public void update(double time)
  {
    double now = SwarmCon.getTime();
    if (originAcked && now - lastPositionRequestTime > positionPollPeriod)
    {
      orbIo.requestPositionReport(orbId);
      lastPositionRequestTime = now;
    }
  }

  /**
   * Get the current velocity of the orb.
   * 
   * @return velocity in meters per second
   */

  public double getVelocity()
  {
    return 0;
  }

  /**
   * Get the yaw rate of the orb.
   * 
   * @return yaw create in angular units per second
   */

  public Angle getYawRate()
  {
    return new Angle();
  }

  /** Command the orb to the next waypoint. */

  protected void commandWaypoint(Waypoint wp)
  {
    orbIo.sendWaypoint(orbId, wp);
  }
}
