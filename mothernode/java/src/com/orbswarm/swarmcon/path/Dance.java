package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.trebor.util.Angle;

import com.orbswarm.swarmcon.vobject.AVobject;

@XmlRootElement(name="dance")
public class Dance extends AVobject implements IDance
{
  @XmlElement(name="paths")
  private final Collection<IBlockPath> mPaths;
  @XmlElement(name="seperation")
  private double mSeperation;
  @XmlElement(name="layout")
  private Layout mLayout;
  
  public enum Layout
  {
    CIRLCE
    {
      public void layout(Dance dance)
      {
        double circumference = dance.mPaths.size() * dance.getSeperation();
        double radius = circumference / (2 * Math.PI);
        Angle theta = new Angle();
        Angle dTheta =
          new Angle(360 / dance.mPaths.size(), Angle.Type.HEADING_RATE);

        for (IBlockPath bp : dance.mPaths)
        {
          bp.setPosition(theta.cartesian(radius));
          bp.setHeading(theta.rotate(dance.getHeading()));
          theta = theta.rotate(dTheta);
        }
      }
    },

    LINE
    {
      public void layout(Dance dance)
      {
        double length = (dance.mPaths.size() - 1) * dance.getSeperation();
        double x = -length / 2;
        for (IBlockPath bp : dance.mPaths)
        {
          bp.setPosition(x, 0);
          x += dance.getSeperation();
        }
      }
    };

    abstract void layout(Dance dance);
  }
  
  public Dance()
  {
    this(Layout.LINE, 2);
//    this(Layout.CIRLCE, 1);
  }
  
  public Dance(Layout layout, double seperation)
  {
    mSeperation = seperation;
    mLayout = layout;
    mPaths = new HashSet<IBlockPath>();
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#add(com.orbswarm.swarmcon.path.IBlockPath)
   */
  public void add(IBlockPath path)
  {
    mPaths.add(path);
    mLayout.layout(this);
  }
  
  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#remove(com.orbswarm.swarmcon.path.IBlockPath)
   */
  
  public boolean remove(IBlockPath path)
  {
    boolean result = mPaths.remove(path);
    mLayout.layout(this);
    return result;
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#getPaths()
   */
  public Collection<IBlockPath> getPaths()
  {
    return Collections.unmodifiableCollection(mPaths);
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#setLayout(com.orbswarm.swarmcon.path.Dance.Layout)
   */
  public void setLayout(Layout layout)
  {
    mLayout = layout;
    mLayout.layout(this);
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#getLayout()
   */
  public Layout getLayout()
  {
    return mLayout;
  }

  public void setHeading(Angle heading)
  {
    super.setHeading(heading);
    mLayout.layout(this);
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#setDistance(double)
   */
  public void setSeperation(double distance)
  {
    mSeperation = distance;
    mLayout.layout(this);
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#getDistance()
   */
  public double getSeperation()
  {
    return mSeperation;
  }

  public Shape getPath()
  {
    GeneralPath gp = new GeneralPath();
    for (IBlockPath bp: mPaths)
    {
      AffineTransform t = getTransform();
      t.concatenate(bp.getTransform());
      gp.append(t.createTransformedShape(bp.getPath()), false);
    }
    
    return gp;
  }
  
  public Rectangle2D getBounds()
  {
    return getPath().getBounds2D();
  }

  public IBlockPath previouse(IBlockPath currentPath)
  {
    IBlockPath result = null;

    for (IBlockPath path : mPaths)
    {
      if (path == currentPath)
      {
        if (result != null)
          break;
      }
      result = path;
    }

    return result;
  }
  
  public IBlockPath next(IBlockPath currentPath)
  {
    IBlockPath result = null;
    IBlockPath first = null;

    Iterator<IBlockPath> paths = mPaths.iterator();

    while (paths.hasNext())
    {
      result = paths.next();
      if (first == null)
        first = result;

      if (result == currentPath)
      {
        if (paths.hasNext())
          result = paths.next();
        else
          result = first;
        break;
      }
    }
    
    return result;
  }
}
