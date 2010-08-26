package com.orbswarm.swarmcon.swing;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

/** SwarmCon action class */

public abstract class SwarmAction extends AbstractAction
{
  private static final long serialVersionUID = 2376655282485450773L;

  // construct the action
  public SwarmAction(String name, KeyStroke key, String description)
  {
    super(name);
    putValue(NAME, name);
    putValue(SHORT_DESCRIPTION, description);
    putValue(ACCELERATOR_KEY, key);
  }

  /**
   * Return accelerator key for this action.
   * 
   * @return accelerator key for this action
   */

  public KeyStroke getAccelerator()
  {
    return (KeyStroke)getValue(ACCELERATOR_KEY);
  }

  /**
   * Return name of this action.
   * 
   * @return name of this action
   */

  public String getName()
  {
    return (String)getValue(NAME);
  }
}