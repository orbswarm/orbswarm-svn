package com.orbswarm.swarmcon.path;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;

import com.orbswarm.swarmcon.util.ISelectableList;
import com.orbswarm.swarmcon.util.SelectableList;
import com.orbswarm.swarmcon.view.APositionable;

@XmlRootElement(name="dance")
@XmlAccessorType(XmlAccessType.FIELD)
public class Dance extends APositionable implements IDance
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Dance.class);
  
  @XmlElement(name = "paths")
  private ISelectableList<IBlockPath> mPathsHolder;
  @XmlElement(name="seperation")
  private double mSeperation;
  @XmlElement(name="layout")
  private Layout mLayout;
  @XmlElement(name="markers")
  private MarkerHolder mMarkers;

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
          bp.setHeading(theta);
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
          bp.setHeading(new Angle(0, Angle.Type.HEADING));
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
    mPathsHolder = new SelectableList<IBlockPath>(true);
    mMarkers = new MarkerHolder();
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
    Rectangle2D bounds = new Rectangle2D.Double();
    for (IBlockPath bp: getPaths())
    {
      AffineTransform t = getTransform();
      t.concatenate(bp.getTransform());
      bounds.add(t.createTransformedShape(bp.getBounds2D()).getBounds2D());
    }
      
    return bounds;
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

  public List<IBlockPath> getPaths()
  {
    return Collections.unmodifiableList(mPathsHolder.getAll());
  }

  public void add(IMarker marker)
  {
    mMarkers.add(marker);
  }
  
  public List<IMarker> getMarkers()
  {
    return mMarkers.getAll();
  }
  
  @Override
  public IDance clone()
  {
    Dance other = null;
    try
    {
      other = (Dance)super.clone();
      other.mPathsHolder = mPathsHolder.clone();
      other.mSeperation = mSeperation;
      other.mLayout = mLayout;
    }
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
    }
    
    return other;
  }
}
