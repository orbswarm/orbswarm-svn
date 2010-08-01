package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.SwarmCon.MouseMobject;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Phantom;
import com.orbswarm.swarmcon.vobject.IVobject;
import com.orbswarm.swarmcon.vobject.IVobjects;

public class Renderer
{
  private static Logger log = Logger.getLogger(OrbRenderer.class);

  private static Class<?> renderClasses[][] =
  {
    {
      IOrb.class, OrbRenderer.class
    },
    {
      Phantom.class, PhantomRenderer.class
    },
    {
      MouseMobject.class, MouseMobjectRenderer.class
    },
    {
      IVobjects.class, MobjectsRenderer.class
    },
  };

  private static Map<Class<?>, IRenderer<?>> mRendererInstanceMap = new HashMap<Class<?>, IRenderer<?>>();

  public static void render(Graphics2D g, IVobject mobject)
  {
    getRenderer(mobject).render(g, mobject);
  }

  public static void renderAsPhantom(Graphics2D g, IVobject mobject,
    double phantomAlpha)
  {
    getRenderer(mobject).renderAsPhantom(g, mobject, phantomAlpha);
  }

  public static IRenderer<?> getRenderer(IVobject mobject)
  {
    for (Class<?>[] map : renderClasses)
      if (map[0].isInstance(mobject))
        return getRenderer(map[0], map[1]);

    throw new Error("no renderer found for " + mobject.getClass().getSimpleName());
  }

  private static IRenderer<?> getRenderer(Class<?> mobjectType,
    Class<?> rendererType)
  {
    IRenderer<?> renderer = mRendererInstanceMap.get(mobjectType);

    if (renderer == null)
    {
      try
      {
        renderer = (IRenderer<?>)rendererType.newInstance();
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
