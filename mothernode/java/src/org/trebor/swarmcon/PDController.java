package org.trebor.swarmcon;

public class PDController extends Controller
{
      protected double kp;
      protected double kd;
      
         // create a PD controller

      public PDController(String inputName, String outputName, 
                          double kp, double kd)
      {
         super(inputName, outputName);
         this.kp = kp;
         this.kd = kd;
      }
         // set tuner

      public void setTuner(PidTuner tuner)
      {
         super.setTuner(tuner);
         if (tuner != null)
         {
            tuner.setP(getKp());
            tuner.setD(getKd());
         }
      }
         // compute result

      public double compute(double measurement)
      {
            // compute error

         currentErr = target - measurement;
         totalErr += currentErr;
         previousErr = currentErr;
         
            // if there is a tuner, get values from it

         if (tuner != null)
         {
            setKp(tuner.getP());
            setKd(tuner.getD());
            tuner.addSample(measurement, getTarget());
         }
            // return computed command

         return kp * currentErr + kd * deltaErr();
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
