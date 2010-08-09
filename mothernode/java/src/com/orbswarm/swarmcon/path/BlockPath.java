package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.Vector;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import com.orbswarm.swarmcon.vobject.AVobject;

@XmlRootElement
public class BlockPath extends AVobject implements IBlockPath
{
  private static final long serialVersionUID = -8911696643772151060L;
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPath.class);

  private Vector<IBlock> mBlocks;
  private Angle mHeading;

  ABlock x;
  
  public BlockPath()
  {
    mBlocks = new Vector<IBlock>();
    mHeading = new Angle();
  }

  public void setBlocks(Vector<IBlock> blocks)
  {
    mBlocks = blocks;
  }

  public Vector<IBlock> getBlocks()
  {
    return mBlocks;
  }

  public void add(IBlock block)
  {
    mBlocks.add(block);
  }

  public IBlock lastElement()
  {
    return mBlocks.lastElement();
  }

  public int size()
  {
    return mBlocks.size();
  }

  public void removeElement(IBlock block)
  {
    mBlocks.removeElement(block);
  }

  public void setHeading(Angle heading)
  {
    mHeading = heading;
  }

  public Angle getHeading()
  {
    return mHeading;
  }
  
  public Shape getPath()
  {
    BlockState state = getState();
    
    GeneralPath gp = new GeneralPath();
    gp.moveTo(state.getX(), state.getY());
    
    for (IBlock block: getBlocks())
    {
      gp.append(block.getPath(state), true);
      state = state.add(block.getDeltaState());
    }
    
    return gp;
  }

  public BlockState getState()
  {
    return new BlockState(getHeading(), getPosition());
  }

  public void update(double time)
  {
  }
}
