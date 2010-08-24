package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import com.orbswarm.swarmcon.vobject.AVobject;

@XmlRootElement(name="dance")
public class Dance extends AVobject implements IDance
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Dance.class);
  @XmlElement(name="paths")
  private final Collection<IBlockPath> mPaths;
  @XmlElement(name="seperation")
  private double mSeperation;
  @XmlElement(name="layout")
  private Layout mLayout;
  @XmlTransient
  private IBlockPath mCurrentPath;
  
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
    mPaths = new Vector<IBlockPath>();
  }

  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#add(com.orbswarm.swarmcon.path.IBlockPath)
   */
  public void add(IBlockPath path)
  {
    if (null == path)
      throw new IllegalArgumentException();
    
    mPaths.add(path);
    setCurrentPath(path);
    mLayout.layout(this);
  }
  
  /* (non-Javadoc)
   * @see com.orbswarm.swarmcon.path.IDance#remove(com.orbswarm.swarmcon.path.IBlockPath)
   */
  
  public boolean remove()
  {
    if (mCurrentPath != null)
    {
      IBlockPath target = mCurrentPath;
      nextPath();
      boolean result = mPaths.remove(target);
      mLayout.layout(this);
      return result;
    }
    
    return false;
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

  public boolean nextPath()
  {
    log.debug("next path " + mCurrentPath);
    ListIterator<IBlockPath> paths =
      ((Vector<IBlockPath>)mPaths).listIterator();

    while (paths.hasNext())
      if (paths.next() == getCurrentPath())
        return setCurrentPath(paths.hasNext()
          ? paths.next()
          : ((Vector<IBlockPath>)mPaths).firstElement());

    return false;
  }

  public boolean previousePath()
  {
    log.debug("prev path " + mCurrentPath);
    ListIterator<IBlockPath> paths =
      ((Vector<IBlockPath>)mPaths).listIterator();

    while (paths.hasNext())
      if (paths.next() == getCurrentPath())
      {
        paths.previous();
        return setCurrentPath(paths.hasPrevious()
          ? paths.previous()
          : ((Vector<IBlockPath>)mPaths).lastElement());
      }
    
    return false;
  }
  
  private boolean setCurrentPath(IBlockPath path)
  {
    if (mCurrentPath == path)
      return false;
    
    if (null != mCurrentPath)
      mCurrentPath.setSelected(false);
    mCurrentPath = path;
    if (null != mCurrentPath)
      mCurrentPath.setSelected(true);
    return true;
  }

  public IBlockPath getCurrentPath()
  {
    return mCurrentPath;
  }
}
