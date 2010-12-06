package com.orbswarm.swarmcon.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.trebor.util.Rate;

import com.orbswarm.swarmcon.model.SimModel;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.Dance;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.IMarker;
import com.orbswarm.swarmcon.path.Marker;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.path.SyncAction;
import com.orbswarm.swarmcon.store.IItem;
import com.orbswarm.swarmcon.store.IItemStore;
import com.orbswarm.swarmcon.store.Item;
import com.orbswarm.swarmcon.store.ItemStoreTest;
import com.orbswarm.swarmcon.swing.Builder;
import com.orbswarm.swarmcon.util.Constants;

public class EventFactoryTest
{
  private static Logger log = Logger.getLogger(EventFactoryTest.class);

  @Test
  @Ignore
  public void basicTest()
  {
    Rate rate = new Rate("Velocity", 0, Constants.ORB_MAX_SPEED, Constants.ORB_ACCELERATION);
    IOrb orb = new Orb(new SimModel(), 0);
    IBlock st = new StraightBlock(1);
    IBlockPath path = new BlockPath(st);
    double timeStep = rate.timeIn(st.getLength() / 2);
    log.debug("time step: "+ timeStep);

    IPerformance p = new Performance();
    p.addAll(EventFactory.create(path, orb, rate, timeStep));

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
  @Ignore
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
  
  @Test 
  public void markerTest()
  {
    IBlockPath bp1 = new BlockPath();
    bp1.addAfter(new StraightBlock(1));
    IBlockPath bp2 = new BlockPath();
    bp2.addAfter(new StraightBlock(2));
    IMarker m1 = new Marker(bp1, bp1.getLength());
    IMarker m2 = new Marker(bp2, bp2.getLength());
    m1.setSyncAction(new SyncAction(m1));
    m1.setSyncAction(new SyncAction(m2));
    log.debug("m1: " + m1);
    
    SyncAction sa = m1.getSyncAction();
    
    assertTrue(sa != null);
    assertTrue(sa.getSyncTo() == m2);
    
    log.debug("sa: " + sa);
  }

  
//  public static void main(String[] args)
//  {
//    IItemStore store = Builder.establishItemStore();
//    IDance dance = ItemStoreTest.createTestDance();
//    IItem<IDance> item = new Item<IDance>(dance, "Test Dance");
//    store.update(item);
//    log.debug("hello there");
//  }
  
  @Test
  public void synchTest()
  {
    // create the dance

    IDance dance = ItemStoreTest.createTestDance();

    List<Rate> rates = new Vector<Rate>();
    List<IOrb> orbs = new Vector<IOrb>();
    for (int i = 0; i < 2; ++i)
    {
      rates.add(new Rate("Velocity", 0, Constants.ORB_MAX_SPEED,
        Constants.ORB_ACCELERATION));
      orbs.add(new Orb(new SimModel(), i));
    }

    EventFactory.create(dance, orbs, rates, 0.25);
  }
}
