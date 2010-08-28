package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.util.ISelectableList;
import com.orbswarm.swarmcon.util.SelectableList;
import com.orbswarm.swarmcon.view.ARenderable;

@XmlRootElement(name = "blockpath")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlockPath extends ARenderable implements IBlockPath
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPath.class);

  @XmlElement(name = "blocks")
  private ISelectableList<IBlock> mBlocksHolder;

  public BlockPath()
  {
    mBlocksHolder = new SelectableList<IBlock>();
  }

  public BlockPath(IBlock... blocks)
  {
    this();
    for (IBlock block : blocks)
      addAfter(block);
  }

  public Collection<IBlock> getBlocks()
  {
    return mBlocksHolder.getAll();
  }

  public void addBefore(IBlock... blocks)
  {
    mBlocksHolder.addAfter(blocks);
  }

  public void addAfter(IBlock... blocks)
  {
    mBlocksHolder.addAfter(blocks);
  }

  public IBlock getCurrentBlock()
  {
    return mBlocksHolder.getCurrent();
  }

  public void replace(IBlock block)
  {
    mBlocksHolder.replace(block);
  }

  public int size()
  {
    return mBlocksHolder.size();
  }

  public boolean remove()
  {
    return mBlocksHolder.remove();
  }

  public Shape getPath()
  {
    AffineTransform t = new AffineTransform();

    GeneralPath gp = new GeneralPath();
    for (IBlock block : getBlocks())
    {
      gp.append(t.createTransformedShape(block.getPath()), true);
      t.concatenate(block.getBlockTransform());
    }

    return gp;
  }

  public Rectangle2D getBounds2D()
  {
    return getPath().getBounds2D();
  }

  @Override
  public void setSuppressed(boolean suppressed)
  {
    for (IBlock block: getBlocks())
      block.setSuppressed(suppressed);
    super.setSuppressed(suppressed);
  }

  public void nextBlock()
  {
    mBlocksHolder.next();
  }

  public void previouseBlock()
  {
    mBlocksHolder.previouse();
  }

  public String toString()
  {
    return "BlockPath [mBlocks=" + mBlocksHolder.getAll() + "]";
  }
}
