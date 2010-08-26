package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Phantom;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;

public class RendererSet
{
  private static Logger log = Logger.getLogger(RendererSet.class);

  // an ordered list of keys used by mRendererClassMap to permit control of
  // which renderers are match to vobjects first
  
  private static Vector<Class<? extends IRenderable>> mKeyOrder =
    new Vector<Class<? extends IRenderable>>();

  // the comparator used to control renderer selection order
  
  private static Comparator<Class<? extends IRenderable>> mRendererComparator =
    new Comparator<Class<? extends IRenderable>>()
    {
      public int compare(Class<? extends IRenderable> o1,
        Class<? extends IRenderable> o2)
      {
        return mKeyOrder.indexOf(o1) - mKeyOrder.indexOf(o2);
      }
    };

  // a map of vobjects to renderers
    
  private static Map<Class<? extends IRenderable>, Class<? extends IRenderer<? extends IRenderable>>> mRendererClassMap =
    new TreeMap<Class<? extends IRenderable>, Class<? extends IRenderer<? extends IRenderable>>>(
      mRendererComparator)
    {
      private static final long serialVersionUID = -2767952369723326950L;

      // add your renderer here, the order in which they appear in this
      // list is the order in which they are tested, so put the more
      // specific renderers earlier in the list 
      
      {
        put(IDance.class, DanceRenderer.class);        
        put(IBlockPath.class, BlockPathRenderer.class);        
        put(Phantom.class, PhantomRenderer.class);
        put(MouseMobject.class, MouseMobjectRenderer.class);
        put(SmoothPath.class, SmoothPathRenderer.class);
        put(IRenderables.class, RenderablesRenderer.class);
        put(IOrb.class, OrbRenderer.class);
      }

      // override of put to capture key order
      
      @Override
      public Class<? extends IRenderer<? extends IRenderable>> put(
        Class<? extends IRenderable> key,
        Class<? extends IRenderer<? extends IRenderable>> value)
      {
        mKeyOrder.add(key);
        return super.put(key, value);
      }

    };

  private static Map<Class<? extends IRenderable>, IRenderer<?>> mRendererInstanceMap =
    new HashMap<Class<? extends IRenderable>, IRenderer<? extends IRenderable>>();

  public static <Type extends IRenderable> void render(Graphics2D g, Type vobject)
  {
    AffineTransform t = g.getTransform();
    getRenderer(vobject).render(g, vobject);
    g.setTransform(t);
  }

  public static void renderAsPhantom(Graphics2D g, IRenderable mobject,
    double phantomAlpha)
  {
    getRenderer(mobject).renderAsPhantom(g, mobject, phantomAlpha);
  }

  /**
   * Get the {@link IRenderer} for a specific type of {@link IRenderable}.
   * 
   * @param <Type>
   * @param mobject
   * @return
   */

  @SuppressWarnings("unchecked")
  public static <Type extends IRenderable> IRenderer<Type> getRenderer(
    Type mobject)
  {
    for (Entry<Class<? extends IRenderable>, Class<? extends IRenderer<? extends IRenderable>>> entry : mRendererClassMap
      .entrySet())
    {
      if (entry.getKey().isInstance(mobject))
        return getRenderer((Class<Type>)entry.getKey(),
          (Class<IRenderer<Type>>)entry.getValue());
    }

    throw new Error("no renderer found for " +
      mobject.getClass().getSimpleName());
  }

  @SuppressWarnings("unchecked")
  private static <Type extends IRenderable> IRenderer<Type> getRenderer(
    Class<Type> mobjectType, Class<IRenderer<Type>> rendererType)
  {
    IRenderer<Type> renderer =
      (IRenderer<Type>)mRendererInstanceMap.get(mobjectType);

    if (renderer == null)
    {
      try
      {
        renderer = (IRenderer<Type>)rendererType.newInstance();
        mRendererInstanceMap.put(mobjectType, renderer);
      }
      catch (InstantiationException e)
      {
        log.error(e);
        e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
        log.error(e);
        e.printStackTrace();
      }
    }

    return renderer;
  }

  /**
   * Establish this {@link IRenderable} is selected by clicking at
   * selectionPoint. A {@link IRenderable} is selected if the {@link Shape}
   * returned by {@link #getShape(IRenderable)}
   * {@link Shape#contains(Point2D)} selectionPoint, and the
   * {@link IRenderable} is the closest to the selectionPoint.
   * 
   * @param selectionPoint the point to select from
   * @param o the object to search for candidate {@link IRenderable}s.
   * @return the selected {@link IRenderable} or null if no valid candidates
   *         exist.
   */

  public static IRenderable getSelected(Point2D selectionPoint, IRenderable o)
  {
    return getRenderer(o).getSelected(selectionPoint, o);
  }
  
  public static <Type extends IRenderable> Shape getShape(Type vobject)
  {
    return getRenderer(vobject).getShape(vobject);
  }
}
