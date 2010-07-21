package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.io.JoystickManager;
import com.orbswarm.swarmcon.model.MotionModel;

import static org.trebor.util.Angle.Type.*;

public class JoyBehavior extends Behavior implements JoystickManager.Listener
{
      double steering;
      double power;
      
      // create a joy behavior
      
      public JoyBehavior()
      {
         super("Joy");
      }

      /** Dispatch joystick axis event.
       *
       * @param stick joystick on which this event occurred
       * @param axis number of axis which has changed
       * @param value value axis has changed to
       */

      public void joystickAxisChanged(int stick, int axis, double value)
      {
//          if (axis == SwarmCon.JOYSTICK_STEERING_AXIS)
//             steering = value;
//          else if(axis == SwarmCon.JOYSTICK_POWER_AXIS)
//             power = -value;
      }

      /** Distpatch joystick hat event.
       *
       * @param orb orb assocated with this stick
       * @param hat number of hat which has changed
       * @param x   X value of hat postion
       * @param x   Y value of hat postion
       */

      public void joystickHatChanged(int stick, int hat, int x, int y)
      {
      }

      /** Distpatch joystick button press event.
       *
       * @param stick joystick on which this event occured
       * @param button number of button which has changed
       */

      public void joystickButtonPressed(int stick, int button)
      {
      }

      /** Distpatch joystick button release event.
       *
       * @param stick joystick on which this event occured
       * @param button number of button which has changed
       */

      public void joystickButtonReleased(int stick, int button)
      {
      }

      // update

      public void update(double time, MotionModel model)
      {
        model.setTargetRoll(new Angle(steering * SwarmCon.MAX_ROLL, DEGREE_RATE));
      }
    
}
