package org.trebor.swarmcon;

public abstract class Controller
{
      protected double target;
      protected double currentErr;
      protected double previousErr;
      protected double totalErr;
      protected PidTuner tuner;

         // set tuner

      public void setTuner(PidTuner tuner)
      {
         this.tuner = tuner;
      }
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
         // get current target

      public double getTarget()
      {
         return target;
      }
}
