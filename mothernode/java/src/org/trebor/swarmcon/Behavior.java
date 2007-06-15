package org.trebor.swarmcon;

   // a behavior object controls the behavior of an orb by setting the
   // target heading and velocity for a given orb, the behavior also has
   // access to the swarm

public abstract class Behavior
{
         // name of this behavior

      String name;
      
         // orb is set when this behavior is added to a particular orb

      protected Orb orb;

         // construct a behavior

      public Behavior(String name)
      {
         this.name = name;
      }
         // set orb for this behavior

      public void setOrb(Orb orb)
      {
         this.orb = orb;
      }
         // update the state of the orb, time (in seconds) provided
      
      abstract public void update(double time, MotionModel model);

         // convert to a string

      public String toString()
      {
         return name;
      }
}
