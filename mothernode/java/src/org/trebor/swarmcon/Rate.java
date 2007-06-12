package org.trebor.swarmcon;

import static java.lang.Math.*;

   // a rate

public class Rate
{
      private String name;
      private double rate;
      private double target;
      private double min;
      private double max;
      private double acceleration;

      public Rate(String name, double min, double max, double acceleration)
      {
         this.name = name;
         this.min = min;
         this.max = max;
         this.acceleration = acceleration;
      }
         // stipulate the rate

      public void setRate(double rate)
      {
         this.rate = max(min, min(rate, max));
      }
         // get current rate

      public double getRate()
      {
         return rate;
      }
         // set target rate

      public void setTarget(double target)
      {
         setWeightedTarget(target, 1);
      }
         // set weighted target rate

      public void setWeightedTarget(double target, double weight)
      {
         assert(weight >= 0 && weight <= 1);
         this.target = (1 - weight) * this.target + 
            weight * max(min, min(target, max));
      }
         // set target as normalized value from 0 to 1

      public void setNormalizedTarget(double target)
      {
         setNormalizedWeightedTarget(target, 1);
      }
         // set target as normalized value from 0 to 1

      public void setNormalizedWeightedTarget(double target, double weight)
      {
         setWeightedTarget(min + (max - min) * target, weight);
      }
         // get target

      public double getTarget()
      {
         return target;
      }
         // get target as a normalized value from 0 to 1

      public double getNormalizedTarget()
      {
         return (target - min) / (max - min);
      }
         // update the rate

      public double update(double time)
      {
         if (target > rate)
            rate = min(rate + acceleration * time, target);
         else if (target < rate)
            rate = max(rate - acceleration * time, target);

         return rate;
      }
}

