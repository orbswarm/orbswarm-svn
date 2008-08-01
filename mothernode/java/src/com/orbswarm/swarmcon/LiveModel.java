package com.orbswarm.swarmcon;

import org.trebor.util.Angle;

import static com.orbswarm.swarmcon.Message.Type.*;
import static com.orbswarm.swarmcon.Message.Field.*;

/** This class is linkage to a live orb */

public class LiveModel extends MotionModel
{
    /** Note that we have not yet initialized the global offset, which
     * is gonna be some big ugly number based on on the UTM values we're
     * getting from the GPS.  Once we start getting values we'll want to
     * assume that the very first orb position is at (0,0) for display
     * purposes.  The actual math for that is handled in SwarmCon.
     */

    private static boolean globalOffsetInitialized = false;

    /** Note that the orb has not yet received, real world inforation
     * about is locaion. */
    
    private boolean orbPositionInitialized = false;
    
    /** Time of the last update so we can compute 1st derivites. */

    private double lastUpdateTime;

    /** The id of the orb of which this is a model. */

    private int orbId;

    /** orb communications object */

    private OrbIo orbIo;

    /** Construct a live motion model which links to a real orb
     * rolling around in the world.
     *
     * @param orbIo the commincations linkn to the physical orb
     */

    public LiveModel(Orb orb, int orbId)
    {
      this.orbId = orbId;
      this.orbIo = orbIo;
      lastUpdateTime = SwarmCon.getTime();
    }

    /** Handel messages from the orb. */

    public void onOrbMessage(Message message)
    {
      if (message.getType() == POSITION_REPORT)
      {
        // compute times

        double newUpdateTime = SwarmCon.getTime();

        // get current position

        Point position = new Point(
          message.getDoubleProperty(EASTING),
          message.getDoubleProperty(NORTHING));

        // if the global offset has not yet been initialized do that with
        // the current position of this very first orb message
        
        if (!globalOffsetInitialized)
        {
          SwarmCon.getInstance().setGlobalOffset(getPosition());
          globalOffsetInitialized = true;
        }
        
        // if the orb has already received some real world data then we
        // can go ahead and infer speed and heading from prevouse
        // position
        
        if (orbPositionInitialized)
        {
          // compute distance and time

          double time = newUpdateTime - lastUpdateTime;
          double distance = getPosition().distance(position);

          // and from that the velocity

          setVelocity(distance / time);

          // and now compute yaw
          
          Angle yaw = new Angle(getPosition(), position);
          setYaw(yaw.degrees());
        }
        else
          orbPositionInitialized = true;

        // update our current postion, and time

        setPosition(position);
        lastUpdateTime = newUpdateTime;
      }
    }

    /** Update the model state by sending a request to the orb for state
     * information.  When recive the local model will be updated to
     * reflect that state information.
     */

    public void update(double time)
    {
      orbIo.requestPositionReport(orbId);
    }

    /** Get the current velocity of the orb.
     *
     * @return velocity in meters per second
     */

    public double getVelocity()
    {
      return 0;
    }
    /** Get the yaw rate of the orb.
     *
     * @return yaw reate in degrees per second
     */

    public double getYawRate()
    {
      return 0;
    }
}
