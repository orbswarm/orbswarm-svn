package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.vobject.AVobject;

@XmlRootElement(name="blockpath")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlockPath extends AVobject implements IBlockPath
{
  private static final long serialVersionUID = -8911696643772151060L;
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPath.class);

  @XmlElementWrapper(name="blocks")
  @XmlAnyElement
  private Collection<IBlock> mBlocks;
  @XmlTransient
  private ListIterator<IBlock> mBlockItr;
  
  public BlockPath()
  {
    mBlocks = new Vector<IBlock>();
    mBlockItr = ((Vector<IBlock>)mBlocks).listIterator();
  }

  public BlockPath(IBlock... blocks)
  {
    this();
    for (IBlock block: blocks)
      addAfter(block);
  }

  public Collection<IBlock> getBlocks()
  {
    return Collections.unmodifiableCollection(mBlocks);
  }

  public void addBefore(IBlock... blocks)
  {
    select(false);
    for (IBlock block : blocks)
    {
      if (mBlockItr.hasPrevious())
        mBlockItr.previous();
      mBlockItr.add(block);
    }
    select(true);
  }

  public void addAfter(IBlock... blocks)
  {
    if (blocks.length > 0)
    {
      select(false);
      if (mBlockItr.hasNext())
        mBlockItr.next();
      for (IBlock block : blocks)
        mBlockItr.add(block);
      mBlockItr.previous();
      select(true);
    }
  }
  
  private void select(boolean selected)
  {
    if (!mBlocks.isEmpty())
      getCurrentBlock().setSelected(selected);
  }
  
  public IBlock getCurrentBlock()
  {
    IBlock current = null;
    
    if (!mBlocks.isEmpty())
    {
      current = mBlockItr.next();
      mBlockItr.previous();
    }
    
    return current;
  }
  
  public void replace(IBlock block)
  {
    select(false);
    mBlockItr.set(block);
    select(true);
  }

  public int size()
  {
    return mBlocks.size();
  }

  public boolean remove()
  {
    log.debug("remove block");
    if (!mBlocks.isEmpty())
    {
      select(false);
      mBlockItr.remove();
      if (!mBlockItr.hasNext() && mBlockItr.hasPrevious())
        mBlockItr.previous();
      select(true);
      return true;
    }
    return false;
  }

  public Shape getPath()
  {
    AffineTransform t = new AffineTransform();

    GeneralPath gp = new GeneralPath();
    for (IBlock block: getBlocks())
    {
      gp.append(t.createTransformedShape(block.getPath()), true);
      t.concatenate(block.getBlockTransform());
    }
    
    return gp;
  }

  public void nextBlock()
  {
    if (mBlockItr.hasNext())
    {
      select(false);
      mBlockItr.next();
      if (!mBlockItr.hasNext())
        mBlockItr.previous();
      select(true);
    }
  }

  public void previouseBlock()
  {
    if (mBlockItr.hasPrevious())
    {
      select(false);
      mBlockItr.previous();
      select(true);
    }
  }
}
