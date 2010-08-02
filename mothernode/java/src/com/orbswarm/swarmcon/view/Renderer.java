package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Phantom;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.vobject.IVobject;
import com.orbswarm.swarmcon.vobject.IVobjects;

public class Renderer
{
  private static Logger log = Logger.getLogger(Renderer.class);


  private static Map<Class<? extends IVobject>, Class<? extends IRenderer<? extends IVobject>>> mRendererClassMap = 
    new HashMap<Class<? extends IVobject>, Class<? extends IRenderer<? extends IVobject>>>()
  {
    private static final long serialVersionUID = -2767952369723326950L;

    {
      put(IOrb.class, OrbRenderer.class);
      put(Phantom.class, PhantomRenderer.class);
      put(MouseMobject.class, MouseMobjectRenderer.class);
      put(SmoothPath.class, SmoothPathRenderer.class);
      put(IVobjects.class, MobjectsRenderer.class);
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
    IRenderer<Type> renderer = (IRenderer<Type>)mRendererInstanceMap.get(mobjectType);

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
}
