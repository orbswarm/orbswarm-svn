package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import com.orbswarm.swarmcon.store.INamed;
import com.orbswarm.swarmcon.vobject.AVobject;

@XmlRootElement(name="blockpath")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlockPath extends AVobject implements IBlockPath, INamed
{
  private static final long serialVersionUID = -8911696643772151060L;
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(BlockPath.class);

  @XmlElementWrapper(name="blocks")
  @XmlAnyElement
  private Collection<IBlock> mBlocks;
  @XmlElement(name="heading")
  private Angle mHeading;
  @XmlElement(name="name")
  private String mName;
  
  public BlockPath()
  {
    mBlocks = new Vector<IBlock>();
    mHeading = new Angle();
    mName = "earl";
  }

  public BlockPath(IBlock... blocks)
  {
    this();
    add(blocks);
  }

  public void setBlocks(Vector<IBlock> blocks)
  {
    mBlocks = blocks;
  }

  @XmlTransient
  public Collection<IBlock> getBlocks()
  {
    return mBlocks;
  }

  public void add(IBlock... blocks)
  {
    for (IBlock block : blocks)
      mBlocks.add(block);
  }
  
  public void add(IBlock block)
  {
    mBlocks.add(block);
  }

  public IBlock lastElement()
  {
    return ((Vector<IBlock>)mBlocks).lastElement();
  }
  
  public int size()
  {
    return mBlocks.size();
  }

  public void removeElement(IBlock block)
  {
    ((Vector<IBlock>)mBlocks).removeElement(block);
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
    BlockState state = new BlockState();
    
    GeneralPath gp = new GeneralPath();
    gp.moveTo(state.getX(), state.getY());
    
    for (IBlock block: getBlocks())
    {
      gp.append(block.getPath(state), true);
      state = state.add(block.getDeltaState());
    }
    
    return gp;
  }

  public void update(double time)
  {
  }

  public String getName()
  {
    return mName;
  }
}
