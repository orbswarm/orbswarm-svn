package org.trebor.swarmcon;

public class PIDController extends Controller
{
      protected double kp;
      protected double ki;
      protected double kd;
      
         // create a PD controller

      public PIDController(String name, double kp, double ki, double kd)
      {
         super(name);
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
         // set kp

      public void setKp(double kp)
      {
         this.kp = kp;
      }
         // get kp

      public double getKp()
      {
         return kp;
      }
         // set ki

      public void setKi(double ki)
      {
         this.ki = ki;
      }
         // get ki

      public double getKi()
      {
         return ki;
      }
         // set kd

      public void setKd(double kd)
      {
         this.kd = kd;
      }
         // get kd

      public double getKd()
      {
         return kd;
      }
}
