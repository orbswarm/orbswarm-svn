package com.orbswarm.swarmcon.behavior;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.io.JoystickManager;
import com.orbswarm.swarmcon.model.IMotionModel;

import static org.trebor.util.Angle.Type.DEGREE_RATE;
import static com.orbswarm.swarmcon.util.Constants.*;

public class JoyBehavior extends Behavior implements JoystickManager.Listener
{
  double steering;
  double power;

  // create a joy behavior

  public JoyBehavior()
  {
    super("Joy");
  }

  /**
   * Dispatch joystick axis event.
   * 
   * @param stick joystick on which this event occurred
   * @param axis number of axis which has changed
   * @param value value axis has changed to
   */

  public void joystickAxisChanged(int stick, int axis, double value)
  {
    // if (axis == SwarmCon.JOYSTICK_STEERING_AXIS)
    // steering = value;
    // else if(axis == SwarmCon.JOYSTICK_POWER_AXIS)
    // power = -value;
  }

  /**
   * Distpatch joystick hat event.
   * 
   * @param orb orb associated with this stick
   * @param hat number of hat which has changed
   * @param x X value of hat position
   * @param x Y value of hat position
   */

  public void joystickHatChanged(int stick, int hat, int x, int y)
  {
  }

  /**
   * Dispatch joystick button press event.
   * 
   * @param stick joystick on which this event occurred
   * @param button number of button which has changed
   */

  public void joystickButtonPressed(int stick, int button)
  {
  }

  /**
   * Dispatch joystick button release event.
   * 
   * @param stick joystick on which this event occurred
   * @param button number of button which has changed
   */

  public void joystickButtonReleased(int stick, int button)
  {
  }

  // update

  public void update(double time, IMotionModel model)
  {
    model.setTargetRoll(new Angle(steering * MAX_ROLL, DEGREE_RATE));
  }
}
