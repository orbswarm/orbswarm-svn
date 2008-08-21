package com.orbswarm.swarmcon;

import java.lang.Thread;
import org.trebor.util.Angle;

import static com.orbswarm.swarmcon.Message.Type.*;
import static com.orbswarm.swarmcon.Message.Field.*;

/** This class is linkage to a live orb */

public class LiveModel extends MotionModel
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

    /** Construct a live motion model which links to a real orb
     * rolling around in the world.
     *
     * @param orbIo the commincations linkn to the physical orb
     */

    public LiveModel(OrbIo orbIo, int orbId, double reportOffset)
    {
      SwarmCon sc = SwarmCon.getInstance();
      this.orbIo = orbIo;
      this.orbId = orbId;
      double now = SwarmCon.getTime();
      lastPositionReportTime = now;
      lastPositionRequestTime = now + reportOffset;
      positionPollPeriod = SwarmCon.positionPollPeriod / 1000d;
    }

    /** Handel messages from the orb. */

    public void onOrbMessage(Message message)
    {
      if (message.getType() == POSITION_REPORT)
      {
        // compute times

        double newPositionReportTime = SwarmCon.getTime();

        // get current position

        Point position = new Point(
           message.getDoubleProperty(EASTING),
           message.getDoubleProperty(NORTHING));

        // if the global offset has not yet been initialized do that with
        // the current position of this very first orb message

        if (!globalOffsetInitialized)
        {
          SwarmCon.getInstance().setGlobalOffset(
            new Point(-position.getX(), -position.getY()));
          globalOffsetInitialized = true;
        }
        
        // if the orb has already received some real world data then we
        // can go ahead and infer speed and heading from prevouse
        // position
        
        if (orbPositionInitialized)
        {
          // compute distance and time

          double time = newPositionReportTime - lastPositionReportTime;
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
        lastPositionReportTime = newPositionReportTime;
      }
    }

    /** Update the model state by sending a request to the orb for state
     * information.  When recive the local model will be updated to
     * reflect that state information.
     */

    public void update(double time)
    {
      double now = SwarmCon.getTime();
      if (now - lastPositionRequestTime > positionPollPeriod)
      {
        orbIo.requestPositionReport(orbId);
        lastPositionRequestTime = now;
      }
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

   /** Command position.
    *
    * @param target target position
    */
    
    public void setTargetPosition(Target target)
    {
      // create a path

      Path path = new Path();

      // add the current orb position

      path.add(new Target(position));

      // add the intermediate point between the orb and the target

      path.add(new Target(yaw.cartesian(HEAD_ROOM / 4, true, getX(), getY())));
      path.add(new Target(yaw.cartesian(HEAD_ROOM, true, getX(), getY())));

      // add the actual target

      path.add(target);

      // command out the path

      setTargetPath(path);
    }

    /** Command a path.
     *
     * @param path the path the orb should follow
     */

    public void setTargetPath(Path path)
    {
      // make a smooth path to follow

      SmoothPath sp = new SmoothPath(
        path, velocityRate, 
        SMOOTH_PATH_UPDATE_RATE, 
        CURVE_WIDTH, CURVE_FLATNESS);
      
      // if there is a live path commander kill it

      if (pathCommander != null)
        pathCommander.requestDeath();

      // make a fresh new commander on this smooth path, and follow it

      pathCommander = new PathCommander(sp);
      pathCommander.start();
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

              // send the waypoint

              orbIo.sendWaypoint(orbId, wp);
              smob.setCurrentWaypoint(wp);
              //System.out.println("wp -> " + wp);

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
