package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import com.orbswarm.swarmcon.util.ISelectableList;
import com.orbswarm.swarmcon.util.SelectableList;
import com.orbswarm.swarmcon.view.ARenderable;

@XmlRootElement(name="dance")
@XmlAccessorType(XmlAccessType.FIELD)
public class Dance extends ARenderable implements IDance
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Dance.class);
  
  @XmlElement(name = "paths")
  private ISelectableList<IBlockPath> mPathsHolder;
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
        double circumference = dance.size() * dance.getSeperation();
        double radius = circumference / (2 * Math.PI);
        Angle theta = new Angle();
        Angle dTheta =
          new Angle(360 / dance.size(), Angle.Type.HEADING_RATE);

        for (IBlockPath bp : dance.getPaths())
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
        double length = (dance.size() - 1) * dance.getSeperation();
        double x = -length / 2;
        for (IBlockPath bp : dance.getPaths())
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
  }
  
  public Dance(Layout layout, double seperation)
  {
    mSeperation = seperation;
    mLayout = layout;
    log.debug("creat PathsHolder");
    mPathsHolder = new SelectableList<IBlockPath>(true);
  }

  public void setLayout(Layout layout)
  {
    mLayout = layout;
    mLayout.layout(this);
  }

  public Layout getLayout()
  {
    return mLayout;
  }

  public void setHeading(Angle heading)
  {
    super.setHeading(heading);
    layout();
  }

  public void setSeperation(double distance)
  {
    mSeperation = distance;
    layout();
  }

  public double getSeperation()
  {
    return mSeperation;
  }

  @Override
  public void setSuppressed(boolean suppressed)
  {
    for (IBlockPath path: getPaths())
      path.setSuppressed(suppressed);
    super.setSuppressed(suppressed);
  }

  public Rectangle2D getBounds2D()
  {
    return getPath().getBounds2D();
  }

  public void layout()
  {
    mLayout.layout(this);
  }
  
  public void addBefore(IBlockPath... paths)
  {
    mPathsHolder.addBefore(paths);
    layout();
  }

  public void addAfter(IBlockPath... paths)
  {
    mPathsHolder.addAfter(paths);
    layout();
  }

  public IBlockPath getCurrentPath()
  {
    return mPathsHolder.getCurrent();
  }

  public void replace(IBlockPath path)
  {
    mPathsHolder.replace(path);
    layout();
  }

  public int size()
  {
    return mPathsHolder.size();
  }

  public boolean remove()
  {
    boolean result = mPathsHolder.remove();
    layout();
    return result;
  }

  public Shape getPath()
  {
    GeneralPath gp = new GeneralPath();
    for (IBlockPath bp: getPaths())
    {
      AffineTransform t = getTransform();
      t.concatenate(bp.getTransform());
      gp.append(t.createTransformedShape(bp.getPath()), false);
    }
    
    return gp;
  }
  
  public void nextPath()
  {
    mPathsHolder.next();
  }

  public void previousePath()
  {
    mPathsHolder.previouse();

  }

  public Collection<IBlockPath> getPaths()
  {
    return Collections.unmodifiableCollection(mPathsHolder.getAll());
  }
}
