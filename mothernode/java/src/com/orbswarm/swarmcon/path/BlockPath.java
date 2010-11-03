package com.orbswarm.swarmcon.path;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.PathTool;

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

  @XmlTransient
  private PathTool mPathTool;

  @XmlTransient
  private GeneralPath mPath;

  public BlockPath()
  {
    mBlocksHolder = new SelectableList<IBlock>();
    updatePath();
  }

  public static void main(String[] args)
  {
    try
    {
      IBlockPath path1 = new BlockPath(new CurveBlock(), new CurveBlock(), new CurveBlock());
      IBlockPath path2 = path1.clone();
      path1 = path2;
    }
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
    }
  }
  
  public BlockPath(IBlock... blocks)
  {
    this();
    try
    {
      for (IBlock block : blocks)
        addAfter(block.clone());
    }
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
    }
  }

  public BlockPath(IBlockPath other)
  {
    this();
    try
    {
      for (IBlock block : other.getBlocks())
        addAfter(block.clone());
    }
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
    }
  }
  
  public Collection<IBlock> getBlocks()
  {
    return mBlocksHolder.getAll();
  }

  public void addBefore(IBlock... blocks)
  {
    mBlocksHolder.addAfter(blocks);
    updatePath();
  }

  public void addAfter(IBlock... blocks)
  {
    mBlocksHolder.addAfter(blocks);
    updatePath();
  }

  public IBlock getCurrentBlock()
  {
    return mBlocksHolder.getCurrent();
  }

  public void replace(IBlock block)
  {
    mBlocksHolder.replace(block);
    updatePath();
  }

  public int size()
  {
    return mBlocksHolder.size();
  }

  public boolean remove()
  {
    boolean result = mBlocksHolder.remove();
    updatePath();
    return result;
  }

  public GeneralPath getPath()
  {
    return mPath;
  }

  private void updatePath()
  {
    AffineTransform t = new AffineTransform();

    mPath = new GeneralPath();
    for (IBlock block : getBlocks())
    {
      mPath.append(t.createTransformedShape(block.getPath()), true);
      t.concatenate(block.getBlockTransform());
    }
    mPathTool = new PathTool(mPath, 0);
  }
  
  public Rectangle2D getBounds2D()
  {
    if (mBlocksHolder.isEmpty())
      return new Rectangle2D.Double(getX(), getY(), 0, 0);
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

  public void firstBlock()
  {
    mBlocksHolder.first();
  }

  public void lastBlock()
  {
    mBlocksHolder.last();
  }

  public String toString()
  {
    return "BlockPath [mBlocks=" + mBlocksHolder.getAll() + "]";
  }

  public Angle getFinalAngle()
  {
    return mPathTool.getEndPoint().getAngle();
  }

  public Point2D getFinalPosition()
  {
    return mPathTool.getEndPoint();
  }

  @Override
  public IBlockPath clone() throws CloneNotSupportedException
  {
    BlockPath other = (BlockPath)super.clone();
    other.mBlocksHolder = mBlocksHolder.clone();
    
    log.debug("holder 1: " + mBlocksHolder);
    log.debug("holder 2: " + other.mBlocksHolder);
    
    return other;
  }
}
