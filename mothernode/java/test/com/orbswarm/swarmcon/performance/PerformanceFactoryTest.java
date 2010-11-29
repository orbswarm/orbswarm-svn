package com.orbswarm.swarmcon.performance;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.trebor.util.Rate;

import com.orbswarm.swarmcon.model.SimModel;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.StraightBlock;

public class PerformanceFactoryTest
{
  private static Logger log = Logger.getLogger(PerformanceFactoryTest.class);

  @Test
  public void basicTest()
  {
    double timeStep = 3.45;
    Rate rate = new Rate("Velocity", 0, 1.0, 0.08);
    IOrb orb = new Orb(new SimModel(), 0);
    IBlock st = new StraightBlock(1);
    IBlockPath path = new BlockPath(st);

    IPerformance p = PerformanceFactory.create(path, orb, rate, timeStep);
    
    log.debug("size: " + p.getEvents().size());

    double oldY = -1;
    for (IEvent e: p.getEvents())
    {
      PositionEvent pe = (PositionEvent)e;
      assertEquals(0, pe.getPosition().getX(), 0);
      assertTrue(pe.getPosition().getY() > oldY);
      oldY = pe.getPosition().getY();
      
      log.debug(String.format("t: %4.2f, y: %5.3f", e.getExecuteTime(), pe.getPosition().getY()));
    }
    
    assertEquals(2, p.getEvents().size(), 0);
  }
}
