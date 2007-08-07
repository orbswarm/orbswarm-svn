package com.orbswarm.swarmcon;

/** This class is linkage to a live orb */

public class LiveModel extends MotionModel
{
      /** orb communications object */

      private OrbIo orbIo;

      /** Construct a live motion model which links to a real orb
       * rolling around in the world.
       *
       * @param orbIo the commincations linkn to the physical orb
       */
      
      public LiveModel(OrbIo orbIo)
      {
         this.orbIo = orbIo;
      }
      /** update the models state */

      public void update(double time)
      {
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
