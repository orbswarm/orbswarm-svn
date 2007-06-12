package org.trebor.swarmcon;

public abstract class Controller
{
      protected double target;
      protected double currentErr;
      protected double previousErr;
      protected double totalErr;

         // set target

      public void setTarget(double target)
      {
         this.target = target;
      }
         // compute result

      abstract public double compute(double measurement);

         // comput dErr/dt

      public double deltaErr()
      {
         return currentErr - previousErr;
      }
}
