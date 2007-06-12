package org.trebor.swarmcon;

public class PDController extends Controller
{
      protected double kp;
      protected double kd;
      
         // create a PD controller

      public PDController(double kp, double kd)
      {
         this.kp = kp;
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

         return kp * currentErr + kd * deltaErr();
      }
}
