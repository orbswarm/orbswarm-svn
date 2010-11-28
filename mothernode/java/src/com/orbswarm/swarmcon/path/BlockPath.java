package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.PathTool;
import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.util.ISelectableList;
import com.orbswarm.swarmcon.util.SelectableList;
import com.orbswarm.swarmcon.view.APositionable;

@XmlRootElement(name = "blockpath")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlockPath extends APositionable implements IBlockPath
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
    setBlocksHolder(new SelectableList<IBlock>());
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
  
  public List<IBlock> getBlocks()
  {
    return getBlocksHolder().getAll();
  }

  public void addBefore(IBlock... blocks)
  {
    getBlocksHolder().addAfter(blocks);
    invalidatePath();
  }

  public void addAfter(IBlock... blocks)
  {
    getBlocksHolder().addAfter(blocks);
    invalidatePath();
  }

  public IBlock getCurrentBlock()
  {
    return getBlocksHolder().getCurrent();
  }

  public void replace(IBlock block)
  {
    getBlocksHolder().replace(block);
    invalidatePath();
  }

  public int size()
  {
    return getBlocksHolder().size();
  }

  public boolean remove()
  {
    boolean result = getBlocksHolder().remove();
    invalidatePath();
    return result;
  }

  public GeneralPath getPath()
  {
    if (null == mPath)
    {
      mPath = new GeneralPath();
      AffineTransform t = new AffineTransform();
      for (IBlock block : getBlocks())
      {
        Shape shape = t.createTransformedShape(block.getPath());
        mPath.append(shape, true);
        t.concatenate(block.getBlockTransform());
      }
    }
    return mPath;
  }

  protected void invalidatePath()
  {
    mPathTool = null;
    mPath = null;
  }
  
  public PathTool getPathTool()
  {
    if (null == mPathTool)
      mPathTool = new PathTool(getPath(), 0);

    return mPathTool;
  }
  
  public Rectangle2D getBounds2D()
  {
    if (getBlocksHolder().isEmpty())
      return new Rectangle2D.Double(getX(), getY(), 0, 0);
    return getPath().getBounds2D();
  }

  @XmlTransient
  @Override
  public void setSuppressed(boolean suppressed)
  {
    for (IBlock block: getBlocks())
      block.setSuppressed(suppressed);
    super.setSuppressed(suppressed);
  }

  public void nextBlock()
  {
    getBlocksHolder().next();
  }

  public void previouseBlock()
  {
    getBlocksHolder().previouse();
  }

  public void firstBlock()
  {
    getBlocksHolder().first();
  }

  public void lastBlock()
  {
    getBlocksHolder().last();
  }

  public String toString()
  {
    return "BlockPath [mBlocks=" + getBlocksHolder().getAll() + "]";
  }

  public Angle getFinalAngle()
  {
    return getEndPoint().getAngle();
  }

  public Point2D getFinalPosition()
  {
    return getEndPoint();
  }

  @Override
  public IBlockPath clone() throws CloneNotSupportedException
  {
    BlockPath other = (BlockPath)super.clone();
    other.setBlocksHolder(getBlocksHolder().clone());
    
    log.debug("holder 1: " + getBlocksHolder());
    log.debug("holder 2: " + other.getBlocksHolder());
    
    return other;
  }

  public double getLength()
  {
    return getPathTool().getLength();
  }

  public PathPoint getStartPoint()
  {
    return getPathTool().getStartPoint();
  }

  public PathPoint getPathPoint(double extent)
  {
    return getPathTool().getPathPoint(extent);
  }

  public PathPoint getEndPoint()
  {
    return getPathTool().getEndPoint();
  }

  public void setBlocksHolder(ISelectableList<IBlock> blocksHolder)
  {
    mBlocksHolder = blocksHolder;
    invalidatePath();
  }

  @XmlElement(name = "blocks")
  public ISelectableList<IBlock> getBlocksHolder()
  {
    return mBlocksHolder;
  }
}
