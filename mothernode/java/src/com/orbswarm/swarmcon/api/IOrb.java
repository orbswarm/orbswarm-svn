package com.orbswarm.swarmcon.api;

import java.awt.Color;

/** This is the one true interface for interactin with an orb.  All orb
 * properties, like it's location, color or any other state should be
 * set and gotten via this interface.
 * 
 * @author trebor
 */

public interface IOrb
{
  public void setColor(Color color);
}

