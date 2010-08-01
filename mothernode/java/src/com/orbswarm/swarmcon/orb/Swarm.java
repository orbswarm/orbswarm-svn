package com.orbswarm.swarmcon.orb;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;


@SuppressWarnings("serial")
public class Swarm extends Mobjects
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Orb.class);

  // arena in which to swarm

  private Rectangle2D.Double arena;

  // globals

  private Point2D.Double centroid = new Point2D.Double();

  // construct a swarm

  public Swarm(Rectangle2D.Double arena)
  {
    this.arena = arena;
  }

  // randomize position of items in swarm

  public void randomize()
  {
    for (IMobject mobject : this)
      if (mobject instanceof Orb)
        ((IOrb)mobject).randomizePos();
  }

  // get arena

  public Rectangle2D.Double getArena()
  {
    return arena;
  }

  /**
   * Compute center of arena.
   * 
   * @return center of the arena
   */

  public Point2D.Double getCenter()
  {
    return new Point2D.Double(arena.getX() + arena.getWidth() / 2, arena
      .getY() +
      arena.getHeight() / 2);
  }

  // get centroid

  public Point2D.Double getCentroid()
  {
    return centroid;
  }

  // select next behavoir for all orbs in swarm

  public void nextBehavior()
  {
    for (IMobject mobject : this)
      if (mobject instanceof Orb)
        ((IOrb)mobject).nextBehavior();
  }

  // select previous behavoir for all orbs in swarm

  public void previousBehavior()
  {
    for (IMobject mobject : this)
      if (mobject instanceof Orb)
        ((IOrb)mobject).previousBehavior();
  }

  // update the swarm

  public void update(double time)
  {
    // establish centroid of swarm

    centroid.x = 0;
    centroid.y = 0;
    for (IMobject mobject : this)
    {
      centroid.x += mobject.getPosition().x;
      centroid.y += mobject.getPosition().y;
    }
    centroid.x /= size();
    centroid.y /= size();

    // update individual mobjects

    for (IMobject mobject : this)
      mobject.update(time);
  }

  //
  // methods implementing com.orbswarm.choreography.Swarm interface
  //
  public IOrb getOrb(int orbNum)
  {
    // assuming orbs are in the Vector in order of their Ids...
    for (IMobject mobject : this)
    {
      if (mobject instanceof Orb)
      {
        IOrb orb = (IOrb)mobject;
        if (orb.getId() == orbNum)
        {
          return orb;
        }
      }
    }
    return null; // ? (com.orbswarm.choreography.Orb)
  }

  public int getNumOrbs()
  {
    return size();
  }

  public void updateOrbDistances()
  {
    for (IMobject mobject : this)
    {
      if (mobject instanceof Orb)
      {
        ((IOrb)mobject).calculateDistances();
      }
    }
  }

}


