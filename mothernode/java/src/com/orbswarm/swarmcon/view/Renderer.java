package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Phantom;
import com.orbswarm.swarmcon.path.Head;
import com.orbswarm.swarmcon.path.IBlock;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.vobject.IVobject;
import com.orbswarm.swarmcon.vobject.IVobjects;

public class Renderer
{
  private static Logger log = Logger.getLogger(Renderer.class);

  // an ordered list of keys used by mRendererClassMap to permit control of
  // which renderers are match to vobjects first
  
  private static Vector<Class<? extends IVobject>> mKeyOrder =
    new Vector<Class<? extends IVobject>>();

  // the comparator used to control renderer selection order
  
  private static Comparator<Class<? extends IVobject>> mRendererComparator =
    new Comparator<Class<? extends IVobject>>()
    {
      public int compare(Class<? extends IVobject> o1,
        Class<? extends IVobject> o2)
      {
        return mKeyOrder.indexOf(o1) - mKeyOrder.indexOf(o2);
      }
    };

  // a map of vobjects to renderers
    
  private static Map<Class<? extends IVobject>, Class<? extends IRenderer<? extends IVobject>>> mRendererClassMap =
    new TreeMap<Class<? extends IVobject>, Class<? extends IRenderer<? extends IVobject>>>(
      mRendererComparator)
    {
      private static final long serialVersionUID = -2767952369723326950L;

      // add your renderer here, the order in which they appear in this
      // list is the order in which they are tested, so put the more
      // specific renderers earlier in the list 
      
      {
        //put(IBlockPath.class, BlockPathRenderer.class);        
        put(Phantom.class, PhantomRenderer.class);
        put(MouseMobject.class, MouseMobjectRenderer.class);
        put(SmoothPath.class, SmoothPathRenderer.class);
        put(Head.class, HeadRenderer.class);
        put(IVobjects.class, MobjectsRenderer.class);
        put(IBlock.class, BlockRenderer.class);
        put(IOrb.class, OrbRenderer.class);
      }

      // override of put to capture key order
      
      @Override
      public Class<? extends IRenderer<? extends IVobject>> put(
        Class<? extends IVobject> key,
        Class<? extends IRenderer<? extends IVobject>> value)
      {
        mKeyOrder.add(key);
        return super.put(key, value);
      }

    };

  private static Map<Class<? extends IVobject>, IRenderer<?>> mRendererInstanceMap =
    new HashMap<Class<? extends IVobject>, IRenderer<? extends IVobject>>();

  public static <Type extends IVobject> void render(Graphics2D g, Type vobject)
  {
    log.debug("render for: " + vobject.getClass().getSimpleName());
    getRenderer(vobject).render(g, vobject);
  }

  public static void renderAsPhantom(Graphics2D g, IVobject mobject,
    double phantomAlpha)
  {
    getRenderer(mobject).renderAsPhantom(g, mobject, phantomAlpha);
  }

  /**
   * Get the {@link IRenderer} for a specific type of {@link IVobject}.
   * 
   * @param <Type>
   * @param mobject
   * @return
   */

  @SuppressWarnings("unchecked")
  public static <Type extends IVobject> IRenderer<Type> getRenderer(
    Type mobject)
  {
    for (Entry<Class<? extends IVobject>, Class<? extends IRenderer<? extends IVobject>>> entry : mRendererClassMap
      .entrySet())
    {
      log.debug("checking: " + entry);
      if (entry.getKey().isInstance(mobject))
        return getRenderer((Class<Type>)entry.getKey(),
          (Class<IRenderer<Type>>)entry.getValue());
    }

    throw new Error("no renderer found for " +
      mobject.getClass().getSimpleName());
  }

  @SuppressWarnings("unchecked")
  private static <Type extends IVobject> IRenderer<Type> getRenderer(
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
   * Establish this {@link IVobject} is selected by clicking at
   * selectionPoint. A {@link IVobject} is selected if the {@link Shape}
   * returned by {@link #getShape(IVobject)}
   * {@link Shape#contains(Point2D)} selectionPoint, and the
   * {@link IVobject} is the closest to the selectionPoint.
   * 
   * @param selectionPoint the point to select from
   * @param o the object to search for candidate {@link IVobject}s.
   * @return the selected {@link IVobject} or null if no valid candidates
   *         exist.
   */

  public static IVobject getSelected(Point2D selectionPoint, IVobject o)
  {
    return getRenderer(o).getSelected(selectionPoint, o);
  }
}
