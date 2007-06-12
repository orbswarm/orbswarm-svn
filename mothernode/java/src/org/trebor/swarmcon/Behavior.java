package org.trebor.swarmcon;

   // a behavior object controls the behavior of an orb by setting the
   // target heading and velocity for a given orb, the behavior also has
   // access to the swarm

public abstract class Behavior
{
         // name of this behavior

      String name;

         // set these values to drive the orb

      private double targetDistance = 0;
      private double distance = 0;
      private double targetHeading = 0;
      
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
         // clone values from other behavior

      public void cloneValues(Behavior other)
      {
         this.targetDistance = other.targetDistance;
         this.distance = other.distance;
         this.targetHeading = other.targetHeading;
      }
         // update the state of the orb, time (in seconds) provided
      
      abstract public void update(double time);

         // set target distance

      public void setTargetDistance(double target, double distance)
      {
         targetDistance = target;
         this.distance = distance;
      }
         // set target heading

      public void setTargetHeading(double target)
      {
         targetHeading = target;
      }
         // get measured distance

      public double getDistance()
      {
         return distance;
      }
         // get target distance

      public double getTargetDistance()
      {
         return targetDistance;
      }
         // get target heading

      public double getTargetHeading()
      {
         return targetHeading;
      }
         // convert to a string

      public String toString()
      {
         return name;
      }
}
