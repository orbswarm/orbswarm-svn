package com.orbswarm.swarmcon.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.PriorityQueue;
import java.util.Queue;

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
    for (IEvent e : p.getEvents())
    {
      PositionEvent pe = (PositionEvent)e;
      assertEquals(0, pe.getPosition().getX(), 0);
      assertTrue(pe.getPosition().getY() > oldY);
      oldY = pe.getPosition().getY();

      log.debug(String.format("t: %4.2f, y: %5.3f", e.getExecuteTime(), pe
        .getPosition().getY()));
    }

    assertEquals(2, p.getEvents().size(), 0);
  }
  
  @Test
  public void treeTest()
  {
    class Foo implements Comparable<Foo>
    {
      private final double mValue;
      
      public Foo(double value)
      {
        mValue = value;
      }
      
      @Override 
      public boolean equals(Object other)
      {
        return this.hashCode() == other.hashCode();
      }
      
      public int compareTo(Foo o)
      {
        return Double.compare(mValue, ((Foo)o).mValue);
      }
    };
    
    Queue<Foo> queue = new PriorityQueue<Foo>();
    
    assertTrue(queue.add(new Foo(1)));
    assertTrue(queue.add(new Foo(2)));
    assertTrue(queue.add(new Foo(3)));
    assertTrue(queue.add(new Foo(1)));
    assertEquals(4, queue.size(), 0);
    
    double current = 0;
    while (!queue.isEmpty())
    {
      Foo foo = queue.poll();
      assertTrue(foo.mValue >= current);
      current = foo.mValue;
    }
  }
}
