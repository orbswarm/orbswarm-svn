package org.trebor.swarmcon;

public class PIDController extends Controller
{
      protected double kp;
      protected double ki;
      protected double kd;
      
         // create a PD controller

      public PIDController(double kp, double ki, double kd)
      {
         this.kp = kp;
         this.ki = ki;
         this.kd = kd;
      }
         // compute result

      public double compute(double measurement)
      {
            // compute error

         currentErr = target - measurement;
         totalErr += currentErr;
         previousErr = currentErr;
         
            // return computed command

         return kp * currentErr + ki * totalErr + kd * deltaErr();
      }
}
