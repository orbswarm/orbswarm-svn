package com.orbswarm.swarmcon.orb;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.mobject.AMobjects;

@SuppressWarnings("serial")
public class Swarm extends AMobjects<IOrb>
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Orb.class);

  // select next behavior for all orbs in swarm

  public void nextBehavior()
  {
    for (IOrb orb: this)
      orb.nextBehavior();
  }

  // select previous behavior for all orbs in swarm

  public void previousBehavior()
  {
    for (IOrb orb: this)
      orb.previousBehavior();
  }

  public IOrb getOrb(int orbNum)
  {
    // assuming orbs are in the Vector in order of their IDs
    
    for (IOrb orb: this)
      if (orb.getId() == orbNum)
        return orb;

    return null;
  }

  public void updateOrbDistances()
  {
    for (IOrb orb: this)
      orb.calculateDistances();
  }
}
