package com.orbswarm.swarmcon.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.Dance;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.IMarker;
import com.orbswarm.swarmcon.path.Marker;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.path.SyncAction;
import com.orbswarm.swarmcon.view.IRenderable;

public class ItemStoreTest
{
  IItemStore mStore;

  private static Logger log = Logger.getLogger(ItemStoreTest.class);
  private Marshaller mMarshaller;
  private Unmarshaller mUnmarshaller;

  @SuppressWarnings("serial")
  @XmlRootElement(name = "testItems")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class TestItems extends Vector<TestItem>
  {
    @SuppressWarnings("unused")
    private final List<TestItem> mItems;
    
    public TestItems()
    {
      mItems = this;
    }
  }
  
  @XmlRootElement(name = "testItem")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class TestItem 
  {
    public static int baseId = 100;
    private final String mValue;
    @SuppressWarnings("unused")
    @XmlID
    private final String mId;
    @XmlIDREF
    private final TestItem mOther;

    public TestItem(String value, TestItem other)
    {
      mValue = value;
      mOther = other;
      mId = "" + baseId++;
    }
    
    public TestItem()
    {
      this("-empty-", null);
    }

    public String getValue()
    {
      return mValue;
    }

    public TestItem getOther()
    {
      return mOther;
    }

    public String toString()
    {
      return "TestItem [mValue=" + mValue + ", mId=" + mId + ", mOther=" +
        mOther + "]";
    }
  };
  
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
  public void refIdTest()
  {

    TestItem ti1 = new TestItem("ti-1", null);
    TestItem ti2 = new TestItem("ti-2", ti1);
    TestItem ti3 = new TestItem("ti-3", ti2);
    TestItems items1 = new TestItems();

    items1.add(ti1);
    items1.add(ti2);
    items1.add(ti3);

    try
    {
      JAXBContext context =
        JAXBContext.newInstance(TestItem.class, TestItems.class);
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      StringWriter writer = new StringWriter();
      marshaller.marshal(items1, writer);
      log.debug("xml: " + writer.toString());

      StringReader reader = new StringReader(writer.toString());
      TestItems items2 = (TestItems)unmarshaller.unmarshal(reader);

      log.debug("result: " + items2);

      log.debug("id: " + items2.get(0).mId);

      for (int i = 0; i < items1.size(); ++i)
      {
        log.debug(String.format("1: %s 2: %s", items1.get(i), items2.get(i)));
        assertEquals(items1.get(i).mId, items2.get(i).mId);
        assertEquals(items1.get(i).mValue, items2.get(i).mValue);
        if (items1.get(i).mOther != null)
          assertTrue(items1.get(i).mOther.hashCode() != items2.get(i).mOther
            .hashCode());
        else
          assertTrue(items1.get(i).mOther == items2.get(i).mOther);
      }

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

  @Test
  public void testDanceMarshal()
  {
    try
    {
      StringWriter writer = new StringWriter();
      mMarshaller.marshal(createTestDance(), writer);
      log.debug("dance xml: " + writer.toString());
      IDance dance = (IDance)mUnmarshaller.unmarshal(new StringReader(writer.toString()));
      validateDance(dance);
    }
    catch (JAXBException e)
    {
      e.printStackTrace();
    }
  }
  
  @Test
  public void testDanceTest()
  {
    validateDance(createTestDance());
  }
  
  public static void validateDance(IDance dance)
  {
    List<IBlockPath> paths = dance.getPaths();
    assertEquals(2, paths.size());
    assertEquals(2, paths.get(0).getBlocks().size());
    assertEquals(2, paths.get(1).getBlocks().size());
    List<IMarker> markers = dance.getMarkers();
    assertEquals(4, markers.size());
    assertTrue(null == markers.get(1).getSyncAction());
    assertTrue(null == markers.get(3).getSyncAction());
    assertEquals(markers.get(1), markers.get(0).getSyncAction().getSyncTo());
    assertEquals(markers.get(3), markers.get(2).getSyncAction().getSyncTo());
  }
  
  public static IDance createTestDance()
  {
    IDance dance = new Dance();
    dance.setLayout(Dance.Layout.LINE);
  
    // create the paths
  
    IBlockPath bp1 = new BlockPath();
    bp1.addAfter(new StraightBlock(1));
    bp1.addAfter(new StraightBlock(2));
    IBlockPath bp2 = new BlockPath();
    bp2.addAfter(new StraightBlock(3));
    bp2.addAfter(new StraightBlock(4));
    dance.addAfter(bp1);
    dance.addAfter(bp2);
  
    // add markers to the path
  
    IMarker m1 = new Marker(bp1, bp1.getLength() * 0.25);
    IMarker m2 = new Marker(bp2, bp2.getLength() * 0.5);
    m1.setSyncAction(new SyncAction(m2));
    IMarker m3 = new Marker(bp1, bp1.getLength() * 0.5);
    IMarker m4 = new Marker(bp2, bp2.getLength() * 0.75);
    m3.setSyncAction(new SyncAction(m4));
    dance.add(m1);
    dance.add(m2);
    dance.add(m3);
    dance.add(m4);
    
    return dance;
  }
}
