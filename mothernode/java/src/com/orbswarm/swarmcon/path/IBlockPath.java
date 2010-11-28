package com.orbswarm.swarmcon.path;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.trebor.util.Angle;
import org.trebor.util.PathTool;
import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.store.IBlockPathAdapter;
import com.orbswarm.swarmcon.view.IPositionable;
import com.orbswarm.swarmcon.view.IRenderable;

@XmlJavaTypeAdapter(IBlockPathAdapter.class)
public interface IBlockPath extends IPositionable, IRenderable
{
// heading getter

Angle getHeading();

// position getter

Point2D getPosition();

// get x position

double getX();

// get y position

double getY();

// position setter

void setPosition(Point2D position);

// position setter

void setPosition(double x, double y);

// set the heading

void setHeading(Angle heading);

/**
 * Get the transform for this vobject.
 * 
 * @return this VObjects transform.
 */

AffineTransform getTransform();


  double getLength();
  
  PathPoint getStartPoint();

  PathPoint getPathPoint(double extent);
  
  PathPoint getEndPoint();
  
  GeneralPath getPath();
  
  Rectangle2D getBounds2D();
  
  void addBefore(IBlock... blocks);

  void addAfter(IBlock... blocks);

  void replace(IBlock block);
  
  int size();

  Collection<IBlock> getBlocks();
  
  boolean remove();
  
  void nextBlock();
  
  void previouseBlock();

  void firstBlock();
  
  void lastBlock();
  
  IBlock getCurrentBlock();
  
  IBlockPath clone() throws CloneNotSupportedException;

  PathTool getPathTool();
}
