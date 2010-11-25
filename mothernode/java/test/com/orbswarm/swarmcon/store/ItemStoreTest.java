package com.orbswarm.swarmcon.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.view.IRenderable;

public class ItemStoreTest
{
  IItemStore mStore;

  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(ItemStoreTest.class);
  private Marshaller mMarshaller;
  private Unmarshaller mUnmarshaller;
  
  @Before
  public void setup()
  {
    mStore = new TestStore();
    try
    {
      JAXBContext context = AItemStore.createContext();
      mMarshaller = context.createMarshaller();
      mUnmarshaller = context.createUnmarshaller();
      mMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
  }

  @Test
  public void blockTest()
  {
    StraightBlock st1 = new StraightBlock(2);
    CurveBlock cb1 = new CurveBlock(10, 2, CurveBlock.Type.LEFT);
    StringWriter writer1 = new StringWriter();
    StringWriter writer2 = new StringWriter();
    try
    {
      log.debug("pre marsal");
      mMarshaller.marshal(st1, writer1);
      mMarshaller.marshal(cb1, writer2);
      log.debug("post marsal");
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }

    log.debug("xml 1: " + writer1.toString());
    log.debug("xml 1: " + writer2.toString());
    
    StringReader reader1 = new StringReader(writer1.toString());
    StringReader reader2 = new StringReader(writer2.toString());
    StraightBlock st2 = null;
    CurveBlock cb2 = null;
    try
    {
      log.debug("pre unmarsal");
      st2 = (StraightBlock)mUnmarshaller.unmarshal(reader1);
      cb2 = (CurveBlock)mUnmarshaller.unmarshal(reader2);
      log.debug("post unmarsal");
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
    
    log.debug("st1: " + st1.hashCode());
    log.debug("st2: " + st2.hashCode());
    log.debug("cb1: " + cb1.hashCode());
    log.debug("cb2: " + cb2.hashCode());

    assertEquals(st1.getLength(), st2.getLength(), 0);
    assertEquals(cb1.getLength(), cb2.getLength(), 0);
    assertEquals(cb1.getExtent(), cb2.getExtent(), 0);
    assertEquals(cb1.getRadius(), cb2.getRadius(), 0);
    assertEquals(cb1.getType(), cb2.getType());
  }

  @Test
  public void blockPathTest()
  {
    log.debug("start test");

    int curveCount = 0;
    int straightCount = 0;

    CurveBlock lt = new CurveBlock(45, 4, CurveBlock.Type.LEFT);
    CurveBlock rt = new CurveBlock(90, 3, CurveBlock.Type.RIGHT);
    StraightBlock st = new StraightBlock(2);

    BlockPath bp1 = new BlockPath(st, lt, st, lt, rt, rt);
    BlockPath bp2 = new BlockPath(rt, st, rt, rt, lt);
    BlockPath bp3 = new BlockPath(st, lt, rt, rt);

    Map<String, BlockPath> paths = new HashMap<String, BlockPath>();
    paths.put("path1", bp1);
    paths.put("path2", bp2);
    paths.put("path3", bp3);

    for (Entry<String, BlockPath> entry : paths.entrySet())
      mStore.create(entry.getValue(), entry.getKey());

    for (IItem<? extends IRenderable> item : mStore.getAll())
    {
      BlockPath newBp = (BlockPath)item.getItem();
      BlockPath oldBp = paths.get(item.getName());

      List<IBlock> newBlocks = newBp.getBlocks();
      List<IBlock> oldBlocks = oldBp.getBlocks();

      assertEquals(oldBlocks.size(), newBlocks.size());

      for (int i = 0; i < newBlocks.size(); ++i)
      {
        IBlock newBlock = newBlocks.get(i);
        IBlock oldBlock = oldBlocks.get(i);
        log.debug("old block: " + oldBlock.hashCode());
        log.debug("new block: " + newBlock.hashCode());

        assertFalse(oldBlock == newBlock);

        if (newBlock instanceof CurveBlock)
        {
          CurveBlock newCurve = (CurveBlock)newBlock;
          CurveBlock oldCurve = (CurveBlock)oldBlock;
          log.debug("old curve: " + oldCurve);
          log.debug("new curve: " + newCurve);

          assertEquals(oldCurve.getExtent(), newCurve.getExtent(), 0);
          assertEquals(oldCurve.getRadius(), newCurve.getRadius(), 0);
          assertEquals(oldCurve.getType(), newCurve.getType());
          curveCount++;

        }

        if (newBlock instanceof StraightBlock)
        {
          StraightBlock newStraight = (StraightBlock)newBlock;
          StraightBlock oldStraight = (StraightBlock)oldBlock;
          log.debug("old straight: " + oldStraight);
          log.debug("new straight: " + newStraight);

          assertEquals(oldStraight.getLength(), newStraight.getLength(), 0);
          straightCount++;
        }
      }
    }

    assertEquals(4, straightCount);
    assertEquals(11, curveCount);
  }
}
