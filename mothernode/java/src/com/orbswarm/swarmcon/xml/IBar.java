/**
 * 
 */
package com.orbswarm.swarmcon.xml;

import java.awt.geom.Point2D;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.BlockPathBuilder.IBarAdapter;

@XmlJavaTypeAdapter(IBarAdapter.class)
public interface IBar extends IBlock
{
  public String getName();

  public Point2D getPos();
}