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
         assert(false);
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
         this.target = target;
      }
         // set target as normalized value from -1 to 1

      public void setNormalizedTarget(double target)
      {
         assert(target >= -1 && target <= 1);
         setTarget(min + (max - min) * ((target + 1) / 2));
      }
         // get target

      public double getTarget()
      {
         return target;
      }
         // get target as a normalized value from 0 to 1

      public double getNormalizedTarget()
      {
         return ((target - min) / (max - min)) * 2 - 1;
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
